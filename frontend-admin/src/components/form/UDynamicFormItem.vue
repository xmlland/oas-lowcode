<template>
  <u-input v-if="formType==='input'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-select v-if="formType==='select'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-date v-if="formType==='date'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-area v-if="formType==='area'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-upload v-if="formType==='upload'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-tree-select v-if="formType==='treeSelect'||formType==='tree-select'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-modal-select v-if="formType==='modalSelect'||formType==='modal-select'" :disabled="disabled" :value="currentValue" @update:value="updateValue" :formModel="currentFormModel" @update:formModel="updateFormModel" v-bind="localPropsValue"/>
  <u-user-select v-if="formType==='userSelect'||formType==='user-select'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-users-select v-if="formType==='usersSelect'||formType==='users-select'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-office-select v-if="formType==='officeSelect'||formType==='office-select'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-modal-multi-select v-if="formType==='modalMultiSelect'||formType==='modal-multi-select'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-icon-select v-if="formType==='iconSelect'||formType==='icon-select'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>
  <u-tinymce v-if="formType==='richText'" :disabled="disabled" defaultValue="" @update:value="updateValue" :value="currentValue" v-bind="localPropsValue"/>
  <u-switch v-if="formType==='switch'" :disabled="disabled" :value="currentValue" @update:value="updateValue" :defaultValue="localPropsValue.defaultValue" v-bind="localPropsValue"/>
  <u-serial-no v-if="formType==='serialNo'||formType==='serial-no'" :disabled="disabled" :value="currentValue" @update:value="updateValue" v-bind="localPropsValue"/>

</template>

<script>
export default {
  name: "UDynamicFormItem"
}
</script>
<script setup>
import {ref, watch} from "vue";
import UInput from "@/components/form/UInput";
import USelect from "@/components/form/USelect";
import UModalSelect from "@/components/form/UModalSelect";
import UUserSelect from "@/components/form/sys/UUserSelect";
import UDate from "@/components/form/UDate";
import UUpload from "@/components/form/UUpload";
import UTreeSelect from "@/components/form/UTreeSelect";
import UUsersSelect from "@/components/form/sys/UUsersSelect";
import UOfficeSelect from "@/components/form/sys/UOfficeSelect";
import UModalMultiSelect from "@/components/form/UModalMultiSelect";
import UArea from "@/components/form/UArea";
import UIconSelect from "@/components/form/UIconSelect";
import UTinymce from "@/components/form/UTinymce.vue";
import USwitch from "@/components/form/USwitch.vue";
import USerialNo from "@/components/form/USerialNo.vue";
import {replaceDefaultValue} from "@/lib/tools";

let currentValue = ref(null)
watch(() => _props.value, () => {
  currentValue.value = _props.value;
}, {immediate: true})

let currentFormModel = ref(null)
watch(() => _props.formModel, () => {
  currentFormModel.value = _props.formModel;
}, {immediate: true})
let _props = defineProps({
  formType: {
    type: String,
    default: 'input'
  },
  value: {
    type: [Object, Array, String],
    default: null
  },
  formModel: {
    type: Object,
    default() {
      return {}
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  props: {
    type: Object,
    default: () => ({})
  },
  formModal: {
    type: Object,
    default: () => ({})
  }
})
let emits = defineEmits(['update:value', 'change', 'update:updateFormModel'])
const updateValue = (value) => {
  emits('update:value', value)
  emits('change', value)
}

const updateFormModel = (value) => {
  emits('update:updateFormModel', value)
}
let localPropsValue = ref({})

const initLocalPropsValue = () => {
  const regex = /\${(.*?)}/g;
  let localProps = JSON.parse(JSON.stringify(_props.props))
  const replaceHandler = (arr) => {
    arr.forEach(field => {
      if (localProps[field]) {
        let val = localProps[field]
        let isPares = false
        if (typeof val === 'object') {
          val = JSON.stringify(val)
          isPares = true
        }
        const matches = val.match(regex);
        if (matches && matches.length > 0) {
          matches.forEach(item => {
            if (localProps[field]) {
              val = val.replace(item, _props.formModal[item.replace(regex, '$1')])
              if (isPares) {
                localProps[field] = JSON.parse(val)
              } else {
                localProps[field] = val
              }
            }
          })
        }
      }
    })
  }

  if (_props.formType === 'date') {
    let replaceFields = ['minValue', 'maxValue']
    replaceHandler(replaceFields)
  }
  if (_props.formType === 'select') {
    let replaceFields = ['postData']
    replaceHandler(replaceFields)
  }
  const replaceDefaultValueHandler=(arr)=>{
    arr.forEach(item=>{
      if (item.value){
        item.value = replaceDefaultValue(item.value)
      }
      if (item.value2){
        item.value = replaceDefaultValue(item.value)
      }
      if (item.children){
        replaceDefaultValueHandler(item.children)
      }
    })
  }
  localPropsValue.value = localProps
}
watch([() => _props.props, () => _props.formModal], () => {
  initLocalPropsValue()
}, {immediate: true, deep: true})

</script>
<style scoped>

</style>
