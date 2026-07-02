package com.jeestudio.bpm.common.entity.gen;

import lombok.Data;

/**
 * @Description: 表单属性配置
 */
@Data
public class GenTableFormProps {

    /** 列表页编辑弹窗是否全屏。 */
    private String modal__Full = "";
    /** 列表页编辑弹窗宽度。 */
    private String modal__Width = "";
    /** 列表页编辑弹窗标题。 */
    private String list__modalTitle = "";
    /** 列表页编辑弹窗标题函数脚本。 */
    private String list__modalTitleFuncStr = "";
    /** 表单标签宽度。 */
    private String labelWidth = "";
    /** 主表单标题。 */
    private String mainTableTitle = "";
    /** 子表显示类型。 */
    private String subTableType = "";
    /** 锚点选项卡宽度。 */
    private String anchorWidth = "";
    /** 锚点位置。 */
    private String anchorLocation = "";
    /** 保存前扩展校验或数据处理函数脚本。 */
    private String getExtendSaveDataFuncStr = "";

}
