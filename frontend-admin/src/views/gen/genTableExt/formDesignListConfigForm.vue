<template>
  <u-form ref="listConfigForm" v-model:value="extendJsConfig" :label-width="130" setFormInjectKey="">
    <u-form-title class="first-form-title">
      基本配置
      <a-button type="link" @click="editConfigJson">编辑配置json</a-button>
      <u-modal ref="configModal" :width="1000">
        <u-json v-model:value="customSingleTableViewProps"/>
      </u-modal>
    </u-form-title>
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="" label="加载查询条件" extra="是否根据查询条件默认值加载">
          <u-select form-type="radio" v-model:value="customSingleTableViewProps.firstLoadDataWithDefaultQueryField" placeholder="" value-field="value" text-field="text" :option-data="booleanOptionData"/>
        </a-form-item>
      </a-col>
    </a-row>
    <u-form-title class="first-form-title">查询区域</u-form-title>
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item label="标签宽度">
          <u-input type="number" :min="0" :step="10" v-model:value="customSingleTableViewProps.queryArea.width"/>
        </a-form-item>
      </a-col>
    </a-row>
    <u-form-title class="first-form-title">操作区域</u-form-title>
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="noOptButton" label="无按钮">
          <u-select form-type="radio" v-model:value="extendJsConfig.noOptButton" placeholder="" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="batchSelect" label="批量选择">
          <u-select form-type="radio" v-model:value="extendJsConfig.batchSelect" @change="changeBatchSelect" placeholder="" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item label="按钮">
          <u-select @change="changeOptButton" v-model:value="selectOptButton" :multiple="true" formType="checkbox" valueField="value" textField="text" :optionData="optButtonArr"/>
        </a-form-item>
      </a-col>
    </a-row>
    <a-row :gutter="16">
      <a-col :span="10">
        <a-form-item name="list__buttons" label="操作区域按钮">
          <template #extra>
            支持disabledFilterStr <br>
            (rows) => {
            return true;//返回true禁用 false启用
            }<br>
            示例：return rows.filter(item=>item.status === '1').length===0
          </template>
          <u-json v-model:value="extendJsConfig.list__buttons"/>
        </a-form-item>
      </a-col>
      <a-col :span="14" v-bind:key="item" v-for="item in clickButtonEventArr">
        <a-form-item :name="'clickButton__'+item" :label="item">
          (value,item,option={tableViewRef,Modal,prompt,saveDataUrl,batchSaveSelectUrl,confirmAction,postAction,downLoadFileAction,encryptByDESModeEBC,UUID,dateFormat})=>{
          <u-code-mirror v-model:value="extendJsConfig['clickButton__'+item]"/>
          }
        </a-form-item>
      </a-col>
    </a-row>
    <u-form-title class="first-form-title">表格区域</u-form-title>
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="noCheckbox" label="无checkbox">
          <u-select form-type="radio" v-model:value="extendJsConfig.noCheckbox" placeholder="" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="noRowButton" label="无操作列">
          <u-select form-type="radio" v-model:value="extendJsConfig.noRowButton" placeholder="" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
        </a-form-item>
      </a-col>
    </a-row>
    <a-row :gutter="16">
      <a-col :span="10">
        <a-form-item name="singleTable__rowButtons" label="操作列">
          <template #extra>
            支持visibleFilterStr <br>
            (btn, row) => {
            return true;//返回true显示 false隐藏
            }<br>
            示例：return row.type_ === '1'
          </template>
          <u-json v-model:value="extendJsConfig.singleTable__rowButtons"/>
        </a-form-item>
      </a-col>
      <a-col :span="14" v-bind:key="item" v-for="item in clickRowEventArr">
        <a-form-item :name="'clickRow__'+item" :label="item">
          (rowObject={value, row, index},option={tableViewRef,Modal,prompt,saveDataUrl,batchSaveSelectUrl,confirmAction,postAction,downLoadFileAction,encryptByDESModeEBC,UUID,dateFormat})=>{
          <u-code-mirror v-model:value="extendJsConfig['clickRow__'+item]"/>
          }
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "formDesignListConfigForm"
}
</script>
<script setup>
import {ref, watch, computed} from "vue";
import UForm from "@/components/form/UForm";
import USelect from "@/components/form/USelect";
import UJson from "@/components/form/UJson";
import UCodeMirror from "@/components/form/code/UCodeMirror";
import {oneOf, replaceAll} from "@/lib/tools";
import {booleanOptionData, yesNoOptionData} from "@/views/gen/genOptionData";
import UFormTitle from "@/components/form/UFormTitle";
import UInput from "@/components/form/UInput";
import UModal from "@/components/modal/UModal";

let props = defineProps({
  tableName: {
    type: String,
    default: ''
  },
  value: {
    type: Object,
    default: () => {
      return {
        singleTable__rowButtons: [],
        list__buttons: [],
      }
    }
  },
  isSubTable: {
    type: Boolean,
    default: false
  },
  allColumns: {
    type: Array,
    default() {
      return []
    }
  },

})

let emits = defineEmits(["update:value"]);
let extendJsConfig = ref(props.value)
let selectOptButton = ref('')
const optButtonArr = [
  {value: 'import', text: '导入数据',},
  {value: 'export', text: '导出Excel',},
  {value: 'enable', text: '启用',},
  {value: 'disable', text: '禁用',},
]
watch(() => props.value, () => {
  let selectOptButtonArr = []
  props.value.list__buttons.forEach((item) => {
    if (oneOf(item.value, optButtonArr.map((item) => item.value))) {
      selectOptButtonArr.push(item.value)
    }
  })
  selectOptButton.value = selectOptButtonArr.join(',')
}, {immediate: true})
let clickRowEventArr = computed(() => {
  return extendJsConfig.value.singleTable__rowButtons.filter((item) => {
    return item.value !== 'view' && item.value !== 'edit' && item.value !== 'delete' && item.value !== 'addChild'
  }).map((item) => item.value)
})

watch(() => extendJsConfig.value, (value) => {
  emits("update:value", value)
})

const sortArr = ['batch-delete', 'export', 'import', 'add']
const permissionArr = ['del', 'export', 'import', 'add']

let clickButtonEventArr = computed(() => {
  return extendJsConfig.value.list__buttons.filter((item) => !oneOf(item.value, sortArr)&&'reloadTree'!==item.value).map((item) => item.value)
})
const changeOptButton = (val) => {
  let selectArr = []
  if (val) {
    selectArr = val.split(',')
  }
  let btnArr = extendJsConfig.value.list__buttons.map((item) => item.value);
  let arr = extendJsConfig.value.list__buttons.filter(item => {
    if (oneOf(item.value, optButtonArr.map(item => item.value))) {
      //如果是可以控制的按钮
      return oneOf(item.value, selectArr)
    } else {
      return true
    }
  })
  selectArr.forEach((item) => {
    if (!oneOf(item, btnArr)) {
      arr = arr.concat(optButtonArr.filter(opt => opt.value === item))
      //按照value add import export batch-delete 排序
      arr.sort((a, b) => {
        return sortArr.indexOf(b.value) - sortArr.indexOf(a.value)
      })
      arr.forEach((item, index) => {
        if (oneOf(item.value, sortArr) && !item.permission) {
          item.permission = props.isSubTable ? '' : `app:${props.tableName}:${permissionArr[sortArr.indexOf(item.value)]}`
        } else if (!item.permission) {
          item.permission = props.isSubTable ? '' : `app:${props.tableName}:${item.value}`
        }
        if (item.value === 'batch-delete' && !item.disabledFilterStr) {
          item.disabledFilterStr = `return rows.length===0`
        }
        if (item.value === 'enable' && !extendJsConfig.value['clickButton__enable']) {
          extendJsConfig.value['clickButton__enable'] = getBatchEventStr(item.text, '1')
          item.disabledFilterStr = `return rows.filter(item=>item.status === '0').length===0`
        }
        if (item.value === 'disable' && !extendJsConfig.value['clickButton__disable']) {
          extendJsConfig.value['clickButton__disable'] = getBatchEventStr(item.text, '0')
          item.disabledFilterStr = `return rows.filter(item=>item.status === '1').length===0`
        }
      })
    }
  })
  extendJsConfig.value.list__buttons = arr
  for (const valueKey in extendJsConfig.value) {
    if (valueKey.indexOf('clickButton__') >= 0 && extendJsConfig.value.list__buttons.filter(item => item.value === replaceAll(valueKey, 'clickButton__')).length === 0) {
      delete extendJsConfig.value[valueKey]
    }
  }
}

const getBatchEventStr = (optText, value) => {
  return `
   //TODO 修改字段名及${optText}状态的值
  let rows = option.tableViewRef.getSelectedRows().filter((item)=>item.status !== '${value}')
  if(rows.length === 0){
    option.Modal.warning({title: '提示',content: '请选择要操作的数据！',})
    return
  }
  let dataArr = rows.map((item)=>{
    return {
      id: item.id,
      //TODO 修改字段名及${optText}状态的值
      status: '${value}'
    }
  })
  option.confirmAction('操作确认', '是否${optText}选中的数据？', option.batchSaveSelectUrl+'?formNo=${props.tableName}', dataArr, () => {
    option.tableViewRef.saveSuccess()
  })
          `
}

const changeBatchSelect = (val) => {
  extendJsConfig.value.list__buttons.forEach((item) => {
    if (item.value === 'add' && val === '1') {
      let gridSelectArr = props.allColumns.filter(item => 'gridselect'===item.showType);
      item.text = '选择'
      item.batchSelect = {

      }
      if (gridSelectArr.length > 0 && gridSelectArr[0].formItemConfig) {
        let formControlProps = JSON.parse(gridSelectArr[0].formItemConfig).formControlProps
        item.batchSelect = {
          selectDataModalProps: {
            singleTable: {
              //pagination: false,
              showSorter: false,
              columns: formControlProps.props.columns
            },
            modalWidth: 1200,
            targetFilterData: [{key: 'targetTable.parent_id', value: '${parentId}', type: 'eq'}],
            filterData: formControlProps.props.filterData,
            fromFormNo: formControlProps.props.formNo,
            targetFormNo: formControlProps.props.targetFormNo,
            targetField: formControlProps.props.targetField
          },
          dataMapping: formControlProps.props.formUpdateMap,//生成多选时传入formUpdateMap
          queryFieldArr: formControlProps.allColumns.filter(item => item.isQuery === '1').map(item => {
            if (formControlProps.searchConfig[item.queryDataIndex || item.dataIndex]) {
              let _props = formControlProps.searchConfig[item.queryDataIndex || item.dataIndex].props
              Object.assign(_props, {placeholder: item.title})
              return {
                type: formControlProps.searchConfig[item.queryDataIndex || item.dataIndex].type,
                name: item.queryDataIndex || item.dataIndex,
                label: item.title,
                props: _props
              }
            }
            return {
              type: 'input',
              name: item.dataIndex,
              label: item.title,
              props: {placeholder: item.title}
            }
          }),
        }
      }

    }else if (item.value === 'add' && val === '0') {
      item.text = '添加'
      delete item.batchSelect
    }
  })
}

const configModal = ref()
let defaultProps = {
  queryArea: {}
}
let _customSingleTableViewProps = extendJsConfig.value.customSingleTableViewProps || {}
let _obj = Object.assign({}, defaultProps, _customSingleTableViewProps)
let customSingleTableViewProps = ref(_obj)
watch(() => customSingleTableViewProps.value, (value) => {
  extendJsConfig.value.customSingleTableViewProps = value
}, {deep: true})

const editConfigJson = () => {
  configModal.value.open('编辑配置json')
}
</script>
<style scoped>

</style>
