<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_office" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="process_type" label="流程类型" >
          <u-input :disabled="disabled" v-model:value="formModel.process_type" placeholder="请输入流程类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sponsor" label="发起人" >
          <u-input :disabled="disabled" v-model:value="formModel.sponsor" placeholder="请输入发起人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="originating_time" label="发起时间" >
          <u-date :disabled="disabled" v-model:value="formModel.originating_time" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择发起时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="current_node" label="当前节点" >
          <u-input :disabled="disabled" v-model:value="formModel.current_node" placeholder="请输入当前节点"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="receiving_time" label="接收时间" >
          <u-date :disabled="disabled" v-model:value="formModel.receiving_time" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择接收时间"/>
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

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>