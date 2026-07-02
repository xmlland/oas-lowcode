package com.jeestudio.bpm.service.common;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.mapper.base.common.ZformDao;
import com.jeestudio.bpm.mapper.base.gen.GenTableDao;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 数据仓库服务
 */
@Service
@Transactional
@DS("datahouse")
public class DatahouseService {

    @Autowired
    ZformDao dao;

    @Autowired
    GenTableDao genTableDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Page<Zform> findPageMap(Page<Zform> page, Zform entity) {
        entity.setPage(page);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Zform> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getPageNo(), page.getPageSize());
        String tableOrViewName = "";
        QueryWrapper queryWrapper = new QueryWrapper();
        if (Zform.class.equals(entity.getClass())) {
            tableOrViewName = entity.getTableOrViewName();
            entity.getSqlMap().put("orderBy", page.getOrderBy());
            queryWrapper = entity.getQueryWrapper();
        }
        IPage<LinkedHashMap> listMap = null;
        listMap = dao.findListMap(iPage, entity, tableOrViewName, queryWrapper, entity.getSqlMap());
        page.setMap(listMap.getRecords());
        page.setCount(listMap.getTotal());
        return page;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Zform zform) {
        if (zform.getIsNewRecord()) {
            zform.preInsert();
            dao.insert(zform);
        } else {
            // Doris UNIQUE KEY 模型不支持 UPDATE KEY 列，采用先删后插
            zform.preUpdate();
            dao.delete(zform);
            zform.setIsNewRecord(true);
            zform.preInsert();
            dao.insert(zform);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveWithTrace(Zform zform) {
        this.save(zform);
        // 写入 _V 留痕表
        dao.insertV(zform);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Zform zform) {
        dao.delete(zform);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteSql(String sql) {
        dao.deleteSql(sql);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteChildren(Zform zform) {
        dao.deleteChildren(zform);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteSqlParm(String sql, Map<String, Object> param) {
        dao.deleteSqlParm(sql, param);
    }

    /**
     * 在 Doris 数据源上执行 DDL 语句（建表、改表等）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void buildTable(String sql) {
        genTableDao.buildTable(sql);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<LinkedHashMap> getMapList(Zform zform) {
        return dao.getMapList(zform);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<LinkedHashMap> findMapList(String querySql, QueryWrapper queryWrapper) {
        return dao.findMapList(querySql, queryWrapper);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LinkedHashMap getMap(String querySql, QueryWrapper queryWrapper) {
        List<LinkedHashMap> list = this.findMapList(querySql, queryWrapper);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Zform> getList(Zform zform) {
        return dao.getList(zform);
    }
}
