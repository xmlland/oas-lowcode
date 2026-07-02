<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_datapermission" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="name" label="名称" :validateFirst="true" :rules="[{ required: true, message: '请输入名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="main_table" label="主表" :validateFirst="true" :rules="[{ required: true, message: '请输入主表' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.main_table" placeholder="请输入主表"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="expression" label="表达式" :validateFirst="true" :rules="[{ required: true, message: '请输入表达式' }]">
          <template #extra>
            <p>{company}: 当前用户所属机构编码</p>
            <p>{office}: 当前用户所属部门编码</p>
            <p>{companyAreaCode}: 当前用户所属机构行政区编码</p>
            <p>{officeAreaCode}: 当前用户所属部门行政区编码</p>
            <p>{roles}: 当前用户角色id列表 , 逗号分隔</p>
            <p>{userId}: 当前用户id</p>
          </template>
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.expression" placeholder="请输入表达式"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="remarks" label="备注信息" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.remarks" placeholder="请输入备注信息"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="dictViewList01" label="数据角色" :validateFirst="true" :rules="[{ required: false, message: '请选择数据角色' }]">
          <u-modal-multi-select formNo="sys_datarole"
                                searchKey="name" searchLabel="名称"
                                :modalWidth="1200"
                                :filterData="[{key:'a.useable',type:'eq',value:'1'}]"
                                :columns="[
                                  {title: '英文名称', dataIndex:'enname',align: 'left'},
                                  {title: '名称', dataIndex:'name',align: 'left'},
                                ]"
                                codeFiled="enname"
                                nameFiled="name"
                                :format="item=>{
                                  if (item.code) { return `${item.name} （${item.code}）` } return item.name
                                }"
                                :disabled="disabled" v-model:value="formModel.dictViewList01"
                                placeholder="请选择数据角色"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_datapermission_form",
}
</script>
<script setup>

import {ref} from "vue";
import * as validator from "@/lib/validator"
let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
