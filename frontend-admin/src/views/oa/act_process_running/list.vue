<template>
  <SingleTableView ref="tableView" @clickRow="clickRow" @clickButton="clickButton" :singleTable="singleTable" modalTitle="" :buttons="buttons"
                   :class="useModernListSkin ? 'modern-list-page act-process-running-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">运行中流程</h2>
        <p class="modern-list-query-desc">查看运行中流程实例、当前环节和挂起状态。</p>
      </div>
    </template>
    <template #queryFields>
<!--      <QueryField name="s02" label="流程实例ID" type="input"></QueryField>-->
      <QueryField name="processDefinitionName" label="流程名称" type="input"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "act_process_running",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import {prompt} from "@/lib/tools";
import {postActionByParams} from "@/api/action";
import {Modal} from "ant-design-vue";
import config from "@/config";
// let url= ref from ""
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})
let buttons = ref([{
  value: 'batch-delProc',
  text: '删除',
  class: 'batch-delete',
  disabledFilter: (rows) => rows.length === 0
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      //permission: 'app:oa_urge_setting:view'
    }, {
      value: 'edit',
      text: '编辑',
      //permission: 'app:oa_urge_setting:edit'
    }, {
      value: 'deleteProc',
      text: '删除',
      //permission: 'app:oa_urge_setting:del',
      color: 'error',
    }
  ],
  columns: [
    {title: '流程名称', dataIndex: 'processDefinitionName', minWidth: 120, align: 'left'},
    {title: '执行ID', dataIndex: 'id', minWidth: 120, align: 'center'},
    {title: '流程实例ID', dataIndex: 'processInstanceId', minWidth: 120, align: 'center'},
    {title: '当前环节', dataIndex: 'activityName', minWidth: 120, align: 'left'},
    {title: '是否挂起', dataIndex: 'suspended', minWidth: 120, align: 'center'},
  ],
  dataUrl: "act/process/runningList",
  initParam: {
    "parent": {"id": ""},
  }
})
let instance = getCurrentInstance();
const saveSuccess = () => {
  instance.refs.tableView.saveSuccess()
}
const clickRow = ({value, row, index}) => {
  if ('deleteProc' === value) {
    prompt({
      title: '请输入删除原因',
      callback: (reason) => {
        postActionByParams('act/process/deleteProcIns', {processInstanceId: row.processInstanceId, reason: reason}).then(res => {
          saveSuccess()
        })
      }
    })
  }
}

const clickButton = ({value}) => {
  if (value === 'batch-delProc') {
    let rows = instance.refs.tableView.getSelectedRows()
    if (rows.length === 0) {
      Modal.warning({
        title: '提示',
        content: '请选择要删除的数据',
      })
      return
    }
    prompt({
      title: '请输入删除原因',
      callback: (reason) => {
        let actions = []
        rows.forEach(row => {
          actions.push(postActionByParams('act/process/deleteProcIns', {processInstanceId: row.processInstanceId, reason: reason}))
        })
        Promise.all(actions).then(res => {
          saveSuccess()

          instance.refs.tableView.getTableRef().updateAllPageSelectedRows({deleteRowKeys: rows.map(row => row.id)})
        })

      }
    })
  }
}
</script>
<style scoped>
</style>

