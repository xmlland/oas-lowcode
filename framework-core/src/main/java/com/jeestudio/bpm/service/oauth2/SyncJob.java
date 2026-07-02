package com.jeestudio.bpm.service.oauth2;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.pojo.OfficeInfo;
import com.jeestudio.bpm.common.pojo.UserInfo;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.tools.base.utils.JSONHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Description: OAuth2数据同步定时任务
 */
@Slf4j
@Component
public class SyncJob {

    @Autowired
    ProjectProperties projectProperties;

    @Autowired
    OAuth2Service oAuth2Service;

    @Autowired
    OAuthOfficeService oAuthOfficeService;

    @Autowired
    OAuthUserService oAuthUserService;

    private static final String SYNC_OFFICE_URL = "{}/oauth2/officeList?clientId={}&clientSecret={}";
    private static final String SYNC_USER_URL = "{}/oauth2/userList?clientId={}&clientSecret={}&syncRole={}";

    /**
     * 每分钟查询一下机构和用户
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void syncOfficeAndUser() {
        if (projectProperties.isSyncOfficeJob()) {
            List<String> syncOfficeList = projectProperties.getSyncOfficeList();
            if (syncOfficeList.size() == 0) {
                log.warn("没有配置syncOfficeList");
                return;
            }
            log.info("开始同步机构和用户");
            Map<String, Object> oauth2ServerMap = projectProperties.getOauth2ServerMap();
            if (oauth2ServerMap.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                jsonArray.addAll(syncOfficeList);
                //获取第一个oauth2Server
                String oauth2Server = oauth2ServerMap.values().iterator().next().toString();
                oauth2Server = oauth2Server + projectProperties.getOauth2ServerContextPath();
                String syncOfficeUrl = StrUtil.format(SYNC_OFFICE_URL, oauth2Server, projectProperties.getOauth2ClientId(), projectProperties.getOauth2ClientSecret());
                log.info("同步机构地址:{}", syncOfficeUrl);
                HttpRequest syncOfficeRequest = HttpUtil.createPost(syncOfficeUrl);
                log.info("同步机构参数:{}", jsonArray.toJSONString());
                syncOfficeRequest.body(jsonArray.toJSONString());
                try {
                    HttpResponse execute = syncOfficeRequest.execute();
                    String body = execute.body();
                    if (execute.isOk()) {
                        JSONObject object = JSONObject.parseObject(body);
                        JSONArray list = object.getJSONObject("data").getJSONArray("list");
                        List<OfficeInfo> officeInfoList = JSONHelper.toList(list.toJSONString(), OfficeInfo.class);
                        oAuthOfficeService.save(officeInfoList);
                    } else {
                        log.error("同步机构失败,{}", body);
                    }
                } catch (Exception e) {
                    log.error("同步机构失败,{}", ExceptionUtil.stacktraceToString(e));
                }

                //是否同步角色
                boolean syncRole = projectProperties.getSyncUserRoleMap().size() > 0;

                String syncUserUrl = StrUtil.format(SYNC_USER_URL, oauth2Server, projectProperties.getOauth2ClientId(), projectProperties.getOauth2ClientSecret(), syncRole);
                log.info("同步用户地址:{}", syncUserUrl);
                HttpRequest syncUserRequest = HttpUtil.createPost(syncUserUrl);
                log.info("同步用户参数:{}", jsonArray.toJSONString());
                syncUserRequest.body(jsonArray.toJSONString());
                try {
                    HttpResponse execute = syncUserRequest.execute();
                    String body = execute.body();
                    if (execute.isOk()) {
                        JSONObject object = JSONObject.parseObject(body);
                        JSONArray list = object.getJSONObject("data").getJSONArray("list");
                        List<UserInfo> officeInfoList = JSONHelper.toList(list.toJSONString(), UserInfo.class);
                        oAuthUserService.save(officeInfoList);
                    } else {
                        log.error("同步用户失败,{}", body);
                    }
                } catch (Exception e) {
                    log.error("同步用户失败,{}", ExceptionUtil.stacktraceToString(e));
                }
            } else {
                log.warn("没有配置oauth2Server");
            }
        }
    }


}
