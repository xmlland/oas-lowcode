import DynamicFormItemConfig from "@/dynamic/DynamicFormItemConfig";
import InputProps from "@/components/form/props/InputProps";
import SelectProps from "@/components/form/props/SelectProps";
import DateProps from "@/components/form/props/DateProps";
import AreaProps from "@/components/form/props/AreaProps";
import UploadProps from "@/components/form/props/UploadProps";
import TreeSelectProps from "@/components/form/props/TreeSelectProps";
import ModalSelectProps from "@/components/form/props/ModalSelectProps";
import UserSelectProps from "@/components/form/props/sys/UserSelectProps";
import UsersSelectProps from "@/components/form/props/sys/UsersSelectProps";
import OfficeSelectProps from "@/components/form/props/sys/OfficeSelectProps";
import ModalMultiSelectProps from "@/components/form/props/ModalMultiSelectProps";
import {Modal} from "ant-design-vue";
import IconSelectProps from "@/components/form/props/IconSelectProps";
import {isEmpty} from "@/lib/tools";
import RichTextProps from "@/components/form/props/RichTextProps";
import SerialNoProps from "@/components/form/props/SerialNoProps";

import imgDhwb from '@/assets/img/dhwb.png'
import imgDdhwb from '@/assets/img/ddhwb.png'
import imgZs from '@/assets/img/zs.png'
import imgSz from '@/assets/img/sz.png'
import imgXl from '@/assets/img/xl.png'
import imgDx from '@/assets/img/dx.png'
import imgFx from '@/assets/img/fx.png'
import imgRl from '@/assets/img/rl.png'
import imgRyxz from '@/assets/img/ryxz.png'
import imgRydx from '@/assets/img/rydx.png'
import imgBmdx from '@/assets/img/bmdx.png'
import imgQy from '@/assets/img/qy.png'
import imgS from '@/assets/img/s.png'
import imgZdy from '@/assets/img/zdy.png'
import imgTcck from '@/assets/img/tcck.png'
import imgZsj from '@/assets/img/zsj.png'
import imgPx from '@/assets/img/px.png'
import imgFj from '@/assets/img/fj.png'
import imgTpfj from '@/assets/img/tpfj.png'
import imgWord from '@/assets/img/word.png'
import imgTb from '@/assets/img/tb.png'

const textInputFormItemType = 'textInput'
const textareaInputFormItemType = 'textareaInput'
const numberInputFormItemType = 'numberInput'
const digitsInputFormItemType = 'digitsInput'
const selectFormItemType = 'select'
const radioFormItemType = 'radio'
const checkboxFormItemType = 'checkbox'
const switchFormItemType = 'switch'
const dateSelectFormItemType = 'dateSelect'
const userSelectFormItemType = 'userSelect'
const usersSelectFormItemType = 'usersSelect'
const officeSelectFormItemType = 'officeSelect'
const areaFormItemType = 'area'
const treeSelectFormItemType = 'treeSelect'
const modalSelectFormItemType = 'modalSelect'
const modalMultiSelectFormItemType = 'modalMultiSelect'
const parentFormItemType = 'parent'
const sortInputFormItemType = 'sortInput'
const uploadFormItemType = 'upload'
const uploadPicFormItemType = 'uploadPic'
const uploadOnlineFormItemType = 'uploadOnline'
// const lngLatSelectFormItemType = 'lngLatSelect'
const iconSelectFormItemType = 'iconSelect'
const richTextFormItemType = 'richText'
const serialNoFormItemType = 'serialNo'

const normalizeTextInputMaxlength = (column = {}) => {
    const varcharLength = Number(column.varcharLength || 0)
    const maxLength = Number(column.maxLength || 0)
    if (!maxLength) {
        return varcharLength || 255
    }
    if (varcharLength > 0 && maxLength < varcharLength) {
        return varcharLength
    }
    return maxLength
}

export const dynamicFormItemType = [
    // {
    //     type: 'container',
    //     key: 'container',
    //     text: '容器',
    //     img:require('@/assets/img/rq.png')
    // },
    {
        type: 'input',
        key: textInputFormItemType,
        text: '单行文本',
        dataJson: {
            "showType": "input",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "单行文本",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "maxLength": "64",
            "minLength": "",
            "minValue": "",
            "maxValue": "",
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(64)",
            "queryType": "like",//输入框默认like
            "validateType": "",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "searchSort": 0,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "",
            "blockChainParam1": "0",
            "blockChainParam2": "0",
            "blockChainParam3": "0"
        },
        img:imgDhwb
    },
    {
        type: 'input',
        key: textareaInputFormItemType,
        text: '多行文本',
        dataJson: {
            "showType": "textarea",
            "isOneLine": 1,
            "isNull": 1,
            "comments": "多行文本",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "maxLength": "255",
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(255)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "0",
            "formSort": 1,
            "searchSort": 0,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "",
            "blockChainParam1": "0",
            "blockChainParam2": "0",
            "blockChainParam3": "0"
        },
        img:imgDdhwb
    },
    {
        type: 'input',
        key: numberInputFormItemType,
        text: '整数',
        dataJson: {
            "showType": "input",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "整数",
            "comments_EN": "",
            "remarks": "",
            "javaField": "i",
            "maxLength": "",
            "minLength": "",
            "minValue": "",
            "maxValue": "",
            "friendlyJdbcType": "整型",
            "javaType": "Integer",
            "jdbcType": "int",
            "queryType": "=",
            "validateType": "number",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "searchSort": 0,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "",
            "blockChainParam1": "0",
            "blockChainParam2": "0",
            "blockChainParam3": "0",

        },
        img:imgZs
    },
    {
        type: 'input',
        key: digitsInputFormItemType,
        text: '数字',
        dataJson: {
            "showType": "input",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "数字",
            "comments_EN": "",
            "remarks": "",
            "javaField": "b",
            "maxLength": "",
            "minLength": "",
            "minValue": "",
            "maxValue": "",
            "friendlyJdbcType": "精确数值",
            "javaType": "java.math.BigDecimal",
            "jdbcType": "decimal(18,4)",
            "queryType": "=",
            "validateType": "digits",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "searchSort": 0,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "",
            "blockChainParam1": "0",
            "blockChainParam2": "0",
            "blockChainParam3": "0",

        },
        img:imgSz
    },
    {
        type: 'select',
        key: selectFormItemType,
        text: '下拉选项',
        dataJson: {
            "showType": "select",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "下拉选项",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "dictType": "",
            "dictList": [],
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "searchSort": 0,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgXl
    },
    {
        type: 'select',
        key: radioFormItemType,
        text: '单选',
        dataJson: {
            "showType": "radiobox",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "单选",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "dictType": "",
            "dictList": [],
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgDx
    },
    {
        type: 'select',
        key: checkboxFormItemType,
        text: '复选',
        dataJson: {
            "showType": "checkbox",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "复选",
            "comments_EN": "",
            "remarks": "",
            "javaField": "c",
            "dictType": "",
            "dictList": [],
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgFx
    },
    {
        type: 'switch',
        key: switchFormItemType,
        text: '开关',
        dataJson: {
            "showType": "switch",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "开关",
            "comments_EN": "",
            "remarks": "",
            "javaField": "f",
            "friendlyJdbcType": "布尔",
            "javaType": "Boolean",
            "jdbcType": "tinyint(1)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "false",
            "name": ""
        },
        img:imgDx
    },
    {
        type: 'date',
        key: dateSelectFormItemType,
        text: '日期',
        dataJson: {
            "showType": "dateselect",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "日期",
            "comments_EN": "",
            "remarks": "",
            "javaField": "d",
            "friendlyJdbcType": "日期",
            "javaType": "java.util.Date",
            "jdbcType": "datetime",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "",
            "dateType": "yyyy-MM-dd"
        },
        img:imgRl
    },
    {
        type: 'userSelect',
        key: userSelectFormItemType,
        text: '人员选择',
        dataJson: {
            "showType": "treeselectRedio",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "人员选择",
            "comments_EN": "",
            "remarks": "",
            "javaField": "user",
            "friendlyJdbcType": "文本",
            "javaType": "com.jeestudio.bpm.common.entity.system.User",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgRyxz
    },
    {
        type: 'usersSelect',
        key: usersSelectFormItemType,
        text: '人员多选',
        dataJson: {
            "showType": "treeselectCheck",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "人员多选",
            "comments_EN": "",
            "remarks": "",
            "javaField": "users",
            "friendlyJdbcType": "文本",
            "javaType": "com.jeestudio.bpm.common.entity.system.User",
            "jdbcType": "varchar(2000)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgRydx
    },
    {
        type: 'officeSelect',
        key: officeSelectFormItemType,
        text: '部门选择',
        dataJson: {
            "showType": "officeselectTree",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "部门选择",
            "comments_EN": "",
            "remarks": "",
            "javaField": "office",
            "friendlyJdbcType": "文本",
            "javaType": "com.jeestudio.bpm.common.entity.system.Office",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgBmdx
    },
    {
        type: 'area',
        key: areaFormItemType,
        text: '区域选择',
        dataJson: {
            "showType": "areaselect",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "区域选择",
            "comments_EN": "",
            "remarks": "",
            "javaField": "area",
            "friendlyJdbcType": "文本",
            "javaType": "com.jeestudio.bpm.common.entity.system.Area",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgQy
    },
    {
        type: 'treeSelect',
        key: treeSelectFormItemType,
        text: '树选择',
        dataJson: {
            "showType": "treeselect",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "树选择",
            "comments_EN": "",
            "remarks": "",
            "javaField": "t",
            "friendlyJdbcType": "文本",
            "javaType": "com.jeestudio.bpm.common.entity.common.Zform",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "",
            "tableName": ""
        },
        img:imgS
    },
    {
        type: 'modalSelect',
        key: modalSelectFormItemType,
        text: '自定义选择',
        dataJson: {
            "showType": "gridselect",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "自定义选择",
            "comments_EN": "",
            "remarks": "",
            "javaField": "g",
            "friendlyJdbcType": "文本",
            "javaType": "com.jeestudio.bpm.common.entity.common.Zform",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "",
            "tableName": "",
            "fieldLabels": "",
            "fieldKeys": "",
            "searchLabel": "",
            "searchKey": "",
            "selectSimple": "0",
            "selectValuefield": "id",
            "selectDsf": "",
            "selectOrder": ""
        },
        img:imgZdy
    },
    {
        type: 'modalMultiSelect',
        key: modalMultiSelectFormItemType,
        text: '弹出多选',
        dataJson: {
            "required": "false",
            "label": "多选",
            "relTable": "",
            "relColumn": "",
            "relManyColumn": "",
            "formNo": "",
            "searchKey": "",
            "searchLabel": "",
            "modalWidth": "1200",
            "dsfPlus": "",
            "codeFiled": "",
            "codeFiledTitle": "",
            "codeFiledAlign": "",
            "nameFiled": "",
            "nameFiledTitle": "",
            "nameFiledAlign": "",
            "javaField": "dictViewList",
        },
        img:imgTcck
    },
    {
        type: 'treeSelect',
        key: parentFormItemType,
        text: '上级',
        dataJson: {
            "showType": "parentId",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "上级",
            "comments_EN": "",
            "remarks": "",
            "javaField": "parent.id|name",
            "friendlyJdbcType": "文本",
            "javaType": "This",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "parent_id"
        },
        img:imgZsj
    },
    {
        type: 'input',
        key: sortInputFormItemType,
        text: '排序',
        dataJson: {
            "showType": "sortInput",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "排序",
            "comments_EN": "",
            "remarks": "",
            "javaField": "sort",
            "maxLength": "",
            "minLength": "",
            "minValue": "",
            "maxValue": "",
            "friendlyJdbcType": "排序",
            "javaType": "Integer",
            "jdbcType": "integer",
            "queryType": "=",
            "validateType": "digits",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "searchSort": 0,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "sort"
        },
        img:imgPx
    },
    {
        type: 'upload',
        key: uploadFormItemType,
        text: '附件',
        dataJson: {
            "showType": "fileupload",
            "isOneLine": 1,
            "isNull": 1,
            "comments": "普通附件",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "0",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgFj
    },
    {
        type: 'upload',
        key: uploadPicFormItemType,
        text: '图片附件',
        dataJson: {
            "showType": "fileuploadpic",
            "isOneLine": 1,
            "isNull": 1,
            "comments": "图片附件",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "0",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgTpfj
    },
    {
        type: 'upload',
        key: uploadOnlineFormItemType,
        text: '在线文档',
        dataJson: {
            "showType": "fileuploadonline",
            "isOneLine": 1,
            "isNull": 1,
            "comments": "在线文档",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "0",
            "formSort": 1,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgWord
    },
    // {
    //     type: 'lngLatSelect',
    //     key: lngLatSelectFormItemType,
    //     text: '经纬度选择',
    //     img:require('@/assets/img/jwd.png')
    // },
    {
        type: 'iconSelect',
        key: iconSelectFormItemType,
        text: '图标选择',
        dataJson: {
            "showType": "iconselect",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "图标选择",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(64)",
            "queryType": "=",
            "validateType": "",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "searchSort": 0,
            "isReadonly": 0,
            "defaultValue": "",
            "name": ""
        },
        img:imgTb
    },
    {
        type: 'richText',
        key: richTextFormItemType,
        text: '富文本',
        dataJson: {
            "showType": "richText",
            "isOneLine": '1',
            "isNull": 1,
            "comments": "富文本",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "maxLength": "255",
            "modalWidth": "1200",
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(2000)",
            "queryType": "=",
            "isForm": 1,
            "isQuery": 0,
            "isList": "0",
            "formSort": 1,
            "searchSort": 0,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "",
            "blockChainParam1": "0",
            "blockChainParam2": "0",
            "blockChainParam3": "0"
        },
        img:imgDdhwb
    },
    {
        type: 'serialNo',
        key: serialNoFormItemType,
        text: '编号',
        dataJson: {
            "showType": "serialNo",
            "isOneLine": 0,
            "isNull": 1,
            "comments": "编号",
            "comments_EN": "",
            "remarks": "",
            "javaField": "s",
            "maxLength": "64",
            "friendlyJdbcType": "文本",
            "javaType": "String",
            "jdbcType": "varchar(64)",
            "queryType": "like",
            "validateType": "",
            "isForm": 1,
            "isQuery": 0,
            "isList": "1",
            "formSort": 1,
            "searchSort": 0,
            "isReadonly": 0,
            "defaultValue": "",
            "name": "",
            "blockChainParam1": "0",
            "blockChainParam2": "0",
            "blockChainParam3": "0"
        },
        img:imgZs
    },
]

export const defaultFieldArray = [
    {
        "showType": "treeselectRedio",
        "isOneLine": 0,
        "isNull": 1,
        "comments": "创建者",
        "javaField": "createBy.id",
        "maxLength": "",
        "minLength": "",
        "minValue": "",
        "maxValue": "",
        "friendlyJdbcType": "文本",
        "javaType": "String",
        "jdbcType": "varchar(64)",
        "queryType": "=",
        "validateType": "",
        "isForm": 0,
        "isQuery": 0,
        "isList": 0,
        "formSort": 100,
        "searchSort": 100,
        "isReadonly": 0,
        "defaultValue": "",
        "name": "create_by",
        "listSort": 0,
        "comments_EN": "Creat by",
        "remarks": ""
    },
    {
        "showType": "dateselect",
        "isOneLine": 0,
        "isNull": 1,
        "comments": "创建时间",
        "javaField": "createDate",
        "friendlyJdbcType": "日期",
        "javaType": "java.util.Date",
        "dateType": "yyyy-MM-dd",
        "jdbcType": "datetime",
        "queryType": "=",
        "validateType": "",
        "isForm": 0,
        "isQuery": 0,
        "isList": 0,
        "formSort": 100,
        "searchSort": 100,
        "isReadonly": 0,
        "defaultValue": "",
        "name": "create_date",
        "listSort": 1,
        "comments_EN": "Creat date",
        "remarks": ""
    },
    {
        "showType": "treeselectRedio",
        "isOneLine": 0,
        "isNull": 1,
        "comments": "更新者",
        "javaField": "updateBy.id",
        "maxLength": "",
        "minLength": "",
        "minValue": "",
        "maxValue": "",
        "friendlyJdbcType": "文本",
        "javaType": "String",
        "jdbcType": "varchar(64)",
        "queryType": "=",
        "validateType": "",
        "isForm": 0,
        "isQuery": 0,
        "isList": 0,
        "formSort": 100,
        "searchSort": 100,
        "isReadonly": 0,
        "defaultValue": "",
        "name": "update_by",
        "listSort": 2,
        "comments_EN": "Update by",
        "remarks": ""
    },
    {
        "showType": "dateselect",
        "isOneLine": 0,
        "isNull": 1,
        "comments": "更新时间",
        "javaField": "updateDate",
        "friendlyJdbcType": "日期",
        "javaType": "java.util.Date",
        "dateType": "yyyy-MM-dd",
        "jdbcType": "datetime",
        "queryType": "=",
        "validateType": "",
        "isForm": 0,
        "isQuery": 0,
        "isList": 0,
        "formSort": 100,
        "searchSort": 100,
        "isReadonly": 0,
        "defaultValue": "",
        "name": "update_date",
        "listSort": 3,
        "comments_EN": "Update date",
        "remarks": ""
    },
    {
        "showType": "input",
        "isOneLine": 0,
        "isNull": 1,
        "comments": "备注信息",
        "javaField": "remarks",
        "maxLength": "",
        "minLength": "",
        "minValue": "",
        "maxValue": "",
        "friendlyJdbcType": "文本",
        "javaType": "String",
        "jdbcType": "varchar(255)",
        "queryType": "=",
        "validateType": "",
        "isForm": 0,
        "isQuery": 0,
        "isList": 0,
        "formSort": 100,
        "searchSort": 100,
        "isReadonly": 0,
        "defaultValue": "",
        "name": "remarks",
        "listSort": 4,
        "comments_EN": "Remarks",
        "remarks": ""
    },
    {
        "showType": "input",
        "isOneLine": 0,
        "isNull": 1,
        "comments": "归属机构编码",
        "javaField": "ownerCode",
        "maxLength": "",
        "minLength": "",
        "minValue": "",
        "maxValue": "",
        "friendlyJdbcType": "文本",
        "javaType": "String",
        "jdbcType": "varchar(255)",
        "queryType": "=",
        "validateType": "",
        "isForm": 0,
        "isQuery": 0,
        "isList": 0,
        "formSort": 100,
        "searchSort": 100,
        "isReadonly": 0,
        "defaultValue": "",
        "name": "owner_code",
        "listSort": 5,
        "comments_EN": "Owner code",
        "remarks": ""
    }
]
export const initDynamicFormItemConfig = (table, item, displayFormItemArr = [], hideFormItemArr = []) => {
    const getNameString = (k, length, start = 1) => {
        let arr = [];
        for (let i = start; i < (start + length); i++) {
            let index = '';
            if (i < 10) {
                index = "0" + i;
            } else {
                index = i;
            }
            arr.push(k + index);
        }
        return arr;
    }
    const getNameObj = (k, length) => {
        let arr = [];
        for (let i = 1; i < length + 1; i++) {
            let index = '';
            if (i < 10) {
                index = "0" + i;
            } else {
                index = i;
            }
            arr.push(k + index + ".id|name");
        }
        return arr;
    }

    const getNameGrid = (k, length) => {
        let arr = [];
        for (let i = 1; i < length + 1; i++) {
            let index = '';
            if (i < 10) {
                index = "0" + i;
            } else {
                index = i;
            }
            arr.push(k + index + ".id|s01");
        }
        return arr;
    }
    let json = {};
    if (!item.dataJson) {
        Modal.warning({
            title: '提示',
            content: '请先配置表单项'
        })
    }
    Object.assign(json, item.dataJson);
    //获取javaField
    let arr = [];
    let reg = "";
    if (json.javaField === "s") {
        arr = getNameString(json.javaField, 600);
        reg = "/^" + json.javaField + "((0[1-9]{1})|([1-9]{1}[0-9]{1})|([1-5]{1}[0-9]{1}[0-9]{1}))|(600)$/";
    } else if (json.javaField === "c") {
        arr = getNameString(json.javaField, 10);
        reg = "/^" + json.javaField + "((0[1-9]{1})|(10))$/";
    } else if (json.javaField === "d") {
        arr = getNameString(json.javaField, 9)
            .concat(getNameString(json.javaField, 9, 11))
            .concat(getNameString(json.javaField, 9, 21))
            .concat(getNameString(json.javaField, 9, 31))
            .concat(getNameString(json.javaField, 9, 41))
            .concat(getNameString(json.javaField, 9, 51));
        reg = "/^" + json.javaField + "(([0-5]{1}[1-9]{1})|)$/";
    } else if (json.javaField === "user") {
        arr = getNameObj(json.javaField, 15);
        reg = "/^" + json.javaField + "((0[1-9]{1})|([1-1]{1}[0-5]{1})|(15)).id|name$/";
    } else if (json.javaField === "users" || json.javaField === "office" || json.javaField === "area") {
        arr = getNameObj(json.javaField, 5);
        reg = "/^" + json.javaField + "(0[1-5]{1}).id|name$/";
    } else if (json.javaField === "g") {
        arr = getNameGrid(json.javaField, 9);
        reg = "/^" + json.javaField + "(0[1-9]{1})/";
    } else if (json.javaField === "t") {
        //fixme zform 里面有20个
        arr = getNameObj(json.javaField, 5);
        reg = "/^" + json.javaField + "(0[1-5]{1})/";
    } else if (json.javaField === "parent.id|name") {
        arr = ["parent.id|name"];
        reg = "/^parent.id|name$/";
    } else if (json.javaField === "sort") {
        arr = ["sort"];
        reg = "/^sort$/";
    } else if (json.javaField === "i") {
        arr = getNameString(json.javaField, 20);
        reg = "/^" + json.javaField + "((0[1-9]{1})|(1[0-9]{1})|(20))$/";
    } else if (json.javaField === "b") {
        arr = getNameString(json.javaField, 20);
        reg = "/^" + json.javaField + "((0[1-9]{1})|(1[0-9]{1})|(20))$/";
    } else if (json.javaField === "f") {
        arr = getNameString(json.javaField, 20);
        reg = "/^" + json.javaField + "((0[1-9]{1})|(1[0-9]{1})|(20))$/";
    } else if (json.javaField === "dictViewList") {
        arr = getNameString(json.javaField, 5);
        reg = "/^" + json.javaField + "(0[1-5]{1})/";
    }
    reg = eval(reg);

    displayFormItemArr.concat(hideFormItemArr).forEach(formItem => {
        if (formItem.type === 'modalMultiSelect') {
            if (reg.test(formItem.formItemProps.name)) {
                arr = arr.filter((item) => item !== formItem.formItemProps.name);
            }
        } else {
            let data = formItem.column;
            if (reg.test(data.javaField)) {
                arr = arr.filter((item) => item !== data.javaField);
            }
        }

    })
    let length = displayFormItemArr.length + hideFormItemArr.length;
    if (arr.length > 0) {
        if (json.javaField === "dictViewList") {
            json.name = arr[0];
        } else {
            json.javaField = arr[0];
            if (!json.name) {
                json.name = arr[0];
            }
            json.isForm = 1;
            json.isQuery = 0;
            json.formSort = 0;
            json.searchSort = 0;
            json.listSort = length++;
            json.javaFieldReplace = json.javaField.replace(/\|/g, '-').replace(/\./g, '-');
            if (!json.comments_EN) {
                json.comments_EN = json.comments;
            }
        }

    } else {
        Modal.error({
            title: '提示',
            content: '表单项已经用完了'
        })
        throw new Error("表单项已经用完了");
    }
    //todo 初始化表单项

    if (json.javaField === "dictViewList") {
        return transformDictViewListToDynamicFormItemConfig(table, json);
    }
    return transformGenTableColumnToDynamicFormItemConfig(table, json);
}

export const initDynamicFormItemFormControlProps = (config, type) => {
    if (type === 'input') {
        config.formControlProps = new InputProps()
    } else if (type === 'select') {
        config.formControlProps = new SelectProps()
    } else if (type === 'date') {
        config.formControlProps = new DateProps()
    } else if (type === 'area') {
        config.formControlProps = new AreaProps()
    } else if (type === 'upload') {
        config.formControlProps = new UploadProps()
    } else if (type === 'treeSelect') {
        config.formControlProps = new TreeSelectProps()
    } else if (type === 'modalSelect') {
        config.formControlProps = new ModalSelectProps()
    } else if (type === 'userSelect') {
        config.formControlProps = new UserSelectProps()
    } else if (type === 'usersSelect') {
        config.formControlProps = new UsersSelectProps()
    } else if (type === 'officeSelect') {
        config.formControlProps = new OfficeSelectProps()
    } else if (type === 'modalMultiSelect') {
        config.formControlProps = new ModalMultiSelectProps()
    }else if (type === 'richText') {
        config.formControlProps = new RichTextProps()
    }else if (type === 'serialNo') {
        config.formControlProps = new SerialNoProps()
    }
    return config
}
/**
 * 将GenTableColumn转换为DynamicFormItemConfig
 * @param table 表配置
 * @param column 列配置
 * @return {DynamicFormItemConfig}
 */
export const transformGenTableColumnToDynamicFormItemConfig = (table, column) => {
    let config = new DynamicFormItemConfig(column);
    config.settings = column.settings//表单项配置

    let num = column.jdbcType.indexOf("varchar") > -1 ? (column.jdbcType.match(/\d+/g) ? column.jdbcType.match(/\d+/g).join("") : 64) : "";
    if (num) {
        column.varcharLength = num
        column.jdbcTypeReplace = column.jdbcType.replace(/\(/g, '').replace(/\)/g, '').replace(/\d+/g, '');
    } else if (column.jdbcType.indexOf("decimal") > -1) {
        let decimalMatch = column.jdbcType.match(/decimal\((\d+),(\d+)\)/i);
        column.decimalPrecision = decimalMatch ? decimalMatch[1] : '18';
        column.decimalScale = decimalMatch ? decimalMatch[2] : '4';
        column.jdbcTypeReplace = 'decimal';
    } else {
        column.jdbcTypeReplace = column.jdbcType
    }
    if (isEmpty(column.selectSimple)) {
        column.selectSimple = ''
    }

    config.colProps.span = column.isOneLine === '1' ? 24 : 12;
    let formItemConfig = {};
    if (column.formItemConfig) {
        formItemConfig = JSON.parse(column.formItemConfig);
        if (formItemConfig.colProps) {
            config.colProps.span = formItemConfig.colProps.span;
        }
    }
    let jsonConfigArr = [];

    let pleaseInput = '请输入' + column.comments;

    let showType = column.showType;
    if (showType === 'input' || showType === 'sortInput') {
        config.type = 'input';
        config.formControlProps = new InputProps();
        //输入框
        if (column.validateType === 'number' || column.validateType === 'digits') {
            config.formControlProps.type = column.validateType;
            jsonConfigArr = ['min', 'max']
            if (showType === 'sortInput'){
                config.key = sortInputFormItemType;
            }else if (column.validateType === 'number') {
                config.key = numberInputFormItemType;
            } else {
                config.key = digitsInputFormItemType;
            }
        } else {
            config.key = textInputFormItemType;
        }
        if (!column.validateType) {
            config.formControlProps.maxlength = normalizeTextInputMaxlength(column);
        }
        config.formItemProps.validateType = column.validateType;
    } else if (showType === 'textarea') {
        config.type = 'input';
        config.key = textareaInputFormItemType;
        config.formControlProps = new InputProps();
        config.formControlProps.textarea = true;
        config.formControlProps.maxlength = column.maxLength;
    } else if (showType === 'select' || showType === 'checkbox' || showType === 'radiobox') {
        pleaseInput = '请选择' + column.comments;
        config.formItemProps.pleasePrefix = '请选择'
        config.type = 'select';
        config.key = selectFormItemType;
        config.formControlProps = new SelectProps();
        if (column.selectSimple === '0') {
            if (formItemConfig?.formControlProps?.dataUrl) {
                config.formControlProps.type = 'url';
            }else{
                config.formControlProps.type = 'table';
                config.formControlProps.dictType = column.tableName;
            }
            config.formControlProps.valueField = column.selectValuefield;
            config.formControlProps.textField = column.searchKey;
            config.formControlProps.tableOrderBy = column.selectOrder;
            config.formControlProps.tableFilterData = []
        } else {
            config.formControlProps.dictType = column.dictType;
        }

        if (showType === 'checkbox') {
            config.key = checkboxFormItemType;
            config.formControlProps.formType = 'checkbox';
            config.formControlProps.multiple = true;
        } else if (showType === 'radiobox') {
            config.key = radioFormItemType;
            config.formControlProps.formType = 'radio';
        }

        jsonConfigArr = ['formatFuncStr', 'tableFilterData', 'multiple', 'dataUrl', 'postData']

    } else if (showType === 'dateselect') {
        pleaseInput = '请选择' + column.comments;
        config.formItemProps.pleasePrefix = '请选择'
        config.type = 'date';
        config.key = dateSelectFormItemType;
        config.formControlProps = new DateProps();
        config.formControlProps.formatPatter = column.dateType;
        jsonConfigArr = ['minValue', 'maxValue']
    } else if (showType === 'areaselect') {
        pleaseInput = '请选择' + column.comments;
        config.formItemProps.pleasePrefix = '请选择'
        config.type = 'area';
        config.key = areaFormItemType;
        config.formControlProps = new AreaProps();
        jsonConfigArr = ['freeChoice', 'showRank', 'rootAreaId']
    } else if (showType === 'fileupload' || showType === 'fileuploadpic' || showType === 'fileuploadonline') {
        pleaseInput = '请上传' + column.comments;
        config.formItemProps.pleasePrefix = '请上传'
        config.type = 'upload';
        config.key = uploadFormItemType;
        config.formControlProps = new UploadProps();
        if (showType === 'fileuploadpic') {
            config.key = uploadPicFormItemType;
            config.formControlProps.picture = true;
        } else if (showType === 'fileuploadonline') {
            config.key = uploadOnlineFormItemType;
            config.formControlProps.online = true;
        }
        jsonConfigArr = ['fileCount', 'maxSize', 'directory', 'multiple', 'acceptsStr']
    } else if (showType === 'treeselect' || showType === 'parentId') {
        pleaseInput = '请选择' + column.comments;
        config.formItemProps.pleasePrefix = '请选择'
        config.type = 'treeSelect';
        config.key = showType === 'treeselect' ? treeSelectFormItemType : parentFormItemType;
        config.formControlProps = new TreeSelectProps();
        //TODO 可能有问题
        config.formControlProps.formNo = table.tableType === "4" ? table.parentTable : table.name;
    } else if (showType === 'gridselect') {
        pleaseInput = '请选择' + column.comments;
        config.formItemProps.pleasePrefix = '请选择'
        config.type = 'modalSelect';
        config.key = modalSelectFormItemType;
        config.formControlProps = new ModalSelectProps();

        config.formControlProps.formNo = column.tableName
        config.formControlProps.modalWidth = 1200
        let fieldLabels = column.fieldLabels;
        let fieldKeys = column.fieldKeys;
        if (fieldLabels && fieldKeys) {
            let columns = []
            let labels = fieldLabels.split(',');
            let keys = fieldKeys.split(',');
            for (let i = 0; i < labels.length; i++) {
                columns.push({
                    title: labels[i],
                    dataIndex: keys[i],
                    align: 'left',
                })
            }
            config.formControlProps.allColumns = columns
            if (formItemConfig.formControlProps&&formItemConfig.formControlProps.allColumns) {
                //如果已经配置了allColumns，就读取
                config.formControlProps.allColumns = formItemConfig.formControlProps.allColumns;
            } else {
                config.formControlProps.searchKey = column.searchKey ? column.searchKey.split(',') : ''
                config.formControlProps.searchLabel = column.searchLabel ? column.searchLabel.split(',') : ''
            }
        }

        jsonConfigArr = ['targetTable', 'targetFormNo', 'targetField', 'targetFilterData', 'searchLabelWidth', 'searchConfig', 'nameDataIndex', 'modalWidth', 'modalTitle', 'filterData', 'formUpdateMap']

    } else if (showType === 'treeselectRedio') {
        //用户单选
        pleaseInput = '请选择' + column.comments;
        config.formItemProps.pleasePrefix = '请选择'
        config.type = 'userSelect';
        config.key = userSelectFormItemType;
        config.formControlProps = new UserSelectProps();
        jsonConfigArr = ['modalWidth', 'dataScope', 'targetOrgId', 'hideLoginName', 'createSysUser', 'loginNameField', 'userNameField', 'parentOrgField', 'userRoles']
    } else if (showType === 'treeselectCheck') {
        //用户多选
        pleaseInput = '请选择' + column.comments;
        config.formItemProps.pleasePrefix = '请选择'
        config.type = 'usersSelect';
        config.key = usersSelectFormItemType;
        config.formControlProps = new UsersSelectProps();
        jsonConfigArr = ['modalWidth', 'dataScope', 'targetOrgId']
    } else if (showType === 'officeselectTree') {
        //机构 部门选择
        pleaseInput = '请选择' + column.comments;
        config.formItemProps.pleasePrefix = '请选择'
        config.type = 'officeSelect';
        config.key = officeSelectFormItemType;
        config.formControlProps = new OfficeSelectProps();
        jsonConfigArr = ['createSysOffice', 'parentOrgId', 'orgNameField', 'areaIdField']
        //todo
    } else if (showType === 'iconselect') {
        //图标选择
        pleaseInput = '请选择' + column.comments;
        config.formItemProps.pleasePrefix = '请选择'
        config.type = 'iconSelect';
        config.key = iconSelectFormItemType;
        config.formControlProps = new IconSelectProps();
        jsonConfigArr = ['showInput']
    }else if (showType === 'richText') {
        config.type = 'richText';
        config.key = richTextFormItemType;
        config.formControlProps = new InputProps();
        config.formControlProps.textarea = true;
        config.formControlProps.maxlength = column.maxLength;
    } else if (showType === 'serialNo') {
        //编号生成
        config.type = 'serialNo';
        config.key = serialNoFormItemType;
        config.formControlProps = new SerialNoProps();
        jsonConfigArr = ['prefix']
    } else if (showType === 'switch') {
        config.type = 'switch';
        config.key = switchFormItemType;
    }

    //如果已经配置了，就读取并设置到formControlProps
    formItemConfig.formControlProps && jsonConfigArr.forEach(item => {

        if (formItemConfig.formControlProps[item]) {
            config.formControlProps[item] = formItemConfig.formControlProps[item]
        }
    })

    config.formControlProps.placeholder = '';
    if (formItemConfig.formControlProps && formItemConfig.formControlProps.placeholder) {
        config.formControlProps.extra = formItemConfig.formControlProps.placeholder;
    }
    config.formControlProps.defaultValue = column.defaultValue;
    config.formControlProps.disabled = column.isReadonly;

    config.formItemProps.name = column.name;
    if (showType === 'parentId') {
        config.formItemProps.name = 'parent';
    }
    config.formItemProps.label = column.comments;
    config.formItemProps.rules = column.isNull === '0' ? [{required: true, message: pleaseInput}] : [];
    config.formItemProps.required = column.isNull === '0' ? '1' : '0'
    if (formItemConfig.formItemProps && formItemConfig.formItemProps.extra) {
        config.formItemProps.extra = formItemConfig.formItemProps.extra;
    }
    if (formItemConfig.formItemProps && formItemConfig.formItemProps.renderPredicate) {
        config.formItemProps.renderPredicate = formItemConfig.formItemProps.renderPredicate;
    }
    if (column.remarks) {
        // 检查 unique 标志
        if (column.remarks.includes('unique')) {
            config.formItemProps.unique = '1';
        }
        // 提取 encrypt 类型
        // 兼容解析 'encrypt_' 标志，适配无分隔符和分隔符格式
        const encryptMatch = column.remarks.match(/encrypt_(\w+)/);
        if (encryptMatch) {
            config.formItemProps.encryptType = encryptMatch[1]; // 提取类型值
        }
    }
    config.apply()
    return config;
}

export const transformDictViewListToDynamicFormItemConfig = (table, item) => {
    let config = new DynamicFormItemConfig();
    config.colProps.span = 24;
    let formItemConfig = {};
    if (item.formItemConfig) {
        formItemConfig = JSON.parse(item.formItemConfig);
        if (formItemConfig.colProps) {
            config.colProps.span = formItemConfig.colProps.span;
        }
    }
    config.formItemProps.pleasePrefix = '请选择'
    config.type = 'modalMultiSelect';
    config.key = modalMultiSelectFormItemType;
    config.formControlProps = new ModalMultiSelectProps();
    config.formControlProps.searchKey = item.searchKey ? item.searchKey.split(',') : ''
    config.formControlProps.searchLabel = item.searchLabel ? item.searchLabel.split(',') : ''
    config.formControlProps.modalWidth = item.modalWidth ? Number(item.modalWidth) : 1200
    config.formControlProps.formNo = item.formNo

    config.formControlProps.allColumns = [
        {title: `${item.codeFiledTitle}`, dataIndex: `${item.codeFiled}`, align: `${item.codeFiledAlign}`},
        {title: `${item.nameFiledTitle}`, dataIndex: `${item.nameFiled}`, align: `${item.nameFiledAlign}`},
    ]
    if (formItemConfig.formControlProps && formItemConfig.formControlProps.allColumns) {
        //如果已经配置了allColumns，就读取
        config.formControlProps.allColumns = formItemConfig.formControlProps.allColumns;
    }
    config.codeFiled = item.codeFiled
    config.nameFiled = item.nameFiled

    config.formItemProps.name = item.name;
    config.formItemProps.label = item.label;
    config.formItemProps.required = item.required === 'true' ? '1' : '0'

    let jsonConfigArr = ['targetTable', 'targetFormNo', 'targetField', 'targetFilterData', 'searchLabelWidth', 'searchConfig', 'modalWidth', 'modalTitle', 'filterData', 'formatFuncStr', 'genTableSql']
    //如果已经配置了，就读取并设置到formControlProps
    formItemConfig.formControlProps && jsonConfigArr.forEach(item => {
        if (formItemConfig.formControlProps[item]) {
            config.formControlProps[item] = formItemConfig.formControlProps[item]
        }
    })

    config.column = {}
    config.column.label = item.label
    config.column.relTable = item.relTable
    config.column.relColumn = item.relColumn
    config.column.relManyColumn = item.relManyColumn
    config.column.formSort = item.formSort//modalMultiSelect 支持formSort

    config.apply()

    return config;
}

/**
 * 动态表单配置转换为动态表格列配置
 * @param config
 */
export const transformDynamicFormItemConfigToGenTableColumn = (config) => {
    let column = config.column
    column.settings = config.settings
    let newConfig = {}
    Object.assign(newConfig, config)
    delete newConfig.column

    column.formItemConfig = JSON.stringify(newConfig)

    column.jdbcType = column.jdbcTypeReplace
    if (column.jdbcType === 'varchar') {
        column.jdbcType = `varchar(${column.varcharLength})`
    } else if (column.jdbcType === 'decimal') {
        column.jdbcType = `decimal(${column.decimalPrecision || 18},${column.decimalScale || 4})`
    }
    let showType = column.showType;
    if (showType === 'input' || showType === 'sortInput') {
        if (!column.validateType) {
            column.maxLength = config.formControlProps.maxlength;
        }
        column.validateType = config.formItemProps.validateType;
    } else if (showType === 'textarea') {
        column.maxLength = config.formControlProps.maxlength;
    } else if (showType === 'select' || showType === 'checkbox' || showType === 'radiobox') {
        if (column.selectSimple === '0') {
            column.tableName = config.formControlProps.dictType;
            column.selectValuefield = config.formControlProps.valueField;
            column.searchKey = config.formControlProps.textField;
            column.selectOrder = config.formControlProps.tableOrderBy;
        } else {
            column.dictType = config.formControlProps.dictType;
            // 同步更新 listConfig 中的 dict 属性，确保列表页能正确显示字典翻译
            if (column.dictType) {
                let listConfig = column.listConfig ? JSON.parse(column.listConfig) : {};
                listConfig.dict = column.dictType;
                column.listConfig = JSON.stringify(listConfig);
            }
        }
    } else if (showType === 'dateselect') {
        column.dateType = config.formControlProps.formatPatter;
    } else if (showType === 'gridselect' || column.relTable) {
        if (showType === 'gridselect'){
            column.tableName = config.formControlProps.formNo
        }else{
            column.formNo = config.formControlProps.formNo
            column.modalWidth = config.formControlProps.modalWidth
            column.label = config.formItemProps.label
            column.required = config.formItemProps.required === '1' ? 'true' : 'false'
        }
        let allColumns = config.formControlProps.allColumns
        let fieldLabels = [];
        let fieldKeys = [];
        let searchList = []
        let codeFiled = []
        let nameFiled = []
        allColumns.forEach((item) => {
            if (item.isShow === '1') {
                fieldLabels.push(item.title)
                fieldKeys.push(item.dataIndex)
            }

            if (item.isQuery === '1') {
                searchList.push({
                    searchKey: item.queryDataIndex || item.dataIndex,
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
        if (searchList.length > 0) {
            column.searchKey = searchList[0].searchKey
            column.searchLabel = searchList[0].searchLabel
        }
        if (showType === 'gridselect'){
            column.fieldLabels = fieldLabels.join(',')
            column.fieldKeys = fieldKeys.join(',')
        }

        if (column.relTable){
            if (codeFiled.length === 0){
                if (allColumns.length > 0){
                    codeFiled.push(allColumns[0])
                }
            }
            if (nameFiled.length === 0){
                if (allColumns.length > 0){
                    nameFiled.push(allColumns[0])
                }
            }
            column.codeFiled = codeFiled.length > 0 ? codeFiled[0].dataIndex : ''
            column.codeFiledTitle = codeFiled.length > 0 ? codeFiled[0].title : ''
            column.codeFiledAlign = codeFiled.length > 0 ? codeFiled[0].align : ''

            column.nameFiled = nameFiled.length > 0 ? nameFiled[0].dataIndex : ''
            column.nameFiledTitle = nameFiled.length > 0 ? nameFiled[0].title : ''
            column.nameFiledAlign = nameFiled.length > 0 ? nameFiled[0].align : ''

            column.genTableSql = config.formControlProps.genTableSql
        }

    }

    column.defaultValue = config.formControlProps.defaultValue;
    column.isReadonly = config.formControlProps.disabled;

    column.name = config.formItemProps.name;
    if ( config.formItemProps.name === 'parent'){
        column.name = 'parent_id'
    }
    column.comments = config.formItemProps.label
    column.isNull = config.formItemProps.required === '0' ? '1' : '0'

    const addRemark = (key, value = true) => {
        const remark = value === true ? key : `${key}_${value}`;
        column.remarks = (column.remarks ?? "") + remark + ";";
    };
    column.remarks = "";
    if (config.formItemProps.encryptType) {
        addRemark("encrypt", config.formItemProps.encryptType);
    }
    if (config.formItemProps.unique === '1') {
        addRemark("unique");
    }
    return column
}

export const validateTypeConfig = {
    email: '请输入电子邮件',
    number: '请输入整数',
    digits: '请输入数字',
    mobile: '请输入手机号码',
    phone: '请输入电话号码',
    telephone: '请输入联系方式',
    zipCode: '请输入邮政编码',
    fax: '请输入传真号码',
    latitude: '请输入纬度',
    longitude: '请输入经度',
    idCard: '请输入身份证号',
}
