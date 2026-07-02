<template>
  <SingleTableView :modalFull="0"
    :class="useModernListSkin ? 'modern-list-page oa-attendance-location-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :queryButton="true" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="考勤地点" :buttons="buttons" formNo="oa_attendance_location" :modalWidth="1200" >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">考勤地点</h2>
        <p class="modern-list-query-desc">维护考勤地点、地址、打卡范围和启用状态。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="location_name" label="地点名称" type="input" :props="{placeholder:'地点名称' }"></QueryField>
      <QueryField name="address" label="详细地址" type="input" :props="{placeholder:'详细地址' }"></QueryField>
      <QueryField name="status" label="状态" type="select" :props="{placeholder:'状态',dictType:'sys_useable'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oa_attendance_location_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oas/oa_attendance_location/form";
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
  permission: 'app:oa_attendance_location:add',
}, {
  value: 'export',
  text: '导出Excel',
  permission: 'app:oa_attendance_location:export',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oa_attendance_location:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:oa_attendance_location:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:oa_attendance_location:edit',
    }
  ],
  columns: [
    {"dataIndex":"location_name","title":"地点名称","minWidth":150,"align":"left","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"input","queryFieldProps":{"placeholder":"地点名称"}},
    {"dataIndex":"address","title":"详细地址","minWidth":300,"align":"left","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"input","queryFieldProps":{"placeholder":"详细地址"}},
    {"dataIndex":"longitude","title":"经度","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"latitude","title":"纬度","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"radius","title":"打卡范围（米）","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"status","title":"状态","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"dict":"sys_useable","dictMultiple":"0","widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"select","queryFieldProps":{"placeholder":"状态","dictType":"sys_useable"}},
  ]
})

</script>
<style scoped>
</style>
