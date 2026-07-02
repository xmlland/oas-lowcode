<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_organization" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="org_name" label="组织名称" :validateFirst="true" :rules="[{ required: true, message: '请输入组织名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.org_name" placeholder="请输入组织名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="primaryperson_id" label="组织负责人" >
          <u-user-select :disabled="disabled" defaultValue="" v-model:value="formModel.primaryperson_id" placeholder="请选择组织负责人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="org_number" label="组织编号" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.org_number" placeholder="请输入组织编号"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.numberValidator, message: '请输入整数'}]">
          <u-input type="number" :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="org_effect" label="是否有效" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.org_effect" dict-type="yes_no" placeholder="请选择是否有效"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="dictViewList01" label="组织成员" :validateFirst="true" :rules="[{ required: false, message: '请选择组织成员' }]">
          <u-modal-multi-select formNo="sys_user"
                                searchKey="name" searchLabel="姓名"
                                :modalWidth="1200"
                                :filterData="[{key:'a.useable',type:'eq',value:'1'},{children:[{key:'a.is_sys',type:'isNull'},{or:true,key:'a.is_sys',type:'ne',value:'1'}]}]"
                                :columns="[
                                  {title: '登录名', dataIndex:'login_name',align: 'left'},
                                  {title: '姓名', dataIndex:'name',align: 'left'},
                                ]"
                                codeFiled="login_name"
                                nameFiled="name"
                                :format="item=>{
                                  if (item.code) { return `${item.name} （${item.code}）` } return item.name
                                }"
                                :disabled="disabled" v-model:value="formModel.dictViewList01"
                                placeholder="请选择组织成员"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_organization_form",
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
