<template>
  <div :class="['dynamic-list-host', useModernListSkin ? 'dynamic-list-host-modern' : '']">
    <template v-bind:key="dynamic.id" v-for="(dynamic) in dynamicListArr">
      <dynamic-list-view v-show="currentKey===dynamic.id"
                         :table="dynamic.table"
                         :columns="dynamic.columns"
                         :pageMode="true"/>
    </template>
  </div>
</template>

<script>
export default {
  name: "DynamicList"
}
</script>
<script setup>
import {computed, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {useStore} from "vuex";
import {postAction} from "@/api/action";
import DynamicListView from "@/components/dynamic/DynamicListView";
import config from "@/config";

let props = defineProps({
  table: {
    type: Object,
    default: () => {
      return {}
    }
  },
  columns: {
    type: Array,
    default: () => {
      return []
    }
  },
})

let route = useRoute()
const store = useStore();
let currentKey = ref('')
let dynamicListArr = ref([])
let activeTabs = computed(() => store.getters.getActiveTabs)
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})

const loadConfig = (id) => {
  postAction('gen/genTable/editForm', {id}).then(res => {
    if (res.data) {
      dynamicListArr.value.push({
        id: id,
        table: res.data.genTable,
        columns: res.data.data,
      })
    }
  })
}

watch(() => [activeTabs.value, route], () => {
  let newKey = route.params.code;
  if (!newKey) {
    return
  }
  let isNew = true
  let currentTabs = activeTabs.value.map(item => '/' + item.key);
  let deleteKeys = []
  dynamicListArr.value.forEach((item, index) => {
    if (currentTabs.indexOf(item.id) >= 0) {
      deleteKeys.push(item.id)
    }
    if (item.id === newKey) {
      isNew = false
    }
  })
  deleteKeys.forEach(key => {
    deleteByKey(key)
  })
  if (route.meta.target && route.meta.target.indexOf('DynamicList') >= 0) {
    if (isNew) {
      loadConfig(newKey)
    }
    currentKey.value = newKey
  }

}, {immediate: true, deep: true})

let deleteByKey = (key) => {
  let deleteIndex = -1
  dynamicListArr.value.forEach((item, index) => {
    if (item.id === key) {
      deleteIndex = index
    }
  })
  if (deleteIndex > -1) {
    dynamicListArr.value.splice(deleteIndex, 1)
  }
}

watch(() => [props.table, props.columns], () => {
  if (!props.columns||props.columns.length===0){
    return
  }
  let table = props.table
  if (table && !table.id) {
    table.id = 'mock'
  }
  if (dynamicListArr.value.length > 0) {
    dynamicListArr.value[0].table = table
    dynamicListArr.value[0].columns = props.columns
  } else {
    dynamicListArr.value = [{id: table.id, table: table, columns: props.columns}]
    currentKey.value = table.id
  }

}, {immediate: true})
</script>
<style scoped>
.dynamic-list-host {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.dynamic-list-host-modern {
  height: auto;
  min-height: 100%;
  overflow: visible;
}
</style>
