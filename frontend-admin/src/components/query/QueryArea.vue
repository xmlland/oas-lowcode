<template>
  <div class="u-query-area" :class="[queryAreaStyle+'-style', collapsed ? 'collapsed' : '']" :id="id">
    <div v-if="$slots.header" class="query-area-header">
      <slot name="header"></slot>
    </div>
    <a-form layout="inline" :model="queryData" @finish="handleQuery" :labelCol="{ style: { width: width+'px' } }">
      <div v-if="showMore" class="query-all-fields">
        <slot name="queryFields"></slot>
      </div>
      <div class="operation-area" v-if="queryAreaStyle==='ltr'">
        <slot name="left"></slot>
      </div>
      <div v-show="!showMore" class="query-fields">
        <div class="query-fields-content" :class="{ 'query-fields-collapsed': collapsed }">
          <slot name="queryFields"></slot>
        </div>
        <template v-if="queryAreaStyle==='rtl'">
          <div class="query-buttons" v-if="queryButton">
            <a-button html-type="submit" class="query-button query-submit-button" type="primary">
              <SearchOutlined/>
              查询
            </a-button>
            <a-button @click="reset" v-if="resetButton" class="query-button query-reset-button" type="primary">
              <Sync-outlined/>
              重置
            </a-button>
            <!-- 折叠/展开按钮 -->
            <a-button v-if="needCollapseButton" @click="toggleCollapse" class="query-button collapse-button" type="link">
              <template v-if="collapsed">
                <DownOutlined/>
                展开条件
              </template>
              <template v-else>
                <UpOutlined/>
                收起条件
              </template>
            </a-button>
            <template v-if="moreFields.length>0">
              <a-button v-if="!showMore" @click="clickShowMore" class="query-button" type="primary">
                <zoom-in-outlined/>
                高级查询
              </a-button>
              <a-button v-else @click="clickHideMore" class="query-button" type="primary">
                <double-left-outlined/>
                返回
              </a-button>
            </template>
          </div>
          <div class="operation-area" v-if="queryAreaStyle==='rtl'">
            <slot name="left"></slot>
          </div>
        </template>
      </div>
      <template v-if="queryAreaStyle==='ltr'||(queryAreaStyle==='rtl'&&moreFields.length>0&&showMore)">
        <div class="query-buttons" v-if="queryButton" :style="queryAreaStyle==='rtl'?{flex: 1,'text-align': 'right','margin-right': '30px'}:{}">
          <a-button html-type="submit" class="query-button query-submit-button" type="primary">
            <SearchOutlined/>
            查询
          </a-button>
          <a-button @click="reset" v-if="resetButton" class="query-button query-reset-button" type="primary">
            <Sync-outlined/>
            重置
          </a-button>
          <!-- 折叠/展开按钮 (ltr样式) -->
          <a-button v-if="needCollapseButton && queryAreaStyle==='ltr'" @click="toggleCollapse" class="query-button collapse-button" type="link">
            <template v-if="collapsed">
              <DownOutlined/>
              展开条件
            </template>
            <template v-else>
              <UpOutlined/>
              收起条件
            </template>
          </a-button>
          <template v-if="moreFields.length>0">
            <a-button v-if="!showMore" @click="clickShowMore" class="query-button" type="primary">
              <zoom-in-outlined/>
              高级查询
            </a-button>
            <a-button v-else @click="clickHideMore" class="query-button" type="primary">
              <double-left-outlined/>
              返回
            </a-button>

          </template>

        </div>
      </template>

      <!--      <div class="data-count">当前查询结果<span>{{ totalCount }}</span>条</div>-->

    </a-form>
    <slot name="queryAreaExt"></slot>
  </div>
</template>
<script>
export default {
  name: "QueryArea"
}
</script>
<script setup>
import {provide, ref, onMounted, nextTick, watch, computed} from "vue";
import {replaceAll, UUID} from "@/lib/tools";
import config from "@/config";
import {DownOutlined, UpOutlined} from "@ant-design/icons-vue";

let id = ref('query-area' + UUID())
let showMore = ref(false)
let collapsed = ref(true) // 默认折叠状态
let queryAreaRowCount = ref(1) // 查询区域行数
let needCollapseButton = computed(() => queryAreaRowCount.value > 1) // 超过1行时显示折叠按钮

let props = defineProps({
  areaWidth: {
    type: Number,
    default: null
  },
  totalCount: {
    type: Number,
    default: 0
  },
  width: {
    type: Number,
    default: 100
  },
  queryButton: {
    type: Boolean,
    default: true
  },
  resetButton: {
    type: Boolean,
    default: false
  },
  /**
   * Mounted时是否触发查询数据
   */
  triggerQueryDataOnMounted: {
    type: Boolean,
    default: false
  },
  queryAreaStyle: {
    type: String,
    default: config.theme.queryAreaStyle
  },
  loadData:{
    type: Function,
    default: null
  }
})

let queryData = ref({})
let queryType = ref({})
let pinyinQuery = ref({})
const isEmptyQueryValue = (value) => {
  if (value === undefined || value === null || value === '') {
    return true
  }
  if (Array.isArray(value)) {
    return value.length === 0
  }
  if (Object.prototype.toString.call(value) === '[object Object]') {
    const keys = Object.keys(value)
    if (keys.length === 0) {
      return true
    }
    if (Object.prototype.hasOwnProperty.call(value, 'id') && (value.id === undefined || value.id === null || value.id === '')) {
      return true
    }
    if (Object.prototype.hasOwnProperty.call(value, 'value') && (value.value === undefined || value.value === null || value.value === '')) {
      return true
    }
    return keys.every(key => value[key] === undefined || value[key] === null || value[key] === '')
  }
  return false
}
provide('setFormValue', (name, value, type, pinyin) => {
  if (isEmptyQueryValue(value)) {
    delete queryData.value[name]
    delete queryType.value[name]
    delete pinyinQuery.value[name]
    return
  }
  queryData.value[name] = value
  queryType.value[name] = type
  pinyinQuery.value[name] = pinyin
});
let callbacks = []
provide('resetFormValue', (callback) => {
  callbacks.push(callback)
});
let setFieldCallbacks = {}
provide('setFieldCurrentValue', (name, callback) => {
  setFieldCallbacks[name] = callback
});
//查询字段是否全部mounted
let queryFieldMounted = ref(false)
//created的回调函数 用于设置查询字段宽度
let createdCallbackArr = []
//mounted的询字段的props
let mountedProps = []
//mounted查询字段的个数
let mountedCount = 0
provide('setQueryFieldCreated', (callback) => {
  createdCallbackArr.push(callback)
});
provide('setQueryFieldMounted', (_props) => {
  mountedCount++;
  mountedProps.push(_props)
  if (mountedCount === createdCallbackArr.length) {
    //所有的queryField都已经mounted
    let defaultWidth = 270;
    mountedProps.forEach(prop => {
      if (prop.type === 'date-range') {
        //如果是日期范围选择器 需要根据日期格式来设置宽度
        let width = 0
        if(prop.props.formatPatter==='yyyy'){
          width = 180 + props.width
        }else if(prop.props.formatPatter==='yyyy-MM'){
          width = 250 + props.width
        }else if(prop.props.formatPatter==='yyyy-MM-dd') {
          width = 290 + props.width
        }else if(prop.props.formatPatter==='yyyy-MM-dd HH') {
          width = 330 + props.width
        }else if(prop.props.formatPatter==='yyyy-MM-dd HH:mm') {
          width = 370 + props.width
        }else if(prop.props.formatPatter==='yyyy-MM-dd HH:mm:ss') {
          width = 410 + props.width
        }else {
          width = 290 + props.width
        }
        if (width > defaultWidth) {
          defaultWidth = width
        }
      }
    })
    if (defaultWidth !== 270) {
      //如果有日期范围选择器 需要重新设置宽度
      createdCallbackArr.forEach(callback => {
        callback(defaultWidth)
      })
    }
    nextTick(()=>{
      queryFieldMounted.value = true
    })

    if (props.triggerQueryDataOnMounted) {
      let queryParam = getQueryData()
      console.log('使用查询区域的默认值查询数据', queryParam)
      props.loadData(queryParam)
    }
  }

});
provide('loadQueryTableData',props.loadData);

let emits = defineEmits(['query', 'reset', 'rowCountChange'])
const handleQuery = () => {
  emits('query', getQueryData())
}
const reset = () => {
  queryData.value = {}
  queryType.value = {}
  pinyinQuery.value = {}
  emits('reset')
  let count = 0
  let maxCount = callbacks.length
  if (maxCount === 0) {
    emits('query', getQueryData())
    return
  }
  callbacks.forEach(callback => {
    count ++
    if (typeof callback === 'function') {
      callback()
    }
    if (count >= maxCount) {
      emits('query', getQueryData())
    }
  })
}

const getActualQueryFieldRowCount = (queryFieldsContent) => {
  if (!queryFieldsContent) {
    return 1
  }
  const items = Array.from(queryFieldsContent.children).filter(item => {
    return item.classList && item.classList.contains('ant-form-item')
  })
  if (items.length === 0) {
    return 1
  }
  const rowTops = []
  items.forEach(item => {
    const top = item.offsetTop || 0
    if (!rowTops.some(rowTop => Math.abs(rowTop - top) <= 2)) {
      rowTops.push(top)
    }
  })
  return Math.max(rowTops.length, 1)
}

const applyQueryAreaRowCount = (rowCount) => {
  queryAreaRowCount.value = Math.max(rowCount, 1)
  emits('rowCountChange', (collapsed.value ? 1 : queryAreaRowCount.value) * 32)
}

//let showFields = ref([])
let moreFields = ref([])
onMounted(() => {
  if (!document.getElementById(id.value)) {
    return
  }
  /* let totalWidth = 0
  let queryFields = document.getElementById(id.value).querySelector('.query-fields')
  if (props.areaWidth) {
    totalWidth = props.areaWidth
  } else {
    totalWidth = queryFields.clientWidth;
  }
  let useWidth = 90 * 2 + 118;
  let _showFields = []
  let _moreFields = []
  let queryFieldItems = queryFields.querySelectorAll('.ant-form-item')
  queryFieldItems.forEach(item => {
    useWidth += item.clientWidth
    if (useWidth <= totalWidth) {
      _showFields.push(item)
    } else {
      _moreFields.push(item)
      item.setAttribute('more', 'more')
      item.style.display = 'none'
    }
  })
  showFields.value = _showFields
  moreFields.value = _moreFields*/
})

/**
 * 当查询区域宽度发生变化时，或者查询区域内的表单项发生初始化成功时，重新计算查询区域占据的行数
 */
watch(() => [queryFieldMounted.value,props.areaWidth], () => {
  if (!queryFieldMounted.value) {
    return
  }
  if (!document.getElementById(id.value)){
    return;
  }
  //获取查询区域元素
  let queryFields = document.getElementById(id.value).querySelector('.query-fields')
  //获取查询字段内容区域元素
  let queryFieldsContent = document.getElementById(id.value).querySelector('.query-fields-content')
  //获取查询按钮区域元素
  let queryButtons = document.getElementById(id.value).querySelector('.query-buttons')
  //获取操作按钮区域元素
  let operationArea = document.getElementById(id.value).querySelector('.operation-area')
  let useWidth = 0
  //获取所有的查询字段元素
  let queryFieldItems = queryFieldsContent ? queryFieldsContent.querySelectorAll('.ant-form-item') : []
  //获取所有的查询按钮
  let queryButtonItems = queryButtons ? queryButtons.querySelectorAll('.ant-btn') : []
  //获取所有的操作按钮
  let operationItems = operationArea ? operationArea.querySelectorAll('.ant-btn') : []
  //查询区域总宽度，如果props.areaWidth有值，则使用props.areaWidth，否则使用查询字段内容区域的宽度
  let totalWidth = 0
  if (props.areaWidth) {
    totalWidth = props.areaWidth
  } else {
    totalWidth = queryFieldsContent ? queryFieldsContent.clientWidth : (queryFields ? queryFields.clientWidth : 0);
  }
  //查询区域占据的行数
  let rowCount = 1
  //检查当前元素是否超出查询区域宽度，如果超出，则换行
  let checkWidth = (widthToAdd) => {
    if (!widthToAdd || isNaN(widthToAdd)) {
      widthToAdd = 270 // 默认宽度
    }
    if (useWidth + widthToAdd > totalWidth) {
      useWidth = widthToAdd
      rowCount++
    } else {
      useWidth += widthToAdd
    }
  }
  // 只计算查询字段的行数
  queryFieldItems.forEach(item => {
    if (item.getAttribute('class').indexOf('act-radio') > -1) {
      //工作流状态切换radio的宽度固定为190
      checkWidth(190)
    } else {
      //其他查询条件为样式上的宽度，如果没有设置则使用 clientWidth
      let width = parseInt(replaceAll(item.style.width, 'px'))
      if (!width || isNaN(width)) {
        width = item.clientWidth || 270
      }
      checkWidth(width)
    }
  })
  rowCount = Math.max(rowCount, getActualQueryFieldRowCount(queryFieldsContent))
  // 不再计算按钮宽度，因为按钮在单独的区域
  //更新查询区域行数
  applyQueryAreaRowCount(rowCount)
  //查询区域占据的行数发生变化时，触发事件（折叠时只计算1行高度）
  nextTick(() => {
    const actualRowCount = getActualQueryFieldRowCount(queryFieldsContent)
    if (actualRowCount !== queryAreaRowCount.value) {
      applyQueryAreaRowCount(actualRowCount)
    }
  })
}, {immediate: true})

// 折叠/展开切换
const toggleCollapse = () => {
  collapsed.value = !collapsed.value
  nextTick(() => {
    // 重新计算并通知父组件高度变化
    emits('rowCountChange', (collapsed.value ? 1 : queryAreaRowCount.value) * 32)
  })
}

const clickShowMore = () => {
  showMore.value = true
  nextTick(() => {
    emits('showMoreChange', document.getElementById(id.value).clientHeight)
  })
}
const clickHideMore = () => {
  showMore.value = false
  nextTick(() => {
    emits('showMoreChange', document.getElementById(id.value).clientHeight)
  })
}
const getQueryData = () => {
  let query = queryData.value
  if(query){
    if(query[""]){
      delete query[""];
    }
  }
  if (Object.keys(queryType.value).length > 0) {
    query.queryParamType = queryType.value
  } else {
    delete query.queryParamType
  }
  return query
}
/**
 * 获取拼音查询设置
 * @return {UnwrapRef<{}>}
 */
const getPinyinQuery = () => {
  return pinyinQuery.value
}
provide('getQueryData',getQueryData);

const setQueryData = (param) => {
  queryData.value = param
  for (const name in param) {
    // if (param[name] && setFieldCallbacks[name]) {
    if ( ( param[name] || param[name] === '' ) && setFieldCallbacks[name] ) {
      setFieldCallbacks[name](param[name])
    }
  }
}
defineExpose({
  getQueryData, setQueryData, getPinyinQuery
})
</script>

<style lang="less" scoped>
.u-query-area {
  :deep( .ant-form-inline) {
    align-items: center;
    justify-content: space-between;

    .ant-form-item {
      margin-right: 0;
      margin-left: 0;
    }

    .ant-form-item:first-child {
      margin-right: 0;
      margin-left: 0;
    }
  }

  .query-button {
    margin-left: 8px;
    /*width: 56px;*/

    :deep( .anticon) {
      /*font-size: 22px;*/
    }
  }

  .data-count {
    background: #fffcec;
    color: #666;
    height: 32px;
    line-height: 32px;
    padding: 0 10px;
    margin-left: 30px;

    span {
      padding: 0 5px;
      color: #FFAB64;
    }
  }

  .operation-area {

    :deep( .ant-btn) {
      margin-right: 8px;
    }
    :deep(button[disabled]){
      &.ant-btn-primary{
        color: #fff;
        border-color: var(--ant-primary-color);
        background: var(--ant-primary-color);
      }
    }
  }

  .query-fields {
    margin-top: -5px;
    flex-wrap: nowrap;
    flex-direction: row;
    flex: 1;
    display: flex;
    justify-content: flex-start;
    align-items: flex-start;

    .query-fields-content {
      display: flex;
      flex-wrap: wrap;
      flex: 1;
      min-width: 0;

      :deep(.ant-form-item) {
        margin-top: 5px;
      }

      // 折叠状态下只显示一行
      &.query-fields-collapsed {
        max-height: 37px;
        overflow: hidden;
      }
    }

    :deep(.ant-form-item) {
      .ant-form-item-label {
        padding-left: 10px;
      }
    }

    .query-buttons {
      margin-top: 5px;
      flex-shrink: 0;
      white-space: nowrap;
      display: flex;
      align-items: center;
    }
  }

  // 折叠按钮样式
  .collapse-button {
    padding: 4px 8px;
    color: var(--ant-primary-color);
    &:hover {
      color: var(--ant-primary-color-hover);
    }
  }

  .query-all-fields {
    width: 100%;
    display: flex;
    /* height: auto; */
    flex-direction: row;
    flex-wrap: wrap;

    :deep( .ant-form-item) {
      margin-bottom: 5px;

      .ant-form-item-label {
        padding-left: 10px;
      }
    }
  }

  :deep( .ant-form-item-control) {
    width: calc(100% - 100px);
  }
}

.rtl-style {
  .query-fields {
    justify-content: flex-start;
  }

  .operation-area {
    margin-top: 5px;
    margin-left: auto;
    flex-shrink: 0;
    display: flex;

    :deep( .ant-btn) {
      margin-right: 0;
      margin-left: 8px;
    }
  }
}
</style>
