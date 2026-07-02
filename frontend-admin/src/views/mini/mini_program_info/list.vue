<template>
  <SingleTableView
    :queryButton="true"
    :singleTable="singleTable"
    :modalWidth="800"
    :modalComponent="modalComponent"
    modalTitle="小程序信息"
    :class="useModernListSkin ? 'modern-list-page mini-program-info-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :buttons="buttons"
    formNo="mini_program_info"
  >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">小程序信息</h2>
        <p class="modern-list-query-desc">维护小程序名称、AppID 和密钥等基础接入配置。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="app_name" label="名称" type="input" :props="{placeholder:'名称' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "mini_program_info_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/mini/mini_program_info/form";
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
  permission: 'app:mini_program_info:add',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:mini_program_info:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:mini_program_info:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:mini_program_info:edit',
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:mini_program_info:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: 'id', dataIndex: 'id', minWidth: 120, align: 'left'},
    {title: '名称', dataIndex: 'app_name', minWidth: 120, align: 'left'},
    {title: 'AppID', dataIndex: 'app_id', minWidth: 120, align: 'left'},
    {title: 'AppSecret', dataIndex: 'app_secret', minWidth: 120, align: 'left'},
  ]
})

</script>
<style scoped>
</style>
