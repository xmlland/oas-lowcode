<template>
  <u-form v-model:value="formModel"
    :showActLog="true" v-model:actRuleArgs="actRuleArgs" v-model:disabled="disabled" :labelWidth="100" formNo="oas_leave_apply"  >
    <a-row :gutter="16">
      <a-col :span="12" v-if="isFormItemShow(actRuleArgs,'apply_no')">
        <a-form-item name="apply_no" label="申请单号" :rules="[{ required: isFormItemRequire(actRuleArgs,'apply_no'), message: '请输入申请单号' }]">
          <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'apply_no')" defaultValue="" v-model:value="formModel.apply_no" placeholder="请输入申请单号" :maxlength="64" />
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="isFormItemShow(actRuleArgs,'applicant.id')">
        <a-form-item name="applicant" label="申请人" :rules="[{ required: isFormItemRequire(actRuleArgs,'applicant.id'), message: '请选择申请人' }]">
          <u-user-select :disabled="disabled || isFormItemDisabled(actRuleArgs,'applicant.id')" defaultValue="${currentUser}" v-model:value="formModel.applicant" :dataScope="all" :modalWidth="1000" placeholder="请选择申请人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="isFormItemShow(actRuleArgs,'leave_type')">
        <a-form-item name="leave_type" label="假期类型" :rules="[{ required: isFormItemRequire(actRuleArgs,'leave_type'), message: '请选择假期类型' }]">
          <u-select :disabled="disabled || isFormItemDisabled(actRuleArgs,'leave_type')" form-type="select" defaultValue="" v-model:value="formModel.leave_type" dict-type="oas_leave_apply_leave_type" placeholder="请选择假期类型"/>
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="isFormItemShow(actRuleArgs,'start_time')">
        <a-form-item name="start_time" label="开始时间" :rules="[{ required: isFormItemRequire(actRuleArgs,'start_time'), message: '请选择开始时间' }]">
          <u-date :disabled="disabled || isFormItemDisabled(actRuleArgs,'start_time')" defaultValue="" v-model:value="formModel.start_time" formatPatter="yyyy-MM-dd HH:mm" maxValue="" minValue="" placeholder="请选择开始时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="isFormItemShow(actRuleArgs,'end_time')">
        <a-form-item name="end_time" label="结束时间" :rules="[{ required: isFormItemRequire(actRuleArgs,'end_time'), message: '请选择结束时间' }]">
          <u-date :disabled="disabled || isFormItemDisabled(actRuleArgs,'end_time')" defaultValue="" v-model:value="formModel.end_time" formatPatter="yyyy-MM-dd HH:mm" maxValue="" minValue="" placeholder="请选择结束时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="isFormItemShow(actRuleArgs,'duration')">
        <a-form-item name="duration" label="请假时长" :rules="[{ required: isFormItemRequire(actRuleArgs,'duration'), message: '请输入请假时长' },{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled || isFormItemDisabled(actRuleArgs,'duration')" defaultValue="" v-model:value="formModel.duration" placeholder="请输入请假时长"  />
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="isFormItemShow(actRuleArgs,'reason')">
        <a-form-item name="reason" label="请假事由" :rules="[{ required: isFormItemRequire(actRuleArgs,'reason'), message: '请输入请假事由' }]">
          <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'reason')" :textarea="true" defaultValue="" v-model:value="formModel.reason" placeholder="请输入请假事由" :maxlength="255"/>
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="isFormItemShow(actRuleArgs,'handover_to.id')">
        <a-form-item name="handover_to" label="工作交接人" :rules="[{ required: isFormItemRequire(actRuleArgs,'handover_to.id'), message: '请选择工作交接人' }]">
          <u-user-select :disabled="disabled || isFormItemDisabled(actRuleArgs,'handover_to.id')" defaultValue="" v-model:value="formModel.handover_to" :dataScope="all" :modalWidth="1000" placeholder="请选择工作交接人"/>
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="isFormItemShow(actRuleArgs,'attachment')">
        <a-form-item name="attachment" label="附件" :rules="[{ required: isFormItemRequire(actRuleArgs,'attachment'), message: '请上传附件' }]">
          <u-upload :disabled="disabled || isFormItemDisabled(actRuleArgs,'attachment')" v-model:value="formModel.attachment" :fileCount="5" :maxSize="50" />
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="isFormItemShow(actRuleArgs,'status')">
        <a-form-item name="status" label="状态" :rules="[{ required: isFormItemRequire(actRuleArgs,'status'), message: '' }]">
          <u-select :disabled="disabled || isFormItemDisabled(actRuleArgs,'status')" form-type="select" defaultValue="" v-model:value="formModel.status" dict-type="oas_leave_apply_apply_status" placeholder=""/>
        </a-form-item>
      </a-col>
      <!--
      <template v-if="actExtendShow(actRuleArgs)">
        <a-divider/>
        <slot v-if="!disabled" name="actComment" v-bind="formItemStatus(actRuleArgs,'act.comment')"/>
        <slot name="actLog" v-bind="formItemStatus(actRuleArgs,'act.log')"/>
      </template>
      -->
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oas_leave_apply_form",
}
</script>
<script setup>
import {isFormItemShow, isFormItemDisabled, isFormItemRequire} from "@/lib/act/actForm";
import {ref} from "vue";
import * as validator from "@/lib/validator"
let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)
let actRuleArgs = ref({})

</script>
<style scoped>
</style>
