<template>
  <div style="width: 100%;height: 100%;">
    <single-table-view v-if=" config.tableType==='0'" ref="tableView"
                       :class="singleTableViewClass"
                       v-bind="effectiveSingleTableViewProps"
                       :disabled="disabled"
                       :autoHeight="effectiveAutoHeight"
                       :height="height"
                       :width="width"
                       :parentId="parentId"
                       :parentFormData="parentFormData"
                       @clickButton="clickButton"
                       @clickRow="clickRow"
                       @update:actStatus="onActStatusChange">
      <template v-if="useModernListSkin" #queryHeader>
        <div class="modern-list-query-heading">
          <h2 class="modern-list-query-title">{{ dynamicPageTitle }}</h2>
          <p v-if="dynamicPageDesc" class="modern-list-query-desc">{{ dynamicPageDesc }}</p>
        </div>
      </template>
      <template #queryFields>
        <template v-bind:key="item.name" v-for="item in config.allColumns">
          <dynamic-query-field v-if="item.isQuery==='1'" :column="item"/>
        </template>
      </template>
      <template #extendHiddenArea>
        {{config.singleTableViewProps}}
      </template>
    </single-table-view>
    <single-table-view :class="treeTableViewClass" v-if="config.tableType==='3'" ref="tableView"
                       :url="{list:'dynamic/zform/datamap?path=path&formNo=${formNo}&traceFlag=&parentId=${parentId}'}"
                       :queryButton="false"
                       v-bind="effectiveSingleTableViewProps"
                       :autoHeight="effectiveAutoHeight"
                       @clickButton="clickButton"
                       @clickRow="clickRow">
      <template v-if="useModernListSkin" #queryHeader>
        <div class="modern-list-query-heading">
          <h2 class="modern-list-query-title">{{ dynamicPageTitle }}</h2>
          <p v-if="dynamicPageDesc" class="modern-list-query-desc">{{ dynamicPageDesc }}</p>
        </div>
      </template>
    </single-table-view>
    <left-tree-right-table-view v-if="config.tableType==='4'" ref="tableView"
                                :class="leftTreeRightViewClass"
                                :treeFormNo="config.parentTable"
                                :modalTitle="effectiveSingleTableViewProps.modalTitle"
                                :rightTableView="effectiveSingleTableViewProps"
                                @clickButton="clickButton"
                                @clickRow="clickRow">
      <template v-if="useModernListSkin" #queryHeader>
        <div class="modern-list-query-heading">
          <h2 class="modern-list-query-title">{{ dynamicPageTitle }}</h2>
          <p v-if="dynamicPageDesc" class="modern-list-query-desc">{{ dynamicPageDesc }}</p>
        </div>
      </template>
      <template #queryFields>
        <template v-bind:key="item.name" v-for="item in config.allColumns">
          <dynamic-query-field v-if="item.isQuery==='1'" :column="item"/>
        </template>
      </template>
    </left-tree-right-table-view>
  </div>

</template>

<script lang="jsx">
export default {
  name: "DynamicListView"
}
</script>
<script setup lang="jsx">
import {getCurrentInstance, ref, computed, watch} from "vue";
import LeftTreeRightTableView from "@/components/view/LeftTreeRightTableView";
import SingleTableView from "@/components/view/SingleTableView";
import DynamicQueryField from "@/components/dynamic/DynamicQueryField";
import {Modal} from "ant-design-vue";
import {prompt, UUID, dateFormat, isNotEmpty} from "@/lib/tools";
import {batchSaveSelectUrl, downLoadFileAction, saveDataUrl} from "@/api/api";
import {confirmAction, postAction} from "@/api/action";
import {encryptByDESModeEBC} from "@/lib/cryptoJS-aes";
import appConfig from "@/config";

let props = defineProps({
  table: {
    type: Object,
    default: () => {
      return {}
    }
  },
  columns: {
    type: Array,
    default: () => {
      return []
    }
  },
  extendSubTableData: {
    type: Object,
    default: () => {
      return {
        table: null,
        columns: [],
      }
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  autoHeight: {
    type: Boolean,
    default: false
  },
  height: {
    type: Number,
    default: null
  },
  width: {
    type: Number,
    default: null
  },
  parentId: {
    type: String,
    default: ''
  },
  parentFormData: {
    type: Object,
    default: () => {
      return {}
    }
  },
  /**
   * 过滤数据的数组
   */
  pageMode: {
    type: Boolean,
    default: false
  },
  filterDataArr: {
    type: Array,
    default: () => {
      return []
    }
  },
})
let instance = getCurrentInstance();
let config = ref({})
const useModernListSkin = computed(() => {
  return props.pageMode
      && (appConfig.theme?.adminLayout === 'modern' || appConfig.theme?.layoutStyle === 'modern')
      && appConfig.theme?.modernListSkin !== false
})
const dynamicPageTitle = computed(() => {
  return props.table?.comments || props.table?.title || config.value.singleTableViewProps?.modalTitle || props.table?.name || '动态表单'
})
const dynamicPageDesc = computed(() => {
  return (props.table?.blockChainParam6 || props.table?.blockchainParam6 || '').trim()
})
const effectiveQueryArea = computed(() => {
  const queryArea = config.value.singleTableViewProps?.queryArea || {}
  if (!useModernListSkin.value) {
    return queryArea
  }
  return {
    ...queryArea,
    resetButton: true,
    queryAreaStyle: 'ltr'
  }
})
const effectiveAutoHeight = computed(() => {
  return props.autoHeight || useModernListSkin.value
})
const effectiveSingleTableViewProps = computed(() => {
  const tableViewProps = config.value.singleTableViewProps || {}
  if (!useModernListSkin.value) {
    return tableViewProps
  }
  return {
    ...tableViewProps,
    queryArea: effectiveQueryArea.value,
    autoHeight: true
  }
})
const singleTableViewClass = computed(() => {
  return useModernListSkin.value ? 'modern-list-page dynamic-list-view' : ''
})
const treeTableViewClass = computed(() => {
  return useModernListSkin.value ? 'modern-tree-table-page tree-table-view dynamic-tree-table-view' : 'tree-table-view'
})
const leftTreeRightViewClass = computed(() => {
  return useModernListSkin.value ? 'modern-left-tree-right-page dynamic-left-tree-right-view' : ''
})
let buttonEventMap = computed(() => {
  return props.config?.buttonEventMap || {}
})
let rowEventMap = computed(() => {
  return props.config?.rowEventMap || {}
})
const clickButton = ({value, item}) => {
  if (buttonEventMap[value]) {
    let option = {
      tableViewRef: instance.refs.tableView,
      Modal: Modal,
      prompt: prompt,
      saveDataUrl: saveDataUrl,
      batchSaveSelectUrl: batchSaveSelectUrl,
      confirmAction: confirmAction,
      postAction: postAction,
      downLoadFileAction: downLoadFileAction,
      encryptByDESModeEBC: encryptByDESModeEBC,
      UUID: UUID,
      dateFormat: dateFormat
    }
    new Function('value', 'item', 'option', buttonEventMap[value])(value, item, option)
  }
}

const clickRow = ({value, row, index}) => {
  if (rowEventMap[value]) {
    let option = {
      tableViewRef: instance.refs.tableView,
      Modal: Modal,
      prompt: prompt,
      saveDataUrl: saveDataUrl,
      batchSaveSelectUrl: batchSaveSelectUrl,
      confirmAction: confirmAction,
      postAction: postAction,
      downLoadFileAction: downLoadFileAction,
      encryptByDESModeEBC: encryptByDESModeEBC,
      UUID: UUID,
      dateFormat: dateFormat
    }
    new Function('rowObject', 'option', rowEventMap[value])({value, row, index}, option)
  }
}

let singleTableViewPropsStatic = {
  extendFormData: {
    table: null,
    columns: [],
  },
  singleTable: {
    disableInitLoad: true,
    rowButtons: []
  }
}

/**
 * 根据工作流tab状态返回对应的操作按钮
 * Unsent(暂存): 编辑 + 删除
 * TodoAndDoing(待办): 办理
 * Done(已办)/path(全部)/其他: 查看
 */
const getActRowButtons = (actStatus, tableName) => {
  if (actStatus === 'Unsent') {
    return [{
      value: 'edit',
      text: '编辑',
    }]
  } else if (actStatus === 'TodoAndDoing') {
    return [{
      value: 'editAct',
      text: '办理',
    }]
  } else {
    // Done, path, suspend
    return [{
      value: 'viewAct',
      text: '查看',
    }]
  }
}

const onActStatusChange = (actStatus) => {
  if (config.value.singleTableViewProps?.act && props.table?.name) {
    config.value.singleTableViewProps.singleTable.rowButtons = getActRowButtons(actStatus, props.table.name)
  }
}

const initList = (table, columns) => {
  if (!table || !columns || columns.length === 0) {
    console.error('table or columns is null')
    return {}
  }
  let singleTableViewProps = JSON.parse(JSON.stringify(singleTableViewPropsStatic))
  singleTableViewProps.buttons = [{
    value: 'add',
    text: '添加',
    permission: 'app:' + table.name + ':add'
  }, {
    value: 'batch-delete',
    text: '删除',
    permission: 'app:' + table.name + ':del'
  }]
  let extendJs = {}
  rowEventMap = {}
  buttonEventMap = {}
  if (table.extendJs) {
    extendJs = JSON.parse(table.extendJs)
    delete table.extendJs
  }

  if (extendJs.list__buttons) {
    singleTableViewProps.buttons = extendJs.list__buttons
    extendJs.list__buttons.forEach(item => {
      if (extendJs['clickButton__' + item.value]) {
        buttonEventMap[item.value] = extendJs['clickButton__' + item.value]
      }
    })
  }
  if (extendJs.noOptButton === '1') {
    singleTableViewProps.buttons = []
  }
  if (extendJs.noCheckbox === '1') {
    singleTableViewProps.singleTable.rowSelection = false
  }

  let formProps = {}
  if (table.formProps) {
    formProps = JSON.parse(table.formProps)
  }
  if (typeof formProps.modal__Full !== 'undefined') {
    singleTableViewProps.modalFull = formProps.modal__Full
  }
  if (typeof formProps.modal__Width !== 'undefined') {
    singleTableViewProps.modalWidth = formProps.modal__Width
  }
  if (typeof formProps.list__modalTitle !== 'undefined') {
    singleTableViewProps.modalTitle = formProps.list__modalTitle
  }
  if (typeof formProps.list__modalTitleFuncStr !== 'undefined') {
    singleTableViewProps.modalTitleFuncStr = formProps.list__modalTitleFuncStr
  }

  singleTableViewProps.extendFormData.table = table
  let formColumns = []
  let listColumns = []
  let allColumns = []
  let queryCount = 0
  let firstLoadDataWithDefaultQueryField = false
  columns.forEach(item => {
    if (item.isQuery === '1') {
      queryCount++
    }
    if (item.isForm === '1') {
      formColumns.push(item)
    }
    if (item.isList === '1') {
      listColumns.push(item)
    }
    if (item.listConfig) {
      let listConfig = JSON.parse(item.listConfig)
      if (listConfig.title) {
        item.queryLabel = listConfig.title
      }
      if (listConfig.queryDefaultValue){
        firstLoadDataWithDefaultQueryField = true
      }
    }
    allColumns.push(item)
  })
  if (extendJs.extendColumns) {
    extendJs.extendColumns.forEach(item => {
      if (item.isQuery === '1') {
        queryCount++
      }
      if (item.listConfig) {
        let listConfig = JSON.parse(item.listConfig)
        if (listConfig.title) {
          item.queryLabel = listConfig.title
        }
        if(listConfig.queryDefaultValue && listConfig.queryDefaultValue !== ''){
          firstLoadDataWithDefaultQueryField = true
        }
        listColumns.push(item)
        allColumns.push(item)
      }
    })
  }
  singleTableViewProps.firstLoadDataWithDefaultQueryField = firstLoadDataWithDefaultQueryField
  singleTableViewProps.queryButton = queryCount > 0
  singleTableViewProps.extendFormData.columns = allColumns//把所有字段都给form页面 便于处理隐藏域的字段
  listColumns.sort((a, b) => {
    return Number(a.listSort) - Number(b.listSort)
  })

  //设置singleTable的配置
  singleTableViewProps.singleTable.columns = listColumns.filter(item => item.isList === '1' && item.listConfig).map(item => {
    let col = JSON.parse(item.listConfig)
    if (item.showType === 'iconselect') {
      col.customRenderSlot = ({column, text, index, record}) => {
        return <u-icon type={text}></u-icon>
      }
    }
    if (item.showType === 'richText' || item.showType === 'umeditor' || item.showType === 'summernote' || item.showType === 'tinymce' || item.showType === 'kindeditor') {
      col.customRenderSlot = ({ text }) => {
        if (!text) return <span></span>
        const plainText = text.replace(/<[^>]*>/g, '')
        return <span>{plainText}</span>
      }
    }
    if (item.isList === '1') {
      if (item.showType == 'fileupload') {
        let settings = JSON.parse(item.settings)
        if (settings.attachment.showTableList == true) {
          col.dataIndex = item.name + "__file"
          col.customRenderSlot = ({ text, record }) => {
            // `targetItem` 为一个数组，可以直接遍历
            const targetItem = record[col.dataIndex];
            if (targetItem && targetItem.length > 0) {
              return (
                <div>
                  {targetItem?.map((file, index) => (
                      <a
                          key={index}
                          onClick={() => downLoadFileAction(file.id)}
                          type="link"
                          style={{ display: 'block', marginBottom: '4px' }}
                      >
                        {file.name_} {/* 显示文件的 name 属性 */}
                      </a>
                  ))}
                </div>
              );
            }
          }
        }
      }
    }
    return col
  })

  //是否是工作流表单
  let isAct = isNotEmpty(table.processDefinitionCategory)
  singleTableViewProps.act = isAct
  if (isAct) {
    // 工作流表单需要设置初始状态和Tab显示配置
    singleTableViewProps.actStatus = 'Unsent'
    singleTableViewProps.actStatusButton = {
      showUnsent: true,       // 暂存
      showTodoAndDoing: true, // 待办
      showDone: true,         // 已办
      showAll: true,          // 全部
      showSuspend: false,     // 挂起
    }
  }

  singleTableViewProps.singleTable.disableInitLoad = (table.name || '').length === 0
  if (isAct) {
    // 工作流表单：根据当前tab设置不同按钮，初始为Unsent
    singleTableViewProps.singleTable.rowButtons = getActRowButtons('Unsent', table.name)
  } else if (extendJs.singleTable__rowButtons) {
    singleTableViewProps.singleTable.rowButtons = extendJs.singleTable__rowButtons.map(item => {
      return item
    })

    extendJs.singleTable__rowButtons.forEach(item => {
      if (extendJs['clickRow__' + item.value]) {
        rowEventMap[item.value] = extendJs['clickRow__' + item.value]
      }
    })
  } else {
    singleTableViewProps.singleTable.rowButtons = [{
      value: 'view',
      text: '查看',
      permission: 'app:' + table.name + ':view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:' + table.name + ':edit'
    }]
  }

  if (extendJs.noRowButton === '1') {
    singleTableViewProps.singleTable.rowButtons = []
  }

  if (table.id && table.name) {
    singleTableViewProps.formNo = table.name
  }
  singleTableViewProps.editOpenMode = table.blockChainParam5 || table.blockchainParam5 || '1'
  if (table.tableType === '3') {
    singleTableViewProps.singleTable.rowExpandable = () => true
    singleTableViewProps.singleTable.rowSelection = false
    singleTableViewProps.queryButton = false
  }
  if (table.parentTable && table.tableType !== '4') {
    //是子表并且不是左数右表
    singleTableViewProps.subTable = true
  }
  singleTableViewProps.singleTable.initParam = {
    filterDataArr: props.filterDataArr
  }
  if (extendJs.customSingleTableViewProps) {
    const setInObject = (target, source) => {
      for (const targetKey in source) {
        if (target[targetKey] && typeof target[targetKey] === 'object' && typeof source[targetKey] === 'object') {
          setInObject(target[targetKey], source[targetKey])
        } else if (typeof target[targetKey] === 'undefined') {
          target[targetKey] = source[targetKey]
        }
      }
    }
    setInObject(singleTableViewProps, extendJs.customSingleTableViewProps)
  }
  config.value = {
    tableType: table.tableType,
    parentTable: table.parentTable,
    singleTableViewProps: singleTableViewProps,
    allColumns: allColumns.sort((a, b) => {
      return Number(a.listSort) - Number(b.listSort)
    }),
    buttonEventMap,
    rowEventMap
  };
  import(`./DynamicForm.vue`).then(res => {
    config.value.singleTableViewProps.modalComponent = res.default
  })
}

watch(() => [props.table, props.columns], () => {
  console.log('initList')
  initList(JSON.parse(JSON.stringify(props.table)), JSON.parse(JSON.stringify(props.columns)))
}, {deep: true, immediate: true})

watch(() => props.extendSubTableData, () => {
  console.log('initList2')
  initList(JSON.parse(JSON.stringify(props.extendSubTableData.table)), JSON.parse(JSON.stringify(props.extendSubTableData.columns)))
}, {deep: true, immediate: true})
</script>
<style lang="less" scoped>
:deep(.ant-table-cell-with-append) {
  display: flex;
  align-items: center;
}
</style>
