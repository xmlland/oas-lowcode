<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="prt_model" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="site" label="所属站点" :validateFirst="true" :rules="[{ required: true, message: '请选择所属站点' }]">
          <u-modal-select formNo="prt_site"
                          searchKey="name" searchLabel="名称"
                          :columns="[
                          {title: '名称', dataIndex:'name',align: 'left'},
                          {title: '编码', dataIndex:'code',align: 'left'},
                          ]"
                          :formUpdateMap="{'targetInputName':'gridFieldKey'}" v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.site" placeholder="请选择所属站点"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="模板名称" :validateFirst="true" :rules="[{ required: true, message: '请输入模板名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入模板名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="types" label="模板类型" :validateFirst="true" :rules="[{ required: true, message: '请选择模板类型' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.types" dict-type="prt_model_types" placeholder="请选择模板类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="model_path" label="模板路径" :validateFirst="true" :rules="[{ required: true, message: '请输入模板路径' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.model_path" placeholder="请输入模板路径"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="pic_path" label="图片路径" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.pic_path" placeholder="请输入图片路径"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="label" label="标签" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.label" placeholder="请输入标签"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="if_default" label="默认" :validateFirst="true" :rules="[{ required: true, message: '请选择默认' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.if_default" dict-type="yes_no" placeholder="请选择默认"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="useable" label="状态" :validateFirst="true" :rules="[{ required: true, message: '请选择状态' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.useable" dict-type="sys_useable" placeholder="请选择状态"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "prt_model_form",
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
