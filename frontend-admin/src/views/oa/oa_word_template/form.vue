<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="oa_word_template"  >
    <a-row :gutter="16">
      <a-col :span="20">
        <a-form-item name="word_name" label="编码" :rules="[{ required: true, message: '请输入模板编码' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.word_code" placeholder="请输入模板编码"/>
        </a-form-item>
      </a-col>
    </a-row>
    <a-row :gutter="16">
      <a-col :span="20">
        <a-form-item name="word_name" label="名称" :rules="[{ required: true, message: '请输入模板名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.word_name" placeholder="请输入模板名称"/>
        </a-form-item>
      </a-col>
    </a-row>
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="template_file" label="模板文件" :rules="[{ required: true, message: '请上传模板文件' }]">
          <u-upload idPrefix="oaword_" :fileCount="1" :disabled="disabled" v-model:value="formModel.template_file"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oa_word_template_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
