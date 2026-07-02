<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="ETL日志"
                   :class="useModernListSkin ? 'modern-list-page etl-log-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="etl_log">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">ETL日志</h2>
        <p class="modern-list-query-desc">查看 ETL 任务执行日志、输入输出数量、错误数和运行状态。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="s01" label="任务名称" type="input" :props="{placeholder:'任务名称' }"></QueryField>
      <QueryField name="d11" label="日志记录时间" type="date-range" :props="{formatPatter:'yyyy-MM-dd HH:mm:ss'}"></QueryField>
      <QueryField name="status" label="状态" type="input" :props="{placeholder:'状态' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "etl_log_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/etl_log/form";
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
  permission: 'app:etl_log:add'
}, {
  value: 'import',
  text: '导入数据',
  permission: 'app:etl_log:import'
}, {
  value: 'export',
  text: '导出Excel',
  permission: 'app:etl_log:export'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:etl_log:remove'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:etl_log:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:etl_log:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:etl_log:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '任务名称', dataIndex: 'jobname', minWidth: 120, align: 'left'},
    {title: '通道ID', dataIndex: 'channel_id', minWidth: 120, align: 'left'},
    {title: '日志记录时间', dataIndex: 'logdate', minWidth: 120, align: 'center'},
    {title: '错误数', dataIndex: 'errors', minWidth: 120, align: 'left'},
    {title: '状态', dataIndex: 'status', minWidth: 120, align: 'left'},
    {title: '输入数', dataIndex: 'lines_input', minWidth: 120, align: 'left'},
    {title: '拒绝数', dataIndex: 'lines_rejected', minWidth: 120, align: 'left'},
    {title: '输出数', dataIndex: 'lines_output', minWidth: 120, align: 'left'},
  ]
})

</script>
<style scoped>
</style>
