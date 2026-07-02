<template>
  <SingleTableView :modalFull="0"
    :class="useModernListSkin ? 'modern-list-page oas-send-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :act="true" :queryButton="true" v-model:actStatus="actStatus" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="发文处理笺" :buttons="buttons" :actStatusButton="actStatusButton" formNo="oas_send" moduleName="oas" :modalWidth="1200" >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">发文处理笺</h2>
        <p class="modern-list-query-desc">维护发文文单、密级、文号和办理状态。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="sec_level" label="密级" type="select" :props="{placeholder:'密级',dictType:'sys_sec_level'}"></QueryField>
      <QueryField name="doc_no" label="文号" type="input" :props="{placeholder:'文号' }"></QueryField>
      <QueryField name="title" label="标题" type="input" :props="{placeholder:'标题' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oas_send_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oas/oas_send/form";
import {hasAnyPermission} from "@/lib/tools";
import config from "@/config";
let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let actStatus = ref('Unsent')
let buttons = ref([{
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oas_send:remove',
}])

let actStatusButton = ref({
  showUnsent: hasAnyPermission(['app:oas_send:add']),
  showTodoAndDoing: hasAnyPermission(['app:oas_send:handle']),
  showDone: hasAnyPermission(['app:oas_send:handle']),
  showAll: hasAnyPermission(['app:oas_send:view']),
})

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
    {title:'密级',dataIndex:'sec_level',minWidth:150,align:'left',sorter:'false',dict:'sys_sec_level',ellipsis:false},
    {title:'紧急程度',dataIndex:'urgency',minWidth:150,align:'left',sorter:'false',dictMultiple:'0',ellipsis:false,dict:'urgency_degree'},
    {title:'文号',dataIndex:'doc_no',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'标题',dataIndex:'title',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'拟稿人',dataIndex:'drafter',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'发文日期',dataIndex:'date_of_publication',minWidth:150,align:'center',sorter:'false',ellipsis:false},
    {title:'生效日期',dataIndex:'effective_date',minWidth:150,align:'center',sorter:'false',ellipsis:false},
  ]
})

</script>
<style scoped>
</style>
