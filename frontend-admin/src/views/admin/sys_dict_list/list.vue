<template>
  <div v-if="pageEditVisible" class="modern-list-page sys-dict-page-edit">
    <div class="sys-dict-page-edit-header">
      <div class="sys-dict-page-edit-title">
        <a-button type="link" class="sys-dict-page-back" @click="closePageEdit">
          <left-outlined />
          返回列表
        </a-button>
        <div>
          <h2>{{ pageEditTitle }}</h2>
          <p>以独立页面方式编辑数据字典，适合字段较多或需要长时间维护的表单。</p>
        </div>
      </div>
      <div class="sys-dict-page-edit-actions">
        <a-button @click="closePageEdit">取消</a-button>
        <a-button type="primary" :loading="pageEditSaving" @click="savePageEdit">保存</a-button>
      </div>
    </div>
    <div class="sys-dict-page-edit-body">
      <sys-dict-edit-panel ref="pageEditPanel" mode="page" @saveSuccess="handlePageEditSuccess" />
    </div>
  </div>
  <SingleTableView v-show="!pageEditVisible" ref="tableView" class="modern-list-page sys-dict-list-view" @clickRow="clickRow" :autoHeight="useModernListSkin" :queryArea="queryArea" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="数据字典" :buttons="buttons" formNo="sys_dictionary">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">数据字典</h2>
        <p class="modern-list-query-desc">维护系统字典分类、编码和英文名称，为表单字段、列表状态和枚举生成提供统一基础数据。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="code" label="编码" type="input" :props="{placeholder:'编码' }"></QueryField>
      <QueryField name="name" label="名称" type="input" :props="{placeholder:'名称' }"></QueryField>
    </template>
  </SingleTableView>
  <a-drawer
      v-model:visible="drawerEditVisible"
      width="900"
      :destroyOnClose="true"
      :body-style="{ padding: 0 }"
      :footer-style="{ padding: 0 }"
      class="sys-dict-edit-drawer"
      @close="closeDrawerEdit">
    <template #title>
      <div class="sys-dict-drawer-title">
        <div class="sys-dict-drawer-title-main">
          <span>抽屉编辑</span>
          <strong>{{ drawerEditDisplayName }}</strong>
        </div>
        <p>右侧快速维护字典分组与字典项，保存后自动刷新列表。</p>
      </div>
    </template>
    <sys-dict-edit-panel v-if="drawerEditVisible" ref="drawerEditPanel" mode="drawer" @saveSuccess="handleDrawerEditSuccess" />
    <template #footer>
      <div class="sys-dict-drawer-footer">
        <a-button @click="closeDrawerEdit">取消</a-button>
        <a-button type="primary" :loading="drawerEditSaving" @click="saveDrawerEdit">保存</a-button>
      </div>
    </template>
  </a-drawer>
</template>

<script>
export default {
  name: "sys_dict_list"
}

</script>
<script setup>
import form from "@/views/admin/sys_dict_list/form";
import SysDictEditPanel from "@/views/admin/sys_dict_list/SysDictEditPanel";
import {computed, nextTick, ref} from "vue";
import {postAction} from "@/api/action";
import {message} from "ant-design-vue";
import config from "@/config";

let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true } : {}
})
const tableView = ref(null)
const drawerEditPanel = ref(null)
const pageEditPanel = ref(null)
const drawerEditVisible = ref(false)
const drawerEditSaving = ref(false)
const drawerEditRecord = ref(null)
const pageEditVisible = ref(false)
const pageEditSaving = ref(false)
const pageEditTitle = ref('页面编辑')
const drawerEditDisplayName = computed(() => {
  return drawerEditRecord.value?.name || drawerEditRecord.value?.code || '数据字典'
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:sys_dictionary:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_dictionary:del'
}])

let singleTable = ref({
  optionWidth: 360,
  isShowMoreDropdown: false,
  initParam:{
    parent_id:{
      id:'data-params'
    }
  },
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_dictionary:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_dictionary:edit'
    }, {
      value: 'editDrawer',
      text: '抽屉编辑',
      permission: 'app:sys_dictionary:edit'
    }, {
      value: 'editPage',
      text: '页面编辑',
      permission: 'app:sys_dictionary:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_dictionary:del',
      color: 'error',
    }*/
    ,{
      value: 'genEnum',
      text: '生成Enum',
      permission: 'app:sys_dictionary:edit'
    }
  ],
  columns: [
    {title: '编码', dataIndex: 'code', minWidth: 120, align: 'left'},
    {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '英文名称', dataIndex: 'name_en', minWidth: 120, align: 'left'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})

const formatEditTitle = (type, row) => {
  return `${type}：${row?.name || row?.code || '数据字典'}`
}

const openDrawerEdit = row => {
  drawerEditRecord.value = row || null
  drawerEditVisible.value = true
  nextTick(() => {
    drawerEditPanel.value?.open(row, false)
  })
}

const closeDrawerEdit = () => {
  drawerEditVisible.value = false
}

const saveDrawerEdit = () => {
  drawerEditSaving.value = true
  const action = drawerEditPanel.value?.save()
  if (!action) {
    drawerEditSaving.value = false
    return
  }
  action.then(() => {
    drawerEditVisible.value = false
    tableView.value?.saveSuccess()
  }).finally(() => {
    drawerEditSaving.value = false
  })
}

const openPageEdit = row => {
  pageEditTitle.value = formatEditTitle('页面编辑', row)
  pageEditVisible.value = true
  nextTick(() => {
    pageEditPanel.value?.open(row, false)
  })
}

const closePageEdit = () => {
  pageEditVisible.value = false
}

const savePageEdit = () => {
  pageEditSaving.value = true
  const action = pageEditPanel.value?.save()
  if (!action) {
    pageEditSaving.value = false
    return
  }
  action.then(() => {
    pageEditVisible.value = false
    tableView.value?.saveSuccess()
  }).finally(() => {
    pageEditSaving.value = false
  })
}

const handleDrawerEditSuccess = () => {}
const handlePageEditSuccess = () => {}

function clickRow({value, row, index}) {
  if (value === 'genEnum') {
    postAction(`/gen/genTable/genEnum`, row).then(res => {
      message.success(res.msg)
    })
  } else if (value === 'editDrawer') {
    openDrawerEdit(row)
  } else if (value === 'editPage') {
    openPageEdit(row)
  }
}

</script>
<style lang="less" scoped>
.sys-dict-page-edit {
  min-height: 100%;
}

.sys-dict-page-edit-header,
.sys-dict-page-edit-body {
  background: #ffffff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(31, 41, 55, 0.04);
}

.sys-dict-page-edit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 18px;
}

.sys-dict-page-edit-title {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;

  h2 {
    margin: 0;
    color: #1f2937;
    font-size: 18px;
    font-weight: 600;
    line-height: 26px;
  }

  p {
    margin: 2px 0 0;
    color: #667085;
    font-size: 13px;
    line-height: 20px;
  }
}

.sys-dict-page-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding: 0 6px;
}

.sys-dict-page-edit-actions,
.sys-dict-drawer-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.sys-dict-page-edit-actions {
  :deep(.ant-btn) {
    height: 32px;
    border-radius: 6px !important;
    box-shadow: none;
  }
}

.sys-dict-page-edit-body {
  padding: 0;
  background: transparent;
  border: 0;
  box-shadow: none;
}

.sys-dict-drawer-footer {
  padding: 10px 16px;
  border-top: 1px solid #eef2f7;
}

</style>
