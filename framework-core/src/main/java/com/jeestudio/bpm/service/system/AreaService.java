package com.jeestudio.bpm.service.system;

import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.entity.system.Area;
import com.jeestudio.bpm.common.entity.tagtree.TagTree;
import com.jeestudio.bpm.mapper.base.system.AreaDao;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Description: 行政区服务
 */
@Service
@Slf4j
public class AreaService {

    @Autowired
    private AreaDao areaDao;

    /**
     * 获取行政区信息
     * @param id 行政区ID
     * @return
     */
    @Cacheable(value = "areaInfo", key = "':'+#id")
    public TagTree getArea(String id) {
        Area area = areaDao.get(id);
        if (area == null) {
            return null;
        }
        TagTree tagTree = new TagTree();
        tagTree.setId(area.getId());
        tagTree.setParentId(area.getParentId());
        tagTree.setName(area.getName());
        tagTree.setHasChildren(area.isHasChildren());
        return tagTree;
    }

    /**
     * 清除行政区缓存
     */
    @CacheEvict(value = "areaInfo", allEntries = true)
    public void cleanCache() {
        log.info("清除行政区缓存");
    }


    @Transactional(readOnly = true)
    public List<TagTree> getAreaTagTreeAsync(String id) {
        Area area = new Area();
        if (StringUtil.isNotEmpty(id)) {
            area.setId(id);
            area.setParent(new Area(id));
        } else {
            area.setParent(new Area(Global.DEFAULT_ROOT_CODE));
        }
        List<TagTree> list = areaDao.findAreaTagTree(area);
        for (TagTree tagTree : list) {
            Area a = new Area();
            a.setParent(new Area(tagTree.getId()));
            List<TagTree> childrenList = areaDao.findAreaTagTree(a);
            if (childrenList != null && childrenList.size() > 0) {
                tagTree.setHasChildren(true);
            }
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<TagTree> getAreaTagTree() {
        return getAreaTagTree(null);
    }

    @Transactional(readOnly = true)
    public List<TagTree> getAreaTagTree(String id) {
        Map<String, TagTree> map = Maps.newHashMap();
        List<TagTree> list = null;
        if (StringUtil.isEmpty(id)) {
            list = areaDao.findAreaTagTreeAll();
        } else {
            list = areaDao.findAreaSubTree(id);
        }
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
    public Area getById(String id){
        return areaDao.get(id);
    }
}
