<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="act_re_model" :url="url" getDataKey="data">
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="category" label="流程分类" :validateFirst="true" :rules="categoryRules">
          <u-select :disabled="disabled || disableWhenUpdate" form-type="select" defaultValue="" v-model:value="formModel.category" dict-type="act_category" placeholder="请选择流程分类"/>
          <u-input :disabled="disabled || disableWhenUpdate" v-model:value="formModel.new_category" placeholder="或新建流程分类"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="模型名称" :validateFirst="true" :rules="[{ required: true, message: '请输入模型名称' }]">
          <u-input :disabled="disabled || disableWhenUpdate" v-model:value="formModel.name" placeholder="请输入模型名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="metaInfo" label="模型描述" >
          <u-input :disabled="disabled || disableWhenUpdate" :textarea="true" defaultValue="" v-model:value="formModel.metaInfo.description" placeholder="请输入模型描述"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="office" label="归属机构" :validateFirst="true">
          <u-tree-select formNo="sys_office" :disabled="disabled" defaultValue="" v-model:value="formModel.office" placeholder="请选择归属机构"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="metaInfo" label="表单路径" :validateFirst="true" >
          <u-input :disabled="disabled" v-model:value="formModel.metaInfo.scope" placeholder="请输入表单路径"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oa_sent_form",
}
</script>
<script setup>

import {computed, ref} from "vue";
let formModel = ref({metaInfo: {}, office: {}})
let disabled = ref(false)
let disableWhenUpdate = computed(() => {
  return formModel.value.id !== undefined
})

const url = {
  getData: 'service/model/getModelData',
  save: 'service/model/updateModel'
}

// 自定义校验函数：至少填写一个
const validateCategory = (rule, value, callback) => {
  // 如果选择了流程分类或填写了新分类名，通过验证
  if (formModel.value.category || formModel.value.new_category) {
    callback()
  } else {
    callback(new Error('请选择流程分类或输入新的流程分类'))
  }
}

// 分类字段的校验规则
const categoryRules = [
  { validator: validateCategory, trigger: 'blur' }
]
</script>
<style scoped>
</style>