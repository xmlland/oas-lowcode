<template>
  <u-form v-model:value="formModel" :extendFormData="extendFormData"
          v-model:disabled="disabled" :labelWidth="100" formNo="sys_feedback" :getExtendSaveData="getExtendSaveData">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="module_" label="模块" :rules="[{ required: true, message: '请输入模块' }]">
          <u-select :disabled="!isCommon||disabled" defaultValue="" v-model:value="formModel.module_" :option-data="moduleList.map(item=>{return {member:item,memberName:item}})" placeholder="请选择"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="title_" label="标题" :rules="[{ required: true, message: '请输入标题' }]">
          <u-input :disabled="!isCommon||disabled" defaultValue="" v-model:value="formModel.title_" placeholder="请输入标题" :maxlength="200"/>
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="!isCommon">
        <a-form-item name="desc_" label="描述" :rules="[{ required: true, message: '请输入描述' }]">
          <u-input :disabled="!isCommon||disabled" :textarea="true" defaultValue="" v-model:value="formModel.desc_" placeholder="请输入描述" :maxlength="2000"/>
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="formModel.file_pic">
        <a-form-item name="file_pic" label="图片">
          <u-upload :picture="true" :disabled="!isCommon||disabled" v-model:value="formModel.file_pic" :fileCount="5" :maxSize="50"/>
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="!isCommon">
        <a-form-item name="submit_user" label="提交人" :rules="[{ required: true, message: '请选择提交人' }]">
          <u-user-select :disabled="!isCommon||disabled" defaultValue="" v-model:value="formModel.submit_user" :modalWidth="1000" placeholder="请选择提交人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="!isCommon">
        <a-form-item name="submit_time" label="提交时间" :rules="[{ required: true, message: '请选择提交时间' }]">
          <u-date :disabled="!isCommon||disabled" defaultValue="" v-model:value="formModel.submit_time" formatPatter="yyyy-MM-dd HH:mm:ss" maxValue="" minValue="" placeholder="请选择提交时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="reply_content" :label="isCommon?'内容':'回复'" :rules="[{ required: true, message: '请输入' }]">
          <u-tinymce :disabled="disabled" v-model:value="formModel.reply_content" placeholder="请输入"/>
        </a-form-item>
      </a-col>

    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_feedback_form",
}
</script>
<script setup>

import {computed, ref} from "vue";
import UTinymce from "@/components/form/UTinymce";
import {dateTimeFormat} from "@/lib/tool-date";
import {replaceDefaultValue} from "@/lib/tools";
import config from "@/config";

let formModel = ref({})
let disabled = ref(false)
let moduleList = ref([])
if (config && config.feedback && config.feedback.moduleList) {
  moduleList.value = config.feedback.moduleList
}
let props = defineProps({
  extendFormData: {
    type: Object,
    default: () => {
      return {}
    }
  }
})

let isCommon = computed(() => {
  return props.extendFormData.is_common === '1'
})

const getExtendSaveData = () => {
  return isCommon.value ? {
    reply_time: dateTimeFormat(),
    reply_user: replaceDefaultValue('${currentUser}')
  } : {
    is_reply: '1',
    user_read: '0',
    reply_time: dateTimeFormat(),
    reply_user: replaceDefaultValue('${currentUser}')
  }
}

</script>
<style scoped>
</style>
