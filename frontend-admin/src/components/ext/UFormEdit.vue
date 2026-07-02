<template>
  <div class="form-edit-container" :class="'operation-'+location">
    <template v-if="empty">
      <a-empty/>
    </template>
    <template v-else>
      <div style=" height: calc(100% - 48px);overflow-y: auto;padding: 0 16px;">
        <a-spin size="large" :spinning="spinning">
          <component ref="orgInfoFrom" :is="component"/>
        </a-spin>
      </div>
      <div class="operation-area" v-if="currentOrg">
        <a-button v-if="saveButton" :loading="spinning" type="primary" @click="saveInfo">
          <save-outlined/>
          保存信息
        </a-button>
        <template v-if="$slots.extendOperation">
          <slot name="extendOperation"></slot>
        </template>
      </div>
    </template>
  </div>
</template>

<script>
export default {
  name: "UFormEdit"
}
</script>
<script setup>
import {onMounted, ref, computed, provide, watch} from "vue";
import {listDataAction, saveDataAction} from "@/api/api";
import {useStore} from "vuex";

let store = useStore()
let props = defineProps({
  component: {
    type: Object,
    default: null
  },
  formNo: {
    type: String,
    default: ''
  },
  orgIdField: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  location: {
    type: String,
    default: 'bottom'//top bottom
  },
  saveButton: {
    type: Boolean,
    default: true
  }
})

let userView = computed(() => store.getters.getUserView);

let spinning = ref(true)
let empty = ref(false)
let currentOrg = ref(false)

let formRef = null
provide('setFormRef', (_formRef, callback) => {
  formRef = _formRef
});
let emits = defineEmits(['loadSuccess'])

const loadData = () => {
  if (userView.value.company && userView.value.company.id) {

    let companyId = userView.value.company.id

    let param = {
      filterDataArr : [
        {
          key: props.orgIdField,
          value: companyId,
          type: 'eq',
        }
      ]
    }
    listDataAction(props.formNo, param).then(res => {
      if (res.rows && res.rows.length === 1 && formRef && formRef.setData) {
        currentOrg.value = res.rows[0][props.orgIdField].id === companyId
        //如果不是当前机构的信息，不允许编辑
        formRef.setData(res.rows[0], !currentOrg.value || props.disabled)
        endLoading()
        emits('loadSuccess', res.rows[0])
      } else {
        endLoading()
        empty.value = true
      }
    })
  } else {
    startLoading()
    empty.value = true
  }
}

watch(() =>  props.disabled, (newVal, oldVal) => {
  formRef.setData({}, newVal)
})

onMounted(() => {
  loadData()
})

const saveInfo = () => {

  if (formRef != null && formRef.validateFields) {
    startLoading();
    formRef.validateFields().then(res => {
      saveDataAction(props.formNo, res).then(saveResult => {
        console.log('saveResult', saveResult)
      }).finally(() => {
        endLoading()
      })
    }).catch(err => {
      endLoading()
    })
  }

}
const getFormRef = () => {
  return formRef
}

const startLoading = () => {
  spinning.value = true
}
const endLoading = () => {
  spinning.value = false
}
defineExpose({
  getFormRef, loadData, startLoading, endLoading
})
</script>
<style lang="less" scoped>
.form-edit-container {
  padding: 0 8px;
  background: #fff;
  height: 100%;
  display: flex;

  .operation-area {
    padding: 8px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: right;
  }

  &.operation-top {
    flex-direction: column-reverse;

    .operation-area {
      border-bottom: 1px solid #dee8ee;
    }
  }

  &.operation-bottom {
    flex-direction: column;

    .operation-area {
      border-top: 1px solid #dee8ee;
    }

  }
}

</style>
