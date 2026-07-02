package com.jeestudio.bpm.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.druid.DbType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.entity.gen.*;
import com.jeestudio.bpm.common.entity.system.Area;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.mapper.JaxbMapper;
import com.jeestudio.bpm.gen.dialect.*;
import com.jeestudio.bpm.gen.enums.DateTypeEnum;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.tools.base.exceptions.BusinessException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * @Description: 代码生成工具
 */
public class GenUtil {

    private static Logger logger = LoggerFactory.getLogger(GenUtil.class);
    private static RestTemplate restTemplate;
    public static final String GENTABLE_CACHE = "genTableCache";
    public static final String SUM_VIEW = "sum@view";
    public static final String VIEW = "@view";
    public static final String DBTYPE_POSTGRESQL = "postgresql";

    /**
     * 获取主键列名，如果未配置则返回默认值 "id"
     */
    private static String getPkColumnName(GenTable table) {
        return StringUtil.isNotBlank(table.getPkColumnName()) ? table.getPkColumnName() : "id";
    }

    /**
     * Initialize column property fields
     */
    public static void initColumnField(GenTable genTable) {
        String uuid = IdGen.uuid();
        for (GenTableColumn column : genTable.getColumnList()) {
            if (StringUtil.isNotBlank(column.getId())) {
                continue;
            }
            if (StringUtil.isBlank(column.getComments())) {
                column.setComments(column.getName());
            }
            if (StringUtil.startsWithIgnoreCase(column.getJdbcType(), "CHAR")
                    || StringUtil.startsWithIgnoreCase(column.getJdbcType(), "VARCHAR")
                    || StringUtil.startsWithIgnoreCase(column.getJdbcType(), "NARCHAR")) {
                column.setJavaType("String");
            } else if (StringUtil.startsWithIgnoreCase(column.getJdbcType(), "DATETIME")
                    || StringUtil.startsWithIgnoreCase(column.getJdbcType(), "DATE")
                    || StringUtil.startsWithIgnoreCase(column.getJdbcType(), "TIMESTAMP")) {
                column.setJavaType("java.util.Date");
                column.setShowType("dateselect");
                // 设置默认的日期格式：DATE类型用yyyy-MM-dd，DATETIME/TIMESTAMP用yyyy-MM-dd HH:mm:ss
                if (StringUtil.startsWithIgnoreCase(column.getJdbcType(), "DATE") 
                        && !StringUtil.startsWithIgnoreCase(column.getJdbcType(), "DATETIME")) {
                    column.setDateType("yyyy-MM-dd");
                } else {
                    column.setDateType("yyyy-MM-dd HH:mm:ss");
                }
            } else if (StringUtil.startsWithIgnoreCase(column.getJdbcType(), "BIGINT")
                    || StringUtil.startsWithIgnoreCase(column.getJdbcType(), "NUMBER")
                    || StringUtil.startsWithIgnoreCase(column.getJdbcType(), "DECIMAL")) {
                String[] ss = StringUtil.split(StringUtil.substringBetween(column.getJdbcType(), "(", ")"), ",");
                if (ss != null && ss.length == 2 && Integer.parseInt(ss[1]) > 0) {
                    column.setJavaType("Double");
                } else if (ss != null && ss.length == 1 && Integer.parseInt(ss[0]) <= 10) {
                    column.setJavaType("Integer");
                } else {
                    column.setJavaType("Long");
                }
            }
            column.setJavaField(StringUtil.toCamelCase(column.getName()));
            column.setIsPk(genTable.getPkList().contains(column.getName()) ? Global.YES : Global.NO);
            column.setIsInsert(Global.YES);
            column.setIsEdit(Global.YES);
            column.setIsList(Global.NO);
            column.setIsQuery(Global.NO);
            column.setIsForm(Global.NO);
            column.setShowType("input");
            column.setIsOneLine(Global.NO);

            //Init isForm
            if (!StringUtil.equalsIgnoreCase(column.getName(), "id")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "create_by")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "create_date")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "update_by")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "update_date")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "remarks")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "owner_code")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "del_flag")) {
                column.setIsForm(Global.YES);
            }

            //Init list and query
            if (StringUtil.equalsIgnoreCase(column.getName(), "name")
                    || StringUtil.endsWithIgnoreCase(column.getName(), "name")
                    || StringUtil.equalsIgnoreCase(column.getName(), "title")) {
                column.setIsList(Global.YES);
            }
            if (StringUtil.equalsIgnoreCase(column.getName(), "name")
                    || StringUtil.endsWithIgnoreCase(column.getName(), "name")
                    || StringUtil.equalsIgnoreCase(column.getName(), "title")) {
                column.setIsQuery(Global.YES);
                column.setQueryType("like");
            }

            //Init javaType, showType and javaField for user, area, office and parent
            if (StringUtil.endsWithIgnoreCase(column.getName(), "user_id")) {
                column.setJavaType(User.class.getName());
                column.setJavaField(column.getJavaField().replaceAll("Id", ".id|name"));
                column.setShowType("treeselectRedio");
            } else if (StringUtil.endsWithIgnoreCase(column.getName(), "office_id")) {
                column.setJavaType(Office.class.getName());
                column.setJavaField(column.getJavaField().replaceAll("Id", ".id|name"));
                column.setShowType("officeselectTree");
            } else if (StringUtil.endsWithIgnoreCase(column.getName(), "area_id")) {
                column.setJavaType(Area.class.getName());
                column.setJavaField(column.getJavaField().replaceAll("Id", ".id|name"));
                column.setShowType("areaselect");
            } else if (StringUtil.startsWithIgnoreCase(column.getName(), "create_by")
                    || StringUtil.startsWithIgnoreCase(column.getName(), "update_by")) {
                column.setJavaType(User.class.getName());
                column.setJavaField(column.getJavaField() + ".id");
                column.setShowType("treeselectRedio");
            } else if (StringUtil.startsWithIgnoreCase(column.getName(), "create_date")
                    || StringUtil.startsWithIgnoreCase(column.getName(), "update_date")) {
                column.setShowType("dateselect");
                column.setDateType("yyyy-MM-dd HH:mm:ss");
            } else if (StringUtil.equalsIgnoreCase(column.getName(), "remarks")
                    || StringUtil.equalsIgnoreCase(column.getName(), "content")) {
                column.setShowType("textarea");
                column.setIsOneLine(Global.YES);
            } else if (StringUtil.equalsIgnoreCase(column.getName(), "parent_id")) {
                column.setJavaType("This");
                column.setJavaField("parent.id|name");
                column.setShowType("parentId");
            } else if (StringUtil.equalsIgnoreCase(column.getName(), "parent_ids")) {
                column.setQueryType("like");
            } else if (StringUtil.equalsIgnoreCase(column.getName(), "del_flag")) {
                column.setShowType("radiobox");
                column.setDictType("del_flag");
            } else if (StringUtil.equalsIgnoreCase(column.getName(), "sort") || StringUtil.startsWithIgnoreCase(column.getName(), "sort")) {
                column.setJavaType("Integer");
                column.setJdbcType("integer");
            }
            buildJavaField(uuid, column);
        }
        removeJavaField(uuid);
    }

    private static LinkedHashMap<String, LinkedHashMap<String, String>> javaFieldMap = Maps.newLinkedHashMap();
    private static void buildJavaField(String uuid, GenTableColumn column) {
        column.setIsReadonly(Global.NO);
        if (false == javaFieldMap.containsKey(uuid)) {
            LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
            javaFieldMap.put(uuid, map);
        }
        LinkedHashMap<String, String> map = javaFieldMap.get(uuid);
        int d0 = 1;
        int d1 = 1;
        int s = 1;
        if ("id".equalsIgnoreCase(column.getName())) {
            column.setJavaField("id");
        } else if ("del_flag".equalsIgnoreCase(column.getName())) {
            column.setJavaField("delFlag");
        } else if ("sort".equalsIgnoreCase(column.getName())) {
            column.setJavaField("sort");
            column.setCommentsEn("sort");
            column.setJavaType("Integer");
            column.setValidateType("number");
        } else if ("status".equalsIgnoreCase(column.getName())) {
            column.setJavaField("status");
        } else if ("owner_code".equalsIgnoreCase(column.getName())) {
            column.setJavaField("ownerCode");
        } else if ("create_date".equalsIgnoreCase(column.getName())) {
            column.setJavaField("createDate");
        } else if ("update_date".equalsIgnoreCase(column.getName())) {
            column.setJavaField("updateDate");
        } else if ("create_by".equalsIgnoreCase(column.getName())) {
            column.setJavaField("createBy.id");
        } else if ("update_by".equalsIgnoreCase(column.getName())) {
            column.setJavaField("updateBy.id");
        } else if ("parent_id".equalsIgnoreCase(column.getName())) {
            column.setJavaField("parent.id|name");
            column.setJavaType("This");
            column.setShowType("parentId");
        } else {
            //先根据字段名判断是否已定义
            if(column.getShowType().equals("dateselect")) {
                if (column.getDateType().equals("yyyy-MM-dd")) {
                    if (map.containsKey("d0")) {
                        d0 = Integer.parseInt(StringUtil.getString(map.get("d0"))) + 1;
                        map.put("d0", String.valueOf(d0));
                    } else {
                        map.put("d0", String.valueOf(d0));
                    }
                    column.setJavaField("d0" + String.valueOf(d0));
                } else {
                    if (map.containsKey("d1")) {
                        d1 = Integer.parseInt(StringUtil.getString(map.get("d1"))) + 1;
                        map.put("d1", String.valueOf(d1));
                    } else {
                        map.put("d1", String.valueOf(d1));
                    }
                    column.setJavaField("d1" + String.valueOf(d1));
                }
            } else {
                if (map.containsKey("s")) {
                    s = Integer.parseInt(StringUtil.getString(map.get("s"))) + 1;
                    map.put("s", String.valueOf(s));
                } else {
                    map.put("s", String.valueOf(s));
                }
                if (s < 10) {
                    column.setJavaField("s0" + String.valueOf(s));
                } else {
                    column.setJavaField("s" + String.valueOf(s));
                }
            }
        }
    }

    private static void removeJavaField(String uuid) {
        if (javaFieldMap.containsKey(uuid)) {
            javaFieldMap.remove(uuid);
        }
    }

    /**
     * Get template path
     */
    public static String getTemplatePath() {
        try {
            File file = new DefaultResourceLoader().getResource("").getFile();
            if (file != null) {
                return file.getAbsolutePath() + File.separator + StringUtil.replaceEach(GenUtil.class.getName(),
                        new String[]{"util." + GenUtil.class.getSimpleName(), "."}, new String[]{"template", File.separator});
            }
        } catch (Exception e) {
            logger.error("Error while getting template path:" + ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    /**
     * XML files converting to object
     */
    public static <T> T fileToObject(String fileName, Class<?> clazz) {
        try {
            String pathName = "/templates/modules/" + fileName;
            Resource resource = new ClassPathResource(pathName);
            InputStream is = resource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\r\n");
            }
            if (is != null) {
                is.close();
            }
            if (br != null) {
                br.close();
            }
            return (T) JaxbMapper.fromXml(sb.toString(), clazz);
        } catch (IOException e) {
            logger.warn("Error while converting to object:" + ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    /**
     * Get code generation configuration object
     */
    public static GenConfig getConfig() {
        return fileToObject("config.xml", GenConfig.class);
    }

    /**
     * Get template list by category
     *
     * @param config
     * @param category
     * @param isChildTable
     * @return template list
     */
    public static List<GenTemplate> getTemplateList(GenConfig config, String category, boolean isChildTable, boolean isMobile, boolean isGenJava) {
        List<GenTemplate> templateList = Lists.newArrayList();
        if (config != null && config.getCategoryList() != null && category != null) {
            for (GenCategory e : config.getCategoryList()) {
                List<String> list = null;
                if (!isChildTable) {
                    list = e.getTemplate();
                } else {
                    list = e.getChildTableTemplate();
                }
                if (list != null) {
                    for (String s : list) {
                        if (false == isMobile && (s.indexOf("mobile") != -1 || s.indexOf("app") == 0)) continue;
                        if (false == isGenJava && s.indexOf("java") != -1) continue;
                        if (StringUtil.startsWith(s, GenCategory.CATEGORY_REF)) {
                            templateList.addAll(getTemplateList(config, StringUtil.replace(s, GenCategory.CATEGORY_REF, ""), false, isMobile, isGenJava));
                        } else {
                            GenTemplate template = fileToObject(s, GenTemplate.class);
                            if (template != null) {
                                template.setId(s); //by David
                                templateList.add(template);
                            }
                        }
                    }
                }
                break;
            }
        }
        return templateList;
    }

    /**
     * Get template list by category for form service
     *
     * @param config
     * @param category
     * @param isChildTable
     * @return template list
     */
    public static List<GenTemplate> getTemplateList(GenConfig config, String category, boolean isChildTable) {
        List<GenTemplate> templateList = Lists.newArrayList();
        if (config != null && config.getCategoryList() != null && category != null) {
            for (GenCategory e : config.getCategoryList()) {
                if (category.equals(e.getName())) {
                    List<String> list = null;
                    if (!isChildTable) {
                        list = e.getTemplate();
                    } else {
                        list = e.getChildTableTemplate();
                    }
                    if (list != null) {
                        for (String s : list) {
                            if (StringUtil.startsWith(s, GenCategory.CATEGORY_REF)) {
                                templateList.addAll(getTemplateList(config, StringUtil.replace(s, GenCategory.CATEGORY_REF, ""), false));
                            } else {
                                GenTemplate template = fileToObject(s, GenTemplate.class);
                                if (template != null) {
                                    template.setId(s);
                                    templateList.add(template);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return templateList;
    }

    /**
     * Get data model
     *
     * @param genScheme
     * @return data model map
     */
    public static Map<String, Object> getDataModel(GenScheme genScheme, String dbType) {
        Map<String, Object> model = Maps.newHashMap();

        model.put("packageName", StringUtil.lowerCase(genScheme.getPackageName()));
        model.put("lastPackageName", StringUtil.substringAfterLast((String) model.get("packageName"), "."));
        model.put("moduleName", StringUtil.lowerCase(genScheme.getGenTable().getModule()));
        model.put("subModuleName", StringUtil.lowerCase(genScheme.getSubModuleName()));
        model.put("className", StringUtil.uncapitalize(genScheme.getGenTable().getJavaInstanceName()));
        model.put("ClassName", StringUtil.capitalize(genScheme.getGenTable().getJavaClassName()));

        model.put("functionName", genScheme.getFunctionName());
        model.put("functionNameSimple", genScheme.getFunctionNameSimple());
        model.put("functionAuthor", StringUtil.isNotBlank(genScheme.getFunctionAuthor()) ? genScheme.getFunctionAuthor() : "David");
        model.put("functionVersion", DateUtil.getDate());

        model.put("urlPrefix", model.get("moduleName") + (StringUtil.isNotBlank(genScheme.getSubModuleName())
                ? "/" + StringUtil.lowerCase(genScheme.getSubModuleName()) : "") + "/" + model.get("className"));
        model.put("viewPrefix",
                model.get("urlPrefix"));
        model.put("permissionPrefix", model.get("moduleName") + (StringUtil.isNotBlank(genScheme.getSubModuleName())
                ? ":" + StringUtil.lowerCase(genScheme.getSubModuleName()) : "") + ":" + model.get("className"));

        model.put("dbType", dbType);
        model.put("table", genScheme.getGenTable());

        return model;
    }

    /**
     * Generating files
     *
     * @param isChild
     * @param category
     * @param tpl
     * @param genScheme
     * @param isReplaceFile
     * @param timePath
     * @return result message
     */
    public static String xgenerateToFile(Boolean isChild,
                                         String category,
                                         GenTemplate tpl,
                                         GenScheme genScheme,
                                         boolean isReplaceFile,
                                         String timePath,
                                         String projectPath,
                                         String genKey,
                                         String genUrl,
                                         String dbType) {
        Map<String, Object> model = GenUtil.getDataModel(genScheme, dbType);
        String fileName = projectPath + "/" + FreeMarkerUtil.renderString(tpl.getFilePath() + "/", model) + tpl.getFileName();
        fileName = fileName.replaceFirst("\\$\\{ClassName}", genScheme.getGenTable().getJavaClassName());
        //local render
        String content = FreeMarkerUtil.renderString(StringUtil.trimToEmpty(tpl.getContent()), model);
        logger.info("Content === \r\n" + content);
        //server render
        //String content = null;
        if (tpl.getName().equals("form") || tpl.getName().equals("list")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String genSchemeString = mapper.writeValueAsString(genScheme);
                genSchemeString = Encodes.encodeBase64(genSchemeString);
                Map<String, String> params = new HashMap<String, String>();
                params.put("genscheme", genSchemeString);
                params.put("isChild", isChild ? Global.YES : Global.NO);
                params.put("code", genKey + ":" + getLocalMac(InetAddress.getLocalHost()));
                params.put("category", category);
                params.put("tplId", tpl.getId());
                params.put("dbType", dbType);

                GenParam genParam = new GenParam();
                genParam.setCategory(category);
                genParam.setCode(genKey + ":" + getLocalMac(InetAddress.getLocalHost()));
                genParam.setGenscheme(genSchemeString);
                genParam.setIsChild(isChild ? Global.YES : Global.NO);
                genParam.setTplId(tpl.getId());

                if (StringUtil.isEmpty(content)) {
                    if (restTemplate == null) restTemplate = new RestTemplate();
                    content = restTemplate.postForObject(genUrl, genParam, String.class);
                }
            } catch (Exception e) {
                logger.error("代码生成失败", e);
                content = "error:代码生成失败";
            }
        } else {
            content = FreeMarkerUtil.renderString(StringUtil.trimToEmpty(tpl.getContent()), model);
        }

        logger.info("Content === \r\n" + content);

        if (content.indexOf("error") == 0) {
            logger.info("File error === " + fileName);
            content = content.replaceAll("error:", "");
            logger.info(content);
            if (content.length() > 9) content = content.substring(0, 9);
            return "Released failed:" + fileName + " " + content + "<br/>";
        } else {
            if (isReplaceFile) {
                FileUtil.deleteFile(fileName);
            }
            if (FileUtil.createFile(fileName)) {
                try {
                    FileUtil.writeToFile(fileName, content, true);
                    logger.info("File create === " + fileName);
                    xgenerateCustom(fileName, genScheme.getGenTable(), isReplaceFile);
                    return "Released success:" + fileName + "<br/>";
                } catch (Exception e) {
                    logger.error(fileName + " released failed, " + ExceptionUtils.getStackTrace(e));
                    return "Released failed:" + fileName + "<br/>";
                }
            } else {
                logger.info("File extents === " + fileName);
                return "File already exist:" + fileName + "<br/>";
            }
        }
    }

    private static void xgenerateCustom(String fileName, GenTable genTable, boolean isReplaceFile) {
        if (Global.YES.equals(genTable.getIsCustom()) && fileName.indexOf("/form.html") != -1) {
            fileName = fileName.replaceFirst("form.html", "customform.html");
            if (isReplaceFile) {
                FileUtil.deleteFile(fileName);
                if (FileUtil.createFile(fileName)) {
                    try {
                        String head = "<div class=\"container\" style=\"width: 100%;\">\n" +
                                "\t<form id=\"inputForm\" class=\"form-horizontal\" style=\"overflow: auto;\">\n" +
                                "\t\t<input name=\"id\" type=\"hidden\" id=\"id\"/>\n" +
                                "\t\t<input name=\"formNo\" type=\"hidden\" id=\"formNo\"/>\n";
                        if (StringUtil.isNotEmpty(genTable.getProcessDefinitionCategory())) {
                            head += "\t\t<input component-type=\"input\" type=\"hidden\" id=\"act.taskId\" name=\"act.taskId\" />\n" +
                                    "\t\t<input component-type=\"input\" type=\"hidden\" id=\"act.taskName\" name=\"act.taskName\" />\n" +
                                    "\t\t<input component-type=\"input\" type=\"hidden\" id=\"act.taskDefKey\" name=\"act.taskDefKey\" />\n" +
                                    "\t\t<input component-type=\"input\" type=\"hidden\" id=\"procInsId\" name=\"procInsId\" />\n" +
                                    "\t\t<input component-type=\"input\" type=\"hidden\" id=\"act.procDefId\" name=\"act.procDefId\" />\n" +
                                    "\t\t<input component-type=\"input\" type=\"hidden\" id=\"flag\" name=\"act.flag\" />\n" +
                                    "\t\t<input component-type=\"input\" type=\"hidden\" id=\"tempNodeKey\" name=\"tempNodeKey\" />\n" +
                                    "\t\t<input component-type=\"input\" type=\"hidden\" id=\"tempLoginName\" name=\"tempLoginName\" />\n" +
                                    "\t\t<input component-type=\"input\" type=\"hidden\" id=\"procDefKey\" name=\"procDefKey\">\n" +
                                    "\t\t<input component-type=\"input\" type=\"hidden\" id=\"ruleArgsJson\" name='ruleArgsJson' />";
                        }
                        String bottom = "\n\t</form>\n" +
                                "</div>";
                        FileUtil.writeToFile(fileName, head + genTable.getExtJsp() + bottom, true);
                        logger.info("File create === " + fileName);
                    } catch (Exception e) {
                        logger.error(fileName + " released failed, " + ExceptionUtils.getStackTrace(e));
                    }
                } else {
                    logger.warn("File extents === " + fileName);
                }
            }
        }
    }

    /**
     * Construct SQL segment of dynamic form
     *
     * @param genTable
     */
    public static void buildSqlMapForDynamicTable(GenTable genTable, String dbType) {
        buildSqlColumn(genTable, dbType);
        if (DBTYPE_POSTGRESQL.equals(dbType)) {
            for(GenTableColumn column : genTable.getColumnList()) {
                column.setName(column.getName().toLowerCase(Locale.ROOT));
            }
        }
        buildSqlJoins(genTable);
        buildSqlInsert(genTable);
        buildSqlUpdate(genTable);
    }

    /**
     * 为 datahouse 模块生成 extSql02（MySQL/Doris 语法的查询列片段）
     * 仅包含主表字段，不含JOIN关联表字段（Doris宽表模式）
     */
    public static void buildExtSql02ForDatahouse(GenTable genTable) {
        StringBuilder s2 = new StringBuilder();
        for (GenTableColumn c : genTable.getColumnList()) {
            // 安全加固：校验列名标识符合法性，跳过非法标识符
            if (!isValidSqlIdentifier(c.getName())) {
                logger.warn("buildExtSql02ForDatahouse跳过非法列名: {}, 表: {}", c.getName(), genTable.getName());
                continue;
            }
            // 跳过关联字段（如 userselect/officeselect 等产生的 .id 字段）
            if (c.getJavaField().indexOf(".id") != -1) {
                continue;
            }
            // 跳过 parentId 类型字段（树表自关联，Doris宽表不需要）
            if ("parentId".equalsIgnoreCase(c.getShowType())) {
                continue;
            }
            // 跳过 procTaskPermission 字段
            if ("procTaskPermission".equalsIgnoreCase(c.getJavaField())) {
                continue;
            }
            if ("java.util.Date".equals(c.getJavaType())) {
                // 日期类型：使用 MySQL 方言的 DATE_FORMAT
                s2.append(getDateFormatSql(c.getName(), c.getDateType(), "mysql", c.getName(), ""));
                s2.append("\n");
            } else {
                // 非日期类型：生成 a.列名 AS "别名" 格式
                String friendlyAlias = Global.YES.equals(c.getIsPk()) ? "id" : c.getName();
                s2.append("a." + c.getName() + " AS \"" + friendlyAlias + "\",");
                s2.append("\n");
            }
        }
        // 去除末尾逗号
        if (s2.length() > 0) s2.deleteCharAt(s2.lastIndexOf(","));
        genTable.setExtSql02(s2.toString());
    }

    /**
     * Construct sqlcolumn
     *
     * @param table
     */
    private static void buildSqlColumn(GenTable table, String dbType) {
        logger.info("table.buildSqlColumn-{}", table.getName());
        StringBuilder s = new StringBuilder();
        StringBuilder s2 = new StringBuilder();
        for (GenTableColumn c : table.getColumnList()) {
            // 安全加固：校验列名标识符合法性，跳过非法标识符
            if (!isValidSqlIdentifier(c.getName())) {
                logger.warn("buildSqlColumn跳过非法列名: {}, 表: {}", c.getName(), table.getName());
                continue;
            }
            String alias = c.getAlias();
            String aliasplus = c.getAliasplus();
            if ("procTaskPermission".equalsIgnoreCase(c.getJavaField())) {
                s.append("a." + c.getName() + " AS \"procTaskPermission.id\",");
                s.append("\n");
                s2.append("a." + c.getName() + " AS \"procTaskPermission.id\",");
                s2.append("\n");
            } else {
                if ("parentId".equalsIgnoreCase(c.getShowType())) {
                    if (table.getTableType().equals(GenTable.TABLE_TYPE_RIGHTTABLE)) {
                        s.append("a.parent_id AS \"parent.id\",");
                        s.append("\n");
                        s2.append("a.parent_id AS \"parent.id\",");
                        s2.append("\n");
                        // parent.name 由下方第二个循环的 javaFieldAttrs 统一添加
                    } else if (table.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
                        s.append("a.parent_id AS \"parent.id\",");
                        s.append("\n");
                        s2.append("a.parent_id AS \"parent.id\",");
                        s2.append("\n");
                        // parent.name 由下方第二个循环的 javaFieldAttrs 统一添加
                        // 使用动态主键列名
                        String pkCol = getPkColumnName(table);
                        s.append("(CASE WHEN EXISTS(SELECT 1 FROM " + table.getName() + " child WHERE child.parent_id = a." + pkCol + ") THEN 1 ELSE 0 END) AS \"hasChildren\",");
                        s.append("\n");
                        s2.append("(CASE WHEN EXISTS(SELECT 1 FROM " + table.getName() + " child WHERE child.parent_id = a." + pkCol + ") THEN 1 ELSE 0 END) AS \"hasChildren\",");
                        s2.append("\n");
                    }
                } else {
                    // 主键列的 Java 属性名固定为 id，数据库列名使用实际字段名
                    String javaFieldAlias = Global.YES.equals(c.getIsPk()) ? "id" : c.getJavaFieldId();
                    s.append("a." + c.getName() + " AS \"" + javaFieldAlias + "\",");
                    s.append("\n");
                    if("java.util.Date".equals(c.getJavaType())) {
                        s2.append(getDateFormatSql(c.getName(), c.getDateType(), dbType, alias, aliasplus));
                        s2.append("\n");
                    } else {
                        //增加 .id 判断
                        if(c.getJavaField().indexOf(".id") == -1) {
                            // 主键列的别名固定为 id
                            String friendlyAlias = Global.YES.equals(c.getIsPk()) ? "id" : c.getName();
                            s2.append("a." + c.getName() + " AS \"" + friendlyAlias + "\",");
                            s2.append("\n");
                        }
                    }
                    if("createBy.id".equals(c.getJavaFieldId())) {
                        s.append("createBy.name AS \"createBy.name\",");
                        s.append("\n");
                        s2.append("a." + c.getName() + " AS \"createBy.id\",");
                        s2.append("\n");
                        s2.append("createBy.name AS \"createBy.name\",");
                        s2.append("\n");
                    } else if ("updateBy.id".equals(c.getJavaFieldId())) {
                        s.append("updateBy.name AS \"updateBy.name\",");
                        s.append("\n");
                        s2.append("a." + c.getName() + " AS \"updateBy.id\",");
                        s2.append("\n");
                        s2.append("updateBy.name AS \"updateBy.name\",");
                        s2.append("\n");
                    }
                }
            }
        }
        for (GenTableColumn c : table.getColumnList()) {
            if (false == StringUtil.isEmpty(c.getShowType())) {
                if ("userselect".equalsIgnoreCase(c.getShowType())
                        || "treeselectRedio".equalsIgnoreCase(c.getShowType())
                        || "officeselect".equalsIgnoreCase(c.getShowType())
                        || "officeselectTree".equalsIgnoreCase(c.getShowType())
                        || "treeselect".equalsIgnoreCase(c.getShowType())
                        || "areaselect".equalsIgnoreCase(c.getShowType())) {
                    for (String[] a : c.getJavaFieldAttrs()) {
                        //${c.simpleJavaField}.${a[1]} AS "${c.simpleJavaField}.${a[0]}",
                        s.append(c.getSimpleJavaField() + "." + a[1] + " AS \"" + c.getSimpleJavaField() + "." + a[0] + "\",");
                        s.append("\n");
                        //s2.append("a." + c.getName() + " AS \"" + c.getSimpleJavaField() + ".id" + "\",");
                        s2.append("a." + c.getName() + " AS \"" + c.getName() + ".id" + "\",");
                        s2.append("\n");
                        //s2.append(c.getSimpleJavaField() + "." + a[1] + " AS \"" + c.getSimpleJavaField() + "." + a[0] + "\",");
                        s2.append(c.getSimpleJavaField() + "." + a[1] + " AS \"" + c.getName() + "." + a[0] + "\",");
                        s2.append("\n");
                    }
                } else if ("treeselectCheck".equalsIgnoreCase(c.getShowType())) {
                    for (String[] a : c.getJavaFieldAttrs()) {
                        //${c.simpleJavaField}.${a[1]} AS "${c.simpleJavaField}.${a[0]}",
                        //s.append("a." + c.getSimpleJavaField() + "_" + a[1] + " AS \"" + c.getSimpleJavaField() + "." + a[0] + "\",");
                        s.append("a." + c.getName() + "_" + a[1] + " AS \"" + c.getSimpleJavaField() + "." + a[0] + "\",");
                        s.append("\n");
                        //s2.append("a." + c.getName() + " AS \"" + c.getSimpleJavaField() + ".id" + "\",");
                        s2.append("a." + c.getName() + " AS \"" + c.getName() + ".id" + "\",");
                        s2.append("\n");
                        //s2.append("a." + c.getSimpleJavaField() + "_" + a[1] + " AS \"" + c.getSimpleJavaField() + "." + a[0] + "\",");
                        //s2.append("a." + c.getSimpleJavaField() + "_" + a[1] + " AS \"" + c.getName() + "." + a[0] + "\",");
                        s2.append("a." + c.getName() + "_" + a[1] + " AS \"" + c.getName() + "." + a[0] + "\",");
                        s2.append("\n");
                    }
                } else if ("select".equalsIgnoreCase(c.getShowType()) && Global.NO.equals(c.getSelectSimple()) && StringUtil.isNotEmpty(c.getTableName())) {
                    if (c.getSearchKey() != null) {
                        String[] search = c.getSearchKey().split("\\|");

                        //s2.append(c.getSimpleJavaField() + "." + search[0] + " AS \"" + c.getSimpleJavaField() + "." + "name" + "\",");
                        s2.append(c.getSimpleJavaField() + "." + search[0] + " AS \"" + c.getName() + "_" + "name" + "\",");
                        s2.append("\n");
                    }
                } else if ("gridselect".equalsIgnoreCase(c.getShowType())) {
                    //${c.simpleJavaField}.${c.searchKey} AS "${c.simpleJavaField}.${c.searchKey}",
                    GenTableColumnFormItemConfig genTableColumnFormItemConfig = c.getGenTableColumnFormItemConfig();
                    String nameDataIndex = genTableColumnFormItemConfig.getNameDataIndex();
                    if (StringUtil.isNotEmpty(nameDataIndex)){
                        //如果设置了nameDataIndex
                        s.append(c.getSimpleJavaField() + "." + nameDataIndex + " AS \"" + c.getJavaFieldName() + "\",");
                        s.append("\n");
                        //s2.append("a." + c.getName() + " AS \"" + c.getSimpleJavaField() + ".id" + "\",");
                        s2.append("a." + c.getName() + " AS \"" + c.getName() + ".id" + "\",");
                        s2.append("\n");
                        //s2.append(c.getSimpleJavaField() + "." + search[0] + " AS \"" + c.getSimpleJavaField() + "." + "name" + "\",");
                        s2.append(c.getSimpleJavaField() + "." + nameDataIndex + " AS \"" + c.getName() + "." + "name" + "\",");
                        s2.append("\n");
                    }else if (StringUtil.isNotBlank(c.getSearchKey())) {
                        String[] search = c.getSearchKey().split("\\|");
                        s.append(c.getSimpleJavaField() + "." + search[0] + " AS \"" + c.getJavaFieldName() + "\",");
                        s.append("\n");
                        //s2.append("a." + c.getName() + " AS \"" + c.getSimpleJavaField() + ".id" + "\",");
                        s2.append("a." + c.getName() + " AS \"" + c.getName() + ".id" + "\",");
                        s2.append("\n");
                        //s2.append(c.getSimpleJavaField() + "." + search[0] + " AS \"" + c.getSimpleJavaField() + "." + "name" + "\",");
                        s2.append(c.getSimpleJavaField() + "." + search[0] + " AS \"" + c.getName() + "." + "name" + "\",");
                        s2.append("\n");
                    }else if (StringUtil.isNotBlank(c.getFieldKeys())) {
                        String[] fieldKeys = c.getFieldKeys().split(",");
                        s.append(c.getSimpleJavaField() + "." + fieldKeys[0] + " AS \"" + c.getJavaFieldName() + "\",");
                        s.append("\n");
                        //s2.append("a." + c.getName() + " AS \"" + c.getSimpleJavaField() + ".id" + "\",");
                        s2.append("a." + c.getName() + " AS \"" + c.getName() + ".id" + "\",");
                        s2.append("\n");
                        //s2.append(c.getSimpleJavaField() + "." + search[0] + " AS \"" + c.getSimpleJavaField() + "." + "name" + "\",");
                        s2.append(c.getSimpleJavaField() + "." + fieldKeys[0] + " AS \"" + c.getName() + "." + "name" + "\",");
                        s2.append("\n");
                    }
                } else if ("parentId".equalsIgnoreCase(c.getShowType())) {
                    // 树表使用自关联 parent，左树右表使用 b
                    String parentAlias = GenTable.TABLE_TYPE_TREE.equals(table.getTableType()) ? "parent" : "b";
                    for (String[] a : c.getJavaFieldAttrs()) {
                        //${parentAlias}.${a[1]} AS "${c.simpleJavaField}.${a[0]}",
                        s.append(parentAlias + "." + a[1] + " AS \"" + c.getSimpleJavaField() + "." + a[0] + "\",");
                        s.append("\n");
                        //s2.append("a." + c.getName() + " AS \"" + c.getSimpleJavaField() + ".id" + "\",");
                        //s2.append("\n");
                        s2.append(parentAlias + "." + a[1] + " AS \"" + c.getSimpleJavaField() + "." + a[0] + "\",");
                        s2.append("\n");
                    }
                }
            }
        }
        //Delete last comma
        if (s.length() > 0) s.deleteCharAt(s.lastIndexOf(","));
        if (StringUtil.isNotEmpty(table.getExportRuleName()) && table.getExportRuleName().indexOf("locksql") != -1) {
            //locksql 不更新sql
        } else {
            table.setSqlColumns(s.toString());
        }
        if (s2.length() > 0) s2.deleteCharAt(s2.lastIndexOf(","));
        if (StringUtil.isNotEmpty(table.getExportRuleName()) && table.getExportRuleName().indexOf("lockfsql") != -1) {
            //lockfsql 不更新友好sql
        } else {
            table.setSqlColumnsFriendly(s2.toString());
        }
    }

    public static AbstractGenDialect getGenDialect(String dbType) {
        if ("doris".equalsIgnoreCase(dbType)) {
            return new DorisGenDialect();
        }
        if (DbType.mysql.name().equalsIgnoreCase(dbType)) {
            return new MysqlGenDialect();
        } else if (DbType.oracle.name().equalsIgnoreCase(dbType)) {
            return new OracleGenDialect();
        } else if ("mssql".equalsIgnoreCase(dbType) || DbType.sqlserver.name().equalsIgnoreCase(dbType)) {
            return new MssqlGenDialect();
        } else if (DbType.dm.name().equalsIgnoreCase(dbType)) {
            return new DmGenDialect();
        } else if (DbType.kingbase.name().equalsIgnoreCase(dbType)) {
            return new KingbaseGenDialect();
        } else if (DbType.postgresql.name().equalsIgnoreCase(dbType)) {
            return new PostgreGenDialect();
        } else if (DbType.gaussdb.name().equalsIgnoreCase(dbType)) {
            //南大通用8c、华为gaussdb数据库
            return new GaussdbGenDialect();
        } else if (DbType.gbase.name().equalsIgnoreCase(dbType)) {
            return new GBaseGenDialect();
        } else {
            throw new BusinessException("Unknown database type: " + dbType);
        }
    }

    private static String getDateFormatSql(String name, String dataType, String dbType, String alias, String aliasplus) {
        AbstractGenDialect genDialect = getGenDialect(dbType);
        DateTypeEnum anEnum = DateTypeEnum.getEnum(dataType);
        if (StringUtil.isNotEmpty(aliasplus)) {
            return genDialect.formatSelectColumn("a", name, anEnum) + " AS \"" + aliasplus + "\",";
        }
        if (StringUtil.isEmpty(alias)) {
            alias = name;
        }
        return genDialect.formatSelectColumn("a", name, anEnum) + " AS \"" + alias + "\",";
    }

    /**
     * Construct sqljoins
     *
     * @param table
     */
    private static void buildSqlJoins(GenTable table) {
        logger.info("table.buildSqlJoins-{}", table.getName());
        StringBuilder s = new StringBuilder();
        // 获取主键列名
        String pkColumnName = getPkColumnName(table);
        //Associated master table
        if (table.getParentExists() && false == GenTable.TABLE_TYPE_RIGHTTABLE.equals(table.getTableType())) {
            //LEFT JOIN ${table.parentTable} b ON b.pkColumnName = a.${table.parentTableFk}
            // 安全加固：校验父表名是否为合法SQL标识符
            if (!isValidSqlIdentifier(table.getParentTable())) {
                logger.warn("buildSqlJoins跳过非法父表名: {}", table.getParentTable());
            } else {
                s.append("LEFT JOIN " + table.getParentTable() + " b ON b." + pkColumnName + " = a." + table.getParentTableFk());
                s.append("\n");
            }
        }
        // 树表自关联获取父节点名称
        if (GenTable.TABLE_TYPE_TREE.equals(table.getTableType())) {
            s.append("LEFT JOIN " + table.getName() + " parent ON parent." + pkColumnName + " = a.parent_id");
            s.append("\n");
        }
        //Associated system table
        for (GenTableColumn c : table.getColumnList()) {
            // 安全加固：校验列名标识符合法性，跳过非法标识符
            if (!isValidSqlIdentifier(c.getName())) {
                logger.warn("buildSqlJoins跳过非法列名: {}", c.getName());
                continue;
            }
            if (c.getSimpleJavaField() != null && !isValidSqlIdentifier(c.getSimpleJavaField())) {
                logger.warn("buildSqlJoins跳过非法Java字段名: {}", c.getSimpleJavaField());
                continue;
            }
            if (false == StringUtil.isEmpty(c.getShowType())) {
                if ("userselect".equalsIgnoreCase(c.getShowType())
                        || "treeselectRedio".equalsIgnoreCase(c.getShowType())
                        || "treeselectCheck".equalsIgnoreCase(c.getShowType())) {
                    //LEFT JOIN sys_user ${c.simpleJavaField} ON ${c.simpleJavaField}.id = a.${c.name}
                    s.append("LEFT JOIN sys_user " + c.getSimpleJavaField() + " ON " + c.getSimpleJavaField() + ".id = a." + c.getName());
                    s.append("\n");
                } else if ("officeselect".equalsIgnoreCase(c.getShowType()) || "officeselectTree".equalsIgnoreCase(c.getShowType())) {
                    //LEFT JOIN sys_office ${c.simpleJavaField} ON ${c.simpleJavaField}.id = a.${c.name}
                    s.append("LEFT JOIN sys_office " + c.getSimpleJavaField() + " ON " + c.getSimpleJavaField() + ".id = a." + c.getName());
                    s.append("\n");
                } else if ("areaselect".equalsIgnoreCase(c.getShowType())) {
                    //LEFT JOIN sys_area ${c.simpleJavaField} ON ${c.simpleJavaField}.id = a.${c.name}
                    s.append("LEFT JOIN sys_area " + c.getSimpleJavaField() + " ON " + c.getSimpleJavaField() + ".id = a." + c.getName());
                    s.append("\n");
                } else if ("treeselect".equalsIgnoreCase(c.getShowType())) {
                    // 自定义关联表使用 pkColumnName
                    if (!isValidSqlIdentifier(c.getTableName())) {
                        logger.warn("buildSqlJoins treeselect跳过非法表名: {}", c.getTableName());
                    } else {
                        s.append("LEFT JOIN " + c.getTableName() + " " + c.getSimpleJavaField() + " ON " + c.getSimpleJavaField() + "." + pkColumnName + " = a." + c.getName());
                        s.append("\n");
                    }
                } else if ("select".equalsIgnoreCase(c.getShowType()) && Global.NO.equals(c.getSelectSimple()) && StringUtil.isNotEmpty(c.getTableName())) {
                    //LEFT JOIN ${c.tableName} ${c.simpleJavaField} ON ${c.simpleJavaField}.selectValuefield = a.${c.name}
                    if (!isValidSqlIdentifier(c.getTableName())) {
                        logger.warn("buildSqlJoins select跳过非法表名: {}", c.getTableName());
                    } else {
                        s.append("LEFT JOIN " + c.getTableName() + " " + c.getSimpleJavaField() + " ON " + c.getSimpleJavaField() + "."+ c.getSelectValuefield() + " = a." + c.getName());
                        s.append("\n");
                    }
                } else if ("gridselect".equalsIgnoreCase(c.getShowType())) {
                    //LEFT JOIN ${c.tableName} ${c.simpleJavaField} ON ${c.simpleJavaField}.pkColumnName = a.${c.name}
                    if (!isValidSqlIdentifier(c.getTableName())) {
                        logger.warn("buildSqlJoins gridselect跳过非法表名: {}", c.getTableName());
                    } else if (c.getTableName().endsWith("@view")) {
                        ZformService zformService = (ZformService) SpringUtil.getBean("zformService");
                        String querySql = zformService.getQuerySql(c.getTableName());
                        // 视图的主键通常为 id，但也使用 pkColumnName 以保持一致性
                        s.append("LEFT JOIN (" + querySql + ") " + c.getSimpleJavaField() + " ON " + c.getSimpleJavaField() + "." + pkColumnName + " = a." + c.getName());
                    } else {
                        // 自定义关联表使用 pkColumnName
                        s.append("LEFT JOIN " + c.getTableName() + " " + c.getSimpleJavaField() + " ON " + c.getSimpleJavaField() + "." + pkColumnName + " = a." + c.getName());
                    }
                    s.append("\n");
                } else if ("parentId".equalsIgnoreCase(c.getShowType())) {
                    // 树表的自关联 JOIN 已在上方（parent 别名）统一处理，此处跳过避免重复
                    if (GenTable.TABLE_TYPE_RIGHTTABLE.equals(table.getTableType())) {
                        // 使用 pkColumnName 作为父表主键
                        s.append("LEFT JOIN " + table.getParentTable() + " b ON b." + pkColumnName + " = a.parent_id ");
                        s.append("\n");
                    }
                }
            }
        }
        if (false == StringUtil.isEmpty(table.getIsProcessDefinition())
                && Global.YES.equals(table.getIsProcessDefinition())) {
            //LEFT JOIN sys_user u  ON a.create_by = u.id
            s.append("LEFT JOIN sys_user u  ON a.create_by = u.id");
            s.append("\n");
        }
        if (StringUtil.isNotEmpty(table.getExportRuleName()) && table.getExportRuleName().indexOf("lockjoins") != -1) {
            //锁定sqlJoins
        } else {
            table.setSqlJoins(s.toString());
        }
    }

    /**
     * Construct sqlinsert
     *
     * @param table
     */
    private static void buildSqlInsert(GenTable table) {
        logger.info("table.buildSqlInsert-{}", table.getName());
        StringBuilder s = new StringBuilder();
        s.append("(");
        s.append("\n");
        for (GenTableColumn c : table.getColumnList()) {
            if (false == StringUtil.isEmpty(c.getIsInsert()) && Global.YES.equals(c.getIsInsert())) {
                // 安全加固：校验列名
                if (!isValidSqlIdentifier(c.getName())) {
                    logger.warn("buildSqlInsert跳过非法列名: {}", c.getName());
                    continue;
                }
                //${c.name},
                s.append(c.getName() + ",");
                s.append("\n");
            }
        }
        //Delete last comma
        if (s.length() > 0 && s.indexOf(",") != -1) s.deleteCharAt(s.lastIndexOf(","));
        s.append(") VALUES (");
        s.append("\n");
        for (GenTableColumn c : table.getColumnList()) {
            if (false == StringUtil.isEmpty(c.getIsInsert()) && Global.YES.equals(c.getIsInsert())) {
                //${#}{${c.javaFieldId}<#if c.javaFieldId == "procTaskPermission">.id</#if>},
                // 主键列始终使用 id 作为 Java 属性名
                String javaFieldId = Global.YES.equals(c.getIsPk()) ? "id" : c.getJavaFieldId();
                s.append("#{" + javaFieldId);
                if ("procTaskPermission".equalsIgnoreCase(javaFieldId)) {
                    s.append(".id");
                }
                s.append("},");
                s.append("\n");
            }
        }
        //Delete last comma
        if (s.length() > 0 && s.indexOf(",") != -1) s.deleteCharAt(s.lastIndexOf(","));
        s.append(")");
        s.append("\n");
        //table.setSqlInsert(s.toString());
        if (StringUtil.isNotEmpty(table.getExportRuleName()) && table.getExportRuleName().indexOf("lockinsert") != -1) {
            //锁定sqlInsert
        } else {
            table.setSqlInsert(s.toString());
        }
    }

    /**
     * Construct sqlupdate
     *
     * @param table
     */
    private static void buildSqlUpdate(GenTable table) {
        logger.info("table.buildSqlUpdate-{}", table.getName());
        StringBuilder s = new StringBuilder();
        for (GenTableColumn c : table.getColumnList()) {
            if (false == StringUtil.isEmpty(c.getIsEdit()) && Global.YES.equals(c.getIsEdit())) {
                // 安全加固：校验列名
                if (!isValidSqlIdentifier(c.getName())) {
                    logger.warn("buildSqlUpdate跳过非法列名: {}", c.getName());
                    continue;
                }
                //${c.name} = ${#}{${c.javaFieldId}<#if c.javaFieldId == "procTaskPermission">.id</#if>},
                // 主键列始终使用 id 作为 Java 属性名
                String javaFieldId = Global.YES.equals(c.getIsPk()) ? "id" : c.getJavaFieldId();
                s.append(c.getName() + " = #{" + javaFieldId);
                if ("procTaskPermission".equalsIgnoreCase(javaFieldId)) {
                    s.append(".id");
                }
                s.append("},");
                s.append("\n");
            }
        }
        //Delete last comma
        if (s.length() > 0) s.deleteCharAt(s.lastIndexOf(","));
        //table.setSqlUpdate(s.toString());
        if (StringUtil.isNotEmpty(table.getExportRuleName()) && table.getExportRuleName().indexOf("lockupdate") != -1) {
            //锁定sqlUpdate
        } else {
            table.setSqlUpdate(s.toString());
        }
    }

    /**
     * Get local mac
     *
     * @param ia
     * @throws SocketException
     */
    public static String getLocalMac(InetAddress ia) {
        try {
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                int temp = mac[i] & 0xff;
                String str = Integer.toHexString(temp);
                if (str.length() == 1) {
                    sb.append("0" + str);
                } else {
                    sb.append(str);
                }
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            logger.error("Error while getting local mac:" + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * 安全校验SQL标识符（表名、列名、别名），仅允许字母、数字、下划线
     * 用于防止代码生成器中表的元数据被污染导致的SQL注入
     */
    private static boolean isValidSqlIdentifier(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    /**
     * 校验带点的标识符（如 {alias}.{column}），每段分别校验
     */
    private static boolean isValidDottedIdentifier(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String[] parts = name.split("\\.");
        for (String part : parts) {
            if (!isValidSqlIdentifier(part)) {
                return false;
            }
        }
        return true;
    }
}
