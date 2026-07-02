<template>
  <SingleTableView  :queryButton="true" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="任务回退权限" :buttons="buttons" formNo="oa_task_fallback_permissions_setting" :autoHeight="true"
                    :class="useModernListSkin ? 'modern-list-page oa-task-fallback-permissions-setting-list-view' : ''"
                    :queryArea="queryArea">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">任务回退权限</h2>
        <p class="modern-list-query-desc">维护流程节点之间的可退回范围和权限关系。</p>
      </div>
    </template>
    <template #queryFields>
<!--      <QueryField name="proc_def_key" label="procDefKey" type="input" :props="{placeholder:'procDefKey' }"></QueryField>-->
      <QueryField
          name="task_def_key" label="任务节点" type="select"
          :width="400"
          :props="{
            placeholder:'任务节点',
            type:'table',
            dictType:'oa_task_setting',
            valueField:'user_task_id',
            textField:'user_task_name',
            tableOrderBy:'',
            format:(option, index)=>{
              return option.user_task_name+'-'+option.permission_name
            },
            tableFilterData:[{
              key:'a.PROC_DEF_KEY',
              value:procDefKey,
              type:'eq'}]}"></QueryField>
      <QueryField name="returnable_to" label="可退回至" type="select"
                  :width="400"
                  :props="{
                    placeholder:'可退回至',
                    type:'table',
                    dictType:'oa_task_setting',
                    valueField:'user_task_id',
                    textField:'user_task_name',
                    tableOrderBy:'',
                    format:(option, index)=>{
                      return option.user_task_name+'-'+option.permission_name
                    },
                    tableFilterData:[{
                      key:'a.PROC_DEF_KEY',
                      value:procDefKey,
                      type:'eq'}]}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oa_task_fallback_permissions_setting_list",
}
</script>
<script setup>
import {computed, ref, provide} from "vue";
import form from "@/views/oa/oa_task_fallback_permissions_setting/form";
import config from "@/config";
let modalComponent = form
let props = defineProps({
  procDefKey:{
    type: String,
    default: null
  }
})

provide("getProcDefKey",props.procDefKey);
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  // permission: 'app:oa_task_fallback_permissions_setting:add',
}, {
  value: 'batch-delete',
  text: '删除',
  // permission: 'app:oa_task_fallback_permissions_setting:remove',
}])

let singleTable = computed(()=>{
  let s = {
    rowButtons: [
      {
        value: 'view',
        text: '查看',
        // permission: 'app:oa_task_fallback_permissions_setting:view',
      }, {
        value: 'edit',
        text: '编辑',
        // permission: 'app:oa_task_fallback_permissions_setting:edit',
      }
    ],
    columns: [
      {title: '任务节点', dataIndex: 'task_def_key_name', minWidth: 120, align: 'center'
        ,customRender: ({record}) => {
          return record.task_def_key_name + " - " + record.task_def_permission_name
        }},
      {title: '可退回至', dataIndex: 'returnable_to_name', minWidth: 120, align: 'center',customRender: ({record}) => {
          return record.returnable_to_name + " - " + record.returnable_to_permission_name
        }},
    ]
  }
  if(props.procDefKey){
    s.initParam={
      'proc_def_key':props.procDefKey,
      queryParamType:{
        'proc_def_key':''
      }
    }
  }
  return s;
})

</script>
<style scoped>
</style>
