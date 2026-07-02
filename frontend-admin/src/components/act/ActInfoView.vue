<template>
  <div :style="{height:height+'px'}">
    <component :is="currentModalComponent"></component>
  </div>

</template>

<script>
export default {
  name: "ActInfoView"
}
</script>
<script setup>
import {watch, ref, nextTick, provide, computed} from "vue";
import {getAction} from "@/api/action";
let diffHeight =  30 + 32 + 32 + 24
let height = computed(() => {
  return document.body.clientHeight * 0.9 - diffHeight
})
let props = defineProps({
  id: {
    type: String,
    default: ''
  },
  procDefKey: {
    type: String,
    default: ''
  },
  formNo: {
    type: String,
    default: ''
  },
  module: {
    type: String,
    default: ''
  }
})
let currentModalComponent = ref(null)
let currentProcRow = ref({})
let formRef = null
let formRefCallback = null

provide('setFormRef', (_formRef, callback) => {
  formRef = _formRef
  formRefCallback = callback
});
watch(() => props.id, (val) => {
  if (val === '') {
    return
  }
  if (props.formNo && props.module) {
    import(`@/views/${props.module}/${props.formNo}/form.vue`).then(res => {
      currentModalComponent.value = res.default
    })
    getAction(`dynamic/zform/getZformWithActMap?formNo=${props.formNo}&id=${val}&procDefKey=${props.procDefKey}`, {}).then(actRes => {
      currentProcRow.value = actRes.data.data
      let row = {actRuleArgs: currentProcRow.value.ruleArgs}
      Object.assign(row, currentProcRow.value)
      nextTick(() => {
        if (row && formRef && formRef.setData) {
          formRef.setData(row, true, {}, () => {
            console.log('formRefCallback', formRefCallback)
            formRefCallback && formRefCallback({full: false,formHeight:height.value - 50})
          })
        }
      })
    })
  }

}, {immediate: true})
</script>
<style scoped>

</style>
