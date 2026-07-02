<template>

  <div>
    <a-input-group class="modal-select" compact @click="onSearch">
      <a-input
          v-model:value="currentValue"
          :placeholder="placeholder"
          :disabled="true"
      />
      <a-button v-if="allowClear && !disabled && currentValue" class="modal-select-clear" @click.stop="clearSelection">
        <span>&times;</span>
      </a-button>
      <a-button v-if="!disabled">
        <template #icon>
          <SearchOutlined/>
        </template>
      </a-button>
    </a-input-group>

    <a-form-item style="width: 0;height: 0;display: none;">
      <u-modal v-if="props.formNo" :customBodyStyle="{
        height: height+'px'
      }" ref="modal" :width="parseInt(modalWidth)" :formDisabled="true" :customOK="true" @clickOk="clickOk">
        <single-table-view ref="tableView" :url="localDataUrl" :singleTable="singleTable" :buttons="buttons" :formNo="props.formNo" :height="height - 20"
                           @clickRow="clickRow"
                           @clickButton="clickTableButton"
                           :disabled="false">
          <template #queryFields>
            <QueryField v-bind:key="item.key" v-for="item in searchArr" :name="item.key" :label="item.label" :type="item.type" :queryType="item.queryType" :pinyin="item.pinyin" :formatValue="item.formatValue" :formatValueFuncStr="item.formatValueFuncStr" :labelWidth="parseInt(searchLabelWidth)"
                        v-model:value="searchObj[item.key]"  :props="item.props"></QueryField>
          </template>
        </single-table-view>
      </u-modal>
    </a-form-item>

  </div>

</template>

<script>
export default {
  name: "UModalSelect"
}

</script>
<script setup>

import {computed, getCurrentInstance, ref, watch, inject} from "vue";
import {getData} from "@/lib/tools";

let instance = getCurrentInstance();
let height = computed(() => {
  //80vh - 弹窗标题 - 弹窗底部按钮 - 弹窗内边距
  return document.body.clientHeight * 0.8 - 53 - 43
})
let props = defineProps({
  value: {
    type: Object,
    default() {
      return {}
    }
  },
  formModel: {
    type: Object,
    default() {
      return {}
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: ''
  },
  modalTitle: {
    type: String,
    default: '请选择'
  },
  modalWidth: {
    type: [String, Number],
    default: 800
  },
  formNo: {
    type: String,
    default: ''
  },
  filterData: {
    type: Array,
    default() {
      return []
    }
  },
  targetFormNo: {
    type: String,
    default: ''
  },
  targetField: {
    type: String,
    default: ''
  },
  targetFilterData: {
    type: Array,
    default() {
      return []
    }
  },
  formUpdateMap: {
    type: Object,
    default() {
      return null
    }
  },
  searchKey: {
    type: [String, Array],
    default: ''
  },
  searchLabel: {
    type: [String, Array],
    default: ''
  },
  searchConfig: {
    type: Object,
    default() {
      return {}
    }
  },
  searchLabelWidth: {
    type: [String, Number],
    default: null
  },
  columns: {
    type: Array,
    default() {
      return []
    }
  },
  nameDataIndex: {
    type: String,
    default: ''
  },
  dataUrl: {
    type: [String,Object],
    default() {
      return {
        list: 'dynamic/zform/gridselectDataMap',
      }
    }
  },
  buttons: {
    type: Array,
    default() {
      return []
    }
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  orderConfig: {
    type: Array,
    default() {
      return []
    }
  },
})
let localDataUrl = computed(() => {
  if (typeof props.dataUrl === 'string') {
    return  {
      list: props.dataUrl
    }
  }
  return props.dataUrl
})
let searchObj = ref({})
let searchArr = computed(() => {
  let arr = []
  if (typeof props.searchKey === 'string') {
    arr.push({key: props.searchKey, label: props.searchLabel})
  }
  if (Array.isArray(props.searchKey)) {
    props.searchKey.forEach((item, index) => {
      let obj = {key: item, label: props.searchLabel[index]}
      obj.type = props.searchConfig[item] ? props.searchConfig[item].type : 'input'
      obj.queryType = props.searchConfig[item] ? props.searchConfig[item].queryType : ''
      obj.pinyin = props.searchConfig[item] ? props.searchConfig[item].pinyin : false
      obj.formatValue = props.searchConfig[item] ? props.searchConfig[item].formatValue : null
      obj.formatValueFuncStr = props.searchConfig[item] ? props.searchConfig[item].formatValueFuncStr : null
      let itemProps = {
        placeholder: item.label
      }
      if (props.searchConfig[item]) {
        Object.assign(itemProps, props.searchConfig[item].props)
      }
      obj.props = itemProps
      arr.push(obj)
    })
  }
  return arr
})
let _columns = ref(props.columns)
watch(() => props.columns, (newVal) => {
  _columns.value = newVal
}, {deep: true})
/*_columns.value.map(item => {
  if (!item.minWidth) {
    item.minWidth = 200
  }
})*/
let singleTableViewMethods = props.targetFormNo && inject(props.targetFormNo + 'SingleTableViewMethods')

let filterDataLocal = computed(() => {
  let unSaveRows = []
  singleTableViewMethods && singleTableViewMethods.getUnSaveRows().forEach(item => {
    unSaveRows.push({key: 'a.id', value: item[props.targetField].id, type: 'ne'})
  })
  let queryData = instance.refs.tableView ? instance.refs.tableView.getQueryData() : {}
  let queryParamType = queryData.queryParamType || {}
  let pinyinQuery = instance.refs.tableView ? instance.refs.tableView.getPinyinQuery() : {}
  let _filterData = []
  for (let key in searchObj.value) {
    if (isEmptyQueryValue(searchObj.value[key])) continue
    _filterData.push({key: key, value: searchObj.value[key], type: queryParamType[key] || 'like', pinyin: pinyinQuery[key]})
  }
  return props.filterData.concat(unSaveRows).concat(_filterData)
})
let targetFilterDataLocal = computed(() => {
  //当表单为编辑时，需要查出当前选中的值 和 当前表单存储的值， 方式编辑修改后无法改为原来的选择项
  return props.targetFilterData.concat((props.targetField &&props.formModel && props.formModel.id && props.value && props.value.id) ?
      [{key: 'targetTable.'+props.targetField, value: props.value.id, type: 'ne'},{key: 'targetTable.id', value: props.formModel.id, type: 'ne'}] :
      [])
})
let orderDataLocal = computed(() =>{
  let order = '';
  if(props.orderConfig && props.orderConfig.length > 0){
    let orderArray = [];
    for(let i=0;i< props.orderConfig.length;i++){
      orderArray.push(props.orderConfig[i].key + ' ' + props.orderConfig[i].type);
    }
    order = orderArray.join(",");
  }
  return order;
})
const isEmptyQueryValue = (value) => {
  if (value === null || value === undefined || value === '') {
    return true
  }
  if (Array.isArray(value)) {
    return value.length === 0
  }
  if (typeof value === 'object') {
    if (Object.keys(value).length === 0) {
      return true
    }
    if (Object.prototype.hasOwnProperty.call(value, 'id') && !value.id) {
      return true
    }
  }
  return false
}
let defaultSelectedRowKeys = ref([])
let singleTable = computed(() => {
  return {
    rowButtons: [{
      value: 'check',
      text: '选择',
      disableShow: true,
      visibleFilter: (row) => {
        return props.value ? row.id !== props.value.id : true
      },
    },{
      value: 'uncheck',
      text: '取消',
      visibleFilter: (row) => {
        return props.value ? row.id === props.value.id : false
      },
      disableShow: true,
      color: 'error'
    }],
    columns: _columns.value,
    ignoreSetTableRef: true,
    initParam: {
      //searchKey: props.searchKey,
      orderBy: orderDataLocal.value,
      tableName: props.formNo,
      filterList: filterDataLocal.value,
      targetTableName: props.targetFormNo,
      targetField: props.targetField,
      targetFilterList: targetFilterDataLocal.value,
    },
    defaultSelectedRowKeys: defaultSelectedRowKeys.value,
    rowSelectionType: 'radio'
  }
})

let currentValue = ref(null)
watch(() => props.value, () => {
  if (props.value && props.value.id) {
    currentValue.value = props.value.name
    defaultSelectedRowKeys.value = [props.value.id]
  } else {
    currentValue.value = null
    defaultSelectedRowKeys.value = []
  }
}, {immediate: true})
watch(() => props.filterData, () => {
  singleTable.value.initParam.filterList = filterDataLocal.value
}, {immediate: true})
watch(() => props.targetFormNo, () => {
  singleTable.value.initParam.targetTableName = props.targetFormNo
}, {immediate: true})

let emits = defineEmits(['update:value', 'change', 'update:formModel'])

const onSearch = () => {
  if (!props.disabled) {
    // 打开弹框时，重置查询条件
    searchObj.value = {};
    instance.refs.modal.open(props.modalTitle)
  }
}
const updateFormModel = (row) => {
  if (props.formUpdateMap) {
    let value = props.formModel
    let updates = 0;
    for (let key in props.formUpdateMap) {
      if (key) {
        value[key] = getData(row,props.formUpdateMap[key])
        updates++
      }
    }
    if (updates > 0) {
      emits('update:formModel', value)
    }
  }
}
const clickRow = ({value, row, index}) => {
  if (value === 'check') {
    let name = ''
    if (props.nameDataIndex){
      name = row[props.nameDataIndex]
    }else{
      name = row[singleTable.value.columns[0].dataIndex]
    }
    currentValue.value = name
    emits('update:value', {
      id: row.id, name: name
    })
    emits('change', {
      id: row.id, name: name
    })
    updateFormModel(row)

    instance.refs.modal.close()
  }else if (value === 'uncheck') {
    clearSelection()
    instance.refs.modal.close()
  }
}
const clickOk = () => {

  let rows = instance.refs.tableView.getSelectedRows();
  let current = null
  if (rows.length === 0){
    instance.refs.modal.close()
    return
  }
  //如果打开弹窗没有选别的数据 直接点确定的话 row里面只有id
  if (rows.length>0){
    let row = rows[0]
    let name = ''
    if (props.nameDataIndex){
      name = row[props.nameDataIndex]
    }else{
      name = row[singleTable.value.columns[0].dataIndex]
    }
    current = {
      id: row.id, name: name || (props.value?props.value.name:'')
    }
    if (name){
      updateFormModel(row)
    }
  }
  emits('update:value', current)
  emits('change', current)
  instance.refs.modal.close()
}
const clickTableButton = (v) =>{
  emits('clickButton',v);
}

const clearSelection = () => {
  currentValue.value = null
  defaultSelectedRowKeys.value = []
  emits('update:value', null)
  emits('change', null)
  updateFormModel({})
}

defineExpose({
  onSearch

})
</script>
<style scoped>
.modal-select {
  display: flex;
}

.modal-select-clear {
  padding: 0 8px;
}
</style>
