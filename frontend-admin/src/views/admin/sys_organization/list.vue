<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="组织"
                   :class="useModernListSkin ? 'modern-list-page sys-organization-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="sys_organization">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">组织管理</h2>
        <p class="modern-list-query-desc">维护组织名称、负责人、编号和有效状态，支撑组织维度的数据归属。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="org_name" label="组织名称" type="input" :props="{placeholder:'组织名称' }"></QueryField>
      <QueryField name="org_number" label="组织编号" type="input" :props="{placeholder:'组织编号' }"></QueryField>
      <QueryField name="org_effect" label="是否有效" type="select" :props="{placeholder:'是否有效',dictType:'yes_no'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_organization_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_organization/form";
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
  permission: 'app:sys_organization:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_organization:del'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_organization:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_organization:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_organization:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '组织名称', dataIndex: 'org_name', minWidth: 120, align: 'left'},
    {title: '组织负责人', dataIndex: ['primaryperson_id','name'], minWidth: 120, align: 'left'},
    {title: '组织编号', dataIndex: 'org_number', minWidth: 120, align: 'left'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'left'},
    {title: '是否有效', dataIndex: 'org_effect', minWidth: 120, align: 'center', dict: 'yes_no'},
  ]
})

</script>
<style scoped>
</style>
