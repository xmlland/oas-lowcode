<template>
  <SingleTableView :modalFull="0"
                   :subTable="true" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="保护记录"
                   :class="useModernListSkin ? 'modern-list-page sys-sec-idata-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons" formNo="sys_sec_idata" :modalWidth="1200">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">保护记录</h2>
        <p class="modern-list-query-desc">查看完整性保护记录、校验状态和校验时间，用于定位异常数据。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="data_id" label="记录主键" type="input" :props="{placeholder:'记录主键'}"></QueryField>
      <QueryField name="valid_pass" label="校验通过" type="select" :props="{placeholder:'校验通过',type:'dict',dictType:'yes_no'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_sec_idata_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_sec_idata/form";
import config from "@/config";

let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
    }
  ],
  columns: [
    {title: '记录主键', dataIndex: 'data_id', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
    {title: '操作时间', dataIndex: 'opt_time', minWidth: 150, align: 'center', sorter: 'false', ellipsis: false},
    {title: '完整性值', dataIndex: 'integrity_value', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
    {title: '操作人姓名', dataIndex: 'opt_name', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
    {
      title: '校验状态', dataIndex: 'valid_pass', minWidth: 150, align: 'center', sorter: 'false', ellipsis: false, status: {
        enableText: '通过',
        disableText: '未通过',
      }
    },
    {title: '校验时间', dataIndex: 'valid_time', minWidth: 150, align: 'center', sorter: 'false', ellipsis: false},
  ]
})

</script>
<style scoped>
</style>
