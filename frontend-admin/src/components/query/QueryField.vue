<template>
  <a-form-item :class="queryFieldClass" :style="{width:formItemWidth+'px'}" :name="name" :label="label" :colon="showColon"
               :labelCol="labelWidth?{ style: { width: labelWidth+'px' }}:null">

    <u-area v-if="type==='area'" v-model:value="formValue"  :showRank="showRank" v-bind="props" @change="change"></u-area>
    <u-date v-if="type==='date'" v-model:value="formValue" v-bind="props" @change="change"></u-date>
    <u-date-range v-if="type==='date-range'" v-model:value="formValue" v-bind="props" @change="change"></u-date-range>
    <u-input v-if="type==='input'" v-model:value="formValue" v-bind="props" @change="change" ></u-input>
    <u-select v-if="type==='select'" :inQueryArea="true" v-model:value="formValue" v-bind="props" @change="change"></u-select>
    <u-yes-no v-if="type==='yes-no'" v-model:value="formValue" v-bind="props" @change="change"></u-yes-no>
    <u-modal-select v-if="type==='modal-select'||type==='modalSelect'" v-model:value="formValue" v-bind="props"
                    @change="change"></u-modal-select>
    <u-user-select v-if="type==='user-select'||type==='userSelect'" v-model:value="formValue" v-bind="props"
                    @change="change"></u-user-select>
    <u-users-select v-if="type==='users-select'||type==='usersSelect'" v-model:value="formValue" v-bind="props"
                    @change="change"></u-users-select>
    <u-office-select v-if="type==='office-select'||type==='officeSelect'" v-model:value="formValue" v-bind="props"
                    @change="change"></u-office-select>
    <u-tree-select v-if="type==='tree-select'||type==='treeSelect'" v-model:value="formValue" v-bind="props"
                   @change="change"></u-tree-select>
    <u-cascader v-if="type==='cascader-select'||type==='cascaderSelect'" v-model:value="formValue" v-bind="props"
                @change="change('cascade')"></u-cascader>
    <u-modal-multi-select v-if="type==='modal-multi-select'||type==='modalMultiSelect'" v-model:value="formValue" v-bind="props"
                          @change="change"></u-modal-multi-select>
  </a-form-item>

</template>
<script>
export default {
  name: "QueryField",
}
</script>
<script setup>
import {computed, inject, onMounted, provide, ref} from "vue";
import {replaceDefaultValue} from "@/lib/tools";

let define = defineProps({
  value: {
    type: [String, Number, Object, Array],
    default: ''
  },
  type: {
    type: String,
    default: 'input'
  },
  name: {
    type: String,
    default: 'field'
  },
  label: {
    type: String,
    default: ''
  },
  /**
   * 不设置默认宽度，默认使用固定值 如果设置了宽度，则使用设置的值 zhoury 2023-07-21 10:33:46
   */
  width: {
    type: Number,
    default: null
  },
  /**
   * 设置宽度倍数，仅在width为null时生效
   */
  widthMultiple: {
    type: Number,
    default: 1
  },
  labelWidth: {
    type: Number,
    default: null
  },
  props: {
    type: Object,
    default() {
      return {}
    }
  },
  defaultValue: {
    type: [String, Object,Array],
    default: null
  },
  showRank:{
    type:Number,
    default: 3
  },
  queryType: {
    type: String,
    default: ''//非主表查询字段需要指定查询类型
  },
  //是否允许拼音查询
  pinyin:{
    type: Boolean,
    default: false
  },
  formatValue: {
    type: Function,
    default: null
  },
  formatValueFuncStr: {
    type: String,
    default: ''
  },
  showColon:{
    type: Boolean,
    default: true
  },
  //日期范围开始字段前缀
  dateRangeStartPrefix: {
    type: String,
    default: 'begin'
  },
  //日期范围开始字段后缀
  dateRangeStartSuffix: {
    type: String,
    default: ''
  },
  //日期范围结束字段前缀
  dateRangeEndPrefix: {
    type: String,
    default: 'end'
  },
  //日期范围结束字段后缀
  dateRangeEndSuffix: {
    type: String,
    default: ''
  },
  //是否将日期范围字段转换为驼峰格式
  dateRangeCamelCase: {
    type: Boolean,
    default: true
  },
})

provide('queryFieldProps',define);

let _value = ref(replaceDefaultValue(define.defaultValue))
/*let _width = ref(define.width)*/
if (define.type === 'area') {
  _value.value = {
    id: ''
  }
}
/*if (define.type === 'date-range' && _width.value === 270) {
  _width.value = 540
}*/

const getVal = () => {
  if (define.formatValueFuncStr){
    try {
      return new Function('val', define.formatValueFuncStr)(formValue.value)
    }catch (e) {
      console.error(e)
    }
    return 'error'
  }
  return define.formatValue?define.formatValue(formValue.value):formValue.value
}

let formValue = ref(_value.value)
let defaultWidth = ref(270)
let queryFieldClass = computed(() => {
  return [
    'query-field-item',
    `query-field-${define.type}`,
    define.width ? 'query-field-custom-width' : 'query-field-default-width'
  ]
})
let formItemWidth = computed(()=>{
  if(define.width){
    return define.width
  }else{
    if(define.type === 'date-range'){
      return defaultWidth.value
    }else{
      return defaultWidth.value * define.widthMultiple
    }
  }
})

let setFormValue = () => {
}
let resetFormValue = () => {
}
let setFieldCurrentValue = () => {
}
setFormValue = inject('setFormValue'); // inject的参数为provide过来的名称
resetFormValue = inject('resetFormValue'); // inject的参数为provide过来的名称

setFieldCurrentValue = inject('setFieldCurrentValue'); // inject的参数为provide过来的名称
setFieldCurrentValue(define.name,(value) => {
  formValue.value = value
})

const emit = defineEmits(['reloadPage','change','update:value'])

const change = (val) => {

  if (setFormValue) {
    if (define.type === 'date-range') {
      let name = define.name;
      if (define.dateRangeCamelCase){
        name = define.name.substr(0, 1).toUpperCase() + define.name.substr(1);
      }
      if (formValue.value) {
        let valueArray = define.formatValue?define.formatValue(formValue.value):formValue.value;
        setFormValue(define.dateRangeStartPrefix + name + define.dateRangeStartSuffix, valueArray[0], define.queryType, define.pinyin)
        setFormValue(define.dateRangeEndPrefix + name + define.dateRangeEndSuffix, valueArray[1], define.queryType, define.pinyin)
      } else {
        setFormValue(define.dateRangeStartPrefix + name + define.dateRangeStartSuffix, null, define.queryType, define.pinyin)
        setFormValue(define.dateRangeEndPrefix + name + define.dateRangeEndSuffix, null, define.queryType, define.pinyin)
      }

    } else {
      // 级联选择查询数据为空时，不提供参数
      if (val === 'cascade' && Object.keys(getVal()).length === 0 && getVal().constructor === Object) {
        setFormValue(define.name, null, define.queryType, define.pinyin)
      }else {
        setFormValue(define.name, getVal(), define.queryType, define.pinyin)
      }
    }

    try {
      emit('reloadPage', "")
      emit('change', getVal())
      emit('update:value', getVal())
    }catch (e){
      console.log(e)
    }

  }

}

resetFormValue(() => {
  if (define.defaultValue === undefined || define.defaultValue === null) {
    formValue.value = null
  }else{
    formValue.value = replaceDefaultValue(define.defaultValue)
  }
  change(formValue.value)
});

if (define.defaultValue) {
  //setFormValue(define.name,getVal(), define.queryType)
  change()
}
let setQueryFieldMounted = inject('setQueryFieldMounted');
let setQueryFieldCreated = inject('setQueryFieldCreated');
setQueryFieldCreated && setQueryFieldCreated((val) => {
  defaultWidth.value = val
})
onMounted(() => {
  setQueryFieldMounted && setQueryFieldMounted(define)
})

</script>

<style lang="less" scoped>
.date-range {
  display: flex;
}
:deep(.ant-select-selection-overflow){
  display: flex;
  flex-wrap: nowrap;

  .ant-select-selection-overflow-item:first-child{
    max-width: calc(100% - 80px);
  }
}
</style>
