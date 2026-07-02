<template>
  <json-viewer v-if="disabled" :value="currentValue" :expand-depth=4 copyable sort></json-viewer>
  <!--  <json-editor v-else v-model:value="currentValue" mode="code" :modes="['code']"
                 @json-change="onJsonChange"
                 @has-error="onError"></json-editor>-->
  <u-code-mirror v-else lang="json" :height="200" :value="currentValue" @change="onJsonChange"/>
</template>

<script>
// 导入模块
//import JsonEditor from 'vue-json-editor'
import JsonViewer from 'vue-json-viewer'

export default {
  name: "UJson",
  components: {JsonViewer},
}
</script>
<script setup>
import {ref, watch} from "vue";
//import {message} from "ant-design-vue";
import UCodeMirror from "@/components/form/code/UCodeMirror";
import {formatJson} from "@/components/form/jsonHelper";

let props = defineProps({
  value: {
    type: [Object, Array, String],
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  stringify: {
    type: Boolean,
    default: false
  }

})

let currentValue = ref(null)
watch(() => props.value, () => {
  if (props.value) {
    if (typeof (props.value) === 'string' || props.stringify ) {
      currentValue.value = props.value;
    } else {
      currentValue.value = formatJson(JSON.stringify(props.value))
    }
  }
}, {immediate: true, deep: true})
let emits = defineEmits(['update:value'])

const onJsonChange = (value) => {
  if (typeof (props.value) === 'string' || props.stringify) {
    emits('update:value', value)
  } else {
    emits('update:value', JSON.parse(value))
  }
}
/*const onError = () => {
  message.warning('json格式错误')
}*/
</script>
<style scoped>

</style>
