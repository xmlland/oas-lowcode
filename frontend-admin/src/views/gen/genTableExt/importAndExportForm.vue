<template>
  <u-form @loadSuccess="loadSuccess" v-model:value="formModel" v-model:disabled="disabled" :get-data-action="postAction" :url="{
    getData:'gen/genTable/editForm',save:'gen/genTable/saveImportAndExport'
  }" :getDataKey="['data','genTable']" :label-width="100" form-no="gen_table" :get-extend-save-data="getExtendSaveData">
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="importTemplateFile" label="导入模板">
          <u-upload :accepts="['xlsx']" :file-count="1" :disabled="disabled" v-model:value="formModel.importTemplateFile"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="importList" label="导入属性">
          <u-select form-type="checkbox" :multiple="true" v-model:value="formModel.importList" :option-data="optionData"
                    value-field="javaField" :format="row=>`${row.name} ${row.comments}`" :checkbox-nowrap="false" :checkboxSpan="6"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="exportTemplateFile" label="导出模板">
          <u-upload :accepts="['xlsx']" :file-count="1" :disabled="disabled" v-model:value="formModel.exportTemplateFile"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="exportList" label="导出属性">
          <u-select form-type="checkbox" :multiple="true" v-model:value="formModel.exportList" :option-data="optionData"
                    value-field="javaField" :format="row=>`${row.name} ${row.comments}`" :checkbox-nowrap="false" :checkboxSpan="6"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "importAndExportForm"
}
</script>
<script setup>

import UForm from "@/components/form/UForm";
import {computed, ref} from "vue";
import UUpload from "@/components/form/UUpload";
import USelect from "@/components/form/USelect";
import {postAction} from "@/api/action";
import {defaultFieldArray} from "@/views/gen/dynamicFormItem";

let formModel = ref({})
let disabled = ref(false)

let displayFormItemArr = ref([])
let hideFormItemArr = ref([])
let json = []
let optionData = computed(() => {
  let arr = []
  displayFormItemArr.value.forEach(item => {
    arr.push(item)
  })
  hideFormItemArr.value.forEach(item => {
    arr.push(item)
  })
  return arr
})

const loadSuccess = (res) => {
  if (!res.data) {
    //如果是新增
    res.data = {
      data: [],
      genTable: {}
    }
    defaultFieldArray.forEach(item => {
      let obj = {}
      Object.assign(obj, item)
      res.data.data.push(obj)
    })
  }
  json = res.data.data
  let _resultData = res.data.data
  _resultData.sort((a, b) => a.formSort - b.formSort);
  let displayArr = _resultData.filter(item => item.isForm === '1')
  let hiddenArr = _resultData.filter(item => item.isForm !== '1')
  displayFormItemArr.value = []
  hideFormItemArr.value = []
  displayArr.forEach(item => {
    displayFormItemArr.value.push(item)
  })
  hiddenArr.forEach(item => {
    hideFormItemArr.value.push(item)
  })
}

const getExtendSaveData = () => {
  return {
    json: json
  }
}
</script>
<style scoped>

</style>
