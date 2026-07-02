<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_weekday"  >
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="year_" label="年度" :rules="[{ required: true, message: '请选择年度' }]">
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.year_" formatPatter="yyyy" placeholder="请选择年度"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="date_" label="日期" :rules="[{ required: true, message: '请选择日期' }]">
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.date_" formatPatter="yyyy-MM-dd" placeholder="请选择日期"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="is_weekday" label="是否工作日" :rules="[{ required: true, message: '请选择是否工作日' }]">
          <u-select :disabled="disabled" form-type="radio" defaultValue="" v-model:value="formModel.is_weekday" dict-type="yes_no" placeholder="请选择是否工作日" />
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_weekday_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
