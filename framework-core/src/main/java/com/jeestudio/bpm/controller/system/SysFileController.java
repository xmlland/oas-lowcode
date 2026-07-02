package com.jeestudio.bpm.controller.system;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.storage.IFileStorageAdapter;
import com.jeestudio.bpm.utils.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import jakarta.mail.internet.MimeUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: 系统设置系统文件
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("${adminPath}/system/sysFile")
public class SysFileController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(SysFileController.class);

    @Value("${fileRoot}")
    private String fileRoot;


    @Value("${fileUploadFolder}")
    private String fileUploadFolder;

    @Value("${uploadPathDateFormat}")
    private String uploadPathDateFormat;

    @Value("${allowedExtensions}")
    private String allowedExtensions;

    @Autowired
    SysFileService sysFileService;

    @Autowired
    GenTableService genTableService;

    @Autowired
    private IFileStorageAdapter fileStorageAdapter;

    /**
     * 批量上传进度查询
     * oss minio:main:test 对象存储类型:存储桶:目录
     * oss main:test 存储桶:目录；对象存储类型读取配置 storage.default-provider 默认是本地对象存储minio
     * oss test 目录；对象存储类型读取配置 storage.default-provider 默认是本地对象存储minio，存储桶读取配置 storage.minio.bucket
     */
    @Operation(summary = "批量上传进度查询")
    @RequiresPermissions("user")
    @PostMapping("/fileUploadBatchProgress")
    public ResultJson fileUploadBatchProgress(HttpServletRequest request,
                                              @RequestParam(value = "file", required = false) MultipartFile[] file,
                                              @RequestParam(value = "groupId", required = false) String groupId,
                                              @RequestParam(value = "fileName", required = false) String fileName,
                                              @RequestParam(value = "fileSize", required = false) String fileSize,
                                              @RequestParam(value = "chunk", required = false) String chunk,
                                              @RequestParam(value = "isChunk", required = false) String isChunk,
                                              @RequestParam(value = "cSize", required = false) String cSize,
                                              @RequestParam(value = "secret", required = false, defaultValue = "") String secret,
                                              @RequestParam(value = "template", required = false, defaultValue = "") String template,
                                              @RequestParam(value = "oss", required = false, defaultValue = "") String oss,
                                              @RequestParam(value = "idPrefix", required = false, defaultValue = "") String idPrefix) {

        //oss = "minio:main:test";
        ResultJson resultJson = null;
        if ("false".equals(isChunk) == false && file != null) {
            Boolean result = this.fileUploadBatchProgress(request.getRequestURI(), file, groupId, chunk);
            resultJson = new ResultJson();
            if (result){
                resultJson.setCode(ResultJson.CODE_SUCCESS);
                resultJson.setMsg("上传Chunk成功");
                resultJson.setMsg_en("Upload Chunk success");
            }else {
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("上传Chunk失败");
                resultJson.setMsg_en("Upload Chunk error");
            }
        } else {
            resultJson = this.uploadFileCompleteOk(request.getRequestURI(), groupId, fileName, fileSize, chunk, cSize, secret, template, oss, idPrefix, fileRoot, fileUploadFolder, uploadPathDateFormat, currentUserName.get());
        }
        resultJson.setToken(token.get());
        return resultJson;
    }

    private ResultJson uploadFileCompleteOk(String requestURI,
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
        ResultJson resultJson = new ResultJson();
        try {
            Map<String, Object> map = sysFileService.uploadFileComplete(requestURI, groupId, fileName, fileSize, chunk, cSize, secret, template, oss, idPrefix, fileRoot, fileUploadFolder, uploadPathDateFormat, loginName);
            resultJson.put("map", map);
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("上传成功");
            resultJson.setMsg_en("Upload file success");
        } catch (Exception e) {
            logger.error("Error while uploading file:" + ExceptionUtils.getStackTrace(e));
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("上传失败");
            resultJson.setMsg_en("Upload file failed");
        }
        return resultJson;
    }

    /**
     * 多文件上传 不分片
     */
    @Operation(summary = "批量上传文件")
    @RequiresPermissions("user")
    @PostMapping("/batchUploadFiles")
    public ResultJson batchUploadFiles(HttpServletRequest request, HttpServletResponse response) {

        ResultJson resultJson = new ResultJson();
        // 文件分组Id，如果文件分组Id为空重新生成一个uuid
        String groupId = request.getParameter("groupId");
        if (StringUtil.isEmpty(groupId)) {
            groupId = UUID.randomUUID().toString();
        }
        List<SysFile> sysFileList = new ArrayList<>();
        try {
            MultiValueMap<String, MultipartFile> fileMap = ((MultipartHttpServletRequest) request).getMultiFileMap();
            for (List<MultipartFile> value : fileMap.values()) {
                if (value != null && value.size() > 0) {
                    for (int i = 0; i < value.size(); i++) {
                        MultipartFile file = value.get(i);
                        SysFile sysFile = sysFileService.saveSysFile(file.getOriginalFilename(), file.getBytes(), groupId, fileUploadFolder + request.getRequestURI());
                        sysFileList.add(sysFile);
                    }
                }
            }
            LinkedHashMap resData = new LinkedHashMap();
            resData.put("files", sysFileList);
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("上传Chunk成功");
            resultJson.setMsg_en("Upload Chunk success");
            resultJson.setData(resData);
        } catch (IOException e) {
            logger.error("上传Chunk异常", e);

            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("上传失败");
            resultJson.setMsg_en("Upload Chunk ERROR");

        }
        resultJson.setToken(token.get());
        return resultJson;
    }


    /**
     * 获取文件列表
     */
    @Operation(summary = "获取文件列表")
    /*@RequiresPermissions("user")*/
    @PostMapping("/getFileList")
    public ResultJson getFileList(@RequestParam(value = "groupId", required = false) String groupId) {
        ResultJson resultJson = this.getFileListOk(groupId);
        /*resultJson.setToken(token.get());*/
        return resultJson;
    }

    private ResultJson getFileListOk(String groupId) {
        ResultJson resultJson = new ResultJson();
        try {
            LinkedHashMap<String, Object> map = sysFileService.getFileList(groupId);
            resultJson.put("fileListMap", map);
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("获取文件列表成功");
            resultJson.setMsg_en("Get file list success");
        } catch (Exception e) {
            logger.error("Error while getting file list:" + ExceptionUtils.getStackTrace(e));
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("获取文件列表失败");
            resultJson.setMsg_en("Get file list failed");
        }
        return resultJson;
    }

    /**
     * 下载文件压缩包
     */
    @Operation(summary = "下载文件压缩包")
    @GetMapping("/fileDownloadZip")
    public String fileDownloadZip(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "groupId", required = true) String groupId, @RequestParam(value = "fileName", required = false) String fileName) throws IOException {
        ResultJson resultJson = this.getFileListOk(groupId);
        LinkedHashMap<String, Object> data = resultJson.getData();
        LinkedHashMap<String, Object> fileListMap = (LinkedHashMap<String, Object>) data.get("fileListMap");
        List<SysFile> files = (List<SysFile>) fileListMap.get("files");
        Map<String, String> map = Maps.newLinkedHashMap();
        for (SysFile file : files) {
            //        JsonConvertUtil.gsonBuilder().fromJson(zformString, new TypeToken<Zform>(){}.getType());
            map.put((String) Optional.ofNullable(file.getName()).orElseGet(() -> ""), fileRoot + (String) Optional.ofNullable(file.getPath()).orElseGet(() -> ""));
        }
        if (StringUtil.isEmpty(fileName)) fileName = groupId + ".zip";
        ZipUtil.MultiFileZipDownload(response, fileName, map, fileRoot);
        return null;
    }

    public String fileDownload(HttpServletRequest request, HttpServletResponse response, File file, List<SysFile> sysFiles, String secret) throws IOException {
        String fileName = null;
        if(file != null){
            fileName = file.getName();
        }
        return fileDownload(request, response, file, fileName, sysFiles, secret);
    }

    public String fileDownload(HttpServletRequest request, HttpServletResponse response, File file, String fileName, List<SysFile> sysFiles, String secret) throws IOException {
        if (sysFiles != null && sysFiles.size() > 0) {

            logger.debug("sysFiles.get(0).getId() : " + sysFiles.get(0).getId());
            logger.debug("sysFiles.get(0).getPath() : " + sysFiles.get(0).getPath());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            ResultJson resultJson = super.success().put("fileId", sysFiles.get(0).getId());
            PrintWriter out = null;
            out = response.getWriter();
            out.write(JSON.toJSONString(resultJson));
            out.flush();
            out.close();
            return null;
        }

        response.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        // #16修复：使用 try-with-resources 确保流正确关闭
        try (InputStream inputStream = new FileInputStream(file)) {

            logger.debug("file.getPath():" + file.getPath());
            logger.debug("file.getName():" + fileName);

            response.setContentType(Files.probeContentType(Paths.get(file.getPath())));

            response.setHeader("Content-Disposition", "attachment;fileName="
                    + this.getFileName(request.getHeader("User-Agent"), fileName));
            OutputStream outputStream = response.getOutputStream();
            int n = 0;
            byte[] fileBuffer = new byte[1024];
            byte[] fileBuffer2 = new byte[1024];
            if (StringUtil.isNotEmpty(secret)) {
                while ((n = inputStream.read(fileBuffer)) != -1) {
                    for (int j = 0; j < n; j++) {
                        fileBuffer2[j] = (byte) (fileBuffer[j] ^ 0x99);
                    }
                    outputStream.write(fileBuffer2, 0, n);
                }
            } else {
                while ((n = inputStream.read(fileBuffer)) != -1) {
                    outputStream.write(fileBuffer, 0, n);
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("Error occurred while trying to download file: " + e.getMessage());
        } catch (IOException e) {
            logger.warn("Error occurred while trying to download file: " + e.getMessage());
        } catch (Exception e) {
            logger.warn("Error occurred while trying to download file: " + e.getMessage());
        }
        return null;
    }

    /**
     * 下载模板文件
     */
    @PostMapping("/fileDownloadTemplate")
    public String fileDownloadTemplate(HttpServletRequest request, HttpServletResponse response,
                                       @RequestBody JSONObject zformMap,
                                       @RequestParam(value = "fileId", required = false, defaultValue = "") String fileId,
                                       @RequestParam(value = "secret", required = false, defaultValue = "") String secret,
                                       @RequestParam(value = "formNo", required = false, defaultValue = "") String formNo,
                                       @RequestParam(value = "areaId", required = false, defaultValue = "") String areaId,
                                       @RequestParam(value = "importTemplateWithData", required = false) boolean importTemplateWithData,
                                       @RequestParam(value = "parentId", required = false, defaultValue = "") String parentId,
                                       @RequestParam(value = "fileName", required = false) String fileName
    ) throws IOException {
        String groupId = genTableService.getImportTemplateFileGroupIdByFormNo(formNo, areaId, fileRoot, importTemplateWithData, parentId, zformMap, fileName);
        List<SysFile> sysFiles = sysFileService.getFileListByGroupId(groupId);
        return fileDownload(request, response, null, sysFiles, secret);
    }

    /**
     * 下载文件
     */
    @GetMapping("/fileDownload")
    public String fileDownload(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(value = "fileId", required = false, defaultValue = "") String fileId,
                               @RequestParam(value = "secret", required = false, defaultValue = "") String secret,
                               @RequestParam(value = "formNo", required = false, defaultValue = "") String formNo,
                               @RequestParam(value = "areaId", required = false, defaultValue = "") String areaId,
                               @RequestParam(value = "importTemplateWithData", required = false) boolean importTemplateWithData,
                               @RequestParam(value = "parentId", required = false, defaultValue = "") String parentId,
                               @RequestParam(value = "oss", required = false, defaultValue = "") String oss,
                               @RequestParam(value = "fileName", required = false) String fileName,
                               @RequestParam(value = "isEdit", required = false) Boolean isEdit
    ) {
        SysFile theSysFile = sysFileService.get(fileId);
        if (theSysFile == null) {
            List<SysFile> sysFiles = sysFileService.getFileListByGroupId(fileId);
            if (sysFiles.size() > 0) {
                theSysFile = sysFiles.get(0);
            }
        }
        if (theSysFile == null) {
            theSysFile = sysFileService.findFirstByStoragePath(fileId);
        }
        theSysFile = sysFileService.findFirstObjectStorageFile(theSysFile);
        if(theSysFile != null && StrUtil.isNotEmpty(fileName)){
            theSysFile.setName(fileName);
        }

        if (theSysFile != null && StringUtil.isNotEmpty(theSysFile.getUrl())) {
            try {
                String bucket = parseBucket(oss);
                fileStorageAdapter.download(bucket, theSysFile.getUrl(), theSysFile.getName(), request, response);
            } catch (Exception e) {
                logger.error("下载OSS文件失败", e);
            }
        } else {
            if (theSysFile != null) {
                logger.warn("fileDownload fallback to local file, fileId={}, sysFileId={}, path={}, url={}",
                        fileId, theSysFile.getId(), theSysFile.getPath(), theSysFile.getUrl());
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                File file = null;
                if (StringUtil.isEmpty(formNo)) {
                    if (isEdit != null) {
                        file = sysFileService.getFile(fileId, fileRoot, isEdit);
                    } else {
                        file = sysFileService.getFile(fileId, fileRoot);
                    }
                    logger.debug("file id: " + fileId);
                    if (file == null) {
                        file = sysFileService.getFirstByGroupId(fileId, fileRoot);
                    }
                } else {

                    logger.debug("formNo : " + formNo);


                    String groupId = genTableService.getImportTemplateFileGroupIdByFormNo(formNo, areaId, fileRoot, importTemplateWithData, parentId, new JSONObject(), fileName);
                    List<SysFile> sysFiles = sysFileService.getFileListByGroupId(groupId);
                    return fileDownload(request, response, null, sysFiles, secret);
                }
                String fName = file.getName();
                if(StrUtil.isNotEmpty(fileName)){
                    fName = fileName;
                }
                return fileDownload(request, response, file, fName, null, secret);

            } catch (Exception e) {
                logger.warn("Error occurred while trying to download file: " + e.getMessage());
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e) {

                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {

                    }
                }
            }
        }
        return null;
    }

    /**
     * 按路径下载文件
     */
    @GetMapping("/fileDownloadByPath/{fileId}")
    public String fileDownloadByPath(HttpServletRequest request, HttpServletResponse response, @PathVariable(value = "fileId") String fileId, @RequestParam(value = "secret", required = false, defaultValue = "") String secret, @RequestParam(value = "formNo", required = false, defaultValue = "") String formNo) throws IOException {
        return fileDownload(request, response, FileUtil.getFilePrefix(fileId), secret, formNo, null, false, null, "", null, false);
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件")
    @RequiresPermissions("user")
    @GetMapping("/deleteFile")
    public ResultJson deleteFile(@RequestParam(value = "fileId", required = false) String fileId) {
        ResultJson resultJson = this.deleteFileOk(fileId, fileRoot);
        resultJson.setToken(token.get());
        return resultJson;
    }

    private ResultJson deleteFileOk(String fileId, String fileRoot) {
        ResultJson resultJson = new ResultJson();
        try {
            sysFileService.deleteFile(fileId, fileRoot);
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("删除文件成功");
            resultJson.setMsg_en("Delete file success");
        } catch (Exception e) {
            logger.error("Error while deleting file list:" + ExceptionUtils.getStackTrace(e));
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("删除文件失败");
            resultJson.setMsg_en("Delete file failed");
        }
        return resultJson;
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String userAgent, String fileName) {
        userAgent = (userAgent == null ? "" : userAgent.toLowerCase());

        String rtn = new String();
        try {
            String new_filename = URLEncoder.encode(fileName, "UTF8");
            new_filename = new_filename.replaceAll("\\+", " ");
            if (userAgent != null) {
                userAgent = userAgent.toLowerCase();
                //IE
                if (userAgent.indexOf("msie") != -1) {
                    rtn = new_filename;
                }
                //Opera filename*
                else if (userAgent.indexOf("opera") != -1) {
                    rtn = "filename*=UTF-8''" + new_filename;
                }
                //Safari
                else if (userAgent.indexOf("safari") != -1) {
                    rtn = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
                }
                //Chrome
                else if (userAgent.indexOf("applewebkit") != -1) {
                    new_filename = MimeUtility.encodeText(fileName, "UTF8", "B");
                    rtn = new_filename;
                }
                //FireFox
                else if (userAgent.indexOf("firefox") != -1) {
                    rtn = "\"" + new String(fileName.getBytes("UTF-8"), "ISO8859-1") + "\"";
                } else {
                    rtn = new_filename;
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Error occurred while trying to get file name: " + ExceptionUtils.getStackTrace(e));
        }
        return rtn;
    }

    private Boolean fileUploadBatchProgress(String requestURI, MultipartFile[] file, String groupId, String chunk) {
        for (MultipartFile mf : file) {
            InputStream input = null;
            FileOutputStream out = null;
            try {
                if (mf != null) {
                    if (this.checkExtension(mf)) {
                        Date theDate = new Date();
                        String uploadPath = this.getUploadPath(requestURI, groupId, theDate);
                        new File(uploadPath).mkdirs();
                        input = mf.getInputStream();
                        out = new FileOutputStream(uploadPath + chunk + "_chunk_" + mf.getOriginalFilename().substring(0, mf.getOriginalFilename().lastIndexOf('.')));
                        IOUtils.copy(input, out);
                    }else return  false;
                }
            } catch (Exception e) {
                logger.error("Error occurred while trying to upload file: " + ExceptionUtils.getStackTrace(e));
            } finally {
                try {
                    if (input != null) input.close();
                } catch (Exception e) {
                }
                try {
                    if (out != null) out.close();
                } catch (Exception e) {
                }
            }
        }
        return true;
    }

    private String getUploadPath(String requestURI, String randomUUID, Date theDate) {
        String realPath = fileRoot + fileUploadFolder;
        return new StringBuffer().append(realPath).append(requestURI)
                .append(new SimpleDateFormat(uploadPathDateFormat).format(theDate))
                .append("/").append(randomUUID).append("/").toString();
    }

    /**
     * #12修复：增强文件扩展名校验
     * 检查所有扩展名段，防止双扩展名绕过（如 malware.jsp.jpg）
     * 同时验证Content-Type是否与扩展名匹配
     */
    private boolean checkExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return false;
        }
        // 禁止文件名包含路径分隔符
        if (originalFilename.contains("/") || originalFilename.contains("\\")) {
            return false;
        }
        String[] fileSplit = originalFilename.split("\\.");
        if (fileSplit.length < 2) {
            return false; // 无扩展名
        }
        // 检查所有扩展名段（防止双扩展名绕过，如 shell.jsp.jpg）
        String[] dangerousExtensions = {"jsp", "jspx", "php", "asp", "aspx", "exe", "bat", "cmd", "sh", "class", "war"};
        Set<String> dangerousSet = new HashSet<>(Arrays.asList(dangerousExtensions));
        for (int i = 1; i < fileSplit.length; i++) {
            if (dangerousSet.contains(fileSplit[i].toLowerCase())) {
                logger.warn("文件上传被拒绝，包含危险扩展名: {}", originalFilename);
                return false;
            }
        }
        // 检查最终扩展名是否在白名单中
        String extension = fileSplit[fileSplit.length - 1].toLowerCase();
        return allowedExtensions.contains(extension);
    }

    /**
     * 获取文件的预签名URL（用于浏览器直接访问MinIO预览）
     */
    @Operation(summary = "获取文件预签名URL")
    @GetMapping("/getPresignedUrl")
    public ResultJson getPresignedUrl(
            @RequestParam(value = "fileId") String fileId,
            @RequestParam(value = "oss", required = false, defaultValue = "") String oss,
            @RequestParam(value = "expirySeconds", defaultValue = "3600") int expirySeconds) {
        ResultJson resultJson = new ResultJson();
        try {
            SysFile theSysFile = sysFileService.get(fileId);
            if (theSysFile == null || StringUtil.isEmpty(theSysFile.getUrl())) {
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("文件不存在或未存储在对象存储中");
                resultJson.setMsg_en("File not found or not stored in object storage");
                return resultJson;
            }

            String bucket = parseBucket(oss);
            String presignedUrl = fileStorageAdapter.getPresignedUrl(bucket, theSysFile.getUrl(), expirySeconds);
            if (presignedUrl == null) {
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("生成预签名URL失败");
                resultJson.setMsg_en("Failed to generate presigned URL");
                return resultJson;
            }

            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("获取预签名URL成功");
            resultJson.setMsg_en("Get presigned URL success");
            resultJson.put("url", presignedUrl);
        } catch (Exception e) {
            logger.error("获取预签名URL异常", e);
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("获取预签名URL异常: " + e.getMessage());
            resultJson.setMsg_en("Get presigned URL failed");
        }
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 批量获取文件的预签名URL
     */
    @Operation(summary = "批量获取文件预签名URL")
    @PostMapping("/batchGetPresignedUrl")
    public ResultJson batchGetPresignedUrl(@RequestBody Map<String, Object> params) {
        ResultJson resultJson = new ResultJson();
        try {
            @SuppressWarnings("unchecked")
            List<String> fileIds = (List<String>) params.get("fileIds");
            int expirySeconds = params.containsKey("expirySeconds") ?
                Integer.parseInt(params.get("expirySeconds").toString()) : 3600;
            String oss = (String) params.get("oss");

            Map<String, String> urlMap = new HashMap<>();
            for (String fileId : fileIds) {
                SysFile theSysFile = sysFileService.get(fileId);
                if (theSysFile != null && StringUtil.isNotEmpty(theSysFile.getUrl())) {
                    String bucket = parseBucket(oss);
                    String presignedUrl = fileStorageAdapter.getPresignedUrl(bucket, theSysFile.getUrl(), expirySeconds);
                    if (presignedUrl != null) {
                        urlMap.put(fileId, presignedUrl);
                    }
                }
            }

            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("批量获取预签名URL成功");
            resultJson.setMsg_en("Batch get presigned URLs success");
            resultJson.put("urls", urlMap);
        } catch (Exception e) {
            logger.error("批量获取预签名URL异常", e);
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("批量获取预签名URL异常: " + e.getMessage());
            resultJson.setMsg_en("Batch get presigned URLs failed");
        }
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 保存密级
     */
    @Operation(summary = "保存密级")
    @PostMapping("/saveSecretLevel")
    public ResultJson saveSecretLevel(@RequestBody SysFile sysFile) {
        ResultJson resultJson = this.saveSecretLevelOk(sysFile);
        resultJson.setToken(token.get());
        return resultJson;
    }

    private ResultJson saveSecretLevelOk(SysFile sysFile) {
        ResultJson resultJson = new ResultJson();
        try {
            int count = sysFileService.saveSecretLevel(sysFile);
            if (count == 1) {
                resultJson.setCode(ResultJson.CODE_SUCCESS);
                resultJson.setMsg("修改密级成功");
                resultJson.setMsg_en("Update secret level success");
            } else {
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("修改密级失败");
                resultJson.setMsg_en("Update secret level failed");
            }
        } catch (Exception e) {
            logger.error("Error while updating secret level:" + ExceptionUtils.getStackTrace(e));
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("修改密级失败");
            resultJson.setMsg_en("Update secret level failed");
        }
        return resultJson;
    }

    /*
     * newFile 跟新后的租金啊
     * path 文件之前的路径
     * */
    @PostMapping("/updateFileByPath")
    public ResultJson updateFileByPath(@RequestParam(value = "file") MultipartFile newFile,
                                       @RequestParam(value = "editObjects") String objectsJson,
                                       @RequestParam(value = "id") String fileId) {
        try {
            SysFile sysFile = sysFileService.get(fileId);
            String path = sysFile.getPath();
            File file = new File(fileRoot + path);
            //保存原图
            Path folderPath = Paths.get(file.getParent());
            if (sysFile.getPrimaryPath() == null) {
                String primaryPath = folderPath + "/primary-" + sysFile.getName();
                Path primaryFilePath = folderPath.resolve(primaryPath);
                String sb = primaryFilePath.toString().replace(fileRoot.replace("/", "\\"), "").replace("\\", "/");
                Files.copy(file.toPath(), primaryFilePath);
                sysFile.setPrimaryPath(sb);
            }
            //小于1MB的文件不压缩
            if (newFile.getSize() < 1024 * 1024) {
                newFile.transferTo(file);
            } else {
                //保存编辑后图片
                FileOutputStream fot = new FileOutputStream(file);
                Thumbnails.of(newFile.getInputStream()).scale(1f).outputQuality(0.1f).toFile(file);
                fot.close();
            }

            sysFile.setEditObjects(objectsJson);
            sysFileService.save(sysFile);
            return ResultJson.success("替换文件成功");
        } catch (IOException e) {
            logger.error("替换文件操作失败！" + e);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 解析 oss 参数中的 bucket
     * 格式: type:bucket:folder 或 bucket:folder 或 folder
     */
    private String parseBucket(String typeBucketFolder) {
        if (typeBucketFolder == null || typeBucketFolder.isEmpty()) return null;
        String[] params = typeBucketFolder.split(":");
        if (params.length == 3) return params[1];
        if (params.length == 2) return params[0];
        return null;
    }

    public void compressImage(InputStream inputFile, FileOutputStream outputFile, float compressionQuality) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();

        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionQuality(compressionQuality);
        writer.setOutput(outputFile);
        writer.write(null, new IIOImage(image, null, null), writeParam);

        outputFile.close();
        writer.dispose();
    }
}
