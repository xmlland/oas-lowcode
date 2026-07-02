<template>
  <u-form v-model:value="formModel"
    :showActLog="true" v-model:actRuleArgs="actRuleArgs" v-model:disabled="disabled" :labelWidth="100" formNo="demo_ticket"  >
    <a-row :gutter="16">
      <a-col :span="24" v-if="isFormItemShow(actRuleArgs,'title')">
        <a-form-item name="title" label="工单标题" :rules="[{ required: isFormItemRequire(actRuleArgs,'title'), message: '请输入工单标题' }]">
          <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'title')" defaultValue="" v-model:value="formModel.title" placeholder="请输入工单标题" :maxlength="100" />
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="isFormItemShow(actRuleArgs,'description')">
        <a-form-item name="description" label="问题描述" :validateFirst="true" :rules="[{ required: isFormItemRequire(actRuleArgs,'description'), message: '请输入问题描述' }]">
          <u-tinymce :disabled="disabled || isFormItemDisabled(actRuleArgs,'description')" defaultValue="" v-model:value="formModel.description"/>
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="isFormItemShow(actRuleArgs,'reply')">
        <a-form-item name="reply" label="回复信息" :validateFirst="true" :rules="[{ required: isFormItemRequire(actRuleArgs,'reply'), message: '请输入回复信息' }]">
          <u-tinymce :disabled="disabled || isFormItemDisabled(actRuleArgs,'reply')" defaultValue="" v-model:value="formModel.reply"/>
        </a-form-item>
      </a-col>
      <a-col :span="12" v-if="isFormItemShow(actRuleArgs,'issue_resolved')">
        <a-form-item name="issue_resolved" label="问题是否解决" :rules="[{ required: isFormItemRequire(actRuleArgs,'issue_resolved'), message: '请选择问题是否解决' }]">
          <u-select :disabled="disabled || isFormItemDisabled(actRuleArgs,'issue_resolved')" form-type="radio" defaultValue="" v-model:value="formModel.issue_resolved" dict-type="demo_ticket_issue_resolved" placeholder="请选择问题是否解决" />
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
  name: "demo_ticket_form",
}
</script>
<script setup>
import {isFormItemShow, isFormItemDisabled, isFormItemRequire} from "@/lib/act/actForm";
import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)
let actRuleArgs = ref({})

</script>
<style scoped>
</style>
