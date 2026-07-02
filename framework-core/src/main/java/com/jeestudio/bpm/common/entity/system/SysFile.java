package com.jeestudio.bpm.common.entity.system;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeestudio.bpm.common.entity.common.DataEntity;

import java.util.Date;

/**
 * @Description: 系统附件
 */
public class SysFile extends DataEntity<SysFile> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码。 */
    private String ownerCode;
    /** 附件分组 ID，用于一组业务附件关联。 */
    private String groupId;
    /** 文件名称。 */
    private String name;
    /** 文件扩展名。 */
    private String ext;
    /** 文件类型或业务分类。 */
    private String type;
    /** 文件大小。 */
    private String size;
    /** 文件存储路径。 */
    private String path;
    /** 转换后的 PDF 文件路径。 */
    private String pdfPath;
    /** 缩略图存储路径。 */
    private String thumbPath;
    /** 上传时间。 */
    private Date uploadTime;
    /** 上传用户。 */
    private User uploadUser;
    /** 排序号。 */
    private Integer sort;
    /** 文件描述。 */
    private String desc;
    /** 音视频文件时长，单位按业务约定解释。 */
    private Integer duration;
    /** 保密标记。 */
    private String secFlag;
    /** 访问次数。 */
    private Integer visitCount;
    /** 文本内容或抽取内容。 */
    private String content;
    /** 是否需要转换为 PDF。 */
    private String toPdf;
    /** 查询条件：上传开始时间。 */
    private Date beginUploadTime;
    /** 查询条件：上传结束时间。 */
    private Date endUploadTime;
    /** 上传或转换失败类型。 */
    private String failType;
    /** 文件密级。 */
    private String secretLevel;
    /** 文件访问 URL。 */
    private String url;
    /** 缩略图访问 URL。 */
    private String thumbUrl;
    /** 原始文件路径。 */
    private String primaryPath;
    /** 允许在线编辑的对象范围。 */
    private String editObjects;

    public SysFile() {
        super();
    }

    public SysFile(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    public User getUploadUser() {
        return uploadUser;
    }

    public void setUploadUser(User uploadUser) {
        this.uploadUser = uploadUser;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getSecFlag() {
        return secFlag;
    }

    public void setSecFlag(String secFlag) {
        this.secFlag = secFlag;
    }

    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToPdf() {
        return toPdf;
    }

    public void setToPdf(String toPdf) {
        this.toPdf = toPdf;
    }

    public Date getBeginUploadTime() {
        return beginUploadTime;
    }

    public void setBeginUploadTime(Date beginUploadTime) {
        this.beginUploadTime = beginUploadTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndUploadTime() {
        return endUploadTime;
    }

    public void setEndUploadTime(Date endUploadTime) {
        this.endUploadTime = endUploadTime;
    }

    public String getFailType() {
        return failType;
    }

    public void setFailType(String failType) {
        this.failType = failType;
    }

    public String getSecretLevel() {
        return secretLevel;
    }

    public void setSecretLevel(String secretLevel) {
        this.secretLevel = secretLevel;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public void setPrimaryPath(String primaryPath) {this.primaryPath = primaryPath;
    }

    public void setEditObjects(String editObjects) {this.editObjects = editObjects;}
    public String getPrimaryPath() {return primaryPath;}

    public String getEditObjects() {return editObjects;}
}
