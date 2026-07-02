<template>
  <a-time-picker
      v-if="isTimePicker"
      :disabled="disabled"
      :allowClear="allowClear"
      @change="changeInput"
      v-model:value="currentValue"
      :format="format"
      :valueFormat="format"
      :placeholder="disabled ? '' : placeholder"
      :inputReadOnly="inputReadOnly"
      :open="open"
      @openChange="openChange"
      :getPopupContainer="getPopupContainer"
  >
    <template v-if="disabled" #suffixIcon>
    </template>
  </a-time-picker>
  <a-date-picker
      v-else
      :disabled="disabled"
      :allowClear="allowClear"
      @change="changeInput"
      v-model:value="currentValue"
      v-bind="localProps"
      :inputReadOnly="inputReadOnly"
      :open="open"
      @openChange="openChange"
      :getPopupContainer="getPopupContainer"
  >
    <template v-if="disabled" #suffixIcon>
    </template>
    <template v-if="showQuickSelect" #renderExtraFooter>
      <a-button type="link" class="linkStyle" size="small" shape="round" ghost @click="emits('quickSelect', 'three')">近三天</a-button>
      <a-button type="link" class="linkStyle" size="small" shape="round" ghost @click="emits('quickSelect', 'week')">近一周</a-button>
      <a-button type="link" class="linkStyle" size="small" shape="round" ghost @click="emits('quickSelect', 'month')">近一个月</a-button>
    </template>
  </a-date-picker>
</template>

<script>
export default {
  name: "UDate"
}
</script>
<script setup>
import {computed, ref, watch} from "vue";
import {patternMapping} from "./date"
import dayjs from "dayjs";
import {replaceDefaultValue} from "@/lib/tools";
import {DatePicker as ADatePicker, TimePicker as ATimePicker} from "ant-design-vue";

let config = defineProps({
  value: {
    type: String,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showQuickSelect: {
    type: Boolean,
    default: false
  },
  inputReadOnly: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: null
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  formatPatter: {
    type: String,
    default: 'yyyy-MM-dd'
  },
  minValue: {
    type: String,
    default: null,
  },
  maxValue: {
    type: String,
    default: null,
  },
  defaultValue: {
    type: String,
    default: null
  },
  container:{
    type: String,
    default: ''
  },
  allowEQ:{
    type: Boolean,
    default: false
  }
})

let valueFormat = 'YYYY-MM-DD'

let picker = ref(patternMapping[config.formatPatter].picker);
let showTime = ref(patternMapping[config.formatPatter].showTime);
let format = ref(patternMapping[config.formatPatter].format);

let isTimePicker = computed(() => picker.value === 'time')

let open = ref(false)
const openChange = (status) => {
  open.value = status
  emits('openChange', status)
}

const disabledDate = (current) => {
  if (config.minValue || config.maxValue) {
    let isAfterMin = true
    let isBeforeMax = true
    if (config.minValue) {
      if(config.allowEQ){
        isAfterMin = current.isAfter(dayjs(config.minValue)) || current.format(format.value) === config.minValue
      }else{
        isAfterMin = current.isAfter(dayjs(config.minValue))
      }
    }
    if (config.maxValue) {
      if(config.allowEQ){
        isBeforeMax = current.isBefore(dayjs(config.maxValue)) || current.format(format.value) === config.maxValue
      }else{
        isBeforeMax = current.isBefore(dayjs(config.maxValue))
      }
    }

    return !isAfterMin || !isBeforeMax
  }
  return false
}

if (picker.value === 'date') {
  valueFormat = format.value
}
if (picker.value === 'week') {
  valueFormat = 'YYYY-wo'
}
if (picker.value === 'month') {
  valueFormat = 'YYYY-MM'
}
if (picker.value === 'year') {
  valueFormat = 'YYYY'
}
if (picker.value === 'time') {
  valueFormat = format.value
}

let localProps = computed(() => {
  let _localProps = {valueFormat: valueFormat, picker: picker.value, showTime: showTime.value, format: format.value}
  Object.assign(_localProps, config)
  let currentPlaceholder = config.disabled ? '' : config.placeholder
  _localProps.placeholder = currentPlaceholder
  delete _localProps.value
  delete _localProps.disabled
  _localProps.disabledDate = disabledDate
  return _localProps
})

let currentValue = ref(null)
watch(() => config.value, () => {
  if (!config.value) {
    currentValue.value = replaceDefaultValue(config.defaultValue)
    emits('update:value', currentValue.value)
    emits('change', currentValue.value)
  } else {
    currentValue.value = config.value;
  }

}, {immediate: true})
let emits = defineEmits(['update:value', 'change','quickSelect'])

const changeInput = () => {
  let updateValue = currentValue.value
  emits('update:value', updateValue)
  emits('change', updateValue)
}
const openPanel = () => {
  open.value = true
}
const closePanel = () => {
  open.value = false
}

const getPopupContainer = () => {
  return config.container === ''?document.body: document.getElementById(config.container)
}
defineExpose({
  openPanel,
  closePanel
})
</script>

<style scoped>
.ant-picker {
  width: 100%;
}

.linkStyle{
  font-size: 14px;
  margin-left: 4px;
}
</style>

