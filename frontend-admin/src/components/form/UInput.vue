<template>
  <a-textarea v-if="textarea"
              :allowClear="allowClear"
              :disabled="disabled"
              :maxlength="Number(maxlength)"
              :placeholder="currentPlaceholder"
              :autoSize="autoSize"
              :showCount="!disabled&&showCount"
              @change="changeInput"
              v-model:value="currentValue"/>
  <a-input v-else-if="type==='text'" :allowClear="allowClear"
           :disabled="disabled"
           :maxlength="Number(maxlength)"
           :placeholder="currentPlaceholder"
           @change="changeInput"
           @blur="blur"
           v-model:value="currentValue">
    <template v-if="$slots.addonBefore" #addonBefore>
      <slot name="addonBefore"></slot>
    </template>
    <template v-if="$slots.addonAfter" #addonAfter>
      <slot name="addonAfter"></slot>
    </template>
    <template v-if="$slots.prefix" #prefix>
      <slot name="prefix"></slot>
    </template>
  </a-input>
  <a-input-number v-else-if="type==='number'" v-model:value="currentValue" :allowClear="allowClear"
                  :disabled="disabled" :precision="precision" :max="max" :min="min"
                  :placeholder="currentPlaceholder" @change="changeInput">
    <template v-if="$slots.addonBefore" #addonBefore>
      <slot name="addonBefore"></slot>
    </template>
    <template v-if="$slots.addonAfter" #addonAfter>
      <slot name="addonAfter"></slot>
    </template>
    <template v-if="$slots.prefix" #prefix>
      <slot name="prefix"></slot>
    </template>
  </a-input-number>
  <a-input-number v-else-if="type==='digits'" v-model:value="currentValue" :allowClear="allowClear"
                  :disabled="disabled" :max="max" :min="min"
                  :placeholder="currentPlaceholder" @change="changeInput">
    <template v-if="$slots.addonBefore" #addonBefore>
      <slot name="addonBefore"></slot>
    </template>
    <template v-if="$slots.addonAfter" #addonAfter>
      <slot name="addonAfter"></slot>
    </template>
    <template v-if="$slots.prefix" #prefix>
      <slot name="prefix"></slot>
    </template>
  </a-input-number>
</template>

<script>
export default {
  name: "UInput"
}
</script>
<script setup>
import {computed, ref, watch} from "vue";
import {isEmpty, removeEmptyStrings, replaceDefaultValue} from "@/lib/tools";

let props = defineProps({
  type: {
    type: String,
    default: 'text'// text digits number
  },
  precision: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: Infinity
  },
  min: {
    type: Number,
    default: -Infinity
  },
  value: {
    type: [String, Number],
    default: null
  },
  defaultValue: {
    type: [String, Number],
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
  maxlength: {
    type: [String, Number],
    default: 255
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  textarea: {
    type: Boolean,
    default: false
  },
  showCount: {
    type: Boolean,
    default: false
  },
  autoSize: {
    type: [Object, Boolean],
    default() {
      return {minRows: 2, maxRows: 6}
    }
  },
  // 是否自动去除数据空字符
  whitespaceRemoval: {
    type: Boolean,
    default: false
  },
})
let currentPlaceholder  = computed(()=>props.disabled?'':props.placeholder)
let currentValue = ref(null)
watch(() => props.value, () => {
  currentValue.value = props.value;
}, {immediate: true})
watch(() => props.defaultValue, () => {
  if (props.defaultValue&&!props.value) {
    currentValue.value = replaceDefaultValue(props.defaultValue);
    emits('update:value', currentValue.value + '')
    emits('change', currentValue.value + '')
  }
}, {immediate: true})
let emits = defineEmits(['update:value', 'change', 'blur'])

const changeInput = () => {
  if ( isEmpty(currentValue.value)) {
    currentValue.value = '';
  }else if (props.whitespaceRemoval) {
    // 自动去除数据空字符
    currentValue.value = removeEmptyStrings(currentValue.value);
  }
  emits('update:value', props.type === 'text' ? (currentValue.value + '') : currentValue.value)
  emits('change', props.type === 'text' ? (currentValue.value + '') : currentValue.value)
}

const blur = () =>{
  emits('blur', currentValue.value + '')
}
</script>
<style scoped>
.ant-input-number{
  width: 100%;
 }

</style>
