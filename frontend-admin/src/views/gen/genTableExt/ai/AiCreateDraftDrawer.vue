<template>
  <a-drawer
      :visible="visible"
      title="AI创建草稿"
      placement="right"
      :width="760"
      :z-index="2000"
      :body-style="{ padding: '0', overflow: 'auto' }"
      @close="close"
  >
    <div class="ai-draft-drawer">
      <div class="demo-flow-panel">
        <div class="demo-flow-head">
          <div>
            <div class="demo-flow-title">AI 表单创建流程</div>
            <div class="demo-flow-status">{{ demoFlowStatusText }}</div>
          </div>
          <div class="demo-flow-head-actions">
            <a-tag color="blue">工作流程</a-tag>
            <a-button
                size="small"
                type="primary"
                :loading="footerPrimaryAction.loading"
                :disabled="footerPrimaryAction.disabled"
                @click="runFooterPrimaryAction"
            >{{ footerPrimaryAction.label }}</a-button>
          </div>
        </div>
        <div class="demo-flow-guide">
          <div class="demo-flow-guide-item">
            <span class="demo-flow-guide-label">当前步骤</span>
            <span class="demo-flow-guide-value">{{ activeDemoFlowStepText }}</span>
          </div>
          <div class="demo-flow-guide-item">
            <span class="demo-flow-guide-label">建议下一步</span>
            <span class="demo-flow-guide-value">{{ footerPrimaryAction.label }}</span>
          </div>
        </div>
        <div class="demo-flow-steps">
          <div
              v-for="step in demoFlowSteps"
              :key="step.key"
              :title="step.detail"
              :class="['demo-flow-step', step.active ? 'active' : '', step.done ? 'done' : '']"
          >
            <div class="demo-flow-index">{{ step.index }}</div>
            <div class="demo-flow-body">
              <div class="demo-flow-step-title">{{ step.title }}</div>
              <div class="demo-flow-step-state">{{ step.stateText }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="draft-input-panel">
        <div class="material-source-head">
          <div>
            <div class="material-source-title">输入来源</div>
            <div class="material-source-subtitle">先选择材料来源；表格和文件先识别字段，手动描述可直接生成 DSL 草稿。应用到设计器前不会改动当前页面。</div>
          </div>
          <a-tag color="blue">材料识别</a-tag>
        </div>

        <a-tabs v-model:activeKey="materialSourceType" class="material-source-tabs">
          <a-tab-pane key="manual" tab="手动描述">
            <div class="draft-mode-row">
              <span class="draft-mode-label">生成模式</span>
              <a-radio-group v-model:value="draftMode" button-style="solid">
                <a-radio-button value="auto">自动识别</a-radio-button>
                <a-radio-button value="normal">普通表单</a-radio-button>
                <a-radio-button value="document-form">公文文单</a-radio-button>
                <a-radio-button value="approval">审批表单</a-radio-button>
              </a-radio-group>
            </div>
            <a-textarea
                v-model:value="requirementText"
                :rows="6"
                placeholder="例如：收文处理笺，需要来文单位、文号、标题、收文日期、缓急、局领导批示、办公室批分、附件"
            />
            <div class="draft-input-actions">
              <div class="draft-sample-actions">
                <a-button size="small" @click="useSample('contract')">合同审批示例</a-button>
                <a-button size="small" @click="useSample('receive')">收文处理笺示例</a-button>
                <a-button size="small" @click="useSample('purchase')">采购申请示例</a-button>
              </div>
              <div class="draft-primary-actions">
                <a-button @click="runRequirementClarification">需求澄清</a-button>
                <a-button type="primary" :loading="aiDraftGenerating" :disabled="aiDraftGenerating" @click="generateDraft">{{ aiDraftGenerateButtonText }}</a-button>
              </div>
            </div>
          </a-tab-pane>
          <a-tab-pane key="table-text" tab="粘贴表格">
            <div class="material-pane">
              <a-textarea
                  v-model:value="materialTableText"
                  :rows="7"
                  placeholder="从 Excel/WPS/网页表格复制后粘贴到这里。AI 会结合表头和样例值识别字段、控件类型、列表字段和查询条件。"
              />
              <div class="material-pane-footer">
                <div class="material-pane-meta">
                  <a-tag color="green">P0</a-tag>
                  <span>已输入 {{ materialTableLineCount }} 行，预计 {{ materialTableColumnCount }} 列；下一步点击 AI 识别材料。</span>
                </div>
                <a-button
                    type="primary"
                    :loading="aiMaterialRecognizing"
                    :disabled="!materialTableText.trim() || aiMaterialRecognizing"
                    @click="recognizeTableTextMaterial"
                >{{ aiMaterialRecognizeButtonText }}</a-button>
              </div>
              <div v-if="materialRecognitionResult" class="material-preview">
                <div class="material-preview-head">
                  <div>
                    <div class="material-preview-title">材料识别结果</div>
                    <div class="material-preview-subtitle">
                      {{ materialPreviewTitle }}，{{ materialPreviewSceneText }}，置信度 {{ materialPreviewConfidenceText }}
                    </div>
                  </div>
                  <div class="material-preview-actions">
                    <a-tag :color="materialPreviewIssueSummary.error > 0 ? 'red' : materialPreviewIssueSummary.warning > 0 ? 'orange' : 'green'">
                      {{ materialPreviewIssueSummary.total || 0 }} 个问题
                    </a-tag>
                    <a-button
                        size="small"
                        type="primary"
                        :disabled="!canGenerateDraftFromMaterial"
                        @click="generateDraftFromMaterial"
                    >生成DSL草稿</a-button>
                  </div>
                </div>
                <div class="material-preview-stats">
                  <div class="material-stat">
                    <span class="material-stat-count">{{ materialPreviewFields.length }}</span>
                    <span>字段</span>
                  </div>
                  <div class="material-stat">
                    <span class="material-stat-count">{{ materialPreviewTableRows.length }}</span>
                    <span>样例行</span>
                  </div>
                  <div class="material-stat">
                    <span class="material-stat-count">{{ materialPreviewGroups.length }}</span>
                    <span>分组</span>
                  </div>
                </div>
                <a-collapse class="material-debug-collapse" :bordered="false">
                  <a-collapse-panel key="debug">
                    <template #header>
                      <div class="material-debug-header">
                        <span>调试信息</span>
                        <span>材料 JSON / 控制台输出</span>
                      </div>
                    </template>
                    <div class="material-debug-actions">
                      <a-button size="small" @click="copyMaterialJson">复制材料JSON</a-button>
                      <a-button size="small" @click="printMaterial">输出到控制台</a-button>
                    </div>
                  </a-collapse-panel>
                </a-collapse>
                <div class="material-confirm-panel">
                  <div class="material-confirm-head">
                    <div>
                      <div class="material-confirm-title">识别结果确认</div>
                      <div class="material-confirm-subtitle">应用前确认字段标题、英文名、控件类型、列表和查询建议；可先筛选待确认项逐个修正。</div>
                    </div>
                    <div class="material-confirm-actions">
                      <a-tag v-if="materialConfirmRiskIssues.length > 0" color="orange">待确认 {{ materialConfirmRiskIssues.length }}</a-tag>
                      <a-tag v-else color="green">建议正常</a-tag>
                      <a-tag :color="materialConfirmationTagColor">{{ materialConfirmationTagText }}</a-tag>
                      <a-button size="small" @click="normalizeAllMaterialFields">按规则补齐</a-button>
                      <a-button
                          size="small"
                          type="primary"
                          :disabled="!materialCanConfirm || materialConfirmed"
                          @click="confirmCurrentMaterialRecognition"
                      >{{ materialConfirmationButtonText }}</a-button>
                    </div>
                  </div>
                  <div class="material-confirm-toolbar">
                    <a-input
                        v-model:value="materialConfirmKeyword"
                        allow-clear
                        size="small"
                        class="material-confirm-search"
                        placeholder="搜索字段标题、英文名、样例"
                    />
                    <a-radio-group v-model:value="materialConfirmFilter" size="small" button-style="solid">
                      <a-radio-button
                          v-for="option in materialConfirmFilterOptions"
                          :key="option.value"
                          :value="option.value"
                      >{{ option.label }}</a-radio-button>
                    </a-radio-group>
                    <div class="material-confirm-mini-stats">
                      <a-tag>显示 {{ materialConfirmStats.visible }}/{{ materialConfirmStats.total }}</a-tag>
                      <a-tag color="green">列表 {{ materialConfirmStats.list }}</a-tag>
                      <a-tag color="cyan">查询 {{ materialConfirmStats.query }}</a-tag>
                      <a-tag v-if="materialConfirmStats.error" color="red">错误 {{ materialConfirmStats.error }}</a-tag>
                      <a-tag v-if="materialConfirmStats.warning" color="orange">警告 {{ materialConfirmStats.warning }}</a-tag>
                    </div>
                  </div>
                  <div :class="['material-confirm-status', materialConfirmed ? 'confirmed' : materialPreviewIssueSummary.error > 0 ? 'blocked' : 'pending']">
                    <div>
                      <div class="material-confirm-status-title">{{ materialConfirmationTitle }}</div>
                      <div class="material-confirm-status-desc">{{ materialConfirmationDescription }}</div>
                    </div>
                    <a-button
                        v-if="materialConfirmRiskIssues.length > 0"
                        size="small"
                        type="link"
                        @click="showMaterialRiskFields"
                    >查看待确认项</a-button>
                  </div>
                  <div class="material-revision-panel">
                    <div class="material-revision-head">
                      <div>
                        <div class="material-revision-title">AI 二次修正</div>
                        <div class="material-revision-subtitle">识别结果不理想时，可以直接描述修正要求，AI 会在当前材料基础上重算字段建议。</div>
                      </div>
                      <div class="material-revision-examples">
                        <a-button
                            v-for="example in materialRevisionExamples"
                            :key="example.label"
                            size="small"
                            @click="appendMaterialRevisionInstruction(example.text)"
                        >{{ example.label }}</a-button>
                      </div>
                    </div>
                    <div class="material-revision-actions">
                      <a-textarea
                          v-model:value="materialRevisionInstruction"
                          :rows="2"
                          placeholder="例如：把描述字段改成多行文本并占整行，不进入列表和查询；把年月字段格式改为 yyyy-MM。"
                      />
                      <a-button
                          type="primary"
                          :loading="materialRevising"
                          :disabled="materialRevising || !materialRevisionInstruction.trim()"
                          @click="reviseMaterialByInstruction"
                      >{{ aiMaterialRevisionButtonText }}</a-button>
                    </div>
                    <div v-if="materialRevisionReview" class="material-revision-review">
                      <div class="material-revision-review-head">
                        <div>
                          <div class="material-revision-review-title">最近一次修正</div>
                          <div class="material-revision-review-subtitle">{{ materialRevisionReviewSummaryText }}</div>
                        </div>
                        <div class="material-revision-review-actions">
                          <a-button size="small" @click="showRevisionChangedOnly">只看变更字段</a-button>
                          <a-button size="small" @click="restoreMaterialRevisionBefore">撤回本次修正</a-button>
                        </div>
                      </div>
                      <div class="material-revision-change-list">
                        <a-tag v-if="materialRevisionReview.added.length > 0" color="green">新增 {{ materialRevisionReview.added.length }}</a-tag>
                        <a-tag v-if="materialRevisionReview.removed.length > 0" color="red">移除 {{ materialRevisionReview.removed.length }}</a-tag>
                        <a-tag v-if="materialRevisionReview.changed.length > 0" color="orange">变更 {{ materialRevisionReview.changed.length }}</a-tag>
                        <span
                            v-for="item in materialRevisionVisibleChanges"
                            :key="item.key"
                            class="material-revision-change-item"
                        >{{ item.text }}</span>
                        <span v-if="materialRevisionVisibleChanges.length === 0" class="material-revision-no-change">本次修正没有发现字段级差异。</span>
                      </div>
                    </div>
                  </div>
                  <div v-if="materialConfirmRiskIssues.length > 0" class="material-risk-list">
                    <div
                        v-for="issue in materialConfirmRiskIssues.slice(0, 6)"
                        :key="issue.id"
                        class="material-risk-row"
                    >
                      <a-tag :color="levelColorMap[issue.level] || 'orange'">{{ levelTextMap[issue.level] || issue.level }}</a-tag>
                      <span>{{ issue.fieldLabel ? `${issue.fieldLabel}：` : '' }}{{ issue.description || issue.title }}</span>
                    </div>
                    <div v-if="materialConfirmRiskIssues.length > 6" class="material-risk-more">
                      还有 {{ materialConfirmRiskIssues.length - 6 }} 个待确认项，可在下方字段行查看。
                    </div>
                  </div>
                  <div v-if="materialFilteredFieldEntries.length === 0" class="material-confirm-empty">
                    <a-empty :description="materialConfirmEmptyText"/>
                    <a-button size="small" @click="resetMaterialConfirmFilter">显示全部字段</a-button>
                  </div>
                  <div v-else class="material-confirm-table">
                    <div class="material-confirm-grid material-confirm-header">
                      <div>#</div>
                      <div>字段</div>
                      <div>控件</div>
                      <div>显示</div>
                      <div>列表建议</div>
                      <div>查询建议</div>
                    </div>
                    <div
                        v-for="entry in materialFilteredFieldEntries"
                        :key="entry.field.id || entry.index"
                        :class="['material-confirm-row', entry.risks.length > 0 ? 'has-risk' : '']"
                    >
                      <div class="material-confirm-grid">
                        <div class="material-confirm-index">
                          <span>{{ entry.index + 1 }}</span>
                          <a-tag
                              v-if="entry.risks.length > 0"
                              color="orange"
                          >{{ entry.risks.length }}</a-tag>
                          <a-button
                              v-if="entry.risks.length > 0"
                              size="small"
                              type="link"
                              @click="normalizeMaterialFieldAt(entry.field)"
                          >修正</a-button>
                        </div>
                        <div class="material-field-editor">
                          <a-input
                              v-model:value="entry.field.label"
                              size="small"
                              placeholder="字段标题"
                          />
                          <a-input
                              v-model:value="entry.field.nameHint"
                              size="small"
                              placeholder="英文名"
                          />
                          <div v-if="entry.field.valueExample" class="material-example-text">样例：{{ entry.field.valueExample }}</div>
                        </div>
                        <div class="material-type-editor">
                          <a-select
                              v-model:value="entry.field.typeHint"
                              size="small"
                              :options="materialFieldTypeOptions"
                          />
                          <a-radio-group v-model:value="entry.field.spanHint" size="small">
                            <a-radio-button :value="12">半行</a-radio-button>
                            <a-radio-button :value="24">整行</a-radio-button>
                          </a-radio-group>
                        </div>
                        <div class="material-check-editor">
                          <a-checkbox v-model:checked="entry.field.requiredHint">必填</a-checkbox>
                          <a-checkbox v-model:checked="entry.field.listHint">列表</a-checkbox>
                          <a-checkbox v-model:checked="entry.field.queryHint">查询</a-checkbox>
                        </div>
                        <div class="material-priority-editor">
                          <a-input-number
                              v-model:value="entry.field.listPriority"
                              size="small"
                              :min="0"
                              :max="100"
                              :precision="0"
                          />
                          <div class="material-reason-text">{{ entry.field.listReason || '无列表理由' }}</div>
                        </div>
                        <div class="material-priority-editor">
                          <div class="material-query-row">
                            <a-input-number
                                v-model:value="entry.field.queryPriority"
                                size="small"
                                :min="0"
                                :max="100"
                                :precision="0"
                            />
                            <a-select
                                v-model:value="entry.field.queryMode"
                                size="small"
                                :options="materialQueryModeOptions"
                            />
                          </div>
                          <div class="material-reason-text">{{ entry.field.queryReason || '无查询理由' }}</div>
                        </div>
                      </div>
                      <div
                          v-if="entry.risks.length > 0"
                          class="material-row-risk-list"
                      >
                        <a-tag
                            v-for="risk in entry.risks"
                            :key="risk.id"
                            color="orange"
                        >{{ risk.title }}</a-tag>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="materialPreviewIssues.length > 0" class="material-issue-list">
                  <div
                      v-for="issue in materialPreviewIssues"
                      :key="issue.id"
                      class="material-issue-row"
                  >
                    <a-tag :color="levelColorMap[issue.level] || 'default'">{{ levelTextMap[issue.level] || issue.level }}</a-tag>
                    <span>{{ issue.title }}：{{ issue.description || issue.suggestion }}</span>
                  </div>
                </div>
              </div>
            </div>
          </a-tab-pane>
          <a-tab-pane key="file" tab="上传文件">
            <div class="material-pane">
              <a-upload
                  :file-list="materialFileList"
                  :before-upload="handleMaterialFileBeforeUpload"
                  :show-upload-list="{ showRemoveIcon: true }"
                  accept=".xlsx,.xls,.docx,.pdf,.png,.jpg,.jpeg,.webp"
                  @remove="removeMaterialFile"
              >
                <a-button>选择 Excel / Word / PDF / 图片文件</a-button>
              </a-upload>
              <div class="material-upload-note">
                当前支持 Excel、Word .docx、文本 PDF、图片和扫描 PDF；图片 / 扫描 PDF 会走 OCR，通常需要更久。
              </div>
              <div class="material-pane-footer">
                <div class="material-pane-meta">
                  <a-tag color="orange">P1-P3</a-tag>
                  <span>{{ materialFileSummary }}</span>
                </div>
                <a-button type="primary" :loading="aiMaterialRecognizing" :disabled="materialFileList.length === 0 || aiMaterialRecognizing" @click="previewMaterialRecognition">{{ aiMaterialRecognizeButtonText }}</a-button>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>

        <div v-if="materialRecognitionResult && materialSourceType === 'file'" class="material-preview">
          <div class="material-preview-head">
            <div>
              <div class="material-preview-title">材料识别结果</div>
              <div class="material-preview-subtitle">
                {{ materialPreviewTitle }}，{{ materialPreviewSceneText }}，置信度 {{ materialPreviewConfidenceText }}
              </div>
            </div>
            <div class="material-preview-actions">
              <a-tag :color="materialPreviewIssueSummary.error > 0 ? 'red' : materialPreviewIssueSummary.warning > 0 ? 'orange' : 'green'">
                {{ materialPreviewIssueSummary.total || 0 }} 个问题
              </a-tag>
              <a-button
                  size="small"
                  type="primary"
                  :disabled="!canGenerateDraftFromMaterial"
                  @click="generateDraftFromMaterial"
              >生成DSL草稿</a-button>
            </div>
          </div>
          <div class="material-preview-stats">
            <div class="material-stat">
              <span class="material-stat-count">{{ materialPreviewFields.length }}</span>
              <span>字段</span>
            </div>
            <div class="material-stat">
              <span class="material-stat-count">{{ materialPreviewTableRows.length }}</span>
              <span>样例行</span>
            </div>
            <div class="material-stat">
              <span class="material-stat-count">{{ materialPreviewGroups.length }}</span>
              <span>分组</span>
            </div>
          </div>
          <a-collapse class="material-debug-collapse" :bordered="false">
            <a-collapse-panel key="debug">
              <template #header>
                <div class="material-debug-header">
                  <span>调试信息</span>
                  <span>材料 JSON / 控制台输出</span>
                </div>
              </template>
              <div class="material-debug-actions">
                <a-button size="small" @click="copyMaterialJson">复制材料JSON</a-button>
                <a-button size="small" @click="printMaterial">输出到控制台</a-button>
              </div>
            </a-collapse-panel>
          </a-collapse>
          <div class="material-confirm-panel">
            <div class="material-confirm-head">
              <div>
                <div class="material-confirm-title">识别结果确认</div>
                <div class="material-confirm-subtitle">应用前确认字段标题、英文名、控件类型、列表和查询建议；可先筛选待确认项逐个修正。</div>
              </div>
              <div class="material-confirm-actions">
                <a-tag v-if="materialConfirmRiskIssues.length > 0" color="orange">待确认 {{ materialConfirmRiskIssues.length }}</a-tag>
                <a-tag v-else color="green">建议正常</a-tag>
                <a-tag :color="materialConfirmationTagColor">{{ materialConfirmationTagText }}</a-tag>
                <a-button size="small" @click="normalizeAllMaterialFields">按规则补齐</a-button>
                <a-button
                    size="small"
                    type="primary"
                    :disabled="!materialCanConfirm || materialConfirmed"
                    @click="confirmCurrentMaterialRecognition"
                >{{ materialConfirmationButtonText }}</a-button>
              </div>
            </div>
            <div class="material-confirm-toolbar">
              <a-input
                  v-model:value="materialConfirmKeyword"
                  allow-clear
                  size="small"
                  class="material-confirm-search"
                  placeholder="搜索字段标题、英文名、样例"
              />
              <a-radio-group v-model:value="materialConfirmFilter" size="small" button-style="solid">
                <a-radio-button
                    v-for="option in materialConfirmFilterOptions"
                    :key="option.value"
                    :value="option.value"
                >{{ option.label }}</a-radio-button>
              </a-radio-group>
              <div class="material-confirm-mini-stats">
                <a-tag>显示 {{ materialConfirmStats.visible }}/{{ materialConfirmStats.total }}</a-tag>
                <a-tag color="green">列表 {{ materialConfirmStats.list }}</a-tag>
                <a-tag color="cyan">查询 {{ materialConfirmStats.query }}</a-tag>
                <a-tag v-if="materialConfirmStats.error" color="red">错误 {{ materialConfirmStats.error }}</a-tag>
                <a-tag v-if="materialConfirmStats.warning" color="orange">警告 {{ materialConfirmStats.warning }}</a-tag>
              </div>
            </div>
            <div :class="['material-confirm-status', materialConfirmed ? 'confirmed' : materialPreviewIssueSummary.error > 0 ? 'blocked' : 'pending']">
              <div>
                <div class="material-confirm-status-title">{{ materialConfirmationTitle }}</div>
                <div class="material-confirm-status-desc">{{ materialConfirmationDescription }}</div>
              </div>
              <a-button
                  v-if="materialConfirmRiskIssues.length > 0"
                  size="small"
                  type="link"
                  @click="showMaterialRiskFields"
              >查看待确认项</a-button>
            </div>
            <div class="material-revision-panel">
              <div class="material-revision-head">
                <div>
                  <div class="material-revision-title">AI 二次修正</div>
                  <div class="material-revision-subtitle">识别结果不理想时，可以直接描述修正要求，AI 会在当前材料基础上重算字段建议。</div>
                </div>
                <div class="material-revision-examples">
                  <a-button
                      v-for="example in materialRevisionExamples"
                      :key="example.label"
                      size="small"
                      @click="appendMaterialRevisionInstruction(example.text)"
                  >{{ example.label }}</a-button>
                </div>
              </div>
              <div class="material-revision-actions">
                <a-textarea
                    v-model:value="materialRevisionInstruction"
                    :rows="2"
                    placeholder="例如：把描述字段改成多行文本并占整行，不进入列表和查询；把年月字段格式改为 yyyy-MM。"
                />
                <a-button
                    type="primary"
                    :loading="materialRevising"
                    :disabled="materialRevising || !materialRevisionInstruction.trim()"
                    @click="reviseMaterialByInstruction"
                >{{ aiMaterialRevisionButtonText }}</a-button>
              </div>
              <div v-if="materialRevisionReview" class="material-revision-review">
                <div class="material-revision-review-head">
                  <div>
                    <div class="material-revision-review-title">最近一次修正</div>
                    <div class="material-revision-review-subtitle">{{ materialRevisionReviewSummaryText }}</div>
                  </div>
                  <div class="material-revision-review-actions">
                    <a-button size="small" @click="showRevisionChangedOnly">只看变更字段</a-button>
                    <a-button size="small" @click="restoreMaterialRevisionBefore">撤回本次修正</a-button>
                  </div>
                </div>
                <div class="material-revision-change-list">
                  <a-tag v-if="materialRevisionReview.added.length > 0" color="green">新增 {{ materialRevisionReview.added.length }}</a-tag>
                  <a-tag v-if="materialRevisionReview.removed.length > 0" color="red">移除 {{ materialRevisionReview.removed.length }}</a-tag>
                  <a-tag v-if="materialRevisionReview.changed.length > 0" color="orange">变更 {{ materialRevisionReview.changed.length }}</a-tag>
                  <span
                      v-for="item in materialRevisionVisibleChanges"
                      :key="item.key"
                      class="material-revision-change-item"
                  >{{ item.text }}</span>
                  <span v-if="materialRevisionVisibleChanges.length === 0" class="material-revision-no-change">本次修正没有发现字段级差异。</span>
                </div>
              </div>
            </div>
            <div v-if="materialConfirmRiskIssues.length > 0" class="material-risk-list">
              <div
                  v-for="issue in materialConfirmRiskIssues.slice(0, 6)"
                  :key="issue.id"
                  class="material-risk-row"
              >
                <a-tag :color="levelColorMap[issue.level] || 'orange'">{{ levelTextMap[issue.level] || issue.level }}</a-tag>
                <span>{{ issue.fieldLabel ? `${issue.fieldLabel}：` : '' }}{{ issue.description || issue.title }}</span>
              </div>
              <div v-if="materialConfirmRiskIssues.length > 6" class="material-risk-more">
                还有 {{ materialConfirmRiskIssues.length - 6 }} 个待确认项，可在下方字段行查看。
              </div>
            </div>
            <div v-if="materialFilteredFieldEntries.length === 0" class="material-confirm-empty">
              <a-empty :description="materialConfirmEmptyText"/>
              <a-button size="small" @click="resetMaterialConfirmFilter">显示全部字段</a-button>
            </div>
            <div v-else class="material-confirm-table">
              <div class="material-confirm-grid material-confirm-header">
                <div>#</div>
                <div>字段</div>
                <div>控件</div>
                <div>显示</div>
                <div>列表建议</div>
                <div>查询建议</div>
              </div>
              <div
                  v-for="entry in materialFilteredFieldEntries"
                  :key="entry.field.id || entry.index"
                  :class="['material-confirm-row', entry.risks.length > 0 ? 'has-risk' : '']"
              >
                <div class="material-confirm-grid">
                  <div class="material-confirm-index">
                    <span>{{ entry.index + 1 }}</span>
                    <a-tag
                        v-if="entry.risks.length > 0"
                        color="orange"
                    >{{ entry.risks.length }}</a-tag>
                    <a-button
                        v-if="entry.risks.length > 0"
                        size="small"
                        type="link"
                        @click="normalizeMaterialFieldAt(entry.field)"
                    >修正</a-button>
                  </div>
                  <div class="material-field-editor">
                    <a-input
                        v-model:value="entry.field.label"
                        size="small"
                        placeholder="字段标题"
                    />
                    <a-input
                        v-model:value="entry.field.nameHint"
                        size="small"
                        placeholder="英文名"
                    />
                    <div v-if="entry.field.valueExample" class="material-example-text">样例：{{ entry.field.valueExample }}</div>
                  </div>
                  <div class="material-type-editor">
                    <a-select
                        v-model:value="entry.field.typeHint"
                        size="small"
                        :options="materialFieldTypeOptions"
                    />
                    <a-radio-group v-model:value="entry.field.spanHint" size="small">
                      <a-radio-button :value="12">半行</a-radio-button>
                      <a-radio-button :value="24">整行</a-radio-button>
                    </a-radio-group>
                  </div>
                  <div class="material-check-editor">
                    <a-checkbox v-model:checked="entry.field.requiredHint">必填</a-checkbox>
                    <a-checkbox v-model:checked="entry.field.listHint">列表</a-checkbox>
                    <a-checkbox v-model:checked="entry.field.queryHint">查询</a-checkbox>
                  </div>
                  <div class="material-priority-editor">
                    <a-input-number
                        v-model:value="entry.field.listPriority"
                        size="small"
                        :min="0"
                        :max="100"
                        :precision="0"
                    />
                    <div class="material-reason-text">{{ entry.field.listReason || '无列表理由' }}</div>
                  </div>
                  <div class="material-priority-editor">
                    <div class="material-query-row">
                      <a-input-number
                          v-model:value="entry.field.queryPriority"
                          size="small"
                          :min="0"
                          :max="100"
                          :precision="0"
                      />
                      <a-select
                          v-model:value="entry.field.queryMode"
                          size="small"
                          :options="materialQueryModeOptions"
                      />
                    </div>
                    <div class="material-reason-text">{{ entry.field.queryReason || '无查询理由' }}</div>
                  </div>
                </div>
                <div
                    v-if="entry.risks.length > 0"
                    class="material-row-risk-list"
                >
                  <a-tag
                      v-for="risk in entry.risks"
                      :key="risk.id"
                      color="orange"
                  >{{ risk.title }}</a-tag>
                </div>
              </div>
            </div>
          </div>
          <div v-if="materialPreviewIssues.length > 0" class="material-issue-list">
            <div
                v-for="issue in materialPreviewIssues"
                :key="issue.id"
                class="material-issue-row"
            >
              <a-tag :color="levelColorMap[issue.level] || 'default'">{{ levelTextMap[issue.level] || issue.level }}</a-tag>
              <span>{{ issue.title }}：{{ issue.description || issue.suggestion }}</span>
            </div>
          </div>
        </div>

        <div class="draft-storage-actions">
          <a-collapse class="draft-ops-collapse" :bordered="false">
            <a-collapse-panel key="draft-template">
              <template #header>
                <div class="draft-ops-header">
                  <span class="draft-ops-title">草稿与模板</span>
                  <span class="draft-ops-badges">
                    <a-tag v-if="localDraftCount" color="blue">本地 {{ localDraftCount }}</a-tag>
                    <a-tag v-if="remoteDraftCount" color="purple">服务器 {{ remoteDraftCount }}</a-tag>
                    <a-tag v-if="!localDraftCount && !remoteDraftCount" color="default">按需展开</a-tag>
                  </span>
                </div>
              </template>
              <div class="draft-ops-panel">
                <div class="draft-ops-group">
                  <div class="draft-ops-group-title">草稿管理</div>
                  <div class="draft-ops-buttons">
                    <a-button size="small" :disabled="!draft" @click="saveCurrentDraft">{{ saveDraftButtonText }}</a-button>
                    <a-button size="small" :disabled="!draft || remoteSaving" :loading="remoteSaving" @click="saveCurrentDraftToRemote">{{ saveRemoteDraftButtonText }}</a-button>
                    <a-button size="small" @click="openDraftHistory">打开草稿<span v-if="localDraftCount">({{ localDraftCount }})</span></a-button>
                    <a-button size="small" :loading="remoteLoading" @click="openRemoteDraftHistory">服务器草稿<span v-if="remoteDraftCount">({{ remoteDraftCount }})</span></a-button>
                  </div>
                </div>
                <div class="draft-ops-group">
                  <div class="draft-ops-group-title">模板复用</div>
                  <div class="draft-ops-buttons">
                    <a-button size="small" :disabled="!draft" @click="saveCurrentDraftAsTemplate">存为模板</a-button>
                    <a-button size="small" @click="openTemplateLibrary">模板库</a-button>
                  </div>
                </div>
                <div class="storage-tip">本地草稿只在当前浏览器；服务器草稿用于后续复用和正式保存追溯。</div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </div>
        <div v-if="materialSourceType === 'manual' && clarificationQuestions.length > 0" class="clarification-panel">
          <div class="clarification-head">
            <div>
              <div class="clarification-title">需求澄清</div>
              <div class="clarification-subtitle">补齐关键信息后再生成，可以减少字段猜测和后续返工。</div>
            </div>
            <div class="clarification-actions">
              <a-tag v-if="unansweredClarificationCount > 0" color="orange">待补充 {{ unansweredClarificationCount }}</a-tag>
              <a-tag v-else color="green">已补充</a-tag>
              <a-button size="small" @click="clearClarification">清空</a-button>
              <a-button size="small" type="primary" :loading="aiDraftGenerating" :disabled="aiDraftGenerating" @click="generateDraft">{{ aiClarifiedGenerateButtonText }}</a-button>
            </div>
          </div>
          <div class="clarification-list">
            <div
                v-for="question in clarificationQuestions"
                :key="question.id"
                class="clarification-item"
            >
              <div class="clarification-question">
                <span>{{ question.title }}</span>
                <a-tag v-if="question.required" color="red">必填</a-tag>
              </div>
              <div class="clarification-desc">{{ question.description }}</div>
              <a-radio-group
                  v-if="question.type === 'choice'"
                  v-model:value="clarificationAnswers[question.id]"
              >
                <a-radio
                    v-for="option in question.options"
                    :key="option.value"
                    :value="option.value"
                >{{ option.label }}</a-radio>
              </a-radio-group>
              <a-input
                  v-else-if="question.type === 'text'"
                  v-model:value="clarificationAnswers[question.id]"
                  :placeholder="question.placeholder || '请输入'"
              />
              <a-textarea
                  v-else
                  v-model:value="clarificationAnswers[question.id]"
                  :rows="2"
                  :placeholder="question.placeholder || '请输入'"
              />
            </div>
          </div>
        </div>
      </div>

      <a-empty
          v-if="!draft"
          class="draft-empty"
          description="先选择输入来源。表格/文件先识别材料，手动描述可直接生成 DSL 草稿；生成前不会修改设计器或保存数据。"
      />

      <template v-else>
        <div class="draft-section draft-overview-section">
          <div class="section-title-row">
            <div>
              <div class="section-title">AI 草稿概览</div>
              <div class="section-subtitle">先看关键结果；字段明细、列表查询和校验问题可按需展开。</div>
            </div>
          </div>

          <div class="draft-summary">
            <div class="summary-item">
              <span class="summary-count">{{ draft.summary.fieldCount }}</span>
              <span>字段</span>
            </div>
            <div class="summary-item">
              <span class="summary-count">{{ draft.summary.groupCount }}</span>
              <span>分组</span>
            </div>
            <div class="summary-item">
              <span class="summary-count">{{ draft.summary.listCount }}</span>
              <span>列表字段</span>
            </div>
            <div class="summary-item">
              <span class="summary-count">{{ draft.summary.queryCount }}</span>
              <span>查询条件</span>
            </div>
            <div :class="['summary-item', schemaSummary.error > 0 ? 'error' : 'success']">
              <span class="summary-count">{{ schemaSummary.error || 0 }}</span>
              <span>结构错误</span>
            </div>
            <div :class="['summary-item', designSummary.error > 0 ? 'error' : designSummary.total > 0 ? 'warning' : 'success']">
              <span class="summary-count">{{ designSummary.total || 0 }}</span>
              <span>设计问题</span>
            </div>
          </div>

          <a-collapse class="draft-overview-collapse" :bordered="false">
            <a-collapse-panel key="form">
              <template #header>
                <div class="draft-overview-panel-header">
                  <span>表单信息</span>
                  <a-tag>{{ layoutText }}</a-tag>
                </div>
              </template>
              <div v-if="draftClarificationAnswers.length > 0" class="clarification-summary">
                <span class="clarification-summary-label">已采纳澄清</span>
                <a-tag
                    v-for="answer in draftClarificationAnswers"
                    :key="answer.id"
                    color="blue"
                >{{ answer.title }}：{{ answer.label || answer.value }}</a-tag>
              </div>
              <div class="meta-grid">
                <div class="meta-item">
                  <span class="meta-label">标题</span>
                  <span class="meta-value">{{ draft.dsl.form.title }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">表名</span>
                  <span class="meta-value code">{{ draft.dsl.form.name }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">模块</span>
                  <span class="meta-value code">{{ draft.dsl.form.module }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">布局</span>
                  <span class="meta-value">{{ layoutText }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">生成方式</span>
                  <span class="meta-value">{{ aiGeneratorText }}</span>
                </div>
              </div>
            </a-collapse-panel>

            <a-collapse-panel key="fields">
              <template #header>
                <div class="draft-overview-panel-header">
                  <span>字段分组</span>
                  <a-tag>{{ draft.summary.groupCount }} 组</a-tag>
                </div>
              </template>
              <div
                  v-for="group in groupedFields"
                  :key="group.key"
                  class="field-group"
              >
                <div class="field-group-title">
                  <span>{{ group.title }}</span>
                  <a-tag>{{ group.fields.length }}</a-tag>
                </div>
                <div class="field-list">
                  <div
                      v-for="field in group.fields"
                      :key="field.name"
                      class="field-row"
                  >
                    <div class="field-main">
                      <span class="field-label">{{ field.label }}</span>
                      <span class="field-name">{{ field.name }}</span>
                    </div>
                    <div class="field-tags">
                      <a-tag color="blue">{{ field.type }}</a-tag>
                      <a-tag>{{ field.span === 24 ? '整行' : '半行' }}</a-tag>
                      <a-tag v-if="field.required" color="red">必填</a-tag>
                      <a-tag v-if="field.isList" color="green">列表</a-tag>
                      <a-tag v-if="field.isQuery" color="cyan">查询</a-tag>
                    </div>
                  </div>
                </div>
              </div>
            </a-collapse-panel>

            <a-collapse-panel key="list-query">
              <template #header>
                <div class="draft-overview-panel-header">
                  <span>列表与查询</span>
                  <span class="draft-overview-tags">
                    <a-tag color="green">列表 {{ listFields.length }}</a-tag>
                    <a-tag color="cyan">查询 {{ queryFields.length }}</a-tag>
                  </span>
                </div>
              </template>
              <div class="preview-columns">
                <div>
                  <div class="preview-title">列表字段</div>
                  <div class="pill-list">
                    <a-tag v-for="field in listFields" :key="field.name" color="green">{{ field.label }}</a-tag>
                    <span v-if="listFields.length === 0" class="empty-text">暂无</span>
                  </div>
                </div>
                <div>
                  <div class="preview-title">查询条件</div>
                  <div class="pill-list">
                    <a-tag v-for="field in queryFields" :key="field.name" color="cyan">{{ field.label }}</a-tag>
                    <span v-if="queryFields.length === 0" class="empty-text">暂无</span>
                  </div>
                </div>
              </div>
            </a-collapse-panel>

            <a-collapse-panel v-if="hasDictionarySuggestions" key="dictionaries">
              <template #header>
                <div class="draft-overview-panel-header">
                  <span>字典建议</span>
                  <span class="draft-overview-tags">
                    <a-tag color="blue">{{ dictionaryStats.total }} 个字段</a-tag>
                    <a-tag v-if="dictionaryStats.create > 0" color="green">新建 {{ dictionaryStats.create }}</a-tag>
                    <a-tag v-if="dictionaryStats.noItems > 0" color="orange">待补项 {{ dictionaryStats.noItems }}</a-tag>
                  </span>
                </div>
              </template>

              <div class="dictionary-confirm-panel">
                <div class="dictionary-confirm-head">
                  <div>
                    <div class="preview-title">系统字典确认</div>
                    <div class="dictionary-confirm-subtitle">下拉、单选、多选字段会先生成候选字典；确认后再写入 DSL 和设计器配置。</div>
                  </div>
                  <div class="dictionary-confirm-actions">
                    <a-button size="small" @click="applyDictionaryConfirmations">应用字典确认到草稿</a-button>
                    <a-button
                        size="small"
                        type="primary"
                        :loading="dictionarySaving"
                        :disabled="!dictionaryCanBatchCreate"
                        @click="batchCreateDictionaries"
                    >批量创建字典</a-button>
                  </div>
                </div>

                <div class="dictionary-confirm-list">
                  <div
                      v-for="row in dictionaryConfirmationRows"
                      :key="row.fieldName"
                      class="dictionary-row"
                  >
                    <div class="dictionary-row-head">
                      <div class="field-main">
                        <span class="field-label">{{ row.fieldLabel || row.fieldName }}</span>
                        <span class="field-name">{{ row.fieldName }}</span>
                      </div>
                      <div class="field-tags">
                        <a-tag color="blue">{{ row.fieldType }}</a-tag>
                        <a-tag :color="getDictionaryModeColor(row.mode)">{{ dictionaryModeTextMap[row.mode] || row.mode }}</a-tag>
                        <a-tag v-if="row.created" color="green">已创建</a-tag>
                        <a-tag v-else-if="row.mode === 'create' && parseDictionaryItemsText(row.itemsText).length === 0" color="orange">无字典项</a-tag>
                      </div>
                    </div>

                    <div :class="['dictionary-edit-grid', row.mode === 'use-existing' ? 'existing' : '']">
                      <div class="dictionary-edit-item mode">
                        <span class="dictionary-edit-label">处理方式</span>
                        <a-select
                            v-model:value="row.mode"
                            size="small"
                            @change="handleDictionaryModeChange(row)"
                        >
                          <a-select-option
                              v-for="option in dictionaryModeOptions"
                              :key="option.value"
                              :value="option.value"
                          >{{ option.label }}</a-select-option>
                        </a-select>
                      </div>
                      <div v-if="row.mode === 'use-existing'" class="dictionary-edit-item existing">
                        <span class="dictionary-edit-label">系统字典</span>
                        <u-select
                            v-model:value="row.existingCode"
                            form-type="select"
                            type="table"
                            dict-type="sys_dictionary"
                            valueField="code"
                            textField="name"
                            :format="formatSystemDictionaryOption"
                            tableOrderBy="a.sort asc"
                            :tableFilterData="systemDictionaryFilterData"
                            placeholder="请选择已有系统字典"
                            @change="markDictionaryDirty(row)"
                        />
                      </div>
                      <div v-else class="dictionary-edit-item code">
                        <span class="dictionary-edit-label">字典编码</span>
                        <a-input
                            v-model:value="row.code"
                            size="small"
                            :disabled="row.mode === 'ignore'"
                            placeholder="表名_字段名"
                            @change="markDictionaryDirty(row)"
                        />
                      </div>
                      <div v-if="row.mode !== 'use-existing'" class="dictionary-edit-item name">
                        <span class="dictionary-edit-label">字典名称</span>
                        <a-input
                            v-model:value="row.name"
                            size="small"
                            :disabled="row.mode === 'ignore'"
                            placeholder="表单标题-字段标题"
                            @change="markDictionaryDirty(row)"
                        />
                      </div>
                    </div>

                    <div v-if="row.mode === 'create'" class="dictionary-items-editor">
                      <div class="dictionary-edit-label">字典项（每行：value text）</div>
                      <a-textarea
                          v-model:value="row.itemsText"
                          :auto-size="{ minRows: 2, maxRows: 6 }"
                          placeholder="例如：normal 普通"
                          @change="markDictionaryDirty(row)"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </a-collapse-panel>

            <a-collapse-panel key="check">
              <template #header>
                <div class="draft-overview-panel-header">
                  <span>草稿校验</span>
                  <span class="draft-overview-tags">
                    <a-tag :color="schemaSummary.error > 0 ? 'red' : 'green'">结构 {{ schemaSummary.total || 0 }}</a-tag>
                    <a-tag :color="designSummary.error > 0 ? 'red' : designSummary.total > 0 ? 'orange' : 'green'">设计 {{ designSummary.total || 0 }}</a-tag>
                  </span>
                </div>
              </template>
              <div class="check-summary-grid">
                <div :class="['check-summary-card', schemaSummary.error > 0 ? 'error' : 'success']">
                  <div class="check-summary-title">结构校验</div>
                  <div class="check-summary-count">{{ schemaSummary.total || 0 }}</div>
                  <div class="check-summary-desc">
                    错误 {{ schemaSummary.error || 0 }}，警告 {{ schemaSummary.warning || 0 }}，建议 {{ schemaSummary.suggestion || 0 }}
                  </div>
                </div>
                <div :class="['check-summary-card', designSummary.error > 0 ? 'error' : designSummary.total > 0 ? 'warning' : 'success']">
                  <div class="check-summary-title">设计体检</div>
                  <div class="check-summary-count">{{ designSummary.total || 0 }}</div>
                  <div class="check-summary-desc">
                    错误 {{ designSummary.error || 0 }}，警告 {{ designSummary.warning || 0 }}，建议 {{ designSummary.suggestion || 0 }}
                  </div>
                </div>
              </div>

              <div class="check-block">
                <div class="check-block-title">结构问题</div>
                <div v-if="draft.schemaIssues.length === 0" class="check-ok">Schema 校验通过，未发现结构错误。</div>
                <div v-else class="issue-list">
                  <div v-for="issue in draft.schemaIssues" :key="issue.id" class="check-issue">
                    <a-tag :color="levelColorMap[issue.level] || 'default'">{{ levelTextMap[issue.level] || issue.level }}</a-tag>
                    <div class="check-issue-body">
                      <div class="check-issue-title">{{ issue.title }}</div>
                      <div class="check-issue-desc">{{ issue.description || issue.suggestion }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="check-block">
                <div class="check-block-title">设计建议</div>
                <div v-if="designIssues.length === 0" class="check-ok">设计体检通过，未发现设计规则问题。</div>
                <div v-else class="design-issue-groups">
                  <div
                      v-for="group in groupedDesignIssues"
                      :key="group.key"
                      class="design-issue-group"
                  >
                    <div class="design-issue-group-title">
                      <span>{{ group.title }}</span>
                      <a-tag :color="group.color">{{ group.issues.length }}</a-tag>
                    </div>
                    <div class="issue-list">
                      <div v-for="issue in group.issues" :key="issue.id" class="check-issue">
                        <a-tag :color="group.color">{{ group.title }}</a-tag>
                        <div class="check-issue-body">
                          <div class="check-issue-title">
                            <span>{{ issue.title }}</span>
                            <span v-if="issue.fieldLabel || issue.fieldName" class="check-field">
                              {{ issue.fieldLabel || issue.fieldName }}
                              <span v-if="issue.fieldName">({{ issue.fieldName }})</span>
                            </span>
                          </div>
                          <div class="check-issue-desc">{{ issue.description }}</div>
                          <div class="check-issue-suggestion">{{ issue.suggestion }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </div>

        <div class="draft-section compact">
          <div class="section-title-row">
            <div>
              <div class="section-title">转换预览</div>
              <div class="section-subtitle">先预览，再应用；应用后只修改当前设计器画布，仍需点击页面原保存才会正式落库。</div>
            </div>
            <div class="section-actions">
              <a-tag v-if="conversionApplied" color="green">已应用</a-tag>
              <a-tag v-else-if="conversionPreview" :color="canApplyConversion ? 'green' : 'orange'">
                {{ canApplyConversion ? '可应用' : '需处理问题' }}
              </a-tag>
              <a-tag v-else color="default">未预览</a-tag>
              <a-button @click="previewDesignerState">{{ conversionPreview ? '重新预览' : '转换预览' }}</a-button>
              <a-button
                  v-if="conversionPreview"
                  type="primary"
                  :disabled="!canApplyConversion || conversionApplied"
                  @click="applyDesignerState"
              >{{ conversionApplied ? '已应用到设计器' : (isIncrementalApplyMode ? '增量补充到设计器' : '全量应用到设计器') }}</a-button>
            </div>
          </div>

          <div class="apply-mode-row">
            <span class="apply-mode-label">应用方式</span>
            <a-radio-group v-model:value="applyMode" button-style="solid" size="small">
              <a-radio-button value="incremental">仅增量补充字段</a-radio-button>
              <a-radio-button value="replace">全量替换</a-radio-button>
            </a-radio-group>
            <a-tag v-if="designerBusinessFieldCount > 0" color="blue">当前画布约 {{ designerBusinessFieldCount }} 个业务字段</a-tag>
            <a-tag v-else color="default">空表 / 无业务字段</a-tag>
          </div>
          <div class="apply-mode-hint">{{ applyModeHintText }}</div>

          <a-alert
              v-if="conversionApplied"
              class="conversion-applied-alert"
              type="success"
              show-icon
              message="已应用到设计器"
              :description="conversionAppliedNoticeText"
          />

          <a-empty
              v-if="!conversionPreview"
              class="conversion-empty"
              description="先点击转换预览，预览通过后再应用到设计器。"
          />

          <template v-else>
            <div class="conversion-summary">
              <div v-if="isIncrementalApplyMode && conversionPreview.mode === 'incremental'" class="summary-item success">
                <span class="summary-count">{{ conversionPreview.summary.addCount ?? 0 }}</span>
                <span>将新增</span>
              </div>
              <div v-if="isIncrementalApplyMode && conversionPreview.mode === 'incremental'" class="summary-item">
                <span class="summary-count">{{ conversionPreview.summary.skipCount ?? 0 }}</span>
                <span>将跳过</span>
              </div>
              <div class="summary-item">
                <span class="summary-count">{{ conversionPreview.summary.displayCount }}</span>
                <span>{{ isIncrementalApplyMode ? '应用后显示' : '显示字段' }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-count">{{ conversionPreview.summary.hiddenCount }}</span>
                <span>{{ isIncrementalApplyMode ? '隐藏(不变)' : '隐藏字段' }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-count">{{ conversionListCount }}</span>
                <span>列表字段</span>
              </div>
              <div class="summary-item">
                <span class="summary-count">{{ conversionQueryCount }}</span>
                <span>查询条件</span>
              </div>
              <div :class="['summary-item', conversionPreview.errors.length > 0 ? 'error' : 'success']">
                <span class="summary-count">{{ conversionPreview.errors.length }}</span>
                <span>错误</span>
              </div>
              <div :class="['summary-item', conversionPreview.warnings.length > 0 ? 'warning' : 'success']">
                <span class="summary-count">{{ conversionPreview.warnings.length }}</span>
                <span>警告</span>
              </div>
            </div>

            <a-collapse class="conversion-detail-collapse" :bordered="false">
              <a-collapse-panel key="detail">
                <template #header>
                  <div class="conversion-detail-header">
                    <span>查看转换详情</span>
                    <span class="draft-overview-tags">
                      <a-tag>{{ conversionDisplayFields.length }} 个显示字段</a-tag>
                      <a-tag v-if="conversionHiddenFields.length > 0">{{ conversionHiddenFields.length }} 个隐藏字段</a-tag>
                      <a-tag :color="conversionPreview.issues.length > 0 ? 'orange' : 'green'">{{ conversionPreview.issues.length }} 个问题</a-tag>
                    </span>
                  </div>
                </template>

                <div class="conversion-meta">
                  <div class="meta-item">
                    <span class="meta-label wide">应用模式</span>
                    <span class="meta-value">{{ conversionPreview.mode === 'incremental' ? '增量补充' : '全量替换' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label wide">表单标题</span>
                    <span class="meta-value">{{ conversionPreview.formPatch?.comments || conversionPreview.summary?.title || '-' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label wide">表名</span>
                    <span class="meta-value code">{{ conversionPreview.formPatch?.name || conversionPreview.summary?.formName || '-' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label wide">Label宽度</span>
                    <span class="meta-value">{{ conversionPreview.formPropsPatch?.labelWidth || (conversionPreview.mode === 'incremental' ? '不修改' : '-') }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label wide">可应用</span>
                    <span :class="['meta-value', conversionPreview.canApply ? 'success-text' : 'error-text']">
                      {{ conversionPreview.canApply ? '是' : (conversionPreview.mode === 'incremental' && (conversionPreview.summary?.addCount || 0) === 0 ? '否，无新增字段' : '否，需先处理错误') }}
                    </span>
                  </div>
                </div>

                <div v-if="conversionPreview.mode === 'incremental'" class="conversion-block">
                  <div class="preview-title">将新增（{{ incrementalAddFields.length }}）</div>
                  <div v-if="incrementalAddFields.length === 0" class="empty-text">无新增字段</div>
                  <div v-else class="conversion-field-list">
                    <div
                        v-for="field in incrementalAddFields"
                        :key="'add-' + field.name"
                        class="conversion-field-row"
                    >
                      <div class="field-main">
                        <span class="field-label">{{ field.label }}</span>
                        <span class="field-name">{{ field.name }}</span>
                      </div>
                      <div class="field-tags">
                        <a-tag color="green">新增</a-tag>
                        <a-tag color="blue">{{ field.key }}</a-tag>
                        <a-tag>{{ field.showType }}</a-tag>
                        <a-tag v-if="field.required" color="red">必填</a-tag>
                        <a-tag v-if="field.isList" color="green">列表</a-tag>
                        <a-tag v-if="field.isQuery" color="cyan">查询</a-tag>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="conversionPreview.mode === 'incremental' && incrementalSkippedFields.length > 0" class="conversion-block">
                  <div class="preview-title">将跳过 · 保留原配置（{{ incrementalSkippedFields.length }}）</div>
                  <div class="conversion-field-list">
                    <div
                        v-for="field in incrementalSkippedFields"
                        :key="'skip-' + field.name"
                        class="conversion-field-row"
                    >
                      <div class="field-main">
                        <span class="field-label">{{ field.label || field.name }}</span>
                        <span class="field-name">{{ field.name }}</span>
                      </div>
                      <div class="field-tags">
                        <a-tag>跳过</a-tag>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="conversionPreview.mode === 'incremental' && incrementalIgnoredHidden.length > 0" class="conversion-block">
                  <div class="preview-title">忽略 · 隐藏/系统字段（{{ incrementalIgnoredHidden.length }}）</div>
                  <div class="empty-text">增量模式不处理 create_by / update_date 等系统隐藏字段。</div>
                </div>

                <div class="conversion-block">
                  <div class="preview-title">{{ conversionPreview.mode === 'incremental' ? '应用后显示区域（含保留）' : '显示区域字段' }}</div>
                  <div v-if="conversionDisplayFields.length === 0" class="empty-text">暂无</div>
                  <div v-else class="conversion-field-list">
                    <div
                        v-for="field in conversionDisplayFields"
                        :key="field.name"
                        class="conversion-field-row"
                    >
                      <div class="field-main">
                        <span class="field-label">{{ field.label }}</span>
                        <span class="field-name">{{ field.name }}</span>
                      </div>
                      <div class="field-tags">
                        <a-tag color="blue">{{ field.key }}</a-tag>
                        <a-tag>{{ field.showType }}</a-tag>
                        <a-tag>{{ field.span === 24 ? '整行' : '半行' }}</a-tag>
                        <a-tag v-if="field.required" color="red">必填</a-tag>
                        <a-tag v-if="field.isList" color="green">列表</a-tag>
                        <a-tag v-if="field.isQuery" color="cyan">查询</a-tag>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="conversionHiddenFields.length > 0" class="conversion-block">
                  <div class="preview-title">隐藏区域字段</div>
                  <div class="conversion-field-list">
                    <div
                        v-for="field in conversionHiddenFields"
                        :key="field.name"
                        class="conversion-field-row"
                    >
                      <div class="field-main">
                        <span class="field-label">{{ field.label }}</span>
                        <span class="field-name">{{ field.name }}</span>
                      </div>
                      <div class="field-tags">
                        <a-tag>{{ field.showType }}</a-tag>
                        <a-tag v-if="field.isList" color="green">列表</a-tag>
                        <a-tag v-if="field.isQuery" color="cyan">查询</a-tag>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="conversion-block">
                  <div class="preview-title">转换问题</div>
                  <div v-if="conversionPreview.issues.length === 0" class="check-ok">转换检查通过，未发现错误或警告。</div>
                  <div v-else class="issue-list">
                    <div
                        v-for="issue in conversionPreview.issues"
                        :key="issue.id"
                        class="check-issue"
                    >
                      <a-tag :color="levelColorMap[issue.level] || 'default'">{{ levelTextMap[issue.level] || issue.level }}</a-tag>
                      <div class="check-issue-body">
                        <div class="check-issue-title">
                          <span>{{ issue.title }}</span>
                          <span v-if="issue.fieldLabel || issue.fieldName" class="check-field">
                            {{ issue.fieldLabel || issue.fieldName }}
                            <span v-if="issue.fieldName">({{ issue.fieldName }})</span>
                          </span>
                        </div>
                        <div class="check-issue-desc">{{ issue.description || issue.suggestion }}</div>
                        <div v-if="issue.suggestion" class="check-issue-suggestion">{{ issue.suggestion }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </a-collapse-panel>
            </a-collapse>
          </template>
        </div>

        <div class="draft-section compact optional-review-section">
          <a-collapse class="optional-review-collapse" :bordered="false">
            <a-collapse-panel key="review">
              <template #header>
                <div class="optional-review-header">
                  <div>
                    <div class="section-title">审查配置完整性（可选）</div>
                    <div class="section-subtitle">用于正式保存前做一次配置体检；这里只审查和提示，正式保存仍走页面原保存按钮。</div>
                  </div>
                  <div class="section-actions" @click.stop>
                    <a-tag v-if="formalizePreview" :color="formalizeCheckResult.canFormalize ? 'green' : 'orange'">
                      {{ formalizeCheckResult.canFormalize ? '已通过' : '有问题' }}
                    </a-tag>
                    <a-tag v-else color="default">未执行</a-tag>
                    <a-button size="small" @click="previewFormalizeState">审查配置完整性</a-button>
                  </div>
                </div>
              </template>

          <a-empty
              v-if="!formalizePreview"
              class="conversion-empty"
              description="点击审查配置完整性后，查看表单元数据、字段、列表、查询和配置完整性检查结果。"
          />

          <template v-else>
            <div class="conversion-summary">
              <div class="summary-item">
                <span class="summary-count">{{ formalizePreview.summary.displayCount }}</span>
                <span>显示字段</span>
              </div>
              <div class="summary-item">
                <span class="summary-count">{{ formalizePreview.summary.hiddenCount }}</span>
                <span>隐藏字段</span>
              </div>
              <div class="summary-item">
                <span class="summary-count">{{ formalizePreview.summary.listCount }}</span>
                <span>列表字段</span>
              </div>
              <div class="summary-item">
                <span class="summary-count">{{ formalizePreview.summary.queryCount }}</span>
                <span>查询条件</span>
              </div>
              <div :class="['summary-item', formalizeCheckResult.errors.length > 0 ? 'error' : 'success']">
                <span class="summary-count">{{ formalizeCheckResult.errors.length }}</span>
                <span>错误</span>
              </div>
              <div :class="['summary-item', formalizeCheckResult.warnings.length > 0 ? 'warning' : 'success']">
                <span class="summary-count">{{ formalizeCheckResult.warnings.length }}</span>
                <span>警告</span>
              </div>
            </div>

            <div class="formalize-notice">
              <a-alert
                  type="info"
                  show-icon
                  message="配置完整性审查只检查表单设计元数据，不会自动建表、改表或同步表结构。"
              />
            </div>

            <div class="conversion-meta">
              <div class="meta-item">
                <span class="meta-label wide">目标模式</span>
                <span class="meta-value">{{ formalizePreview.target.mode === 'optimize' ? '优化已有表单' : '新建表单' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label wide">表单标题</span>
                <span class="meta-value">{{ formalizePreview.target.formTitle || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label wide">表名</span>
                <span class="meta-value code">{{ formalizePreview.target.formName || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label wide">模块</span>
                <span class="meta-value code">{{ formalizePreview.target.module || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label wide">主键</span>
                <span class="meta-value code">{{ formalizePreview.target.pkColumnName || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label wide">配置可保存</span>
                <span :class="['meta-value', formalizeCheckResult.canFormalize ? 'success-text' : 'error-text']">
                  {{ formalizeCheckResult.canFormalize ? '是' : '否，需先处理错误' }}
                </span>
              </div>
            </div>

            <div class="formalize-source">
              <div class="preview-title">草稿来源</div>
              <div class="meta-grid">
                <div class="meta-item">
                  <span class="meta-label wide">来源</span>
                  <span class="meta-value">{{ formalizeSourceText }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label wide">版本</span>
                  <span class="meta-value">{{ formalizePreview.source.versionName || formalizePreview.source.versionNo || formalizePreview.source.versionId || '-' }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label wide">草稿ID</span>
                  <span class="meta-value code">{{ formalizePreview.source.draftId || '-' }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label wide">已修改</span>
                  <span :class="['meta-value', formalizePreview.checksum.changed ? 'warning-text' : 'success-text']">
                    {{ formalizePreview.checksum.changed ? '是' : '否' }}
                  </span>
                </div>
              </div>
            </div>

            <div class="conversion-block">
              <div class="preview-title">提交字段</div>
              <div v-if="formalizePreview.fieldPreview.columns.length === 0" class="empty-text">暂无</div>
              <div v-else class="conversion-field-list">
                <div
                    v-for="field in formalizePreviewColumns"
                    :key="field.name + field.javaField"
                    class="conversion-field-row"
                >
                  <div class="field-main">
                    <span class="field-label">{{ field.comments || field.name }}</span>
                    <span class="field-name">{{ field.name }}</span>
                  </div>
                  <div class="field-tags">
                    <a-tag>{{ field.showType || '-' }}</a-tag>
                    <a-tag>{{ field.jdbcType || '-' }}</a-tag>
                    <a-tag v-if="field.isForm === '1'" color="blue">表单</a-tag>
                    <a-tag v-if="field.isList === '1'" color="green">列表</a-tag>
                    <a-tag v-if="field.isQuery === '1'" color="cyan">查询</a-tag>
                  </div>
                </div>
              </div>
              <div v-if="formalizePreview.fieldPreview.columns.length > formalizePreviewColumns.length" class="formalize-more">
                其余 {{ formalizePreview.fieldPreview.columns.length - formalizePreviewColumns.length }} 个字段可在控制台查看 FormDesignFormalizePreview。
              </div>
            </div>

            <div v-if="formalizePreview.fieldPreview.extJava.length > 0" class="conversion-block">
              <div class="preview-title">多对多字段</div>
              <div class="pill-list">
                <a-tag
                    v-for="field in formalizePreview.fieldPreview.extJava"
                    :key="field.name"
                    color="purple"
                >{{ field.comments || field.name }}</a-tag>
              </div>
            </div>

            <div class="conversion-block">
              <div class="preview-title">配置完整性检查</div>
              <div v-if="formalizeCheckResult.issues.length === 0" class="check-ok">配置完整性检查通过，未发现错误或警告。</div>
              <div v-else class="issue-list">
                <div
                    v-for="issue in formalizeCheckResult.topIssues"
                    :key="issue.id"
                    class="check-issue"
                >
                  <a-tag :color="levelColorMap[issue.level] || 'default'">{{ levelTextMap[issue.level] || issue.level }}</a-tag>
                  <div class="check-issue-body">
                    <div class="check-issue-title">
                      <span>{{ issue.title }}</span>
                      <span v-if="issue.fieldLabel || issue.fieldName" class="check-field">
                        {{ issue.fieldLabel || issue.fieldName }}
                        <span v-if="issue.fieldName">({{ issue.fieldName }})</span>
                      </span>
                    </div>
                    <div class="check-issue-desc">{{ issue.description || issue.suggestion }}</div>
                    <div v-if="issue.suggestion" class="check-issue-suggestion">{{ issue.suggestion }}</div>
                  </div>
                </div>
              </div>
              <div v-if="formalizeCheckResult.issues.length > formalizeCheckResult.topIssues.length" class="formalize-more">
                其余 {{ formalizeCheckResult.issues.length - formalizeCheckResult.topIssues.length }} 个问题可在控制台查看 FormDesignFormalizeCheckResult。
              </div>
            </div>
          </template>
            </a-collapse-panel>
          </a-collapse>
        </div>

        <a-collapse class="advanced-info-collapse draft-section compact" :bordered="false">
          <a-collapse-panel key="advanced">
            <template #header>
              <div class="advanced-info-header">
                <div>
                  <div class="section-title">高级信息</div>
                  <div class="section-subtitle">排查问题或导出 DSL 时使用，日常创建表单可以忽略。</div>
                </div>
                <div class="section-actions" @click.stop>
                  <a-tag v-if="aiDiagnosticVisible" :color="aiDiagnosticTagColor">{{ aiDiagnosticStatusText }}</a-tag>
                  <a-tag color="default">DSL JSON</a-tag>
                </div>
              </div>
            </template>

            <div v-if="aiDiagnosticVisible" class="advanced-info-block ai-diagnostic-panel">
              <div class="ai-diagnostic-head">
                <div>
                  <div class="ai-diagnostic-title">AI 调用诊断</div>
                  <div class="ai-diagnostic-subtitle">问题排查用：可查看 requestId、模型、耗时、OCR 和原始输出摘要。</div>
                </div>
                <div class="ai-diagnostic-head-actions">
                  <a-tag :color="aiDiagnosticTagColor">{{ aiDiagnosticStatusText }}</a-tag>
                  <a-button size="small" @click="copyAiDiagnostics">复制诊断</a-button>
                  <a-button size="small" @click="printAiDiagnostics">输出诊断</a-button>
                </div>
              </div>
              <a-collapse class="ai-diagnostic-collapse" :bordered="false">
                <a-collapse-panel key="diagnostic" header="查看 requestId / OCR / 原始输出摘要">
                  <div class="ai-diagnostic-grid">
                    <div
                        v-for="row in aiDiagnosticRows"
                        :key="row.label"
                        class="ai-diagnostic-item"
                    >
                      <span class="ai-diagnostic-label">{{ row.label }}</span>
                      <span :class="['ai-diagnostic-value', row.mono ? 'code' : '']">{{ row.value }}</span>
                    </div>
                  </div>
                  <div v-if="aiDiagnosticExtractionRows.length > 0" class="ai-diagnostic-block">
                    <div class="ai-diagnostic-block-title">文件 / OCR 抽取</div>
                    <div class="ai-diagnostic-grid">
                      <div
                          v-for="row in aiDiagnosticExtractionRows"
                          :key="row.label"
                          class="ai-diagnostic-item"
                      >
                        <span class="ai-diagnostic-label">{{ row.label }}</span>
                        <span :class="['ai-diagnostic-value', row.mono ? 'code' : '']">{{ row.value }}</span>
                      </div>
                    </div>
                  </div>
                  <div v-if="aiDiagnosticInfo.errorMessage" class="ai-diagnostic-block">
                    <div class="ai-diagnostic-block-title">最近失败</div>
                    <div class="ai-diagnostic-error">{{ aiDiagnosticInfo.errorMessage }}</div>
                  </div>
                  <div v-if="aiDiagnosticInfo.rawOutputPreview" class="ai-diagnostic-block">
                    <div class="ai-diagnostic-block-title">原始输出摘要</div>
                    <pre class="ai-diagnostic-preview">{{ aiDiagnosticInfo.rawOutputPreview }}</pre>
                  </div>
                </a-collapse-panel>
              </a-collapse>
            </div>

            <div class="advanced-info-block">
              <div class="json-panel-header">
                <span class="preview-title">DSL JSON</span>
                <div class="json-panel-actions">
                  <a-button size="small" :disabled="!draft" @click="copyDraftDsl">复制DSL</a-button>
                  <a-button size="small" :disabled="!draft" @click="exportDraftDsl">导出DSL</a-button>
                  <a-button size="small" :disabled="!draft" @click="printDraft">输出到控制台</a-button>
                </div>
              </div>
              <pre class="json-preview">{{ draftJson }}</pre>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </template>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <div class="footer-status">{{ footerText }}</div>
        <div class="footer-actions">
          <a-button @click="close">关闭</a-button>
          <a-button
              type="primary"
              :loading="footerPrimaryAction.loading"
              :disabled="footerPrimaryAction.disabled"
              @click="runFooterPrimaryAction"
          >{{ footerPrimaryAction.label }}</a-button>
        </div>
      </div>
    </template>
  </a-drawer>

  <a-modal
      v-model:visible="draftHistoryVisible"
      title="AI草稿历史"
      :width="760"
      :footer="null"
      :z-index="2100"
      @cancel="draftHistoryVisible = false"
  >
    <a-empty
        v-if="localDrafts.length === 0"
        description="当前浏览器还没有保存过 AI 草稿。"
    />
    <div v-else class="local-draft-list">
      <div
          v-for="item in localDrafts"
          :key="item.id"
          :class="['local-draft-item', item.id === currentLocalDraftId ? 'active' : '']"
      >
        <div class="local-draft-main">
          <div class="local-draft-title">
            <span>{{ item.title || '未命名草稿' }}</span>
            <a-tag v-if="item.id === currentLocalDraftId" color="blue">当前</a-tag>
          </div>
          <div class="local-draft-meta">
            <span>{{ getDraftTargetText(item) }}</span>
            <span>{{ getDraftVersionLabel(item) }}</span>
            <span>{{ getDraftFieldCount(item) }} 个字段</span>
            <span>{{ getDraftUpdatedText(item) }}</span>
          </div>
          <div class="local-draft-source">{{ getDraftSourcePreview(item) }}</div>
        </div>
        <div class="local-draft-actions">
          <a-button size="small" type="primary" @click="loadLocalDraft(item)">打开</a-button>
          <a-button size="small" @click="showDraftVersions(item)">版本</a-button>
          <a-button size="small" :loading="localDraftImportingId === item.id" @click="importLocalDraft(item)">导入服务器</a-button>
          <a-button size="small" danger @click="removeLocalDraft(item)">删除</a-button>
        </div>
        <div v-if="selectedHistoryDraftId === item.id" class="local-version-panel">
          <div class="version-panel-head">
            <div class="version-panel-title">版本管理</div>
            <div class="version-compare-controls">
              <span>对比</span>
              <a-select v-model:value="compareLeftVersionId" size="small" style="width: 92px">
                <a-select-option
                    v-for="version in getDraftVersions(item)"
                    :key="version.id"
                    :value="version.id"
                >{{ version.name }}</a-select-option>
              </a-select>
              <span>和</span>
              <a-select v-model:value="compareRightVersionId" size="small" style="width: 92px">
                <a-select-option
                    v-for="version in getDraftVersions(item)"
                    :key="version.id"
                    :value="version.id"
                >{{ version.name }}</a-select-option>
              </a-select>
            </div>
          </div>
          <div class="local-version-list">
            <div
                v-for="version in getDraftVersions(item)"
                :key="version.id"
                :class="['local-version-row', version.id === currentLocalVersionId ? 'active' : '']"
            >
              <div class="version-main">
                <span class="version-name">{{ version.name }}</span>
                <span>{{ getVersionFieldCount(version) }} 个字段</span>
                <span>{{ getVersionListCount(version) }} 个列表字段</span>
                <span>{{ getVersionQueryCount(version) }} 个查询条件</span>
                <span>{{ getVersionCreatedText(version) }}</span>
              </div>
              <a-button size="small" @click="loadLocalDraft(item, version.id)">打开此版</a-button>
            </div>
          </div>
          <div class="version-diff">
            <div class="version-diff-summary">
              <a-tag color="green">新增 {{ versionDiff.added.length }}</a-tag>
              <a-tag color="red">移除 {{ versionDiff.removed.length }}</a-tag>
              <a-tag color="orange">变更 {{ versionDiff.changed.length }}</a-tag>
            </div>
            <div v-if="versionDiff.added.length === 0 && versionDiff.removed.length === 0 && versionDiff.changed.length === 0" class="empty-text">
              两个版本的字段配置没有发现差异。
            </div>
            <div v-else class="version-diff-groups">
              <div v-if="versionDiff.added.length">
                <div class="version-diff-title">新增字段</div>
                <a-tag v-for="field in versionDiff.added" :key="field.name" color="green">{{ field.label || field.name }}</a-tag>
              </div>
              <div v-if="versionDiff.removed.length">
                <div class="version-diff-title">移除字段</div>
                <a-tag v-for="field in versionDiff.removed" :key="field.name" color="red">{{ field.label || field.name }}</a-tag>
              </div>
              <div v-if="versionDiff.changed.length">
                <div class="version-diff-title">变更字段</div>
                <div v-for="field in versionDiff.changed" :key="field.name" class="version-changed-field">
                  <span>{{ field.label || field.name }}</span>
                  <span>{{ field.changes.join('、') }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </a-modal>

  <a-modal
      v-model:visible="remoteDraftHistoryVisible"
      title="服务器AI草稿"
      :width="760"
      :footer="null"
      :z-index="2100"
      @cancel="remoteDraftHistoryVisible = false"
  >
    <div class="remote-draft-toolbar">
      <a-input-search
          v-model:value="remoteDraftKeyword"
          allow-clear
          placeholder="按标题搜索"
          style="width: 280px"
          @search="refreshRemoteDrafts"
      />
      <a-button :loading="remoteLoading" @click="refreshRemoteDrafts">刷新</a-button>
    </div>
    <a-empty
        v-if="!remoteLoading && remoteDrafts.length === 0"
        description="服务器还没有可打开的 AI 草稿。"
    />
    <div v-else class="local-draft-list">
      <div
          v-for="item in remoteDrafts"
          :key="item.id"
          :class="['local-draft-item', item.id === currentRemoteDraftId ? 'active' : '']"
      >
        <div class="local-draft-main">
          <div class="local-draft-title">
            <span>{{ item.title || '未命名草稿' }}</span>
            <a-tag color="purple">服务器</a-tag>
            <a-tag v-if="item.id === currentRemoteDraftId" color="blue">当前</a-tag>
          </div>
          <div class="local-draft-meta">
            <span>{{ getDraftTargetText(item) }}</span>
            <span>{{ getRemoteDraftVersionLabel(item) }}</span>
            <span>{{ getDraftUpdatedText(item) }}</span>
          </div>
          <div class="local-draft-source">{{ getDraftSourcePreview(item) }}</div>
        </div>
        <div class="local-draft-actions">
          <a-button size="small" type="primary" :loading="remoteLoadingDraftId === item.id" @click="loadRemoteDraft(item)">打开</a-button>
          <a-button size="small" danger :loading="remoteDeletingDraftId === item.id" @click="removeRemoteDraft(item)">删除</a-button>
        </div>
      </div>
    </div>
  </a-modal>

  <a-modal
      v-model:visible="templateLibraryVisible"
      title="AI模板库"
      :width="820"
      :footer="null"
      :z-index="2100"
      @cancel="templateLibraryVisible = false"
  >
    <div class="template-toolbar">
      <a-radio-group v-model:value="templateSourceFilter" button-style="solid" size="small">
        <a-radio-button value="all">全部</a-radio-button>
        <a-radio-button value="builtin">内置</a-radio-button>
        <a-radio-button value="local">本地</a-radio-button>
      </a-radio-group>
      <a-input
          v-model:value="templateKeyword"
          class="template-search"
          placeholder="搜索模板、字段或标签"
          allow-clear
      />
    </div>
    <a-empty
        v-if="filteredTemplates.length === 0"
        description="没有匹配的模板。"
    />
    <div v-else class="template-list">
      <div
          v-for="item in filteredTemplates"
          :key="item.id"
          class="template-item"
      >
        <div class="template-main">
          <div class="template-title-row">
            <span class="template-title">{{ item.title }}</span>
            <a-tag :color="item.source === 'builtin' ? 'blue' : 'green'">{{ item.source === 'builtin' ? '内置' : '本地' }}</a-tag>
            <a-tag>{{ getTemplateStyleText(item) }}</a-tag>
            <a-tag>{{ getTemplateFieldTotal(item) }} 个字段</a-tag>
          </div>
          <div class="template-desc">{{ item.description || '无描述' }}</div>
          <div class="template-meta">
            <span>{{ item.category || '未分类' }}</span>
            <span v-if="item.updatedAt">更新 {{ getTemplateUpdatedText(item) }}</span>
          </div>
          <div class="template-fields">
            <a-tag
                v-for="field in getTemplatePreviewFields(item)"
                :key="field"
            >{{ field }}</a-tag>
          </div>
        </div>
        <div class="template-actions">
          <a-button size="small" @click="useTemplate(item)">使用</a-button>
          <a-button size="small" type="primary" @click="generateDraftFromTemplate(item)">生成草稿</a-button>
          <a-button
              v-if="item.source === 'local'"
              size="small"
              danger
              @click="removeLocalTemplate(item)"
          >删除</a-button>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {message, Modal} from 'ant-design-vue'
import {generateFormDesignDslDraft} from "@/views/gen/genTableExt/ai/dslDraftGenerator";
import {checkFormDesignDsl, summarizeDesignIssues} from "@/views/gen/genTableExt/ai/designCheckRules";
import {explainFormDesignIssues} from "@/views/gen/genTableExt/ai/designIssueExplain";
import {convertDslToDesignerStatePatch} from "@/views/gen/genTableExt/ai/dslToDesignerState";
import {
  buildIncrementalFieldPatch,
  resolveDefaultApplyMode,
} from "@/views/gen/genTableExt/ai/mergeIncrementalFields";
import {summarizeSchemaIssues, validateFormDesignDslSchema} from "@/views/gen/genTableExt/ai/dslSchema";
import {
  buildClarifiedRequirementText,
  createClarificationQuestions,
  createDefaultClarificationAnswers,
  getClarificationAnswerList,
  getClarifiedGenerationOptions,
  getUnansweredClarificationQuestions,
} from "@/views/gen/genTableExt/ai/requirementClarifier";
import {
  createRuntimeDraftFromLocalDraft,
  deleteLocalDraft,
  getCurrentDraftVersion,
  listLocalDrafts,
  saveLocalDraft,
} from "@/views/gen/genTableExt/ai/draftStorage";
import {
  createRuntimeDraftFromRemoteDraft,
  deleteRemoteDraft,
  getRemoteDraft,
  importLocalDraftToRemote,
  listRemoteDrafts,
  saveRemoteDraft,
} from "@/views/gen/genTableExt/ai/remoteDraftProvider";
import {generateRemoteFormDesignDslDraft} from "@/views/gen/genTableExt/ai/remoteAiDraftProvider";
import {
  recognizeExcelFormMaterial,
  recognizeFileFormMaterial,
  recognizeRemoteFormMaterial,
  reviseRemoteFormMaterial,
} from "@/views/gen/genTableExt/ai/remoteMaterialProvider";
import {
  createRuntimeDraftFromTemplate,
  deleteLocalTemplate,
  getTemplateFieldCount,
  listAllTemplates,
  listLocalTemplates,
  saveLocalTemplateFromDraft,
} from "@/views/gen/genTableExt/ai/templateLibrary";
import {buildFormalizePreview} from "@/views/gen/genTableExt/ai/formalizePreview";
import {
  buildFormalizeCheckResult,
  mergeFormalizeIssuesToPreview,
} from "@/views/gen/genTableExt/ai/formalizeCheckRules";
import {convertFormMaterialToDslDraft} from "@/views/gen/genTableExt/ai/materialToDslConverter";
import {
  FORM_MATERIAL_FIELD_TYPE_OPTIONS,
  FORM_MATERIAL_QUERY_MODE_OPTIONS,
  summarizeFormMaterialIssues,
  validateFormMaterialSchema,
} from "@/views/gen/genTableExt/ai/formMaterialSchema";
import {
  createMaterialFieldQualityIssues,
  normalizeMaterialFieldSuggestion,
} from "@/views/gen/genTableExt/ai/formMaterialQualityRules";
import {
  applyDictionaryConfirmationsToDsl,
  buildSysDictionarySaveData,
  createDictionaryConfirmationMap,
  parseDictionaryItemsText,
} from "@/views/gen/genTableExt/ai/dictionarySuggestions";
import {
  applyExistingDictionaryMatch,
  applyExistingDictionaryMatchesToMap,
  clearSystemDictionaryCodeMapCache,
  collectDictionarySuggestionCodes,
  loadSystemDictionaryCodeMap,
} from "@/views/gen/genTableExt/ai/systemDictionaryMatcher";
import {saveDataAction} from "@/api/api";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  table: {
    type: Object,
    default() {
      return {}
    },
  },
  formModel: {
    type: Object,
    default() {
      return {}
    },
  },
  formPropsModel: {
    type: Object,
    default() {
      return {}
    },
  },
  extendJsConfig: {
    type: Object,
    default() {
      return {}
    },
  },
  displayFormItemArr: {
    type: Array,
    default() {
      return []
    },
  },
  hideFormItemArr: {
    type: Array,
    default() {
      return []
    },
  },
  fixedArr: {
    type: Array,
    default() {
      return []
    },
  },
})

const emit = defineEmits(['update:visible', 'apply-draft-patch'])

function createEmptyFormalizeCheckResult() {
  return {
    preview: null,
    issues: [],
    summary: {
      total: 0,
      error: 0,
      warning: 0,
      suggestion: 0,
      fixable: 0,
    },
    errors: [],
    warnings: [],
    suggestions: [],
    canFormalize: false,
    needConfirm: false,
    topIssues: [],
  }
}

const draftMode = ref('auto')
const requirementText = ref('')
const materialSourceType = ref('manual')
const materialTableText = ref('')
const materialFileList = ref([])
const materialConfirmKeyword = ref('')
const materialConfirmFilter = ref('all')
const materialRevisionInstruction = ref('')
const materialRevisionReview = ref(null)
const materialConfirmation = ref({
  signature: '',
  confirmedAt: '',
  issueSummary: null,
  fieldCount: 0,
})
const materialRevising = ref(false)
const excelFileNamePattern = /\.(xlsx|xls)$/i
const wordFileNamePattern = /\.docx$/i
const pdfFileNamePattern = /\.pdf$/i
const imageFileNamePattern = /\.(png|jpg|jpeg|webp)$/i
const materialRecognitionResult = ref(null)
const draft = ref(null)
const conversionPreview = ref(null)
const conversionApplied = ref(false)
/** replace=全量替换；incremental=仅追加新字段 */
const applyMode = ref('replace')
const formalizePreview = ref(null)
const formalizeCheckResult = ref(createEmptyFormalizeCheckResult())
const draftHistoryVisible = ref(false)
const localDrafts = ref([])
const remoteDraftHistoryVisible = ref(false)
const remoteDrafts = ref([])
const remoteDraftKeyword = ref('')
const remoteLoading = ref(false)
const remoteSaving = ref(false)
const remoteLoadingDraftId = ref('')
const remoteDeletingDraftId = ref('')
const localDraftImportingId = ref('')
const aiLastError = ref(null)
const currentLocalDraftId = ref('')
const currentLocalVersionId = ref('')
const currentRemoteDraftId = ref('')
const currentRemoteVersionId = ref('')
const selectedHistoryDraftId = ref('')
const compareLeftVersionId = ref('')
const compareRightVersionId = ref('')
const clarificationQuestions = ref([])
const clarificationAnswers = ref({})
const templateLibraryVisible = ref(false)
const localTemplates = ref([])
const templateKeyword = ref('')
const templateSourceFilter = ref('all')
const aiDraftGenerating = ref(false)
const aiMaterialRecognizing = ref(false)
const dictionaryConfirmations = ref({})
const dictionarySaving = ref(false)
const dictionaryExistingCodeMap = ref({})
const dictionaryExistingMatching = ref(false)

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

const materialFieldTypeTextMap = {
  text: '单行文本',
  textarea: '多行文本',
  integer: '整数',
  decimal: '小数',
  select: '下拉',
  radio: '单选',
  checkbox: '多选',
  switch: '开关',
  date: '日期',
  user: '人员',
  users: '多人',
  office: '部门',
  area: '区域',
  tree: '树选择',
  modalSelect: '弹窗选择',
  modalMultiSelect: '弹窗多选',
  upload: '附件',
  imageUpload: '图片',
  onlineFile: '在线文件',
  richText: '富文本',
  serialNo: '流水号',
}

const materialQueryModeTextMap = {
  '': '自动',
  input: '输入框',
  like: '模糊',
  exact: '精确',
  select: '选择',
  'date-range': '日期范围',
  range: '数值范围',
}

const materialFieldTypeOptions = FORM_MATERIAL_FIELD_TYPE_OPTIONS.map(value => ({
  value,
  label: materialFieldTypeTextMap[value] ? `${materialFieldTypeTextMap[value]} (${value})` : value,
}))

const materialQueryModeOptions = FORM_MATERIAL_QUERY_MODE_OPTIONS.map(value => ({
  value,
  label: materialQueryModeTextMap[value] || value,
}))

const materialConfirmFilterOptions = [
  {value: 'all', label: '全部'},
  {value: 'risk', label: '待确认'},
  {value: 'revision', label: '本次变更'},
  {value: 'list', label: '列表'},
  {value: 'query', label: '查询'},
  {value: 'required', label: '必填'},
]

const dictionaryModeOptions = [
  {value: 'create', label: '新建系统字典'},
  {value: 'use-existing', label: '使用已有字典'},
  {value: 'ignore', label: '暂不处理'},
]

const dictionaryModeTextMap = {
  create: '新建',
  'use-existing': '已有',
  ignore: '忽略',
}

const systemDictionaryFilterData = [
  {key: 'a.parent_code', type: 'eq', value: 'data-params'},
]
const SELECT_DICT_DATA_REFRESH_EVENT = 'u-select-dict-data-refresh'

const materialRevisionExamples = [
  {label: '长文本整行', text: '把描述、说明、备注、意见、批示、批分等大段文字字段改为多行文本并占整行，不要进入列表和查询。'},
  {label: '年月格式', text: '标题中包含“年/月”“年月”“月份”的日期字段，日期格式改为 yyyy-MM。'},
  {label: '人员部门', text: '申请人、经办人、负责人等改为人员选择；部门、机构、单位类字段改为部门选择。'},
  {label: '列表查询', text: '重新建议列表字段和查询条件，只保留常用摘要字段和高频查询条件。'},
]

const sampleMap = {
  contract: '合同审批表，需要合同名称、合同编号、甲方、乙方、金额、签订日期、经办人、附件、部门意见、领导意见',
  receive: '收文处理笺，需要来文单位、文号、标题、收文日期、缓急、局领导批示、办公室批分、附件',
  purchase: '采购申请表，需要名称、编号、申请人、申请部门、申请日期、采购金额、采购数量、说明、部门意见、领导意见、附件',
}

const materialSourceTextMap = {
  manual: '手动描述',
  'table-text': '粘贴表格',
  file: '上传文件',
}

const layoutTextMap = {
  normal: '普通表单',
  'document-form': '公文文单',
  approval: '审批表单',
}

const levelTextMap = {
  error: '错误',
  warning: '警告',
  suggestion: '建议',
}

const levelColorMap = {
  error: 'red',
  warning: 'orange',
  suggestion: 'green',
}

const designLevelConfig = [
  {key: 'error', title: '错误', color: 'red'},
  {key: 'warning', title: '警告', color: 'orange'},
  {key: 'suggestion', title: '建议', color: 'green'},
]

const schemaSummary = computed(() => {
  return draft.value?.summary?.schema || {
    total: 0,
    error: 0,
    warning: 0,
    suggestion: 0,
    fixable: 0,
  }
})

const designIssues = computed(() => draft.value?.designIssues || [])

const materialTableRows = computed(() => {
  return String(materialTableText.value || '')
      .split(/\r?\n/)
      .map(row => row.trim())
      .filter(Boolean)
})

const materialTableLineCount = computed(() => materialTableRows.value.length)

const materialTableColumnCount = computed(() => {
  const firstRow = materialTableRows.value[0] || ''
  if (!firstRow) {
    return 0
  }
  if (firstRow.includes('\t')) {
    return firstRow.split('\t').filter(Boolean).length
  }
  if (firstRow.includes('|')) {
    return firstRow.split('|').map(item => item.trim()).filter(Boolean).length
  }
  if (firstRow.includes(',')) {
    return firstRow.split(',').map(item => item.trim()).filter(Boolean).length
  }
  return firstRow.split(/\s+/).filter(Boolean).length
})

const materialFileSummary = computed(() => {
  const file = materialFileList.value[0]
  if (!file) {
    return '尚未选择文件。'
  }
  const size = Number(file.size || 0)
  const sizeText = size > 0 ? `${(size / 1024).toFixed(1)} KB` : '未知大小'
  return `${file.name || '已选择文件'}，${sizeText}。`
})

const getMaterialRawFile = () => {
  const file = materialFileList.value[0]
  return file?.originFileObj || file || null
}

const isExcelMaterialFile = (file = {}) => excelFileNamePattern.test(file.name || '')

const isWordMaterialFile = (file = {}) => wordFileNamePattern.test(file.name || '')

const isPdfMaterialFile = (file = {}) => pdfFileNamePattern.test(file.name || '')

const isImageMaterialFile = (file = {}) => {
  const fileName = file.name || ''
  const mimeType = file.type || ''
  return imageFileNamePattern.test(fileName) || String(mimeType).startsWith('image/')
}

const getMaterialFileSourceType = (file = {}) => {
  if (isExcelMaterialFile(file)) {
    return 'excel'
  }
  if (isWordMaterialFile(file)) {
    return 'word'
  }
  if (isPdfMaterialFile(file)) {
    return 'pdf'
  }
  if (isImageMaterialFile(file)) {
    return 'image'
  }
  return ''
}

const getMaterialFileKindText = (file = {}) => {
  const sourceType = getMaterialFileSourceType(file)
  if (sourceType === 'excel') {
    return 'Excel'
  }
  if (sourceType === 'word') {
    return 'Word'
  }
  if (sourceType === 'pdf') {
    return 'PDF'
  }
  if (sourceType === 'image') {
    return '图片'
  }
  return '文件'
}

const isSupportedMaterialFile = (file = {}) => Boolean(getMaterialFileSourceType(file))

const formatDiagnosticValue = (value, emptyText = '-') => {
  if (value === undefined || value === null || value === '') {
    return emptyText
  }
  return String(value)
}

const formatElapsedMs = (value) => {
  const elapsed = Number(value || 0)
  return elapsed > 0 ? `${(elapsed / 1000).toFixed(1)} 秒` : '-'
}

const formatDiagnosticBoolean = (value) => {
  if (value === true) {
    return '是'
  }
  if (value === false) {
    return '否'
  }
  return '-'
}

const createAiErrorDiagnostic = (error = {}, stage = 'AI 调用', fallbackMessage = 'AI 调用失败') => {
  const result = error?.result || {}
  return {
    stage,
    status: 'error',
    requestId: error?.requestId || result.requestId || '',
    provider: result.provider || '',
    model: result.model || '',
    promptVersion: result.promptVersion || '',
    elapsedMs: result.elapsedMs || 0,
    errorCode: error?.code || result.errorCode || '',
    errorMessage: error?.message || result.message || fallbackMessage,
    extraction: result.extraction || null,
    rawOutputPreview: result.rawOutputPreview || '',
    occurredAt: new Date().toISOString(),
  }
}

const formatAiErrorMessage = (error = {}, fallbackMessage = 'AI 调用失败') => {
  const requestId = error?.requestId || error?.result?.requestId || ''
  const errorCode = error?.code || error?.result?.errorCode || ''
  const baseMessage = error?.message || fallbackMessage
  const suffix = []
  if (requestId) {
    suffix.push(`requestId：${requestId}`)
  }
  if (errorCode) {
    suffix.push(`错误码：${errorCode}`)
  }
  return suffix.length > 0 ? `${baseMessage}（${suffix.join('，')}）` : baseMessage
}

const materialPreview = computed(() => materialRecognitionResult.value?.material || null)

const materialPreviewFields = computed(() => Array.isArray(materialPreview.value?.fields) ? materialPreview.value.fields : [])

const materialPreviewGroups = computed(() => Array.isArray(materialPreview.value?.groups) ? materialPreview.value.groups : [])

const materialPreviewTableRows = computed(() => {
  const tables = Array.isArray(materialPreview.value?.tables) ? materialPreview.value.tables : []
  return Array.isArray(tables[0]?.rows) ? tables[0].rows : []
})

const getMaterialFieldKey = (field = {}, index = 0) => field.id || `${normalizeText(field.label) || 'field'}-${index}`

const materialFieldRiskMap = computed(() => {
  const map = {}
  const nameIndexes = {}
  materialPreviewFields.value.forEach((field, index) => {
    const name = normalizeText(field.nameHint).toLowerCase()
    if (!name) {
      return
    }
    if (!nameIndexes[name]) {
      nameIndexes[name] = []
    }
    nameIndexes[name].push(index)
  })

  materialPreviewFields.value.forEach((field, index) => {
    const key = getMaterialFieldKey(field, index)
    const name = normalizeText(field.nameHint).toLowerCase()
    map[key] = createMaterialFieldQualityIssues({
      field,
      index,
      duplicateNameCount: name ? (nameIndexes[name] || []).length : 0,
    })
  })
  return map
})

const materialConfirmRiskIssues = computed(() => {
  return Object.values(materialFieldRiskMap.value).reduce((list, issues) => list.concat(issues), [])
})

const getMaterialFieldRisks = (field = {}, index = 0) => {
  return materialFieldRiskMap.value[getMaterialFieldKey(field, index)] || []
}

const materialFieldEntries = computed(() => {
  return materialPreviewFields.value.map((field, index) => ({
    field,
    index,
    risks: getMaterialFieldRisks(field, index),
  }))
})

const getMaterialFieldSearchText = (field = {}) => {
  return [
    field.label,
    field.nameHint,
    field.typeHint,
    field.valueExample,
    field.listReason,
    field.queryReason,
  ].map(item => normalizeText(item)).join(' ').toLowerCase()
}

const materialFilteredFieldEntries = computed(() => {
  const keyword = normalizeText(materialConfirmKeyword.value).toLowerCase()
  const filter = materialConfirmFilter.value
  return materialFieldEntries.value.filter(entry => {
    if (keyword && getMaterialFieldSearchText(entry.field).indexOf(keyword) < 0) {
      return false
    }
    if (filter === 'risk') {
      return entry.risks.length > 0
    }
    if (filter === 'revision') {
      return materialRevisionChangedFieldKeySet.value.has(getMaterialFieldKey(entry.field, entry.index))
    }
    if (filter === 'list') {
      return Boolean(entry.field.listHint)
    }
    if (filter === 'query') {
      return Boolean(entry.field.queryHint)
    }
    if (filter === 'required') {
      return Boolean(entry.field.requiredHint)
    }
    return true
  })
})

const materialConfirmStats = computed(() => {
  const entries = materialFieldEntries.value
  const riskEntries = entries.filter(entry => entry.risks.length > 0)
  const riskIssues = riskEntries.reduce((list, entry) => list.concat(entry.risks), [])
  return {
    total: entries.length,
    visible: materialFilteredFieldEntries.value.length,
    risk: riskEntries.length,
    error: riskIssues.filter(issue => issue.level === 'error').length,
    warning: riskIssues.filter(issue => issue.level === 'warning').length,
    suggestion: riskIssues.filter(issue => issue.level === 'suggestion').length,
    list: entries.filter(entry => entry.field.listHint).length,
    query: entries.filter(entry => entry.field.queryHint).length,
    required: entries.filter(entry => entry.field.requiredHint).length,
  }
})

const materialRevisionCompareFields = [
  {key: 'label', label: '标题'},
  {key: 'nameHint', label: '英文名'},
  {key: 'typeHint', label: '控件'},
  {key: 'spanHint', label: '显示'},
  {key: 'requiredHint', label: '必填'},
  {key: 'listHint', label: '列表'},
  {key: 'queryHint', label: '查询'},
  {key: 'queryMode', label: '查询方式'},
  {key: 'dateType', label: '日期格式'},
  {key: 'defaultValue', label: '默认值'},
]

const getMaterialRevisionFieldTitle = (field = {}) => {
  return normalizeText(field.label) || normalizeText(field.nameHint) || '未命名字段'
}

const getMaterialRevisionFieldKeys = (field = {}) => {
  return [
    field.id ? `id:${normalizeText(field.id)}` : '',
    field.nameHint ? `name:${normalizeText(field.nameHint).toLowerCase()}` : '',
    field.label ? `label:${normalizeText(field.label)}` : '',
  ].filter(Boolean)
}

const getMaterialRevisionCompareValue = (field = {}, key = '') => {
  if (key === 'defaultValue') {
    return field.defaultValue ?? field.defaultHint ?? field.form?.defaultValue ?? ''
  }
  return field[key]
}

const normalizeMaterialRevisionCompareValue = (value) => {
  if (value === undefined || value === null) {
    return ''
  }
  if (typeof value === 'boolean') {
    return value ? '1' : '0'
  }
  if (typeof value === 'object') {
    try {
      return JSON.stringify(value)
    } catch (error) {
      return String(value)
    }
  }
  return String(value)
}

const formatMaterialRevisionValue = (key = '', value) => {
  if (key === 'spanHint') {
    return Number(value) === 24 ? '整行' : Number(value) === 12 ? '半行' : (value || '空')
  }
  if (['requiredHint', 'listHint', 'queryHint'].includes(key)) {
    return value === true || value === '1' || value === 'true' ? '是' : '否'
  }
  return normalizeText(value) || '空'
}

const findMaterialRevisionAfterIndex = (beforeField = {}, afterEntries = [], usedIndexes = new Set()) => {
  const beforeKeys = getMaterialRevisionFieldKeys(beforeField)
  for (const beforeKey of beforeKeys) {
    const matched = afterEntries.find(entry => !usedIndexes.has(entry.index) && entry.keys.includes(beforeKey))
    if (matched) {
      return matched.index
    }
  }
  return -1
}

const createMaterialRevisionReview = (beforeResult = {}, afterResult = {}, instruction = '') => {
  const beforeMaterial = beforeResult?.material || {}
  const afterMaterial = afterResult?.material || {}
  const beforeFields = toArray(beforeMaterial.fields)
  const afterFields = toArray(afterMaterial.fields)
  const afterEntries = afterFields.map((field, index) => ({
    field,
    index,
    keys: getMaterialRevisionFieldKeys(field),
  }))
  const usedAfterIndexes = new Set()
  const added = []
  const removed = []
  const changed = []
  const visibleFieldKeys = []

  beforeFields.forEach((beforeField, beforeIndex) => {
    const afterIndex = findMaterialRevisionAfterIndex(beforeField, afterEntries, usedAfterIndexes)
    if (afterIndex < 0) {
      removed.push({
        key: `removed-${beforeIndex}`,
        beforeIndex,
        field: beforeField,
        title: getMaterialRevisionFieldTitle(beforeField),
      })
      return
    }
    usedAfterIndexes.add(afterIndex)
    const afterField = afterFields[afterIndex]
    const diffs = materialRevisionCompareFields.reduce((list, config) => {
      const beforeValue = getMaterialRevisionCompareValue(beforeField, config.key)
      const afterValue = getMaterialRevisionCompareValue(afterField, config.key)
      if (normalizeMaterialRevisionCompareValue(beforeValue) !== normalizeMaterialRevisionCompareValue(afterValue)) {
        list.push({
          key: config.key,
          label: config.label,
          before: formatMaterialRevisionValue(config.key, beforeValue),
          after: formatMaterialRevisionValue(config.key, afterValue),
        })
      }
      return list
    }, [])
    if (diffs.length > 0) {
      changed.push({
        key: `changed-${afterIndex}`,
        beforeIndex,
        afterIndex,
        beforeField,
        afterField,
        title: getMaterialRevisionFieldTitle(afterField),
        diffs,
      })
      visibleFieldKeys.push(getMaterialFieldKey(afterField, afterIndex))
    }
  })

  afterFields.forEach((afterField, afterIndex) => {
    if (usedAfterIndexes.has(afterIndex)) {
      return
    }
    added.push({
      key: `added-${afterIndex}`,
      afterIndex,
      field: afterField,
      title: getMaterialRevisionFieldTitle(afterField),
    })
    visibleFieldKeys.push(getMaterialFieldKey(afterField, afterIndex))
  })

  return {
    instruction: normalizeText(instruction),
    revisedAt: new Date().toISOString(),
    beforeResult: clonePlain(beforeResult),
    afterRequestId: afterResult?.requestId || afterResult?.remoteResult?.requestId || '',
    added,
    removed,
    changed,
    visibleFieldKeys,
  }
}

const materialRevisionChangedFieldKeySet = computed(() => {
  return new Set(toArray(materialRevisionReview.value?.visibleFieldKeys))
})

const materialRevisionVisibleChanges = computed(() => {
  const review = materialRevisionReview.value
  if (!review) {
    return []
  }
  const rows = []
  toArray(review.added).forEach(item => {
    rows.push({
      key: item.key,
      text: `新增「${item.title}」`,
    })
  })
  toArray(review.removed).forEach(item => {
    rows.push({
      key: item.key,
      text: `移除「${item.title}」`,
    })
  })
  toArray(review.changed).forEach(item => {
    const changeText = item.diffs.slice(0, 3)
        .map(diff => `${diff.label} ${diff.before} -> ${diff.after}`)
        .join('；')
    const moreText = item.diffs.length > 3 ? `；另 ${item.diffs.length - 3} 项` : ''
    rows.push({
      key: item.key,
      text: `变更「${item.title}」：${changeText}${moreText}`,
    })
  })
  return rows.slice(0, 8)
})

const materialRevisionReviewSummaryText = computed(() => {
  const review = materialRevisionReview.value
  if (!review) {
    return ''
  }
  const total = toArray(review.added).length + toArray(review.removed).length + toArray(review.changed).length
  const requestText = review.afterRequestId ? `，requestId：${review.afterRequestId}` : ''
  return total > 0
      ? `本次 AI 修正涉及 ${total} 个字段${requestText}`
      : `AI 已返回新结果，未发现字段级差异${requestText}`
})

const materialConfirmEmptyText = computed(() => {
  if (normalizeText(materialConfirmKeyword.value)) {
    return '没有匹配当前搜索条件的字段'
  }
  if (materialConfirmFilter.value !== 'all') {
    const option = materialConfirmFilterOptions.find(item => item.value === materialConfirmFilter.value)
    return `没有${option?.label || '当前筛选'}字段`
  }
  return '暂无识别字段'
})

const resetMaterialConfirmFilter = () => {
  materialConfirmKeyword.value = ''
  materialConfirmFilter.value = 'all'
}

const clearMaterialRevisionReview = () => {
  materialRevisionReview.value = null
}

const clearMaterialConfirmation = () => {
  materialConfirmation.value = {
    signature: '',
    confirmedAt: '',
    issueSummary: null,
    fieldCount: 0,
  }
}

const confirmCurrentMaterialRecognition = () => {
  if (!materialPreview.value) {
    message.warning('请先识别材料')
    return
  }
  if ((materialPreviewIssueSummary.value.error || 0) > 0) {
    message.error('材料存在错误，暂不能确认生成')
    return
  }
  materialConfirmation.value = {
    signature: materialConfirmationSignature.value,
    confirmedAt: new Date().toISOString(),
    issueSummary: clonePlain(materialPreviewIssueSummary.value),
    fieldCount: materialPreviewFields.value.length,
  }
  message.success('已确认当前材料识别结果，可生成 DSL 草稿')
}

const showMaterialRiskFields = () => {
  materialConfirmKeyword.value = ''
  materialConfirmFilter.value = 'risk'
}

const showRevisionChangedOnly = () => {
  materialConfirmKeyword.value = ''
  materialConfirmFilter.value = 'revision'
}

const restoreMaterialRevisionBefore = () => {
  if (!materialRevisionReview.value?.beforeResult) {
    message.warning('没有可撤回的修正记录')
    return
  }
  materialRecognitionResult.value = clonePlain(materialRevisionReview.value.beforeResult)
  clearMaterialRevisionReview()
  clearMaterialConfirmation()
  resetMaterialDerivedDraftState()
  message.success('已撤回到本次 AI 修正前的识别结果')
}

const normalizeMaterialFieldAt = (field = {}) => {
  normalizeMaterialFieldSuggestion(field)
  clearMaterialConfirmation()
  message.success(`已按规则修正：${field.label || field.nameHint || '字段'}`)
}

const materialPreviewIssues = computed(() => {
  if (!materialRecognitionResult.value) {
    return []
  }
  const schemaIssues = materialPreview.value ? validateFormMaterialSchema(materialPreview.value) : []
  return []
      .concat(toArray(materialRecognitionResult.value.parseIssues))
      .concat(schemaIssues)
      .concat(materialConfirmRiskIssues.value)
})

const materialPreviewIssueSummary = computed(() => {
  return summarizeFormMaterialIssues(materialPreviewIssues.value)
})

const materialConfirmationSignature = computed(() => {
  if (!materialPreview.value) {
    return ''
  }
  const payload = {
    title: normalizeText(materialPreview.value.title),
    scene: normalizeText(materialPreview.value.scene),
    fields: materialPreviewFields.value.map(field => ({
      id: normalizeText(field.id),
      label: normalizeText(field.label),
      nameHint: normalizeText(field.nameHint),
      typeHint: normalizeText(field.typeHint),
      spanHint: Number(field.spanHint || 12),
      requiredHint: field.requiredHint === true || field.requiredHint === '1',
      listHint: field.listHint === true || field.listHint === '1',
      listPriority: Number(field.listPriority || 0),
      queryHint: field.queryHint === true || field.queryHint === '1',
      queryPriority: Number(field.queryPriority || 0),
      queryMode: normalizeText(field.queryMode),
      dateType: normalizeText(field.dateType),
      defaultValue: normalizeText(field.defaultValue || field.defaultHint || field.form?.defaultValue),
    })),
    groups: materialPreviewGroups.value.map(group => ({
      key: normalizeText(group.key),
      title: normalizeText(group.title),
    })),
  }
  return JSON.stringify(payload)
})

const materialConfirmed = computed(() => {
  return Boolean(materialConfirmation.value.signature
      && materialConfirmation.value.signature === materialConfirmationSignature.value)
})

const materialCanConfirm = computed(() => {
  return Boolean(materialPreview.value
      && materialPreviewFields.value.length > 0
      && (materialPreviewIssueSummary.value.error || 0) === 0
      && !materialRevising.value)
})

const canGenerateDraftFromMaterial = computed(() => {
  return Boolean(materialCanConfirm.value && materialConfirmed.value)
})

const materialConfirmationTagColor = computed(() => {
  if ((materialPreviewIssueSummary.value.error || 0) > 0) {
    return 'red'
  }
  if (materialConfirmed.value) {
    return 'green'
  }
  return 'orange'
})

const materialConfirmationTagText = computed(() => {
  if ((materialPreviewIssueSummary.value.error || 0) > 0) {
    return '不可生成'
  }
  if (materialConfirmed.value) {
    return '已确认'
  }
  if (materialConfirmation.value.signature) {
    return '已变更'
  }
  return '未确认'
})

const formatMaterialConfirmedAt = (value = '') => {
  if (!value) {
    return ''
  }
  try {
    return new Date(value).toLocaleString()
  } catch (error) {
    return value
  }
}

const materialConfirmationTitle = computed(() => {
  if ((materialPreviewIssueSummary.value.error || 0) > 0) {
    return '当前材料存在错误，暂不能生成 DSL'
  }
  if (materialConfirmed.value) {
    return '当前材料已确认，可以生成 DSL 草稿'
  }
  if (materialConfirmation.value.signature) {
    return '确认后材料又发生了变化'
  }
  return '请先确认当前材料识别结果'
})

const materialConfirmationDescription = computed(() => {
  const summary = materialPreviewIssueSummary.value
  if ((summary.error || 0) > 0) {
    return `还有 ${summary.error} 个错误需要处理，可先筛选待确认项或使用 AI 二次修正。`
  }
  if (materialConfirmed.value) {
    const confirmedAt = formatMaterialConfirmedAt(materialConfirmation.value.confirmedAt)
    return confirmedAt
        ? `确认时间：${confirmedAt}。之后生成 DSL 将使用当前字段、控件、列表和查询建议。`
        : '之后生成 DSL 将使用当前字段、控件、列表和查询建议。'
  }
  if (materialConfirmation.value.signature) {
    return '字段内容、控件类型、列表/查询建议或 AI 修正结果有变化，请重新确认后再生成 DSL。'
  }
  if ((summary.warning || 0) > 0 || (summary.suggestion || 0) > 0) {
    return `还有 ${summary.warning || 0} 个警告、${summary.suggestion || 0} 个建议。可以先处理，也可以确认当前结果继续生成。`
  }
  return '确认后才能生成 DSL 草稿，避免把未检查的识别结果直接应用到后续链路。'
})

const materialConfirmationButtonText = computed(() => {
  return materialConfirmed.value ? '已确认当前材料' : '确认当前材料'
})

const materialPreviewTitle = computed(() => materialPreview.value?.title || '未识别标题')

const materialPreviewSceneText = computed(() => {
  const scene = materialPreview.value?.scene || ''
  return {
    normal: '普通表单',
    'document-form': '公文文单',
    approval: '审批表单',
    ledger: '台账/清单',
  }[scene] || '未识别场景'
})

const materialPreviewConfidenceText = computed(() => {
  const confidence = Number(materialPreview.value?.confidence || 0)
  return `${Math.round(confidence * 100)}%`
})

const designSummary = computed(() => {
  return draft.value?.designSummary || {
    total: 0,
    error: 0,
    warning: 0,
    suggestion: 0,
    fixable: 0,
  }
})

const groupedDesignIssues = computed(() => {
  return designLevelConfig
      .map(config => ({
        ...config,
        issues: designIssues.value.filter(issue => issue.level === config.key),
      }))
      .filter(group => group.issues.length > 0)
})

const layoutText = computed(() => {
  const style = draft.value?.dsl?.layout?.style || ''
  return layoutTextMap[style] || style || '-'
})

const aiGeneratorText = computed(() => {
  if (!draft.value) {
    return '-'
  }
  if (draft.value.sourceType === 'remote-ai' || draft.value.provider === 'remote') {
    return draft.value.model ? `真实 AI：${draft.value.model}` : '真实 AI'
  }
  if (draft.value.generator === 'local-heuristic') {
    if (draft.value.remoteError) {
      return '本地生成器（远程AI已回退）'
    }
    return '本地生成器'
  }
  if (draft.value.sourceType === 'template') {
    return '模板库'
  }
  if (draft.value.sourceType === 'form-material' || draft.value.generator === 'form-material-local') {
    return '材料转换器'
  }
  return draft.value.generator || draft.value.sourceType || '-'
})

const aiDiagnosticInfo = computed(() => {
  if (aiLastError.value) {
    return aiLastError.value
  }

  const currentDraft = draft.value
  if (currentDraft?.remoteError) {
    return {
      ...currentDraft.remoteError,
      stage: 'DSL 草稿生成',
      status: 'warning',
      provider: currentDraft.remoteError?.result?.provider || '',
      model: currentDraft.remoteError?.result?.model || '',
      promptVersion: currentDraft.remoteError?.result?.promptVersion || '',
      elapsedMs: currentDraft.remoteError?.result?.elapsedMs || 0,
      errorCode: currentDraft.remoteError?.code || currentDraft.remoteError?.result?.errorCode || '',
      errorMessage: currentDraft.remoteError?.message || '远程 AI 失败，已回退本地生成器',
      extraction: currentDraft.remoteError?.result?.extraction || null,
      rawOutputPreview: currentDraft.remoteError?.result?.rawOutputPreview || '',
      fieldCount: currentDraft.summary?.fieldCount || 0,
    }
  }

  if (currentDraft && (currentDraft.requestId || currentDraft.remoteResult?.requestId)) {
    return {
      stage: 'DSL 草稿生成',
      status: 'success',
      requestId: currentDraft.requestId || currentDraft.remoteResult?.requestId || '',
      provider: currentDraft.provider || currentDraft.remoteResult?.provider || '',
      model: currentDraft.model || currentDraft.remoteResult?.model || '',
      promptVersion: currentDraft.promptVersion || currentDraft.remoteResult?.promptVersion || '',
      elapsedMs: currentDraft.elapsedMs || currentDraft.remoteResult?.elapsedMs || 0,
      errorCode: currentDraft.errorCode || currentDraft.remoteResult?.errorCode || '',
      errorMessage: '',
      extraction: null,
      rawOutputPreview: currentDraft.rawOutputPreview || currentDraft.remoteResult?.rawOutputPreview || '',
      fieldCount: currentDraft.summary?.fieldCount || 0,
      issueCount: currentDraft.summary?.schema?.total || 0,
    }
  }

  const materialResult = materialRecognitionResult.value
  if (materialResult) {
    return {
      stage: '材料识别',
      status: (materialResult.summary?.error || 0) > 0 ? 'warning' : 'success',
      requestId: materialResult.requestId || materialResult.remoteResult?.requestId || '',
      provider: materialResult.provider || materialResult.remoteResult?.provider || '',
      model: materialResult.model || materialResult.remoteResult?.model || '',
      promptVersion: materialResult.promptVersion || materialResult.remoteResult?.promptVersion || '',
      elapsedMs: materialResult.elapsedMs || materialResult.remoteResult?.elapsedMs || 0,
      errorCode: materialResult.errorCode || materialResult.remoteResult?.errorCode || '',
      errorMessage: materialResult.message || '',
      extraction: materialResult.extraction || materialResult.remoteResult?.extraction || null,
      rawOutputPreview: materialResult.rawOutputPreview || materialResult.remoteResult?.rawOutputPreview || '',
      fieldCount: materialResult.material?.fields?.length || 0,
      issueCount: materialResult.summary?.total || 0,
    }
  }

  return {}
})

const aiDiagnosticVisible = computed(() => Boolean(
    aiDiagnosticInfo.value.stage
    || aiDiagnosticInfo.value.requestId
    || aiDiagnosticInfo.value.errorMessage
))

const aiDiagnosticTagColor = computed(() => {
  if (aiDiagnosticInfo.value.status === 'error') {
    return 'red'
  }
  if (aiDiagnosticInfo.value.status === 'warning') {
    return 'orange'
  }
  return 'green'
})

const aiDiagnosticStatusText = computed(() => {
  if (aiDiagnosticInfo.value.status === 'error') {
    return '最近失败'
  }
  if (aiDiagnosticInfo.value.status === 'warning') {
    return '需关注'
  }
  return '最近成功'
})

const aiDiagnosticRows = computed(() => {
  const info = aiDiagnosticInfo.value
  return [
    {label: '阶段', value: formatDiagnosticValue(info.stage)},
    {label: 'requestId', value: formatDiagnosticValue(info.requestId), mono: true},
    {label: '模型', value: formatDiagnosticValue(info.model)},
    {label: 'Provider', value: formatDiagnosticValue(info.provider)},
    {label: 'Prompt', value: formatDiagnosticValue(info.promptVersion)},
    {label: '耗时', value: formatElapsedMs(info.elapsedMs)},
    {label: '字段数', value: formatDiagnosticValue(info.fieldCount)},
    {label: '问题数', value: formatDiagnosticValue(info.issueCount)},
    {label: '错误码', value: formatDiagnosticValue(info.errorCode), mono: true},
  ]
})

const aiDiagnosticExtractionRows = computed(() => {
  const extraction = aiDiagnosticInfo.value.extraction || {}
  if (!Object.keys(extraction).length) {
    return []
  }
  return [
    {label: '来源类型', value: formatDiagnosticValue(extraction.sourceType)},
    {label: '文件名', value: formatDiagnosticValue(extraction.fileName)},
    {label: 'MIME', value: formatDiagnosticValue(extraction.mimeType), mono: true},
    {label: '文本长度', value: formatDiagnosticValue(extraction.rawTextLength)},
    {label: '表格数', value: formatDiagnosticValue(extraction.tableCount)},
    {label: '使用 OCR', value: formatDiagnosticBoolean(extraction.usedOcr)},
    {label: 'OCR Provider', value: formatDiagnosticValue(extraction.ocrProvider || extraction.providerCode)},
    {label: 'OCR 页数', value: formatDiagnosticValue(extraction.ocrPageCount)},
  ]
})

const groupedFields = computed(() => {
  if (!draft.value) {
    return []
  }
  const groups = draft.value.dsl.layout.groups || []
  const fields = draft.value.dsl.fields || []
  return groups.map(group => ({
    ...group,
    fields: fields.filter(field => field.group === group.key),
  }))
})

const listFields = computed(() => {
  return draft.value ? draft.value.dsl.fields.filter(field => field.isList) : []
})

const queryFields = computed(() => {
  return draft.value ? draft.value.dsl.fields.filter(field => field.isQuery) : []
})

const dictionaryConfirmationRows = computed(() => {
  return Object.values(dictionaryConfirmations.value || {})
})

const hasDictionarySuggestions = computed(() => dictionaryConfirmationRows.value.length > 0)

const dictionaryStats = computed(() => {
  const rows = dictionaryConfirmationRows.value
  return {
    total: rows.length,
    create: rows.filter(row => row.mode === 'create').length,
    useExisting: rows.filter(row => row.mode === 'use-existing').length,
    ignore: rows.filter(row => row.mode === 'ignore').length,
    created: rows.filter(row => row.created).length,
    pendingCreate: rows.filter(row => row.mode === 'create' && !row.created).length,
    noItems: rows.filter(row => row.mode === 'create' && parseDictionaryItemsText(row.itemsText).length === 0).length,
  }
})

const dictionaryCanBatchCreate = computed(() => {
  return !dictionarySaving.value
      && dictionaryConfirmationRows.value.some(row => row.mode === 'create' && !row.created && normalizeText(row.code) && normalizeText(row.name))
})

const getDictionaryModeColor = (mode = '') => {
  if (mode === 'create') {
    return 'green'
  }
  if (mode === 'use-existing') {
    return 'blue'
  }
  if (mode === 'ignore') {
    return 'default'
  }
  return 'default'
}

const formatSystemDictionaryOption = (row = {}) => {
  const name = normalizeText(row.name)
  const code = normalizeText(row.code)
  if (name && code) {
    return `${name}(${code})`
  }
  return name || code || ''
}

const notifySystemDictionarySelectRefresh = () => {
  if (typeof window === 'undefined') {
    return
  }
  clearSystemDictionaryCodeMapCache()
  window.dispatchEvent(new CustomEvent(SELECT_DICT_DATA_REFRESH_EVENT, {
    detail: {
      type: 'table',
      dictType: 'sys_dictionary',
    },
  }))
}

const refreshExistingDictionaryMatches = async () => {
  const codes = collectDictionarySuggestionCodes(Object.values(dictionaryConfirmations.value || {}))
  if (codes.length === 0) {
    dictionaryExistingCodeMap.value = {}
    return
  }
  dictionaryExistingMatching.value = true
  try {
    const codeMap = await loadSystemDictionaryCodeMap(codes)
    dictionaryExistingCodeMap.value = codeMap
    const nextMap = applyExistingDictionaryMatchesToMap(dictionaryConfirmations.value, codeMap)
    dictionaryConfirmations.value = nextMap
  } catch (error) {
    console.warn('FormDesignAIDictionaryExistingMatchFailed', error)
  } finally {
    dictionaryExistingMatching.value = false
  }
}

const resetDictionaryConfirmations = (runtimeDraft = null) => {
  dictionaryConfirmations.value = runtimeDraft?.dsl ? createDictionaryConfirmationMap(runtimeDraft.dsl) : {}
  refreshExistingDictionaryMatches()
}

const refreshDraftCheckResult = (runtimeDraft = {}) => {
  if (!runtimeDraft?.dsl) {
    return runtimeDraft
  }
  const schemaIssues = validateFormDesignDslSchema(runtimeDraft.dsl)
  const designIssues = explainFormDesignIssues(checkFormDesignDsl(runtimeDraft.dsl), runtimeDraft.dsl)
  return {
    ...runtimeDraft,
    schemaIssues,
    designIssues,
    designSummary: summarizeDesignIssues(designIssues),
    summary: {
      ...(runtimeDraft.summary || {}),
      schema: summarizeSchemaIssues(schemaIssues),
    },
  }
}

const syncDictionaryConfirmationsToDraft = (options = {}) => {
  if (!draft.value || !hasDictionarySuggestions.value) {
    return false
  }
  if (Object.keys(dictionaryExistingCodeMap.value || {}).length > 0) {
    dictionaryConfirmations.value = applyExistingDictionaryMatchesToMap(dictionaryConfirmations.value, dictionaryExistingCodeMap.value)
  }
  const nextDsl = applyDictionaryConfirmationsToDsl(draft.value.dsl, dictionaryConfirmations.value)
  draft.value = refreshDraftCheckResult({
    ...draft.value,
    dsl: nextDsl,
  })
  Object.values(dictionaryConfirmations.value || {}).forEach(row => {
    row.applied = true
  })
  if (options.resetConversion !== false) {
    conversionPreview.value = null
    conversionApplied.value = false
    resetFormalizePreview()
  }
  if (!options.silent) {
    message.success('字典确认已应用到当前 DSL 草稿')
  }
  return true
}

const applyDictionaryConfirmations = () => {
  if (!hasDictionarySuggestions.value) {
    message.info('当前草稿没有需要系统字典的字段')
    return
  }
  syncDictionaryConfirmationsToDraft()
}

const markDictionaryDirty = (row = {}) => {
  row.applied = false
  row.userEdited = true
  if (row.created) {
    row.created = false
  }
  conversionPreview.value = null
  conversionApplied.value = false
  resetFormalizePreview()
}

const handleDictionaryModeChange = (row = {}) => {
  if (row.mode === 'use-existing' && !row.existingCode) {
    row.existingCode = ''
  }
  markDictionaryDirty(row)
}

const batchCreateDictionaries = async () => {
  const rows = dictionaryConfirmationRows.value.filter(row => row.mode === 'create' && !row.created)
  if (rows.length === 0) {
    message.info('没有待创建的系统字典')
    return
  }
  const invalidRow = rows.find(row => !normalizeText(row.code) || !normalizeText(row.name))
  if (invalidRow) {
    message.warning(`请先补齐「${invalidRow.fieldLabel || invalidRow.fieldName}」的字典编码和名称`)
    return
  }

  dictionarySaving.value = true
  try {
    const existingCodeMap = await loadSystemDictionaryCodeMap(rows.map(row => row.code), {force: true})
    let createdCount = 0
    let reusedCount = 0
    for (const row of rows) {
      const matchedRow = applyExistingDictionaryMatch(row, existingCodeMap, {forceUserEdited: true})
      if (matchedRow.autoMatchedExistingDictionary) {
        Object.assign(row, matchedRow, {
          created: false,
          applied: false,
        })
        reusedCount += 1
        continue
      }
      const saveData = buildSysDictionarySaveData(row)
      await saveDataAction('sys_dictionary', saveData)
      row.created = true
      row.applied = false
      createdCount += 1
    }
    syncDictionaryConfirmationsToDraft({silent: true, resetConversion: false})
    notifySystemDictionarySelectRefresh()
    refreshExistingDictionaryMatches()
    console.log('FormDesignAIDictionariesCreated', rows)
    message.success(`字典处理完成：新建 ${createdCount} 个，复用已有 ${reusedCount} 个，并已刷新字典下拉`)
  } catch (error) {
    console.warn('FormDesignAIDictionaryCreateFailed', error)
    message.error(error?.message || '创建系统字典失败，请检查编码是否重复或稍后重试')
  } finally {
    dictionarySaving.value = false
  }
}

const normalizePreviewField = (item = {}) => ({
  id: item.id,
  name: item.formItemProps?.name || item.column?.name || '',
  label: item.formItemProps?.label || item.column?.comments || '',
  type: item.type || '',
  key: item.key || '',
  showType: item.column?.showType || '',
  span: Number(item.colProps?.span || 12),
  required: item.formItemProps?.required === '1' || item.column?.isNull === '0',
  isList: item.column?.isList === '1',
  isQuery: item.column?.isQuery === '1',
})

const conversionDisplayFields = computed(() => {
  return conversionPreview.value ? conversionPreview.value.displayFormItemArr.map(normalizePreviewField) : []
})

const conversionHiddenFields = computed(() => {
  return conversionPreview.value ? conversionPreview.value.hideFormItemArr.map(normalizePreviewField) : []
})

const conversionListCount = computed(() => {
  return conversionDisplayFields.value.concat(conversionHiddenFields.value).filter(field => field.isList).length
})

const conversionQueryCount = computed(() => {
  return conversionDisplayFields.value.concat(conversionHiddenFields.value).filter(field => field.isQuery).length
})

const canApplyConversion = computed(() => {
  return Boolean(conversionPreview.value && conversionPreview.value.canApply)
})

const isIncrementalApplyMode = computed(() => applyMode.value === 'incremental')

const designerBusinessFieldCount = computed(() => {
  const names = new Set(['create_by', 'create_date', 'update_by', 'update_date', 'remarks', 'owner_code', 'del_flag', 'id'])
  return (props.displayFormItemArr || []).filter(item => {
    const name = String(item?.formItemProps?.name || item?.column?.name || '').trim().toLowerCase()
    return name && !names.has(name)
  }).length
})

const applyModeHintText = computed(() => {
  if (isIncrementalApplyMode.value) {
    return '增量补充：只追加草稿中尚不存在的业务字段；同名跳过；不修改隐藏区域与表头。'
  }
  return '全量替换：用草稿覆盖当前显示区与隐藏区字段配置（已有微调会丢失）。'
})

const incrementalAddFields = computed(() => {
  if (!conversionPreview.value || conversionPreview.value.mode !== 'incremental') {
    return []
  }
  return (conversionPreview.value.toAdd || []).map(normalizePreviewField)
})

const incrementalSkippedFields = computed(() => conversionPreview.value?.skipped || [])

const incrementalIgnoredHidden = computed(() => conversionPreview.value?.ignoredHidden || [])

const formalizePreviewColumns = computed(() => {
  return formalizePreview.value ? formalizePreview.value.fieldPreview.columns.slice(0, 16) : []
})

const formalizeSourceText = computed(() => {
  const source = formalizePreview.value?.source || {}
  if (source.type === 'server-draft') {
    return source.draftTitle ? `服务器草稿：${source.draftTitle}` : '服务器草稿'
  }
  if (source.type === 'local-draft') {
    return source.draftTitle ? `本地草稿：${source.draftTitle}` : '本地草稿'
  }
  if (source.type === 'ai-draft') {
    return '当前 AI 草稿'
  }
  if (source.type === 'form-material') {
    return '当前材料转换草稿'
  }
  return '当前设计器'
})

const localDraftCount = computed(() => localDrafts.value.length)

const remoteDraftCount = computed(() => remoteDrafts.value.length)

const saveDraftButtonText = computed(() => currentLocalDraftId.value ? '保存新版本' : '保存草稿')

const saveRemoteDraftButtonText = computed(() => currentRemoteDraftId.value ? '保存服务器新版本' : '保存服务器草稿')

const activeDemoFlowKey = computed(() => {
  if (aiMaterialRecognizing.value) {
    return 'recognize'
  }
  if (aiDraftGenerating.value) {
    return 'dsl'
  }
  if (formalizePreview.value) {
    return 'formalize'
  }
  if (currentRemoteDraftId.value && conversionApplied.value) {
    return 'formalize'
  }
  if (currentRemoteDraftId.value) {
    return 'save'
  }
  if (conversionApplied.value) {
    return 'save'
  }
  if (conversionPreview.value) {
    return 'apply'
  }
  if (draft.value) {
    return 'apply'
  }
  if (materialRecognitionResult.value) {
    return 'confirm'
  }
  if (materialSourceType.value === 'manual' && String(requirementText.value || '').trim()) {
    return 'dsl'
  }
  if (materialSourceType.value === 'table-text' && String(materialTableText.value || '').trim()) {
    return 'recognize'
  }
  if (materialSourceType.value === 'file' && materialFileList.value.length > 0) {
    return 'recognize'
  }
  return 'input'
})

const isDemoFlowStepDone = (key) => {
  if (key === 'input') {
    return Boolean(
        String(requirementText.value || '').trim()
        || String(materialTableText.value || '').trim()
        || materialFileList.value.length > 0
        || materialRecognitionResult.value
        || draft.value
    )
  }
  if (key === 'recognize') {
    return Boolean(materialRecognitionResult.value || draft.value)
  }
  if (key === 'confirm') {
    return Boolean(draft.value || (materialRecognitionResult.value && materialConfirmed.value))
  }
  if (key === 'dsl') {
    return Boolean(draft.value)
  }
  if (key === 'apply') {
    return Boolean(conversionApplied.value || currentRemoteDraftId.value || formalizePreview.value)
  }
  if (key === 'save') {
    return Boolean(currentRemoteDraftId.value)
  }
  if (key === 'formalize') {
    return Boolean(formalizePreview.value && formalizeCheckResult.value.canFormalize)
  }
  return false
}

const demoFlowStatusText = computed(() => {
  if (aiMaterialRecognizing.value) {
    return 'AI 正在识别材料，请耐心等待；图片和扫描 PDF 通常会更慢。'
  }
  if (aiDraftGenerating.value) {
    return 'AI 正在生成 DSL 草稿，完成前不会修改设计器。'
  }
  if (formalizePreview.value) {
    return formalizeCheckResult.value.canFormalize
        ? '配置完整性审查已通过；关闭抽屉后点击页面原保存，进入保存前审查和二次确认。'
        : '配置完整性审查发现错误，请先按检查结果处理后再正式保存。'
  }
  if (currentRemoteDraftId.value) {
    return formalizePreview.value
        ? '服务器草稿已保存，配置完整性审查已执行；可以关闭抽屉，回到设计器点击页面原保存。'
        : '服务器草稿已保存；建议做一次配置完整性审查，确认字段、列表、查询和来源追溯。'
  }
  if (conversionApplied.value) {
    return '已应用到设计器；当前只修改页面内存，建议保存服务器草稿用于追溯，最后关闭抽屉回到设计器保存。'
  }
  if (conversionPreview.value) {
    return canApplyConversion.value
        ? '转换预览可应用；点击应用到设计器后，再保存服务器草稿或做配置完整性审查。'
        : '转换预览存在错误，请先处理错误后再应用到设计器。'
  }
  if (draft.value) {
    return 'DSL 草稿已生成；下一步先做转换预览，再应用到设计器。'
  }
  if (materialRevisionReview.value) {
    return 'AI 二次修正已完成；请查看最近一次修正摘要，确认当前材料后再生成 DSL。'
  }
  if (materialRecognitionResult.value && !materialConfirmed.value) {
    return (materialPreviewIssueSummary.value.error || 0) > 0
        ? '材料已识别，但存在错误；请先修正错误后再确认材料。'
        : '材料已识别；请确认字段、英文名、控件类型、列表和查询建议，然后点击确认当前材料。'
  }
  if (materialRecognitionResult.value) {
    return '材料已确认；下一步可以生成 DSL 草稿。'
  }
  if (materialSourceType.value === 'manual' && String(requirementText.value || '').trim()) {
    return '已输入需求；下一步点击 AI 生成草稿。'
  }
  if (materialSourceType.value === 'table-text' && String(materialTableText.value || '').trim()) {
    return '已粘贴表格；下一步点击 AI 识别材料。'
  }
  if (materialSourceType.value === 'file' && materialFileList.value.length > 0) {
    return '已选择文件；下一步点击 AI 识别材料，图片和扫描 PDF 需要等待 OCR。'
  }
  return '先选择输入来源：手动描述直接生成草稿，表格/文件先识别材料再生成草稿。'
})

const demoFlowSteps = computed(() => {
  const fieldCount = draft.value?.summary?.fieldCount || materialPreviewFields.value.length || 0
  const steps = [
    {key: 'input', index: 1, title: '输入来源', detail: '描述 / 表格 / 文件'},
    {key: 'recognize', index: 2, title: '材料识别', detail: materialRecognitionResult.value ? `已识别 ${materialPreviewFields.value.length} 个字段` : '点击识别，需稍等'},
    {key: 'confirm', index: 3, title: '字段确认', detail: materialRecognitionResult.value ? '检查名称、控件、列表、查询' : '识别后人工确认'},
    {key: 'dsl', index: 4, title: 'DSL草稿', detail: draft.value ? `已生成 ${fieldCount} 个字段` : '生成前不会改设计器'},
    {key: 'apply', index: 5, title: '应用设计器', detail: conversionApplied.value ? '已应用到设计器' : conversionPreview.value ? (canApplyConversion.value ? '可应用到设计器' : '需处理错误') : '先预览再应用'},
    {key: 'save', index: 6, title: '服务器草稿', detail: currentRemoteDraftId.value ? '已保存可追溯' : conversionApplied.value ? '建议保存追溯' : '建议保存'},
    {key: 'formalize', index: 7, title: '配置审查', detail: formalizePreview.value ? (formalizeCheckResult.value.canFormalize ? '可正式保存' : '需处理错误') : '可选检查'},
  ]
  return steps.map(step => ({
    ...step,
    active: step.key === activeDemoFlowKey.value,
    done: isDemoFlowStepDone(step.key),
    stateText: step.key === activeDemoFlowKey.value ? '当前' : isDemoFlowStepDone(step.key) ? '完成' : '待处理',
  }))
})

const activeDemoFlowStepText = computed(() => {
  const activeStep = demoFlowSteps.value.find(step => step.active) || demoFlowSteps.value[0]
  return activeStep ? `${activeStep.index}. ${activeStep.title}` : '1. 输入来源'
})

const conversionAppliedNoticeText = computed(() => {
  const modeHint = isIncrementalApplyMode.value
    ? '已按增量补充写入画布（旧字段配置保留，隐藏区未改）。'
    : '已按全量替换写入画布。'
  if (currentRemoteDraftId.value) {
    return `${modeHint} 服务器草稿已保存，可选做配置完整性审查；正式落库仍需关闭抽屉后点击页面原保存。`
  }
  return `${modeHint} 当前只修改了设计器页面内存，建议先保存服务器草稿便于追溯；正式落库仍需关闭抽屉后点击页面原保存。`
})

const aiDraftGenerateButtonText = computed(() => {
  return aiDraftGenerating.value ? 'AI生成中，请稍候' : 'AI生成DSL草稿（需稍等）'
})

const aiDraftRegenerateButtonText = computed(() => {
  return aiDraftGenerating.value ? 'AI生成中，请稍候' : 'AI重新生成DSL（需稍等）'
})

const aiClarifiedGenerateButtonText = computed(() => {
  return aiDraftGenerating.value ? 'AI生成中，请稍候' : '按澄清生成DSL（需稍等）'
})

const aiMaterialRecognizeButtonText = computed(() => {
  return aiMaterialRecognizing.value ? 'AI识别中，请稍候' : 'AI识别材料（需稍等）'
})

const aiMaterialRevisionButtonText = computed(() => {
  return materialRevising.value ? 'AI修正中，请稍候' : 'AI按要求修正（需稍等）'
})

const aiFooterGenerateLoading = computed(() => aiDraftGenerating.value || aiMaterialRecognizing.value || materialRevising.value)

const aiFooterGenerateButtonText = computed(() => {
  if (aiDraftGenerating.value) {
    return 'AI生成中，请稍候'
  }
  if (aiMaterialRecognizing.value) {
    return 'AI识别中，请稍候'
  }
  if (materialSourceType.value === 'manual') {
    return draft.value ? aiDraftRegenerateButtonText.value : aiDraftGenerateButtonText.value
  }
  if (['table-text', 'file'].includes(materialSourceType.value) && !materialRecognitionResult.value) {
    return 'AI识别材料（需稍等）'
  }
  return draft.value ? '重新生成DSL草稿' : '生成DSL草稿'
})

const createMaterialGenerateFooterAction = () => {
  if ((materialPreviewIssueSummary.value.error || 0) > 0) {
    return {label: '材料有错误，暂不能生成', action: '', loading: false, disabled: true}
  }
  if (!materialConfirmed.value) {
    return {label: '请先确认材料', action: '', loading: false, disabled: true}
  }
  return {label: '生成DSL草稿', action: 'generate-from-material', loading: false, disabled: false}
}

const footerPrimaryAction = computed(() => {
  if (aiMaterialRecognizing.value) {
    return {
      label: 'AI识别中，请稍候',
      action: '',
      loading: true,
      disabled: true,
    }
  }
  if (materialRevising.value) {
    return {
      label: 'AI修正中，请稍候',
      action: '',
      loading: true,
      disabled: true,
    }
  }
  if (aiDraftGenerating.value) {
    return {
      label: 'AI生成中，请稍候',
      action: '',
      loading: true,
      disabled: true,
    }
  }
  if (formalizePreview.value) {
    return formalizeCheckResult.value.canFormalize
        ? {label: '关闭，回到设计器保存', action: 'close', loading: false, disabled: false}
        : {label: '需处理配置问题', action: '', loading: false, disabled: true}
  }
  if (!draft.value) {
    if (materialSourceType.value === 'table-text') {
      if (materialRecognitionResult.value) {
        return createMaterialGenerateFooterAction()
      }
      return {
        label: 'AI识别材料（需稍等）',
        action: 'recognize-material',
        loading: false,
        disabled: !String(materialTableText.value || '').trim(),
      }
    }
    if (materialSourceType.value === 'file') {
      if (materialRecognitionResult.value) {
        return createMaterialGenerateFooterAction()
      }
      return {
        label: 'AI识别材料（需稍等）',
        action: 'recognize-material',
        loading: false,
        disabled: materialFileList.value.length === 0,
      }
    }
    return {
      label: 'AI生成DSL草稿（需稍等）',
      action: 'generate-draft',
      loading: false,
      disabled: !String(requirementText.value || '').trim() || unansweredClarificationCount.value > 0,
    }
  }
  if (!conversionPreview.value) {
    return {
      label: '转换预览',
      action: 'preview-conversion',
      loading: false,
      disabled: false,
    }
  }
  if (!canApplyConversion.value) {
    return {
      label: '需处理转换问题',
      action: '',
      loading: false,
      disabled: true,
    }
  }
  if (!conversionApplied.value) {
    return {
      label: '应用到设计器',
      action: 'apply-conversion',
      loading: false,
      disabled: false,
    }
  }
  if (!currentRemoteDraftId.value) {
    return {
      label: saveRemoteDraftButtonText.value,
      action: 'save-remote-draft',
      loading: remoteSaving.value,
      disabled: remoteSaving.value,
    }
  }
  if (!formalizePreview.value) {
    return {
      label: '审查配置完整性（可选）',
      action: 'preview-formalize',
      loading: false,
      disabled: false,
    }
  }
  return {
    label: '关闭，回到设计器保存',
    action: 'close',
    loading: false,
    disabled: false,
  }
})

const runFooterPrimaryAction = async () => {
  const action = footerPrimaryAction.value.action
  if (!action) {
    return
  }
  if (action === 'recognize-material') {
    await previewMaterialRecognition()
    return
  }
  if (action === 'generate-from-material') {
    await generateDraftFromMaterial()
    return
  }
  if (action === 'generate-draft') {
    await generateDraft()
    return
  }
  if (action === 'preview-conversion') {
    previewDesignerState()
    return
  }
  if (action === 'apply-conversion') {
    applyDesignerState()
    return
  }
  if (action === 'save-remote-draft') {
    await saveCurrentDraftToRemote()
    return
  }
  if (action === 'preview-formalize') {
    previewFormalizeState()
    return
  }
  if (action === 'close') {
    close()
  }
}

const allTemplates = computed(() => {
  localTemplates.value
  return listAllTemplates()
})

const filteredTemplates = computed(() => {
  const keyword = String(templateKeyword.value || '').trim().toLowerCase()
  return allTemplates.value.filter(item => {
    if (templateSourceFilter.value !== 'all' && item.source !== templateSourceFilter.value) {
      return false
    }
    if (!keyword) {
      return true
    }
    const searchText = [
      item.title,
      item.category,
      item.description,
      item.requirement,
      ...(item.tags || []),
      ...(item.fields || []),
    ].join(' ').toLowerCase()
    return searchText.indexOf(keyword) >= 0
  })
})

const unansweredClarificationQuestions = computed(() => {
  return getUnansweredClarificationQuestions(clarificationQuestions.value, clarificationAnswers.value)
})

const unansweredClarificationCount = computed(() => unansweredClarificationQuestions.value.length)

const currentClarificationAnswerList = computed(() => {
  return getClarificationAnswerList(clarificationQuestions.value, clarificationAnswers.value)
})

const draftClarificationAnswers = computed(() => draft.value?.clarifyAnswers || [])

const selectedHistoryDraft = computed(() => {
  return localDrafts.value.find(item => item.id === selectedHistoryDraftId.value) || null
})

const versionDiff = computed(() => {
  const item = selectedHistoryDraft.value
  if (!item) {
    return createEmptyVersionDiff()
  }
  const versions = getDraftVersions(item)
  const left = versions.find(version => version.id === compareLeftVersionId.value)
  const right = versions.find(version => version.id === compareRightVersionId.value)
  return compareDraftVersionFields(left, right)
})

const draftJson = computed(() => {
  return draft.value ? JSON.stringify(draft.value.dsl, null, 2) : ''
})

const footerText = computed(() => {
  if (!draft.value) {
    return demoFlowStatusText.value
  }
  return `${demoFlowStatusText.value} 已生成 ${draft.value.summary.fieldCount} 个字段，${schemaSummary.value.error || 0} 个结构错误，${designSummary.value.total || 0} 个设计问题。`
})

const getDraftDslText = () => {
  return draft.value ? JSON.stringify(draft.value.dsl, null, 2) : ''
}

const getCurrentDraftSourceContent = () => {
  return draft.value?.sourceContent || materialPreview.value?.rawText || requirementText.value
}

const createDraftFileName = () => {
  const formName = String(draft.value?.dsl?.form?.name || 'form_design_draft')
      .replace(/[^a-zA-Z0-9_-]/g, '_')
      .replace(/_+/g, '_')
      .replace(/^_+|_+$/g, '') || 'form_design_draft'
  const timestamp = new Date().toISOString()
      .replace(/[-:]/g, '')
      .replace(/\.\d{3}Z$/, '')
      .replace('T', '_')
  return `${formName}_${timestamp}.json`
}

const fallbackCopyText = (text) => {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', 'readonly')
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  textarea.style.top = '0'
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  let copied = false
  try {
    copied = document.execCommand('copy')
  } finally {
    document.body.removeChild(textarea)
  }
  return copied
}

const copyTextToClipboard = async (text) => {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    try {
      await navigator.clipboard.writeText(text)
      return true
    } catch (error) {
      console.warn('FormDesignDraftDSL clipboard api failed', error)
    }
  }
  return fallbackCopyText(text)
}

const refreshLocalDrafts = () => {
  localDrafts.value = listLocalDrafts()
  if (selectedHistoryDraftId.value && !localDrafts.value.some(item => item.id === selectedHistoryDraftId.value)) {
    selectedHistoryDraftId.value = ''
    compareLeftVersionId.value = ''
    compareRightVersionId.value = ''
  }
}

const refreshLocalTemplates = () => {
  localTemplates.value = listLocalTemplates()
}

const buildRemoteDraftQuery = () => {
  const keyword = String(remoteDraftKeyword.value || '').trim()
  return {
    title: keyword,
    pageParam: {
      pageNo: 1,
      pageSize: 50,
      orderBy: 'update_date desc',
    },
  }
}

const refreshRemoteDrafts = async () => {
  remoteLoading.value = true
  try {
    const result = await listRemoteDrafts(buildRemoteDraftQuery())
    remoteDrafts.value = result.rows || []
    if (currentRemoteDraftId.value && !remoteDrafts.value.some(item => item.id === currentRemoteDraftId.value)) {
      currentRemoteDraftId.value = ''
      currentRemoteVersionId.value = ''
    }
  } catch (error) {
    console.warn('FormDesignRemoteDraftListFailed', error)
    message.error(error?.message || '服务器草稿加载失败')
  } finally {
    remoteLoading.value = false
  }
}

watch(() => props.visible, (visible) => {
  if (visible) {
    refreshLocalDrafts()
    refreshLocalTemplates()
    refreshRemoteDrafts()
    applyMode.value = resolveDefaultApplyMode({
      displayFormItemArr: props.displayFormItemArr,
      hideFormItemArr: props.hideFormItemArr,
    })
    conversionPreview.value = null
    conversionApplied.value = false
  }
  if (visible && !requirementText.value) {
    requirementText.value = sampleMap.receive
  }
})

watch(materialTableText, () => {
  materialRecognitionResult.value = null
  clearMaterialRevisionReview()
  clearMaterialConfirmation()
})

watch(materialRecognitionResult, () => {
  resetMaterialConfirmFilter()
})

const close = () => {
  emit('update:visible', false)
}

const resetFormalizePreview = () => {
  formalizePreview.value = null
  formalizeCheckResult.value = createEmptyFormalizeCheckResult()
}

const switchToManualSource = () => {
  materialSourceType.value = 'manual'
}

const handleMaterialFileBeforeUpload = (file = {}) => {
  materialRecognitionResult.value = null
  clearMaterialRevisionReview()
  clearMaterialConfirmation()
  materialFileList.value = [{
    uid: file.uid || `material-file-${Date.now()}`,
    name: file.name || '未命名文件',
    status: 'done',
    size: file.size || 0,
    type: file.type || '',
    originFileObj: file,
  }]
  if (isSupportedMaterialFile(file)) {
    message.info(`已选择 ${getMaterialFileKindText(file)} 文件，可点击识别材料`)
  } else {
    message.warning('当前 MVP 支持 .xlsx / .xls / .docx / .pdf / .png / .jpg / .webp 文件')
  }
  return false
}

const removeMaterialFile = () => {
  materialFileList.value = []
  materialRecognitionResult.value = null
  clearMaterialRevisionReview()
  clearMaterialConfirmation()
  return true
}

const previewMaterialRecognition = async () => {
  if (materialSourceType.value === 'table-text') {
    await recognizeTableTextMaterial()
    return
  }
  if (materialSourceType.value === 'file' && materialFileList.value.length === 0) {
    message.warning('请先选择文件')
    return
  }
  if (materialSourceType.value === 'file') {
    await recognizeFileMaterial()
    return
  }
  const sourceText = materialSourceTextMap[materialSourceType.value] || '当前材料'
  message.info(`${sourceText}入口已就绪，材料识别将在后续任务接入`)
}

const recognizeTableTextMaterial = async () => {
  const text = String(materialTableText.value || '').trim()
  if (!text) {
    message.warning('请先粘贴表格文本')
    return null
  }
  let result
  aiMaterialRecognizing.value = true
  clearMaterialRevisionReview()
  clearMaterialConfirmation()
  try {
    result = await recognizeRemoteFormMaterial(text, {
      sourceType: 'table-text',
      scene: draftMode.value === 'auto' ? 'auto' : draftMode.value,
      target: buildDraftStorageTarget(),
    })
  } catch (error) {
    console.warn('FormMaterialRemoteAIRecognizeFailed', error)
    aiLastError.value = createAiErrorDiagnostic(error, '材料识别', 'AI 材料识别失败')
    message.error(formatAiErrorMessage(error, 'AI 材料识别失败'))
    return null
  } finally {
    aiMaterialRecognizing.value = false
  }
  aiLastError.value = null
  toArray(result.material?.fields).forEach(field => normalizeMaterialFieldSuggestion(field))
  materialRecognitionResult.value = result
  console.log('FormMaterial', result.material)
  console.log('FormMaterialIssues', materialPreviewIssues.value)
  if ((result.summary?.error || 0) > 0) {
    message.error(`材料识别完成，但有 ${result.summary.error} 个错误需要处理`)
  } else if ((result.summary?.warning || 0) > 0) {
    message.warning(`材料识别完成，发现 ${result.summary.warning} 个警告`)
  } else {
    const elapsedText = result.elapsedMs ? `，耗时 ${(result.elapsedMs / 1000).toFixed(1)} 秒` : ''
    message.success(`AI 已识别 ${result.material.fields.length} 个字段${elapsedText}`)
  }
  return result
}

const recognizeFileMaterial = async () => {
  const file = getMaterialRawFile()
  if (!file) {
    message.warning('请先选择文件')
    return null
  }
  const sourceType = getMaterialFileSourceType(file)
  if (!sourceType) {
    message.warning('当前 MVP 支持 .xlsx / .xls / .docx / .pdf / .png / .jpg / .webp 文件')
    return null
  }
  const fileKindText = getMaterialFileKindText(file)
  let result
  aiMaterialRecognizing.value = true
  clearMaterialRevisionReview()
  clearMaterialConfirmation()
  try {
    if (sourceType === 'excel') {
      result = await recognizeExcelFormMaterial(file, {
        sourceType: 'excel',
        scene: draftMode.value === 'auto' ? 'auto' : draftMode.value,
        target: buildDraftStorageTarget(),
        maxRows: 80,
        maxColumns: 60,
      })
    } else {
      result = await recognizeFileFormMaterial(file, {
        sourceType,
        scene: draftMode.value === 'auto' ? 'auto' : draftMode.value,
        target: buildDraftStorageTarget(),
        maxChars: 16000,
        maxPages: 3,
        ocrProvider: 'auto',
        ocrLanguage: 'chi_sim+eng',
        extractTables: true,
        extractHeaders: true,
      })
    }
  } catch (error) {
    console.warn('FormMaterialFileRecognizeFailed', error)
    aiLastError.value = createAiErrorDiagnostic(error, `${fileKindText} 材料识别`, `${fileKindText} 材料识别失败`)
    message.error(formatAiErrorMessage(error, `${fileKindText} 材料识别失败`))
    return null
  } finally {
    aiMaterialRecognizing.value = false
  }
  aiLastError.value = null
  toArray(result.material?.fields).forEach(field => normalizeMaterialFieldSuggestion(field))
  materialRecognitionResult.value = result
  console.log('FormMaterial', result.material)
  console.log('FormMaterialIssues', materialPreviewIssues.value)
  if ((result.summary?.error || 0) > 0) {
    message.error(`${fileKindText} 材料识别完成，但有 ${result.summary.error} 个错误需要处理`)
  } else if ((result.summary?.warning || 0) > 0) {
    message.warning(`${fileKindText} 材料识别完成，发现 ${result.summary.warning} 个警告`)
  } else {
    const elapsedText = result.elapsedMs ? `，耗时 ${(result.elapsedMs / 1000).toFixed(1)} 秒` : ''
    message.success(`AI 已识别 ${fileKindText} 中的 ${result.material.fields.length} 个字段${elapsedText}`)
  }
  return result
}

const normalizeAllMaterialFields = () => {
  if (!materialPreview.value) {
    message.warning('请先识别材料')
    return
  }
  materialPreviewFields.value.forEach(field => normalizeMaterialFieldSuggestion(field))
  clearMaterialConfirmation()
  console.log('FormMaterialNormalized', materialPreview.value)
  message.success('已按本地规则补齐字段建议')
}

const appendMaterialRevisionInstruction = (text = '') => {
  const current = normalizeText(materialRevisionInstruction.value)
  const next = normalizeText(text)
  if (!next) {
    return
  }
  materialRevisionInstruction.value = current ? `${current}\n${next}` : next
}

const resetMaterialDerivedDraftState = () => {
  draft.value = null
  resetDictionaryConfirmations()
  conversionPreview.value = null
  conversionApplied.value = false
  currentLocalDraftId.value = ''
  currentLocalVersionId.value = ''
  currentRemoteDraftId.value = ''
  currentRemoteVersionId.value = ''
  resetFormalizePreview()
}

const reviseMaterialByInstruction = async () => {
  if (!materialPreview.value) {
    message.warning('请先识别材料')
    return
  }
  const instruction = normalizeText(materialRevisionInstruction.value)
  if (!instruction) {
    message.warning('请先输入修正要求')
    return
  }
  const beforeResult = clonePlain(materialRecognitionResult.value)
  materialRevising.value = true
  let result = null
  try {
    result = await reviseRemoteFormMaterial(materialPreview.value, instruction, {
      scene: materialPreview.value.scene || (draftMode.value === 'auto' ? 'auto' : draftMode.value),
      title: materialPreview.value.title || '',
      target: buildDraftStorageTarget(),
      sourceContent: getCurrentDraftSourceContent(),
      temperature: 0.1,
    })
  } catch (error) {
    console.warn('FormMaterialReviseFailed', error)
    aiLastError.value = createAiErrorDiagnostic(error, '材料二次修正', '材料二次修正失败')
    message.error(formatAiErrorMessage(error, '材料二次修正失败'))
    return
  } finally {
    materialRevising.value = false
  }

  aiLastError.value = null
  materialRecognitionResult.value = result
  materialRevisionReview.value = createMaterialRevisionReview(beforeResult, result, instruction)
  clearMaterialConfirmation()
  materialRevisionInstruction.value = ''
  resetMaterialDerivedDraftState()
  console.log('FormMaterialRevised', result.material)
  console.log('FormMaterialIssues', materialPreviewIssues.value)
  const elapsedText = result.elapsedMs ? `，耗时 ${(result.elapsedMs / 1000).toFixed(1)} 秒` : ''
  if ((result.summary?.error || 0) > 0) {
    message.error(`AI 二次修正完成，但有 ${result.summary.error} 个错误需要处理${elapsedText}`)
  } else if ((result.summary?.warning || 0) > 0) {
    message.warning(`AI 二次修正完成，发现 ${result.summary.warning} 个警告${elapsedText}`)
  } else {
    message.success(`AI 二次修正完成，已更新 ${result.material.fields.length} 个字段${elapsedText}`)
  }
}

const useSample = (type) => {
  switchToManualSource()
  requirementText.value = sampleMap[type] || ''
  currentLocalDraftId.value = ''
  currentLocalVersionId.value = ''
  currentRemoteDraftId.value = ''
  currentRemoteVersionId.value = ''
  conversionPreview.value = null
  resetFormalizePreview()
  clearClarification()
}

const runRequirementClarification = () => {
  const text = String(requirementText.value || '').trim()
  if (!text) {
    message.warning('请先输入表单需求')
    return
  }
  const questions = createClarificationQuestions(text, {mode: draftMode.value})
  clarificationQuestions.value = questions
  const defaultAnswers = createDefaultClarificationAnswers(questions)
  clarificationAnswers.value = questions.reduce((answers, question) => {
    answers[question.id] = clarificationAnswers.value[question.id] || defaultAnswers[question.id] || ''
    return answers
  }, {})
  if (questions.length === 0) {
    message.success('当前需求已经比较清晰，可以直接生成草稿')
  } else {
    message.info(`已生成 ${questions.length} 个澄清问题`)
  }
}

const clearClarification = () => {
  clarificationQuestions.value = []
  clarificationAnswers.value = {}
}

const openTemplateLibrary = () => {
  refreshLocalTemplates()
  templateLibraryVisible.value = true
}

const resetTemplateDraftContext = () => {
  draft.value = null
  resetDictionaryConfirmations()
  conversionPreview.value = null
  resetFormalizePreview()
  currentLocalDraftId.value = ''
  currentLocalVersionId.value = ''
  currentRemoteDraftId.value = ''
  currentRemoteVersionId.value = ''
  clearClarification()
}

const useTemplate = (item = {}) => {
  switchToManualSource()
  requirementText.value = item.requirement || ''
  draftMode.value = item.style || 'auto'
  resetTemplateDraftContext()
  templateLibraryVisible.value = false
  message.success('已填入模板需求，可继续调整后生成草稿')
}

const generateDraftFromTemplate = (item = {}) => {
  switchToManualSource()
  requirementText.value = item.requirement || ''
  draftMode.value = item.style || 'auto'
  clearClarification()
  currentLocalDraftId.value = ''
  currentLocalVersionId.value = ''
  conversionPreview.value = null
  resetFormalizePreview()

  const templateDraft = createRuntimeDraftFromTemplate(item)
  if (templateDraft) {
    draft.value = refreshDraftCheckResult(templateDraft)
    resetDictionaryConfirmations(draft.value)
    templateLibraryVisible.value = false
    console.log('FormDesignTemplateDraftLoaded', item)
    message.success('已从模板生成草稿')
    return
  }

  templateLibraryVisible.value = false
  generateDraft()
}

const saveCurrentDraftAsTemplate = () => {
  if (!draft.value) {
    message.warning('请先生成草稿')
    return
  }
  try {
    syncDictionaryConfirmationsToDraft({silent: true, resetConversion: false})
    const result = saveLocalTemplateFromDraft(draft.value, {
      requirement: getCurrentDraftSourceContent(),
    })
    refreshLocalTemplates()
    console.log('FormDesignLocalTemplateSaved', result.template)
    message.success('已保存为本地模板，只保存在当前浏览器')
  } catch (error) {
    console.warn('FormDesignLocalTemplateSaveFailed', error)
    message.error(error?.message || '保存模板失败')
  }
}

const removeLocalTemplate = (item = {}) => {
  Modal.confirm({
    title: '删除本地模板？',
    content: '只会删除当前浏览器中的模板，不会影响草稿、正式表单或数据库。',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk() {
      deleteLocalTemplate(item.id)
      refreshLocalTemplates()
      console.log('FormDesignLocalTemplateDeleted', item)
      message.success('本地模板已删除')
    },
  })
}

const getTemplateStyleText = (item = {}) => layoutTextMap[item.style] || item.style || '-'

const getTemplateFieldTotal = (item = {}) => getTemplateFieldCount(item)

const getTemplatePreviewFields = (item = {}) => {
  return (item.fields || []).slice(0, 12)
}

const getTemplateUpdatedText = (item = {}) => {
  if (!item.updatedAt) {
    return '-'
  }
  return item.updatedAt.replace('T', ' ').replace(/\.\d{3}Z$/, '')
}

const buildDraftStorageTarget = () => ({
  mode: props.formModel?.id ? 'optimize' : 'create',
  genTableId: props.formModel?.id || '',
  formName: draft.value?.dsl?.form?.name || props.formModel?.name || '',
  module: draft.value?.dsl?.form?.module || props.formModel?.module || '',
})

const saveCurrentDraft = () => {
  if (!draft.value) {
    message.warning('请先生成草稿')
    return
  }
  try {
    syncDictionaryConfirmationsToDraft({silent: true, resetConversion: false})
    const result = saveLocalDraft(draft.value, {
      draftId: currentLocalDraftId.value,
      target: buildDraftStorageTarget(),
      mode: draftMode.value,
      sourceContent: getCurrentDraftSourceContent(),
      parentVersionId: currentLocalVersionId.value,
      clarifyAnswers: currentClarificationAnswerList.value,
      previewSummary: conversionPreview.value?.summary || {},
    })
    currentLocalDraftId.value = result.draft.id
    currentLocalVersionId.value = result.version.id
    refreshLocalDrafts()
    selectedHistoryDraftId.value = result.draft.id
    const versions = getDraftVersions(result.draft)
    const parentVersion = versions.find(version => version.id === result.version.parentVersionId)
        || versions[versions.length - 2]
        || result.version
    compareLeftVersionId.value = parentVersion?.id || ''
    compareRightVersionId.value = result.version.id
    console.log('FormDesignLocalDraftSaved', result.draft)
    if (result.created) {
      message.success('草稿已保存到当前浏览器，不会保存数据库')
    } else {
      message.success(`已保存为新版本 ${result.version.name}，不会保存数据库`)
    }
  } catch (error) {
    console.warn('FormDesignLocalDraftSaveFailed', error)
    message.error(error?.message || '保存草稿失败')
  }
}

const saveCurrentDraftToRemote = async () => {
  if (!draft.value) {
    message.warning('请先生成草稿')
    return
  }
  remoteSaving.value = true
  try {
    syncDictionaryConfirmationsToDraft({silent: true, resetConversion: false})
    const result = await saveRemoteDraft(draft.value, {
      draftId: currentRemoteDraftId.value,
      target: buildDraftStorageTarget(),
      mode: draftMode.value,
      sourceContent: getCurrentDraftSourceContent(),
      parentVersionId: currentRemoteVersionId.value,
      clarifyAnswers: currentClarificationAnswerList.value,
      previewSummary: conversionPreview.value?.summary || {},
    })
    currentRemoteDraftId.value = result.draft.id
    currentRemoteVersionId.value = result.version.id
    await refreshRemoteDrafts()
    console.log('FormDesignRemoteDraftSaved', result.draft)
    if (result.created) {
      message.success('草稿已保存到服务器')
    } else {
      message.success(`已保存为服务器新版本 ${result.version.name || ''}`)
    }
  } catch (error) {
    console.warn('FormDesignRemoteDraftSaveFailed', error)
    message.error(error?.message || '保存到服务器失败，本地草稿不受影响')
  } finally {
    remoteSaving.value = false
  }
}

const openDraftHistory = () => {
  refreshLocalDrafts()
  if (!selectedHistoryDraftId.value && localDrafts.value.length > 0) {
    showDraftVersions(localDrafts.value[0])
  }
  draftHistoryVisible.value = true
}

const openRemoteDraftHistory = async () => {
  remoteDraftHistoryVisible.value = true
  await refreshRemoteDrafts()
}

const loadLocalDraft = (item, versionId = '') => {
  try {
    switchToManualSource()
    const restoredDraft = createRuntimeDraftFromLocalDraft(item, versionId)
    draft.value = refreshDraftCheckResult(restoredDraft)
    resetDictionaryConfirmations(draft.value)
    requirementText.value = restoredDraft.sourceContent || item.source?.content || requirementText.value
    draftMode.value = item.source?.mode || 'auto'
    clarificationQuestions.value = []
    clarificationAnswers.value = {}
    conversionPreview.value = null
    resetFormalizePreview()
    currentLocalDraftId.value = item.id
    currentLocalVersionId.value = versionId || getCurrentDraftVersion(item)?.id || ''
    currentRemoteDraftId.value = ''
    currentRemoteVersionId.value = ''
    draftHistoryVisible.value = false
    console.log('FormDesignLocalDraftLoaded', item)
    message.success('草稿已从当前浏览器打开，可重新体检、预览并应用')
  } catch (error) {
    console.warn('FormDesignLocalDraftLoadFailed', error)
    message.error(error?.message || '打开草稿失败')
  }
}

const loadRemoteDraft = async (item = {}, versionId = '') => {
  remoteLoadingDraftId.value = item.id
  try {
    switchToManualSource()
    const remoteDraft = await getRemoteDraft(item.id)
    const restoredDraft = createRuntimeDraftFromRemoteDraft(remoteDraft, versionId)
    draft.value = refreshDraftCheckResult(restoredDraft)
    resetDictionaryConfirmations(draft.value)
    requirementText.value = restoredDraft.sourceContent || remoteDraft.source?.content || requirementText.value
    draftMode.value = remoteDraft.source?.mode || 'auto'
    clarificationQuestions.value = []
    clarificationAnswers.value = {}
    conversionPreview.value = null
    resetFormalizePreview()
    currentLocalDraftId.value = ''
    currentLocalVersionId.value = ''
    currentRemoteDraftId.value = remoteDraft.id
    currentRemoteVersionId.value = versionId || remoteDraft.currentVersionId || remoteDraft.currentVersion?.id || ''
    remoteDraftHistoryVisible.value = false
    console.log('FormDesignRemoteDraftLoaded', remoteDraft)
    message.success('服务器草稿已打开，可继续体检、预览并应用')
  } catch (error) {
    console.warn('FormDesignRemoteDraftLoadFailed', error)
    message.error(error?.message || '打开服务器草稿失败')
  } finally {
    remoteLoadingDraftId.value = ''
  }
}

const removeLocalDraft = (item) => {
  Modal.confirm({
    title: '删除本地草稿？',
    content: '只会删除当前浏览器中的草稿，不会影响数据库或正式表单。',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk() {
      deleteLocalDraft(item.id)
      if (currentLocalDraftId.value === item.id) {
        currentLocalDraftId.value = ''
        currentLocalVersionId.value = ''
      }
      if (selectedHistoryDraftId.value === item.id) {
        selectedHistoryDraftId.value = ''
        compareLeftVersionId.value = ''
        compareRightVersionId.value = ''
      }
      refreshLocalDrafts()
      console.log('FormDesignLocalDraftDeleted', item)
      message.success('本地草稿已删除')
    },
  })
}

const importLocalDraft = (item = {}) => {
  Modal.confirm({
    title: '导入服务器？',
    content: '会将这个本地草稿及其版本复制到服务器。本地草稿会继续保留。',
    okText: '导入',
    cancelText: '取消',
    async onOk() {
      localDraftImportingId.value = item.id
      try {
        const remoteDraft = await importLocalDraftToRemote(item)
        currentRemoteDraftId.value = remoteDraft.id
        currentRemoteVersionId.value = remoteDraft.currentVersionId || remoteDraft.currentVersion?.id || ''
        await refreshRemoteDrafts()
        console.log('FormDesignLocalDraftImportedToRemote', {
          localDraft: item,
          remoteDraft,
        })
        message.success('本地草稿已导入服务器，本地副本仍然保留')
      } catch (error) {
        console.warn('FormDesignLocalDraftImportFailed', error)
        message.error(error?.message || '导入服务器失败，本地草稿不受影响')
      } finally {
        localDraftImportingId.value = ''
      }
    },
  })
}

const removeRemoteDraft = (item = {}) => {
  Modal.confirm({
    title: '删除服务器草稿？',
    content: '会删除服务器中的草稿和版本记录，不会影响已经正式保存的表单。',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      remoteDeletingDraftId.value = item.id
      try {
        await deleteRemoteDraft(item.id)
        if (currentRemoteDraftId.value === item.id) {
          currentRemoteDraftId.value = ''
          currentRemoteVersionId.value = ''
        }
        await refreshRemoteDrafts()
        console.log('FormDesignRemoteDraftDeleted', item)
        message.success('服务器草稿已删除')
      } catch (error) {
        console.warn('FormDesignRemoteDraftDeleteFailed', error)
        message.error(error?.message || '删除服务器草稿失败')
      } finally {
        remoteDeletingDraftId.value = ''
      }
    },
  })
}

const showDraftVersions = (item = {}) => {
  selectedHistoryDraftId.value = item.id
  const versions = getDraftVersions(item)
  const latest = getCurrentDraftVersion(item) || versions[versions.length - 1]
  const previous = versions.length > 1 ? versions[versions.length - 2] : latest
  compareLeftVersionId.value = previous?.id || ''
  compareRightVersionId.value = latest?.id || ''
}

const getDraftVersions = (item = {}) => {
  return Array.isArray(item.versions) ? item.versions.slice() : []
}

const getDraftVersionLabel = (item = {}) => {
  const currentVersion = getCurrentDraftVersion(item)
  return `${currentVersion?.name || `V${item.versionCount || 1}`} / 共 ${getDraftVersions(item).length || 1} 版`
}

const getRemoteDraftVersionLabel = (item = {}) => {
  return `共 ${item.versionCount || getDraftVersions(item).length || 0} 个版本`
}

const getDraftFieldCount = (item = {}) => {
  const currentVersion = getCurrentDraftVersion(item)
  return Array.isArray(currentVersion?.dsl?.fields) ? currentVersion.dsl.fields.length : 0
}

const getDraftUpdatedText = (item = {}) => {
  if (!item.updatedAt) {
    return '-'
  }
  return item.updatedAt.replace('T', ' ').replace(/\.\d{3}Z$/, '')
}

const getDraftTargetText = (item = {}) => {
  const target = item.target || {}
  const formName = target.formName || '-'
  const moduleName = target.module ? ` / ${target.module}` : ''
  return `${target.mode === 'optimize' ? '优化' : '新建'}：${formName}${moduleName}`
}

const getDraftSourcePreview = (item = {}) => {
  const text = String(item.source?.content || '')
  return text.length > 90 ? `${text.slice(0, 90)}...` : text || '无来源描述'
}

const getVersionFieldCount = (version = {}) => {
  return Array.isArray(version.dsl?.fields) ? version.dsl.fields.length : 0
}

const getVersionListCount = (version = {}) => {
  return Array.isArray(version.dsl?.fields) ? version.dsl.fields.filter(field => field.isList).length : 0
}

const getVersionQueryCount = (version = {}) => {
  return Array.isArray(version.dsl?.fields) ? version.dsl.fields.filter(field => field.isQuery).length : 0
}

const getVersionCreatedText = (version = {}) => {
  if (!version.createdAt) {
    return '-'
  }
  return version.createdAt.replace('T', ' ').replace(/\.\d{3}Z$/, '')
}

const createEmptyVersionDiff = () => ({
  added: [],
  removed: [],
  changed: [],
})

const normalizeDiffField = (field = {}) => ({
  name: String(field.name || ''),
  label: String(field.label || ''),
  type: String(field.type || ''),
  span: Number(field.span || 12),
  required: Boolean(field.required),
  isList: Boolean(field.isList),
  isQuery: Boolean(field.isQuery),
})

const indexFieldsByName = (fields = []) => {
  const map = {}
  fields.forEach(field => {
    const normalized = normalizeDiffField(field)
    if (normalized.name) {
      map[normalized.name] = normalized
    }
  })
  return map
}

const compareField = (left = {}, right = {}) => {
  const changes = []
  if (left.label !== right.label) changes.push('标题')
  if (left.type !== right.type) changes.push('类型')
  if (left.span !== right.span) changes.push('布局')
  if (left.required !== right.required) changes.push('必填')
  if (left.isList !== right.isList) changes.push('列表')
  if (left.isQuery !== right.isQuery) changes.push('查询')
  return changes
}

const compareDraftVersionFields = (leftVersion = {}, rightVersion = {}) => {
  if (!leftVersion || !rightVersion) {
    return createEmptyVersionDiff()
  }
  const leftMap = indexFieldsByName(leftVersion.dsl?.fields || [])
  const rightMap = indexFieldsByName(rightVersion.dsl?.fields || [])
  const added = []
  const removed = []
  const changed = []

  Object.keys(rightMap).forEach(name => {
    if (!leftMap[name]) {
      added.push(rightMap[name])
      return
    }
    const changes = compareField(leftMap[name], rightMap[name])
    if (changes.length > 0) {
      changed.push({
        ...rightMap[name],
        changes,
      })
    }
  })

  Object.keys(leftMap).forEach(name => {
    if (!rightMap[name]) {
      removed.push(leftMap[name])
    }
  })

  return {
    added,
    removed,
    changed,
  }
}

const enrichGeneratedDraft = (generatedDraft) => {
  const designIssues = explainFormDesignIssues(checkFormDesignDsl(generatedDraft.dsl), generatedDraft.dsl)
  generatedDraft.designIssues = designIssues
  generatedDraft.designSummary = summarizeDesignIssues(designIssues)
  generatedDraft.clarifyAnswers = currentClarificationAnswerList.value
  return {
    generatedDraft,
    designIssues,
  }
}

const generateLocalDraftWithChecks = (text, options) => {
  const generatedDraft = generateFormDesignDslDraft(text, options)
  return enrichGeneratedDraft(generatedDraft)
}

const generateRemoteDraftWithChecks = async (text, options) => {
  const generatedDraft = await generateRemoteFormDesignDslDraft(text, {
    ...options,
    target: buildDraftStorageTarget(),
    fallbackToLocal: true,
    clarifyAnswers: currentClarificationAnswerList.value,
  })
  return enrichGeneratedDraft(generatedDraft)
}

const applyGeneratedDraft = (generatedDraft, designIssues, options = {}) => {
  draft.value = refreshDraftCheckResult(generatedDraft)
  resetDictionaryConfirmations(draft.value)
  conversionPreview.value = null
  resetFormalizePreview()
  currentLocalDraftId.value = ''
  currentLocalVersionId.value = ''
  currentRemoteDraftId.value = ''
  currentRemoteVersionId.value = ''
  console.log('FormDesignDraftDSL', draft.value)
  console.log('FormDesignDraftCheckIssues', designIssues)
  if (options.successMessage) {
    message.success(options.successMessage)
  }
}

const generateDraftFromMaterial = async () => {
  if (!['table-text', 'file'].includes(materialSourceType.value)) {
    await previewMaterialRecognition()
    return
  }
  const result = materialRecognitionResult.value || (
    materialSourceType.value === 'file'
        ? await recognizeFileMaterial()
        : await recognizeTableTextMaterial()
  )
  const material = materialPreview.value || result?.material
  if (!material) {
    return
  }
  if ((materialPreviewIssueSummary.value.error || 0) > 0) {
    message.error('材料存在错误，暂不能生成 DSL 草稿')
    return
  }
  if (!materialConfirmed.value) {
    message.warning('请先确认当前材料识别结果')
    return
  }
  material.fields = toArray(material.fields).map(field => {
    normalizeMaterialFieldSuggestion(field)
    return field
  })
  const draftResult = convertFormMaterialToDslDraft(material, {
    form: {
      module: props.formModel?.module || 'oas',
    },
  })
  const {generatedDraft, designIssues} = enrichGeneratedDraft(draftResult)
  applyGeneratedDraft(generatedDraft, designIssues, {
    successMessage: `已从材料生成 ${generatedDraft.summary.fieldCount} 个字段的 DSL 草稿`,
  })
}

const generateDraft = async () => {
  if (materialSourceType.value !== 'manual') {
    if (['table-text', 'file'].includes(materialSourceType.value)) {
      if (!materialRecognitionResult.value) {
        await previewMaterialRecognition()
        return
      }
      await generateDraftFromMaterial()
    } else {
      await previewMaterialRecognition()
    }
    return
  }
  const text = String(requirementText.value || '').trim()
  if (!text) {
    message.warning('请先输入表单需求')
    return
  }
  if (unansweredClarificationCount.value > 0) {
    message.warning(`还有 ${unansweredClarificationCount.value} 个必填澄清问题未补充`)
    return
  }
  const clarifiedText = buildClarifiedRequirementText(text, clarificationQuestions.value, clarificationAnswers.value)
  const options = getClarifiedGenerationOptions(draftMode.value, clarificationQuestions.value, clarificationAnswers.value)
  let result
  let usedFallback = false
  aiDraftGenerating.value = true
  try {
    result = await generateRemoteDraftWithChecks(clarifiedText, options)
    aiLastError.value = null
  } catch (error) {
    usedFallback = true
    console.warn('FormDesignRemoteAIDraftGenerateFailed', error)
    aiLastError.value = createAiErrorDiagnostic(error, 'DSL 草稿生成', '远程 AI 生成 DSL 失败')
    result = generateLocalDraftWithChecks(clarifiedText, options)
    result.generatedDraft.remoteError = {
      code: error?.code || '',
      requestId: error?.requestId || error?.result?.requestId || '',
      message: error?.message || '',
      result: error?.result || null,
      schemaIssues: error?.schemaIssues || [],
    }
  } finally {
    aiDraftGenerating.value = false
  }
  const {generatedDraft, designIssues} = result
  applyGeneratedDraft(generatedDraft, designIssues)
  if (usedFallback) {
    console.warn('FormDesignRemoteAIFallback', draft.value.remoteError)
    message.warning(`${formatAiErrorMessage(draft.value.remoteError, '远程 AI 暂不可用')}，已使用本地生成器生成草稿`)
  } else {
    console.info('FormDesignRemoteAISuccess', {
      requestId: draft.value.requestId || draft.value.remoteResult?.requestId || '',
      model: draft.value.model || '',
      promptVersion: draft.value.promptVersion || '',
      elapsedMs: draft.value.elapsedMs || draft.value.remoteResult?.elapsedMs || 0,
      fieldCount: draft.value.summary?.fieldCount || 0,
      schemaSummary: draft.value.summary?.schema || {},
      designSummary: draft.value.designSummary || {},
    })
    message.success('真实 AI 草稿已生成')
  }
}

const buildConversionContext = () => ({
  table: props.table,
  formModel: props.formModel,
  formPropsModel: props.formPropsModel,
  extendJsConfig: props.extendJsConfig,
  displayFormItemArr: props.displayFormItemArr,
  hideFormItemArr: props.hideFormItemArr,
  fixedArr: props.fixedArr,
})

const clonePlain = (value) => {
  if (value === undefined || value === null) {
    return value
  }
  try {
    return JSON.parse(JSON.stringify(value))
  } catch (error) {
    return value
  }
}

const buildFormalizeContext = () => {
  if (!conversionPreview.value) {
    return buildConversionContext()
  }
  return {
    table: props.table,
    formModel: {
      ...clonePlain(props.formModel),
      ...(conversionPreview.value.formPatch || {}),
    },
    formPropsModel: {
      ...clonePlain(props.formPropsModel),
      ...(conversionPreview.value.formPropsPatch || {}),
    },
    extendJsConfig: props.extendJsConfig,
    displayFormItemArr: conversionPreview.value.displayFormItemArr || [],
    hideFormItemArr: conversionPreview.value.hideFormItemArr || [],
    fixedArr: props.fixedArr,
  }
}

const getCurrentDraftVersionId = () => {
  return currentRemoteVersionId.value || currentLocalVersionId.value || ''
}

const buildFormalizeSource = () => {
  if (currentRemoteDraftId.value) {
    return {
      type: 'server-draft',
      draftId: currentRemoteDraftId.value,
      versionId: getCurrentDraftVersionId(),
      draftTitle: draft.value?.title || draft.value?.dsl?.form?.title || '',
    }
  }
  if (currentLocalDraftId.value) {
    return {
      type: 'local-draft',
      draftId: currentLocalDraftId.value,
      versionId: getCurrentDraftVersionId(),
      draftTitle: draft.value?.title || draft.value?.dsl?.form?.title || '',
    }
  }
  if (draft.value) {
    return {
      type: draft.value.sourceType === 'form-material' ? 'form-material' : 'ai-draft',
      draftTitle: draft.value?.title || draft.value?.dsl?.form?.title || '',
    }
  }
  return {
    type: 'current-designer',
  }
}

const buildApplyDraftContext = () => {
  const dslForm = draft.value?.dsl?.form || {}
  const dslFields = Array.isArray(draft.value?.dsl?.fields) ? draft.value.dsl.fields : []
  return {
    source: buildFormalizeSource(),
    draft: {
      title: draft.value?.title || dslForm.title || '',
      formName: dslForm.name || '',
      module: dslForm.module || '',
      fieldCount: draft.value?.summary?.fieldCount || dslFields.length || 0,
    },
    formalizePreview: formalizePreview.value ? {
      checksum: formalizePreview.value.checksum,
      summary: formalizePreview.value.summary,
      target: formalizePreview.value.target,
    } : null,
    conversionSummary: conversionPreview.value?.summary || {},
    appliedAt: new Date().toISOString(),
  }
}

const previewFormalizeState = () => {
  try {
    const preview = buildFormalizePreview(buildFormalizeContext(), {
      source: buildFormalizeSource(),
    })
    const checkResult = buildFormalizeCheckResult(preview, {
      requireAiSource: Boolean(draft.value),
      requireRemoteSource: Boolean(currentRemoteDraftId.value),
    })
    const checkedPreview = mergeFormalizeIssuesToPreview(preview, checkResult.issues)
    formalizePreview.value = checkedPreview
    formalizeCheckResult.value = {
      ...checkResult,
      preview: checkedPreview,
    }
    console.log('FormDesignFormalizePreview', checkedPreview)
    console.log('FormDesignFormalizeCheckResult', formalizeCheckResult.value)

    if (checkResult.errors.length > 0) {
      message.warning(`配置完整性审查完成，发现 ${checkResult.errors.length} 个错误，暂不能正式保存`)
    } else if (checkResult.warnings.length > 0) {
      message.warning(`配置完整性审查完成，发现 ${checkResult.warnings.length} 个警告`)
    } else {
      message.success('配置完整性审查完成')
    }
  } catch (error) {
    console.error('FormDesignFormalizePreviewFailed', error)
    message.error(error?.message || '配置完整性审查失败，请打开控制台查看详情')
  }
}

watch(applyMode, () => {
  conversionPreview.value = null
  conversionApplied.value = false
  resetFormalizePreview()
})

const previewDesignerState = () => {
  if (!draft.value) {
    message.warning('请先生成草稿')
    return
  }
  try {
    syncDictionaryConfirmationsToDraft({silent: true})
    const context = buildConversionContext()
    const preview = applyMode.value === 'incremental'
      ? buildIncrementalFieldPatch(draft.value.dsl, context)
      : convertDslToDesignerStatePatch(draft.value.dsl, context)
    conversionPreview.value = preview
    conversionApplied.value = false
    resetFormalizePreview()
    console.log('FormDesignDslConversionPreview', {
      applyMode: applyMode.value,
      preview,
    })
    if (preview.errors.length > 0) {
      message.warning(`转换预览完成，发现 ${preview.errors.length} 个错误，暂不能应用`)
    } else if (applyMode.value === 'incremental' && (preview.summary?.addCount || 0) === 0) {
      message.warning('增量预览完成：没有可新增字段（同名均已跳过）')
    } else if (preview.warnings.length > 0) {
      const addHint = applyMode.value === 'incremental'
        ? `，将新增 ${preview.summary?.addCount || 0} 个字段`
        : ''
      message.warning(`转换预览完成，发现 ${preview.warnings.length} 个警告${addHint}`)
    } else {
      const addHint = applyMode.value === 'incremental'
        ? `，将新增 ${preview.summary?.addCount || 0} 个字段、跳过 ${preview.summary?.skipCount || 0} 个`
        : ''
      message.success(`转换预览完成${addHint}`)
    }
  } catch (error) {
    console.error('FormDesignDslConversionPreviewFailed', error)
    message.error(error?.message || '转换预览失败，请打开控制台查看详情')
  }
}

const applyDesignerState = () => {
  if (!conversionPreview.value) {
    message.warning('请先生成转换预览')
    return
  }
  if (!conversionPreview.value.canApply) {
    if (conversionPreview.value.mode === 'incremental' && (conversionPreview.value.summary?.addCount || 0) === 0) {
      message.warning('没有可增量补充的字段，请调整草稿或改用全量替换')
    } else {
      message.warning('转换预览存在错误，暂不能应用')
    }
    return
  }
  emit('apply-draft-patch', conversionPreview.value, {
    ...buildApplyDraftContext(),
    applyMode: conversionPreview.value.mode || applyMode.value,
  })
  conversionApplied.value = true
}

const printDraft = () => {
  if (!draft.value) {
    return
  }
  console.log('FormDesignDraftDSL', draft.value)
  message.success('已输出到控制台')
}

const printMaterial = () => {
  if (!materialRecognitionResult.value) {
    message.warning('请先识别材料')
    return
  }
  console.log('FormMaterial', materialRecognitionResult.value.material)
  console.log('FormMaterialIssues', materialPreviewIssues.value)
  message.success('已输出到控制台')
}

const printAiDiagnostics = () => {
  if (!aiDiagnosticVisible.value) {
    message.warning('暂无 AI 调用诊断信息')
    return
  }
  console.log('FormDesignAIDiagnostics', aiDiagnosticInfo.value)
  message.success('AI 诊断已输出到控制台')
}

const copyAiDiagnostics = async () => {
  if (!aiDiagnosticVisible.value) {
    message.warning('暂无 AI 调用诊断信息')
    return
  }
  const text = JSON.stringify(aiDiagnosticInfo.value, null, 2)
  console.log('FormDesignAIDiagnostics', aiDiagnosticInfo.value)
  try {
    const copied = await copyTextToClipboard(text)
    if (copied) {
      message.success('AI 诊断已复制到剪贴板')
    } else {
      message.info('复制未成功，已输出到控制台')
    }
  } catch (error) {
    console.warn('FormDesignAIDiagnostics copy failed', error)
    message.info('复制未成功，已输出到控制台')
  }
}

const copyMaterialJson = async () => {
  if (!materialRecognitionResult.value) {
    message.warning('请先识别材料')
    return
  }
  const text = JSON.stringify(materialRecognitionResult.value.material, null, 2)
  console.log('FormMaterial', materialRecognitionResult.value.material)
  try {
    const copied = await copyTextToClipboard(text)
    if (copied) {
      message.success('材料 JSON 已复制到剪贴板')
    } else {
      message.info('复制未成功，已输出到控制台')
    }
  } catch (error) {
    console.warn('FormMaterial copy failed', error)
    message.info('复制未成功，已输出到控制台')
  }
}

const copyDraftDsl = async () => {
  if (!draft.value) {
    message.warning('请先生成草稿')
    return
  }
  const text = getDraftDslText()
  console.log('FormDesignDraftDSL', draft.value)
  try {
    const copied = await copyTextToClipboard(text)
    if (copied) {
      message.success('DSL 已复制到剪贴板')
    } else {
      message.info('复制未成功，已输出到控制台')
    }
  } catch (error) {
    console.warn('FormDesignDraftDSL copy failed', error)
    message.info('复制未成功，已输出到控制台')
  }
}

const exportDraftDsl = () => {
  if (!draft.value) {
    message.warning('请先生成草稿')
    return
  }
  const text = getDraftDslText()
  console.log('FormDesignDraftDSL', draft.value)
  try {
    const blob = new Blob([text], {type: 'application/json;charset=utf-8'})
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = createDraftFileName()
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    message.success('DSL 文件已导出')
  } catch (error) {
    console.warn('FormDesignDraftDSL export failed', error)
    message.info('导出未成功，已输出到控制台')
  }
}
</script>

<style scoped lang="less">
.ai-draft-drawer {
  min-height: 100%;
  padding: 12px 16px 24px;
  background: #f6f8fb;
}

.demo-flow-panel,
.draft-input-panel,
.draft-section,
.draft-json-collapse {
  border: 1px solid #e8edf3;
  border-radius: 6px;
  background: #fff;
}

.demo-flow-panel {
  margin-bottom: 12px;
  padding: 12px 14px;
}

.demo-flow-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.demo-flow-head-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.demo-flow-title {
  color: #1f2937;
  font-size: 15px;
  font-weight: 600;
}

.demo-flow-status {
  margin-top: 2px;
  color: #4b5563;
  font-size: 12px;
  line-height: 1.5;
}

.demo-flow-guide {
  margin-bottom: 10px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.demo-flow-guide-item {
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid #e8edf3;
  border-radius: 6px;
  background: #fbfcfe;
  display: flex;
  align-items: center;
  gap: 8px;
}

.demo-flow-guide-label {
  flex: 0 0 auto;
  color: #64748b;
  font-size: 12px;
}

.demo-flow-guide-value {
  min-width: 0;
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.demo-flow-steps {
  display: grid;
  grid-template-columns: repeat(7, minmax(84px, 1fr));
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.demo-flow-step {
  min-width: 84px;
  min-height: 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 8px 6px;
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #fbfcfe;
  text-align: center;
}

.demo-flow-step.active {
  border-color: #91caff;
  background: #f0f7ff;
}

.demo-flow-step.done {
  border-color: #b7eb8f;
  background: #f6ffed;
}

.demo-flow-step.active.done {
  border-color: #91caff;
  background: #f0f7ff;
}

.demo-flow-index {
  flex: 0 0 auto;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #374151;
  font-size: 12px;
  line-height: 22px;
  text-align: center;
  font-weight: 600;
}

.demo-flow-step.active .demo-flow-index {
  background: #1677ff;
  color: #fff;
}

.demo-flow-step.done .demo-flow-index {
  background: #52c41a;
  color: #fff;
}

.demo-flow-step.active.done .demo-flow-index {
  background: #1677ff;
}

.demo-flow-body {
  min-width: 0;
  width: 100%;
}

.demo-flow-step-title {
  color: #1f2937;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
  word-break: keep-all;
}

.demo-flow-step-state {
  margin-top: 2px;
  color: #6b7280;
  font-size: 11px;
  line-height: 1.2;
}

.demo-flow-step.active .demo-flow-step-state {
  color: #1677ff;
  font-weight: 600;
}

.demo-flow-step.done .demo-flow-step-state {
  color: #389e0d;
}

.demo-flow-step.active.done .demo-flow-step-state {
  color: #1677ff;
}

.draft-input-panel {
  padding: 14px;
}

.material-source-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.material-source-title {
  color: #1f2937;
  font-size: 15px;
  font-weight: 600;
}

.material-source-subtitle {
  margin-top: 2px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.material-source-tabs {
  margin-top: 4px;
}

.material-source-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 10px;
}

.material-pane {
  padding: 12px;
  border: 1px solid #edf2f7;
  border-radius: 6px;
  background: #fbfcfe;
}

.material-pane-footer {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.material-pane-meta {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #4b5563;
  font-size: 12px;
  flex-wrap: wrap;
}

.material-upload-note {
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.material-preview {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #dbe3ee;
}

.material-preview-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.material-preview-title {
  color: #1f2937;
  font-weight: 600;
}

.material-preview-subtitle {
  margin-top: 3px;
  color: #6b7280;
  font-size: 12px;
}

.material-preview-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.material-preview-stats {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.material-debug-collapse {
  margin-top: 8px;
  border: 1px solid #edf2f7;
  border-radius: 6px;
  background: #fbfcfe;
}

.material-debug-collapse :deep(.ant-collapse-header) {
  padding: 7px 10px !important;
}

.material-debug-collapse :deep(.ant-collapse-content-box) {
  padding: 0 10px 10px !important;
}

.material-debug-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #64748b;
  font-size: 12px;
}

.material-debug-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.material-stat {
  padding: 8px 10px;
  border: 1px solid #e5edf7;
  border-radius: 6px;
  background: #fff;
  color: #6b7280;
  font-size: 12px;
}

.material-stat-count {
  margin-right: 4px;
  color: #1d4ed8;
  font-size: 18px;
  font-weight: 700;
}

.material-confirm-panel {
  margin-top: 10px;
  border: 1px solid #edf2f7;
  border-radius: 6px;
  background: #fff;
}

.material-confirm-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-bottom: 1px solid #edf2f7;
}

.material-confirm-title {
  color: #1f2937;
  font-weight: 600;
}

.material-confirm-subtitle {
  margin-top: 2px;
  color: #6b7280;
  font-size: 12px;
}

.material-confirm-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.material-confirm-toolbar {
  padding: 10px 12px;
  border-bottom: 1px solid #edf2f7;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  background: #fbfdff;
}

.material-confirm-search {
  width: 220px;
}

.material-confirm-mini-stats {
  margin-left: auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  flex-wrap: wrap;
}

.material-confirm-status {
  padding: 9px 12px;
  border-bottom: 1px solid #edf2f7;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.material-confirm-status.pending {
  background: #fff7ed;
}

.material-confirm-status.confirmed {
  background: #f0fdf4;
}

.material-confirm-status.blocked {
  background: #fef2f2;
}

.material-confirm-status-title {
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
}

.material-confirm-status-desc {
  margin-top: 2px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.material-revision-panel {
  padding: 10px 12px;
  border-bottom: 1px solid #edf2f7;
  background: #f8fbff;
}

.material-revision-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.material-revision-title {
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
}

.material-revision-subtitle {
  margin-top: 2px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.material-revision-examples {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
}

.material-revision-actions {
  margin-top: 8px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: flex-start;
}

.material-revision-review {
  margin-top: 10px;
  padding: 9px 10px;
  border: 1px solid #dbeafe;
  border-radius: 6px;
  background: #fff;
}

.material-revision-review-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.material-revision-review-title {
  color: #1e3a8a;
  font-size: 13px;
  font-weight: 600;
}

.material-revision-review-subtitle {
  margin-top: 2px;
  color: #64748b;
  font-size: 12px;
}

.material-revision-review-actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.material-revision-change-list {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.material-revision-change-item,
.material-revision-no-change {
  max-width: 100%;
  padding: 3px 7px;
  border-radius: 4px;
  background: #f1f5f9;
  color: #475569;
  font-size: 12px;
  line-height: 1.5;
}

.material-risk-list {
  padding: 8px 12px;
  border-bottom: 1px solid #edf2f7;
  background: #fffbeb;
}

.material-risk-row {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  color: #92400e;
  font-size: 12px;
  line-height: 1.7;
}

.material-risk-more {
  margin-top: 4px;
  color: #92400e;
  font-size: 12px;
}

.material-confirm-table {
  max-height: 380px;
  overflow: auto;
}

.material-confirm-empty {
  padding: 24px 12px;
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 8px;
}

.material-confirm-grid {
  min-width: 960px;
  display: grid;
  grid-template-columns: 72px 190px 150px 92px 170px 230px;
  gap: 8px;
  align-items: start;
}

.material-confirm-header {
  position: sticky;
  top: 0;
  z-index: 2;
  padding: 8px 10px;
  border-bottom: 1px solid #e5edf7;
  background: #f8fafc;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
}

.material-confirm-row {
  padding: 9px 10px;
  border-bottom: 1px solid #f1f5f9;
}

.material-confirm-row.has-risk {
  background: #fffdf7;
}

.material-confirm-row:last-child {
  border-bottom: none;
}

.material-confirm-index {
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  gap: 4px;
  color: #6b7280;
  font-size: 12px;
}

.material-field-editor,
.material-type-editor,
.material-check-editor,
.material-priority-editor {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.material-type-editor :deep(.ant-select),
.material-priority-editor :deep(.ant-input-number),
.material-query-row :deep(.ant-select) {
  width: 100%;
}

.material-check-editor {
  gap: 4px;
  color: #374151;
  font-size: 12px;
}

.material-query-row {
  display: grid;
  grid-template-columns: 70px minmax(0, 1fr);
  gap: 6px;
}

.material-example-text,
.material-reason-text {
  color: #6b7280;
  font-size: 12px;
  line-height: 1.4;
}

.material-row-risk-list {
  min-width: 960px;
  margin-top: 6px;
  padding-left: 80px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.material-field-list {
  margin-top: 10px;
  max-height: 260px;
  overflow: auto;
  border: 1px solid #edf2f7;
  border-radius: 6px;
  background: #fff;
}

.material-field-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 10px;
  border-bottom: 1px solid #f1f5f9;
}

.material-field-row:last-child {
  border-bottom: none;
}

.material-field-main {
  min-width: 0;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.material-field-label {
  color: #111827;
  font-weight: 600;
}

.material-field-name {
  color: #6b7280;
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
}

.material-field-tags {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  flex-wrap: wrap;
}

.material-issue-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.material-issue-row {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  color: #4b5563;
  font-size: 12px;
  line-height: 1.6;
}

.draft-mode-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.draft-mode-label {
  color: #374151;
  font-weight: 600;
}

.draft-input-actions {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.draft-sample-actions,
.draft-primary-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.draft-primary-actions {
  margin-left: auto;
}

.draft-storage-actions {
  margin-top: 12px;
}

.draft-ops-collapse {
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fafbfc;
}

.draft-ops-collapse :deep(.ant-collapse-item) {
  border-bottom: 0;
}

.draft-ops-collapse :deep(.ant-collapse-header) {
  align-items: center;
  padding: 9px 12px !important;
}

.draft-ops-collapse :deep(.ant-collapse-content-box) {
  padding: 0 12px 12px;
}

.draft-ops-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
}

.draft-ops-title {
  color: #1f2937;
  font-weight: 600;
}

.draft-ops-badges {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.draft-ops-panel {
  display: grid;
  gap: 10px;
  padding-top: 2px;
}

.draft-ops-group {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  align-items: flex-start;
  gap: 10px;
}

.draft-ops-group-title {
  color: #6b7280;
  font-size: 12px;
  line-height: 24px;
  white-space: nowrap;
}

.draft-ops-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.storage-tip {
  color: #6b7280;
  font-size: 12px;
}

.ai-diagnostic-panel {
  margin-top: 12px;
  border: 1px solid #dbeafe;
  border-radius: 6px;
  background: #f8fbff;
}

.ai-diagnostic-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
}

.ai-diagnostic-title {
  color: #1f2937;
  font-weight: 600;
}

.ai-diagnostic-subtitle {
  margin-top: 3px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.ai-diagnostic-head-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.ai-diagnostic-collapse {
  border-top: 1px solid #e8edf3;
  background: #fff;
}

.ai-diagnostic-collapse :deep(.ant-collapse-header) {
  padding: 8px 12px !important;
  color: #374151;
  font-size: 12px;
}

.ai-diagnostic-collapse :deep(.ant-collapse-content-box) {
  padding: 0 12px 12px !important;
}

.ai-diagnostic-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.ai-diagnostic-item {
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #fbfcfe;
}

.ai-diagnostic-label {
  display: block;
  margin-bottom: 4px;
  color: #6b7280;
  font-size: 12px;
}

.ai-diagnostic-value {
  display: block;
  color: #1f2937;
  word-break: break-all;
}

.ai-diagnostic-block {
  margin-top: 12px;
}

.ai-diagnostic-block-title {
  margin-bottom: 8px;
  color: #374151;
  font-weight: 600;
}

.ai-diagnostic-error {
  padding: 8px 10px;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  background: #fff2f0;
  color: #a8071a;
  line-height: 1.5;
  word-break: break-word;
}

.ai-diagnostic-preview {
  max-height: 180px;
  margin: 0;
  padding: 10px;
  overflow: auto;
  border-radius: 6px;
  background: #0f172a;
  color: #e5e7eb;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.clarification-panel {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid #dbeafe;
  border-radius: 6px;
  background: #f8fbff;
}

.clarification-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.clarification-title {
  color: #1f2937;
  font-weight: 600;
}

.clarification-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.clarification-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.clarification-list {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.clarification-item {
  padding: 10px;
  border: 1px solid #e8edf3;
  border-radius: 6px;
  background: #fff;
}

.clarification-question {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  color: #374151;
  font-weight: 600;
}

.clarification-desc {
  margin-bottom: 8px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.draft-empty {
  margin-top: 90px;
}

.draft-summary {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;
  margin: 12px 0;
}

.summary-item {
  border: 1px solid #e8edf3;
  border-radius: 6px;
  padding: 10px;
  background: #fff;
  display: flex;
  align-items: baseline;
  gap: 6px;
  color: #4b5563;
}

.summary-count {
  font-size: 20px;
  font-weight: 600;
  line-height: 1;
  color: #1677ff;
}

.summary-item.error .summary-count {
  color: #d9363e;
}

.summary-item.warning .summary-count {
  color: #d48806;
}

.summary-item.success .summary-count {
  color: #389e0d;
}

.draft-section {
  margin-top: 12px;
  padding: 14px;
}

.draft-section.compact {
  padding-bottom: 10px;
}

.draft-overview-section {
  padding-bottom: 12px;
}

.draft-overview-section .section-title-row {
  margin-bottom: 10px;
}

.draft-overview-section .draft-summary {
  margin: 0 0 12px;
}

.draft-overview-collapse {
  overflow: hidden;
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #fbfcfe;
}

.draft-overview-collapse :deep(.ant-collapse-item) {
  border-bottom-color: #edf1f5;
}

.draft-overview-collapse :deep(.ant-collapse-item:last-child) {
  border-bottom: 0;
}

.draft-overview-collapse :deep(.ant-collapse-header) {
  align-items: center;
  padding: 9px 12px !important;
}

.draft-overview-collapse :deep(.ant-collapse-content-box) {
  padding: 12px;
  background: #fff;
}

.draft-overview-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  color: #374151;
  font-weight: 600;
}

.draft-overview-tags {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.section-title {
  margin-bottom: 12px;
  color: #1f2937;
  font-weight: 600;
}

.section-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-title-row .section-title {
  margin-bottom: 4px;
}

.section-subtitle {
  color: #6b7280;
  font-size: 12px;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.clarification-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.clarification-summary-label {
  color: #6b7280;
  font-size: 12px;
}

.meta-item {
  min-width: 0;
  display: flex;
  gap: 8px;
}

.meta-label {
  flex: 0 0 44px;
  color: #6b7280;
}

.meta-label.wide {
  flex-basis: 68px;
}

.meta-value {
  min-width: 0;
  color: #1f2937;
  word-break: break-all;
}

.success-text {
  color: #237804;
}

.error-text {
  color: #d9363e;
}

.warning-text {
  color: #d48806;
}

.code,
.field-name {
  font-family: Consolas, Monaco, monospace;
}

.field-group + .field-group {
  margin-top: 12px;
}

.field-group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  color: #374151;
  font-weight: 600;
}

.field-list {
  border: 1px solid #edf1f5;
  border-radius: 6px;
  overflow: hidden;
}

.field-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 10px;
  background: #fff;
  border-top: 1px solid #edf1f5;
}

.field-row:first-child {
  border-top: 0;
}

.field-main {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.field-label {
  color: #1f2937;
  font-weight: 600;
}

.field-name {
  color: #6b7280;
  background: #f3f4f6;
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 12px;
}

.field-tags {
  flex: 0 0 auto;
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  flex-wrap: wrap;
}

.dictionary-confirm-panel {
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
}

.dictionary-confirm-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-bottom: 1px solid #edf1f5;
  background: #fbfcfe;
}

.dictionary-confirm-subtitle {
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.dictionary-confirm-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.dictionary-confirm-list {
  background: #fff;
}

.dictionary-row {
  padding: 12px;
  border-top: 1px solid #edf1f5;
}

.dictionary-row:first-child {
  border-top: 0;
}

.dictionary-row-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.dictionary-edit-grid {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr) minmax(0, 1fr);
  gap: 10px;
}

.dictionary-edit-grid.existing {
  grid-template-columns: 150px minmax(0, 1fr);
}

.dictionary-edit-item {
  min-width: 0;
}

.dictionary-edit-label {
  display: block;
  margin-bottom: 4px;
  color: #6b7280;
  font-size: 12px;
}

.dictionary-items-editor {
  margin-top: 10px;
}

.preview-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.preview-title {
  margin-bottom: 8px;
  color: #4b5563;
}

.pill-list {
  min-height: 26px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.empty-text {
  color: #9ca3af;
}

.conversion-empty {
  margin: 24px 0 8px;
}

.conversion-applied-alert {
  margin-top: 10px;
}

.conversion-summary {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 12px;
}

.apply-mode-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin: 10px 0 4px;
}

.apply-mode-label {
  color: #4b5563;
  font-size: 13px;
  font-weight: 500;
}

.apply-mode-hint {
  margin-bottom: 10px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.conversion-detail-collapse {
  overflow: hidden;
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #fbfcfe;
}

.conversion-detail-collapse :deep(.ant-collapse-item),
.optional-review-collapse :deep(.ant-collapse-item),
.advanced-info-collapse :deep(.ant-collapse-item) {
  border-bottom: 0;
}

.conversion-detail-collapse :deep(.ant-collapse-header),
.optional-review-collapse :deep(.ant-collapse-header),
.advanced-info-collapse :deep(.ant-collapse-header) {
  align-items: center;
  padding: 10px 12px !important;
}

.conversion-detail-collapse :deep(.ant-collapse-content-box),
.optional-review-collapse :deep(.ant-collapse-content-box),
.advanced-info-collapse :deep(.ant-collapse-content-box) {
  padding: 12px;
  background: #fff;
}

.conversion-detail-header,
.optional-review-header,
.advanced-info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.optional-review-section,
.advanced-info-collapse {
  overflow: hidden;
  padding: 0;
}

.optional-review-header .section-title,
.advanced-info-header .section-title {
  margin-bottom: 4px;
}

.advanced-info-block + .advanced-info-block {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #edf1f5;
}

.advanced-info-block.ai-diagnostic-panel {
  margin-top: 0;
}

.conversion-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  padding: 10px;
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #fbfcfe;
}

.conversion-block {
  margin-top: 12px;
}

.formalize-notice,
.formalize-source {
  margin: 12px 0;
}

.formalize-more {
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
}

.conversion-field-list {
  border: 1px solid #edf1f5;
  border-radius: 6px;
  overflow: hidden;
}

.conversion-field-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 10px;
  background: #fff;
  border-top: 1px solid #edf1f5;
}

.conversion-field-row:first-child {
  border-top: 0;
}

.check-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.check-summary-card {
  border: 1px solid #e8edf3;
  border-radius: 6px;
  padding: 10px 12px;
  background: #fff;
}

.check-summary-card.success {
  border-color: #b7eb8f;
  background: #f6ffed;
}

.check-summary-card.warning {
  border-color: #ffe58f;
  background: #fffbe6;
}

.check-summary-card.error {
  border-color: #ffccc7;
  background: #fff2f0;
}

.check-summary-title {
  color: #374151;
  font-weight: 600;
}

.check-summary-count {
  margin-top: 4px;
  font-size: 22px;
  font-weight: 600;
  color: #1677ff;
}

.check-summary-card.success .check-summary-count {
  color: #389e0d;
}

.check-summary-card.warning .check-summary-count {
  color: #d48806;
}

.check-summary-card.error .check-summary-count {
  color: #d9363e;
}

.check-summary-desc {
  margin-top: 2px;
  color: #6b7280;
  font-size: 12px;
}

.check-block {
  margin-top: 12px;
}

.check-block-title,
.design-issue-group-title {
  margin-bottom: 8px;
  color: #374151;
  font-weight: 600;
}

.design-issue-group + .design-issue-group {
  margin-top: 12px;
}

.design-issue-group-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.check-ok {
  color: #237804;
}

.issue-list {
  display: grid;
  gap: 8px;
}

.check-issue {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 9px 10px;
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #fff;
}

.check-issue-body {
  min-width: 0;
  flex: 1;
}

.check-issue-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  color: #1f2937;
  font-weight: 600;
}

.check-field {
  color: #6b7280;
  font-size: 12px;
  font-weight: 400;
}

.check-issue-desc,
.check-issue-suggestion {
  margin-top: 4px;
  color: #4b5563;
  line-height: 1.5;
}

.draft-json-collapse {
  margin-top: 12px;
}

.json-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.json-panel-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.json-preview {
  max-height: 360px;
  margin: 0;
  padding: 12px;
  overflow: auto;
  background: #0f172a;
  color: #e5e7eb;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.drawer-footer {
  display: flex;
  align-items: stretch;
  flex-direction: column;
  gap: 10px;
}

.footer-status {
  min-width: 0;
  color: #4b5563;
  line-height: 1.5;
  text-align: left;
  white-space: normal;
  word-break: break-word;
}

.footer-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.remote-draft-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.local-draft-list {
  display: grid;
  gap: 10px;
  max-height: 560px;
  overflow: auto;
}

.local-draft-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e8edf3;
  border-radius: 6px;
  background: #fff;
}

.local-draft-item.active {
  border-color: #91caff;
  background: #f0f7ff;
}

.local-draft-main {
  min-width: 0;
  flex: 1;
}

.local-draft-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2937;
  font-weight: 600;
}

.local-draft-meta {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: #6b7280;
  font-size: 12px;
}

.local-draft-source {
  margin-top: 6px;
  color: #4b5563;
  line-height: 1.5;
  word-break: break-word;
}

.local-draft-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.local-version-panel {
  flex: 0 0 100%;
  padding-top: 10px;
  border-top: 1px solid #e8edf3;
}

.version-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.version-panel-title {
  color: #374151;
  font-weight: 600;
}

.version-compare-controls {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6b7280;
}

.local-version-list {
  display: grid;
  gap: 6px;
}

.local-version-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 10px;
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #fbfcfe;
}

.local-version-row.active {
  border-color: #91caff;
  background: #f0f7ff;
}

.version-main {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: #6b7280;
  font-size: 12px;
}

.version-name {
  color: #1f2937;
  font-weight: 600;
}

.version-diff {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #fff;
}

.version-diff-summary {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.version-diff-groups {
  display: grid;
  gap: 10px;
}

.version-diff-title {
  margin-bottom: 6px;
  color: #374151;
  font-weight: 600;
}

.version-changed-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
  padding: 6px 8px;
  border-radius: 4px;
  background: #fff7e6;
  color: #6b4f00;
}

.template-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.template-search {
  max-width: 280px;
}

.template-list {
  display: grid;
  gap: 10px;
  max-height: 600px;
  overflow: auto;
}

.template-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e8edf3;
  border-radius: 6px;
  background: #fff;
}

.template-main {
  min-width: 0;
  flex: 1;
}

.template-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.template-title {
  color: #1f2937;
  font-weight: 600;
}

.template-desc {
  margin-top: 6px;
  color: #4b5563;
  line-height: 1.5;
}

.template-meta {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: #6b7280;
  font-size: 12px;
}

.template-fields {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.template-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .demo-flow-head {
    flex-direction: column;
  }

  .demo-flow-head-actions {
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .demo-flow-guide {
    grid-template-columns: 1fr;
  }

  .demo-flow-steps {
    grid-template-columns: repeat(7, minmax(136px, 1fr));
  }

  .draft-summary,
  .conversion-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .meta-grid,
  .preview-columns,
  .check-summary-grid,
  .conversion-meta,
  .dictionary-edit-grid {
    grid-template-columns: 1fr;
  }

  .field-row,
  .conversion-field-row,
  .dictionary-confirm-head,
  .dictionary-row-head,
  .section-title-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .footer-actions {
    justify-content: flex-start;
  }

  .draft-input-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .draft-primary-actions {
    margin-left: 0;
  }

  .draft-ops-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .draft-overview-panel-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .conversion-detail-header,
  .optional-review-header,
  .advanced-info-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .draft-ops-group {
    grid-template-columns: 1fr;
    gap: 4px;
  }

  .clarification-head {
    flex-direction: column;
  }

  .material-source-head,
  .material-pane-footer,
  .material-preview-head,
  .material-debug-header,
  .material-confirm-head,
  .material-confirm-status,
  .material-revision-head,
  .material-revision-review-head,
  .material-field-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .material-preview-stats {
    grid-template-columns: 1fr;
  }

  .ai-diagnostic-grid {
    grid-template-columns: 1fr;
  }

  .clarification-actions {
    justify-content: flex-start;
  }

  .material-confirm-actions {
    justify-content: flex-start;
  }

  .dictionary-confirm-actions {
    justify-content: flex-start;
  }

  .material-confirm-toolbar,
  .material-confirm-mini-stats,
  .material-revision-examples,
  .material-revision-review-actions {
    align-items: flex-start;
    flex-direction: column;
    justify-content: flex-start;
  }

  .material-confirm-search {
    width: 100%;
  }

  .material-revision-actions {
    grid-template-columns: 1fr;
  }

  .ai-diagnostic-head,
  .ai-diagnostic-head-actions {
    align-items: flex-start;
    flex-direction: column;
    justify-content: flex-start;
  }

  .local-draft-item {
    flex-direction: column;
  }

  .version-panel-head,
  .local-version-row,
  .version-changed-field,
  .template-toolbar,
  .template-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .template-search {
    max-width: none;
    width: 100%;
  }

  .template-actions {
    justify-content: flex-start;
  }

  .json-panel-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
