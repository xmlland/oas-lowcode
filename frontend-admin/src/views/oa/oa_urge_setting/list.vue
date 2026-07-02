<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="督办设置" :buttons="buttons" formNo="oa_urge_setting"
                   :class="useModernListSkin ? 'modern-list-page oa-urge-setting-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">督办设置</h2>
        <p class="modern-list-query-desc">维护督办人员、流程范围、期限规则和提醒内容。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="urge_limit" label="督办期限" type="select" :props="{placeholder:'督办期限',dictType:'oa_urge_setting_limit'}"></QueryField>
      <QueryField name="urge_content" label="督办内容" type="input" :props="{placeholder:'督办内容' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oa_urge_setting_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oa/oa_urge_setting/form";
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
  permission: 'app:oa_urge_setting:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oa_urge_setting:remove'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:oa_urge_setting:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:oa_urge_setting:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:oa_urge_setting:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '督办人', dataIndex: ['urge_user','name'], minWidth: 120, align: 'left'},
    {title: '督办期限', dataIndex: 'urge_limit', minWidth: 120, align: 'center', dict: 'oa_urge_setting_limit'},
    {title: '督办流程', dataIndex: 'urge_process', minWidth: 120, align: 'center', dict: 'act_category', dictMultiple: true},
    {title: '督办内容', dataIndex: 'urge_content', minWidth: 120, align: 'left'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
