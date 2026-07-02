package com.jeestudio.bpm.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.jeestudio.bpm.utils.FlowableIdGen;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.engine.*;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * @Description: 工作流引擎配置
 */

@Configuration
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class ActivitiConfig {

    private static final Logger logger = LoggerFactory.getLogger(ActivitiConfig.class);

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration(@Autowired DataSource dataSource, PlatformTransactionManager transactionManagerBase) {
        SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setTransactionManager(transactionManagerBase);
        processEngineConfiguration.setDatabaseSchemaUpdate("true");
        processEngineConfiguration.setAsyncExecutorActivate(true);
        processEngineConfiguration.setHistoryLevel(HistoryLevel.FULL);
        processEngineConfiguration.setProcessDefinitionCacheLimit(10);
        processEngineConfiguration.setIdGenerator(new FlowableIdGen());
        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
        processEngineConfiguration.setDatabaseCatalog(getDatabaseCatalog(dataSource));
        processEngineConfiguration.setDatabaseSchema(getDatabaseSchema(dataSource));
        processEngineConfiguration.setDisableIdmEngine(true);
        processEngineConfiguration.setDisableEventRegistry(true);
        // Activiti 5 -> Flowable 6 迁移: 跳过 v5 流程定义校验
        // 升级脚本会将已有流程定义标记为 ENGINE_VERSION_='5', ValidateV5EntitiesCmd 会因此报错.
        // 禁用校验后, 由 fixPostgresColumnTypes() 中的 Fix 5 在后续启动时清除该标记.
        processEngineConfiguration.setValidateFlowable5EntitiesEnabled(false);

        // Activiti 5 -> Flowable 6 迁移: 修复 PostgreSQL 类型不兼容
        // Activiti 5 的 EXCLUSIVE_ 列为 numeric 类型，Flowable 6 升级脚本要求 boolean 类型
        fixPostgresColumnTypes(dataSource);

        // 国产数据库适配：检测并设置数据库类型
        String forcedDbType = detectDatabaseType(dataSource);
        if (forcedDbType != null) {
            processEngineConfiguration.setDatabaseType(forcedDbType);
        }

        return processEngineConfiguration;
    }

    /**
     * Activiti 5 -> Flowable 6 迁移前修复 (仅 PostgreSQL/KingbaseES):
     * 1. EXCLUSIVE_ 列 numeric -> boolean 类型转换
     * 2. 删除旧 ACT_IDX_* 索引, 避免 Flowable 升级脚本 CREATE INDEX 冲突
     * 3. 删除已有的 ACT_UNIQ_* 约束, 避免升级脚本重建冲突
     * 4. 预创建 Flowable 升级脚本期望能 DROP 的约束 (如 act_uniq_procdef),
     *    避免 PostgreSQL 因 DROP 不存在的约束而终止整个升级事务
     */
    private void fixPostgresColumnTypes(DataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                String dbProductName = metaData.getDatabaseProductName();
                if (dbProductName == null || (!dbProductName.contains("PostgreSQL")
                        && !dbProductName.contains("KingbaseES") && !dbProductName.contains("Kingbase"))) {
                    return;
                }

                // 每次启动都清除 ENGINE_VERSION_='5' 标记 (必须在引擎初始化前执行)
                // Flowable 升级脚本会将 Activiti 5 的流程定义标记为 ENGINE_VERSION_='5',
                // 如果引擎初始化时缓存了这些带 v5 标记的定义, 运行时 getBpmnModel() 等操作会报错.
                try {
                    boolean originalAutoCommit = connection.getAutoCommit();
                    connection.setAutoCommit(true);
                    Statement evStmt = connection.createStatement();
                    int updatedProcdef = evStmt.executeUpdate(
                            "UPDATE act_re_procdef SET engine_version_ = NULL WHERE engine_version_ = 'v5'");
                    int updatedDeployment = evStmt.executeUpdate(
                            "UPDATE act_re_deployment SET engine_version_ = NULL WHERE engine_version_ = 'v5'");
                    evStmt.close();
                    connection.setAutoCommit(originalAutoCommit);
                    if (updatedProcdef > 0 || updatedDeployment > 0) {
                        logger.info("Cleared ENGINE_VERSION_='v5': {} process definitions, {} deployments",
                                updatedProcdef, updatedDeployment);
                    } else {
                        logger.info("No ENGINE_VERSION_='v5' records found, skip cleanup");
                    }
                } catch (SQLException e) {
                    logger.warn("ENGINE_VERSION_ cleanup failed: {}", e.getMessage());
                }

                // 检查是否需要迁移: schema.version 未达到目标版本 6.8.1 时执行
                boolean needsMigration = false;
                try {
                    Statement checkStmt = connection.createStatement();
                    ResultSet versionRs = checkStmt.executeQuery(
                            "SELECT VALUE_ FROM ACT_GE_PROPERTY WHERE NAME_ = 'schema.version'");
                    if (versionRs.next()) {
                        String version = versionRs.getString(1);
                        needsMigration = version != null && !version.startsWith("6.8.");
                    }
                    versionRs.close();
                    checkStmt.close();
                } catch (SQLException e) {
                    logger.debug("ACT_GE_PROPERTY not found, skip migration fix: {}", e.getMessage());
                    return;
                }
                if (!needsMigration) {
                    return;
                }

                logger.info("Schema not yet at 6.8.x, applying pre-migration fixes for PostgreSQL...");

                // Fix 1: EXCLUSIVE_ 列 numeric -> boolean
                String[] colFixTables = {"act_ru_job", "act_ru_timer_job", "act_ru_suspended_job"};
                for (String table : colFixTables) {
                    try {
                        ResultSet rs = metaData.getColumns(null, null, table, "exclusive_");
                        if (rs.next()) {
                            String typeName = rs.getString("TYPE_NAME");
                            if (typeName != null && !typeName.equalsIgnoreCase("bool")
                                    && !typeName.equalsIgnoreCase("boolean")) {
                                logger.info("Migration fix: converting {}.EXCLUSIVE_ from {} to boolean", table, typeName);
                                Statement stmt = connection.createStatement();
                                stmt.execute("ALTER TABLE " + table
                                        + " ALTER COLUMN exclusive_ TYPE boolean USING (exclusive_::int::boolean)");
                                stmt.close();
                            }
                        }
                        rs.close();
                    } catch (SQLException e) {
                        logger.debug("Table {} column fix skipped: {}", table, e.getMessage());
                    }
                }

                // Fix 2: 删除所有 ACT_IDX_* 索引 (Flowable 升级脚本会重建)
                try {
                    Statement idxStmt = connection.createStatement();
                    ResultSet idxRs = idxStmt.executeQuery(
                            "SELECT indexname FROM pg_indexes WHERE schemaname = current_schema() "
                                    + "AND indexname LIKE 'act_idx_%'");
                    java.util.List<String> indexNames = new java.util.ArrayList<>();
                    while (idxRs.next()) {
                        indexNames.add(idxRs.getString(1));
                    }
                    idxRs.close();
                    idxStmt.close();

                    if (!indexNames.isEmpty()) {
                        logger.info("Migration fix: dropping {} existing ACT_IDX_* indexes to avoid conflicts", indexNames.size());
                        for (String idxName : indexNames) {
                            try {
                                Statement dropStmt = connection.createStatement();
                                dropStmt.execute("DROP INDEX IF EXISTS " + idxName);
                                dropStmt.close();
                            } catch (SQLException e) {
                                logger.debug("Failed to drop index {}: {}", idxName, e.getMessage());
                            }
                        }
                    }
                } catch (SQLException e) {
                    logger.warn("Failed to query/drop indexes: {}", e.getMessage());
                }

                // Fix 3: 删除所有 ACT_UNIQ_* 约束 (Flowable 升级脚本会先 DROP 再重建)
                try {
                    Statement conStmt = connection.createStatement();
                    ResultSet conRs = conStmt.executeQuery(
                            "SELECT tc.constraint_name, tc.table_name FROM information_schema.table_constraints tc "
                                    + "WHERE tc.constraint_schema = current_schema() "
                                    + "AND tc.table_name LIKE 'act_%' "
                                    + "AND tc.constraint_type = 'UNIQUE' "
                                    + "AND tc.constraint_name LIKE 'act_uniq_%'");
                    java.util.List<String[]> constraints = new java.util.ArrayList<>();
                    while (conRs.next()) {
                        constraints.add(new String[]{conRs.getString(1), conRs.getString(2)});
                    }
                    conRs.close();
                    conStmt.close();

                    if (!constraints.isEmpty()) {
                        logger.info("Migration fix: dropping {} ACT_UNIQ_* constraints to avoid conflicts", constraints.size());
                        for (String[] con : constraints) {
                            try {
                                Statement dropStmt = connection.createStatement();
                                dropStmt.execute("ALTER TABLE " + con[1] + " DROP CONSTRAINT IF EXISTS " + con[0]);
                                dropStmt.close();
                            } catch (SQLException e) {
                                logger.debug("Failed to drop constraint {}: {}", con[0], e.getMessage());
                            }
                        }
                    }
                } catch (SQLException e) {
                    logger.warn("Failed to query/drop constraints: {}", e.getMessage());
                }

                // Fix 4: 预创建 Flowable 升级脚本期望能 DROP 的约束
                // Flowable 6.3 升级脚本包含 DROP CONSTRAINT act_uniq_procdef (无 IF EXISTS),
                // 如果约束不存在(Activiti 5 未创建或前次升级中断), PostgreSQL 会报错并终止整个事务.
                // 解决: 在升级前预创建这些约束, 让 Flowable 的 DROP 能成功执行.
                String[][] expectedConstraints = {
                        {"act_re_procdef", "act_uniq_procdef", "key_, version_, tenant_id_"},
                };
                for (String[] ec : expectedConstraints) {
                    String tableName = ec[0];
                    String constraintName = ec[1];
                    String columns = ec[2];
                    try {
                        // 检查约束是否已存在
                        Statement checkStmt = connection.createStatement();
                        ResultSet checkRs = checkStmt.executeQuery(
                                "SELECT 1 FROM information_schema.table_constraints "
                                        + "WHERE constraint_schema = current_schema() "
                                        + "AND table_name = '" + tableName + "' "
                                        + "AND constraint_name = '" + constraintName + "'");
                        boolean exists = checkRs.next();
                        checkRs.close();
                        checkStmt.close();

                        if (!exists) {
                            logger.info("Migration fix: pre-creating constraint {} on {} so Flowable upgrade DROP succeeds",
                                    constraintName, tableName);
                            try {
                                Statement createStmt = connection.createStatement();
                                createStmt.execute("ALTER TABLE " + tableName
                                        + " ADD CONSTRAINT " + constraintName
                                        + " UNIQUE (" + columns + ")");
                                createStmt.close();
                            } catch (SQLException e) {
                                // 如果因重复数据无法创建 UNIQUE 约束, 则先去重再创建
                                logger.warn("Cannot create constraint {} (likely duplicate data): {}", constraintName, e.getMessage());
                                try {
                                    // 用 ctid 去重: 保留每组重复中 ctid 最小的行
                                    Statement dedupStmt = connection.createStatement();
                                    dedupStmt.execute("DELETE FROM " + tableName + " a USING " + tableName + " b "
                                            + "WHERE a.ctid > b.ctid AND a.key_ = b.key_ "
                                            + "AND a.version_ = b.version_ AND a.tenant_id_ = b.tenant_id_");
                                    dedupStmt.close();
                                    Statement retryStmt = connection.createStatement();
                                    retryStmt.execute("ALTER TABLE " + tableName
                                            + " ADD CONSTRAINT " + constraintName
                                            + " UNIQUE (" + columns + ")");
                                    retryStmt.close();
                                    logger.info("Migration fix: constraint {} created after dedup", constraintName);
                                } catch (SQLException e2) {
                                    logger.warn("Failed to create constraint {} even after dedup: {}", constraintName, e2.getMessage());
                                }
                            }
                        }
                    } catch (SQLException e) {
                        logger.debug("Check/create constraint {} skipped: {}", constraintName, e.getMessage());
                    }
                }

            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            logger.warn("Failed to apply pre-migration fixes: {}", e.getMessage());
        }
    }

    /**
     * 检测国产数据库类型并映射为 Flowable 支持的类型
     */
    private String detectDatabaseType(DataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                String dbProductName = metaData.getDatabaseProductName();
                if (dbProductName != null) {
                    if (dbProductName.contains("DM DBMS") || dbProductName.contains("DM")) {
                        return "oracle";
                    } else if (dbProductName.contains("KingbaseES") || dbProductName.contains("Kingbase")) {
                        return "postgres";
                    } else if (dbProductName.contains("GBase")) {
                        return "postgres";
                    }
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            logger.warn("Failed to detect database type: {}", e.getMessage());
        }
        return null;
    }

    public String getDatabaseCatalog(DataSource dataSource) {
        DynamicRoutingDataSource dynamicRoutingDataSource = (DynamicRoutingDataSource) dataSource;
        ItemDataSource itemDataSource = (ItemDataSource) dynamicRoutingDataSource.determineDataSource();
        DruidDataSource druidDataSource = (DruidDataSource) itemDataSource.getRealDataSource();
        if (druidDataSource.getUrl().toLowerCase().contains("SQLMODE=GBase".toLowerCase()) && "com.gbasedbt.jdbc.Driver".equals(druidDataSource.getDriverClassName())) {
            try {
                Connection connection = dataSource.getConnection();
                String catalog = connection.getCatalog();
                connection.close();
                return catalog;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public String getDatabaseSchema(DataSource dataSource) {
        DynamicRoutingDataSource dynamicRoutingDataSource = (DynamicRoutingDataSource) dataSource;
        ItemDataSource itemDataSource = (ItemDataSource) dynamicRoutingDataSource.determineDataSource();
        DruidDataSource druidDataSource = (DruidDataSource) itemDataSource.getRealDataSource();
        if (druidDataSource.getUrl().toLowerCase().contains("SQLMODE=Oracle".toLowerCase()) && "com.gbasedbt.jdbc.Driver".equals(druidDataSource.getDriverClassName())) {
            try {
                Connection connection = dataSource.getConnection();
                String schema = connection.getSchema();
                connection.close();
                return schema;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Bean
    public ProcessEngineFactoryBean processEngine(SpringProcessEngineConfiguration processEngineConfiguration) {
        ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
        processEngineFactoryBean.setProcessEngineConfiguration(processEngineConfiguration);
        return processEngineFactoryBean;
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngineFactoryBean processEngine) {
        RepositoryService repositoryService = null;
        try {
            repositoryService = processEngine.getObject().getRepositoryService();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return repositoryService;
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngineFactoryBean processEngine) {
        RuntimeService runtimeService = null;
        try {
            runtimeService = processEngine.getObject().getRuntimeService();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return runtimeService;
    }

    @Bean
    public FormService formService(ProcessEngineFactoryBean processEngine) {
        FormService formService = null;
        try {
            formService = processEngine.getObject().getFormService();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return formService;
    }

    @Bean
    public IdentityService identityService(ProcessEngineFactoryBean processEngine) {
        IdentityService identityService = null;
        try {
            identityService = processEngine.getObject().getIdentityService();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return identityService;
    }

    @Bean
    public TaskService taskService(ProcessEngineFactoryBean processEngine) {
        TaskService taskService = null;
        try {
            taskService = processEngine.getObject().getTaskService();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return taskService;
    }

    @Bean
    public HistoryService historyService(ProcessEngineFactoryBean processEngine) {
        HistoryService historyService = null;
        try {
            historyService = processEngine.getObject().getHistoryService();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return historyService;
    }

    @Bean
    public ManagementService managementService(ProcessEngineFactoryBean processEngine) {
        ManagementService managementService = null;
        try {
            managementService = processEngine.getObject().getManagementService();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return managementService;
    }
}
