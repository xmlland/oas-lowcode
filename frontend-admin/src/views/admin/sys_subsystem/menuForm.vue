<template>
  <a-spin :spinning="spinning">
    <u-tree :data="treeData" v-model:checkedKeys="checkedKeys" :expandedKeys="expandedKeys" :defaultExpandAll="false"
            :checkStrictly="true"
            :checkable="true"
            :selectable="false"
            @check="check"></u-tree>
  </a-spin>
</template>

<script>
export default {
  name: "menuForm"
}
</script>
<script setup>
import {ref} from "vue";
import {listToTree, oneOf} from "@/lib/tools";
import {getMenuTreeAction, listDataAction} from "@/api/api";

let props = defineProps({
  systemId: {
    type: String,
    default: ''
  }
})
let spinning = ref(true)
let treeData = ref([])
let checkedKeys = ref({
  checked: [], halfChecked: []
})

const checkNode = (id) => {
  let data = checkedKeys.value.checked
  if (!oneOf(id, data)) {
    data.push(id)
    checkedKeys.value.checked = data
  }
}

const check = (_checkedKeys, {checked, checkedNodes, node, event}) => {
  checkedKeys.value = _checkedKeys
  if (checked) {
    if (node.parent.nodes) {
      //自动选择上级
      node.parent.nodes.forEach(child => {
        checkNode(child.key)
      })
    }
    if (node.children) {

      const checkChildren = (arr) => {
        arr.forEach(child => {
          if (child.children && child.children.length > 0) {
            checkNode(child.key)
            checkChildren(child.children)
          }else{
            checkNode(child.key)
          }
        })
      }
      //自动选择子级
      checkChildren( node.children)
    }
  }
}

let expandedKeys = ref([])
let dealExpand = (data, auths) => {
  data.forEach(item => {
    if (item.children && oneOf(item.key, auths)) {
      expandedKeys.value.push(item.key)
      dealExpand(item.children, auths)
    }
  })
}
let deleteIdArr = []
if (props.systemId) {
  let actions = [getMenuTreeAction(), listDataAction('sys_subsystem_menu', {subsystem_id: props.systemId})]
  Promise.all(actions).then(resArr => {
    treeData.value = listToTree(resArr[0].data.data.map(item => {
      item.name = item.remarks ? `${item.name}(${item.remarks})` : item.name
      return item
    }), '0', {
      title: 'name',
      key: 'id',
      parent: 'parentId'
    }, true)
    checkedKeys.value.checked = resArr[1].rows.map(item => item.menu_id)
    expandedKeys.value = []
    dealExpand(treeData.value, checkedKeys.value.checked)
    deleteIdArr = resArr[1].rows.map(item => item.id)
  }).finally(() => {
    spinning.value = false
  })
}
const getCheckData = () => {
  return {
    id: props.systemId,
    ids: Array.from(new Set(checkedKeys.value.checked)).join(),
    deleteIdArr:  Array.from(new Set(deleteIdArr))
  }
}
defineExpose({
  getCheckData
})
</script>
<style scoped>

</style>
