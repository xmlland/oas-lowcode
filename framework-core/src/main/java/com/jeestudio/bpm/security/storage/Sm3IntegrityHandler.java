package com.jeestudio.bpm.security.storage;

import cn.hutool.crypto.SmUtil;

/**
 * @Description: SM3完整性处理器
 */
public class Sm3IntegrityHandler extends IntegrityHandler {

    @Override
    public String encrypt(String data) {
        return SmUtil.sm3(data);
    }
}
