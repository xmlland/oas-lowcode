<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="ETL列表"
                   :class="useModernListSkin ? 'modern-list-page etl-info-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="etl_info">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">ETL列表</h2>
        <p class="modern-list-query-desc">维护数据抽取任务、执行频率、开启状态和异常状态。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="s01" label="ETL名称" type="input" :props="{placeholder:'ETL名称' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "etl_info_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/etl_info/form";
import config from "@/config";
let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:etl_info:add'
}, {
  value: 'import',
  text: '导入数据',
  permission: 'app:etl_info:import'
}, {
  value: 'export',
  text: '导出Excel',
  permission: 'app:etl_info:export'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:etl_info:remove'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:etl_info:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:etl_info:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:etl_info:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: 'ETL名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '执行频率', dataIndex: 'internal', minWidth: 120, align: 'center', dict: 'etl_internal'},
    {title: '异常状态', dataIndex: 'status', minWidth: 120, align: 'left'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'left'},
    {title: '是否开启', dataIndex: 'open_', minWidth: 120, align: 'center', dict: 'yes_no'},
  ]
})

</script>
<style scoped>
</style>
