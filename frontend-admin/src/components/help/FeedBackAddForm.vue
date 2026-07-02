<template>
  <u-form ref="formRef" v-model:value="formModel" v-model:disabled="disabled" :labelWidth="60">

    <a-row>
      <a-col :span="24">
        <a-form-item name="module_" label="模块" extra="问题所在的系统模块"
                     :rules="[{ required: true, message: '请选择' }]">
          <u-select :disabled="disabled" defaultValue="" v-model:value="formModel.module_" :option-data="moduleList.map(item=>{return {member:item,memberName:item}})" placeholder="请选择"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="title_" label="标题" extra="方便我们更直观的了解您要反馈内容"
                     :rules="[{ required: true, message: '请输入' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.title_" placeholder="请输入"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="desc_" label="描述" extra="请对您反馈的内容进行详细的描述，方便我们更好的解决您的问题"
                     :rules="[{ required: true, message: '请输入' }]">
          <u-input :textarea="true" :disabled="disabled"
                   :autoSize="{minRows: 6, maxRows: 12}"
                   defaultValue=""
                   v-model:value="formModel.desc_" placeholder="请输入" :maxlength="2000"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="file_pic" label="图片" extra="附上一些图片可以让我们更充分的了解您的问题">
          <u-upload :picture="true" :is-edit="true" :disabled="disabled" v-model:value="formModel.file_pic" :fileCount="5" :maxSize="5"/>
        </a-form-item>
      </a-col>
      <a-col :span="24" style="text-align: center;">
        <a-button type="primary" @click="submitData">提交</a-button>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "FeedBackAddForm"
}
</script>
<script setup>
import {ref} from "vue";
import UForm from "@/components/form/UForm";
import UInput from "@/components/form/UInput";
import UUpload from "@/components/form/UUpload";
import config from "@/config";
import USelect from "@/components/form/USelect";
import {confirmAction} from "@/api/action";
import {saveDataUrl} from "@/api/api";
import {dateTimeFormat} from "@/lib/tool-date";
import {replaceDefaultValue} from "@/lib/tools";

let moduleList = ref([])
if (config && config.feedback && config.feedback.moduleList) {
  moduleList.value = config.feedback.moduleList
}
let formModel = ref({})
let disabled = ref(false)

let emits = defineEmits(['close'])

const formRef = ref(null)
const submitData = () => {
  formRef.value.validateFields().then((values) => {
    console.log('values', values)
    values.formNo = 'sys_feedback'
    values.is_submit = '1'
    values.submit_time = dateTimeFormat()
    values.submit_user = replaceDefaultValue('${currentUser}')
    confirmAction('操作确认', '确定提交吗', saveDataUrl, values, (res) => {
      emits('close', ({setCurrentKey}) => {
        setCurrentKey('2')
      })
    })
  }).catch((e) => {
    console.log('error', e)
  })
}
</script>
<style scoped>

</style>
