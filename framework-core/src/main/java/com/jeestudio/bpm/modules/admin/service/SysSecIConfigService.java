package com.jeestudio.bpm.modules.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.common.entity.gen.GenTableExtRuleManyToMany;
import com.jeestudio.bpm.modules.admin.dao.SysSecIdataMapper;
import com.jeestudio.bpm.modules.admin.entity.SysSecIdataEntity;
import com.jeestudio.bpm.modules.oa.service.OaSysAnnouncementServiceI;
import com.jeestudio.bpm.security.storage.IntegrityHandler;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ReflectUtils;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 安全配置服务
 */
@Service
@Slf4j
public class SysSecIConfigService {

    @Autowired
    GenTableService genTableService;

    @Autowired
    @Lazy
    ZformService zformService;

    @Autowired
    SysSecIdataServiceI sysSecIdataService;

    @Autowired
    SysSecIdataMapper sysSecIdataMapper;

    @Autowired
    IntegrityHandler integrityHandler;

    @Autowired
    @Lazy
    OaSysAnnouncementServiceI oaSysAnnouncementService;

    @Value("${spring.profiles.active}")
    String active;

    public String getActive() {
        return active;
    }

    @Cacheable(value = "SysSecIConfig", key = "':'+#tableName")
    public Map<String, Map<String, String>> getColumns(String tableName) {
        List<LinkedHashMap> mapList = zformService.findMapList("sys_sec_iconfig", "table_name", tableName);
        if (mapList == null || mapList.size() == 0) {
            return null;
        }
        LinkedHashMap map = mapList.get(0);
        String column_names = ConvertUtil.getString(map.get("column_names"));
        GenTable genTable = genTableService.getGenTableWithDefination(tableName);
        List<GenTableColumn> columnList = genTable.getColumnList();
        List<GenTableExtRuleManyToMany> manyToMany = genTable.getExtRuleManyToMany();
        //实际字段 与 zform字段中的对应关系
        Map<String, String> columnMap = new HashMap<>();
        for (String column_name : column_names.split(",")) {
            columnList.stream().filter(column -> column_name.equals(column.getName())).findFirst().ifPresent(column -> columnMap.put(column_name, column.getSimpleJavaField()));
            manyToMany.stream().filter(column -> column_name.equals(column.getName())).findFirst().ifPresent(column -> columnMap.put(column_name, column.getName()));
        }
        Map<String, Map<String, String>> columnsMap = new HashMap<>();
        columnsMap.put(ConvertUtil.getString(map.get("id")), columnMap);
        return columnsMap;
    }

    public String mergeColumn(LinkedHashMap<String, Object> map, List<String> dbColumns) {
        return integrityHandler.mergeColumn(map, dbColumns.toArray(new String[0]));
    }

    public void saveMapRecord(String configId, List<String> dbColumns, List<LinkedHashMap> dataList, GenTable genTable) {
        if (dbColumns == null || dbColumns.size() == 0) {
            return;
        }
        List<SysSecIdataEntity> list = new ArrayList<>();
        for (LinkedHashMap<String, Object> map : dataList) {
            String mergeColumn = mergeColumn(map, dbColumns);
            String encrypt = integrityHandler.encrypt(mergeColumn);
            SysSecIdataEntity sysSecIdataEntity = new SysSecIdataEntity();
            sysSecIdataEntity.setId(IdUtil.nanoId());
            Map<String, Object> optUser = null;
            if (map.containsKey("updateBy")) {
                optUser = (Map<String, Object>) map.get("updateBy");
            } else if (map.containsKey("createBy")) {
                optUser = (Map<String, Object>) map.get("createBy");
            }
            if (optUser != null) {
                sysSecIdataEntity.setCreateBy(ConvertUtil.getString(optUser.get("id")));
                sysSecIdataEntity.setOptName(ConvertUtil.getString(optUser.get("name")));
            }
            sysSecIdataEntity.setCreateDate(DateUtil.date());
            sysSecIdataEntity.setParentId(configId);
            sysSecIdataEntity.setEnv(active);
            sysSecIdataEntity.setDataId(ConvertUtil.getString(map.get("id")));
            sysSecIdataEntity.setRawData(mergeColumn);
            sysSecIdataEntity.setOptTime(DateUtil.date());
            sysSecIdataEntity.setIntegrityValue(encrypt);
            sysSecIdataEntity.setValidPass(Global.YES);
            sysSecIdataEntity.setValidTime(DateUtil.date());
            list.add(sysSecIdataEntity);
        }
        int delete = this.execBySplit(list, (entityList) -> {
            List<String> idList = entityList.stream().map(SysSecIdataEntity::getDataId).collect(Collectors.toList());
            return sysSecIdataMapper.deleteByDataId(configId, active, idList);
        });
        log.info("删除{}完整性保护数据,数量：{}条", genTable.getName(), delete);


        int execCount = this.execBySplit(list, (entityList) -> sysSecIdataMapper.batchInsert(entityList));
        log.info("批量插入{}完整性保护数据,数量：{}条", genTable.getName(), execCount);
    }

    public void saveRecord(String configId, Map<String, String> columnMap, List<Zform> zformList, GenTable genTable) {
        List<LinkedHashMap> dataList = new ArrayList<>();
        for (Zform zform : zformList) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            for (String column : columnMap.keySet()) {
                try {
                    Object value = ReflectUtils.getValue(zform, columnMap.get(column));
                    if (value != null) {
                        map.put(columnMap.get(column), value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            map.put("id", zform.getId());
            if (zform.getCreateBy() != null) {
                LinkedHashMap<String, Object> createByMap = new LinkedHashMap<>();
                createByMap.put("id", zform.getCreateBy().getId());
                createByMap.put("name", zform.getCreateBy().getName());
                map.put("createBy", createByMap);
            }
            if (zform.getUpdateBy() != null) {
                LinkedHashMap<String, Object> updateByMap = new LinkedHashMap<>();
                updateByMap.put("id", zform.getUpdateBy().getId());
                updateByMap.put("name", zform.getUpdateBy().getName());
                map.put("updateBy", updateByMap);
            }
            dataList.add(map);
        }
        List<String> dbColumns = new ArrayList<>(columnMap.keySet());
        this.saveMapRecord(configId, dbColumns, dataList, genTable);

    }

    public void save(SysSecIdataEntity sysSecIdataEntity) {
        List<SysSecIdataEntity> list = new ArrayList<>();
        list.add(sysSecIdataEntity);
        sysSecIdataMapper.deleteByDataId(sysSecIdataEntity.getParentId(), sysSecIdataEntity.getEnv(), Arrays.asList(sysSecIdataEntity.getDataId()));
        sysSecIdataMapper.batchInsert(list);
    }

    private int execBySplit(List<SysSecIdataEntity> list, Function<List<SysSecIdataEntity>, Integer> function) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        int execCount = 0;
        if (list.size() > 1000) {
            int count = list.size() / 1000 + 1;
            for (int i = 0; i < count; i++) {
                int startIndex = i * 1000;
                int endIndex = (i + 1) * 1000;
                if (endIndex > list.size()) {
                    endIndex = list.size();
                }
                List<SysSecIdataEntity> ts = list.subList(startIndex, endIndex);
                if (ts.size() > 0) {
                    //判断是否需要执行
                    execCount += function.apply(ts);
                }
            }
        } else {
            execCount += function.apply(list);
        }
        return execCount;
    }

    /**
     * 清除完整性保护配置缓存
     */
    @CacheEvict(value = "SysSecIConfig", allEntries = true)
    public void cleanCache() {
        log.info("清除完整性保护配置缓存");
    }


    public Map<String, Object> parseExtInfo(String id, String tableName) {
        Map<String, Object> map = new HashMap<>();
        long tableCount = sysSecIdataMapper.countTable(tableName);
        map.put("table_count", tableCount);
        map.putAll(sysSecIdataMapper.countRecord(id, active, tableName));
        return map;
    }


    public void autoValid() {
        List<LinkedHashMap> mapList = zformService.findMapList("sys_sec_iconfig");
        for (LinkedHashMap map : mapList) {
            this.autoValid(ConvertUtil.getString(map.get("table_name")));
        }
    }

    public void autoValid(String tableName) {
        log.info("开始自动完整性验证：{}", tableName);
        Map<String, Map<String, String>> columnMap = this.getColumns(tableName);

        AtomicInteger errorCount = new AtomicInteger();
        for (Map.Entry<String, Map<String, String>> entry : columnMap.entrySet()) {
            String configId = entry.getKey();
            GenTable genTable = genTableService.getGenTableWithDefination(tableName);
            List<LinkedHashMap> mapList = zformService.findMapList(tableName);
            zformService.appendExtManyToMany(mapList, genTable);
            List<String> dbColumns = new ArrayList<>(entry.getValue().keySet());
            List<SysSecIdataEntity> listNeedValid = new ArrayList<>();
            for (LinkedHashMap<String, Object> map : mapList) {
                String mergeColumn = mergeColumn(map, dbColumns);
                SysSecIdataEntity sysSecIdataEntity = new SysSecIdataEntity();
                sysSecIdataEntity.setId(IdUtil.nanoId());
                sysSecIdataEntity.setParentId(configId);
                sysSecIdataEntity.setEnv(active);
                sysSecIdataEntity.setDataId(ConvertUtil.getString(map.get("id")));
                sysSecIdataEntity.setRawData(mergeColumn);
                listNeedValid.add(sysSecIdataEntity);
            }


            this.execBySplit(listNeedValid, (entityList) -> {
                List<SysSecIdataEntity> dbList = sysSecIdataMapper.selectDbList(configId, active, tableName);
                Map<String, SysSecIdataEntity> dbMap = ConvertUtil.listToMap(dbList, SysSecIdataEntity::getDataId);
                List<SysSecIdataEntity> needToAdd = new ArrayList<>();
                List<String> validSuccess = new ArrayList<>();
                List<String> validFail = new ArrayList<>();
                int updateCount = 0;
                for (SysSecIdataEntity sysSecIdataEntity : entityList) {
                    if (dbMap.containsKey(sysSecIdataEntity.getDataId())) {
                        SysSecIdataEntity dbEntity = dbMap.get(sysSecIdataEntity.getDataId());
                        if (dbEntity.getRawData().equals(sysSecIdataEntity.getRawData())) {
                            validSuccess.add(dbEntity.getId());
                        } else {
                            errorCount.getAndIncrement();
                            validFail.add(dbEntity.getId());
                        }
                        updateCount++;
                    } else {
                        sysSecIdataEntity.setValidPass(Global.NO);
                        sysSecIdataEntity.setValidTime(DateUtil.date());
                        needToAdd.add(sysSecIdataEntity);
                    }
                }
                if (validSuccess.size() > 0) {
                    UpdateWrapper<SysSecIdataEntity> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.set("valid_pass", Global.YES);
                    updateWrapper.set("valid_time", DateUtil.date());
                    updateWrapper.in("id", validSuccess);
                    sysSecIdataService.update(updateWrapper);
                }
                if (validFail.size() > 0) {
                    UpdateWrapper<SysSecIdataEntity> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.set("valid_pass", Global.NO);
                    updateWrapper.set("valid_time", DateUtil.date());
                    updateWrapper.in("id", validFail);
                    sysSecIdataService.update(updateWrapper);
                }


                if (needToAdd.size() > 0) {

                    errorCount.addAndGet(needToAdd.size());
                    sysSecIdataMapper.batchInsert(needToAdd);
                }
                return updateCount + needToAdd.size();
            });
        }
        log.info("自动完整性验证完成：{}", tableName);

        if (errorCount.get() > 0) {
            oaSysAnnouncementService.sendToRole("admin","完整性验证失败-" + tableName,
                    "完整性验证失败-" + tableName + "，" + errorCount.get() + "条数据验证失败，请及时处理"
                    , false, "system");
        }

    }
}
