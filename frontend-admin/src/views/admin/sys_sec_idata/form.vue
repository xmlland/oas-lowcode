<template>
  <u-form v-model:value="formModel"
    v-model:disabled="disabled" :labelWidth="100" formNo="sys_sec_idata"  parentFormNo="sys_sec_iconfig">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="env" label="环境" :rules="[{ required: true, message: '请输入环境' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.env" placeholder="请输入环境" :maxlength="64" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="data_id" label="记录主键" :rules="[{ required: true, message: '请输入记录主键' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.data_id" placeholder="请输入记录主键" :maxlength="64" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="raw_data" label="原始值" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.raw_data" placeholder="请输入原始值"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="raw_data_now" label="原始值(最新)" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.raw_data_now" placeholder="请输入原始值" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="opt_time" label="操作时间" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.opt_time" formatPatter="yyyy-MM-dd HH:mm:ss" maxValue="" minValue="" placeholder="请选择操作时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="integrity_value" label="完整性值" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.integrity_value" placeholder="请输入完整性值" :maxlength="64" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="opt_name" label="操作人姓名" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.opt_name" placeholder="请输入操作人姓名" :maxlength="64" />
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_sec_idata_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
