package com.alibaba.druid.util;

import com.alibaba.druid.DbType;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * @Description: Druid JDBC 工具扩展类
 */
public class JdbcUtils implements JdbcConstants {
    private static final Log LOG = LogFactory.getLog(JdbcUtils.class);

    private static final Properties DRIVER_URL_MAPPING = new Properties();

    private static Boolean mysql_driver_version_6;

    private static Boolean clickhouse_driver_version_new;

    static {
        try {
            ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
            Properties property;
            if (ctxClassLoader != null) {
                for(Enumeration<URL> e = ctxClassLoader.getResources("META-INF/druid-driver.properties"); e.hasMoreElements(); DRIVER_URL_MAPPING.putAll(property)) {
                    URL url = e.nextElement();
                    property = new Properties();
                    InputStream is = null;

                    try {
                        is = url.openStream();
                        property.load(is);
                    } finally {
                        close(is);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("load druid-driver.properties error", e);
        }
    }

    public static void close(Connection x) {
        if (x != null) {
            try {
                if (x.isClosed()) {
                    return;
                }

                x.close();
            } catch (SQLRecoverableException var2) {
            } catch (Exception e) {
                LOG.debug("close connection error", e);
            }

        }
    }

    public static void close(Statement x) {
        if (x != null) {
            try {
                x.close();
            } catch (Exception var3) {
                boolean printError = true;
                if (var3 instanceof SQLRecoverableException && "Closed Connection".equals(var3.getMessage())) {
                    printError = false;
                }

                if (printError) {
                    LOG.debug("close statement error", var3);
                }
            }

        }
    }

    public static void close(ResultSet x) {
        if (x != null) {
            try {
                x.close();
            } catch (Exception e) {
                LOG.debug("close result set error", e);
            }

        }
    }

    public static void close(Closeable x) {
        if (x != null) {
            try {
                x.close();
            } catch (Exception e) {
                LOG.debug("close error", e);
            }

        }
    }

    public static void close(Blob x) {
        if (x != null) {
            try {
                x.free();
            } catch (Exception e) {
                LOG.debug("close error", e);
            }

        }
    }

    public static void close(Clob x) {
        if (x != null) {
            try {
                x.free();
            } catch (Exception e) {
                LOG.debug("close error", e);
            }

        }
    }

    public static void printResultSet(ResultSet rs) throws SQLException {
        printResultSet(rs, System.out);
    }

    public static void printResultSet(ResultSet rs, PrintStream out) throws SQLException {
        printResultSet(rs, out, true, "\t");
    }

    public static void printResultSet(ResultSet rs,
                                      PrintStream out,
                                      boolean printHeader,
                                      String seperator) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
        if (printHeader) {
            for(int columnIndex = 1; columnIndex <= columnCount; ++columnIndex) {
                if (columnIndex != 1) {
                    out.print(seperator);
                }
                out.print(metadata.getColumnName(columnIndex));
            }
        }

        out.println();

        while(rs.next()) {
            for(int columnIndex = 1; columnIndex <= columnCount; ++columnIndex) {
                if (columnIndex != 1) {
                    out.print(seperator);
                }

                int type = metadata.getColumnType(columnIndex);
                if (type != Types.VARCHAR && type != Types.CHAR && type != Types.NVARCHAR && type != Types.NCHAR) {
                    if (type == Types.DATE) {
                        Date date = rs.getDate(columnIndex);
                        if (rs.wasNull()) {
                            out.print("null");
                        } else {
                            out.print(date.toString());
                        }
                    } else if (type == Types.BIT) {
                        boolean value = rs.getBoolean(columnIndex);
                        if (rs.wasNull()) {
                            out.print("null");
                        } else {
                            out.print(Boolean.toString(value));
                        }
                    } else if (type == Types.BOOLEAN) {
                        boolean value = rs.getBoolean(columnIndex);
                        if (rs.wasNull()) {
                            out.print("null");
                        } else {
                            out.print(Boolean.toString(value));
                        }
                    } else if (type == Types.TINYINT) {
                        byte value = rs.getByte(columnIndex);
                        if (rs.wasNull()) {
                            out.print("null");
                        } else {
                            out.print(Byte.toString(value));
                        }
                    } else if (type == Types.SMALLINT) {
                        short value = rs.getShort(columnIndex);
                        if (rs.wasNull()) {
                            out.print("null");
                        } else {
                            out.print(Short.toString(value));
                        }
                    } else if (type == Types.INTEGER) {
                        int value = rs.getInt(columnIndex);
                        if (rs.wasNull()) {
                            out.print("null");
                        } else {
                            out.print(Integer.toString(value));
                        }
                    } else if (type == Types.BIGINT) {
                        long value = rs.getLong(columnIndex);
                        if (rs.wasNull()) {
                            out.print("null");
                        } else {
                            out.print(Long.toString(value));
                        }
                    } else if (type != Types.TIMESTAMP && type != Types.TIMESTAMP_WITH_TIMEZONE) {
                        if (type == Types.DECIMAL) {
                            out.print(String.valueOf(rs.getBigDecimal(columnIndex)));
                        } else if (type == Types.CLOB) {
                            out.print(String.valueOf(rs.getString(columnIndex)));
                        } else if (type == Types.JAVA_OBJECT) {
                            Object object = rs.getObject(columnIndex);
                            if (rs.wasNull()) {
                                out.print("null");
                            } else {
                                out.print(String.valueOf(object));
                            }
                        } else if (type == Types.LONGVARCHAR) {
                            Object object = rs.getString(columnIndex);
                            if (rs.wasNull()) {
                                out.print("null");
                            } else {
                                out.print(String.valueOf(object));
                            }
                        } else if (type == Types.NULL) {
                            out.print("null");
                        } else {
                            Object object = rs.getObject(columnIndex);
                            if (rs.wasNull()) {
                                out.print("null");
                            } else if (object instanceof byte[]) {
                                byte[] bytes = (byte[])object;
                                String text = HexBin.encode(bytes);
                                out.print(text);
                            } else {
                                out.print(String.valueOf(object));
                            }
                        }
                    } else {
                        out.print(String.valueOf(rs.getTimestamp(columnIndex)));
                    }
                } else {
                    out.print(rs.getString(columnIndex));
                }
            }
            out.println();
        }
    }

    public static String getTypeName(int sqlType) {
        switch (sqlType) {
            case Types.LONGNVARCHAR:
                return "LONGNVARCHAR";
            case Types.NCHAR:
                return "NCHAR";
            case Types.NVARCHAR:
                return "NVARCHAR";
            case Types.ROWID:
                return "ROWID";
            case Types.BIT:
                return "BIT";
            case Types.TINYINT:
                return "TINYINT";
            case Types.BIGINT:
                return "BIGINT";
            case Types.LONGVARBINARY:
                return "LONGVARBINARY";
            case Types.VARBINARY:
                return "VARBINARY";
            case Types.BINARY:
                return "BINARY";
            case Types.NULL:
                return "NULL";
            case Types.CHAR:
                return "CHAR";
            case Types.NUMERIC:
                return "NUMERIC";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.INTEGER:
                return "INTEGER";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.FLOAT:
                return "FLOAT";
            case Types.REAL:
                return "REAL";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.VARCHAR:
                return "VARCHAR";
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.DATALINK:
                return "DATALINK";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.JAVA_OBJECT:
                return "JAVA_OBJECT";
            case Types.DISTINCT:
                return "DISTINCT";
            case Types.STRUCT:
                return "STRUCT";
            case Types.ARRAY:
                return "ARRAY";
            case Types.BLOB:
                return "BLOB";
            case Types.CLOB:
                return "CLOB";
            case Types.REF:
                return "REF";
            case Types.SQLXML:
                return "SQLXML";
            case Types.NCLOB:
                return "NCLOB";
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "TIMESTAMP_WITH_TIMEZONE";
            default:
                return "OTHER";
        }
    }

    public static String getDriverClassName(String rawUrl) throws SQLException {
        if (rawUrl == null) {
            return null;
        } else if (rawUrl.startsWith("jdbc:derby:")) {
            return "org.apache.derby.jdbc.EmbeddedDriver";
        } else if (rawUrl.startsWith("jdbc:mysql:")) {
            if (mysql_driver_version_6 == null) {
                mysql_driver_version_6 = Utils.loadClass(MYSQL_DRIVER_6) != null;
            }

            return mysql_driver_version_6 ? MYSQL_DRIVER_6 : MYSQL_DRIVER;
        } else if (rawUrl.startsWith("jdbc:log4jdbc:")) {
            return JdbcConstants.LOG4JDBC_DRIVER;
        } else if (rawUrl.startsWith("jdbc:mariadb:")) {
            return JdbcConstants.MARIADB_DRIVER;
        } else if (rawUrl.startsWith("jdbc:tidb:")) {
            return JdbcConstants.TIDB_DRIVER;
        } else if (!rawUrl.startsWith("jdbc:oracle:") && !rawUrl.startsWith("JDBC:oracle:")) {
            if (rawUrl.startsWith("jdbc:alibaba:oracle:")) {
                return JdbcConstants.ALI_ORACLE_DRIVER;
            } else if (rawUrl.startsWith("jdbc:oceanbase:")) {
                return JdbcConstants.OCEANBASE_DRIVER;
            } else if (rawUrl.startsWith("jdbc:microsoft:")) {
                return JdbcConstants.SQL_SERVER_DRIVER;
            } else if (rawUrl.startsWith("jdbc:sqlserver:")) {
                return JdbcConstants.SQL_SERVER_DRIVER_SQLJDBC4;
            } else if (rawUrl.startsWith("jdbc:sybase:Tds:")) {
                return "com.sybase.jdbc2.jdbc.SybDriver";
            } else if (rawUrl.startsWith("jdbc:jtds:")) {
                return JdbcConstants.SQL_SERVER_DRIVER_JTDS;
            } else if (!rawUrl.startsWith("jdbc:fake:") && !rawUrl.startsWith("jdbc:mock:")) {
                if (rawUrl.startsWith("jdbc:postgresql:")) {
                    return JdbcConstants.POSTGRESQL_DRIVER;
                } else if (rawUrl.startsWith("jdbc:edb:")) {
                    return JdbcConstants.ENTERPRISEDB_DRIVER;
                } else if (rawUrl.startsWith("jdbc:odps:")) {
                    return JdbcConstants.ODPS_DRIVER;
                } else if (rawUrl.startsWith("jdbc:hsqldb:")) {
                    return "org.hsqldb.jdbcDriver";
                } else if (rawUrl.startsWith("jdbc:db2:")) {
                    String prefix = "jdbc:db2:";
                    if (rawUrl.startsWith(prefix + "//")) {
                        return JdbcConstants.DB2_DRIVER;
                    } else {
                        String suffix = rawUrl.substring(prefix.length());
                        return suffix.indexOf(58) > 0 ? DB2_DRIVER3 : DB2_DRIVER2;
                    }
                } else if (rawUrl.startsWith("jdbc:sqlite:")) {
                    return JdbcConstants.SQLITE_DRIVER;
                } else if (rawUrl.startsWith("jdbc:ingres:")) {
                    return "com.ingres.jdbc.IngresDriver";
                } else if (rawUrl.startsWith("jdbc:h2:")) {
                    return JdbcConstants.H2_DRIVER;
                } else if (rawUrl.startsWith("jdbc:mckoi:")) {
                    return "com.mckoi.JDBCDriver";
                } else if (rawUrl.startsWith("jdbc:cloudscape:")) {
                    return "COM.cloudscape.core.JDBCDriver";
                } else if (rawUrl.startsWith("jdbc:informix-sqli:")) {
                    return "com.informix.jdbc.IfxDriver";
                } else if (rawUrl.startsWith("jdbc:timesten:")) {
                    return "com.timesten.jdbc.TimesTenDriver";
                } else if (rawUrl.startsWith("jdbc:as400:")) {
                    return "com.ibm.as400.access.AS400JDBCDriver";
                } else if (rawUrl.startsWith("jdbc:sapdb:")) {
                    return "com.sap.dbtech.jdbc.DriverSapDB";
                } else if (rawUrl.startsWith("jdbc:JSQLConnect:")) {
                    return "com.jnetdirect.jsql.JSQLDriver";
                } else if (rawUrl.startsWith("jdbc:JTurbo:")) {
                    return "com.newatlanta.jturbo.driver.Driver";
                } else if (rawUrl.startsWith("jdbc:firebirdsql:")) {
                    return "org.firebirdsql.jdbc.FBDriver";
                } else if (rawUrl.startsWith("jdbc:interbase:")) {
                    return "interbase.interclient.Driver";
                } else if (rawUrl.startsWith("jdbc:pointbase:")) {
                    return "com.pointbase.jdbc.jdbcUniversalDriver";
                } else if (rawUrl.startsWith("jdbc:edbc:")) {
                    return "ca.edbc.jdbc.EdbcDriver";
                } else if (rawUrl.startsWith("jdbc:mimer:multi1:")) {
                    return "com.mimer.jdbc.Driver";
                } else if (rawUrl.startsWith("jdbc:dm:")) {
                    return JdbcConstants.DM_DRIVER;
                } else if (rawUrl.startsWith("jdbc:kingbase:")) {
                    return JdbcConstants.KINGBASE_DRIVER;
                } else if (rawUrl.startsWith("jdbc:kingbase8:")) {
                    return JdbcConstants.KINGBASE8_DRIVER;
                } else if (rawUrl.startsWith("jdbc:gbase:")) {
                    return JdbcConstants.GBASE_DRIVER;
                } else if (rawUrl.startsWith("jdbc:xugu:")) {
                    return JdbcConstants.XUGU_DRIVER;
                } else if (rawUrl.startsWith("jdbc:hive:")) {
                    return JdbcConstants.HIVE_DRIVER;
                } else if (rawUrl.startsWith("jdbc:hive2:")) {
                    return JdbcConstants.HIVE_DRIVER;
                } else if (rawUrl.startsWith("jdbc:phoenix:thin:")) {
                    return "org.apache.phoenix.queryserver.client.Driver";
                } else if (rawUrl.startsWith("jdbc:phoenix://")) {
                    return JdbcConstants.PHOENIX_DRIVER;
                } else if (rawUrl.startsWith("jdbc:kylin:")) {
                    return JdbcConstants.KYLIN_DRIVER;
                } else if (rawUrl.startsWith("jdbc:elastic:")) {
                    return JdbcConstants.ELASTIC_SEARCH_DRIVER;
                } else if (rawUrl.startsWith("jdbc:clickhouse:")) {
                    if (clickhouse_driver_version_new == null) {
                        clickhouse_driver_version_new = Utils.loadClass(CLICKHOUSE_DRIVER_NEW) != null;
                    }

                    return clickhouse_driver_version_new ? CLICKHOUSE_DRIVER_NEW : CLICKHOUSE_DRIVER;
                } else if (rawUrl.startsWith("jdbc:presto:")) {
                    return JdbcConstants.PRESTO_DRIVER;
                } else if (rawUrl.startsWith("jdbc:trino:")) {
                    return JdbcConstants.TRINO_DRIVER;
                } else if (rawUrl.startsWith("jdbc:inspur:")) {
                    return JdbcConstants.KDB_DRIVER;
                } else if (rawUrl.startsWith("jdbc:polardb")) {
                    return JdbcConstants.POLARDB_DRIVER;
                } else if (rawUrl.startsWith("jdbc:highgo:")) {
                    return "com.highgo.jdbc.Driver";
                } else if (rawUrl.startsWith("jdbc:oscar")) {
                    return JdbcConstants.OSCAR_DRIVER;
                } else if (rawUrl.startsWith("jdbc:dbcp:")) {
                    return JdbcConstants.TYDB_DRIVER;
                } else if (rawUrl.startsWith("jdbc:opengauss:")) {
                    return "org.opengauss.Driver";
                } else if (rawUrl.startsWith("jdbc:TAOS:")) {
                    return JdbcConstants.TAOS_DATA;
                } else if (rawUrl.startsWith("jdbc:TAOS-RS:")) {
                    return JdbcConstants.TAOS_DATA_RS;
                } else if (rawUrl.startsWith("jdbc:gbasedbt-sqli:")) {
                    return JdbcConstants.GBASE8S_DRIVER;
                } else {
                    throw new SQLException("unknown jdbc driver : " + rawUrl);
                }
            } else {
                return "com.alibaba.druid.mock.MockDriver";
            }
        } else {
            return JdbcConstants.ORACLE_DRIVER;
        }
    }

    public static DbType getDbTypeRaw(String rawUrl, String driverClassName) {
        if (rawUrl == null) {
            return null;
        } else if (!rawUrl.startsWith("jdbc:derby:") && !rawUrl.startsWith("jdbc:log4jdbc:derby:")) {
            if (!rawUrl.startsWith("jdbc:mysql:") && !rawUrl.startsWith("jdbc:cobar:") && !rawUrl.startsWith("jdbc:log4jdbc:mysql:")) {
                if (rawUrl.startsWith("jdbc:goldendb:")) {
                    return DbType.mysql;
                } else if (rawUrl.startsWith("jdbc:mariadb:")) {
                    return DbType.mariadb;
                } else if (rawUrl.startsWith("jdbc:tidb:")) {
                    return DbType.tidb;
                } else if (!rawUrl.startsWith("jdbc:oracle:") && !rawUrl.startsWith("jdbc:log4jdbc:oracle:")) {
                    if (rawUrl.startsWith("jdbc:alibaba:oracle:")) {
                        return DbType.ali_oracle;
                    } else if (rawUrl.startsWith("jdbc:oceanbase:oracle:")) {
                        return DbType.oceanbase_oracle;
                    } else if (rawUrl.startsWith("jdbc:oceanbase:")) {
                        return DbType.oceanbase;
                    } else if (!rawUrl.startsWith("jdbc:microsoft:") && !rawUrl.startsWith("jdbc:log4jdbc:microsoft:")) {
                        if (!rawUrl.startsWith("jdbc:sqlserver:") && !rawUrl.startsWith("jdbc:log4jdbc:sqlserver:")) {
                            if (!rawUrl.startsWith("jdbc:sybase:Tds:") && !rawUrl.startsWith("jdbc:log4jdbc:sybase:")) {
                                if (!rawUrl.startsWith("jdbc:jtds:") && !rawUrl.startsWith("jdbc:log4jdbc:jtds:")) {
                                    if (!rawUrl.startsWith("jdbc:fake:") && !rawUrl.startsWith("jdbc:mock:")) {
                                        if (!rawUrl.startsWith("jdbc:postgresql:") && !rawUrl.startsWith("jdbc:log4jdbc:postgresql:")) {
                                            if (rawUrl.startsWith("jdbc:edb:")) {
                                                return DbType.edb;
                                            } else if (!rawUrl.startsWith("jdbc:hsqldb:") && !rawUrl.startsWith("jdbc:log4jdbc:hsqldb:")) {
                                                if (rawUrl.startsWith("jdbc:odps:")) {
                                                    return DbType.odps;
                                                } else if (rawUrl.startsWith("jdbc:db2:")) {
                                                    return DbType.db2;
                                                } else if (rawUrl.startsWith("jdbc:sqlite:")) {
                                                    return DbType.sqlite;
                                                } else if (rawUrl.startsWith("jdbc:ingres:")) {
                                                    return DbType.ingres;
                                                } else if (!rawUrl.startsWith("jdbc:h2:") && !rawUrl.startsWith("jdbc:log4jdbc:h2:")) {
                                                    if (rawUrl.startsWith("jdbc:mckoi:")) {
                                                        return DbType.mock;
                                                    } else if (rawUrl.startsWith("jdbc:cloudscape:")) {
                                                        return DbType.cloudscape;
                                                    } else if (!rawUrl.startsWith("jdbc:informix-sqli:") && !rawUrl.startsWith("jdbc:log4jdbc:informix-sqli:")) {
                                                        if (rawUrl.startsWith("jdbc:timesten:")) {
                                                            return DbType.timesten;
                                                        } else if (rawUrl.startsWith("jdbc:as400:")) {
                                                            return DbType.as400;
                                                        } else if (rawUrl.startsWith("jdbc:sapdb:")) {
                                                            return DbType.sapdb;
                                                        } else if (rawUrl.startsWith("jdbc:JSQLConnect:")) {
                                                            return DbType.JSQLConnect;
                                                        } else if (rawUrl.startsWith("jdbc:JTurbo:")) {
                                                            return DbType.JTurbo;
                                                        } else if (rawUrl.startsWith("jdbc:firebirdsql:")) {
                                                            return DbType.firebirdsql;
                                                        } else if (rawUrl.startsWith("jdbc:interbase:")) {
                                                            return DbType.interbase;
                                                        } else if (rawUrl.startsWith("jdbc:pointbase:")) {
                                                            return DbType.pointbase;
                                                        } else if (rawUrl.startsWith("jdbc:edbc:")) {
                                                            return DbType.edbc;
                                                        } else if (rawUrl.startsWith("jdbc:mimer:multi1:")) {
                                                            return DbType.mimer;
                                                        } else if (rawUrl.startsWith("jdbc:dm:")) {
                                                            return JdbcConstants.DM;
                                                        } else if (!rawUrl.startsWith("jdbc:kingbase:") && !rawUrl.startsWith("jdbc:kingbase8:")) {
                                                            if (rawUrl.startsWith("jdbc:gbase:")) {
                                                                return JdbcConstants.GBASE;
                                                            } else if (rawUrl.startsWith("jdbc:xugu:")) {
                                                                return JdbcConstants.XUGU;
                                                            } else if (rawUrl.startsWith("jdbc:log4jdbc:")) {
                                                                return DbType.log4jdbc;
                                                            } else if (rawUrl.startsWith("jdbc:hive:")) {
                                                                return DbType.hive;
                                                            } else if (rawUrl.startsWith("jdbc:hive2:")) {
                                                                return DbType.hive;
                                                            } else if (rawUrl.startsWith("jdbc:phoenix:")) {
                                                                return DbType.phoenix;
                                                            } else if (rawUrl.startsWith("jdbc:kylin:")) {
                                                                return DbType.kylin;
                                                            } else if (rawUrl.startsWith("jdbc:elastic:")) {
                                                                return DbType.elastic_search;
                                                            } else if (rawUrl.startsWith("jdbc:clickhouse:")) {
                                                                return DbType.clickhouse;
                                                            } else if (rawUrl.startsWith("jdbc:presto:")) {
                                                                return DbType.presto;
                                                            } else if (rawUrl.startsWith("jdbc:trino:")) {
                                                                return DbType.trino;
                                                            } else if (rawUrl.startsWith("jdbc:inspur:")) {
                                                                return DbType.kdb;
                                                            } else if (rawUrl.startsWith("jdbc:polardb")) {
                                                                return DbType.polardb;
                                                            } else if (rawUrl.startsWith("jdbc:highgo:")) {
                                                                return DbType.highgo;
                                                            } else if (!rawUrl.startsWith("jdbc:pivotal:greenplum:") && !rawUrl.startsWith("jdbc:datadirect:greenplum:")) {
                                                                if (!rawUrl.startsWith("jdbc:opengauss:") && !rawUrl.startsWith("jdbc:gaussdb:") && !rawUrl.startsWith("jdbc:dws:iam:")) {
                                                                    return !rawUrl.startsWith("jdbc:TAOS:") && !rawUrl.startsWith("jdbc:TAOS-RS:") ? null : DbType.taosdata;
                                                                } else {
                                                                    return DbType.gaussdb;
                                                                }
                                                            } else {
                                                                return DbType.greenplum;
                                                            }
                                                        } else {
                                                            return JdbcConstants.KINGBASE;
                                                        }
                                                    } else {
                                                        return DbType.informix;
                                                    }
                                                } else {
                                                    return DbType.h2;
                                                }
                                            } else {
                                                return DbType.hsql;
                                            }
                                        } else {
                                            return DbType.postgresql;
                                        }
                                    } else {
                                        return DbType.mock;
                                    }
                                } else {
                                    return DbType.jtds;
                                }
                            } else {
                                return DbType.sybase;
                            }
                        } else {
                            return DbType.sqlserver;
                        }
                    } else {
                        return DbType.sqlserver;
                    }
                } else {
                    return DbType.oracle;
                }
            } else {
                return DbType.mysql;
            }
        } else {
            return DbType.derby;
        }
    }

    public static String getDbType(String rawUrl, String driverClassName) {
        DbType dbType = getDbTypeRaw(rawUrl, driverClassName);
        return dbType == null ? null : dbType.name();
    }

    public static Driver createDriver(String driverClassName) throws SQLException {
        return createDriver(null, driverClassName);
    }

    public static Driver createDriver(ClassLoader classLoader, String driverClassName) throws SQLException {
        Class<?> clazz = null;
        if (classLoader != null) {
            try {
                clazz = classLoader.loadClass(driverClassName);
            } catch (ClassNotFoundException var8) {
            }
        }

        if (clazz == null) {
            try {
                ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
                if (contextLoader != null) {
                    clazz = contextLoader.loadClass(driverClassName);
                }
            } catch (ClassNotFoundException var7) {
            }
        }

        if (clazz == null) {
            try {
                clazz = Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                throw new SQLException(e.getMessage(), e);
            }
        }

        try {
            return (Driver) clazz.newInstance();
        } catch (IllegalAccessException e) {
            throw new SQLException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    public static int executeUpdate(DataSource dataSource, String sql, Object... parameters) throws SQLException {
        return executeUpdate(dataSource, sql, Arrays.asList(parameters));
    }

    public static int executeUpdate(DataSource dataSource, String sql, List<Object> parameters) throws SQLException {
        Connection conn = null;

        int var4;
        try {
            conn = dataSource.getConnection();
            var4 = executeUpdate(conn, sql, parameters);
        } finally {
            close(conn);
        }

        return var4;
    }

    public static int executeUpdate(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;

        int updateCount;
        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            updateCount = stmt.executeUpdate();
        } finally {
            close(stmt);
        }

        return updateCount;
    }

    public static void execute(DataSource dataSource, String sql, Object... parameters) throws SQLException {
        execute(dataSource, sql, Arrays.asList(parameters));
    }

    public static void execute(DataSource dataSource, String sql, List<Object> parameters) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            execute(conn, sql, parameters);
        } finally {
            close(conn);
        }
    }

    public static void execute(Connection conn, String sql) throws SQLException {
        execute(conn, sql, Collections.emptyList());
    }

    public static void execute(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            stmt.executeUpdate();
        } finally {
            close(stmt);
        }
    }

    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, Object... parameters)
            throws SQLException {
        return executeQuery(dataSource, sql, Arrays.asList(parameters));
    }

    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, List<Object> parameters)
            throws SQLException {
        Connection conn = null;

        List var4;
        try {
            conn = dataSource.getConnection();
            var4 = executeQuery(conn, sql, parameters);
        } finally {
            close(conn);
        }

        return var4;
    }

    public static List<Map<String, Object>> executeQuery(Connection conn, String sql, List<Object> parameters)
            throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            rs = stmt.executeQuery();

            ResultSetMetaData rsMeta = rs.getMetaData();

            while(rs.next()) {
                Map<String, Object> row = new LinkedHashMap();
                int i = 0;

                for(int size = rsMeta.getColumnCount(); i < size; ++i) {
                    String columName = rsMeta.getColumnLabel(i + 1);
                    Object value = rs.getObject(i + 1);
                    row.put(columName, value);
                }

                rows.add(row);
            }
        } finally {
            close(rs);
            close(stmt);
        }

        return rows;
    }

    static void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        int i = 0;

        for(int size = parameters.size(); i < size; ++i) {
            Object param = parameters.get(i);
            stmt.setObject(i + 1, param);
        }
    }

    public static void insertToTable(DataSource dataSource, String tableName, Map<String, Object> data)
            throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            insertToTable(conn, tableName, data);
        } finally {
            close(conn);
        }
    }

    public static void insertToTable(Connection conn, String tableName, Map<String, Object> data) throws SQLException {
        String sql = makeInsertToTableSql(tableName, data.keySet());
        List<Object> parameters = new ArrayList<Object>(data.values());
        execute(conn, sql, parameters);
    }

    public static String makeInsertToTableSql(String tableName, Collection<String> names) {
        StringBuilder sql = new StringBuilder() //
                .append("insert into ") //
                .append(tableName) //
                .append("("); //

        int nameCount = 0;
        for (String name : names) {
            if (nameCount > 0) {
                sql.append(",");
            }
            sql.append(name);
            ++nameCount;
        }
        sql.append(") values (");
        for (int i = 0; i < nameCount; ++i) {
            if (i != 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");

        return sql.toString();
    }

    public static <T> void executeQuery(
            DataSource dataSource,
            ResultSetConsumer<T> consumer,
            String sql,
            Object... parameters
    ) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < parameters.length; ++i) {
                stmt.setObject(i + 1, parameters[i]);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                if (consumer != null) {
                    T object = consumer.apply(rs);
                    consumer.accept(object);
                }
            }
        } finally {
            close(rs);
            close(stmt);
            close(conn);
        }
    }

    public static List<String> showTables(Connection conn, DbType dbType) throws SQLException {
        if (isMysqlDbType(dbType)) {
            return MySqlUtils.showTables(conn);
        } else if (dbType != DbType.oracle && dbType != DbType.oceanbase_oracle) {
            if (dbType == DbType.postgresql) {
                return PGUtils.showTables(conn);
            } else {
                throw new SQLException("show tables dbType not support for " + dbType);
            }
        } else {
            return OracleUtils.showTables(conn);
        }
    }

    public static String getCreateTableScript(Connection conn, DbType dbType) throws SQLException {
        return getCreateTableScript(conn, dbType, true, true);
    }

    public static String getCreateTableScript(Connection conn,
                                              DbType dbType,
                                              boolean sorted,
                                              boolean simplify) throws SQLException {
        if (isMysqlDbType(dbType)) {
            return MySqlUtils.getCreateTableScript(conn, sorted, simplify);
        } else if (dbType != DbType.oracle && dbType != DbType.oceanbase_oracle) {
            throw new SQLException("getCreateTableScript dbType not support for " + dbType);
        } else {
            return OracleUtils.getCreateTableScript(conn, sorted, simplify);
        }
    }

    public static boolean isMySqlDriver(String driverClassName) {
        return driverClassName.equals(JdbcConstants.MYSQL_DRIVER) //
                || driverClassName.equals(JdbcConstants.MYSQL_DRIVER_6)
                || driverClassName.equals(JdbcConstants.MYSQL_DRIVER_603)
                || driverClassName.equals(JdbcConstants.MYSQL_DRIVER_REPLICATE);
    }

    public static boolean isOracleDbType(String dbType) {
        return DbType.oracle.name().equals(dbType) || //
                DbType.oceanbase_oracle.name().equals(dbType) || //
                DbType.ali_oracle.name().equalsIgnoreCase(dbType);
    }

    public static boolean isOracleDbType(DbType dbType) {
        return DbType.oracle == dbType || //
                DbType.oceanbase_oracle == dbType || //
                DbType.ali_oracle == dbType;
    }

    public static boolean isMysqlDbType(String dbTypeName) {
        return isMysqlDbType(
                DbType.of(dbTypeName));
    }

    public static boolean isMysqlDbType(DbType dbType) {
        if (dbType == null) {
            return false;
        } else {
            switch (dbType) {
                case mysql:
                case oceanbase:
                case ads:
                case drds:
                case mariadb:
                case tidb:
                case h2:
                case goldendb:
                    return true;
                default:
                    return false;
            }
        }
    }

    public static boolean isPgsqlDbType(String dbTypeName) {
        return isPgsqlDbType(
                DbType.of(dbTypeName)
        );
    }

    public static boolean isPgsqlDbType(DbType dbType) {
        if (dbType == null) {
            return false;
        } else {
            switch (dbType) {
                case postgresql:
                case edb:
                case polardb:
                case greenplum:
                case gaussdb:
                    return true;
                default:
                    return false;
            }
        }
    }

    public static boolean isSqlserverDbType(String dbTypeName) {
        return isSqlserverDbType(
                DbType.of(dbTypeName));
    }

    public static boolean isSqlserverDbType(DbType dbType) {
        if (dbType == null) {
            return false;
        } else {
            switch (dbType) {
                case sqlserver:
                case jtds:
                    return true;
                default:
                    return false;
            }
        }
    }
}
