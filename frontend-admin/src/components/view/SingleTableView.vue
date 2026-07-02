<template>
  <div class="single-table-view" :style="height?{height:height+'px'}:{}">
    <div v-if="runtimePageEditVisible" class="single-table-runtime-page">
      <div class="single-table-runtime-page-header">
        <div class="single-table-runtime-page-title">
          <a-button type="link" class="single-table-runtime-back" @click="closeRuntimeEdit">
            <left-outlined />
            返回列表
          </a-button>
          <div>
            <h2>{{ runtimeEditTitle }}</h2>
            <p></p>
          </div>
        </div>
        <div class="single-table-runtime-actions">
          <a-button @click="closeRuntimeEdit">{{ runtimeEditDisabled ? '关闭' : '取消' }}</a-button>
          <a-button v-if="!runtimeEditDisabled" type="primary" :loading="runtimeEditSaving" @click="saveRuntimeEdit">保存</a-button>
        </div>
      </div>
      <div class="single-table-runtime-page-body">
        <component v-if="currentModalComponent"
                   :key="runtimeEditFormKey"
                   :is="currentModalComponent"
                   :parentId="parentId"
                   :extendFormData="extendFormData"/>
      </div>
    </div>
    <div v-show="!runtimePageEditVisible" class="single-table-view-main">
    <slot name="pageHeader"></slot>
    <!--    查询区域-->
    <query-area :areaWidth="width" ref="query" @query="handleQuery" @reset="handleReset" @rowCountChange="rowCountChange" :totalCount="totalCount" v-bind="queryArea"
                v-if="queryAreaShow" :queryButton="queryButton" :loadData="loadData" :triggerQueryDataOnMounted="firstLoadDataWithDefaultQueryField">
      <template v-if="$slots.queryHeader" #header>
        <slot name="queryHeader"></slot>
      </template>
      <template #left>
        <template v-bind:key="item.value" v-for="item in queryAreaButtons">
          <span v-if="item.declare" v-show="!item.disabled" style="color: red;">{{item.declare}}</span>
          <a-badge :style="item.badgeCount?{'margin-right':'8px','white-space': 'nowrap'}:{'white-space': 'nowrap'}" :count="item.badgeCount">
            <a-button @click="downloadTemplate"
                      v-if="(item.visible || item.disabledShow) && config.importTemplateToQuery && item.value === 'import'"
                      :class="['opt-btn-'+item.value,'opt-btn-'+item.class]"
                      :style="`
                        background-color: ${item.disabled ? '#f5f5f5' : (item.bgcolor ? item.bgcolor : '')};
                        border: 0px;
                        color: ${item.disabled ? '#00000040' : (item.color ? item.color : '')};
                        border-color: ${item.disabled?'#d9d9d9': ''};
                        border-top-right-radius: ${item.exportQueueGroup?0:'unset'};
                        border-bottom-right-radius: ${item.exportQueueGroup?0:'unset'};
                      `"
                      :type="item.type?item.type:'primary'" :danger="item.danger" :disabled="item.disabled">
              <u-icon v-if="item.icon" :type="item.icon"/>
              下载模板
            </a-button>
            <a-button @click="clickButton(item.value,item)"
                      v-if="item.visible||item.disabledShow"
                      :class="['opt-btn-'+item.value,'opt-btn-'+item.class]"
                      :style="`
                        background-color: ${item.disabled ? '#f5f5f5' : (item.bgcolor ? item.bgcolor : '')};
                        border: 0px;
                        color: ${item.disabled ? '#00000040' : (item.color ? item.color : '')};
                        border-color: ${item.disabled?'#d9d9d9': ''};
                        border-top-right-radius: ${item.exportQueueGroup?0:'unset'};
                        border-bottom-right-radius: ${item.exportQueueGroup?0:'unset'};
                      `"
                      :type="item.type?item.type:'primary'" :danger="item.danger" :disabled="item.disabled">
              <u-icon v-if="item.icon" :type="item.icon"/>
              {{ item.text }}
            </a-button>
            <a-button @click="openExportQueueModal(item.exportQueueGroup)" style="margin-left: 0;padding:0 8px;" title="点击查看历史导出文件" v-if="item.exportQueueGroup&&(item.visible||item.disabledShow)"
                      :class="['opt-btn-'+item.value,'opt-btn-'+item.class]"
                      :style="`
                        background-color: ${item.disabled ? '#f5f5f5' : (item.bgcolor ? item.bgcolor : '')};
                        border: 0px;
                        color: ${item.disabled ? '#00000040' : (item.color ? item.color : '')};
                        border-color: ${item.disabled?'#d9d9d9': ''};
                        border-top-left-radius: 0;
                        border-bottom-left-radius: 0;
                        border-left: 1px solid #dedede
                      `"
                      :type="item.type?item.type:'primary'" :danger="item.danger" :disabled="item.disabled">
              <file-done-outlined />
            </a-button>
          </a-badge>
        </template>
        <slot name="queryAreaButtonsExt"/>
      </template>
      <template #queryFields>
        <!--        流程状态-->
        <a-radio-group v-if="currentActStatus" class="ant-form-item act-radio" v-model:value="currentActStatus">
          <a-radio-button class="act-status-unsent" value="Unsent" v-if="actStatusButton.showUnsent">
            <div class="status-bar">暂存
              <div v-if="dataCount.Unsent" class="status-count" v-html="formatCount(dataCount.Unsent)"/>
            </div>
          </a-radio-button>
          <a-radio-button class="act-status-todo-and-doing" value="TodoAndDoing" v-if="actStatusButton.showTodoAndDoing">
            <div class="status-bar">待办
              <div v-if="dataCount.TodoAndDoing" class="status-count" v-html="formatCount(dataCount.TodoAndDoing)"/>
            </div>
          </a-radio-button>
          <a-radio-button class="act-status-done" value="Done" v-if="actStatusButton.showDone">
            已办
          </a-radio-button>
          <a-radio-button class="act-status-path" value="suspend" v-if="actStatusButton.showSuspend">
            挂起
          </a-radio-button>
          <a-radio-button class="act-status-path" value="path" v-if="actStatusButton.showAll">
            全部
          </a-radio-button>
        </a-radio-group>
        <slot name="queryFields"/>
      </template>
      <template #queryAreaExt>
        <slot name="queryAreaExt"/>
      </template>
    </query-area>
    <!--    数据表格-->
    <single-table class="single-table-view-table" ref="table"
                  :style="{padding:$slots.title ? '0 16px 16px 16px !important' : ''}"
                  :data-url="subTable&&!parentId?null:dataUrl"
                  :disabled="disabled"
                  :queryAreaShow="queryAreaShow"
                  v-bind="singleTable"
                  :disableInitLoad="firstLoadDataWithDefaultQueryField?true:singleTable.disableInitLoad"
                  :height="height?(queryAreaShow?height - queryAreaHeight - 10 :height): null"
                  :width="width"
                  :autoHeight="autoHeight"
                  :formNo="formNo"
                  @clickRow="clickRow"
                  @clickTableRow="clickTableRow"
                  @loadSuccess="loadSuccess"
                  @onSelectAll="onSelectAll"
                  @onSelect="onSelect" @selectChange="selectChange" @cellValueChange="onCellValueChange" @update:value="updateValue">
      <template v-if="$slots.expandedRowRender" #expandedRowRender="{record}">
        <slot name="expandedRowRender" :record="record"></slot>
      </template>
      <template v-if="$slots.title" #title>
        <slot name="title"></slot>
      </template>
      <template #summary>
        <slot name="summary"></slot>
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
    </single-table>

    <u-export-file-queue v-if="currentExportQueueGroup||exportToFileQueue" :fileFrom="currentExportQueueGroup||props.formNo"
                         ref="exportQueueModal" @autoDownLoad="triggerExportByQueue"></u-export-file-queue>
    <!--    弹窗组件-->
    <u-modal ref="modal" :width="modalWidth" :full="modalFull" :component="currentModalComponent" :closeShowLeft="closeShowLeft"
             :extendFormData="extendFormData" :parentId="parentId" @clickOperate="clickOperate" v-bind="uModal"
             @saveSuccess="saveSuccess"
             @onCancel="onCancel"
             @afterClose="onCancel"></u-modal>

    <!--    工作流弹窗-->
    <u-modal ref="actModal" :width="modalWidth" :full="modalFull" :closeShowLeft="closeShowLeft"
             :extendFormData="extendFormData" :parentId="parentId" :showOk="false" :showCancel="false" v-bind="uModal"
             :extendButtons="actModalExtendButtons"
             @clickOperate="actClickOperate"
             @saveSuccess="saveSuccess"
             @onCancel="onCancel"
             @afterClose="onCancel">
      <component :is="currentModalComponent" :extendFormData="extendFormData">

        <template #actComment="{formItemShow,formItemDisabled,formItemRequire}">
          <a-col v-if="formItemShow" :span="24">
            <a-form-item name="comment" :label="actCommentLabel" :rules="[{ required: formItemRequire, message: '请输入'+actCommentLabel }]">
              <u-input :disabled="formItemDisabled" :maxlength="500" :textarea="true" v-model:value="actComment" :placeholder="'请输入'+actCommentLabel"/>
            </a-form-item>
          </a-col>
        </template>
        <template #actLog="{formItemShow}">
          <a-col v-if="formItemShow&&currentProcRow.act.procInsId" :span="24">
            <a-form-item label="流程日志">
              <a-button @click="showProcImg">流程图</a-button>
              <a-button @click="showProcTable">流程日志</a-button>
              <div style="padding: 10px 0;    overflow: auto;" v-if="procImgShow">
                <img :src="procImgData">
              </div>
              <div style="padding: 10px 0" v-if="procTableShow">
                <single-table :autoHeight="true" :showRowButtons="false" :pagination="false" :rowSelection="false"
                              :formNo="formNo"
                              :columns="[
                                  {title: '执行环节', dataIndex: ['actMap','histTaskName'], minWidth: 80, align: 'left',sorter:'false'},
                                  {title: '执行人', dataIndex: ['actMap','assignee'], minWidth: 80, align: 'left',sorter:'false'},
                                  {title: '开始时间', dataIndex: ['actMap','startTime'], minWidth: 120, align: 'center',sorter:'false'},
                                  {title: '结束时间', dataIndex: ['actMap','endTime'], minWidth: 120, align: 'center',sorter:'false'},
                                  {title: '任务历时', dataIndex:['actMap','durationTime'], minWidth: 80, align: 'center',sorter:'false'},
                                  {title: '审批意见', dataIndex: 'comment', minWidth: 150, align: 'left',sorter:'false'},
                              ]"
                              :data="procTableData"
                />
              </div>

            </a-form-item>
          </a-col>
        </template>
      </component>
    </u-modal>

    <u-act-select-user-modal @afterCloseOrOnCancel="afterCloseOrOnCancel" ref="actSelectUser"/>
    <u-act-create-node-modal ref="actCreateNode"/>

    <!--    导入弹窗-->
    <u-modal ref="import" :width="600" @saveSuccess="saveSuccess" :action="importAction" ok-text="导入">

      <u-form v-model:value="importModel" :labelWidth="120" :formNo="importDataFormNo||formNo"
              :url="{save: tableViewUrl['import'].replace('&parentId=','&parentId='+parentId) + '&areaId=' + config.areaId}">
        <a-row :gutter="16">
          <a-col :span="24"  v-if="!config.importTemplateToQuery">
            <a-form-item :colon='false' label=" ">
              <a-button @click="downloadTemplate" type="link">{{downloadTemplateText}}</a-button>
            </a-form-item>
          </a-col>
          <slot name="imprortParm" :modal="importModel"></slot>
          <a-col :span="24">
            <a-form-item label="导入数据" name="fileId"
                         :rules="[{ required: true, message: importSet['message'] }]">
              <u-upload :file-count="1" name="fileId" v-bind="importSet"
                        v-model:value="importModel.fileId"></u-upload>
            </a-form-item>
          </a-col>
        </a-row>
      </u-form>
    </u-modal>

    <!--   导入时错误弹窗 -->
    <a-modal
        v-model:visible="importErrorModalData.visible"
        title="导入错误"
        ok-text="知道了"
        @ok="importErrorModalData.hideModal"
        wrapClassName="import-error-modal"
        :width="800"
    >
      <component :is="importErrorModalData.content"></component>
      <template #footer>
        <a-button @click="exportImportErrorModalText">导出</a-button>
        <a-button type="primary" @click="importErrorClickOk">知道了</a-button>
      </template>
    </a-modal>

    <!--    批量选择数据组件-->
    <u-select-data-modal :clickOkBefore="batchSelectBeforeClickOkFun" ref="selectDataModal" v-bind="selectDataModalProps" :parentId="parentId" :beforeSave="selectDataModalBeforeSave" @saveSuccess="saveSuccess">
      <template #queryFields>
        <query-field v-bind:key="index"  v-bind="item" v-for="(item,index) in selectDataModalQueryFieldArr"/>
      </template>
    </u-select-data-modal>

    <!--   当SingleTableView需要在根节点接收props， 但是又有需要的元素放置在查询区域会导致查询功能失效时，就需要一个额外的区域放置额外的元素 -->
    <div v-show="false">
      <slot name="extendHiddenArea"/>
    </div>
    </div>
    <a-drawer
        v-model:visible="runtimeDrawerEditVisible"
        :width="runtimeDrawerWidth"
        :destroyOnClose="true"
        :body-style="{ padding: 0 }"
        :footer-style="{ padding: 0 }"
        class="single-table-runtime-drawer"
        @close="closeRuntimeEdit">
      <template #title>
        <div class="single-table-runtime-drawer-title">
          <strong>{{ runtimeEditTitle }}</strong>
          <span>右侧抽屉编辑，保存后自动刷新列表。</span>
        </div>
      </template>
      <div class="single-table-runtime-drawer-body">
        <component v-if="runtimeDrawerEditVisible && currentModalComponent"
                   :key="runtimeEditFormKey"
                   :is="currentModalComponent"
                   :parentId="parentId"
                   :extendFormData="extendFormData"/>
      </div>
      <template #footer>
        <div class="single-table-runtime-drawer-footer">
          <a-button @click="closeRuntimeEdit">{{ runtimeEditDisabled ? '关闭' : '取消' }}</a-button>
          <a-button v-if="!runtimeEditDisabled" type="primary" :loading="runtimeEditSaving" @click="saveRuntimeEdit">保存</a-button>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script>

export default {
  name: "SingleTableView",
}

</script>
<script setup>
import {getCurrentInstance, ref, computed, watch, onMounted, nextTick, inject, createVNode, provide} from "vue";
import {message, Modal} from 'ant-design-vue';
import {confirmAction, postActionByParams, getAction, postAction, postActionShowLoading} from "@/api/action";
import {confirm, getData, hasAnyPermission, prompt, setData, UUID} from "@/lib/tools";
import {downLoadFileAction, previewFileByDialog, saveDataUrl} from "@/api/api";
import UActSelectUserModal from "@/components/modal/UActSelectUserModal";
import {getActButtons, handleCreateNode, handleDeleteNode, handleDistribute, handleNotify, handleRollBack, handleSelectUser, handleSuperReject} from "@/lib/act/actList";
import UActCreateNodeModal from "@/components/modal/UActCreateNodeModal";
import config from "@/config";
import {defaultTableViewUrl} from "@/api/api";
import {buildExportData} from "@/lib/exportExcel";
import USelectDataModal from "@/components/modal/USelectDataModal";
import QueryField from "@/components/query/QueryField";
import {isFormItemShow} from "@/lib/act/actForm";
import {mergeQueryData} from "@/components/table/tableTool";
import {Base64} from "js-base64";
import {useStore} from "vuex";

let instance = getCurrentInstance();

let props = defineProps({

  height: {
    type: Number,
    default: null
  },
  width: {
    type: Number,
    default: null
  },
  autoHeight: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  queryButton: {
    type: Boolean,
    default: true
  },
  /**
   * 查询区域按钮
   */
  buttons: {
    type: Array,
    default() {
      return [{
        value: 'add',
        text: '添加'
      }, {
        value: 'batch-delete',
        text: '删除'
      }]
    }
  },
  /**
   * 导入模板是否携带数据
   */
  importTemplateWithData: {
    type: Boolean,
    default: false
  },
  /**
   * 导入模板的查询参数
   */
  importTemplateQueryData: {
    type: Object,
    default() {
      return null
    }
  },
  /**
   * 导入数据对应的FormNo
   */
  importDataFormNo: {
    type: String,
    default: ''
  },
  /**
   * 是否自定义新增按钮点击事件
   */
  customHandleAdd: {
    type: Boolean,
    default: false
  },
  queryAreaShow: {
    type: Boolean,
    default: true
  },
  customHandleQuery: {
    type: Boolean,
    default: false
  },
  /**
   * 查询区域的配置
   */
  queryArea: {
    type: Object,
    default() {
      return {}
    }
  },
  totalCount: {
    type: Number,
    default: 0
  },
  /**
   * 首次使用查询条件查询数据
   */
  firstLoadDataWithDefaultQueryField:{
    type: Boolean,
    default: false
  },
  /**
   * 默认去除查询字符串的前后空格
   */
  isTrimQuery:{
      type: Boolean,
      default: true
  },
  /**
   * 表格组件的配置
   */
  singleTable: {
    type: Object,
    default() {
      return {}
    }
  },
  extendFormData: {
    type: Object,
    default() {
      return {}
    }
  },
  modalComponent: {
    type: Object,
    default: null
  },
  extendButtons: {
    type: Array,
    default() {
      return []
    }
  },
  uModal: {
    type: Object,
    default: null
  },
  /**
   * 取消时触发刷新
   */
  cancelTriggerRefresh: {
    type: Boolean,
    default: false
  },
  formNo: {
    type: String,
    default: ''
  },
  moduleName: {
    type: String,
    default: 'zform'
  },
  parentId: {
    type: String,
    default: ''
  },
  parentFormData: {
    type: Object,
    default() {
      return {}
    }
  },
  /**
   * 相关的请求地址
   */
  url: {
    type: Object,
    default() {
      return defaultTableViewUrl
    }
  },
  /**
   * 是否使用表格的表头导出
   */
  exportAsTableColumns: {
    type: Boolean,
    default: false
  },
  modalWidth: {
    type: [String, Number],
    default: 1200
  },
  modalTitle: {
    type: [String, Function],
    default: ''
  },
  modalTitleFuncStr: {
    type: String,
    default: ''
  },
  modalFull: {
    type: Boolean,
    default: false
  },
  editOpenMode: {
    type: [String, Number],
    default: '1'
  },
  /**
   * 是否工作流
   */
  act: {
    type: Boolean,
    default: false
  },
  actProcDefFilter: {
    type: Function,
    default: null
  },
  actAddButtonText:{
    type: String,
    default: '添加'
  },
  showActButtons: {
    type: Boolean,
    default: true
  },
  actStatus: {
    type: String,
    default: ''
  },
  actStatusButton: {
    type: Object,
    default() {
      return {
        showUnsent: true,
        showTodoAndDoing: true,
        showDone: true,
        showAll: false,
        showSuspend: false, //挂起
      }
    }
  },
  actCommentLabel: {
    type: String,
    default: '审批意见'
  },
  handleSelectUserGetSaveDataType: {
    type: String,
    default: 'request'  //值为 request 或者 save（如果设置成 save 会携带 用户最新修改的表单的值 作为请求参数 调用后端获取工作流用户接口）
  },
  /**
   * 统一工作流待办列表页
   */
  commonActTaskList: {
    type: Boolean,
    default: false
  },
  onActSaveAndReject:{
    type: Function,
    default: ()=>{
      return undefined;
    }
  },
  /**
   * 工作流添加数据时右下角的操作按钮
   */
  actNewRowButtonArr: {
    type: Array,
    default: () => null
  },
  customSuperReject:{
    type: Function,
    default: undefined
  },
  /**
   * 是否子表
   */
  subTable: {
    type: Boolean,
    default: false
  },
  selectedRowKeyName: {
    type: String,
    default: 'a.id'
  },
  /*
  * 导出队列
  * */
  exportToFileQueue: {
    type: Boolean,
    default: false
  },
  /**
   * singleTable表格的数据
   */
  value : {
    type: Array,
    default: () => {
      return []
    }
  },
  /**
   * 是否开启行编辑
   */
  rowEdit: {
    type: Boolean,
    default: false
  },
  /**
   * 行编辑的默认行
   */
  rowEditDefaultRow: {
    type: Object,
    default: () => {
      return {}
    }
  },
  beforeQuery: {
    type: Function,
    default:null
  },
  /**
   * 是否传出保存成功事件
   */
  triggerSaveSuccess:{
    type: Boolean,
    default: false
  },
  /**
   * 关闭按钮是否显示在左边
   */
  closeShowLeft: {
    type: Boolean,
    default: true
  },
  importTemplateUrl: {
    type: String,
    default: null
  },
  importSet: {
    type: Object,
    default() {
      return {
        message: '请上传Excel',
        accepts: ['.xls', '.xlsx'],
        buttonText: '上传Excel',
      }
    }
  }
})
let idField = computed(() => {
  return props.singleTable.idField || 'id'
})
let tableViewUrl = ref({})
watch(() => props.url, (newVal) => {
  tableViewUrl.value = Object.assign({}, defaultTableViewUrl, newVal)
}, {immediate: true, deep: true})
/*流程状态*/
let currentActStatus = ref(props.actStatus)
watch(()=> props.actStatus,() => {
  currentActStatus.value = props.actStatus
})

let currentModalComponent = ref(props.modalComponent)

watch(() => props.modalComponent,() => {
  currentModalComponent.value = props.modalComponent
},{deep:true})

watch(() => currentActStatus.value, () => {
  emits('update:actStatus', currentActStatus.value)
  saveSuccess()

})

provide('getCurrentListActStatus',()=>{
  return currentActStatus.value
})

let dataUrl = computed(() => {
  /*if (props.disabled) {
    return props.url.list.replace('datamap', 'datamapView').replace('${formNo}', props.formNo).replace('${parentId}', props.parentId)
  }*/
  if (currentActStatus.value) {
    return tableViewUrl.value.list.replace('path=path', 'path=' + currentActStatus.value).replace('${formNo}', props.formNo).replace('${parentId}', props.parentId)
  }
  return tableViewUrl.value.list.replace('${formNo}', props.formNo).replace('${parentId}', props.parentId)
})
const normalizedEditOpenMode = computed(() => String(props.editOpenMode || '1'))
const isRuntimePageEditMode = computed(() => normalizedEditOpenMode.value === '2')
const isRuntimeDrawerEditMode = computed(() => normalizedEditOpenMode.value === '3')
const isRuntimeEditMode = computed(() => {
  return !props.act && !props.rowEdit && (isRuntimePageEditMode.value || isRuntimeDrawerEditMode.value)
})
const runtimeDrawerWidth = computed(() => {
  const width = Number(props.modalWidth)
  if (!Number.isNaN(width) && width > 0) {
    return Math.min(Math.max(width, 720), 1200)
  }
  return props.modalWidth || 900
})
let runtimePageEditVisible = ref(false)
let runtimeDrawerEditVisible = ref(false)
let runtimeEditTitle = ref('')
let runtimeEditRow = ref({})
let runtimeEditDisabled = ref(false)
let runtimeEditSaving = ref(false)
let runtimeEditFormKey = ref(0)
let runtimeFormRef = null
let runtimeFormRefCallback = null

provide('setFormRef', (_formRef, callback) => {
  runtimeFormRef = _formRef
  runtimeFormRefCallback = callback
})
provide('addExtendFormRef', () => {})

const setRuntimeFormData = (tryTimes = 0) => {
  nextTick(() => {
    if (runtimeFormRef && runtimeFormRef.setData) {
      runtimeFormRef.setData(runtimeEditRow.value || {}, runtimeEditDisabled.value, {}, () => {
        runtimeFormRefCallback && runtimeFormRefCallback({full: isRuntimePageEditMode.value})
      })
      return
    }
    if (tryTimes < 10) {
      window.setTimeout(() => setRuntimeFormData(tryTimes + 1), 30)
    }
  })
}

const openRuntimeEdit = (title, row = {}, disabled = false) => {
  if (!isRuntimeEditMode.value || !currentModalComponent.value) {
    instance.refs.modal.open(title, row, disabled)
    return
  }
  runtimeFormRef = null
  runtimeFormRefCallback = null
  runtimeEditTitle.value = title
  runtimeEditRow.value = row || {}
  runtimeEditDisabled.value = disabled
  runtimeEditSaving.value = false
  runtimeEditFormKey.value += 1
  if (isRuntimePageEditMode.value) {
    runtimePageEditVisible.value = true
    runtimeDrawerEditVisible.value = false
  } else {
    runtimeDrawerEditVisible.value = true
    runtimePageEditVisible.value = false
  }
  setRuntimeFormData()
}

const closeRuntimeEdit = (triggerCancel = true) => {
  runtimePageEditVisible.value = false
  runtimeDrawerEditVisible.value = false
  runtimeEditSaving.value = false
  runtimeEditRow.value = {}
  runtimeFormRef = null
  runtimeFormRefCallback = null
  if (triggerCancel) {
    onCancel()
  }
}

const notifyRuntimeFormSaveSuccess = (saveResult, submitData, options = {}) => {
  if (runtimeFormRef && typeof runtimeFormRef.onSaveSuccess === 'function') {
    Promise.resolve(runtimeFormRef.onSaveSuccess(saveResult, submitData, options)).catch(error => {
      console.error('UForm onSaveSuccess failed', error)
    })
  }
}

const saveRuntimeEdit = () => {
  if (runtimeEditDisabled.value) {
    closeRuntimeEdit()
    return
  }
  if (!runtimeFormRef || !runtimeFormRef.validateFields) {
    message.warning('表单尚未加载完成，请稍后再试')
    return
  }
  runtimeEditSaving.value = true
  runtimeFormRef.validateFields().then(data => {
    if (runtimeFormRef.subTable && !props.parentId) {
      return runtimeFormRef.getFormData().then(formData => {
        formData.formNo = runtimeFormRef.formNo
        if (formData.__temp_id__) {
          message.success('修改成功')
          saveSuccess({type: 'update', data: [formData]})
        } else {
          message.success('添加成功')
          saveSuccess({type: 'add', data: [formData]})
        }
        closeRuntimeEdit(false)
      })
    }
    if (runtimeFormRef.saveUrl && runtimeFormRef.formNo) {
      data.formNo = runtimeFormRef.formNo
      data.parentFormNo = runtimeFormRef.parentFormNo
      const action = props.uModal?.action || postAction
      return action(runtimeFormRef.saveUrl, data).then(saveResult => {
        notifyRuntimeFormSaveSuccess(saveResult, data)
        saveSuccess(saveResult)
        if (saveResult?.code === 0 && saveResult.msg) {
          message.success(saveResult.msg)
        } else if (saveResult?.code && saveResult.msg) {
          message.error(saveResult.msg)
        }
        closeRuntimeEdit(false)
      })
    }
    saveSuccess()
    closeRuntimeEdit(false)
  }).finally(() => {
    runtimeEditSaving.value = false
  })
}
let viewId = UUID()
onMounted(() => {

  watch(() => props.parentId, () => {
    if (props.parentId) {
      nextTick(() => {
        instance.refs.table.loadData({}, 1)
      })
    }

  }, {immediate: true})

  if (props.subTable && !props.parentId) {
    let addParentFormDataFunc = inject('addParentFormDataFunc')
    addParentFormDataFunc && addParentFormDataFunc((formData) => {
      if (formData[props.formNo]) {
        formData[props.formNo].forEach(row => {
          instance.refs.table.addUnSaveRow(row);
        })
      }
    })

  }
  if (props.act){
    refreshActTable.push({
      id : viewId,
      refresh : () => {
        saveSuccess()
      }
    })
  }
})

let actButtons = ref([])

const getTableRef = () =>{
  return instance.refs.table;
}

/**
 * 加载待发和待办数量
 */
const loadActTaskCount = () => {
  if (props.act) {
    let param = props.singleTable.initParam || {}
    postAction(`dynamic/zform/dataCount?formNo=${props.formNo}&path=Unsent`, param).then(unsentRes => {
      dataCount.value.Unsent = parseInt(unsentRes.data.data)
    })

    postAction(`dynamic/zform/dataCount?formNo=${props.formNo}&path=TodoAndDoing`, param).then(todoAndDoingRes => {
      dataCount.value.TodoAndDoing = parseInt(todoAndDoingRes.data.data)
    })
  }

}
let refreshActTable = window['_refreshActTable'] || []
if (props.act) {
  //加载可提交的流程
  getAction('dynamic/zform/getProcDefList', {formNo: props.formNo}).then(actRes => {
    let arr = actRes.data.procDefList.filter(item => {
      if (typeof props.actProcDefFilter === 'function'){
        return props.actProcDefFilter(item)
      }
      return true
    })
    actButtons.value = arr.map(item => {
      return {
        value: item.procDefKey,
        text: arr.length === 1 ? props.actAddButtonText : item.procDefName,
        procDefKey: item.procDefKey,
        icon: arr.length !== 1 ? 'fa-plus' : null,
        visible: true
      }
    })
  })
  loadActTaskCount()

}

/*---------查询区域相关事件回调 START-----*/

let queryAreaButtons = computed(() => {
  let _queryAreaButtons = props.buttons || []
  _queryAreaButtons = _queryAreaButtons.filter(item => {
    //主表没有保存,子表不需要显示导出按钮(只有主子表才需要这样显示)
    if (props.subTable && !props.parentId && item.value === 'export') {
      return false
    }
    if (!item.permission) {
      return true
    }
    return hasAnyPermission([item.permission])
  }).map(t => {
    if ((t.value !== 'export')&&(t.value !== 'export-queue')) {
      t.visible = !props.disabled
    }else{
      t.visible = true
    }
    //disabledFilter支持函数字符串
    if (t.disabledFilterStr){
      let res = false
      try {
        res = new Function('rows', t.disabledFilterStr)(localSelectedRows.value)
      } catch (e) {
        console.error(e)
      }
      t.disabled = res
    }else if (t.disabledFilter){
      t.disabled = t.disabledFilter(localSelectedRows.value)
    }else if (isSelectionRequiredButton(t)){
      t.disabled = props.disabled || localSelectedRows.value.length === 0
    }else{
      t.disabled = t.disabledShow ? false : props.disabled
    }
    if ((t.value === 'export')||(t.value === 'export-queue')) {
      t.disabled = false
    }
    return t
  })

  return (props.showActButtons ? actButtons.value : []).concat(_queryAreaButtons.filter(item => {
    // 工作流表单已有工作流添加按钮时，隐藏默认的添加按钮
    if (props.act && actButtons.value.length > 0 && item.value === 'add') {
      return false
    }
    return true
  }))
})

const formatModalTitle = (typeName, row) => {
  if (props.modalTitleFuncStr){
    let res = ''
    try {
      res = new Function('row', 'typeName', props.modalTitleFuncStr)(row, typeName)
    } catch (e) {
      console.error(e)
    }
    return res
  }
  if (typeof (props.modalTitle) === 'function') {
    return `${props.modalTitle(row, typeName)}`
  }
  return props.modalTitle ? `${typeName}【${props.modalTitle}】` : typeName
}

let downloadTemplateText = computed(()=>{
  if (typeof (props.modalTitle) === 'function') {
    return `${props.modalTitle({}, '下载模板')}`
  }
  return props.modalTitle ? `下载${props.modalTitle}模板` : '下载导入模板'
})

let currentExportQueueGroup = ref('')
let openExportQueueModal = (queueGroup='') =>{
  if (queueGroup){
    currentExportQueueGroup.value = queueGroup
  }
  nextTick(()=>{
    instance.refs.exportQueueModal.open()
  })
}

/*
* 添加按钮前的事件
* 接收一个Function(resolve, reject)
* */
let beforeSingleTableViewAdd = inject('beforeSingleTableViewAdd');

const isSelectionRequiredButton = (item) => {
  if (item.requireSelectedRows === true) {
    return true
  }
  if (item.requireSelectedRows === false) {
    return false
  }
  return ['batch-delete', 'batch-remove', 'remove'].includes(item.value)
}

//批量选择数据 开始
let selectDataModalProps = ref({})
let selectDataModalQueryFieldArr = ref({})
let selectDataModalDataMapping = ref({})

const selectDataModalBeforeSave = (rows) => {
  return rows.map(row => {
    let obj = {}
    obj.parent_id = {id: props.parentId}
    let targetFromId = row.id;
    let targetFrom = selectDataModalProps.value.targetFrom;
    if (targetFrom) {
      if (typeof targetFrom === 'function') {
        targetFromId = targetFrom(row);
      } else {
        targetFromId = row[targetFrom];
      }
    }
    obj[selectDataModalProps.value.targetField] = {id: targetFromId}
    if (typeof selectDataModalDataMapping.value === 'function') {
      return selectDataModalDataMapping.value(row, obj)
    }
    for (let objKey in selectDataModalDataMapping.value) {
      let k = selectDataModalDataMapping.value[objKey]
      if (typeof k === 'function') {
        obj[objKey] = k(row);
      } else {
        obj[objKey] = getData(row, k)
      }
    }
    return obj
  })
}
/**
 * 解析批量选择数据配置
 * @param config
 */
const parseBatchSelectConfig = (config) => {
  selectDataModalProps.value = config.selectDataModalProps
  console.log('selectDataModalProps', selectDataModalProps.value)
  selectDataModalProps.value.targetFilterData.forEach(item => {
    if (item.value === '${parentId}') {
      item.value = props.parentId
    }
  })
  selectDataModalQueryFieldArr.value = config.queryFieldArr
  parseDataMapping(config.dataMapping)
}
/**
 * 解析数据映射
 * @param {Array<String>, Object<String, String>, Object<String, Function(row)>} dataMapping
 */
const parseDataMapping = (dataMapping) => {
  selectDataModalDataMapping.value = {}
  if (dataMapping){
    if (Array.isArray(dataMapping)){
      dataMapping.forEach(item => {
        selectDataModalDataMapping.value[item] = item
      })
    } else if(dataMapping instanceof Object){
      selectDataModalDataMapping.value = dataMapping;
    }
  }
}

const batchSelectBeforeClickOkFun = ref(null)

//批量选择数据 结束
const clickButton = (value, item) => {
  batchSelectBeforeClickOkFun.value = null
  emits('beforeClickButton', value, item);
  currentExportQueueGroup.value = ''
  if (value === 'add') {
    if (!props.customHandleAdd) {
      if (props.rowEdit){
        //如果是行编辑模式,则直接新增一行
        let uuid = UUID();
        let row = props.rowEditDefaultRow || {}
        row.__temp_id__ = uuid
        instance.refs.table.addUnSaveRow(row);
        return
      }
      let openAddModal = () =>{
        if (props.subTable && props.parentId) {
          openRuntimeEdit(formatModalTitle('添加'), {parent: {id: props.parentId}, parentFormData: props.parentFormData})
        } else if (props.subTable && !props.parentId) {
          openRuntimeEdit(formatModalTitle('添加'), {parentFormData: props.parentFormData})
        } else {
          openRuntimeEdit(formatModalTitle('添加'))
        }
      }
      if (item.batchSelect){
        if (item.batchSelect.bathSelectClickOkBefore && typeof item.batchSelect.bathSelectClickOkBefore === 'function') {
          batchSelectBeforeClickOkFun.value = item.batchSelect.bathSelectClickOkBefore
        }
        parseBatchSelectConfig(item.batchSelect)
        if((typeof beforeSingleTableViewAdd)==='function'){
          let p = new Promise(beforeSingleTableViewAdd);
          p.then((data)=>{
            nextTick(()=>{
              instance.refs.selectDataModal.open()
            })
          }).catch((err)=>{
            console.log(err)
          })
        }else {
          nextTick(()=>{
            instance.refs.selectDataModal.open()
          })
        }
      }else{
        if((typeof beforeSingleTableViewAdd)==='function'){
          let p = new Promise(beforeSingleTableViewAdd);
          p.then((data)=>{
            openAddModal();
          }).catch((err)=>{
            console.log(err)
          })
        }else {
          openAddModal();
        }
      }
    } else {
      emits('handleAdd', {
        modal: instance.refs.modal,
        callback: () => {
          if (item.batchSelect){
            parseBatchSelectConfig(item.batchSelect)
            nextTick(()=>{
              instance.refs.selectDataModal.open()
            })
          }else{
            openRuntimeEdit(formatModalTitle('添加'))
          }
        }
      })
    }
  } else if (value === 'import') {
    if (props.subTable && !props.parentId) {
      if ((typeof beforeSingleTableViewAdd) === 'function') {
        let p = new Promise(beforeSingleTableViewAdd);
        p.then((data) => {
          importModel.value = {}
          instance.refs.import.open(formatModalTitle('导入'))
        }).catch((err) => {
          Modal.error({
            title: '提示',
            content: '请先保存数据后再进行导入操作！'
          })
          return false
        })
      }

    } else {

      importModel.value = {}
      instance.refs.import.open(formatModalTitle('导入'))
    }
  } else if (value === 'export') {
    let queryData = {}
    let exportFilterData = item.exportFilterData
    if (exportFilterData) {
      Object.assign(queryData, exportFilterData)
    }

    let _excelFileName = props.singleTable.exportExcelFileName
    if (_excelFileName) {
      if (typeof (_excelFileName) === 'function') {
        _excelFileName = _excelFileName({selectedRows:getSelectedRows()})
      }
      queryData.exportExcelFileName = _excelFileName;
    }
    if (item.fileName){
      if (typeof (item.fileName) === 'function') {
        queryData.exportExcelFileName = item.fileName({selectedRows:getSelectedRows()})
      }else {
        queryData.exportExcelFileName = item.fileName
      }
    }
    if(props.exportToFileQueue){
      queryData.exportToFileQueue = true
      if(props.singleTable.exportQueueFileFrom){
        queryData.exportQueueFileFrom = props.singleTable.exportQueueFileFrom
      }else {
        queryData.exportQueueFileFrom = props.formNo
      }
    }
    Object.assign(queryData, instance.refs.query?.getQueryData())
    if (props.singleTable.initParam){
      queryData = mergeQueryData(queryData, props.singleTable.initParam)
    }
    let selectedRowKeys = getSelectedRowKeys();
    if (selectedRowKeys.length > 0) {
      queryData[props.selectedRowKeyName] = selectedRowKeys.join(',')
      let queryParamType = queryData.queryParamType || {}
      queryParamType[props.selectedRowKeyName] = 'in'
      queryData.queryParamType = queryParamType
    }
    let exportUrl = tableViewUrl.value.export.replace('${formNo}', props.formNo).replace('${parentId}', props.parentId);
    if (item.exportByUrl) {
      exportUrl = 'dynamic/zform/expdataByUrl?url=' + Base64.encode(tableViewUrl.value.list.replace('?path=path', '/ttt/?path=path').replace('${formNo}', props.formNo).replace('${parentId}', props.parentId)) +'&formNo=' + props.formNo;
    }
    let columns;
    if(props.singleTable.exportAsColumns){
      columns = Array.from(props.singleTable.exportAsColumns);
    }else {
      columns = Array.from(props.singleTable.columns);
    }
    let subTables = []
    if(props.singleTable.exportWithSubTables){
      subTables = Array.from(props.singleTable.exportWithSubTables);
    }

    if (columns) {
      let exportData = buildExportData(columns, subTables);
      Object.assign(queryData, exportData)
      queryData.exportAsTableColumns = props.exportAsTableColumns
    } else {
      queryData.tableColumns = []
    }
    if(props.singleTable.mergeColumnConfig){
      queryData.mergeColumnConfig = props.singleTable.mergeColumnConfig
    }

    /*if (exportUrl.includes("?")) {
      exportFile(exportUrl + '&zformString=' + encodeURI(JSON.stringify(queryData)))
    } else {
      exportFile(exportUrl + '?zformString=' + encodeURI(JSON.stringify(queryData)))
    }*/

    let beforeExport = item.beforeExport;
    if(beforeExport){
      beforeExport({exportUrl, queryData});
    }
    postActionShowLoading(exportUrl, queryData).then(res => {
      if(res.data.queueId){
        openExportQueueModal();
      }else {
        if(item.preview){
          previewFileByDialog(res.data.file,item.previewTitle,res.data.groupId)
        }else {
          downLoadFileAction(res.data.fileId)
        }
      }
    })
  } else if (value === 'batch-delete') {
    let rows = instance.refs.table.getSelectedRows()
    console.log(rows)
    if (rows.length === 0) {
      message.warning('请选择一条记录')
      return
    }
    if ((props.subTable&&!props.parentId)||props.rowEdit) {

      //还未保存到数据库的子表数据 或者是行编辑模式
      confirm({
        title: '删除确认',
        content: '确认要删除选中的'+rows.length+'条记录吗',
        onOk: () => {
          rows.forEach(row => {
            instance.refs.table.removeUnSaveRow(row);
            //删除行编辑模式下的行
            instance.refs.table.removeRow(row);
          })
          let ids = rows.map(item => {
            return getData(item, idField.value)
          })
          instance.refs.table.updateAllPageSelectedRows({deleteRowKeys: ids})
        }
      })
    } else{
      let rowKeys = rows.map(item => {
        return getData(item, idField.value)
      })
      let deleteUrl = tableViewUrl.value.deleteBatch.replace('${formNo}', props.formNo)
      confirmAction('删除确认', '确认要删除选中的'+rowKeys.length+'条记录吗', `${deleteUrl}`, rowKeys, () => {
        saveSuccess()
        message.success("删除成功");
        emits('deleteSuccess')
        instance.refs.table.updateAllPageSelectedRows({deleteRowKeys: rowKeys})
      })
    }

  } else if(value === 'export-queue') {
    openExportQueueModal();
  } else if (item && item.procDefKey) {
    currentProcRow.value = {}
    openActModal(item.procDefKey)
  } else if(item.consideredAnAdd) {
    if((typeof beforeSingleTableViewAdd)==='function'){
      let p = new Promise(beforeSingleTableViewAdd);
      p.then((data)=>{
        emits('clickButton', {value, item, selectedRows: localSelectedRows.value, data: getQueryData()})
      }).catch((err)=>{
        console.log(err)
      })
    }else {
      emits('clickButton', {value, item, selectedRows: localSelectedRows.value, data: getQueryData()})
    }
  } else if (value === 'reloadTree') {
    instance.refs.table.loadData({}, 1)
  } else if (item.exportQueueGroup){
    currentExportQueueGroup.value = item.exportQueueGroup;
    emits('clickButton', {value, item, selectedRows: localSelectedRows.value, data: getQueryData()})
  }else {
    emits('clickButton', {value, item, selectedRows: localSelectedRows.value, data: getQueryData()})
  }
}
const clickOperate = (item, callback, data) => {
  emits('clickOperate', item, callback, data)
}
/*导入相关 start*/

let importErrorModalData = ref({
  visible:false,
  content:[],
  text:[],
  hideModal:() => {
    importErrorModalData.value.visible = false;
  }
})

const exportImportErrorModalText = () =>{
  postAction('/dynamic/zform/exportModalText',{
    modalText:importErrorModalData.value.text
  }).then((res)=>{
    downLoadFileAction(res.data.fileId)
  })
}

let importAction = (url, data) => {
  return new Promise((resolve, reject) => {
    postActionByParams(url, data).then(res => {
      console.log('res', res)
      Modal.info({
        content: res.msg
      })
      resolve(res)
    }).catch(err => {
      console.log('err', err)

      if (err.msg) {
        let errors = err.msg.split('<br>')
        let spans = []
        errors.forEach(error => {
          spans.push(createVNode('div', {}, error))
        })
        for(let k=1;k<errors.length; k++){
          let error = errors[k];
          if(error.length===0){
            continue;
          }
          importErrorModalData.value.text.push(error);
        }

        importErrorModalData.value.content =  createVNode('div', {}, spans);
        importErrorModalData.value.visible = true;
        // Modal.error({
        //   width: 800,
        //   wrapClassName: 'import-error-modal',
        //   content: createVNode('div', {}, spans)
        // })
      } else {
        Modal.error({
          content: '导入错误'
        })
      }

      reject(err)
    })
  })
}
let importModel = ref({})
const downloadTemplate = () => {
  if(props.importTemplateUrl){
    const link = document.createElement('a');
    link.href = props.importTemplateUrl;
    link.setAttribute('download', ''); // 设置下载属性，让浏览器下载文件而不是在浏览器中打开
    document.body.appendChild(link);
    link.click(); // 模拟点击链接，开始下载文件
    document.body.removeChild(link); // 下载完成后移除链接
  }else{
    let templateUrl = ref(tableViewUrl.value.template.replace('${formNo}', props.formNo))
    if(!props.importTemplateWithData&&tableViewUrl.value.template&&defaultTableViewUrl.template){
      if(!tableViewUrl.value.template.startsWith(defaultTableViewUrl.template)){
        getAction(templateUrl.value + '&areaId=' + config.areaId).then(res => {
          downLoadFileAction(res.data.fileId)
        })
        return;
      }
    }
    let queryData = mergeQueryData(getQueryData(), props.singleTable ? props.singleTable.initParam : {});
    let url = templateUrl.value + '&areaId=' + config.areaId + '&importTemplateWithData=' + props.importTemplateWithData + '&parentId=' + props.parentId
    if (props.importTemplateQueryData) {
      queryData = props.importTemplateQueryData
      url = templateUrl.value + '&areaId=' + config.areaId + '&importTemplateWithData=' + props.importTemplateWithData
    }
    postAction(url, queryData).then(res => {
      downLoadFileAction(res.data.fileId)
    })
  }
  //exportFile(templateUrl.value + '&areaId=' + config.areaId)
}
/*导入相关 end*/
/*---------查询区域相关事件回调 END-----*/

/*流程相关 start*/
const formatCount = (count) => {
  if (count > 10) {
    return `<span class="number-span-more">${count > 99 ? 99 : count}</span>`
  }

  return `<span class="number-span">${count}</span>`
}

let dataCount = ref({
  Unsent: 0,
  TodoAndDoing: 0,
})

// region 避免工作流提交按钮 重复提交
let  store = useStore()
let isNotCanSaveAndComplete = computed(() => store.getters.getIsNotCanSaveAndComplete)

function afterCloseOrOnCancel() {
  instance.refs.actModal.editIsProcessingHandleSelectUser(false)
}

watch(() => isNotCanSaveAndComplete.value, (newValue, oldValue) => {
  if (newValue === false && instance.refs.actModal) {
    instance.refs.actModal.editIsProcessingHandleSelectUser(false)
  }
}, {deep: true, immediate: true})
// endregion

let actModalExtendButtons = computed(() => {
  return getActButtons(currentProcRow.value, props.actNewRowButtonArr)
})

let currentProcDefKey = ref('')
let currentProcRow = ref({})
let actComment = ref('')

let saveActObj = computed(() => {
  return {
    act: {
      //comment: currentProcRow.value.act.comment,
      flag: currentProcRow.value.act.flag,
      procDefId: currentProcRow.value.act.procDefId,
      procDefKey: currentProcDefKey.value,
      procInsId: currentProcRow.value.act.procInsId,
      taskDefKey: currentProcRow.value.act.taskDefKey,
      taskId: currentProcRow.value.act.taskId,
      taskName: currentProcRow.value.act.taskName,
    }
  }
})
const actClickOperate = (item, callback, data,dataSimple) => {
  console.log(item, callback, data,dataSimple)

  const getSaveData = (dataType='save') => {
    let saveData = {procDefKey: currentProcDefKey.value, act: {}}
    Object.assign(saveData, saveActObj.value, dataType === 'save' ? dataSimple[0] : data[0])
    saveData.act.param = JSON.stringify(item.extend)
    saveData.act.comment = actComment.value
    saveData.procInsId = currentProcRow.value.act.procInsId
    return saveData
  }
  if ('cancel' === item.value) {
    //取消
    instance.refs.actModal.close()
  } else if ('save' === item.value || 'saveAndClaim' === item.value) {
    //暂存
    callback(postAction, saveDataUrl, getSaveData())
  } else if ('saveAndStart' === item.value || 'saveAndComplete' === item.value) {
    /*'saveAndStart','saveAndComplete' 提交*/
    let data = getSaveData();
    let actSubmitText
    if (currentProcRow.value.ruleArgs.formExtend['act.submitText']){
      actSubmitText = currentProcRow.value.ruleArgs.formExtend['act.submitText']
    }
    let actCommentVisible = isFormItemShow( currentProcRow.value.ruleArgs,'act.comment')
    handleSelectUser(instance.refs.actSelectUser, data, {hasSlot: !!instance.slots.actComment, actCommentLabel: props.actCommentLabel, actCommentVisible},getSaveData(props.handleSelectUserGetSaveDataType),actSubmitText).then(save => {
      callback(postAction, saveDataUrl, save)
    })

  } else if ('saveAndReject' === item.value) {
    //saveAndReject 回退
    let data = getSaveData();
    let onSaveAndReject = props.onActSaveAndReject;
    if((typeof onSaveAndReject)!=='function'){
      onSaveAndReject = ()=>{
        return undefined;
      }
    }
    handleRollBack(data,onSaveAndReject({
      procDefKey: currentProcDefKey.value,
      taskDefKey: currentProcRow.value.act.taskDefKey,
    })).then(() => {
      callback(postAction, saveDataUrl, data)
    })
  } else if ('saveAndSuperReject' === item.value) {
    //saveAndSuperReject 指定回退
    let data = getSaveData();
    let superReject = handleSuperReject;
    if(props.customSuperReject){
      superReject = props.customSuperReject;
    }
    superReject(data).then(save => {
      callback(postAction, saveDataUrl, save)
    })
  } else if ('saveAndCreateNode' === item.value) {

    //saveAndCreateNode 加签
    let data = getSaveData();
    handleCreateNode(instance.refs.actCreateNode, data).then((res) => {
      procImgShow.value = false
      showProcImg()
    })
  } else if ('saveAndDeleteNode' === item.value) {
    //saveAndDeleteNode 减签
    let data = getSaveData();
    handleDeleteNode(data).then((res) => {
      procImgShow.value = false
      showProcImg()
    })
  } else if ('saveAndNotify' === item.value) {
    //saveAndNotify 知会
    let data = getSaveData();
    handleNotify(instance.refs.actCreateNode, data).then((res) => {
      procImgShow.value = false
      showProcImg()
    })
  } else if ('saveAndDistribute' === item.value) {
    //saveAndDistribute 分发
    let data = getSaveData();
    handleDistribute(instance.refs.actCreateNode, data).then((res) => {
      procImgShow.value = false
      showProcImg()
    })
  } else if ('saveAndTerminate' === item.value) {
    //saveAndTerminate 终止
    if (!instance.slots.actComment) {
      //没有意见slot
      prompt({
        title: '请输入' + props.actCommentLabel,
        maxlength: 500,
        callback: (value) => {
          actComment.value = value
          let data = getSaveData();
          callback(postAction, saveDataUrl, data)
        }
      })
    } else {
      confirm({
        title: '操作确认',
        content: '确定终止流程吗？',
        onOk: () => {
          let data = getSaveData();
          callback(postAction, saveDataUrl, data)
        }
      })
    }

  } else if ('saveAndSend' === item.value) {
    //saveAndSend ？？//todo 不知道是什么操作
  } else if ('saveAndSuspend' === item.value) {
    confirm({
      title: '操作确认',
      content: '确定【挂起或激活】?',
      onOk: () => {
        let data = getSaveData();
        callback(postAction, saveDataUrl, data)
      }
    })
  }
}

//打开流程弹窗
const openActModal = (procDefKey, disabled = false) => {
  procImgShow.value = false
  procTableShow.value = false
  currentProcDefKey.value = procDefKey
  actComment.value = ''
  if (getData(currentProcRow.value, idField.value)) {

    if (currentProcRow.value.__actFormNo__ && currentProcRow.value.__actModule__) {
      import(`@/views/${currentProcRow.value.__actModule__}/${currentProcRow.value.__actFormNo__}/form.vue`).then(res => {
        currentModalComponent.value = res.default
      })
    }
    //当前数据taskId
    let taskId = '';
    if (currentProcRow.value.act) {
      taskId = currentProcRow.value.act.taskId
    }
    //查询流程相关数据
    getAction(`dynamic/zform/getZformWithActMap?formNo=${currentProcRow.value.__actFormNo__ ? currentProcRow.value.__actFormNo__ : props.formNo}&id=${getData(currentProcRow.value, idField.value)}&procDefKey=${procDefKey}&procTaskId=${taskId}`, {}).then(actRes => {
      currentProcRow.value = actRes.data.data
      if (currentProcRow.value.act.procInsId) {
        //查询目前办理的节点按钮数据
        getAction('dynamic/zform/isCacheData', {processInstanceId: currentProcRow.value.act.procInsId}).then(res => {
          currentProcRow.value.act.procTaskPermission = res.data.data.taskPermission
        })
      }

      let row = {actRuleArgs: currentProcRow.value.ruleArgs}
      Object.assign(row, currentProcRow.value)
      let title = '办理'
      if (disabled) {
        title = '查看'
        currentProcRow.value.disabled = disabled
      }
      instance.refs.actModal.open(formatModalTitle(title, row), row, disabled)
    })
  } else {
    //查询流程相关数据
    getAction(`dynamic/zform/getZformWithActMap?formNo=${props.formNo}&id=&procDefKey=${procDefKey}`, {}).then(actRes => {
      currentProcRow.value = actRes.data.data
      currentProcRow.value.actRuleArgs = currentProcRow.value.ruleArgs
      instance.refs.actModal.open(formatModalTitle('添加'), currentProcRow.value)
    })
  }

}

let procImgShow = ref(false)//流程图是否显示
let procImgData = ref(null)//流程图图片base64数据
let procTableShow = ref(false)//流程历史table是否显示
let procTableData = ref([])//流程历史table数据
const showProcImg = () => {
  if (!procImgShow.value) {
    let procDefId = currentProcRow.value.act.procDefId;
    let procInsId = currentProcRow.value.act.procInsId;

    getAction('dynamic/zform/tracePhoto', {procDefId: procDefId, procInsId: procInsId}).then(imgRes => {
      console.log(imgRes)
      procImgData.value = 'data:image/png;base64,' + imgRes.data.data
      procImgShow.value = true
      procTableShow.value = false
    })
  } else {
    procImgShow.value = false
  }

}
const showProcTable = () => {
  if (!procTableShow.value) {
    let procInsId = currentProcRow.value.act.procInsId;
    getAction('dynamic/zform/histoicFlow', {procInsId: procInsId}).then(hisRes => {
      procTableData.value = hisRes.data.data
      procImgShow.value = false
      procTableShow.value = true
    })
  } else {
    procTableShow.value = false
  }

}
/*流程相关 end*/
// region 将用户输入的查询 数据 的前后空格 去除
const trimObjectStrings = (obj) => {
    for (const key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key) && obj[key] !== undefined) {
            if (typeof obj[key] === 'string') {
                obj[key] = obj[key].trim();
            } else if (typeof obj[key] === 'object') {
                // 如果字段是对象类型，则递归调用 trimObjectStrings
                trimObjectStrings(obj[key]);
            }
        }
    }
};
// endregion

/*---------表格相关事件回调 START-----*/
/**
 * 查询按钮方法
 */
const handleQuery = (data) => {
  // region 将输入的查询 数据 的前后空格 去除
  try {
      if (props.isTrimQuery) {
          trimObjectStrings(data);
      }
  } catch (error) {
      console.error("An error occurred while trimming object strings:", error);
  }
  // endregion
  if (props.customHandleQuery) {
    emits('clickButton', {value: 'query', data: data})
  } else if (dataUrl.value) {
    if(props.beforeQuery !== null){
      let result = props.beforeQuery(data);
      if(isPromise(result)){
        result.then(res => {
          instance.refs.table.loadData(data, 1)
          emits('onQuery', {value: 'query', data: data})
        })
      }else{
        data = Object.assign(data, result);
        instance.refs.table.loadData(data, 1)
      }
    }else{
      instance.refs.table.loadData(data, 1)
      emits('onQuery', {value: 'query', data: data})
    }
  } else {
    emits('clickButton', {value: 'query', data: data})
  }

}

const isPromise = (val) => {
  return val != null && typeof (val) === 'object' && typeof (val.then) === 'function'
};

const handleReset = () => {
  emits('clickButton', {value: 'reset'})
}
let queryAreaHeight = ref(32)
const rowCountChange = (_queryAreaHeight) => {
  queryAreaHeight.value = _queryAreaHeight
}
/**
 * 表格数据总条数 用于查询区域显示
 */
let totalCount = ref(0)
/**
 * 表格加载成功回调
 */
const loadSuccess = ({total, rows, res}) => {
  totalCount.value = total
  localSelectedRows.value = instance.refs.table.getSelectedRows();
  emits('update:totalCount', total)
  emits('totalCountChange', total)
  emits('requestResult', res)
  emits('loadSuccess', res)
}
const onSelectAll = (selected, selectedRows, changeRows) => {
  emits('onSelectAll', selected, selectedRows, changeRows)
}
const onSelect = (record, selected, selectedRows, nativeEvent) => {
  emits('onSelect', record, selected, selectedRows, nativeEvent)
}
let localSelectedRows = ref([])
const selectChange = (selectedRowKeys, selectedRows) => {
  emits('selectChange', selectedRowKeys, selectedRows)
  localSelectedRows.value = selectedRows
}

const onCellValueChange = (data)=>{
  emits('cellValueChange',data)
}

let emits = defineEmits(['clickRow', 'onSelectAll', 'onSelect', 'clickButton', 'update:totalCount', 'saveSuccess',  'loadSuccess', 'totalCountChange', 'handleAdd', 'update:actStatus', 'clickOperate', 'requestResult', 'deleteSuccess','clickTableRow','cellValueChange','update:value','onQuery','onCancel','beforeEdit','beforeClickButton'])

const updateValue = (data) => {
  emits('update:value',data)
}
/**
 * 点击行事件
 */
const clickRow = ({value, row, index}) => {
  emits('beforeClickRow', {value, row, index});
  if (props.commonActTaskList) {
    //通用待办页 构造对应工作流task的row数据
    let commonTaskRow = {}
    Object.assign(commonTaskRow, row)
    row = {}
    row.proc_def_key = commonTaskRow.map.PROC_DEF_ID.split(":")[0]
    row.proc_ins_id = commonTaskRow.map.PROC_INS_ID
    setData(row, idField.value, commonTaskRow.map.ENTITY_ID)
    row.__actFormNo__ = commonTaskRow.map.FORM_NO
    row.__actModule__ = commonTaskRow.map.MODULE
  }
  if (value === 'view') {
    handleView({row, index});
  } else if (value === 'edit') {
    handleEdit({row, index});
  } else if (value === 'addChild') {
    instance.refs.modal.open(formatModalTitle('添加', row), {parent: {id: row.id, name: row.name}})
  }else if (value === 'delete') {
    handleDelete({row, index});
  } else if (value === 'editAct') {
    currentProcRow.value = row
    openActModal(row.proc_def_key)
  } else if (value === 'viewAct') {
    currentProcRow.value = row
    openActModal(row.proc_def_key, true)
  } else {
    emits('clickRow', {value, row, index})
  }
  emits('afterClickRow', {value, row, index});
}
const clickTableRow=({record,index})=>{
  emits('clickTableRow', {record,index})
}
const handleView = ({row, index}) => {
  openRuntimeEdit(formatModalTitle('查看', row), row, true)
}
const handleEdit = ({row, index}) => {
  emits('beforeEdit', {row,index});
  // 工作流表单使用actModal，显示工作流相关按钮（暂存/提交等）
  if (props.act && actButtons.value.length > 0) {
    const procDefKey = row.proc_def_key || actButtons.value[0].procDefKey
    currentProcRow.value = row
    openActModal(procDefKey)
  } else {
    openRuntimeEdit(formatModalTitle('编辑', row), row)
  }
}
const handleDelete = ({row, index}) => {
  let deleteUrl = tableViewUrl.value.delete.replace('${formNo}', props.formNo)
  if ((props.subTable&&!props.parentId)||props.rowEdit) {
    //还未保存到数据库的子表数据 或者是行编辑模式
    confirm({
      title: '删除确认',
      content: '确认要删除该条记录吗',
      onOk: () => {
        instance.refs.table.removeUnSaveRow(row);
        //删除行编辑模式下的行
        instance.refs.table.removeRow(row);
        instance.refs.table.updateAllPageSelectedRows({deleteRowKeys: [getData(row, idField.value)]})
      }
    })
  }else if (getData(row, idField.value)) {
    confirmAction('删除确认', '确认要删除该条记录吗', `${deleteUrl}&ids=${getData(row, idField.value)}`, {}, () => {
      saveSuccess()
      emits('deleteSuccess')
      instance.refs.table.updateAllPageSelectedRows({deleteRowKeys: [getData(row, idField.value)]})
    })
  }

}
/*---------表格相关事件回调 END-----*/

/*---------弹窗相关事件回调 START-----*/
const updateUserTodoCount = inject('updateUserTodoCount')
/**
 * 保存成功回调
 */
const saveSuccess = (e) => {
  if (dataUrl.value) {

    if (props.subTable && e && (e.type === 'add'||e.type === 'update')) {
      if (e.type === 'add') {
        let uuid = UUID();
        e.data[0].__temp_id__ = uuid
        instance.refs.table.addUnSaveRow(e.data[0]);
      } else if (e.type === 'update') {
        instance.refs.table.updateUnSaveRow(e.data[0]);
      }

    }
    if (props.customHandleQuery || props.triggerSaveSuccess){
      emits('saveSuccess',e)
    }else{
      loadData(getQueryData())
      clearSelectedRows()
    }
  } else {
    emits('clickButton', {value: 'query'})
  }
  if (props.act) {
    updateUserTodoCount()
    let arr = window['_refreshActTable'] || []
    arr.forEach((item) => {
      if (item.id === viewId) {
        item.refresh()
      }
    })
  }
}

const onCancel = () => {
  if (props.cancelTriggerRefresh) {
    saveSuccess()
  }
  emits('onCancel')
}
/*---------弹窗相关事件回调 END-----*/
const loadData = (param = {}, setQueryArea = false,pageNo = undefined) => {
  if (setQueryArea) {
    instance.refs.query.setQueryData(param)
  }
  nextTick(() => {
    instance.refs.table.loadData(param,pageNo)
    loadActTaskCount()
  })
}

const getModal = () => {
  return instance.refs.modal
}
const getImportModal = () => {
  return instance.refs.import
}
const getSelectedRowKeys = () => {
  return instance.refs.table.getSelectedRowKeys()
}
const getSelectedRows = () => {
  return instance.refs.table.getSelectedRows()
}
const clearSelectedRows = () => {
  instance.refs.table.clearSelectedRows();
  localSelectedRows.value = []
}
const getDataSource = (validate = true) => {
  return instance.refs.table.getDataSource(validate)
}
const autoMergeColumn = (colId='id',mergeColumns=[]) => {
  instance.refs.table.autoMergeColumn(colId,mergeColumns)
}
const getQueryData = () => {
  return instance.refs.query.getQueryData()
}
const getPinyinQuery = () => {
  return instance.refs.query.getPinyinQuery()
}
const setQueryData = (param) => {
  return instance.refs.query.setQueryData(param)
}
const getActSelectUserRef = () => {
  return instance.refs.actSelectUser
}
const importErrorClickOk = () =>{
  importErrorModalData.value.text = [];
  importErrorModalData.value.hideModal()
}
//向form组件提供 form可以获取table的数据
provide(props.formNo + 'SingleTableViewMethods', {
  getUnSaveRows: () => {
    return getTableRef().getUnSaveRows()
  },
  getDataSource: getDataSource
})
defineExpose({
  getModal, getSelectedRowKeys, getSelectedRows, clearSelectedRows, getDataSource, loadData, getQueryData, getPinyinQuery, saveSuccess, setQueryData, clickRow, getTableRef, autoMergeColumn, handleEdit, handleView, getActSelectUserRef, openExportQueueModal, clickButton,getImportModal
})
</script>
<style lang="less" scoped>
.single-table-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  padding: 10px;

  .single-table-view-main,
  .single-table-runtime-page {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-height: 0;
    width: 100%;
  }

  .single-table-view-table {
    flex: 1;
  }

  .single-table-runtime-page {
    gap: 12px;
  }

  .single-table-runtime-page-header,
  .single-table-runtime-page-body {
    background: #ffffff;
    border: 1px solid #e6ebf2;
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(31, 41, 55, 0.04);
  }

  .single-table-runtime-page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 14px 18px;
  }

  .single-table-runtime-page-title {
    display: flex;
    align-items: center;
    gap: 14px;
    min-width: 0;

    h2 {
      margin: 0;
      color: #1f2937;
      font-size: 18px;
      font-weight: 600;
      line-height: 26px;
    }

    p {
      margin: 2px 0 0;
      color: #667085;
      font-size: 13px;
      line-height: 20px;
    }
  }

  .single-table-runtime-back {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    height: 32px;
    padding: 0 6px;
  }

  .single-table-runtime-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    flex-shrink: 0;

    :deep(.ant-btn) {
      height: 32px;
      border-radius: 6px !important;
      box-shadow: none;
    }
  }

  .single-table-runtime-page-body {
    flex: 1;
    min-height: 0;
    padding: 16px;
    overflow: auto;
  }

  .query-fields {
    .ant-radio-button-wrapper {

      .status-bar {
        position: relative;

        .status-count {
          position: absolute;
          background: #ff4d4f;
          color: #fff;
          min-width: 16px;
          height: 16px;
          width: auto;
          top: 1px;
          right: -15px;
          border-radius: 50%;
          font-size: 12px;
          line-height: 16px;
          text-align: center;

        }

      }
    }

  }

  :deep(.ant-scroll-number){
    margin-top: 5px;
  }
}

:global(.single-table-runtime-drawer .ant-drawer-header) {
  padding: 14px 18px;
}

:global(.single-table-runtime-drawer .ant-drawer-body) {
  background: #f5f7fb;
}

:global(.single-table-runtime-drawer .ant-drawer-footer) {
  padding: 0;
}

:global(.single-table-runtime-drawer .single-table-runtime-drawer-title) {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

:global(.single-table-runtime-drawer .single-table-runtime-drawer-title strong) {
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

:global(.single-table-runtime-drawer .single-table-runtime-drawer-title span) {
  color: #667085;
  font-size: 13px;
  font-weight: 400;
  line-height: 20px;
}

:global(.single-table-runtime-drawer .single-table-runtime-drawer-body) {
  min-height: 100%;
  padding: 16px;
}

:global(.single-table-runtime-drawer .single-table-runtime-drawer-body > .u-form-container) {
  background: #ffffff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  padding: 16px;
}

:global(.single-table-runtime-drawer .single-table-runtime-drawer-footer) {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 10px 16px;
  border-top: 1px solid #eef2f7;
  background: #ffffff;
}

:global(.single-table-runtime-drawer .single-table-runtime-drawer-footer .ant-btn) {
  height: 32px;
  border-radius: 6px !important;
  box-shadow: none;
}
</style>
<style>
.number-span-more {
  transform: scale(0.8);
}

</style>
