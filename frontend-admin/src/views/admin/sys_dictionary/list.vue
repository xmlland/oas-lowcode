<template>
  <TreeTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="系统字典" :buttons="buttons" :rowButtons="rowButtons"
                 :queryArea="queryArea"
                 :class="useModernListSkin ? 'modern-tree-table-page sys-dictionary-tree-table-view' : ''"
                 formNo="sys_dictionary">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">系统字典</h2>
        <p class="modern-list-query-desc">维护系统字典层级、编码和名称，保留原树形字典管理入口。</p>
      </div>
    </template>
  </TreeTableView>
</template>

<script>
export default {
  name: "sys_dictionary_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_dictionary/form";
import config from "@/config";
let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:sys_dictionary:add'
}])

let rowButtons = ref([{
  value: 'view',
  text: '查看',
  permission: 'app:sys_dictionary:view'
}, {
  value: 'edit',
  text: '编辑',
  permission: 'app:sys_dictionary:edit'
}, {
  value: 'delete',
  text: '删除',
  permission: 'app:sys_dictionary:del'
}, {
  value: 'addChild',
  text: '添加下级',
  permission: 'app:sys_dictionary:lowerlevel'
}])

let singleTable = ref({
  autoHeight: useModernListSkin.value,
  isShowMoreDropdown:false,
  optionWidth: 260,
  rowSelection:false,
  columns: [
    {title: '编码', dataIndex: 'code', minWidth: 120, align: 'left'},
    {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
  ]
})

</script>
<style scoped>
</style>
