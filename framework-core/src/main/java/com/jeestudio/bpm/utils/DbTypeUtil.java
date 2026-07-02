package com.jeestudio.bpm.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;

/**
 * @Description: 数据库类型工具
 */
public class DbTypeUtil {

    public static String getDbType() {
        DynamicRoutingDataSource dataSource = SpringUtil.getBean(DynamicRoutingDataSource.class);

        ItemDataSource itemDataSource = (ItemDataSource) dataSource.determineDataSource();
        DruidDataSource druidDataSource = (DruidDataSource) itemDataSource.getRealDataSource();
        return druidDataSource.getDbType();


    }
}
