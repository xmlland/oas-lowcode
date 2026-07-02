<template>
  <SingleTableView
    :queryButton="true"
    :singleTable="singleTable"
    :modalComponent="modalComponent"
    modalTitle="执行日志"
    :class="useModernListSkin ? 'modern-list-page qrtz-job-log-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :buttons="buttons"
    formNo="qrtz_ext_job_log"
  >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">调度日志</h2>
        <p class="modern-list-query-desc">查看定时任务执行结果、执行人、日志时间、耗时和异常信息。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField :widthMultiple="0.5" name="job_id" label="任务" type="select" :props="{placeholder:'任务',type:'table',dictType:'qrtz_ext_job',valueField:'id',textField:'task_name',tableOrderBy:'',tableFilterData:[]}"></QueryField>
      <QueryField :widthMultiple="0.5" name="exec_user" label="执行人" type="userSelect" :props="{placeholder: '执行人'}"></QueryField>
      <QueryField name="log_time" label="日志时间" type="date-range" :props="{formatPatter:'yyyy-MM-dd HH:mm:ss'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "qrtz_ext_job_log_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/qrtz/qrtz_ext_job_log/form";
import config from "@/config";
let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([/*{
  value: 'add',
  text: '添加',
  permission: 'app:qrtz_ext_job_log:add',
}, */{
  value: 'export',
  text: '导出Excel',
  permission: 'app:qrtz_ext_job_log:export',
}/*, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:qrtz_ext_job_log:remove',
}*/])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:qrtz_ext_job_log:view',
    }/*, {
      value: 'edit',
      text: '编辑',
      permission: 'app:qrtz_ext_job_log:edit',
    }*/
  ],
  columns: [
    {title: '任务', dataIndex: 'job_id_name', minWidth: 120, align: 'left'},
    {title: '执行人', dataIndex: ['exec_user','name'], minWidth: 120, align: 'left'},
    {title: '是否成功', dataIndex: 'job_success', minWidth: 120, align: 'center', dict: 'yes_no'},
    {title: '日志时间', dataIndex: 'log_time', minWidth: 120, align: 'center'},
    {title: '耗时(ms)', dataIndex: 'use_times', minWidth: 120, align: 'center'},
    {title: '异常信息', dataIndex: 'exception_message', minWidth: 120, align: 'left'},
  ]
})

</script>
<style scoped>
</style>
