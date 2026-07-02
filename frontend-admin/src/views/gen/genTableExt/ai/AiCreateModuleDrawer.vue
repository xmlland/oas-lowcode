<template>
  <a-drawer
      :visible="visible"
      title="AI创建模块"
      placement="right"
      :width="900"
      :z-index="2000"
      :body-style="{ padding: '0', overflow: 'auto' }"
      @close="close"
  >
    <div class="ai-module-drawer">
      <div class="module-flow-panel">
        <div class="module-flow-head">
          <div>
            <div class="module-flow-title">AI 模块创建流程</div>
            <div class="module-flow-status">{{ flowStatusText }}</div>
          </div>
          <div class="module-flow-actions">
            <a-tag color="blue">模块草稿</a-tag>
            <a-button
                size="small"
                :disabled="moduleCreateBusy"
                @click="confirmResetModuleCreateState"
            >重置</a-button>
            <a-button
                size="small"
                type="primary"
                :loading="moduleMainActionLoading"
                :disabled="!canRecognizeBlueprint || moduleMainActionLoading"
                @click="recognizeModuleBlueprint"
            >{{ recognizeBlueprintButtonText }}</a-button>
          </div>
        </div>
        <div class="module-flow-steps">
          <div
              v-for="step in flowSteps"
              :key="step.key"
              :class="['module-flow-step', step.active ? 'active' : '', step.done ? 'done' : '']"
          >
            <div class="module-flow-index">{{ step.index }}</div>
            <div class="module-flow-body">
              <div class="module-flow-step-title">{{ step.title }}</div>
              <div class="module-flow-step-state">{{ step.stateText }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="module-main-layout">
        <section class="module-section">
          <div class="module-section-head">
            <div>
              <div class="module-section-title">输入来源</div>
              <div class="module-section-subtitle">可以用文字说明、原型 URL、Excel 多表格、Word / PDF 材料识别模块蓝图。</div>
            </div>
            <a-tag :color="sourceTagColor">{{ sourceTypeText }}</a-tag>
          </div>

          <div v-if="moduleBlueprintConfirmed" class="module-compact-summary">
            <div>
              <span class="module-summary-label">来源</span>
              <strong>{{ sourceTypeText }}</strong>
            </div>
            <div>
              <span class="module-summary-label">识别结果</span>
              <strong>{{ modulePages.length }} 个页面、{{ moduleForms.length }} 个表单</strong>
            </div>
            <div class="module-summary-desc">{{ moduleInputHint }}</div>
          </div>
          <template v-else>
            <div class="module-source-grid">
              <button
                  v-for="source in sourceOptions"
                  :key="source.value"
                  :class="['module-source-option', sourceType === source.value ? 'active' : '']"
                  type="button"
                  @click="selectModuleSourceType(source.value)"
              >
                <span class="module-source-title">{{ source.label }}</span>
                <span class="module-source-desc">{{ source.description }}</span>
              </button>
            </div>

            <div class="module-input-area">
              <template v-if="sourceType === 'text'">
                <a-textarea
                    v-model:value="sourceText"
                    :rows="7"
                    placeholder="例如：收文管理模块包含收文登记、拟办、办理、查询统计几个页面，需要生成收文登记表、办理意见表、查询列表，并复用或创建收文状态、紧急程度等字典。"
                />
              </template>
              <template v-else-if="sourceType === 'url'">
                <a-input v-model:value="sourceUrl" placeholder="输入原型系统 URL，例如 https://prototype.example.com/receive"/>
                <div class="module-url-options">
                  <div class="module-url-render-option">
                    <a-switch
                        v-model:checked="moduleUrlDynamicRender"
                        size="small"
                        :disabled="moduleUrlExtracting"
                        @change="handleModuleUrlDynamicRenderChange"
                    />
                    <div>
                      <div class="module-url-render-title">动态渲染采集</div>
                      <div class="module-url-render-desc">适合 Vue / React 原型页面；会更慢，失败时自动降级静态 HTML。</div>
                    </div>
                  </div>
                  <a-tag :color="moduleUrlCollectorTagColor">{{ moduleUrlCollectorText }}</a-tag>
                </div>
                <a-textarea
                    v-model:value="sourceText"
                    :rows="5"
                    class="module-input-gap"
                    placeholder="补充说明：需要采集哪些菜单、是否有登录态、哪些页面优先生成表单。"
                />
                <div v-if="moduleUrlExtractionResult" class="module-url-candidates">
                  <div class="module-url-candidate-head">
                    <div>
                      <div class="module-blueprint-title">原型页面候选</div>
                      <div class="module-url-candidate-subtitle">
                        已采集 {{ moduleUrlCandidatePages.length }} 个页面，已选择 {{ moduleUrlSelectedPages.length }} 个。
                        <span v-if="moduleUrlExtractionResult.fromCache">Redis 缓存命中。</span>
                        <span v-if="moduleUrlCollectorDetailText">{{ moduleUrlCollectorDetailText }}</span>
                      </div>
                    </div>
                    <div class="module-url-candidate-actions">
                      <a-button size="small" @click="selectAllModuleUrlPages(true)">全选</a-button>
                      <a-button size="small" @click="selectAllModuleUrlPages(false)">全不选</a-button>
                      <a-button size="small" :loading="moduleUrlExtracting" @click="extractUrlPrototypeMaterial(true)">重新采集</a-button>
                    </div>
                  </div>
                  <div class="module-url-page-list">
                    <div
                        v-for="page in moduleUrlCandidatePages"
                        :key="page.id"
                        class="module-url-page-item"
                    >
                      <a-checkbox
                          :checked="isModuleUrlPageSelected(page.id)"
                          @change="event => toggleModuleUrlPage(page.id, event.target.checked)"
                      />
                      <div class="module-url-page-body">
                        <div class="module-url-page-title">{{ page.title || '未命名页面' }}</div>
                        <div class="module-url-page-url">{{ page.url }}</div>
                        <div class="module-url-page-meta">
                          <a-tag :color="getUrlPageQualityColor(page)">{{ getUrlPageQualityText(page) }}</a-tag>
                          <a-tag>标题 {{ page.headings.length }}</a-tag>
                          <a-tag>标签 {{ page.labels.length }}</a-tag>
                          <a-tag>输入 {{ page.inputs.length }}</a-tag>
                          <a-tag>表格 {{ page.tables.length }}</a-tag>
                        </div>
                        <div v-if="page.qualityReasons.length" class="module-url-page-quality-reasons">
                          {{ page.qualityReasons.slice(0, 2).join(' / ') }}
                        </div>
                        <div class="module-url-page-preview">{{ page.textPreview || '暂无可预览文本' }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="module-upload-placeholder">
                  <div>
                    <div class="module-upload-title">{{ currentFileUploadTitle }}</div>
                    <div class="module-upload-desc">{{ currentFileUploadDesc }}</div>
                  </div>
                  <a-upload
                      :file-list="moduleFileList"
                      :before-upload="handleModuleFileBeforeUpload"
                      :show-upload-list="{ showRemoveIcon: true }"
                      :accept="moduleFileAccept"
                      :max-count="1"
                      @remove="removeModuleFile"
                  >
                    <a-button>{{ currentFileUploadButtonText }}</a-button>
                  </a-upload>
                </div>
                <a-textarea
                    v-model:value="sourceText"
                    :rows="5"
                    class="module-input-gap"
                    :placeholder="currentFileSupplementPlaceholder"
                />
              </template>
            </div>

            <div class="module-input-footer">
              <div class="module-input-meta">
                <a-tag :color="canRecognizeBlueprint ? 'green' : 'default'">{{ canRecognizeBlueprint ? '可识别' : '待补充' }}</a-tag>
                <span>{{ moduleInputHint }}</span>
              </div>
              <a-button
                  type="primary"
                  :loading="moduleMainActionLoading"
                  :disabled="!canRecognizeBlueprint || moduleMainActionLoading"
                  @click="recognizeModuleBlueprint"
              >{{ recognizeBlueprintButtonText }}</a-button>
            </div>
          </template>
        </section>

        <section class="module-section">
          <div class="module-section-head">
            <div>
              <div class="module-section-title">模块基础信息</div>
              <div class="module-section-subtitle">这些信息会作为后续 ModuleMaterial 和 ModuleDesignDSL 的初始上下文。</div>
            </div>
            <a-tag :color="materialSummary.error > 0 ? 'red' : materialSummary.warning > 0 ? 'orange' : 'green'">
              {{ materialSummary.total }} 个结构提示
            </a-tag>
          </div>
          <div v-if="moduleBlueprintConfirmed" class="module-compact-summary module-info-summary">
            <div>
              <span class="module-summary-label">模块编码</span>
              <strong>{{ moduleName || '-' }}</strong>
            </div>
            <div>
              <span class="module-summary-label">模块标题</span>
              <strong>{{ moduleTitle || '-' }}</strong>
            </div>
            <div>
              <span class="module-summary-label">模块场景</span>
              <strong>{{ moduleScene || 'normal' }}</strong>
            </div>
          </div>
          <div v-else class="module-meta-grid">
            <label class="module-field">
              <span>模块编码</span>
              <a-input v-model:value="moduleName" placeholder="contract_manage" @change="moduleNameTouched = true"/>
            </label>
            <label class="module-field">
              <span>模块标题</span>
              <a-input v-model:value="moduleTitle" placeholder="合同管理" @change="moduleTitleTouched = true"/>
            </label>
            <label class="module-field wide">
              <span>模块场景</span>
              <a-radio-group v-model:value="moduleScene" button-style="solid">
                <a-radio-button value="normal">普通</a-radio-button>
                <a-radio-button value="document">公文</a-radio-button>
                <a-radio-button value="approval">审批</a-radio-button>
                <a-radio-button value="ledger">台账</a-radio-button>
                <a-radio-button value="mixed">混合</a-radio-button>
              </a-radio-group>
            </label>
          </div>
        </section>

        <section class="module-section">
          <div class="module-section-head">
            <div>
              <div class="module-section-title">模块蓝图</div>
              <div class="module-section-subtitle">先确认 AI 识别出的菜单、页面和表单边界；确认后再进入逐个表单生成。</div>
            </div>
            <div class="module-blueprint-actions">
              <a-tag :color="moduleRecognitionTagColor">{{ moduleRecognitionTagText }}</a-tag>
              <template v-if="moduleMaterialReady">
                <a-button
                    v-if="!moduleBlueprintConfirmed"
                    size="small"
                    type="primary"
                    @click="confirmModuleBlueprint"
                >确认蓝图</a-button>
                <a-button
                    v-else
                    size="small"
                    @click="editModuleBlueprint"
                >继续编辑</a-button>
              </template>
            </div>
          </div>
          <div v-if="!moduleMaterialResult" class="module-empty-state">
            <div class="module-empty-title">还没有模块蓝图</div>
            <div class="module-empty-desc">输入文字说明或选择文件后，点击 AI 识别模块蓝图。</div>
          </div>
          <div v-else-if="!moduleMaterialReady" class="module-recognition-warning">
            <div class="module-recognition-warning-title">{{ moduleRecognitionWarningTitle }}</div>
            <div class="module-recognition-warning-desc">{{ moduleRecognitionWarningDesc }}</div>
            <div v-if="materialIssues.length > 0" class="module-issue-list">
              <div v-for="issue in materialIssues.slice(0, 5)" :key="issue.id" class="module-issue-item">
                <a-tag :color="issue.level === 'error' ? 'red' : issue.level === 'warning' ? 'orange' : 'blue'">{{ issue.level || 'warning' }}</a-tag>
                <span>{{ issue.title }}：{{ issue.description || issue.suggestion }}</span>
              </div>
            </div>
          </div>
          <div v-else class="module-blueprint-editor">
            <div v-if="moduleBlueprintConfirmed" class="module-blueprint-readonly">
              <div class="module-editor-toolbar">
                <div class="module-editor-hint">
                  蓝图已确认，继续编辑会重新进入待确认状态。
                </div>
              </div>
              <div class="module-blueprint">
                <div class="module-blueprint-column">
                  <div class="module-blueprint-title">菜单</div>
                  <div v-if="moduleMenus.length === 0" class="module-mini-empty">暂未识别菜单</div>
                  <div v-for="menu in moduleMenus" :key="menu.id || menu.title" class="module-blueprint-item">
                    <span class="module-blueprint-main">{{ menu.title || '未命名菜单' }}</span>
                    <span class="module-blueprint-sub">{{ menu.pathHint || menu.id }}</span>
                  </div>
                </div>
                <div class="module-blueprint-column wide">
                  <div class="module-blueprint-title">页面</div>
                  <div v-if="modulePages.length === 0" class="module-mini-empty">暂未识别页面</div>
                  <div v-for="page in modulePages" :key="page.id || page.title" class="module-page-item">
                    <div>
                      <span class="module-blueprint-main">{{ page.title || '未命名页面' }}</span>
                      <span class="module-blueprint-sub">{{ page.description || page.id }}</span>
                    </div>
                    <div class="module-page-readonly-tags">
                      <a-tag>{{ modulePageTypeTextMap[page.pageType] || page.pageType || '待确认' }}</a-tag>
                      <a-tag :color="hasPageForm(page.id) ? 'green' : 'default'">
                        {{ hasPageForm(page.id) ? '生成表单' : '不生成表单' }}
                      </a-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <template v-else>
            <div class="module-editor-toolbar">
              <div class="module-editor-hint">
                {{ moduleBlueprintConfirmed ? '蓝图已确认，继续编辑会重新进入待确认状态。' : '可微调菜单、页面类型和哪些页面需要生成表单。' }}
              </div>
              <div class="module-editor-actions" v-if="!moduleBlueprintConfirmed">
                <a-button size="small" @click="addModuleMenu">添加菜单</a-button>
                <a-button size="small" @click="addModulePage">添加页面</a-button>
              </div>
            </div>
            <div class="module-blueprint">
              <div class="module-blueprint-column">
                <div class="module-blueprint-title">菜单</div>
                <div v-if="moduleMenus.length === 0" class="module-mini-empty">暂未识别菜单</div>
                <div v-for="menu in moduleMenus" :key="menu.id || menu.title" class="module-edit-item">
                  <div class="module-edit-grid">
                    <label class="module-edit-field">
                      <span>名称</span>
                      <a-input
                          v-model:value="menu.title"
                          size="small"
                          :disabled="moduleBlueprintConfirmed"
                          @change="markModuleBlueprintDirty"
                      />
                    </label>
                    <label class="module-edit-field">
                      <span>ID</span>
                      <a-input
                          v-model:value="menu.id"
                          size="small"
                          :disabled="moduleBlueprintConfirmed"
                          @change="markModuleBlueprintDirty"
                      />
                    </label>
                    <label class="module-edit-field wide">
                      <span>路径建议</span>
                      <a-input
                          v-model:value="menu.pathHint"
                          size="small"
                          :disabled="moduleBlueprintConfirmed"
                          @change="markModuleBlueprintDirty"
                      />
                    </label>
                  </div>
                  <div class="module-edit-row-actions" v-if="!moduleBlueprintConfirmed">
                    <a-button size="small" danger @click="removeModuleMenu(menu)">删除</a-button>
                  </div>
                </div>
              </div>
              <div class="module-blueprint-column wide">
                <div class="module-blueprint-title">页面</div>
                <div v-if="modulePages.length === 0" class="module-mini-empty">暂未识别页面</div>
                <div v-for="page in modulePages" :key="page.id || page.title" class="module-edit-item">
                  <div class="module-page-edit-head">
                    <label class="module-page-generate">
                      <span>生成表单</span>
                      <a-switch
                          size="small"
                          :checked="hasPageForm(page.id)"
                          :disabled="moduleBlueprintConfirmed"
                          @change="checked => togglePageForm(page, checked)"
                      />
                    </label>
                    <a-button
                        v-if="!moduleBlueprintConfirmed"
                        size="small"
                        danger
                        @click="removeModulePage(page)"
                    >删除页面</a-button>
                  </div>
                  <div class="module-edit-grid page">
                    <label class="module-edit-field">
                      <span>页面名称</span>
                      <a-input
                          v-model:value="page.title"
                          size="small"
                          :disabled="moduleBlueprintConfirmed"
                          @change="markModuleBlueprintDirty"
                      />
                    </label>
                    <label class="module-edit-field">
                      <span>页面 ID</span>
                      <a-input
                          v-model:value="page.id"
                          size="small"
                          :disabled="moduleBlueprintConfirmed"
                          @change="markModuleBlueprintDirty"
                      />
                    </label>
                    <label class="module-edit-field">
                      <span>归属菜单</span>
                      <a-select
                          v-model:value="page.menuId"
                          size="small"
                          :options="menuSelectOptions"
                          allow-clear
                          :disabled="moduleBlueprintConfirmed"
                          @change="markModuleBlueprintDirty"
                      />
                    </label>
                    <label class="module-edit-field">
                      <span>页面类型</span>
                      <a-select
                          v-model:value="page.pageType"
                          size="small"
                          :options="pageTypeOptions"
                          :disabled="moduleBlueprintConfirmed"
                          @change="markModuleBlueprintDirty"
                      />
                    </label>
                    <label class="module-edit-field wide">
                      <span>说明</span>
                      <a-input
                          v-model:value="page.description"
                          size="small"
                          :disabled="moduleBlueprintConfirmed"
                          @change="markModuleBlueprintDirty"
                      />
                    </label>
                  </div>
                </div>
              </div>
            </div>
            </template>
          </div>
        </section>

        <section class="module-section">
          <div class="module-section-head">
            <div>
              <div class="module-section-title">表单草稿</div>
              <div class="module-section-subtitle">后续会按页面逐个生成 FormDesignDSL，并复用现有单表单检查。</div>
            </div>
            <a-tag :color="moduleFormDraftTagColor">{{ moduleFormDraftTagText }}</a-tag>
          </div>
          <div class="module-overview-grid">
            <div class="module-overview-item">
              <span class="module-overview-count">{{ moduleMenus.length }}</span>
              <span>菜单</span>
            </div>
            <div class="module-overview-item">
              <span class="module-overview-count">{{ modulePages.length }}</span>
              <span>页面</span>
            </div>
            <div class="module-overview-item">
              <span class="module-overview-count">{{ moduleForms.length }}</span>
              <span>表单</span>
            </div>
            <div class="module-overview-item">
              <span class="module-overview-count">{{ moduleDictionarySuggestionCount }}</span>
              <span>字典</span>
            </div>
          </div>
          <div v-if="moduleMaterialReady && !moduleBlueprintConfirmed" class="module-form-toolbar">
            <span>表单边界用于后续逐个生成 FormDesignDSL。</span>
            <a-button size="small" @click="addModuleForm()">添加表单</a-button>
          </div>
          <div v-if="moduleBlueprintConfirmed && moduleForms.length > 0" class="module-form-toolbar module-batch-toolbar">
            <div class="module-batch-summary">
              <span>{{ moduleFormDraftBatchSummaryText }}</span>
              <span v-if="moduleFormDraftBatchGenerating">当前：{{ moduleFormDraftBatchCurrentTitle || '-' }}</span>
            </div>
            <div class="module-form-toolbar-actions">
              <a-button
                  size="small"
                  type="primary"
                  :loading="moduleFormDraftBatchGenerating && moduleFormDraftBatchMode === 'pending'"
                  :disabled="!canBatchGenerateModuleFormDrafts"
                  @click="batchGenerateModuleFormDrafts('pending')"
              >批量生成未完成</a-button>
              <a-button
                  size="small"
                  :loading="moduleFormDraftBatchGenerating && moduleFormDraftBatchMode === 'all'"
                  :disabled="!canBatchRegenerateModuleFormDrafts"
                  @click="batchGenerateModuleFormDrafts('all')"
              >重新生成全部</a-button>
              <a-button
                  size="small"
                  :disabled="!canBatchConfirmModuleFormDrafts"
                  @click="batchConfirmModuleFormDrafts"
              >批量确认草稿</a-button>
            </div>
          </div>
          <div v-if="moduleForms.length > 0" class="module-form-list">
            <div v-for="(form, formIndex) in moduleForms" :key="form.id || form.title" class="module-form-item">
              <div class="module-form-edit-main">
                <div v-if="moduleBlueprintConfirmed">
                  <div class="module-form-title">{{ form.title || '未命名表单' }}</div>
                  <div class="module-form-sub">{{ form.nameHint || '待确认表名' }}，{{ getFormFields(form).length }} 个字段</div>
                </div>
                <div v-else class="module-form-edit-grid">
                  <label class="module-edit-field">
                    <span>表单标题</span>
                    <a-input
                        v-model:value="form.title"
                        size="small"
                        @change="markModuleBlueprintDirty"
                    />
                  </label>
                  <label class="module-edit-field">
                    <span>表名建议</span>
                    <a-input
                        v-model:value="form.nameHint"
                        size="small"
                        @change="markModuleBlueprintDirty"
                    />
                  </label>
                  <label class="module-edit-field">
                    <span>所属页面</span>
                    <a-select
                        v-model:value="form.pageId"
                        size="small"
                        :options="pageSelectOptions"
                        allow-clear
                        @change="markModuleBlueprintDirty"
                    />
                  </label>
                </div>
              </div>
              <div class="module-form-tags">
                <div class="module-form-status-tags">
                  <a-tag
                      v-if="moduleBlueprintConfirmed"
                      :color="getModuleFormDraftStatusMeta(form, formIndex).color"
                  >{{ getModuleFormDraftStatusMeta(form, formIndex).text }}</a-tag>
                  <a-tag color="blue">{{ getFormListFieldCount(form) }} 个列表字段</a-tag>
                  <a-tag color="green">{{ getFormQueryFieldCount(form) }} 个查询条件</a-tag>
                  <a-tag color="orange">{{ getFormDictionaryHintCount(form, formIndex) }} 个字典建议</a-tag>
                </div>
                <div v-if="moduleBlueprintConfirmed" class="module-form-actions">
                  <a-button
                      size="small"
                      type="primary"
                      :loading="getModuleFormDraft(form, formIndex).status === 'generating'"
                      :disabled="moduleFormDraftBatchGenerating || getModuleFormDraft(form, formIndex).status === 'generating'"
                      @click="prepareModuleFormDraft(form, formIndex)"
                  >{{ getModuleFormDraftActionText(form, formIndex) }}</a-button>
                  <a-button size="small" @click="viewModuleFormDraft(form, formIndex)">查看草稿</a-button>
                  <a-button size="small" @click="checkModuleFormDraft(form, formIndex)">AI体检</a-button>
                </div>
                <a-button
                    v-if="!moduleBlueprintConfirmed"
                    size="small"
                    danger
                    @click="removeModuleForm(form)"
                >删除</a-button>
              </div>
            </div>
          </div>
        </section>

        <section class="module-section">
          <div class="module-section-head">
            <div>
              <div class="module-section-title">模块级字典确认</div>
              <div class="module-section-subtitle">汇总所有表单草稿中的字典建议，先确认使用已有字典还是新建系统字典；应用后会回写到各个表单草稿 DSL。</div>
            </div>
            <div class="module-dictionary-actions">
              <div class="module-dictionary-tags">
                <a-tag :color="moduleDictionaryConfirmationRows.length > 0 ? 'blue' : 'default'">共 {{ moduleDictionaryStats.total }} 个</a-tag>
                <a-tag color="green">新建 {{ moduleDictionaryStats.create }}</a-tag>
                <a-tag color="blue">已有 {{ moduleDictionaryStats.existing }}</a-tag>
                <a-tag color="default">忽略 {{ moduleDictionaryStats.ignore }}</a-tag>
                <a-tag v-if="moduleDictionaryStats.noItems > 0" color="orange">无字典项 {{ moduleDictionaryStats.noItems }}</a-tag>
              </div>
              <a-button
                  size="small"
                  type="primary"
                  :disabled="!canApplyModuleDictionaryConfirmations"
                  @click="applyModuleDictionaryConfirmations"
              >应用字典确认到草稿</a-button>
            </div>
          </div>

          <div v-if="moduleDictionaryConfirmationRows.length === 0" class="module-dictionary-empty">
            <div class="module-empty-title">暂无模块级字典建议</div>
            <div class="module-empty-desc">生成表单草稿后，会自动汇总每个表单中的下拉、单选、多选字段；如果材料蓝图里已有字典提示，也会先显示在这里。</div>
          </div>
          <div v-else class="module-dictionary-list">
            <div
                v-for="row in moduleDictionaryConfirmationRows"
                :key="row.key"
                class="module-dictionary-row"
            >
              <div class="module-dictionary-row-head">
                <div class="module-dictionary-main">
                  <strong>{{ row.name || row.code || row.fieldLabel || row.key }}</strong>
                  <span>{{ row.code || row.existingCode || '待确认字典编码' }}</span>
                </div>
                <div class="module-dictionary-row-tags">
                  <a-tag :color="getDictionaryModeColor(row.mode)">{{ dictionaryModeTextMap[row.mode] || row.mode }}</a-tag>
                  <a-tag v-if="row.applied" color="green">已应用</a-tag>
                  <a-tag v-else color="orange">待应用</a-tag>
                  <a-tag v-if="row.mode === 'create' && parseDictionaryItemsText(row.itemsText).length === 0" color="orange">无字典项</a-tag>
                </div>
              </div>

              <div class="module-dictionary-field-refs">
                <a-tag
                    v-for="fieldRef in row.fieldRefs"
                    :key="`${fieldRef.formId}:${fieldRef.fieldName}`"
                    color="purple"
                >{{ fieldRef.formTitle || fieldRef.formId }} - {{ fieldRef.fieldLabel || fieldRef.fieldName }}</a-tag>
              </div>

              <div :class="['module-dictionary-edit-grid', row.mode === 'use-existing' ? 'existing' : '']">
                <label class="module-dictionary-edit-item mode">
                  <span>处理方式</span>
                  <a-select
                      v-model:value="row.mode"
                      size="small"
                      :options="dictionaryModeOptions"
                      @change="handleModuleDictionaryModeChange(row)"
                  />
                </label>
                <label v-if="row.mode === 'use-existing'" class="module-dictionary-edit-item existing">
                  <span>系统字典</span>
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
                      @change="markModuleDictionaryDirty(row)"
                  />
                </label>
                <label v-else class="module-dictionary-edit-item code">
                  <span>字典编码</span>
                  <a-input
                      v-model:value="row.code"
                      size="small"
                      :disabled="row.mode === 'ignore'"
                      placeholder="表名_字段名"
                      @change="markModuleDictionaryDirty(row)"
                  />
                </label>
                <label v-if="row.mode !== 'use-existing'" class="module-dictionary-edit-item name">
                  <span>字典名称</span>
                  <a-input
                      v-model:value="row.name"
                      size="small"
                      :disabled="row.mode === 'ignore'"
                      placeholder="表单标题-字段标题"
                      @change="markModuleDictionaryDirty(row)"
                  />
                </label>
              </div>

              <div v-if="row.mode === 'create'" class="module-dictionary-items-editor">
                <div class="module-dictionary-edit-label">字典项，每行一个：value text</div>
                <a-textarea
                    v-model:value="row.itemsText"
                    :auto-size="{ minRows: 2, maxRows: 6 }"
                    placeholder="例如：normal 普通"
                    @change="markModuleDictionaryDirty(row)"
                />
              </div>
            </div>
          </div>
        </section>

        <section class="module-section">
          <div class="module-section-head">
            <div>
              <div class="module-section-title">模块级批量体检</div>
              <div class="module-section-subtitle">汇总检查模块结构、已确认表单草稿、字典确认状态和跨表字段引用；通过后再进入批量正式化预览。</div>
            </div>
            <div class="module-batch-check-actions">
              <div class="module-batch-check-tags">
                <a-tag :color="moduleBatchCheckSummary.error > 0 ? 'red' : 'green'">错误 {{ moduleBatchCheckSummary.error }}</a-tag>
                <a-tag :color="moduleBatchCheckSummary.warning > 0 ? 'orange' : 'green'">警告 {{ moduleBatchCheckSummary.warning }}</a-tag>
                <a-tag :color="moduleBatchCheckSummary.suggestion > 0 ? 'blue' : 'green'">建议 {{ moduleBatchCheckSummary.suggestion }}</a-tag>
              </div>
              <a-button
                  size="small"
                  type="primary"
                  :disabled="!canRunModuleBatchCheck"
                  @click="runModuleBatchCheck"
              >执行模块级体检</a-button>
            </div>
          </div>

          <div v-if="!moduleBatchCheckCheckedAt" class="module-batch-check-empty">
            <div class="module-empty-title">待执行模块级体检</div>
            <div class="module-empty-desc">建议在表单草稿全部确认、字典确认已应用后执行。体检只做检查和汇总，不会修改草稿。</div>
          </div>
          <div v-else class="module-batch-check-result">
            <div class="module-batch-check-summary">
              <div>
                <span>检查时间</span>
                <strong>{{ moduleBatchCheckCheckedAt }}</strong>
              </div>
              <div>
                <span>表单</span>
                <strong>{{ moduleConfirmedFormDraftCount }}/{{ moduleForms.length }} 已确认</strong>
              </div>
              <div>
                <span>字典</span>
                <strong>{{ moduleDictionaryStepText }}</strong>
              </div>
              <div>
                <span>结论</span>
                <strong>{{ moduleBatchCheckConclusion }}</strong>
              </div>
            </div>

            <a-empty
                v-if="moduleBatchCheckIssues.length === 0"
                description="模块级体检通过，未发现阻断问题。"
            />
            <div v-else class="module-batch-check-issue-list">
              <div
                  v-for="issue in moduleBatchCheckIssues.slice(0, 12)"
                  :key="issue.key"
                  class="module-batch-check-issue-item"
              >
                <a-tag :color="getIssueTagColor(issue.level)">{{ getIssueLevelText(issue.level) }}</a-tag>
                <div>
                  <strong>{{ issue.title || issue.description || issue.suggestion }}</strong>
                  <span>
                    <template v-if="issue.formTitle">{{ issue.formTitle }} / </template>
                    <template v-if="issue.fieldLabel">{{ issue.fieldLabel }} / </template>
                    {{ issue.description || issue.suggestion || issue.id }}
                  </span>
                </div>
              </div>
              <div v-if="moduleBatchCheckIssues.length > 12" class="module-batch-check-more">
                还有 {{ moduleBatchCheckIssues.length - 12 }} 条结果，完整内容已输出到控制台 ModuleBatchCheckIssues。
              </div>
            </div>
          </div>
        </section>

        <section class="module-section">
          <div class="module-section-head">
            <div>
              <div class="module-section-title">模块级批量正式化预览</div>
              <div class="module-section-subtitle">预览字典、表单和后续保存顺序，只生成计划，不会创建字典或保存正式表单。</div>
            </div>
            <div class="module-formalize-preview-actions">
              <div class="module-formalize-preview-tags">
                <a-tag :color="moduleFormalizePreviewSummary.formCount > 0 ? 'cyan' : 'default'">表单 {{ moduleFormalizePreviewSummary.formCount }}</a-tag>
                <a-tag :color="moduleFormalizePreviewSummary.dictionaryCreateCount > 0 ? 'green' : 'default'">新建字典 {{ moduleFormalizePreviewSummary.dictionaryCreateCount }}</a-tag>
                <a-tag :color="moduleFormalizePreviewSummary.blockingIssueCount > 0 ? 'red' : 'green'">阻断 {{ moduleFormalizePreviewSummary.blockingIssueCount }}</a-tag>
              </div>
              <a-button
                  size="small"
                  type="primary"
                  :disabled="!canBuildModuleFormalizePreview"
                  @click="previewModuleFormalizeState"
              >生成正式化预览</a-button>
            </div>
          </div>

          <div v-if="!moduleFormalizePreview" class="module-formalize-preview-empty">
            <div class="module-empty-title">待生成正式化预览</div>
            <div class="module-empty-desc">{{ moduleFormalizePreviewEmptyText }}</div>
          </div>
          <div v-else class="module-formalize-preview-result">
            <div class="module-formalize-summary">
              <div>
                <span>确认表单</span>
                <strong>{{ moduleFormalizePreview.summary.confirmedFormCount }}/{{ moduleFormalizePreview.summary.formCount }}</strong>
              </div>
              <div>
                <span>字典处理</span>
                <strong>新建 {{ moduleFormalizePreview.summary.dictionaryCreateCount }}，已有 {{ moduleFormalizePreview.summary.dictionaryExistingCount }}，忽略 {{ moduleFormalizePreview.summary.dictionaryIgnoreCount }}</strong>
              </div>
              <div>
                <span>执行步骤</span>
                <strong>{{ moduleFormalizePreview.summary.readyStepCount }}/{{ moduleFormalizePreview.summary.stepCount }} 可执行</strong>
              </div>
              <div>
                <span>预览时间</span>
                <strong>{{ moduleFormalizePreview.generatedAt }}</strong>
              </div>
            </div>

            <div class="module-formalize-columns">
              <div class="module-formalize-card">
                <div class="module-formalize-card-title">字典计划</div>
                <div v-if="moduleFormalizePreview.dictionaries.length === 0" class="module-mini-empty">没有需要处理的系统字典。</div>
                <div
                    v-for="dictionary in moduleFormalizePreview.dictionaries.slice(0, 6)"
                    :key="dictionary.key"
                    class="module-formalize-row"
                >
                  <div>
                    <strong>{{ dictionary.name || dictionary.code || dictionary.fieldLabel || '未命名字典' }}</strong>
                    <span>{{ dictionary.code || dictionary.existingCode || '-' }} / {{ dictionary.fieldRefs.length }} 个字段</span>
                  </div>
                  <a-tag :color="dictionary.mode === 'create' ? 'green' : dictionary.mode === 'use-existing' ? 'blue' : 'default'">
                    {{ getDictionaryModeText(dictionary.mode) }}
                  </a-tag>
                </div>
                <div v-if="moduleFormalizePreview.dictionaries.length > 6" class="module-formalize-more">
                  还有 {{ moduleFormalizePreview.dictionaries.length - 6 }} 个字典，完整内容已输出到控制台 ModuleFormalizePreview。
                </div>
              </div>

              <div class="module-formalize-card">
                <div class="module-formalize-card-title">表单计划</div>
                <div
                    v-for="form in moduleFormalizePreview.forms.slice(0, 6)"
                    :key="form.id"
                    class="module-formalize-row"
                >
                  <div>
                    <strong>{{ form.title || form.formName || '未命名表单' }}</strong>
                    <span>{{ form.formName || form.id }} / 字段 {{ form.fieldCount }} / 查询 {{ form.queryCount }} / 列表 {{ form.listCount }}</span>
                  </div>
                  <a-tag :color="form.canSave ? 'green' : 'red'">{{ form.canSave ? '可保存' : '不可保存' }}</a-tag>
                </div>
                <div v-if="moduleFormalizePreview.forms.length > 6" class="module-formalize-more">
                  还有 {{ moduleFormalizePreview.forms.length - 6 }} 个表单，完整内容已输出到控制台 ModuleFormalizePreview。
                </div>
              </div>
            </div>

            <div class="module-formalize-card">
              <div class="module-formalize-card-title">执行顺序</div>
              <div class="module-formalize-step-list">
                <div
                    v-for="step in moduleFormalizePreview.applyPlan.steps.slice(0, 10)"
                    :key="step.id"
                    class="module-formalize-step"
                >
                  <a-tag :color="getFormalizeStepStatusColor(step.status)">{{ getFormalizeStepStatusText(step.status) }}</a-tag>
                  <span>{{ step.title }}</span>
                </div>
              </div>
              <div v-if="moduleFormalizePreview.applyPlan.steps.length > 10" class="module-formalize-more">
                还有 {{ moduleFormalizePreview.applyPlan.steps.length - 10 }} 个执行步骤，完整内容已输出到控制台 ModuleFormalizePreview。
              </div>
            </div>

            <div v-if="moduleFormalizePreview.applyPlan.blockingIssues.length > 0" class="module-formalize-blocking">
              <div class="module-formalize-card-title">阻断问题</div>
              <div
                  v-for="issue in moduleFormalizePreview.applyPlan.blockingIssues.slice(0, 6)"
                  :key="issue.key"
                  class="module-formalize-step"
              >
                <a-tag color="red">阻断</a-tag>
                <span>{{ issue.title || issue.description || issue.id }}</span>
              </div>
            </div>
          </div>
        </section>

        <section class="module-section">
          <div class="module-section-head">
            <div>
              <div class="module-section-title">模块级批量正式保存</div>
              <div class="module-section-subtitle">按正式化预览串行执行：先创建系统字典，再保存正式表单；同步表和菜单绑定暂作为后续步骤提示。</div>
            </div>
            <div class="module-formalize-save-actions">
              <div class="module-formalize-save-tags">
                <a-tag :color="moduleFormalizeSaveSummary.success > 0 ? 'green' : 'default'">成功 {{ moduleFormalizeSaveSummary.success }}</a-tag>
                <a-tag :color="moduleFormalizeSaveSummary.failed > 0 ? 'red' : 'default'">失败 {{ moduleFormalizeSaveSummary.failed }}</a-tag>
                <a-tag :color="moduleFormalizeSaving ? 'processing' : moduleFormalizeSaveResult?.success ? 'green' : 'default'">{{ moduleFormalizeSaveStepText }}</a-tag>
              </div>
              <a-button
                  size="small"
                  type="primary"
                  danger
                  :loading="moduleFormalizeSaving"
                  :disabled="!canRunModuleFormalizeSave"
                  @click="confirmRunModuleFormalizeSave"
              >批量正式保存</a-button>
            </div>
          </div>

          <div v-if="!moduleFormalizeSaveResult" class="module-formalize-save-empty">
            <div class="module-empty-title">待执行批量正式保存</div>
            <div class="module-empty-desc">{{ moduleFormalizeSaveEmptyText }}</div>
          </div>
          <div v-else class="module-formalize-save-result">
            <div class="module-formalize-save-summary">
              <div>
                <span>保存状态</span>
                <strong>{{ moduleFormalizeSaveResult.success ? '已完成' : moduleFormalizeSaving ? '执行中' : '未完成' }}</strong>
              </div>
              <div>
                <span>系统字典</span>
                <strong>{{ moduleFormalizeSaveSummary.dictionarySuccess }}/{{ moduleFormalizeSaveSummary.dictionaryTotal }} 已处理</strong>
              </div>
              <div>
                <span>正式表单</span>
                <strong>{{ moduleFormalizeSaveSummary.formSuccess }}/{{ moduleFormalizeSaveSummary.formTotal }} 已保存</strong>
              </div>
              <div>
                <span>结束时间</span>
                <strong>{{ moduleFormalizeSaveResult.finishedAt || '-' }}</strong>
              </div>
            </div>

            <div class="module-formalize-columns">
              <div class="module-formalize-card">
                <div class="module-formalize-card-title">字典执行结果</div>
                <div v-if="moduleFormalizeSaveResult.dictionaries.length === 0" class="module-mini-empty">没有需要处理的系统字典。</div>
                <div
                    v-for="dictionary in moduleFormalizeSaveResult.dictionaries"
                    :key="dictionary.key"
                    class="module-formalize-row"
                >
                  <div>
                    <strong>{{ dictionary.name || dictionary.code || dictionary.key }}</strong>
                    <span>{{ dictionary.message || dictionary.code || '-' }}</span>
                  </div>
                  <a-tag :color="getFormalizeSaveStatusColor(dictionary.status)">{{ getFormalizeSaveStatusText(dictionary.status) }}</a-tag>
                </div>
              </div>

              <div class="module-formalize-card">
                <div class="module-formalize-card-title">表单保存结果</div>
                <div
                    v-for="form in moduleFormalizeSaveResult.forms"
                    :key="form.id"
                    class="module-formalize-row"
                >
                  <div>
                    <strong>{{ form.title || form.formName || form.id }}</strong>
                    <span>{{ form.message || form.formName || '-' }}</span>
                  </div>
                  <a-tag :color="getFormalizeSaveStatusColor(form.status)">{{ getFormalizeSaveStatusText(form.status) }}</a-tag>
                </div>
              </div>
            </div>

            <div class="module-formalize-card">
              <div class="module-formalize-card-title">执行日志</div>
              <div class="module-formalize-step-list">
                <div
                    v-for="step in moduleFormalizeSaveResult.steps"
                    :key="step.id"
                    class="module-formalize-step"
                >
                  <a-tag :color="getFormalizeSaveStatusColor(step.status)">{{ getFormalizeSaveStatusText(step.status) }}</a-tag>
                  <span>{{ step.title }}<template v-if="step.message">：{{ step.message }}</template></span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="module-section">
          <div class="module-section-head">
            <div>
              <div class="module-section-title">后续工作区</div>
              <div class="module-section-subtitle">展示模块级字典确认、批量体检、正式化预览和后续保存结果的整体进度。</div>
            </div>
          </div>
          <div class="module-workbench-list">
            <div v-for="item in workbenchItems" :key="item.title" class="module-workbench-item">
              <span class="module-workbench-title">{{ item.title }}</span>
              <span class="module-workbench-state">{{ item.state }}</span>
            </div>
          </div>
        </section>

        <a-collapse class="module-advanced" :bordered="false">
          <a-collapse-panel key="debug" header="高级信息">
            <div class="module-debug-actions">
              <a-button size="small" @click="printModuleMaterial">输出 ModuleMaterial</a-button>
              <a-button size="small" @click="printModuleDesign">输出 ModuleDesignDSL</a-button>
            </div>
            <pre class="module-json-preview">{{ moduleMaterialPreviewText }}</pre>
          </a-collapse-panel>
        </a-collapse>
      </div>
    </div>
  </a-drawer>

  <a-modal
      v-model:visible="moduleFormDraftPreviewVisible"
      :title="moduleFormDraftPreviewTitle"
      :width="980"
      :footer="null"
      :z-index="2100"
      @cancel="closeModuleFormDraftPreview"
  >
    <div v-if="moduleFormDraftPreview" class="module-draft-preview">
      <div class="module-draft-preview-head">
        <div>
          <div class="module-draft-preview-title">{{ moduleFormDraftPreview.title || '未命名表单' }}</div>
          <div class="module-draft-preview-sub">
            {{ moduleFormDraftPreview.formName || moduleFormDraftPreview.id || '-' }}
            <span v-if="moduleFormDraftPreviewDsl"> / {{ moduleFormDraftPreviewDslFormName }}</span>
          </div>
        </div>
        <a-tag :color="moduleFormDraftPreviewStatus.color">{{ moduleFormDraftPreviewStatus.text }}</a-tag>
      </div>

      <div class="module-draft-preview-summary">
        <div>
          <span>字段</span>
          <strong>{{ moduleFormDraftPreviewSummary.fieldCount }}</strong>
        </div>
        <div>
          <span>列表</span>
          <strong>{{ moduleFormDraftPreviewSummary.listCount }}</strong>
        </div>
        <div>
          <span>查询</span>
          <strong>{{ moduleFormDraftPreviewSummary.queryCount }}</strong>
        </div>
        <div>
          <span>字典</span>
          <strong>{{ moduleFormDraftPreviewSummary.dictionaryCount }}</strong>
        </div>
        <div>
          <span>问题</span>
          <strong>{{ moduleFormDraftPreviewSummary.issueCount }}</strong>
        </div>
      </div>

      <a-empty
          v-if="!moduleFormDraftPreviewDsl"
          description="该表单还没有生成 FormDesignDSL，请先点击生成草稿。"
      />
      <template v-else>
        <div class="module-draft-preview-card">
          <div class="module-draft-preview-card-title">字段明细</div>
          <div class="module-draft-field-table">
            <div class="module-draft-field-row head">
              <span>字段</span>
              <span>类型</span>
              <span>物理类型</span>
              <span>Java类型</span>
              <span>必填</span>
              <span>列表</span>
              <span>查询</span>
              <span>栅格</span>
            </div>
            <div
                v-for="field in moduleFormDraftPreviewFields"
                :key="field.key"
                class="module-draft-field-row"
            >
              <span class="module-draft-field-main">
                <strong>{{ field.label }}</strong>
                <em>{{ field.name }}</em>
              </span>
              <span>{{ field.typeText }}</span>
              <span>{{ field.jdbcType || '-' }}</span>
              <span>{{ field.javaType || '-' }}</span>
              <span>{{ formatBooleanText(field.required) }}</span>
              <span>{{ formatBooleanText(field.isList) }}</span>
              <span>{{ formatBooleanText(field.isQuery) }}</span>
              <span>{{ field.span || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="module-draft-preview-card">
          <div class="module-draft-preview-card-title">字典建议</div>
          <a-empty
              v-if="moduleFormDraftPreviewDictionaries.length === 0"
              description="当前表单暂无字典建议。"
          />
          <div v-else class="module-draft-dictionary-list">
            <div
                v-for="dictionary in moduleFormDraftPreviewDictionaries"
                :key="dictionary.key"
                class="module-draft-dictionary-item"
            >
              <div>
                <strong>{{ dictionary.name || dictionary.code || dictionary.fieldLabel }}</strong>
                <span>{{ dictionary.fieldLabel }} / {{ dictionary.fieldName || '-' }}</span>
              </div>
              <a-tag>{{ getDictionaryModeText(dictionary.mode) }}</a-tag>
              <span>{{ dictionary.code || '-' }}</span>
              <span class="module-draft-dictionary-items">{{ dictionary.itemsText }}</span>
            </div>
          </div>
        </div>

        <div class="module-draft-preview-card">
          <div class="module-draft-preview-card-title">检查结果</div>
          <a-empty
              v-if="moduleFormDraftPreviewIssues.length === 0"
              description="未发现结构检查提示。"
          />
          <div v-else class="module-draft-issue-list">
            <div
                v-for="issue in moduleFormDraftPreviewIssues"
                :key="issue.key"
                class="module-draft-issue-item"
            >
              <a-tag :color="getIssueTagColor(issue.level)">{{ getIssueLevelText(issue.level) }}</a-tag>
              <span>{{ issue.fieldLabel ? `${issue.fieldLabel}：` : '' }}{{ issue.title || issue.description || issue.suggestion }}</span>
            </div>
          </div>
        </div>
      </template>

      <div class="module-draft-preview-card">
        <div class="module-draft-preview-card-title">生成信息</div>
        <div class="module-draft-meta-grid">
          <div>
            <span>请求ID</span>
            <strong>{{ formatPreviewValue(moduleFormDraftPreviewMeta.requestId) }}</strong>
          </div>
          <div>
            <span>模型</span>
            <strong>{{ formatPreviewValue(moduleFormDraftPreviewMeta.model) }}</strong>
          </div>
          <div>
            <span>Prompt</span>
            <strong>{{ formatPreviewValue(moduleFormDraftPreviewMeta.promptVersion) }}</strong>
          </div>
          <div>
            <span>耗时</span>
            <strong>{{ formatPreviewValue(moduleFormDraftPreviewMeta.elapsedText) }}</strong>
          </div>
          <div>
            <span>缓存</span>
            <strong>{{ moduleFormDraftPreviewMeta.cacheHitText }}</strong>
          </div>
          <div>
            <span>生成时间</span>
            <strong>{{ formatPreviewValue(moduleFormDraftPreviewMeta.generatedAt) }}</strong>
          </div>
          <div>
            <span>确认时间</span>
            <strong>{{ formatPreviewValue(moduleFormDraftPreviewMeta.confirmedAt) }}</strong>
          </div>
        </div>
      </div>

      <div class="module-draft-preview-footer">
        <a-button @click="closeModuleFormDraftPreview">关闭</a-button>
        <a-button
            :loading="moduleFormDraftPreviewStatusKey === 'generating'"
            :disabled="moduleFormDraftBatchGenerating || moduleFormDraftPreviewStatusKey === 'generating'"
            @click="regeneratePreviewModuleFormDraft"
        >重新生成</a-button>
        <a-button
            type="primary"
            :disabled="!moduleFormDraftPreviewCanConfirm"
            @click="confirmPreviewModuleFormDraft"
        >{{ moduleFormDraftPreviewConfirmText }}</a-button>
      </div>
    </div>
    <a-empty v-else description="暂无表单草稿可预览。"/>
  </a-modal>
</template>

<script setup>
import {computed, h, nextTick, ref, watch} from "vue";
import {message, Modal} from "ant-design-vue";
import {postAction} from "@/api/action";
import {saveDataAction} from "@/api/api";
import {defaultListButtonArr, defaultRowButtonArr, treeTableListButtonArr, treeTableRowButtonArr} from "@/views/gen/genTableExt/formStaticConfig";
import {validateFormDesignDslSchema} from "@/views/gen/genTableExt/ai/dslSchema";
import {convertDslToDesignerStatePatch, enrichDslModalSelectFields} from "@/views/gen/genTableExt/ai/dslToDesignerState";
import {buildFormalizeSubmitDataPreview} from "@/views/gen/genTableExt/ai/formalizePreview";
import {
  MODULE_MATERIAL_PAGE_TYPE_OPTIONS,
  createEmptyModuleMaterial,
  summarizeModuleMaterialIssues,
  validateModuleMaterialSchema,
} from "@/views/gen/genTableExt/ai/moduleMaterialSchema";
import {
  createEmptyModuleDesign,
  summarizeModuleDesignIssues,
  validateModuleDesignSchema,
} from "@/views/gen/genTableExt/ai/moduleDesignSchema";
import {
  extractUrlModuleMaterial,
  generateRemoteModuleFormDesignDsl,
  recognizeFileModuleMaterial,
  recognizeRemoteModuleMaterial,
} from "@/views/gen/genTableExt/ai/remoteModuleMaterialProvider";
import {
  applyDictionaryConfirmationsToDsl,
  buildSysDictionarySaveData,
  createDictionaryCode,
  createDictionaryDisplayName,
  createDictionaryEnglishName,
  createDictionaryConfirmationMap,
  createDictionaryName,
  dictionaryItemsToText,
  normalizeDictionaryItems,
  parseDictionaryItemsText,
} from "@/views/gen/genTableExt/ai/dictionarySuggestions";
import {
  applyExistingDictionaryMatch,
  clearSystemDictionaryCodeMapCache,
  collectDictionarySuggestionCodes,
  loadSystemDictionaryCodeMap,
} from "@/views/gen/genTableExt/ai/systemDictionaryMatcher";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  formModel: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['update:visible'])

const sourceType = ref('text')
const sourceText = ref('')
const sourceUrl = ref('')
const moduleUrlDynamicRender = ref(false)
const moduleUrlExtractionResult = ref(null)
const moduleUrlExtracting = ref(false)
const moduleUrlSelectedPageMap = ref({})
const moduleUrlExtractionSignature = ref('')
const moduleFileList = ref([])
const moduleMaterialResult = ref(null)
const moduleMaterialRecognizing = ref(false)
const moduleBlueprintConfirmed = ref(false)
const moduleFormDraftMap = ref({})
const moduleDictionaryConfirmations = ref({})
const moduleFormDraftPreviewVisible = ref(false)
const moduleFormDraftPreviewTarget = ref(null)
const moduleFormDraftBatchGenerating = ref(false)
const moduleFormDraftBatchMode = ref('')
const moduleFormDraftBatchTotal = ref(0)
const moduleFormDraftBatchDone = ref(0)
const moduleFormDraftBatchSuccess = ref(0)
const moduleFormDraftBatchFailed = ref(0)
const moduleFormDraftBatchSkipped = ref(0)
const moduleFormDraftBatchCurrentTitle = ref('')
const moduleBatchCheckCheckedAt = ref('')
const moduleFormalizePreview = ref(null)
const moduleFormalizeSaving = ref(false)
const moduleFormalizeSaveResult = ref(null)
const moduleDictionaryExistingCodeMap = ref({})
const moduleDictionaryExistingMatching = ref(false)
const moduleDictionaryExistingLookupKey = ref('')
const moduleName = ref('')
const moduleTitle = ref('')
const moduleScene = ref('normal')
const moduleNameTouched = ref(false)
const moduleTitleTouched = ref(false)

const excelFileNamePattern = /\.(xlsx|xls)$/i
const wordFileNamePattern = /\.docx$/i
const pdfFileNamePattern = /\.pdf$/i
const imageFileNamePattern = /\.(png|jpg|jpeg|webp)$/i
const WORD_PDF_MAX_FILE_SIZE = 10 * 1024 * 1024
const EXCEL_MAX_FILE_SIZE = 15 * 1024 * 1024
const MODULE_BLUEPRINT_MAX_FORMS = 20

const toArray = (value) => Array.isArray(value) ? value : []
const normalizeText = (value) => String(value || '').trim()

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

const modulePageTypeTextMap = {
  list_form: '列表+表单',
  form: '录入表单',
  list: '列表页',
  detail: '详情页',
  config: '配置页',
  workflow: '流程页',
  dashboard: '看板页',
  report: '报表页',
  external: '外部页面',
  unknown: '待确认',
}

const pageTypeOptions = MODULE_MATERIAL_PAGE_TYPE_OPTIONS
    .filter(Boolean)
    .map(value => ({
      value,
      label: modulePageTypeTextMap[value] || value,
    }))

const SELECT_DICT_DATA_REFRESH_EVENT = 'u-select-dict-data-refresh'
const FORMALIZE_SAVE_EXECUTABLE_TYPES = ['create-dictionary', 'save-form']

const moduleFormDraftStatusMap = {
  pending: {text: '待生成', color: 'default'},
  ready: {text: '已准备', color: 'blue'},
  generating: {text: '生成中', color: 'processing'},
  generated: {text: '已生成', color: 'green'},
  confirmed: {text: '已确认', color: 'cyan'},
  failed: {text: '有问题', color: 'red'},
}

const createModuleRecognizeFailureResult = (error = {}) => ({
  material: null,
  parseIssues: [{
    id: 'module-recognition-request-failed',
    code: error.code || 'AI_MODULE_RECOGNITION_REQUEST_FAILED',
    level: 'error',
    title: 'AI 模块蓝图识别失败',
    description: error.message || 'AI 请求失败，请稍后重试或检查模型配置。',
    suggestion: '可以缩短材料、换用更快的模型，或稍后重新识别。',
    fixable: false,
    meta: {
      requestId: error.requestId || '',
    },
  }],
  schemaIssues: [],
  summary: {
    total: 1,
    error: 1,
    warning: 0,
    suggestion: 0,
    fixable: 0,
  },
  remoteResult: error.result || null,
  success: false,
  errorCode: error.code || 'AI_MODULE_RECOGNITION_REQUEST_FAILED',
  message: error.message || 'AI 请求失败，请稍后重试或检查模型配置。',
  provider: 'remote',
  model: '',
  promptVersion: '',
  requestId: error.requestId || '',
  elapsedMs: 0,
  extraction: null,
  rawOutputPreview: '',
})

const sourceOptions = [
  {value: 'text', label: '文字说明', description: '从模块需求描述开始'},
  {value: 'word', label: 'Word / PDF', description: '从制度或说明材料开始'},
  {value: 'excel', label: 'Excel', description: '从多张表格材料开始'},
  {value: 'url', label: '原型 URL', description: '从原型页面采集开始'},
]

const fileSourceConfigMap = {
  word: {
    title: '上传 Word / PDF 模块材料',
    description: '支持 .docx 和文本型 .pdf；扫描 PDF 需要后续 OCR 能力，本任务先处理可读取文字的文件。',
    buttonText: '选择 Word / PDF',
    accept: '.docx,.pdf',
    allowedTypes: ['word', 'pdf'],
    maxSize: WORD_PDF_MAX_FILE_SIZE,
    maxSizeText: '10MB',
    unsupportedText: 'Word / PDF 来源仅支持 .docx 和 .pdf 文件。',
    placeholder: '可补充文件中没有写清楚的页面边界、表单关系、字典复用要求，例如：合同状态复用已有字典，关联合同指向合同基本信息。',
    emptyHint: '请选择 .docx 或文本型 .pdf 文件，或先填写补充说明。',
    textOnlyHint: '已填写补充说明，可先按文字材料识别；选择 Word / PDF 文件后效果更完整。',
  },
  excel: {
    title: '上传 Excel 模块材料',
    description: '支持 .xlsx 和 .xls，多 Sheet、单 Sheet 内多个表格块会作为多个页面和表单候选材料一起识别。',
    buttonText: '选择 Excel',
    accept: '.xlsx,.xls',
    allowedTypes: ['excel'],
    maxSize: EXCEL_MAX_FILE_SIZE,
    maxSizeText: '15MB',
    unsupportedText: 'Excel 来源仅支持 .xlsx 和 .xls 文件。',
    placeholder: '可补充 Sheet 与页面或表单的对应关系，例如：合同台账 Sheet 是主表，履约记录 Sheet 是子表。',
    emptyHint: '请选择 .xlsx 或 .xls 文件，或先填写补充说明。',
    textOnlyHint: '已填写补充说明，可先按文字材料识别；选择 Excel 文件后效果更完整。',
  },
}

function isFileSourceTypeValue(value = '') {
  return Boolean(fileSourceConfigMap[value])
}

const workbenchItems = computed(() => [
  {title: '模块级字典确认', state: moduleDictionaryStepText.value},
  {title: '批量体检', state: moduleBatchCheckStepText.value},
  {title: '批量正式化预览', state: moduleFormalizePreviewStepText.value},
  {title: '批量保存结果', state: moduleFormalizeSaveStepText.value},
])

const flowSteps = computed(() => [
  {key: 'source', index: 1, title: '输入来源', stateText: canRecognizeBlueprint.value ? '已填写' : '待填写', active: true, done: canRecognizeBlueprint.value},
  {key: 'blueprint', index: 2, title: '模块蓝图', stateText: moduleRecognitionStepText.value, active: Boolean(moduleMaterialResult.value), done: moduleBlueprintConfirmed.value},
  {key: 'forms', index: 3, title: '表单草稿', stateText: moduleFormDraftStepText.value, active: moduleBlueprintConfirmed.value, done: moduleFormDraftStepDone.value},
  {key: 'dictionaries', index: 4, title: '字典确认', stateText: moduleDictionaryStepText.value, active: moduleDictionaryStepActive.value, done: moduleDictionaryStepDone.value},
  {key: 'check', index: 5, title: '批量体检', stateText: moduleBatchCheckStepText.value, active: moduleBatchCheckStepActive.value, done: moduleBatchCheckStepDone.value},
  {key: 'preview', index: 6, title: '正式预览', stateText: moduleFormalizePreviewStepText.value, active: moduleFormalizePreviewStepActive.value, done: moduleFormalizePreviewStepDone.value},
  {key: 'save', index: 7, title: '保存结果', stateText: moduleFormalizeSaveStepText.value, active: moduleFormalizeSaveStepActive.value, done: moduleFormalizeSaveStepDone.value},
])

const sourceTypeText = computed(() => {
  return sourceOptions.find(item => item.value === sourceType.value)?.label || '文字说明'
})

const sourceTagColor = computed(() => {
  if (sourceType.value === 'url') {
    return 'purple'
  }
  if (sourceType.value === 'excel') {
    return 'green'
  }
  if (sourceType.value === 'word') {
    return 'orange'
  }
  return 'blue'
})

const currentFileSourceConfig = computed(() => fileSourceConfigMap[sourceType.value] || fileSourceConfigMap.word)

const moduleFileAccept = computed(() => currentFileSourceConfig.value.accept)

const currentFileUploadTitle = computed(() => currentFileSourceConfig.value.title)

const currentFileUploadDesc = computed(() => currentFileSourceConfig.value.description)

const currentFileUploadButtonText = computed(() => currentFileSourceConfig.value.buttonText)

const currentFileSupplementPlaceholder = computed(() => currentFileSourceConfig.value.placeholder)

const canRecognizeBlueprint = computed(() => {
  if (sourceType.value === 'url') {
    return Boolean(sourceUrl.value.trim() || sourceText.value.trim())
  }
  if (isFileSourceType.value) {
    return moduleFileList.value.length > 0 || Boolean(sourceText.value.trim())
  }
  return Boolean(sourceText.value.trim())
})

const moduleUrlCandidatePages = computed(() => toArray(moduleUrlExtractionResult.value?.pages))

const moduleUrlSelectedPages = computed(() => {
  return moduleUrlCandidatePages.value.filter(page => isModuleUrlPageSelected(page.id))
})

const moduleUrlCollectorText = computed(() => {
  const extraction = moduleUrlExtractionResult.value?.extraction || {}
  const mode = extraction.collectorMode || extraction.dynamicCollector || (moduleUrlDynamicRender.value ? 'playwright' : 'static')
  const status = extraction.collectorStatus || extraction.dynamicCollectorStatus || ''
  if (status === 'fallback') {
    return '已降级静态采集'
  }
  if (mode === 'axure') {
    return 'Axure 原型采集'
  }
  if (mode === 'playwright') {
    return '动态渲染采集'
  }
  return '静态 HTML 采集'
})

const moduleUrlCollectorTagColor = computed(() => {
  const extraction = moduleUrlExtractionResult.value?.extraction || {}
  const status = extraction.collectorStatus || extraction.dynamicCollectorStatus || ''
  const mode = extraction.collectorMode || extraction.dynamicCollector || ''
  if (status === 'fallback') {
    return 'orange'
  }
  if (mode === 'axure') {
    return 'cyan'
  }
  if (mode === 'playwright' || moduleUrlDynamicRender.value) {
    return 'purple'
  }
  return 'blue'
})

const moduleUrlCollectorDetailText = computed(() => {
  const extraction = moduleUrlExtractionResult.value?.extraction || {}
  const messageText = normalizeText(extraction.collectorMessage)
  if (!messageText) {
    return ''
  }
  return `采集提示：${messageText}`
})

const getUrlPageQualityColor = (page = {}) => {
  if (page.qualityLevel === 'high') {
    return 'green'
  }
  if (page.qualityLevel === 'medium') {
    return 'blue'
  }
  return 'default'
}

const getUrlPageQualityText = (page = {}) => {
  if (page.qualityLevel === 'high') {
    return `推荐 ${page.qualityScore ?? '-'}`
  }
  if (page.qualityLevel === 'medium') {
    return `可参考 ${page.qualityScore ?? '-'}`
  }
  return `较弱 ${page.qualityScore ?? '-'}`
}

const getModuleUrlExtractionSignature = () => JSON.stringify({
  sourceUrl: sourceUrl.value.trim(),
  supplementText: sourceText.value.trim(),
  maxPages: MODULE_BLUEPRINT_MAX_FORMS,
  maxChars: 16000,
  collectSameOriginLinks: true,
  dynamicRender: moduleUrlDynamicRender.value,
  timeoutSeconds: moduleUrlDynamicRender.value ? 20 : 8,
  dynamicWaitMillis: moduleUrlDynamicRender.value ? 2000 : 0,
})

const moduleUrlExtractionReady = computed(() => {
  return Boolean(moduleUrlExtractionResult.value?.success)
      && moduleUrlExtractionSignature.value === getModuleUrlExtractionSignature()
      && moduleUrlCandidatePages.value.length > 0
})

const moduleHasGeneratingFormDraft = computed(() => {
  return Object.values(moduleFormDraftMap.value || {}).some(draft => draft?.status === 'generating')
})

const moduleMainActionLoading = computed(() => moduleMaterialRecognizing.value || moduleUrlExtracting.value)

const moduleCreateBusy = computed(() => {
  return moduleMaterialRecognizing.value
      || moduleUrlExtracting.value
      || moduleFormDraftBatchGenerating.value
      || moduleFormalizeSaving.value
      || moduleHasGeneratingFormDraft.value
})

const moduleCreateHasContent = computed(() => {
  return sourceType.value !== 'text'
      || Boolean(sourceText.value.trim())
      || Boolean(sourceUrl.value.trim())
      || Boolean(moduleName.value.trim())
      || Boolean(moduleTitle.value.trim())
      || moduleScene.value !== 'normal'
      || Boolean(moduleUrlExtractionResult.value)
      || moduleFileList.value.length > 0
      || Boolean(moduleMaterialResult.value)
      || moduleBlueprintConfirmed.value
      || Object.keys(moduleFormDraftMap.value || {}).length > 0
      || Object.keys(moduleDictionaryConfirmations.value || {}).length > 0
      || Boolean(moduleBatchCheckCheckedAt.value)
      || Boolean(moduleFormalizePreview.value)
      || Boolean(moduleFormalizeSaveResult.value)
})

const flowStatusText = computed(() => {
  if (moduleBlueprintConfirmed.value) {
    return `模块蓝图已确认：${modulePages.value.length} 个页面、${moduleForms.value.length} 个表单，下一步可以逐个生成表单草稿。`
  }
  if (moduleMaterialReady.value) {
    return `已识别 ${modulePages.value.length} 个页面、${moduleForms.value.length} 个表单，请先确认或微调模块蓝图。`
  }
  if (moduleMaterialResult.value) {
    return 'AI 返回了结果，但模块蓝图结构还不完整，请查看结构提示后重试或补充材料。'
  }
  return canRecognizeBlueprint.value
      ? '输入来源已准备，后续任务会接入模块蓝图识别。'
      : '先填写模块材料来源，再进入模块蓝图识别。'
})

const recognizeBlueprintButtonText = computed(() => {
  if (moduleUrlExtracting.value) {
    return '采集原型页面中，请稍候'
  }
  if (moduleMaterialRecognizing.value) {
    return 'AI识别中，请稍候'
  }
  if (sourceType.value === 'url' && sourceUrl.value.trim() && !moduleUrlExtractionReady.value) {
    return moduleUrlDynamicRender.value ? '动态采集原型页面（需稍等）' : '采集原型页面（需稍等）'
  }
  if (sourceType.value === 'url' && moduleUrlExtractionReady.value) {
    return 'AI识别已选页面（需稍等）'
  }
  return 'AI识别模块蓝图（需稍等）'
})

const isFileSourceType = computed(() => isFileSourceTypeValue(sourceType.value))

const moduleInputHint = computed(() => {
  if (isFileSourceType.value) {
    if (moduleFileList.value.length > 0) {
      return moduleFileSummary.value
    }
    return sourceText.value.trim() ? currentFileSourceConfig.value.textOnlyHint : currentFileSourceConfig.value.emptyHint
  }
  if (sourceType.value === 'url') {
    return sourceUrl.value.trim() ? '已填写原型 URL；可先采集页面候选，再交给 AI 识别模块蓝图。' : '请输入原型 URL 或补充说明。'
  }
  return sourceText.value.trim() ? '已填写文字材料，可以开始识别。' : '请先输入模块需求说明。'
})

const moduleMaterialDraft = computed(() => {
  const material = createEmptyModuleMaterial(sourceType.value)
  material.source.name = moduleTitle.value
  material.source.url = sourceUrl.value.trim()
  material.source.rawText = sourceText.value.trim()
  material.module.nameHint = moduleName.value.trim()
  material.module.title = moduleTitle.value.trim()
  material.module.scene = moduleScene.value
  material.module.description = sourceText.value.trim()
  return material
})

const activeModuleMaterial = computed(() => moduleMaterialResult.value?.material || moduleMaterialDraft.value)

const clonePlain = (value) => JSON.parse(JSON.stringify(value || null))

const getModuleFormId = (form = {}, index = 0) => normalizeText(form.id) || `form_${String(index + 1).padStart(2, '0')}`

const getModuleMaterialFieldName = (field = {}) => normalizeText(field.name || field.nameHint || field.fieldName || field.key)

const getModuleMaterialFieldLabel = (field = {}) => normalizeText(field.label || field.title || field.fieldLabel || field.name)

const resolveModuleMaterialDictionaryField = (form = {}, dictionary = {}) => {
  const fields = toArray(form.fields)
  const explicitFieldName = normalizeText(dictionary.fieldName || dictionary.nameHint)
  const explicitFieldLabel = normalizeText(dictionary.fieldLabel || dictionary.label)
  const formName = normalizeText(form.nameHint || form.formName || form.id)
  const code = normalizeText(dictionary.code || dictionary.dictType || dictionary.existingCode)
  const findByName = (name = '') => fields.find(field => getModuleMaterialFieldName(field) === normalizeText(name))
  const findByLabel = (label = '') => fields.find(field => getModuleMaterialFieldLabel(field) === normalizeText(label))

  let matchedField = explicitFieldName ? findByName(explicitFieldName) : null
  if (!matchedField && explicitFieldLabel) {
    matchedField = findByLabel(explicitFieldLabel)
  }
  if (!matchedField && code && formName && code.startsWith(`${formName}_`)) {
    matchedField = findByName(code.substring(formName.length + 1))
  }
  if (!matchedField && explicitFieldName && fields.length === 0) {
    return {
      fieldName: explicitFieldName,
      fieldLabel: explicitFieldLabel || explicitFieldName,
    }
  }
  if (!matchedField) {
    return null
  }
  return {
    fieldName: getModuleMaterialFieldName(matchedField),
    fieldLabel: explicitFieldLabel || getModuleMaterialFieldLabel(matchedField),
  }
}

const normalizeModuleMaterialDictionaryHint = (form = {}, dictionary = {}) => {
  const resolvedField = resolveModuleMaterialDictionaryField(form, dictionary)
  if (!resolvedField?.fieldName) {
    return null
  }
  return {
    ...dictionary,
    fieldName: resolvedField.fieldName,
    fieldLabel: resolvedField.fieldLabel,
  }
}

const getFormMaterialDictionaryHints = (form = {}) => {
  return toArray(form.dictionaryHints)
      .map(dictionary => normalizeModuleMaterialDictionaryHint(form, dictionary))
      .filter(Boolean)
}

const collectModuleDictionaryDrafts = (forms = []) => {
  const dictionaryMap = {}
  toArray(forms).forEach((form, formIndex) => {
    const formId = getModuleFormId(form, formIndex)
    const formTitle = normalizeText(form.title)
    getFormMaterialDictionaryHints(form).forEach((dictionary, index) => {
      const code = normalizeText(dictionary.code || dictionary.existingCode || `${form.nameHint || formId}_dict_${index + 1}`)
      if (!code) return
      if (!dictionaryMap[code]) {
        dictionaryMap[code] = {
          id: `dict_${Object.keys(dictionaryMap).length + 1}`,
          mode: dictionary.mode || 'need-confirm',
          code,
          existingCode: dictionary.existingCode || '',
          name: dictionary.name || dictionary.title || code,
          items: toArray(dictionary.items).map(item => ({...item})),
          fieldRefs: [],
          confidence: dictionary.confidence || 0.8,
          source: 'module-material',
        }
      }
      dictionaryMap[code].fieldRefs.push({
        formId,
        formTitle,
        fieldName: dictionary.fieldName || '',
        fieldLabel: dictionary.fieldLabel || dictionary.label || '',
      })
    })
  })
  return Object.values(dictionaryMap)
}

const buildModuleDesignFormDraft = (form = {}, index = 0) => {
  const formId = getModuleFormId(form, index)
  const draft = moduleFormDraftMap.value[formId] || {}
  return {
    id: formId,
    pageId: form.pageId || '',
    title: form.title || '',
    formName: form.nameHint || form.formName || formId,
    status: draft.status || 'pending',
    dsl: draft.dsl || null,
    issues: toArray(draft.issues),
    generatedAt: draft.generatedAt || '',
    sourceMaterialForm: clonePlain(form),
    meta: {
      ...(draft.meta || {}),
      fieldCount: getFormFields(form).length,
      listFieldCount: getFormListFieldCount(form),
      queryFieldCount: getFormQueryFieldCount(form),
      dictionaryHintCount: getFormDictionaryHintCount(form, index),
    },
  }
}

const moduleDesignDraft = computed(() => {
  const design = createEmptyModuleDesign()
  const material = activeModuleMaterial.value || {}
  const forms = toArray(material.forms)
  design.module.name = moduleName.value.trim()
  design.module.title = moduleTitle.value.trim()
  design.module.scene = moduleScene.value
  design.module.description = sourceText.value.trim()
  design.menus = toArray(material.menus).map(menu => ({...menu}))
  design.pages = toArray(material.pages).map(page => ({
    ...page,
    formId: normalizeText(forms.find(form => normalizeText(form.pageId) === normalizeText(page.id))?.id),
    shouldGenerateForm: forms.some(form => normalizeText(form.pageId) === normalizeText(page.id)),
  }))
  design.forms = forms.map((form, index) => buildModuleDesignFormDraft(form, index))
  design.dictionaries = buildModuleDictionaryDesignRows(forms)
  design.relations = toArray(material.relations).map(relation => ({...relation}))
  design.applyPlan.steps = design.forms.map(form => ({
    type: 'save-form',
    targetId: form.id,
    formId: form.id,
    title: form.title,
    status: ['generated', 'confirmed'].includes(form.status) ? 'ready' : 'pending',
  }))
  design.applyPlan.warnings = moduleBlueprintConfirmed.value ? [] : ['模块蓝图尚未确认，暂不建议生成正式表单草稿。']
  design.applyPlan.blockingIssues = []
  design.meta.sourceType = sourceType.value
  design.meta.blueprintConfirmed = moduleBlueprintConfirmed.value
  design.meta.materialRequestId = moduleMaterialResult.value?.requestId || ''
  return design
})

const materialIssues = computed(() => {
  if (moduleMaterialResult.value && !moduleMaterialResult.value.material) {
    return toArray(moduleMaterialResult.value.parseIssues)
  }
  const material = moduleMaterialResult.value?.material || moduleMaterialDraft.value
  return toArray(moduleMaterialResult.value?.parseIssues).concat(validateModuleMaterialSchema(material))
})
const materialSummary = computed(() => summarizeModuleMaterialIssues(materialIssues.value))
const designIssues = computed(() => validateModuleDesignSchema(moduleDesignDraft.value))
const designSummary = computed(() => summarizeModuleDesignIssues(designIssues.value))
const normalizeModuleBatchCheckIssue = (issue = {}, index = 0, defaults = {}) => ({
  ...defaults,
  ...issue,
  key: issue.key || issue.id || `${defaults.source || 'module-batch-check'}:${issue.level || 'suggestion'}:${issue.title || issue.description || index}:${index}`,
  level: issue.level || defaults.level || 'suggestion',
  title: issue.title || defaults.title || '模块级体检提示',
  description: issue.description || issue.suggestion || defaults.description || '',
})
const summarizeModuleBatchCheckIssues = (issues = []) => {
  const summary = {
    total: issues.length,
    error: 0,
    warning: 0,
    suggestion: 0,
  }
  issues.forEach(issue => {
    if (summary[issue.level] !== undefined) {
      summary[issue.level] += 1
    } else {
      summary.suggestion += 1
    }
  })
  return summary
}
const moduleBatchCheckIssues = computed(() => {
  const issues = []
  const formTitleMap = {}
  moduleForms.value.forEach((form, index) => {
    const formId = getModuleFormId(form, index)
    formTitleMap[formId] = form.title || form.nameHint || formId
  })

  if (!moduleBlueprintConfirmed.value) {
    issues.push(normalizeModuleBatchCheckIssue({
      id: 'module-batch-blueprint-not-confirmed',
      level: 'error',
      title: '模块蓝图尚未确认',
      description: '请先确认模块蓝图，再执行模块级体检。',
      source: 'flow',
    }))
  }

  if (moduleForms.value.length === 0) {
    issues.push(normalizeModuleBatchCheckIssue({
      id: 'module-batch-forms-empty',
      level: 'error',
      title: '模块没有可生成表单',
      description: '请先在模块蓝图中保留至少一个需要生成的表单。',
      source: 'flow',
    }))
  }

  if (moduleForms.value.length > 0 && moduleConfirmedFormDraftCount.value < moduleForms.value.length) {
    issues.push(normalizeModuleBatchCheckIssue({
      id: 'module-batch-forms-not-confirmed',
      level: 'error',
      title: '表单草稿尚未全部确认',
      description: `当前已确认 ${moduleConfirmedFormDraftCount.value}/${moduleForms.value.length}，请逐个查看草稿并确认后再进入正式化预览。`,
      source: 'flow',
    }))
  }

  moduleForms.value.forEach((form, index) => {
    const formId = getModuleFormId(form, index)
    const formTitle = form.title || form.nameHint || formId
    const draft = getModuleFormDraft(form, index)
    if (!draft.dsl) {
      issues.push(normalizeModuleBatchCheckIssue({
        id: `module-batch-form-dsl-missing:${formId}`,
        level: 'error',
        title: '表单草稿缺少 FormDesignDSL',
        description: '请先生成该表单草稿。',
        formId,
        formTitle,
        source: 'form-draft',
      }))
      return
    }
    if (draft.status !== 'confirmed') {
      issues.push(normalizeModuleBatchCheckIssue({
        id: `module-batch-form-not-confirmed:${formId}`,
        level: 'error',
        title: '表单草稿尚未确认',
        description: '请打开查看草稿并点击确认草稿。',
        formId,
        formTitle,
        source: 'form-draft',
      }))
    }
    validateFormDesignDslSchema(draft.dsl).forEach((issue, issueIndex) => {
      issues.push(normalizeModuleBatchCheckIssue({
        ...issue,
        id: `module-batch-form-schema:${formId}:${issue.id || issueIndex}`,
        formId,
        formTitle,
        source: 'form-schema',
      }, issueIndex))
    })
  })

  designIssues.value
      .filter(issue => !normalizeText(issue.id).startsWith('module-design-form-dsl:'))
      .forEach((issue, index) => {
        issues.push(normalizeModuleBatchCheckIssue({
          ...issue,
          id: `module-batch-design:${issue.id || index}`,
          formTitle: issue.formId ? formTitleMap[issue.formId] || issue.formId : '',
          source: 'module-design',
        }, index))
      })

  moduleDictionaryConfirmationRows.value.forEach((row, index) => {
    const mode = normalizeModuleDictionaryMode(row.mode)
    if (!row.applied) {
      issues.push(normalizeModuleBatchCheckIssue({
        id: `module-batch-dictionary-not-applied:${row.key || index}`,
        level: 'error',
        title: '字典确认尚未应用到草稿',
        description: `${row.name || row.code || '未命名字典'} 还没有回写到表单草稿 DSL。`,
        source: 'dictionary',
      }))
    }
    if (mode === 'use-existing' && !normalizeText(row.existingCode)) {
      issues.push(normalizeModuleBatchCheckIssue({
        id: `module-batch-dictionary-existing-code-missing:${row.key || index}`,
        level: 'error',
        title: '已有字典未选择编码',
        description: `${row.name || row.code || '未命名字典'} 选择了使用已有字典，但还没有选择系统字典。`,
        source: 'dictionary',
      }))
    }
    if (mode === 'create' && !normalizeText(row.code)) {
      issues.push(normalizeModuleBatchCheckIssue({
        id: `module-batch-dictionary-code-missing:${row.key || index}`,
        level: 'error',
        title: '新建字典缺少编码',
        description: `${row.name || '未命名字典'} 缺少系统字典编码。`,
        source: 'dictionary',
      }))
    }
    if (mode === 'create' && parseDictionaryItemsText(row.itemsText).length === 0) {
      issues.push(normalizeModuleBatchCheckIssue({
        id: `module-batch-dictionary-items-empty:${row.key || index}`,
        level: 'warning',
        title: '新建字典缺少字典项',
        description: `${row.name || row.code || '未命名字典'} 没有字典项，后续批量创建前需要补充。`,
        source: 'dictionary',
      }))
    }
    if (mode !== 'ignore' && toArray(row.fieldRefs).length === 0) {
      issues.push(normalizeModuleBatchCheckIssue({
        id: `module-batch-dictionary-field-refs-empty:${row.key || index}`,
        level: 'warning',
        title: '字典缺少关联字段',
        description: `${row.name || row.code || '未命名字典'} 没有关联到任何表单字段。`,
        source: 'dictionary',
      }))
    }
  })

  return issues.map((issue, index) => normalizeModuleBatchCheckIssue(issue, index))
})
const moduleBatchCheckSummary = computed(() => summarizeModuleBatchCheckIssues(moduleBatchCheckIssues.value))
const moduleBatchCheckStepActive = computed(() => moduleDictionaryStepDone.value || Boolean(moduleBatchCheckCheckedAt.value))
const moduleBatchCheckStepDone = computed(() => Boolean(moduleBatchCheckCheckedAt.value) && moduleBatchCheckSummary.value.error === 0)
const moduleBatchCheckStepText = computed(() => {
  if (!moduleBlueprintConfirmed.value) return '待蓝图'
  if (!moduleDictionaryStepDone.value && !moduleBatchCheckCheckedAt.value) return '待字典'
  if (!moduleBatchCheckCheckedAt.value) return '待体检'
  if (moduleBatchCheckSummary.value.error > 0) return `错误 ${moduleBatchCheckSummary.value.error}`
  if (moduleBatchCheckSummary.value.warning > 0) return `警告 ${moduleBatchCheckSummary.value.warning}`
  if (moduleBatchCheckSummary.value.suggestion > 0) return `建议 ${moduleBatchCheckSummary.value.suggestion}`
  return '已通过'
})
const moduleBatchCheckConclusion = computed(() => {
  if (moduleBatchCheckSummary.value.error > 0) {
    return '存在阻断问题'
  }
  if (moduleBatchCheckSummary.value.warning > 0) {
    return '可继续，但建议处理警告'
  }
  if (moduleBatchCheckSummary.value.suggestion > 0) {
    return '基本通过，有少量建议'
  }
  return '体检通过'
})
const moduleFormalizeBlockingIssues = computed(() => moduleBatchCheckIssues.value.filter(issue => issue.level === 'error'))
const moduleFormalizeWarningIssues = computed(() => moduleBatchCheckIssues.value.filter(issue => issue.level === 'warning' || issue.level === 'suggestion'))
const getDslFields = (dsl = {}) => toArray(dsl?.fields)
const getDslListFieldCount = (dsl = {}) => getDslFields(dsl).filter(field => isTruthyValue(field?.isList) || isTruthyValue(field?.list?.show)).length
const getDslQueryFieldCount = (dsl = {}) => getDslFields(dsl).filter(field => isTruthyValue(field?.isQuery) || isTruthyValue(field?.query?.show)).length
const createModuleFormalizeCurrentSummary = () => ({
  formCount: moduleDesignDraft.value.forms.length,
  confirmedFormCount: moduleConfirmedFormDraftCount.value,
  dictionaryCount: moduleDictionaryStats.value.total,
  dictionaryCreateCount: moduleDictionaryStats.value.create,
  dictionaryExistingCount: moduleDictionaryStats.value.existing,
  dictionaryIgnoreCount: moduleDictionaryStats.value.ignore,
  stepCount: 0,
  readyStepCount: 0,
  blockingIssueCount: moduleFormalizeBlockingIssues.value.length,
  warningCount: moduleFormalizeWarningIssues.value.length,
})
const moduleFormalizePreviewSummary = computed(() => moduleFormalizePreview.value?.summary || createModuleFormalizeCurrentSummary())
const moduleFormalizePreviewStepActive = computed(() => moduleBatchCheckStepDone.value || Boolean(moduleFormalizePreview.value))
const moduleFormalizePreviewStepDone = computed(() => Boolean(moduleFormalizePreview.value) && moduleFormalizePreviewSummary.value.blockingIssueCount === 0)
const moduleFormalizePreviewStepText = computed(() => {
  if (!moduleBatchCheckCheckedAt.value) return '待体检'
  if (moduleBatchCheckSummary.value.error > 0) return `阻断 ${moduleBatchCheckSummary.value.error}`
  if (!moduleFormalizePreview.value) return '可预览'
  if (moduleFormalizePreviewSummary.value.blockingIssueCount > 0) return `阻断 ${moduleFormalizePreviewSummary.value.blockingIssueCount}`
  if (moduleFormalizePreviewSummary.value.warningCount > 0) return `已预览，提示 ${moduleFormalizePreviewSummary.value.warningCount}`
  return '已预览'
})
const canBuildModuleFormalizePreview = computed(() => {
  return moduleBatchCheckStepDone.value
      && moduleDictionaryStepDone.value
      && moduleConfirmedFormDraftCount.value === moduleForms.value.length
      && moduleForms.value.length > 0
      && !moduleFormDraftBatchGenerating.value
})
const moduleFormalizePreviewEmptyText = computed(() => {
  if (!moduleBlueprintConfirmed.value) return '请先识别并确认模块蓝图。'
  if (moduleForms.value.length === 0) return '当前模块没有可正式化的表单。'
  if (moduleConfirmedFormDraftCount.value < moduleForms.value.length) return `请先确认全部表单草稿，当前已确认 ${moduleConfirmedFormDraftCount.value}/${moduleForms.value.length}。`
  if (!moduleDictionaryStepDone.value) return '请先完成模块级字典确认，并应用到表单草稿。'
  if (!moduleBatchCheckCheckedAt.value) return '请先执行模块级批量体检。'
  if (moduleBatchCheckSummary.value.error > 0) return '模块级体检仍有阻断错误，请处理后再生成正式化预览。'
  return '体检已通过，可以生成正式化预览。'
})
const createModuleFormalizeDictionaryPlans = () => {
  return moduleDictionaryConfirmationRows.value.map((rawRow, index) => {
    const row = applyExistingDictionaryMatch(rawRow, moduleDictionaryExistingCodeMap.value)
    const mode = normalizeModuleDictionaryMode(row.mode)
    const items = row.itemsText !== undefined
        ? parseDictionaryItemsText(row.itemsText)
        : normalizeDictionaryItems(row.items)
    const effectiveCode = mode === 'use-existing'
        ? normalizeText(row.existingCode)
        : normalizeText(row.code)
    return {
      key: row.key || row.id || `dictionary_${index + 1}`,
      id: row.id || `module_dictionary_${index + 1}`,
      mode,
      code: mode === 'ignore' ? '' : effectiveCode,
      existingCode: mode === 'use-existing' ? effectiveCode : '',
      name: row.name || effectiveCode || row.fieldLabel || `字典${index + 1}`,
      nameEn: row.nameEn || row.name_en || effectiveCode,
      items,
      itemCount: items.length,
      fieldRefs: toArray(row.fieldRefs).map(ref => ({
        formId: normalizeText(ref.formId),
        formTitle: normalizeText(ref.formTitle),
        fieldName: normalizeText(ref.fieldName),
        fieldLabel: normalizeText(ref.fieldLabel),
      })),
      applied: row.applied === true,
      source: row.source || 'module-dictionary-confirmed',
    }
  })
}
const createModuleFormalizeFormPlans = () => {
  return moduleDesignDraft.value.forms.map((form, index) => {
    const dsl = form.dsl || null
    const fields = getDslFields(dsl)
    const schemaIssues = dsl ? validateFormDesignDslSchema(dsl) : []
    const schemaErrorCount = schemaIssues.filter(issue => issue.level === 'error').length
    return {
      id: form.id || `form_${index + 1}`,
      pageId: form.pageId || '',
      title: form.title || form.formName || `表单${index + 1}`,
      formName: dsl?.form?.name || dsl?.form?.tableName || form.formName || form.id || '',
      status: form.status || 'pending',
      fieldCount: fields.length,
      listCount: getDslListFieldCount(dsl),
      queryCount: getDslQueryFieldCount(dsl),
      dictionaryCount: toArray(dsl?.dictionaries).length,
      issueCount: schemaIssues.length,
      schemaErrorCount,
      canSave: form.status === 'confirmed' && Boolean(dsl) && schemaErrorCount === 0,
      action: 'save-form',
      generatedAt: form.generatedAt || '',
      requestId: form.meta?.requestId || '',
    }
  })
}
const createModuleFormalizeApplySteps = (dictionaryPlans = [], formPlans = []) => {
  const steps = []
  dictionaryPlans.forEach((dictionary, index) => {
    if (dictionary.mode === 'ignore') {
      steps.push({
        id: `skip-dictionary:${dictionary.key || index}`,
        type: 'skip-dictionary',
        targetId: dictionary.key,
        title: `跳过字典：${dictionary.name || dictionary.code || dictionary.key}`,
        status: 'skipped',
      })
      return
    }
    if (dictionary.mode === 'use-existing') {
      steps.push({
        id: `use-dictionary:${dictionary.key || index}`,
        type: 'use-existing-dictionary',
        targetId: dictionary.existingCode || dictionary.code,
        title: `复用系统字典：${dictionary.name || dictionary.existingCode || dictionary.code}`,
        status: dictionary.existingCode ? 'ready' : 'blocked',
      })
      return
    }
    steps.push({
      id: `create-dictionary:${dictionary.key || index}`,
      type: 'create-dictionary',
      targetId: dictionary.code,
      title: `创建系统字典：${dictionary.name || dictionary.code}`,
      status: dictionary.code ? 'ready' : 'blocked',
      itemCount: dictionary.itemCount,
    })
  })

  formPlans.forEach((form, index) => {
    steps.push({
      id: `save-form:${form.id || index}`,
      type: 'save-form',
      targetId: form.id,
      formId: form.id,
      title: `保存正式表单：${form.title || form.formName || form.id}`,
      status: form.canSave ? 'ready' : 'blocked',
    })
    steps.push({
      id: `sync-table:${form.id || index}`,
      type: 'sync-table',
      targetId: form.id,
      formId: form.id,
      title: `同步业务表结构：${form.title || form.formName || form.id}`,
      status: form.canSave ? 'pending' : 'blocked',
    })
  })

  if (moduleDesignDraft.value.menus.length > 0 || moduleDesignDraft.value.pages.length > 0) {
    steps.push({
      id: 'bind-module-menu-pages',
      type: 'bind-menu-pages',
      targetId: moduleDesignDraft.value.module.name,
      title: '绑定模块菜单与页面入口',
      status: 'pending',
    })
  }

  return steps
}
const buildModuleFormalizePreview = () => {
  const dictionaryPlans = createModuleFormalizeDictionaryPlans()
  const formPlans = createModuleFormalizeFormPlans()
  const steps = createModuleFormalizeApplySteps(dictionaryPlans, formPlans)
  const blockingIssues = moduleFormalizeBlockingIssues.value.map((issue, index) => normalizeModuleBatchCheckIssue(issue, index, {source: 'formalize-preview'}))
  const warnings = moduleFormalizeWarningIssues.value.map((issue, index) => normalizeModuleBatchCheckIssue(issue, index, {source: 'formalize-preview'}))
  const summary = {
    ...createModuleFormalizeCurrentSummary(),
    formCount: formPlans.length,
    confirmedFormCount: formPlans.filter(form => form.status === 'confirmed').length,
    dictionaryCount: dictionaryPlans.length,
    dictionaryCreateCount: dictionaryPlans.filter(dictionary => dictionary.mode === 'create').length,
    dictionaryExistingCount: dictionaryPlans.filter(dictionary => dictionary.mode === 'use-existing').length,
    dictionaryIgnoreCount: dictionaryPlans.filter(dictionary => dictionary.mode === 'ignore').length,
    saveFormCount: formPlans.filter(form => form.canSave).length,
    stepCount: steps.length,
    readyStepCount: steps.filter(step => step.status === 'ready').length,
    pendingStepCount: steps.filter(step => step.status === 'pending').length,
    skippedStepCount: steps.filter(step => step.status === 'skipped').length,
    blockedStepCount: steps.filter(step => step.status === 'blocked').length,
    blockingIssueCount: blockingIssues.length + steps.filter(step => step.status === 'blocked').length,
    warningCount: warnings.length,
  }
  return {
    previewType: 'module-formalize',
    version: '1.0',
    generatedAt: new Date().toLocaleString(),
    module: clonePlain(moduleDesignDraft.value.module),
    menus: clonePlain(moduleDesignDraft.value.menus) || [],
    pages: clonePlain(moduleDesignDraft.value.pages) || [],
    dictionaries: dictionaryPlans,
    forms: formPlans,
    summary,
    applyPlan: {
      steps,
      warnings,
      blockingIssues,
    },
    source: {
      sourceType: sourceType.value,
      sourceUrl: sourceUrl.value,
      materialRequestId: moduleMaterialResult.value?.requestId || '',
      checkedAt: moduleBatchCheckCheckedAt.value,
    },
    design: clonePlain(moduleDesignDraft.value),
  }
}
const createEmptyModuleFormalizeSaveSummary = () => ({
  total: 0,
  pending: 0,
  running: 0,
  success: 0,
  failed: 0,
  skipped: 0,
  blocked: 0,
  dictionaryTotal: 0,
  dictionarySuccess: 0,
  formTotal: 0,
  formSuccess: 0,
})
const summarizeModuleFormalizeSaveResult = (result = null) => {
  if (!result) {
    return createEmptyModuleFormalizeSaveSummary()
  }
  const summary = createEmptyModuleFormalizeSaveSummary()
  const countStatus = (status = '') => {
    if (summary[status] !== undefined) {
      summary[status] += 1
    }
    summary.total += 1
  }
  toArray(result.dictionaries).forEach(item => {
    summary.dictionaryTotal += item.mode === 'create' ? 1 : 0
    if (item.mode === 'create' && item.status === 'success') {
      summary.dictionarySuccess += 1
    }
    countStatus(item.status)
  })
  toArray(result.forms).forEach(item => {
    summary.formTotal += 1
    if (item.status === 'success') {
      summary.formSuccess += 1
    }
    countStatus(item.status)
  })
  return summary
}
const moduleFormalizeSaveSummary = computed(() => summarizeModuleFormalizeSaveResult(moduleFormalizeSaveResult.value))
const moduleFormalizeSaveStepActive = computed(() => Boolean(moduleFormalizePreview.value) || Boolean(moduleFormalizeSaveResult.value))
const moduleFormalizeSaveStepDone = computed(() => Boolean(moduleFormalizeSaveResult.value?.success))
const moduleFormalizeSaveStepText = computed(() => {
  if (moduleFormalizeSaving.value) return `保存中 ${moduleFormalizeSaveSummary.value.success}/${moduleFormalizeSaveSummary.value.total}`
  if (!moduleFormalizePreview.value && !moduleFormalizeSaveResult.value) return '待预览'
  if (!moduleFormalizeSaveResult.value) return '待保存'
  if (moduleFormalizeSaveResult.value.success) return '已保存'
  if (moduleFormalizeSaveSummary.value.failed > 0) return `失败 ${moduleFormalizeSaveSummary.value.failed}`
  return '未完成'
})
const canRunModuleFormalizeSave = computed(() => {
  return Boolean(moduleFormalizePreview.value)
      && moduleFormalizePreviewStepDone.value
      && !moduleFormalizeSaving.value
      && moduleFormalizeSaveResult.value?.success !== true
})
const moduleFormalizeSaveEmptyText = computed(() => {
  if (!moduleFormalizePreview.value) return '请先生成模块级批量正式化预览。'
  if (!moduleFormalizePreviewStepDone.value) return '正式化预览仍有阻断问题，暂不能批量正式保存。'
  if (moduleFormalizeSaving.value) return '批量正式保存正在执行，请稍候。'
  return '正式化预览已通过，可以执行批量正式保存。'
})
const canRunModuleBatchCheck = computed(() => moduleBlueprintConfirmed.value && moduleForms.value.length > 0 && !moduleFormDraftBatchGenerating.value)
const moduleFormDraftTagColor = computed(() => {
  if (!moduleMaterialReady.value) return 'default'
  if (!moduleBlueprintConfirmed.value) return 'orange'
  if (moduleConfirmedFormDraftCount.value > 0) return 'cyan'
  if (moduleGeneratedFormDraftCount.value > 0) return 'green'
  return 'blue'
})
const modulePreparedFormDraftCount = computed(() => moduleForms.value.filter((form, index) => {
  const status = getModuleFormDraft(form, index).status
  return ['ready', 'generating', 'generated', 'confirmed', 'failed'].includes(status)
}).length)
const moduleGeneratedFormDraftCount = computed(() => moduleForms.value.filter((form, index) => {
  return getModuleFormDraft(form, index).status === 'generated'
}).length)
const moduleConfirmableFormDraftCount = computed(() => moduleForms.value.filter((form, index) => {
  const draft = getModuleFormDraft(form, index)
  return draft.status === 'generated' && Boolean(draft.dsl)
}).length)
const moduleConfirmedFormDraftCount = computed(() => moduleForms.value.filter((form, index) => {
  return getModuleFormDraft(form, index).status === 'confirmed'
}).length)
const moduleFailedFormDraftCount = computed(() => moduleForms.value.filter((form, index) => {
  return getModuleFormDraft(form, index).status === 'failed'
}).length)
const moduleUnfinishedFormDraftCount = computed(() => moduleForms.value.filter((form, index) => {
  const status = getModuleFormDraft(form, index).status || 'pending'
  return !isModuleFormDraftCompletedStatus(status)
}).length)
const moduleCompletedFormDraftCount = computed(() => moduleForms.value.length - moduleUnfinishedFormDraftCount.value)
const moduleFormDraftStepText = computed(() => {
  if (!moduleBlueprintConfirmed.value) return '待确认'
  if (moduleFormDraftBatchGenerating.value) {
    return `生成中 ${moduleFormDraftBatchDone.value}/${moduleFormDraftBatchTotal.value}`
  }
  if (moduleForms.value.length === 0) return '无表单'
  if (moduleUnfinishedFormDraftCount.value === 0) return '已完成'
  if (moduleCompletedFormDraftCount.value > 0) {
    return `已完成 ${moduleCompletedFormDraftCount.value}/${moduleForms.value.length}`
  }
  return '可生成'
})
const moduleFormDraftStepDone = computed(() => {
  return moduleBlueprintConfirmed.value && moduleForms.value.length > 0 && moduleUnfinishedFormDraftCount.value === 0
})
const canBatchGenerateModuleFormDrafts = computed(() => {
  return moduleBlueprintConfirmed.value
      && moduleForms.value.length > 0
      && !moduleFormDraftBatchGenerating.value
      && moduleUnfinishedFormDraftCount.value > 0
})
const canBatchRegenerateModuleFormDrafts = computed(() => {
  return moduleBlueprintConfirmed.value
      && moduleForms.value.length > 0
      && !moduleFormDraftBatchGenerating.value
})
const canBatchConfirmModuleFormDrafts = computed(() => {
  return moduleBlueprintConfirmed.value
      && moduleForms.value.length > 0
      && !moduleFormDraftBatchGenerating.value
      && moduleConfirmableFormDraftCount.value > 0
})
const moduleFormDraftBatchSummaryText = computed(() => {
  if (moduleFormDraftBatchGenerating.value) {
    return `批量生成中：${moduleFormDraftBatchDone.value}/${moduleFormDraftBatchTotal.value}，成功 ${moduleFormDraftBatchSuccess.value}，失败 ${moduleFormDraftBatchFailed.value}，跳过 ${moduleFormDraftBatchSkipped.value}`
  }
  return `待生成 ${moduleUnfinishedFormDraftCount.value}，已生成 ${moduleGeneratedFormDraftCount.value}，待确认 ${moduleConfirmableFormDraftCount.value}，已确认 ${moduleConfirmedFormDraftCount.value}，失败 ${moduleFailedFormDraftCount.value}`
})
const moduleFormDraftTagText = computed(() => {
  if (!moduleMaterialReady.value) return '待识别蓝图'
  if (!moduleBlueprintConfirmed.value) return '待确认蓝图'
  if (moduleConfirmedFormDraftCount.value > 0) {
    return `已确认 ${moduleConfirmedFormDraftCount.value}/${moduleForms.value.length}`
  }
  if (moduleGeneratedFormDraftCount.value > 0) {
    return `已生成 ${moduleGeneratedFormDraftCount.value}/${moduleForms.value.length}`
  }
  if (modulePreparedFormDraftCount.value > 0) {
    return `已准备 ${modulePreparedFormDraftCount.value}/${moduleForms.value.length}`
  }
  return `可生成 ${moduleForms.value.length} 个表单`
})
const isTruthyValue = (value) => value === true || value === 1 || value === '1' || value === 'true'
const formatBooleanText = (value) => isTruthyValue(value) ? '是' : '否'
const formatPreviewValue = (value, fallback = '-') => {
  if (value === 0) return '0'
  const text = String(value ?? '').trim()
  return text || fallback
}
const formatElapsedMs = (value) => {
  const numberValue = Number(value || 0)
  if (!Number.isFinite(numberValue) || numberValue <= 0) return ''
  return numberValue >= 1000 ? `${(numberValue / 1000).toFixed(1)} 秒` : `${numberValue} ms`
}
const getIssueTagColor = (level = '') => {
  if (level === 'error') return 'red'
  if (level === 'warning') return 'orange'
  return 'blue'
}
const getIssueLevelText = (level = '') => {
  if (level === 'error') return '错误'
  if (level === 'warning') return '警告'
  return '建议'
}
const getFormalizeStepStatusColor = (status = '') => {
  if (status === 'ready') return 'green'
  if (status === 'pending') return 'blue'
  if (status === 'blocked') return 'red'
  if (status === 'skipped') return 'default'
  return 'default'
}
const getFormalizeStepStatusText = (status = '') => {
  if (status === 'ready') return '可执行'
  if (status === 'pending') return '后续执行'
  if (status === 'blocked') return '阻断'
  if (status === 'skipped') return '跳过'
  return status || '待确认'
}
const getFormalizeSaveStatusColor = (status = '') => {
  if (status === 'success') return 'green'
  if (status === 'running') return 'processing'
  if (status === 'failed') return 'red'
  if (status === 'blocked') return 'red'
  if (status === 'skipped') return 'default'
  return 'default'
}
const getFormalizeSaveStatusText = (status = '') => {
  if (status === 'success') return '成功'
  if (status === 'running') return '执行中'
  if (status === 'failed') return '失败'
  if (status === 'blocked') return '阻断'
  if (status === 'skipped') return '跳过'
  if (status === 'pending') return '待执行'
  return status || '待执行'
}
const getDictionaryModeText = (mode = '') => {
  if (mode === 'create') return '新建字典'
  if (mode === 'use-existing') return '使用已有'
  if (mode === 'ignore') return '暂不处理'
  if (mode === 'need-confirm') return '待确认'
  return mode || '待确认'
}
const resetModuleFormalizePreview = () => {
  moduleFormalizePreview.value = null
  resetModuleFormalizeSaveResult()
}
const resetModuleFormalizeSaveResult = () => {
  if (moduleFormalizeSaving.value) {
    return
  }
  moduleFormalizeSaveResult.value = null
}
const invalidateModuleBatchCheckAndPreview = () => {
  moduleBatchCheckCheckedAt.value = ''
  ensureAllModuleFormDraftsEnhanced()
  resetModuleFormalizePreview()
}
const getModuleFormDraftPreviewContext = () => {
  const target = moduleFormDraftPreviewTarget.value
  if (!target) {
    return {form: null, index: -1, formId: ''}
  }
  const formIndex = moduleForms.value.findIndex((form, index) => getModuleFormId(form, index) === target.formId)
  const index = formIndex >= 0 ? formIndex : Number(target.index || 0)
  const form = moduleForms.value[index] || target.form || {}
  const formId = target.formId || getModuleFormId(form, index)
  return {form, index, formId}
}
const moduleFormDraftPreview = computed(() => {
  const {formId} = getModuleFormDraftPreviewContext()
  if (!formId) return null
  return moduleDesignDraft.value.forms.find(item => item.id === formId) || null
})
const moduleFormDraftPreviewDsl = computed(() => moduleFormDraftPreview.value?.dsl || null)
const moduleFormDraftPreviewDslFormName = computed(() => moduleFormDraftPreviewDsl.value?.form?.name || '-')
const moduleFormDraftPreviewStatusKey = computed(() => moduleFormDraftPreview.value?.status || 'pending')
const moduleFormDraftPreviewStatus = computed(() => {
  return moduleFormDraftStatusMap[moduleFormDraftPreviewStatusKey.value] || moduleFormDraftStatusMap.pending
})
const moduleFormDraftPreviewTitle = computed(() => {
  const title = moduleFormDraftPreview.value?.title || '未命名表单'
  return `表单草稿预览：${title}`
})
const moduleFormDraftPreviewFields = computed(() => {
  return toArray(moduleFormDraftPreviewDsl.value?.fields).map((field, index) => {
    const type = field.type || field.showType || ''
    const showType = field.showType && field.showType !== type ? field.showType : ''
    return {
      key: field.id || field.name || `field_${index}`,
      label: field.label || field.comments || field.name || `字段${index + 1}`,
      name: field.name || field.javaField || '',
      type,
      typeText: showType ? `${type || '-'} / ${showType}` : (type || '-'),
      jdbcType: field.jdbcType || '',
      javaType: field.javaType || '',
      required: isTruthyValue(field.required) || field.isNull === '0',
      isList: isTruthyValue(field.isList),
      isQuery: isTruthyValue(field.isQuery),
      span: field.span || field.form?.colProps?.span || '',
      dictType: field.dictType || field.list?.dict || '',
    }
  })
})
const moduleFormDraftPreviewDictionaries = computed(() => {
  const fieldsByName = new Map(moduleFormDraftPreviewFields.value.map(field => [field.name, field]))
  const dslDictionaries = toArray(moduleFormDraftPreviewDsl.value?.dictionaries)
  if (dslDictionaries.length > 0) {
    return dslDictionaries.map((dictionary, index) => {
      const fieldName = dictionary.fieldName || ''
      const matchedField = fieldsByName.get(fieldName) || {}
      const items = toArray(dictionary.items)
      return {
        key: dictionary.id || dictionary.code || dictionary.existingCode || fieldName || `dict_${index}`,
        fieldName,
        fieldLabel: dictionary.fieldLabel || matchedField.label || fieldName || '-',
        mode: dictionary.mode || '',
        code: dictionary.existingCode || dictionary.code || matchedField.dictType || '',
        name: dictionary.name || dictionary.title || '',
        itemsText: items.length > 0
            ? items.map(item => `${item.text || item.label || item.name || item.value}${item.value ? `(${item.value})` : ''}`).join('、')
            : '-',
      }
    })
  }
  return getFormMaterialDictionaryHints(moduleFormDraftPreview.value?.sourceMaterialForm).map((dictionary, index) => ({
    key: dictionary.id || dictionary.code || dictionary.existingCode || dictionary.fieldName || `material_dict_${index}`,
    fieldName: dictionary.fieldName || '',
    fieldLabel: dictionary.fieldLabel || dictionary.label || dictionary.fieldName || '-',
    mode: dictionary.mode || 'need-confirm',
    code: dictionary.existingCode || dictionary.code || '',
    name: dictionary.name || dictionary.title || '',
    itemsText: toArray(dictionary.items).length > 0
        ? toArray(dictionary.items).map(item => `${item.text || item.label || item.name || item.value}${item.value ? `(${item.value})` : ''}`).join('、')
        : '-',
  }))
})
const moduleFormDraftPreviewIssues = computed(() => {
  const draftIssues = toArray(moduleFormDraftPreview.value?.issues)
  const schemaIssues = moduleFormDraftPreviewDsl.value ? validateFormDesignDslSchema(moduleFormDraftPreviewDsl.value) : []
  const seenKeys = new Set()
  return draftIssues.concat(schemaIssues).map((issue, index) => ({
    ...issue,
    key: issue.id || `${issue.level || ''}:${issue.title || ''}:${issue.fieldName || ''}:${issue.description || ''}:${index}`,
  })).filter(issue => {
    if (seenKeys.has(issue.key)) return false
    seenKeys.add(issue.key)
    return true
  })
})
const moduleFormDraftPreviewSummary = computed(() => {
  const fields = moduleFormDraftPreviewFields.value
  const issues = moduleFormDraftPreviewIssues.value
  return {
    fieldCount: fields.length,
    listCount: fields.filter(field => field.isList).length,
    queryCount: fields.filter(field => field.isQuery).length,
    dictionaryCount: moduleFormDraftPreviewDictionaries.value.length,
    issueCount: issues.length,
    errorCount: issues.filter(issue => issue.level === 'error').length,
  }
})
const moduleFormDraftPreviewMeta = computed(() => {
  const preview = moduleFormDraftPreview.value || {}
  const dsl = moduleFormDraftPreviewDsl.value || {}
  const meta = preview.meta || {}
  return {
    requestId: meta.requestId || dsl.raw?.requestId || '',
    model: meta.model || dsl.raw?.model || '',
    provider: meta.provider || dsl.raw?.provider || '',
    promptVersion: meta.promptVersion || dsl.raw?.promptVersion || '',
    elapsedText: formatElapsedMs(meta.elapsedMs),
    cacheHitText: meta.cacheHit ? 'Redis 命中' : '未命中',
    generatedAt: preview.generatedAt || dsl.generatedAt || '',
    confirmedAt: meta.confirmedAt || '',
  }
})
const moduleFormDraftPreviewCanConfirm = computed(() => {
  if (!moduleFormDraftPreviewDsl.value) return false
  if (moduleFormDraftBatchGenerating.value) return false
  if (moduleFormDraftPreviewStatusKey.value === 'generating') return false
  if (moduleFormDraftPreviewStatusKey.value === 'confirmed') return false
  return !moduleFormDraftPreviewIssues.value.some(issue => issue.level === 'error')
})
const moduleFormDraftPreviewConfirmText = computed(() => {
  if (moduleFormDraftPreviewStatusKey.value === 'confirmed') return '已确认'
  if (!moduleFormDraftPreviewDsl.value) return '待生成'
  if (moduleFormDraftPreviewIssues.value.some(issue => issue.level === 'error')) return '存在错误'
  return '确认草稿'
})
const moduleMaterialPreviewText = computed(() => JSON.stringify(activeModuleMaterial.value, null, 2))
const moduleMenus = computed(() => toArray(activeModuleMaterial.value?.menus))
const modulePages = computed(() => toArray(activeModuleMaterial.value?.pages))
const moduleForms = computed(() => toArray(activeModuleMaterial.value?.forms))
const menuSelectOptions = computed(() => moduleMenus.value
    .filter(menu => normalizeText(menu.id))
    .map(menu => ({
      value: menu.id,
      label: menu.title ? `${menu.title}（${menu.id}）` : menu.id,
    })))
const pageSelectOptions = computed(() => modulePages.value
    .filter(page => normalizeText(page.id))
    .map(page => ({
      value: page.id,
      label: page.title ? `${page.title}（${page.id}）` : page.id,
    })))
const moduleMaterialHasBlueprint = computed(() => modulePages.value.length > 0 || moduleForms.value.length > 0)
const moduleMaterialReady = computed(() => moduleMaterialResult.value?.success === true && moduleMaterialHasBlueprint.value)
const moduleRecognitionTagColor = computed(() => {
  if (!moduleMaterialResult.value) return 'default'
  if (moduleBlueprintConfirmed.value) return 'blue'
  if (moduleMaterialReady.value) return 'green'
  return materialSummary.value.error > 0 ? 'red' : 'orange'
})
const moduleRecognitionTagText = computed(() => {
  if (!moduleMaterialResult.value) return '待识别'
  if (moduleBlueprintConfirmed.value) return '已确认'
  if (moduleMaterialReady.value) return '已识别'
  return moduleMaterialResult.value.success === false ? '识别未通过' : '待补充'
})
const moduleRecognitionStepText = computed(() => {
  if (!moduleMaterialResult.value) return '待识别'
  if (moduleBlueprintConfirmed.value) return '已确认'
  return moduleMaterialReady.value ? '待确认' : '需处理'
})
const moduleRecognitionWarningTitle = computed(() => {
  if (moduleMaterialResult.value?.success === false) {
    return 'AI 返回的模块蓝图未通过结构检查'
  }
  return 'AI 暂未识别出可用的页面或表单'
})
const moduleRecognitionWarningDesc = computed(() => {
  if (moduleMaterialResult.value?.message) {
    return moduleMaterialResult.value.message
  }
  if (!moduleMaterialHasBlueprint.value) {
    return '当前结果没有菜单、页面或表单。可以补充“包含哪些页面、每个页面有哪些字段”等说明后重新识别。'
  }
  return '请根据下方结构提示补充材料后重新识别。'
})
const normalizeModuleDictionaryMode = (mode = '') => {
  const normalized = normalizeText(mode)
  return dictionaryModeOptions.some(option => option.value === normalized) ? normalized : 'create'
}
const getDictionaryModeColor = (mode = '') => {
  if (mode === 'create') return 'green'
  if (mode === 'use-existing') return 'blue'
  if (mode === 'ignore') return 'default'
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
const createModuleDictionaryRefKey = (fieldRef = {}) => `${normalizeText(fieldRef.formId)}:${normalizeText(fieldRef.fieldName)}`
const mergeModuleDictionaryFieldRefs = (baseRefs = [], nextRefs = []) => {
  const refMap = {}
  toArray(baseRefs).concat(toArray(nextRefs)).forEach(ref => {
    if (!normalizeText(ref.formId) || !normalizeText(ref.fieldName)) {
      return
    }
    const key = createModuleDictionaryRefKey(ref)
    refMap[key] = {
      formId: normalizeText(ref.formId),
      formTitle: normalizeText(ref.formTitle),
      fieldName: normalizeText(ref.fieldName),
      fieldLabel: normalizeText(ref.fieldLabel),
    }
  })
  return Object.values(refMap)
}
const mergeModuleDictionaryItems = (baseItems = [], nextItems = []) => {
  return normalizeDictionaryItems(toArray(baseItems).concat(toArray(nextItems)))
}
const getModuleDictionaryAggregationKey = (suggestion = {}) => {
  const effectiveCode = normalizeText(suggestion.existingCode || suggestion.code)
  if (effectiveCode) {
    return `code:${effectiveCode}`
  }
  const name = normalizeText(suggestion.name)
  if (name) {
    return `name:${name}`
  }
  return `field:${normalizeText(suggestion.formId)}:${normalizeText(suggestion.fieldName)}`
}
const hasCjkText = (value = '') => /[\u3400-\u9fff]/.test(normalizeText(value))
const isLikelyMachineCodeText = (value = '') => {
  const text = normalizeText(value)
  return /^[a-z][a-z0-9_]*$/.test(text) && (text.includes('_') || text.length > 24)
}
const isWeakModuleDictionaryName = (name = '', row = {}) => {
  const text = normalizeText(name)
  if (!text) {
    return true
  }
  const code = normalizeText(row.code || row.existingCode || row.dictType)
  if (code && text === code) {
    return true
  }
  return !hasCjkText(text) && isLikelyMachineCodeText(text)
}
const getModuleDictionaryDisplayContext = (row = {}) => {
  const fieldRef = toArray(row.fieldRefs).find(ref => normalizeText(ref.fieldLabel) || normalizeText(ref.formTitle)) || {}
  return {
    formTitle: normalizeText(row.formTitle || fieldRef.formTitle),
    fieldLabel: normalizeText(row.fieldLabel || row.label || fieldRef.fieldLabel),
  }
}
const normalizeModuleDictionaryDisplayName = (row = {}) => {
  const context = getModuleDictionaryDisplayContext(row)
  const code = normalizeText(row.code || row.existingCode || row.dictType)
  const rawName = normalizeText(row.name || row.title)
  if (context.fieldLabel && (!rawName || !hasCjkText(rawName) || isLikelyMachineCodeText(rawName))) {
    return createDictionaryName(context.formTitle, context.fieldLabel)
  }
  const name = createDictionaryDisplayName({
    ...row,
    code,
    formTitle: context.formTitle,
    fieldLabel: context.fieldLabel,
  })
  return name || (context.fieldLabel ? createDictionaryName(context.formTitle, context.fieldLabel) : code)
}
const chooseModuleDictionaryDisplayName = (current = {}, baseRow = {}) => {
  const currentName = normalizeText(current.name)
  const baseName = normalizeModuleDictionaryDisplayName(baseRow)
  if (!currentName) {
    return baseName
  }
  if (isWeakModuleDictionaryName(currentName, current) && baseName && !isWeakModuleDictionaryName(baseName, baseRow)) {
    return baseName
  }
  return currentName
}
const createModuleDictionaryBaseSignature = (row = {}) => {
  const fieldRefText = toArray(row.fieldRefs)
      .map(ref => `${normalizeText(ref.formId)}:${normalizeText(ref.fieldName)}`)
      .sort()
      .join('|')
  const itemsText = dictionaryItemsToText(row.items)
  return [
    normalizeText(row.mode),
    normalizeText(row.code),
    normalizeText(row.existingCode),
    normalizeText(row.name),
    normalizeText(row.nameEn || row.name_en),
    itemsText,
    fieldRefText,
  ].join('::')
}
const createModuleDictionaryBaseRow = (suggestion = {}) => {
  const mode = normalizeModuleDictionaryMode(suggestion.mode)
  const formName = normalizeText(suggestion.formName || suggestion.formId || moduleName.value || 'module')
  const fieldName = normalizeText(suggestion.fieldName)
  const fieldLabel = normalizeText(suggestion.fieldLabel || suggestion.label)
  const code = normalizeText(suggestion.code || suggestion.dictType || suggestion.existingCode)
      || (fieldName ? createDictionaryCode(formName, fieldName) : '')
  const name = createDictionaryDisplayName({
    ...suggestion,
    code,
    formTitle: suggestion.formTitle,
    fieldLabel,
  }) || (fieldLabel ? createDictionaryName(suggestion.formTitle, fieldLabel) : code)
  const nameEn = createDictionaryEnglishName({
    ...suggestion,
    name,
    code,
  }, code)
  const items = normalizeDictionaryItems(suggestion.items || suggestion.options || suggestion.dictItems || [])
  const fieldRefs = fieldName && normalizeText(suggestion.formId)
      ? [{
        formId: normalizeText(suggestion.formId),
        formTitle: normalizeText(suggestion.formTitle),
        fieldName,
        fieldLabel,
      }]
      : []
  const normalizedName = normalizeModuleDictionaryDisplayName({
    ...suggestion,
    code,
    name,
    formTitle: suggestion.formTitle,
    fieldLabel,
    fieldRefs,
  })
  return {
    key: '',
    id: '',
    mode,
    code,
    existingCode: normalizeText(suggestion.existingCode || (mode === 'use-existing' ? code : '')),
    name: normalizedName || name,
    nameEn,
    items,
    itemsText: dictionaryItemsToText(items),
    fieldName,
    fieldLabel,
    fieldType: normalizeText(suggestion.fieldType || suggestion.type),
    fieldRefs,
    confidence: suggestion.confidence || 0.8,
    source: suggestion.source || 'module-dictionary',
    applied: false,
  }
}
const addModuleDictionaryBaseRow = (rowMap = {}, suggestion = {}) => {
  const row = createModuleDictionaryBaseRow(suggestion)
  const key = getModuleDictionaryAggregationKey(row)
  if (!key) {
    return
  }
  if (!rowMap[key]) {
    rowMap[key] = {
      ...row,
      key,
      id: `module_dict_${Object.keys(rowMap).length + 1}`,
      sources: [row.source],
    }
    return
  }
  const current = rowMap[key]
  current.fieldRefs = mergeModuleDictionaryFieldRefs(current.fieldRefs, row.fieldRefs)
  current.items = mergeModuleDictionaryItems(current.items, row.items)
  current.itemsText = dictionaryItemsToText(current.items)
  current.code = current.code || row.code
  current.existingCode = current.existingCode || row.existingCode
  current.name = chooseModuleDictionaryDisplayName(current, row)
  current.fieldType = current.fieldType || row.fieldType
  current.confidence = Math.max(Number(current.confidence || 0), Number(row.confidence || 0))
  current.sources = Array.from(new Set(toArray(current.sources).concat(row.source).filter(Boolean)))
}
const moduleDictionaryBaseRows = computed(() => {
  const rowMap = {}
  moduleForms.value.forEach((form, index) => {
    const formId = getModuleFormId(form, index)
    const formTitle = normalizeText(form.title)
    const formName = normalizeText(form.nameHint || form.formName || formId)
    const draft = getModuleFormDraft(form, index)
    if (draft?.dsl) {
      Object.values(createDictionaryConfirmationMap(draft.dsl)).forEach(suggestion => {
        addModuleDictionaryBaseRow(rowMap, {
          ...suggestion,
          formId,
          formTitle: suggestion.formTitle || formTitle,
          formName: suggestion.formName || formName,
          source: suggestion.source || 'form-dsl',
        })
      })
    } else {
      getFormMaterialDictionaryHints(form).forEach(dictionary => {
        addModuleDictionaryBaseRow(rowMap, {
          ...dictionary,
          formId,
          formTitle,
          formName,
          source: 'module-material',
        })
      })
    }
  })
  return Object.values(rowMap).map(row => ({
    ...row,
    name: normalizeModuleDictionaryDisplayName(row),
    baseSignature: createModuleDictionaryBaseSignature(row),
  }))
})

const refreshModuleExistingDictionaryMatches = async (options = {}) => {
  const codes = collectDictionarySuggestionCodes(moduleDictionaryBaseRows.value)
  const lookupKey = codes.slice().sort().join(',')
  if (!lookupKey) {
    moduleDictionaryExistingLookupKey.value = ''
    moduleDictionaryExistingCodeMap.value = {}
    return
  }
  if (!options.force && lookupKey === moduleDictionaryExistingLookupKey.value) {
    return
  }
  moduleDictionaryExistingMatching.value = true
  try {
    moduleDictionaryExistingCodeMap.value = await loadSystemDictionaryCodeMap(codes, {force: options.force === true})
    moduleDictionaryExistingLookupKey.value = lookupKey
    syncModuleDictionaryConfirmations()
  } catch (error) {
    moduleDictionaryExistingLookupKey.value = ''
    console.warn('ModuleAIDictionaryExistingMatchFailed', error)
  } finally {
    moduleDictionaryExistingMatching.value = false
  }
}

const syncModuleDictionaryConfirmations = () => {
  const previousMap = moduleDictionaryConfirmations.value || {}
  const nextMap = {}
  moduleDictionaryBaseRows.value.forEach(rawBaseRow => {
    const baseRow = applyExistingDictionaryMatch(rawBaseRow, moduleDictionaryExistingCodeMap.value)
    const previous = previousMap[baseRow.key] || {}
    const userEdited = previous.userEdited === true
    const applied = previous.applied === true && previous.baseSignature === baseRow.baseSignature
    const previousItems = previous.itemsText !== undefined
        ? parseDictionaryItemsText(previous.itemsText)
        : normalizeDictionaryItems(previous.items)
    const baseItems = normalizeDictionaryItems(baseRow.items)
    const shouldUseBaseItems = !userEdited || previousItems.length === 0 && baseItems.length > 0
    const mergedRow = userEdited
        ? {
          ...baseRow,
          ...previous,
        }
        : {
          ...baseRow,
          mode: baseRow.autoMatchedExistingDictionary ? baseRow.mode : previous.mode || baseRow.mode,
          existingCode: baseRow.autoMatchedExistingDictionary ? baseRow.existingCode : previous.existingCode || baseRow.existingCode,
        }
    nextMap[baseRow.key] = {
      ...mergedRow,
      key: baseRow.key,
      id: previous.id || baseRow.id,
      fieldName: baseRow.fieldName,
      fieldLabel: baseRow.fieldLabel,
      fieldType: baseRow.fieldType,
      fieldRefs: baseRow.fieldRefs,
      sources: baseRow.sources,
      baseSignature: baseRow.baseSignature,
      applied,
      userEdited,
      items: shouldUseBaseItems ? baseItems : previousItems,
      itemsText: shouldUseBaseItems ? dictionaryItemsToText(baseItems) : dictionaryItemsToText(previousItems),
    }
  })
  moduleDictionaryConfirmations.value = nextMap
}
const moduleDictionaryConfirmationRows = computed(() => {
  return Object.values(moduleDictionaryConfirmations.value || {})
      .map(row => {
        row.mode = normalizeModuleDictionaryMode(row.mode)
        return row
      })
      .sort((a, b) => normalizeText(a.name || a.code).localeCompare(normalizeText(b.name || b.code), 'zh-CN'))
})
const moduleDictionaryStats = computed(() => {
  const stats = {
    total: moduleDictionaryConfirmationRows.value.length,
    create: 0,
    existing: 0,
    ignore: 0,
    noItems: 0,
    unapplied: 0,
  }
  moduleDictionaryConfirmationRows.value.forEach(row => {
    if (row.mode === 'use-existing') {
      stats.existing += 1
    } else if (row.mode === 'ignore') {
      stats.ignore += 1
    } else {
      stats.create += 1
      if (parseDictionaryItemsText(row.itemsText).length === 0) {
        stats.noItems += 1
      }
    }
    if (!row.applied) {
      stats.unapplied += 1
    }
  })
  return stats
})
const moduleDictionaryBlockingRows = computed(() => moduleDictionaryConfirmationRows.value.filter(row => {
  if (row.mode !== 'ignore' && toArray(row.fieldRefs).length === 0) {
    return true
  }
  if (row.mode === 'use-existing') {
    return !normalizeText(row.existingCode)
  }
  if (row.mode === 'create') {
    return !normalizeText(row.code)
  }
  return false
}))
const moduleDictionarySuggestionCount = computed(() => moduleDictionaryConfirmationRows.value.length || moduleDictionaryHintCount.value)
const moduleDictionaryStepActive = computed(() => moduleFormDraftStepDone.value || moduleDictionaryConfirmationRows.value.length > 0)
const moduleDictionaryStepDone = computed(() => {
  if (!moduleDictionaryStepActive.value) {
    return false
  }
  if (moduleDictionaryConfirmationRows.value.length === 0) {
    return moduleGeneratedFormDraftCount.value > 0 || moduleConfirmedFormDraftCount.value > 0
  }
  return moduleDictionaryStats.value.unapplied === 0 && moduleDictionaryBlockingRows.value.length === 0
})
const moduleDictionaryStepText = computed(() => {
  if (!moduleBlueprintConfirmed.value) return '待草稿'
  if (moduleDictionaryConfirmationRows.value.length === 0) {
    return moduleGeneratedFormDraftCount.value > 0 || moduleConfirmedFormDraftCount.value > 0 ? '无字典' : '待生成'
  }
  if (moduleDictionaryBlockingRows.value.length > 0) {
    return `待补充 ${moduleDictionaryBlockingRows.value.length}`
  }
  if (moduleDictionaryStats.value.unapplied > 0) {
    return `待应用 ${moduleDictionaryStats.value.unapplied}`
  }
  return '已确认'
})
const canApplyModuleDictionaryConfirmations = computed(() => {
  return moduleDictionaryConfirmationRows.value.length > 0
      && moduleDictionaryBlockingRows.value.length === 0
      && !moduleFormDraftBatchGenerating.value
})
const buildModuleDictionaryDesignRows = (forms = []) => {
  if (moduleDictionaryConfirmationRows.value.length === 0) {
    return collectModuleDictionaryDrafts(forms)
  }
  return moduleDictionaryConfirmationRows.value.map((row, index) => {
    const mode = normalizeModuleDictionaryMode(row.mode)
    const items = row.itemsText !== undefined
        ? parseDictionaryItemsText(row.itemsText)
        : normalizeDictionaryItems(row.items)
    const effectiveCode = mode === 'use-existing'
        ? normalizeText(row.existingCode)
        : normalizeText(row.code)
    return {
      id: row.id || `module_dict_${index + 1}`,
      mode,
      code: mode === 'ignore' ? '' : effectiveCode,
      existingCode: mode === 'use-existing' ? effectiveCode : '',
      name: row.name || effectiveCode,
      nameEn: row.nameEn || row.name_en || effectiveCode,
      items,
      fieldRefs: toArray(row.fieldRefs).map(ref => ({
        formId: normalizeText(ref.formId),
        formTitle: normalizeText(ref.formTitle),
        fieldName: normalizeText(ref.fieldName),
        fieldLabel: normalizeText(ref.fieldLabel),
      })),
      confidence: row.confidence || 0.8,
      source: row.applied ? 'module-dictionary-confirmed' : 'module-dictionary-draft',
    }
  })
}
const moduleDictionaryHintCount = computed(() => moduleForms.value.reduce((total, form, index) => total + getFormDictionaryHintCount(form, index), 0))
const moduleFileSummary = computed(() => {
  const file = moduleFileList.value[0]
  if (!file) {
    return '尚未选择文件。'
  }
  const size = Number(file.size || 0)
  const sizeText = size > 0 ? `${(size / 1024).toFixed(1)} KB` : '未知大小'
  return `${getModuleFileKindText(file)}：${file.name || '已选择文件'}，${sizeText}。`
})

const getEditableModuleMaterial = () => {
  return moduleMaterialResult.value?.material || null
}

const syncModuleFormDraftSlots = () => {
  const nextMap = {}
  moduleForms.value.forEach((form, index) => {
    const formId = getModuleFormId(form, index)
    nextMap[formId] = {
      status: moduleFormDraftMap.value[formId]?.status || 'pending',
      dsl: moduleFormDraftMap.value[formId]?.dsl || null,
      issues: toArray(moduleFormDraftMap.value[formId]?.issues),
      generatedAt: moduleFormDraftMap.value[formId]?.generatedAt || '',
      meta: {
        ...(moduleFormDraftMap.value[formId]?.meta || {}),
        sourceTitle: form.title || '',
        sourceNameHint: form.nameHint || '',
      },
    }
  })
  moduleFormDraftMap.value = nextMap
}

const resetModuleFormDraftSlots = () => {
  moduleFormDraftMap.value = {}
  moduleDictionaryConfirmations.value = {}
  moduleDictionaryExistingLookupKey.value = ''
  moduleDictionaryExistingCodeMap.value = {}
  invalidateModuleBatchCheckAndPreview()
  moduleFormDraftBatchGenerating.value = false
  resetModuleFormDraftBatchState()
}

const getModuleFormDraft = (form = {}, index = 0) => {
  const formId = getModuleFormId(form, index)
  return moduleFormDraftMap.value[formId] || {status: 'pending', dsl: null, issues: [], meta: {}}
}

const getModuleFormDraftStatusMeta = (form = {}, index = 0) => {
  const status = getModuleFormDraft(form, index).status || 'pending'
  return moduleFormDraftStatusMap[status] || moduleFormDraftStatusMap.pending
}

const isModuleFormDraftCompletedStatus = (status = '') => ['generated', 'confirmed'].includes(status)

const getModuleFormDraftActionText = (form = {}, index = 0) => {
  const status = getModuleFormDraft(form, index).status || 'pending'
  if (status === 'generating') return '生成中'
  if (status === 'pending') return '生成草稿'
  return '重新生成'
}

const getModuleFormPage = (form = {}) => {
  const pageId = normalizeText(form.pageId)
  return modulePages.value.find(page => normalizeText(page.id) === pageId) || {}
}

const updateModuleFormDraft = (form = {}, index = 0, patch = {}) => {
  const formId = getModuleFormId(form, index)
  const current = getModuleFormDraft(form, index)
  moduleFormDraftMap.value = {
    ...moduleFormDraftMap.value,
    [formId]: {
      ...current,
      ...patch,
      meta: {
        ...(current.meta || {}),
        ...(patch.meta || {}),
      },
    },
  }
  invalidateModuleBatchCheckAndPreview()
}

const buildModuleFormDslEnhanceContext = (form = {}, index = 0) => ({
  currentFormId: getModuleFormId(form, index),
  moduleForms: moduleDesignDraft.value.forms,
  moduleRelations: moduleDesignDraft.value.relations,
  moduleDesign: moduleDesignDraft.value,
})

const enrichModuleFormDsl = (dsl = {}, form = {}, index = 0) => {
  return enrichDslModalSelectFields(dsl, buildModuleFormDslEnhanceContext(form, index))
}

const ensureModuleFormDraftDslEnhanced = (form = {}, index = 0) => {
  const draft = getModuleFormDraft(form, index)
  if (!draft.dsl) {
    return null
  }
  const enhancedDsl = enrichModuleFormDsl(draft.dsl, form, index)
  if (JSON.stringify(enhancedDsl) !== JSON.stringify(draft.dsl)) {
    updateModuleFormDraft(form, index, {
      dsl: enhancedDsl,
      meta: {
        modalSelectEnhancedAt: new Date().toISOString(),
      },
    })
  }
  return enhancedDsl
}

const ensureAllModuleFormDraftsEnhanced = () => {
  moduleForms.value.forEach((form, index) => {
    ensureModuleFormDraftDslEnhanced(form, index)
  })
}

const createModuleFormGenerateErrorIssue = (error = {}) => ({
  level: 'error',
  title: '表单草稿生成失败',
  description: error.message || 'AI 生成失败，请稍后重试。',
  code: error.code || 'AI_MODULE_FORM_DSL_GENERATE_FAILED',
  requestId: error.requestId || '',
})

const getModuleFormGenerationDictionaries = () => {
  const material = activeModuleMaterial.value || {}
  return collectModuleDictionaryDrafts(toArray(material.forms)).map(dictionary => ({
    ...dictionary,
    source: 'module-material',
  }))
}

const buildModuleFormDslPayload = (form = {}, index = 0) => {
  const material = activeModuleMaterial.value || {}
  const moduleInfo = {
    ...(material.module || {}),
    nameHint: moduleName.value || material.module?.nameHint || '',
    title: moduleTitle.value || material.module?.title || '',
    scene: moduleScene.value || material.module?.scene || 'normal',
  }
  return {
    module: moduleInfo,
    page: getModuleFormPage(form),
    form: clonePlain(form),
    dictionaries: getModuleFormGenerationDictionaries(),
    relations: toArray(material.relations).map(relation => ({...relation})),
    sourceType: sourceType.value,
    sourceUrl: sourceUrl.value,
    scene: moduleScene.value || moduleInfo.scene || 'normal',
  }
}

const prepareModuleFormDraft = async (form = {}, index = 0, options = {}) => {
  const silent = options.silent === true
  if (!moduleBlueprintConfirmed.value) {
    if (!silent) {
      message.warning('请先确认模块蓝图。')
    }
    return false
  }
  const currentDraft = getModuleFormDraft(form, index)
  if (currentDraft.status === 'generating') {
    if (!silent) {
      message.info('该表单正在生成，请稍候。')
    }
    return false
  }
  const formId = getModuleFormId(form, index)
  const startedAt = new Date().toISOString()
  updateModuleFormDraft(form, index, {
    status: 'generating',
    issues: [],
    meta: {
      preparedAt: startedAt,
      sourceTitle: form.title || '',
      sourceNameHint: form.nameHint || '',
      pageId: form.pageId || '',
    },
  })
  try {
    const generated = await generateRemoteModuleFormDesignDsl(buildModuleFormDslPayload(form, index), {
      rejectOnSchemaError: false,
    })
    const enhancedDsl = enrichModuleFormDsl(generated.dsl, form, index)
    const schemaIssues = validateFormDesignDslSchema(enhancedDsl)
    updateModuleFormDraft(form, index, {
      status: 'generated',
      dsl: enhancedDsl,
      issues: toArray(generated.backendIssues).concat(schemaIssues),
      generatedAt: generated.generatedAt || new Date().toISOString(),
      meta: {
        sourceTitle: form.title || '',
        sourceNameHint: form.nameHint || '',
        pageId: form.pageId || '',
        requestId: generated.requestId || '',
        model: generated.model || '',
        provider: generated.provider || '',
        promptVersion: generated.promptVersion || '',
        elapsedMs: generated.elapsedMs || 0,
        cacheHit: generated.fromCache === true,
        modalSelectEnhancedAt: new Date().toISOString(),
      },
    })
    console.log('ModuleFormDraftGenerated', moduleFormDraftMap.value[formId])
    if (!silent) {
      if (generated.fromCache) {
        message.success(`已从缓存读取 ${form.title || '表单'} 的 FormDesignDSL。`)
      } else {
        message.success(`已生成 ${form.title || '表单'} 的 FormDesignDSL。`)
      }
    }
    return true
  } catch (error) {
    console.warn('ModuleFormDraftGenerateFailed', error)
    updateModuleFormDraft(form, index, {
      status: 'failed',
      issues: [createModuleFormGenerateErrorIssue(error)].concat(toArray(error.schemaIssues)),
      meta: {
        sourceTitle: form.title || '',
        sourceNameHint: form.nameHint || '',
        pageId: form.pageId || '',
        requestId: error.requestId || '',
        errorCode: error.code || '',
        failedAt: new Date().toISOString(),
      },
    })
    if (!silent) {
      message.error(error.message || '表单草稿生成失败')
    }
    return false
  }
}

const getModuleFormDraftBatchItems = () => {
  return moduleForms.value.map((form, index) => {
    const draft = getModuleFormDraft(form, index)
    return {
      form,
      index,
      draft,
      status: draft.status || 'pending',
      title: form.title || form.nameHint || `表单${index + 1}`,
    }
  })
}

const getModuleFormDraftBatchTargets = (mode = 'pending') => {
  return getModuleFormDraftBatchItems().filter(item => {
    if (item.status === 'generating') return false
    if (mode === 'all') return true
    return !isModuleFormDraftCompletedStatus(item.status)
  })
}

const resetModuleFormDraftBatchState = (mode = '', total = 0, skipped = 0) => {
  moduleFormDraftBatchMode.value = mode
  moduleFormDraftBatchTotal.value = total
  moduleFormDraftBatchDone.value = 0
  moduleFormDraftBatchSuccess.value = 0
  moduleFormDraftBatchFailed.value = 0
  moduleFormDraftBatchSkipped.value = skipped
  moduleFormDraftBatchCurrentTitle.value = ''
}

const batchGenerateModuleFormDrafts = async (mode = 'pending') => {
  if (!moduleBlueprintConfirmed.value) {
    message.warning('请先确认模块蓝图。')
    return
  }
  if (moduleFormDraftBatchGenerating.value) {
    message.info('表单草稿正在批量生成，请稍候。')
    return
  }
  const allItems = getModuleFormDraftBatchItems()
  const targets = getModuleFormDraftBatchTargets(mode)
  const skipped = Math.max(allItems.length - targets.length, 0)
  if (targets.length === 0) {
    message.info(mode === 'all' ? '当前没有可重新生成的表单。' : '当前没有未完成的表单草稿。')
    return
  }

  moduleFormDraftBatchGenerating.value = true
  resetModuleFormDraftBatchState(mode, targets.length, skipped)
  try {
    for (const item of targets) {
      moduleFormDraftBatchCurrentTitle.value = item.title
      const success = await prepareModuleFormDraft(item.form, item.index, {silent: true})
      if (success) {
        moduleFormDraftBatchSuccess.value += 1
      } else {
        moduleFormDraftBatchFailed.value += 1
      }
      moduleFormDraftBatchDone.value += 1
    }
  } finally {
    moduleFormDraftBatchGenerating.value = false
    moduleFormDraftBatchCurrentTitle.value = ''
  }

  if (moduleFormDraftBatchFailed.value > 0) {
    message.warning(`批量生成完成：成功 ${moduleFormDraftBatchSuccess.value} 个，失败 ${moduleFormDraftBatchFailed.value} 个，跳过 ${moduleFormDraftBatchSkipped.value} 个。`)
  } else {
    message.success(`批量生成完成：成功 ${moduleFormDraftBatchSuccess.value} 个，跳过 ${moduleFormDraftBatchSkipped.value} 个。`)
  }
}

const viewModuleFormDraft = (form = {}, index = 0) => {
  const formId = getModuleFormId(form, index)
  ensureModuleFormDraftDslEnhanced(form, index)
  const designForm = moduleDesignDraft.value.forms.find(item => item.id === formId)
  moduleFormDraftPreviewTarget.value = {
    formId,
    index,
    form: clonePlain(form),
  }
  moduleFormDraftPreviewVisible.value = true
  console.log('ModuleFormDraft', designForm || getModuleFormDraft(form, index))
}

const checkModuleFormDraft = (form = {}, index = 0) => {
  const draft = getModuleFormDraft(form, index)
  if (!draft.dsl) {
    message.warning('该表单还没有生成 FormDesignDSL，生成后才能体检。')
    return
  }
  const enhancedDsl = ensureModuleFormDraftDslEnhanced(form, index) || draft.dsl
  const issues = validateFormDesignDslSchema(enhancedDsl)
  console.log('ModuleFormDraftCheckTarget', enhancedDsl)
  console.log('ModuleFormDraftCheckIssues', issues)
  if (issues.some(issue => issue.level === 'error')) {
    message.warning(`表单草稿体检完成，发现 ${issues.length} 个问题，请查看控制台。`)
    return
  }
  message.success(`表单草稿体检完成，发现 ${issues.length} 个提示。`)
}

const runModuleBatchCheck = () => {
  if (!canRunModuleBatchCheck.value) {
    message.warning('请先确认模块蓝图并生成表单草稿。')
    return
  }
  resetModuleFormalizePreview()
  moduleBatchCheckCheckedAt.value = new Date().toLocaleString()
  console.log('ModuleDesignDSL', moduleDesignDraft.value)
  console.log('ModuleBatchCheckSummary', moduleBatchCheckSummary.value)
  console.log('ModuleBatchCheckIssues', moduleBatchCheckIssues.value)
  if (moduleBatchCheckSummary.value.error > 0) {
    message.warning(`模块级体检完成，发现 ${moduleBatchCheckSummary.value.error} 个阻断错误，请查看结果区。`)
    return
  }
  if (moduleBatchCheckSummary.value.warning > 0) {
    message.warning(`模块级体检基本通过，发现 ${moduleBatchCheckSummary.value.warning} 个警告，建议处理后再正式化。`)
    return
  }
  message.success('模块级体检通过，可以进入批量正式化预览。')
}

const previewModuleFormalizeState = async () => {
  if (!canBuildModuleFormalizePreview.value) {
    message.warning(moduleFormalizePreviewEmptyText.value)
    return
  }
  ensureAllModuleFormDraftsEnhanced()
  await refreshModuleExistingDictionaryMatches({force: true})
  if (!canBuildModuleFormalizePreview.value) {
    message.warning(moduleFormalizePreviewEmptyText.value)
    return
  }
  const preview = buildModuleFormalizePreview()
  resetModuleFormalizeSaveResult()
  moduleFormalizePreview.value = preview
  console.log('ModuleFormalizePreview', preview)
  console.log('ModuleFormalizeApplyPlan', preview.applyPlan)
  if (preview.summary.blockingIssueCount > 0) {
    message.warning(`正式化预览已生成，但还有 ${preview.summary.blockingIssueCount} 个阻断项，请查看预览区。`)
    return
  }
  if (preview.summary.warningCount > 0) {
    message.warning(`正式化预览已生成，包含 ${preview.summary.warningCount} 个提示，正式保存前建议复核。`)
    return
  }
  message.success(`正式化预览已生成：${preview.summary.saveFormCount} 个表单、${preview.summary.dictionaryCreateCount} 个新建字典。`)
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

const createModuleFormalizeSaveConfirmContent = (preview = {}) => {
  const summary = preview.summary || {}
  return h('div', {style: {lineHeight: 1.7}}, [
    h('div', {style: {fontWeight: 600, marginBottom: '8px'}}, '即将批量正式保存模块表单配置'),
    h('div', `新建系统字典：${summary.dictionaryCreateCount || 0} 个`),
    h('div', `复用已有字典：${summary.dictionaryExistingCount || 0} 个，忽略：${summary.dictionaryIgnoreCount || 0} 个`),
    h('div', `保存正式表单：${summary.saveFormCount || summary.formCount || 0} 个`),
    h('div', `后续步骤：同步表结构、菜单绑定会先记录为待处理，本次不自动执行。`),
    h('div', {style: {marginTop: '8px', color: '#8c5a00'}}, '该操作会真实写入系统字典和表单配置，请确认预览无误后继续。'),
  ])
}

const confirmRunModuleFormalizeSave = () => {
  if (!canRunModuleFormalizeSave.value) {
    message.warning(moduleFormalizeSaveEmptyText.value)
    return
  }
  Modal.confirm({
    title: '批量正式保存确认',
    content: createModuleFormalizeSaveConfirmContent(moduleFormalizePreview.value),
    okText: '确认保存',
    cancelText: '返回检查',
    onOk() {
      return runModuleFormalizeSave()
    },
  })
}

const cloneButtonConfigs = (buttons = []) => toArray(buttons).map(button => ({...button}))

const applyDefaultButtonPermissions = (buttons = [], formName = '', type = 'row') => {
  return buttons.map(button => {
    const value = normalizeText(button.value)
    if (!formName || !value) {
      return button
    }
    if (value.indexOf('view') === 0) {
      button.permission = `app:${formName}:view`
    } else if (value.indexOf('edit') === 0) {
      button.permission = `app:${formName}:edit`
    } else if (value.indexOf('delete') === 0 || value.indexOf('batch-delete') === 0) {
      button.permission = `app:${formName}:del`
    } else if (value.indexOf('addChild') === 0) {
      button.permission = `app:${formName}:lowerlevel`
    } else if (type === 'list' && value.indexOf('add') === 0) {
      button.permission = `app:${formName}:add`
    } else if (type === 'list' && value.indexOf('import') === 0) {
      button.permission = `app:${formName}:import`
    } else if (type === 'list' && value.indexOf('export') === 0) {
      button.permission = `app:${formName}:export`
    }
    return button
  })
}

const createDefaultExtendJsConfig = (formName = '', tableType = '0') => {
  const isTreeTable = tableType === '3'
  return {
    singleTable__rowButtons: applyDefaultButtonPermissions(
        cloneButtonConfigs(isTreeTable ? treeTableRowButtonArr : defaultRowButtonArr),
        formName,
        'row'
    ),
    list__buttons: applyDefaultButtonPermissions(
        cloneButtonConfigs(isTreeTable ? treeTableListButtonArr : defaultListButtonArr),
        formName,
        'list'
    ),
    extendColumns: [],
  }
}

const createDefaultFormPropsModel = (formTitle = '') => ({
  labelWidth: 100,
  modal__Width: 1200,
  modal__Full: '0',
  mainTableTitle: formTitle,
})

const getModuleDesignFormById = (formId = '') => {
  return moduleDesignDraft.value.forms.find(form => normalizeText(form.id) === normalizeText(formId)) || null
}

const buildModuleFormalizeSavePayload = (formPlan = {}) => {
  const designForm = getModuleDesignFormById(formPlan.id)
  const dsl = designForm?.dsl
  if (!dsl) {
    throw new Error(`${formPlan.title || formPlan.id || '表单'} 缺少 FormDesignDSL`)
  }
  const dslForm = dsl.form || {}
  const tableType = normalizeText(dslForm.tableType || '0')
  const baseFormModel = {
    id: '',
    name: formPlan.formName || dslForm.name || designForm.formName || '',
    comments: formPlan.title || dslForm.title || designForm.title || '',
    comments_EN: formPlan.title || dslForm.title || designForm.title || '',
    module: dslForm.module || moduleName.value || '',
    tableType,
    parentTable: dslForm.parentTable || '',
    parentTableFk: dslForm.parentTableFk || '',
    pkColumnName: dslForm.pkColumnName || 'id',
    isVersion: dslForm.isVersion || '0',
    blockChainParam1: '',
    blockChainParam2: '',
    blockChainParam3: '',
    blockChainParam4: '',
  }
  const extendJsConfig = createDefaultExtendJsConfig(baseFormModel.name, tableType)
  const formPropsModel = createDefaultFormPropsModel(baseFormModel.comments)
  const patch = convertDslToDesignerStatePatch(dsl, {
    table: baseFormModel,
    formModel: baseFormModel,
    formPropsModel,
    extendJsConfig,
    displayFormItemArr: [],
    hideFormItemArr: [],
    fixedArr: [],
    currentFormId: designForm.id,
    moduleForms: moduleDesignDraft.value.forms,
    moduleRelations: moduleDesignDraft.value.relations,
    moduleDesign: moduleDesignDraft.value,
  })
  if (!patch.canApply) {
    const firstError = toArray(patch.errors)[0]
    throw new Error(firstError?.description || firstError?.title || `${baseFormModel.comments || baseFormModel.name} 转换设计器状态失败`)
  }
  const formModel = {
    ...baseFormModel,
    ...(patch.formPatch || {}),
  }
  const finalFormPropsModel = {
    ...formPropsModel,
    ...(patch.formPropsPatch || {}),
  }
  const finalExtendJsConfig = createDefaultExtendJsConfig(formModel.name, formModel.tableType || tableType)
  const submitData = buildFormalizeSubmitDataPreview({
    table: formModel,
    formModel,
    formPropsModel: finalFormPropsModel,
    extendJsConfig: finalExtendJsConfig,
    displayFormItemArr: patch.displayFormItemArr,
    hideFormItemArr: patch.hideFormItemArr,
    fixedArr: patch.fixedArr,
  })
  return {
    ...formModel,
    ...submitData,
  }
}

const createModuleFormalizeSaveResult = (preview = {}) => ({
  saveType: 'module-formalize-save',
  version: '1.0',
  startedAt: new Date().toLocaleString(),
  finishedAt: '',
  success: false,
  stopped: false,
  previewGeneratedAt: preview.generatedAt || '',
  module: clonePlain(preview.module || {}),
  dictionaries: toArray(preview.dictionaries).map(dictionary => ({
    ...dictionary,
    status: dictionary.mode === 'create' ? 'pending' : 'skipped',
    message: dictionary.mode === 'use-existing'
        ? '复用已有系统字典，无需创建。'
        : dictionary.mode === 'ignore'
            ? '已选择暂不处理。'
            : '',
  })),
  forms: toArray(preview.forms).map(form => ({
    ...form,
    status: form.canSave ? 'pending' : 'blocked',
    message: form.canSave ? '' : '正式化预览判定该表单暂不可保存。',
    genTableId: '',
    response: null,
  })),
  steps: [],
})

const appendModuleFormalizeSaveStep = (step = {}) => {
  if (!moduleFormalizeSaveResult.value) {
    return null
  }
  const nextStep = {
    id: step.id || `step_${moduleFormalizeSaveResult.value.steps.length + 1}`,
    status: step.status || 'pending',
    title: step.title || '',
    message: step.message || '',
    type: step.type || '',
    targetId: step.targetId || '',
    startedAt: step.startedAt || '',
    finishedAt: step.finishedAt || '',
  }
  moduleFormalizeSaveResult.value.steps.push(nextStep)
  return nextStep
}

const refreshModuleFormalizeSaveResultSummary = () => {
  if (!moduleFormalizeSaveResult.value) {
    return
  }
  moduleFormalizeSaveResult.value.summary = summarizeModuleFormalizeSaveResult(moduleFormalizeSaveResult.value)
}

const assertFormalizeResponseSuccess = (response = {}, fallbackMessage = '保存失败') => {
  if (response && response.code !== undefined && Number(response.code) !== 0) {
    throw new Error(response.msg || response.message || fallbackMessage)
  }
  return response
}

const runModuleFormalizeSave = async () => {
  if (!canRunModuleFormalizeSave.value) {
    message.warning(moduleFormalizeSaveEmptyText.value)
    return false
  }
  const preview = moduleFormalizePreview.value
  moduleFormalizeSaving.value = true
  moduleFormalizeSaveResult.value = createModuleFormalizeSaveResult(preview)
  refreshModuleFormalizeSaveResultSummary()
  let createdDictionaryCount = 0

  try {
    const existingDictionaryCodeMap = await loadSystemDictionaryCodeMap(
        moduleFormalizeSaveResult.value.dictionaries
            .filter(dictionary => dictionary.mode === 'create')
            .map(dictionary => dictionary.code),
        {force: true},
    )
    for (const dictionary of moduleFormalizeSaveResult.value.dictionaries) {
      if (dictionary.mode === 'create') {
        Object.assign(dictionary, applyExistingDictionaryMatch(dictionary, existingDictionaryCodeMap, {forceUserEdited: true}))
        if (dictionary.mode === 'use-existing') {
          dictionary.status = 'skipped'
          dictionary.message = dictionary.message || '已匹配已有系统字典，无需重复创建。'
        }
      }
      if (dictionary.mode !== 'create') {
        appendModuleFormalizeSaveStep({
          id: `dictionary-skip:${dictionary.key}`,
          type: dictionary.mode === 'use-existing' ? 'use-existing-dictionary' : 'skip-dictionary',
          targetId: dictionary.existingCode || dictionary.code || dictionary.key,
          title: dictionary.mode === 'use-existing'
              ? `复用系统字典：${dictionary.name || dictionary.existingCode || dictionary.code}`
              : `跳过字典：${dictionary.name || dictionary.code || dictionary.key}`,
          status: 'skipped',
          message: dictionary.message,
        })
        continue
      }

      const step = appendModuleFormalizeSaveStep({
        id: `dictionary-create:${dictionary.key}`,
        type: 'create-dictionary',
        targetId: dictionary.code,
        title: `创建系统字典：${dictionary.name || dictionary.code}`,
        status: 'running',
        startedAt: new Date().toLocaleString(),
      })
      dictionary.status = 'running'
      dictionary.message = '正在创建系统字典。'
      refreshModuleFormalizeSaveResultSummary()
      try {
        const response = await saveDataAction('sys_dictionary', buildSysDictionarySaveData(dictionary))
        dictionary.status = 'success'
        dictionary.message = '系统字典已创建。'
        dictionary.response = response
        step.status = 'success'
        step.message = '创建成功'
        createdDictionaryCount += 1
      } catch (error) {
        dictionary.status = 'failed'
        dictionary.message = error?.message || '系统字典创建失败。'
        step.status = 'failed'
        step.message = dictionary.message
        moduleFormalizeSaveResult.value.stopped = true
        throw error
      } finally {
        step.finishedAt = new Date().toLocaleString()
        refreshModuleFormalizeSaveResultSummary()
      }
    }

    if (createdDictionaryCount > 0) {
      notifySystemDictionarySelectRefresh()
      moduleDictionaryExistingLookupKey.value = ''
      refreshModuleExistingDictionaryMatches()
    }

    for (const form of moduleFormalizeSaveResult.value.forms) {
      if (!form.canSave) {
        const step = appendModuleFormalizeSaveStep({
          id: `form-blocked:${form.id}`,
          type: 'save-form',
          targetId: form.id,
          title: `保存正式表单：${form.title || form.formName || form.id}`,
          status: 'blocked',
          message: form.message,
        })
        moduleFormalizeSaveResult.value.stopped = true
        refreshModuleFormalizeSaveResultSummary()
        throw new Error(step.message || `${form.title || form.formName || form.id} 暂不可保存`)
      }

      const step = appendModuleFormalizeSaveStep({
        id: `form-save:${form.id}`,
        type: 'save-form',
        targetId: form.id,
        title: `保存正式表单：${form.title || form.formName || form.id}`,
        status: 'running',
        startedAt: new Date().toLocaleString(),
      })
      form.status = 'running'
      form.message = '正在保存正式表单配置。'
      refreshModuleFormalizeSaveResultSummary()
      try {
        const payload = buildModuleFormalizeSavePayload(form)
        const response = assertFormalizeResponseSuccess(
            await postAction('gen/genTable/saveGenTable', payload),
            `${form.title || form.formName || form.id} 保存失败`
        )
        form.status = 'success'
        form.message = '正式表单配置已保存。'
        form.genTableId = response?.data?.gentableId || response?.gentableId || response?.data?.genTable?.id || ''
        form.response = response
        step.status = 'success'
        step.message = form.genTableId ? `保存成功，gentableId=${form.genTableId}` : '保存成功'
      } catch (error) {
        form.status = 'failed'
        form.message = error?.message || '正式表单配置保存失败。'
        step.status = 'failed'
        step.message = form.message
        moduleFormalizeSaveResult.value.stopped = true
        throw error
      } finally {
        step.finishedAt = new Date().toLocaleString()
        refreshModuleFormalizeSaveResultSummary()
      }
    }

    toArray(preview.applyPlan?.steps)
        .filter(step => !FORMALIZE_SAVE_EXECUTABLE_TYPES.includes(step.type))
        .forEach(step => {
          appendModuleFormalizeSaveStep({
            id: `future:${step.id}`,
            type: step.type,
            targetId: step.targetId,
            title: step.title,
            status: 'skipped',
            message: 'MVP 暂不自动执行，留待后续任务处理。',
          })
        })

    moduleFormalizeSaveResult.value.success = true
    moduleFormalizeSaveResult.value.finishedAt = new Date().toLocaleString()
    refreshModuleFormalizeSaveResultSummary()
    console.log('ModuleFormalizeSaveResult', moduleFormalizeSaveResult.value)
    message.success(`批量正式保存完成：创建字典 ${createdDictionaryCount} 个，保存表单 ${moduleFormalizeSaveSummary.value.formSuccess} 个。`)
    return true
  } catch (error) {
    moduleFormalizeSaveResult.value.success = false
    moduleFormalizeSaveResult.value.finishedAt = new Date().toLocaleString()
    refreshModuleFormalizeSaveResultSummary()
    console.warn('ModuleFormalizeSaveFailed', error)
    console.log('ModuleFormalizeSaveResult', moduleFormalizeSaveResult.value)
    message.error(error?.message || '批量正式保存失败，已停止后续步骤。')
    return false
  } finally {
    moduleFormalizeSaving.value = false
  }
}

const closeModuleFormDraftPreview = () => {
  moduleFormDraftPreviewVisible.value = false
}

const mergeModuleFormDraftIssues = (baseIssues = [], nextIssues = []) => {
  const seenKeys = new Set()
  return toArray(baseIssues).concat(toArray(nextIssues)).filter((issue, index) => {
    const key = issue.id || `${issue.level || ''}:${issue.title || ''}:${issue.fieldName || ''}:${issue.description || ''}:${index}`
    if (seenKeys.has(key)) return false
    seenKeys.add(key)
    return true
  })
}

const markModuleDictionaryDirty = (row = {}) => {
  row.applied = false
  row.userEdited = true
  invalidateModuleBatchCheckAndPreview()
}

const handleModuleDictionaryModeChange = (row = {}) => {
  if (row.mode === 'use-existing') {
    row.existingCode = normalizeText(row.existingCode)
  }
  markModuleDictionaryDirty(row)
}

const buildModuleDictionaryConfirmationForRef = (row = {}, fieldRef = {}) => {
  const mode = normalizeModuleDictionaryMode(row.mode)
  const items = row.itemsText !== undefined
      ? parseDictionaryItemsText(row.itemsText)
      : normalizeDictionaryItems(row.items)
  const effectiveCode = mode === 'use-existing'
      ? normalizeText(row.existingCode)
      : normalizeText(row.code)
  return {
    ...row,
    mode,
    fieldName: normalizeText(fieldRef.fieldName),
    fieldLabel: normalizeText(fieldRef.fieldLabel),
    code: effectiveCode,
    existingCode: mode === 'use-existing' ? effectiveCode : '',
    nameEn: row.nameEn || row.name_en || effectiveCode,
    items,
    itemsText: dictionaryItemsToText(items),
    source: 'module-dictionary-confirmed',
  }
}

const applyModuleDictionaryConfirmations = async () => {
  if (moduleDictionaryBaseRows.value.length > 0) {
    await refreshModuleExistingDictionaryMatches({force: true})
  }
  if (moduleDictionaryConfirmationRows.value.length === 0) {
    message.info('当前没有需要确认的模块级字典。')
    return
  }
  if (moduleDictionaryBlockingRows.value.length > 0) {
    message.warning(`还有 ${moduleDictionaryBlockingRows.value.length} 个字典缺少编码或已有字典选择，请补充后再应用。`)
    return
  }

  const nextDraftMap = {
    ...moduleFormDraftMap.value,
  }
  let appliedFormCount = 0
  let appliedFieldCount = 0
  moduleForms.value.forEach((form, index) => {
    const formId = getModuleFormId(form, index)
    const draft = nextDraftMap[formId]
    if (!draft?.dsl) {
      return
    }
    const confirmations = {}
    moduleDictionaryConfirmationRows.value.forEach(row => {
      toArray(row.fieldRefs)
          .filter(ref => normalizeText(ref.formId) === formId && normalizeText(ref.fieldName))
          .forEach(ref => {
            confirmations[normalizeText(ref.fieldName)] = buildModuleDictionaryConfirmationForRef(row, ref)
          })
    })
    const confirmationKeys = Object.keys(confirmations)
    if (confirmationKeys.length === 0) {
      return
    }
    const nextDsl = applyDictionaryConfirmationsToDsl(draft.dsl, confirmations)
    const schemaIssues = validateFormDesignDslSchema(nextDsl)
    nextDraftMap[formId] = {
      ...draft,
      dsl: nextDsl,
      issues: mergeModuleFormDraftIssues(draft.issues, schemaIssues),
      meta: {
        ...(draft.meta || {}),
        dictionaryConfirmedAt: new Date().toISOString(),
      },
    }
    appliedFormCount += 1
    appliedFieldCount += confirmationKeys.length
  })

  if (appliedFormCount === 0) {
    message.info('当前还没有已生成的表单草稿可以应用字典确认。')
    return
  }

  moduleFormDraftMap.value = nextDraftMap
  invalidateModuleBatchCheckAndPreview()
  await nextTick()
  const nextConfirmationMap = {
    ...moduleDictionaryConfirmations.value,
  }
  moduleDictionaryConfirmationRows.value.forEach(row => {
    if (nextConfirmationMap[row.key]) {
      nextConfirmationMap[row.key] = {
        ...nextConfirmationMap[row.key],
        applied: true,
        baseSignature: row.baseSignature,
      }
    }
  })
  moduleDictionaryConfirmations.value = nextConfirmationMap
  message.success(`模块级字典确认已应用到 ${appliedFormCount} 个表单草稿，涉及 ${appliedFieldCount} 个字段。`)
}

const getModuleFormDraftConfirmIssues = (draft = {}) => {
  if (!draft?.dsl) {
    return [{
      level: 'error',
      title: '表单草稿缺少 FormDesignDSL',
      description: '请先生成该表单草稿。',
    }]
  }
  return mergeModuleFormDraftIssues(draft.issues, validateFormDesignDslSchema(draft.dsl))
}

const confirmModuleFormDraft = (form = {}, index = 0, options = {}) => {
  const silent = options.silent === true
  const draft = getModuleFormDraft(form, index)
  if (!draft.dsl) {
    if (!silent) {
      message.warning('该表单还没有生成 FormDesignDSL，生成后才能确认。')
    }
    return false
  }
  const enhancedDsl = ensureModuleFormDraftDslEnhanced(form, index) || draft.dsl
  const confirmIssues = getModuleFormDraftConfirmIssues({
    ...draft,
    dsl: enhancedDsl,
  })
  const errorIssues = confirmIssues.filter(issue => issue.level === 'error')
  if (errorIssues.length > 0) {
    console.log('ModuleFormDraftConfirmIssues', confirmIssues)
    if (!silent) {
      message.warning(`该表单草稿还有 ${errorIssues.length} 个错误，请处理后再确认。`)
    }
    return false
  }
  updateModuleFormDraft(form, index, {
    status: 'confirmed',
    issues: confirmIssues,
    meta: {
      confirmedAt: new Date().toISOString(),
    },
  })
  if (!silent) {
    message.success(`${form.title || '表单'} 草稿已确认。`)
  }
  return true
}

const batchConfirmModuleFormDrafts = () => {
  if (!moduleBlueprintConfirmed.value) {
    message.warning('请先确认模块蓝图。')
    return
  }
  if (moduleFormDraftBatchGenerating.value) {
    message.info('表单草稿正在批量生成，请稍候。')
    return
  }
  const allItems = getModuleFormDraftBatchItems()
  const targets = allItems.filter(item => item.status === 'generated' && item.draft?.dsl)
  const skipped = Math.max(allItems.length - targets.length, 0)
  if (targets.length === 0) {
    message.info('当前没有可批量确认的已生成草稿。')
    return
  }

  let successCount = 0
  const failedItems = []
  targets.forEach(item => {
    const success = confirmModuleFormDraft(item.form, item.index, {silent: true})
    if (success) {
      successCount += 1
      return
    }
    failedItems.push({
      title: item.title,
      formId: getModuleFormId(item.form, item.index),
      issues: getModuleFormDraftConfirmIssues(getModuleFormDraft(item.form, item.index)),
    })
  })

  if (failedItems.length > 0) {
    console.log('ModuleFormDraftBatchConfirmIssues', failedItems)
    message.warning(`批量确认完成：确认 ${successCount} 个，失败 ${failedItems.length} 个，跳过 ${skipped} 个。`)
    return
  }
  message.success(`批量确认完成：确认 ${successCount} 个，跳过 ${skipped} 个。`)
}

const confirmPreviewModuleFormDraft = () => {
  const {form, index} = getModuleFormDraftPreviewContext()
  if (!form) {
    message.warning('未找到当前预览的表单草稿。')
    return
  }
  confirmModuleFormDraft(form, index)
}

const regeneratePreviewModuleFormDraft = () => {
  const {form, index} = getModuleFormDraftPreviewContext()
  if (!form) {
    message.warning('未找到当前预览的表单草稿。')
    return
  }
  prepareModuleFormDraft(form, index)
}

const markModuleBlueprintDirty = () => {
  if (moduleBlueprintConfirmed.value) {
    moduleBlueprintConfirmed.value = false
  }
}

const nextModuleItemId = (items = [], prefix = 'item') => {
  const existingIds = new Set(toArray(items).map(item => normalizeText(item?.id)).filter(Boolean))
  let index = toArray(items).length + 1
  let id = `${prefix}_${String(index).padStart(2, '0')}`
  while (existingIds.has(id)) {
    index += 1
    id = `${prefix}_${String(index).padStart(2, '0')}`
  }
  return id
}

const getModulePathBase = () => {
  const moduleCode = normalizeText(moduleName.value || activeModuleMaterial.value?.module?.nameHint)
  return moduleCode ? `/${moduleCode.replace(/_/g, '/')}` : '/module'
}

const addModuleMenu = () => {
  const material = getEditableModuleMaterial()
  if (!material) return
  const id = nextModuleItemId(material.menus, 'menu')
  material.menus.push({
    id,
    title: '新菜单',
    pathHint: `${getModulePathBase()}/${id.replace(/^menu_/, 'menu')}`,
    parentId: '',
    sourceRef: '',
  })
  markModuleBlueprintDirty()
}

const removeModuleMenu = (menu = {}) => {
  const material = getEditableModuleMaterial()
  if (!material) return
  const menuId = normalizeText(menu.id)
  material.menus = toArray(material.menus).filter(item => item !== menu && normalizeText(item.id) !== menuId)
  const fallbackMenuId = normalizeText(material.menus?.[0]?.id)
  toArray(material.pages).forEach(page => {
    if (normalizeText(page.menuId) === menuId) {
      page.menuId = fallbackMenuId
    }
  })
  markModuleBlueprintDirty()
}

const addModulePage = () => {
  const material = getEditableModuleMaterial()
  if (!material) return
  const id = nextModuleItemId(material.pages, 'page')
  material.pages.push({
    id,
    menuId: normalizeText(material.menus?.[0]?.id),
    title: '新页面',
    pageType: 'list_form',
    description: '',
    sourceRefs: [],
    confidence: 0.8,
  })
  markModuleBlueprintDirty()
}

const removeModulePage = (page = {}) => {
  const material = getEditableModuleMaterial()
  if (!material) return
  const pageId = normalizeText(page.id)
  material.pages = toArray(material.pages).filter(item => item !== page && normalizeText(item.id) !== pageId)
  material.forms = toArray(material.forms).filter(form => normalizeText(form.pageId) !== pageId)
  markModuleBlueprintDirty()
}

const createModuleFormFromPage = (page = {}) => {
  const material = getEditableModuleMaterial()
  if (!material) return null
  const id = nextModuleItemId(material.forms, 'form')
  const pageId = normalizeText(page.id)
  const moduleCode = normalizeText(moduleName.value || activeModuleMaterial.value?.module?.nameHint || 'module')
  const formSuffix = pageId ? pageId.replace(/^page_/, '') : String(toArray(material.forms).length + 1).padStart(2, '0')
  return {
    id,
    pageId,
    title: `${normalizeText(page.title) || '新页面'}表单`,
    nameHint: `${moduleCode}_${formSuffix}`.replace(/[^a-zA-Z0-9_]/g, '_').toLowerCase(),
    fields: [],
    groups: [],
    listHints: [],
    queryHints: [],
    dictionaryHints: [],
    confidence: 0.8,
  }
}

const addModuleForm = (page = null) => {
  const material = getEditableModuleMaterial()
  if (!material) return
  const targetPage = page || modulePages.value[0] || {}
  const form = createModuleFormFromPage(targetPage)
  if (!form) return
  material.forms.push(form)
  markModuleBlueprintDirty()
}

const removeModuleForm = (form = {}) => {
  const material = getEditableModuleMaterial()
  if (!material) return
  const formId = normalizeText(form.id)
  material.forms = toArray(material.forms).filter(item => item !== form && normalizeText(item.id) !== formId)
  markModuleBlueprintDirty()
}

const hasPageForm = (pageId = '') => {
  const normalizedPageId = normalizeText(pageId)
  return moduleForms.value.some(form => normalizeText(form.pageId) === normalizedPageId)
}

const togglePageForm = (page = {}, checked = false) => {
  const material = getEditableModuleMaterial()
  if (!material) return
  const pageId = normalizeText(page.id)
  if (checked) {
    if (!hasPageForm(pageId)) {
      const form = createModuleFormFromPage(page)
      if (form) {
        material.forms.push(form)
      }
    }
  } else {
    material.forms = toArray(material.forms).filter(form => normalizeText(form.pageId) !== pageId)
  }
  markModuleBlueprintDirty()
}

const confirmModuleBlueprint = () => {
  if (!moduleMaterialReady.value) {
    message.warning('请先完成模块蓝图识别。')
    return
  }
  if (materialSummary.value.error > 0) {
    message.warning('当前蓝图还有结构错误，请先处理后再确认。')
    return
  }
  syncModuleFormDraftSlots()
  moduleBlueprintConfirmed.value = true
  message.success('模块蓝图已确认，可以进入表单草稿生成。')
}

const editModuleBlueprint = () => {
  moduleBlueprintConfirmed.value = false
}

const close = () => {
  emit('update:visible', false)
}

const clearModuleUrlExtractionState = () => {
  moduleUrlExtractionResult.value = null
  moduleUrlSelectedPageMap.value = {}
  moduleUrlExtractionSignature.value = ''
}

const handleModuleUrlDynamicRenderChange = () => {
  clearModuleUrlExtractionState()
  clearModuleRecognitionState()
}

const isModuleUrlPageSelected = (pageId = '') => {
  if (!pageId) {
    return false
  }
  return moduleUrlSelectedPageMap.value[pageId] !== false
}

const toggleModuleUrlPage = (pageId = '', checked = true) => {
  if (!pageId) {
    return
  }
  moduleUrlSelectedPageMap.value = {
    ...moduleUrlSelectedPageMap.value,
    [pageId]: checked,
  }
  clearModuleRecognitionState()
}

const selectAllModuleUrlPages = (checked = true) => {
  const nextMap = {}
  moduleUrlCandidatePages.value.forEach(page => {
    nextMap[page.id] = checked
  })
  moduleUrlSelectedPageMap.value = nextMap
  clearModuleRecognitionState()
}

const extractUrlPrototypeMaterial = async (force = false) => {
  if (!sourceUrl.value.trim()) {
    message.warning('请先输入原型 URL。')
    return null
  }
  if (moduleUrlExtracting.value) {
    return moduleUrlExtractionResult.value
  }
  const signature = getModuleUrlExtractionSignature()
  if (!force && moduleUrlExtractionReady.value) {
    return moduleUrlExtractionResult.value
  }
  moduleUrlExtracting.value = true
  moduleMaterialResult.value = null
  moduleBlueprintConfirmed.value = false
  resetModuleFormDraftSlots()
  try {
    const result = await extractUrlModuleMaterial(sourceUrl.value, {
      scene: moduleScene.value || 'auto',
      moduleName: moduleNameTouched.value ? moduleName.value : '',
      moduleTitle: moduleTitleTouched.value ? moduleTitle.value : '',
      supplementText: sourceText.value,
      maxPages: MODULE_BLUEPRINT_MAX_FORMS,
      maxChars: 16000,
      timeoutSeconds: moduleUrlDynamicRender.value ? 20 : 8,
      collectSameOriginLinks: true,
      dynamicRender: moduleUrlDynamicRender.value,
      fallbackToStatic: true,
      forceRefresh: force === true,
      dynamicWaitMillis: moduleUrlDynamicRender.value ? 2000 : 0,
    })
    moduleUrlExtractionResult.value = result
    moduleUrlExtractionSignature.value = signature
    const nextMap = {}
    toArray(result.pages).forEach(page => {
      nextMap[page.id] = page.included !== false
    })
    moduleUrlSelectedPageMap.value = nextMap
    console.log('UrlModuleMaterialExtraction', result)
    const cacheText = result.fromCache ? '，Redis 缓存命中' : ''
    message.success(`原型页面采集完成：${result.pages.length} 个候选页面${cacheText}，请确认后继续识别蓝图。`)
    return result
  } catch (error) {
    console.warn('UrlModuleMaterialExtractFailed', error)
    message.error(error.message || '原型 URL 采集失败')
    return null
  } finally {
    moduleUrlExtracting.value = false
  }
}

const buildUrlModuleRecognizeText = () => {
  const pages = moduleUrlSelectedPages.value
  const textParts = [
    'Prototype URL material',
    `Source URL: ${sourceUrl.value.trim()}`,
    `Confirmed pages: ${pages.length}`,
  ]
  if (sourceText.value.trim()) {
    textParts.push('User supplement:')
    textParts.push(sourceText.value.trim())
  }
  pages.forEach((page, index) => {
    textParts.push('')
    textParts.push(`Confirmed candidate page ${index + 1}:`)
    textParts.push(page.rawText || [
      `Prototype page: ${page.title || ''}`,
      `URL: ${page.url || ''}`,
      `Headings: ${toArray(page.headings).join(' / ')}`,
      `Form labels: ${toArray(page.labels).join(' / ')}`,
      `Inputs: ${toArray(page.inputs).join(' / ')}`,
      `Buttons: ${toArray(page.buttons).join(' / ')}`,
      `Text: ${page.textPreview || ''}`,
    ].join('\n'))
  })
  return textParts.join('\n').trim()
}

const resetModuleCreateState = (options = {}) => {
  if (moduleCreateBusy.value) {
    message.info('当前任务正在执行，请稍后再重置。')
    return
  }
  sourceType.value = 'text'
  sourceText.value = ''
  sourceUrl.value = ''
  moduleUrlDynamicRender.value = false
  clearModuleUrlExtractionState()
  moduleFileList.value = []
  moduleMaterialResult.value = null
  moduleBlueprintConfirmed.value = false
  moduleFormDraftPreviewVisible.value = false
  moduleFormDraftPreviewTarget.value = null
  moduleName.value = ''
  moduleTitle.value = ''
  moduleScene.value = 'normal'
  moduleNameTouched.value = false
  moduleTitleTouched.value = false
  resetModuleFormDraftSlots()
  resetInitialContext()
  if (options.silent !== true) {
    message.success('AI 创建模块已重置到初始状态。')
  }
}

const confirmResetModuleCreateState = () => {
  if (moduleCreateBusy.value) {
    message.info('当前任务正在执行，请稍后再重置。')
    return
  }
  if (!moduleCreateHasContent.value) {
    resetModuleCreateState()
    return
  }
  Modal.confirm({
    title: '重置 AI 创建模块',
    content: '将清空当前输入来源、识别结果、表单草稿、字典确认、体检和保存预览状态，确定继续吗？',
    okText: '确认重置',
    cancelText: '取消',
    onOk() {
      resetModuleCreateState()
    },
  })
}

const clearModuleRecognitionState = () => {
  moduleMaterialResult.value = null
  moduleBlueprintConfirmed.value = false
  resetModuleFormDraftSlots()
}

const selectModuleSourceType = (nextType = 'text') => {
  if (!nextType || sourceType.value === nextType) {
    return
  }
  sourceType.value = nextType
  moduleFileList.value = []
  clearModuleUrlExtractionState()
  if (nextType !== 'url') {
    sourceUrl.value = ''
  }
  clearModuleRecognitionState()
}

const resetInitialContext = () => {
  const hasFormContext = Boolean(props.formModel?.module || props.formModel?.comments)
  const hasUserInput = Boolean(sourceText.value.trim() || sourceUrl.value.trim() || moduleFileList.value.length > 0 || moduleMaterialResult.value)
  if (!hasFormContext && !hasUserInput) {
    moduleName.value = ''
    moduleTitle.value = ''
    moduleNameTouched.value = false
    moduleTitleTouched.value = false
    return
  }
  if (!moduleName.value && props.formModel?.module) {
    moduleName.value = props.formModel.module
    moduleNameTouched.value = false
  }
  if (!moduleTitle.value && props.formModel?.comments) {
    moduleTitle.value = `${props.formModel.comments}模块`
    moduleTitleTouched.value = false
  }
}

const handleModuleFileBeforeUpload = (file = {}) => {
  const fileSourceType = getModuleFileSourceType(file)
  const currentConfig = currentFileSourceConfig.value
  if (!fileSourceType) {
    message.warning(currentConfig.unsupportedText || '当前文件类型暂不支持')
    return false
  }
  if (!toArray(currentConfig.allowedTypes).includes(fileSourceType)) {
    message.warning(currentConfig.unsupportedText || '当前来源不支持该文件类型，请切换来源后重试')
    return false
  }
  if (currentConfig.maxSize && Number(file.size || 0) > currentConfig.maxSize) {
    message.warning(`${sourceTypeText.value} 文件不能超过 ${currentConfig.maxSizeText}`)
    return false
  }
  clearModuleRecognitionState()
  moduleFileList.value = [{
    uid: file.uid || `module-file-${Date.now()}`,
    name: file.name || '未命名文件',
    status: 'done',
    size: file.size || 0,
    type: file.type || '',
    originFileObj: file,
  }]
  message.info(`已选择 ${getModuleFileKindText(file)} 文件，可点击识别模块蓝图`)
  return false
}

const removeModuleFile = () => {
  moduleFileList.value = []
  clearModuleRecognitionState()
  return true
}

const getModuleRawFile = () => {
  const file = moduleFileList.value[0]
  return file?.originFileObj || file || null
}

const isExcelModuleFile = (file = {}) => excelFileNamePattern.test(file.name || '')

const isWordModuleFile = (file = {}) => wordFileNamePattern.test(file.name || '')

const isPdfModuleFile = (file = {}) => pdfFileNamePattern.test(file.name || '')

const isImageModuleFile = (file = {}) => {
  const fileName = file.name || ''
  const mimeType = file.type || ''
  return imageFileNamePattern.test(fileName) || String(mimeType).startsWith('image/')
}

const getModuleFileSourceType = (file = {}) => {
  if (isExcelModuleFile(file)) return 'excel'
  if (isWordModuleFile(file)) return 'word'
  if (isPdfModuleFile(file)) return 'pdf'
  if (isImageModuleFile(file)) return 'image'
  return ''
}

const getModuleFileKindText = (file = {}) => {
  const fileSourceType = getModuleFileSourceType(file)
  if (fileSourceType === 'excel') return 'Excel'
  if (fileSourceType === 'word') return 'Word'
  if (fileSourceType === 'pdf') return 'PDF'
  if (fileSourceType === 'image') return '图片'
  return '文件'
}

const recognizeModuleBlueprint = async () => {
  if (!canRecognizeBlueprint.value) {
    message.warning('请先填写模块材料或选择文件')
    return null
  }
  let result
  moduleMaterialResult.value = null
  moduleBlueprintConfirmed.value = false
  resetModuleFormDraftSlots()
  moduleMaterialRecognizing.value = true
  try {
    if (sourceType.value === 'url' && sourceUrl.value.trim()) {
      moduleMaterialRecognizing.value = false
      const hadUrlExtractionReady = moduleUrlExtractionReady.value
      const extraction = await extractUrlPrototypeMaterial()
      if (!extraction) {
        return null
      }
      if (!hadUrlExtractionReady) {
        return null
      }
      if (!moduleUrlExtractionReady.value) {
        return null
      }
      if (moduleUrlSelectedPages.value.length === 0) {
        message.warning('请至少选择一个原型页面候选。')
        return null
      }
      moduleMaterialRecognizing.value = true
      result = await recognizeRemoteModuleMaterial(buildUrlModuleRecognizeText(), {
        sourceType: 'url',
        scene: moduleScene.value || 'auto',
        moduleName: moduleNameTouched.value ? moduleName.value : '',
        moduleTitle: moduleTitleTouched.value ? moduleTitle.value : '',
        sourceUrl: sourceUrl.value,
      })
      result.extraction = {
        ...(result.extraction || {}),
        ...(moduleUrlExtractionResult.value?.extraction || {}),
        selectedPageCount: moduleUrlSelectedPages.value.length,
        pages: moduleUrlSelectedPages.value.map(page => ({
          id: page.id,
          title: page.title,
          url: page.url,
          headingCount: toArray(page.headings).length,
          labelCount: toArray(page.labels).length,
          inputCount: toArray(page.inputs).length,
          tableCount: toArray(page.tables).length,
        })),
      }
      result.material = {
        ...(result.material || {}),
        meta: {
          ...(result.material?.meta || {}),
          extraction: result.extraction,
        },
      }
    } else if (isFileSourceType.value && moduleFileList.value.length > 0) {
      const file = getModuleRawFile()
      const fileSourceType = getModuleFileSourceType(file)
      if (!fileSourceType) {
        message.warning(currentFileSourceConfig.value.unsupportedText || '当前文件类型暂不支持')
        return null
      }
      if (!toArray(currentFileSourceConfig.value.allowedTypes).includes(fileSourceType)) {
        message.warning(currentFileSourceConfig.value.unsupportedText || '当前来源不支持该文件类型，请切换来源后重试')
        return null
      }
      result = await recognizeFileModuleMaterial(file, {
        sourceType: fileSourceType,
        scene: moduleScene.value || 'auto',
        moduleName: moduleNameTouched.value ? moduleName.value : '',
        moduleTitle: moduleTitleTouched.value ? moduleTitle.value : '',
        extractAllSheets: fileSourceType === 'excel',
        maxRows: fileSourceType === 'excel' ? 80 : 120,
        maxColumns: 80,
        maxPages: 5,
        maxChars: 16000,
        extractTables: true,
        extractHeaders: true,
        ocrProvider: 'auto',
        ocrLanguage: 'chi_sim+eng',
      })
    } else {
      const textParts = []
      if (sourceType.value === 'url' && sourceUrl.value.trim()) {
        textParts.push(`原型 URL：${sourceUrl.value.trim()}`)
      }
      if (sourceText.value.trim()) {
        textParts.push(sourceText.value.trim())
      }
      result = await recognizeRemoteModuleMaterial(textParts.join('\n\n'), {
        sourceType: sourceType.value,
        scene: moduleScene.value || 'auto',
        moduleName: moduleNameTouched.value ? moduleName.value : '',
        moduleTitle: moduleTitleTouched.value ? moduleTitle.value : '',
        sourceUrl: sourceUrl.value,
      })
    }
  } catch (error) {
    console.warn('ModuleMaterialRecognizeFailed', error)
    const failureResult = createModuleRecognizeFailureResult(error)
    moduleMaterialResult.value = failureResult
    console.log('ModuleMaterialIssues', materialIssues.value)
    message.error(error.message || 'AI 模块蓝图识别失败')
    return failureResult
  } finally {
    moduleMaterialRecognizing.value = false
  }

  moduleMaterialResult.value = result
  const recognizedModule = result.material?.module || {}
  if (!moduleNameTouched.value && recognizedModule.nameHint) {
    moduleName.value = recognizedModule.nameHint
  }
  if (!moduleTitleTouched.value && recognizedModule.title) {
    moduleTitle.value = recognizedModule.title
  }
  await refreshModuleExistingDictionaryMatches({force: true})
  console.log('ModuleMaterial', result.material)
  console.log('ModuleMaterialIssues', materialIssues.value)
  if (!result.success) {
    message.error(`模块蓝图识别未通过结构检查：${result.message || result.errorCode || '请查看结构提示'}`)
  } else if (!moduleMaterialHasBlueprint.value) {
    message.warning('AI 暂未识别出页面或表单，请补充模块页面和表单说明后重试')
  } else if ((result.summary?.error || 0) > 0) {
    message.error(`模块蓝图识别完成，但有 ${result.summary.error} 个错误需要处理`)
  } else if ((result.summary?.warning || 0) > 0) {
    message.warning(`模块蓝图识别完成，发现 ${result.summary.warning} 个警告`)
  } else {
    const elapsedText = result.elapsedMs ? `，耗时 ${(result.elapsedMs / 1000).toFixed(1)} 秒` : ''
    message.success(`AI 已识别 ${modulePages.value.length} 个页面、${moduleForms.value.length} 个表单${elapsedText}`)
  }
  return result
}

const getFormFields = (form = {}) => toArray(form.fields)

const getFormListFieldCount = (form = {}) => getFormFields(form).filter(field => field.listHint === true).length

const getFormQueryFieldCount = (form = {}) => getFormFields(form).filter(field => field.queryHint === true).length

const getFormDictionaryHintCount = (form = {}, index = -1) => {
  if (index >= 0) {
    const draft = getModuleFormDraft(form, index)
    if (draft?.dsl) {
      return Object.keys(createDictionaryConfirmationMap(draft.dsl)).length
    }
  }
  return getFormMaterialDictionaryHints(form).length
}

const printModuleMaterial = () => {
  console.log('ModuleMaterial', activeModuleMaterial.value)
  console.log('ModuleMaterialIssues', materialIssues.value)
  message.success('ModuleMaterial 已输出到控制台')
}

const printModuleDesign = () => {
  console.log('ModuleDesignDSL', moduleDesignDraft.value)
  console.log('ModuleDesignIssues', designIssues.value)
  message.success('ModuleDesignDSL 已输出到控制台')
}

watch(() => props.visible, (visible) => {
  if (visible) {
    resetInitialContext()
  }
}, {immediate: true})

watch(moduleDictionaryBaseRows, () => {
  syncModuleDictionaryConfirmations()
  refreshModuleExistingDictionaryMatches()
}, {immediate: true, deep: true})
</script>

<style lang="less" scoped>
.ai-module-drawer {
  min-height: 100%;
  background: #f6f8fb;
}

.module-flow-panel {
  padding: 16px 18px;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
}

.module-flow-head,
.module-section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.module-flow-title,
.module-section-title {
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
}

.module-flow-status,
.module-section-subtitle {
  margin-top: 2px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.module-flow-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.module-url-candidates {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #f8fbff;
}

.module-url-options {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.module-url-render-option {
  min-width: 0;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.module-url-render-title {
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
}

.module-url-render-desc {
  margin-top: 2px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.module-url-candidate-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.module-url-candidate-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.module-url-candidate-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.module-url-page-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.module-url-page-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.module-url-page-body {
  min-width: 0;
  flex: 1;
}

.module-url-page-title {
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
}

.module-url-page-url {
  margin-top: 2px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.module-url-page-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.module-url-page-quality-reasons {
  margin-top: 6px;
  color: #2563eb;
  font-size: 12px;
  line-height: 1.5;
}

.module-url-page-preview {
  margin-top: 6px;
  color: #475569;
  font-size: 12px;
  line-height: 1.6;
}

.module-flow-steps {
  display: grid;
  grid-template-columns: repeat(7, minmax(88px, 1fr));
  gap: 8px;
  margin-top: 14px;
}

.module-flow-step {
  min-height: 70px;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
  display: flex;
  gap: 8px;
}

.module-flow-step.active {
  border-color: #93c5fd;
  background: #eff6ff;
}

.module-flow-step.done {
  border-color: #86efac;
  background: #f0fdf4;
}

.module-flow-index {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #dbeafe;
  color: #1d4ed8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex: 0 0 auto;
}

.module-flow-body {
  min-width: 0;
}

.module-flow-step-title {
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.35;
}

.module-flow-step-state {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.35;
}

.module-main-layout {
  padding: 14px 18px 24px;
}

.module-section {
  margin-bottom: 12px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
}

.module-compact-summary {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
  display: grid;
  grid-template-columns: minmax(120px, 0.8fr) minmax(160px, 1fr) minmax(0, 1.4fr);
  gap: 10px;
  align-items: center;
}

.module-info-summary {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.module-summary-label {
  display: block;
  margin-bottom: 3px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-summary-desc {
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.module-source-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.module-source-option {
  min-height: 76px;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.module-source-option.active {
  border-color: #2563eb;
  background: #eff6ff;
}

.module-source-title {
  display: block;
  color: #1f2937;
  font-size: 14px;
  font-weight: 600;
}

.module-source-desc {
  display: block;
  margin-top: 5px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-input-area {
  margin-top: 12px;
}

.module-input-gap {
  margin-top: 10px;
}

.module-input-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.module-input-meta {
  min-width: 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.module-upload-placeholder {
  padding: 14px;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  gap: 12px;
}

.module-upload-placeholder > div {
  min-width: 0;
  flex: 1;
}

.module-upload-title {
  color: #1f2937;
  font-weight: 600;
}

.module-upload-desc {
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
  flex: 1;
}

.module-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.module-field {
  min-width: 0;
}

.module-field.wide {
  grid-column: 1 / -1;
}

.module-field > span {
  display: block;
  margin-bottom: 5px;
  color: #475569;
  font-size: 12px;
}

.module-empty-state {
  margin-top: 12px;
  padding: 18px;
  border-radius: 6px;
  background: #f8fafc;
  text-align: center;
}

.module-empty-title {
  color: #1f2937;
  font-weight: 600;
}

.module-empty-desc {
  margin-top: 5px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.module-blueprint {
  display: grid;
  grid-template-columns: minmax(180px, 0.8fr) minmax(0, 1.2fr);
  gap: 12px;
  margin-top: 12px;
}

.module-blueprint-actions,
.module-editor-actions,
.module-form-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.module-blueprint-editor {
  margin-top: 12px;
}

.module-editor-toolbar {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.module-editor-hint {
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.module-blueprint-column {
  min-width: 0;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}

.module-blueprint-title {
  margin-bottom: 8px;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.module-blueprint-item,
.module-page-item {
  padding: 8px 0;
  border-top: 1px solid #e5e7eb;
}

.module-blueprint-item:first-of-type,
.module-page-item:first-of-type {
  border-top: 0;
}

.module-page-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.module-page-readonly-tags {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.module-edit-item {
  padding: 10px 0;
  border-top: 1px solid #e5e7eb;
}

.module-edit-item:first-of-type {
  border-top: 0;
}

.module-edit-grid,
.module-form-edit-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.module-edit-grid.page {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.module-edit-field {
  min-width: 0;
}

.module-edit-field.wide {
  grid-column: 1 / -1;
}

.module-edit-field > span {
  display: block;
  margin-bottom: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-edit-row-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.module-page-edit-head {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.module-page-generate {
  color: #475569;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.module-blueprint-main,
.module-blueprint-sub {
  display: block;
}

.module-blueprint-main {
  color: #1f2937;
  font-weight: 600;
}

.module-blueprint-sub {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-mini-empty {
  color: #94a3b8;
  font-size: 12px;
}

.module-recognition-warning {
  margin-top: 12px;
  padding: 14px;
  border: 1px solid #fed7aa;
  border-radius: 6px;
  background: #fff7ed;
}

.module-recognition-warning-title {
  color: #9a3412;
  font-weight: 600;
}

.module-recognition-warning-desc {
  margin-top: 5px;
  color: #9a3412;
  font-size: 12px;
  line-height: 1.6;
}

.module-issue-list {
  margin-top: 10px;
  display: grid;
  gap: 6px;
}

.module-issue-item {
  color: #475569;
  font-size: 12px;
  line-height: 1.5;
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.module-overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.module-overview-item {
  min-height: 72px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 4px;
  color: #64748b;
}

.module-overview-count {
  color: #1f2937;
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.module-form-list {
  margin-top: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.module-form-toolbar {
  margin-top: 12px;
  padding: 8px 10px;
  border: 1px solid #dbeafe;
  border-radius: 6px;
  background: #eff6ff;
  color: #475569;
  font-size: 12px;
  justify-content: space-between;
}

.module-batch-toolbar {
  align-items: center;
}

.module-batch-summary {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
  line-height: 1.5;
}

.module-form-toolbar-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.module-form-item {
  padding: 10px 12px;
  border-top: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.module-form-item:first-child {
  border-top: 0;
}

.module-form-edit-main {
  min-width: 0;
  flex: 1;
}

.module-form-edit-grid {
  grid-template-columns: minmax(120px, 0.9fr) minmax(130px, 1fr) minmax(120px, 0.9fr);
}

.module-form-title {
  color: #1f2937;
  font-weight: 600;
}

.module-form-sub {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
}

.module-form-tags {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: flex-end;
  gap: 6px;
}

.module-form-status-tags,
.module-form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.module-dictionary-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.module-dictionary-tags {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.module-dictionary-empty {
  margin-top: 12px;
  padding: 18px;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  background: #f8fafc;
  text-align: center;
}

.module-dictionary-list {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.module-dictionary-row {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}

.module-dictionary-row-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.module-dictionary-main {
  min-width: 0;
}

.module-dictionary-main strong,
.module-dictionary-main span {
  display: block;
  overflow-wrap: anywhere;
}

.module-dictionary-main strong {
  color: #1f2937;
  font-weight: 600;
  line-height: 1.4;
}

.module-dictionary-main span {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-dictionary-row-tags,
.module-dictionary-field-refs {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.module-dictionary-field-refs {
  justify-content: flex-start;
  margin-top: 8px;
}

.module-dictionary-edit-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: minmax(130px, 0.8fr) minmax(160px, 1fr) minmax(180px, 1.1fr);
  gap: 8px;
}

.module-dictionary-edit-grid.existing {
  grid-template-columns: minmax(130px, 0.8fr) minmax(260px, 1.8fr);
}

.module-dictionary-edit-item {
  min-width: 0;
}

.module-dictionary-edit-item > span,
.module-dictionary-edit-label {
  display: block;
  margin-bottom: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-dictionary-items-editor {
  margin-top: 10px;
}

.module-batch-check-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.module-batch-check-tags {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.module-batch-check-empty {
  margin-top: 12px;
  padding: 18px;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  background: #f8fafc;
  text-align: center;
}

.module-batch-check-result {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.module-batch-check-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.module-batch-check-summary > div {
  min-width: 0;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}

.module-batch-check-summary span,
.module-batch-check-summary strong {
  display: block;
  overflow-wrap: anywhere;
}

.module-batch-check-summary span {
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-batch-check-summary strong {
  margin-top: 3px;
  color: #1f2937;
  font-weight: 600;
  line-height: 1.4;
}

.module-batch-check-issue-list {
  display: grid;
  gap: 8px;
}

.module-batch-check-issue-item {
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.module-batch-check-issue-item > div {
  min-width: 0;
}

.module-batch-check-issue-item strong,
.module-batch-check-issue-item span {
  display: block;
  overflow-wrap: anywhere;
}

.module-batch-check-issue-item strong {
  color: #1f2937;
  font-weight: 600;
  line-height: 1.4;
}

.module-batch-check-issue-item span,
.module-batch-check-more {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.module-batch-check-more {
  padding: 8px 10px;
}

.module-formalize-preview-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.module-formalize-preview-tags {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.module-formalize-preview-empty {
  margin-top: 12px;
  padding: 18px;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  background: #f8fafc;
  text-align: center;
}

.module-formalize-preview-result {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.module-formalize-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.module-formalize-summary > div,
.module-formalize-card,
.module-formalize-blocking {
  min-width: 0;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}

.module-formalize-summary span,
.module-formalize-summary strong {
  display: block;
  overflow-wrap: anywhere;
}

.module-formalize-summary span {
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-formalize-summary strong {
  margin-top: 3px;
  color: #1f2937;
  font-weight: 600;
  line-height: 1.4;
}

.module-formalize-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.module-formalize-card-title {
  margin-bottom: 8px;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.module-formalize-row {
  padding: 8px 0;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.module-formalize-row:first-of-type {
  border-top: 0;
}

.module-formalize-row > div {
  min-width: 0;
}

.module-formalize-row strong,
.module-formalize-row span {
  display: block;
  overflow-wrap: anywhere;
}

.module-formalize-row strong {
  color: #1f2937;
  font-weight: 600;
  line-height: 1.4;
}

.module-formalize-row span,
.module-formalize-more {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.module-formalize-step-list {
  display: grid;
  gap: 6px;
}

.module-formalize-step {
  color: #475569;
  font-size: 12px;
  line-height: 1.5;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.module-formalize-step span {
  min-width: 0;
  overflow-wrap: anywhere;
}

.module-formalize-blocking {
  border-color: #fecaca;
  background: #fef2f2;
}

.module-formalize-save-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.module-formalize-save-tags {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.module-formalize-save-empty {
  margin-top: 12px;
  padding: 18px;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  background: #f8fafc;
  text-align: center;
}

.module-formalize-save-result {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.module-formalize-save-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.module-formalize-save-summary > div {
  min-width: 0;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}

.module-formalize-save-summary span,
.module-formalize-save-summary strong {
  display: block;
  overflow-wrap: anywhere;
}

.module-formalize-save-summary span {
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-formalize-save-summary strong {
  margin-top: 3px;
  color: #1f2937;
  font-weight: 600;
  line-height: 1.4;
}

.module-workbench-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.module-workbench-item {
  min-height: 54px;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}

.module-workbench-title,
.module-workbench-state {
  display: block;
}

.module-workbench-title {
  color: #1f2937;
  font-weight: 600;
}

.module-workbench-state {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.module-advanced {
  margin-top: 4px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
}

.module-debug-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.module-json-preview {
  max-height: 260px;
  margin: 0;
  padding: 10px;
  border-radius: 6px;
  background: #0f172a;
  color: #dbeafe;
  font-size: 12px;
  line-height: 1.6;
  overflow: auto;
}

.module-draft-preview {
  display: grid;
  gap: 12px;
}

.module-draft-preview-head {
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.module-draft-preview-title {
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
}

.module-draft-preview-sub {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.module-draft-preview-summary,
.module-draft-meta-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
}

.module-draft-preview-summary > div,
.module-draft-meta-grid > div {
  min-width: 0;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}

.module-draft-preview-summary span,
.module-draft-meta-grid span {
  display: block;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.module-draft-preview-summary strong,
.module-draft-meta-grid strong {
  display: block;
  margin-top: 3px;
  color: #1f2937;
  font-weight: 600;
  line-height: 1.4;
  word-break: break-all;
}

.module-draft-preview-card {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
}

.module-draft-preview-card-title {
  margin-bottom: 10px;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.module-draft-field-table {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow-x: auto;
}

.module-draft-field-row {
  min-width: 880px;
  display: grid;
  grid-template-columns: minmax(150px, 1.4fr) minmax(96px, 0.8fr) minmax(96px, 0.8fr) minmax(96px, 0.8fr) 58px 58px 58px 58px;
  align-items: center;
  border-top: 1px solid #e5e7eb;
}

.module-draft-field-row:first-child {
  border-top: 0;
}

.module-draft-field-row.head {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
}

.module-draft-field-row > span {
  min-width: 0;
  padding: 8px 10px;
  color: #475569;
  font-size: 12px;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.module-draft-field-main strong,
.module-draft-field-main em {
  display: block;
}

.module-draft-field-main strong {
  color: #1f2937;
  font-weight: 600;
}

.module-draft-field-main em {
  color: #64748b;
  font-style: normal;
}

.module-draft-dictionary-list,
.module-draft-issue-list {
  display: grid;
  gap: 8px;
}

.module-draft-dictionary-item {
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
  display: grid;
  grid-template-columns: minmax(140px, 1.2fr) 88px minmax(120px, 1fr) minmax(0, 1.4fr);
  align-items: center;
  gap: 8px;
  color: #475569;
  font-size: 12px;
}

.module-draft-dictionary-item strong,
.module-draft-dictionary-item span {
  min-width: 0;
  overflow-wrap: anywhere;
}

.module-draft-dictionary-item strong {
  display: block;
  color: #1f2937;
}

.module-draft-dictionary-items {
  color: #64748b;
}

.module-draft-issue-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #475569;
  font-size: 12px;
  line-height: 1.5;
}

.module-draft-preview-footer {
  padding-top: 4px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 1100px) {
  .module-flow-steps,
  .module-source-grid,
  .module-overview-grid,
  .module-dictionary-edit-grid,
  .module-dictionary-edit-grid.existing,
  .module-batch-check-summary,
  .module-workbench-list,
  .module-draft-preview-summary,
  .module-draft-meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .module-draft-dictionary-item {
    grid-template-columns: minmax(0, 1fr) minmax(88px, auto);
  }
}

@media (max-width: 760px) {
  .module-flow-head,
  .module-section-head,
  .module-input-footer,
  .module-upload-placeholder,
  .module-editor-toolbar,
  .module-form-toolbar,
  .module-dictionary-actions,
  .module-dictionary-row-head,
  .module-batch-check-actions,
  .module-draft-preview-head,
  .module-draft-preview-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .module-flow-steps,
  .module-source-grid,
  .module-meta-grid,
  .module-blueprint,
  .module-edit-grid,
  .module-edit-grid.page,
  .module-form-edit-grid,
  .module-overview-grid,
  .module-dictionary-edit-grid,
  .module-dictionary-edit-grid.existing,
  .module-batch-check-summary,
  .module-workbench-list,
  .module-draft-preview-summary,
  .module-draft-meta-grid,
  .module-draft-dictionary-item {
    grid-template-columns: 1fr;
  }

  .module-form-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .module-form-toolbar-actions {
    justify-content: flex-start;
  }

  .module-draft-preview-footer {
    align-items: stretch;
  }
}
</style>
