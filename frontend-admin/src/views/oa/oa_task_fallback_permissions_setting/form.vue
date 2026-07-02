<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="oa_task_fallback_permissions_setting"  >
    <a-row :gutter="16">
      <a-col :span="12" v-show="false">
        <a-form-item name="proc_def_key" label="procDefKey" :rules="[{ required: true, message: '请输入procDefKey' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.proc_def_key" placeholder="请输入procDefKey"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="task_def_key" label="任务节点" :rules="[{ required: true, message: '请选择任务节点' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue=""
                    v-model:value="formModel.task_def_key" type="table"
                    dict-type="oa_task_setting" valueField="user_task_id"
                    textField="user_task_name" tableOrderBy=""
                    :tableFilterData="[{
                      key:'a.PROC_DEF_KEY',
                      value:procDefKey,
                      type:'eq'}
                    ]"
                    :format="(option, index)=>{
                      return option.user_task_name+'-'+option.permission_name
                    }"
                    placeholder="请选择任务节点"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="returnable_to" label="可退回至" :rules="[{ required: true, message: '请选择可退回至' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue=""
                    v-model:value="formModel.returnable_to" type="table"
                    dict-type="oa_task_setting" valueField="user_task_id"
                    textField="user_task_name" tableOrderBy=""
                    :tableFilterData="[{
                      key:'a.PROC_DEF_KEY',
                      value:procDefKey,
                      type:'eq'}
                    ]"
                    :format="(option, index)=>{
                      return option.user_task_name+'-'+option.permission_name
                    }"
                    placeholder="请选择可退回至"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oa_task_fallback_permissions_setting_form",
}
</script>
<script setup>

import {ref,inject} from "vue";
let procDefKey = inject('getProcDefKey');
let formModel = ref({
  proc_def_key:procDefKey
})
let disabled = ref(false)

</script>
<style scoped>
</style>
