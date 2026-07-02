<template>
  <div :style="{height:fullHeight?'100%':(height+'px'),overflowY:'auto',overflowX:'hidden'}">
    <DynamicListView v-if="table" :table="table" :columns="columns" :disabled="true" :parentId="parentId" :filterDataArr="filterDataArr"/>
  </div>
</template>

<script>
export default {
  name: "ListView"
}
</script>
<script setup>
import {ref, computed, watch} from "vue";
import {postAction} from "@/api/action";
import DynamicListView from "@/components/dynamic/DynamicListView";

let diffHeight = 30 + 32 + 32 + 24
let height = computed(() => {
  return document.body.clientHeight * 0.9 - diffHeight
})
let props = defineProps({
  formNo: {
    type: String,
    default: ''
  },
  parentId: {
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
  filterDataArr: {
    type: Array,
    default: () => {
      return []
    }
  },
  columnsDefaultValue: {
    type: Array,
    default: () => {
      return []
    }
  }
})
let table = ref(null)
let columns = ref([])
watch([() => props.formNo, () => props.parentId], (val) => {
  table.value = null
  postAction('gen/genTable/editForm', {name: props.formNo}).then(res => {
    let colums = res.data.data.map(item => {
      props.columnsDefaultValue.forEach(defaultItem => {
        if (item.name === defaultItem.name) {
          item.defaultValue = defaultItem.defaultValue
          let listConfigData = JSON.parse(item.listConfig)
          if (listConfigData) {
            listConfigData.queryDefaultValue = defaultItem.defaultValue
            item.listConfig = JSON.stringify(listConfigData)
          }
        }
      })
      return item
    })
    console.log('colums', colums)
    columns.value = colums
    table.value = res.data.genTable
  })
}, {immediate: true})
</script>
<style scoped>

</style>
