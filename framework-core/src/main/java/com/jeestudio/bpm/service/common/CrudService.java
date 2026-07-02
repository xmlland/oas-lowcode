package com.jeestudio.bpm.service.common;

import com.alibaba.druid.DbType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.around.AroundDaoI;
import com.jeestudio.bpm.common.around.AroundUtil;
import com.jeestudio.bpm.common.entity.common.DataEntity;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.utils.DbTypeUtil;
import com.jeestudio.bpm.utils.GenUtil;
import com.jeestudio.bpm.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;

/**
 * @Description: 通用CRUD服务
 */
public abstract class CrudService<D extends CrudDao<T>, T extends DataEntity<T>> {

    @Autowired
    protected D dao;

    public T get(String id) {
        return dao.get(id);
    }

    public T get(T entity) {
        return dao.get(entity);
    }

    public List<T> findList(T entity) {
        return dao.findList(entity);
    }

    public Page<T> findPage(Page<T> page, T entity) {
        entity.setPage(page);
        IPage<T> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getPageNo(),page.getPageSize());
        entity.getSqlMap().put("orderBy", entity.getPage().getOrderBy());
        if (Zform.class.equals(entity.getClass())) {
            Zform zform = (Zform) entity;
            entity.getSqlMap().put("tableOrViewName", zform.getTableOrViewName());
        }

        IPage<T> pageList = dao.findPageList(iPage, entity, entity.getSqlMap());
        page.setList(pageList.getRecords());
        page.setCount(pageList.getTotal());
        return page;
    }

    public Page<T> findPageMap(Page<T> page, T entity, String tableOrViewName) {
        if (tableOrViewName.toLowerCase().endsWith(GenUtil.SUM_VIEW)) {
            return findPageMap(page, entity, false, true);
        } else {
            return findPageMap(page, entity, false, false);
        }
    }

    public Page<T> findPageMap(Page<T> page, T entity) {
        return findPageMap(page, entity, false, false);
    }

    public Page<T> findPageMap(Page<T> page, T entity, Boolean isVersion) {
        return findPageMap(page, entity, isVersion, false);
    }

    protected String getTableNameWithSchema(String versionSchema, String tableName) {
        //没有配置留痕库 直接返回
        if (StringUtil.isBlank(versionSchema)){
            return tableName;
        }
        if (tableName.contains(".")) {
            tableName = tableName.substring(tableName.indexOf(".") + 1);
        }
        return (StringUtil.isBlank(versionSchema) ? "" : (versionSchema + ".")) + tableName;
    }

    public Page<T> findPageMap(Page<T> page, T entity, Boolean isVersion, Boolean isStatisticalView) {
        entity.setPage(page);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getPageNo(), page.getPageSize());
        String tableOrViewName = "";
        String versionSchema = "";
        QueryWrapper queryWrapper = new QueryWrapper();
        if (Zform.class.equals(entity.getClass())) {
            Zform zform = (Zform) entity;
            tableOrViewName = zform.getTableOrViewName();
            if (StringUtil.isNotBlank(zform.getVersionSchema())) {
                versionSchema = zform.getVersionSchema();
            }
            entity.getSqlMap().put("orderBy", page.getOrderBy());
            queryWrapper = zform.getQueryWrapper();
        }
        if (isVersion) {
            tableOrViewName += "_v";
            if (StringUtil.isNotBlank(versionSchema)) {
                tableOrViewName = this.getTableNameWithSchema(versionSchema, tableOrViewName);
            }
        }
        if ("kingbase".equalsIgnoreCase(DbTypeUtil.getDbType()) ||"gaussdb".equalsIgnoreCase(DbTypeUtil.getDbType()) ){
            iPage.setOptimizeCountSql(false);
        }
        IPage<LinkedHashMap> listMap;
        if (isStatisticalView) {
            //统计视图不分页
            listMap = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
            List<LinkedHashMap> records = dao.findListMapWith(iPage, entity, tableOrViewName.trim(), queryWrapper, entity.getSqlMap());
            if (records == null) {
                records = Lists.newArrayList();
            }
            listMap.setRecords(records);
            listMap.setPages(records.size());
            listMap.setTotal(records.size());
        } else {
            AroundDaoI<T> aroundDao = AroundUtil.getAroundDaoI(tableOrViewName);
            if (aroundDao != null) {
                if (aroundDao.isCustomSqlColumnsFriendly()){
                    entity.getSqlMap().put("sqlColumnsFriendly", aroundDao.getCustomSqlColumnsFriendly(entity));
                }
                if (aroundDao.isCustomSqlJoins()){
                    entity.getSqlMap().put("sqlJoins", aroundDao.getCustomSqlJoins(entity));
                }
            }
            listMap = dao.findListMap(iPage, entity, tableOrViewName, queryWrapper, entity.getSqlMap());
        }
        page.setMap(listMap.getRecords());
        page.setCount(listMap.getTotal());
        return page;
    }

    @Transactional()
    public void save(T entity) {
        if (entity.getIsNewRecord()){
            entity.preInsert();
            dao.insert(entity);
            entity.setIsNewRecord(false);
        }else{
            entity.preUpdate();
            dao.update(entity);
        }
    }

    @Transactional()
    public int batchInsert(List<T> list, Map<String, String> sqlMap) {
        return this.execBySplit(list, (tList) -> dao.batchInsert(tList, sqlMap));
    }

    @Transactional()
    public void saveV(T entity) {
        if (!entity.getIsNewRecord()){
            entity.preUpdate();
            dao.insertV(entity);
        } else {
            entity.preInsert();
            entity.preUpdate();
            dao.insertV(entity);
        }
    }

    @Transactional()
    public void saveVersionSchema(T entity) {
        if (!entity.getIsNewRecord()){
            entity.preUpdate();
            dao.insertVersionSchema(entity);
        } else {
            entity.preInsert();
            entity.preUpdate();
            dao.insertVersionSchema(entity);
        }
    }

    @Transactional()
    public void delete(T entity) {
        dao.delete(entity);
    }

    @Transactional()
    public int batchDelete(String tableName, List<T> list) {
        return this.execBySplit(list, (tList) -> dao.batchDelete(tableName, tList));
    }

    /**
     * 分批次执行
     */
    private int execBySplit(List<T> list, Function<List<T>, Integer> function) {
        String dbType = DbTypeUtil.getDbType();
        int splitCount = 1000;
        if (DbType.sqlserver.name().equalsIgnoreCase(dbType)) {
            splitCount = 100;
        } else if (DbType.postgresql.name().equalsIgnoreCase(dbType)) {
            // PostgreSQL PreparedStatement 参数上限 65535，宽表(200+列)时 1000 行会超限
            splitCount = 200;
        }
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int execCount = 0;
        if (list.size() > splitCount) {
            int count = list.size() / splitCount + 1;
            for (int i = 0; i < count; i++) {
                int startIndex = i * splitCount;
                int endIndex = (i + 1) * splitCount;
                if (endIndex > list.size()) {
                    endIndex = list.size();
                }
                List<T> ts = list.subList(startIndex, endIndex);
                if (!ts.isEmpty()) {
                    //判断是否需要执行
                    execCount += function.apply(ts);
                }
            }
        } else {
            execCount += function.apply(list);
        }
        return execCount;
    }

    @Transactional()
    public void deleteAll(Collection<T> entitys) {
        for (T entity : entitys) {
            dao.delete(entity);
        }
    }

    public T findUniqueByProperty(String propertyName, Object value){
        return dao.findUniqueByProperty(propertyName, value);
    }
}
