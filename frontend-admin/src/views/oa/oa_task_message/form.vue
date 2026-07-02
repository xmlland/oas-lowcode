<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="oa_task_message" >
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="operation" label="操作" :validateFirst="true" :rules="[{ required: true, message: '请选择操作' }]">
          <u-select :disabled="disabled" :multiple="true" form-type="checkbox" defaultValue="" v-model:value="formModel.operation" dict-type="oa_task_operation" placeholder="请选择操作" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="process_scope" label="范围" :validateFirst="true" :rules="[{ required: true, message: '请选择范围' }]">
          <u-select :disabled="disabled" :multiple="true" form-type="checkbox" defaultValue="" v-model:value="formModel.process_scope" dict-type="act_category" placeholder="请选择范围" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="remarks" label="备注信息" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.remarks" placeholder="请输入备注信息"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oa_task_message_form",
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
