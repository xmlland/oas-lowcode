<template>
  <SingleTableView ref="tableView" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="角色"
                   :class="useModernListSkin ? 'modern-list-page sys-role-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   @clickRow="clickRow"
                   formNo="sys_role">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">角色管理</h2>
        <p class="modern-list-query-desc">维护系统角色、成员范围和权限配置，集中控制菜单访问与数据操作能力。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField queryType="like" name="a.enname" label="英文名称" type="input" :props="{placeholder:'英文名称' }"></QueryField>
      <QueryField name="name" label="名称" type="input" :props="{placeholder:'名称' }"></QueryField>
      <QueryField name="is_sys" label="系统角色" type="select" :props="{placeholder:'系统角色',dictType:'yes_no'}"></QueryField>
      <QueryField name="useable" label="启用" type="select" :props="{placeholder:'启用',dictType:'yes_no'}"></QueryField>
      <QueryField name="remarks" label="备注" type="input" :props="{placeholder:'备注' }"></QueryField>
    </template>
  </SingleTableView>
  <u-modal ref="authModal" :width="1200" :customOK="true" @clickOk="saveAuth" @saveSuccess="saveSuccess"
           :customBodyStyle="{height:'70vh',overflow:'auto'}">
    <auth-form ref="authForm" :role-id="currentRow.id"/>
  </u-modal>
</template>

<script>
export default {
  name: "sys_role_list",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/admin/sys_role/form";
import AuthForm from "@/views/admin/sys_role/authForm";
import {confirmAction, getAction, postAction} from "@/api/action";
import {saveDataAction, saveDataUrl} from "@/api/api";
import {cloneJavaBean, confirmModal} from "@/lib/tools";
import config from "@/config";

let modalComponent = form

let instance = getCurrentInstance();
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:sys_role:add'
}, {
  value: 'import',
  text: '导入数据',
  permission: 'app:sys_role:import'
}, {
  value: 'export',
  text: '导出Excel',
  permission: 'app:sys_role:export'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_role:del'
}])

let singleTable = ref({
  optionWidth: 360,
  isShowMoreDropdown: false,
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_role:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_role:edit'
    }, {
      value: 'auth',
      text: '权限设置',
      permission: 'app:sys_role:edit'
    }, {
      value: 'copy',
      text: '复制',
      permission: 'app:sys_role:edit'
    }, {
      value: 'copyWithAuth',
      text: '复制(含权限)',
      permission: 'app:sys_role:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_role:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '英文名称', dataIndex: 'enname', minWidth: 120, align: 'left'},
    {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '系统角色', dataIndex: 'is_sys', minWidth: 120, align: 'center', dict: 'yes_no'},
    {title: '启用', dataIndex: 'useable', minWidth: 120, align: 'center', dict: 'yes_no'},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'left'},
    {title: '备注', dataIndex: 'remarks', minWidth: 120, align: 'left'},
  ]
})
const saveSuccess = () => {
  instance.refs.tableView.saveSuccess()
}
let currentRow = ref({})
const clickRow = ({value, row, index}) => {
  if ('auth' === value) {
    currentRow.value = row
    instance.refs.authModal.open('【权限设置】', row)
  }
  if ('copy' === value) {
    let newRow = {...row}
    delete newRow.id
    delete newRow.createBy
    delete newRow.create_date
    delete newRow.updateBy
    delete newRow.update_date
    newRow.formNo = 'sys_role'
    newRow.name = newRow.name + '(复制)'
    newRow.enname = newRow.enname + '-copy'
    confirmAction('操作确认', '确定复制该角色吗？', saveDataUrl, newRow, () => {
      instance.refs.tableView.loadData()
    })
  }
  if ('copyWithAuth' === value) {
    let id = row.id
    let newRow = cloneJavaBean(row);
    newRow.name = newRow.name + '(复制)'
    newRow.enname = newRow.enname + '-copy'
    confirmModal({
      title: '操作确认',
      content: '确定复制该角色吗？',
      onOk: () => {
        saveDataAction('sys_role', newRow).then(res => {
          let entityId = res.data.entityId
          getAction('system/role/getAuth', {id: id}).then(authRes => {
            postAction('system/role/saveAuth', {
              id: entityId,
              ids: authRes.data.data.join()
            }).then(() => {
              instance.refs.tableView.saveSuccess()
            })
          })
        })
      }
    })
  }
}

const saveAuth = (callback) => {
  callback(postAction, 'system/role/saveAuth', instance.refs.authForm.getCheckData())
}
</script>
<style scoped>
</style>
