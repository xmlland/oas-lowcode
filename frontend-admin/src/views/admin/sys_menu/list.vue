<template>
  <TreeTableView ref="treeView" @clickRow="clickRow" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="菜单" :buttons="buttons" :rowButtons="rowButtons"
                 :queryArea="queryArea"
                 :class="useModernListSkin ? 'modern-tree-table-page sys-menu-tree-table-view' : ''"
                 formNo="sys_menu">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">菜单管理</h2>
        <p class="modern-list-query-desc">维护系统菜单层级、访问地址、权限标识和终端可见范围。</p>
      </div>
    </template>
  </TreeTableView>
</template>

<script lang="jsx">
export default {
  name: "sys_menu_list",
}
</script>
<script setup lang="jsx">
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/admin/sys_menu/form";
import {confirmAction} from "@/api/action";
import {saveDataUrl} from "@/api/api";
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
  permission: 'app:sys_menu:add'
}])

let rowButtons = ref([{
  value: 'view',
  text: '查看',
  permission: 'app:sys_menu:view'
}, {
  value: 'edit',
  text: '编辑',
  permission: 'app:sys_menu:edit'
}, {
  value: 'copy',
  text: '复制',
  permission: 'app:sys_menu:edit', visible: ({row}) => {
    return row.type_ === '1'
  }
}, {
  value: 'copyWithItem',
  text: '复制(携带下一级)',
  permission: 'app:sys_menu:edit', visible: ({row}) => {
    return row.type_ === '1'
  }
}, {
  value: 'delete',
  text: '删除',
  permission: 'app:sys_menu:del'
}, {
  value: 'addChild',
  text: '添加下级',
  permission: 'app:sys_menu:lowerlevel'
}])

let singleTable = ref({
  autoHeight: useModernListSkin.value,
  isShowMoreDropdown: false,
  optionWidth: 420,
  rowSelection: false,
  columns: [
    {title: '名称', dataIndex: 'name', minWidth: 200, align: 'left'},
    {
      title: '图标', dataIndex: 'icon', minWidth: 60, align: 'center', customRenderSlot: ({column, text, index, record}) => {
        return <u-icon type={text}></u-icon>
      }
    },
    {title: '菜单级别', dataIndex: 'type_', minWidth: 120, align: 'center', dict: 'sys_menu_type'},
    {title: '链接', dataIndex: 'href', minWidth: 200, align: 'left'},
    {title: '目标', dataIndex: 'target', minWidth: 120, align: 'left'},
    {title: '权限标识', dataIndex: 'permission', minWidth: 200, align: 'left'},
    {title: '排序', dataIndex: 'sort', minWidth: 60, align: 'center'},
    {
      title: '可见',
      dataIndex: 'is_show',
      minWidth: 120,
      align: 'center',sorter:'false',
      status: {
        enableText:'是',
        disableText:'否'
      },
    },
    {
      title: '移动端菜单',
      dataIndex: 'app_menu',
      minWidth: 120,
      align: 'center',sorter:'false',
      status: {
        enableText:'是',
        disableText:'否'
      },
    },
    {title: '备注信息', dataIndex: 'remarks', minWidth: 60, align: 'left'},
  ]
})
const clickRow = ({value, row, index}) => {
  if ('copy' === value) {
    let newRow = {...row}
    delete newRow.id
    delete newRow.createBy
    delete newRow.create_date
    delete newRow.updateBy
    delete newRow.update_date
    newRow.formNo = 'sys_menu'
    newRow.name = newRow.name + '(复制)'
    confirmAction('操作确认', '确定复制该菜单吗？', saveDataUrl, newRow, () => {
      instance.refs.treeView.loadData()
    })
  }else if ('copyWithItem' === value) {
    let newRow = {...row}
    delete newRow.id
    delete newRow.createBy
    delete newRow.create_date
    delete newRow.updateBy
    delete newRow.update_date
    delete newRow.children
    newRow.formNo = 'sys_menu'
    newRow.name = newRow.name + '(复制)'
    let children = JSON.parse(JSON.stringify(row.children))
    children.forEach(item => {
      delete item.id
      delete item.createBy
      delete item.create_date
      delete item.updateBy
      delete item.update_date
      delete item.parent
      delete item.parent_ids
      item.formNo = 'sys_menu'
    })
    newRow.sys_menu = children
    confirmAction('操作确认', '确定复制该菜单吗？', saveDataUrl, newRow, () => {
      instance.refs.treeView.loadData()
    })
  }
}
</script>
<style scoped>
</style>
