package com.jeestudio.bpm.common.entity.cms;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.jeestudio.bpm.common.entity.common.ActEntity;
import com.jeestudio.bpm.common.view.system.UserView;
import com.jeestudio.bpm.utils.Encodes;
import com.jeestudio.bpm.utils.StringUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 内容信息
 */
public class PrtInformation extends ActEntity<PrtInformation> {

    private static final long serialVersionUID = 1L;
    /** 归属机构编码 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerCode;
    /** 站点名称 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrtSite site;
    /** 栏目名称 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrtChannel channel;
    /** 栏目名称标题 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String channelName;
    /** 文章标题 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;
    /** 作者 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String author;
    /** 来源 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String origin;
    /** URL */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String originUrl;
    /** 关键字 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String keyword;
    /** 文章摘要 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    /** 文章类型 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String types;
    /** 轮播 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ifPlay;
    /** 图文 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String typesimg;
    /** 视频 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String video;
    /** 音频 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String audio;
    /** 图集 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pics;
    /** 附件 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String files;
    /** 文章内容 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String content;
    /** 是否有标题图 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hasTitleImg;
    /** 标题图 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String titleimg;
    /** 文档 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String doc;
    /** 文档类型 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String docType;
    /** 固顶级别 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String topLevel;
    /** 固顶到期日期 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date topDate;
    /** 拟稿时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date draftDate;
    /** 拟稿人 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserView draftUser;
    /** 拟稿人姓名 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String draftName;
    /** 发布时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date releaseDate;
    /** 发布人 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserView releaseUser;
    /** 发布人姓名 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String releaseName;
    /** 归档时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date pigeDate;
    /** 归档人 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserView pigeUser;
    /** 归档人姓名 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pigeName;
    /** 出档时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date unpigeDate;
    /** 出档人 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserView unpigeUser;
    /** 出档人姓名 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String unpigeName;
    /** 允许评论 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ifComment;
    /** 发布范围(互联网) */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String scope;
    /** 发布范围(内网) */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String range;
    /** 仅本单位 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String theOrg;
    /** 全员可见 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ifFullVisible;
    /** 密级 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String secLevel;
    /** 保密期限 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String secPeriod;
    /** 发文字号 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fileNo;
    /** 发文机关 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fileOrg;
    /** 主送单位 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sendOrg;
    /** 抄送单位 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String copyOrg;
    /** 文章状态 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;
    /** 操作信息 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String audits;
    /** 开始 固顶到期日期 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginTopDate;
    /** 结束 固顶到期日期 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endTopDate;
    /** 开始 拟稿时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginDraftDate;
    /** 结束 拟稿时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endDraftDate;
    /** 开始 发布时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginReleaseDate;
    /** 结束 发布时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endReleaseDate;
    /** 开始 归档时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginPigeDate;
    /** 结束 归档时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endPigeDate;
    /** 开始 出档时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginUnpigeDate;
    /** 结束 出档时间 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endUnpigeDate;
    /** 发布范围(内网)-用户 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userIds;
    /** 发布范围(内网)-用户 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userNames;
    /** 发布范围(内网)-机构 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String officeIds;
    /** 发布范围(内网)-机构 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String officeNames;
    /** 发布范围(内网)-组织 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> orgIds;
    /** 发布范围(内网)-组织 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String orgNames;
    /** 文章显示状态 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String showTypes;
    /** 图片url */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String typesimgUrl;
    /** 视频url */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String videoUrl;
    /** 音频url */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String audioUrl;
    /** 图集url */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String picsUrl;
    /** 前台查询 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean isSelf;
    /** 仅用于接收数据 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String contentHtml;
    /** 仅用于接收数据， 去除title中的br */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String titleDelBr;

    public PrtInformation() {
        super();
    }

    public PrtInformation(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public PrtSite getSite() {
        return site;
    }

    public void setSite(PrtSite site) {
        this.site = site;
    }

    public PrtChannel getChannel() {
        return channel;
    }

    public void setChannel(PrtChannel channel) {
        this.channel = channel;
    }

    public String getTitle() {
        return Encodes.unescapeHtml(title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getIfPlay() {
        return ifPlay;
    }

    public void setIfPlay(String ifPlay) {
        this.ifPlay = ifPlay;
    }

    public String getTypesimg() {
        return typesimg;
    }

    public void setTypesimg(String typesimg) {
        this.typesimg = typesimg;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getPics() {
        return pics;
    }

    public void setPics(String pics) {
        this.pics = pics;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHasTitleImg() {
        return hasTitleImg;
    }

    public void setHasTitleImg(String hasTitleImg) {
        this.hasTitleImg = hasTitleImg;
    }

    public String getTitleimg() {
        return titleimg;
    }

    public void setTitleimg(String titleimg) {
        this.titleimg = titleimg;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getTopLevel() {
        return topLevel;
    }

    public void setTopLevel(String topLevel) {
        this.topLevel = topLevel;
    }

    @JsonFormat(pattern = "yyyy-MM-dd",timezone="GMT+8")
    public Date getTopDate() {
        return topDate;
    }

    public void setTopDate(Date topDate) {
        this.topDate = topDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getDraftDate() {
        return draftDate;
    }

    public void setDraftDate(Date draftDate) {
        this.draftDate = draftDate;
    }

    public UserView getDraftUser() {
        return draftUser;
    }

    public void setDraftUser(UserView draftUser) {
        this.draftUser = draftUser;
    }

    public String getDraftName() {
        return draftName;
    }

    public void setDraftName(String draftName) {
        this.draftName = draftName;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public UserView getReleaseUser() {
        return releaseUser;
    }

    public void setReleaseUser(UserView releaseUser) {
        this.releaseUser = releaseUser;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getPigeDate() {
        return pigeDate;
    }

    public void setPigeDate(Date pigeDate) {
        this.pigeDate = pigeDate;
    }

    public UserView getPigeUser() {
        return pigeUser;
    }

    public void setPigeUser(UserView pigeUser) {
        this.pigeUser = pigeUser;
    }

    public String getPigeName() {
        return pigeName;
    }

    public void setPigeName(String pigeName) {
        this.pigeName = pigeName;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getUnpigeDate() {
        return unpigeDate;
    }

    public void setUnpigeDate(Date unpigeDate) {
        this.unpigeDate = unpigeDate;
    }

    public UserView getUnpigeUser() {
        return unpigeUser;
    }

    public void setUnpigeUser(UserView unpigeUser) {
        this.unpigeUser = unpigeUser;
    }

    public String getUnpigeName() {
        return unpigeName;
    }

    public void setUnpigeName(String unpigeName) {
        this.unpigeName = unpigeName;
    }

    public String getIfComment() {
        return ifComment;
    }

    public void setIfComment(String ifComment) {
        this.ifComment = ifComment;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getTheOrg() {
        return theOrg;
    }

    public void setTheOrg(String theOrg) {
        this.theOrg = theOrg;
    }

    public String getIfFullVisible() {
        return ifFullVisible;
    }

    public void setIfFullVisible(String ifFullVisible) {
        this.ifFullVisible = ifFullVisible;
    }

    public String getSecLevel() {
        return secLevel;
    }

    public void setSecLevel(String secLevel) {
        this.secLevel = secLevel;
    }

    public String getSecPeriod() {
        return secPeriod;
    }

    public void setSecPeriod(String secPeriod) {
        this.secPeriod = secPeriod;
    }

    public String getFileNo() {
        return fileNo;
    }

    public void setFileNo(String fileNo) {
        this.fileNo = fileNo;
    }

    public String getFileOrg() {
        return fileOrg;
    }

    public void setFileOrg(String fileOrg) {
        this.fileOrg = fileOrg;
    }

    public String getSendOrg() {
        return sendOrg;
    }

    public void setSendOrg(String sendOrg) {
        this.sendOrg = sendOrg;
    }

    public String getCopyOrg() {
        return copyOrg;
    }

    public void setCopyOrg(String copyOrg) {
        this.copyOrg = copyOrg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String preStatus;
    public String getPreStatus() {
        if (StringUtil.isEmpty(preStatus)) {
            return status;
        } else {
            return preStatus;
        }
    }

    public void setPreStatus(String preStatus) {
        this.preStatus = preStatus;
    }

    public String getAudits() {
        return audits;
    }

    public void setAudits(String audits) {
        this.audits = audits;
    }


    private String preAudits;
    public String getPreAudits() {
        if (StringUtil.isEmpty(preAudits)) {
            return audits;
        } else {
            return preAudits;
        }
    }

    public void setPreAudits(String preAudits) {
        this.preAudits = preAudits;
    }
    public Date getBeginTopDate() {
        return beginTopDate;
    }

    public void setBeginTopDate(Date beginTopDate) {
        this.beginTopDate = beginTopDate;
    }

    public Date getEndTopDate() {
        return endTopDate;
    }

    public void setEndTopDate(Date endTopDate) {
        this.endTopDate = endTopDate;
    }

    public Date getBeginDraftDate() {
        return beginDraftDate;
    }

    public void setBeginDraftDate(Date beginDraftDate) {
        this.beginDraftDate = beginDraftDate;
    }

    public Date getEndDraftDate() {
        return endDraftDate;
    }

    public void setEndDraftDate(Date endDraftDate) {
        this.endDraftDate = endDraftDate;
    }

    public Date getBeginReleaseDate() {
        return beginReleaseDate;
    }

    public void setBeginReleaseDate(Date beginReleaseDate) {
        this.beginReleaseDate = beginReleaseDate;
    }

    public Date getEndReleaseDate() {
        return endReleaseDate;
    }

    public void setEndReleaseDate(Date endReleaseDate) {
        this.endReleaseDate = endReleaseDate;
    }

    public Date getBeginPigeDate() {
        return beginPigeDate;
    }

    public void setBeginPigeDate(Date beginPigeDate) {
        this.beginPigeDate = beginPigeDate;
    }

    public Date getEndPigeDate() {
        return endPigeDate;
    }

    public void setEndPigeDate(Date endPigeDate) {
        this.endPigeDate = endPigeDate;
    }

    public Date getBeginUnpigeDate() {
        return beginUnpigeDate;
    }

    public void setBeginUnpigeDate(Date beginUnpigeDate) {
        this.beginUnpigeDate = beginUnpigeDate;
    }

    public Date getEndUnpigeDate() {
        return endUnpigeDate;
    }

    public void setEndUnpigeDate(Date endUnpigeDate) {
        this.endUnpigeDate = endUnpigeDate;
    }

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public String getUserNames() {
        return userNames;
    }

    public void setUserNames(String userNames) {
        this.userNames = userNames;
    }

    public String getOfficeIds() {
        return officeIds;
    }

    public void setOfficeIds(String officeIds) {
        this.officeIds = officeIds;
    }

    public String getOfficeNames() {
        return officeNames;
    }

    public void setOfficeNames(String officeNames) {
        this.officeNames = officeNames;
    }

    public List<String> getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(List<String> orgIds) {
        this.orgIds = orgIds;
    }

    public String getOrgNames() {
        return orgNames;
    }

    public void setOrgNames(String orgNames) {
        this.orgNames = orgNames;
    }

    public String getShowTypes() {
        if(PrtConstants.INFO_STATUS_DF.equals(this.getStatus())){//待发
            if(this.getReleaseDate() != null){
                if(this.getReleaseDate().compareTo(new Date()) > 0){
                    return PrtConstants.INFO_SHOW_NO;
                }
                return PrtConstants.INFO_SHOW_YES;
            } else {
                return PrtConstants.INFO_SHOW_NO;
            }
        } else if(PrtConstants.INFO_STATUS_YF.equals(this.getStatus())){//已发
            return PrtConstants.INFO_SHOW_YES;
        }
        return "";
    }

    public void setShowTypes(String showTypes) {
        this.showTypes = showTypes;
    }

    @SuppressWarnings("unchecked")
    public String getTypesimgUrl() {
        return typesimgUrl;
    }

    public String getVideoUrl() {
        String files = getVideo();
        if(StringUtil.isNotBlank(files)){
            List<Map<String, String>> list = new Gson().fromJson(files.replaceAll("&quot;", "\""), List.class);
            return list.get(0).get("fileUrl");
        }
        return videoUrl;
    }

    public String getAudioUrl() {
        String files = getAudio();
        if(StringUtil.isNotBlank(files)){
            List<Map<String, String>> list = new Gson().fromJson(files.replaceAll("&quot;", "\""), List.class);
            return list.get(0).get("fileUrl");
        }
        return audioUrl;
    }

    public String getPicsUrl() {
        return Encodes.unescapeHtml(this.pics);
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }

    public String getContentHtml() {
        return Encodes.unescapeHtml(content);
    }

    public String getTitleDelBr() {
        return this.title.replace("<br>", "").replace("<br/>", "");
    }

    public void setTitleDelBr(String titleDelBr) {
        this.titleDelBr = titleDelBr;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
