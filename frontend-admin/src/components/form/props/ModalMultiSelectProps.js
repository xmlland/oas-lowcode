import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";

class ModalMultiSelectProps extends FormControlProps {

        /**
     * 选择数据弹窗标题
     * @type {string}
     */
    modalTitle = '请选择'

    /**
     * 选择数据弹窗宽度
     * @type {number}
     */
    modalWidth = 800

    /**
     * 选择数据表
     * @type {string}
     */
    formNo = ''

    /**
     * 数据过滤条件
     * @type {[]}
     */
    filterData = []

    /**
     * 查询字段
     * @type {string}
     */
    searchKey = ''

    /**
     * 查询字段label
     * @type {string}
     */
    searchLabel = ''

    /**
     * 查询配置
     * @type {{}}
     */
    searchConfig = {}

    /**
     * 查询label宽度
     * @type {null}
     */
    searchLabelWidth = null

    /**
     * 显示的列配置
     * @type {[]}
     */
    columns = []

    dictIdFiled = 'id'
    codeFiled = 'code'
    nameFiled = 'name'

    format = (item) => {
        if (item.code) {
            return `${item.name}（${item.code} ）`
        }
        return item.name
    }

    /**
     * 显示值格式化函数字符串
     * @type {null}
     */
    formatFuncStr = null

    /**
     * 选中项是否可点击
     * @type {boolean}
     */
    clickable = false

    /**
     * 查询数据url
     * @type {{list:''}}
     */
    urlData = {}

    /**
     * 是否使用genTable的sql
     * @type {string}
     */
    genTableSql = '0'

    ignoreProperty = ['ignoreProperty', 'props', 'apply', 'disabled']
    props = {}
    apply = () => {
        let names = Object.getOwnPropertyNames(this);
        let _props = {
            columns: [],
        }
        for (let name of names) {
            if (this.ignoreProperty.indexOf(name) === -1) {
                _props[name] = this[name]
            }
        }
        if (this.allColumns.length > 0){
            _props.columns = []
            let searchList = []
            let codeFiled = []
            let nameFiled = []
            this.allColumns.forEach((item) => {
                if (item.isShow === '1') {
                    _props.columns.push(item)
                }
                if (item.isQuery === '1') {
                    searchList.push({
                        searchKey: item.queryDataIndex||item.dataIndex,
                        searchLabel: item.title
                    })
                }
                if (item.isCode === '1'){
                    codeFiled.push(item)
                }
                if (item.isName === '1'){
                    nameFiled.push(item)
                }
            })
            if (codeFiled.length === 0) {
                if (this.allColumns.length > 0) {
                    codeFiled.push(this.allColumns[0])
                }
            }
            if (nameFiled.length === 0) {
                if (this.allColumns.length > 0) {
                    nameFiled.push(this.allColumns[0])
                }
            }
            _props.codeFiled = codeFiled.length > 0 ? codeFiled[0].dataIndex : ''
            _props.nameFiled = nameFiled.length > 0 ? nameFiled[0].dataIndex : ''

            _props.searchKey = searchList.map((item) => item.searchKey)
            _props.searchLabel = searchList.map((item) => item.searchLabel)
            this.searchKey = _props.searchKey
            this.searchLabel = _props.searchLabel
            this.codeFiled = _props.codeFiled
            this.nameFiled = _props.nameFiled
            this.columns = _props.columns
        }
        let newObj = {}
        Object.assign(newObj, _props)
        this.props = newObj
    }
    constructor() {
        super();
    }
}

export default ModalMultiSelectProps
