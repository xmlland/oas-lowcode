<template>
  <u-form v-model:value="formModel"
    extendClass="modern-generated-form demo_sigletable-form-view"
    v-model:disabled="disabled" :labelWidth="120" formNo="demo_sigletable"   :pkColumnName="id">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="serial_no" label="编号" :rules="[{ required: true, message: '请输入编号' }]">
          <u-serial-no :disabled="disabled" defaultValue="" v-model:value="formModel.serial_no" prefix="BH" placeholder="请输入编号"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="名称" :rules="[{ required: true, message: '请输入名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入名称" :maxlength="255" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="explain_" label="说明" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.explain_" placeholder="请输入说明" :maxlength="500"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="quantity" label="数量" :rules="[{ validator : customValidator.numberValidator, message: '请输入整数'}]">
          <u-input type="number" :disabled="disabled" defaultValue="" v-model:value="formModel.quantity" placeholder="请输入数量"  />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="standard_limit_value" label="标准限值" :rules="[{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.standard_limit_value" placeholder="请输入标准限值"  />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="app_form_code" label="APP表单分组" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.app_form_code" dict-type="app-form-group" placeholder="请选择APP表单分组"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="available" label="是否可用" >
          <u-select :disabled="disabled" form-type="radio" defaultValue="" v-model:value="formModel.available" dict-type="yes_no" placeholder="请选择是否可用" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="monitoring" label="监测指标" >
          <u-select :disabled="disabled" :multiple="true" form-type="checkbox" defaultValue="" v-model:value="formModel.monitoring" dict-type="monitor_index" placeholder="请选择监测指标" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="init_date" label="初始日期" >
          <u-date :disabled="disabled" defaultValue="${currentDate}" v-model:value="formModel.init_date" formatPatter="yyyy-MM-dd" maxValue="" minValue="" placeholder="请选择初始日期"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="start_date" label="开始日期" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.start_date" formatPatter="yyyy-MM-dd" :maxValue="formModel.end_date" minValue="2025-01-01" placeholder="请选择开始日期"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="end_date" label="结束日期" >
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.end_date" formatPatter="yyyy-MM-dd" maxValue="2029-12-31" :minValue="formModel.start_date" placeholder="请选择结束日期"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="person_id" label="负责人" >
          <u-user-select :disabled="disabled" defaultValue="${currentUser}" v-model:value="formModel.person_id" :dataScope="all" :modalWidth="1000" :hideLoginName="true" placeholder="请选择负责人"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="group_members" label="小组成员" >
          <u-users-select :disabled="disabled" defaultValue="" v-model:value="formModel.group_members" :dataScope="all" :modalWidth="1200" placeholder="请选择小组成员"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="department_id" label="所属部门" >
          <u-office-select :disabled="disabled" defaultValue="" v-model:value="formModel.department_id" placeholder="请选择所属部门"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="administrative" label="行政区" >
          <u-area :disabled="disabled" defaultValue="${currentCompanyArea}" v-model:value="formModel.administrative" :freeChoice="false" :showRank="2" :rootAreaId="1" placeholder="请选择行政区"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="post" label="岗位" >
          <u-modal-select formNo="sys_post"
                          :searchKey="['code','name']"
                          :searchLabel="['岗位代码','岗位名称']"
                          :columns="[
                           {title:'岗位代码',dataIndex:'code',align:'center'},
                           {title:'岗位名称',dataIndex:'name',align:'left'},
                           {title:'排序',dataIndex:'sort',align:'center'},
                          ]"
                          :modalWidth="1200" :modalTitle="请选择" :nameDataIndex="name" 
                          :filterData="[{type:'eq', value:'1', key:'a.useable'}]" 
                          :searchConfig="{code:{type:'input', queryType:'in', props:{  }}}" 
                          :formUpdateMap={}
                          v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.post" placeholder="请选择岗位"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="switch_" label="开关" >
          <u-switch :disabled="disabled" defaultValue="false" v-model:value="formModel.switch_"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="online_doc" label="在线文档" >
          <u-upload :online="true" :disabled="disabled" v-model:value="formModel.online_doc" :fileCount="1" :maxSize="50" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="enclosure" label="附件" >
          <u-upload :disabled="disabled" v-model:value="formModel.enclosure" :fileCount="5" :maxSize="50" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="picture" label="图片" >
          <u-upload :picture="true" :disabled="disabled" v-model:value="formModel.picture" :fileCount="5" :maxSize="50" />
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "demo_sigletable_form",
}
</script>
<script setup>

import {ref} from "vue";
import * as validator from "@/lib/validator"
let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
