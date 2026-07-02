<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_file_" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="name_" label="附件名称" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name_" placeholder="请输入附件名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="ext_" label="扩展名" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.ext_" placeholder="请输入扩展名"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="type_" label="附件类型" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.type_" placeholder="请输入附件类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="size_" label="附件大小" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.size_" placeholder="请输入附件大小"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sec_flag_" label="是否加密" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.sec_flag_" dict-type="yes_no" placeholder="请选择是否加密"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="secret_level" label="密级" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.secret_level" dict-type="sys_sec_level" placeholder="请选择密级"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="group_id_" label="分组" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.group_id_" placeholder="请输入分组"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="path_" label="附件路径" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.path_" placeholder="请输入附件路径"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="thumb_path_" label="缩略图路径" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.thumb_path_" placeholder="请输入缩略图路径"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="to_pdf_" label="转换PDF状态" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.to_pdf_" placeholder="请输入转换PDF状态"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="pdf_path_" label="pdf路径" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.pdf_path_" placeholder="请输入pdf路径"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="visit_count_" label="访问次数" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.visit_count_" placeholder="请输入访问次数"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="upload_user_id_" label="上传人" >
          <u-user-select :disabled="disabled" defaultValue="" v-model:value="formModel.upload_user_id_" placeholder="请选择上传人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="upload_time_" label="上传时间" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.upload_time_" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择上传时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="duration_" label="播放时长(秒)" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.duration_" placeholder="请输入播放时长(秒)"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="desc_" label="描述" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.desc_" placeholder="请输入描述"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_file__form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
