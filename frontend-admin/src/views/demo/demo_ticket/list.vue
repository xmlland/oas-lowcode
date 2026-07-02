<template>
  <SingleTableView :modalFull="0"
    :class="useModernListSkin ? 'modern-list-page demo-ticket-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :act="true" :queryButton="true" v-model:actStatus="actStatus" :singleTable="singleTable" :modalComponent="modalComponent"
                   ref="tableViewDemoTicket"
                   :modalTitle="modalTitle" :buttons="buttons"
                   :showActButtons="showActButtons"
                   :actStatusButton="actStatusButton" formNo="demo_ticket" moduleName="demo" :modalWidth="1200"
                   @loadSuccess="loadSuccess">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">测试工单</h2>
        <p class="modern-list-query-desc">验证工单流程、状态切换、查询条件和审批操作。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="create_by" label="创建者" type="userSelect" :props="{placeholder: '创建者'}"></QueryField>
      <QueryField name="create_date" label="创建时间" type="date-range" :props="{formatPatter:'yyyy-MM-dd'}"></QueryField>
      <QueryField name="status" label="工单状态" type="select" :props="{placeholder:'工单状态',dictType:'demo_ticket_status'}"></QueryField>
      <QueryField name="title" label="工单标题" type="input" :props="{placeholder:'工单标题' }"></QueryField>
      <QueryField name="issue_resolved" label="问题是否解决" type="select" :props="{placeholder:'问题是否解决',dictType:'demo_ticket_issue_resolved'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "demo_ticket_list",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref, watch} from "vue";
import form from "@/views/demo/demo_ticket/form";
import {hasAnyPermission} from "@/lib/tools";
import {useRoute} from "vue-router";
import config from "@/config";
let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let instance = getCurrentInstance()
let route = useRoute()
let actStatus = ref('path')
let actStatusButton = ref({})
let showActButtons = ref(true)

const setQueryParam = (newValue) => {
  // 只有第一个节点存在工作流的添加按钮
  let isFirst = (newValue||'').indexOf('first_user_task') !== -1
  if (isFirst) {
    showActButtons.value = true
  } else {
    showActButtons.value = false
  }
  actStatus.value = isFirst ? 'Unsent' : 'TodoAndDoing'
  actStatusButton.value = {
    showUnsent: isFirst,
    showTodoAndDoing: hasAnyPermission(['app:demo_ticket:handle']),
    showDone: hasAnyPermission(['app:demo_ticket:handle']),
    showAll: hasAnyPermission(['app:demo_ticket:view']),
  }
  modalComponent.value=form
}
watch([() => route.meta.permission], ([newValue]) => {
  setQueryParam(newValue)
  instance.refs.tableViewDemoTicket.loadData()
})

setQueryParam(route.meta.permission)

let buttons = ref([{
  value: 'export',
  text: '导出Excel',
  permission: 'app:demo_ticket:export',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:demo_ticket:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'viewAct',
      text: '查看',
    }, {
      value: 'editAct',
      text: '编辑',
      visibleFilter(row) {
        return !row.proc_ins_id && actStatus.value === 'Unsent'
      }
    }, {
      value: 'editAct',
      text: '办理',
      visibleFilter(row) {
        return actStatus.value === 'TodoAndDoing'
      }
    }
  ],
  columns: [
    {title:'创建者',dataIndex:'create_by',minWidth:150,align:'center',queryColumn:'createBy.id',sorter:'false',ellipsis:false},
    {title:'创建时间',dataIndex:'create_date',minWidth:150,align:'center',sorter:'false',ellipsis:false},
    {title:'工单状态',dataIndex:'status',minWidth:150,align:'left',sorter:'false',dictMultiple:'0',ellipsis:false,dict:'demo_ticket_status'},
    {title:'工单标题',dataIndex:'title',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'问题描述',dataIndex:'description',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'回复信息',dataIndex:'reply',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'问题是否解决',dataIndex:'issue_resolved',minWidth:150,align:'center',sorter:'false',dict:'demo_ticket_issue_resolved',ellipsis:false},
  ]
})

//根据节点名称定义弹窗名称
let modalTitle = ref('工单');
const loadSuccess = (res) =>{
  modalTitle.value = '工单';
  if(!res.rows){
    return;
  }
  let rows = res.rows;
  if(rows.length>0){
    let t = rows[0]?.act?.taskName;
    if(t){
      modalTitle.value = t;
    }
  }
}
</script>
<style scoped>
</style>
