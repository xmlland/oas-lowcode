<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="sys_role" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="enname" label="英文名称" :validateFirst="true" :rules="[{ required: true, message: '请输入英文名称' },
        uniqueValidator('sys_role','英文名称',formModel.id)]">
          <u-input :disabled="disabled" v-model:value="formModel.enname" placeholder="请输入英文名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="名称" :validateFirst="true" :rules="[{ required: true, message: '请输入名称' }]">
          <u-input :disabled="disabled" v-model:value="formModel.name" placeholder="请输入名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="is_sys" label="系统角色" :validateFirst="true" :rules="[{ required: true, message: '请选择系统角色' }]">
          <u-select :disabled="disabled" form-type="select" v-model:value="formModel.is_sys" dict-type="yes_no" placeholder="请选择系统角色"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="useable" label="启用" :validateFirst="true" :rules="[{ required: true, message: '请选择启用' }]">
          <u-select :disabled="disabled" form-type="select" v-model:value="formModel.useable" dict-type="yes_no" placeholder="请选择启用"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="sort" label="序号" :validateFirst="true" :rules="[{ required: true, message: '请输入序号' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" v-model:value="formModel.sort" placeholder="请输入序号"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="remarks" label="备注" >
          <u-input :disabled="disabled" v-model:value="formModel.remarks" placeholder="请输入备注"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="dictViewList01" label="角色成员" :validateFirst="true" :rules="[{ required: false, message: '请选择角色成员' }]">
          <u-modal-multi-select formNo="sys_user"
                                :searchKey="['b.parent_ids','a.login_name','a.name']" :searchLabel="['归属机构','登录名','姓名']"
                                :modalWidth="1200"
                                :searchConfig="{
                                  'b.parent_ids':{
                                    type:'office-select',
                                    queryType:'like',
                                    formatValue:(val)=>{
                                      if(val){
                                        return val.id
                                      }
                                      return ''
                                    }
                                  }
                                }"
                                :filterData="[{key:'a.useable',type:'eq',value:'1'}]"
                                :columns="[
                                  {title: '归属机构', dataIndex: ['parent','name'], align: 'left'},
                                  {title: '登录名', dataIndex:'login_name',align: 'left'},
                                  {title: '姓名', dataIndex:'name',align: 'left'},
                                ]"
                                codeFiled="login_name"
                                nameFiled="name"
                                :format="item=>{
                                  if (item.code) { return `${item.name} （${item.code}）` } return item.name
                                }"
                                :disabled="disabled" v-model:value="formModel.dictViewList01"
                                placeholder="请选择角色成员"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="dictViewList02" label="数据角色" :validateFirst="true" :rules="[{ required: false, message: '请选择数据角色' }]">
          <u-modal-multi-select formNo="sys_datarole"
                                :searchKey="['a.enname','a.name']" :searchLabel="['编码','名称']"
                                :modalWidth="1200"
                                dsfPlus=""
                                :columns="[
                                  {title: '编码', dataIndex:'enname',align: 'left'},
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
  name: "sys_role_form",
}
</script>
<script setup>
import {uniqueValidator} from "@/lib/validator";
import {ref} from "vue";
import * as validator from "@/lib/validator"
let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
