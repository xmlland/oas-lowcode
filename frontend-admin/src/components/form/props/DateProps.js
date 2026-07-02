import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";

class DateProps extends FormControlProps {

    /**
     * 是否显示快捷选择
     * @type {boolean}
     */
    showQuickSelect = false

    /**
     * 是否inputReadOnly 选择框只读（避免键盘输入）
     */
    inputReadOnly = false

    /**
     * 是否允许清除
     * @type {boolean}
     */
    allowClear = true

    /**
     * 日期格式
     * @type {string}
     */
    formatPatter = 'yyyy-MM-dd'

    /**
     * 最小日期 字符串
     * @type {null}
     */
    minValue = null
    /**
     * 最大日期 字符串
     * @type {null}
     */
    maxValue = null

    constructor() {
        super();
    }
}

export default DateProps
