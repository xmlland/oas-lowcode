package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.common.entity.common.TreeEntity;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description: 机构（单位或部门）
 */
public class Office extends TreeEntity<Office> {

    private static final long serialVersionUID = 1L;

    /** 机构类型：单位。 */
    public static final String TYPE_COMPANY = "1";
    /** 机构类型：部门。 */
    public static final String TYPE_OFFICE = "2";

    /** 所属行政区。 */
    private Area area;
    /** 行政区名称冗余字段，便于列表展示。 */
    private String areaName;
    /** 机构编码。 */
    private String code;
    /** 机构简称。 */
    private String shortName;
    /** 机构短编码。 */
    private String shortCode;
    /** 机构类型：单位、部门、组等。 */
    private String type;
    /** 机构级别。 */
    private String grade;
    /** 联系地址。 */
    private String address;
    /** 邮政编码。 */
    private String zipCode;
    /** 负责人姓名。 */
    private String master;
    /** 联系电话。 */
    private String phone;
    /** 传真号码。 */
    private String fax;
    /** 邮箱地址。 */
    private String email;
    /** 是否启用。 */
    private String useable;
    /** 主负责人。 */
    private User primaryPerson;
    /** 副负责人。 */
    private User deputyPerson;
    /** 子部门 ID 列表，查询过滤时使用。 */
    private List<String> childDeptList;
    /** 查询岗位关系时使用的岗位参数。 */
    private Post postParam;
    /** 机构下岗位列表。 */
    private List<Post> postList;

    public Office(){
        super();
        this.type = TYPE_OFFICE; //部门
    }

    public Office(String id){
        super(id);
    }

    public List<String> getChildDeptList() {
        return childDeptList;
    }

    public void setChildDeptList(List<String> childDeptList) {
        this.childDeptList = childDeptList;
    }

    public String getUseable() {
        return useable;
    }

    public void setUseable(String useable) {
        this.useable = useable;
    }

    public User getPrimaryPerson() {
        return primaryPerson;
    }

    public void setPrimaryPerson(User primaryPerson) {
        this.primaryPerson = primaryPerson;
    }

    public User getDeputyPerson() {
        return deputyPerson;
    }

    public void setDeputyPerson(User deputyPerson) {
        this.deputyPerson = deputyPerson;
    }

    public Office getParent() {
        return parent;
    }

    public void setParent(Office parent) {
        this.parent = parent;
    }

    @NotNull
    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return name;
    }

    public List<Post> getPostList() {
        return this.postList;
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;
    }

    public Post getPostParam() {
        return this.postParam;
    }

    public void setPostParam(Post postParam) {
        this.postParam = postParam;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
}
