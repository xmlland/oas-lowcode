<template>
  <a-form ref="uForm" :model="tableEditorValue">
    <a-table :id="id"
             class="ant-table-striped"
             :class="[autoHeight?'table-auto-height':'',titleAlignCenter?'title-align-center':'']"
             size="small"
             :loading="loading"
             :columns="tableColumns"
             :data-source="tableDataSource"
             :pagination="localPagination"
             :rowExpandable="rowExpandable"
             :defaultExpandAllRows="false"
             :bordered="bordered"
             row-key="id"
             :customRow="customRow"
             :transformCellText="transformCellText"
             :row-class-name="(_record, index) => {return (getCheckboxProps&&getCheckboxProps(_record).disabled?'disabled-row ':' ')+
                                                          (index % 2 === 1 ? ' table-striped ' : ' table-not-striped ') +
                                                          (shouldChangeStyle!=null?(shouldChangeStyle(_record,index)==true?'change-style': ' '+shouldChangeStyle(_record)+' '):' ') +
                                                          (showSelectRowStyle&&currentSelectIndex === index?selectRowStyle:' ') }"

             :row-selection="(disabledShowRowSelection || (!disabled && rowSelection))
              ? {
                  selectedRowKeys: selectedRowKeys,
                  onSelectAll: onSelectAll,
                  onSelect: onSelect,
                  onChange: onSelectChange,
                  type: rowSelectionType,
                  getCheckboxProps: getCheckboxProps
                }
              : null"
             :scroll="tableScroll"
             :style="height?{height:tableViewHeight+'px'}:{}"
             @change="tableChange"
             :expandedRowKeys="currentTableExpand"
             @expandedRowsChange="expand"
             @resizeColumn="handleResizeColumn">
      <template #bodyCell="{ column, text, index,record}">
        <template v-if="column">
          <template v-if="column.dataIndex === '_opt'">
            <div class="table-row-btn-container" v-if="rowButtonCount(record) <= 3 || !isShowMoreDropdown">
              <template v-for="item in getRowButtonsByRow(record)">
                <div v-if="!item.customRenderSlot"
                     @click="clickRow(item, record)"
                     v-bind:key="item.value"
                     :style="item.color ? { color: 'var(--ant-' + item.color + '-color)' } : {}"
                     class="table-row-btn">
                  {{ item.text }}
                </div>
                <template v-else>
                  <component  @click="clickRow(item, record)" :is="item.customRenderSlot({ item, record })" v-bind:key="item.value"></component>
                </template>
              </template>
            </div>
            <div class="table-row-btn-container" v-else>
              <template v-for="(item,index) in getRowButtonsByRow(record)" v-bind:key="item.value">
                <div v-if="index<2" @click="clickRow(item,record)" class="table-row-btn"
                     :style="item.color?{color:'var(--ant-'+item.color+'-color)'}:{}">
                  {{ item.text }}
                </div>
              </template>
              <a-dropdown>
                <div class="table-row-btn" @click.prevent>
                  更多
                  <DownOutlined/>
                </div>
                <template #overlay>
                  <a-menu>
                    <template v-for="(item,index) in getRowButtonsByRow(record)" v-bind:key="item.value">
                      <a-menu-item v-if="index>=2">
                        <div @click="clickRow(item,record)" class="table-row-btn"
                             :style="item.color?{color:'var(--ant-'+item.color+'-color)'}:{}">
                          {{ item.text }}
                        </div>
                      </a-menu-item>
                    </template>
                  </a-menu>
                </template>
              </a-dropdown>

            </div>
          </template>
          <template v-else-if="column.dataIndex === '_index'">
            {{ formatIndex(index) }}
          </template>
          <span v-else-if="column.customRender" v-html="column.customRender({column, text, index, record})">
        </span>
          <template v-else-if="column.status">
            <a-tag v-if="typeof text === 'string' ? text ==='1' :text" color="green">{{ column.status.enableText || '启用' }}</a-tag>
            <a-tag v-if="text==='0'" color="red">{{ column.status.disableText || '禁用' }}</a-tag>
            <a-tag v-if="text==='2'"  color="rgba(0,0,0,0)"></a-tag>
          </template>
          <template v-else-if="column.progress">
            <a-progress style="width: 80%;" :percent="text" size="small" :stroke-color="{
          '0%': '#108ee9',
          '100%': '#87d068',
        }"/>
          </template>
          <template v-else-if="column.dynamicDict">
            {{ translateDict(text, column.dynamicDict({column, text, index, record}), column.dictMultiple) }}
          </template>
          <template v-else-if="column.dict">
            {{ translateDict(text, column.dict, column.dictMultiple) }}
          </template>
          <template v-else-if="column.boolean">
            {{ text === true || text === 1 || text === '1' ? '是' : (text === false || text === 0 || text === '0' ? '否' : '') }}
          </template>
          <template v-else-if="column.editorSlot">
            <component :is="column.editorSlot({column, text, index, record})"></component>
          </template>
          <template v-else-if="column.customRenderSlot">
            <component :is="column.customRenderSlot({column, text, index, record,instance})"></component>
          </template>
          <template v-else>
            {{ text }}
          </template>
        </template>
      </template>
      <template v-if="$slots.expandedRowRender"  #expandedRowRender="{ record }">
        <slot name="expandedRowRender" :record="record"></slot>
      </template>
      <template v-if="$slots.title" #title>
        <slot name="title"></slot>
      </template>
      <template #summary>
        <slot name="summary"></slot>
        <a-table-summary-row v-if="!isNullOrEmpty(totalRow)">
          <a-table-summary-cell v-if="!disabled&&rowSelection"></a-table-summary-cell>

          <template v-bind:key="column.dataIndex" v-for="column in treeToList(tableColumns)">
            <a-table-summary-cell v-if="column.dataIndex === '_opt'||column.dataIndex === '_index'"></a-table-summary-cell>
            <a-table-summary-cell v-else :style="{textAlign: column.align}">
              {{totalRow[column.dataIndex]}}
            </a-table-summary-cell>
          </template>
        </a-table-summary-row>
        <a-table-summary-row v-if="!isNullOrEmpty(groupMap)">
          <a-table-summary-cell v-if="!disabled&&rowSelection"></a-table-summary-cell>
          <a-table-summary-cell v-if="showIndex"></a-table-summary-cell>
          <a-table-summary-cell :col-span="treeToList(tableColumns).length - (showIndex?1:0)  - 1">
            <span class="table-group-statistics" v-bind:key="key" v-for="(value,key) in groupMap">
               <template v-bind:key="textName" v-for="(text,textName) in value">
                  &nbsp;&nbsp;{{ textName }} ：  {{ text }}
               </template>
            </span>
          </a-table-summary-cell>
          <a-table-summary-cell></a-table-summary-cell>
        </a-table-summary-row>
      </template>
      <template v-if="$slots.customFilterDropdown" #customFilterDropdown="{ setSelectedKeys, selectedKeys, confirm, clearFilters, column }">
        <slot name="customFilterDropdown"
              :setSelectedKeys="setSelectedKeys"
              :selectedKeys="selectedKeys"
              :confirm="confirm"
              :clearFilters="clearFilters"
              :column="column"
        ></slot>
      </template>
    </a-table>
  </a-form>
</template>

<script lang="jsx">
export default {
  name: "SingleTable"
}
</script>
<script setup lang="jsx">
import {ref, onMounted, watch, getCurrentInstance, inject, nextTick, computed} from "vue";
import {postAction} from "@/api/action";
import {getData, hasAnyPermission, UUID, translateDict, oneOf, deepClone, isNullOrEmpty} from "@/lib/tools";
import {DownOutlined} from "@ant-design/icons-vue";
import config from "@/config"
import {mergeQueryData} from "@/components/table/tableTool";
let id = ref('single-table' + UUID())
let instance = getCurrentInstance();
let tableViewHeight = ref(0);//表格总高度
let tableHeight = ref(0);//表格高度
let totalColumnWidth = ref(0)//表格总列宽
let tableWidth = ref(0);//表格非固定列宽度
let tempTableWidth = ref(0);//表格非固定可resize列宽度 临时存储
let tempTableResizeWidth = ref(0);//表格非固定不可resize列宽度 临时存储

let define = defineProps({
  width: {
    type: Number,
    default: null
  },
  height: {
    type: Number,
    default: null
  },
  autoHeight: {
    type: Boolean,
    default: false
  },
  scrollHeight: {
    type: Number,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  disabledShowRowSelection: {
    type: Boolean,
    default: false
  },
  /**
   * 初始表格时是否加载数据
   */
  disableInitLoad: {
    type: Boolean,
    default: false,
  },
  /**
   * 默认查询参数
   */
  initParam: {
    type: Object,
    default() {
      return {}
    },
  },
  // 自定义参数,
  customParam: {
    type: Function,
    default: null
  },
  rowSelection: {
    type: Boolean,
    default: true,
  },
  rowSelectionType: {
    type: String,
    default: 'checkbox',
  },
  defaultSelectedRowKeys: {
    type: Array,
    default() {
      return []
    },
  },
  getCheckboxProps: {
    type: Function,
    default: null
  },
  /*设置某行单元格样式*/
  shouldChangeStyle: {
    type: Function,
    default: null
  },
  /**
   * 加载数据的url
   */
  dataUrl: {
    type: String,
    default: '',
  },
  /**
   * 统计数据的url 用于自定义查询汇总行及分组统计行的数据
   */
  statisticsDataUrl: {
    type: String,
    default: '',
  },
  /**
   * 表单id字段名称
   */
  idField: {
    type: [String, Array],
    default: 'id',
  },
  /**
   * 静态数据
   */
  data: {
    type: Array,
    default() {
      return null
    }
  },
  /**
   * 列配置
   */
  columns: {
    type: Array,
    default() {
      return []
    }
  },
  showSorter:{
    type: Boolean,
    default: true,
  },
  /**
   * 分页配置
   */
  pagination: {
    type: [Object, Boolean],
    default() {
      return (!config.table || typeof config.table.pagination === 'undefined') ? {
        current: 1,
        defaultPageSize: 10,
        pageSize: 10,
        showQuickJumper: true,
        showSizeChanger: true,
        hideOnSinglePage: false,
        showTotal: (total) => {
          return `共 ${total} 条`
        },
        total: 0
      } : deepClone(config.table.pagination)
    }
  },
  showRowButtons: {
    type: Boolean,
    default: true
  },
  showIndex: {
    type: Boolean,
    default: true
  },
  rowExpandable: {
    type: Function,
    default:(record)=>{
      return false
    }
  },
  loadChildrenAction: {
    type: Function,
    default: null
  },
  bordered: {
    type: Boolean,
    default: (!config.table || typeof config.table.bordered === 'undefined') ? true : config.table.bordered
  },
  /**
   * 标题强制居中 为false时与数据的对齐方式一致
   */
  titleAlignCenter: {
    type: Boolean,
    default: (!config.table || typeof config.table.titleAlignCenter === 'undefined') ? false : config.table.titleAlignCenter
  },
  /**
   * 额外减去的高度
   */
  extraDiffHeight: {
    type: Number,
    default: (!config.table || typeof config.table.extraDiffHeight === 'undefined') ? 0 : config.table.extraDiffHeight
  },
  /**
   * 忽略expend事件
   */
  ignoreExpandEvent: {
    type: Boolean,
    default: false
  },
  /**
   * 行编辑按钮
   */
  rowButtons: {
    type: Array,
    default() {
      return [{
        value: 'view',
        text: '查看',
      }, {
        value: 'edit',
        text: '编辑',
      }/*, {
        value: 'delete',
        text: '删除',
        color: 'error',
      }*/]
    }
  },
  /**
   * 行点击事件
   * */
  customTableRowEvent:{
    type: Function,
    default:undefined
  },
  formNo: {
    type: String,
    default: ''
  },
  ignoreSetTableRef: {
    type: Boolean,
    default: false
  },
  /**
   * 合并行配置
   */
  mergeColumnConfig: {
    type: Array,
    default: ()=>{
      return []
    }
  },
  optionWidth:{
    type: Number,
    default: null
  },
  /**
   * 表格数据
   */
  value : {
    type: Array,
    default: () => {
      return []
    }
  },
  /**
   * 未保存数据的位置 是在前面还是后面 before afeter
   */
  unSaveRowsLocation: {
    type: String,
    default: 'before'
  },
  showSelectRowStyle:{
    type:Boolean,
    default:false
  },
  selectRowStyle:{
    type:String,
    default:'select-style'
  },
  /**
   * 行操作区域 是否显示 “更多”
   */
  isShowMoreDropdown:{
    type:Boolean,
    default:true
  },
  /**
   * 单元格为空时默认值
   */
  cellEmptyDefaultValue:{
    type: String,
    default: config.cellEmptyDefaultValue,
  }
})
let indexColumn = {
  title: '序号',
  align: 'center',
  dataIndex: '_index',
  fixed: 'left',
  width: 60,
  customRender({index}) {
    if (localPagination.value) {
      return (localPagination.value.current - 1) * localPagination.value.pageSize + (index + 1)
    }
    return index + 1
  }
}

let _rowButtons = computed(()=>{
  return define.rowButtons.filter(t => {
    if (t.disableShow) {
      return true
    }
    if (define.disabled) {
      if (t.value !== 'view') {
        return false
      }
    }
    return true
  }).filter(item => {
    if (!item.permission) {
      return true
    }
    return hasAnyPermission([item.permission])
  })
})
/*watch(() => define.disabled, () => {
  filterButton()
}, {immediate: true})*/

const rowButtonShow = (btn, row) => {
  if (btn.visibleFilterStr){
    let res = true
    try {
      res = new Function('btn','row', btn.visibleFilterStr)(btn, row)
    } catch (e) {
      console.error(e)
    }
    return res
  }else if (btn.visibleFilter) {
    return btn.visibleFilter(row)
  }
  return true
}

//根据行内容动态修改 btn
const rowButtonTextEdit = (btn, row) => {
  if (btn.rowButtonTextEdit) {
    return btn.rowButtonTextEdit(btn, row)
  } else {
    return btn
  }
}

const rowButtonCount = (row) => {
  let count = 0;
  // 当有编辑按钮时， 去除查看按钮
  let haveEditFlag = false;
  _rowButtons.value.forEach(r => {
    if(r.value){
      if(r.value.substring(0,4)==='edit'){
        haveEditFlag = true;
      }
    }
  })
  _rowButtons.value.filter(item=>{
    if(!haveEditFlag){
      return true;
    }else {
      if(item.value){
        if(item.value.substring(0,4)!=='view'){
          return true;
        }
      }
    }
    return false;
  }).forEach(btn => {
    if (rowButtonShow(btn, row)) {
      count++
    }
  })
  return count
}
const getRowButtonsByRow = (row) => {
  let buttons = [];
  _rowButtons.value.forEach(btn => {
    if (rowButtonShow(btn, row)) {
      let btnRes = rowButtonTextEdit(btn, row)
      buttons.push(btnRes)
    }
  })

  // 当有编辑按钮时， 去除查看按钮
  let haveEditFlag = false;
  buttons.forEach(r => {
    if(r.value){
      if(r.value.substring(0,4)==='edit'){
        haveEditFlag = true;
      }
    }
  })
  let t_buttons = []
  buttons.forEach(btn => {
    if(!haveEditFlag){
      t_buttons.push(btn);
    }else {
      if(btn.value){
        if(btn.value.substring(0,4)!=='view'){
          t_buttons.push(btn);
        }
      }else {
        t_buttons.push(btn);
      }
    }
  })

  // region 根据按钮设置的  replaceRowButtons （英文逗号拼接的按钮value数据） 判断是否去除该按钮
  const currentValues = new Set();
  t_buttons.forEach(btn => {
    if (btn.value) {
      currentValues.add(btn.value);
    }
  });

  // 过滤掉需要被替换的按钮
  t_buttons = t_buttons.filter(btn => {
    // 没有 replaceRowButtons 则保留
    if (!btn.replaceRowButtons) return true;

    // 分割并处理可能的空格
    const replaceList = btn.replaceRowButtons.split(',').map(v => v.trim());
    // 检查是否存在需要替换的按钮
    const hasReplacement = replaceList.some(v => currentValues.has(v));

    // 存在则过滤掉当前按钮
    return !hasReplacement;
  });
  // endregion

  return t_buttons
}
let optColumn = computed(()=>{
  // 当有编辑按钮时， 去除查看按钮
  let haveEditFlag = false;
  _rowButtons.value.forEach(r => {
    if(r.value){
      if(r.value.substring(0,4)==='edit'){
        haveEditFlag = true;
      }
    }
  })
  return {
    title: '操作',
    align: 'center',
    dataIndex: '_opt',
    fixed: 'right',
    width: define.optionWidth != null? define.optionWidth: _rowButtons.value.length > 3 ? 200 : (60 * _rowButtons.value.filter(item=>{
      if(!haveEditFlag){
        return true;
      }else {
        if(item.value){
          if(item.value.substring(0,4)!=='view'){
            return true;
          }
        }
        return false;
      }
    }).length + 20),
    customCell: (record, rowIndex, column) => {
      if (record['_opt_rowspan'] || record['_opt_rowspan'] === 0) {
        return {
          rowSpan: record['_opt_rowspan'],
        }
      }
      return {
        rowSpan: 1,
      }
    }
  }
})
let defineColumns = ref([]);
let columnsDeepLength = ref(1);
let tableEditorValue = ref({});

const onCellValueChange = (data) =>{
  emits('cellValueChange',data)
  emits('update:value', tableDataSource.value)
}

const dealColumnEditor = (column) => {
  /**
   * 更新单元格的值
   * @param formId
   * @param e
   */
  const updateCellValue = ({formId, e}) => {
    tableEditorValue.value[formId] = e
    let totalIndex = 0;
    let currentIndex = 0;
    let currentRecord = {};

    const forEachFunc = row => {
      if (formId.indexOf(row.__temp_id__) >= 0 || formId.indexOf(getData(row, define.idField)) >= 0) {
        row[column.dataIndex] = e
        currentIndex = totalIndex
        currentRecord = row
      }
      totalIndex++
    }
    unSaveRows.value.forEach(forEachFunc)
    dataSource.value.forEach(forEachFunc)
    onCellValueChange({column, text: e, index:currentIndex, record: currentRecord})
  }

  /**
   * 初始化编辑器
   * @param column
   * @param text
   * @param index
   * @param record
   * @return {{formId: string, rules: *[], props: {}}}
   */
  const initEditor = ({column, text, index, record}) => {
    let props =  {}
    if (column.editor.props) {
      Object.assign(props, column.editor.props)
    }
    if (typeof props.disabled === 'function') {
      props.disabled = props.disabled({column, text, index, record})
    }
    //输入框最大、最小值支持function start zhuhao 2024-07-25
    if (column.editor.type==='input'){
      if (typeof props.max === 'function') {
        props.max = props.max({column, text, index, record})
      }
      if (typeof props.min === 'function') {
        props.min = props.min({column, text, index, record})
      }
    }
    //输入框最大、最小值支持function end
    let formId = record.__temp_id__ || record.id
    if (!formId) {
      formId = 'temp_' + index
      record.__temp_id__ = formId
    }
    formId = formId + '_' + column.dataIndex
    tableEditorValue.value[formId] = text
    //单元格校验规则支持function start zhoury 2024-12-19
    let rules = column.editor.rules || []
    if (typeof rules === 'function') {
      rules = rules({column, text, index, record})
    }
    //单元格校验规则支持function end
    return {
      formId,
      props,
      rules: rules,
    }
  }
  column.editorSlot = ({column, text, index, record}) => {
    let {props, formId, rules} = initEditor({column, text, index, record});
    // 如果单元格类型时，函数，那么通过函数判断当前编辑行类型，
    let editorType = typeof column.editor.type === 'function' ? column.editor.type({column, text, index, record}) : column.editor.type

    let style = {display:'flex'}
    if(column.editor.style){
      Object.assign(style,column.editor.style);
    }

    if ('upload' === editorType) {
      //行编辑支持上传组件 zhoury 2024-12-19
      return <a-form-item name={formId} rules={rules}>
        <div style={style}>
          {column.editor.addonBefore && typeof column.editor.addonBefore === 'function'?column.editor.addonBefore({column, text, index, record}):<span></span>}
          <u-upload {...props} value={tableEditorValue.value[formId]} onUpdate:value={(e) => {
            updateCellValue({formId, e})
          }}/>
        </div>
      </a-form-item>
    }else if ('input' === editorType) {
      return <a-form-item name={formId} rules={rules}>
        <div style={style}>
          {column.editor.addonBefore && typeof column.editor.addonBefore === 'function'?column.editor.addonBefore({column, text, index, record}):<span></span>}
          <u-input {...props} value={tableEditorValue.value[formId]} onUpdate:value={(e) => {
            updateCellValue({formId, e})
          }}/>
        </div>
      </a-form-item>
    }else  if ('select' === editorType) {
      if (typeof props.optionData === 'function') {
        props.optionData = props.optionData({column, text, index, record})
      }
      if (typeof props.dictType === 'function') {
        props.dictType = props.dictType({column, text, index, record})
      }

      return <a-form-item name={formId} rules={rules}>
        <div style={style}>
          {column.editor.addonBefore && typeof column.editor.addonBefore === 'function'?column.editor.addonBefore({column, text, index, record}):<span></span>}
          <u-select {...props} value={tableEditorValue.value[formId]} onUpdate:value={(e) => {
            updateCellValue({formId, e})
          }}/>
        </div>
      </a-form-item>
    }else  if ('date' === editorType) {
      return <a-form-item name={formId} rules={rules}>
        <div style={style}>
          {column.editor.addonBefore && typeof column.editor.addonBefore === 'function'?column.editor.addonBefore({column, text, index, record}):<span></span>}
          <u-date {...props} value={tableEditorValue.value[formId]} onUpdate:value={(e) => {
            updateCellValue({formId, e})
          }}/>
        </div>
      </a-form-item>
    } else if ('rowSort' === editorType) {
      return (
          <span class="row-sort-container">
            <caret-up-outlined onclick={()=>{
              changeRowIndex(record,-1)
            }}/>
            <caret-down-outlined onclick={()=>{
              changeRowIndex(record,1)
            }}/>
          </span>
      )
    } else if ('dialog' === editorType) {
      const open = (formId, text) => {
        let tempVal = null
        const updateValue = (e) => {
          //先临时存储，最后点击确定时才触发更新
          tempVal = e
          //updateCellValue({formId, e})
        }
        let _props = column.editor.props || {}
        Object.assign(_props, {
          width: 1000,
          noFooter: !!define.disabled,// 禁用时不显示底部按钮
          component: column.editor.dialogSlot({text, updateValue}),
          onCancel: () => {
          },
          customOK:true,
          onClickOk: () => {
            //触发更新
            updateCellValue({formId, e: tempVal})
            top.globalRef.modal.getRef(modalId).close()
          }
        })
        let modalId = top.globalRef.modal.init(_props)
        nextTick(() => {
          if (define.disabled) {
            top.globalRef.modal.getRef(modalId).open('查看', {}, true)
          } else {
            top.globalRef.modal.getRef(modalId).open('编辑', {})
          }
        })
      }
      return (
          <a class="table-row-btn" onclick={() => {
            open(formId, text)
          }}>
            {define.disabled ? column.editor.disabledText : column.editor.text}
          </a>
      )
    }
  }
}
/**
 * 改变行索引
 * @param record
 * @param diff
 */
const changeRowIndex = (record,diff) => {
  let arr = []
  if (define.unSaveRowsLocation === 'before') {
    arr = [unSaveRows.value, dataSource.value]
  } else if (define.unSaveRowsLocation === 'after') {
    arr = [dataSource.value, unSaveRows.value]
  } else {
    arr = [unSaveRows.value, dataSource.value]
  }
  let index0 = -1
  let index1 = -1
  arr[0].forEach((item,index)=>{
    let _id = item.__temp_id__ || item.id
    let id = record.__temp_id__ || record.id
    if (_id === id) {
      index0 = index
    }
  })
  arr[1].forEach((item,index)=>{
    let _id = item.__temp_id__ || item.id
    let id = record.__temp_id__ || record.id
    if (_id === id) {
      index0 = index
    }
  })
  if(diff === 1){
    // 向下移动
    if (index0 !== -1 && index0 < arr[0].length - 1) {
      let temp = arr[0][index0]
      arr[0][index0] = arr[0][index0 + 1]
      arr[0][index0 + 1] = temp
    }else if (index1 !== -1 && index1 < arr[1].length - 1) {
      let temp = arr[1][index1]
      arr[1][index1] = arr[1][index1 + 1]
      arr[1][index1 + 1] = temp
    }else if (index0 === arr[0].length - 1) {
      arr[1].unshift(arr[0][index0])
      arr[0].splice(index0,1)
    }

  }else if (diff === -1){
    // 向上移动
    if (index0 !== -1 && index0 > 0) {
      let temp = arr[0][index0]
      arr[0][index0] = arr[0][index0 - 1]
      arr[0][index0 - 1] = temp
    }else if (index1 !== -1 && index1 > 0) {
      let temp = arr[1][index1]
      arr[1][index1] = arr[1][index1 - 1]
      arr[1][index1 - 1] = temp
    }else if (index1 === 0) {
      arr[0].push(arr[1][index1])
      arr[1].splice(index1,1)
    }
  }
  if (define.unSaveRowsLocation === 'before') {
    unSaveRows.value = arr[0]
    dataSource.value = arr[1]
  } else if (define.unSaveRowsLocation === 'after') {
    dataSource.value = arr[0]
    unSaveRows.value = arr[1]
  } else {
    unSaveRows.value = arr[0]
    dataSource.value = arr[1]
  }
  emits('update:value', tableDataSource.value)
}
const dealColumn = (columns) => {
  columns.forEach((column) => {
    let currentDepth = 1;
    if (column.children) {
      currentDepth++;
      dealColumn(column.children)
    } else {
      if (!column.customCell){
        column.customCell = (record, rowIndex, column) => {
          if (record[column.dataIndex + '_rowspan'] || record[column.dataIndex + '_rowspan'] === 0) {
            return {
              rowSpan: record[column.dataIndex + '_rowspan'],
            }
          }
          return {
            rowSpan: 1,
          }
        }
      }
      column.showSorterTooltip = false
      let sortWidth = 0
      if (column.sorter) {
        if (typeof (column.sorter) === 'string' && column.sorter === 'true') {
          column.sortDirections = ['descend', 'ascend']
          column.sorter = true
          sortWidth = 30
        } else if (typeof (column.sorter) !== 'function') {
          column.sorter = false
        }
      } else if (define.showSorter){
        column.sortDirections = ['descend', 'ascend']
        column.sorter = true
        sortWidth = 30
      }
      if (column.resizable === undefined) {
        column.resizable = true
      }
      if (!column.width) {
        let number = (column.title?column.title.length:1) * 20 + sortWidth;
        column.width = Math.max(number, column.minWidth || 0)
        if (column.resizable === true) {
          tempTableWidth.value += column.width
        }else{
          tempTableResizeWidth.value += column.width
        }
      }else{
        if (column.resizable === true) {
          tempTableWidth.value += column.width
          tempTableWidth.value += sortWidth
        }else{
          tempTableResizeWidth.value += column.width
          tempTableResizeWidth.value += sortWidth
        }
      }
      if (column.editor){
        dealColumnEditor(column)
      }
    }
    if (currentDepth > columnsDeepLength.value) {
      columnsDeepLength.value = currentDepth;
    }
  })
}
const autoCalcWidth = (columns, total, reduce, tempTableWidth) => {
  columns.forEach((column) => {
    if (column.children) {
      autoCalcWidth(column.children, total, reduce, tempTableWidth)
    } else {
      if (column.width && !column.fixed && column.resizable === true) {
        let ceil = Math.ceil(total * column.width / tempTableWidth);
        let diff = reduce - ceil
        if (diff < 0) {
          ceil = reduce
        }
        reduce -= ceil
        column.width += ceil
      }
    }
  })
}
let tableColumns = ref([])
const initTableLayout = () => {
  tempTableWidth.value = 0
  tempTableResizeWidth.value = 0
  let forEachTree = (columns) => {
    let arr = [];
    columns.forEach(item => {
      let copyJson = Object.assign({}, item);
      if (item.children && item.children.length > 0) {
        copyJson.children = forEachTree(item.children);
      }
      arr.push(copyJson);
    })
    return arr;
  }
  let newArr = forEachTree(define.columns);
  defineColumns.value = newArr;
  dealColumn(defineColumns.value)
  let extendWidth = 2 + 2 + 12// 边框和右侧滚动条的宽度
  extendWidth += 16//还不清楚为什么要加  先加上 以后再看 zry 2023-06-21 13:55:04
  extendWidth += 32//新增的padding
  if (define.autoHeight) {
    extendWidth += 16
  }
  if (define.showIndex && !define.rowExpandable()) {
    extendWidth += indexColumn.width
  }
  if (!define.disabled && define.rowSelection) {
    extendWidth += 50
  }
  if (_rowButtons.value.length > 0 && define.showRowButtons) {
    extendWidth += optColumn.value.width
  }

  let _width = define.width - 24 - 20
  if (instance.vnode.el.clientWidth) {
    _width = instance.vnode.el.clientWidth
  }
  if (_width > tempTableWidth.value + extendWidth) {
    let total = _width - tempTableWidth.value - extendWidth - tempTableResizeWidth.value;
    let reduce = total
    autoCalcWidth(defineColumns.value, total, reduce, tempTableWidth.value)
  }
  tempTableWidth.value += extendWidth
  tableWidth.value = tempTableWidth.value
  let _columns = (define.showIndex&&!define.rowExpandable() ? [indexColumn] : []).concat(defineColumns.value)

  //endregion
  if (_rowButtons.value.length > 0 && define.showRowButtons) {
    _columns = _columns.concat([optColumn.value])
  }

  _columns.push({
    "title": "",
    "dataIndex": UUID()
  })

  let totalWidth = 0
  _columns.map(item => totalWidth+=item.width)
  totalColumnWidth.value = totalWidth
  tableColumns.value = _columns
}
const rowType = ref(null)
watch(() => define.columns, () => {
  initTableLayout();
}, {deep: true})

let dataSource = ref([])
let tableDataSource = computed(() => {
  if (define.unSaveRowsLocation === 'before') {
    return unSaveRows.value.concat(dataSource.value)
  } else if (define.unSaveRowsLocation === 'after') {
    return dataSource.value.concat(unSaveRows.value)
  } else {
    return unSaveRows.value.concat(dataSource.value)
  }
})
const isRowHasChildren = (row) => {
  if (!row) {
    return false
  }
  return row.hasChildren === true || row.hasChildren === 1 || row.hasChildren === '1'
}
const tableScroll = computed(() => {
  const scroll = {x: tableWidth.value}
  if (define.scrollHeight !== null) {
    scroll.y = define.scrollHeight
  } else if (!define.autoHeight) {
    scroll.y = tableHeight.value
  }
  return scroll
})

// watchEffect(() => {
//   if (dataSource.value.length > 0 && dataSource.value[0]?.rowType === '首行统计') {
//     totalRow.value = dataSource.value[0]
//     // 删除首行统计行
//     dataSource.value.shift()
//   }
// })

/*表格选中 start*/
let selectedRowKeys = ref(define.defaultSelectedRowKeys);
let selectedRows = ref([]);
let allPageSelectedRows = ref([]);//跨页选择存储的内容 #1458  zry 2022-11-26 13:31
watch(() => define.defaultSelectedRowKeys, () => {
  selectedRowKeys.value = define.defaultSelectedRowKeys;
  if ('radio' === define.rowSelectionType) {
    allPageSelectedRows.value = []
  }
  define.defaultSelectedRowKeys.forEach(id => {
    allPageSelectedRows.value.push({
      id: id
    })
  })
}, {immediate: true})
const onSelectChange = (_selectedRowKeys, _selectedRows) => {
  //分页切换后选择项丢失的问题 zry 2023-02-18 10:45:20
  let filter = allPageSelectedRows.value.filter(item=>_selectedRowKeys.indexOf(item.id)===-1);
  selectedRowKeys.value = filter.map(item=>item.id).concat(_selectedRowKeys)
  selectedRows.value = filter.concat(_selectedRows)
  emits('selectChange', selectedRowKeys.value, selectedRows.value)
}
let deleteAllPageSelectedRows=(id)=>{
  let spliceIndex = -1;
  allPageSelectedRows.value.forEach((item, index) => {
    if (item.id === id) {
      spliceIndex = index
    }
  })
  if (spliceIndex > -1) {
    allPageSelectedRows.value.splice(spliceIndex, 1)
  }
}
const onSelectAll = (selected, selectedRows, changeRows) => {
  //allPageSelectedRows初次进来为空
  //console.log(allPageSelectedRows.value)
  if(selected){
    selectedRows.forEach((row, indx) => {
      if (row&&row.id){
        if (allPageSelectedRows.value.filter(item=>item.id===row.id).length===0){
          allPageSelectedRows.value.push(row)
        }
      }
    })
  }else{
    //allPageSelectedRows.value = [];
    changeRows.forEach((row, indx) => {
      deleteAllPageSelectedRows(row.id)
    })
  }
  emits('onSelectAll',selected, selectedRows, changeRows)
}

//用户手动选择/取消选择某列的回调 #1458  zry 2022-11-26 13:31
const onSelect = (record, selected, selectedRows, nativeEvent) => {
  if (selected) {
    if ('radio' === define.rowSelectionType) {
      allPageSelectedRows.value = []
    }
    //选择后判断所有分页里面有没有，没有的话再加入避免重复 zry 2023-02-18 10:41:35
    if (allPageSelectedRows.value.filter(item=>item.id===record.id).length===0){
      allPageSelectedRows.value.push(record)
    }
  } else {
    let spliceIndex = -1;
    allPageSelectedRows.value.forEach((item, index) => {
      if (item.id === record.id) {
        spliceIndex = index
      }
    })
    if (spliceIndex > -1) {
      allPageSelectedRows.value.splice(spliceIndex, 1)
    }
  }
  emits('onSelect',record, selected, selectedRows, nativeEvent)
}

//批量删除之后，需要更新当前存储的key
const updateAllPageSelectedRows = ({deleteRowKeys}) => {
  deleteRowKeys&&deleteRowKeys.forEach(id=>{
    deleteAllPageSelectedRows(id)
  })
  selectedRowKeys.value = allPageSelectedRows.value.map(item=>item.id)
}

const getSelectedRowKeys = () => {
  return selectedRowKeys.value
}
const getSelectedRows = () => {
  return allPageSelectedRows.value
  /*if (selectedRowKeys.value.length > 0 && selectedRows.value.length === 0) {
    return dataSource.value.filter(item => selectedRowKeys.value.indexOf(item.id) >= 0)
  }
  return selectedRows.value*/
}
const clearSelectedRows = () => {
  allPageSelectedRows.value = []
  selectedRowKeys.value = []
}

/*表格选中 end*/
let loading = ref(false)
let localPagination = ref(define.pagination)

watch(() => define.pagination, (newValue, oldValue) => {
  if (JSON.stringify(newValue) !== JSON.stringify(oldValue)) {
    localPagination.value = newValue;
  }
}, { immediate: true });

let emits = defineEmits(['clickRow', 'loadSuccess', 'onSelectAll', 'onSelect', 'selectChange','clickTableRow','cellValueChange','update:value'])
const clickRow = (item, row) => {
  emits('clickRow', {value: item.value, row: row})
}

let currentSelectIndex = ref(-1);
const customRow = (record,index) => {
  let defaultEvent = {
    onclick: () => {
      if(currentSelectIndex.value !== index){
        currentSelectIndex.value = index
      }
      emits('clickTableRow', {record,index})

    }
  }
  if(define.customTableRowEvent){
    Object.assign(defaultEvent,define.customTableRowEvent(record));
  }
  return defaultEvent
}

const clearSelectRowStyle = () =>{
  currentSelectIndex.value = -1
}

let queryParam = ref({})
let queryOrderByParam = ref('')

const tableChange = (pagination, filters, sorter, {currentDataSource}) => {
  //不分页的情况下，不设置分页参数
  if (pagination && localPagination.value){
    localPagination.value.current = pagination.current
    localPagination.value.pageSize = pagination.pageSize
  }
  let orderBy = '';
  if (!define.dataUrl) {
    //没有后端请求的使用前端排序
    tableColumns.value.forEach(column => {
      if (column.dataIndex === sorter.field) {
        column.sortOrder = sorter.order
      }else{
        delete column.sortOrder
      }
    })
    return
  }
  if (sorter.field && sorter.order) {
    // 已修复 这里拼的a. 这里拼会导致一个问题，如果是sql 计算的 例如：ifnull(TIMESTAMPDIFF(YEAR, a.birth_date, CURDATE()),'-') as "expert_age" ,就会排序报错， 报错原因是 a.expert_age 没有这个字段报错
    if ('false'===sorter.column.isDataBaseColumn){
      if (sorter.order === 'descend') {
        orderBy = `${sorter.field} desc`
      } else if (sorter.order === 'ascend') {
        orderBy = `${sorter.field} asc`
      }
    } else if (sorter.column.sortColumn){
      if (sorter.order === 'descend') {
        orderBy = `${sorter.column.sortColumn} desc`
      } else if (sorter.order === 'ascend') {
        orderBy = `${sorter.column.sortColumn} asc`
      }
    } else {
      if (sorter.order === 'descend') {
        orderBy = `a.${sorter.field} desc`
      } else if (sorter.order === 'ascend') {
        orderBy = `a.${sorter.field} asc`
      }
    }
  }
  loadData(queryParam.value, localPagination.value.current, orderBy)
}

const handleResizeColumn = (w, col) => {
  col.width = w;
}
let currentTableExpand = ref([])
const expand = (expanded) => {
  if (define.ignoreExpandEvent){
    currentTableExpand.value = expanded
    return
  }
  currentTableExpand.value = expanded
  loadChildren(dataSource.value, expanded[expanded.length - 1])
}

const loadChildren = (data, key, callback) => {
  let flag = false
  data.forEach(item => {
    if (item.id === key) {
      loading.value = true
      flag = true
      let url = define.dataUrl.slice(0, define.dataUrl.indexOf('&traceFlag='))
      let data = mergeQueryData(queryParam.value, define.initParam);
      if (localPagination.value) {
        data.pageParam = {
          orderBy: queryOrderByParam.value,
          pageNo: localPagination.value.current,
          pageSize: localPagination.value.pageSize
        }
      }else{
        data.pageParam = {
          orderBy: queryOrderByParam.value,
          pageNo: localPagination.value.current,
          pageSize: 100000
        }
      }
      // 如果存在 自定义参数函数，那么执行
      data = define.customParam?define.customParam(data,item):data;
      let action = null
      if (define.loadChildrenAction == null) {
        action = () => postAction(url + '&traceFlag=1&parentId=' + key, data)
      } else {
        action = () => define.loadChildrenAction(item)
      }
      action().then(res => {
        if (define.rowExpandable()) {
          res.rows.forEach(row => {
            if (isRowHasChildren(row)) {
              row.children = []
            }
          })
          item.children = res.rows
        }
        callback && callback()
      }).finally(() => {
        if (!callback) {
          loading.value = false
        }
      })
    }
  })
  if (!flag) {
    let childFlag = false;
    data.forEach(item => {
      if (item.children) {
        childFlag = true;
        loadChildren(item.children, key, callback)
      }
    })
    if (!childFlag) {
      loading.value = false;
    }
  }

}
let setTableRef = define.formNo && inject('setTableRef'); // inject的参数为provide过来的名称

/**
 * 获取加载数据的方法
 * @param data
 * @return {Promise<unknown>}
 */
const loadDataAction = (data) => {
  return new Promise(resolve => {
    if(define.dataUrl){
      postAction(define.dataUrl, data).then(res => {
        resolve(res)
      })
    }
  })
}

const getScrollContainer = () => {
  const tableEl = document.getElementById(id.value)
  if (!tableEl) {
    return null
  }
  return tableEl.querySelector('.ant-table-body') || tableEl.querySelector('.ant-table-content')
}

/**
 * 加载数据
 * @param param 查询参数
 * @param page 页码
 * @param isUseParam true：使用 方法 形参 param
 */
const loadData = (param = {}, page, orderBy = '', isShowLoading = true, isUseParam = true) => {

  let scrollTop = getScrollContainer()?.scrollTop || 0
  //直接通过第三个参数orderby传不进来 出此下策
  /*
    if (!orderBy) {
      orderBy = param.orderBy
    }
  */
  if (!define.dataUrl) {
    return
  }
  if (page&&localPagination.value) {
    localPagination.value.current = page
  }
  if (isUseParam) {
    queryParam.value = param;
  } else {
    param = queryParam.value
  }

  if (define.dataUrl) {
    let data = mergeQueryData(param, define.initParam);

    orderBy = orderBy || param.orderBy || data.orderBy || queryOrderByParam.value  ; // 一进去就加载自定义排序功能
    delete data.orderBy
    queryOrderByParam.value = orderBy;

    //把initParam.pageParam中，除了pageNo，pageSize，orderBy的字段赋给新的pageParam
    let initParamPageParam ={}
    Object.assign(initParamPageParam,define.initParam.pageParam)
    delete initParamPageParam.pageNo
    delete initParamPageParam.pageSize
    delete initParamPageParam.orderBy
    if (localPagination.value) {
      data.pageParam = {
        orderBy: orderBy,
        pageNo: localPagination.value.current,
        pageSize: localPagination.value.pageSize,
        ...initParamPageParam
      }
    }else{
      data.pageParam = {
        orderBy: orderBy,
        pageNo: localPagination.value.current,
        pageSize: 100000,
        ...initParamPageParam
      }
    }
    if (isShowLoading){
      loading.value = true
    }
    let action = loadDataAction(data)
    action.then(res => {
      if (define.rowExpandable()) {
        res.rows.forEach(item => {
          if (isRowHasChildren(item)) {
            item.children = []
          }
        })
      }
      res.rows.forEach(item => {
        if (!item.id) {
          item.id = getData(item, define.idField)
        }
      })
      dataSource.value = res.rows
      !define.ignoreSetTableRef && setTableRef && setTableRef(define.formNo, {
        rows: res.rows,
        total: res.total,
        initParam: define.initParam,
      }, {
        getUnSaveRows: (validate = false) => {
          return getUnSaveRows(validate)
        },
        loadData:loadData,
        getDataSource: getDataSource
      })
      if (localPagination.value) {
        localPagination.value.total = res.total
        if (!define.pagination.hideOnSinglePage) {
          localPagination.value.hideOnSinglePage = false
        } else {
          localPagination.value.hideOnSinglePage = res.total <= localPagination.value.defaultPageSize
        }
      }
      if (res.data?.has_statistics || define.statisticsDataUrl) {
        //如果有统计数据，或者有统计数据url，那么加载统计数据
        if (isShowLoading){
          totalRow.value = {}
          groupMap.value = {}
        }
        loadStatisticsData(data)
      }
      emits('loadSuccess', {rows: res.rows, total: res.total,res:res})
      if (define.mergeColumnConfig.length>0){
        define.mergeColumnConfig.forEach(item=>{
          autoMergeColumn(item.colId, item.mergeColumns)
        })
      }
      // 树表首次加载时，自动展开第一层节点
      if (currentTableExpand.value.length === 0 && define.rowExpandable && define.rowExpandable()) {
        const firstLevelIds = res.rows
          .filter(item => (!item.parentId || item.parentId === '0') && isRowHasChildren(item))
          .map(item => item.id)
        if (firstLevelIds.length > 0) {
          currentTableExpand.value = firstLevelIds
        }
      }
      if (currentTableExpand.value.length > 0 && !define.ignoreExpandEvent) {
        const load = (index) => {
          if (isShowLoading){
            loading.value = true
          }
          if (index === currentTableExpand.value.length) {
            nextTick(() => {
              const scrollContainer = getScrollContainer()
              if (scrollContainer) {
                scrollContainer.scrollTop = scrollTop
              }
              loading.value = false
            })

            return
          }
          loadChildren(dataSource.value, currentTableExpand.value[index], () => {
            index++;
            load(index)
          })
        }
        load(0)
      }
    }).finally(() => {
      // 如果忽略展开事件，那么自动全部收缩，并且 取消loading
      if (currentTableExpand.value.length === 0 || define.ignoreExpandEvent) {
        loading.value = false
        currentTableExpand.value = [];
      }
    })
  }
}

const treeToList = (tree) => {
  let arr = []
  tree.forEach(row => {
    if (!row.children || row.children.length === 0) {
      arr.push(row)
    } else {
      arr = arr.concat(treeToList(row.children))
    }
  })
  return arr
}

//合计行
let totalRow = ref(null)
//分组统计
let groupMap = ref({})
//统计数据发起时间
let statisticsPostTime
/**
 * 加载统计数据
 * @param data
 */
const loadStatisticsData = (data) => {
  //记录当前请求的时间
  statisticsPostTime = new Date().getTime()
  let currentPostTime = statisticsPostTime
  if (define.dataUrl && define.dataUrl.indexOf('dynamic/zform/datamap') >= 0) {
    let url = define.dataUrl.replace('datamap', `statisticsData/${new Date().getTime()}`)
    if (define.statisticsDataUrl){
      url = define.statisticsDataUrl
    }
    postAction(url, data).then(res => {
      if (currentPostTime !== statisticsPostTime) {
        console.warn('loadStatisticsData请求已过期')
        return
      }
      if (res.data.statisticsVo?.sumMap){
        totalRow.value = res.data.statisticsVo?.sumMap
      }
      if (res.data.statisticsVo?.groupMap){
        groupMap.value = res.data.statisticsVo?.groupMap
      }
    })
  }
}

const calcHeight = () => {
  let height = instance.vnode.el.clientHeight
  if (define.height) {
    height = define.height
  }
  //高度 - 上外距 - 额外减去的高度
  tableViewHeight.value = height - 10 - define.extraDiffHeight
  //高度 - 上外距 - 6 暂时不清楚6是什么原因 不减的话表格会变高6个px  - 额外减去的高度
  let _tableHeight = height - 10 - 6 - define.extraDiffHeight
  let diffPage = 0
  if (localPagination.value && !localPagination.value.hideOnSinglePage) {
    diffPage = 32
  }
  //高度 - 分页 - 表头行高 * 表头行数
  tableHeight.value = _tableHeight - diffPage - 36 * columnsDeepLength.value
}

// 处理表格为空时显示字符
const transformCellText = ({text, column}) =>{
  const emptyDefaultValue = define.cellEmptyDefaultValue
  if(!text || text.length < 1){
    return emptyDefaultValue
  }else{
    let textInfo = text[0];

    if(column && textInfo) {
      if(typeof textInfo.type === 'symbol'){
        if (!textInfo.children || (Array.isArray(textInfo.children) && textInfo.children.length === 0)) {
          return emptyDefaultValue;
        }
      }else if(textInfo.dynamicProps && textInfo.dynamicProps.length >0 && textInfo.dynamicProps[0] === 'title' && textInfo.props && !textInfo.props.title){
        if (!textInfo.props.title ) {
          return emptyDefaultValue
        }
      }

    }
  }
  return text;
}

watch(() => define.height, (v) => {
  calcHeight()
})
watch(() => define.width, (v) => {
  initTableLayout()
})
onMounted(() => {
  initTableLayout()
  calcHeight()
  //  当窗口或者大小发生改变时执行resize，重新调整表格布局
  window.addEventListener("resize", function () {
    nextTick(() => {
      initTableLayout()
      calcHeight()
    })
  });
  if (!define.disableInitLoad) {
    loadData({}, 1)
  }
  !define.ignoreSetTableRef && setTableRef && setTableRef(define.formNo, {
    initParam: define.initParam,
  }, {
    getUnSaveRows: (validate = false) => {
      return getUnSaveRows(validate)
    },
    loadData:loadData,
    getDataSource: getDataSource
  })

  watch(() => define.disabled, () => {
    //filterButton()
    nextTick(() => {
      initTableLayout()
    })
  }, {immediate: true})
})

const formatIndex = (index) => {
  if (tableDataSource.value[index]?.rowType === '首行统计') {
    rowType.value = '首行统计'
    return '';
  }
  if (localPagination.value) {
    return (localPagination.value.current - 1) * localPagination.value.pageSize + index + 1
  }
  if (rowType.value === '首行统计') {
    return index
  } else {
    return index + 1
  }
}
let unSaveRows = ref([])
watch(() => define.data, () => {
  if (define.data) {
    let removeUnSaveRowIdArr = []
    define.data.forEach((item, index) => {
      if (!item.__temp_id__){
        item.__temp_id__ = UUID()
      }else{
        removeUnSaveRowIdArr.push(item.__temp_id__)
      }
      if (!item.id) {
        item.id = getData(item, define.idField)
      }
    })
    unSaveRows.value.forEach((item, index) => {
      if (oneOf(item.__temp_id__, removeUnSaveRowIdArr)){
        unSaveRows.value.splice(index, 1)
      }
    })
    dataSource.value = define.data
    nextTick(()=>{
      define.mergeColumnConfig.forEach(item=>{
        autoMergeColumn(item.colId, item.mergeColumns)
      })
    })
  }
}, {immediate: true, deep: true})

const addUnSaveRow = (row) => {
  if (row.__temp_id__) {
    row.id = row.__temp_id__
  }
  unSaveRows.value.push(row)
  emits('update:value', tableDataSource.value)
}
/**
 * 删除未保存的行
 * @param row
 */
const removeUnSaveRow = (row) => {
  let data = tableDataSource.value
  let deleteIndex = -1
  unSaveRows.value.forEach((_row, index) => {
    if (JSON.stringify(_row) === JSON.stringify(row)) {
      deleteIndex = index
    }
  })
  if (deleteIndex > -1) {
    unSaveRows.value.splice(deleteIndex, 1)
  }
  data.forEach((_row, index) => {
    if (_row.__temp_id__ === row.__temp_id__) {
      data.splice(index, 1)
    }
  })
  emits('update:value', data)
}
/**
 * 删除行
 * @param row
 */
const removeRow = (row) => {
  let data = tableDataSource.value
  let deleteIndex = -1
  dataSource.value.forEach((_row, index) => {
    if (JSON.stringify(_row) === JSON.stringify(row)) {
      deleteIndex = index
    }
  })
  if (deleteIndex > -1) {
    dataSource.value.splice(deleteIndex, 1)
  }
  data.forEach((_row, index) => {
    if (_row.__temp_id__ === row.__temp_id__) {
      data.splice(index, 1)
    }
  })
  emits('update:value', data)
}
const updateUnSaveRow = (row) => {
  unSaveRows.value.forEach((_row, index) => {
    if (_row.__temp_id__ === row.__temp_id__) {
      unSaveRows.value[index] = row;
    }
  });
}
const getDataSource = (validate = true) => {
  return new Promise((resolve, reject) => {
    if (!validate){
      resolve(dataSource.value)
    }else {
      instance.refs.uForm.validateFields().then(values => {
        resolve(dataSource.value)
      }).catch(err => {
        if (err.errorFields){
          instance.refs.uForm.scrollToField(err.errorFields[0].name, {behavior: (actions)=>{
              actions.forEach(({ el, top, left }) => {
                // implement the scroll anyway you want
                el.scrollTop = top - 20
                el.scrollLeft = left

              })
            }})
        }
        reject(err)
      })
    }
  })
}
/**
 * 获取所有页数据 从后台查询
 * 不使用查询条件
 * @return {Promise<unknown>}
 */
const getAllPageRows = () => {
  return new Promise((resolve, reject) => {
    let data = {
      pageParam: {
        pageNo: 1,
        pageSize: 10000
      }
    }
    if (define.initParam) {
      data = Object.assign(data, define.initParam)
    }
    loadDataAction(data).then(res=>{
      resolve(res.rows)
    }).catch(err=>{
      reject(err)
    })
  })
}
/**
 * 获取未保存的数据
 * @param validate
 * @return {Promise<unknown>|{}[]}
 */
const getUnSaveRows= (validate = false) => {
  if (validate){
    return new Promise((resolve, reject) => {
      instance.refs.uForm.validateFields().then(values => {
        let data = tableDataSource.value
        resolve(data.filter(item => item.__temp_id__).map(item => {
          let obj = {}
          Object.assign(obj, item)
          delete obj.id
          return obj
        }))
      }).catch(err => {
        reject(err)
      })
    })
  }
  return unSaveRows.value.map(item => {
    let obj = {}
    Object.assign(obj, item)
    delete obj.id
    return obj
  })
}

const autoMergeColumn = (colId = 'id', mergeColumns = []) => {
  let mergeColId = colId
  let mergeColName = mergeColumns
  let colIdData = []
  let colIdDataCount = {}
  tableDataSource.value.forEach((item, index) => {
    if (colIdData.join().indexOf(item[mergeColId]) >= 0) {
      colIdDataCount[item[mergeColId]]++
    } else {
      colIdData.push(item[mergeColId])
      colIdDataCount[item[mergeColId]] = 1
    }
  })
  let count = 0
  tableDataSource.value.forEach((item, index) => {
    if (colIdDataCount[item[mergeColId]] > 1) {
      if (count === 0) {
        mergeColName.forEach((col, j) => {
          item[col + '_rowspan'] = colIdDataCount[item[mergeColId]]
        })
        count = 1
      } else {
        mergeColName.forEach((col, j) => {
          item[col + '_rowspan'] = 0
        })
      }
    }
    if (index < tableDataSource.value.length - 1 && tableDataSource.value[index + 1][mergeColId] !== tableDataSource.value[index][mergeColId]) {
      count = 0
    }
  })
}
const startLoading = () => {
  loading.value = true
}
const endLoading = () => {
  loading.value = false
}
defineExpose({
  loadData, getSelectedRowKeys, getSelectedRows, clearSelectedRows, getDataSource, getAllPageRows, updateAllPageSelectedRows,
  addUnSaveRow, updateUnSaveRow, removeUnSaveRow, removeRow,clearSelectRowStyle,
  initTableLayout, calcHeight, getUnSaveRows ,autoMergeColumn,
  startLoading, endLoading
})

</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.title-align-center{
  :deep(.ant-table-thead){
    tr {
      th {
        text-align: center !important;
      }
    }
  }
}
.ant-table-striped {
  height: calc(100% - 10px);
  margin: 10px 0 0 0;
  /*border-radius: 14px;*/

  :deep(>.ant-spin-nested-loading) {
    border: 1px solid @border-color-base;
    box-shadow: 0 3px 3px @border-color-base;
    height: 100%;

    > .ant-spin-container {
      height: 100%;
      display: flex;
      flex-direction: column;

      .ant-table {
        border-bottom: 1px solid @border-color-base;
        flex: 1;
        height: unset!important;

        .ant-table-body {
          border-bottom: 1px solid @border-color-base;
        }
      }
    }
  }

  :deep(.ant-table-empty .ant-table-body) {
    overflow: hidden !important;
  }
  :deep(.ant-table-thead) {
    .ant-table-cell {
      /*text-align: center !important;*/
    }

    .ant-table-cell-scrollbar{
      position: sticky !important;
      z-index: 2;
      right:0;
    }

    .ant-table-cell:not(.ant-table-cell-scrollbar) {

      &:last-child {
        width:0 !important;
        padding: 0 !important;

        .ant-table-resize-handle {

          display: none;
        }
      }
    }

  }

  :deep(.ant-table-column-title) {
    text-align: inherit;
  }

  :deep(.table-striped) {
    td {
      background-color: #f5fbfd;
    }
  }

  :deep(.table-not-striped) {
    td {
      background-color: #ffffff;
    }
  }

  :deep(.ant-table-header) {
  }

  :deep(.ant-table-thead) {
    tr {
      th {

        /*font-family: @main-font-name;*/
        font-style: normal;
        font-weight: 700;
        font-size: @font-size-base;
        line-height: @font-size-base;
        height: 36px;

        /* identical to box height, or 100% */

        color: @text-color;
        background: #fafafa !important;
        border-bottom: 1px solid #eeeeee;

        /*border-width: 0 1px 1px 0;
        border-style: solid;
        border-color: #ddd;*/
      }
    }

  }

  :deep(.ant-table-tbody > tr > td) {
    height: 34px;
    font-family: @main-font-name;
    font-style: normal;
    font-weight: 400;
    font-size: @font-size-base;
    line-height: @font-size-base;
    /* identical to box height, or 100% */

    text-align: center;

    color: @text-color;

    /*border-width: 0 1px 1px 0;
    border-style: solid;
    border-color: #ddd;*/
  }

  :deep(.ant-table-tbody > tr:not(:has(td:nth-child(1):nth-last-child(1))) > td:last-child) {
    display: none !important;
  }

  :deep(.ant-table-thead > tr:not(:has(th:nth-child(1):nth-last-child(1))) > th:last-child) {
    display: none !important;
  }

  :deep(.ant-table-body table:first-of-type) {
    width: min-content !important;
  }

  :deep(.ant-table-tbody > tr > td.ant-table-cell-row-hover) {
    background: var(--ant-primary-1);
  }

  :deep(.ant-table-thead > tr > th:not(:last-child):not(.ant-table-selection-column):not(.ant-table-row-expand-icon-cell):not([colspan])::before) {
    /*background: var(--ant-primary-5);
    opacity: 0.8;
    height: 16px;
    width: 0.6px;*/
    content: unset;
  }

  :deep(.ant-table-summary) {
    .ant-table-cell:not(.ant-table-cell-scrollbar) {
      &:last-child {
        display: none !important;
      }
    }
  }

  .table-row-btn-container {
    display: flex;
    align-items: center;
    justify-content: center;

    .table-row-btn {
      font-family: @main-font-name;
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 14px;
      /* identical to box height, or 100% */

      text-align: center;

      color: var(--ant-primary-color);
      padding: 0 10px;
      cursor: pointer;
    }
  }

  :deep(.ant-table-pagination.ant-pagination){
    margin: 5px 16px;
  }

  .ant-table-cell{
    .ant-form-item{
      margin: -6px;
    }
    .row-sort-container{
      display: flex;
      flex-direction: column;
      align-items: center;

      .anticon{
        cursor: pointer;
      }
    }
  }
}
.table-auto-height{
  :deep(.ant-table-content){
    height: auto !important;
    max-height: none !important;
    overflow-x: auto !important;
    overflow-y: hidden !important;
    scrollbar-color: #cfd8e6 transparent;
    scrollbar-width: thin;

    &::-webkit-scrollbar {
      width: 0 !important;
      height: 8px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: #cfd8e6;
      border-radius: 999px;
    }
  }

  :deep(.ant-table-body) {
    height: auto !important;
    max-height: none !important;
    overflow-y: hidden !important;
    scrollbar-width: none;
    -ms-overflow-style: none;

    &::-webkit-scrollbar {
      width: 0 !important;
      height: 0 !important;
    }
  }

  :deep(.ant-table-empty .ant-table-content) {
    overflow-x: hidden !important;
    scrollbar-width: none;
    -ms-overflow-style: none;

    &::-webkit-scrollbar {
      width: 0 !important;
      height: 0 !important;
    }
  }

  :deep(.ant-table-cell-scrollbar) {
    display: none !important;
    width: 0 !important;
    min-width: 0 !important;
    padding: 0 !important;
  }
}

.table-group-statistics{
  padding: 0 20px;
  border-right: 1px solid #DDDDDD;
  color: var(--ant-primary-color);

  &:last-child{
    border-right: none;
  }
}
</style>
