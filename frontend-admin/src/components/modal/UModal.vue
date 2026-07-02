<template>

  <a-modal :width="isFull?'100%':Number(width)"
           :wrap-class-name="['u-modal-container',(helpVisible?'help-container-show-modal':''),(feedbackVisible?'feedback-container-show-modal':''),(isFull?'full-modal':''),(modalProps.showFooter?'':'no-footer'),mask?'':'no-mask'].join(' ')"
           v-model:visible="visible"
           :maskClosable="false"
           :mask="mask"
           :confirm-loading="confirmLoading"
           :destroyOnClose="true"
           :closable="closable"
           :body-style="isFull?null:customBodyStyle?customBodyStyle:bodyStyle"
           :getContainer="getContainer"
           v-bind="modalProps"
           :keyboard="keyboard"
           @cancel="onCancel">
    <template #title>
      <div ref="modalTitleRef" :style="{width: '100%',cursor: isFull || !isMoveModal ? 'auto' : 'move' }" v-html="localTitle"></div>
    </template>
    <template #modalRender="{ originVNode }">
      <component :is="originVNode" :style="transformStyle" ref="modalRef"/>
    </template>
    <template #footer>
      <a-button v-if="closeShowLeft&&formDisabled&&showCancel" @click="close">关闭</a-button>
      <a-button v-if="closeShowLeft&&!formDisabled&&showCancel" @click="close">关闭</a-button>
      <a-button v-if="!formDisabled&&showOk" type="primary" :loading="confirmLoading" @click="handleOk(false)">{{ okText }}</a-button>
      <a-button v-if="!formDisabled&&showTempSave" :loading="confirmLoading" @click="handleOk(true)">{{ tempSaveText }}</a-button>

      <template v-bind:key="btn.value" v-for="btn in localExtendButtons">
        <a-button :danger="btn.danger" :type="btn.type||''"
                  v-if="formDisabled?btn.disableShow:true"
                  :disabled="btn.disabled?btn.disabled:false"
                  :style="btn.bgcolor||btn.color?`background-color: ${btn.bgcolor?btn.bgcolor:''}; border: 0px; color: ${btn.color?btn.color:''};`:''"
                  :loading="btnLoading[btn.value]" @click="handleOperate(btn)">{{ btn.text }}
        </a-button>
      </template>
      <a-button v-if="!closeShowLeft&&formDisabled&&showCancel" @click="close">关闭</a-button>
      <a-button v-if="!closeShowLeft&&!formDisabled&&showCancel" @click="close">关闭</a-button>
    </template>
    <a-spin v-if="showSpin" :spinning="spinning">
      <component ref="modalComponentRef" @clearExtendFormRef="clearExtendFormRef" :parentId="parentId" v-if="component" :is="component"
                 :extendFormData="extendFormData"/>
      <slot></slot>
    </a-spin>
    <template v-else>
      <component ref="modalComponentRef" @clearExtendFormRef="clearExtendFormRef" :parentId="parentId" v-if="component" :is="component"
                 :extendFormData="extendFormData"/>
      <slot></slot>
    </template>
  </a-modal>
</template>

<script>
export default {
  name: "UModal"
}
</script>
<script setup>
import {ref, provide, nextTick, computed, watch, watchEffect, onUnmounted, getCurrentInstance} from "vue";
import {postAction} from "@/api/action";
import {useStore} from "vuex";
import {getZIndex} from "@/lib/zIndexUtil";
import {message} from "ant-design-vue";
import {useDraggable} from "@vueuse/core";

let instance = getCurrentInstance();

const store = useStore();
let helpVisible = computed(() => store.getters.getHelpVisible)
let feedbackVisible = computed(() => store.getters.getFeedbackVisible)
let emits = defineEmits(['saveSuccess', 'clickOk', 'clickOperate', 'afterClose', 'onCancel','visibleChange'])
let bodyStyle = {
  maxHeight: '75vh',
  overflow: 'auto'
}
let props = defineProps({
  component: {
    type: Object,
    default: null
  },
  extendFormData: {
    type: Object,
    default() {
      return {}
    }
  },
  action: {
    type: [Object, Function],
    default() {
      return postAction
    }
  },
  title: {
    type: String,
    default: 'title'
  },
  width: {
    type: [Number, String],
    default: 1200
  },
  showOk: {
    type: Boolean,
    default: true
  },
  showCancel: {
    type: Boolean,
    default: true
  },
  okText: {
    type: String,
    default: '确定'
  },
  /**
   * 是否显示暂存按钮
   */
  showTempSave: {
    type: Boolean,
    default: false
  },
  /**
   * 暂存按钮的文字
   */
  tempSaveText: {
    type: String,
    default: '暂存'
  },
  /**
   * 是否暂存字段
   */
  tempSaveField: {
    type: String,
    default: 'is_temp_save'
  },
  /**
   * 暂存数据改为非暂存的值
   */
  unTempSaveValue: {
    type: String,
    default: '0'
  },
  /**
   * 暂存后关闭弹窗
   */
  closeAfterTempSave: {
    type: Boolean,
    default: false
  },
  extendButtons: {
    type: Array,
    default() {
      return []
    }
  },
  customOK: {
    type: Boolean,
    default: false
  },
  full: {
    type: [String, Boolean],
    default: false
  },
  parentId: {
    type: String,
    default: ''
  },
  noFooter: {
    type: Boolean,
    default: false
  },
  closable: {
    type: Boolean,
    default: true
  },
  mask: {
    type: Boolean,
    default: true
  },
  customBodyStyle: {
    type: Object,
    default: () => {
      return null
    }
  },
  setFormRefProvideKey: {
    type: String,
    default: 'setFormRef'
  },
  showSpin: {
    type: Boolean,
    default: true
  },
  autoClose: {
    type: Boolean,
    default: true
  },
  closeShowLeft: {
    type: Boolean,
    default: true
  },
  keyboard: {
    type: Boolean,
    default: true//是否支持键盘 esc 关闭
  },
  isOpenActSaveVerify: {
    type: Boolean,
    default: false
  },
  isMoveModal: {// 是否支持拖动
    type: Boolean,
    default: true
  },
  fullModalStyles: {
    type: Object,
    default: () => {
      return {'--overflow-type': 'auto'}
    }
  }
})
const modalTitleRef = ref();
const modalRef = ref();
const startX = ref(0);
const startY = ref(0);
const startedDrag = ref(false);
const transformX = ref(0);
const transformY = ref(0);
const preTransformX = ref(0);
const preTransformY = ref(0);
const dragRect = ref({left: 0, right: 0, top: 0, bottom: 0});
const {x, y, isDragging} = useDraggable(modalTitleRef);
const modalRefWidth = ref(0);
const modalRefHeight = ref(0);
// 监听元素的尺寸变化函数
const resizeObserver = new ResizeObserver((entries) => {
  const entry = entries[0];
  modalRefWidth.value = entry.contentRect.width;
  modalRefHeight.value = entry.contentRect.height;
  // console.log("弹框高度宽度变化：",modalRefWidth.value,modalRefHeight.value)
});
watch([x, y], () => {
  if (!startedDrag.value) {
    startX.value = x.value;
    startY.value = y.value;
    const bodyRect = document.body.getBoundingClientRect();
    const titleRect = modalTitleRef.value.getBoundingClientRect();
    dragRect.value.right = bodyRect.width - titleRect.width;
    dragRect.value.bottom = bodyRect.height - titleRect.height;
    preTransformX.value = transformX.value;
    preTransformY.value = transformY.value;
  }
  startedDrag.value = true;
});
watch([modalRefWidth, modalRefHeight], (value, oldValue) => {
  // 如果 弹框高度或者宽度变化，则 直接重置为默认位置
  transformX.value = 0;
  transformY.value = 0;
  // 此处可，重算  startX startY dragRect preTransformX preTransformY transformX transformY 为根据高度自适应当前显示位置，如需修改，请注意head需要保留能显示，不然将无法改变弹框位置
}, {deep: true});
watch(isDragging, () => {
  if (!isDragging) {
    startedDrag.value = false;
  }
});
watchEffect(() => {
  // 如果是在拖动 并且已开启拖动 并且 非全屏状态，可进行拖动，否则不可拖动
  if (startedDrag.value && props.isMoveModal && !isFull.value) {
    transformX.value = preTransformX.value +
        Math.min(Math.max(dragRect.value.left, x.value), dragRect.value.right) -
        startX.value;
    transformY.value = preTransformY.value +
        Math.min(Math.max(dragRect.value.top, y.value), dragRect.value.bottom) -
        startY.value;
  }
});
const transformStyle = computed(() => {
  return {
    transform: `translate(${transformX.value}px, ${transformY.value}px)`,
  };
});

let isFull = computed(() => {
  if (typeof props.full === 'string') {
    return props.full === '1' || props.full === 'true'
  }
  return props.full
})
let localExtendButtons = computed(() => {
  let _extendButtons = []
  props.extendButtons.forEach((item) => {
    if (item.visibleFilter) {
      if (item.visibleFilter({row: formRow.value})) {
        _extendButtons.push(item)
      }
    } else {
      _extendButtons.push(item)
    }
  })
  return _extendButtons
})
let obj = {}
if (props.noFooter || (!props.showOk && !props.showCancel && !props.showTempSave && props.extendButtons.length === 0)) {
  obj.showFooter = false
} else {
  obj.showFooter = true
}
obj.zIndex = getZIndex()
let modalProps = ref(obj)

watch(()=>[props.noFooter,props.showOk,props.showCancel,props.extendButtons],()=>{
  if (props.noFooter||(!props.showOk&&!props.showCancel&&props.extendButtons.length===0)) {
    modalProps.value.showFooter = false
  }else{
    modalProps.value.showFooter = true
  }
})

let btnLoading = ref({})
let localTitle = ref(props.title)
let visible = ref(false)
let formDisabled = ref(false)
let confirmLoading = ref(false)
let spinning = ref(false)
let formRef = null
let formRefCallback = null
let extendFormRef = []

provide(props.setFormRefProvideKey, (_formRef, callback) => {
  formRef = _formRef
  formRefCallback = callback
});
provide('addExtendFormRef', (_formRef) => {
  extendFormRef.push(_formRef)
});
const clearExtendFormRef = () => {
  extendFormRef = []
};
const notifyFormSaveSuccess = (saveResult, submitData, options = {}) => {
  if (formRef && typeof formRef.onSaveSuccess === 'function') {
    Promise.resolve(formRef.onSaveSuccess(saveResult, submitData, options)).catch(error => {
      console.error('UForm onSaveSuccess failed', error)
    })
  }
}
provide('closeAndEmitSuccess', (_formRef) => {
  emits('saveSuccess')
  close()
});
/**
 *
 * @param tempSave 是否暂存
 */
const handleOk = (tempSave = false) => {
  if (props.customOK) {
    emits('clickOk', (action, url, data) => {
      spinning.value = true
      action(url, data).then(saveResult => {
        emits('saveSuccess', saveResult)
        close()
      }).finally(() => {
        spinning.value = false
      })
    })
    return
  }
  console.log(formRef, extendFormRef)

  //调用 form 的验证方法
  if (formRef != null && (formRef.validateFields || (tempSave && formRef.getFormData))) {
    confirmLoading.value = true;
    let children = []
    if (tempSave) {
      children = [formRef.getFormData()]
    } else {
      children = [formRef.validateFields()]
    }
    let childrenFormRef = []
    extendFormRef.forEach(form => {
      if (form.validateFields) {
        if (tempSave) {
          children.push(form.getFormData())
        } else {
          children.push(form.validateFields())
        }
        childrenFormRef.push(form)
      }
    })
    Promise.all(children).then(res => {
      if (formRef.subTable && !props.parentId) {
        console.log('没有主表,子表不存到数据库')
        let childrenData = [formRef.getFormData()]
        let formNos = [formRef.formNo]
        extendFormRef.forEach(form => {
          if (form.getFormData) {
            childrenData.push(form.getFormData())
            formNos.push(form.formNo)
          }
        })
        Promise.all(childrenData).then(res => {
          res.forEach((form, index) => {
            form.formNo = formNos[index]
          })
          if (res[0].__temp_id__) {
            message.success("修改成功");
            emits('saveSuccess', {type: 'update', data: res})
          } else {
            message.success("添加成功");
            emits('saveSuccess', {type: 'add', data: res})
          }

          close()
        }).finally(()=>{
          confirmLoading.value = false;
        })

      } else if (formRef.saveUrl && formRef.formNo && res.length > 0) {
        res[0].formNo = formRef.formNo
        res[0].parentFormNo = formRef.parentFormNo
        if (!tempSave) {
          //非暂存的 数据把是否暂存改为非暂存的值
          res[0][props.tempSaveField] = props.unTempSaveValue
        }
        spinning.value = true
        confirmLoading.value = true;
        let action = props.action
        action(formRef.saveUrl, res[0]).then(saveResult => {
          //触发保存成功
          if (extendFormRef.length > 0) {
            let formIndex = 1
            let actions = []
            childrenFormRef.forEach(form => {
              res[formIndex].formNo = form.formNo
              res[formIndex].parentFormNo = form.parentFormNo
              if (form.extendFormFiled) {
                res[formIndex][form.extendFormFiled] = saveResult.data.entityId
              }
              actions.push(action(form.saveUrl, res[formIndex]))
              formIndex++;
            })
            Promise.all(children).then(childRes => {
              notifyFormSaveSuccess(saveResult, res[0], {tempSave, childRes})
              emits('saveSuccess', {saveResult, childRes})
              if (!tempSave || props.closeAfterTempSave) {
                close()
              }
            }).catch(() => {
              spinning.value = false
              confirmLoading.value = false;
            })

          } else {
            if(tempSave && saveResult?.data?.entityId){
              formRef?.setData({
                id: saveResult.data.entityId
              }, false, {}, () => {
                //暂存的再次查询数据完成后关闭spinning
                spinning.value = false
              })
            }
            notifyFormSaveSuccess(saveResult, res[0], {tempSave})
            emits('saveSuccess', saveResult)
            if (!props.autoClose) {
              if (saveResult.code === 0) {
                message.success(saveResult.msg)
              } else {
                message.error(saveResult.msg)
              }
            } else if (!tempSave || props.closeAfterTempSave) {
              if(tempSave){
                message.success("暂存成功");
              }else{
                if(res[0].id){
                  message.success("修改成功");
                }else{
                  message.success("添加成功");
                }
              }
              close()
            }
          }

        }).finally(() => {
          if(!tempSave){
            //暂存的不在这里关闭 等再次查询数据完成后再关闭
            spinning.value = false
          }
          confirmLoading.value = false;
        })
      }
    }).catch(err => {
      confirmLoading.value = false;
    })
  } else {
    emits('saveSuccess')
    close()
  }
};

const isProcessingHandleSelectUser = ref(false)

function editIsProcessingHandleSelectUser (val){
  isProcessingHandleSelectUser.value = val
}

const getModalComponentRef = () =>{
  return instance.refs.modalComponentRef
}

const handleOperate = (item) => {

  // region 避免工作流提交按钮 重复提交
  if ((item.value === 'saveAndComplete' || item.value === 'saveAndStart') && props.isOpenActSaveVerify) {
    if (isProcessingHandleSelectUser.value){
      console.warn('isProcessingHandleSelectUser.value === true 防止重复提交生效中！！！')
      return;
    }
    store.commit('setIsNotCanSaveAndComplete', true)
    isProcessingHandleSelectUser.value = true
  }
  // endregion

  function exec(data, dataSimple) {
    emits('clickOperate', item, (action, url, data, closeModal = true) => {
      btnLoading.value[item.value] = true
      spinning.value = true
      action(url, data).then(saveResult => {
        emits('saveSuccess', saveResult)
        if (closeModal) {
          close()
        }
      }).finally(() => {
        btnLoading.value[item.value] = false
        spinning.value = false
      })
    }, data, dataSimple)
  }

  if (item.simpleButton) {
    emits('clickOperate', item, () => {
      close()
    });
    return;
  }
  if (item.validate) {
    let children = [() => formRef.validateFields(null,false,true,item)]
    let children2 = [() => formRef.getFormData(true)]
    let formNos = [formRef.formNo]
    let childrenFormRef = []
    extendFormRef.forEach(form => {
      if (form.validateFields) {
        children.push(() => form.validateFields())
        children2.push(() => form.getFormData(true))
        formNos.push(form.formNo)
        childrenFormRef.push(form)
      }
    })
    Promise.all(children.map(item => item())).then(res => {
      res.forEach((form, index) => {
        if (formNos[index]) {
          form.formNo = formNos[index]
        }
      })
      Promise.all(children2.map(item => item())).then(res2 => {
        res2.forEach((form, index) => {
          if (formNos[index]) {
            form.formNo = formNos[index]
          }
        })
        exec(res2, res)
      })
    })
  } else {
    let children = [() => formRef.getFormData()]
    let children2 = [() => formRef.getFormData(true)]
    let formNos = [formRef.formNo]
    let childrenFormRef = []
    extendFormRef.forEach(form => {
      if (form.getFormData) {
        children.push(() => form.getFormData())
        children2.push(() => form.getFormData(true))
        formNos.push(form.formNo)
        childrenFormRef.push(form)
      }
    })
    Promise.all(children.map(item => item())).then(res => {
      res.forEach((form, index) => {
        if (formNos[index]) {
          form.formNo = formNos[index]
        }
      })
      Promise.all(children2.map(item => item())).then(res2 => {
        res2.forEach((form, index) => {
          if (formNos[index]) {
            form.formNo = formNos[index]
          }
        })
        exec(res2, res)
      })
    })

  }

}

let parentFormData = ref({})
let formRow = ref({})
const open = (title, row = {}, disabled = false, callback = null) => {
  parentFormData.value = row
  modalProps.value.zIndex = getZIndex()
  extendFormRef = []
  if (title) {
    localTitle.value = title
  }
  formRow.value = row
  visible.value = true
  formDisabled.value = disabled
  nextTick(() => {
    // 开启当前弹框显示宽高监控
    resizeObserver.observe(modalRef.value);
    if (row && formRef && formRef.setData) {
      formRef.setData(row, disabled, {}, () => {
        formRefCallback && formRefCallback({full: props.full})
        callback && callback()
      })
    } else {
      //formRef.setData({}, disabled)
      formRefCallback && formRefCallback({full: props.full})
      callback && callback()
    }
  })
}
const close = () => {
  visible.value = false;
  // 关闭当前弹框显示宽高监控
  resizeObserver?.disconnect();
  // 重置默认偏移位置，默认为0,0
  transformX.value = 0;
  transformY.value = 0;
  emits('afterClose')
}
const onCancel = () => {
  emits('onCancel')
}
const getModalVisible = () => {
  return visible.value;
}
const getContainer = () => {
  return document.getElementById('modal-container') ?? document.body
}

const startLoading = () => {
  spinning.value = true
  confirmLoading.value = true
}
const endLoading = () => {
  spinning.value = false
  confirmLoading.value = false
}

watch(() => visible.value, (newValue) => {
  emits('visibleChange',newValue)
}, {deep: true, immediate: true})

watchEffect(() => {
  if (props.fullModalStyles?.['--overflow-type'] !== undefined) {
    document.documentElement.style.setProperty('--overflow-type', props.fullModalStyles['--overflow-type']);
  }
});
onUnmounted(() => {
  document.documentElement.style.setProperty('--overflow-type', 'auto');
})

defineExpose({
  open, close, getModalVisible, startLoading, endLoading,editIsProcessingHandleSelectUser,getModalComponentRef
})
</script>

<style lang="less">
.u-modal-container {
  overflow: hidden;
}

.full-modal {
  .ant-modal {
    max-width: 100%;
    top: 0;
    padding-bottom: 0;
    margin: 0;
  }

  .ant-modal-content {
    display: flex;
    flex-direction: column;
    height: calc(100vh);
  }

  .ant-modal-body {
    flex: 1;
    overflow: var(--overflow-type);

    .ant-spin-nested-loading, .ant-spin-container, .u-form-container {
      height: 100%;
    }
  }
}

.no-footer {
  .ant-modal-footer {
    display: none;
  }
}

.ant-modal-wrap.no-mask{
  pointer-events: none;
}
</style>
