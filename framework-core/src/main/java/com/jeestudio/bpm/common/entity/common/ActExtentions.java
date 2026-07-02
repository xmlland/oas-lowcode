package com.jeestudio.bpm.common.entity.common;

/**
 * @Description: 工作流扩展常量
 */
public class ActExtentions {

    /** 表单配置项：指向表单中的用户选择字段，用表单值指定下一节点固定办理人。 */
    public static final String actNextUser = "act.nextuser";
    /** 表单配置项：获取表单数据时改由 Java 代码重新查询。 */
    public static final String actQueryDataInJava = "act.queryDataInJava";
    /** 表单配置项：指向流程历史办理人字段，用历史办理人指定下一节点固定办理人。 */
    public static final String actPrevTaskAssignee = "act.prevTaskAssignee";
    /** 表单扩展配置根节点。 */
    public static final String formExtend = "formExtend";
    /** 用户列表扩展配置键。 */
    public static final String userList = "userList";
    /** 表单配置项：通过自定义实现类获取下一节点审批人。 */
    public static final String nextNodeUser = "act.nextNodeUser";
}
