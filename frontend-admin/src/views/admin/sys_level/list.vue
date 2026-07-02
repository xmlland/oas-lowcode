<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="职务"
                   :class="useModernListSkin ? 'modern-list-page sys-level-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="sys_level">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">职务管理</h2>
        <p class="modern-list-query-desc">维护职务名称、代码和状态，统一支撑人员职务信息管理。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="name" label="职务名称" type="input" :props="{placeholder:'职务名称' }"></QueryField>
      <QueryField name="code" label="职务代码" type="input" :props="{placeholder:'职务代码' }"></QueryField>
      <QueryField name="useable" label="状态" type="select" :props="{placeholder:'状态',dictType:'sys_useable'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_level_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_level/form";
import config from "@/config";
let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:sys_level:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_level:del'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_level:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_level:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_level:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '职务名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '职务代码', dataIndex: 'code', minWidth: 120, align: 'left'},
    {title: '状态', dataIndex: 'useable', minWidth: 120, align: 'center', dict: 'sys_useable'},
    {title: '排序号', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
