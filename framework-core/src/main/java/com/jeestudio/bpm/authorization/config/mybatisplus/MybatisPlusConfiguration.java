package com.jeestudio.bpm.authorization.config.mybatisplus;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: MyBatis-Plus配置
 */
@Configuration
@AutoConfigureAfter({DynamicDataSourceAutoConfiguration.class})
@ConfigurationProperties(prefix = "mybatis-plus")
@Slf4j
@Data
public class MybatisPlusConfiguration {

    private String[] mapperLocations;
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler();
    }
    @Bean
    public MapperRefresh mapperRefresh(SqlSessionFactory sqlSessionFactory) {
        List<Resource> resources = new ArrayList<>();
        try {
            for (String mapperLocation : mapperLocations) {
                for (Resource resource : new PathMatchingResourcePatternResolver().getResources(mapperLocation)) {
                    resources.add(resource);
                }
            }
        } catch (IOException e) {
            log.error(ExceptionUtil.stacktraceToString(e));
        }
        return new MapperRefresh(resources.toArray(new Resource[resources.size()]), sqlSessionFactory);
    }


    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DataSource dataSource) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        if (DynamicRoutingDataSource.class.equals(dataSource.getClass())){
            DynamicRoutingDataSource dynamicRoutingDataSource = (DynamicRoutingDataSource) dataSource;
            MyPaginationInnerInterceptor paginationInnerInterceptor = new MyPaginationInnerInterceptor(dynamicRoutingDataSource);
            interceptor.addInnerInterceptor(paginationInnerInterceptor);
        }else{
            PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
            interceptor.addInnerInterceptor(paginationInnerInterceptor);
        }
        return interceptor;
    }


}
