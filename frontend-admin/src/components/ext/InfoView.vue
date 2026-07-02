<template>
  <div :style="{height:fullHeight?'100%':(height+'px'),overflowY:'auto',overflowX:'hidden'}">
    <component :is="currentModalComponent" :extendFormData="extendFormData"></component>
  </div>

</template>

<script>
export default {
  name: "InfoView"
}
</script>
<script setup>
import {watch, ref, nextTick, provide, computed} from "vue";
import {getAction, postAction} from "@/api/action";
// 30 内容padding累计, 标题栏高度22, 标题栏padding高度16,标题栏底部横线1,底部高度32,底部padding高度20,底部top横线1
// let diffHeight =  30 + 32 + 32 + 24 // 废弃
let titleHeight = 22 + 16 + 1
let bottomHeight = 32 + 20 + 1
let antTapHeight = 46
// 计算内容区高度
let diffHeight = computed(() => {
  let height = titleHeight
  if (props.antTapView) {
    height = height + antTapHeight
  }
  if (props.showBottom) {
    height = height + bottomHeight
  }
  return height
})
let height = computed(() => {
  // 获取ant-modal-content的高度
  const modalContent = document.querySelector(".ant-modal-content");
  if (!modalContent) {
    // 如果找不到，则使用默认值
    return document.body.clientHeight * 0.75 - diffHeight.value
  }
  return modalContent.clientHeight - diffHeight.value
})
let props = defineProps({
  id: {
    type: String,
    default: ''
  },
  formNo: {
    type: String,
    default: ''
  },
  module: {
    type: String,
    default: ''
  },
  fullHeight: {
    type: Boolean,
    default: false
  },
  // 是否有ant-tap切换tab功能(默认没有)
  antTapView: {
    type: Boolean,
    default: false
  },
  // 是否有底部按钮(默认有)
  showBottom: {
    type: Boolean,
    default: true
  }
})
let currentModalComponent = ref(null)
let currentProcRow = ref({})
let formRef = null
let formRefCallback = null

let extendFormData = ref({
  table: null,
  columns: [],
})

provide('setFormRef', (_formRef, callback) => {
  formRef = _formRef
  formRefCallback = callback
});
watch(() => props.id, (val) => {
  if (val === '') {
    return
  }
  if (props.formNo && props.module) {
    import(`@/views/${props.module}/${props.formNo}/form.vue`).then(res => {
      currentModalComponent.value = res.default
      queryFormData(props.id)
    }).catch(err => {
      postAction('gen/genTable/editForm', {name:props.formNo}).then(res => {
        if (res.data) {
          let table = res.data.genTable
          let extendJs = {}
          if (table.extendJs) {
            extendJs = JSON.parse(table.extendJs)
            delete table.extendJs
          }
          let allColumns = res.data.data
          if (extendJs.extendColumns) {
            extendJs.extendColumns.forEach(item => {
              if (item.listConfig) {
                allColumns.push(item)
              }
            })
          }
          extendFormData.value = {
            table: table,
            columns: allColumns,
          }
        }
        import(`@/components/dynamic/DynamicForm.vue`).then(res => {
          currentModalComponent.value = res.default
          queryFormData(props.id)
        })
      })

    })
  }

}, {immediate: true})

const queryFormData = (id) => {
  getAction(`dynamic/zform/getZformMap?formNo=${props.formNo}&id=${id}`, {}).then(actRes => {
    currentProcRow.value = actRes.data.data
    let row = {}
    Object.assign(row, currentProcRow.value)
    nextTick(() => {
      if (row && formRef && formRef.setData) {
        formRef.setData(row, true, {}, () => {
          console.log('formRefCallback', formRefCallback)
          // formRefCallback && formRefCallback({full: false,formHeight:height.value})
        })
      }
    })
  })
}
</script>
<style scoped>

</style>
