package com.jeestudio.bpm.service.system;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.bpm.common.entity.tagtree.TagTree;
import com.jeestudio.bpm.common.view.system.OfficeView;
import com.jeestudio.bpm.mapper.base.system.OfficeDao;
import com.jeestudio.bpm.service.common.TreeService;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 机构管理服务
 */
@Service
public class OfficeService extends TreeService<OfficeDao, Office> {

    @Autowired
    @Lazy
    ZformService zformService;

    @Autowired
    private OfficeDao officeDao;

    @Transactional(readOnly = true)
    public List<TagTree> getOfficeTagTree() {
        Map<String, TagTree> map = Maps.newHashMap();
        List<TagTree> list = officeDao.findOfficeTagTreeAll();
        for (TagTree tagTree : list) {
            map.put(tagTree.getId(), tagTree);
        }
        for (TagTree tagTree : list) {
            if (StringUtil.isNotBlank(tagTree.getParentId())) {
                TagTree tt = map.get(tagTree.getParentId());
                if (tt != null) {
                    tt.setHasChildren(true);
                }
            }
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<TagTree> getOfficeTagTreeAsync(String id) {
        Office office = new Office();
        if (id != null && !"".equals(id)) {
            office.setId(id);
            office.setParent(new Office(id));
        } else {
            office.setParent(new Office(Global.DEFAULT_ROOT_CODE));
        }
        List<TagTree> list = officeDao.findOfficeTagTree(office);
        for (TagTree tagTree : list) {
            Office o = new Office();
            o.setParent(new Office(tagTree.getId()));
            List<TagTree> childrenList = officeDao.findOfficeTagTree(o);
            if (childrenList != null && childrenList.size() > 0) {
                tagTree.setHasChildren(true);
            }
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<OfficeView> findOfficeViewData(OfficeView officeView) {
        return officeDao.findOfficeViewData(officeView);
    }

    /**
     * 根据office批量获取companyId
     * @param officeIdList
     * @return
     */
    public Map<String, String> getCompanyIdByOfficeId(List<String> officeIdList) {
        Map<String, String> resultMap = new HashMap<>();
        if (officeIdList == null || officeIdList.size() == 0) {
            return resultMap;
        }

        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("a.id", officeIdList.stream().distinct().collect(Collectors.toList()));
        List<LinkedHashMap> officeList = zformService.findMapList("sys_office", queryWrapper);
        Set<String> officeIdSet = new TreeSet<>();
        List<LinkedHashMap> needFindList = new ArrayList<>();
        for (LinkedHashMap map : officeList) {
            if (Office.TYPE_COMPANY.equals(map.get("types"))) {
                resultMap.put(ConvertUtil.getString(map.get("id")), ConvertUtil.getString(map.get("id")));
            } else {
                String parent_ids = ConvertUtil.getString(map.get("parent_ids"));
                if (StrUtil.isNotBlank(parent_ids)) {
                    String[] parentIds = parent_ids.split(",");
                    officeIdSet.addAll(Arrays.asList(parentIds));
                }
                needFindList.add(map);
            }
        }
        if (officeIdSet.size() > 0) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.in("a.id", officeIdSet);
            List<LinkedHashMap> parentList = zformService.findMapList("sys_office", queryWrapper);
            Map<String, LinkedHashMap> parentMap = ConvertUtil.listToMap(parentList, k -> ConvertUtil.getString(k.get("id")));

            for (LinkedHashMap toFind : needFindList) {
                String id = ConvertUtil.getString(toFind.get("id"));
                if (!toFind.containsKey("parent")) {
                    continue;
                }
                Map parent = (Map) toFind.get("parent");
                String targetId = ConvertUtil.getString(parent.get("id"));
                do {
                    LinkedHashMap theOffice = parentMap.get(targetId);
                    if (theOffice == null) {
                        break;
                    }
                    if (Office.TYPE_COMPANY.equals(theOffice.get("types"))) {
                        resultMap.put(id, ConvertUtil.getString(theOffice.get("id")));
                    } else {
                        parent = (Map) theOffice.get("parent");
                        targetId = ConvertUtil.getString(parent.get("id"));
                    }
                } while (!resultMap.containsKey(id));
            }

        }
        return resultMap;
    }

    @Transactional(readOnly = true)
    public String getCompanyIdByOfficeId(String officeId) {
        Office theOffice = officeDao.get(officeId);
        if (Office.TYPE_COMPANY.equals(theOffice.getType())) {
            return theOffice.getId();
        } else {
            return getCompanyIdByOfficeId(theOffice.getParent().getId());
        }
    }

    @Transactional(readOnly = true)
    public void updateByMasterData(Office office) {
        officeDao.updateByMasterData(office);
    }


    public String getNewSubOrgCode(String parentId) {
        return getNewSubOrgCode(parentId, 3);
    }

    public String getNewSubOrgCode(String parentId,int codeLength) {
        Office office = officeDao.get(parentId);
        List<Office> officeList = officeDao.findPageOfficeByParentId(new Page<>(1, 1), parentId).getRecords();
        String maxCode = "000";
        if (officeList.size() > 0) {
            maxCode = ConvertUtil.getString(officeList.get(0).getCode());
        }
        String codePrefix =  office.getCode() ;
        String code = StringUtil.right(maxCode, 3);
        int codeInt = Integer.parseInt(code, 36);//使用36进制 可以保存更多的机构
        if (StrUtil.isNotBlank(code)) {
            codeInt++;
            code = StrUtil.fillBefore(Integer.toString(codeInt, 36), '0', codeLength);
        } else {
            code = "001";
        }
        return (codePrefix + code).toUpperCase();
    }

    public String getNewSubOrgCode() {
        return getNewSubOrgCode("1");
    }

    public String getNewSubOrgCodeByCode(String parentCode) {
        Office office = officeDao.findUniqueByProperty("code", parentCode);
        if (office == null) {
            throw new RuntimeException("父机构不存在");
        }
        return getNewSubOrgCode(office.getId());
    }


    /**
     * 创建新机构 不会保存到数据库 仅生成java对象
     * @param parentId 父机构id
     * @param orgName 机构名称
     * @return
     */
    public Office createNewOffice(String parentId, String orgName) {
        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("a.parent_id", parentId);
        queryWrapper.orderByDesc("a.code");
        List<LinkedHashMap> offList = zformService.findMapList("sys_office", queryWrapper);
        String newSubOrgCode = getNewSubOrgCode(parentId);
        Office office = new Office();
        office.setName(orgName);
        office.setCode(newSubOrgCode);
        office.setParent(new Office(parentId));
        office.setGrade("1");
        office.setType("1");
        office.setSort((offList.size() + 1) * 10);
        office.setUseable(Global.YES);
        return office;
    }

    @Transactional(readOnly = false)
    public ResultJson saveAuth(String id, String ids) {
        ResultJson resultJson = new ResultJson();
        officeDao.deleteAuthById(id);
        String[] id_s = {};
        if (StringUtil.isNotBlank(ids)) {
            id_s = ids.split(",");
            officeDao.saveAuth(id, Arrays.asList(id_s));
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("设置权限成功");
            resultJson.setMsg_en("Save permission success");
        } else {
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("请选择权限");
            resultJson.setMsg_en("Save permission failed");
        }
        return resultJson;
    }

    @Transactional(readOnly = true)
    public ResultJson getAuth(String id) {
        ResultJson resultJson = new ResultJson();
        resultJson.put("data", officeDao.getAuth(id));
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg("获取权限成功");
        resultJson.setMsg_en("Get permission success");
        return resultJson;
    }

}
