<template>
  <TreeTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="流程树" :buttons="buttons" :rowButtons="rowButtons"
                 :queryArea="queryArea"
                 :class="useModernListSkin ? 'modern-tree-table-page oa-process-tree-setting-tree-table-view' : ''"
                 formNo="oa_process_tree_setting">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">流程树</h2>
        <p class="modern-list-query-desc">维护流程分类树、层级节点和排序信息。</p>
      </div>
    </template>
  </TreeTableView>
</template>

<script>
export default {
  name: "oa_process_tree_setting_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oa/oa_process_tree_setting/form";
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
  permission: 'app:oa_process_tree_setting:add'
}])

let rowButtons = ref([{
  value: 'view',
  text: '查看',
  permission: 'app:oa_process_tree_setting:view'
}, {
  value: 'edit',
  text: '编辑',
  permission: 'app:oa_process_tree_setting:edit'
}, {
  value: 'delete',
  text: '删除',
  permission: 'app:oa_process_tree_setting:del'
}, {
  value: 'addChild',
  text: '添加下级',
  permission: 'app:oa_process_tree_setting:lowerlevel'
}])

let singleTable = ref({
  autoHeight: useModernListSkin.value,
  rowSelection:false,
  columns: [
    {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
