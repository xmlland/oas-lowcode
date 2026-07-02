<template>
  <SingleTableView :subTable="true" :queryArea="queryArea" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="数据字典项" :buttons="buttons" formNo="sys_dictionary"
                   :class="useModernListSkin ? 'modern-list-page sys-dict-item-list-view' : ''"
                   :autoHeight="useModernListSkin">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">数据字典项</h2>
        <p class="modern-list-query-desc">维护字典项编码、名称、显示标记和排序信息。</p>
      </div>
    </template>

  </SingleTableView>
</template>

<script>
export default {
  name: "sys_dict_item_list"
}
</script>
<script setup>
import form from "@/views/admin/sys_dict_item_list/form";
import {computed, ref} from "vue";
import config from "@/config";

let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { queryAreaStyle: 'ltr' } : { queryAreaStyle: 'ltr' }
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:sys_dictionary:add'
},{
  value: 'import',
  text: '导入数据',
},{
  value: 'export',
  text: '导出Excel',
}, {
  value: 'batch-delete',
  text: '删除',
  //permission: 'app:sys_dictionary:remove'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_dictionary:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_dictionary:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_dictionary:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '编码', dataIndex: 'code', minWidth: 120, align: 'left'},
    {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '英文名称', dataIndex: 'name_en', minWidth: 120, align: 'left'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
    {title: '查询显示', dataIndex: 'view_flag', minWidth: 120, align: 'center', sorter: 'false', status: true},
    {title: '编辑显示', dataIndex: 'edit_flag', minWidth: 120, align: 'center', sorter: 'false', status: true},
    {title: '备注信息', dataIndex: 'remarks', minWidth: 120, align: 'left'},
  ]
})
</script>
<style scoped>

</style>
