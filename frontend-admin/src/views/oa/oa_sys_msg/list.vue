<template>
  <SingleTableView
    ref="tableViewRef"
    :queryButton="true"
    :singleTable="singleTable"
    :modalComponent="modalComponent"
    modalTitle="系统消息"
    :class="useModernListSkin ? 'modern-list-page oa-sys-msg-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :buttons="buttons"
    formNo="oa_sys_msg"
    @clickButton="handleClickBtn"
  >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">系统消息</h2>
        <p class="modern-list-query-desc">查看系统消息发送、接收、阅读状态和跳转来源。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField :width="270" name="title" label="消息标题" type="input" :props="{placeholder:'消息标题' }"></QueryField>
      <QueryField :width="270" name="sender" label="发送人" type="userSelect" :props="{placeholder: '发送人'}"></QueryField>
      <QueryField :width="510" name="send_time" label="发送时间" type="date-range" :props="{formatPatter:'yyyy-MM-dd HH:mm:ss'}"></QueryField>
      <QueryField :width="270" name="recipient" label="接收人" type="userSelect" :props="{placeholder: '接收人'}"></QueryField>
      <QueryField :width="270" name="status" label="状态" type="select" :props="{placeholder:'状态',dictType:'message_type'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script lang="jsx">
export default {
  name: "oa_sys_msg_list",
}
</script>
<script setup lang="jsx">
import {computed, ref, inject} from "vue";
import form from "@/views/oa/oa_sys_msg/form";
import {setMsgRead, setMsgReadBatch} from "@/api/msg";
import {message, Modal} from "ant-design-vue";
import {useStore} from "vuex";
import config from "@/config";

const store = useStore();
let tableViewRef = ref(null);
let modalComponent = form;

const refreshMsgUnreadCount = inject('refreshMsgUnreadCount', null);
const addTabToAdminContent = inject('addTabToAdminContent', null);

const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'readBatch',
  text: '标记已读',
  permission: 'app:oa_sys_msg:edit',
  requireSelectedRows: true,
}]);

const handleClickBtn = (btn) => {
  if (btn.value === 'readBatch') {
    const selectedRows = btn.selectedRows || [];
    if (!selectedRows.length) {
      message.warning('请先勾选要标记为已读的消息');
      return true;
    }
    const ids = selectedRows.map(r => r.id).join(',');
    Modal.confirm({
      title: '确认',
      content: `确定要将选中的 ${selectedRows.length} 条消息标记为已读吗？`,
      onOk: () => {
        setMsgReadBatch(ids).then(res => {
          if (res.code === 0) {
            message.success('已标记为已读');
            tableViewRef.value && tableViewRef.value.loadData();
            if (refreshMsgUnreadCount) {
              refreshMsgUnreadCount();
            } else {
              store.dispatch('refreshMsgUnreadCount');
            }
          }
        });
      }
    });
    return true;
  }
  return false;
};

const handleTitleClick = (record) => {
  if (record.status === '1') {
    setMsgRead(record.id).then(res => {
      if (res.code === 0) {
        if (refreshMsgUnreadCount) {
          refreshMsgUnreadCount();
        } else {
          store.dispatch('refreshMsgUnreadCount');
        }
        tableViewRef.value && tableViewRef.value.loadData();
      }
    });
  }
  if (record.menu_href && addTabToAdminContent) {
    addTabToAdminContent({
      attributes: {
        pageName: record.menu_name
      },
      key: record.menu_href
    });
  }
};

let singleTable = ref({
  rowSelection: true,
  rowButtons: [],
  columns: [
    {
      title: '消息标题',
      dataIndex: 'title',
      minWidth: 240,
      align: 'left',
      customRenderSlot: ({text, record}) => {
        const isUnread = record.status === '1';
        return (
          <a-button type="link" style={{padding: 0, height: 'auto', lineHeight: 'inherit'}} onClick={() => handleTitleClick(record)}>
            <span style={{
              textAlign: 'center',
              fontWeight: isUnread ? 'bold' : 'normal',
              color: isUnread ? '#1890ff' : 'inherit'
            }}>
              {isUnread && <span style={{color: '#ff4d4f', marginRight: '4px'}}>●</span>}
              {text}
            </span>
          </a-button>
        );
      }
    },
    {title: '发送人', dataIndex: ['sender', 'name'], minWidth: 60, align: 'left'},
    {title: '发送时间', dataIndex: 'send_time', minWidth: 120, align: 'center'},
    {title: '接收人', dataIndex: ['recipient', 'name'], minWidth: 60, align: 'center'},
    {title: '阅读时间', dataIndex: 'read_time', minWidth: 120, align: 'center'},
    {title: '状态', dataIndex: 'status', minWidth: 60, align: 'center', dict: 'message_type'},
  ]
});
</script>
<style scoped>
</style>
