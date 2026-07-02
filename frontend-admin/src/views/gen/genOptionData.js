import {validateTypeConfig} from './dynamicFormItem'
import {patternMapping} from "@/components/form/date";
import config from '@/config'

/**
 * 是否选项数据
 * @type {[{text: string, value: string},{text: string, value: string}]}
 */
export const yesNoOptionData = [
    {value: '1', text: '是'},
    {value: '0', text: '否'},
]

/**
 * true false 选项数据
 * @type {[{text: string, value: string},{text: string, value: string}]}
 */
export const booleanOptionData = [
    {value: true, text: '是'},
    {value: false, text: '否'},
]

/**
 * true false 选项数据
 * @type {[{text: string, value: string},{text: string, value: string}]}
 */
export const booleanStrOptionData = [
    {value: 'true', text: '是'},
    {value: 'false', text: '否'},
]

/**
 * 父表类型选项数据
 * @type {[{text: string, value: string},{text: string, value: string},{text: string, value: string}]}
 */
export const parentTableOptionData = [
    {value: '0', text: '列表'},
    {value: '3', text: '树表'},
    {value: '4', text: '左树右表'},
]
/**
 * 子表类型选项数据
 * @type {[{text: string, value: string},{text: string, value: string}]}
 */
export const childTableOptionData = [
    {value: '0', text: '列表'},
    {value: '4', text: '左树右表'},
]
/**
 * 表单类型选项数据
 * @type {({text: string, value: string}|{text: string, value: string}|{text: string, value: string})[]}
 */
export const tableTypeOptionData = parentTableOptionData.concat(childTableOptionData)

/**
 * 校验类型选项数据
 * @type {{text: *, value: *}[]}
 */
export const validateTypeOptionData = Object.keys(validateTypeConfig).map(key => {
    return {
        text: validateTypeConfig[key].replace('请输入', ''),
        value: key
    }
})

/**
 * 日期格式选项数据
 * @type {{text: *, value: *}[]}
 */
export const patternTypeOptionData = Object.keys(patternMapping).map(key => {
    return {
        text: key,
        value: key
    }
})

/**
 * 加密类型选项数据
 * @type {[{text: string, value: string},{text: string, value: string},{text: string, value: string},{text: string, value: string},{text: string, value: string},null,null,null,null]}
 */
export const encryptTypeOptionData = [
    {value: 'personName', text: '姓名'},
    {value: 'idCard', text: '身份证号'},
    {value: 'idCardStrong', text: '身份证号(强)'},
    {value: 'otherIdCard', text: '其他证件'},
    {value: 'mobilePhone', text: '手机'},
    {value: 'fixedPhone', text: '固定电话'},
    {value: 'email', text: '电子邮件'},
    {value: 'bankCard', text: '银行卡号'},
    {value: 'other', text: '其他'},
]

export const javaTypeOptionData = [
    {value: 'String', text: 'String'},
    {value: 'Long', text: 'Long'},
    {value: 'Integer', text: 'Integer'},
    {value: 'Double', text: 'Double'},
    {value: 'Boolean', text: 'Boolean'},
    {value: 'java.math.BigDecimal', text: 'BigDecimal'},
    {value: 'java.util.Date', text: 'Date'},
    {value: 'com.jeestudio.bpm.common.entity.system.User', text: 'User'},
    {value: 'com.jeestudio.bpm.common.entity.system.Office', text: 'Office'},
    {value: 'com.jeestudio.bpm.common.entity.system.Area', text: 'Area'},
    {value: 'This', text: 'ThisObj'},
    {value: 'com.jeestudio.bpm.common.entity.common.Zform', text: 'Zform'},

]

export const jdbcTypeOptionData = [
    {value: 'varchar', text: '文本'},
    {value: 'integer', text: '整型'},
    {value: 'double', text: '数字'},
    {value: 'decimal', text: '精确数值'},
    {value: 'boolean', text: '布尔'},
    {value: 'datetime', text: '日期'},
    {value: 'time', text: '时间'},
    {value: 'longblob', text: '二进制'},
    {value: 'longtext', text: '长文本'},

]

/**
 * 对齐方式选项数据
 * @type {[{text: string, value: string},{text: string, value: string},{text: string, value: string}]}
 */
export const alignTypeOptionData = [
    {value: 'left', text: 'left'},
    {value: 'center', text: 'center'},
    {value: 'right', text: 'right'},
]

export const acceptFilesOptionData = config.acceptFiles.map(item => {
    return {
        text: item,
        value: item
    }
})

/**
 * 查询类型选项数据
 * @type {[{text: string, value: string},{text: string, value: string},{text: string, value: string},{text: string, value: string},{text: string, value: string},null,null,null,null,null,null]}
 */
export const queryTypeOptionData = [
    {value: '=', text: '='},
    {value: 'in', text: 'in'},
    {value: '!=', text: '!='},
    {value: '>', text: '>'},
    {value: '>=', text: '>='},
    {value: '<', text: '<'},
    {value: '<=', text: '<='},
    {value: 'between', text: 'Between'},
    {value: 'like', text: 'Like'},
    {value: 'left_like', text: 'Left Like'},
    {value: 'right_like', text: 'Right Like'},
]

/**
 * 查询字段组件选项数据
 */
export const queryFieldTypeOptionData = [
    {value: 'input', text: '文本输入框'},
    {value: 'yes-no', text: '是否选择'},
    {value: 'area', text: '行政区选择'},
    {value: 'date', text: '日期选择'},
    {value: 'date-range', text: '日期范围'},
    {value: 'select', text: '下拉选择'},
    {value: 'modal-select', text: '弹窗选择'},
    {value: 'modal-multi-select', text: '弹窗多选'},
    {value: 'user-select', text: '用户选择'},
    {value: 'users-select', text: '用户多选'},
    {value: 'office-select', text: '机构选择'},
    {value: 'tree-select', text: '树选择'},
    {value: 'cascader-select', text: '级联选择'},
]

/**
 * 用户选择数据范围选项数据
 * @type {[{text: string, value: string},{text: string, value: string},{text: string, value: string}]}
 */
export const userSelectDataScopeOptionData = [
    {value: 'all', text: '全部'},
    {value: 'org', text: '本部门及下属部门'},
    {value: 'target', text: '指定部门'},
]

/**
 * 是否选项数据
 * @type {[{text: string, value: string},{text: string, value: string}]}
 */
export const areaLevelOptionData = [
    {value: '1', text: '省/直辖市'},
    {value: '2', text: '市'},
    {value: '3', text: '区/县'},
]

export const subTableTypeOptionData = [
    {value: 'top', text: '顶部选项卡'},
    {value: 'bottom', text: '主表下方选项卡'},
    {value: 'anchor', text: '锚点选项卡'},
]

/**
 * 动态表单编辑打开方式
 * gen_table.blockchain_param5：空或1=默认弹窗，2=页面编辑，3=右侧抽屉
 */
export const editOpenModeOptionData = [
    {value: '1', text: '默认弹窗'},
    {value: '2', text: '页面编辑'},
    {value: '3', text: '右侧抽屉'},
]
