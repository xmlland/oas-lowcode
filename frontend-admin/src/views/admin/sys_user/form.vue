<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_user" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="parent" label="归属机构" :validateFirst="true" :rules="[{ required: true, message: '请选择归属机构' }]">
          <u-tree-select formNo="sys_office" :disabled="disabled" defaultValue="" v-model:value="formModel.parent" placeholder="请选择归属机构"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="login_name" label="登录名" :validateFirst="true" :rules="[{ required: true, message: '请输入登录名' },
        uniqueValidator('sys_user','登录名',formModel.id)]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.login_name" placeholder="请输入登录名"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="password_expired_date" label="密码有效日期" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.password_expired_date" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择密码有效日期"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="no" label="用户编号" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.no" placeholder="请输入用户编号"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="姓名" :validateFirst="true" :rules="[{ required: true, message: '请输入姓名' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入姓名"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="mobile" label="手机" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.mobile" placeholder="请输入手机"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="phone" label="电话" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.phone" placeholder="请输入电话"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="email" label="邮箱" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.email" placeholder="请输入邮箱"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="user_type" label="密级" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.user_type" dict-type="sys_sec_level" placeholder="请选择密级"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="level_id" label="职务" >
          <u-modal-select formNo="sys_level"
                          searchKey="name" searchLabel="名称"
                          :columns="[
                          {title: '名称', dataIndex:'name',align: 'left'},
                          {title: '备注', dataIndex:'remarks',align: 'left'},
                          ]"
                          :formUpdateMap="{'targetInputName':'gridFieldKey'}" v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.level_id" placeholder="请选择职务"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="post_id" label="主岗" >
          <u-modal-select formNo="sys_post"
                          searchKey="a.name" searchLabel="名称"
                          :columns="[
                          {title: '名称', dataIndex:'name',align: 'left'},
                          {title: '备注', dataIndex:'remarks',align: 'left'},
                          ]"
                          :formUpdateMap="{'targetInputName':'gridFieldKey'}" v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.post_id" placeholder="请选择主岗"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="part_post_id" label="副岗" >
          <u-modal-select formNo="sys_post"
                          searchKey="name" searchLabel="名称"
                          :columns="[
                          {title: 'a.名称', dataIndex:'name',align: 'left'},
                          {title: '备注', dataIndex:'remarks',align: 'left'},
                          ]"
                          :formUpdateMap="{'targetInputName':'gridFieldKey'}" v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.part_post_id" placeholder="请选择副岗"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="login_flag" label="是否允许登录" :validateFirst="true" :rules="[{ required: true, message: '请选择是否允许登录' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.login_flag" dict-type="yes_no" placeholder="请选择是否允许登录"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sso_login_flag" label="仅域控登录" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.sso_login_flag" dict-type="yes_no" placeholder="请选择仅域控登录"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="useable" label="状态" :validateFirst="true" :rules="[{ required: true, message: '请选择状态' }]">
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.useable" dict-type="sys_useable" placeholder="请选择状态"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="排序" :validateFirst="true" :rules="[{ required: true, message: '请输入排序' }]">
          <u-input :disabled="disabled" defaultValue="10" v-model:value="formModel.sort" placeholder="请输入排序"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="remarks" label="备注信息" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.remarks" placeholder="请输入备注信息"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="dictViewList01" label="系统角色" :validateFirst="true" :rules="[{ required: false, message: '请选择系统角色' }]">
          <u-modal-multi-select formNo="sys_role"
                                :searchKey="['a.enname','a.name']" :searchLabel="['英文名称','名称']"
                                :modalWidth="1200"
                                :filterData="[{key:'a.useable',type:'eq',value:'1'}]"
                                :columns="[
                                  {title: '英文名称', dataIndex:'enname',align: 'left'},
                                  {title: '名称', dataIndex:'name',align: 'left'},
                                  {title: '排序', dataIndex:'sort',align: 'left'},
                                ]"
                                codeFiled="enname"
                                nameFiled="name"
                                :format="item=>{
                                  if (item.code) { return `${item.name} （${item.code}）` } return item.name
                                }"
                                :disabled="disabled" v-model:value="formModel.dictViewList01"
                                placeholder="请选择系统角色"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="dictViewList02" label="数据角色" :validateFirst="true" :rules="[{ required: false, message: '请选择数据角色' }]">
          <u-modal-multi-select formNo="sys_datarole"
                                searchKey="a.name" searchLabel="名称"
                                :modalWidth="1200"
                                :filterData="[{key:'a.useable',type:'eq',value:'1'}]"
                                :columns="[
                                  {title: '英文名称', dataIndex:'enname',align: 'left'},
                                  {title: '名称', dataIndex:'name',align: 'left'},
                                ]"
                                codeFiled="enname"
                                nameFiled="name"
                                :format="item=>{
                                  if (item.code) { return `${item.name} （${item.code}）` } return item.name
                                }"
                                :disabled="disabled" v-model:value="formModel.dictViewList02"
                                placeholder="请选择数据角色"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "sys_user_form",
}
</script>
<script setup>
import {uniqueValidator} from "@/lib/validator";
import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
