<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_subsystem" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="code" label="编码" :validateFirst="true" :rules="[{ required: true, message: '请输入编码' },
        uniqueValidator('sys_subsystem','编码',formModel.id)]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.code" placeholder="请输入编码"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="名称" :validateFirst="true" :rules="[{ required: true, message: '请输入名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="baseurl" label="访问地址" :validateFirst="true" :rules="[{ required: true, message: '访问地址' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.baseurl" placeholder="请输入访问地址"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
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
  name: "sys_subsystem_form",
}
</script>
<script setup>
import {uniqueValidator} from "@/lib/validator";
import {ref, watch} from "vue";
import * as validator from "@/lib/validator"
let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)
watch(()=>formModel.value.code,(newVal)=>{

  formModel.value.baseurl = `/#/?subSystemCode=${newVal}`
},{immediate:true})
</script>
<style scoped>
</style>
