<template>
  <dynamic-list :table="table" :columns="columns" :extend-js="extendJs"/>
</template>

<script>
export default {
  name: "DynamicMockHome"
}
</script>
<script setup>

import DynamicList from "@/components/dynamic/DynamicList";
import {ref} from "vue";

let table = ref(null)
let columns = ref([])
let extendJs = ref(null)
window.opener.addEventListener('acceptDataFormParent', (data) => {
  console.log('监听到了', data, data.detail)
  table.value = data.detail.table
  columns.value = data.detail.data.json
  extendJs.value = data.detail.data.extendJs
})
</script>
<style scoped>

</style>
