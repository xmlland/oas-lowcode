<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled"  :labelWidth="180" formNo="sys_menu">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="parent" label="上级菜单" :validateFirst="true" :rules="[{ required: formModel.quickPermission, message: '请选择上级菜单' }]">
          <u-tree-select formNo="sys_menu" :disabled="disabled" defaultValue="" v-model:value="formModel.parent" placeholder="请选择上级菜单"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="名称" :validateFirst="true" :rules="[{ required: true, message: '请输入名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="icon" label="图标" :validateFirst="true">
          <u-icon-select :disabled="disabled" defaultValue="" v-model:value="formModel.icon" placeholder="请选择图标"/>
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.icon" placeholder="请输入图标"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name_en" label="英文名称" :validateFirst="true" :rules="[{ required: true, message: '请输入英文名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name_en" placeholder="请输入英文名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="type_" label="菜单级别" :validateFirst="true" :rules="[{ required: true, message: '请选择菜单级别' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.type_" dict-type="sys_menu_type" placeholder="请选择菜单级别"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="is_show" label="可见" :validateFirst="true" :rules="[{ required: true, message: '请选择可见' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.is_show" dict-type="yes_no" placeholder="请选择可见"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="href" label="链接">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.href" placeholder="请输入链接"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="target" label="目标">
          <template #extra>
            <span class="target-item-span" v-bind:key="index" v-for="(item,index) in targetList">
              <a-popover title="" trigger="click" placement="right">
                <template #content>
                  {{item.label}}
                </template>
                <question-circle-outlined />
              </a-popover>
              <a-typography-paragraph copyable>{{item.value}}</a-typography-paragraph>
            </span>
          </template>
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.target" placeholder="请输入目标"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="permission" label="权限标识">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.permission" placeholder="请输入权限标识"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sign" label="按钮标识">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.sign" placeholder="请输入按钮标识"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="opening_mode" label="打开方式">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.opening_mode" dict-type="sys_opening_mode" placeholder="请选择打开方式"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="app_menu" label="移动端菜单" :rules="[{ required: false, message: '选择是否在移动端显示' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.app_menu" dict-type="yes_no" placeholder="选择是否在移动端显示"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="remarks" label="备注信息">
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.remarks" placeholder="请输入备注信息"/>
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="showWorkflowMenu">
        <a-form-item name="is_workflow_menu" label="创建流程菜单" :validateFirst="true" :rules="[{ required: true, message: '是否创建流程菜单' }]">
          <u-select :disabled="disabled" defaultValue="1" form-type="radio" v-model:value="formModel.is_workflow_menu" dict-type="yes_no" placeholder="是否创建流程菜单"/>
        </a-form-item>
      </a-col>
      <template v-if="showWorkflowMenu">
        <single-table-view :height="300" :query-button="false" :row-edit="true" :single-table="singleTableMenu" :buttons="[]">

        </single-table-view>
      </template>
      <template v-if="formModel.quickPermission">
        <single-table-view :height="500" :query-button="false" form-no="sys_menu" :row-edit="true" :row-edit-default-row="{type_:'4',is_show:'1',formNo:'sys_menu'}" :single-table="singleTable">

        </single-table-view>
      </template>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_menu_form",
}
</script>
<script setup>

import {computed, ref, watch} from "vue";
import * as validator from "@/lib/validator"
import SingleTableView from "@/components/view/SingleTableView";
import {getAction} from "@/api/action";

let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)

const targetList = [
  {value: '_blank', label: '新标签页'},
  {value: 'DynamicList', label: '动态列表 需在链接中填写表单管理的id'},
  {value: 'newWindow', label: '浏览器弹窗打开嵌套链接中的地址 可用|分割定义宽高  例如 https://www.baidu.com/|width=500,height=800'},
  {value: 'iframe', label: '通过iframe嵌套链接中的地址'},
  {value: 'home', label: '系统首页 需在链接中填写对应的页面'},
  {value: 'home inMenu', label: '系统首页在菜单中显示 需在链接中填写对应的页面'},
  {value: 'router', label: '仅在路由表生效,不显示在菜单中 需在链接中填写对应的页面'},
  {value: 'PlatformLayout', label: '平台页'},
  {value: 'PlatformBlankLayout', label: '平台页 需在链接中填写对应的页面'},
  {value: 'modifyPwd', label: '调用系统方法-修改密码'},
  {value: 'exitSystem', label: '调用系统方法-退出系统'},
  {value: 'newTab', label: '新标签页不带当前登录信息   目前仅一些高版本的360浏览器支持  如果是多个开发框架项目的跳转 还需要修改window[\'tokenKey\']'},
]

let singleTable = computed(()=>{
  return {
    disableInitLoad: true,
    pagination: false,
    showIndex: false,
    showSorter: false,
    unSaveRowsLocation: 'after',
    showRowButtons: false,
    data: formModel.value.permissionArr || [],
    columns: [
      {title: '名称', dataIndex: 'name', minWidth: 120, editor: {type: 'input', rules: [{required: true, message: ''}]}},
      {title: '英文名称', dataIndex: 'name_en', minWidth: 120, editor: {type: 'input', rules: [{required: true, message: ''}]}},
      {title: '权限标识', dataIndex: 'permission', minWidth: 120, editor: {type: 'input', rules: [{required: true, message: ''}]}},
      {title: '排序', dataIndex: 'sort', minWidth: 120, editor: {type: 'input', props: {type: 'digits'}, rules: [{required: true, message: ''}]}},
    ]
  }
})
let singleTableMenu = computed(()=>{
  return {
    rowSelection:false,
    disableInitLoad: true,
    pagination: false,
    showIndex: false,
    showSorter: false,
    unSaveRowsLocation: 'after',
    showRowButtons: false,
    data: formModel.value['workflow_menu'],
    columns: [
      {title: '流程菜单', dataIndex: 'user_task_name', minWidth: 120, editor: {type: 'input', rules: [{required: true, message: ''}]}},
      {title: '排序', dataIndex: 'sort', minWidth: 120, editor: {type: 'input', props: {type: 'digits'}, rules: [{required: true, message: ''}]}},
    ]
  }
})
watch(()=> formModel.value.processDefinitionCategory, ()=>{
  getAction('sys/menu/getWorkflowMenuList', {processDefinitionCategory: formModel.value.processDefinitionCategory}).then(hisRes => {
    formModel.value['workflow_menu'] = hisRes.data.workflow_menu
  })
})
let showWorkflowMenu = computed(()=>{
  return !!formModel.value.processDefinitionCategory
})
</script>
<style lang="less" scoped>
:deep(.ant-form-item-extra){
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-start;
}

.target-item-span {
  margin-right: 10px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  .ant-typography{
    margin-bottom: 0;
  }
}
</style>
