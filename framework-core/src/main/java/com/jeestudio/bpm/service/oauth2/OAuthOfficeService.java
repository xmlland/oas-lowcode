package com.jeestudio.bpm.service.oauth2;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.pojo.OfficeInfo;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.OfficeService;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description: OAuth2机构服务
 */
@Service
@Slf4j
public class OAuthOfficeService {

    @Autowired
    OfficeService officeService;

    @Autowired
    ZformService zformService;

    public List<OfficeInfo> list(List<String> ids) {
        List<OfficeInfo> officeInfos = new ArrayList<>();
        Set<String> idList = new HashSet<>();
        List<Office> officeList = officeService.findList(new Office());
        //找出id符合的机构的所有子机构
        for (String id : ids) {
            for (Office office : officeList) {
                if (ConvertUtil.getString(office.getParentIds()).contains(StrUtil.format(",{},", id))) {
                    idList.add(office.getId());
                }
            }
            idList.addAll(findParentIds(id, officeList));
        }
        //找出上级机构
        for (Office office : officeList) {
            if (idList.contains(office.getId())) {
                OfficeInfo officeInfo = new OfficeInfo(office);
                officeInfos.add(officeInfo);
            }
        }
        return officeInfos;
    }

    private List<String> findParentIds(String id, List<Office> officeList) {
        //递归找出最上层的跟机构
        List<String> parentIds = new ArrayList<>();
        for (Office office : officeList) {
            if (office.getId().equals(id)) {
                office.getParent();
                parentIds.addAll(findParentIds(office.getParent().getId(), officeList));
                parentIds.add(office.getId());
            }
        }
        return parentIds;
    }


    public void save(List<OfficeInfo> officeInfos) {
        try {
            List<Office> officeList = officeService.findList(new Office());
            Map<String, Office> orgMap = ConvertUtil.listToMap(officeList, Office::getId);
            int addCount = 0;
            int updateCount = 0;
            int skipCount = 0;
            for (OfficeInfo officeInfo : officeInfos) {
                JSONObject jsonObject = officeInfo.toJSONObject();
                Zform zform = zformService.getZformFromMap(jsonObject, "");
                if (orgMap.containsKey(officeInfo.getId())) {
                    Office office = orgMap.get(officeInfo.getId());
                    Date updateDate = office.getUpdateDate();
                    if (updateDate == null) {
                        updateDate = office.getCreateDate();
                    }
                    if (updateDate != null) {
                        Date syncUpdateDate = officeInfo.getUpdate_date();
                        if (syncUpdateDate == null) {
                            syncUpdateDate = officeInfo.getCreate_date();
                        }
                        if (syncUpdateDate != null) {
                            //如果更新时间相同则不更新
                            if (DateUtil.between(updateDate, syncUpdateDate, DateUnit.SECOND) == 0) {
                                log.debug("机构信息{},{}已经同步,跳过", officeInfo.getName(), officeInfo.getCode());
                                skipCount++;
                                continue;
                            }
                        }
                    }
                    updateCount++;
                    zform.setId(officeInfo.getId());
                    zform.setPreUpdateBy(new User(officeInfo.getUpdate_by()));
                    zform.setPreUpdateDate(officeInfo.getUpdate_date());
                    log.debug("更新机构信息{},{}", officeInfo.getName(), officeInfo.getCode());
                } else {
                    addCount++;
                    zform.setPreId(officeInfo.getId());
                    zform.setPreCreateBy(new User(officeInfo.getCreate_by()));
                    zform.setPreCreateDate(officeInfo.getCreate_date());
                    zform.setPreUpdateBy(new User(officeInfo.getUpdate_by()));
                    zform.setPreUpdateDate(officeInfo.getUpdate_date());
                    log.debug("新增机构信息{},{}", officeInfo.getName(), officeInfo.getCode());
                }
                zformService.saveZform(zform, "", "");
            }
            log.info("保存机构信息成功,新增{}条,更新{}条,跳过{}条", addCount, updateCount, skipCount);
        } catch (Exception e) {
            log.error("保存机构信息失败,{}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException(e);
        }
    }

}
