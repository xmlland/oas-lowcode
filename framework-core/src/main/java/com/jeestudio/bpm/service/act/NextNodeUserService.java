package com.jeestudio.bpm.service.act;

import com.jeestudio.bpm.common.entity.common.Zform;

import java.util.LinkedHashMap;

/**
 * @Description: 通过流程任务节点规则权限的 nextNodeUser 规则自定义获取下一节点用户
 */
public interface NextNodeUserService {

    /**
     *
     * @param zform
     * @param loginName
     * @param frameUserInfo 框架原生配置，获取到的下一节点用户
     * @return
     */
    LinkedHashMap<String, Object> getTargetUserList(Zform zform, String loginName,LinkedHashMap<String, Object> frameUserInfo);
}
