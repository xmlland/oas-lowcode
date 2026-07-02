<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_dictionary" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="parent" label="上级" :validateFirst="true">
          <u-tree-select formNo="sys_dictionary" :disabled="disabled" defaultValue="" v-model:value="formModel.parent" placeholder="请选择上级"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="code" label="编码" :validateFirst="true" :rules="[{ required: true, message: '请输入编码' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.code" placeholder="请输入编码"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="名称" :validateFirst="true" :rules="[{ required: true, message: '请输入名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name_en" label="英文名称" :validateFirst="true" :rules="[{ required: true, message: '请输入英文名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name_en" placeholder="请输入英文名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.numberValidator, message: '请输入整数'}]">
          <u-input type="number" :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="parent_code" label="上级编码" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.parent_code" placeholder="请输入上级编码"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="view_flag" label="查询显示" :validateFirst="true" :rules="[{ required: true, message: '请选择查询显示' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.view_flag" dict-type="yes_no" placeholder="请选择查询显示"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="edit_flag" label="编辑显示" :validateFirst="true" :rules="[{ required: true, message: '请选择编辑显示' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.edit_flag" dict-type="yes_no" placeholder="请选择编辑显示"/>
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
  name: "sys_dictionary_form",
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
