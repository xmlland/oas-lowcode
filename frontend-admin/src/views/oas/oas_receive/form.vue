<template>
  <div class="form-container">
    <u-form v-model:value="formModel"
      :showActLog="true" v-model:actRuleArgs="actRuleArgs" v-model:disabled="disabled" :labelWidth="100" formNo="oas_receive"  >
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="1" tab="文单"></a-tab-pane>
        <a-tab-pane key="2" tab="正文"></a-tab-pane>
      </a-tabs>
      <div class="tab-content">
        <div v-if="activeKey === '1'" class="document-form-pane">
          <div class="document-paper">
            <div class="document-prelude">
              <span>来文单位：<em>{{ formModel.sending_unit || '' }}</em></span>
              <span>内部编号：<em>{{ formModel.inner_no || '' }}</em></span>
            </div>
            <div class="document-head">
              <div class="document-head-kicker">OAS 公文流转</div>
              <h3 class="u-form-top-title">收文处理笺</h3>
            </div>
            <div class="document-head-meta">
              <span>年度：{{ formModel.year_ || '' }}</span>
              <span>文号：{{ formModel.doc_no || '' }}</span>
              <span>收文日期：{{ formModel.receive_date || '' }}</span>
              <span>缓急：{{ formModel.urgency || '' }}</span>
            </div>
            <table class="u-form-table">
              <colgroup>
                <col class="label-col" />
                <col class="value-col" />
                <col class="label-col" />
                <col class="value-col" />
              </colgroup>
            <!-- 第一组 - 收文登记信息 -->
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">标题</span>
              </td>
              <td colspan="3" class="u-form-table-con">
                <a-form-item name="title" :rules="[{ required: isFormItemRequire(actRuleArgs,'title'), message: '请输入标题' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'title')" defaultValue="" v-model:value="formModel.title" placeholder="请输入标题" :maxlength="64" />
                </a-form-item>
              </td>
            </tr>
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">来文单位</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="sending_unit" :rules="[{ required: isFormItemRequire(actRuleArgs,'sending_unit'), message: '请输入来文单位' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'sending_unit')" defaultValue="" v-model:value="formModel.sending_unit" placeholder="请输入来文单位" :maxlength="255"/>
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">日期</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="receive_date" :rules="[{ required: isFormItemRequire(actRuleArgs,'receive_date'), message: '请选择日期' }]">
                  <u-date :disabled="disabled || isFormItemDisabled(actRuleArgs,'receive_date')" defaultValue="" v-model:value="formModel.receive_date" formatPatter="yyyy-MM-dd" maxValue="" minValue="" placeholder="请选择日期"/>
                </a-form-item>
              </td>
            </tr>
            <tr>
              <td class="td-title">
                <span class="u-form-table-title">年</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="year_" :rules="[{ required: isFormItemRequire(actRuleArgs,'year_'), message: '请选择年' }]">
                  <u-date :disabled="disabled || isFormItemDisabled(actRuleArgs,'year_')" defaultValue="${currentYear}" v-model:value="formModel.year_" formatPatter="yyyy" maxValue="" minValue="" placeholder="请选择年"/>
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">文号</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="doc_no" :rules="[{ required: isFormItemRequire(actRuleArgs,'doc_no'), message: '请输入文号' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'doc_no')" defaultValue="" v-model:value="formModel.doc_no" placeholder="请输入文号" :maxlength="64" />
                </a-form-item>
              </td>
            </tr>
            <!-- 第二组 - 收文属性 -->
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
                <span class="u-form-table-title">缓急</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="urgency" :rules="[{ required: isFormItemRequire(actRuleArgs,'urgency'), message: '请选择缓急' }]">
                  <u-select :disabled="disabled || isFormItemDisabled(actRuleArgs,'urgency')" form-type="select" defaultValue="" v-model:value="formModel.urgency" dict-type="urgency_degree" placeholder="请选择缓急"/>
                </a-form-item>
              </td>
              <td class="td-title">
                <span class="u-form-table-title">内部编号</span>
              </td>
              <td class="u-form-table-con">
                <a-form-item name="inner_no" :rules="[{ required: isFormItemRequire(actRuleArgs,'inner_no'), message: '请输入内部编号' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'inner_no')" defaultValue="" v-model:value="formModel.inner_no" placeholder="请输入内部编号" :maxlength="64" />
                </a-form-item>
              </td>
            </tr>
            <!-- 第三组 - 审批流转 -->
            <tr class="opinion-row">
              <td class="td-title">
                <span class="u-form-table-title">局领导批示</span>
              </td>
              <td colspan="3" class="u-form-table-con">
                <a-form-item name="leader_directive" :rules="[{ required: isFormItemRequire(actRuleArgs,'leader_directive'), message: '请输入局领导批示' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'leader_directive')" :textarea="true" defaultValue="" v-model:value="formModel.leader_directive" placeholder="请输入局领导批示" :maxlength="255"/>
                </a-form-item>
              </td>
            </tr>
            <tr class="opinion-row">
              <td class="td-title">
                <span class="u-form-table-title">办公室批分</span>
              </td>
              <td colspan="3" class="u-form-table-con">
                <a-form-item name="office_directive" :rules="[{ required: isFormItemRequire(actRuleArgs,'office_directive'), message: '请输入办公室批分' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'office_directive')" :textarea="true" defaultValue="" v-model:value="formModel.office_directive" placeholder="请输入办公室批分" :maxlength="255"/>
                </a-form-item>
              </td>
            </tr>
            <tr class="opinion-row">
              <td class="td-title">
                <span class="u-form-table-title">处室办理结果</span>
              </td>
              <td colspan="3" class="u-form-table-con">
                <a-form-item name="department_directive" :rules="[{ required: isFormItemRequire(actRuleArgs,'department_directive'), message: '请输入处室办理结果' }]">
                  <u-input :disabled="disabled || isFormItemDisabled(actRuleArgs,'department_directive')" :textarea="true" defaultValue="" v-model:value="formModel.department_directive" placeholder="请输入处室办理结果" :maxlength="255"/>
                </a-form-item>
              </td>
            </tr>
            </table>
          </div>
        </div>
        <div v-else-if="activeKey === '2'" class="document-body-pane">
          <div class="document-paper document-attachment-paper">
            <a-form-item name="enclosure" label="正文文档" >
              <u-upload :disabled="disabled" v-model:value="formModel.enclosure" oss="minio:main:test" :online="true" :fileCount="5" :maxSize="50" />
            </a-form-item>
          </div>
        </div>
      </div>
    </u-form>
  </div>
</template>

<script>
export default {
  name: "oas_receive_form",
}
</script>
<script setup>
import {isFormItemShow, isFormItemDisabled, isFormItemRequire} from "@/lib/act/actForm";
import {ref} from "vue";
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
}

.document-prelude,
.document-head-meta {
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
    justify-content: center;
    width: 100%;
    min-height: 28px;
    color: @document-red-deep;
    font-weight: 700;
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
