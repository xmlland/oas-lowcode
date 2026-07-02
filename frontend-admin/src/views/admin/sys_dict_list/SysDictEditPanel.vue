<template>
  <a-spin :spinning="loading">
    <div :class="panelClass">
      <component :is="SysDictForm" :key="formKey" />
    </div>
  </a-spin>
</template>

<script>
export default {
  name: "SysDictEditPanel"
}
</script>

<script setup>
import {computed, nextTick, provide, ref} from "vue";
import {message} from "ant-design-vue";
import {postAction} from "@/api/action";
import SysDictForm from "@/views/admin/sys_dict_list/form";

const props = defineProps({
  mode: {
    type: String,
    default: 'modal'
  }
})
const emit = defineEmits(['saveSuccess'])

let formRef = null
let formRefCallback = null
let pendingOpen = null
const formKey = ref(0)
const loading = ref(false)
const panelClass = computed(() => ['sys-dict-edit-panel', `sys-dict-edit-panel-${props.mode}`])

provide('setFormRef', (_formRef, callback) => {
  formRef = _formRef
  formRefCallback = callback
  applyPendingOpen()
})

provide('addExtendFormRef', () => {})

const applyPendingOpen = () => {
  if (!pendingOpen || !formRef?.setData) {
    return
  }
  const {row, disabled, callback} = pendingOpen
  pendingOpen = null
  formRef.setData(row || {}, disabled, {}, () => {
    formRefCallback && formRefCallback({full: false})
    callback && callback()
  })
}

const open = (row = {}, disabled = false, callback = null) => {
  formRef = null
  formRefCallback = null
  pendingOpen = {row, disabled, callback}
  formKey.value += 1
  nextTick(applyPendingOpen)
}

const save = () => {
  if (!formRef?.validateFields || !formRef?.saveUrl) {
    return Promise.reject(new Error('表单尚未初始化完成'))
  }
  loading.value = true
  return formRef.validateFields().then(data => {
    data.formNo = formRef.formNo
    data.parentFormNo = formRef.parentFormNo
    return postAction(formRef.saveUrl, data).then(res => {
      message.success(data.id ? '修改成功' : '添加成功')
      emit('saveSuccess', res)
      return res
    })
  }).finally(() => {
    loading.value = false
  })
}

defineExpose({
  open,
  save
})
</script>
