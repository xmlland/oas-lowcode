<template>
  <SingleTableView :modalFull="0"
    :class="useModernListSkin ? 'modern-list-page demo-260222-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :queryButton="true" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="测试数字类型" :buttons="buttons" formNo="demo_260222" :modalWidth="1200" >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">测试数字类型</h2>
        <p class="modern-list-query-desc">验证文本、整数、数字和开关类字段展示。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="s01" label="单行文本" type="input" :props="{placeholder:'单行文本' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "demo_260222_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/demo/demo_260222/form";
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
  permission: 'app:demo_260222:add',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:demo_260222:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:demo_260222:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:demo_260222:edit',
    }
  ],
  columns: [
    {"dataIndex":"s01","title":"单行文本","minWidth":150,"align":"left","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"input","queryFieldProps":{"placeholder":"单行文本"}},
    {"dataIndex":"i01","title":"整数1","minWidth":150,"align":"left","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"i02","title":"整数2","minWidth":150,"align":"left","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"b01","title":"数字1","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"f01","title":"开关","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
    {"dataIndex":"b02","title":"数字2","minWidth":150,"align":"center","sorter":"false","ellipsis":false,"widthMultiple":1,"queryColumn":"","queryDefaultValue":"","queryFieldType":"","queryFieldProps":null},
  ]
})

</script>
<style scoped>
</style>
