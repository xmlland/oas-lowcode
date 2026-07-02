<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="oa_urge_setting" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="urge_user" label="督办人" :validateFirst="true" :rules="[{ required: true, message: '请选择督办人' }]">
          <u-user-select :disabled="disabled" defaultValue="" v-model:value="formModel.urge_user" placeholder="请选择督办人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="urge_limit" label="督办期限" :validateFirst="true" :rules="[{ required: true, message: '请选择督办期限' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.urge_limit" dict-type="oa_urge_setting_limit" placeholder="请选择督办期限"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="urge_process" label="督办流程" :validateFirst="true" :rules="[{ required: true, message: '请选择督办流程' }]">
          <u-select :disabled="disabled" :multiple="true" form-type="checkbox" defaultValue="" v-model:value="formModel.urge_process" dict-type="act_category" placeholder="请选择督办流程" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="urge_content" label="督办内容" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.urge_content" placeholder="请输入督办内容"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oa_urge_setting_form",
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
