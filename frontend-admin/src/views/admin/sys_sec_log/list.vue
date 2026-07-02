<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="系统日志" :buttons="buttons" formNo="sys_sec_log"
                   :class="useModernListSkin ? 'modern-list-page sys-sec-log-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   @clickButton="clickButton">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">系统日志</h2>
        <p class="modern-list-query-desc">查询系统操作日志、操作结果和完整性校验状态，辅助审计追踪。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField :widthMultiple="0.5" name="account_" label="登陆账号" type="input" :props="{placeholder:'登陆账号' }"></QueryField>
      <QueryField :widthMultiple="0.5" name="content_" label="操作内容" type="input" :props="{placeholder:'操作内容' }"></QueryField>
      <QueryField name="time_" label="操作时间" type="date-range" :props="{formatPatter:'yyyy-MM-dd HH:mm:ss',showQuickSelect:true}"></QueryField>
      <QueryField :widthMultiple="0.5" name="ip_" label="操作者IP" type="input" :props="{placeholder:'操作者IP' }"></QueryField>
      <QueryField :widthMultiple="0.5" name="type_" label="操作类型" type="input" :props="{placeholder:'操作类型' }"></QueryField>
      <QueryField :widthMultiple="0.5" name="result_" label="操作结果" type="input" :props="{placeholder:'操作结果' }"></QueryField>
      <QueryField :widthMultiple="0.5" name="check_flag" label="是否篡改" type="select" :props="{placeholder:'是否篡改',dictType:'yes_no'}"></QueryField>

    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_sec_log_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_sec_log/form";
import {confirmAction} from "@/api/action";
import {Modal} from "ant-design-vue";
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
  permission: 'app:sys_sec_log:add'
}, {
  value: 'import',
  text: '导入数据',
  permission: 'app:sys_sec_log:import'
}, {
  value: 'export',
  text: '导出Excel',
  permission: 'app:sys_sec_log:export'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_sec_log:remove'
}, {
  value: 'check',
  text: '校验完整性',
  permission: 'app:sys_sec_log:export',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_sec_log:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_sec_log:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_sec_log:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '登陆账号', dataIndex: 'account_', minWidth: 120, align: 'left'},
    {title: '操作内容', dataIndex: 'content_', minWidth: 120, align: 'left'},
    {title: '操作时间', dataIndex: 'time_', minWidth: 120, align: 'center'},
    {title: '操作者IP', dataIndex: 'ip_', minWidth: 120, align: 'left'},
    {title: '操作类型', dataIndex: 'type_', minWidth: 120, align: 'left'},
    {title: '操作结果', dataIndex: 'result_', minWidth: 120, align: 'left'},
  ]
})
const clickButton = ({value}) => {

  if ('check' === value) {
    confirmAction('操作确认', '确定校验完整性保护吗？', 'system/secLog/checkIntegrityProtection', {}, () => {
      Modal.info({
        title: '操作结果',
        content: '调用成功！'
      })
    })
  }
}
</script>
<style scoped>
</style>
