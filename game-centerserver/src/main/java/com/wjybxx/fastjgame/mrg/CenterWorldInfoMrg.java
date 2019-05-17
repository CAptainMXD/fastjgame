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

package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.misc.PlatformType;
import com.wjybxx.fastjgame.net.common.RoleType;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 23:30
 * @github - https://github.com/hl845740757
 */
public class CenterWorldInfoMrg extends WorldCoreInfoMrg{
    /**
     * 所属的运营平台
     */
    private PlatformType platformType;
    /**
     * 从属的战区
     */
    private int warzoneId;
    /**
     * 服id
     */
    private int serverId;

    @Inject
    public CenterWorldInfoMrg(GuidMrg guidMrg) {
        super(guidMrg);
    }

    @Override
    protected void initImp(ConfigWrapper startArgs) throws Exception {
        platformType=PlatformType.valueOf(startArgs.getAsString("platform"));
        warzoneId=startArgs.getAsInt("warzoneId");
        serverId=startArgs.getAsInt("serverId");
    }

    @Override
    public RoleType getProcessType() {
        return RoleType.CENTER;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public int getWarzoneId() {
        return warzoneId;
    }

    public int getServerId() {
        return serverId;
    }
}
