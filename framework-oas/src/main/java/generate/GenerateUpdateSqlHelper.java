package generate;

import cn.hutool.core.io.FileUtil;
import com.jeestudio.bpm.utils.DateUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import generate.dialect.DialectI;
import generate.dialect.PostgresqlDialect;
import generate.pojo.DbColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * @Description: 数据库增量更新SQL生成工具
 */
public class GenerateUpdateSqlHelper {
    static DialectI dialect;

    static String charter = "\"";

    private static void initDialect(String url) {
        if (url.contains("postgresql")) {
            dialect = new PostgresqlDialect();
        } else if (url.contains("opengauss")) {
            // 兼容说明：南大通用 8c、华为 GaussDB 使用 PostgreSQL 方言。
            dialect = new PostgresqlDialect();
        } else if (url.contains("kingbase8")) {
            dialect = new generate.dialect.Kingbase8Dialect();
        } else if (url.contains("gbase")) {
            dialect = new generate.dialect.GBaseDialect();
        } else if (url.contains("mysql")) {
            dialect = new generate.dialect.Mysql8Dialect();
            charter = "`";
        } else if (url.contains(":dm:")) {
            dialect = new generate.dialect.DM8Dialect();
        }
    }

    public static void generate(String url, String username, String password, String savePath, String tableNames) {

        initDialect(url);
        dialect.init(url, username, password);

        String time = DateUtil.getDateTime().replaceAll(":", "_") + System.currentTimeMillis();
        String file = savePath + "update" + time.replaceAll(" ", "") + ".sql";
        for (String s : tableNames.split(",")) {
            FileUtil.appendLines(generate(s), file, "utf-8");
        }
    }

    public static void generate(String url, String username, String password, String driver, String savePath, String tableNames) {
        String time = DateUtil.getDateTime().replaceAll(":", "_") + System.currentTimeMillis();
        String fileName = "update" + time.replaceAll(" ", "");
        generate(url, username, password, driver, savePath, tableNames, fileName);
    }

    public static void generate(String url, String username, String password, String driver, String savePath, String tableNames,String fileName) {
        initDialect(url);
        dialect.init(url, username, password, driver);
        String file = savePath + fileName + ".sql";
        for (String s : tableNames.split(",")) {
            FileUtil.appendLines(generate(s), file, "utf-8");
        }
    }


    private static List<String> generate(String tableName) {
        if (tableName == null || tableName.trim().length() == 0) {
            return new ArrayList<>();
        }
        int batchSize = 1000;
        if (tableName.contains("gen_table_column")) {
            batchSize = 20;
        }
        if (tableName.contains("gen_table")) {
            batchSize = 5;
        }

        List<DbColumn> dbColumns = dialect.selectColumns(tableName);
        StringBuilder insert = new StringBuilder();
        String insertPrefix = "insert into " + charter + tableName + charter + "(";
        String insertSuffix = ") values";
        insert.append(insertPrefix);
        for (int i = 0; i < dbColumns.size(); i++) {
            DbColumn dbColumn = dbColumns.get(i);
            insert.append(charter);
            insert.append(dbColumn.getColumn_name());
            insert.append(charter);
            if (i < dbColumns.size() - 1) {
                insert.append(",");
            }
        }
        insert.append(insertSuffix);
        List<Supplier<List<Map<String, Object>>>> supplierList = dialect.selectData(tableName, charter);
        List<String> sqlListRes = new ArrayList<>();
        List<List<String>> sqlListArr = new ArrayList<>(supplierList.size());
        sqlListRes.add("delete from " + charter + tableName + charter+ ";");
        int finalBatchSize = batchSize;
        List<List<String>> valuesListArr = new ArrayList<>(supplierList.size());
        AtomicLong total = new AtomicLong();
        //线程池
        final CountDownLatch countDownLatch = new CountDownLatch(supplierList.size()); //
        ExecutorService executor = Executors.newFixedThreadPool(1);
        AtomicInteger k = new AtomicInteger(0);
        for (Supplier<List<Map<String, Object>>> supplier : supplierList) {
            executor.submit(() -> {
                try {
                    List<String> sqlList = new ArrayList<>();
                    List<String> valuesList = new ArrayList<>();
                    List<Map<String, Object>> maps = supplier.get();
                    maps.forEach(map -> {

                        StringBuilder valuesSql = new StringBuilder();
                        valuesSql.append("(");
                        for (int i = 0; i < dbColumns.size(); i++) {
                            DbColumn dbColumn = dbColumns.get(i);
                            if (!map.containsKey(dbColumn.getColumn_name())) {
                                valuesSql.append("null");
                                if (i < dbColumns.size() - 1) {
                                    valuesSql.append(",");
                                }
                                continue;
                            }
                            Object value = map.get(dbColumn.getColumn_name());
                            if (value == null) {
                                valuesSql.append("null");
                            } else {
                                valuesSql.append("'");
                                valuesSql.append(ConvertUtil.getString(value).replaceAll("'", "''"));
                                valuesSql.append("'");
                            }
                            if (i < dbColumns.size() - 1) {
                                valuesSql.append(",");
                            }
                        }
                        valuesSql.append(")");
                        valuesList.add(valuesSql.toString());
                        if (valuesList.size() >= finalBatchSize) {
                            StringBuilder sql = new StringBuilder();
                            sql.append(insert);
                            sql.append(String.join(",", valuesList));
                            sql.append(";");
                            sqlList.add(sql.toString());
                            valuesList.clear();
                        }
                    });
                    total.addAndGet(maps.size());
                    sqlListArr.add(sqlList);
                    valuesListArr.add(k.get(), valuesList);
                } finally {
                    k.getAndIncrement();
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();
        System.out.println("======================>表" + tableName + "共" + total.get() + "条数据");
        int k0 = 0;
        for (List<String> valuesList : valuesListArr) {
            List<String> sqlList = sqlListArr.get(k0);
            if (valuesList != null && valuesList.size() > 0) {
                StringBuilder sql = new StringBuilder();
                sql.append(insert);
                sql.append(String.join(",", valuesList));
                sql.append(";");
                sqlList.add(sql.toString());
                valuesList.clear();
            }
            k0++;
        }
        for (List<String> sqlList : sqlListArr) {
            sqlListRes.addAll(sqlList);
        }
        return sqlListRes;
    }
}
