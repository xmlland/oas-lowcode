<template>
  <SingleTableView :modalFull="0"
    ref="tableview_oas_leave_apply"
    :class="useModernListSkin ? 'modern-list-page oas-leave-apply-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :act="true" :queryButton="true" v-model:actStatus="actStatus" :singleTable="singleTable" :modalComponent="modalComponent" :modalTitle="modalTitle" :buttons="buttons" :showActButtons="showActButtons" :actStatusButton="actStatusButton" formNo="oas_leave_apply" moduleName="oas" :modalWidth="1200"  @loadSuccess="loadSuccess">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">请假申请</h2>
        <p class="modern-list-query-desc">维护请假申请、假期类型、时间范围和审批状态。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="apply_no" label="申请单号" type="input" :props="{placeholder:'申请单号' }"></QueryField>
      <QueryField name="applicant" label="申请人" type="userSelect" :props="{placeholder: '申请人'}"></QueryField>
      <QueryField name="leave_type" label="假期类型" type="select" :props="{placeholder:'假期类型',dictType:'oas_leave_apply_leave_type'}"></QueryField>
      <QueryField name="start_time" label="开始时间" type="date-range" :props="{formatPatter:'yyyy-MM-dd HH:mm'}"></QueryField>
      <QueryField name="status" label="状态" type="select" :props="{placeholder:'状态',dictType:'oas_leave_apply_apply_status'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oas_leave_apply_list",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref, watch} from "vue";
import form from "@/views/oas/oas_leave_apply/form";
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
  // 第一个节点有工作流添加按钮
  let isFirst = true //(newValue||'').indexOf('first_user_task') !== -1
  if (isFirst) {
    showActButtons.value = true
  } else {
    showActButtons.value = false
  }
  actStatus.value = isFirst ? 'Unsent' : 'TodoAndDoing'
  actStatusButton.value = {
    showUnsent: isFirst,
    showTodoAndDoing: hasAnyPermission(['app:oas_leave_apply:handle']),
    showDone: hasAnyPermission(['app:oas_leave_apply:handle']),
    showAll: hasAnyPermission(['app:oas_leave_apply:view']),
  }
  modalComponent.value=form
}
watch([() => route.meta.permission], ([newValue]) => {
  setQueryParam(newValue)
  instance.refs.tableview_oas_leave_apply.loadData()
})
setQueryParam(route.meta.permission)

let buttons = ref([{
  value: 'export',
  text: '导出Excel',
  permission: 'app:oas_leave_apply:export',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oas_leave_apply:remove',
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
    {title:'申请单号',dataIndex:'apply_no',minWidth:150,align:'center',sorter:'true',ellipsis:false},
    {title:'申请人',dataIndex:'applicant__name',minWidth:150,align:'center',queryColumn:'user01.name',sorter:'false',ellipsis:false},
    {title:'假期类型',dataIndex:'leave_type',minWidth:150,align:'center',sorter:'true',dictMultiple:'0',ellipsis:false,dict:'oas_leave_apply_leave_type'},
    {title:'开始时间',dataIndex:'start_time',minWidth:150,align:'center',sorter:'true',ellipsis:false},
    {title:'结束时间',dataIndex:'end_time',minWidth:150,align:'center',sorter:'false',ellipsis:false},
    {title:'请假时长',dataIndex:'duration',minWidth:150,align:'center',sorter:'true',ellipsis:false},
    {title:'工作交接人',dataIndex:'handover_to__name',minWidth:150,align:'center',sorter:'false',ellipsis:false},
    {title:'状态',dataIndex:'status',minWidth:150,align:'center',sorter:'false',dictMultiple:'0',ellipsis:false,dict:'oas_leave_apply_apply_status'},
  ]
})

let modalTitle = ref('请假申请');
const loadSuccess = (res) =>{
  modalTitle.value = '请假申请';
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
