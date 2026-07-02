<template>
  <u-modal ref="actModal" :showOk="false" :width="1200" :formDisabled="true">
    <act-history-content :proc-ins-id="localProcInsId"></act-history-content>
  </u-modal>
</template>

<script>

export default {
  name: "ActHistoryModal",
}
</script>
<script setup>
import {ref, watch, getCurrentInstance} from "vue";
import UModal from "@/components/modal/UModal";
import ActHistoryContent from "@/components/act/ActHistoryContent";

let instance = getCurrentInstance()

let props = defineProps({
  procInsId: {
    type: String,
    default: ''
  },
  dialogTitle: {
    type: String,
    default: '流程跟踪'
  }
})

let localProcInsId = ref(props.procInsId)
watch(() => props.procInsId, (newVal) => {
  localProcInsId.value = newVal
}, {immediate: true})
const open = (insId) => {
  localProcInsId.value = insId
  instance.refs.actModal.open(props.dialogTitle)
}
defineExpose({
  open
})
</script>
<style scoped>

</style>
