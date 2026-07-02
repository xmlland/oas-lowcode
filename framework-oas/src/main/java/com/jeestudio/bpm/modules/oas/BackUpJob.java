package com.jeestudio.bpm.modules.oas;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import generate.GenerateUpdateSqlHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 配置表自动备份定时任务
 */
@Slf4j
@Component
public class BackUpJob {


    @Autowired
    DynamicRoutingDataSource dataSource;

    @Value("${cachePrefix}")
    private String cachePrefix;

    @Value("${spring.profiles.active}")
    String active;
    public String getFolder() {
        return "D:/temp/sql/backup/" + cachePrefix + "/";
    }

    /**
     * 每10分钟备份一次配置表
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void backupDb() {
        if ("debug".equals(active)) {
            log.info("开始备份配置表");
            ItemDataSource itemDataSource = (ItemDataSource) dataSource.determineDataSource();
            DruidDataSource druidDataSource = (DruidDataSource) itemDataSource.getRealDataSource();
            String savePath = getFolder() + DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN) + "/";
            String tableNames = "oa_word_template,gen_table,gen_table_column,sys_dictionary,sys_datapermission,sys_datarole_datapermission,sys_datarole,sys_menu,sys_role,sys_role_datarole,sys_role_menu,sys_subsystem,sys_subsystem_menu,oa_task_setting,oa_task_permission";
            for (String tableName : tableNames.split(",")) {
                log.info("配置表自动备份:{}", tableName);
                GenerateUpdateSqlHelper.generate(druidDataSource.getUrl(), druidDataSource.getUsername(), druidDataSource.getPassword(), druidDataSource.getDriverClassName(), savePath + tableName, tableName);
            }
            log.info("备份配置表完成");
        }
    }

    @Scheduled(cron = "0 31 * * * ?")
    public void deleteFile() {
        if ("debug".equals(active)) {
            //删除过期文件
            log.info("删除过期文件");
            List<File> files = FileUtil.loopFiles(new File(getFolder()),1    ,null);
            if (files.size() > 0) {
                //按日期分组
                files.stream().collect(Collectors.groupingBy(file -> file.getName().substring(0, 8)))
                        .forEach((k, v) -> {
                            //如果是今天之前的日期
                            if (Integer.parseInt(k) < Integer.parseInt(DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_PATTERN))) {
                                if (v.size() > 1) {
                                    v.stream().sorted(Comparator.comparing(File::getName)).limit(v.size() - 1).forEach(file->FileUtil.del(file.getPath()));
                                }
                            }
                        });
            }
        }

    }
}
