<template>
  <u-form v-model:value="formModel" v-model:actRuleArgs="actRuleArgs" v-model:disabled="disabled" :form-no="tableConfig.name" v-bind="formProps" :sub-tables="subTables">
    <a-row :gutter="16">
      <template v-bind:key="index"  v-for="(item,index) in formItemArr">
        <a-col v-if="isRender(item)"  v-bind="item.colProps.props" >
          <a-form-item v-bind="item.formItemProps.props"
                       :rules="buildRules(item).concat(item.formItemProps.unique==='1'?[ uniqueValidator(tableConfig.name,item.formItemProps.label,formModel.id)]:[])">
              <u-dynamic-form-item :form-modal="formModel" :disabled="item.formControlProps.disabled==='1'||disabled||isFormItemDisabled(actRuleArgs,item.formItemProps.name)"
                                   v-model:value="formModel[item.formItemProps.name]" v-model:formModel="formModel"
                                   :formType="item.type" :props="item.formControlProps.props" />
          </a-form-item>
        </a-col>
      </template>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "DynamicForm"
}
</script>
<script setup>
import {ref, watch, computed} from "vue";
import UForm from "@/components/form/UForm";
import {uniqueValidator} from "@/lib/validator";
import * as validator from "@/lib/validator";
import UDynamicFormItem from "@/components/form/UDynamicFormItem";
import {transformDictViewListToDynamicFormItemConfig} from "@/views/gen/dynamicFormItem";
import {postAction} from "@/api/action";
import {isNotEmpty, replaceDefaultValue} from "@/lib/tools";
import {calcPredicate} from "@/lib/tool-dynamic-form";
import {isFormItemShow, isFormItemDisabled, isFormItemRequire} from "@/lib/act/actForm";
let props = defineProps({
  extendFormData: {
    type: Object,
    default: () => {
      return {
        table: null,
        columns: [],
      }
    }
  },
})

let tableConfig = ref({
  name: '',
})
let formModel = ref({
})
let actRuleArgs = ref({})
let disabled = ref(false)
let formItemArr = ref([])

watch(() => props.extendFormData, (newVal) => {
  if (newVal.table && newVal.columns) {
    tableConfig.value = newVal.table
    let arr = []
    //过滤出隐藏域的字段
    let hideColumns = newVal.columns.filter(item => item.isForm === '0')
    hideColumns.forEach(hide=>{
      if (isNotEmpty(hide.defaultValue)){
        //设置默认值
        formModel.value[hide.name] = replaceDefaultValue(hide.defaultValue)
      }
    })
    let newColumns = newVal.columns.filter(item => item.isForm === '1')
    newColumns.sort((a, b) => Number(a.formSort) - Number(b.formSort))
    newColumns.forEach(col => {
      if (col.formItemConfig) {
        arr.push(JSON.parse(col.formItemConfig))
      }
    })
    if (tableConfig.value.extJava){
      let arr2 = JSON.parse(tableConfig.value.extJava)
      arr = arr.concat(arr2.map(item => transformDictViewListToDynamicFormItemConfig(tableConfig.value, item)))
    }
    formItemArr.value = arr

    if (newVal.table.children) {
      let promiseArr = []
      let titleArr = []
      let arr = []
      newVal.table.children.forEach((item) => {
        promiseArr.push(import(`./DynamicListView.vue`))
        promiseArr.push(postAction('gen/genTable/editForm', {id: item.id}))
        titleArr.push(item.title)
      })

      Promise.all(promiseArr).then(resArr => {
        titleArr.forEach((item, index) => {
          let table =  resArr[index * 2 + 1].data.genTable
          let tableSort = table.tableSort
          let data = {
            mainTableTitle: table.comments,
          }
          if (table.formProps){
            data =  JSON.parse(table.formProps)
          }
          arr.push({
            title: data.mainTableTitle,
            component: resArr[index * 2].default,
            extendSubTableData: {
              table: table,
              columns: resArr[index * 2 + 1].data.data,
            },
            tableSort: tableSort,
          })
        })
        arr.sort((a, b) => Number(a.tableSort) - Number(b.tableSort))
        console.log(resArr,arr)
        subTables.value = arr
      })
    }
  }
}, {deep: true, immediate: true})

const buildRules = (item) => {
  let arr  = []
  const fieldName = item.formItemProps.name
  const actRequired = isFormItemRequire(actRuleArgs.value, fieldName)
  item.formItemProps.rules.forEach(children => {
    if (children.validateType) {
      children.validator = validator.getValidator(item.formItemProps.validateType)
    }
    // 工作流规则覆盖 required 设置
    if (children.required !== undefined && actRuleArgs.value.form) {
      children.required = actRequired
    }
    arr.push(children)
  })
  // 如果工作流配置了必填但原始规则中没有 required 规则，则追加
  if (actRequired && !arr.some(r => r.required)) {
    arr.unshift({required: true, message: '请输入' + (item.formItemProps.label || '')})
  }
  return arr
}

let formProps = computed(() => {
  let data = {}
  if (tableConfig.value.formProps){
    data =  JSON.parse(tableConfig.value.formProps)
  }
  if (tableConfig.value.parentTable && tableConfig.value.tableType !== '4') {
    //是子表并且不是左数右表
    data.parentFormNo = tableConfig.value.parentTable
  }
  //删除actRuleArgs 由v-model传入
  delete data.actRuleArgs
  return data
})

let subTables = ref([])

const isRender = (item) => {
  const fieldName = item.formItemProps.name
  // 工作流规则：如果 actRuleArgs 中配置了该字段为隐藏，则不渲染
  if (!isFormItemShow(actRuleArgs.value, fieldName)) {
    return false
  }
  if (item.formItemProps && item.formItemProps.renderPredicate) {
    return calcPredicate(JSON.parse(item.formItemProps.renderPredicate), formModel.value)
  }
  return true
}
</script>
<style scoped>

</style>
