<template>
  <u-select v-model:value="currentValue"
            :disabled="disabled"
            :placeholder="placeholder"
            :optionData="optionData"
            :allowClear="allowClear"
            value-field="value"
            text-field="text"
            :formType="formType"
            :showSearch="false"
            @change="changeInput"></u-select>
</template>

<script>
export default {
  name: "UYesNo"
}
</script>
<script setup>

import {ref, watch} from "vue";

let props = defineProps({
  value: {
    type: String,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: ''
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  formType: {
    type: String,
    default: 'select'
  },
})

let optionData = [{value: '1', text: '是'}, {value: '0', text: '否'}]

let currentValue = ref(null)
watch(() => props.value, () => {
  currentValue.value = props.value;
}, {immediate: true})
let emits = defineEmits(['update:value', 'change'])

const changeInput = () => {
  emits('update:value', currentValue.value)
  emits('change', currentValue.value)
}
</script>
<style scoped>

</style>
