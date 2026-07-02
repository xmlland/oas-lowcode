<template>
  <left-tree-right-table-view :rightTableView="rightTableView" treeFormNo="oa_process_tree_setting"
                              :treeWidth="useModernListSkin ? 260 : 300"
                              :class="useModernListSkin ? 'modern-left-tree-right-page oa-todoanddoing-ltr-view' : ''">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">待办任务</h2>
        <p class="modern-list-query-desc">按流程分类查看待办理事项，快速定位发起人、发起时间和当前节点。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="pageParam.paramMap.PROC_CREATE_USER" label="发起人" type="input" :props="{placeholder:'发起人' }"></QueryField>
      <QueryField name="pageParam.paramMap.PROC_CREATE_TIME_BEGIN" label="发起时间" type="date"
                  :props="{formatPatter:'yyyy-MM-dd'}"></QueryField>
      <QueryField name="pageParam.paramMap.PROC_CREATE_TIME_END" label="-" type="date" :props="{formatPatter:'yyyy-MM-dd'}"></QueryField>
    </template>
  </left-tree-right-table-view>
</template>

<script>
export default {
  name: "oa_todoanddoing_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import config from "@/config";

const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})

let rightTableView = ref({
  commonActTaskList: true,
  formNo: 'oa_urge_setting',
  autoHeight: useModernListSkin.value,
  queryArea: useModernListSkin.value ? { resetButton: true } : {},
  url: {
    list: 'dynamic/zform/getTaskList?path=todoanddoing',
  },
  buttons: [],
  singleTable: {
    idField: ['map', 'ENTITY_ID'],
    rowButtons: [{
      value: 'viewAct',
      text: '详情',
    }, {
      value: 'editAct',
      text: '办理',
    }],
    columns: [
      {title: '流程类型', dataIndex: ['map', 'PROC_CATEGORY'], dict: 'act_category', minWidth: 120, align: 'left'},
      {title: '发起人', dataIndex: ['map', 'PROC_CREATE_USER'], minWidth: 120, align: 'left'},
      {title: '发起时间', dataIndex: ['map', 'PROC_CREATE_TIME'], minWidth: 120, align: 'center'},
      {title: '当前节点', dataIndex: ['map', 'TASK_NAME'], minWidth: 120, align: 'left'},
      {title: '接收时间', dataIndex: ['map', 'TASK_CREATE_TIME'], minWidth: 120, align: 'center'},
    ],
    initParam: {
      "parent": {"id": ""},
    }
  }
})

</script>
<style scoped>
</style>
