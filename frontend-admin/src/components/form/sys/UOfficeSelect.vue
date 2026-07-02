<template>
  <u-tree-select formNo="sys_office" :disabled="disabled" v-model:value="currentValue" :parentId="parentId" :data="data"
                 :placeholder="placeholder" @change="treeSelectChange"/>
</template>

<script>
export default {
  name: "UOfficeSelect"
}
</script>
<script setup>
import {ref, watch} from "vue";
import {replaceDefaultValue} from "@/lib/tools";

let props = defineProps({
  value: {
    type: Object,
    default() {
      return {}
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: ''
  },
  defaultValue: {
    type: String,
    default: null
  },
  parentId: {
    type: String,
    default: undefined
  },
  data: {
    type: Array,
    default: ()=>{
      return []
    }
  },
})
let currentValue = ref(null)
watch(() => props.value, () => {
  if (!props.value || !props.value.id) {
    let defaultVal = replaceDefaultValue(props.defaultValue);
    if (defaultVal) {
      currentValue.value = defaultVal
    } else {
      currentValue.value = null
    }
    emits('update:value', currentValue.value)
    emits('change', currentValue.value)
  } else if (props.value) {
    currentValue.value = props.value
  }
}, {immediate: true})
let emits = defineEmits(['update:value', 'change'])

const treeSelectChange = (e) =>{
  currentValue.value = e;
  emits('update:value', currentValue.value)
  emits('change', currentValue.value)
}
</script>
<style scoped>

</style>
