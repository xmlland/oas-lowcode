<template>
  <div style="display: inline-block">
    <a-button type="link" @click="addDict">添加字典</a-button>
    <u-modal ref="dictModal" :width="1000" :mask="false" :customOK="true" @clickOk="saveDict">
      <u-form ref="dictForm" v-model:value="dictModel" :labelWidth="100">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item name="code" label="编码" :validateFirst="true" :rules="[{ required: true, message: '请输入编码' },
            uniqueValidator('sys_dictionary','编码',dictModel.id)]">
              <u-input v-model:value="dictModel.code" placeholder="请输入编码"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="name" label="名称" :validateFirst="true" :rules="[{ required: true, message: '请输入名称' }]">
              <u-input v-model:value="dictModel.name" placeholder="请输入名称"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.numberValidator, message: '请输入整数'}]">
              <u-input type="number" defaultValue="" v-model:value="dictModel.sort" placeholder="请输入排序"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="dictItems" label="字典项" extra="value text 多个以回车分隔">
              <a-button type="link" @click="tranOption">转换option</a-button>
              <u-input :textarea="true" defaultValue="" v-model:value="dictModel.dictItems"
                       :autoSize="{minRows: 6, maxRows: 20}" placeholder="请输入备注信息" :maxlength="1000000"/>
            </a-form-item>
          </a-col>
        </a-row>
      </u-form>
    </u-modal>
  </div>
</template>

<script>
export default {
  name: "QuickAddDict"
}
</script>
<script setup>
import {getCurrentInstance, nextTick, ref} from "vue";
import UModal from "@/components/modal/UModal";
import UForm from "@/components/form/UForm";
import {uniqueValidator} from "@/lib/validator";
import * as validator from "@/lib/validator"
import {saveDataAction} from "@/api/api";
import {replaceAll} from "@/lib/tools";

let customValidator = ref(validator)
let instance = getCurrentInstance();

let props = defineProps({
  code: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: ''
  },
})
let dictModel = ref({
  code: '',
  name: '',
  sort: '',
  dictItems: ''
})
const addDict = () => {
  dictModel.value = {
    code: props.code,
    name: props.name,
    sort: '',
    dictItems: ''
  }
  nextTick(() => {
    instance.refs.dictModal.open('添加字典')
  })
}

let emits = defineEmits(['addSuccess'])

const saveDict = () => {
  instance.refs.dictForm.validateFields().then(() => {
    let saveData = {
      parent: {
        id: 'data-params'
      },
      parent_code: 'data-params',
      view_flag: '1',
      edit_flag: '1',
    }
    saveData.code = dictModel.value.code
    saveData.name = dictModel.value.name
    saveData.name_en = dictModel.value.code
    saveData.sort = dictModel.value.sort
    let dictItems = dictModel.value.dictItems.split('\n')
    let dictArr = []
    dictItems.forEach((item, index) => {
      let obj = {
        view_flag: '1',
        edit_flag: '1',
        sort: (index + 1) * 10,
        parent_code: dictModel.value.code,
        formNo: 'sys_dictionary'
      }
      let arr = replaceAll(item, '  ', '').split(' ')
      if (arr.length < 2) {
        return
      }
      obj.code = arr[0]
      obj.name = arr[1]
      obj.name_en = arr[0]
      dictArr.push(obj)
    })
    saveData['sys_dictionary'] = dictArr

    saveDataAction('sys_dictionary', saveData).then(() => {
      emits('addSuccess', saveData.code)
      instance.refs.dictModal.close()
    })
  })
}

const tranOption = () => {
  //输入框输入得是多个 html option  标签 使用正则匹配
  let dictItems = dictModel.value.dictItems
// 正则表达式匹配 value 和 text 多行模式
  let regex = /<option[^>]*>(.*?)<\/option>/g;
  let match = regex.exec(dictItems);
  let res = []
  while (match) {

    let option = match[0]
    //从option 中匹配value
    let value = /value="([^"]*)"/.exec(option)[1]
    let text = match[1]
    res.push(value + ' ' + text)

    match = regex.exec(dictItems)
  }
  dictModel.value.dictItems = res.join('\n')
}
</script>
<style scoped>

</style>
