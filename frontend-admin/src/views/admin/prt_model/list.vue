<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="模板"
                   :class="useModernListSkin ? 'modern-list-page prt-model-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="prt_model">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">模板管理</h2>
        <p class="modern-list-query-desc">维护门户模板、模板类型、默认状态和启用状态。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="name" label="模板名称" type="input" :props="{placeholder:'模板名称' }"></QueryField>
      <QueryField name="types" label="模板类型" type="select" :props="{placeholder:'模板类型',dictType:'prt_model_types'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "prt_model_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/prt_model/form";
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
  permission: 'app:prt_model:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:prt_model:remove'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:prt_model:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:prt_model:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:prt_model:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '所属站点', dataIndex: ['site','name'], minWidth: 120, align: 'left'},
    {title: '模板名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '模板类型', dataIndex: 'types', minWidth: 120, align: 'center', dict: 'prt_model_types'},
    {title: '默认', dataIndex: 'if_default', minWidth: 120, align: 'center', dict: 'yes_no'},
    {title: '状态', dataIndex: 'useable', minWidth: 120, align: 'center', dict: 'sys_useable'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
