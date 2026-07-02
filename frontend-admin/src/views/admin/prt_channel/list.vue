<template>
  <TreeTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="栏目管理" :buttons="buttons" :rowButtons="rowButtons"
                 :queryArea="queryArea"
                 :class="useModernListSkin ? 'modern-tree-table-page prt-channel-tree-table-view' : ''"
                 formNo="prt_channel">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">栏目管理</h2>
        <p class="modern-list-query-desc">按层级维护门户栏目、文章类型、状态和排序，支持添加下级栏目。</p>
      </div>
    </template>
  </TreeTableView>
</template>

<script>
export default {
  name: "prt_channel_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/prt_channel/form";
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
  permission: 'app:prt_channel:add'
}])

let rowButtons = ref([{
  value: 'view',
  text: '查看',
  permission: 'app:prt_channel:view'
}, {
  value: 'edit',
  text: '编辑',
  permission: 'app:prt_channel:edit'
}, {
  value: 'delete',
  text: '删除',
  permission: 'app:prt_channel:del'
}, {
  value: 'addChild',
  text: '添加下级',
  permission: 'app:prt_channel:lowerlevel'
}])

let singleTable = ref({
  autoHeight: useModernListSkin.value,
  isShowMoreDropdown:false,
  optionWidth: 260,
  rowSelection:false,
  columns: [
    {title: '所属站点', dataIndex: ['site','name'], minWidth: 120, align: 'left'},
    {title: '栏目名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '文章类型', dataIndex: 'info_type', minWidth: 120, align: 'center', dict: 'prt_info_types'},
    {title: '状态', dataIndex: 'useable', minWidth: 120, align: 'center', dict: 'sys_useable'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
