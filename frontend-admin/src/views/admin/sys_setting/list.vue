<template>
  <SingleTableView  :queryButton="true" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="参数设置"
                    :class="useModernListSkin ? 'modern-list-page sys-setting-list-view' : ''"
                    :autoHeight="useModernListSkin"
                    :queryArea="queryArea"
                    :buttons="buttons"
                    formNo="sys_setting" :modalWidth="800" >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">参数设置</h2>
        <p class="modern-list-query-desc">维护系统运行参数、参数值和说明信息，集中管理平台级配置。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="KEY_" label="参数名称" type="input" :props="{placeholder:'参数名称' }"></QueryField>
      <QueryField name="NAME" label="参数说明" type="input" :props="{placeholder:'参数说明' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_setting_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_setting/form";
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
  permission: 'app:sys_setting:add',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_setting:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_setting:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_setting:edit',
    }
  ],
  columns: [
    {title: '参数名称', dataIndex: 'KEY_', minWidth: 240,ellipsis: true, align: 'left'},
    {title: '参数值', dataIndex: 'VALUE_', minWidth: 120, align: 'left'},
    {title: '参数说明', dataIndex: 'NAME', minWidth: 240,ellipsis: true, align: 'left'},
  ]
})

</script>
<style scoped>
</style>
