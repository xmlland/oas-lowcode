import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";
import config from "@/config"

class AreaProps extends FormControlProps {

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
     * 是否自由选择 true:可以自由选择任意区域  false:只能选择到最后一级
     * @type {boolean}
     */
    freeChoice = false

    /**
     * 显示到那个等级:1:显示到省,2:显示到市,3:显示到区/县
     * @type {number}
     */
    showRank = 3

    /**
     * 顶级行政区id
     * @type {number}
     */
    rootAreaId = config.areaId

    /**
     * （单选时生效）当此项为 true 时，点选每级菜单选项值都会发生变化
     * @type {boolean}
     */
    changeOnSelect = false

    constructor() {
        super();
    }
}

export default AreaProps
