<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="prt_information" >
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
        <a-form-item name="parent" label="栏目名称" :validateFirst="true">
          <u-tree-select formNo="prt_channel" :disabled="disabled" defaultValue="" v-model:value="formModel.parent" placeholder="请选择栏目名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="title" label="文章标题" :validateFirst="true" :rules="[{ required: true, message: '请输入文章标题' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.title" placeholder="请输入文章标题"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="author" label="作者" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.author" placeholder="请输入作者"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="origin" label="来源" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.origin" placeholder="请输入来源"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="keyword" label="关键字" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.keyword" placeholder="请输入关键字"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="description" label="文章摘要" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.description" placeholder="请输入文章摘要"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="types" label="文章类型" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.types" dict-type="prt_info_types" placeholder="请选择文章类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="if_play" label="轮播" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.if_play" dict-type="yes_no" placeholder="请选择轮播"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="content" label="文章内容" :validateFirst="true" >
          <u-tinymce :disabled="disabled" defaultValue="" v-model:value="formModel.content"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="typesimg" label="图片" >
          <u-upload :picture="true" :disabled="disabled" v-model:value="formModel.typesimg" :fileCount="1"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="files" label="附件" >
          <u-upload :disabled="disabled" v-model:value="formModel.files"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="top_level" label="固顶级别" :validateFirst="true" :rules="[{ required: true, message: '请选择固顶级别' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.top_level" dict-type="prt_top_level" placeholder="请选择固顶级别"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="top_date" label="固顶到期日期" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.top_date" formatPatter="yyyy-MM-dd" placeholder="请选择固顶到期日期"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="draft_name" label="拟稿人" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.draft_name" placeholder="请输入拟稿人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="draft_date" label="拟稿时间" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.draft_date" formatPatter="yyyy-MM-dd" placeholder="请选择拟稿时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="release_name" label="发布人" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.release_name" placeholder="请输入发布人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="if_full_visible" label="全员可见" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.if_full_visible" dict-type="yes_no" placeholder="请选择全员可见"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="release_date" label="发布时间" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.release_date" formatPatter="yyyy-MM-dd" placeholder="请选择发布时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="status" label="状态" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.status" dict-type="prt_info_status" placeholder="请选择状态"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "prt_information_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
