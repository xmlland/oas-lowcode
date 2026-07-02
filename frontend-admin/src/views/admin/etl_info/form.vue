<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="etl_info" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="name" label="ETL名称" :validateFirst="true" :rules="[{ required: true, message: '请输入ETL名称' }]">
          <u-input :disabled="disabled" v-model:value="formModel.name" placeholder="请输入ETL名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="internal" label="执行频率" >
          <u-select :disabled="disabled" form-type="select" v-model:value="formModel.internal" dict-type="etl_internal" placeholder="请选择执行频率"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="status" label="异常状态" >
          <u-input :disabled="disabled" v-model:value="formModel.status" placeholder="请输入异常状态"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="scriptfile" label="脚本文件" >
          <u-upload :disabled="disabled" v-model:value="formModel.scriptfile"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="remarks" label="参数信息" >
          <u-input :disabled="disabled" :textarea="true" v-model:value="formModel.remarks" placeholder="请输入参数信息"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="open_" label="是否开启" >
          <u-select :disabled="disabled" form-type="select" v-model:value="formModel.open_" dict-type="yes_no" placeholder="请选择是否开启"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "etl_info_form",
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