<template>
  <SingleTableView :modalFull="false"
    :class="useModernListSkin ? 'modern-list-page oas-receive-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :act="true" :queryButton="true" v-model:actStatus="actStatus" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="收文处理笺" :buttons="buttons" :actStatusButton="actStatusButton" formNo="oas_receive" moduleName="oas" :modalWidth="1200" >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">收文处理笺</h2>
        <p class="modern-list-query-desc">维护收文登记、来文单位、文号和办理状态。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="sec_level" label="密级" type="select" :props="{placeholder:'密级',dictType:'sys_sec_level'}"></QueryField>
      <QueryField name="year_" label="年" type="date-range" :props="{formatPatter:'yyyy'}"></QueryField>
      <QueryField name="doc_no" label="文号" type="input" :props="{placeholder:'文号' }"></QueryField>
      <QueryField name="title" label="标题" type="input" :props="{placeholder:'标题' }"></QueryField>
<!--      <QueryField name="sending_unit" label="来文单位" type="input" :props="{placeholder:'来文单位' }"></QueryField>-->
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oas_receive_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oas/oas_receive/form";
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
  value: 'export',
  text: '导出Excel',
  permission: 'app:oas_receive:export',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oas_receive:remove',
}])

let actStatusButton = ref({
  showUnsent: hasAnyPermission(['app:oas_receive:add']),
  showTodoAndDoing: hasAnyPermission(['app:oas_receive:handle']),
  showDone: hasAnyPermission(['app:oas_receive:handle']),
  showAll: hasAnyPermission(['app:oas_receive:view']),
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
    {title:'密级',dataIndex:'sec_level',minWidth:150,align:'center',sorter:'true',dictMultiple:'0',ellipsis:false,dict:'sys_sec_level'},
    {title:'年',dataIndex:'year_',minWidth:150,align:'center',sorter:'false',ellipsis:false},
    {title:'文号',dataIndex:'doc_no',minWidth:150,align:'center',sorter:'true',ellipsis:false},
    {title:'缓急',dataIndex:'urgency',minWidth:150,align:'center',sorter:'false',dictMultiple:'0',ellipsis:false,dict:'urgency_degree'},
    {title:'内部编号',dataIndex:'inner_no',minWidth:150,align:'center',sorter:'false',ellipsis:false},
    {title:'标题',dataIndex:'title',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'来文单位',dataIndex:'sending_unit',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'日期',dataIndex:'receive_date',minWidth:150,align:'center',sorter:'true',ellipsis:false},
  ]
})

</script>
<style scoped>
</style>
