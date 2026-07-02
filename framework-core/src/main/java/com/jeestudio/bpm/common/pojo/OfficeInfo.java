package com.jeestudio.bpm.common.pojo;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.tools.base.utils.JSONHelper;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 机构信息
 */
@Data
public class OfficeInfo implements Serializable {
    private static final long serialVersionUID = 7414659039698713402L;
    private String id;
    private String parent_id;
    private String parent_ids;
    private String name;

    private String name_en;


    private Integer sort;
    private String area_id;
    private String code;
    private String short_name;
    private String short_code;
    //Organization type (1: Company; 2: Department; 3: Group)
    private String types;
    //Organization level (1: Level I; 2: Level II; 3: Level III; 4: level IV)
    private String grade;


    private String address;
    private String zip_code;
    private String master;
    private String phone;
    private String fax;
    private String email;
    private String useable;
    private String primary_person;
    private String deputy_person;

    private String remarks;
    private String create_by;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date create_date;
    private String update_by;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date update_date;
    private String del_flag;

    public OfficeInfo() {
    }

    public OfficeInfo(Office office) {
        this.id = office.getId();
        this.parent_id = office.getParentId();
        this.parent_ids = office.getParentIds();
        this.name = office.getName();
        this.name_en = office.getNameEn();
        this.sort = office.getSort();
        this.area_id = office.getArea() != null ? office.getArea().getId() : null;
        this.code = office.getCode();
        this.short_name = office.getShortName();
        this.short_code = office.getShortCode();
        this.types = office.getType();
        this.grade = office.getGrade();
        this.address = office.getAddress();
        this.zip_code = office.getZipCode();
        this.master = office.getMaster();
        this.phone = office.getPhone();
        this.fax = office.getFax();
        this.email = office.getEmail();
        this.useable = office.getUseable();
        this.primary_person = office.getPrimaryPerson() != null ? office.getPrimaryPerson().getId() : null;
        this.deputy_person = office.getDeputyPerson() != null ? office.getDeputyPerson().getId() : null;
        this.remarks = office.getRemarks();
        this.create_by = office.getCreateBy() != null ? office.getCreateBy().getId() : null;
        this.create_date = office.getCreateDate();
        this.update_by = office.getUpdateBy() != null ? office.getUpdateBy().getId() : null;
        this.update_date = office.getUpdateDate();
        this.del_flag = office.getDelFlag();
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = JSONHelper.toJSONObject(this);
        jsonObject.put("formNo", "sys_office");
        jsonObject.remove("id");
        jsonObject.put("create_date", DateUtil.formatDateTime(this.create_date));
        jsonObject.put("update_date", DateUtil.formatDateTime(this.update_date));
        return jsonObject;
    }
}
