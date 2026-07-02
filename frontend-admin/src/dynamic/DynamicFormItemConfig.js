import {UUID} from "@/lib/tools";
import * as validator from "@/lib/validator";
import {initDynamicFormItemFormControlProps, validateTypeConfig} from "@/views/gen/dynamicFormItem";

class ColProps {
    /**
     * 栅格占位格数，为 0 时相当于 display: none
     * @type {number}
     */
    span = 12
    ignoreProperty = ['ignoreProperty', 'props', 'apply']

    props = {}
    apply = () => {
        let names = Object.getOwnPropertyNames(this);
        let _props = {}
        for (let name of names) {
            if (this.ignoreProperty.indexOf(name) === -1) {
                _props[name] = this[name]
            }
        }
        let newObj = {}
        Object.assign(newObj, _props)
        this.props = newObj
    }

    constructor(span) {
        this.span = span
    }
}

export class FormItemProps {
    /**
     * 表单项名称
     * @type {string}
     */
    name = ''
    /**
     * 表单项标签
     * @type {string}
     */
    label = ''
    /**
     *    当某一规则校验不通过时，是否停止剩下的规则的校验。
     * @type {boolean}
     */
    validateFirst = true
    /**
     * 表单项校验规则
     * @type {[]}
     */
    rules = []

    /**
     * 是否校验唯一性
     * @type {string}
     */
    unique = '0'

    /**
     * 校验类型
     * @type {string}
     */
    validateType = ''

    /**
     * 加密类型
     * @type {string}
     */
    encryptType = ''

    /**
     * 是否必填
     * @type {string}
     */
    required = '0'

    /**
     * 必填提示语前缀
     * @type {string}
     */
    pleasePrefix = '请输入'

    /**
     * 表单项提示信息
     * @type {string}
     */
    extra = ''

    ignoreProperty = ['ignoreProperty', 'props', 'apply', 'rules', 'unique', 'validateType', 'encryptType', 'required', 'pleasePrefix']

    props = {}
    apply = () => {
        let names = Object.getOwnPropertyNames(this);
        let _props = {}
        for (let name of names) {
            if (this.ignoreProperty.indexOf(name) === -1) {
                _props[name] = this[name]
            }
        }
        let _rules = this.required === '1' ? [{required: true, message: this.pleasePrefix + this.label}] : []
        if (this.validateType && validateTypeConfig[this.validateType]) {
            _rules.push({validator: validator.getValidator(this.validateType), validateType: this.validateType, message: validateTypeConfig[this.validateType]});
        }
        this.rules = _rules
        let newObj = {}
        Object.assign(newObj, _props)
        this.props = newObj
    }

    constructor() {

    }
}

export class FormControlProps {

    /**
     * placeholder
     * @type {string}
     */
    placeholder = ''
    /**
     * 是否禁用
     * @type {string}
     */
    disabled = '0'

    /**
     * 默认值
     * @type {null}
     */
    defaultValue = null

    ignoreProperty = ['ignoreProperty', 'props', 'apply', 'disabled']
    props = {}
    apply = () => {
        let names = Object.getOwnPropertyNames(this);
        let _props = {}
        for (let name of names) {
            if (this.ignoreProperty.indexOf(name) === -1) {
                _props[name] = this[name]
            }
        }
        let newObj = {}
        Object.assign(newObj, _props)
        this.props = newObj
    }

    constructor() {

    }

}

class DynamicFormItemConfig {
    colProps = new ColProps()
    formItemProps = new FormItemProps()
    formControlProps = new FormControlProps()

    /**
     * 表单项id
     * @type {string}
     */
    id = UUID()
    /**
     * 表单项类型
     * @type {string}
     */
    type = ''

    /**
     * 表单类型key
     * @type {string}
     */
    key = ''

    /**
     * 表单项配置
     * @type {{}}
     */
    column = {}

    constructor(column) {
        this.column = column
    }

    apply = () => {
        this.colProps.apply()
        this.formItemProps.apply()
        this.formControlProps.apply()
    }

    toJson = () => {
        let json = {
            toObject: () => {
                let obj = new DynamicFormItemConfig({})
                initDynamicFormItemFormControlProps(obj, json.type)
                for (let name in json) {
                    if (name.indexOf('__') >= 0) {
                        if (!obj[name.split('__')[0]]) {
                            obj[name.split('__')[0]] = {}
                        }
                        obj[name.split('__')[0]][name.split('__')[1]] = json[name]
                    } else {
                        obj[name] = json[name]
                    }
                }
                obj.id = json.__id
                return obj
            }
        }
        json.__id = this.id
        json.type = this.type
        json.key = this.key
        for (let name in this.column) {
            json['column__' + name] = this.column[name]
        }
        for (let name in this.colProps) {
            if (this.colProps.ignoreProperty.indexOf(name) === -1) {
                json['colProps__' + name] = this.colProps[name]
            }
        }
        for (let name in this.formItemProps) {
            if (this.formItemProps.ignoreProperty.indexOf(name) === -1) {
                json['formItemProps__' + name] = this.formItemProps[name]
            }
            json['formItemProps__required'] = this.formItemProps.required
            json['formItemProps__unique'] = this.formItemProps.unique
            json['formItemProps__validateType'] = this.formItemProps.validateType
            json['formItemProps__encryptType'] = this.formItemProps.encryptType
        }
        for (let name in this.formControlProps) {
            if (this.formControlProps.ignoreProperty.indexOf(name) === -1) {
                json['formControlProps__' + name] = this.formControlProps[name]
            }
            json['formControlProps__disabled'] = this.formControlProps.disabled
            if (name==='genTableSql') {

                json['formControlProps__genTableSql'] = this.formControlProps.genTableSql
            }
        }
        return json
    }

}

export default DynamicFormItemConfig
