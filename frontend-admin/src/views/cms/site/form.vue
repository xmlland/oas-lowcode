<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="prt_site" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="code" label="站点编号" :validateFirst="true" :rules="[{ required: true, message: '请输入站点编号' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.code" placeholder="请输入站点编号"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="站点名称" :validateFirst="true" :rules="[{ required: true, message: '请输入站点名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入站点名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="http_path" label="站点HTTP" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.http_path" placeholder="请输入站点HTTP"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="keyword" label="关键字" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.keyword" placeholder="请输入关键字"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="resource_" label="资源位置" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.resource_" placeholder="请输入资源位置"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="status" label="状态" :validateFirst="true" :rules="[{ required: true, message: '请选择状态' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.status" dict-type="sys_useable" placeholder="请选择状态"/>
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
  name: "cms_site_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
