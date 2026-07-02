<template>
  <u-modal ref="modal" :width="modalWidth" :customOK="true" @clickOk="clickOk" v-bind="uModal"
           :formDisabled="true">
    <slot></slot>
    <single-table-view :autoHeight="autoHeight" @saveSuccess="addModalSaveSuccess" :modalComponent="props.modalComponent" class="select-data-modal-view" v-show="singleTableShow" ref="tableView" :customHandleQuery="true" @clickButton="clickButton"
                       :url="url" :singleTable="singleTableRef" :buttons="buttonsRef" :formNo="props.fromFormNo"
                       :height="height" @clickRow="clickRow">
      <template #queryFields>
        <slot name="queryFields"/>
      </template>
      <template #queryAreaExt>
        <template v-if="$slots.extendForm">
          <u-form ref="extendRef" extend-class="select-data-modal-form" v-model:value="localExtendFormModel" v-bind="extendFormProps" set-form-inject-key="none">
            <slot name="extendForm"/>
          </u-form>
        </template>
      </template>
    </single-table-view>
  </u-modal>
</template>

<script>
export default {
  name: "USelectDataModal"
}
</script>
<script setup>
import {getCurrentInstance, computed, ref, nextTick, watch} from "vue";
import {Modal} from "ant-design-vue";
import {isPromise} from "@/lib/tools";
import {batchSaveSelectAction} from "@/api/api";
import UForm from "@/components/form/UForm";

let instance = getCurrentInstance();

let props = defineProps({
  modalWidth: {
    type: Number,
    default: 1200
  },
  modalTitle: {
    type: String,
    default: '请选择'
  },
  fromFormNo: {
    type: String,
    default: ''
  },
  filterData: {
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
  targetJoinKey: {
    type: String,
    default: null
  },
  parentId: {
    type: String,
    default: null
  },
  targetFilterData: {
    type: Array,
    default() {
      return []
    }
  },
  singleTable: {
    type: Object,
    default() {
      return {}
    }
  },
  modalComponent: {
    type: Object,
    default() {
      return {}
    }
  },

  beforeSave: {
    type: Function,
    default: null
  },
  dataUrl : {
    type: String,
    default: 'dynamic/zform/gridselectDataMap'
  },
  saveUrl : {
    type: String,
    default: 'dynamic/zform/batchSaveSelect'
  },
  customOk:{
    type: Boolean,
    default: false
  },
  customOkClose:{
    type: Boolean,
    default: true
  },
  uModal: {
    type: Object,
    default() {
      return {}
    }
  },
  buttons: {
    type: Array,
    default() {
      return []
    }
  },
  showRowButtons: {
    type: Boolean,
    default: false
  },
  orderBy:{
    type: String,
    default: undefined
  },
  /**
   * 额外表单UForm的配置
   */
  extendFormProps: {
    type: Object,
    default: () => {
      return {}
    }
  },
  /**
   * 额外表单饿数据 使用v-model绑定
   */
  extendFormModel: {
    type: Object,
    default: () => {
      return {}
    }
  },
  /**
   * 表格 y轴高度 计算系数
   */
  tableHeightCoefficient:{
    type: Number,
    default: 0.9
  },
  autoHeight: {
    type: Boolean,
    default: false
  },
  /**
   * 选中数据 确认按钮前 执行函数，可以根据选择的数据弹出确认框等
   */
  clickOkBefore: {
    type: Function,
    required: false
  }

})

let localExtendFormModel = ref(props.extendFormModel)

function addModalSaveSuccess() {
  loadData()
}
watch(() => props.extendFormModel, () => {
  localExtendFormModel.value = props.extendFormModel
}, {immediate: true})

let url = computed(()=>{
  return {
    list: props.dataUrl,
  }
})
let height = computed(() => {
  return document.body.clientHeight * props.tableHeightCoefficient - 160 - 80
})

let buttonsRef = computed(()=>{
  return props.buttons
})

let singleTableShow = ref(true)

let singleTableRef = computed(() => {
  let obj = {
    initParam: {
      tableName: props.fromFormNo,
      filterList: props.filterData,
      targetTableName: props.targetFormNo,
      targetField: props.targetField,
      targetJoinKey: props.targetJoinKey,
      targetFilterList: props.targetFilterData.map(item => {
        if (item.value === '${parentId}') {
          item.value = props.parentId
        }
        return item
      }),
      orderBy: props.orderBy,
    },
    ignoreSetTableRef: true,
    showRowButtons: props.showRowButtons
  }
  Object.assign(obj, props.singleTable)
  return obj
})
let emits = defineEmits(['saveSuccess','customClickOk','afterOpen']);
const saveData = (dataList) => {
  if (props.customOk){
    emits('customClickOk')
    instance.refs.modal.endLoading()
    if(props.customOkClose){
      instance.refs.modal.close()
    }
    return
  }
  batchSaveSelectAction(props.targetFormNo, dataList,props.saveUrl).then(() => {
    emits('saveSuccess')
    instance.refs.modal.endLoading()
    instance.refs.modal.close()
  }).catch(() => {
    // Modal.error({content:'保存失败'})
  })

}
const clickOk = () => {
  const next = () => {
    let rows = instance.refs.tableView.getSelectedRows();
    if (rows.length > 0) {
      instance.refs.modal.startLoading()
      if (props.beforeSave) {
        let data = props.beforeSave(rows);
        if (isPromise(data)) {
          data.then(dataToSave => {
            saveData(dataToSave)
          }).catch(() => {
            instance.refs.modal.endLoading()
          })
        }else {
          saveData(data)
        }
      }else {
        saveData(rows)
      }
    }else {
      Modal.warning({content:'请选择数据',zIndex: 999999})
    }
  }
  // 执行确认逻辑：如果 props.confirmBefore 存在则调用，否则直接 next
  const runWithConfirm = () => {
    if (typeof props.clickOkBefore === 'function') {
      let rows = instance.refs.tableView.getSelectedRows();
      if (rows.length > 0) {
        props.clickOkBefore(next,rows); // 把 next 作为回调传给外部确认逻辑
      }else {
        next();
      }
    } else {
      next();
    }
  };

  if (instance.refs.extendRef) {
    instance.refs.extendRef.validateFields().then(() => {
      runWithConfirm();
    });
  } else {
    runWithConfirm();
  }

}

const clickRow = ({value, row, index}) => {
  emits('clickRow', {value, row, index})
}

const clickButton = ({value, data}) => {
  if ('query' === value) {
    loadData()
  }else{
    emits('clickButton', {value, data})
  }

}
const open = () => {
  instance.refs.modal.open(props.modalTitle)
  singleTableShow.value = true
  nextTick(()=>{
    emits('afterOpen')
  })
}
const close = () => {
  instance.refs.modal.close()
}
const hideSingleTable = () => {
  singleTableShow.value = false
}
const getSelectedRows = () => {
  return instance.refs.tableView.getSelectedRows();
}

const loadData = () => {
  let data = instance.refs.tableView.getQueryData()
  let pinyinQuery = instance.refs.tableView.getPinyinQuery()
  let filterList = []
  filterList = filterList.concat(props.filterData)
  let queryParamType = data.queryParamType
  if (queryParamType) {
    for (let key in data) {
      if (key !== 'queryParamType') {
        if ((key.indexOf('begin') === 0 || key.indexOf('end') === 0) && queryParamType[key] === 'between') {
          key = key.indexOf('begin') === 0 ? key.substring(5) : key.substring(3)
          //首字母小写
          let queryKey = key.substring(0, 1).toLowerCase() + key.substring(1)
          let filter = {
            key: queryKey,
            value: data['begin' + key],
            value2: data['end' + key],
            type: 'between'
          }
          delete data['begin' + key]
          delete data['end' + key]
          filterList.push(filter)
        } else {
          let filter = {
            key: key,
            value: data[key],
            type: queryParamType[key]
          }
          if (pinyinQuery[key]) {
            filter.pinyin = true
          }
          filterList.push(filter)
        }

      }
    }
  }
  instance.refs.tableView.loadData({
    filterList
  },false,1)
}

const getQueryData = () => {
  return instance.refs.tableView.getQueryData()
}

defineExpose({
  open, close, getSelectedRows, hideSingleTable, loadData, getQueryData
})
</script>
<style lang="less" scoped>
.select-data-modal-view {

  :deep(.u-query-area) {
    width: 100%;
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: space-between;
  }

  .select-data-modal-form {
    :deep(.ant-form-item) {
      margin-top: 5px;
      margin-bottom: 0;
    }
  }
}

</style>
