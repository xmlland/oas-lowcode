<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="oa_sys_msg"  >
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="title" label="消息标题" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.title" placeholder="请输入消息标题"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="content_" label="消息内容" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.content_" placeholder="请输入消息内容" maxlength=""/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sender" label="发送人" >
          <u-user-select :disabled="disabled" defaultValue="" v-model:value="formModel.sender" placeholder="请选择发送人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="send_time" label="发送时间" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.send_time" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择发送时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="recipient" label="接收人" >
          <u-user-select :disabled="disabled" defaultValue="" v-model:value="formModel.recipient" placeholder="请选择接收人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="read_time" label="阅读时间" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.read_time" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择阅读时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="status" label="状态" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.status" dict-type="message_type" placeholder="请选择状态"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="types" label="消息类型" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.types" placeholder="请输入消息类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="menu_href" label="菜单链接" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.menu_href" placeholder="请输入菜单链接"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="menu_name" label="菜单名称" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.menu_name" placeholder="请输入菜单名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="record_id" label="记录ID" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.record_id" placeholder="请输入记录ID"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="menu_name_en" label="菜单英文名称" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.menu_name_en" placeholder="请输入菜单英文名称"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oa_sys_msg_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
