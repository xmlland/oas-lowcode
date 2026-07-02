<template>
  <SingleTableView
    :singleTable="singleTable"
    :modalComponent="modalComponent"
    modalTitle="文单序列"
    :class="useModernListSkin ? 'modern-list-page oa-sequence-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :buttons="buttons"
    formNo="oa_sequence"
  >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">文单序列</h2>
        <p class="modern-list-query-desc">维护公文编号规则、年度循环方式和当前序列值。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="key_" label="编码" type="input" :props="{placeholder:'编码' }"></QueryField>
      <QueryField name="name" label="名称" type="input" :props="{placeholder:'名称' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oa_sequence_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oa/oa_sequence/form";
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
  permission: 'app:oa_sequence:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oa_sequence:remove'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:oa_sequence:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:oa_sequence:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:oa_sequence:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '编码', dataIndex: 'key_', minWidth: 120, align: 'left'},
    {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '当前年度', dataIndex: 'year', minWidth: 120, align: 'left'},
    {title: '按年循环', dataIndex: 'cycle_by_year', minWidth: 120, align: 'center', dict: 'annual_cycle'},
    {title: '起始值', dataIndex: 'start_value', minWidth: 120, align: 'left'},
    {title: '当前值', dataIndex: 'current_value', minWidth: 120, align: 'left'},
    {title: '上年度值', dataIndex: 'last_year_value', minWidth: 120, align: 'left'},
  ]
})

</script>
<style scoped>
</style>
