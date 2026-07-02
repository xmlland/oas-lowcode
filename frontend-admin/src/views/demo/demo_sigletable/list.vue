<template>
  <SingleTableView :modalFull="0"
    :class="useModernListSkin ? 'modern-list-page demo_sigletable-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :queryButton="true" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="测试信息" :buttons="buttons" formNo="demo_sigletable" :modalWidth="1200" >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">测试单表</h2>
        <p class="modern-list-query-desc">维护测试单表数据，支持查询、批量操作和业务表单编辑。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="name" label="名称" type="input" :props="{placeholder:'名称' }"></QueryField>
      <QueryField name="explain_" label="说明" type="input" :props="{placeholder:'说明' }"></QueryField>
      <QueryField name="start_date" label="开始日期" type="date-range" :props="{formatPatter:'yyyy-MM-dd'}"></QueryField>
      <QueryField name="person_id" label="负责人" type="userSelect" :props="{placeholder: '负责人'}"></QueryField>
      <QueryField name="department_id" label="所属部门" type="officeSelect" :props="{placeholder: '所属部门'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "demo_sigletable_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/demo/demo_sigletable/form";
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
  permission: 'app:demo_sigletable:add',
}, {
  value: 'export',
  text: '导出Excel',
  permission: 'app:demo_sigletable:export',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:demo_sigletable:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:demo_sigletable:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:demo_sigletable:edit',
    }
  ],
  columns: [
    {"dataIndex":"serial_no","title":"编号","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"name","title":"名称","minWidth":150,"align":"left","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"input","queryFieldProps":{"placeholder":"名称"}},
    {"dataIndex":"quantity","title":"数量","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"app_form_code","title":"APP表单分组","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"dict":"app-form-group","dictMultiple":"1","widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"start_date","title":"开始日期","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"date-range","queryFieldProps":{"placeholder":"开始日期","formatPattern":"yyyy-MM-dd"}},
    {"dataIndex":"person_id__name","title":"负责人","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"user01.name","queryDefaultValue":"","queryFieldType":"input","queryFieldProps":{"placeholder":"负责人"}},
    {"dataIndex":"department_id__name","title":"所属部门","minWidth":150,"align":"left","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"office01.id","queryDefaultValue":"","queryFieldType":"office-select","queryFieldProps":{"placeholder":"所属部门"}},
    {"dataIndex":"administrative__name","title":"行政区","minWidth":150,"align":"left","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":{"placeholder":"行政区"}},
    {"dataIndex":"switch_","title":"开关","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
  ]
})

</script>
<style scoped>
</style>
