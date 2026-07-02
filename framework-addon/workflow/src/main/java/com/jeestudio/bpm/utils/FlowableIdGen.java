package com.jeestudio.bpm.utils;

import org.flowable.common.engine.impl.cfg.IdGenerator;

import java.util.UUID;

/**
 * @Description: Flowable流程ID生成器
 */
public class FlowableIdGen implements IdGenerator {

    @Override
    public String getNextId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
