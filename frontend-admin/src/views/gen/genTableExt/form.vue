<template>
  <u-form style="" @loadSuccess="loadSuccess" v-model:value="formModel" v-model:disabled="disabled" :get-data-action="postAction" :url="{
    getData:'gen/genTable/editForm',save:'gen/genTable/saveGenTable'
  }" :getDataKey="['data','genTable']" :label-width="70" form-no="gen_table" :get-extend-save-data="getExtendSaveData" :on-save-success="handleFormalSaveSuccess">
    <a-tabs>
      <template #rightExtra>
        <div class="design-top-actions">
          <div v-if="formModel.id" class="design-action-group primary">
            <span class="design-action-label">主操作</span>
            <a-button size="small" @click="preview">预览</a-button>
          </div>
          <div class="design-action-group ai">
            <span class="design-action-label">AI助手</span>
            <a-button size="small" type="primary" @click="openAiCreateDraft">AI创建草稿</a-button>
            <a-button size="small" @click="checkDesignDsl">AI体检</a-button>
            <a-button v-if="aiFormalSourceDrafts.length > 0 || aiFormalSourceLoading" size="small" :loading="aiFormalSourceLoading" @click="showAiFormalSource">AI来源</a-button>
            <a-button v-if="aiDraftRollbackSnapshot" size="small" danger @click="rollbackAiDraftState">撤销AI应用</a-button>
          </div>
          <div class="design-action-group tools">
            <span class="design-action-label">工具</span>
            <a-button size="small" @click="exportDesignDsl">导出DSL</a-button>
          </div>
        </div>
      </template>
      <a-tab-pane :key="0" forceRender tab="表单设计">
        <div class="design-container">
          <div class="design-left">
            <u-form-title class="formtitle">表单信息</u-form-title>
              <a-row :gutter="16" style="">
                <a-col :span="24">
                  <a-form-item name="name" label="表名"  :validateFirst="true" :rules="[{ required: true, message: '请输入表名' }]">
                    <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder=""/>
                  </a-form-item>
                </a-col>
                <a-col :span="24">
                  <a-form-item name="comments" label="备注" :validateFirst="true" :rules="[{ required: true, message: '请输入备注' }]">
                    <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.comments" placeholder=""/>
                  </a-form-item>
                </a-col>
                <a-col :span="24" v-show="false">
                  <a-form-item name="comments_EN" label="Comments"  :validateFirst="true" :rules="[{ required: true, message: '请输入comments' }]">
                    <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.comments_EN" placeholder=""/>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item name="module" label="模块" :validateFirst="true" :rules="[{ required: true, message: '请输入模块' }]">
                    <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.module" placeholder=""/>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item name="tableType" label="类型" :validateFirst="true" :rules="[{ required: true, message: '请选择类型' }]">
                    <u-select v-if="!formModel.parentTable" :disabled="disabled" defaultValue="" v-model:value="formModel.tableType" placeholder="" value-field="value" text-field="text"
                              :option-data="parentTableOptionData"/>
                    <u-select v-else :disabled="true" defaultValue="" v-model:value="formModel.tableType" placeholder="" value-field="value" text-field="text" :option-data="childTableOptionData"/>
                  </a-form-item>
                </a-col>
                <a-col :span="24" v-show="formModel.module === MODULE_DATAHOUSE">
                  <a-form-item name="blockChainParam2" label="唯一键">
                    <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.blockChainParam2" placeholder="field1,datefiled"/>
                  </a-form-item>
                </a-col>
                <a-col :span="24" v-show="formModel.module === MODULE_DATAHOUSE">
                  <a-form-item name="blockChainParam3" label="分区键">
                    <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.blockChainParam3" placeholder="datefiled"/>
                  </a-form-item>
                </a-col>
                <a-col :span="24" v-show="formModel.module === MODULE_DATAHOUSE">
                  <a-form-item name="blockChainParam4" label="分桶键">
                    <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.blockChainParam4" placeholder="field1"/>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item name="pkColumnName" label="主键字段">
                    <u-input :disabled="disabled" defaultValue="id" v-model:value="formModel.pkColumnName" placeholder="默认为id"/>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item name="isVersion" label="留痕">
                    <u-select :disabled="disabled"  defaultValue="" v-model:value="formModel.isVersion" placeholder="" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
                  </a-form-item>
                </a-col>
                <a-col :span="24">
                  <a-form-item name="parentTable" label="主表" :validateFirst="true">
                    <u-select :disabled="disabled" defaultValue="" v-model:value="formModel.parentTable" placeholder="" type="table" dict-type="gen_table" value-field="name"
                              :format="row=>`${row.name} ${row.comments}`"/>
                  </a-form-item>
                </a-col>
                <a-col :span="24">
                  <a-form-item name="parentTableFk" label="外键" :validateFirst="true">
                    <u-input :disabled="true" defaultValue="" v-model:value="formModel.parentTableFk" placeholder=""/>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item name="processDefinitionCategory" label="流程">
                    <u-select :disabled="disabled"  defaultValue="" v-model:value="formModel.processDefinitionCategory" dict-type="act_category"/>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item name="tableSort" label="排序">
                    <u-input type="number" :min="0" :step="10" v-model:value="formModel.tableSort"/>
                  </a-form-item>
                </a-col>
              </a-row>
            <div style="width: 100%;display: flex;justify-content: center"><a-button type="primary" @click="quickAddFormItem" shape="round"><b>快速创建</b></a-button></div>
            <u-form-title class="controltitle">控件列表</u-form-title>
            <div class="form-control-container">
              <div class="form-control-item" @click="addFormItem(item)" v-bind:key="item.value"  v-for="item in dynamicFormItemType">
                <img :src="item.img" style="width: 15px;height: 15px"/>&nbsp;{{ item.text }}
              </div>
            </div>
          </div>
          <div class="design-center">
            <u-form ref="mockForm" v-model:value="mockFormModel" v-model:disabled="mockDisabled" :label-width="Number(formPropsModel.labelWidth)" setFormInjectKey="">
              <div class="mock-modal-container" style="box-shadow: 1px 1px 10px rgba(0, 0, 0, .3);margin-top: 5px;height:500px">
                <div class="mock-modal-title" style="color:var(--ant-primary-color);height: 40px;position: relative">表单内容
                  <a-input style="width:200px;position: absolute;right:8%" placeholder="搜索字段名" v-model:value="zd"></a-input>
                  <SearchOutlined style="position: absolute;right:5%" @click="namefilter"/>
                  <setting-outlined @click="openFormSetting"/>
                </div>
                <div class="mock-modal-content" style="height:410px;overflow: auto">
                  <TransitionGroup name="drag-item" tag="div" class="ant-row" style="margin-left: -8px; margin-right: -8px; row-gap: 0px;">
                    <div @dragenter="drag('display')"
                         key="drag-item-key"
                         @dragend="dragend"
                         @dragover="dragover" draggable="false" style="width: 100%;display: flex;flex-wrap: wrap;justify-content: space-between;transition: all 1s">
                      <div style="background-color:#b4bccc;width: 100%;font-size: xxx-large;opacity: 0.3;border-radius: 5px;text-align: center"
                           draggable="false"
                           key="drag-item-key"
                           v-show="displayFormItemArr.length==0"
                      >+</div>
                        <a-col class="dynamic-form-item" v-bind:key="index" v-for="(item,index) in displayFormItemArr"
                               draggable="true"
                               @dragstart="dragstartDynamicItem($event,item,index,'display')"
                               @dragenter="dragenterDynamicItem($event,item,index,'display')"
                               @dragend="dragend"
                               @dragover="dragover"
                               :class="[item.id=== currentFormItem.id?'selected':'',isRender(item)?'render-display':'render-none']"
                               @click="clickDynamicItem(item)"
                               v-bind="item.colProps.props"
                               style="transition: all 1s" >
                          <a-form-item style="" v-bind="item.formItemProps.props"
                                       :rules="item.formItemProps.rules.concat(item.formItemProps.unique==='1'?[ uniqueValidator(formModel.name,item.formItemProps.label,formModel.id)]:[])">
                            <div class="dynamic-form-item-content">
                              <u-dynamic-form-item :form-modal="mockFormModel" v-model:value="mockFormModel[item.formItemProps.name]" :disabled="item.formControlProps.disabled==='1'" :formType="item.type"
                                                   :props="item.formControlProps.props"/>
                            </div>
                            <copy-outlined class="dynamic-form-item-copy" @click.stop="copyItem('display',item,index)"/>
                            <close-circle-outlined v-if="!oneOf(item.formItemProps.name,fixedHiddenFieldNameArr)" class="dynamic-form-item-remove" @click.stop="removeDisplayItem(index,$event)"/>
                          </a-form-item>
                        </a-col>
                    </div>
                  </TransitionGroup>
                </div>
                <div class="mock-modal-footer">
                  <a-button>取消</a-button>
                  <a-button type="primary" @click="saveMockForm">确定</a-button>
                </div>
              </div>
              <div class="mock-modal-container" style="margin-top: 10px;height:270px">
                <div class="mock-modal-title" style="height: 40px">隐藏区域</div>

                <div class="mock-modal-content" style="height: 230px;overflow: auto">
                  <TransitionGroup name="drag-item" tag="div" class="ant-row" style="margin-left: -8px; margin-right: -8px; row-gap: 0px;" >
                    <div @dragenter="drag('hide')"
                         key="drag-item-key"
                         @dragend="dragend"
                         @dragover="dragover" draggable="false" style="width: 100%;display: flex;flex-wrap: wrap;justify-content: space-between">
                      <div style="background-color:#b4bccc;width: 100%;font-size: xxx-large;opacity: 0.3;border-radius: 5px;text-align: center"
                           draggable="false"
                           key="drag-item-key"
                           v-show="hideFormItemArr.length==0"
                      >+</div>
                        <a-col class="dynamic-form-item" v-bind:key="index" v-for="(item,index) in hideFormItemArr"
                               draggable="true"
                               @dragstart="dragstartDynamicItem($event,item,index,'hide')"
                               @dragenter="dragenterDynamicItem($event,item,index,'hide')"
                               @dragend="dragend"
                               @dragover="dragover"
                               :class="[item.id === currentFormItem.id?'selected':'',isRender(item)?'render-display':'render-none']"
                               @click="clickDynamicItem(item)"
                               v-bind="item.colProps.props"
                               style="transition: all 1s">
                          <a-form-item v-bind="item.formItemProps.props"
                                       :rules="item.formItemProps.rules.concat(item.formItemProps.unique==='1'?[ uniqueValidator(formModel.name,item.formItemProps.label,formModel.id)]:[])">
                            <div class="dynamic-form-item-content">
                              <u-dynamic-form-item :form-modal="mockFormModel" v-model:value="mockFormModel[item.formItemProps.name]" :disabled="item.formControlProps.disabled==='1'" :formType="item.type"
                                                   :props="item.formControlProps.props"/>
                            </div>
                            <copy-outlined class="dynamic-form-item-copy" @click.stop="copyItem('hide',item,index)"/>
                            <close-circle-outlined v-if="!oneOf(item.formItemProps.name,fixedHiddenFieldNameArr)" class="dynamic-form-item-remove" @click.stop="removeHideItem(index,$event)"/>
                          </a-form-item>
                        </a-col>

                    </div>

                  </TransitionGroup>
                </div>

              </div>
            </u-form>
          </div>
          <div class="design-right">
            <u-tabs>
              <a-tab-pane :key="0" tab="配置项">

                <template v-if="formSetting">
                  <u-form ref="settingForm" v-model:value="formPropsModel" :label-width="130" setFormInjectKey="">
                    <u-form-title class="opentitle">打开方式</u-form-title>
                    <a-row :gutter="16">
                      <a-col :span="24">
                        <a-form-item name="blockChainParam5" label="编辑方式">
                          <u-select :disabled="disabled" defaultValue="1" v-model:value="formModel.blockChainParam5" placeholder="" value-field="value" text-field="text" :option-data="editOpenModeOptionData"/>
                        </a-form-item>
                      </a-col>
                      <a-col :span="24">
                        <a-form-item name="blockChainParam6" label="列表说明">
                          <u-input :disabled="disabled" :textarea="true" :maxlength="200" v-model:value="formModel.blockChainParam6" placeholder="为空时使用系统默认说明"/>
                        </a-form-item>
                      </a-col>
                    </a-row>
                    <u-form-title class="poptitle">弹窗配置</u-form-title>
                    <a-row :gutter="16">
                      <a-col :span="24">
                        <a-form-item name="modal__Full" label="全屏">
                          <u-select formType="radio" :disabled="disabled" defaultValue="" v-model:value="formPropsModel.modal__Full" placeholder="" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
                        </a-form-item>
                      </a-col>
                      <a-col :span="24" v-if="formPropsModel.modal__Full!=='1'">
                        <a-form-item name="modal__Width" label="弹窗宽度" :rules="[{ required: true, message: '请输入弹窗宽度' }]">
                          <u-input type="number" :min="0" :step="100" v-model:value="formPropsModel.modal__Width"/>
                        </a-form-item>
                      </a-col>
                      <a-col :span="24">
                        <a-form-item name="list__modalTitle" label="弹窗标题" :rules="[{ required: true, message: '请输入弹窗标题' }]">
                          <u-input v-model:value="formPropsModel.list__modalTitle"/>
                        </a-form-item>
                      </a-col>
                      <a-col :span="24">
                        <a-form-item name="" label="弹窗标题Function">
                          <a-button size="small" type="link" @click="modalTitleFunctionVisible = !modalTitleFunctionVisible">
                            {{ modalTitleFunctionVisible ? '收起代码' : '展开代码' }}
                          </a-button>
                        </a-form-item>
                      </a-col>
                      <a-col v-if="modalTitleFunctionVisible" :span="24">
                        <a-form-item  :labelCol="{style: '20px'}" label=" " :colon="false" name="list__modalTitleFuncStr">
                          <template #extra>
                            用于格式化显示弹窗标题<br/>
                            typeName为 添加|编辑|查看|导入|办理<br/>
                            row 为该行数据 需要判断row是否存在
                          </template>
                          (row, typeName)=>{
                          <u-code-mirror v-model:value="currentFormItemFormModel.list__modalTitleFuncStr"/>
                          }
                        </a-form-item>
                      </a-col>

                    </a-row>
                    <u-form-title class="devtitle">表单配置</u-form-title>
                    <a-row :gutter="16">
                      <a-col :span="24">
                        <a-form-item name="labelWidth" label="Label宽度" :rules="[{ required: true, message: '请输入label宽度' }]">
                          <u-input type="number" :min="0" :step="10" v-model:value="formPropsModel.labelWidth"/>
                        </a-form-item>
                      </a-col>
                      <a-col :span="24">
                        <a-form-item name="mainTableTitle" label="表单标题" :rules="[{ required: true, message: '请输入表单标题' }]">
                          <u-input  v-model:value="formPropsModel.mainTableTitle"/>
                        </a-form-item>
                      </a-col>
                      <a-col :span="24">
                        <a-form-item name="" label="校验函数">
                          <a-button size="small" type="link" @click="extendSaveFunctionVisible = !extendSaveFunctionVisible">
                            {{ extendSaveFunctionVisible ? '收起代码' : '展开代码' }}
                          </a-button>
                        </a-form-item>
                      </a-col>
                      <a-col v-if="extendSaveFunctionVisible" :span="24">
                        <a-form-item  :labelCol="{style: '20px'}" label=" " :colon="false" name="getExtendSaveDataFuncStr">
                          <template #extra>
                            用于提交前的校验，参数为当前{}，返回值为Promise
                            //TODO option传入一些可以调用的js方法
                          </template>
                          (option)=>{
                          <u-code-mirror v-model:value="formPropsModel.getExtendSaveDataFuncStr"/>
                          }
                        </a-form-item>
                      </a-col>
                      <a-col :span="24">
                        <a-form-item name="saveBeforeListAddItem" label="子表添加前保存主表">
                          <u-select v-model:value="formPropsModel.saveBeforeListAddItem" value-field="value" text-field="text" :option-data="booleanOptionData"/>
                        </a-form-item>
                      </a-col>
                      <a-col :span="24">
                        <a-form-item name="subTableType" label="子表显示类型">
                          <u-select v-model:value="formPropsModel.subTableType" value-field="value" text-field="text" :option-data="subTableTypeOptionData"/>
                        </a-form-item>
                      </a-col>
                      <template v-if="formPropsModel.subTableType==='anchor'">
                        <a-col :span="24">
                          <a-form-item name="anchorWidth" label="锚点选项卡宽度" :rules="[{ required: true, message: '请输入锚点选项卡宽度' }]">
                            <u-input type="number" :min="0" :step="10" v-model:value="formPropsModel.anchorWidth"/>
                          </a-form-item>
                        </a-col>
                        <a-col :span="24">
                          <a-form-item name="anchorLocation" label="锚点位置">
                            <u-select v-model:value="formPropsModel.anchorLocation" value-field="value" text-field="text"
                                      :option-data="[{value: 'left', text: '左侧'},{value: 'center', text: '右侧'},]"/>
                          </a-form-item>
                        </a-col>
                      </template>
                    </a-row>
                  </u-form>
                </template>
                <template v-else-if="currentFormItemFormModel.key">
                  <form-design-form-item-config-form ref="configForm" :formNo="formModel.name" :tableComment="formModel.comments" :all-columns="allListColumns" v-model:value="currentFormItemFormModel" @changeType="changeType" @changeSort="changeSort"/>
                </template>
                <a-empty v-else description="请选择字段"/>
              </a-tab-pane>
              <!--            <a-tab-pane :key="1" tab="源码"></a-tab-pane>-->
            </u-tabs>

          </div>
        </div>
      </a-tab-pane>
      <a-tab-pane :key="1" tab="列表设计">
        <div class="mock-list-container">
          <div class="mock-list-top">
            <form-design-list-setting :height="configTableHeight" :all-columns="allListColumns" @update:value="updateMockTableColumns"/>
          </div>
          <div class="mock-list-bottom">
            <form-design-mock-list v-model:value="extendJsConfig" :mock-buttons="extendJsConfig.list__buttons" :mockSingleTable="mockSingleTable" :mock-columns="mockColumns"/>
          </div>
        </div>
      </a-tab-pane>
      <a-tab-pane :key="2" tab="列表页配置">
        <div class="list-config-container">
          <form-design-list-config-form :isSubTable="isSubTable" :all-columns="allListColumns" v-model:value="extendJsConfig" :tableName="formModel.name"/>
        </div>
      </a-tab-pane>

    </a-tabs>

  </u-form>
  <ai-design-check-drawer
      v-model:visible="aiCheckDrawerVisible"
      :issues="aiDesignIssues"
      :summary="aiDesignSummary"
      :dsl="aiDesignDsl"
      @apply-fixes="applyAiDesignFixes"
  />
  <ai-create-draft-drawer
      v-model:visible="aiCreateDraftDrawerVisible"
      :table="table"
      :form-model="formModel"
      :form-props-model="formPropsModel"
      :extend-js-config="extendJsConfig"
      :display-form-item-arr="displayFormItemArr"
      :hide-form-item-arr="hideFormItemArr"
      :fixed-arr="fixedArr"
      @apply-draft-patch="applyAiDraftStatePatch"
  />
</template>

<script>

export default {
  name: "genTableExt_form",
}
</script>
<script setup>
import {computed, getCurrentInstance, h, nextTick,ref, watch} from "vue";
import UForm from "@/components/form/UForm";
import {postAction, postActionShowLoading} from "@/api/action";
import UFormTitle from "@/components/form/UFormTitle";
import USelect from "@/components/form/USelect";
import {
  yesNoOptionData,
  booleanOptionData,
  parentTableOptionData,
  childTableOptionData,
  subTableTypeOptionData,
  editOpenModeOptionData
} from "@/views/gen/genOptionData";
import {
  defaultFieldArray,
  dynamicFormItemType,
  initDynamicFormItemConfig,
  transformDictViewListToDynamicFormItemConfig, transformDynamicFormItemConfigToGenTableColumn,
  transformGenTableColumnToDynamicFormItemConfig
} from "@/views/gen/dynamicFormItem";
import {deepClone, getWebUrl, oneOf, promptModal} from "@/lib/tools";
import {uniqueValidator} from "@/lib/validator";
import UDynamicFormItem from "@/components/form/UDynamicFormItem";
import UTabs from "@/components/nav/UTabs";
import DynamicFormItemConfig, {FormItemProps} from "@/dynamic/DynamicFormItemConfig";
import UInput from "@/components/form/UInput";
import {Modal} from "ant-design-vue";
import {useRouter} from "vue-router";
import {defaultListButtonArr, defaultRowButtonArr, treeTableListButtonArr, treeTableRowButtonArr} from "@/views/gen/genTableExt/formStaticConfig";
import UCodeMirror from "@/components/form/code/UCodeMirror";
import FormDesignListConfigForm from "@/views/gen/genTableExt/formDesignListConfigForm";
import FormDesignFormItemConfigForm from "@/views/gen/genTableExt/formDesignFormItemConfigForm";
import {dispatchMockEvent, initColumnListConfig} from "@/views/gen/genTableExt/formDesign";
import FormDesignMockList from "@/views/gen/genTableExt/formDesignMockList";
import FormDesignListSetting from "@/views/gen/genTableExt/formDesignListSetting";
import {calcPredicate} from "@/lib/tool-dynamic-form";
import {exportCurrentDesignToDsl} from "@/views/gen/genTableExt/ai/dslExport";
import {checkFormDesignDsl, summarizeDesignIssues} from "@/views/gen/genTableExt/ai/designCheckRules";
import AiDesignCheckDrawer from "@/views/gen/genTableExt/ai/AiDesignCheckDrawer";
import AiCreateDraftDrawer from "@/views/gen/genTableExt/ai/AiCreateDraftDrawer";
import {applyDesignFixes} from "@/views/gen/genTableExt/ai/applyDesignFixes";
import {explainFormDesignIssues} from "@/views/gen/genTableExt/ai/designIssueExplain";
import {applyDraftStatePatch, rollbackDraftStateSnapshot} from "@/views/gen/genTableExt/ai/applyDraftStatePatch";
import {buildSaveReviewResult} from "@/views/gen/genTableExt/ai/saveReview";
import {listRemoteDrafts, markRemoteDraftFormalSaved} from "@/views/gen/genTableExt/ai/remoteDraftProvider";

const MODULE_DATAHOUSE = 'datahouse'

let formModel = ref({
  blockChainParam1: '',
  blockChainParam2: '',
  blockChainParam5: '1',
  blockChainParam6: '',
})
let disabled = ref(false)
let aiCheckDrawerVisible = ref(false)
let aiCreateDraftDrawerVisible = ref(false)
let aiDesignIssues = ref([])
let aiDesignSummary = ref({
  total: 0,
  error: 0,
  warning: 0,
  suggestion: 0,
  fixable: 0,
})
let aiDesignDsl = ref({})
let aiDraftRollbackSnapshot = ref(null)
let aiFormalizeContext = ref(null)
let aiFormalSaveTraceState = ref({
  lastTraceKey: '',
})
let aiFormalSourceLoading = ref(false)
let aiFormalSourceDrafts = ref([])
let aiApplyState = ref({
  draftApplied: false,
  draftFieldCount: 0,
  fixAppliedCount: 0,
  lastAppliedAt: '',
})

// 监听主表变化设置外键
watch(() => formModel.value.parentTable, (val) => {
  if (val) {
    formModel.value.parentTableFk = 'parent_id'
  } else {
    formModel.value.parentTableFk = ''
  }
}, {immediate: true})

/**
 * 模拟表单数据
 * @type {Ref<UnwrapRef<{}>>}
 */
let mockFormModel = ref({})
/**
 * 模拟表单项是否禁用
 * @type {Ref<UnwrapRef<boolean>>}
 */
let mockDisabled = ref(false)
// let flagd=ref(false)
// let flagh=ref(false)
let formSetting = ref(true)
let modalTitleFunctionVisible = ref(false)
let extendSaveFunctionVisible = ref(false)
let formPropsModel = ref({
  labelWidth: 100,
  modal__Width: 1200,
  modal__Full: '0',
})
// 监听主表备注变化设置主表标题
watch(() => formModel.value.comments, (val) => {
  if (!formPropsModel.value.mainTableTitle) {
    formPropsModel.value.mainTableTitle = val
  }
  formModel.value.comments_EN = val
}, {immediate: true})

/**
 * 表单项是否渲染
 * @param item
 * @return {boolean}
 */
const isRender = (item) => {
  if (item.formItemProps && item.formItemProps.renderPredicate) {
    return calcPredicate(JSON.parse(item.formItemProps.renderPredicate), mockFormModel.value)
  }
  return true
}

/**
 * 扩展js配置
 * @type
 */
let extendJsConfig = ref({
  singleTable__rowButtons: formModel.value.tableType === '3' ? treeTableRowButtonArr : defaultRowButtonArr,
  list__buttons: formModel.value.tableType === '3' ? treeTableListButtonArr : defaultListButtonArr,
  extendColumns: [],
})

watch(() => formModel.value.name, (val) => {
  if (val) {
    extendJsConfig.value.singleTable__rowButtons.forEach((item) => {
      if (item.value.indexOf('view') === 0) {
        item.permission = isSubTable.value ? '' : `app:${val}:view`
      } else if (item.value.indexOf('edit') === 0) {
        item.permission = isSubTable.value ? '' : `app:${val}:edit`
      } else if (item.value.indexOf('delete') === 0) {
        item.permission = isSubTable.value ? '' : `app:${val}:del`
      } else if (item.value.indexOf('addChild') === 0) {
        item.permission = isSubTable.value ? '' : `app:${val}:lowerlevel`
      }
    })
    extendJsConfig.value.list__buttons.forEach((item) => {
      if (item.value.indexOf('add') === 0) {
        item.permission = isSubTable.value ? '' : `app:${val}:add`
      } else if (item.value.indexOf('batch-delete') === 0) {
        item.permission = isSubTable.value ? '' : `app:${val}:del`
      } else if (item.value.indexOf('import') === 0) {
        item.permission = isSubTable.value ? '' : `app:${val}:import`
      } else if (item.value.indexOf('export') === 0) {
        item.permission = isSubTable.value ? '' : `app:${val}:export`
      }
    })
  }
}, {immediate: true})

const openFormSetting = () => {
  changeCurrent()
  formSetting.value = true
}

const saveMockForm = () => {
  instance.refs.mockForm.validateFields().then((obj) => {
  })
}

/**
 * 当前表单的配置
 * @type {{}}
 */
let table = {}

let isSubTable = computed(() => {
  if ('4' === formModel.value.tableType) {
    //左树右表 不为子表
    return false
  }
  return formModel.value.parentTable !== '' && formModel.value.parentTable !== undefined && formModel.value.parentTable !== null
})
/**
 * 显示的动态表单项
 * @type {Ref<UnwrapRef<*[]>>}
 */
let displayFormItemArr = ref([])
/**
 * 隐藏的动态表单项
 * @type {Ref<UnwrapRef<*[]>>}
 */
let hideFormItemArr = ref([])

/**
 * 固定的的动态表单项
 * @type {*[]}
 * @private
 */
let fixedArr = []
/**
 * 添加表单项
 */
const addFormItem = (item) => {
  let dynamicFormItemConfig = initDynamicFormItemConfig(table, item, displayFormItemArr.value, hideFormItemArr.value);
  displayFormItemArr.value.push(dynamicFormItemConfig)
  updateListSort()//更新列配置-排序
  let addMap = {}
  addMap[dynamicFormItemConfig.formItemProps.name] = initColumnListConfig(dynamicFormItemConfig.column)
  updateMockTableColumns(addMap)//添加表单项时初始化listConfig
}
/**
 * 切换表单类型
 * @param value
 * @param item
 */
const changeType = (value, item) => {
  const remove = (arr) => {
    let deleteIndex = -1;
    arr.forEach((obj, index) => {
      if (value.__id === obj.id) {
        deleteIndex = index
      }
    })
    return deleteIndex
  }
  let index0 = remove(displayFormItemArr.value);
  let index1 = remove(hideFormItemArr.value)

  const init = () => {
    let dynamicFormItemConfig = initDynamicFormItemConfig(table, item, displayFormItemArr.value, hideFormItemArr.value);
    let validateType = dynamicFormItemConfig.formItemProps.validateType//获取初始化时的validateType
    dynamicFormItemConfig.formItemProps = new FormItemProps()
    dynamicFormItemConfig.formItemProps.name = value.formItemProps__name
    dynamicFormItemConfig.formItemProps.validateType = validateType//把初始化的validateType赋值给新的formItemProps
    dynamicFormItemConfig.formItemProps.label = value.formItemProps__label
    dynamicFormItemConfig.formItemProps.required = value.formItemProps__required
    dynamicFormItemConfig.formItemProps.extra = value.formItemProps__extra
    return dynamicFormItemConfig
  }
  let dynamicFormItemConfig = null
  if (index0 !== -1) {
    displayFormItemArr.value.splice(index0, 1)//先删除
    dynamicFormItemConfig = init()//初始化
    displayFormItemArr.value.splice(index0, 0, dynamicFormItemConfig)//再添加
    updateListSort()//更新列配置-排序
  }
  if (index1 !== -1) {
    hideFormItemArr.value.splice(index1, 1)//先删除
    dynamicFormItemConfig = init()//初始化
    hideFormItemArr.value.splice(index1, 0, dynamicFormItemConfig)//再添加
  }
  changeCurrent(dynamicFormItemConfig)
}

/**
 * 更新列配置-排序
 */
const updateListSort = () => {
  displayFormItemArr.value.forEach((item, index) => {
    if (item.column.isList === '1') {
      item.column.listSort = (index + 1) * 10
    }
  })
}

/**
 * 修改表单项排序
 * @param id 表单项id
 * @param sort 排序
 */
const changeSort = ({id, sort}) => {

  const exec = (arr) => {
    let move = false//是否需要移动
    let currentIndex = -1 //当前索引
    let targetIndex = 0//目标索引
    let moveItem = null//移动的对象
    arr.forEach((item, index) => {
      if (item.id === id) {
        currentIndex = index
        move = true
        moveItem = item
        item.column.formSort = sort
      }else if (Number(sort) >= Number(item.column.formSort)) {
        //如果当前表单项的排序大于等于目标表单项的排序
        targetIndex = index
      }
    })
    if (move && currentIndex !== targetIndex) {
      arr.splice(currentIndex, 1)
      arr.splice(targetIndex, 0, moveItem)
    }
  }

  //执行表单内容
  exec(displayFormItemArr.value)
  updateListSort()
  //执行隐藏区域
  exec(hideFormItemArr.value)

}

/**
 * 快速创建表单项
 */
const quickAddFormItem = () => {
  promptModal({
    title: '快速创建表单项',
    callback: (value) => {
      let names = value.split(/((\r\n)\r\n)+/);
      // eslint-disable-next-line no-undef
      postActionShowLoading(`/gen/genTable/getRealmColumnsByNames?names=${names}&mainTableModule=${formModel.value.module}`, {}).then(res => {
        let addMap = {}
        res.rows.forEach(row=>{
          let dynamicFormItemConfig = initDynamicFormItemConfig(table, {
            dataJson: row,
          }, displayFormItemArr.value, hideFormItemArr.value);
          displayFormItemArr.value.push(dynamicFormItemConfig)
          addMap[dynamicFormItemConfig.formItemProps.name] = initColumnListConfig(dynamicFormItemConfig.column)
        })
        updateListSort()//更新列配置-排序
        updateMockTableColumns(addMap)//添加表单项时初始化listConfig
      })
    }
  })
}

/**
 * 当前点击的动态表单项
 */
let currentFormItem = ref(new DynamicFormItemConfig())
let currentFormItemFormModel = ref(currentFormItem.value.toJson())
let instance = getCurrentInstance();
/**
 * 监听当前点击的动态表单项的配置内容
 */
watch(() => currentFormItemFormModel.value, (value) => {
  if (value) {
    let toObject = value.toObject();
    toObject.column = transformDynamicFormItemConfigToGenTableColumn(toObject)
    let arr = []
    displayFormItemArr.value.forEach((item, index) => {
      if (item.id === toObject.id) {
        item = toObject
        item.apply()
      }
      arr.push(item)
    })
    displayFormItemArr.value = []
    displayFormItemArr.value = arr
    let arr2 = []
    hideFormItemArr.value.forEach((item, index) => {
      if (item.id === toObject.id) {
        item = toObject
        item.apply()
      }
      arr2.push(item)
    })
    hideFormItemArr.value = []
    hideFormItemArr.value = arr2
  }

}, {deep: true})

const changeCurrent = (item) => {
  if (instance.refs.configForm) {
    instance.refs.configForm.getRef().resetTableRef()
  }
  nextTick(() => {
    if (item) {
      currentFormItem.value = item
      currentFormItemFormModel.value = currentFormItem.value.toJson()
    } else {
      currentFormItem.value = new DynamicFormItemConfig()
      currentFormItemFormModel.value = currentFormItem.value.toJson()
    }
    updateMockColumns()
  })
}

/**
 * 点击动态表单项
 * @param item
 */
const clickDynamicItem = (item) => {
  formSetting.value = false
  if(other.value){
    changeCurrent(item);
  }
  other.value=false;
  if (currentFormItem.value.type) {
    if (currentFormItem.value.type === dynamicFormItemType.table) {
      return
    }
    instance.refs.configForm.getRef().validateFields().then((obj) => {
      changeCurrent(item)
    }).catch((err) => {
      console.error('validateFields', err)
      Modal.warning({
        title: '提示',
        content: '请先完善当前表单项配置'
      })
    })
  } else {
    changeCurrent(item)
  }
}

let other=ref(false);
const removeDisplayItem = (index,e) => {
  e.stopPropagation();
  displayFormItemArr.value.splice(index, 1)
  updateListSort()//更新列配置-排序
  updateMockColumns()
  formSetting.value=true;
  other.value=true;
}

const removeHideItem = (index,e) => {
  e.stopPropagation();
  const item = hideFormItemArr.value[index]
  // 当删除 update_date 时，检查是否已设置默认排序字段
  if (item && item.formItemProps && item.formItemProps.name === 'update_date') {
    const hasSortField = displayFormItemArr.value.concat(hideFormItemArr.value).some(i =>
      i.formItemProps && i.formItemProps.name && i.formItemProps.name !== 'update_date' &&
      i.column && i.column.isList === '1' && i.column.sqlSort
    )
    if (!hasSortField) {
      Modal.warning({
        title: '提示',
        content: '删除更新时间字段后，系统将无法按默认时间排序，请在编辑SQL中指定一个默认排序字段。'
      })
    }
  }
  hideFormItemArr.value.splice(index, 1)
  updateMockColumns();
  formSetting.value=true;
  other.value=true;
}

const copyItem = (type,item,itemIndex) => {
  let targetTypeArr = dynamicFormItemType.filter(formItem => formItem.type == item.type)
  if (targetTypeArr.length === 0) {
    return
  }
  let targetType = targetTypeArr[0]

  let dynamicFormItemConfig = initDynamicFormItemConfig(table, targetType, displayFormItemArr.value, hideFormItemArr.value);
  let name = dynamicFormItemConfig.formItemProps.name
  dynamicFormItemConfig.formItemProps = deepClone(item.formItemProps)
  dynamicFormItemConfig.formItemProps.name = name
  dynamicFormItemConfig.formItemProps.label = item.formItemProps.label + 'copy'
  dynamicFormItemConfig.formControlProps = deepClone(item.formControlProps)
  if (type === 'display') {
    displayFormItemArr.value.splice(itemIndex + 1, 0, dynamicFormItemConfig)
    updateListSort()//更新列配置-排序
  } else {
    hideFormItemArr.value.splice(itemIndex + 1, 0, dynamicFormItemConfig)
  }
  changeCurrent(dynamicFormItemConfig)

  let addMap = {}
  addMap[dynamicFormItemConfig.formItemProps.name] = initColumnListConfig(dynamicFormItemConfig.column)
  updateMockTableColumns(addMap)//添加表单项时初始化listConfig
}

let dragIndex = {
  display: 0,
  hide: 0,
}
dragIndex['dorh']=''
let tempsource=null;
const drag=(type)=>{
  if(dragIndex['dorh']!=type){
    let arr1 = displayFormItemArr.value
    let arr2 = hideFormItemArr.value
    if (type === 'display') {
      const source = arr2[dragIndex['hide']]
      source.column.isForm="1";
      arr2.splice(dragIndex['hide'],1)
      dragIndex['display']=999;
      dragIndex['hide']=999
      if(displayFormItemArr.value.length==0){
        displayFormItemArr.value.push(source)
        tempsource=1;
      }else {
        tempsource = source
      }
      //arr1.splice()
    } else {
      const source = arr1[dragIndex['display']]
      source.column.isForm="0";
      arr1.splice(dragIndex['display'],1)
      dragIndex['display']=999;
      dragIndex['hide']=999
      if(hideFormItemArr.value.length==0){
        hideFormItemArr.value.push(source)
        tempsource=1
      }else {
        tempsource = source
      }
      //arr2.push(source)
    }
    updateListSort()//更新列配置-排序
    dragIndex['dorh']=type
    //dragIndex[type] == index
  }
}
const dragstartDynamicItem = (e, item, index, type) => {
  e.stopPropagation()
  dragIndex[type] = index
  dragIndex['dorh']=type;
  setTimeout(() => {
    e.target.classList.add('moving')
  }, 0)
}

const dragenterDynamicItem = (e, item, index, type) => {
  e.preventDefault()

    if (dragIndex[type] !== index&&dragIndex['dorh']==type) {
      let arr = []
      if (type === 'display') {
        arr = displayFormItemArr.value
      } else {
        arr = hideFormItemArr.value
      }
      const source = arr[dragIndex[type]]
      if(tempsource&&tempsource!=1){
        arr.splice(index, 0, tempsource)
        tempsource=null;
      }else{
        if(tempsource==1){
          tempsource=null;
        }else{
          arr.splice(dragIndex[type], 1)
          arr.splice(index, 0, source)
        }
      }
      updateListSort()//更新列配置-排序
      dragIndex[type] = index
    }
}

const dragend = (e) => {
  e.target.classList.remove('moving')
}

const dragover = (e) => {
  e.preventDefault()
  e.dataTransfer.dropEffect = 'move'
}

const fixedFieldNameArr = ['id', 'delFlag', 'procInsId', 'parent.id', 'procDefKey']
const fixedHiddenFieldNameArr = []
/**
 * 页面加载数据成功
 * @param res
 */
const loadSuccess = (res) => {
  aiApplyState.value = {
    draftApplied: false,
    draftFieldCount: 0,
    fixAppliedCount: 0,
    lastAppliedAt: '',
  }
  aiDraftRollbackSnapshot.value = null
  aiFormalizeContext.value = null
  aiFormalSaveTraceState.value = {
    lastTraceKey: '',
  }
  if (!res.data) {
    //如果是新增
    res.data = {
      data: [],
      genTable: {}
    }
    defaultFieldArray.forEach(item => {
      let obj = {}
      Object.assign(obj, item)
      res.data.data.push(obj)
    })
  }
  table = res.data.genTable
  loadAiFormalSourceDrafts(table.id)
  //确保pkColumnName有默认值
  if (!table.pkColumnName) {
    table.pkColumnName = 'id'
  }
  if (!table.blockChainParam5) {
    table.blockChainParam5 = '1'
  }
  if (table.formProps) {
    formPropsModel.value = JSON.parse(table.formProps)
  }
  if (table.extendJs) {
    let parse = JSON.parse(table.extendJs);
    if ((!parse.singleTable__rowButtons || parse.singleTable__rowButtons.length === 0) && parse.noRowButton !== '1') {
      parse.singleTable__rowButtons = formModel.value.tableType === '3' ? treeTableRowButtonArr : defaultRowButtonArr
    }
    if ((!parse.list__buttons || parse.list__buttons.length === 0) && parse.noOptButton !== '1') {
      parse.list__buttons = formModel.value.tableType === '3' ? treeTableListButtonArr : defaultListButtonArr
    }
    if (!parse.extendColumns) {
      parse.extendColumns = []
    }
    extendJsConfig.value = parse
  }
  fixedArr = res.data.data.filter(item => oneOf(item.javaField, fixedFieldNameArr))
  let _resultData = res.data.data.filter(item => !oneOf(item.javaField, fixedFieldNameArr))
  _resultData.forEach(item => {
    item.javaFieldReplace = item.javaField.replace(/\|/g, '-').replace(/\./g, '-');

  })
  _resultData.sort((a, b) => a.formSort - b.formSort);

  let displayArr = _resultData.filter(item => item.isForm === '1')
  let hiddenArr = _resultData.filter(item => item.isForm !== '1')
  displayFormItemArr.value = []
  hideFormItemArr.value = []
  displayArr.forEach(item => {
    displayFormItemArr.value.push(transformGenTableColumnToDynamicFormItemConfig(table, item))
  })
  if (table.extJava) {
    //多对多关系表
    let arr = JSON.parse(table.extJava);
    arr.forEach(item => {
      displayFormItemArr.value.push(transformDictViewListToDynamicFormItemConfig(table, item))
    })
  }
  hiddenArr.forEach(item => {
    hideFormItemArr.value.push(transformGenTableColumnToDynamicFormItemConfig(table, item))
  })
  updateMockColumns()
}

const namefilter=()=>{let dom=document.querySelectorAll('.dynamic-form-item');for(let i=0;i<zdarr.value.length;i++){if(zdarr.value[i].column.name===zd.value){dom[i].style.border='1px solid blue';dom[i].scrollIntoView({ behavior: "smooth" });setTimeout(()=>{dom[i].style.border='none'},1000)}}}
let zd=ref('');
let zdarr=ref([]);
let configTableHeight = ref(0)
const updateMockColumns = () => {
  let arr = []
  arr = displayFormItemArr.value.concat(hideFormItemArr.value)
  zdarr.value=arr;
  mockColumns.value = fixedArr.concat(arr.filter(item => item.type !== 'modalMultiSelect').map(item => item.column)).concat(extendJsConfig.value.extendColumns).map(item => {
    let obj = {}
    Object.assign(obj, item)
    let listConfig = item.listConfig ? JSON.parse(item.listConfig) : {}
    obj.listConfig__dataIndex = listConfig.dataIndex || item.name
    obj.listConfig__title = listConfig.title || item.comments
    obj.listConfig__minWidth = listConfig.minWidth || 150
    obj.listConfig__align = item.align || 'left'
    obj.listConfig__sorter = listConfig.sorter || 'false'
    obj.listConfig__sortColumn = listConfig.sortColumn
    obj.listConfig__ellipsis = listConfig.ellipsis || false
    obj.listConfig__dict = item.dictType
    obj.listConfig__dictMultiple = item.dictMultiple || false
    obj.listConfig__enableText = listConfig.status ? listConfig.status.enableText : ''
    obj.listConfig__disableText = listConfig.status ? listConfig.status.disableText : ''
    obj.listConfig__widthMultiple = listConfig.widthMultiple || 1
    obj.blockChainParam4 = item.blockChainParam4 || '0'
    obj.blockChainParam5 = item.blockChainParam5 || '0'
    obj.listConfig__queryColumn = listConfig.queryColumn || ''
    obj.listConfig__queryDefaultValue = listConfig.queryDefaultValue || ''
    obj.listConfig__queryFieldType = listConfig.queryFieldType || ''
    obj.listConfig__queryFieldProps = listConfig.queryFieldProps || null
    if (listConfig.title){
      obj.queryLabel = listConfig.title
    }
    return obj
  }).sort((a, b) => Number(a.listSort) - Number(b.listSort))
  dispatchMockEvent(table, getSubmitData())

}
let mockColumns = ref([])
/**
 * 所有的列
 * @type {ComputedRef<*[]>}
 */
let allListColumns = computed(() => {
  let arr = displayFormItemArr.value.concat(hideFormItemArr.value)
  return fixedArr.concat(extendJsConfig.value.extendColumns.map(item => JSON.parse(JSON.stringify(item))))
      .concat(arr.filter(item => item.type !== 'modalMultiSelect').map(item => JSON.parse(JSON.stringify(item.column))))
})
/**
 * 监听mockColumns变化
 */
const updateMockTableColumns = (map) => {
  let columnMap = map
  let extendColumns = []

  const configToColumnJson = (columnConfig)=>{
    let listConfig = {
      dataIndex: columnConfig.listConfig__dataIndex,
      title: columnConfig.listConfig__title,
      minWidth: parseInt(columnConfig.listConfig__minWidth),
      align: columnConfig.listConfig__align,
      sorter: columnConfig.listConfig__sorter,
      sortColumn: columnConfig.listConfig__sortColumn,
      ellipsis: columnConfig.listConfig__ellipsis,
      dict: columnConfig.listConfig__dict,
      dictMultiple: columnConfig.listConfig__dictMultiple,//字典是否多选

      widthMultiple: columnConfig.listConfig__widthMultiple,
      queryColumn: columnConfig.listConfig__queryColumn,//查询条件的字段
      queryDefaultValue: columnConfig.listConfig__queryDefaultValue,//查询条件的默认值
      queryFieldType: columnConfig.listConfig__queryFieldType,//查询条件的控件类型
      queryFieldProps: columnConfig.listConfig__queryFieldProps,//查询条件的控件属性
    }
    //console.log(columnMap[item.column.name],columnMap[item.column.name].listConfig__enableText,columnMap[item.column.name].listConfig__disableText)
    if (columnConfig.listConfig__enableText||columnConfig.listConfig__disableText) {
      listConfig.status = {
        enableText: columnConfig.listConfig__enableText,
        disableText: columnConfig.listConfig__disableText
      }
    }
    return listConfig
  }

  fixedArr.map(item=> {
    return {column:item}
  }).concat(displayFormItemArr.value).concat(hideFormItemArr.value).forEach(item => {
    let columnConfig = columnMap[item.column.name]
    if (item.column && columnConfig) {
      delete columnMap[item.column.name]
      item.column.isList = columnConfig.isList
      item.column.listSort = columnConfig.listSort
      item.column.isQuery = columnConfig.isQuery
      item.column.queryType = columnConfig.queryType
      item.column.align = columnConfig.listConfig__align
      item.column.blockChainParam4 = columnConfig.blockChainParam4
      item.column.blockChainParam5 = columnConfig.blockChainParam5

      item.column.listConfig = JSON.stringify(configToColumnJson(columnConfig))

    }
  })
  for (let key in columnMap) {
    extendColumns.push({
      isExtend: '1',
      isList: columnMap[key].isList,
      listSort: columnMap[key].listSort,
      isQuery: columnMap[key].isQuery,
      queryType: columnMap[key].queryType,
      name: key,
      blockChainParam4: columnMap[key].blockChainParam4,
      blockChainParam5: columnMap[key].blockChainParam5,
      listConfig: JSON.stringify(configToColumnJson(columnMap[key])),
    })
  }
  extendJsConfig.value.extendColumns = extendColumns

  updateMockColumns()
}

const syncMockColumnTitles = () => {
  const columnMap = {}
  allListColumns.value
      .map(item => initColumnListConfig(item, true))
      .forEach(item => {
        columnMap[item.name] = item
      })
  updateMockTableColumns(columnMap)
}

let mockSingleTable = computed(() => {
  return {
    disableInitLoad: true,
    data: [{}, {}, {}, {}, {}],
    rowButtons: extendJsConfig.value.singleTable__rowButtons,
    columns: mockColumns.value.filter(item => item.isList === '1').map(item => {
      return item.listConfig ? JSON.parse(item.listConfig) : {
        title: item.comments,
        dataIndex: item.name,
        minWidth: 150,
        align: item.align,
      }
    })
  }
})
const getSubmitData = () => {
  let json = []
  let arr = []
  displayFormItemArr.value.concat(hideFormItemArr.value).forEach((item, index) => {
    let obj = transformDynamicFormItemConfigToGenTableColumn(item)
    obj.formSort = index * 5//设置formSort
    if (item.type !== 'modalMultiSelect') {
      json.push(obj)
    } else {
      arr.push(obj)// modalMultiSelect 支持formSort
    }
  })
  fixedArr.forEach(item => {
    json.push(item)
  })
  table.extJava = JSON.stringify(arr)
  table.formProps = JSON.stringify(formPropsModel.value)
  table.extendJs = JSON.stringify(extendJsConfig.value)
  return {
    isBuildImport: extendJsConfig.value.list__buttons.filter(item => item.value === 'import').length > 0 ? '1' : '0',
    isBuildExport: extendJsConfig.value.list__buttons.filter(item => item.value === 'export').length > 0 ? '1' : '0',
    json: json,
    //多对多关系表
    extJava: JSON.stringify(arr),
    formProps: JSON.stringify(formPropsModel.value),
    extendJs: JSON.stringify(extendJsConfig.value),
    pkColumnName: formModel.value.pkColumnName || 'id'
  }
}
const validateCurrentFormItemConfig = () => {
  return new Promise((resolve, reject) => {
    if (!currentFormItem.value.type) {
      resolve()
      return
    }
    instance.refs.configForm.getRef().validateFields().then(() => {
      resolve()
    }).catch((err) => {
      if ((err?.errorFields || []).length === 0) {
        resolve()
        return
      }
      console.error('validateFields', err)
      Modal.warning({
        title: '提示',
        content: '请先完善当前表单项配置'
      })
      reject()
    })
  })
}

const getAiSourceTypeText = (type) => {
  const typeMap = {
    'server-draft': '服务器草稿',
    'local-draft': '本地草稿',
    'ai-draft': 'AI 新生成草稿',
    'form-material': '材料转换草稿',
    'current-designer': '当前设计器',
  }
  return typeMap[type] || type || '-'
}

const createAiFormalSaveConfirmContent = (review) => {
  const context = review.aiFormalizeContext || {}
  const source = context.source || {}
  if (!source.type || source.type === 'current-designer') {
    return []
  }
  const preview = context.formalizePreview || {}
  const summary = preview.summary || context.conversionSummary || review.summary || {}
  const target = preview.target || {}
  const sourceTitle = source.draftTitle || context.draft?.title || '-'
  const versionText = source.versionName || source.versionNo || source.versionId || '-'
  return [
    h('div', {
      style: {
        padding: '10px 12px',
        marginBottom: '10px',
        background: '#fff7e6',
        border: '1px solid #ffd591',
        borderRadius: '4px',
      },
    }, [
      h('div', {style: {fontWeight: 600, marginBottom: '6px'}}, '即将正式保存当前表单设计'),
      h('div', {style: {marginBottom: '4px'}}, `来源：${getAiSourceTypeText(source.type)} / ${sourceTitle} / ${versionText}`),
      h('div', {style: {marginBottom: '4px'}}, `目标表单：${target.formTitle || review.summary.formTitle || '-'} / ${target.formName || review.summary.formName || '-'}`),
      h('div', {style: {marginBottom: '4px'}}, `字段数量：${summary.fieldCount || 0}，列表字段：${summary.listCount || 0}，查询字段：${summary.queryCount || 0}`),
      h('div', {style: {color: '#8c5a00'}}, '本操作只保存表单设计元数据，不会自动建表、同步表结构或生成代码。'),
    ]),
  ]
}

const createSaveReviewContent = (review) => {
  const summary = review.summary
  const issueNodes = review.topIssues.map(issue => {
    const fieldText = issue.fieldLabel || issue.fieldName ? `（${issue.fieldLabel || issue.fieldName}${issue.fieldName ? ` / ${issue.fieldName}` : ''}）` : ''
    return h('li', {key: issue.id}, [
      h('strong', `${issue.title}${fieldText}`),
      h('div', {style: {color: '#4b5563', marginTop: '2px'}}, issue.description || issue.suggestion || ''),
    ])
  })
  return h('div', {class: 'save-review-content'}, [
    ...createAiFormalSaveConfirmContent(review),
    h('div', {style: {marginBottom: '8px'}}, `表单：${summary.formTitle || '-'} / ${summary.formName || '-'} / ${summary.module || '-'}`),
    h('div', {style: {marginBottom: '8px'}}, `字段 ${summary.fieldCount} 个，显示 ${summary.displayCount} 个，隐藏 ${summary.hiddenCount} 个，列表 ${summary.listCount} 个，查询 ${summary.queryCount} 个。`),
    h('div', {style: {marginBottom: '8px'}}, `审查结果：错误 ${summary.error} 个，警告 ${summary.warning} 个，建议 ${summary.suggestion} 个，可自动修复 ${summary.fixable} 个。`),
    issueNodes.length > 0 ? h('ol', {style: {paddingLeft: '18px', margin: '8px 0 0'}}, issueNodes) : null,
    review.issues.length > review.topIssues.length
        ? h('div', {style: {marginTop: '8px', color: '#6b7280'}}, `其余 ${review.issues.length - review.topIssues.length} 个问题可在 AI体检 中查看。`)
        : null,
  ])
}

const runBeforeSaveReview = () => {
  return new Promise((resolve, reject) => {
    const submitData = getSubmitData()
    const dsl = buildCurrentDesignDsl()
    const designIssues = explainFormDesignIssues(checkFormDesignDsl(dsl), dsl)
    const review = buildSaveReviewResult(dsl, designIssues, {
      hasAiRollbackSnapshot: Boolean(aiDraftRollbackSnapshot.value),
      aiApplyState: aiApplyState.value,
      aiFormalizeContext: aiFormalizeContext.value,
    })
    aiDesignDsl.value = dsl
    aiDesignIssues.value = designIssues
    aiDesignSummary.value = summarizeDesignIssues(designIssues)
    console.log('FormDesignBeforeSaveReview', review)
    console.log('FormDesignBeforeSaveAiFormalizeContext', aiFormalizeContext.value)

    if (!review.canSave) {
      Modal.error({
        title: '保存前审查未通过',
        content: createSaveReviewContent(review),
        okText: '返回修改',
      })
      reject()
      return
    }

    if (!review.needConfirm) {
      resolve(submitData)
      return
    }

    Modal.confirm({
      title: '保存前审查',
      content: createSaveReviewContent(review),
      okText: '继续保存',
      cancelText: '返回修改',
      onOk() {
        resolve(submitData)
      },
      onCancel() {
        reject()
      },
    })
  })
}

const getExtendSaveData = () => {
  return validateCurrentFormItemConfig().then(() => runBeforeSaveReview())
}

const formatAiSourceTime = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ')
}

const getAiDraftStatusText = (status) => {
  const statusMap = {
    saved: '已保存草稿',
    applied: '已应用到设计器',
    formal_saved: '已正式保存',
    archived: '已归档',
    rejected: '已拒绝',
  }
  return statusMap[status] || status || '-'
}

const loadAiFormalSourceDrafts = async (genTableId = '') => {
  const targetGenTableId = genTableId || formModel.value.id || table?.id || ''
  aiFormalSourceDrafts.value = []
  if (!targetGenTableId) {
    return
  }
  aiFormalSourceLoading.value = true
  try {
    const result = await listRemoteDrafts({
      genTableId: targetGenTableId,
      status: 'formal_saved',
      pageParam: {
        pageNo: 1,
        pageSize: 5,
      },
    })
    aiFormalSourceDrafts.value = result.rows || []
    console.log('FormDesignAiFormalSourceDrafts', aiFormalSourceDrafts.value)
  } catch (error) {
    aiFormalSourceDrafts.value = []
    console.warn('FormDesignAiFormalSourceDraftsFailed', error)
  } finally {
    aiFormalSourceLoading.value = false
  }
}

const createAiFormalSourceContent = () => {
  if (aiFormalSourceDrafts.value.length === 0) {
    return h('div', '当前正式表单暂未查询到 AI 草稿来源记录。')
  }
  return h('div', {style: {maxHeight: '420px', overflow: 'auto'}}, [
    h('div', {style: {marginBottom: '8px', color: '#4b5563'}}, '以下记录来自 AI 草稿正式保存追溯，仅表示表单元数据来源，不代表已建表或同步表结构。'),
    h('ol', {style: {paddingLeft: '18px', margin: 0}}, aiFormalSourceDrafts.value.map(draft => {
      const versionText = draft.currentVersion?.name || draft.currentVersionId || '-'
      return h('li', {key: draft.id, style: {marginBottom: '10px'}}, [
        h('div', {style: {fontWeight: 600}}, draft.title || '-'),
        h('div', `状态：${getAiDraftStatusText(draft.status)}，版本：${versionText}，版本数：${draft.versionCount || 0}`),
        h('div', `目标：${draft.target?.formTitle || '-'} / ${draft.target?.formName || '-'} / ${draft.target?.module || '-'}`),
        h('div', `最近正式保存：${formatAiSourceTime(draft.lastAppliedAt || draft.updatedAt)}`),
        h('div', {style: {color: '#6b7280'}}, `草稿ID：${draft.id || '-'}`),
      ])
    })),
  ])
}

const showAiFormalSource = () => {
  Modal.confirm({
    title: 'AI来源草稿',
    content: createAiFormalSourceContent(),
    okText: '打开AI创建',
    cancelText: '关闭',
    onOk() {
      openAiCreateDraft()
    },
  })
}

const getSavedGenTableId = (saveResult = {}, submitData = {}) => {
  return saveResult?.data?.entityId
      || saveResult?.data?.id
      || saveResult?.insertedId
      || submitData?.id
      || formModel.value.id
      || table?.id
      || ''
}

const shouldTraceFormalSave = (saveResult = {}, options = {}) => {
  if (options.tempSave) {
    return false
  }
  if (saveResult && saveResult.code !== undefined && Number(saveResult.code) !== 0) {
    return false
  }
  const source = aiFormalizeContext.value?.source || {}
  return source.type === 'server-draft' && source.draftId && source.versionId
}

const buildFormalSaveTracePayload = (saveResult = {}, submitData = {}) => {
  const context = aiFormalizeContext.value || {}
  const source = context.source || {}
  const preview = context.formalizePreview || {}
  const target = preview.target || {}
  const checksum = preview.checksum || {}
  const genTableId = getSavedGenTableId(saveResult, submitData)
  return {
    draftId: source.draftId || '',
    versionId: source.versionId || '',
    genTableId,
    formName: target.formName || submitData.name || formModel.value.name || '',
    formTitle: target.formTitle || submitData.comments || formModel.value.comments || '',
    beforeChecksum: checksum.before || source.checksum || '',
    afterChecksum: checksum.after || '',
    detailJson: JSON.stringify({
      source,
      draft: context.draft || {},
      appliedResult: context.appliedResult || {},
      conversionSummary: context.conversionSummary || {},
      formalizePreview: context.formalizePreview || null,
      savedAt: new Date().toISOString(),
    }),
  }
}

const handleFormalSaveSuccess = async (saveResult = {}, submitData = {}, options = {}) => {
  if (!shouldTraceFormalSave(saveResult, options)) {
    return
  }
  const payload = buildFormalSaveTracePayload(saveResult, submitData)
  if (!payload.genTableId) {
    console.warn('FormDesignFormalSaveTraceSkipped', {
      reason: 'missing genTableId',
      payload,
      saveResult,
    })
    return
  }
  const traceKey = [
    payload.draftId,
    payload.versionId,
    payload.genTableId,
    payload.afterChecksum,
  ].join('|')
  if (aiFormalSaveTraceState.value.lastTraceKey === traceKey) {
    return
  }
  try {
    const response = await markRemoteDraftFormalSaved(payload)
    aiFormalSaveTraceState.value = {
      lastTraceKey: traceKey,
    }
    console.log('FormDesignFormalSaveTraceResult', {
      payload,
      response,
    })
    loadAiFormalSourceDrafts(payload.genTableId)
  } catch (error) {
    console.error('FormDesignFormalSaveTraceFailed', error)
    Modal.warning({
      title: 'AI追溯记录失败',
      content: '正式表单已保存成功，但 AI 草稿来源追溯记录失败。可以稍后手动质控数据库中的草稿状态和应用日志。',
    })
  }
}

const router = useRouter();

const preview = () => {
  if (window.previewWindow) {
    window.previewWindow.focus()
    dispatchMockEvent(table, getSubmitData())
    return
  }
  let routerUrl = router.resolve({
    path: '/dynamic',
  })
  const windowFeatures = `width=${window.screen.width},height=${window.screen.height},location=yes,menubar=yes,toolbar=yes`;
  window.previewWindow = window.open(getWebUrl() + routerUrl.href, '_blank', windowFeatures)
  updateMockColumns()

  //当 childWindow 页面关闭时，
  let timer = setInterval(() => {
    if (window.previewWindow.closed) {
      window.previewWindow = null
      clearInterval(timer);
    }
  }, 200);
}

const buildCurrentDesignDsl = () => {
  return exportCurrentDesignToDsl({
    formModel,
    table,
    formPropsModel,
    extendJsConfig,
    displayFormItemArr,
    hideFormItemArr,
    fixedArr,
    mockColumns,
  })
}

const exportDesignDsl = () => {
  const dsl = buildCurrentDesignDsl()
  const text = JSON.stringify(dsl, null, 2)
  console.log('FormDesignDSL', dsl)
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text).then(() => {
      Modal.success({
        title: '导出成功',
        content: 'FormDesignDSL 已输出到控制台，并复制到剪贴板。'
      })
    }).catch(() => {
      Modal.info({
        title: '导出成功',
        content: 'FormDesignDSL 已输出到控制台。'
      })
    })
  } else {
    Modal.info({
      title: '导出成功',
      content: 'FormDesignDSL 已输出到控制台。'
    })
  }
}

const openAiCreateDraft = () => {
  aiCreateDraftDrawerVisible.value = true
}

const checkDesignDsl = () => {
  console.log('AI体检开始')
  try {
    const dsl = buildCurrentDesignDsl()
    const issues = explainFormDesignIssues(checkFormDesignDsl(dsl), dsl)
    const summary = summarizeDesignIssues(issues)
    aiDesignDsl.value = dsl
    aiDesignIssues.value = issues
    aiDesignSummary.value = summary
    aiCheckDrawerVisible.value = true
    console.log('FormDesignDSL', dsl)
    console.log('FormDesignCheckIssues', issues)
    if (issues.length > 0) {
      console.table(issues.map(issue => ({
        level: issue.level,
        fieldName: issue.fieldName,
        fieldLabel: issue.fieldLabel,
        title: issue.title,
        fixable: issue.fixable,
      })))
      issues
          .filter(issue => issue.id.indexOf('duplicate-field-name:') === 0)
          .forEach(issue => {
            console.log(`DuplicateFieldDetails:${issue.fieldName}`)
            console.table(issue.meta.fields || [])
          })
    }
  } catch (e) {
    console.error('AI体检失败', e)
    Modal.error({
      title: 'AI体检失败',
      content: e?.message || '执行体检时发生未知错误，请打开控制台查看 AI体检失败 日志。',
    })
  }
}

const refreshAiDesignCheck = () => {
  const dsl = buildCurrentDesignDsl()
  const issues = explainFormDesignIssues(checkFormDesignDsl(dsl), dsl)
  aiDesignDsl.value = dsl
  aiDesignIssues.value = issues
  aiDesignSummary.value = summarizeDesignIssues(issues)
  console.log('FormDesignDSL', dsl)
  console.log('FormDesignCheckIssues', issues)
}

const applyAiDesignFixes = (selectedIssues = []) => {
  if (!selectedIssues.length) {
    Modal.warning({
      title: '未选择修复项',
      content: '请先勾选需要应用的修复项。',
    })
    return
  }

  Modal.confirm({
    title: '应用修复到设计器？',
    content: `即将把 ${selectedIssues.length} 个修复项应用到当前设计器内存状态。此操作不会直接保存数据库，确认无误后仍需点击原保存按钮。`,
    okText: '应用',
    cancelText: '取消',
    onOk() {
      const result = applyDesignFixes({
        displayFormItemArr,
        hideFormItemArr,
        fixedArr,
        extendJsConfig,
      }, selectedIssues)

      const appliedFieldNames = result.appliedChanges.map(change => change.fieldName)
      if (currentFormItem.value?.column?.name && appliedFieldNames.includes(currentFormItem.value.column.name)) {
        currentFormItemFormModel.value = currentFormItem.value.toJson()
      }
      updateMockColumns()
      syncMockColumnTitles()
      refreshAiDesignCheck()
      if (result.appliedCount > 0) {
        aiApplyState.value = {
          ...aiApplyState.value,
          fixAppliedCount: Number(aiApplyState.value.fixAppliedCount || 0) + result.appliedCount,
          lastAppliedAt: new Date().toISOString(),
        }
      }

      console.log('FormDesignAppliedFixes', result)
      if (result.appliedCount > 0) {
        Modal.success({
          title: '应用成功',
          content: `已应用 ${result.appliedCount} 项字段变更。请检查预览效果，确认后再保存。`,
        })
      } else {
        Modal.warning({
          title: '未产生变更',
          content: result.skippedIssues.length > 0 ? result.skippedIssues.map(item => item.reason).join('；') : '所选修复项与当前配置一致。',
        })
      }
    },
  })
}

const resetCurrentDesignerSelection = () => {
  currentFormItem.value = new DynamicFormItemConfig()
  currentFormItemFormModel.value = currentFormItem.value.toJson()
  formSetting.value = true
  mockFormModel.value = {}
}

const applyAiDraftStatePatch = (patch, applyContext = {}) => {
  if (!patch || !patch.canApply) {
    Modal.warning({
      title: '暂不能应用',
      content: '转换预览存在错误，请先处理后再应用。',
    })
    return
  }

  Modal.confirm({
    title: '应用 DSL 草稿到设计器？',
    content: `即将用草稿替换当前动态字段：显示区域 ${patch.displayFormItemArr.length} 个，隐藏区域 ${patch.hideFormItemArr.length} 个。应用前会自动保存一份可撤销快照；也建议先点击“导出DSL”备份当前设计。此操作只修改当前页面内存，不会保存数据库；确认无误后仍需点击原保存按钮。`,
    okText: '应用',
    cancelText: '取消',
    onOk() {
      const result = applyDraftStatePatch({
        formModel,
        formPropsModel,
        displayFormItemArr,
        hideFormItemArr,
      }, patch)
      aiDraftRollbackSnapshot.value = result.snapshot
      aiApplyState.value = {
        ...aiApplyState.value,
        draftApplied: true,
        draftFieldCount: result.appliedFieldCount,
        lastAppliedAt: new Date().toISOString(),
      }
      aiFormalizeContext.value = {
        ...applyContext,
        appliedResult: {
          appliedFieldCount: result.appliedFieldCount,
          listFieldCount: result.listFieldCount,
          queryFieldCount: result.queryFieldCount,
        },
        appliedAt: new Date().toISOString(),
      }
      aiFormalSaveTraceState.value = {
        lastTraceKey: '',
      }

      resetCurrentDesignerSelection()

      updateMockColumns()
      syncMockColumnTitles()
      refreshAiDesignCheck()
      aiCreateDraftDrawerVisible.value = false

      console.log('FormDesignAppliedDraftStatePatch', result)
      console.log('FormDesignAiFormalizeContext', aiFormalizeContext.value)
      Modal.success({
        title: '应用成功',
        content: `已应用 ${result.appliedFieldCount} 个草稿字段，包含 ${result.listFieldCount} 个列表字段、${result.queryFieldCount} 个查询条件。页面右上角可点击“撤销AI应用”恢复应用前状态；确认后再保存。`,
      })
    },
  })
}

const rollbackAiDraftState = () => {
  if (!aiDraftRollbackSnapshot.value) {
    Modal.info({
      title: '暂无可撤销内容',
      content: '当前没有最近一次 AI 草稿应用快照。',
    })
    return
  }

  Modal.confirm({
    title: '撤销本次 AI 应用？',
    content: '将恢复到最近一次应用 AI 草稿之前的设计器内存状态。此操作不会保存数据库，也不会调用保存接口。',
    okText: '撤销',
    cancelText: '取消',
    onOk() {
      const result = rollbackDraftStateSnapshot({
        formModel,
        formPropsModel,
        displayFormItemArr,
        hideFormItemArr,
      }, aiDraftRollbackSnapshot.value)

      aiDraftRollbackSnapshot.value = null
      aiFormalizeContext.value = null
      aiFormalSaveTraceState.value = {
        lastTraceKey: '',
      }
      aiApplyState.value = {
        ...aiApplyState.value,
        draftApplied: false,
        draftFieldCount: 0,
        lastAppliedAt: new Date().toISOString(),
      }
      resetCurrentDesignerSelection()
      updateMockColumns()
      refreshAiDesignCheck()

      console.log('FormDesignRollbackDraftStatePatch', result)
      Modal.success({
        title: '已撤销',
        content: `已恢复应用前状态，共 ${result.restoredFieldCount} 个动态字段。撤销未触发保存接口。`,
      })
    },
  })
}

</script>
<style lang="less" scoped>
@import "./formDesign";

.design-top-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.design-action-group {
  min-height: 30px;
  padding: 3px 6px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fff;
}

.design-action-group.ai {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.design-action-label {
  color: #64748b;
  font-size: 12px;
  line-height: 1;
  white-space: nowrap;
}

.design-action-group :deep(.ant-btn) {
  height: 24px;
  padding: 0 8px;
}

@media (max-width: 1200px) {
  .design-top-actions {
    justify-content: flex-start;
  }
}

.drag-item-move,.drag-item-enter-active,.drag-item-leave-active{
  transition: all 0.3s ease;
}
.formtitle{
  :deep(.business-form-title.default-icon::before){
      content: '';
      display: block;
      height: 1px;
      background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
      margin-right: 30px;
      flex: 1;
      opacity: .3;
  }
}

.controltitle {
  :deep(.business-form-title.default-icon::before) {
    content: '';
    display: block;
    height: 1px;
    background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
    margin-right: 30px;
    flex: 1;
    opacity: .3;
  }
}
.poptitle{
  :deep(.business-form-title.default-icon::before){
    content: '';
    display: block;
    height: 1px;
    background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
    margin-right: 30px;
    flex: 1;
    opacity: .3;
  }
}
.devtitle{
  :deep(.business-form-title.default-icon::before){
    content: '';
    display: block;
    height: 1px;
    background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
    margin-right: 30px;
    flex: 1;
    opacity: .3;
  }
}
</style>
