<template>
  <query-field v-if="listConfig.queryFieldType" :default-value="replaceDefaultValue(listConfig.queryDefaultValue)" :widthMultiple="Number(listConfig.widthMultiple)" :type="listConfig.queryFieldType" :queryType="listConfig.queryType" :name="listConfig.queryColumn||column.name" :label="column.queryLabel||column.comments" :props="resolvedQueryFieldProps" :width="queryFieldWidth" :labelWidth="queryLabelWidth" ></query-field>
  <query-field v-else-if="!column.showType" :default-value="replaceDefaultValue(listConfig.queryDefaultValue)" :widthMultiple="Number(listConfig.widthMultiple)" type="input" queryType="like" :name="listConfig.queryColumn||column.name" :label="column.queryLabel||column.comments" :width="queryFieldWidth" :labelWidth="queryLabelWidth" ></query-field>
  <query-field v-else-if="column.showType === 'treeselectRedio'" :default-value="replaceDefaultValue(listConfig.queryDefaultValue)" :widthMultiple="Number(listConfig.widthMultiple)" type="input" :name="listConfig.queryColumn" :label="column.queryLabel||column.comments" :width="queryFieldWidth" :labelWidth="queryLabelWidth"></query-field>
  <query-field v-else-if="column.showType === 'areaselect'" :default-value="replaceDefaultValue(listConfig.queryDefaultValue)" :widthMultiple="Number(listConfig.widthMultiple)" type="area" :name="listConfig.queryColumn" :label="column.queryLabel||column.comments" :width="queryFieldWidth" :labelWidth="queryLabelWidth"></query-field>
  <query-field v-else-if="column.showType === 'select'||column.showType === 'radiobox'" :default-value="replaceDefaultValue(listConfig.queryDefaultValue)" :widthMultiple="Number(listConfig.widthMultiple)" type="select" :name="listConfig.queryColumn||column.name" :label="column.queryLabel||column.comments" :width="queryFieldWidth" :labelWidth="queryLabelWidth"
               :props="column.selectSimple==='1'?{
                 dictType:column.dictType
               }:{
                 type:'table',
                  dictType:column.tableName,
                  valueField:column.selectValuefield,
                  textField:column.searchKey,
               }"></query-field>
  <query-field v-else-if="column.showType === 'dateselect'&&column.queryType!=='between'" :default-value="replaceDefaultValue(listConfig.queryDefaultValue)" :widthMultiple="Number(listConfig.widthMultiple)" type="date" :name="listConfig.queryColumn||column.name" :label="column.queryLabel||column.comments" :width="queryFieldWidth" :labelWidth="queryLabelWidth" :props="{
                 formatPatter:column.dateType
               }"></query-field>
  <query-field v-else-if="column.showType === 'dateselect'&&column.queryType==='between'" :default-value="replaceDefaultValue(listConfig.queryDefaultValue)" :widthMultiple="Number(listConfig.widthMultiple)" type="date-range" :name="listConfig.queryColumn||column.name" :label="column.queryLabel||column.comments" :width="queryFieldWidth" :labelWidth="queryLabelWidth" :props="{
                 formatPatter:column.dateType
               }"></query-field>
  <query-field v-else :default-value="replaceDefaultValue(listConfig.queryDefaultValue)" :widthMultiple="Number(listConfig.widthMultiple)" type="input" :name="listConfig.queryColumn||column.name" :label="column.queryLabel||column.comments" :width="queryFieldWidth" :labelWidth="queryLabelWidth"></query-field>

</template>

<script>
export default {
  name: "DynamicQueryField",
}
</script>
<script setup>
import {computed} from "vue";
import QueryField from "@/components/query/QueryField";
import {replaceDefaultValue} from "@/lib/tools";
//TODO 处理不同的showType
let props = defineProps({
  column: {
    type: Object,
    default: () => {
      return {
        showType: 'input',
        name: '',
        comments: '',
        dictType: '',
        tableName: '',
        selectValuefield: '',
        searchKey: '',
        selectSimple: '1'
      }
    }
  },
})
/**
 * 查询类型映射
 * 需要把兼容旧表单的查询类型映射到新表单的查询类型
 */
const queryTypeMap = {
  '=': 'eq',
  'in': 'in',
  '!=': 'ne',
  '>': 'gt',
  '>=': 'ge',
  '<': 'lt',
  '<=': 'le',
  'between': 'between',
  'like': 'like',
  'left_like': 'likeRight',
  'right_like': 'likeLeft',

}
let listConfig = computed(() => {
  let config = {
    widthMultiple: 1,
  }
  if (props.column.listConfig) {
    config = JSON.parse(props.column.listConfig)
    if (!config.widthMultiple) {
      config.widthMultiple = 1
    }
  }
  // 兼容 formatPattern -> formatPatter（组件prop名为formatPatter）
  if (config.queryFieldProps && config.queryFieldProps.formatPattern && !config.queryFieldProps.formatPatter) {
    config.queryFieldProps.formatPatter = config.queryFieldProps.formatPattern
  }
  // 查询类型映射
  config.queryType = queryTypeMap[props.column.queryType] || 'eq'
  return config
})
/**
 * 当 listConfig 显式声明 queryFieldType=select 时，DynamicQueryField 会优先走该分支。
 * AI/正式化历史数据可能只写了 queryFieldType，queryFieldProps 为空对象，
 * 此时必须回退到 column.dictType / listConfig.dict，否则查询下拉无数据。
 */
const resolvedQueryFieldProps = computed(() => {
  const config = listConfig.value || {}
  const rawProps = config.queryFieldProps && typeof config.queryFieldProps === 'object'
      ? {...config.queryFieldProps}
      : {}
  const queryFieldType = config.queryFieldType
  const needsDict = queryFieldType === 'select'
      || props.column.showType === 'select'
      || props.column.showType === 'radiobox'
  if (needsDict && !rawProps.dictType) {
    rawProps.dictType = props.column.dictType || config.dict || ''
  }
  if (!rawProps.placeholder) {
    rawProps.placeholder = props.column.queryLabel || props.column.comments || props.column.name || ''
  }
  return rawProps
})
const toPositiveNumber = (value) => {
  const number = Number(value)
  return Number.isFinite(number) && number > 0 ? number : null
}
const queryFieldWidth = computed(() => {
  return toPositiveNumber(listConfig.value.queryWidth || listConfig.value.width)
})
const queryLabelWidth = computed(() => {
  return toPositiveNumber(listConfig.value.labelWidth || listConfig.value.queryLabelWidth)
})
</script>
<style scoped>

</style>
