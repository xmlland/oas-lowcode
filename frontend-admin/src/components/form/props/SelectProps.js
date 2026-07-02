import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";

class SelectProps extends FormControlProps {
    /**
     * 是否多选
     * @type {boolean}
     */
    multiple = false

    /**
     * 多选分隔符
     * @type {string}
     */
    multipleSeparator = ','

    /**
     * 类型     dict|table 系统字典|表格数据
     * @type {string}
     */
    type = 'dict'

    /**
     * 控件类型  select | radio | checkbox  下拉框 | 单选框 | 复选框
     * @type {string}
     */
    formType = 'select'

    /**
     * 复选框不换行
     * @type {boolean}
     */
    checkboxNowrap = true

    /**
     * 复选框span列数
     * @type {number}
     */
    checkboxSpan = 24

    /**
     * 字典编码
     * @type {string}
     */
    dictType = ''

    /**
     * 从表格选数据的最大条数
     * @type {number}
     */
    tablePageSize = 1000

    /**
     * 排序字段 a.sort asc
     * @type {string}
     */
    tableOrderBy = ''

    /**
     * 表格数据过滤条件
     * @type {[]}
     */
    tableFilterData = []

    /**
     * 固定选项
     * @type {[]}
     */
    optionData = []

    /**
     * 存储值字段
     * @type {string}
     */
    valueField = 'member'

    /**
     * 显示值字段
     * @type {string}
     */
    textField = 'memberName'

    /**
     * 是否显示值存储值
     * @type {boolean}
     */
    showValue = false

    /**
     * 显示值格式化函数字符串
     * @type {null}
     */
    formatFuncStr = null

    /**
     * 是否显示序号
     * @type {boolean}
     */
    showIndex = false

    /**
     * 是否允许清除
     * @type {boolean}
     */
    allowClear = true

    /**
     * 是否显示搜索图标
     * @type {boolean}
     */
    showSearch = true

    /**
     * 多选做多显示多少个tag
     * @type {null}
     */
    maxTagCount = null

    /**
     * 默认选中的index
     * @type {null}
     */
    defaultIndex = null

    /**
     * 在查询区域出现时，如果设置了defaultIndex，是否自动加载数据
     * @type {boolean}
     */
    loadDataByDefaultIndex = true

    /**
     * 是否在查询区域出现时，如果改变了值，是否自动加载数据
     * @type {boolean}
     */
    triggerLoadDataOnChange = false

    constructor() {
        super();
    }
}

export default SelectProps
