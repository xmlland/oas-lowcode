<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="数据角色"
                   :class="useModernListSkin ? 'modern-list-page sys-datarole-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="sys_datarole">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">数据角色</h2>
        <p class="modern-list-query-desc">维护数据访问角色及启用状态，为数据权限分配提供统一角色基础。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="enname" label="英文名称" type="input" :props="{placeholder:'英文名称' }"></QueryField>
      <QueryField name="name" label="名称" type="input" :props="{placeholder:'名称' }"></QueryField>
      <QueryField name="useable" label="启用" type="select" :props="{placeholder:'启用',dictType:'yes_no'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_datarole_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_datarole/form";
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
  permission: 'app:sys_datarole:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_datarole:del'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_datarole:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_datarole:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_datarole:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '英文名称', dataIndex: 'enname', minWidth: 120, align: 'left'},
    {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '启用', dataIndex: 'useable', minWidth: 120, align: 'center', dict: 'yes_no'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
