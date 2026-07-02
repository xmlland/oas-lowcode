package com.jeestudio.bpm.common.entity.cms;

/**
 * @Description: 内容常量
 */
public class PrtConstants {

    /**
     * 通知公告阅读状态 : 已读 1 ; 未读 0
     */
    public static final String NOTIFY_READ_FLAG = "1";
    /**
     * 通知公告阅读状态 : 已读 1 ; 未读 0
     */
    public static final String NOTIFY_UNREAD_FLAG = "0";

    /**
     * 通知公告：发布状态 草稿
     */
    public static final String NOTIFY_STATUS_ONE = "10";

    /**
     * 通知公告：发布状态 发布
     */
    public static final String NOTIFY_STATUS_TWO = "20";

    /**
     * 是否状态： 是 1 ； 否 0
     */
    public static final String YES = "1";
    /**
     * 是否状态： 是 1 ； 否 0
     */
    public static final String NO = "0";

    /**
     * 密级：无 5；秘密★ 6；机密★ 7
     */
    public static final String SEC_LEVEL_NONE = "5";
    /**
     * 密级：无 5；秘密★ 6；机密★ 7
     */
    public static final String SEC_LEVEL_MM = "6";
    /**
     * 密级：无 5；秘密★ 6；机密★ 7
     */
    public static final String SEC_LEVEL_JM = "7";

    /**
     * 启用停用状态：启用 : 1
     */
    public static final String ENABLED = "1";

    /**
     * 启用停用状态：停用 : 0
     */
    public static final String DISABLED = "0";

    /**
     * 启用停用状态：废止 : 2
     */
    public static final String DISUSE = "2";

    /**
     * 数字：0
     */
    public static final String NUMBER_0 = "0";

    /**
     * 标点符号：逗号
     */
    public static final String PUNC_COMMA = ",";

    /**
     * 标点符号：#
     */
    public static final String PUNC_J = "#";

    /**
     * 标点符号：#
     */
    public static final String STR_K = "";

    /**
     * 标点符号：横线
     */
    public static final String PUNC_LINE = "-";

    /**
     * 标点符号：下划线
     */
    public static final String PUNC_UNDERLINE = "_";

    /**
     * 标点符号：大于
     */
    public static final String PUNC_GT = ">";

    /**
     * 文章发布范围类型： 用户 user；机构 office；组织 org
     */
    public static final String RANGE_TYPE_USER = "user";
    /**
     * 文章发布范围类型： 用户 user；机构 office；组织 org
     */
    public static final String RANGE_TYPE_OFFICE = "office";
    /**
     * 文章发布范围类型： 用户 user；机构 office；组织 org
     */
    public static final String RANGE_TYPE_ORG = "org";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_CG = "10";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_DYS = "11";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_YSTG = "12";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_YSTH = "13";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_DS = "20";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_DF = "30";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_TH = "40";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_YF = "50";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_CH = "60";

    /**
     * 文章状态：10草稿；11 待预审；12预审通过；13预审退回；20待审；30待发；40退回；50已发；60撤回；70归档
     */
    public static final String INFO_STATUS_GD = "70";

    /**
     * 文章类型：10 普通；20 图文
     */
    public static final String INFO_TYPE_ONE = "10";

    /**
     * 文章类型：10 普通；20 图文
     */
    public static final String INFO_TYPE_TWO = "20";

    /**
     * 文件上传路径：文章发布 /portal/prtInformation；通知公告 /portal/prtNotify
     */
    public static final String UPLOAD_PATH_INFO = "/portal/prtInformation";
    /**
     * 文件上传路径：文章发布 /portal/prtInformation；通知公告 /portal/prtNotify
     */
    public static final String UPLOAD_PATH_NOTIFY = "/portal/prtNotify";

    /**
     * 推送状态：10 草稿 50 发布
     */
    public static final String INFO_SEND_STATUS_CG = "10";
    /**
     * 推送状态：10 草稿 50 发布
     */
    public static final String INFO_SEND_STATUS_FB = "50";

    /**
     * cms是否使用缓存
     */
    public static final String USE_CMS_CACHE = "useCmsCache";

    /**
     * 子系统标识：cms
     */
    public static final String SUB_SYSTEM_CMS = "wz_cms";

    /**
     * cms前台访问标识
     */
    public static final String PAGE_FLAG_CMS = "cms";

    /**
     * 国家局前台访问标识
     */
    public static final String PAGE_FLAG_GJJ = "wzcb";

    /**
     * 固顶级别：999 不固顶
     */
    public static final String NOT_TOP = "999";

    /**
     * 允许评论：10 继承自栏目；20 是；30 否
     */
    public static final String COMMENT_TYPE_CHANNEL = "10";
    /**
     * 允许评论：10 继承自栏目；20 是；30 否
     */
    public static final String COMMENT_TYPE_YES = "20";
    /**
     * 允许评论：10 继承自栏目；20 是；30 否
     */
    public static final String COMMENT_TYPE_NO = "30";

    /**
     * 发布范围(内网)：10 全体
     */
    public static final String SCOPE_TYPE_ALL = "10";

    /**
     * 发布类型：10 即时发布；20 定时发布
     */
    public static final String RELEASE_TYPE_TEN = "10";
    /**
     * 发布类型：10 即时发布；20 定时发布
     */
    public static final String RELEASE_TYPE_TWENTY = "20";

    /**
     * 归档类型：10 即时归档；20 定时归档
     */
    public static final String PIGE_TYPE_TEN = "10";
    /**
     * 归档类型：10 即时归档；20 定时归档
     */
    public static final String PIGE_TYPE_TWENTY = "20";

    /**
     * 文章显示状态：10 显示；20 不显示
     */
    public static final String INFO_SHOW_YES = "10";

    /**
     * 文章显示状态：10 显示；20 不显示
     */
    public static final String INFO_SHOW_NO = "20";

    /**
     * 栏目默认名称：default
     */
    public static final String  DEFAULT = "default";


    public static final int ZERO = 0;

    public static final int ONE = 1;

    public static final int TWO = 2;

    public static final int THREE = 3;

    public static final String TRUE = "true";

    public static final String FALSE = "false";

    /**
     * 互联网模式
     */
    public static final String INTERNET = "internet";

    /**
     * 内容管理员
     */
    public static final String PRT_INFO_MANAGER = "prt_info_manager";

    /**
     * 栏目树根节点
     */
    //public static final String CHANNEL_ROOT_NAME = "栏目管理";
    public static final String CHANNEL_ROOT_NAME = "新闻中心";

    /**
     * 栏目树根节点
     */
    public static final String SITE_ROOT_NAME = "站点栏目";

    /**
     * 栏目树根节点上级
     */
    public static final String CHANNEL_ROOT_PARENT = "#";

    /**
     * 栏目评论默认：否
     */
    public static final String ZEROSTR = "0";

    /**
     * 相似文章比例
     */
    public static final String INFO_SIMILAR_RATE = "info_similar_rate";

    /**
     * ztree禁用图标名称
     */
    public static final String ZTREE_DISABLE_ICON = "ico_disable";

    /**
     * 文章状态字典类型
     */
    public static final String PRT_INFO_STATUS = "prt_info_status";

    /**
     * 系统发通知给内容预审员的url
     */
    public static final String PRT_INFO_PREAUDIT_URL = "/portal/prtInformation/preauditindex";

    /**
     * 系统发通知给内容审核员的url
     */
    public static final String PRT_INFO_AUDIT_URL = "/portal/prtInformation/auditindex";

    /**
     * 系统发通知给内容管理员的url
     */
    public static final String PRT_INFO_MANAGER_URL = "/portal/prtInformation";

    /**
     * 任务处理状态，未处理
     */
    public static final String STATUS_NO = "0";
    /**
     * 任务处理状态，已处理
     */
    public static final String STATUS_YES = "1";
    /**
     * 任务处理状态，执行中
     */
    public static final String STATUS_EXE = "2";
    /**
     * CMS任务通知类型
     */
    public static final String TYPE_CMS = "cms";
    /**
     * cms缓存域名称
     */
    public static final String CMS_CACHE = "cmsCache";

    /**
     * cms缓存key用户前缀
     */
    public static final String CMS_CACHE_USER_PREFIX = "user_";

    /**
     * cms缓存key栏目前缀
     */
    public static final String CMS_CACHE_CHANNEL_PREFIX = "channel_";

    /**
     * 任务通知：子系统标识
     */
    public static final String SUBSYSTEM_CODE = "subsystem_code";
    /**
     * 任务通知：消息编码
     */
    public static final String MESSAGE_ID = "message_id";
    /**
     * 任务通知：提交人
     */
    public static final String CREATE_BY = "create_by";
    /**
     * 任务通知：标题名称
     */
    public static final String TASK_TITLE = "task_title";

    /**
     * 内容索引名称
     */
    public static final String INFO_INDEX_NAME = "infoindex";

    /**
     * 内容索引类型
     */
    public static final String INFO_INDEX_TYPE = "content";

    /**
     * 模板类型：栏目
     */
    public static final String PRT_MODEL_TYPES_CHANNEL = "20";

    /**
     * 模板类型：文章
     */
    public static final String PRT_MODEL_TYPES_INFO = "30";

    /**
     * 模板存放路径
     */
    public static final String MODEL_PATH = "/webpage/modules/cms/model/";

    /**
     * 模板存放路径：栏目
     */
    public static final String MODEL_PATH_CHANNEL = "channel";

    /**
     * 模板存放路径：首页
     */
    public static final String MODEL_PATH_INDEX = "index";

    /**
     * 模板存放路径：文章
     */
    public static final String MODEL_PATH_INFO = "info";

    /**
     * 模板存放路径：友情链接
     */
    public static final String MODEL_PATH_LINK = "link";

    /**
     * 模板存放路径：后缀
     */
    public static final String MODEL_PATH_JSP = ".jsp";

    /**
     * 模板存放路径：图片后缀
     */
    public static final String MODEL_PATH_PIC = ".png";

    /**
     * 树节点类型:栏目
     */
    public static final String TREE_NODE_TYPE_CHANNEL = "channel";

    /**
     * 树节点类型:站点
     */
    public static final String TREE_NODE_TYPE_SITE = "site";

    /**
     * 广告显示状态：10 上线；20 下线
     */
    public static final String ADVERT_SHOW_SX = "10";
    /**
     * 广告显示状态：10 上线；20 下线
     */
    public static final String ADVERT_SHOW_XX = "20";

    /**
     * 默认站点ID
     */
    public static final String MODEL_SITE_ID = "4f6ce948b2a144d899e527a1a6936f8b";

    /**
     * 空字符串
     */
    public static final String NULL_STR = "";

    /**
     * 栏目标志新添加
     */
    public static final String ADD_STR = "addStr";

    /**
     * 栏目评论默认：否
     */
    public static final String NO_COMMENT = "0";

    /**
     * 流程状态：暂存
     */
    public static final String ACT_STATUS_UNSTART = "unstart";

    /**
     * 流程状态：申请
     */
    public static final String ACT_STATUS_APPLY = "first_";

    /**
     * 流程状态：终止
     */
    public static final String ACT_STATUS_TERMINATE = "terminate";

    /**
     * 部门通讯录：间隔符
     */
    public static final String SPLIT_STR = ":";
}
