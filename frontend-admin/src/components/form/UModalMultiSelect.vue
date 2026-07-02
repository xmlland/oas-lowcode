<template>

  <div>
    <a-input-group class="modal-multi-select" compact @click="onSearch">
      <div class="name-container">
        <a-tag :closable="!disabled" v-bind:key="item.dictId" v-for="(item,index) in  showValue"
               @close="remove(index)">
          <a-button @click="clickItem(item)" type="link" size="small" v-if="clickable">{{ formatText(item) }}</a-button>
          <template v-else>
            {{ formatText(item) }}
          </template>
        </a-tag>
        <a-tag :closable="false" v-if="hideCount > 0">
          {{ '+' + hideCount + '...' }}
        </a-tag>
        <close-circle-filled @click.stop="clearSelect" v-if="!disabled&&allowClear&&currentValue&&currentValue.length>0" class="clear-icon" />
      </div>

      <a-button v-if="!disabled">
        <template #icon>
          <SearchOutlined/>
        </template>
      </a-button>
    </a-input-group>
    <a-form-item style="width: 0;height: 0;display: none;">
      <u-modal v-if="props.formNo" :customBodyStyle="{
        height: height+'px'
      }" ref="modal" :width="parseInt(modalWidth)" :customOK="true" @clickOk="clickOk"
               :formDisabled="true">
        <single-table-view ref="tableView" :url="url" :singleTable="singleTable" :buttons="[]" :formNo="props.formNo"
                           :height="height - 20"
                           :disabled="disabled">
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
  name: "UModalMultiSelect"
}

</script>
<script setup>

import {computed, getCurrentInstance, ref, watch} from "vue";
import {translateDict} from "@/lib/tools";

let instance = getCurrentInstance();
let height = computed(() => {
  //80vh - 弹窗标题 - 弹窗底部按钮 - 弹窗内边距
  return document.body.clientHeight * 0.8 - 53 - 43
})
let props = defineProps({
  value: {
    type: Array,
    default() {
      return null
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
  allowClear: {
    type: Boolean,
    default: true
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
  dictIdFiled: {
    type: String,
    default: 'id'
  },
  codeFiled: {
    type: String,
    default: 'code'
  },
  nameFiled: {
    type: String,
    default: 'name'
  },
  format: {
    type: Function,
    default(item) {
      if (item.code) {
        return `${item.name}（${item.code} ）`
      }
      return item.name
    }
  },
  formatFuncStr: {
    type: String,
    default: ''
  },
  clickable:{
    type: Boolean,
    default: false
  },
  urlData:{
    type: Object,
    default:null
  },
  maxTagCount:{
    type: Number,
    default: null
  }
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
// _columns.value.map(item => {
//   if (!item.minWidth) {
//     item.minWidth = 200
//   }
// })
watch(() => props.columns, (newVal) => {
  _columns.value = newVal
}, {deep: true})
let filterDataLocal = computed(() => {
  let queryData = instance.refs.tableView ? instance.refs.tableView.getQueryData() : {}
  let queryParamType = queryData.queryParamType || {}
  let pinyinQuery = instance.refs.tableView ? instance.refs.tableView.getPinyinQuery() : {}
  let _filterData = []
  for (let key in searchObj.value) {
    if (!searchObj.value[key]) continue
    _filterData.push({key: key, value: searchObj.value[key], type: queryParamType[key] || 'like', pinyin: pinyinQuery[key]})
  }
  console.log('filterDataLocal', props.filterData.concat(_filterData), pinyinQuery);
  return props.filterData.concat(_filterData)
})
let tmpDefaultSelectedRowKeys = computed(() => {
  return currentValue.value?currentValue.value.map(item => item.dictId):[]
});
let singleTable = computed(()=>{
  return {
    rowButtons: [],
    columns: _columns.value,
    ignoreSetTableRef: true,
    initParam: {
      // searchKey: props.searchKey,
      searchValue: "",
      tableName: props.formNo,
      filterList: filterDataLocal.value,
    },
    defaultSelectedRowKeys: tmpDefaultSelectedRowKeys.value,
    rowSelectionType: 'checkbox'
  }
})
let currentValue = ref(null)
let showValue = ref(null);
let hideCount = ref(0);
let defaultSelectValue = ref([])//存储初始值 #1458  zry 2022-11-26 13:31
watch(() => props.value, () => {
  if (props.value) {
    currentValue.value = props.value
    updateShowValue();
    singleTable.value.defaultSelectedRowKeys = props.value.map(item => item.dictId)
    defaultSelectValue.value = props.value
  } else {
    currentValue.value = []
    showValue.value = []
    hideCount.value = 0
    singleTable.value.defaultSelectedRowKeys = []
    defaultSelectValue.value = []
  }

}, {immediate: true})
// watch(() => props.filterData, () => {
//   singleTable.value.initParam.filterList = filterDataLocal.value
// }, {immediate: true})
let emits = defineEmits(['update:value', 'change', 'clickItem'])

let url = ref({
  list: 'dynamic/zform/gridselectDataMap',
})

if(props.urlData){
  Object.assign(url.value,props.urlData);
}

const onSearch = () => {
  if (!props.disabled) {
    searchObj.value = {}//置空该对象 解决再次打开选择框查询条件还生效的问题 zhoury 2024-03-15
    instance.refs.modal.open(props.modalTitle)
  }
}

const clickOk = () => {
  currentValue.value = []
  let rows = instance.refs.tableView.getSelectedRows();

  //存储初始值 #1458  zry 2022-11-26 13:31
  let map = {}
  defaultSelectValue.value.forEach(item => {
    map[item.dictId] = item
  })
  rows.forEach(row => {
    let obj = {}
    if (row[props.dictIdFiled]) {
      obj.dictId = row[props.dictIdFiled]
    }
    if (row[props.codeFiled]) {
      obj.code = row[props.codeFiled]
    } else if (map[obj.dictId] && map[obj.dictId].code) {
      //从存储的初始值取相应的内容 #1458  zry 2022-11-26 13:31
      obj.code = map[obj.dictId].code
    }
    if (row[props.nameFiled]) {
      obj.name = row[props.nameFiled]
    } else if (map[obj.dictId] && map[obj.dictId].name) {
      //从存储的初始值取相应的内容 #1458  zry 2022-11-26 13:31
      obj.name = map[obj.dictId].name
    }
    currentValue.value.push(obj)
  })
  updateShowValue();
  emits('update:value', currentValue.value)
  emits('change', currentValue.value)
  instance.refs.modal.close()
}

const remove = (index) => {
  currentValue.value.splice(index, 1)
  updateShowValue();
  // singleTable.value.defaultSelectedRowKeys = currentValue.value.map(item => item.dictId)
  defaultSelectValue.value = currentValue.value
  emits('change', currentValue.value);
}

const clickItem = (item) => {
  emits('clickItem',item)
}

const clearSelect = () => {
  currentValue.value = []
  defaultSelectValue.value = []
  showValue.value = []
  hideCount.value = 0
  emits('update:value', currentValue.value)
  emits('change', currentValue.value)
}

const formatText= (item) => {
  if (props.formatFuncStr){
    try {
      return new Function('item','obj', props.formatFuncStr)(item, {translateDict: translateDict})
    }catch (e) {
      console.error(e)
    }
    return 'error'
  }
  return props.format(item)
}

const updateShowValue = () =>{
  const deepCopied = JSON.parse(JSON.stringify(currentValue.value));
  if(props.maxTagCount && props.maxTagCount > 0){
    showValue.value = deepCopied.splice(0,props.maxTagCount);
    hideCount.value = currentValue.value.length - props.maxTagCount;
  }else{
    showValue.value = deepCopied
  }
}
defineExpose({
  onSearch
})
</script>
<style lang="less" scoped>
.modal-multi-select {
  display: flex;
  min-height: 32px;
  .name-container {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
    overflow: hidden;
    flex: 1;
    padding: 0 22px 0 9px;
    flex-wrap: wrap;
    border: 1px solid #d9d9d9;
    position: relative;

    .ant-tag {
      margin: 2px;
    }
    .clear-icon{
      position: absolute;
      right: 4px;
      display: block;
      cursor: pointer;
      font-size: 12px;
    }

    &:hover {
      .clear-icon {
        color: rgba(0, 0, 0, 0.45)
      }
    }
  }
}
</style>
