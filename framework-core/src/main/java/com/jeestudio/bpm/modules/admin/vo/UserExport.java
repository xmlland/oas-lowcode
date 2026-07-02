package com.jeestudio.bpm.modules.admin.vo;

import com.jeestudio.tools.base.annotation.Excel;
import lombok.Data;

/**
 * @Description: 用户导出视图
 */
@Data
public class UserExport {

    @Excel(title = "所属机构")
    private String orgName;

    @Excel(title = "登录名")
    private String loginName;

    @Excel(title = "姓名")
    private String name;

    @Excel(title = "密码")
    private String password;
}
