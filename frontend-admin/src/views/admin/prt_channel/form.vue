<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="prt_channel" >
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
        <a-form-item name="parent" label="上级栏目" :validateFirst="true">
          <u-tree-select formNo="prt_channel" :disabled="disabled" defaultValue="" v-model:value="formModel.parent" placeholder="请选择上级栏目"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="栏目名称" :validateFirst="true" :rules="[{ required: true, message: '请输入栏目名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入栏目名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name_en" label="栏目英文名称" :validateFirst="true" :rules="[{ required: true, message: '请输入栏目英文名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name_en" placeholder="请输入栏目英文名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="channel_mod" label="栏目模板" :validateFirst="true" :rules="[{ required: true, message: '请选择栏目模板' }]">
          <u-modal-select formNo="prt_model"
                          searchKey="name" searchLabel="名称"
                          :columns="[
                          {title: '名称', dataIndex:'name',align: 'left'},
                          ]"
                          :formUpdateMap="{'targetInputName':'gridFieldKey'}" v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.channel_mod" placeholder="请选择栏目模板"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="info_mod" label="文章模板" :validateFirst="true" :rules="[{ required: true, message: '请选择文章模板' }]">
          <u-modal-select formNo="prt_model"
                          searchKey="name" searchLabel="名称"
                          :columns="[
                          {title: '名称', dataIndex:'name',align: 'left'},
                          ]"
                          :formUpdateMap="{'targetInputName':'gridFieldKey'}" v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.info_mod" placeholder="请选择文章模板"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="info_type" label="文章类型" :validateFirst="true" :rules="[{ required: true, message: '请选择文章类型' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.info_type" dict-type="prt_info_types" placeholder="请选择文章类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="charge_user" label="负责人" :validateFirst="true" :rules="[{ required: true, message: '请选择负责人' }]">
          <u-users-select :disabled="disabled" defaultValue="" v-model:value="formModel.charge_user" placeholder="请选择负责人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="if_comment" label="可评论" :validateFirst="true" :rules="[{ required: true, message: '请选择可评论' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.if_comment" dict-type="yes_no" placeholder="请选择可评论"/>
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
  name: "prt_channel_form",
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
