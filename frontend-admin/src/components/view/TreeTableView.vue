<template>
  <div class="tree-table-view" :style="height?{height:height+'px'}:{}">
    <!--    查询区域-->
    <query-area ref="query" @query="handleQuery" @reset="handleReset" :totalCount="totalCount" v-bind="queryArea" :queryButton="queryButton">
      <template v-if="$slots.queryHeader" #header>
        <slot name="queryHeader"/>
      </template>
      <template #left>
        <template v-bind:key="item.value" v-for="item in queryAreaButtons">
          <a-button @click="clickButton(item.value)"
                    v-if="!item.disabled"
                    :class="['opt-btn-'+item.value]"
                    type="primary">
            {{ item.text }}
          </a-button>
        </template>

      </template>
      <template #queryFields>
        <slot name="queryFields"/>
      </template>

    </query-area>
    <!--    数据表格-->
    <single-table class="tree-table-view-table" ref="table"
                  :data-url="dataUrl"
                  :disabled="disabled"
                  v-bind="singleTable"
                  :rowButtons="rowButtons"
                  :height="height?height-32: null"
                  :rowExpandable="rowExpandable"
                  @clickRow="clickRow"
                  @loadSuccess="loadSuccess"></single-table>

    <!--    弹窗组件-->
    <u-modal ref="modal" :width="modalWidth" :full="modalFull" :component="modalComponent" :parentId="parentId"
             @saveSuccess="saveSuccess"></u-modal>

    <!--    导入弹窗-->
    <u-modal ref="import" :width="600" @saveSuccess="saveSuccess" :action="importAction" ok-text="导入">

      <u-form v-model:value="importModel" :labelWidth="120" :formNo="formNo"
              :url="{save:'dynamic/zform/impdata?uniqueId=&toCompany=&loginName=admin&parentId='}">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="导入模板">
              <a-button @click="downloadTemplate" type="link">下载</a-button>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="上传Excel" name="fileId"
                         :rules="[{ required: true, message: '请上传Excel' }]">
              <u-upload :file-count="1" name="fileId" :accepts="['.xls', '.xlsx']"
                        v-model:value="importModel.fileId"></u-upload>
            </a-form-item>
          </a-col>
        </a-row>
      </u-form>
    </u-modal>

  </div>
</template>

<script>

export default {
  name: "TreeTableView",
}

</script>
<script setup>
import {computed, getCurrentInstance, ref, nextTick} from "vue";
import {message} from 'ant-design-vue';
import {confirmAction, postActionByParams, exportFile} from "@/api/action";
import {hasAnyPermission, isPromise} from "@/lib/tools";

let instance = getCurrentInstance();

let props = defineProps({

  height: {
    type: Number,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
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
      }, /*{
        value: 'import',
        text: '导入'
      }, {
        value: 'export',
        text: '导出'
      }, */{
        value: 'batch-delete',
        text: '删除'
      }]
    }
  },
  /**
   * 是否自定义新增按钮点击事件
   */
  customHandleAdd: {
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
  queryButton: {
    type: Boolean,
    default: false
  },
  rowExpandable: {
    type: Function,
    default(record){
      return true
    }
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
      }, {
        value: 'delete',
        text: '删除',
      }, {
        value: 'addChild',
        text: '添加下级',
      }]
    }
  },
  modalComponent: {
    type: Object,
    default: null
  },
  formNo: {
    type: String,
    default: ''
  },
  parentId: {
    type: String,
    default: ''
  },
  /**
   * 相关的请求地址
   */
  url: {
    type: Object,
    default() {
      return {
        list: 'dynamic/zform/datamap?path=path&formNo=${formNo}&traceFlag=&parentId=${parentId}',
        delete: 'dynamic/zform/delete?formNo=${formNo}',
        template: 'system/sysFile/fileDownload?formNo=demo_list',
      }
    }
  },
  modalWidth: {
    type: Number,
    default: 1200
  },
  modalTitle: {
    type: String,
    default: ''
  },
  modalFull: {
    type: Boolean,
    default: false
  },
  beforeQuery: {
    type: Function,
    default: null
  }
})
let dataUrl = ref(props.url.list.replace('${formNo}', props.formNo).replace('${parentId}', props.parentId))
/*---------查询区域相关事件回调 START-----*/
let queryAreaButtons = computed(() => {
  let _queryAreaButtons = props.buttons||[]
  _queryAreaButtons =  _queryAreaButtons.filter(item=>{
    if (!item.permission){
      return true
    }
    return hasAnyPermission([item.permission])
  }).map(t => {
    if (t.value !== 'export') {
      t.disabled = props.disabled
    }
    return t
  })
  return _queryAreaButtons
})
const clickButton = (value) => {
  if (value === 'add') {
    if (!props.customHandleAdd) {
      instance.refs.modal.open(props.modalTitle ? `添加【${props.modalTitle}】` : '添加')
    }
  } else if (value === 'import') {
    importModel.value = {}
    instance.refs.import.open(props.modalTitle ? `导入【${props.modalTitle}】` : '导入')
  } else if (value === 'export') {
    let queryData = instance.refs.query.getQueryData()
    exportFile('dynamic/zform/expdata?path=path&traceFlag=&formNo=' + props.formNo + '&parentId=&zformString=' + encodeURI(JSON.stringify(queryData)))
  } else if (value === 'batch-delete') {
    let rowKeys = instance.refs.table.getSelectedRowKeys()
    if (rowKeys.length === 0) {
      message.warning('请选择一条记录')
      return
    }
    let deleteUrl = props.url.delete.replace('${formNo}', props.formNo)
    confirmAction('删除确认', '确认要删除选中记录吗', `${deleteUrl}&ids=${rowKeys.join()}`, {}, () => {
      instance.refs.table.loadData()
    })
  } else {
    emits('clickButton', {value})
  }
}
let importAction = postActionByParams;
let importModel = ref({})
const downloadTemplate = () => {
  let templateUrl = ref(props.url.template.replace('${formNo}', props.formNo))
  exportFile(templateUrl.value)
}

/*---------查询区域相关事件回调 END-----*/

/*---------表格相关事件回调 START-----*/
/**
 * 查询按钮方法
 */
const handleQuery = (data) => {
  if(props.beforeQuery !== null){
    let result = props.beforeQuery(data);
    if(isPromise(result)){
      result.then(res => {
        instance.refs.table.loadData(data, 1)
      })
    }else{
      data = Object.assign(data, result);
      instance.refs.table.loadData(data, 1)
    }
    return;
  }
  instance.refs.table.loadData(data, 1)
}
const handleReset = () => {
  instance.refs.table.loadData({}, 1)
}
/**
 * 表格数据总条数 用于查询区域显示
 */
let totalCount = ref(0)
/**
 * 表格加载成功回调
 */
const loadSuccess = ({total, rows,res}) => {
  totalCount.value = total
  emits('loadSuccess', res)
}

let emits = defineEmits(['clickRow', 'clickButton','loadSuccess'])
/**
 * 点击行事件
 */
const clickRow = ({value, row, index}) => {
  if (value === 'view') {
    handleView({row, index});
  } else if (value === 'edit') {
    handleEdit({row, index});
  } else if (value === 'delete') {
    handleDelete({row, index});
  } else if (value === 'addChild') {
    instance.refs.modal.open(props.modalTitle ? `添加【${props.modalTitle}】` : '添加', {parent: {id: row.id, name: row.name}})
  } else {
    emits('clickRow', {value, row, index})
  }
}
const handleView = ({row, index}) => {
  instance.refs.modal.open(props.modalTitle ? `查看【${props.modalTitle}】` : '查看', row, true)
}
const handleEdit = ({row, index}) => {
  instance.refs.modal.open(props.modalTitle ? `编辑【${props.modalTitle}】` : '编辑', row)
}
const handleDelete = ({row, index}) => {
  let deleteUrl = props.url.delete.replace('${formNo}', props.formNo)
  confirmAction('删除确认', '确认要删除该条记录吗', `${deleteUrl}&ids=${row.id}`, {}, () => {
    instance.refs.table.loadData()
  })
}
/*---------表格相关事件回调 END-----*/

/*---------弹窗相关事件回调 START-----*/
/**
 * 保存成功回调
 */
const saveSuccess = () => {

  instance.refs.table.loadData()
}
/*---------弹窗相关事件回调 END-----*/
const loadData = (param = {}) => {
  nextTick(() => {
    instance.refs.table.loadData(param)
  })
}
defineExpose({
  loadData,saveSuccess
})
</script>
<style lang="less" scoped>
.tree-table-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 14px;

  .tree-table-view-table {
    flex: 1;
  }

  :deep(.ant-table-cell-with-append) {
    display: flex;
    align-items: center;
  }
}
</style>
