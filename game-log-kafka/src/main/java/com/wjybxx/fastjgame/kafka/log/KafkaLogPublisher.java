/*
 *  Copyright 2019 wjybxx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to iBn writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wjybxx.fastjgame.kafka.log;

import com.wjybxx.fastjgame.log.core.LogBuilder;
import com.wjybxx.fastjgame.log.core.LogDirector;
import com.wjybxx.fastjgame.log.core.LogPublisher;
import com.wjybxx.fastjgame.log.imp.DefaultLogRecord;
import com.wjybxx.fastjgame.utils.CloseableUtils;
import com.wjybxx.fastjgame.utils.concurrent.RejectedExecutionHandler;
import com.wjybxx.fastjgame.utils.concurrent.disruptor.DisruptorEventLoop;
import com.wjybxx.fastjgame.utils.concurrent.disruptor.SleepWaitStrategyFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;

/**
 * kafka日志生产者线程 - 将应用程序的日志存储到kafka中。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/11/27
 * github - https://github.com/hl845740757
 */
public class KafkaLogPublisher<T extends LogBuilder> extends DisruptorEventLoop implements LogPublisher<T> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaLogPublisher.class);

    /**
     * 日志线程任务缓冲区大小，也不需要太大
     */
    private static final int PRODUCER_RING_BUFFER_SIZE = 64 * 1024;
    private static final int PRODUCER_TASK_BATCH_SIZE = 1024;

    /**
     * 由于游戏打点日志并不是太多，可以将日志总是打在同一个partition下（可以获得全局的顺序性）
     */
    private static final Integer PARTITION_ID = 0;

    /**
     * kafka生产者 - kafka会自动处理网络问题，因此我们不必付出过多精力在上面。
     */
    private final KafkaProducer<String, String> producer;

    private final LogDirector<T, DefaultLogRecord> logDirector;

    public KafkaLogPublisher(@Nonnull ThreadFactory threadFactory,
                             @Nonnull RejectedExecutionHandler rejectedExecutionHandler,
                             @Nonnull String brokerList,
                             @Nonnull LogDirector<T, DefaultLogRecord> logDirector) {
        super(null, threadFactory, rejectedExecutionHandler, PRODUCER_RING_BUFFER_SIZE, PRODUCER_TASK_BATCH_SIZE, new SleepWaitStrategyFactory());
        this.producer = new KafkaProducer<>(newConfig(brokerList), new StringSerializer(), new StringSerializer());
        this.logDirector = logDirector;
    }

    private static Properties newConfig(String brokerList) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        // 不使用"all"机制是为了提高吞吐量。
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 64 * 1024);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 100);
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);
        return properties;
    }

    @Override
    protected void init() throws Exception {
    }

    @Override
    protected void loopOnce() throws Exception {
    }

    @Override
    protected void clean() throws Exception {
        CloseableUtils.closeQuietly(producer);
    }

    @Override
    public void publish(T logBuilder) {
        execute(new KafkaLogTask(logBuilder));
    }

    private class KafkaLogTask implements Runnable {

        private final T builder;

        KafkaLogTask(T builder) {
            this.builder = builder;
        }

        @Override
        public void run() {
            try {
                final DefaultLogRecord logRecordDTO = logDirector.build(builder);
                final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(logRecordDTO.topic(), PARTITION_ID,
                        null, logRecordDTO.data());
                producer.send(producerRecord);
            } catch (Throwable e) {
                logger.warn("publish caught exception, builder {}", builder, e);
            }
        }
    }
}
