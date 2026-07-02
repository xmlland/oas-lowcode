import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";

class SerialNoProps extends FormControlProps {
    /**
     * 编号前缀，如 "BK"、"CG" 等
     * @type {string}
     */
    prefix = ''

    /**
     * 允许清除
     * @type {boolean}
     */
    allowClear = true

    constructor() {
        super();
    }
}

export default SerialNoProps
