package com.jeestudio.bpm.common.entity.gen;

import com.jeestudio.bpm.utils.Global;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 表单列配置项
 */
@Data
public class GenTableColumnFormItemConfig {

    //Form页
    /*
    {"colProps":{"span":12,"ignoreProperty":["ignoreProperty","props","apply"],
    "props":{"span":12}},"formItemProps":{"name":"item_count","label":"项目个数",
    "validateFirst":true,"rules":[{"validateType":"number","message":"请输入整数"}],
    "unique":"0","validateType":"number","encryptType":"","required":"0","pleasePrefix":"请输入","extra":"",
    "ignoreProperty":["ignoreProperty","props","apply","rules","unique","validateType","encryptType","required","pleasePrefix"],
    "props":{"name":"item_count","label":"项目个数","validateFirst":true,"extra":""}},
    "formControlProps":{"placeholder":"","disabled":"0","defaultValue":"",
    "ignoreProperty":["ignoreProperty","props","apply","disabled"],"props":{"placeholder":"","defaultValue":"",
    "type":"number","max":"30","min":"20","maxlength":255,"allowClear":true,"textarea":false,"showCount":false,
    "autoSize":{"minRows":2,"maxRows":6}},"type":"number","max":"30","min":"20","maxlength":255,"allowClear":true,
    "textarea":false,"showCount":false,"autoSize":{"minRows":2,"maxRows":6}},"id":"bda27e82-1fbc-4949-802d-4034ef8dfb72",
    "type":"input","key":"numberInput"}
     */
    /** 表单项栅格宽度，对应 a-col span。 */
    private String span = "12";
    /** 表单项额外提示，对应 a-form-item extra。 */
    private String extra = "";
    /** 控件占位提示。 */
    private String placeholder = "";
    /** 整数或数字输入框最小值。 */
    private String min = "";
    /** 整数或数字输入框最大值。 */
    private String max = "";
    /** 非系统字典下拉、单选、多选的过滤条件。 */
    private String tableFilterData = "";
    /** 非系统字典下拉、单选、多选的格式化函数脚本。 */
    private String formatFuncStr = "";
    /** 日期选择最小值。 */
    private String minValue = "";
    /** 日期选择最大值。 */
    private String maxValue = "";
    /** 用户选择或用户多选的数据范围。 */
    private String dataScope = "";
    /** 用户选择或用户多选的指定部门 ID。 */
    private String targetOrgId = "";

    /** 序列号前缀。 */
    private String prefix = "";

    /** 是否多选。 */
    private String multiple = "";

    /** 用户选择控件是否自动创建系统用户。 */
    private String createSysUser = "";
    /** 自动创建用户时的登录名来源字段。 */
    private String loginNameField = "";
    /** 自动创建用户时的姓名来源字段。 */
    private String userNameField = "";
    /** 自动创建用户时的上级机构来源字段。 */
    private String parentOrgField = "";
    /** 自动创建用户时绑定的角色。 */
    private String userRoles = "";

    /** 机构选择控件是否自动创建机构。 */
    private String createSysOffice = "";
    /** 自动创建机构时的上级机构 ID。 */
    private String parentOrgId = "";
    /** 自动创建机构时的机构名称来源字段。 */
    private String orgNameField = "";
    /** 自动创建机构时的行政区来源字段。 */
    private String areaIdField = "";

    /** 用户选择、弹出单选、弹出多选的弹窗宽度。 */
    private String modalWidth = "";
    /** 用户选择控件是否隐藏登录名。 */
    private String hideLoginName = "";
    /** 行政区选择是否允许自由选择。 */
    private String freeChoice = "";
    /** 行政区选择展示级别。 */
    private String showRank = "";
    /** 行政区选择根节点 ID。 */
    private String rootAreaId = "";
    /** 文件上传允许的文件格式。 */
    private String accepts = "";
    /** 文件上传数量限制。 */
    private String fileCount = "";
    /** 文件上传大小限制，单位 MB。 */
    private String maxSize = "";
    /** 弹出单选或弹出多选目标表单编号。 */
    private String targetFormNo = "";
    /** 弹出单选或弹出多选的存储字段。 */
    private String targetField = "";
    /** 弹出单选或弹出多选显示列配置。 */
    private String columns = "";
    /** 弹出单选或弹出多选显示列配置列表。 */
    private List<String> columnsList = new ArrayList<>();
    /** 弹出单选或弹出多选查询字段集合。 */
    private String searchKey = "";
    /** 弹出单选或弹出多选查询标签集合。 */
    private String searchLabel = "";
    /** 弹出单选或弹出多选查询区标签宽度。 */
    private String searchLabelWidth = "";
    /** 弹出单选或弹出多选查询配置。 */
    private String searchConfig = "";
    /** 弹出单选或弹出多选弹窗标题。 */
    private String modalTitle = "";
    /** 弹出单选选择后带入当前表单的数据映射。 */
    private String formUpdateMap = "";
    /** 弹出单选回显字段。 */
    private String nameDataIndex = "";
    /** 弹出单选显示数据过滤条件。 */
    private String filterData = "";
    /** 用于识别 ${field} 动态表达式的正则。 */
    String regex = "\\$\\{(.*?)\\}";

    /** 预编译动态表达式正则。 */
    Pattern pattern = Pattern.compile(regex);
    public String getMaxValue() {
        //正则匹配提取maxValue中${}包裹的内容
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(maxValue);
        // 查找匹配的内容
        while (matcher.find()) {
            // 输出匹配到的内容
            return ":maxValue=\"formModel."+matcher.group(1)+"\"";
        }
        return "maxValue=\""+maxValue+"\"";
    }

    public String getMinValue() {
        //正则匹配提取maxValue中${}包裹的内容
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(minValue);
        // 查找匹配的内容
        while (matcher.find()) {
            // 输出匹配到的内容
            return ":minValue=\"formModel."+matcher.group(1)+"\"";
        }
        return "minValue=\""+minValue+"\"";
    }

    public boolean isMultiple() {
        return Global.YES.equals(multiple) || "true".equalsIgnoreCase(multiple);
    }
}
