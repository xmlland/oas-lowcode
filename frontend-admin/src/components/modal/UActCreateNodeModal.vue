<template>
  <u-modal ref="modal" :width="800" :formDisabled="true" :showOk="false"
           :extendButtons="[{value:'SURE',text:'确定',validate:true,type:'primary'}]" @clickOperate="clickOperate">
    <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="100">
      <a-col :span="24">
        <a-form-item name="assigneeSelect" label="用户" :validateFirst="true" :rules="[{ required: true, message: '请选择用户' }]">
          <u-users-select :disabled="disabled" v-model:value="formModel.assigneeSelect" placeholder="请选择用户"/>
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="showCustomNodeName">
        <a-form-item name="customNodeName" label="节点名称" :validateFirst="true" :rules="[{ required: true, message: '请输入节点名称' }]">
          <u-input :disabled="disabled" v-model:value="formModel.customNodeName" placeholder="请输入节点名称"/>
        </a-form-item>
      </a-col>
    </u-form>
  </u-modal>
</template>

<script>
export default {
  name: "UActCreateNodeModal"
}
</script>
<script setup>
import {getCurrentInstance, ref} from "vue";

let formModel = ref({
  customNodeName: '自定义节点'
})
let disabled = ref(false)
let instance = getCurrentInstance();
let showCustomNodeName = ref(true)
let currentResolve = null
const open = (title = '加签', showCustomNode = true) => {
  showCustomNodeName.value = showCustomNode
  formModel.value.customNodeName = '自定义节点'
  formModel.value.assigneeSelect = null
  instance.refs.modal.open(title)
  return new Promise(resolve => {
    currentResolve = resolve;
  })
}
const clickOperate = () => {
  //console.log('clickOperate', currentResolve, formModel.value)
  let obj = {tempLoginName: formModel.value.assigneeSelect.loginName.split(',')}
  if (showCustomNodeName.value) {
    obj.customNodeName = formModel.value.customNodeName
  }
  currentResolve(obj)
  instance.refs.modal.close()
}
defineExpose({
  open
})
</script>
<style scoped>

</style>
