<template>
  <single-table-view form-no="columns" @update:value="mockTableColumnsChange" :rowEdit="true" :height="configTableHeight"
                     :buttons="[{value:'addRow',text:'添加'},{value:'selectFormItem',text:'设置'},{value:'sync',text:'同步字段title'},{value:'resetDataIndex',text:'重置dataIndex'}]"
                     :query-button="false"
                     :single-table="mockColumnsColumnsEditorTable"
                     @clickButton="clickButton">
    <template #queryFields>
    </template>
    <template #extendHiddenArea>
      <u-modal ref="settingModal" :width="1200" :custom-body-style="{height:'70vh',overflow:'auto'}" :customOK="true" @clickOk="clickSettingModal">
        <div class="setting-container">
          <div>
            <a-typography-title :level="4">隐藏域字段</a-typography-title>
            <a-tree
                checkable
                :selectable="false"
                :tree-data="treeDataFormHidden"
                v-model:checkedKeys="hiddenCheckedKeys"
                :fieldNames="{children: 'children', title: 'listConfig__title', key: 'listConfig__dataIndex'}"
            />
          </div>
          <div>
            <a-typography-title :level="4">扩展字段</a-typography-title>
            <a-tree
                checkable
                :tree-data="treeDataExtendColumn"
                :selectable="false"
                v-model:checkedKeys="extendCheckedKeys"
                :fieldNames="{children: 'children', title: 'listConfig__title', key: 'listConfig__dataIndex'}"
            />
          </div>
          <div>
            <a-typography-title :level="4">显示域字段</a-typography-title>
            <a-tree
                checkable
                :tree-data="treeDataFormShow"
                :selectable="false"
                v-model:checkedKeys="showCheckedKeys"
                :fieldNames="{children: 'children', title: 'listConfig__title', key: 'listConfig__dataIndex'}"
            />
          </div>
          <div>

            <a-typography-title :level="4">列表显示字段</a-typography-title>
            <a-tree
                class="draggable-tree"
                draggable
                block-node
                :tree-data="sortListColumn"
                @drop="onDrop"
                :fieldNames="{children: 'children', title: 'listConfig__title', key: 'listConfig__dataIndex'}"
            />
          </div>
        </div>

      </u-modal>
    </template>
  </single-table-view>
</template>

<script>
export default {
  name: "formDesignListSetting"
}
</script>
<script setup>
import {computed, getCurrentInstance, ref, watch} from "vue";
import {editorTable, mockTableColumnsArr} from "@/views/gen/genTableExt/formStaticConfig";
import SingleTableView from "@/components/view/SingleTableView";
import UModal from "@/components/modal/UModal";
import {oneOf, UUID} from "@/lib/tools";
import {initColumnListConfig} from "@/views/gen/genTableExt/formDesign";

let instance = getCurrentInstance();
let props = defineProps({
  configTableHeight: {
    type: Number,
    default: 300
  },
  allColumns: {
    type: Array,
    default() {
      return []
    }
  },
})
let listColumns = ref([])

let hiddenCheckedKeys = ref([])
let treeDataFormHidden = computed(() => {
  return listColumns.value.filter(item => item.isForm === '0' && item.isExtend !=='1')
})
let showCheckedKeys = ref([])
let treeDataFormShow = computed(() => {
  return listColumns.value.filter(item => item.isForm === '1' && item.isExtend !=='1')
})

let extendCheckedKeys = ref([])
let treeDataExtendColumn = computed(() => {
  return listColumns.value.filter(item => item.isExtend === '1')
})

let sortListColumn = ref([])
let mockColumns = computed(() => {
  let arr = listColumns.value.filter(item => item.isList === '1')
  arr.sort((a, b) => {
    return a.listSort - b.listSort
  })
  return arr
})

const updateSortListColumn = () => {
  sortListColumn.value = mockColumns.value.map(item => {
    let obj = {...item}
    obj.children = []
    return obj
  })
}
let allColumnsMap = computed(() => {
  let map = {}
  props.allColumns.forEach(item => {
    map[item.name] = item
  })
  return map
})

const updateSortTree = () => {
  let listColumnIndexArr = mockColumns.value.map(item => item.listConfig__dataIndex)
  hiddenCheckedKeys.value = treeDataFormHidden.value.filter(item => oneOf(item.listConfig__dataIndex, listColumnIndexArr)).map(item => item.listConfig__dataIndex)
  showCheckedKeys.value = treeDataFormShow.value.filter(item => oneOf(item.listConfig__dataIndex, listColumnIndexArr)).map(item => item.listConfig__dataIndex)
  extendCheckedKeys.value = treeDataExtendColumn.value.filter(item => oneOf(item.listConfig__dataIndex, listColumnIndexArr)).map(item => item.listConfig__dataIndex)

}
watch(() => props.allColumns, () => {
  listColumns.value = props.allColumns.map(initColumnListConfig)
  updateSortTree()
  updateSortListColumn()
}, {deep: true, immediate: true})

const changeListShowColumns = (targetArr, arr) => {
  listColumns.value.filter(item => oneOf(item.listConfig__dataIndex, targetArr)).forEach(item => {
    if (oneOf(item.listConfig__dataIndex, arr)) {
      item.isList = '1'
    } else {
      item.isList = '0'
    }
  })
  updateSortListColumn()
}
watch(() => hiddenCheckedKeys.value, () => {
  changeListShowColumns(treeDataFormHidden.value.map(item => item.listConfig__dataIndex), hiddenCheckedKeys.value)
})
watch(() => showCheckedKeys.value, () => {
  changeListShowColumns(treeDataFormShow.value.map(item => item.listConfig__dataIndex), showCheckedKeys.value)
})
watch(() => extendCheckedKeys.value, () => {
  changeListShowColumns(treeDataExtendColumn.value.map(item => item.listConfig__dataIndex), extendCheckedKeys.value)
})

let mockColumnsColumnsEditorTable = computed(() => {
  let config = {}
  Object.assign(config, editorTable)
  config.showIndex = true
  config.idField = 'name'
  config.data = mockColumns.value
  config.columns = mockTableColumnsArr
  config.rowButtons = [
    {
      value: 'delete',
      text: '移除',
      visibleFilter: (row) => {
        return row.isExtend === '1'
      },
    }
  ]
  config.showRowButtons = true
  return config
})

const clickButton = ({value}) => {
  if (value === 'selectFormItem') {
    instance.refs.settingModal.open('设置')
  }else if(value === 'addRow'){
    listColumns.value.push({
      name: UUID(),
      isExtend: '1',
      isList: '1',
      listSort: listColumns.value.length * 10,
      isQuery: '0',
      queryType: '',
      blockChainParam4: '0',
      blockChainParam5: '0',
      listConfig__dataIndex: '',
      listConfig__title: '',
      listConfig__minWidth: 150,
      listConfig__align: 'left',
      listConfig__sorter: 'false',
      listConfig__ellipsis: false,
      queryLabel: '',
    })
  }else if(value === 'sync'){
    listColumns.value = props.allColumns.map(item => initColumnListConfig(item, true))
    updateSortTree()//同步表单title更新字段排序树
    updateMockTableColumnSetting(listColumns.value)
  }else if(value === 'resetDataIndex'){
    listColumns.value.forEach(item => {
      // 1. 重置dataIndex：根据控件类型（showType）判断
      // 从allColumns中查找对应的原始列信息以获取showType
      const column = props.allColumns.find(col => col.name === item.name)
      const showType = column ? column.showType : ''
      // 人员选择(treeselectRedio)、人员多选(treeselectCheck)、部门选择(officeselectTree)、自定义选择(gridselect)、上级(parentId)、行政区(areaselect)
      // 这些控件类型的dataIndex为字段名加__name
      if (['treeselectRedio', 'treeselectCheck', 'officeselectTree', 'gridselect', 'parentId', 'areaselect'].includes(showType)) {
        item.listConfig__dataIndex = item.name + '__name'
      } else {
        // 其他控件类型dataIndex为字段名
        item.listConfig__dataIndex = item.name
      }

      // 2. 重置查询字段：根据查询组件类型（queryFieldType）判断
      // 查询字段格式为 "tablealias.id" 或 "tablealias.name"
      const queryFieldType = item.listConfig__queryFieldType
      const queryColumn = item.listConfig__queryColumn
      if (queryColumn && queryColumn.includes('.')) {
        const prefix = queryColumn.substring(0, queryColumn.lastIndexOf('.'))
        if (idQueryFieldTypes.includes(queryFieldType)) {
          // 对象/选择类查询字段后缀为.id
          item.listConfig__queryColumn = prefix + '.id'
        } else if (nameQueryFieldTypes.includes(queryFieldType)) {
          // 文本输入框：查询字段后缀为.name
          item.listConfig__queryColumn = prefix + '.name'
        }
      }
    })
    updateMockTableColumnSetting(listColumns.value)
  }
}
let emtis = defineEmits(['update:value'])
const idQueryFieldTypes = [
  'area',
  'cascader-select',
  'modal-select',
  'modal-multi-select',
  'user-select',
  'users-select',
  'office-select',
  'tree-select',
]
const nameQueryFieldTypes = [
  'input',
]
const getIdQueryColumn = (javaField = '', fallback = '') => {
  if (!javaField) {
    return fallback
  }
  return javaField.replace(/\|.*$/, '')
}
const updateMockTableColumnSetting = (columns) => {
  let map = {}
  columns.forEach(item => {
    map[item.name] = item
  })
  emtis('update:value', map)
}

const mockTableColumnsChange = (data) => {
  data.forEach(item => {
    if (item.isQuery === '1') {
      //如果是查询字段 设置默认值
      let column = allColumnsMap.value[item.name];
      let listConfig = column.listConfig ? JSON.parse(column.listConfig) : {};
      if (column.showType === 'input'){
        if (!item.listConfig__queryFieldType){
          item.listConfig__queryFieldType = 'input'
        }
        if (!item.queryType){
          item.queryType = 'like'
        }
      }
      if (column.showType === 'select' || column.showType === 'radiobox' || column.showType === 'checkbox'){
        if (!item.listConfig__queryFieldType){
          item.listConfig__queryFieldType = 'select'
        }
        if (!item.listConfig__queryFieldProps) {
          //下拉选择设根据 是否系统字典设置 props
          if (column.selectSimple === '0') {
            let formItemConfig = column.formItemConfig ? JSON.parse(column.formItemConfig) : {}
            let formControlProps = formItemConfig.formControlProps || {}
            let props = formControlProps.props || {}
            props.placeholder = column.comments
            item.listConfig__queryFieldProps = props
          } else {
              item.listConfig__queryFieldProps = {
                placeholder: column.comments,
                dictType: column.dictType
              }
          }

        }
      }
      /*if (listConfig.queryFieldType === 'select') {
        item.listConfig__queryFieldProps = {
          placeholder: listConfig.title,
          dictType: listConfig.dict
        }
      }*/
      if (column.showType === 'dateselect'){
        if (!item.listConfig__queryFieldType){
          item.listConfig__queryFieldType = 'date-range'
        }
        if (!item.listConfig__queryFieldProps) {
          //日期选择设置默认的日期格式 zhoury 2024-06-28
          item.listConfig__queryFieldProps = {
            placeholder: column.comments,
            formatPatter: column.dateType
          }
        }
      }
      if (column.showType === 'treeselectRedio'){
        if (!item.listConfig__queryFieldType){
          item.listConfig__queryFieldType = 'user-select'
          item.listConfig__queryColumn = getIdQueryColumn(column.javaField, item.name)
          item.queryType = '='
        }
      }
      if (column.showType === 'treeselectCheck'){
        if (!item.listConfig__queryFieldType){
          item.listConfig__queryFieldType = 'users-select'
          item.listConfig__queryColumn = getIdQueryColumn(column.javaField, item.name)
          item.queryType = 'in'
        }
      }
      if (column.showType === 'officeselectTree'){
        if (!item.listConfig__queryFieldType){
          item.listConfig__queryFieldType = 'office-select'
          item.listConfig__queryColumn = getIdQueryColumn(column.javaField, item.name)
          item.queryType = '='
        }
      }
      if (column.showType === 'areaselect'){
        if (!item.listConfig__queryFieldType){
          item.listConfig__queryFieldType = 'area'
          item.listConfig__queryColumn = getIdQueryColumn(column.javaField, item.name)
          item.queryType = '='
        }
      }
      if (column.showType === 'treeselect' || column.showType === 'treeSelect'){
        if (!item.listConfig__queryFieldType){
          item.listConfig__queryFieldType = 'tree-select'
          item.listConfig__queryColumn = getIdQueryColumn(column.javaField, item.name)
          item.queryType = '='
        }
      }
      if (!item.listConfig__queryFieldProps){
        item.listConfig__queryFieldProps = {
          placeholder: column.comments ||  listConfig.title
        }
      }
    }
  })
  updateMockTableColumnSetting(data)
}

const onDrop = (info) => {
  const dropPosition = info.dropPosition//放下的位置
  const dropKey = info.node.key;
  const dragKey = info.dragNode.key;
  //dropToGap 是否在两个节点的缝隙中 为false 代表拖动为子节点  这里先不用考虑

  const data = [...sortListColumn.value];
  const loop = (data, key, callback) => {
    data.forEach((item, index) => {
      if (item.listConfig__dataIndex === key) {
        return callback(item, index, data);
      }
      if (item.children) {
        return loop(item.children, key, callback);
      }
    });
  };
  //寻找被拖动的节点
  // Find dragObject
  let dragObj;
  loop(data, dragKey, (item, index, arr) => {
    arr.splice(index, 1);
    dragObj = item;
  });

  let ar = [];
  let i = 0;
  loop(data, dropKey, (_item, index, arr) => {
    ar = arr;
    i = index;
  });
  if (dropPosition === -1) {
    ar.splice(i, 0, dragObj);
  } else {
    ar.splice(i + 1, 0, dragObj);
  }
  sortListColumn.value = data
}

const clickSettingModal = () => {
  let listSortMap = {}
  sortListColumn.value.forEach((item, index) => {
    listSortMap[item.listConfig__dataIndex] = (index + 1) * 10
  })
  listColumns.value.forEach(item => {
    if (listSortMap[item.listConfig__dataIndex]) {
      item.listSort = listSortMap[item.listConfig__dataIndex]
    }
  })
  updateMockTableColumnSetting(listColumns.value)
  instance.refs.settingModal.close()
}
</script>
<style lang="less" scoped>
.setting-container {
  height: 100%;
  display: flex;
  flex-direction: row;
  align-items: flex-start;

  > div {
    width: 33%;
    overflow-y: auto;
  }
}
</style>
