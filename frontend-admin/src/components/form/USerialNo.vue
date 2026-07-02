<template>
  <div class="u-serial-no">
    <a-input
        :value="currentValue"
        :disabled="disabled"
        :placeholder="currentPlaceholder"
        :maxlength="64"
        :allowClear="allowClear"
        @change="changeInput"
        v-model:value="currentValue">
      <template #addonAfter>
        <a-button type="link" size="small" :disabled="disabled" :loading="loading" @click="generate" class="serial-no-btn">
          生成编号
        </a-button>
      </template>
    </a-input>
  </div>
</template>

<script>
export default {
  name: "USerialNo"
}
</script>
<script setup>
import {computed, ref, watch} from "vue";
import {getAction} from "@/api/action";
import {message} from "ant-design-vue";

let props = defineProps({
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
    default: '点击右侧按钮生成编号'
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  // 编号前缀，如 "BK"、"CG" 等
  prefix: {
    type: String,
    default: ''
  }
})

let currentPlaceholder = computed(() => props.disabled ? '' : props.placeholder)
let currentValue = ref(null)
let loading = ref(false)
let emits = defineEmits(['update:value', 'change'])

watch(() => props.value, () => {
  currentValue.value = props.value;
}, {immediate: true})

const changeInput = () => {
  emits('update:value', currentValue.value ? (currentValue.value + '') : '')
  emits('change', currentValue.value ? (currentValue.value + '') : '')
}

const generate = () => {
  if (!props.prefix) {
    message.warning('未配置编号前缀')
    return
  }
  loading.value = true
  getAction('oa/serialNo/generate', {prefix: props.prefix}).then(res => {
    if (res.code === 0 && res.data && res.data.serialNo) {
      currentValue.value = res.data.serialNo
      emits('update:value', currentValue.value)
      emits('change', currentValue.value)
    } else {
      message.error(res.msg || '生成编号失败')
    }
  }).catch(() => {
    message.error('生成编号失败')
  }).finally(() => {
    loading.value = false
  })
}
</script>

<style scoped>
.u-serial-no {
  width: 100%;
}
.serial-no-btn {
  padding: 0 4px;
  height: auto;
  line-height: 1;
}
</style>
