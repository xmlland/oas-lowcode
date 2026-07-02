<template>
  <a-spin :spinning="spinning" :id="'u-form-spinning'+id">
    <div class="u-form-container" :class="extendClass" :id="'u-form-container'+id" v-if="'top'===subTableType&&localSubTables.length>0">

      <a-tabs class="multi-table-tabs" :class="tabClass" v-model:activeKey="activeKey">
        <template #rightExtra>
          <slot name="tabBarExtraContent"></slot>
          <a-button v-if="showActLog&&localFormModel.proc_ins_id" @click="showActLogModal" type="link">{{ actLogButtonText }}</a-button>
        </template>
        <a-tab-pane v-if="mainTableVisible" :key="0" :forceRender="false" :tab="mainTableTitle" style="overflow:auto;padding:15px 8px" :style="{height:tabPageHeight+'px'}">
          <a-form :id="id" ref="form" :model="localFormModel" :labelCol="{ style: { width: labelWidth+'px' } }" :colon="colon"
                  :hideRequiredMark="disabled||hideRequiredMark">
            <slot></slot>
          </a-form>
        </a-tab-pane>
        <template v-else>
          <a-form :id="id" ref="form" :model="localFormModel" style="height: 0" :labelCol="{ style: { width: labelWidth+'px' } }" :colon="colon"
                  :hideRequiredMark="disabled||hideRequiredMark">
            <slot></slot>
          </a-form>
        </template>
        <a-tab-pane v-for="(table,index) in localSubTables" :key="index+1" :tab="table.title" :forceRender="false">
          <component :style="{height:tabPageHeight+'px'}" :disabled="table.hasOwnProperty('tabDisabled')?(table.tabDisabled||disabled):disabled"
                     :formItemRequire="isFormItemRequire(actRuleArgs,table.key)"
                     :height="tabPageHeight"
                     :width="formFull?clientWidth:subTableWidth"
                     :parentId="table.parentId?table.parentId:(table.ignoreParent?'':subTableParentId)"
                     :parentFormData="localFormModel"
                     :parentActRuleArgs="actRuleArgs"
                     :extendSubTableData="table.extendSubTableData"
                     v-bind="extendFormData"
                     :is="table.component"></component>
        </a-tab-pane>
      </a-tabs>
    </div>
    <div class="u-form-container" :class="extendClass" :id="'u-form-container'+id" :style="localSubTables.length>0&&maxHeight?'height: 70vh': ''"
         v-if="'bottom'===subTableType||localSubTables.length===0">
      <template v-if="localSubTables.length>0&&mainTableTitleVisible">
        <u-title>{{ mainTableTitle }}
          <template v-if="showActLog&&localFormModel.proc_ins_id" #extra>
            <a-button @click="showActLogModal" type="link">{{ actLogButtonText }}</a-button>
          </template>
        </u-title>
      </template>
      <template v-else-if="showActLog&&localFormModel.proc_ins_id">
        <div style="display: flex;justify-content: right">
          <a-button @click="showActLogModal" type="link">{{ actLogButtonText }}</a-button>
        </div>
      </template>
      <a-form v-show="mainTableVisible" :id="id" ref="form" :model="localFormModel" :labelCol="{ style: { width: labelWidth+'px' } }" :colon="colon"
              :hideRequiredMark="disabled||hideRequiredMark">
        <slot></slot>
      </a-form>
      <template v-if="'bottom'===subTableType">
        <a-tabs class="" :class="tabClass" v-model:activeKey="activeKey">
          <a-tab-pane v-for="(table,index) in localSubTables" :key="index" :tab="table.title" :forceRender="false">
            <component :autoHeight="autoHeight" :disabled="table.hasOwnProperty('tabDisabled')?(table.tabDisabled||disabled):disabled"
                       :formItemRequire="isFormItemRequire(actRuleArgs,table.key)"
                       :width="formFull?clientWidth:subTableWidth"
                       :parentId="table.parentId?table.parentId:(table.ignoreParent?'':subTableParentId)"
                       :parentFormData="localFormModel"
                       :parentActRuleArgs="actRuleArgs"
                       :extendSubTableData="table.extendSubTableData"
                       v-bind="extendFormData"
                       :is="table.component"></component>
          </a-tab-pane>
        </a-tabs>
      </template>
    </div>
    <div class="u-form-container" :class="[extendClass]" :id="'u-form-container'+id" v-if="'anchor'===subTableType&&localSubTables.length>0">
      <div :class="['anchor-container','anchor-' + anchorLocation]" :style="{height: tabPageHeight + 'px'}">
        <div class="anchor-area" :style="{ width : anchorWidth + 'px'}">
          <a-anchor @click="handleAnchorClick" :getContainer="getContainer" :get-current-anchor="getCurrentAnchor">
            <a-anchor-link v-show="mainTableVisible" :href="mainAnchor" :title="mainTableTitle"/>
            <template v-bind:key="index" v-for="(table,index) in localSubTables">
              <a-anchor-link :href="id+'-sub-table-'+index" :title="table.title"/>
            </template>

          </a-anchor>
        </div>
        <div class="form-area" :id="'form-area-'+id">
          <div :id="mainAnchor" v-show="mainTableVisible" :style="{height:tabPageHeight-40+'px'}">
            <div :style="{height:tabPageHeight-40+'px',background: '#ffffff',padding: '10px'}">
              <a-form :id="id" ref="form" :model="localFormModel" :labelCol="{ style: { width: labelWidth+'px' } }"
                      :hideRequiredMark="disabled||hideRequiredMark">
                <slot></slot>
              </a-form>
            </div>
          </div>
          <div :id="id+'-sub-table-'+index" v-bind:key="index" :style="{height:tabPageHeight-40+'px'}" v-for="(table,index) in localSubTables">
            <component :style="{height:tabPageHeight-40+'px',background: '#ffffff'}" :disabled="table.hasOwnProperty('tabDisabled')?(table.tabDisabled||disabled):disabled"
                       :formItemRequire="isFormItemRequire(actRuleArgs,table.key)"
                       :width="formFull?clientWidth:subTableWidth"
                       :parentId="table.parentId?table.parentId:(table.ignoreParent?'':subTableParentId)"
                       :parentFormData="localFormModel"
                       :parentActRuleArgs="actRuleArgs"
                       :extendSubTableData="table.extendSubTableData"
                       v-bind="extendFormData"
                       :is="table.component"></component>
          </div>
        </div>
      </div>
    </div>
    <act-history-modal ref="actModal" :dialog-title="actLogButtonText">

    </act-history-modal>
  </a-spin>
</template>

<script>
export default {
  name: "UForm",
}
</script>
<script setup>
import {ref, inject, watch, computed, provide, getCurrentInstance, onMounted} from "vue";
import {getAction} from "@/api/action";
import {getData, isNotEmpty, removeEmptyStrings, scrollToDiv, UUID} from "@/lib/tools";
import {getDataUrl, saveDataUrl, saveDataAction} from "@/api/api";
import {isFormItemRequire, setSubTableFromActRuleArgs} from "@/lib/act/actForm";
import ActHistoryModal from "@/components/act/ActHistoryModal";
import {Modal} from "ant-design-vue";
import {buildTempSaveActData} from "@/lib/act/actList";
import store from "@/store";

let instance = getCurrentInstance();
let id = ref('form' + UUID())
let spinning = ref(false)
const form = ref();
let props = defineProps({
  value: {
    type: Object,
    default() {
      return {}
    }
  },
  actRuleArgs: {
    type: Object,
    default() {
      return {}
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  labelWidth: {
    type: Number,
    default: 150
  },
  formNo: {
    type: String,
    default: ''
  },
  parentFormNo: {
    type: String,
    default: ''
  },
  parentId: {
    type: String,
    default: ''
  },
  /**
   * 主键字段名，默认为id，可配置为其他名称如pk_id、row_id等
   */
  pkColumnName: {
    type: String,
    default: 'id'
  },
  url: {
    type: Object,
    default() {
      return {
        getData: getDataUrl,
        save: saveDataUrl
      }
    }
  },
  /**
   * 获取数据的方法
   */
  getDataAction: {
    type: Function,
    default: getAction
  },
  getDataKey: {
    type: [Array, String],
    default() {
      return ['data', 'data']
    }
  },
  mainTableTitle: {
    type: String,
    default: 'title'
  },
  mainTableTitleVisible: {
    type: Boolean,
    default: true
  },
  subTables: {
    type: Array,
    default() {
      return []
    }
  },
  tabClass: {
    type: String,
    default: ''
  },
  /**
   * 静态子表单 无论是否满足工作流等配置都会显示
   */
  prefixSubTables: {
    type: Array,
    default() {
      return []
    }
  },
  suffixSubTables: {
    type: Array,
    default() {
      return []
    }
  },
  subTableType: {
    type: String,
    default: 'top' //top| bottom | anchor
  },
  anchorWidth: {
    type: Number,
    default: 220
  },
  anchorLocation: {
    type: String,
    default: 'left' //left| right
  },
  maxHeight: {
    type: Boolean,
    default: true
  },
  isTabPageHeightCus: {
    type: Boolean,
    default: false
  },
  tabPageHeightCus: {
    type: Number,
    default: null
  },
  extendFormData: {
    type: Object,
    default() {
      return {}
    }
  },
  getExtendSaveData: {
    type: Function,
    default() {
      return {}
    }
  },
  getExtendSaveDataFuncStr: {
    type: String,
    default: ''
  },
  setFormInjectKey: {
    type: String,
    default: 'setFormRef'
  },
  extendFormFiled: {
    type: String,
    default: null
  },
  hideRequiredMark: {
    type: Boolean,
    default: false
  },
  ignoreFormData: {
    type: Boolean,
    default: false
  },
  extendClass: {
    type: String,
    default: ''
  },
  showActLog: {
    type: Boolean,
    default: false
  },
  actLogButtonText: {
    type: String,
    default: '流程跟踪'
  },
  mainTableVisible: {
    type: Boolean,
    default: true
  },
  // 子表添加前保存
  saveBeforeListAddItem: {
    type: Boolean,
    default: false
  },
  // 是否自动去除数据空字符
  whitespaceRemoval: {
    type: Boolean,
    default: false
  },
  colon: { // 表单label后是否带冒号
    type: Boolean,
    default: true,
  },
  autoHeight: {
    type: Boolean,
    default: true
  },
  onSaveSuccess: {
    type: Function,
    default: null
  },
})
const localFormModel = ref(props.value)

let localSubTables = ref(props.subTables)

let subTableParentId = computed(() => {
  return localFormModel.value.id
})

let subTableSingleTableRef = {}
let subTableSingleTableRefList = [];

const getSubTableSingleTableRef = () => {
  return subTableSingleTableRef
}

// 解决在有多个子表组件，但是都用的同一个表（表名）时，获取ref会被同名子表覆盖的问题
const getSubTableSingleTableRefList = () => {
  return subTableSingleTableRefList
}

provide('setTableRef', (formNo, tableData, methods) => {
  subTableSingleTableRef[formNo] = {tableData, methods}

  let existFlag = false;
  for (let s of subTableSingleTableRefList) {
    // 利用methods.loadData地址是否相同判断是否为同一个组件
    if(s.methods?.loadData === methods?.loadData){
      existFlag = true;
      break;
    }else {
      s.tableData = tableData;
    }
  }
  if(!existFlag){
    subTableSingleTableRefList.push({
      formNo, tableData, methods
    })
  }
});
let updateParentFormDataArr = []
provide('addParentFormDataFunc', (callback) => {
  updateParentFormDataArr.push(callback)
})

provide('beforeSingleTableViewAdd', (resolve, reject) => {
  if (props.saveBeforeListAddItem) {
    if (!localFormModel.value.id) {
      validateFields(null, true,false).then((data) => {
        let tempSaveActData = buildTempSaveActData(props.formNo, '');
        Object.assign(data, tempSaveActData);
        saveDataAction(props.formNo, data).then((res) => {
          localFormModel.value.id = res.data.entityId;
          resolve()
        });
      }).catch((err) => {
        console.log(err);
        if (props.subTableType !== 'bottom' && localSubTables.value.length > 0 && err.errorFields) {
          Modal.confirm({
            title: '添加失败',
            content: '请先将' + props.mainTableTitle + '填写完整',
            okText: '去填写',
            onOk: () => {
              return new Promise((resolve, reject) => {
                activeKey.value = 0
                resolve();
              })
            }
          })
          store.commit('setIsNotCanSaveAndComplete', false)

        }
        reject({msg: '主表信息未填写完全'});
      })
    } else {
      resolve();
    }
  } else {
    resolve();
  }
})

watch(() => props.value, () => {
  localFormModel.value = props.value
}, {immediate: true})

watch(() => props.subTables, () => {
  localSubTables.value = props.prefixSubTables.concat(props.subTables).concat(props.suffixSubTables)
}, {deep: true})

watch(() => props.actRuleArgs, () => {
  setSubTableFromActRuleArgs(props.actRuleArgs).then((_subTables) => {
    if (_subTables && _subTables.length > 0) {
      localSubTables.value = props.prefixSubTables.concat(_subTables).concat(props.suffixSubTables)
      if (props.actRuleArgs) {
        let formExtend = props.actRuleArgs.formExtend
        if (formExtend && formExtend['act.activetab']) {
          setActiveTabByKey(formExtend['act.activetab'])
        }
      }
      emits('afterConcatSubTables', localSubTables.value);
    }
  })
  localFormModel.value.actRuleArgs = props.actRuleArgs
}, {immediate: true})

const isPromise = (val) => {
  return val != null && typeof (val) === 'object' && typeof (val.then) === 'function'
};
//
let emits = defineEmits(['update:value', 'update:actRuleArgs', 'update:disabled', 'change', 'beforeChange', 'loadSuccess', 'afterConcatSubTables'])
/**
 * 校验表单
 * @param fileds 校验字段
 * @param isAutoSaveMainForm 是否是点击子表时自动保存主表的校验
 * @param mainTableValidFailedTriggerModal 主表校验失败时是否触发提示完善主表信息的弹窗
 * @param btn 用户点击的具体按钮
 * @returns {Promise<unknown>}
 */
const validateFields = (fileds = null, isAutoSaveMainForm = false, mainTableValidFailedTriggerModal = true,btn) => {
  return new Promise((resolve, reject) => {

    form.value.validateFields(fileds).then(values => {
      if (props.actRuleArgs) {
        values.actRuleArgs = props.actRuleArgs
      }
      if (localFormModel.value.id) {
        values.id = localFormModel.value.id
      }
      if (props.parentId) {
        values.parent = {id: props.parentId}
      }
      values = Object.assign(values, props.extendFormData)
      //现在singeTable支持行编辑 此处要检验子表的行数据 所以要改成异步 2023-11-06
      let promiseArr = []
      let keyArr = []
      if (!localFormModel.value.id) {
        for (const key in subTableSingleTableRef) {
          let subTableSingleTable = subTableSingleTableRef[key]
          promiseArr.push(new Promise((resolve, reject) => {
            subTableSingleTable.methods.getUnSaveRows(true).then((data) => {
              resolve(data)
            }).catch((err) => {
              reject(err)
            })
          }))
          keyArr.push(key)
          //values[key] = subTableSingleTable.methods.getUnSaveRows()
        }
      }
      /**
       * 保存前执行的方法
       */
      const next = () => {
        let saveData = {}
        //是否忽略表单数据 如果忽略的话 仅传出表单的id actRuleArgs parent
        if (!props.ignoreFormData) {
          Object.assign(saveData, localFormModel.value, values)
        } else {
          saveData = {id: localFormModel.value.id}
          if (props.actRuleArgs) {
            saveData.actRuleArgs = props.actRuleArgs
          }
          if (props.parentId) {
            values.parent = {id: props.parentId}
          }
        }

        //添加主键字段名配置（支持非id主键）
        if (props.pkColumnName && props.pkColumnName !== 'id') {
          saveData.sqlMap = saveData.sqlMap || {}
          saveData.sqlMap.pkColumnName = props.pkColumnName
        }

        //删除不需要的字段
        delete saveData['update_date']
        delete saveData['updateBy']
        delete saveData['create_date']
        delete saveData['createBy']
        //delete saveData['hasChildren']//保存时去掉 目前后台有bug FIXME 后台修复后去掉 2023-05-11 11:21:58
        delete saveData['children']//保存时去掉 目前后台有bug FIXME 后台修复后去掉 2023-05-11 11:21:58

        //如果有额外的保存数据 则合并 这个也可以做额外的校验
        if (props.getExtendSaveData || props.getExtendSaveDataFuncStr) {
          let data = getExtendSaveDataFunc('validateFields', isAutoSaveMainForm,btn);
          if (isPromise(data)) {
            data.then(extendSaveData => {
              Object.assign(saveData, extendSaveData)
              //resolve最终的数据
              resolve(saveData)
            }).catch((e) => {
              reject(e)
            })
          } else {
            Object.assign(saveData, data)
            //resolve最终的数据
            resolve(saveData)
          }
        } else {
          //resolve最终的数据
          resolve(saveData)
        }

      }
      if (promiseArr.length > 0) {
        //如果有子表的行数据未保存 则先获取子表的行数据
        Promise.all(promiseArr).then((resArr) => {
          resArr.forEach((res, index) => {
            values[keyArr[index]] = res
          })
          next()
        }).catch((err) => {
          reject(err)
        })
      } else {
        //没有子表的行数据未保存 直接执行下一步
        next()
      }

    }).catch(errorInfo => {
      console.error('errorInfo', errorInfo)
      if (props.subTableType !== 'bottom' && localSubTables.value.length > 0 && mainTableValidFailedTriggerModal) {
        Modal.confirm({
          title: '提示信息',
          content: '请先将' + props.mainTableTitle + '填写完整',
          okText: '去填写',
          onOk: () => {
            return new Promise((resolve, reject) => {
              activeKey.value = 0
              resolve();
            })
          }
        })
        store.commit('setIsNotCanSaveAndComplete', false)

      }
      reject(errorInfo)
    })
  })
}
/**
 * 获取form ref
 * @returns {any}
 */
const getForm = () => {
  return form.value
}
/**
 * 获取表单数据
 * @returns {Promise<unknown>}
 */
const getFormData = (allData = true) => {
  return new Promise((resolve,reject) => {
    let values = {}
    if (!props.ignoreFormData || allData) {
      Object.assign(values, props.extendFormData, localFormModel.value)
    } else {
      values = {id: localFormModel.value.id}
    }
    if (props.actRuleArgs) {
      values.actRuleArgs = props.actRuleArgs
    }
    delete values['update_date']
    delete values['updateBy']
    delete values['create_date']
    delete values['createBy']
    if (props.parentId) {
      values.parent = {id: props.parentId}
    }
    if (!localFormModel.value.id) {
      for (const key in subTableSingleTableRef) {
        let subTableSingleTable = subTableSingleTableRef[key]
        values[key] = subTableSingleTable.methods.getUnSaveRows()
      }
    }
    if (props.getExtendSaveData || props.getExtendSaveDataFuncStr) {
      let data = getExtendSaveDataFunc('getFormData');
      if (isPromise(data)) {
        data.then(extendSaveData => {
          Object.assign(values, extendSaveData)
          if (props.whitespaceRemoval) {
            // 自动去除数据空字符
            values = removeEmptyStrings( values );
          }
          resolve(values)
        }).catch((e) => {
          reject(e)
        })
      } else {
        Object.assign(values, data)
        if (props.whitespaceRemoval) {
          // 自动去除数据空字符
          values = removeEmptyStrings( values );
        }
        resolve(values)
      }
    } else {
      if (props.whitespaceRemoval) {
        // 自动去除数据空字符
        values = removeEmptyStrings( values );
      }
      resolve(values)
    }
  })
}
/**
 * 获取额外的保存数据
 * @param type validateFields getFormData
 * @param isAutoSaveMainForm 是否是点击子表时自动保存主表的校验
 * @param btn 用户点击的具体按钮
 * @return {{}|*}
 */
const getExtendSaveDataFunc = (type, isAutoSaveMainForm = false,btn) => {
  if (props.getExtendSaveDataFuncStr) {
    try {
      return new Function('option', props.getExtendSaveDataFuncStr)({
        type, isAutoSaveMainForm
      })
    } catch (e) {
      console.error(e)
    }
    return {}
  }
  if (props.getExtendSaveData) {
    return props.getExtendSaveData(type, isAutoSaveMainForm,btn)
  }
  return {}
}
/**
 * 设置数据
 * @param data
 * @param disabled
 * @param actRuleArgs
 */
const setData = (data, disabled = false, actRuleArgs, callback) => {
  emits('beforeChange', data)
  emits('update:disabled', disabled)
  if (data.id && props.formNo !== '' && !data.__temp_id__) {
    spinning.value = true
    let getDataParam = {formNo: props.formNo, id: data.id}
    //非id主键时，传递pkColumnName
    if (props.pkColumnName && props.pkColumnName !== 'id') {
      getDataParam.sqlMap = {pkColumnName: props.pkColumnName}
    }
    props.getDataAction(props.url.getData, getDataParam).then(res => {
      var tempData = getData(res, props.getDataKey);
      if ( isNotEmpty(tempData) ){
        localFormModel.value = tempData
      }else{
        localFormModel.value = Object.assign(localFormModel.value, data);
      }
      emits('update:value', localFormModel.value)
      updateParentFormDataArr.forEach(func => {
        func(localFormModel.value)
      })
      if (data.actRuleArgs) {
        emits('update:actRuleArgs', data.actRuleArgs)
      }
      emits('change', localFormModel.value)
      emits('loadSuccess', res)
    }).finally(() => {
      spinning.value = false
      callback && callback()
    })
  } else {
    Object.assign(localFormModel.value, data);
    emits('update:value', localFormModel.value)
    updateParentFormDataArr.forEach(func => {
      func(localFormModel.value)
    })
    if (data.actRuleArgs) {
      emits('update:actRuleArgs', data.actRuleArgs)
    }
    emits('loadSuccess', {})
    callback && callback()
  }
}
let formFull = ref(false)
let formHeightCurrent = ref(null)
//子表开始
let activeKey = ref(0)
watch(() => props.mainTableVisible, (val) => {
  if (val) {
    activeKey.value = 0
  } else {
    if (props.subTableType === 'top') {
      activeKey.value = 1
    } else {
      activeKey.value = 0
    }
  }
}, {immediate: true})

/**
 * 根据key设置当前激活的tab 主要提供工作流的act.activetab设置
 * @param keyStr
 */
const setActiveTabByKey = (keyStr) => {
  localSubTables.value.forEach((item, index) => {
    if (item.key === keyStr) {

      if (props.subTableType === 'top') {
        activeKey.value = index + 1
      } else {
        activeKey.value = index
      }

    }
  })
}

const setActiveKey = (k) => {
  activeKey.value = k;
}
// 30 内容padding累计, 标题栏高度22, 标题栏padding高度16,标题栏底部横线1,底部高度32,底部padding高度20,底部top横线1
let titleHeight = 22 + 16 + 1
let bottomHeight = 32 + 20 + 1
let antTapHeight = 46
// 计算内容区高度
let diffHeight = computed(() => {
  let height = titleHeight
  // 判断有没有使用tab选项卡页面
  if ('top'===props.subTableType&&localSubTables.value.length>0) {
    height = height + antTapHeight
  }
  height = height + bottomHeight
  return height
})
let tabPageHeight = computed(() => {
  if (props.isTabPageHeightCus){
    return props.tabPageHeightCus
  }
  console.log('formFull.value', formFull.value)
  if (formFull.value && document.getElementById('u-form-container' + id.value)) {
    if (props.subTableType === 'anchor') {
      return document.getElementById('u-form-container' + id.value).clientHeight
    }
    //弹窗高度 - 选项卡高度
    return document.getElementById('u-form-container' + id.value).clientHeight - 46
  }
  if (formHeightCurrent.value) {
    //设置表单高度
    return formHeightCurrent.value
  }
    console.log('document.body.clientHeight', document.body.clientHeight)
    console.log('document.body.clientHeight', diffHeight.value)
  return document.body.clientHeight * 0.75 - diffHeight.value
})
let clientWidth = ref(document.body.clientWidth - 24 - 20)
let subTableWidth = computed(() => {
  if (document.getElementById('u-form-container' + id.value)) {
    return document.getElementById('u-form-container' + id.value).clientWidth
  }
  return 1200
})
//子表结束

//提供给provide的方法
let setFormRef = () => {
}
setFormRef = props.setFormInjectKey && inject(props.setFormInjectKey); // inject的参数为provide过来的名称
setFormRef && setFormRef({
  validateFields: validateFields,
  saveUrl: props.url.save,
  formNo: props.formNo,
  parentFormNo: props.parentFormNo,
  setData: setData,
  onSaveSuccess: props.onSaveSuccess,
  getFormData: getFormData,
  extendFormFiled: props.extendFormFiled,
  subTable: props.parentFormNo !== '',
  getId: () => {
    subTableParentId.value
  }
}, ({full, formHeight}) => {
  if (full) {
    formFull.value = true
  }
  if (formHeight) {
    formHeightCurrent.value = formHeight
  }
})

const resetTableRef = () => {
  subTableSingleTableRef = {}
}
/**
 * 导出供外部使用
 */
defineExpose({
  validateFields, getFormData, setData, getForm, getSubTableSingleTableRef,getSubTableSingleTableRefList, setActiveKey, resetTableRef
})

/**
 * 显示工作流日志
 */
const showActLogModal = () => {
  instance.refs.actModal.open(localFormModel.value.proc_ins_id)
}
let mainAnchor = id.value + '-main-table'
let currentAnchor = ref(props.mainTableVisible ? mainAnchor : id.value + '-sub-table-0')
const getCurrentAnchor = () => {
  return currentAnchor.value
};
const getContainer = () => {
  return document.getElementById('form-area-' + id.value);
}
let autoScroll = ref(false)
const handleAnchorClick = (e, link) => {
  e.preventDefault();
  currentAnchor.value = link.href
  autoScroll.value = true
  scrollToDiv('form-area-' + id.value, link.href, 500, () => {
    autoScroll.value = false
  })

}
const addAnchorFormAreaListener = () => {
  // 获取要监听滚动位置的元素
  let element = document.getElementById('form-area-' + id.value);
// 监听滚动事件
  element.addEventListener('scroll', function () {
    if (autoScroll.value) {
      return
    }
    // 获取元素的滚动位置
    let scrollTop = element.scrollTop;
    let index = scrollTop / tabPageHeight.value
    if (index < 0.5) {
      currentAnchor.value = mainAnchor
    } else {
      currentAnchor.value = id.value + '-sub-table-' + (index - 0.5).toFixed(0)
    }
  });
}
onMounted(() => {
  if ('anchor' === props.subTableType && localSubTables.value.length > 0) {
    addAnchorFormAreaListener()
  }
})
</script>

<style lang="less" scoped>
:deep( .ant-form-item-label > label) {
  white-space: pre-wrap;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  line-height: 1;
}

:deep( .multi-table-tabs) {

  /*height: calc(100vh - 160px);*/
}

:deep( .multi-table-tabs .ant-tabs-content) {

  /*height: 100%;*/
}

:deep( .multi-table-tabs .ant-tabs-content .ant-tabs-tabpane > div) {

  /*  height: 100%;*/
}

@import "@/assets/less/main.less";
.u-tabs {
  height: 100%;

  :deep(.ant-tabs-nav::before) {
    border: 0;
  }

  :deep( .ant-tabs-content) {
    height: 100%;

  }

  :deep( .ant-tabs-nav-list) {
    background: #FAFBFD;
    border: 0.6px solid #D5D5D5;
    border-radius: 12px;

    .ant-tabs-tab {
      padding: 6px 16px;
      font-family: @main-font-name;
      font-style: normal;
      font-weight: 700;
      font-size: @font-size-base;
      line-height: @font-size-base;
      margin: 0;
      border-bottom: 0;
    }

    .ant-tabs-tab:first-child.ant-tabs-tab-active {
      background: var(--ant-primary-color);
      border-radius: 12px 0 0 12px;

      .ant-tabs-tab-btn {
        color: #fff;
      }
    }

    .ant-tabs-tab.ant-tabs-tab-active {
      background: var(--ant-primary-color);
      border-radius: 0;

      .ant-tabs-tab-btn {
        color: #fff;
      }
    }

    .ant-tabs-tab:nth-last-child(2).ant-tabs-tab-active {
      background: var(--ant-primary-color);
      border-radius: 0 12px 12px 0;

      .ant-tabs-tab-btn {
        color: #fff;
      }
    }

    .ant-tabs-ink-bar {
      height: 0;
    }
  }
}

.anchor-container {
  display: flex;

  &.anchor-left {

  }

  &.anchor-right {

  }

  .anchor-area {
    padding: 20px;
    border-right: 2px solid #e3e3e3;
    background: #f5f5f5;
  }

  .form-area {
    flex: 1;
    overflow-y: auto;
    height: 100%;

    > div {
      background: var(--ant-primary-1);
      padding: 20px 10px;
      box-sizing: content-box;
    }
  }
}

</style>
