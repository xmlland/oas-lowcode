<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="oa_assignee_setting" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="begin_time" label="开始时间" :validateFirst="true" :rules="[{ required: true, message: '请选择开始时间' }]">
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.begin_time" formatPatter="yyyy-MM-dd HH:mm" placeholder="请选择开始时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="end_time" label="结束时间" :validateFirst="true" :rules="[{ required: true, message: '请选择结束时间' }]">
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.end_time" formatPatter="yyyy-MM-dd HH:mm" placeholder="请选择结束时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="assignee" label="代理人" :validateFirst="true" :rules="[{ required: true, message: '请选择代理人' }]">
          <u-user-select :disabled="disabled" defaultValue="" v-model:value="formModel.assignee" placeholder="请选择代理人"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="process_scope" label="代理流程" >
          <u-select :disabled="disabled" :multiple="true" form-type="checkbox" defaultValue="" v-model:value="formModel.process_scope" dict-type="act_category" placeholder="请选择代理流程" />
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
  name: "oa_assignee_setting_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
