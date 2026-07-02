<template>
  <single-table-view ref="tableView" :single-table="singleTable" :buttons="buttons" :custom-handle-query="true" v-bind="customSingleTableViewProps">
    <template #queryFields>
      <template v-bind:key="item.name" v-for="item in mockColumns">
        <dynamic-query-field v-if="item.isQuery==='1'" :column="item"/>
      </template>
    </template>
  </single-table-view>
</template>

<script>
export default {
  name: "formDesignMockList"
}
</script>
<script setup>
import {ref, computed, watch} from "vue";
import SingleTableView from "@/components/view/SingleTableView";
import DynamicQueryField from "@/components/dynamic/DynamicQueryField";

let props = defineProps({
  mockButtons: {
    type: Array,
    default() {
      return []
    }
  },
  mockSingleTable: {
    type: Object,
    default() {
      return {}
    }
  },
  mockColumns: {
    type: Array,
    default() {
      return []
    }
  },
  value: {
    type: Object,
    default: () => {
      return {}
    }
  },
})
let buttons = computed(() => {
  if (props.value.noOptButton === '1') {
    return []
  }
  return props.mockButtons.map((item) => {
    let obj = {...item}
    delete obj.permission//模拟显示 去掉权限 以便显示
    return obj
  })
})

let singleTable = computed(() => {
  let obj = {...props.mockSingleTable}
  if (props.value.noCheckbox === '1') {
    obj.rowSelection = false
  }
  if (props.value.noRowButton === '1') {
    obj.showRowButtons = false
  }
  if (obj.rowButtons) {
    obj.rowButtons = obj.rowButtons.map((item) => {
      let obj = {...item}
      delete obj.permission//模拟显示 去掉权限 以便显示
      return obj
    })
  }
  return obj
})

let customSingleTableViewProps = computed(()=>{
  return props.value.customSingleTableViewProps || {}
})
const tableView = ref(null)
watch(() => props.value, (newVal) => {
  if (tableView.value) {
    tableView.value.getTableRef().initTableLayout()
  }
}, {deep: true, immediate: true})
</script>
<style scoped>

</style>
