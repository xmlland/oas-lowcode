<template>
  <single-table-view :single-table="singleTable" :buttons="[{value:'add',text:'添加'}]" :row-edit="true"
                     :auto-height="true"
                     :query-button="false" :value="predicateArr" @update:value="updateValue">
    <template #queryFields>
      每组的第一个条件不需要选择方式 默认为and 即使选择了or 也会被强制转换为and
    </template>
  </single-table-view>
</template>

<script>
export default {
  name: "PredicateEditor"
}
</script>
<script setup>

import {ref, watch, computed} from "vue";
import SingleTableView from "@/components/view/SingleTableView";

let props = defineProps({
  value: {
    type: String,
    default: ''
  },
  allColumns: {
    type: Array,
    default() {
      return []
    }
  },
})
let predicateArr = ref([])
watch(() => props.value, (val) => {
  if (val === '') {
    return
  }
  predicateArr.value = JSON.parse(val)
}, {deep: true, immediate: true})
let singleTable = computed(() => {
  return {
    rowKey: 'dictValue',
    disableInitLoad: true,
    pagination: false,
    showSorter: false,
    rowButtons: [{
      value: 'delete', text: '移除'
    }],
    data: predicateArr.value,
    columns: [
      {title: '分组', dataIndex: 'group', minWidth: 120, align: 'left', editor: {type: 'input', rules: [{required: true, message: ''}]}},
      {
        title: '方式', dataIndex: 'condition', minWidth: 120, align: 'left', editor:
            {
              type: 'select', props: {
                optionData: [
                  {value: 'and', text: 'and'},
                  {value: 'or', text: 'or'}
                ], valueField: 'value', textField: 'text'
              }, rules: [{required: true, message: ''}]
            }
      },
      {
        title: '字段', dataIndex: 'fieldName', minWidth: 120, align: 'left', editor:
            {
              type: 'select', props: {
                optionData: props.allColumns.filter(item => {
                  return item.showType
                }).map(item => {
                  return {value: item.name, text: `${item.comments}(${item.name})`}
                })
                , valueField: 'value', textField: 'text'
              }, rules: [{required: true, message: ''}]
            }
      },
      {
        title: '类型', dataIndex: 'type', minWidth: 120, align: 'left', editor:
            {
              type: 'select', props: {
                optionData: [
                  {value: 'eq', text: '='},
                  {value: 'ne', text: '!='}
                ], valueField: 'value', textField: 'text'
              }, rules: [{required: true, message: ''}]
            }
      },
      {title: '值', dataIndex: 'value', minWidth: 120, align: 'left', editor: {type: 'input', rules: [{required: true, message: ''}]}},
    ]
  }
})

let emits = defineEmits(['update:value']);

const updateValue = (e) => {
  emits('update:value', JSON.stringify(e))
}
</script>
<style scoped>

</style>
