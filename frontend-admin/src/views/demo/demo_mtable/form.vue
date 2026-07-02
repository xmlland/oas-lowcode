<template>
  <u-form v-model:value="formModel" :saveBeforeListAddItem="true"
    :subTableType="top" v-model:disabled="disabled" :labelWidth="100" formNo="demo_mtable" mainTableTitle="测试主表" :subTables="subTables" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="name" label="名称" :rules="[{ required: true, message: '请输入名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入名称" :maxlength="100" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="explain_" label="说明" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.explain_" placeholder="请输入说明" :maxlength="500"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="available" label="是否可用" >
          <u-select :disabled="disabled" form-type="radio" defaultValue="" v-model:value="formModel.available" dict-type="yes_no" placeholder="请选择是否可用" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="enclosure" label="附件" >
<!--         oss="minio:main:test" :online="true"-->
          <u-upload :disabled="disabled" v-model:value="formModel.enclosure" :online="true" :fileCount="1" :maxSize="50" />
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "demo_mtable_form",
}
</script>
<script setup>

import {ref} from "vue";
import demo_mtable_sub1_list from "@/views/demo/demo_mtable_sub1/list";
let formModel = ref({})
let disabled = ref(false)
let subTables = ref([
  {title: '测试子表1', component: demo_mtable_sub1_list},
])

</script>
<style scoped>
</style>
