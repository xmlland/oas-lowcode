package com.jeestudio.bpm.common.pojo;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.tools.base.utils.JSONHelper;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: 用户信息
 */
@Data
public class UserInfo implements Serializable {
    private static final long serialVersionUID = -8682378180393566079L;

    /** 用户 ID。 */
    private String id;
    /** 登录账号，使用下划线命名以兼容旧接口字段。 */
    private String login_name;
    /** 工号或用户编号。 */
    private String no;
    /** 用户姓名。 */
    private String name;
    /** 邮箱地址。 */
    private String email;
    /** 办公电话。 */
    private String phone;
    /** 手机号码。 */
    private String mobile;


    /** 父级组织 ID，旧接口兼容字段。 */
    private String parent_id;
    /** 所属公司或单位 ID。 */
    private String company_id;
    /** 所属部门 ID。 */
    private String office_id;

    /** 备注。 */
    private String remarks;
    /** 创建人 ID。 */
    private String create_by;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /** 创建时间。 */
    private Date create_date;
    /** 更新人 ID。 */
    private String update_by;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /** 更新时间。 */
    private Date update_date;
    /** 删除标记。 */
    private String del_flag;


    /** 用户角色编码列表。 */
    private List<String> roleCodes;


    public UserInfo() {
    }

    public UserInfo(User user) {

        this.id = user.getId();
        this.login_name = user.getLoginName();
        this.no = user.getNo();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.mobile = user.getMobile();
        this.parent_id = user.getCompany() != null ? user.getCompany().getId() : null;
        this.company_id = user.getCompany() != null ? user.getCompany().getId() : null;
        this.office_id = user.getOffice() != null ? user.getOffice().getId() : null;
        this.remarks = user.getRemarks();
        this.create_by = user.getCreateBy() != null ? user.getCreateBy().getId() : null;
        this.create_date = user.getCreateDate();
        this.update_by = user.getUpdateBy() != null ? user.getUpdateBy().getId() : null;
        this.update_date = user.getUpdateDate();
        this.del_flag = user.getDelFlag();
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = JSONHelper.toJSONObject(this);
        jsonObject.put("formNo", "sys_user");
        jsonObject.remove("id");
        jsonObject.put("create_date", DateUtil.formatDateTime(this.create_date));
        jsonObject.put("update_date", DateUtil.formatDateTime(this.update_date));
        jsonObject.remove("roleCodes");
        return jsonObject;
    }
}
