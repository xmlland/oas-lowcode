<template>
  <div class="left-tree-right-table-view">
    <u-tree :style="treeWidthStr" :showSearch="showSearch" :defaultActiveIndex="null" :url="treeDataUrl" :postData="postData" v-bind:="uTree"
            @select="onTreeSelect"
            :defaultExpandAll="defaultExpandAll"
            :selectedKeys="selectedKeys"
            :treeFormNo="treeFormNo"
            :responseData="['data','data']" :transform="{ title: 'name', key: 'id',  parent: 'parentId',
        showCount: (item,node)=>{
          return item.dataCount
        }, }"
            class="left-tree"/>
    <single-table-view ref="refRightTableView" :customHandleQuery="true" v-bind="rightTableView" @clickButton="clickButton" @clickRow="clickRow" @requestResult="requestResult" :style="tableWidthStr"
                       :modalTitle="modalTitle" @saveSuccess="saveSuccess" @totalCountChange="totalCountChange"
                       class="right-table">
      <template v-if="$slots.queryHeader" #queryHeader>
        <slot name="queryHeader"/>
      </template>
      <template #queryFields>
        <slot name="queryFields"/>
      </template>
    </single-table-view>
  </div>

</template>

<script>
export default {
  name: "LeftTreeRightTableView",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import {mergeQueryData} from "@/components/table/tableTool";

let instance = getCurrentInstance();
let props = defineProps({
  treeWidth: {
    type: Number,
    default: 300
  },
  treeFormNo: {
    type: String,
    default: ''
  },
  traceFlag: {
    type: String,
    default: '1'
  },
  showSearch: {
    type: Boolean,
    default: true
  },
  filterTableFiled: {
    type: String,
    default: 'parent_id'
  },
  rightTableView: {
    type: Object,
    default() {
      return {}
    }
  },
  modalTitle: {
    type: String,
    default: ''
  },
  defaultExpandAll:{
    type: Boolean,
    default: true,
  },
  //请求路径
  url:{
    type: String,
    default: 'dynamic/zform/treeData'
  },
  extendQueryField:{
    type: String,
    default:''
  },
  uTree:{
    type: Object,
    default: null
  },
})
let treeDataUrl = ref(props.url+`?formNo=${props.treeFormNo}&parentId=&traceFlag=${props.traceFlag}&rightTableFormNo=${props.rightTableView.formNo}`)
let treeWidthStr = computed(() => 'width: ' + props.treeWidth + 'px')
let tableWidthStr = computed(() => 'width: calc(100% - 28px - ' + props.treeWidth + 'px)')
let selectedKeys = ref([])
const onTreeSelect = (_selectedKeys, info) => {
  let obj = {}
  if (props.extendQueryField){
    obj[props.extendQueryField] = info.node.attributes
  }
  selectedKeys.value = _selectedKeys
  loadRightTableData(obj)
}
/**
 * 右侧列表查询数据
 * @param obj
 */
const loadRightTableData = (obj={}) => {

  if (selectedKeys.value.length > 0) {
    obj[props.filterTableFiled] = {id: selectedKeys.value[0]}
  }
  //点击左树调用右表格的loadData方法时 将右侧的查询条件带上
  let queryData = getQueryData();
  Object.assign(obj, queryData)
  instance.refs.refRightTableView.loadData(obj,false,1)
}
const clickButton = ({value}) => {
  if ('reset' === value) {
    selectedKeys.value = []
  }else if ('query' === value) {
    loadRightTableData()
  }else{
    emits('clickButton', {value})
  }
}

let emits = defineEmits(['clickRow','clickButton','requestResult','totalCountChange'])
const requestResult = (res) => {
  emits('requestResult', res)
  postData.value = mergeQueryData(getQueryData(), props.rightTableView.singleTable?props.rightTableView.singleTable.initParam:{})
}
const clickRow = ({value, row, index}) => {
  emits('clickRow', {value, row, index})
}
const loadData = (param = {}, setQueryArea = false) => {
  return instance.refs.refRightTableView.loadData(param, setQueryArea)
}
const saveSuccess = () => {
  loadRightTableData()
  instance.refs.refRightTableView.clearSelectedRows()
}
const handleEdit = ({row, index}) => {
  instance.refs.refRightTableView.handleEdit({row, index})
}
const handleView = ({row, index}) => {
  instance.refs.refRightTableView.handleView({row, index})
}

const getSelectedRowKeys = () => {
  return instance.refs.refRightTableView.getSelectedRowKeys()
}
const getSelectedRows = () => {
  return instance.refs.refRightTableView.getSelectedRows()
}
const getQueryData = () => {
  let queryData = instance.refs.refRightTableView ? instance.refs.refRightTableView.getQueryData() : {}
  if (selectedKeys.value.length > 0) {
    queryData[props.filterTableFiled] = selectedKeys.value[0]
  }
  return queryData
}

const totalCountChange = (total) => {
  emits('totalCountChange', total)
}
let postData = ref(mergeQueryData(getQueryData(), props.rightTableView.singleTable?props.rightTableView.singleTable.initParam:{}))
defineExpose({
  getSelectedRowKeys, getSelectedRows, loadData, getQueryData, saveSuccess, handleEdit, handleView
})
</script>
<style lang="less" scoped>
.left-tree-right-table-view {
  height: 100%;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;

}
</style>
