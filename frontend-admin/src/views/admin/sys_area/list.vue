<template>
  <TreeTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="区域" :buttons="buttons" :rowButtons="rowButtons"
                 :queryArea="queryArea"
                 :class="useModernListSkin ? 'modern-tree-table-page sys-area-tree-table-view' : ''"
                 formNo="sys_area">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">区域管理</h2>
        <p class="modern-list-query-desc">维护行政区域编码和区域层级，支持新增下级和层级结构调整。</p>
      </div>
    </template>
  </TreeTableView>
</template>

<script>
export default {
  name: "sys_area_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_area/form";
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
  permission: 'app:sys_area:add'
}])

let rowButtons = ref([{
  value: 'view',
  text: '查看',
  permission: 'app:sys_area:view'
}, {
  value: 'edit',
  text: '编辑',
  permission: 'app:sys_area:edit'
}, {
  value: 'delete',
  text: '删除',
  permission: 'app:sys_area:del'
}, {
  value: 'addChild',
  text: '添加下级',
  permission: 'app:sys_area:lowerlevel'
}])

let singleTable = ref({
  autoHeight: useModernListSkin.value,
  isShowMoreDropdown:false,
  optionWidth: 260,
  rowSelection:false,
  columns: [
    {title: '区域编码', dataIndex: 'code', minWidth: 120, align: 'left'},
    {title: '区域名称', dataIndex: 'name', minWidth: 120, align: 'left'},
  ]
})

</script>
<style scoped>
</style>
