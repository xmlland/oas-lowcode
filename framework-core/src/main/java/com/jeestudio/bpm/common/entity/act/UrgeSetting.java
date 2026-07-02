package com.jeestudio.bpm.common.entity.act;

import com.jeestudio.bpm.common.entity.common.DataEntity;
import com.jeestudio.bpm.common.entity.system.User;

/**
 * @Description: 流程催办设置实体
 */
public class UrgeSetting extends DataEntity<UrgeSetting> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码。 */
    private String ownerCode;
    /** 默认催办人。 */
    private User urgeUser;
    /** 催办时限配置。 */
    private String urgeLimit;
    /** 催办消息内容模板。 */
    private String urgeContent;
    /** 适用催办流程范围。 */
    private String urgeProcess;

    public UrgeSetting() {
        super();
    }

    public UrgeSetting(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public User getUrgeUser() {
        return urgeUser;
    }

    public void setUrgeUser(User urgeUser) {
        this.urgeUser = urgeUser;
    }

    public String getUrgeLimit() {
        return urgeLimit;
    }

    public void setUrgeLimit(String urgeLimit) {
        this.urgeLimit = urgeLimit;
    }

    public String getUrgeContent() {
        return urgeContent;
    }

    public void setUrgeContent(String urgeContent) {
        this.urgeContent = urgeContent;
    }

    public String getUrgeProcess() {
        return urgeProcess;
    }

    public void setUrgeProcess(String urgeProcess) {
        this.urgeProcess = urgeProcess;
    }
}
