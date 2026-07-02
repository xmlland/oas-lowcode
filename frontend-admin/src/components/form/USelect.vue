<template>
  <a-select v-if="formType==='select'"
            :mode="isMultiple?'multiple':null"
            :allowClear="allowClear"
            :disabled="disabled"
            :placeholder="currentPlaceholder"
            :showSearch="showSearch"
            optionFilterProp="title"
            @change="selectChange"
            :maxTagCount="maxTagCount"
            :showArrow="!disabled"
            :getPopupContainer="getPopupContainer"
            :notFoundContent="notFoundContent"
            v-model:value="currentValue">
    <template v-bind:key="getData(option,valueField)" v-for="(option,optionIndex) in selectOptions">
      <a-select-option :title="formatText(option, optionIndex)" :value="getData(option,valueField)">
        <div v-if="contentType === 'html'" v-html="formatText(option, optionIndex)"></div>
        <div v-if="contentType === 'text'" v-text="formatText(option, optionIndex)"></div>
      </a-select-option>
    </template>
  </a-select>
  <!-- 单选按钮 -->
  <a-radio-group v-else-if="formType==='radio'&&!isMultiple"
                 :disabled="disabled"
                 v-model:value="currentValue"
                 :name="name"
                 @change="selectChange">
    <a-radio v-for="(option,optionIndex) in options" v-bind:key="getData(option,valueField)" :value="getData(option,valueField)">
      <div v-if="contentType === 'html'" v-html="formatText(option, optionIndex)"></div>
      <div v-if="contentType === 'text'" v-text="formatText(option, optionIndex)"></div>
    </a-radio>
  </a-radio-group>
  <!-- 多选按钮 -->
  <a-checkbox-group v-else-if="formType==='checkbox'&&checkboxNowrap" :disabled="disabled" v-model:value="currentValue"
                    :name="name"
                    :options="checkboxOptions" @change="selectChange"/>
  <!-- 纳入企业名单原因 -->
  <a-checkbox-group v-else-if="formType==='checkbox'&&!checkboxNowrap" :disabled="disabled" v-model:value="currentValue"
                    :name="name"
                    @change="selectChange">
    <a-row :gutter="[16,16]">
      <a-col :span="checkboxSpan" v-bind:key="item.value" v-for="item in checkboxOptions">
        <a-checkbox :value="item.value">
          <div v-if="contentType === 'html'" v-html="item.label"></div>
          <div v-if="contentType === 'text'" v-html="item.label"></div>
        </a-checkbox>
      </a-col>
    </a-row>
  </a-checkbox-group>
</template>

<script>
import {
  Checkbox as ACheckbox,
  CheckboxGroup as ACheckboxGroup,
  Col as ACol,
  Radio as ARadio,
  RadioGroup as ARadioGroup,
  Row as ARow,
  Select as ASelect,
  SelectOption as ASelectOption
} from "ant-design-vue"

export default {
  name: "USelect",
  components: {ARow, ACol, ACheckbox, ACheckboxGroup, ASelect, ASelectOption, ARadioGroup, ARadio}
}
</script>
<script setup>
import {ref, watch, computed, inject, nextTick, onMounted, onBeforeUnmount} from "vue";
import {getDictAction} from "@/api/api";
import {postAction} from "@/api/action";
import {getData, isNotEmpty} from "@/lib/tools";

let name = ref('radioGroup' + new Date().getTime())
let props = defineProps({
  value: {
    type: [String, Number, Boolean],
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  defaultValue: {
    type: [String, Number, Array],
    default: null
  },
  multiple: {
    type: [String, Boolean],
    default: false
  },
  multipleSeparator: {
    type: String,
    default: ','
  },
  type: {
    type: String,
    default: 'dict'// dict|table|url
  },
  /**
   * select | radio | checkbox
   */
  formType: {
    type: String,
    default: 'select'
  },
  checkboxNowrap: {
    type: Boolean,
    default: true
  },
  checkboxSpan: {
    type: Number,
    default: 24
  },
  dictType: {
    type: String,
    default: null
  },
  tablePageSize: {
    type: Number,
    default: 1000
  },
  tableOrderBy: {
    type: String,
    default: ''
  },
  tableFilterData: {
    type: Array,
    default() {
      return []
    }
  },
  targetFormNo: {
    type: String,
    default: ''
  },
  targetField: {
    type: String,
    default: ''
  },
  targetFilterData: {
    type: Array,
    default() {
      return []
    }
  },
  optionData: {
    type: Array,
    default() {
      return []
    }
  },
  optionFilter:{
    type: Function,
    default: null
  },
  dataUrl: {
    type: String,
    default: ''
  },
  /**
   * 使用url加载数据时的参数
   */
  postData: {
    type: Object,
    default: () => {
      return {}
    }
  },
  valueField: {
    type: [String, Array],
    default: 'member'
  },
  textField: {
    type: [String, Array],
    default: 'memberName'
  },
  showValue: {
    type: Boolean,
    default: false
  },
  format: {
    type: Function,
    default: null
  },
  formatFuncStr: {
    type: String,
    default: ''
  },
  showIndex: {
    type: Boolean,
    default: false
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  placeholder: {
    type: String,
    default: ''
  },
  showSearch: {
    type: Boolean,
    default: true
  },
  maxTagCount:{
    type: Number,
    default: null
  },
  defaultIndex:{
    type: Number,
    default: null
  },
  loadDataByDefaultIndex:{
    type: Boolean,
    default: true
  },
  /**
   * 是否在值改变时触发加载数据
   */
  triggerLoadDataOnChange:{
    type: Boolean,
    default: false
  },
  /**
   * 是否在查询区域中
   */
  inQueryArea:{
    type: Boolean,
    default: false
  },
  container:{
    type: String,
    default: ''
  },
  /**
   * 多选数量上限，到达上限后隐藏未选择的选项
   */
  maxSelectedCount:{
    type: Number,
    default: null
  },
  /**
   * 当下拉列表为空时显示的内容
   */
  notFoundContent:{
    type: String,
    default: undefined
  },
  /**
   * 数据更新时是否设置默认索引
   */
  dataUpdateSetDefIndex:{
    type: Boolean,
    default: false
  },
  /**
   * 内容类型：text,html
   */
  contentType:{
    type: String,
    default: 'text'
  }
})

let emits = defineEmits(['update:value', 'change', 'changeText','loadSuccess'])
const SELECT_DICT_DATA_REFRESH_EVENT = 'u-select-dict-data-refresh'
/*if (!props.defaultValue) {
  emits('update:value', props.defaultValue)
}*/

let isMultiple = computed(() => {
  if (typeof props.multiple === 'string') {
    return props.multiple === '1' || props.multiple === 'true'
  }
  return props.multiple
})

let options = ref([])
// 标记 options 是否已加载完成
let optionsLoaded = ref(false)
// 标记是否有待处理的默认值需要在 options 加载后 emit
let pendingDefaultEmit = ref(false)

let checkboxOptions = computed(() => options.value.map((option, index) => {
  return {label: formatText(option, index), value: getData(option,props.valueField)}
}))
let selectOptions = computed(()=>{
  if(!props.maxSelectedCount||!currentValue.value){
    return options.value;
  }
  let cValue = Array.from(currentValue.value);
  if(cValue.length>=props.maxSelectedCount){
    return options.value.filter(o => cValue.includes(o?.id));
  }
  return options.value;
})

let currentPlaceholder = computed(() => props.disabled ? '' : props.placeholder)
let currentValue = ref(isMultiple.value ? [] : props.defaultValue)

/**
 * 检查值是否在 options 中存在
 */
const isValueInOptions = (val) => {
  if (!options.value || options.value.length === 0) return false;
  if (isMultiple.value && Array.isArray(val)) {
    return val.some(v => options.value.some(opt => getData(opt, props.valueField) === v));
  }
  return options.value.some(opt => getData(opt, props.valueField) === val);
}

/**
 * 在 options 加载完成后处理默认值的 emit
 */
const emitDefaultValueAfterOptionsLoaded = () => {
  if (!pendingDefaultEmit.value || !optionsLoaded.value) return;
  if (!isNotEmpty(currentValue.value)) return;
  
  // 检查当前值是否在 options 中
  if (!isValueInOptions(currentValue.value)) return;
  
  pendingDefaultEmit.value = false;
  
  // emit 更新事件
  if (isMultiple.value || props.formType === 'checkbox') {
    const emitVal = Array.isArray(currentValue.value) ? currentValue.value.join(props.multipleSeparator) : currentValue.value;
    emits('update:value', emitVal);
    emits('change', emitVal, options.value.filter(item => {
      const itemVal = getData(item, props.valueField);
      return Array.isArray(currentValue.value) ? currentValue.value.includes(itemVal) : currentValue.value === itemVal;
    }));
  } else {
    emits('update:value', currentValue.value);
    emits('change', currentValue.value, options.value.filter(item => getData(item, props.valueField) === currentValue.value));
  }
}

watch(() => props.value, (newVal) => {
  if (newVal || typeof newVal === 'boolean') {
    currentValue.value = isMultiple.value ? newVal.split(props.multipleSeparator) : newVal;
  } else {
    // 当 value 为空时，保留 defaultValue 而不是直接设为 null
    if (isNotEmpty(props.defaultValue)) {
      currentValue.value = isMultiple.value ? props.defaultValue.split(props.multipleSeparator) : props.defaultValue;
      // 如果 options 未加载完成，标记需要在加载后 emit
      if (!optionsLoaded.value) {
        pendingDefaultEmit.value = true;
      }
    } else {
      currentValue.value = isMultiple.value ? [] : null;
    }
  }
}, {immediate: true})

let filterDataLocal = computed(() => {
  return props.tableFilterData
})

const setFormValue =  props.inQueryArea && inject('setFormValue');
const queryFieldProps =  props.inQueryArea && inject('queryFieldProps');
const loadQueryTableData = props.inQueryArea && inject('loadQueryTableData');
const getQueryData = props.inQueryArea && inject('getQueryData');

let defaultIndexSetFlag = false;

const setDefaultIndex = ()=>{
  if(defaultIndexSetFlag && !props.dataUpdateSetDefIndex){
    return;
  }
  defaultIndexSetFlag = true;
  if (!options.value || options.value.length === 0 || (props.defaultIndex === null) || ((typeof props.defaultIndex) === 'undefined')) {
    let defaultVal = isMultiple.value ? [] : null;
    if (isNotEmpty(props.defaultValue)) {
      defaultVal = isMultiple.value ? props.defaultValue.split(props.multipleSeparator) : props.defaultValue
    }
    currentValue.value = (props.value || typeof (props.value) === 'boolean') ? (isMultiple.value ? props.value.split(props.multipleSeparator) : props.value) : defaultVal;

    if (props.value && props.triggerLoadDataOnChange && loadQueryTableData) {
      loadQueryTableData(getQueryData());
    }
    return;
  }
  currentValue.value = options.value[props.defaultIndex][props.valueField];

  // -----------------设置默认值，需要更新值，--------------------------- by ZhangJQ
  // 如果 组件类型为多选框，那么数据强制修正为 多选数据格式
  if (isMultiple.value || props.formType==='checkbox') {
    emits('update:value', currentValue.value.join(props.multipleSeparator))
    emits('change', currentValue.value.join(props.multipleSeparator), options.value.filter(item => currentValue.value.indexOf(getData(item,props.valueField)) >= 0))
  } else {
    emits('update:value', currentValue.value)
    emits('change', currentValue.value, options.value.filter(item => currentValue.value === getData(item, props.valueField)))
  }
  // ----------------------------------------------------------------

  if(!(setFormValue&&queryFieldProps)){
    return;
  }
  setFormValue(queryFieldProps.name,currentValue.value,queryFieldProps.queryType);
  if(props.loadDataByDefaultIndex && loadQueryTableData){
    nextTick(()=>{
      loadQueryTableData(getQueryData());
    })
  }
}

const loadDictData = () => {
  if (props.type === 'dict') {
    if (props.dictType) {
      getDictAction(props.dictType).then(res => {
        if (props.optionFilter){
          options.value = res.data.data.filter(props.optionFilter);
        }else{
          options.value = res.data.data;
        }
        optionsLoaded.value = true;
        emits('loadSuccess', JSON.parse(JSON.stringify(options.value)), JSON.parse(JSON.stringify(res.data.data)));
        setDefaultIndex();
        // options 加载完成后处理待 emit 的默认值
        nextTick(() => {
          emitDefaultValueAfterOptionsLoaded();
        });
      })
    }
  } else if (props.type === 'table'||props.type === 'url') {
    let url = 'dynamic/zform/gridselectDataMap';
    let data = {}
    if (props.type === 'url') {
      url = props.dataUrl;
      data = props.postData;
    } else {
      data = {
        searchKey: '',
        searchValue: '',
        tableName: props.dictType,
        filterList: filterDataLocal.value,
        targetTableName: props.targetFormNo,
        targetField: props.targetField,
        targetFilterList: props.targetFilterData,
      }
    }
    data.pageParam = {
      pageNo: 1,
      pageSize: props.tablePageSize,
      orderBy: props.tableOrderBy
    }
    postAction(url, data).then(res => {
      if (props.optionFilter){
        options.value = res.rows.filter(props.optionFilter);
      }else{
        options.value = res.rows;
      }
      optionsLoaded.value = true;
      emits('loadSuccess', JSON.parse(JSON.stringify(options.value)), JSON.parse(JSON.stringify(res.rows)));
      setDefaultIndex();
      // options 加载完成后处理待 emit 的默认值
      nextTick(() => {
        emitDefaultValueAfterOptionsLoaded();
      });
    })
  }

}
const getOptions =()=>{
  return options.value;
}

const shouldRefreshByExternalEvent = (detail = {}) => {
  if (detail.type && detail.type !== props.type) {
    return false
  }
  if (detail.dictType && detail.dictType !== props.dictType) {
    return false
  }
  return Boolean(props.dictType)
}

const handleExternalDictDataRefresh = (event) => {
  if (shouldRefreshByExternalEvent(event?.detail || {})) {
    loadDictData()
  }
}

onMounted(() => {
  window.addEventListener(SELECT_DICT_DATA_REFRESH_EVENT, handleExternalDictDataRefresh)
})

onBeforeUnmount(() => {
  window.removeEventListener(SELECT_DICT_DATA_REFRESH_EVENT, handleExternalDictDataRefresh)
})

watch(() => props.dictType, () => {
  if (props.dictType) {
    loadDictData();
  }
}, {immediate: true})
watch(() => props.dataUrl, () => {
  if (props.dataUrl) {
    loadDictData();
  }
}, {immediate: true})
watch(() => props.postData, (value, oldValue) => {
  if (props.dataUrl && (JSON.stringify(value) !== JSON.stringify(oldValue))) {
    loadDictData();
  }
}, {deep: true})
watch(() => props.optionData, (value, oldValue) => {
  if (props.optionData && (value.length > 0 || (oldValue && oldValue.length > 0))) {
    defaultIndexSetFlag = false;
    options.value = props.optionData;
    optionsLoaded.value = true;
    setDefaultIndex();
    // options 加载完成后处理待 emit 的默认值
    nextTick(() => {
      emitDefaultValueAfterOptionsLoaded();
    });
  }
}, {immediate: true, deep: true})
watch(() => props.targetFormNo, () => {
  loadDictData();
}, {immediate: true})
watch(() => props.tableFilterData, (o,n) => {
  if (JSON.stringify(o) !== JSON.stringify(n)) {
    loadDictData();
  }
}, {})

const selectChange = () => {
  if (typeof currentValue.value === "undefined"){
    currentValue.value = ''
  }
  // 如果 组件类型为多选框，那么数据强制修正为 多选数据格式
  if (isMultiple.value || props.formType==='checkbox') {
    emits('update:value', currentValue.value.join(props.multipleSeparator))
    emits('change', currentValue.value.join(props.multipleSeparator), options.value.filter(item => currentValue.value.indexOf(getData(item,props.valueField)) >= 0))
  } else {
    emits('update:value', currentValue.value)
    emits('change', currentValue.value, options.value.filter(item => currentValue.value === getData(item, props.valueField)))
  }
  if (props.triggerLoadDataOnChange){
    loadQueryTableData(getQueryData());
  }
  let checkboxText = ref([])
  if (checkboxText && Array.isArray(currentValue.value)) {
    currentValue.value.forEach((item, index) => {
      checkboxOptions.value.forEach((checkboxItem) => {
        if (checkboxItem.value === item) {
          checkboxText.value.push(checkboxItem.label)
        }
      })
    })
    emits('changeText', checkboxText.value.join(props.multipleSeparator))
  }
}

const formatText = (option, index) => {
  if (props.format) {
    return props.format(option, index)
  }
  if (props.formatFuncStr){
    try {
      return new Function('option', 'index', props.formatFuncStr)(option, index)
    }catch (e) {
      console.error(e)
    }
    return 'error'
  }
  if (props.showValue) {
    return getData(option,props.valueField) + ' | ' + getData(option, props.textField)
  }
  if (props.showIndex) {
    return `${index + 1}、${getData(option, props.textField)}`
  }
  return getData(option, props.textField)
}

const getPopupContainer = () => {
  return props.container === ''?document.body: document.getElementById(props.container)
}

defineExpose({
  loadDictData,getOptions
})
</script>
<style scoped>
.ant-checkbox-group {
  width: 100%;
}
</style>
