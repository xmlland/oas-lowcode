<template>
  <a-menu v-model:selectedKeys="menuSelectedKeys" mode="horizontal" v-if="formType !== 'add'">
    <a-menu-item key="info" @click="menuClick('info')">内容信息</a-menu-item>
    <a-menu-item key="record" @click="menuClick('record')">阅读记录</a-menu-item>
  </a-menu>
  <div v-show="currentMenuType === 'info'" style="height: 640px;padding-top: 8px">
    <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="oa_sys_announcement" @change="formChange"  >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item name="org_id" label="所属机构" :rules="[{ required: true, message: '请选择所属机构' }]">
            <u-office-select :disabled="disabled" defaultValue="${currentCompany}" v-model:value="formModel.org_id" placeholder="请选择所属机构"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="sender" label="发送人" :rules="[{ required: true, message: '请选择发送人' }]">
            <u-user-select :disabled="disabled" defaultValue="${currentUser}" v-model:value="formModel.sender" placeholder="请选择发送人"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="title" label="消息标题" :rules="[{ required: true, message: '请输入消息标题' }]">
            <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.title" placeholder="请输入消息标题"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="automatically_pop" label="未读是否自动弹出" :rules="[{ required: true, message: '请选择未读是否自动弹出' }]">
            <u-select :disabled="disabled" form-type="radio" defaultValue="" v-model:value="formModel.automatically_pop" dict-type="yes_no" placeholder="请选择未读是否自动弹出" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="send_time" label="发送时间" >
            <u-date :disabled="disabled" defaultValue="${currentTime}" v-model:value="formModel.send_time" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择发送时间"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="receiving_roles" label="接收角色" >
            <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.receiving_roles" type="table" dict-type="sys_role" valueField="id" textField="name" tableOrderBy="" :tableFilterData="[]" placeholder="请选择接收角色"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="receiving_users" label="接收人员" >
            <u-users-select :disabled="disabled" defaultValue="" v-model:value="formModel.receiving_users" :dataScope="all" :modalWidth="1200" placeholder="请选择人员选择"/>
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item name="message_color" label="消息颜色" >
            <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.message_color" dict-type="color" placeholder="请选择消息颜色"/>
          </a-form-item>
        </a-col>
        <!--      <a-col :span="12">-->
        <!--        <a-form-item name="menu_href" label="菜单链接" >-->
        <!--          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.menu_href" placeholder="请输入菜单链接"/>-->
        <!--        </a-form-item>-->
        <!--      </a-col>-->
        <!--      <a-col :span="12">-->
        <!--        <a-form-item name="menu_name" label="菜单名称" >-->
        <!--          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.menu_name" placeholder="请输入菜单名称"/>-->
        <!--        </a-form-item>-->
        <!--      </a-col>-->
        <!--      <a-col :span="12">-->
        <!--        <a-form-item name="record_id" label="记录ID" >-->
        <!--          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.record_id" placeholder="请输入记录ID"/>-->
        <!--        </a-form-item>-->
        <!--      </a-col>-->
        <!--      <a-col :span="12">-->
        <!--        <a-form-item name="menu_name_en" label="菜单英文名称" >-->
        <!--          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.menu_name_en" placeholder="请输入菜单英文名称"/>-->
        <!--        </a-form-item>-->
        <!--      </a-col>-->
        <a-col :span="24">
          <a-form-item name="content_" label="消息内容" :validateFirst="true" >
            <u-tinymce :disabled="disabled" defaultValue="" v-model:value="formModel.content_"/>
          </a-form-item>
        </a-col>
      </a-row>
    </u-form>
  </div>
  <div v-if="currentMenuType === 'record'" style="height: 640px;padding-top: 8px">
    <SingleTableView ref="recordTableView" :modalFull="0" :queryAreaShow="false"
                     :queryButton="true" :singleTable="singleTable" :buttons="[]" formNo="oa_sys_announcement_read" >
    </SingleTableView>
  </div>
</template>

<script>
export default {
  name: "oa_sys_announcement_form",
}
</script>
<script setup>

import {ref,computed} from "vue";
let formModel = ref({})
let disabled = ref(false)

let formType = ref('add');

const formChange = (e) =>{
  if(e.id){
    if(disabled.value){
      formType.value = 'view'
    }else{
      formType.value = 'edit'
    }
  }
}

let menuSelectedKeys = ref(['info']);
let currentMenuType = ref('info');
const menuClick = (e) =>{
  currentMenuType.value = e;
}

let singleTable = computed(() => {
  return {
    rowButtons: [],
    pagination:false,
    rowSelection:false,
    initParam:{
      'a.announcement_id':formModel.value.id
    },
    columns: [
      {title:'时间',dataIndex:'create_date',minWidth:80,align:'center',sorter:'false',ellipsis:false},
      {title:'姓名',dataIndex:'user_name',minWidth:80,align:'left',sorter:'false',ellipsis:false},
      {title:'单位',dataIndex:'office_name',minWidth:120,align:'left',sorter:'false',ellipsis:false},
    ]
  }
})
</script>
<style scoped>
</style>
