<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_setting"  >
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="KEY_" label="参数名称" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.KEY_" placeholder="请输入参数名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="VALUE_" label="参数值" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.VALUE_" placeholder="请输入参数值"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="NAME" label="参数说明" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.NAME" placeholder="请输入参数说明"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_setting_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
