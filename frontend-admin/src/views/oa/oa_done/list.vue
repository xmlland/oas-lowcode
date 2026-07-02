<template>
  <left-tree-right-table-view :rightTableView="rightTableView" modalTitle="已办" treeFormNo="oa_process_tree_setting"
                              :treeWidth="useModernListSkin ? 260 : 300"
                              :class="useModernListSkin ? 'modern-left-tree-right-page oa-done-ltr-view' : ''">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">已办任务</h2>
        <p class="modern-list-query-desc">查询已处理流程事项，跟踪流程类型、发起人、当前节点和接收时间。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="s02" label="发起人" type="input" :props="{placeholder:'发起人' }"></QueryField>
    </template>
  </left-tree-right-table-view>
</template>

<script>
export default {
  name: "oa_done_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import config from "@/config";

const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})

let rightTableView=ref({
  commonActTaskList: true,
  autoHeight: useModernListSkin.value,
  queryArea: useModernListSkin.value ? { resetButton: true } : {},
  url: {
    list: 'dynamic/zform/getTaskList?path=done',
  },
  buttons: [],
  singleTable:{
    idField:['map','ENTITY_ID'],
    rowButtons: [{
      value: 'viewAct',
      text: '查看',
    }],
    columns: [
      {title: '流程类型', dataIndex:['map','PROC_CATEGORY'], dict:'act_category', minWidth: 120, align: 'left'},
      {title: '发起人', dataIndex: ['map','PROC_CREATE_USER'], minWidth: 120, align: 'left'},
      {title: '发起时间', dataIndex: ['map','PROC_CREATE_TIME'], minWidth: 120, align: 'center'},
      {title: '当前节点', dataIndex: ['map','TASK_NAME'], minWidth: 120, align: 'left'},
      {title: '接收时间', dataIndex: ['map','TASK_CREATE_TIME'], minWidth: 120, align: 'center'},
    ],
    initParam:{
      "parent": {"id": ""},
    }
  }
})

</script>
<style scoped>
</style>
