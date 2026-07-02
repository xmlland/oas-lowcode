<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="qrtz_ext_job"  >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="task_name" label="任务名称" :rules="[{ required: true, message: '请输入任务名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.task_name" placeholder="请输入任务名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="cronexpression" label="cron" :rules="[{ required: true, message: '请输入cron' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.cronexpression" placeholder="请输入cron"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="task_description" label="任务描述" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.task_description" placeholder="请输入任务描述" maxlength="100"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="execute_class" label="执行类" :rules="[{ required: true, message: '请输入执行类' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.execute_class" placeholder="请输入执行类"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="exec_param" label="执行参数" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.exec_param" placeholder="请输入执行参数"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="job_status" label="是否启用" :rules="[{ required: true, message: '请选择是否启用' }]">
          <u-select :disabled="disabled" form-type="radio" defaultValue="" v-model:value="formModel.job_status" dict-type="yes_no" placeholder="请选择是否启用" />
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "qrtz_ext_job_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
