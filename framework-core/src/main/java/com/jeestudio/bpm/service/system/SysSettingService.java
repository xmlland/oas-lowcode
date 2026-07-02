package com.jeestudio.bpm.service.system;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.bpm.utils.oConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * @Description: 系统设置服务
 */
@Service
@Slf4j
public class SysSettingService {
    private static final String KEY_ = "key_";
    private static final String VALUE_ = "value_";

    @Autowired
    ZformService zformService;

    @Autowired
    GenTableService genTableService;

    /**
     * 根据前缀查询系统设置项
     */
    public JSONObject getSettingMap(String prefix, String loginName) throws Exception {
        List<LinkedHashMap> settingList = this.getSysSettingList(prefix, loginName);
        JSONObject jsonObject = new JSONObject();
        for (LinkedHashMap theMap: settingList) {
            if (theMap.get(KEY_) != null) {
                jsonObject.put(StringUtil.getString(theMap.get(KEY_)), theMap.get(VALUE_));
            } else if (theMap.get(KEY_.toUpperCase(Locale.ROOT)) != null) {
                jsonObject.put(StringUtil.getString(theMap.get(KEY_.toUpperCase(Locale.ROOT))), theMap.get(VALUE_.toUpperCase(Locale.ROOT)));
            }
        }
        return jsonObject;
    }

    private List<LinkedHashMap> getSysSettingList(String prefix, String loginName) throws Exception {
        JSONObject zformMap = new JSONObject();
        zformMap.put("formNo", "sys_setting");
        Zform zform = zformService.getZformFromMap(zformMap, loginName);
        if (StringUtil.isNotEmpty(prefix)) {
            zform.getQueryWrapper().likeRight("a.key_", prefix);
        }
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }
        Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), Integer.MAX_VALUE, zform.getPageParam().getOrderBy());
        page.setFromIndex(zform.getPageParam().getFromIndex());
        page = zformService.findPageMap(page,
                zform,
                "",
                loginName,
                genTable,
                "",
                "",
                "");
        List<LinkedHashMap> list = page.getMap();
        return list;
    }

    /**
     * 检查接口访问者是否在白名单中
     * @param request
     * @param whiteIpKey 每个要设置白名单的接口，有一个单独的key，以 wip_ 开头
     * @param loginName 实际是guest，接口是白名单访问的，没有登录token
     * @return 该接口的访问者IP是否在白名单中
     */
    public Boolean isWhiteIp(HttpServletRequest request, String whiteIpKey, String loginName) {
        Boolean isWhiteIp = false;
        String ip = oConvertUtils.getClientIp(request);
        try {
            if (whiteIpKey != null && whiteIpKey.startsWith("wip_")) {
                String prefix = whiteIpKey.substring(0,4);
                JSONObject settingMap = this.getSettingMap(prefix, loginName);
                String wips = settingMap.getString(whiteIpKey);
                if (wips != null && (wips.contains(ip) || wips.contains("*"))) {
                    isWhiteIp = true;
                    log.info(ip + " client ip is in settings");
                } else {
                    log.info(ip + " client ip is in settings");
                    log.warn(whiteIpKey + " in settings are " + StringUtil.getString(wips));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return isWhiteIp;
    }

    public String getSettingValueByKey(String key, String loginName) throws Exception {
        JSONObject settingMap = this.getSettingMap(key, loginName);
        if (settingMap.containsKey(key)) {
            return settingMap.getString(key);
        } else {
            return null;
        }
    }
}
