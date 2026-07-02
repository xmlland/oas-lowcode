<template>
  <a-tree-select
      v-model:value="currentValue"
      :show-search="showSearch"
      :disabled="disabled"
      style="width: 100%"
      :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
      :placeholder="placeholder"
      :allow-clear="allowClear"
      :getPopupContainer="getPopupContainer"
      :treeDefaultExpandAll="defaultExpandAll"
      :tree-line="treeLine"
      :tree-data="treeData"
      @change="changeInput"
      :filterTreeNode="filterTreeNode"
      :labelInValue="true"
  >
  </a-tree-select>
</template>

<script>
export default {
  name: "UTreeSelect"
}
</script>
<script setup>

import {ref, watch} from "vue";
import {getTreeDataAction} from "@/api/api";
import {getData, listToTree} from "@/lib/tools"

let props = defineProps({
  value: {
    type: Object,
    default() {
      return {
        id: ''
      }
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  placeholder: {
    type: String,
    default: ''
  },
  showSearch: {
    type: Boolean,
    default: true
  },
  treeLine: {
    type: Boolean,
    default: true
  },
  parentId: {
    type: String,
    default: '0'
  },
  formNo: {
    type: String,
    default: ''
  },
  data: {
    type: Array,
    default: ()=>{
      return []
    }
  },
  onlyLeafSelect: {
    type: Boolean,
    default: false
  },
  //城市等级，当左树为行政区时可能会用到，用于控制“上级”的行政区显示到那个等级:1:显示到省,2:显示到市,3:显示到区/县
  cityLevel: {
    type: Number,
    default: 3
  },
  defaultExpandAll: {
    type: Boolean,
    default: true
  },
  container:{
    type: String,
    default: ''
  },
})
let currentValue = ref(null)
watch(() => props.value, () => {
  if (props.value && props.value.id) {
    // labelInValue 模式下，value 需要是 {value, label} 格式
    currentValue.value = {value: props.value.id, label: props.value.name || props.value.id}
  } else {
    currentValue.value = null
  }
}, {immediate: true})

let treeData = ref([])
const loadTreeData = () => {

  if (props.formNo) {
    getTreeDataAction(props.parentId, props.formNo).then(res => {
      let data = res.data.data
      let id = ''
      let showData = []
      //判断城市等级是否为3
      if (props.cityLevel !== 3) {
        //城市等级不为3
        if (props.cityLevel === 2) {
          //城市等级为2，帅选出所有市级地区
          data.forEach(item => {
            id = item.id
            //根据地区代码筛选出所有的市级以上单位（包括市级），包括4个直辖市的所有区，重庆市还包括县
            if (id.startsWith("31") || id.startsWith("11") || id.startsWith("12") || id.startsWith("50") || id.endsWith("00") || id == "1") {
              showData.push(item);
            }
          })
        } else if (props.cityLevel === 1) {
          //城市等级为1，帅选出所有省级地区
          data.forEach(item => {
            id = item.id
            //根据地区代码筛选出所有的省级以上单位（包括省级），包括4个直辖市
            if (id.endsWith("0000") || id === "1") {
              showData.push(item);
            }
          })
        }
      } else {
        //城市等级为3，直接将所有数据显示
        showData = data
      }
      let rootId = props.parentId;

      let rootArr = showData.filter(item=>getData(item, 'parentId')=='0')
      if (rootArr.length === 0) {
        let orgIds = showData.filter(item=>item.hasChildren).map(item=>getData(item, 'parentId'));
        if (orgIds.length === 0 && showData.length > 0) {
          rootId = getData(showData[0], 'parentId')
        }else{
          rootId = orgIds[0]
        }
      }
      treeData.value = listToTree(showData, rootId, {
        title: 'name',
        key: 'id',
        parent: 'parentId',
        selectable: (node) => {
          if (props.onlyLeafSelect) {
            return !node.children || node.children.length === 0
          } else {
            return true
          }
        }
      })
    })
  }

}
watch(() => props.parentId, () => loadTreeData(), {immediate: true})
watch(() => props.formNo, () => loadTreeData(), {immediate: true})
watch(() => props.data, () => {
  // 仅当外部传入了实际数据时才覆盖，避免空数组清除 API 加载的树数据
  if (props.data && props.data.length > 0) {
    treeData.value = props.data
  }
}, {immediate: true, deep: true})

let emits = defineEmits(['update:value', 'change'])

const changeInput = (valueObj, label, extra) => {
  let data = null;
  if(valueObj){
    // labelInValue 模式下，valueObj 是 {value, label} 格式
    data = {id: valueObj.value, name: valueObj.label};
  }
  emits('update:value', data)
  emits('change', data)
}

const filterTreeNode = (inputValue, treeNode) => {
  let label = treeNode.title || treeNode.label || ''
  return label.toUpperCase().indexOf(inputValue.toUpperCase()) >= 0
}
const getPopupContainer = () => {
  return props.container === ''?document.body: document.getElementById(props.container)
}

</script>
<style scoped>

</style>
