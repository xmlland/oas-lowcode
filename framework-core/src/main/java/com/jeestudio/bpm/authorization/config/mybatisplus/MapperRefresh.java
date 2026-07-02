package com.jeestudio.bpm.authorization.config.mybatisplus;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: Mapper热加载刷新
 */
@Slf4j
public class MapperRefresh {

    private Resource[] mapperLocations;
    private Configuration configuration;
    private SqlSessionFactory sqlSessionFactory;
    private Set<String> location;
    private Map<String, Long> lastUpdateTime = new HashMap<>();

    public MapperRefresh(Resource[] mapperLocations, SqlSessionFactory sqlSessionFactory) {
        this.mapperLocations = mapperLocations;
        this.sqlSessionFactory = sqlSessionFactory;
        this.configuration = sqlSessionFactory.getConfiguration();
        this.startThreadListener();
    }

    public void startThreadListener() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        //每5秒执行一次
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void start() {
        if (location == null) {
            location = new HashSet<>();
            for (Resource mapperLocation : mapperLocations) {
                String s = mapperLocation.toString().replaceAll("\\\\", "/");
                s = s.substring("file [".length(), s.lastIndexOf("]"));
                if (!location.contains(s)) {
                    location.add(s);
                    long time = FileUtil.lastModifiedTime(s).getTime();
                    lastUpdateTime.put(s, time);
                }
            }
            log.info("location size:{}", location.size());

        }
        for (String s : location) {
            Long beforeTime = lastUpdateTime.get(s);
            refresh(s, beforeTime);
        }

    }

    private void refresh(String filePath, Long beforeTime) {
        try {
            File file = new File(filePath);
            long lastModified = file.lastModified();
            if (lastModified == beforeTime) {
                return;
            }

            lastUpdateTime.put(filePath, lastModified);
            FileInputStream fileInputStream = new FileInputStream(filePath);
            boolean isSupper = configuration.getClass().getSuperclass() == Configuration.class;
            Field loadedResourcesField = isSupper ? configuration.getClass().getSuperclass().getDeclaredField("loadedResources")
                    : configuration.getClass().getDeclaredField("loadedResources");
            loadedResourcesField.setAccessible(true);
            Resource resource = new PathMatchingResourcePatternResolver().getResource(filePath);

            Set loadedResourcesSet = ((Set) loadedResourcesField.get(configuration));
            XPathParser xPathParser = new XPathParser(fileInputStream, true, configuration.getVariables(),
                    new XMLMapperEntityResolver());
            XNode context = xPathParser.evalNode("/mapper");
            String namespace = context.getStringAttribute("namespace");
            Field field = MapperRegistry.class.getDeclaredField("knownMappers");
            field.setAccessible(true);
            Map mapConfig = (Map) field.get(configuration.getMapperRegistry());
            mapConfig.remove(Resources.classForName(namespace));
            loadedResourcesSet.remove(resource.toString());
            configuration.getCacheNames().remove(namespace);
            List<String> mappedStatementIds = new ArrayList<>();
            List<XNode> children = context.getChildren();
            for (XNode child : children) {
                mappedStatementIds.add(namespace + "." + child.getStringAttribute("id"));
            }
            Field fieldMappedStatements = configuration.getClass().getDeclaredField("mappedStatements");
            fieldMappedStatements.setAccessible(true);
            Map map = (Map) fieldMappedStatements.get(configuration);
            for (String mappedStatementId : mappedStatementIds) {
                map.remove(mappedStatementId);
            }
            cleanParameterMap(context.evalNodes("/mapper/parameterMap"), namespace);
            cleanResultMap(context.evalNodes("/mapper/resultMap"), namespace);
            cleanKeyGenerators(context.evalNodes("insert|update|select"), namespace);
            cleanSqlElement(context.evalNodes("/mapper/sql"), namespace);
            fileInputStream = new FileInputStream(filePath);
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(fileInputStream,
                    sqlSessionFactory.getConfiguration(),
                    resource.toString(), sqlSessionFactory.getConfiguration().getSqlFragments());
            xmlMapperBuilder.parse();
            log.info("{} refresh success", filePath);
        } catch (Exception e) {
            log.error("{} refresh error", filePath, e);
        }
    }

    private void cleanParameterMap(List<XNode> list, String namespace) {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            configuration.getParameterMaps().remove(namespace + "." + id);
        }
    }

    private void cleanResultMap(List<XNode> list, String namespace) {
        for (XNode resultMapNode : list) {
            String id = resultMapNode.getStringAttribute("id", resultMapNode.getValueBasedIdentifier());
            configuration.getResultMapNames().remove(id);
            configuration.getResultMapNames().remove(namespace + "." + id);
            clearResultMap(resultMapNode, namespace);
        }
    }

    private void clearResultMap(XNode xNode, String namespace) {
        for (XNode resultChild : xNode.getChildren()) {
            if ("association".equals(resultChild.getName()) || "collection".equals(resultChild.getName())
                    || "case".equals(resultChild.getName())) {
                if (resultChild.getStringAttribute("select") == null) {
                    configuration.getResultMapNames().remove(
                            resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    configuration.getResultMapNames().remove(
                            namespace + "." + resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    if (resultChild.getChildren() != null && !resultChild.getChildren().isEmpty()) {
                        clearResultMap(resultChild, namespace);
                    }
                }
            }
        }
    }

    private void cleanKeyGenerators(List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getKeyGeneratorNames().remove(id + SelectKeyGenerator.SELECT_KEY_SUFFIX);
            configuration.getKeyGeneratorNames().remove(namespace + "." + id + SelectKeyGenerator.SELECT_KEY_SUFFIX);

            Collection<MappedStatement> mappedStatements = configuration.getMappedStatements();
            String k = namespace + "." + id;
            List<Object> objects = Stream.of(mappedStatements.toArray()).filter(p -> p instanceof MappedStatement && Objects.equals(((MappedStatement) p).getId(), k)).collect(Collectors.toList());
            mappedStatements.removeAll(objects);
        }
    }

    private void cleanSqlElement(List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getSqlFragments().remove(id);
            configuration.getSqlFragments().remove(namespace + "." + id);
        }
    }
}
