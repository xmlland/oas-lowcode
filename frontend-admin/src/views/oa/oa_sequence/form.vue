<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="oa_sequence" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="key_" label="编码" :validateFirst="true" :rules="[{ required: true, message: '请输入编码' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.key_" placeholder="请输入编码"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="名称" :validateFirst="true" :rules="[{ required: true, message: '请输入名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="year" label="当前年度" :validateFirst="true" :rules="[{ required: true, message: '请输入当前年度' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.year" placeholder="请输入当前年度"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="cycle_by_year" label="按年循环" :validateFirst="true" :rules="[{ required: true, message: '请选择按年循环' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.cycle_by_year" dict-type="annual_cycle" placeholder="请选择按年循环"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="start_value" label="起始值" :validateFirst="true" :rules="[{ required: true, message: '请输入起始值' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.start_value" placeholder="请输入起始值"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="current_value" label="当前值" :validateFirst="true" :rules="[{ required: true, message: '请输入当前值' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.current_value" placeholder="请输入当前值"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="last_year_value" label="上年度值" :validateFirst="true" :rules="[{ required: true, message: '请输入上年度值' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.last_year_value" placeholder="请输入上年度值"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="remarks" label="备注" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.remarks" placeholder="请输入备注"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oa_sequence_form",
}
</script>
<script setup>

import {ref} from "vue";
import * as validator from "@/lib/validator"
let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
