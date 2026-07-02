<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="岗位"
                   :class="useModernListSkin ? 'modern-list-page sys-post-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="sys_post">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">岗位管理</h2>
        <p class="modern-list-query-desc">维护岗位名称、代码和启用状态，用于人员岗位归属和权限范围配置。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="name" label="岗位名称" type="input" :props="{placeholder:'岗位名称' }"></QueryField>
      <QueryField name="code" label="岗位代码" type="input" :props="{placeholder:'岗位代码' }"></QueryField>
      <QueryField name="useable" label="状态" type="select" :props="{placeholder:'状态',dictType:'sys_useable'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_post_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_post/form";
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
  permission: 'app:sys_post:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_post:del'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_post:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_post:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_post:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '岗位名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '岗位代码', dataIndex: 'code', minWidth: 120, align: 'left'},
    {title: '状态', dataIndex: 'useable', minWidth: 120, align: 'center', dict: 'sys_useable'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
