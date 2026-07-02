<template>
  <left-tree-right-table-view ref="tableView" :rightTableView="rightTableView" modalTitle="用户" treeFormNo="sys_office"
                              :treeWidth="useModernListSkin ? 260 : 300"
                              :class="useModernListSkin ? 'modern-left-tree-right-page sys-user-ltr-view' : ''"
                              @clickRow="clickRow" @clickButton="clickButton" @totalCountChange="totalCountChange">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">用户管理</h2>
        <p class="modern-list-query-desc">按组织机构快速筛选用户，集中维护账号、角色、状态和登录权限。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="login_name" label="登录名" type="input" :props="{placeholder:'登录名' }"></QueryField>
      <QueryField name="name" label="姓名" type="input" :props="{placeholder:'姓名' }"></QueryField>
      <QueryField name="role_id" label="角色" type="select" :props="{placeholder:'角色',type:'table',dictType:'sys_role',valueField:'id',textField:'name',tableOrderBy:'a.sort' }"></QueryField>
      <QueryField name="login_flag" label="是否允许登录" type="select" :props="{placeholder:'是否允许登录',dictType:'yes_no'}"></QueryField>
      <QueryField name="useable" label="用户状态" type="select" :props="{placeholder:'用户状态',dictType:'sys_useable'}"></QueryField>
      <!--      <QueryField name="sso_login_flag" label="仅域控登录" type="select" :props="{placeholder:'仅域控登录',dictType:'yes_no'}"></QueryField>-->
    </template>
  </left-tree-right-table-view>
</template>

<script>
export default {
  name: "sys_user_list",
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/admin/sys_user/form";
import {confirmAction, postAction} from "@/api/action";
import {Modal} from "ant-design-vue";
import {prompt} from "@/lib/tools";
import {encryptByDESModeEBC} from "@/lib/cryptoJS-aes";
import {downLoadFileAction} from "@/api/api";
import {Sm2GenerateKeyPairHex,Sm2Decrypt} from "@/lib/tool-security";
import config from "@/config";

let instance = getCurrentInstance();
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let rightTableView = ref({
  modalComponent: form,
  formNo: 'sys_user',
  autoHeight: useModernListSkin.value,
  queryArea: useModernListSkin.value ? { resetButton: true } : {},
  buttons: [{
    value: 'add',
    text: '添加',
    permission: 'app:sys_user:add'
  }, {
    value: 'batch-delete',
    text: '删除',
    permission: 'app:sys_user:del'
  }, {
    value: 'export',
    text: '导出Excel',
    permission: 'app:sys_user:export'
  }, {
    value: 'batch-reset',
    text: '重置密码并导出',
    permission: 'app:sys_user:reset',
    disabledFilter(rows) {
      return rows.length === 0 && totalCount.value === 0
    }
  }],
  singleTable: {
    optionWidth: 260,
    rowButtons: [
      {
        value: 'view',
        text: '查看',
        permission: 'app:sys_user:view'
      }, {
        value: 'reset',
        text: '重置',
        permission: 'app:sys_user:reset'
      }/*, {
        value: 'resetTemp',
        text: '临时',
        permission: 'app:sys_user:reset'
      }*/, {
        value: 'enable',
        text: '启用',
        permission: 'app:sys_user:enable',
        visibleFilter(row) {
          return row.useable === '0'
        }
      }, {
        value: 'disable',
        text: '禁用',
        permission: 'app:sys_user:disable',
        color: 'error',
        visibleFilter(row) {
          return row.useable === '1'
        }
      }, {
        value: 'unlock',
        text: '解锁',
        permission: 'app:sys_user:unlock',
        visibleFilter(row) {
          return row.login_flag === '0'
        }
      }, {
        value: 'edit',
        text: '编辑',
        permission: 'app:sys_user:edit'
      }/*, {
        value: 'delete',
        text: '删除',
        permission: 'app:sys_user:del',
        color: 'error',
      }*/
    ],
    columns: [
      {title: '归属机构', dataIndex: ['parent', 'name'], sortColumn: 'b.name', minWidth: 120, align: 'left'},
      {title: '登录名', dataIndex: 'login_name', minWidth: 120, align: 'left'},
      {
        title: '是否允许登录',
        dataIndex: 'login_flag',
        minWidth: 120,
        align: 'center', sorter: 'false',
        status: {
          enableText: '是',
          disableText: '否'
        },
      },
      {
        title: '状态',
        dataIndex: 'useable',
        minWidth: 120,
        align: 'center', sorter: 'false',
        status: true
      },
      {title: '用户编号', dataIndex: 'no', minWidth: 120, align: 'left'},
      {title: '姓名', dataIndex: 'name', minWidth: 120, align: 'left'},
      {title: '职务', dataIndex: ['level_id', 'name'], minWidth: 120, align: 'left'},
      /*{title: '状态', dataIndex: 'useable', minWidth: 120, align: 'center', dict: 'sys_useable'},*/
      {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
    ]
  }
})
const saveSuccess = () => {
  instance.refs.tableView.saveSuccess()
}
let totalCount = ref(0)
const totalCountChange = (total) => {
  totalCount.value = total
}
const clickButton = ({value}) => {

  if ('batch-reset' === value) {
    let rows = instance.refs.tableView.getSelectedRows()
    let queryData = instance.refs.tableView.getQueryData();
    let total = rows.length
    if (total === 0) {
      total = totalCount.value
    } else {
      queryData['a.id'] = rows.map(row => row.id).join(',')
      queryData.queryParamType['a.id'] = 'in'
    }
    if (total === 0) {
      Modal.info({
        content: '请选择要重置密码的用户'
      })
      return
    }
    prompt({
      title: `是否重置${total}个用户的密码并导出excel？`,
      required: false,
      maxlength: 12,
      placeholder: '请输入新密码,为空则随机生成',
      callback: (value) => {
        let newPassword = ''
        if (value) {
          newPassword = encryptByDESModeEBC(value)
        }
        postAction(`sys/user/batchResetPassword?newPassword=${newPassword}`, queryData).then((res) => {
          downLoadFileAction(res.data.fileId)
        })
      }
    })
  }
}
const clickRow = ({value, row}) => {
  if ('reset' === value) {
    confirmAction('操作确认', '是否重置该用户密码？', 'sys/user/resetPassword?loginName=' + row.login_name, {}, (res) => {
      Modal.info({
        content: res.msg
      })
      // saveSuccess()
    })
  }
  if ('resetTemp' === value) {
    let sm2GenerateKeyPairHex = Sm2GenerateKeyPairHex()
    confirmAction('操作确认', '是否重置该用户临时密码？', 'sys/user/resetTempPassword?loginName=' + row.login_name + '&publicKey=' + sm2GenerateKeyPairHex.publicKey, {}, (res) => {
      Modal.info({
        content: Sm2Decrypt(res.data.msg,sm2GenerateKeyPairHex.privateKey)
      })
      // saveSuccess()
    },)
  }
  if ('enable' === value) {
    confirmAction('操作确认', '是否启用该用户？', 'sys/user/enableUser?loginName=' + row.login_name, {}, () => {
      saveSuccess()
    })
  }
  if ('disable' === value) {
    confirmAction('操作确认', '是否禁用该用户？', 'sys/user/disableUser?loginName=' + row.login_name, {}, () => {
      saveSuccess()
    })
  }
  if ('unlock' === value) {
    confirmAction('操作确认', '是否解锁该用户？', 'sys/user/unlockUser?loginName=' + row.login_name, {}, () => {
      saveSuccess()
    })
  }
}
</script>
<style scoped>
</style>
