<template>
  <u-form v-model:value="formModel"
    v-model:disabled="disabled" :labelWidth="100" formNo="sys_sec_iconfig" mainTableTitle="完整性保护配置" :subTables="subTables" >
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="table_name" label="表名" :rules="[{ required: true, message: '请选择表名' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.table_name" type="table" dict-type="gen_table" valueField="name" textField="comments" tableOrderBy=""
                    :tableFilterData="[]" formatFuncStr="return option.name + '|' + option.comments" placeholder="请选择表名"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="column_names" label="保护列" :rules="[{ required: true, message: '请选择保护列' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.column_names" type="url" dict-type="" valueField="name" textField="comments" tableOrderBy=""
                    dataUrl="gen/genTable/editForm" :postData="{name:formModel.table_name}" :multiple="true"
                    :tableFilterData="[]" formatFuncStr="return option.name + '|' + option.comments" placeholder="请选择保护列"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_sec_iconfig_form",
}
</script>
<script setup>

import {ref} from "vue";
import sys_sec_idata_list from "@/views/admin/sys_sec_idata/list";
let formModel = ref({})
let disabled = ref(false)
let subTables = ref([
  {title: '完整性保护记录', component: sys_sec_idata_list},
])

</script>
<style scoped>
</style>
