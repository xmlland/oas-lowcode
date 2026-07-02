package com.jeestudio.bpm.authorization.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 长连接数据源配置
 */
@Configuration
@AutoConfigureAfter({DynamicDataSourceAutoConfiguration.class})
public class LongConnectionDataSourceConfig implements InitializingBean {

    @Autowired
    DynamicRoutingDataSource dynamicRoutingDataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        ItemDataSource itemDataSource = (ItemDataSource) dynamicRoutingDataSource.determineDataSource();
        DruidDataSource druidDataSource = (DruidDataSource) itemDataSource.getRealDataSource();
        DruidDataSource longConnection = druidDataSource.cloneDruidDataSource();
        String url = longConnection.getUrl();
        //替换url中的超时时间 &connectTimeout=xxx 或 ?connectTimeout=xxx

        if (url.contains("&connectTimeout=")) {
            url = url.replaceAll("&connectTimeout=\\d+", "&connectTimeout=3600");
        } else if (url.contains("?connectTimeout=")) {
            url = url.replaceAll("\\?connectTimeout=\\d+", "?connectTimeout=3600");
        }else if (url.contains("?")) {
            url = url + "&connectTimeout=3600";
        } else {
            url = url + "?connectTimeout=3600";

        }
        longConnection.setUrl(url);
        longConnection.setConnectTimeout(Integer.MAX_VALUE);
        longConnection.setMaxWait(Integer.MAX_VALUE);
        longConnection.setSocketTimeout(Integer.MAX_VALUE);
        longConnection.setRemoveAbandoned(false);
        longConnection.setTransactionQueryTimeout(Integer.MAX_VALUE);
        longConnection.setKillWhenSocketReadTimeout(false);

        longConnection.setName("longConnection");
        dynamicRoutingDataSource.addDataSource("longConnection", longConnection);
    }
}
