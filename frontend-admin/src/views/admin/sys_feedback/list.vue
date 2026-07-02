<template>
  <SingleTableView ref="tableView" :modalFull="0" :extendFormData="extendFormData"
                   :queryButton="true" :singleTable="singleTable" :modalComponent="modalComponent"
                   :class="useModernListSkin ? 'modern-list-page sys-feedback-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   modalTitle="问题反馈" :buttons="buttons" formNo="sys_feedback@view" :modalWidth="1200">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">问题反馈</h2>
        <p class="modern-list-query-desc">统一处理用户反馈和常见问题，按模块、标题、提交人和回复状态快速筛选。</p>
      </div>
    </template>
    <template #queryFields>
      <a-radio-group v-model:value="is_common" @change="changeIsCommon" style="margin-top: 5px;">
        <a-radio-button value="0">
          用户反馈
        </a-radio-button>
        <a-radio-button value="1">
          常见问题
        </a-radio-button>
      </a-radio-group>
      <QueryField name="module_" label="模块" type="input" :props="{placeholder:'模块' }"></QueryField>
      <QueryField name="title_" label="标题" type="input" :props="{placeholder:'标题' }"></QueryField>
      <QueryField v-if="is_common!=='1'" name="user02.name" label="提交人" type="input" :props="{placeholder:'提交人' }"></QueryField>
      <QueryField v-if="is_common!=='1'" name="is_reply" label="是否回复" type="select" :props="{placeholder:'是否回复',dictType:'yes_no'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "sys_feedback_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/admin/sys_feedback/form";
import config from "@/config";

let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = computed(() => {
  return isCommon.value ? [{
    value: 'add',
    text: '添加',
    permission: 'app:sys_feedback:add',
  }, {
    value: 'batch-delete',
    text: '删除',
    permission: 'app:sys_feedback:remove',
  }] : []
})
let is_common = ref('0')
let isCommon = computed(() => {
  return is_common.value === '1'
})

const changeIsCommon = () => {
  tableView.value.loadData()
}
const tableView = ref(null)

let extendFormData = computed(() => {
  return {
    is_common: is_common.value
  }
})

let singleTable = computed(() => {
  return {
    initParam: {
      is_common: is_common.value
    },
    rowButtons: [
      {
        value: 'view',
        text: '查看',
        permission: 'app:sys_feedback:view',
      }, {
        value: 'edit',
        text: '回复',
        permission: 'app:sys_feedback:edit',
        visibleFilter: (record) => {
          return record.is_common !== '1'
        }
      }, {
        value: 'edit',
        text: '编辑',
        permission: 'app:sys_feedback:edit',
        visibleFilter: (record) => {
          return record.is_common === '1'
        }
      }
    ],
    columns: isCommon.value ? [
      {title: '模块', dataIndex: 'module_', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
      {title: '标题', dataIndex: 'title_', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
      {title: '时间', dataIndex: 'reply_time', minWidth: 150, align: 'center', sorter: 'false', ellipsis: false},
    ] : [
      {title: '模块', dataIndex: 'module_', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
      {title: '标题', dataIndex: 'title_', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
      {title: '提交单位', dataIndex: 'company_name', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
      {title: '提交人', dataIndex: 'submit_user__name', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
      {title: '提交时间', dataIndex: 'submit_time', minWidth: 150, align: 'center', sorter: 'false', ellipsis: false},
      {title: '是否回复', dataIndex: 'is_reply__name', minWidth: 150, align: 'center', sorter: 'false', dict: 'yes_no', ellipsis: false},
      {title: '回复时间', dataIndex: 'reply_time', minWidth: 150, align: 'center', sorter: 'false', ellipsis: false},
    ]
  }
})

</script>
<style scoped>
</style>
