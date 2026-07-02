import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";

class RichTextProps extends FormControlProps {
    /**
     * 类型 text digits number
     * @type {string}
     */
    type = 'richText'
    /**
     * 最大值
     * @type {number}
     */
    max = null

    /**
     * 最小值
     */
    min = null

    /**
     * 最大长度
     */
    maxlength = 2000

    /**
     * 允许清除
     * @type {boolean}
     */
    allowClear = true

    /**
     * 是否多行文本
     * @type {boolean}
     */
    textarea = false

    /**
     * 是否显示字数
     * @type {boolean}
     */
    showCount = false

    /**
     * 自适应高度
     * @type {{minRows: number, maxRows: number}}
     */
    autoSize = {minRows: 2, maxRows: 6}

    constructor() {
        super();
    }
}

export default RichTextProps
