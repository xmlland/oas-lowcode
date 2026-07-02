<template>
  <SingleTableView @clickRow="clickRow" ref="tableView" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="数据权限"
                   :class="useModernListSkin ? 'modern-list-page sys-datapermission-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="sys_datapermission">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">数据权限</h2>
        <p class="modern-list-query-desc">维护主表、表达式和排序规则，集中管理系统数据过滤策略。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="name" label="名称" type="input" :props="{placeholder:'名称' }"></QueryField>
      <QueryField name="main_table" label="主表" type="input" :props="{placeholder:'主表' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_datapermission_list",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/admin/sys_datapermission/form";
import {confirmAction} from "@/api/action";
import {saveDataUrl} from "@/api/api";
import config from "@/config";
let modalComponent = form
let instance = getCurrentInstance();
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})
let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:sys_datapermission:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_datapermission:del'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_datapermission:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_datapermission:edit'
    },{
      value: 'copy',
      text: '复制',
      permission: 'app:sys_datapermission:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_datapermission:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '主表', dataIndex: 'main_table', minWidth: 120, align: 'left'},
    {title: '表达式', dataIndex: 'expression', minWidth: 120, align: 'left'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})
const clickRow = ({value, row, index}) => {
  if ('copy' === value) {
    let newRow = {...row}
    delete newRow.id
    delete newRow.createBy
    delete newRow.create_date
    delete newRow.updateBy
    delete newRow.update_date
    newRow.formNo = 'sys_datapermission'
    newRow.name = newRow.name + '(复制)'
    confirmAction('操作确认', '确定复制该数据权限吗？', saveDataUrl, newRow, () => {
      instance.refs.tableView.loadData()
    })
  }
}
</script>
<style scoped>
</style>
