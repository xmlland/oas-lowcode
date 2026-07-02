<template>
  <TreeTableView ref="tableView" @clickRow="clickRow" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="机构" :buttons="buttons" :rowButtons="rowButtons" formNo="sys_office"
                 :queryArea="queryArea" :class="useModernListSkin ? 'modern-tree-table-page sys-office-tree-table-view' : ''">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">机构管理</h2>
        <p class="modern-list-query-desc">按层级维护组织机构、状态和排序，支持快速新增下级与权限设置。</p>
      </div>
    </template>
  </TreeTableView>

  <u-modal ref="authModal" :width="1200" :customOK="true" @clickOk="saveAuth" @saveSuccess="saveSuccess"
           :customBodyStyle="{height:'70vh',overflow:'auto'}">
    <auth-form get-auth-url="system/office/getAuth" ref="authForm" :role-id="currentRow.id"/>
  </u-modal>
</template>

<script>
export default {
  name: "sys_office_list",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/admin/sys_office/form";
import AuthForm from "@/views/admin/sys_role/authForm.vue";
import {postAction} from "@/api/action";
import config from "@/config";
let instance = getCurrentInstance();

let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:sys_office:add'
}])

let rowButtons = ref([{
  value: 'view',
  text: '查看',
  permission: 'app:sys_office:view'
}, {
  value: 'edit',
  text: '编辑',
  permission: 'app:sys_office:edit'
}, {
  value: 'delete',
  text: '删除',
  permission: 'app:sys_office:del'
}, {
  value: 'addChild',
  text: '添加下级',
  permission: 'app:sys_office:lowerlevel'
},{
  value: 'auth',
  text: '权限设置',
  permission: 'app:sys_role:edit'
}])

let singleTable = ref({
  autoHeight: useModernListSkin.value,
  isShowMoreDropdown:false,
  optionWidth: 300,
  rowSelection:false,
  columns: [
    {title: '机构编码', dataIndex: 'code', minWidth: 120, align: 'left'},
    {title: '机构名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '简称', dataIndex: 'short_name', minWidth: 120, align: 'left'},
    {
      title: '状态',
      dataIndex: 'useable',
      minWidth: 120,
      align: 'center',sorter:'false',
      status:true
    },
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})
let currentRow = ref({})

const clickRow = ({value, row, index}) => {
  if ('auth' === value) {
    currentRow.value = row
    instance.refs.authModal.open('【权限设置】', row)
  }
}
const saveSuccess = () => {
  instance.refs.tableView.saveSuccess()
}
const saveAuth = (callback) => {
  callback(postAction, 'system/office/saveAuthOffice', instance.refs.authForm.getCheckData())
}

</script>
<style scoped>
</style>
