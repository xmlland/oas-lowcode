<template>
  <u-modal :ref="'globalModalRef'+key" v-bind="value" v-for="(value, key) in globalModalProps" :key="key"></u-modal>
</template>

<script>
export default {
  name: "GlobalModal"
}
</script>
<script setup>

import UModal from "@/components/modal/UModal";
//全局事件
import globalEvent from "@/lib/globalEvent";
import {getCurrentInstance, ref} from "vue";
let instance = getCurrentInstance();
top.globalEvent = globalEvent

let globalModalProps = ref({})
//全局组件
top.globalRef = {
  //全局弹窗
  modal: {
    getRef: (id) => {
      return instance.refs['globalModalRef' + id][0]
    },
    /**
     * 设置属性
     * @param props
     */
    init: (props) => {
      let id = 'uModal' + new Date().getTime();
      globalModalProps.value[id] = props
      return id
    }
  }
}
</script>

<style scoped>

</style>
