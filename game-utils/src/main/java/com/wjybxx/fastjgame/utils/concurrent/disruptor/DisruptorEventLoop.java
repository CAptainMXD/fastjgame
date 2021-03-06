/*
 * Copyright 2019 wjybxx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wjybxx.fastjgame.utils.concurrent.disruptor;

import com.lmax.disruptor.*;
import com.wjybxx.fastjgame.utils.concurrent.*;
import com.wjybxx.fastjgame.utils.timer.TimerSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于Disruptor的事件循环。
 *
 * <p>
 * Q: {@link DisruptorEventLoop}比起{@link SingleThreadEventLoop}，优势在哪？<br>
 * A: 1. {@link DisruptorEventLoop}采用的是无锁队列，性能高于{@link SingleThreadEventLoop}。
 * 2. {@link DisruptorEventLoop}对资源的利用率远胜{@link SingleThreadEventLoop}。
 * <p>
 * Q: 那缺陷在哪呢？
 * A: 1. 最大的缺陷就是它只能是有界的队列。<br>
 * 2. {@link DisruptorEventLoop}涉及很多额外的知识，涉及到Disruptor框架，而且我为了解决它存在的一些问题，又选择了一些自定义的实现，
 * 导致其阅读难度较大，不像{@link SingleThreadEventLoop}那么清晰易懂。<br>
 * 3. 子类不能控制循环逻辑，无法自己决定什么时候调用{@link #loopOnce()}。<br>
 *
 * <p>
 * Q: 哪些事件循环适合使用{@link DisruptorEventLoop} ?<br>
 * A: 如果你的服务处于终端节点，且需要极低的延迟和极高的吞吐量的时候。{@link DisruptorEventLoop}最好不要用在中间节点，超过负载可能死锁！
 *
 * <p>
 * 警告：由于{@link EventLoop}都是单线程的，如果两个{@link EventLoop}都使用有界队列，如果互相通信，如果超过负载，则可能死锁！
 * - eg: 网络层尝试向应用层提交任务，应用层尝试向网络层提交任务。
 * （其实两个EventLoop中只要有一个使用有界队列，且有阻塞式操作，则可能死锁或阻塞较长时间）
 * <b>
 * 而{@link DisruptorEventLoop}使用的就是有界队列。
 * </b>
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/7/24
 * github - https://github.com/hl845740757
 */
public class DisruptorEventLoop extends AbstractEventLoop {

    private static final Logger logger = LoggerFactory.getLogger(DisruptorEventLoop.class);

    /**
     * 默认ringBuffer大小 - 大一点可以减少降低阻塞概率
     * 64 * 1024 个{@link RunnableEvent}对象大概1M。
     * 如果提交的任务较大，那么内存占用可能较大，用户请根据实际情况调整。
     */
    private static final int DEFAULT_RING_BUFFER_SIZE = 64 * 1024;
    /**
     * 批量拉取(执行)任务数 - 该值越小{@link #loopOnce()}执行越频繁，响应关闭请求越快。
     */
    private static final int DEFAULT_BATCH_EVENT_SIZE = 1024;

    // 线程的状态
    /**
     * 初始状态，未启动状态
     */
    private static final int ST_NOT_STARTED = 1;
    /**
     * 运行状态
     */
    private static final int ST_STARTED = 2;
    /**
     * 正在关闭状态
     */
    private static final int ST_SHUTTING_DOWN = 3;
    /**
     * 已关闭状态，正在进行最后的清理
     */
    private static final int ST_SHUTDOWN = 4;
    /**
     * 终止状态(二阶段终止模式 - 已关闭状态下进行最后的清理，然后进入终止状态)
     */
    private static final int ST_TERMINATED = 5;
    /**
     * 工作线程
     */
    private final Thread thread;
    /**
     * 真正执行逻辑的对象
     */
    private final Worker worker;

    /**
     * 事件队列
     */
    private final RingBuffer<RunnableEvent> ringBuffer;
    /**
     * 批量拉取任务的大小
     */
    private final int taskBatchSize;

    /**
     * 线程状态
     */
    private final AtomicInteger stateHolder = new AtomicInteger(ST_NOT_STARTED);

    /**
     * 线程终止future
     */
    private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventLoop.INSTANCE);
    /**
     * 任务拒绝策略
     */
    private final RejectedExecutionHandler rejectedExecutionHandler;

    /**
     * @param parent                   容器节点
     * @param threadFactory            线程工厂
     * @param rejectedExecutionHandler 拒绝策略
     */
    public DisruptorEventLoop(@Nullable EventLoopGroup parent,
                              @Nonnull ThreadFactory threadFactory,
                              @Nonnull RejectedExecutionHandler rejectedExecutionHandler) {
        this(parent, threadFactory, rejectedExecutionHandler,
                DEFAULT_RING_BUFFER_SIZE, DEFAULT_BATCH_EVENT_SIZE,
                new SleepWaitStrategyFactory());
    }

    /**
     * @param parent                   容器节点
     * @param threadFactory            线程工厂
     * @param rejectedExecutionHandler 拒绝策略
     * @param ringBufferSize           环形缓冲区大小
     * @param taskBatchSize            批量拉取(执行)任务数(小于等于0则不限制)
     */
    public DisruptorEventLoop(@Nullable EventLoopGroup parent,
                              @Nonnull ThreadFactory threadFactory,
                              @Nonnull RejectedExecutionHandler rejectedExecutionHandler,
                              int ringBufferSize, int taskBatchSize) {
        this(parent, threadFactory, rejectedExecutionHandler,
                ringBufferSize, taskBatchSize,
                new SleepWaitStrategyFactory());
    }

    /**
     * @param parent                   容器节点
     * @param threadFactory            线程工厂
     * @param rejectedExecutionHandler 拒绝策略
     * @param waitStrategyFactory      等待策略工厂
     */
    public DisruptorEventLoop(@Nullable EventLoopGroup parent,
                              @Nonnull ThreadFactory threadFactory,
                              @Nonnull RejectedExecutionHandler rejectedExecutionHandler,
                              @Nonnull WaitStrategyFactory waitStrategyFactory) {
        this(parent, threadFactory, rejectedExecutionHandler,
                DEFAULT_RING_BUFFER_SIZE, DEFAULT_BATCH_EVENT_SIZE,
                waitStrategyFactory);
    }

    /**
     * @param parent                   容器节点
     * @param threadFactory            线程工厂
     * @param rejectedExecutionHandler 拒绝策略
     * @param ringBufferSize           环形缓冲区大小
     * @param taskBatchSize            批量拉取(执行)任务数(小于等于0则不限制)
     * @param waitStrategyFactory      等待策略工厂
     */
    public DisruptorEventLoop(@Nullable EventLoopGroup parent,
                              @Nonnull ThreadFactory threadFactory,
                              @Nonnull RejectedExecutionHandler rejectedExecutionHandler,
                              int ringBufferSize, int taskBatchSize,
                              @Nonnull WaitStrategyFactory waitStrategyFactory) {

        super(parent);
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.ringBuffer = RingBuffer.createMultiProducer(RunnableEvent::new,
                ringBufferSize,
                waitStrategyFactory.newWaitStrategy(this));
        this.taskBatchSize = taskBatchSize;

        // 它不依赖于其它消费者，只依赖生产者的sequence
        worker = new Worker(ringBuffer.newBarrier());
        // 添加worker的sequence为网关sequence，生产者们会监听到该sequence
        ringBuffer.addGatingSequences(worker.sequence);

        // 保存线程对象
        this.thread = Objects.requireNonNull(threadFactory.newThread(worker), "newThread");
        UncaughtExceptionHandlers.logIfAbsent(thread, logger);
    }

    @Override
    public final boolean inEventLoop() {
        return thread == Thread.currentThread();
    }

    @Override
    public final boolean isShuttingDown() {
        return isShuttingDown0(stateHolder.get());
    }

    private static boolean isShuttingDown0(int state) {
        return state >= ST_SHUTTING_DOWN;
    }

    @Override
    public final boolean isShutdown() {
        return isShutdown0(stateHolder.get());
    }

    private static boolean isShutdown0(int state) {
        return state >= ST_SHUTDOWN;
    }

    @Override
    public final boolean isTerminated() {
        return stateHolder.get() == ST_TERMINATED;
    }

    @Override
    public final boolean awaitTermination(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        return terminationFuture.await(timeout, unit);
    }

    @Override
    public final ListenableFuture<?> terminationFuture() {
        return terminationFuture;
    }

    @Override
    public final void shutdown() {
        for (; ; ) {
            // 为何要存为临时变量？表示我们是基于特定的状态执行代码，compareAndSet才有意义
            int oldState = stateHolder.get();
            if (isShuttingDown0(oldState)) {
                return;
            }

            if (stateHolder.compareAndSet(oldState, ST_SHUTTING_DOWN)) {
                // 确保线程能够关闭
                ensureThreadTerminable(oldState);
                return;
            }
        }
    }

    @Nonnull
    @Override
    public final List<Runnable> shutdownNow() {
        for (; ; ) {
            int oldState = stateHolder.get();
            if (isShutdown0(oldState)) {
                return Collections.emptyList();
            }

            if (stateHolder.compareAndSet(oldState, ST_SHUTDOWN)) {
                // 确保线程能够关闭 - 这里不能操作ringBuffer中的数据，不能打破[多生产者单消费者]的架构
                ensureThreadTerminable(oldState);
                return Collections.emptyList();
            }
        }
    }

    /**
     * 确保线程可终止。
     * - terminable
     *
     * @param oldState 切换到shutdown之前的状态
     */
    private void ensureThreadTerminable(int oldState) {
        if (oldState == ST_NOT_STARTED) {
            stateHolder.set(ST_TERMINATED);
            terminationFuture.setSuccess(null);
        } else {
            // 消费者可能阻塞在等待事件的地方，即使inEventLoop也需要中断，否则可能丢失信号，在waitFor处无法停止
            worker.sequenceBarrier.alert();

            // 唤醒线程 - 如果线程可能阻塞在其它地方
            if (!inEventLoop()) {
                wakeUp();
            }
        }
    }

    /**
     * 如果子类可能阻塞在其它地方，那么应该重写该方法以唤醒线程
     */
    protected void wakeUp() {

    }

    /**
     * 中断消费者线程。
     * 通常用于唤醒线程，如果线程需要通过中断唤醒。
     */
    protected final void interruptThread() {
        thread.interrupt();
    }

    /**
     * Q: 如何保证算法的安全性的？
     * A: 前提：{@link Worker#cleanRingBuffer()}与其它生产者调用{@link RingBuffer#publish(long)}发布事件之间是互斥的！
     * 1. 如果sequence是在{@link Worker#cleanRingBuffer()}之后申请的sequence，那么一定能检测到{@link #isShuttingDown() true}，
     * 因此不会发布任务。
     * 2. 如果是在{@link Worker#cleanRingBuffer()}之前申请的sequence，那么{@link Worker#cleanRingBuffer()}一定能清理掉它！
     */
    @Override
    public final void execute(@Nonnull Runnable task) {
        if (inEventLoop()) {
            // 防止死锁 - 因为是单消费者模型，自己发布事件时，如果没有足够空间，会导致死锁。
            // 线程内部，请使用TimerSystem延迟执行

            // Q: 为什么不使用 tryNext()？
            // A: 会使得使用的人懵逼的，怎么一会儿能用，一会儿不能用。

            // Q: 这里为什么不直接添加到timerSystem？
            // A: 因为时序问题，会让让用户以为execute之间有时序保证，而实际上是没有的。

            throw new BlockingOperationException("");
        } else {
            // 1. 这里不再先判断{@code isShuttingDown()}，这里的判断只会在“请求了关闭EventLoop而EventLoop还未响应关闭请求”这段时间内有意义，
            // 因为一旦EventLoop响应了关闭请求，删除了自己的sequence之后，next是不会阻塞的，可以在申请sequence后拒绝任务。
            // 乐观执行，降低关闭期间的响应速度以提高运行期间的响应速度，整体看来，这样是划算的。

            // 2. 这里之所以可以安全的调用 next()，是因为我实现了自己的事件处理器(Worker)，否则next()是可能死锁的！
            final long sequence = ringBuffer.next(1);
            if (isShuttingDown()) {
                // 如果申请sequence之后发现EventLoop已开始关闭，则申请到的sequence对应的数据可能未被EventLoop消费，
                // 需要先放弃申请到的sequence(避免阻塞EventLoop的清理动作，同时避免破坏数据)，再拒绝任务。
                ringBuffer.publish(sequence);

                rejectedExecutionHandler.rejected(task, this);
            } else {
                try {
                    // 发布任务
                    ringBuffer.get(sequence).setTask(task);
                } finally {
                    ringBuffer.publish(sequence);
                    // 确保线程已启动
                    ensureThreadStarted();
                }
            }
        }
    }

    /**
     * 确保线程已启动
     */
    private void ensureThreadStarted() {
        int oldState = stateHolder.get();
        if (oldState == ST_NOT_STARTED) {
            if (stateHolder.compareAndSet(ST_NOT_STARTED, ST_STARTED)) {
                thread.start();
            }
        }
    }

    // --------------------------------------- 线程管理 ----------------------------------------

    /**
     * 将运行状态转换为给定目标，或者至少保留给定状态。
     * 参考自{@code ThreadPoolExecutor#advanceRunState}
     *
     * @param targetState 期望的目标状态
     */
    private void advanceRunState(int targetState) {
        for (; ; ) {
            int oldState = stateHolder.get();
            if (oldState >= targetState || stateHolder.compareAndSet(oldState, targetState))
                break;
        }
    }

    /**
     * 事件循环线程启动时的初始化操作。
     *
     * @apiNote 抛出任何异常都将导致线程终止
     */
    protected void init() throws Exception {

    }

    /**
     * 执行一次循环。
     *
     * @apiNote 注意由于调用时机并不确定，子类实现需要自己控制真实的帧间隔，可以使用{@link TimerSystem}控制。
     */
    protected void loopOnce() throws Exception {

    }

    /**
     * 线程退出前的清理动作
     */
    protected void clean() throws Exception {

    }

    /**
     * 安全的执行一次循环。
     * 注意：该方法不是给子类的API。用于{@link WaitStrategy}的api
     */
    public final void safeLoopOnce() {
        assert inEventLoop();
        try {
            loopOnce();
        } catch (Throwable t) {
            if (t instanceof VirtualMachineError) {
                logger.error("loopOnce caught exception", t);
            } else {
                logger.warn("loopOnce caught exception", t);
            }
        }
    }

    /**
     * 实现{@link RingBuffer}的消费者，实现基本和{@link BatchEventProcessor}一致。
     * 但解决了两个问题：
     * 1. 生产者调用{@link RingBuffer#next()}时，如果消费者已关闭，则会死锁！为避免死锁不得不使用{@link RingBuffer#tryNext()}，但是那样的代码并不友好。
     * 2. 内存泄漏问题，使用{@link BatchEventProcessor}在关闭时无法清理{@link RingBuffer}中的数据。
     */
    private class Worker implements Runnable {

        private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        private final SequenceBarrier sequenceBarrier;

        private Worker(SequenceBarrier sequenceBarrier) {
            this.sequenceBarrier = sequenceBarrier;
        }

        @Override
        public void run() {
            try {
                init();

                loop();
            } catch (Throwable e) {
                logger.error("thread exit due to exception!", e);
            } finally {
                // 如果是非正常退出，需要切换到正在关闭状态 - 告知其它线程，已经开始关闭
                advanceRunState(ST_SHUTTING_DOWN);
                try {
                    // 清理ringBuffer中的数据
                    cleanRingBuffer();
                } finally {
                    // 退出前进行必要的清理，释放系统资源
                    try {
                        clean();
                    } catch (Throwable e) {
                        logger.error("thread clean caught exception!", e);
                    } finally {
                        // 设置为终止状态
                        stateHolder.set(ST_TERMINATED);
                        terminationFuture.setSuccess(null);
                    }
                }
            }
        }

        private void loop() throws Exception {
            long availableSequence;
            long nextSequence = sequence.get() + 1L;

            while (true) {
                try {
                    // 等待生产者生产数据
                    availableSequence = waitFor(nextSequence);

                    // 处理所有可消费的事件
                    while (nextSequence <= availableSequence) {
                        safeExecute(ringBuffer.get(nextSequence).detachTask());
                        nextSequence++;
                    }

                    // 标记这批事件已处理
                    sequence.set(availableSequence);

                    // 处理完一批事件，执行一次循环
                    safeLoopOnce();
                } catch (AlertException | InterruptedException e) {
                    // 请求了关闭 BatchEventProcessor并没有响应中断请求，会导致中断信号丢失。
                    if (isShuttingDown()) {
                        break;
                    }
                } catch (TimeoutException e) {
                    // 等待超时，执行一次循环
                    safeLoopOnce();
                } catch (Throwable e) {
                    // 不好的等待策略实现
                    // 这是和BatchEventProcessor不一样的地方，这里并不会更新sequence，不会导致数据丢失问题！
                    logger.error("bad waitStrategy imp", e);
                    // 检测退出
                    if (isShuttingDown()) {
                        break;
                    }
                }
            }
        }

        private long waitFor(long nextSequence) throws AlertException, InterruptedException, TimeoutException {
            if (taskBatchSize < 1) {
                return sequenceBarrier.waitFor(nextSequence);
            } else {
                return Math.min(nextSequence + taskBatchSize - 1, sequenceBarrier.waitFor(nextSequence));
            }
        }

        private void cleanRingBuffer() {
            // 删除自己：这是解决生产者死锁问题的关键，生产者一定能从next中醒来
            // 由于此时已经不存在gatingSequence，因此生产者都能很快的申请到空间，不会阻塞
            ringBuffer.removeGatingSequence(sequence);

            long startTimeMillis = System.currentTimeMillis();
            // 申请整个空间，因此与真正的生产者之间是互斥的！这是关键
            final long finalSequence = ringBuffer.next(ringBuffer.getBufferSize());
            final long initialSequence = finalSequence - (ringBuffer.getBufferSize() - 1);
            try {
                // Q: 为什么可以继续消费
                // A: 保证了生产者不会覆盖未消费的数据 - shutdown处的处理是必须的
                long nextSequence = sequence.get() + 1;
                final long endSequence = sequence.get() + ringBuffer.getBufferSize();
                for (; nextSequence <= endSequence; nextSequence++) {
                    final Runnable task = ringBuffer.get(nextSequence).detachTask();
                    // Q: 这里可能为null吗？
                    // A: 这里可能为null - 因为是多生产者模式，关闭前发布的数据可能是不连续的
                    // 如果已进入shutdown阶段，则直接丢弃任务，而不是执行
                    if (null != task && !isShutdown()) {
                        safeExecute(task);
                    }
                }
            } finally {
                ringBuffer.publish(initialSequence, finalSequence);
                logger.info("cleanRingBuffer success! cost timeMillis = {}", System.currentTimeMillis() - startTimeMillis);
                // 标记为已进入最终清理阶段
                advanceRunState(ST_SHUTDOWN);
            }
        }
    }

    static final class RunnableEvent {

        private Runnable task;

        RunnableEvent() {

        }

        Runnable detachTask() {
            Runnable r = task;
            task = null;
            return r;
        }

        void setTask(@Nonnull Runnable task) {
            this.task = task;
        }

    }
}
