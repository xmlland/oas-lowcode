<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="qrtz_ext_job_log"  >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="job_id" label="任务id" :rules="[{ required: true, message: '请选择任务id' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.job_id" type="table" dict-type="qrtz_ext_job" valueField="id" textField="task_name" tableOrderBy="" :tableFilterData="[]" placeholder="请选择任务id"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="exec_user" label="执行人" :rules="[{ required: true, message: '请选择执行人' }]">
          <u-user-select :disabled="disabled" defaultValue="" v-model:value="formModel.exec_user" placeholder="请选择执行人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="job_success" label="是否成功" :rules="[{ required: true, message: '请选择是否成功' }]">
          <u-select :disabled="disabled" form-type="radio" defaultValue="" v-model:value="formModel.job_success" dict-type="yes_no" placeholder="请选择是否成功" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="log_time" label="日志时间" :rules="[{ required: true, message: '请选择日志时间' }]">
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.log_time" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择日志时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="use_times" label="耗时(ms)" :rules="[{ required: true, message: '请输入耗时(ms)' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.use_times" placeholder="请输入耗时(ms)"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="exception_message" label="异常信息" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.exception_message" placeholder="请输入异常信息"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "qrtz_ext_job_log_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
