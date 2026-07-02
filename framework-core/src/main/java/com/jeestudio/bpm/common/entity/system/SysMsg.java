package com.jeestudio.bpm.common.entity.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeestudio.bpm.common.entity.common.DataEntity;

import java.util.Date;

/**
 * @Description: 系统消息
 */
public class SysMsg extends DataEntity<SysMsg> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码。 */
    private String ownerCode;
    /** 消息类型。 */
    private String types;
    /** 消息标题。 */
    private String title;
    /** 消息内容。 */
    private String content;
    /** 关联业务记录 ID 或摘要。 */
    private String record;
    /** 关联菜单中文名称。 */
    private String menuName;
    /** 关联菜单英文名称。 */
    private String menuName_EN;
    /** 关联菜单地址。 */
    private String menuHref;
    /** 发送人。 */
    private User sender;
    /** 发送时间。 */
    private Date sendTime;
    /** 接收人。 */
    private User recipient;
    /** 阅读时间。 */
    private Date readTime;
    /** 消息状态，例如未读、已读。 */
    private String status;
    /** 查询条件：发送开始时间。 */
    private Date beginSendTime;
    /** 查询条件：发送结束时间。 */
    private Date endSendTime;
    /** 查询条件：阅读开始时间。 */
    private Date beginReadTime;
    /** 查询条件：阅读结束时间。 */
    private Date endReadTime;

    public SysMsg() {
        super();
    }

    public SysMsg(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuName_EN() {
        return menuName_EN;
    }

    public void setMenuName_EN(String menuName_EN) {
        this.menuName_EN = menuName_EN;
    }

    public String getMenuHref() {
        return menuHref;
    }

    public void setMenuHref(String menuHref) {
        this.menuHref = menuHref;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getBeginSendTime() {
        return beginSendTime;
    }

    public void setBeginSendTime(Date beginSendTime) {
        this.beginSendTime = beginSendTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getEndSendTime() {
        return endSendTime;
    }

    public void setEndSendTime(Date endSendTime) {
        this.endSendTime = endSendTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getBeginReadTime() {
        return beginReadTime;
    }

    public void setBeginReadTime(Date beginReadTime) {
        this.beginReadTime = beginReadTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getEndReadTime() {
        return endReadTime;
    }

    public void setEndReadTime(Date endReadTime) {
        this.endReadTime = endReadTime;
    }
}
