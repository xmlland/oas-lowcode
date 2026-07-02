package com.jeestudio.bpm.service.system;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.mapper.base.system.OfficeDao;
import com.jeestudio.bpm.mapper.base.system.SysFileDao;
import com.jeestudio.bpm.service.common.CrudService;
import com.jeestudio.bpm.service.common.DatahouseService;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.storage.IFileStorageAdapter;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.exceptions.BusinessException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Description: 系统文件服务
 */
@Service
public class SysFileService extends CrudService<SysFileDao, SysFile> {

    protected static final Logger logger = LoggerFactory.getLogger(SysFileService.class);

    @Value("${fileRoot}")
    private String fileRoot;

    @Value("${templatePath:/wordTemplate}")
    private String templatePath;

    @Resource
    DataService dataService;

    @Autowired
    ProjectProperties projectProperties;

    @Autowired
    private OfficeDao officeDao;

    @Autowired
    private IFileStorageAdapter fileStorageAdapter;

    // ---------- 多数据源路由 ----------
    @Value("${sys.file.datasource:master}")
    private String sysFileDatasource;

    @Lazy
    @Autowired
    private ZformService zformService;

    @Autowired
    private GenTableService genTableService;

    @Autowired
    private DatahouseService datahouseService;

    private static final String SYS_FILE_FORM_NO = "sys_file_";

    @PostConstruct
    public void logDatasourceInfo() {
        if (isDatahouseMode()) {
            logger.info("[sys_file_] 当前数据源模式: datahouse (Doris)，请确保 gen_table 中 sys_file_ 的模块已设置为 datahouse");
        } else {
            logger.info("[sys_file_] 当前数据源模式: master (MySQL)，请确保 gen_table 中 sys_file_ 的模块已设置为 admin");
        }
    }

    private boolean isDatahouseMode() {
        return GenTable.MODULE_DATAHOUSE.equalsIgnoreCase(sysFileDatasource);
    }

    /**
     * 在 datahouse 数据源上执行查询（替代 zformService.findMapList，后者不走 @DS 路由）
     */
    private List<LinkedHashMap> queryDatahouse(QueryWrapper queryWrapper, boolean isDelFlag) {
        String querySql = zformService.getQuerySql(SYS_FILE_FORM_NO);
        if (isDelFlag) {
            GenTable genTable = genTableService.getGenTableWithDefination(SYS_FILE_FORM_NO);
            if (genTable.getDelFlagExists()) {
                queryWrapper.eq("a.del_flag", '0');
            }
        }
        return datahouseService.findMapList(querySql, queryWrapper);
    }

    private LinkedHashMap queryDatahouseByMap(QueryWrapper queryWrapper, boolean isDelFlag) {
        List<LinkedHashMap> mapList = queryDatahouse(queryWrapper, isDelFlag);
        return (mapList != null && mapList.size() > 0) ? mapList.get(0) : null;
    }
    // ---------- 多数据源路由 END ----------

    public static final String STATUS_NO = "0";

    public List<SysFile> findListAndContent(SysFile sysFile) {
        if (isDatahouseMode()) {
            QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
            if (StringUtils.isNotBlank(sysFile.getGroupId())) {
                queryWrapper.eq("a.group_id_", sysFile.getGroupId());
            }
            if (StringUtils.isNotBlank(sysFile.getId())) {
                queryWrapper.eq("a.id", sysFile.getId());
            }
            List<LinkedHashMap> mapList = queryDatahouse(queryWrapper, false);
            return mapList.stream().map(this::mapToSysFile).collect(Collectors.toList());
        }
        return dao.findListAndContent(sysFile);
    }

    // ==================== 数据源路由 CRUD 重写 ====================

    @Override
    @Transactional(readOnly = false)
    public void save(SysFile sysFile) {
        if (sysFile.getUploadUser() != null
                && sysFile.getUploadUser().getId() != null
                && StringUtil.isEmpty(sysFile.getUploadUser().getName())) {
            sysFile.getUploadUser().setName(Objects.requireNonNull(UserUtil.get(sysFile.getUploadUser().getId())).getId());
        }
        if (isDatahouseMode()) {
            try {
                String loginName = UserUtil.getCurrentLoginName();
                if (loginName == null) loginName = "";
                JSONObject zformMap = buildZformMap(sysFile);
                Zform zform = zformService.getZformFromMap(zformMap, loginName);
                zform.setIsNewRecord(sysFile.getIsNewRecord());
                zformService.saveZform(zform, loginName, "/dynamic/zform");
                // 将 Zform 生成的 ID 回写到 SysFile 实体（模拟 MyBatis useGeneratedKeys）
                if (sysFile.getIsNewRecord() && StringUtils.isBlank(sysFile.getId())) {
                    sysFile.setId(zform.getId());
                }
                sysFile.setIsNewRecord(false);
            } catch (Exception e) {
                logger.error("datahouse 模式保存 sys_file_ 失败", e);
                throw new RuntimeException("datahouse 模式保存文件记录失败", e);
            }
        } else {
            super.save(sysFile);
        }
    }

    @Override
    public SysFile get(String id) {
        if (isDatahouseMode()) {
            QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("a.id", id);
            return mapToSysFile(queryDatahouseByMap(queryWrapper, true));
        }
        return super.get(id);
    }

    @Override
    public SysFile get(SysFile entity) {
        if (isDatahouseMode()) {
            QueryWrapper<Zform> queryWrapper = buildQueryWrapper(entity);
            return mapToSysFile(queryDatahouseByMap(queryWrapper, true));
        }
        return super.get(entity);
    }

    @Override
    public List<SysFile> findList(SysFile sysFile) {
        if (isDatahouseMode()) {
            QueryWrapper<Zform> queryWrapper = buildQueryWrapper(sysFile);
            List<LinkedHashMap> mapList = queryDatahouse(queryWrapper, true);
            return mapList.stream().map(this::mapToSysFile).collect(Collectors.toList());
        }
        return super.findList(sysFile);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(SysFile sysFile) {
        if (isDatahouseMode()) {
            try {
                GenTable genTable = genTableService.getGenTableWithDefination(SYS_FILE_FORM_NO);
                Zform zform = new Zform();
                zform.setId(sysFile.getId());
                zform.setFormNo(SYS_FILE_FORM_NO);
                zformService.delete(zform, genTable);
            } catch (Exception e) {
                logger.error("datahouse 模式删除 sys_file_ 失败", e);
                throw new RuntimeException("datahouse 模式删除文件记录失败", e);
            }
        } else {
            super.delete(sysFile);
        }
    }

    // ==================== 转换方法（替代 SysFileDatahouseHelper） ====================

    private JSONObject buildZformMap(SysFile sysFile) {
        JSONObject map = new JSONObject();
        map.put("formNo", SYS_FILE_FORM_NO);
        if (sysFile.getId() != null) map.put("id", sysFile.getId());
        if (sysFile.getOwnerCode() != null) map.put("owner_code", sysFile.getOwnerCode());
        if (sysFile.getGroupId() != null) map.put("group_id_", sysFile.getGroupId());
        if (sysFile.getName() != null) map.put("name_", sysFile.getName());
        if (sysFile.getExt() != null) map.put("ext_", sysFile.getExt());
        if (sysFile.getType() != null) map.put("type_", sysFile.getType());
        if (sysFile.getSize() != null) map.put("size_", sysFile.getSize());
        if (sysFile.getPath() != null) map.put("path_", sysFile.getPath());
        if (sysFile.getPdfPath() != null) map.put("pdf_path_", sysFile.getPdfPath());
        if (sysFile.getThumbPath() != null) map.put("thumb_path_", sysFile.getThumbPath());
        if (sysFile.getUploadTime() != null) map.put("upload_time_", sysFile.getUploadTime());
        if (sysFile.getUploadUser() != null && StringUtils.isNotBlank(sysFile.getUploadUser().getId())) {
            map.put("upload_user_id_", sysFile.getUploadUser().getId());
            map.put("upload_user_name_", sysFile.getUploadUser().getName());
        }
        if (sysFile.getSort() != null) map.put("sort", sysFile.getSort());
        if (sysFile.getDesc() != null) map.put("desc_", sysFile.getDesc());
        if (sysFile.getDuration() != null) map.put("duration_", sysFile.getDuration());
        if (sysFile.getSecFlag() != null) map.put("sec_flag_", sysFile.getSecFlag());
        if (sysFile.getVisitCount() != null) map.put("visit_count_", sysFile.getVisitCount());
        if (sysFile.getContent() != null) map.put("content_", sysFile.getContent());
        if (sysFile.getToPdf() != null) map.put("to_pdf_", sysFile.getToPdf());
        if (sysFile.getSecretLevel() != null) map.put("secret_level", sysFile.getSecretLevel());
        if (sysFile.getUrl() != null) map.put("url", sysFile.getUrl());
        if (sysFile.getThumbUrl() != null) map.put("thumb_url", sysFile.getThumbUrl());
        if (sysFile.getPrimaryPath() != null) map.put("primary_path_", sysFile.getPrimaryPath());
        if (sysFile.getEditObjects() != null) map.put("edit_objects", sysFile.getEditObjects());
        if (sysFile.getRemarks() != null) map.put("remarks", sysFile.getRemarks());
        return map;
    }

    private SysFile mapToSysFile(LinkedHashMap<String, Object> map) {
        if (map == null) return null;
        SysFile sysFile = new SysFile();
        sysFile.setId((String) map.get("id"));
        sysFile.setOwnerCode((String) map.get("owner_code"));
        sysFile.setGroupId((String) map.get("group_id_"));
        sysFile.setName((String) map.get("name_"));
        sysFile.setExt((String) map.get("ext_"));
        sysFile.setType((String) map.get("type_"));
        sysFile.setSize((String) map.get("size_"));
        sysFile.setPath((String) map.get("path_"));
        sysFile.setPdfPath((String) map.get("pdf_path_"));
        sysFile.setThumbPath((String) map.get("thumb_path_"));
        sysFile.setUploadTime(getDate(map.get("upload_time_")));
        sysFile.setSort(getInt(map.get("sort")));
        sysFile.setDesc((String) map.get("desc_"));
        sysFile.setDuration(getInt(map.get("duration_")));
        sysFile.setSecFlag((String) map.get("sec_flag_"));
        sysFile.setVisitCount(getInt(map.get("visit_count_")));
        sysFile.setContent((String) map.get("content_"));
        sysFile.setToPdf((String) map.get("to_pdf_"));
        sysFile.setSecretLevel((String) map.get("secret_level"));
        sysFile.setUrl((String) map.get("url"));
        sysFile.setThumbUrl((String) map.get("thumb_url"));
        sysFile.setPrimaryPath((String) map.get("primary_path_"));
        sysFile.setEditObjects((String) map.get("edit_objects"));
        String uploadUserId = (String) map.get("upload_user_id_");
        if (StringUtils.isNotBlank(uploadUserId)) {
            User uploadUser = new User();
            uploadUser.setId(uploadUserId);
            uploadUser.setName((String) map.get("upload_user_name_"));
            sysFile.setUploadUser(uploadUser);
        }
        return sysFile;
    }

    private QueryWrapper<Zform> buildQueryWrapper(SysFile sysFile) {
        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        if (sysFile.getId() != null) queryWrapper.eq("a.id", sysFile.getId());
        if (sysFile.getOwnerCode() != null) queryWrapper.eq("a.owner_code", sysFile.getOwnerCode());
        if (sysFile.getGroupId() != null) queryWrapper.eq("a.group_id_", sysFile.getGroupId());
        if (sysFile.getName() != null) queryWrapper.eq("a.name_", sysFile.getName());
        if (sysFile.getType() != null) queryWrapper.eq("a.type_", sysFile.getType());
        if (sysFile.getSecFlag() != null) queryWrapper.eq("a.sec_flag_", sysFile.getSecFlag());
        if (sysFile.getSecretLevel() != null) queryWrapper.eq("a.secret_level", sysFile.getSecretLevel());
        if (sysFile.getToPdf() != null) queryWrapper.eq("a.to_pdf_", sysFile.getToPdf());
        if (sysFile.getUploadUser() != null && StringUtils.isNotBlank(sysFile.getUploadUser().getId()))
            queryWrapper.eq("a.upload_user_id_", sysFile.getUploadUser().getId());
        return queryWrapper;
    }

    private Integer getInt(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date getDate(Object value) {
        if (value == null) return null;
        if (value instanceof Date) return (Date) value;
        if (value instanceof java.sql.Timestamp) return new Date(((java.sql.Timestamp) value).getTime());
        try {
            // 尝试解析常见日期字符串格式
            String str = value.toString();
            return DateUtil.parse(str);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== saveSecretLevel 数据源路由 ====================

    @Transactional(readOnly = false)
    public int saveSecretLevel(SysFile sysFile) {
        if (isDatahouseMode()) {
            try {
                String loginName = UserUtil.getCurrentLoginName();
                if (loginName == null) loginName = "";
                JSONObject zformMap = new JSONObject();
                zformMap.put("formNo", SYS_FILE_FORM_NO);
                zformMap.put("id", sysFile.getId());
                zformMap.put("secret_level", sysFile.getSecretLevel());
                Zform zform = zformService.getZformFromMap(zformMap, loginName);
                zform.setIsNewRecord(false);
                zformService.saveZform(zform, loginName, "/dynamic/zform");
                return 1;
            } catch (Exception e) {
                logger.error("datahouse 模式更新密级失败", e);
                throw new RuntimeException("datahouse 模式更新密级失败", e);
            }
        }
        return dao.saveSecretLevel(sysFile);
    }

    @Transactional(readOnly = false)
    public void saveSysFileList(String groupId, Map<String, Object> resultMap, boolean secFlag, String loginName) {
        User uploadUser = UserUtil.getByLoginName(loginName);
        List<SysFile> successList = (List<SysFile>) resultMap.get("successList");
        String ownerCode = this.findRootOffice().getCode();
        int sort = 0;
        if (StringUtils.isNotBlank(groupId)) {
            SysFile f = new SysFile();
            f.setGroupId(groupId);
            List<SysFile> files = this.findList(f);
            if (files != null && files.size() > 0) {
                sort = files.get(files.size() - 1).getSort();
            }
        }
        boolean toPdf = false;
        for (SysFile sysFile : successList) {
            if (FileUtil.TYPE_FILE.equals(sysFile.getType())) {
                toPdf = true;
                sysFile.setToPdf(this.STATUS_NO);
            }
            sysFile.setIsNewRecord(true);
            sysFile.setOwnerCode(ownerCode);
            sysFile.setUploadTime(new Date());
            sysFile.setUploadUser(uploadUser);
            sysFile.setSort(++sort);
            sysFile.setDesc("");
            if (true == secFlag) {
                if (FileUtil.TYPE_FILE.equals(sysFile.getType())) {
                    sysFile.setSecFlag(Global.YES);
                } else {
                    sysFile.setSecFlag(Global.NO);
                }
            } else {
                sysFile.setSecFlag(Global.NO);
            }
            sysFile.setVisitCount(0);
            this.save(sysFile);
        }
    }

    @Transactional(readOnly = false)
    public void saveSysFileList(String groupId, Map<String, Object> resultMap, String loginName) {
        saveSysFileList(groupId, resultMap, true, loginName);
    }

    public Office findRootOffice() {
        Office office = new Office();
        office.getSqlMap().put("dsf", " AND a.parent_id = '0' ");
        List<Office> list = officeDao.findList(office);
        //list 根据a.parent_ids, a.sort, a.code排序
        Collections.sort(list, Comparator.comparing(Office::getParentId)
                .thenComparing(Office::getSort)
                .thenComparing(Office::getCode));
        if (list != null && list.size() > 0) {
            for (Office f : list) {
                if ((Global.DEFAULT_ROOT_CODE).equals(f.getParentId())) {
                    office = f;
                    break;
                }
            }
        }
        return office;
    }

    @Transactional(readOnly = false)
    public Map<String, Object> uploadFileComplete(String requestURI,
                                                  String groupId,
                                                  String fileName,
                                                  String fileSize,
                                                  String chunk,
                                                  String cSize,
                                                  String secret,
                                                  String template,
                                                  String oss,
                                                  String idPrefix,
                                                  String fileRoot,
                                                  String fileUploadFolder,
                                                  String uploadPathDateFormat,
                                                  String loginName) {
        SysFile sysFileSuccess = new SysFile();
        if (StringUtil.isNotEmpty(idPrefix)) {
            String preId = fileName;
            if (preId.indexOf(".") != -1) {
                preId = preId.substring(0, preId.lastIndexOf("."));
            }
            if (preId.startsWith(idPrefix)) {
                sysFileSuccess.setPreId(preId);
            } else {
                sysFileSuccess.setPreId(idPrefix + preId);
            }
        }
        Date theDate = new Date();
        String fileType = FileUtil.getFileType(fileName);
        groupId = StringUtils.isNotBlank(groupId) ? groupId : UUID.randomUUID().toString();
        sysFileSuccess.setGroupId(groupId);
        sysFileSuccess.setName(fileName);
        sysFileSuccess.setExt(FilenameUtils.getExtension(fileName));
        sysFileSuccess.setType(fileType);
        sysFileSuccess.setSize(this.getFileSizeMerge(Long.valueOf(fileSize)));
        Map<String, Object> map = Maps.newHashMap();
        map.put("groupId", groupId);
        this.uploadFileComplete(requestURI, groupId, fileType, fileName, theDate, sysFileSuccess, chunk, map, cSize, secret, template, oss, idPrefix, fileRoot, fileUploadFolder, uploadPathDateFormat, loginName);
        return map;
    }

    public void uploadFileComplete(String requestURI,
                                   String groupId,
                                   String fileType,
                                   String fileNameMerge,
                                   Date theDate,
                                   SysFile sysFileSuccess,
                                   String chunk,
                                   Map<String, Object> map,
                                   String cSize,
                                   String secret,
                                   String template,
                                   String oss,
                                   String idPrefix,
                                   String fileRoot,
                                   String fileUploadFolder,
                                   String uploadPathDateFormat,
                                   String loginName) {
        int cs = Integer.valueOf(cSize);
        List<SysFile> successList = Lists.newArrayList();
        List<SysFile> failList = Lists.newArrayList();

        sysFileSuccess.setGroupId(groupId);
        String downloadPath = this.getDownloadPath(requestURI, groupId, theDate, fileUploadFolder, uploadPathDateFormat, template);
        String uploadPath = this.getUploadPath(requestURI, groupId, theDate, fileRoot, fileUploadFolder, uploadPathDateFormat, template);
        if (StringUtil.isEmpty(idPrefix)) {
            sysFileSuccess.setPath(downloadPath + fileNameMerge);
        } else {
            if (fileNameMerge.startsWith(idPrefix)) {
                sysFileSuccess.setPath(templatePath + "/" + fileNameMerge);
            } else {
                sysFileSuccess.setPath(templatePath + "/" + idPrefix + fileNameMerge);
            }
        }
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileOutputStream fileou = null;
        FileOutputStream fileouEncode = null;
        FileInputStream filein = null;
        try {
            File outDir = new File(fileRoot + sysFileSuccess.getPath().substring(0, sysFileSuccess.getPath().lastIndexOf("/")));
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            fileou = new FileOutputStream(fileRoot + sysFileSuccess.getPath(), false);
            int num_name = Integer.valueOf(chunk);
            for (int i = 0; i < num_name; i++) {
                filein = new FileInputStream(uploadPath + i + "_chunk_" +fileNameMerge.substring(0, fileNameMerge.lastIndexOf('.')));
                byte[] fileBuffer = new byte[cs];
//                byte[] fileBuffer2 = new byte[cs];
                int n = 0;
                while ((n = filein.read(fileBuffer)) != -1) {
                    fileou.write(fileBuffer, 0, n);
                }
                if (filein != null) {
                    filein.close();
                }

                if (StringUtil.isNotEmpty(secret)) {
                    FileInputStream fis = new FileInputStream(uploadPath + fileNameMerge);
                    byte[] b = new byte[fis.available()];
                    int read = fis.read(b);
                    for (int j = 0; j < read; j++) {
                        b[j] = (byte) (b[j] ^ 0x99);
                    }
                    FileOutputStream fos = new FileOutputStream(uploadPath + fileNameMerge);
                    fos.write(b);
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                }

                File mergeFile = new File(uploadPath + i + "_chunk_" +fileNameMerge.substring(0, fileNameMerge.lastIndexOf('.')));
                if (mergeFile.exists()) {
                    mergeFile.delete();
                }
            }

            String webappFile = null;
            String fileNameLast = "";
            String fileNameWithoutExtension = "";
            if (fileNameMerge.indexOf(".") != -1) {
                fileNameLast = fileNameMerge.substring(fileNameMerge.indexOf("."));
                fileNameWithoutExtension = fileNameMerge.substring(0, fileNameMerge.lastIndexOf(".")); // 获取文件名（不包含扩展名）
            }
            if (FileUtil.TYPE_PIC.equals(fileType)) {
                String inputFile = uploadPath + fileNameMerge;
                String fileName = fileNameMerge;
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                String outputFile = uploadPath + fileName + "_thumb" + fileNameLast;
                PicUtil.commpressPicForScale(inputFile, outputFile, 100, 0.9);
                sysFileSuccess.setThumbPath(downloadPath + fileName + "_thumb" + fileNameLast);
                webappFile = this.getUploadPath(requestURI, groupId, theDate, fileRoot, fileUploadFolder, uploadPathDateFormat, template) + fileName + "_thumb" + fileNameLast;
                FileUtil.copyFile(outputFile, webappFile);
            }
            if (StringUtil.isNotEmpty(oss)) {
                File uploadedFile = new File(uploadPath + fileNameMerge);
                String ossFileName = dataService.nextId();
                String ossUrl = doUploadFile(oss, fileNameWithoutExtension +"-"+ ossFileName + fileNameLast, uploadedFile);
                sysFileSuccess.setUrl(ossUrl);
                if (webappFile != null) {
                    File uploadedFileThumb = new File(webappFile);
                    String thumbUrl = doUploadFile(oss, "thumb_" + ossFileName + fileNameLast, uploadedFileThumb);
                    sysFileSuccess.setThumbUrl(thumbUrl);
                }
            }
            if (StringUtil.isNotEmpty(idPrefix) && sysFileSuccess.getName().startsWith(idPrefix)) {
                sysFileSuccess.setName(sysFileSuccess.getName().replaceFirst(idPrefix, ""));
            }
            successList.add(sysFileSuccess);
            map.put("successList", successList);
            map.put("failList", failList);
            saveSysFileList(groupId, map, loginName);
        } catch (Exception err) {
            sysFileSuccess.setName(fileNameMerge);
            sysFileSuccess.setFailType("IllegalExtension");
            failList.add(sysFileSuccess);
            map.put("successList", successList);
            map.put("failList", failList);
            logger.error("文件合并处理失败", err);
        } finally {
            if (fileou != null)
                try {
                    fileou.close();
                } catch (IOException e) {
                    logger.warn("关闭文件输出流失败", e);
                }
            if (filein != null)
                try {
                    filein.close();
                } catch (IOException e) {
                    logger.warn("关闭文件输入流失败", e);
                }
            if (fileouEncode != null)
                try {
                    fileouEncode.close();
                } catch (IOException e) {
                    logger.warn("关闭编码输出流失败", e);
                }
        }

    }

    @Transactional(readOnly = false)
    public LinkedHashMap<String, Object> getFileList(String groupId) {
        LinkedHashMap<String, Object> map = Maps.newLinkedHashMap();
        SysFile sysFile = new SysFile();
        sysFile.setGroupId(groupId);
        if (StringUtils.isBlank(groupId)) {
            return map;
        }
        List<SysFile> sysFileListInDb = this.findList(sysFile);
        map.put("files", sysFileListInDb);
        return map;
    }

    @Transactional(readOnly = true)
    public List<SysFile> getFileListByGroupId(String groupId) {
        SysFile sysFile = new SysFile();
        sysFile.setGroupId(groupId);
        List<SysFile> sysFileListInDb = this.findList(sysFile);
        return sysFileListInDb;
    }

    @Transactional(readOnly = true)
    public SysFile findFirstByStoragePath(String rawPath) {
        List<String> pathCandidates = buildStoragePathCandidates(rawPath);
        if (pathCandidates.isEmpty()) {
            return null;
        }
        List<SysFile> sysFiles = dao.findByStoragePath(pathCandidates);
        if (sysFiles == null || sysFiles.isEmpty()) {
            return null;
        }
        return sysFiles.get(0);
    }

    @Transactional(readOnly = true)
    public SysFile findFirstObjectStorageFile(SysFile sysFile) {
        if (sysFile == null || StringUtils.isNotBlank(sysFile.getUrl())) {
            return sysFile;
        }
        LinkedHashSet<String> pathCandidates = new LinkedHashSet<>();
        pathCandidates.addAll(buildStoragePathCandidates(sysFile.getPath()));
        pathCandidates.addAll(buildStoragePathCandidates(sysFile.getThumbPath()));
        if (pathCandidates.isEmpty()) {
            return sysFile;
        }
        List<SysFile> sysFiles = dao.findByStoragePath(new ArrayList<>(pathCandidates));
        if (sysFiles == null || sysFiles.isEmpty()) {
            return sysFile;
        }
        for (SysFile item : sysFiles) {
            if (StringUtils.isNotBlank(item.getUrl())) {
                return item;
            }
        }
        return sysFile;
    }

    private List<String> buildStoragePathCandidates(String rawPath) {
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        addStoragePathCandidate(paths, rawPath);
        if (StringUtils.isNotBlank(rawPath)) {
            String decoded = decodeStoragePath(rawPath);
            addStoragePathCandidate(paths, decoded);
            if (!decoded.equals(rawPath)) {
                addStoragePathCandidate(paths, decodeStoragePath(decoded));
            }
        }
        return new ArrayList<>(paths);
    }

    private String decodeStoragePath(String path) {
        try {
            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return path;
        }
    }

    private void addStoragePathCandidate(Set<String> paths, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        String path = value.trim();
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        int hashIndex = path.indexOf('#');
        if (hashIndex >= 0) {
            path = path.substring(0, hashIndex);
        }
        path = path.replace('\\', '/');
        while (path.contains("//")) {
            path = path.replace("//", "/");
        }
        addNormalizedStoragePath(paths, path);
        int jeeStudioIndex = path.indexOf("jeeStudio/");
        if (jeeStudioIndex >= 0) {
            addNormalizedStoragePath(paths, path.substring(jeeStudioIndex));
        }
        int uploadIndex = path.indexOf("fileUploadBatchProgress/");
        if (uploadIndex >= 0) {
            String uploadPath = path.substring(uploadIndex);
            addNormalizedStoragePath(paths, uploadPath);
            addNormalizedStoragePath(paths, "system/sysFile/" + uploadPath);
            addNormalizedStoragePath(paths, "gtoa/a/system/sysFile/" + uploadPath);
            addNormalizedStoragePath(paths, "jeeStudio/gtoa/a/system/sysFile/" + uploadPath);
        }
    }

    private void addNormalizedStoragePath(Set<String> paths, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        String path = value.trim();
        String withoutLeadingSlash = path;
        while (withoutLeadingSlash.startsWith("/")) {
            withoutLeadingSlash = withoutLeadingSlash.substring(1);
        }
        if (StringUtils.isBlank(withoutLeadingSlash)) {
            return;
        }
        paths.add(withoutLeadingSlash);
        paths.add("/" + withoutLeadingSlash);
        paths.add("userfiles/" + withoutLeadingSlash);
        paths.add("/userfiles/" + withoutLeadingSlash);
    }

    @Transactional(readOnly = false)
    public File getFile(String fileId, String fileRoot) {
        SysFile sysFile = this.get(fileId);
        if (sysFile == null) {
            return null;
        } else {
            String url = sysFile.getPath();
            File file = new File(fileRoot + Encodes.unescapeHtml(url));
            // #13修复：路径穿越校验
            validateFilePath(file, fileRoot);
            return file;
        }
    }
    @Transactional(readOnly = false)
    public File getFile(String fileId, String fileRoot,boolean isEdit) {
        String url = this.get(fileId).getPrimaryPath();
        if(url==null) {
            url = this.get(fileId).getPath();
        }
        File file = new File(fileRoot + Encodes.unescapeHtml(url));
        // #13修复：路径穿越校验
        validateFilePath(file, fileRoot);
        return file;
    }
    @Transactional(readOnly = true)
    public File getFirstByGroupId(String groupId, String fileRoot) {
        SysFile sysFile = new SysFile();
        sysFile.setGroupId(groupId);
        List<SysFile> sysFileListInDb = this.findList(sysFile);
        String fileId = "";
        for (SysFile theSysFile : sysFileListInDb) {
            fileId = theSysFile.getId();
            break;
        }
        if (StringUtil.isNotEmpty(fileId)) {
            String url = this.get(fileId).getPath();
            File file = new File(fileRoot + Encodes.unescapeHtml(url));
            // #13修复：路径穿越校验
            validateFilePath(file, fileRoot);
            return file;
        } else {
            return null;
        }
    }

    /**
     * #13修复：校验文件路径是否在允许的根目录下，防止路径穿越攻击
     */
    private void validateFilePath(File file, String fileRoot) {
        try {
            String canonicalPath = file.getCanonicalPath();
            String canonicalRoot = new File(fileRoot).getCanonicalPath();
            if (!canonicalPath.startsWith(canonicalRoot)) {
                throw new SecurityException("非法文件路径：文件路径不在允许的目录范围内");
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("文件路径校验失败", e);
        }
    }

    @Transactional(readOnly = true)
    public String getFirstFilePathByGroupId(String groupId, String fileRoot) {
        SysFile sysFile = new SysFile();
        sysFile.setGroupId(groupId);
        List<SysFile> sysFileListInDb = this.findList(sysFile);
        String filePath = "";
        for (SysFile theSysFile : sysFileListInDb) {
            filePath = fileRoot + theSysFile.getPath();
            break;
        }
        return filePath;
    }

    @Transactional(readOnly = false)
    public void deleteFile(String fileId, String fileRoot) {
        this.delete(this.get(fileId), fileRoot);
    }

    private String getFileSizeMerge(long size) {
        if (size / 1024 < 1024) {
            return "(" + new DecimalFormat("0.0").format(size / 1024D) + "K)";
        } else {
            return "(" + new DecimalFormat("0.0").format(size / 1024D / 1024D) + "M)";
        }
    }

    private String getDownloadPath(String requestURI, String randomUUID, Date theDate, String fileUploadFolder, String uploadPathDateFormat, String template) {
        if (StringUtil.isNotEmpty(template)) {
            return new StringBuffer().append(fileUploadFolder).append(requestURI)
                    .append(template)
                    .append("/").append(randomUUID).append("/").toString();
        } else {
            return new StringBuffer().append(fileUploadFolder).append(requestURI)
                    .append(new SimpleDateFormat(uploadPathDateFormat).format(theDate))
                    .append(randomUUID).append("/").toString();
        }
    }

    private String getUploadPath(String requestURI, String randomUUID, Date theDate, String fileRoot, String fileUploadFolder, String uploadPathDateFormat, String template) {
        String realPath = fileRoot + fileUploadFolder;
        if (StringUtil.isNotEmpty(template)) {
            return new StringBuffer().append(realPath).append(requestURI)
                    .append(template)
                    .append("/").append(randomUUID).append("/").toString();
        } else {
            return new StringBuffer().append(realPath).append(requestURI)
                    .append(new SimpleDateFormat(uploadPathDateFormat).format(theDate))
                    .append(randomUUID).append("/").toString();
        }
    }

    @Transactional(readOnly = false)
    public void delete(SysFile sysFile, String fileRoot) {
        /*String url = sysFile.getPath();
        File file = new File(fileRoot + Encodes.unescapeHtml(url));
        if (file.exists()) {
            file.delete();
        }
        String pdfUrl = sysFile.getPdfPath();
        File pdfFile = new File(fileRoot + Encodes.unescapeHtml(pdfUrl));
        if (pdfFile.exists()) {
            pdfFile.delete();
        }
        String thumbUrl = sysFile.getThumbPath();
        File thumbFile = new File(fileRoot + Encodes.unescapeHtml(thumbUrl));
        if (thumbFile.exists()) {
            thumbFile.delete();
        }
        String fileType = sysFile.getType();
        if (FileUtil.TYPE_PIC.equals(fileType)) {
            String webappUrl = SpringContextHolder.getBean(ServletContext.class).getRealPath(sysFile.getThumbPath());
            File webappFile = new File(webappUrl);
            if (webappFile.exists()) {
                webappFile.delete();
            }
        }
        if (FileUtil.TYPE_VIDEO.equals(fileType) || FileUtil.TYPE_AUDIO.equals(fileType)) {
            String webappUrl = SpringContextHolder.getBean(ServletContext.class).getRealPath(sysFile.getPath());
            File webappFile = new File(webappUrl);
            if (webappFile.exists()) {
                webappFile.delete();
            }
        }*/
        super.delete(sysFile);
    }

    /**
     * 统一文件上传方法，使用 IFileStorageAdapter
     *
     * @param typeBucketFolder 类型:bucket:文件夹 或 bucket:文件夹 或 文件夹
     * @param fileName         文件名
     * @param file             文件
     * @return 文件URL
     */
    private String doUploadFile(String typeBucketFolder, String fileName, File file) {
        // 解析 typeBucketFolder 参数
        String[] params = typeBucketFolder.split(":");
        String bucket = null;
        String folder = null;
        if (params.length == 3) {
            // type:bucket:folder — type 由 adapter 内部决定
            bucket = params[1];
            folder = params[2];
        } else if (params.length == 2) {
            bucket = params[0];
            folder = params[1];
        } else {
            folder = params[0];
        }
        // 处理日期占位符
        if (folder != null && folder.contains("%")) {
            java.time.LocalDate date = java.time.LocalDate.now();
            String year = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy"));
            String month = date.format(java.time.format.DateTimeFormatter.ofPattern("MM"));
            String day = date.format(java.time.format.DateTimeFormatter.ofPattern("dd"));
            folder = folder.replaceAll("%Y", year).replaceAll("%y", year);
            folder = folder.replaceAll("%m", month);
            folder = folder.replaceAll("%d", day);
        }

        try (InputStream is = new FileInputStream(file)) {
            return fileStorageAdapter.upload(bucket, folder, fileName, is, null);
        } catch (Exception e) {
            logger.error("文件上传失败: {}", fileName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Value("${fileRoot}")
    protected String urlFile;

    /**
     * 获取文件磁盘路径
     * @param sysFile
     * @return
     */
    public String getFileDiskPath(SysFile sysFile){
        return urlFile + sysFile.getPath();
    }

    /**
     * 保存zip文件
     *
     * @param fileName 文件名
     * @param fileList 文件列表
     * @return
     */
    public SysFile saveZipSysFile(String fileName, List<SysFile> fileList) {
        String groupId = UUID.randomUUID().toString().replaceAll("-", "");
        String folder = "ExportZip";
        String dateFolder = DateUtil.formatDate(new Date()).replaceAll("-", "\\" + File.separator);
        String path = File.separator + folder + File.separator + dateFolder + File.separator + groupId;
        String parent = urlFile + path + File.separator + "temp";
        new File(parent).mkdirs();
        for (SysFile sysFile : fileList) {
            String filePath = urlFile + sysFile.getPath();
            String tempPath = parent + File.separator + sysFile.getName();
            FileUtil.copyFile(filePath, tempPath);
        }
        fileName = fileName + ".zip";
        ZipUtil.zip(parent, urlFile + path + File.separator + "/result/" + fileName);
        File file = new File(urlFile + path + File.separator + "/result/" + fileName);
        byte[] bytes = cn.hutool.core.io.FileUtil.readBytes(file);
        return saveExportSysFile(fileName, bytes, groupId);
    }

    /**
     * 生成zip文件
     * @param fileName 压缩文件名称
     * @param fileMap 需要压缩的文件map {path1:List<Consumer<String>,path2:List<Consumer<String>}
     * @return
     */
    public SysFile saveZipSysFile(String fileName, LinkedHashMap<String, List<Consumer<String>>> fileMap) {
        String zipFileName = fileName;
        if (!zipFileName.endsWith(".zip")) {
            zipFileName += ".zip";
        }
        String groupId = UUID.randomUUID().toString().replaceAll("-", "");
        String folder = "ExportZip";
        String rootTempPath = fileRoot + "/" + folder + "/" + groupId + "/";
        String toCompressFolder = fileRoot + "/" + folder + "temp/" + groupId + "/";
        //开启线程池获取文件
        // 单次计数器
        final CountDownLatch countDownLatchFolder = new CountDownLatch(fileMap.size()); //
        ExecutorService executorFolder = Executors.newFixedThreadPool(16);

        logger.info("压缩文件-线程池开启");
        for (Map.Entry<String, List<Consumer<String>>> entry : fileMap.entrySet()) {
            executorFolder.submit(() -> {
                try {
                    //新建文件夹
                    String dirsPath = toCompressFolder + entry.getKey();
                    File director = new File(dirsPath);
                    director.mkdirs();

                    List<Consumer<String>> consumerList = entry.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList());
                    logger.info("压缩文件-{}-线程池开启", entry.getKey());
                    //开启线程池获取文件
                    // 单次计数器
                    final CountDownLatch countDownLatchPs = new CountDownLatch(consumerList.size()); //
                    ExecutorService executorPs = Executors.newFixedThreadPool(16);
                    for (Consumer<String> consumer : consumerList) {
                        executorPs.submit(() -> {
                            try {
                                consumer.accept(dirsPath);
                            } catch (Exception e) {
                                logger.error(ExceptionUtil.stacktraceToString(e));
                            } finally {
                                countDownLatchPs.countDown();
                            }
                        });
                    }
                    try {
                        countDownLatchPs.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    executorPs.shutdown();
                    logger.info("压缩文件-{}-线程池关闭", entry.getKey());
                } finally {
                    countDownLatchFolder.countDown();
                }
            });
        }
        try {
            countDownLatchFolder.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorFolder.shutdown();
        logger.info("压缩文件-线程池关闭");

        //压缩文件
        try {
            ZipUtil.zip(toCompressFolder, rootTempPath + zipFileName);
            //删除临时文件夹
            cn.hutool.core.io.FileUtil.del(toCompressFolder);
        } catch (Exception e) {
            logger.error(ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("压缩文件失败");
        }
        //将压缩文件保存到数据库中
        try {
            return this.saveExportSysFile(zipFileName, Files.newInputStream(Paths.get(rootTempPath + zipFileName)), groupId);
        } catch (IOException e) {
            logger.error(ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("保存压缩文件失败");
        }

    }

    /**
     * 保存文件
     *
     * @param fileName 文件名 abc.docx
     * @param bytes    文件字节
     * @return
     */
    @Deprecated
    public SysFile saveExportSysFile(String fileName, byte[] bytes) {
        return this.saveExportSysFile(fileName, bytes, UUID.randomUUID().toString().replaceAll("-", ""));
    }

    /**
     * 保存文件
     *
     * @param fileName 文件名 abc.docx
     * @param bytes    文件字节
     * @param groupId  文件组id
     * @return
     */
    @Deprecated
    public SysFile saveExportSysFile(String fileName, byte[] bytes, String groupId) {
        return this.saveSysFile(fileName, bytes, groupId, "Export");
    }

    @Deprecated
    public SysFile saveSysFile(String fileName, byte[] bytes, String groupId, String folder) {
        SysFile sysFile = new SysFile();
        sysFile.setGroupId(groupId);
        sysFile.setName(fileName);
        sysFile.setExt(FilenameUtils.getExtension(fileName));
        String dateFolder = DateUtil.formatDate(new Date()).replaceAll("-", "\\" + File.separator);
        String path = File.separator + folder + File.separator + dateFolder + File.separator + groupId;
        new File(urlFile + path).mkdirs();
        File file = new File(urlFile + path + File.separator + fileName);
        try {
            FileUtil.writeByteArrayToFile(file, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long size = file.length();
        String sizeStr = getFileSizeMerge(size);
        sysFile.setSize(sizeStr);
        sysFile.setPath(path + File.separator + fileName);
        sysFile.setUploadTime(new Date());
        sysFile.setType("FILE");
        sysFile.setSort(1);
        sysFile.setSecFlag(Global.NO);
        sysFile.setVisitCount(0);
        sysFile.setToPdf(Global.NO);
        this.save(sysFile);
        logger.info("保存文件成功：{},{}", sysFile.getId(), file.getPath());
        return sysFile;
    }
    /**
     * 保存文件
     *
     * @param fileName 文件名 abc.xlsx
     * @param workbook    excel
     * @return
     */
    public SysFile saveExportSysFile(String fileName, Workbook workbook) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            workbook.write(byteArrayOutputStream);
            return this.saveExportSysFile(fileName, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        } catch (IOException e) {
            throw new BusinessException("保存excel文件失败");
        }
    }

    /**
     * 保存文件
     *
     * @param fileName 文件名 abc.docx
     * @param inputStream    输入流
     * @return
     */
    public SysFile saveExportSysFile(String fileName, InputStream inputStream) {
        return this.saveExportSysFile(fileName, inputStream, UUID.randomUUID().toString().replaceAll("-", ""));
    }

    /**
     * 保存文件
     *
     * @param fileName 文件名 abc.docx
     * @param inputStream    输入流
     * @param groupId  文件组id
     * @return
     */
    public SysFile saveExportSysFile(String fileName, InputStream inputStream, String groupId) {
        return this.saveSysFile(fileName, inputStream, groupId, "Export");
    }

    /**
     * 保存文件
     * @param fileName  文件名 abc.docx
     * @param inputStream  输入流
     * @param groupId  文件组id
     * @param folder  文件夹
     * @return
     */
    public SysFile saveSysFile(String fileName, InputStream inputStream, String groupId, String folder) {
        SysFile sysFile = new SysFile();
        sysFile.setGroupId(groupId);
        sysFile.setName(fileName);
        sysFile.setExt(FilenameUtils.getExtension(fileName));
        String dateFolder = DateUtil.formatDate(new Date()).replaceAll("-", "\\" + File.separator);
        String path = File.separator + folder + File.separator + dateFolder + File.separator + groupId;
        logger.debug("path: "+path);
        logger.debug("dateFolder: "+dateFolder);
        logger.debug("groupId: "+groupId);
        logger.debug("folder: "+folder);
        logger.debug("fileName: "+fileName);
        logger.debug("file from: "+urlFile + path + File.separator + fileName);
        new File(urlFile + path).mkdirs();
        File file = new File(urlFile + path + File.separator + fileName);
        try {
            OutputStream outputStream = Files.newOutputStream(file.toPath());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long size = file.length();
        String sizeStr = getFileSizeMerge(size);
        sysFile.setSize(sizeStr);
        sysFile.setPath(path + File.separator + fileName);

        logger.debug("setPath: "+path + File.separator + fileName);

        sysFile.setUploadTime(new Date());
        sysFile.setType("FILE");
        sysFile.setSort(1);
        sysFile.setSecFlag(Global.NO);
        sysFile.setVisitCount(0);
        sysFile.setToPdf(Global.NO);
        this.save(sysFile);

        logger.debug("sysFile id: "+sysFile.getId());

        return sysFile;
    }

}
