<template>
  <SingleTableView :modalFull="0"
                   :queryButton="true" :singleTable="singleTable"
                   :modalComponent="modalComponent" modalTitle="完整性保护配置"
                   :class="useModernListSkin ? 'modern-list-page sys-sec-iconfig-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons" formNo="sys_sec_iconfig" :modalWidth="1400" @clickRow="clickRow" @clickButton="clickButton">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">完整性保护配置</h2>
        <p class="modern-list-query-desc">维护表级完整性保护配置，支持保护数据重建、同步和异常数据核验。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="table_name" label="表名" type="select"
                  :props="{placeholder:'表名',type:'table',dictType:'gen_table',valueField:'name',textField:'comments',tableOrderBy:'',tableFilterData:[]}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script lang="jsx">
export default {
  name: "sys_sec_iconfig_list",
}
</script>
<script setup lang="jsx">
import {computed, ref} from "vue";
import form from "@/views/admin/sys_sec_iconfig/form";
import {confirmAction} from "@/api/action";
import {Modal} from "ant-design-vue";
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
  permission: 'app:sys_sec_iconfig:add',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_sec_iconfig:remove',
}, {
  value: 'rebuild',
  text: '全部重建',
  permission: 'app:sys_sec_iconfig:add',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_sec_iconfig:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_sec_iconfig:edit',
    }, {
      value: 'rebuild',
      text: '重建',
      permission: 'app:sys_sec_iconfig:edit',
    }, {
      value: 'sync',
      text: '同步',
      permission: 'app:sys_sec_iconfig:edit',
    }
  ],
  columns: [
    {title: '表名', dataIndex: 'table_name', minWidth: 150, align: 'left', sorter: 'false', ellipsis: false},
    {title: '保护列', dataIndex: 'column_names', minWidth: 300, align: 'left', sorter: 'false', ellipsis: false},
    {title: '表中数据量', dataIndex: 'table_count', minWidth: 150, align: 'center', sorter: 'false', ellipsis: false},
    {
      title: '完整性保护数据量', dataIndex: 'recordCount', minWidth: 150, align: 'center', sorter: 'false', customRenderSlot: ({record}) => {
        let color = record.table_count === record.record_count ? 'green' : 'red'
        return <span style={{color: color}}>{record.record_count}</span>
      }
    },
    {
      title: '异常数据量', dataIndex: 'error_count', minWidth: 150, align: 'center', sorter: 'false', customRenderSlot: ({record}) => {
        let color = record.error_count === 0 ? 'green' : 'red'
        return <span style={{color: color}}>{record.error_count}</span>
      }
    },
  ]
})

const rebuild = (tableName = '', all = true) => {
  let title = all ? '全部重建' : '同步（仅处理差异数据）'
  confirmAction('操作确认', '确定'+title+'完整性保护校验数据吗？', 'sys/secIConfig/syncData', {tableName: tableName, all: all}, () => {
    Modal.info({
      title: '操作结果',
      content: '调用成功！'
    })
  })
}

const clickRow = ({value, row, index}) => {
  if ('rebuild' === value) {
    rebuild(row.table_name)
  } else if ('sync' === value) {
    rebuild(row.table_name, false)
  }
}
const clickButton = ({value}) => {

  if ('rebuild' === value) {
    rebuild()
  }
}
</script>
<style scoped>
</style>
