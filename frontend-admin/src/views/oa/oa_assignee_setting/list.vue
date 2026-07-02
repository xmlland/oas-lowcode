<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="代理管理" :buttons="buttons" formNo="oa_assignee_setting"
                   :class="useModernListSkin ? 'modern-list-page oa-assignee-setting-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">代理管理</h2>
        <p class="modern-list-query-desc">维护流程代理人、代理范围和生效时间。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="user01.name" label="代理人" type="input" queryType="like" :props="{}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oa_assignee_setting_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oa/oa_assignee_setting/form";
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
  permission: 'app:oa_assignee_setting:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oa_assignee_setting:del'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:oa_assignee_setting:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:oa_assignee_setting:edit'
    }, {
      value: 'delete',
      text: '删除',
      permission: 'app:oa_assignee_setting:del',
      color: 'error',
    }
  ],
  columns: [
    {title: '开始时间', dataIndex: 'begin_time', minWidth: 120, align: 'center'},
    {title: '结束时间', dataIndex: 'end_time', minWidth: 120, align: 'center'},
    {title: '代理人', dataIndex: ['assignee','name'], minWidth: 120, align: 'left'},
    {title: '代理流程', dataIndex: 'process_scope', minWidth: 120, align: 'center', dict: 'oa_process_definition', dictMultiple: true},
    {title: '备注', dataIndex: 'remarks', minWidth: 120, align: 'left'},
  ]
})

</script>
<style scoped>
</style>
