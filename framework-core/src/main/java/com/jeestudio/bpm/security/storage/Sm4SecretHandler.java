package com.jeestudio.bpm.security.storage;

import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.tools.security.utils.security.SM4Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: SM4机密性处理器
 */
public class Sm4SecretHandler extends SecretHandler{

    @Autowired
    ProjectProperties projectProperties;

    @Override
    public String encrypt(String data) {
        return SM4Util.encrypt(projectProperties.getSm4Key(), data);
    }

    @Override
    public String decrypt(String data) {
        return SM4Util.decrypt(projectProperties.getSm4Key(), data);
    }
}
