<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_office" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="parent" label="上级机构" :validateFirst="true">
          <u-tree-select formNo="sys_office" :disabled="disabled" defaultValue="" v-model:value="formModel.parent" placeholder="请选择上级机构"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="area_id" label="行政区" :validateFirst="true" :rules="[{ required: true, message: '请选择行政区' }]">
          <u-area rootAreaId="1" :freeChoice="true" :disabled="disabled" defaultValue="" v-model:value="formModel.area_id" placeholder="请选择行政区"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="code" label="机构编码" :validateFirst="true" :rules="[{ required: true, message: '请输入机构编码' },
        uniqueValidator('sys_office','机构编码',formModel.id)]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.code" placeholder="请输入机构编码"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="机构名称" :validateFirst="true" :rules="[{ required: true, message: '请输入机构名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入机构名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name_en" label="机构英文名称" :validateFirst="true" :rules="[{ required: true, message: '请输入机构英文名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name_en" placeholder="请输入机构英文名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="short_code" label="机构简码" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.short_code" placeholder="请输入机构简码"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="short_name" label="机构简称" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.short_name" placeholder="请输入机构简称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="types" label="机构类型" :validateFirst="true" :rules="[{ required: true, message: '请选择机构类型' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.types" dict-type="sys_office_type" placeholder="请选择机构类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="grade" label="级别" :validateFirst="true" :rules="[{ required: true, message: '请输入级别' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.grade" placeholder="请输入级别"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="primary_person" label="主负责人" >
          <u-user-select :disabled="disabled" defaultValue="" v-model:value="formModel.primary_person" placeholder="请选择主负责人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="deputy_person" label="副负责人" >
          <u-user-select :disabled="disabled" defaultValue="" v-model:value="formModel.deputy_person" placeholder="请选择副负责人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="master" label="联系人" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.master" placeholder="请输入联系人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="phone" label="电话" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.phone" placeholder="请输入电话"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="fax" label="传真" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.fax" placeholder="请输入传真"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="email" label="电子邮件" :rules="[{ validator : customValidator.emailValidator, message: '请输入电子邮件'}]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.email" placeholder="请输入电子邮件"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="zip_code" label="邮编" :rules="[{ validator : customValidator.zipCodeValidator, message: '请输入邮政编码'}]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.zip_code" placeholder="请输入邮编"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="address" label="地址" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.address" placeholder="请输入地址"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="useable" label="状态" :validateFirst="true" :rules="[{ required: true, message: '请选择状态' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.useable" dict-type="sys_useable" placeholder="请选择状态"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.numberValidator, message: '请输入整数'}]">
          <u-input type="number" :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="remarks" label="备注信息" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.remarks" placeholder="请输入备注信息"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_office_form",
}
</script>
<script setup>
import {uniqueValidator} from "@/lib/validator";
import {ref} from "vue";
import * as validator from "@/lib/validator"
let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
