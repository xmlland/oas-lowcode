<template>
  <SingleTableView
    :queryButton="true"
    :singleTable="singleTable"
    :modalComponent="modalComponent"
    modalTitle="通知公告"
    :class="useModernListSkin ? 'modern-list-page oa-sys-announcement-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :buttons="buttons"
    formNo="oa_sys_announcement"
  >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">通知公告</h2>
        <p class="modern-list-query-desc">维护公告标题、发送时间、消息颜色和接收范围。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="title" label="消息标题" type="input" :props="{placeholder:'消息标题' }"></QueryField>
      <QueryField name="send_time" label="发送时间" type="date" :props="{formatPatter:'yyyy-MM-dd HH:mm:ss'}"></QueryField>
      <QueryField name="message_color" label="消息颜色" type="select" :props="{placeholder:'消息颜色',dictType:'color'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oa_sys_announcement_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oa/oa_sys_announcement/form";
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
  permission: 'app:oa_sys_announcement:add',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oa_sys_announcement:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:oa_sys_announcement:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:oa_sys_announcement:edit',
    }
  ],
  columns: [
    {title: '发送人', dataIndex: ['sender','name'], minWidth: 120, align: 'left'},
    {title: '消息标题', dataIndex: 'title', minWidth: 120, align: 'left'},
    {title: '发送时间', dataIndex: 'send_time', minWidth: 120, align: 'center'},
    {title: '消息颜色', dataIndex: 'message_color', minWidth: 120, align: 'center', dict: 'color'},
  ]
})

</script>
<style scoped>
</style>
