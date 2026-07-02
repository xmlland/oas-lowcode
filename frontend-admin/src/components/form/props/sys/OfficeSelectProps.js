import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";

class OfficeSelectProps extends FormControlProps {

    /**
     * 上级机构
     * @type {string}
     */
    parentId = undefined

    constructor() {
        super();
    }
}

export default OfficeSelectProps
