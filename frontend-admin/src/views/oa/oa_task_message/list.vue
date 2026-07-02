<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="消息提醒" :buttons="buttons" formNo="oa_task_message"
                   :class="useModernListSkin ? 'modern-list-page oa-task-message-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryButton="false"
                   :queryArea="queryArea">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">消息提醒</h2>
        <p class="modern-list-query-desc">维护流程操作消息、提醒范围和排序规则。</p>
      </div>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oa_task_message_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oa/oa_task_message/form";
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
  permission: 'app:oa_task_message:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oa_task_message:remove'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:oa_task_message:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:oa_task_message:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:oa_task_message:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '操作', dataIndex: 'operation', minWidth: 120, align: 'center', dict: 'oa_task_operation', dictMultiple: true},
    {title: '范围', dataIndex: 'process_scope', minWidth: 120, align: 'center', dict: 'act_category', dictMultiple: true},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
