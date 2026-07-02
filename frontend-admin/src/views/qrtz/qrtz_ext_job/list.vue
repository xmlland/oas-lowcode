<template>
  <SingleTableView
    ref="tableView"
    @clickRow="clickRow"
    :queryButton="true"
    :singleTable="singleTable"
    :modalComponent="modalComponent"
    modalTitle="任务列表"
    :class="useModernListSkin ? 'modern-list-page qrtz-job-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :buttons="buttons"
    formNo="qrtz_ext_job"
  >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">调度任务</h2>
        <p class="modern-list-query-desc">维护定时任务、执行类、cron 表达式和启用状态，支持手动触发任务执行。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="task_name" label="任务名称" type="input" :props="{placeholder:'任务名称' }"></QueryField>
      <QueryField name="job_status" label="是否启用" type="select" :props="{placeholder:'是否启用',dictType:'yes_no'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "qrtz_ext_job_list",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/qrtz/qrtz_ext_job/form";
import {confirmAction} from "@/api/action";
import {Modal} from "ant-design-vue";
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
  permission: 'app:qrtz_ext_job:add',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:qrtz_ext_job:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:qrtz_ext_job:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:qrtz_ext_job:edit',
    }, {
      value: 'execute',
      text: '执行',
      permission: 'app:qrtz_ext_job:edit',
    }/*, {
      value: 'pause',
      text: '暂停',
      permission: 'app:qrtz_ext_job:edit',
    }, {
      value: 'resume',
      text: '恢复',
      permission: 'app:qrtz_ext_job:edit',
    }*/
  ],
  columns: [
    {title: '任务名称', dataIndex: 'task_name', minWidth: 200, align: 'left'},
    {title: 'cron', dataIndex: 'cronexpression', minWidth: 120, align: 'left'},
    {title: '任务描述', dataIndex: 'task_description', minWidth: 120, align: 'left'},
    {title: '执行类', dataIndex: 'execute_class', minWidth: 200, align: 'left'},
    {title: '执行参数', dataIndex: 'exec_param', minWidth: 200, align: 'left'},
    {title: '是否启用', dataIndex: 'job_status', minWidth: 120, align: 'center', dict: 'yes_no', status: true},
    {title: '上次执行时间', dataIndex: 'prev_fire_time', align: 'center', width: 180},
    {title: '下次执行时间', dataIndex: 'next_fire_time', align: 'center', width: 180},/*
    {title: '执行状态', dataIndex: 'trigger_state', align: 'center', width: 150},*/
  ]
})
const saveSuccess = () => {
  instance.refs.tableView.loadData()
}
const clickRow = ({value, row}) => {
  if ('execute' === value) {
    confirmAction('操作确认', '是否执行该定时任务？', 'qrtz/qrtzExtJob/execute?id=' + row.id, {}, () => {
      Modal.info({
        content: '调用成功！'
      })
      saveSuccess()
    })
  }
}
</script>
<style scoped>
</style>
