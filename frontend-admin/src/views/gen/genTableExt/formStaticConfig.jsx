import {alignTypeOptionData, booleanOptionData, booleanStrOptionData, queryTypeOptionData, queryFieldTypeOptionData, yesNoOptionData} from "@/views/gen/genOptionData";
import UJson from "@/components/form/UJson";
export const editorTable = {
    disableInitLoad: true,
    pagination: false,
    showIndex: false,
    autoHeight: true,
    showSorter: false,
    unSaveRowsLocation: 'after',
    rowSelection: false,
    rowButtons: [
        {
            value: 'delete',
            text: '移除',
        }
    ]
}
export const  mockTableColumnsArr = [
    {title: 'dataIndex', dataIndex: 'listConfig__dataIndex', resizable: false,width: 225, editor: {type: 'input',}},
    {title: 'title', dataIndex: 'listConfig__title', resizable: false,width: 150, editor: {type: 'input',}},
    {
        title: '是否查询',
        dataIndex: 'isQuery',
        width: 120,
        resizable: false,
        editor: {type: 'select', props: {formType: 'radio', optionData: yesNoOptionData, valueField: 'value', textField: 'text'}, rules: [{required: true, message: ''}]}
    },
    {
        title: '查询类型',
        dataIndex: 'queryType',
        width: 90,
        resizable: false,
        editor: {type: 'select', props: {optionData: queryTypeOptionData, valueField: 'value', textField: 'text'}}
    },
    {title: '查询字段', dataIndex: 'listConfig__queryColumn',resizable: false, width: 150, editor: {type: 'input',}},
    {title: '查询默认值', dataIndex: 'listConfig__queryDefaultValue',resizable: false, width: 150, editor: {type: 'input',}},
    {
        title: '查询组件',
        dataIndex: 'listConfig__queryFieldType',
        width: 120,
        resizable: false,
        editor: {type: 'select', props: {optionData: queryFieldTypeOptionData, valueField: 'value', textField: 'text'}}
    },
    {
        title: '组件Props',
        dataIndex: 'listConfig__queryFieldProps',
        width: 120,
        resizable: false,
        editor: {
            type: 'dialog', text: '编辑', props: {width: 1000}, dialogSlot: ({text, updateValue}) => {
                return <UJson value={text} onUpdate:value={(val) => {
                    updateValue(val)
                }}/>
            }
        }
    },
    {
        title: 'minWidth', dataIndex: 'listConfig__minWidth', width: 100, resizable: false, editor: {
            type: 'input',
            props: {
                type: 'number', step: 10, min: 20
            }
        }
    },
    {
        title: 'align',
        dataIndex: 'listConfig__align',
        width: 100,
        resizable: false,
        editor: {type: 'select', props: {optionData: alignTypeOptionData, valueField: 'value', textField: 'text'}, rules: [{required: true, message: ''}]}
    },
    {
        title: 'sorter',
        dataIndex: 'listConfig__sorter',
        width: 120,
        resizable: false,
        editor: {type: 'select', props: {formType: 'radio', optionData: booleanStrOptionData, valueField: 'value', textField: 'text'}, rules: [{required: true, message: ''}]}
    },
    {title: 'sortColumn', dataIndex: 'listConfig__sortColumn',resizable: false, width: 150, editor: {type: 'input',}},
    {
        title: 'ellipsis',
        dataIndex: 'listConfig__ellipsis',
        width: 120,
        resizable: false,
        editor: {type: 'select', props: {formType: 'radio', optionData: booleanOptionData, valueField: 'value', textField: 'text'}, rules: [{required: true, message: ''}]}
    },
    {
        title: 'dict', dataIndex: 'listConfig__dict',resizable: false, width: 150, editor: {
            type: 'select', props: {
                type: 'table',
                dictType: 'sys_dictionary', valueField: 'code', textField: 'name',
                format: (row) => row.name + '(' + row.code + ')',
                tableOrderBy: 'a.sort asc', tableFilterData: [{key: 'a.parent_code', type: 'eq', value: 'data-params'}]
            }
        }
    },
    {
        title: '字段多选',
        dataIndex: 'listConfig__dictMultiple',
        width: 120,
        resizable: false,
        editor: {type: 'select', props: {formType: 'radio', optionData: yesNoOptionData, valueField: 'value', textField: 'text'}, rules: []}
    },
    {
        title: 'APP列表显示',
        dataIndex: 'blockChainParam4',
        width: 120,
        resizable: false,
        editor: {type: 'select', props: {formType: 'radio', optionData: yesNoOptionData, valueField: 'value', textField: 'text'}, rules: []}
    },
    {
        title: 'APP列表标题',
        dataIndex: 'blockChainParam5',
        width: 120,
        resizable: false,
        editor: {type: 'select', props: {formType: 'radio', optionData: yesNoOptionData, valueField: 'value', textField: 'text'}, rules: []}
    },
    {title: 'enableText', dataIndex: 'listConfig__enableText',resizable: false, width: 150, editor: {type: 'input',}},
    {title: 'disableText', dataIndex: 'listConfig__disableText',resizable: false, width: 150, editor: {type: 'input',}},
    {
        title: 'widthMultiple', dataIndex: 'listConfig__widthMultiple',resizable: false, width: 150,editor: {
            type: 'input',
            props: {
                type: 'number', step: 0.1, min: 0.1, precision: 1
            }
        }
    },
]
//TODO 支持更多column的配置 支持自定义列

export const modalSelectColumnsArr = [
    {title: 'dataIndex', dataIndex: 'dataIndex', minWidth: 120, editor: {type: 'input', rules: [{required: true, message: ''}]}},
    {title: 'title', dataIndex: 'title', minWidth: 120, editor: {type: 'input', rules: [{required: true, message: ''}]}},
    {
        title: 'align',
        dataIndex: 'align',
        width: 100,
        resizable: false,
        editor: {type: 'select', props: {optionData: alignTypeOptionData, valueField: 'value', textField: 'text'}, rules: [{required: true, message: ''}]}
    },
    {
        title: '是否显示',
        dataIndex: 'isShow',
        width: 80,
        resizable: false,
        editor: {type: 'select', props: {optionData: yesNoOptionData, valueField: 'value', textField: 'text'}, rules: [{required: true, message: ''}]}
    },
    {
        title: '是否查询',
        dataIndex: 'isQuery',
        width: 80,
        resizable: false,
        editor: {type: 'select', props: {optionData: yesNoOptionData, valueField: 'value', textField: 'text'}, rules: [{required: true, message: ''}]}
    },
    {title: '查询字段', dataIndex: 'queryDataIndex', minWidth: 120, editor: {type: 'input'}},
    {title: '字典', dataIndex: 'dict', minWidth: 120, editor: {
            type: 'select', props: {
                type: 'table',
                dictType: 'sys_dictionary', valueField: 'code', textField: 'name',
                format: (row) => row.name + '(' + row.code + ')',
                tableOrderBy: 'a.sort asc', tableFilterData: [{key: 'a.parent_code', type: 'eq', value: 'data-params'}]
            }
        }},
    {
        title: 'minWidth', dataIndex: 'minWidth', width: 100, resizable: false, editor: {
            type: 'input',
            props: {
                type: 'number', step: 10, min: 20
            }
        }
    },
    {title: '排序', dataIndex: 'rowSort', width: 60, fixed: 'right', editor: {type: 'rowSort'}}
]

export const modalMultiSelectColumnsArr = modalSelectColumnsArr.concat([
    {
        title: '是否编码',
        dataIndex: 'isCode',
        width: 80,
        resizable: false,
        editor: {type: 'select', props: {optionData: yesNoOptionData, valueField: 'value', textField: 'text'}}
    },
    {
        title: '是否名称',
        dataIndex: 'isName',
        width: 80,
        resizable: false,
        editor: {type: 'select', props: {optionData: yesNoOptionData, valueField: 'value', textField: 'text'}}
    },
])
export const defaultRowButtonArr = [{
    value: 'view',
    text: '查看',
}, {
    value: 'edit',
    text: '编辑',
}]

export const treeTableRowButtonArr = [{
    value: 'view',
    text: '查看',
}, {
    value: 'edit',
    text: '编辑',
}, {
    value: 'delete',
    text: '删除'
}, {
    value: 'addChild',
    text: '添加下级'
}]

export const defaultListButtonArr = [{
    value: 'add',
    text: '添加'
}, {
    value: 'batch-delete',
    text: '删除'
}]

export const treeTableListButtonArr = [{
    value: 'add',
    text: '添加'
}, {
    value: 'reloadTree',
    text: '刷新'
}]
