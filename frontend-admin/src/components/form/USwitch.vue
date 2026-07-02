<template>
  <a-switch v-model:checked="currentValue" :disabled="disabled"/>
</template>

<script>
export default {
  name: 'USwitch',
}
</script>
<script setup>
import {ref, watch} from "vue";

const props = defineProps({
  value: {
    type: Boolean,
    default: null
  },
  defaultValue: {
    type: [String, Boolean],
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
})

let currentValue = ref(null)
let emits = defineEmits(['update:value', 'change'])

// Watch for external value changes
watch(() => props.value, () => {
  currentValue.value = props.value;
}, {immediate: true})

// Handle defaultValue - convert string to boolean
watch(() => props.defaultValue, () => {
  if (props.defaultValue !== null && props.defaultValue !== '' && props.value === null) {
    let defaultBool = props.defaultValue;
    if (typeof defaultBool === 'string') {
      defaultBool = defaultBool === 'true' || defaultBool === '1';
    }
    currentValue.value = defaultBool;
    emits('update:value', currentValue.value)
    emits('change', currentValue.value)
  }
}, {immediate: true})

// Emit changes when user toggles switch
watch(() => currentValue.value, (newVal, oldVal) => {
  if (newVal !== oldVal && newVal !== props.value) {
    emits('update:value', newVal)
    emits('change', newVal)
  }
})
</script>
