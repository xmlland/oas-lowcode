<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="附件"
                   :class="useModernListSkin ? 'modern-list-page sys-file-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="sys_file_">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">附件管理</h2>
        <p class="modern-list-query-desc">查询和查看系统附件资源，按名称、类型和上传时间快速定位文件记录。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField :widthMultiple="0.5" name="name_" label="附件名称" type="input" :props="{placeholder:'附件名称' }"></QueryField>
      <QueryField :widthMultiple="0.5" name="type_" label="附件类型" type="input" :props="{placeholder:'附件类型' }"></QueryField>
      <QueryField name="upload_time_" label="上传时间" type="date-range" :props="{formatPatter:'yyyy-MM-dd HH:mm:ss'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_file__list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_file_/form";
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
  permission: 'app:sys_file_:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_file_:remove'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_file_:view'
    }
  ],
  columns: [
    {title: '附件名称', dataIndex: 'name_', minWidth: 120, align: 'left'},
    {title: '附件类型', dataIndex: 'type_', minWidth: 120, align: 'left'},
    {title: '附件大小', dataIndex: 'size_', minWidth: 120, align: 'left'},
    {title: '分组', dataIndex: 'group_id_', minWidth: 120, align: 'left'},
    {title: '上传人', dataIndex: 'upload_user_name_', minWidth: 120, align: 'left'},
    {title: '上传时间', dataIndex: 'upload_time_', minWidth: 120, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
