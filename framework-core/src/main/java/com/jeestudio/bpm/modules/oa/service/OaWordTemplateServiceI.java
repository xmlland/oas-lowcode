package com.jeestudio.bpm.modules.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jeestudio.bpm.modules.oa.entity.OaWordTemplateEntity;

import java.util.LinkedHashMap;


/**
 * @Description: Word模板服务接口
 */
public interface OaWordTemplateServiceI extends IService<OaWordTemplateEntity> {

	LinkedHashMap<String, Object> checkTemplate();
}
