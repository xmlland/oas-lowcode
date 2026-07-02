package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 组织
 */
public class Organization extends DataEntity<Organization> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码。 */
    private String ownerCode;
    /** 组织编号。 */
    private String orgNumber;
    /** 组织名称。 */
    private String orgName;
    /** 组织负责人。 */
    private User primaryPerson;
    /** 组织有效状态。 */
    private String orgEffect;
    /** 组织排序号。 */
    private Integer orgSequenceNumber;

    public Organization() {
        super();
    }

    public Organization(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getOrgNumber() {
        return orgNumber;
    }

    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public User getPrimaryPerson() {
        return primaryPerson;
    }

    public void setPrimaryPerson(User primaryPerson) {
        this.primaryPerson = primaryPerson;
    }

    public String getOrgEffect() {
        return orgEffect;
    }

    public void setOrgEffect(String orgEffect) {
        this.orgEffect = orgEffect;
    }

    public Integer getOrgSequenceNumber() {
        return orgSequenceNumber;
    }

    public void setOrgSequenceNumber(Integer orgSequenceNumber) {
        this.orgSequenceNumber = orgSequenceNumber;
    }
}
