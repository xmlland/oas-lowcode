<template>
  <SingleTableView :modalFull="false"
    :class="useModernListSkin ? 'modern-list-page demo-mtable-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :queryButton="true" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="测试主表" :buttons="buttons" formNo="demo_mtable" :modalWidth="1200" >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">测试主表</h2>
        <p class="modern-list-query-desc">验证主子表结构、导入导出和状态字段展示。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="name" label="名称" type="input" :props="{placeholder:'名称' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "demo_mtable_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/demo/demo_mtable/form";
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
  permission: 'app:demo_mtable:add',
}, {
  value: 'import',
  text: '导入数据',
  permission: 'app:demo_mtable:import',
}, {
  value: 'export',
  text: '导出Excel',
  permission: 'app:demo_mtable:export',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:demo_mtable:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:demo_mtable:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:demo_mtable:edit',
    }
  ],
  columns: [
    {title:'名称',dataIndex:'name',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'说明',dataIndex:'explain_',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'是否可用',dataIndex:'available',minWidth:150,align:'center',sorter:'false',dict:'yes_no',ellipsis:false},
  ]
})

</script>
<style scoped>
</style>
