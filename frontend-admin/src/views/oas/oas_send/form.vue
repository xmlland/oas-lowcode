<template>
  <div class="form-container">
    <u-form v-model:value="formModel"
      :showActLog="true" v-model:actRuleArgs="actRuleArgs" v-model:disabled="disabled" :labelWidth="100" formNo="oas_send"  >
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="1" tab="文单"></a-tab-pane>
        <a-tab-pane key="2" tab="正文"></a-tab-pane>
      </a-tabs>
      <div class="tab-content">
        <div v-if="activeKey === '1'" class="document-form-pane">
          <div class="document-paper">
            <div class="document-prelude">
              <span>拟稿单位：<em>{{ formModel.drafting_department || '' }}</em></span>
              <span>编号：<em>{{ formModel.doc_no || '' }}</em></span>
            </div>
            <div class="document-head">
              <div class="document-head-kicker">OAS 公文流转</div>
              <h3 class="u-form-top-title">发文处理笺</h3>
            </div>
            <div class="document-head-meta">
              <span>拟稿：{{ formModel.drafter || '' }}</span>
              <span>核稿：{{ formModel.reviewer || '' }}</span>
              <span>签发：{{ formModel.issuer || '' }}</span>
              <span>归档号：{{ formModel.archive_number || '' }}</span>
            </div>
            <table class="u-form-table">
              <colgroup>
                <col class="label-col" />
                <col class="value-col" />
                <col class="label-col" />
                <col class="value-col" />
              </colgroup>
            <!-- 第一组 - 基本信息 -->
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">标题</span>
              </td>
              <td class="u-form-table-con" colspan="3">
                <a-form-item name="title" :rules="[{ required: isFormItemRequire(actRuleArgs,'title'), message: '请输入标题' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'title')" defaultValue="" v-model:value="formModel.title" placeholder="请输入标题" :maxlength="100" />
                </a-form-item>
              </td>
            </tr>
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">拟稿部门</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="drafting_department" :rules="[{ required: isFormItemRequire(actRuleArgs,'drafting_department'), message: '请输入拟稿部门' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'drafting_department')" defaultValue="" v-model:value="formModel.drafting_department" placeholder="请输入拟稿部门" :maxlength="64" />
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">拟稿人</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="drafter" :rules="[{ required: isFormItemRequire(actRuleArgs,'drafter'), message: '请输入拟稿人' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'drafter')" defaultValue="" v-model:value="formModel.drafter" placeholder="请输入拟稿人" :maxlength="64" />
                </a-form-item>
              </td>
            </tr>
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">核稿人</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="reviewer" :rules="[{ required: isFormItemRequire(actRuleArgs,'reviewer'), message: '请输入核稿人' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'reviewer')" defaultValue="" v-model:value="formModel.reviewer" placeholder="请输入核稿人" :maxlength="64" />
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">签发人</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="issuer" :rules="[{ required: isFormItemRequire(actRuleArgs,'issuer'), message: '请输入签发人' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'issuer')" defaultValue="" v-model:value="formModel.issuer" placeholder="请输入签发人" :maxlength="64" />
                </a-form-item>
              </td>
            </tr>
            <!-- 第二组 - 发文属性 -->
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">文号</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="doc_no" :rules="[{ required: isFormItemRequire(actRuleArgs,'doc_no'), message: '请输入文号' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'doc_no')" defaultValue="" v-model:value="formModel.doc_no" placeholder="请输入文号" :maxlength="64" />
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">发文日期</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="date_of_publication" :rules="[{ required: isFormItemRequire(actRuleArgs,'date_of_publication'), message: '请选择发文日期' }]">
                  <u-date :disabled="disabled || isFormItemDisabled(actRuleArgs,'date_of_publication')" defaultValue="" v-model:value="formModel.date_of_publication" formatPatter="yyyy-MM-dd" maxValue="" minValue="" placeholder="请选择发文日期"/>
                </a-form-item>
              </td>
            </tr>
            <tr v-if="isFormItemShow(actRuleArgs,'sec_level')">
              <td class="td-title">
                <span class="u-form-table-title">密级</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="sec_level" :rules="[{ required: isFormItemRequire(actRuleArgs,'sec_level'), message: '请选择密级' }]">
                  <u-select :disabled="disabled || isFormItemDisabled(actRuleArgs,'sec_level')" form-type="select" defaultValue="" v-model:value="formModel.sec_level" dict-type="sys_sec_level" placeholder="请选择密级"/>
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">密级期限</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="secret_limit" :rules="[{ required: isFormItemRequire(actRuleArgs,'secret_limit'), message: '请输入密级期限' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'secret_limit')" defaultValue="" v-model:value="formModel.secret_limit" placeholder="请输入密级期限" :maxlength="64" />
                </a-form-item>
              </td>
            </tr>
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">紧急程度</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="urgency" :rules="[{ required: isFormItemRequire(actRuleArgs,'urgency'), message: '请选择紧急程度' }]">
                  <u-select :disabled="disabled || isFormItemDisabled(actRuleArgs,'urgency')" form-type="select" defaultValue="" v-model:value="formModel.urgency" dict-type="urgency_degree" placeholder="请选择紧急程度"/>
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">印发份数</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="number_of_copies" :rules="[{ required: isFormItemRequire(actRuleArgs,'number_of_copies'), message: '请输入印发份数' },{ validator : customValidator.numberValidator, message: '请输入整数'}]">
                  <u-input type="number" :disabled="disabled || isFormItemDisabled(actRuleArgs,'number_of_copies')" defaultValue="" v-model:value="formModel.number_of_copies" placeholder="请输入印发份数"  />
                </a-form-item>
              </td>
            </tr>
            <!-- 第三组 - 审批意见（全宽） -->
            <tr class="opinion-row">
              <td class="td-title">
                <span class="u-form-table-title">签发意见</span>
              </td>
              <td class="u-form-table-con" colspan="3">
                <a-form-item name="issuing_opinions" :rules="[{ required: isFormItemRequire(actRuleArgs,'issuing_opinions'), message: '请输入签发意见' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'issuing_opinions')" :textarea="true" defaultValue="" v-model:value="formModel.issuing_opinions" placeholder="请输入签发意见" :maxlength="255"/>
                </a-form-item>
              </td>
            </tr>
            <tr class="opinion-row">
              <td class="td-title">
                <span class="u-form-table-title">部门意见</span>
              </td>
              <td class="u-form-table-con" colspan="3">
                <a-form-item name="department_comments" :rules="[{ required: isFormItemRequire(actRuleArgs,'department_comments'), message: '请输入部门意见' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'department_comments')" :textarea="true" defaultValue="" v-model:value="formModel.department_comments" placeholder="请输入部门意见" :maxlength="255"/>
                </a-form-item>
              </td>
            </tr>
            <tr class="opinion-row">
              <td class="td-title">
                <span class="u-form-table-title">会签意见</span>
              </td>
              <td class="u-form-table-con" colspan="3">
                <a-form-item name="joint_signature" :rules="[{ required: isFormItemRequire(actRuleArgs,'joint_signature'), message: '请输入会签意见' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'joint_signature')" :textarea="true" defaultValue="" v-model:value="formModel.joint_signature" placeholder="请输入会签意见" :maxlength="255"/>
                </a-form-item>
              </td>
            </tr>
            <tr class="opinion-row">
              <td class="td-title">
                <span class="u-form-table-title">领导批示</span>
              </td>
              <td class="u-form-table-con" colspan="3">
                <a-form-item name="leadership" :rules="[{ required: isFormItemRequire(actRuleArgs,'leadership'), message: '请输入领导批示' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'leadership')" :textarea="true" defaultValue="" v-model:value="formModel.leadership" placeholder="请输入领导批示" :maxlength="255"/>
                </a-form-item>
              </td>
            </tr>
            <!-- 第四组 - 归档信息 -->
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">生效日期</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="effective_date" :rules="[{ required: isFormItemRequire(actRuleArgs,'effective_date'), message: '请选择生效日期' }]">
                  <u-date :disabled="disabled || isFormItemDisabled(actRuleArgs,'effective_date')" defaultValue="" v-model:value="formModel.effective_date" formatPatter="yyyy-MM-dd" maxValue="" minValue="" placeholder="请选择生效日期"/>
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">主题词</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="keyword" :rules="[{ required: isFormItemRequire(actRuleArgs,'keyword'), message: '请输入主题词' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'keyword')" defaultValue="" v-model:value="formModel.keyword" placeholder="请输入主题词" :maxlength="64" />
                </a-form-item>
              </td>
            </tr>
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">分发范围</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="distribution_scope" :rules="[{ required: isFormItemRequire(actRuleArgs,'distribution_scope'), message: '请输入分发范围' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'distribution_scope')" defaultValue="" v-model:value="formModel.distribution_scope" placeholder="请输入分发范围" :maxlength="64" />
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">盖章状态</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="stamp_status" :rules="[{ required: isFormItemRequire(actRuleArgs,'stamp_status'), message: '请输入盖章状态' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'stamp_status')" defaultValue="" v-model:value="formModel.stamp_status" placeholder="请输入盖章状态" :maxlength="64" />
                </a-form-item>
              </td>
            </tr>
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">归档编号</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="archive_number" :rules="[{ required: isFormItemRequire(actRuleArgs,'archive_number'), message: '请输入归档编号' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'archive_number')" defaultValue="" v-model:value="formModel.archive_number" placeholder="请输入归档编号" :maxlength="64" />
                </a-form-item>
              </td>
              <td class="td-title"></td>
              <td class="u-form-table-con"></td>
            </tr>
            </table>
          </div>
          <a-row :gutter="16">
            <!--
            <template v-if="actExtendShow(actRuleArgs)">
              <a-divider/>
              <slot v-if="!disabled" name="actComment" v-bind="formItemStatus(actRuleArgs,'act.comment')"/>
              <slot name="actLog" v-bind="formItemStatus(actRuleArgs,'act.log')"/>
            </template>
            -->
          </a-row>
        </div>
        <div v-else-if="activeKey === '2'" class="document-body-pane">
          <div class="document-paper document-attachment-paper">
            <a-form-item name="attachment_upload" label="发文文档" >
              <u-upload :disabled="disabled" v-model:value="formModel.attachment_upload" oss="minio:main:test" :online="true" :fileCount="1" :maxSize="50" />
            </a-form-item>
          </div>
        </div>
      </div>
    </u-form>
  </div>
</template>

<script>
export default {
  name: "oas_send_form",
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
const activeKey = ref('1');

</script>
<style lang="less" scoped>
@document-red: #d71920;
@document-red-deep: #b5121b;
@document-red-soft: #fff6f4;
@document-paper: #fffefa;

.form-container {
  height: 68vh;
  min-height: 600px;
  display: flex;
  flex-direction: column;
}

.form-container>.u-form {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.form-container > .u-form :deep(.ant-tabs) {
  flex-shrink: 0;
  margin: 0 18px;
}

.form-container > .u-form :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.tab-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px 18px 132px;
  background: #f2f2f2;
}

.document-form-pane,
.document-body-pane {
  max-width: 1120px;
  margin: 0 auto;
}

.document-paper {
  position: relative;
  padding: 14px 30px 34px;
  background: @document-paper;
  border: 1px solid #dcd2c4;
  box-shadow: none;
}

.document-paper::before {
  display: none;
}

.document-prelude,
.document-head-meta {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  color: #6f2b2b;
  font-family: "SimSun", "宋体", serif;
  font-size: 13px;
  line-height: 22px;
}

.document-prelude {
  margin: 0 2px 6px;
}

.document-prelude span,
.document-head-meta span {
  flex: 1;
  min-width: 0;
  border-bottom: 1px solid rgba(181, 18, 27, 0.34);
  white-space: nowrap;
}

.document-prelude span:last-child,
.document-head-meta span:last-child {
  flex: 0.8;
}

.document-prelude em {
  color: #333;
  font-style: normal;
}

.document-head {
  position: relative;
  z-index: 1;
  padding: 2px 0 10px;
  margin: 0 2px 7px;
  border-bottom: 4px double @document-red;
  text-align: center;
}

.document-head-kicker {
  margin-bottom: 2px;
  color: @document-red-deep;
  font-size: 13px;
  line-height: 1;
}

.u-form-top-title {
  margin: 0;
  color: @document-red;
  font-family: "SimSun", "宋体", serif;
  font-size: 29px;
  font-weight: 700;
  line-height: 1.25;
  text-align: center;
  letter-spacing: 0;
}

.document-head-meta {
  margin: 0 2px 12px;
}

.u-form-table {
  position: relative;
  z-index: 1;
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  margin: 0 auto;
  background: #fff;
  border: 2px solid @document-red;
  font-family: "SimSun", "宋体", serif;
  font-size: 15px;

  tr td {
    height: 50px;
    border: 1px solid @document-red;
    vertical-align: middle;
  }

  .u-form-table-title {
    display: inline-flex;
    align-items: center;
    min-height: 28px;
    color: @document-red-deep;
    font-weight: 700;
    padding-left: 0;
    justify-content: center;
    width: 100%;
  }

  .ant-form-item {
    margin: 0;
  }
}

.label-col {
  width: 14%;
}

.value-col {
  width: 36%;
}

.td-title {
  background: @document-red-soft;
}

.u-form-table-con {
  padding: 0 14px;
  background: #fff;
}

.u-form-table .opinion-row td {
  height: 88px;
  vertical-align: top;
}

.u-form-table .opinion-row .u-form-table-title {
  padding-top: 12px;
}

.u-form-table .opinion-row .u-form-table-con {
  padding-top: 8px;
}

.u-form-table :deep(.ant-input),
.u-form-table :deep(.ant-input-affix-wrapper),
.u-form-table :deep(.ant-input-number),
.u-form-table :deep(.ant-input-number-input-wrap),
.u-form-table :deep(.ant-picker),
.u-form-table :deep(.ant-select-selector) {
  border: 0 !important;
  border-color: transparent !important;
  border-radius: 0 !important;
  background: #fff !important;
  box-shadow: none !important;
}

.u-form-table :deep(.ant-input),
.u-form-table :deep(.ant-input-affix-wrapper),
.u-form-table :deep(.ant-input-number-input),
.u-form-table :deep(.ant-picker),
.u-form-table :deep(.ant-select-selector) {
  padding-left: 0 !important;
  padding-right: 0 !important;
}

.u-form-table :deep(.ant-input),
.u-form-table :deep(.ant-input-number),
.u-form-table :deep(.ant-picker),
.u-form-table :deep(.ant-select-selector) {
  height: 48px !important;
}

.u-form-table :deep(.ant-input-number-input),
.u-form-table :deep(.ant-picker-input),
.u-form-table :deep(.ant-select-selection-search-input) {
  height: 46px !important;
}

.u-form-table :deep(.ant-input),
.u-form-table :deep(.ant-picker-input > input),
.u-form-table :deep(.ant-select-selection-placeholder),
.u-form-table :deep(.ant-select-selection-item) {
  font-family: "SimSun", "宋体", serif;
  font-size: 15px;
}

.u-form-table :deep(.ant-select),
.u-form-table :deep(.ant-picker),
.u-form-table :deep(.ant-input-number) {
  width: 100%;
}

.u-form-table :deep(.ant-input:hover),
.u-form-table :deep(.ant-input:focus),
.u-form-table :deep(.ant-input-focused),
.u-form-table :deep(.ant-input-affix-wrapper:hover),
.u-form-table :deep(.ant-input-affix-wrapper-focused),
.u-form-table :deep(.ant-picker:hover),
.u-form-table :deep(.ant-picker-focused),
.u-form-table :deep(.ant-select-focused .ant-select-selector) {
  background: rgba(255, 246, 244, 0.55) !important;
  border-color: transparent !important;
}

.u-form-table :deep(.ant-input::placeholder),
.u-form-table :deep(.ant-picker-input > input::placeholder),
.u-form-table :deep(.ant-select-selection-placeholder) {
  color: #b9a4a4;
}

.u-form-table :deep(textarea.ant-input) {
  min-height: 78px !important;
  padding-top: 10px !important;
  resize: vertical;
}

.document-attachment-paper {
  min-height: 360px;

  :deep(.ant-form-item) {
    position: relative;
    z-index: 1;
    margin-bottom: 0;
  }

  :deep(.ant-form-item-label > label) {
    color: @document-red-deep;
    font-weight: 500;
  }
}

@media (max-width: 768px) {
  .form-container {
    min-height: 560px;
  }

  .tab-content {
    padding: 14px 12px 24px;
  }

  .document-paper {
    padding: 24px 18px 28px;
  }

  .u-form-top-title {
    font-size: 24px;
  }

  .document-head-meta {
    gap: 10px;
    flex-wrap: wrap;
  }

  .u-form-table {
    font-size: 14px;
  }

  .label-col {
    width: 20%;
  }

  .value-col {
    width: 30%;
  }
}
</style>
