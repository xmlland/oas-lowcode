import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";

class ModalSelectProp extends FormControlProps {

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
     * 目标表 从数据表选择之后存储到某个表
     */
    targetFormNo = ''

    /**
     * 目标表 存储字段
     * @type {string}
     */
    targetField = ''

    /**
     * 目标表数据过滤条件 一般用于数据选择后不允许多次选择
     * @type {[]}
     */
    targetFilterData = []

    /**
     * 数据带回配置
     * @type {{}}
     */
    formUpdateMap = {}

    /**
     * 查询字段
     * @type {Array}
     */
    searchKey = []

    /**
     * 查询字段label
     * @type {Array}
     */
    searchLabel = []

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
     * 所以的列配置
     * @type {[]}
     */
    allColumns = []
    /**
     * 显示的列配置
     * @type {[]}
     */
    columns = []

    /**
     * 选中后显示值的字段
     * @type {string}
     */
    nameDataIndex = ''

    /**
     * 查询数据的url
     * @type {string}
     */
    dataUrl = 'dynamic/zform/gridselectDataMap'
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
            })
            _props.searchKey = searchList.map((item) => item.searchKey)
            _props.searchLabel = searchList.map((item) => item.searchLabel)
            this.searchKey = _props.searchKey
            this.searchLabel = _props.searchLabel
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

export default ModalSelectProp
