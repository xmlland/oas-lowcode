import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";

class TreeSelectProps extends FormControlProps {

    /**
     * 是否允许清除
     * @type {boolean}
     */
    allowClear = true

    /**
     * 是否显示搜索
     * @type {boolean}
     */
    showSearch = true

    /**
     * 是否显示连接线
     * @type {boolean}
     */
    treeLine = false

    /**
     * 上级数据id
     * @type {string}
     */
    parentId = '0'

    /**
     * 表名
     * @type {string}
     */
    formNo = ''
    /**
     * 固定数据
     * @type {[]}
     */
    data = []

    /**
     * 是否仅允许选择叶子节点
     * @type {boolean}
     */
    onlyLeafSelect = false

    constructor() {
        super();
    }
}

export default TreeSelectProps
