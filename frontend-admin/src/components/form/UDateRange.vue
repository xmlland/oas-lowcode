<template>
  <div class="date-range-container">
    <div class="date-range-item">
      <u-date  ref="startDate" :show-quick-select="showQuickSelect" :minValue="minValueLocal" @quickSelect="quickSelectInfo" :maxValue="end" @openChange="openChange" @change="changeInput" :disabled="disabled" v-model:value="start" :formatPatter="formatPatter" :inputReadOnly="inputReadOnly" :allowClear="allowClear" :allowEQ="allowEQ"></u-date>
    </div>
    <span class="date-range-separator">~</span>
    <div class="date-range-item">
      <u-date ref="endDate" :minValue="start" :maxValue="maxValueLocal" @change="changeInput" :disabled="disabled" v-model:value="end" :formatPatter="formatPatter" :inputReadOnly="inputReadOnly" :allowClear="allowClear" :allowEQ="allowEQ"></u-date>
    </div>
  </div>
</template>

<script>

export default {
  name: "UDateRange",
}
</script>
<script setup>
import UDate from "@/components/form/UDate";
import {computed, getCurrentInstance, ref, watch} from "vue";
import dayjs from "dayjs";
import {patternMapping} from "./date"
let instance = getCurrentInstance();
let config = defineProps({
  value: {
    type: String,
    default: null
  },
  disabled: {
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
  formatPatter: {
    type: String,
    default: 'yyyy-MM-dd'
  },
  minValue: {
    type: [String, Function],
    default: null,
  },
  maxValue: {
    type: [String, Function],
    default: null,
  },
  showQuickSelect: {
    type: Boolean,
    default: false
  },
  allowClear:{
    type: Boolean,
    default: true
  },
  allowEQ:{
    type: Boolean,
    default: false
  }
})

let currentValue = ref(null)
let start = ref(null)
let end = ref(null)

watch(() => config.value, () => {
  currentValue.value = config.value;
  if (currentValue.value&&Array.isArray(currentValue.value)) {
    let arr = currentValue.value
    start.value = arr[0]
    end.value = arr[1]
  } else {
    start.value = null
    end.value = null
  }
}, {immediate: true})
let emits = defineEmits(['update:value', 'change'])
let isSelectQuick= ref(false)
const quickSelectInfo = (dataType) => {
  isSelectQuick.value = true
  let now = dayjs().format(patternMapping[config.formatPatter].format);
if (dataType ==='three'){
  currentValue.value = [dayjs().add(-2, 'day').format(patternMapping[config.formatPatter].format),now ]
} else if(dataType === 'week'){
  currentValue.value = [dayjs().add(-1, 'week').add(1, 'day').format(patternMapping[config.formatPatter].format), now]
}else if(dataType === 'month'){
  currentValue.value = [dayjs().add(-1, 'month').add(1, 'day').format(patternMapping[config.formatPatter].format), dayjs().format(patternMapping[config.formatPatter].format), now]
}

  emits('update:value', currentValue.value)
  emits('change', currentValue.value)
  instance.refs.startDate.closePanel()
}

let maxValueLocal = computed(()=>{
  if((typeof config.maxValue)==='function'){
    return config.maxValue(start.value,end.value);
  }
  return config.maxValue;
})

let minValueLocal = computed(()=>{
  if((typeof config.minValue)==='function'){
    return config.minValue(start.value,end.value);
  }
  return config.minValue;
})

const changeInput = () => {
  if (start.value && end.value) {
    currentValue.value = [start.value, end.value]
    emits('update:value', currentValue.value)
    emits('change', currentValue.value)
  } else if (!start.value && !end.value) {
    currentValue.value = null
    emits('update:value', currentValue.value)
    emits('change', currentValue.value)
  }
}

const openChange = (status) => {
  if (!status && !isSelectQuick.value) {
    instance.refs.endDate.openPanel()
  }
}
</script>

<style scoped>
.ant-picker {
  width: 100%;
}
.date-range-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 100%;
}
.date-range-item {
  flex: 1;
  min-width: 0;
}
.date-range-separator {
  margin: 0 5px;
  flex-shrink: 0;
}
</style>
