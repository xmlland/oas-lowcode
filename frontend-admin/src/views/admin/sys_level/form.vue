<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_level" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="name" label="职务名称" :validateFirst="true" :rules="[{ required: true, message: '请输入职务名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入职务名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="code" label="职务代码" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.code" placeholder="请输入职务代码"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="useable" label="状态" :validateFirst="true" :rules="[{ required: true, message: '请选择状态' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.useable" dict-type="sys_useable" placeholder="请选择状态"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序号" :validateFirst="true" :rules="[{ required: true, message: '请输入排序号' },{ validator : customValidator.numberValidator, message: '请输入整数'}]">
          <u-input type="number" :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序号"/>
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
  name: "sys_level_form",
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
