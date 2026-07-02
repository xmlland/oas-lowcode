<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_area" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="parent" label="上级区域" :validateFirst="true">
          <u-tree-select formNo="sys_area" :disabled="disabled" defaultValue="" v-model:value="formModel.parent" placeholder="请选择上级区域"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="code" label="区域编码" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.code" placeholder="请输入区域编码"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="区域名称" :validateFirst="true" :rules="[{ required: true, message: '请输入区域名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入区域名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="types" label="区域类型" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.types" dict-type="area_type" placeholder="请选择区域类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_area_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
