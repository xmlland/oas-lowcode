<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="s01" label="流程分类"  :rules="[{ required: true, message: '请选择流程分类' }]">
          <u-select :disabled="disabled" form-type="select" v-model:value="formModel.s01" dict-type="act_category" placeholder="请选择流程分类"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="s02" label="流程文件"  :rules="[{ required: true, message: '请上传流程文件' }]">
          <u-upload :disabled="disabled" v-model:value="formModel.s02" fileCount="1" />
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "act_process_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>