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

package com.wjybxx.fastjgame.net;

/**
 * token解压密策略。
 * 由于token只在服务器加解密，是不对客户端开放的，客户端每次直接返回token字节数组，因此必须能还原。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/6 17:39
 * github - https://github.com/hl845740757
 */
public interface TokenEncryptStrategy {

    /**
     * 加密token
     *
     * @param token 待加密的token
     * @return 加密之后的字节数组
     */
    byte[] encryptToken(Token token);

    /**
     * 解密token
     *
     * @param encryptedTokenBytes 加密后的token字节数组
     * @return 解密得到的token
     * @throws Exception 允许无法解密时抛出异常
     */
    Token decryptToken(byte[] encryptedTokenBytes) throws Exception;
}
