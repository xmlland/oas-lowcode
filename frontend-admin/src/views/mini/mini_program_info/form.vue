<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="100" formNo="mini_program_info"  >
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="app_name" label="名称" :rules="[{ required: true, message: '请输入名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.app_name" placeholder="请输入名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="app_id" label="AppID" :rules="[{ required: true, message: '请输入AppID' },
        uniqueValidator('mini_program_info','AppID',formModel.id)]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.app_id" placeholder="请输入AppID"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="app_secret" label="AppSecret" :rules="[{ required: true, message: '请输入AppSecret' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.app_secret" placeholder="请输入AppSecret"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="role_ids" label="角色" >
          <u-select :disabled="disabled" :multiple="true" defaultValue="" v-model:value="formModel.role_ids" type="table" dict-type="sys_role" valueField="id" textField="name" tableOrderBy="" :tableFilterData="[{key:'a.useable',type:'eq',value:'1'}]" placeholder="请选择角色id"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="data_role_ids" label="数据角色" >
          <u-select :disabled="disabled" :multiple="true" form-type="select" defaultValue="" v-model:value="formModel.data_role_ids" type="table" dict-type="sys_datarole" valueField="id" textField="name" tableOrderBy="" :tableFilterData="[{key:'a.useable',type:'eq',value:'1'}]" placeholder="请选择数据角色"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "mini_program_info_form",
}
</script>
<script setup>

import {ref} from "vue";
import {uniqueValidator} from "@/lib/validator";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
