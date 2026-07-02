import {isEndWith} from "@/lib/tools";

export const dispatchMockEvent = (table,data) => {

    if (window.previewWindow) {
        setTimeout(() => {
            window.dispatchEvent(new CustomEvent('acceptDataFormParent', {
                detail: {
                    table: table,
                    data: data
                }
            }))

            setTimeout(() => {
                window.dispatchEvent(new CustomEvent('acceptDataFormParent', {
                    detail: {
                        table: table,
                        data: data
                    }
                }))
            }, 800)
        }, 500)

    }

}

/**
 * 初始化列的listConfig
 * @param column
 * @param syncTitle 是否同步标题
 * @return {{}}
 */
export const initColumnListConfig = (column,syncTitle=false) => {
    let obj = {}
    let listConfig = column.listConfig ? JSON.parse(column.listConfig) : {}
    let formItemConfig = column.formItemConfig ? JSON.parse(column.formItemConfig) : {}
    let formControlProps = formItemConfig.formControlProps || {}
    obj.isExtend = column.isExtend || '0'
    obj.isList = column.isList || '0'
    obj.isForm = column.isForm || '0'
    obj.listSort = column.listSort || 0
    obj.isQuery = column.isQuery || '0'
    obj.queryType = column.queryType === 'eq' ? '=' : (column.queryType || '')
    obj.name = column.name
    //TODO 根据不同的类型，设置不同的默认值
    //截取最后5位字符 判断是否为子对象字段
    let isNameField = isEndWith(column.javaField || '', '|name') || (column.javaField || '').indexOf('id|') >= 0

    // 优先使用已保存的 dataIndex，否则根据规则计算默认值
    if (listConfig.dataIndex) {
        obj.listConfig__dataIndex = listConfig.dataIndex
    } else {
        obj.listConfig__dataIndex = column.name + (isNameField ? '__name' : '')
        //判断是否以__name结尾
        if (!isEndWith(obj.listConfig__dataIndex, '__name') && isNameField) {
            obj.listConfig__dataIndex += '__name'
        }
    }

    if (obj.isExtend === '1') {
        obj.listConfig__dataIndex = listConfig.dataIndex
    }
    if (syncTitle){
        obj.listConfig__title = column.comments || listConfig.title
    }else{
        obj.listConfig__title = listConfig.title || column.comments
    }
    let defaultAlign = 'left'
    //数字类型、日期类型默认居中 zhoury 2024-06-27
    if (column.jdbcType === 'integer' || column.jdbcType === 'double' || column.jdbcType === 'datetime' || column.jdbcType.startsWith('decimal') || column.jdbcType === 'tinyint(1)') {
        defaultAlign = 'center'
    }

    obj.listConfig__minWidth = listConfig.minWidth || 150
    obj.listConfig__align = column.align || defaultAlign
    obj.listConfig__sorter = listConfig.sorter || 'false'
    obj.listConfig__sortColumn = listConfig.sortColumn
    obj.listConfig__ellipsis = typeof (listConfig.ellipsis) === 'undefined' ? false : (listConfig.ellipsis === '1')//是否显示省略号 需要使用boolean类型
    obj.listConfig__dict = column.dictType || listConfig.dict
    obj.listConfig__dictMultiple = formControlProps.multiple || listConfig.dictMultiple
    obj.listConfig__enableText = listConfig.status ? listConfig.status.enableText : ''
    obj.listConfig__disableText = listConfig.status ? listConfig.status.disableText : ''
    obj.listConfig__queryColumn = listConfig.queryColumn || ''//查询条件的字段
    obj.listConfig__queryDefaultValue = listConfig.queryDefaultValue || ''//查询条件的默认值
    obj.listConfig__queryFieldType = listConfig.queryFieldType || ''//查询条件的控件类型
    obj.listConfig__queryFieldProps = listConfig.queryFieldProps || null//查询条件的props
    obj.listConfig__widthMultiple = listConfig.widthMultiple || 1
    obj.blockChainParam4 = column.blockChainParam4 || '0'
    obj.blockChainParam5 = column.blockChainParam5 || '0'
    if (listConfig.title) {
        obj.queryLabel = listConfig.title
    }
    return obj
}
