<template>
  <SingleTableView :singleTable="singleTable"  modalTitle="" :buttons="[]" formNo=""
                   :class="useModernListSkin ? 'modern-list-page act-process-history-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">历史流程</h2>
        <p class="modern-list-query-desc">查询已流转流程实例、执行记录和结束状态。</p>
      </div>
    </template>
    <template #queryFields>
      <!--      <QueryField name="s01" label="流程实例ID" type="select" :props="{placeholder:'督办期限',dictType:'oa_urge_setting_limit'}"></QueryField>-->
      <QueryField name="s02" label="流程实例ID" type="input"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "act_process_running",
}
</script>
<script setup>
import {computed, ref} from "vue";
import config from "@/config";
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let singleTable = ref({
  rowButtons: [
    /*{
      value: 'view',
      text: '查看',
      //permission: 'app:oa_urge_setting:view'
    }, {
      value: 'edit',
      text: '编辑',
      //permission: 'app:oa_urge_setting:edit'
    }, {
      value: 'delete',
      text: '删除',
      //permission: 'app:oa_urge_setting:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '流程发起人', dataIndex: 'startUserId', minWidth: 120, align: 'left'},
    {title: '执行ID', dataIndex: 'id', minWidth: 120, align: 'center'},
    {title: '流程实例ID', dataIndex: 'processInstanceId', minWidth: 120, align: 'center'},
    {title: '流程定义ID', dataIndex: 'processDefinitionId', minWidth: 120, align: 'left'},
    {title: '流程启动时间', dataIndex: 'startTime', minWidth: 120, align: 'center'},
    {title: '流程结束时间', dataIndex: 'endTime', minWidth: 120, align: 'center'},
    {title: '流程状态', dataIndex: 'deleteReason', minWidth: 120, align: 'center'},
  ],
  dataUrl:"act/process/historyList",
  initParam:{
    "parent": {"id": ""},
  }
})

</script>
<style scoped>
</style>

