<template>
  <SingleTableView ref="tableView" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="流程管理" :buttons="buttons" formNo="act_re_model"
                   :class="useModernListSkin ? 'modern-list-page act-process-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   @clickRow="clickRow" @clickButton="clickButton" :url="url">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">流程管理</h2>
        <p class="modern-list-query-desc">维护流程定义、部署版本、启停状态和模型转换。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="s01" label="流程分类" type="select" :props="{placeholder:'流程分类',dictType:'act_category'}"></QueryField>
    </template>
  </SingleTableView>
  <u-modal ref="modal" @clickOperate="uploadProcessFile" :showOk="false" :extendButtons="[{value:'uploadProcessFile',type:'primary',text:'确认',validate:true}]">
    <act_process_form ref="uploadForm" :height="500"/>
  </u-modal>
</template>

<script>
export default {
  name: "act_process_list",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/oa/act_process/form";
import {confirmAction, postAction} from "@/api/action";
import Act_process_form from "@/views/oa/act_process/form";
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
  value: 'add-model',
  text: '上传流程文件',
  permission: 'app:act_process:add'
}])

let url = ref({
  list: "act/process/list",
})

let singleTable = ref({
  rowButtons: [
    {
      value: 'active',
      text: '激活',
      visibleFilter(row) {
        return (row.suspended)
      }
    }, {
      value: 'suspend',
      text: '挂起',
      color: 'error',
      visibleFilter(row) {
        return (false === row.suspended)
      }
    }, {
      value: 'deleteProc',
      text: '删除',
      color: 'error',
    }, {
      value: 'deleteProcAll',
      text: '全部删除',
      color: 'error',
    }, {
      value: 'toModel',
      text: '转换',
    }
  ],
  columns: [
    {title: '流程分类', dataIndex: 'category', minWidth: 120, align: 'left', dict: 'act_category'},
    {title: '流程ID', dataIndex: 'id', minWidth: 120, align: 'center'},
    {title: '流程标识', dataIndex: 'key', minWidth: 120, align: 'center'},
    {title: '流程名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '流程版本', dataIndex: 'version', minWidth: 120, align: 'center'},
    {title: '部署时间', dataIndex: 'deploymentTime', minWidth: 120, align: 'center'},
  ],
  initParam:{
    "parent": {"id": ""},
  }
})

const clickRow = ({value, row}) => {
  if('active' === value) {
    confirmAction('操作提示','确认要激活吗？',`act/process/update/active?procDefId=${row.id}`, {},()=>{
      saveSuccess()
    })
  } else if ('suspend' === value) {
    confirmAction('操作提示','确认要挂起吗？',`act/process/update/suspend?procDefId=${row.id}`, {},()=>{
      saveSuccess()
    })
  } else if ('deleteProc' === value) {
    confirmAction('操作提示','确认要删除吗？',`act/process/delete?deploymentId=${row.deploymentId}`, {},()=>{
      saveSuccess()
    })
  } else if ('deleteProcAll' === value) {
    confirmAction('操作提示','确认要删除所有版本吗？',`act/process/deleteAll?key=${row.key}`, {},()=>{
      saveSuccess()
    })
  } else if ('toModel' === value) {
    confirmAction('操作提示','确认要转换成模型吗？',`act/process/toModel?procDefId=${row.id}`, {},()=>{
      saveSuccess()
    })
  }
}

const saveSuccess = () => {
  instance.refs.tableView.loadData()
}

const clickButton = ({value}) => {
  if (value === 'add-model') {
    instance.refs.modal.open('上传', {}, false)
  }
}

const uploadProcessFile = (item,callback,data) => {
  callback(postAction,'act/process/deploy',data[0])
}
</script>
<style scoped>
</style>
