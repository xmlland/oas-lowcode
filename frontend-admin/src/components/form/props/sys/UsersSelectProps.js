import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";
import config from "@/config"

class UsersSelectProps extends FormControlProps {
    /**
     * 选择数据弹窗宽度
     * @type {number}
     */
    modalWidth = 1200

    /**
     *  数据范围 all:全部 org:本部门及下属部门 target:指定部门
     */
    dataScope = 'all'

    /**
     * 指定部门id
     * @type {string}
     */
    targetOrgId = ''

    /**
     * 组件类型
     * @type {string}
     */
    componentType = config.component && config.component.usersSelect && config.component.usersSelect.componentType ? config.component.usersSelect.componentType : 'default'

    constructor() {
        super();
    }
}

export default UsersSelectProps
