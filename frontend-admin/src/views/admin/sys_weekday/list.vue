<template>
  <SingleTableView  :singleTable="singleTable" :modalWidth="800" :modalComponent="modalComponent" modalTitle="工作日"
                    :class="useModernListSkin ? 'modern-list-page sys-weekday-list-view' : ''"
                    :autoHeight="useModernListSkin"
                    :queryArea="queryArea"
                    :buttons="buttons"
                    formNo="sys_weekday">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">工作日管理</h2>
        <p class="modern-list-query-desc">维护年度工作日与非工作日数据，支持导入导出和日历类业务判断。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="year_" label="年度" type="date" :props="{formatPatter:'yyyy'}"></QueryField>
      <QueryField name="is_weekday" label="是否工作日" type="select" :props="{placeholder:'是否工作日',dictType:'yes_no'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_weekday_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_weekday/form";
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
  permission: 'app:sys_weekday:add',
}, {
  value: 'import',
  text: '导入数据',
  permission: 'app:sys_weekday:import',
}, {
  value: 'export',
  text: '导出Excel',
  permission: 'app:sys_weekday:export',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_weekday:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_weekday:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_weekday:edit',
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_weekday:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '年度', dataIndex: 'year_', minWidth: 120, align: 'center'},
    {title: '日期', dataIndex: 'date_', minWidth: 120, align: 'center'},
    {title: '是否工作日', dataIndex: 'is_weekday', minWidth: 120, align: 'center', dict: 'yes_no'},
  ]
})

</script>
<style scoped>
</style>
