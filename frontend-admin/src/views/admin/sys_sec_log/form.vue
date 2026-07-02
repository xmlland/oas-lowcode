<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_sec_log" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="account_" label="登陆账号" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.account_" placeholder="请输入登陆账号"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="content_" label="操作内容" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.content_" placeholder="请输入操作内容"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="time_" label="操作时间" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.time_" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择操作时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="ip_" label="操作者IP" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.ip_" placeholder="请输入操作者IP"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="type_" label="操作类型" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.type_" placeholder="请输入操作类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="result_" label="操作结果" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.result_" placeholder="请输入操作结果"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_sec_log_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
