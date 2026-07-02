<template>
  <u-form v-model:value="formModel"
    v-model:disabled="disabled" :labelWidth="100" formNo="demo_mtable_sub1"  parentFormNo="demo_mtable">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="user_id" label="用户" >
          <u-modal-select formNo="sys_user"
                          :searchKey="[]"
                          :searchLabel="[]"
                          :columns="[
                           {title:'登录名',dataIndex:'login_name',align:'left'},
                           {title:'姓名',dataIndex:'name',align:'left'},
                          ]"
                          :targetFormNo="demo_mtable_sub1" :targetField="user_id" :modalWidth="1200" :modalTitle="请选择" :nameDataIndex="name" 
                          :filterData="[]" 
                          :searchConfig="{}" 
                          :formUpdateMap={}
                          v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.user_id" placeholder="请选择用户"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="user_id" label="用户" >
          <u-modal-select formNo="sys_user"
                          :searchKey="[]"
                          :searchLabel="[]"
                          :columns="[
                           {title:'登录名',dataIndex:'login_name',align:'left'},
                           {title:'姓名',dataIndex:'name',align:'left'},
                          ]"
                          :targetFormNo="demo_mtable_sub1" :targetField="user_id" :modalWidth="1200" :modalTitle="请选择" :nameDataIndex="name" 
                          :filterData="[]" 
                          :searchConfig="{}" 
                          :formUpdateMap={}
                          v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.user_id" placeholder="请选择用户"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="姓名" :rules="[{ required: true, message: '请输入姓名' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入姓名" :maxlength="50" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="姓名" :rules="[{ required: true, message: '请输入姓名' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入姓名" :maxlength="50" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="gender" label="性别" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.gender" dict-type="sex" placeholder="请选择性别"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="gender" label="性别" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.gender" dict-type="sex" placeholder="请选择性别"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="id_number" label="身份证号" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.id_number" placeholder="请输入身份证号" :maxlength="64" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="id_number" label="身份证号" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.id_number" placeholder="请输入身份证号" :maxlength="64" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="birthday" label="生日" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.birthday" formatPatter="yyyy-MM-dd" maxValue="" minValue="" placeholder="请选择生日"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="birthday" label="生日" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.birthday" formatPatter="yyyy-MM-dd" maxValue="" minValue="" placeholder="请选择生日"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="years_of_work" label="工作年限" :rules="[{ validator : customValidator.numberValidator, message: '请输入整数'}]">
          <u-input type="number" :disabled="disabled" defaultValue="" v-model:value="formModel.years_of_work" placeholder="请输入工作年限"  />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="years_of_work" label="工作年限" :rules="[{ validator : customValidator.numberValidator, message: '请输入整数'}]">
          <u-input type="number" :disabled="disabled" defaultValue="" v-model:value="formModel.years_of_work" placeholder="请输入工作年限"  />
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "demo_mtable_sub1_form",
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
