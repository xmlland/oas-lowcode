<template>
  <SingleTableView ref="tableView" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="子系统"
                   :class="useModernListSkin ? 'modern-list-page sys-subsystem-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   @clickRow="clickRow"
                   :buttons="buttons" formNo="sys_subsystem">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">子系统管理</h2>
        <p class="modern-list-query-desc">维护业务子系统、访问地址和菜单分配关系，支撑多系统入口管理。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="name" label="名称" type="input" :props="{placeholder:'名称' }"></QueryField>
    </template>
  </SingleTableView>
  <u-modal ref="menuModal" :width="1200" :customOK="true" @clickOk="saveMenu" @saveSuccess="saveSuccess"
           :customBodyStyle="{height:'70vh',overflow:'auto'}">
    <menu-form ref="menuForm" :system-id="currentRow.id"/>
  </u-modal>
</template>

<script lang="jsx">
export default {
  name: "sys_subsystem_list",
}
</script>
<script setup lang="jsx">
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/admin/sys_subsystem/form";
import MenuForm from "@/views/admin/sys_subsystem/menuForm";
import {saveDataUrl} from "@/api/api";
import {postAction} from "@/api/action";
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
  permission: 'app:sys_subsystem:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:sys_subsystem:del'
}])

let singleTable = ref({
  optionWidth: 220,
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:sys_subsystem:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:sys_subsystem:edit'
    }, {
      value: 'menu',
      text: '分配菜单',
      permission: 'app:sys_subsystem:edit'
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:sys_subsystem:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: '编码', dataIndex: 'code', minWidth: 120, align: 'left'},
    {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '访问地址', dataIndex: 'baseurl', minWidth: 120, align: 'left',customRenderSlot:({text, record, index})=>{
      return <a-button type="link" onclick={()=>{
        window.open(text)}
      }>
        {text}
      </a-button>
    }},
    {title: '排序', dataIndex: 'sort', minWidth: 120, align: 'center'},
  ]
})
const saveSuccess = () => {
  instance.refs.tableView.saveSuccess()
}
let currentRow = ref({})
const clickRow = ({value, row, index}) => {
  if ('menu' === value) {
    currentRow.value = row
    instance.refs.menuModal.open('【分配菜单】', row)
  }
}

const saveMenu = (callback) => {
  let checkData = instance.refs.menuForm.getCheckData()
  let data = {
    id: checkData.id,
    formNo: 'sys_subsystem',
    sys_subsystem_menu: checkData.ids.split(',').map(item => {
      return {
        menu_id: item,
        subsystem_id: checkData.id,
        formNo: 'sys_subsystem_menu',
      }
    })
  }
  checkData.deleteIdArr.forEach(item => {
    data.sys_subsystem_menu.push({
      id: item,
      formNo: 'sys_subsystem_menu',
      del_flag: '1'
    })
  })

  callback(postAction, saveDataUrl, data)
}
</script>
<style scoped>
</style>
