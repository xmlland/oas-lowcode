<template>
  <a-drawer
      :visible="visible"
      title="AI体检报告"
      placement="right"
      :width="640"
      :z-index="2000"
      :body-style="{ padding: '0', overflow: 'auto' }"
      @close="close"
  >
    <div class="ai-check-drawer">
      <div class="ai-check-toolbar">
        <div class="ai-check-summary">
          <div class="summary-item error">
            <span class="summary-count">{{ summary.error || 0 }}</span>
            <span>错误</span>
          </div>
          <div class="summary-item warning">
            <span class="summary-count">{{ summary.warning || 0 }}</span>
            <span>警告</span>
          </div>
          <div class="summary-item suggestion">
            <span class="summary-count">{{ summary.suggestion || 0 }}</span>
            <span>建议</span>
          </div>
          <div class="summary-item fixable">
            <span class="summary-count">{{ summary.fixable || 0 }}</span>
            <span>可修复</span>
          </div>
        </div>
        <div class="ai-check-actions">
          <a-checkbox v-model:checked="showFixableOnly">只看可修复</a-checkbox>
          <a-button size="small" @click="expandAll">全部展开</a-button>
          <a-button size="small" @click="collapseAll">全部收起</a-button>
        </div>
      </div>

      <a-empty v-if="issues.length === 0" class="ai-check-empty" description="未发现问题"/>

      <a-collapse v-else v-model:activeKey="activeKeys" class="ai-check-collapse" :bordered="false">
        <a-collapse-panel
            v-for="group in groupedIssues"
            :key="group.key"
            :class="['level-panel', group.key]"
        >
          <template #header>
            <div class="group-header">
              <span class="group-title">{{ group.title }}</span>
              <a-tag :color="group.color">{{ group.issues.length }}</a-tag>
            </div>
          </template>

          <div v-if="group.issues.length === 0" class="group-empty">暂无</div>

          <div
              v-for="issue in group.issues"
              :key="issue.id"
              class="issue-row"
          >
            <div class="issue-select">
              <a-checkbox
                  v-if="issue.fixable"
                  :checked="selectedIssueIds.includes(issue.id)"
                  @change="toggleIssue(issue.id, $event.target.checked)"
              />
            </div>
            <div class="issue-body">
              <div class="issue-title-line">
                <span class="issue-title">{{ issue.title }}</span>
                <a-tag v-if="issue.fixable" color="blue">可修复</a-tag>
                <a-tag v-else>手工处理</a-tag>
              </div>
              <div class="issue-field">
                <span>{{ issue.fieldLabel || issue.fieldName || '-' }}</span>
                <span v-if="issue.fieldName" class="field-name">{{ issue.fieldName }}</span>
              </div>
              <div class="issue-desc">{{ issue.description }}</div>
              <div class="issue-suggestion">{{ issue.suggestion }}</div>
              <div v-if="issue.aiExplanation" class="issue-ai-explain">
                <div class="ai-explain-head">
                  <span>AI解读</span>
                  <a-tag color="cyan">本地规则</a-tag>
                </div>
                <div class="ai-explain-summary">{{ issue.aiExplanation.summary }}</div>
                <div class="ai-explain-line">
                  <span class="ai-explain-label">影响</span>
                  <span>{{ issue.aiExplanation.risk }}</span>
                </div>
                <div class="ai-explain-line">
                  <span class="ai-explain-label">处理</span>
                  <span>{{ issue.aiExplanation.fixPlan || issue.aiExplanation.action }}</span>
                </div>
                <div v-if="issue.aiExplanation.context && issue.aiExplanation.context.length" class="ai-explain-context">
                  <span v-for="item in issue.aiExplanation.context" :key="item">{{ item }}</span>
                </div>
              </div>
            </div>
          </div>
        </a-collapse-panel>
      </a-collapse>

      <div v-if="previewVisible" class="fix-preview">
        <div class="fix-preview-header">
          <div>
            <div class="fix-preview-title">修复预览</div>
            <div class="fix-preview-subtitle">
              已生成 {{ previewResult.changes.length }} 项字段变更预览，确认前不会修改设计器。
            </div>
          </div>
          <a-button size="small" @click="previewVisible = false">收起预览</a-button>
        </div>

        <a-empty
            v-if="previewResult.changes.length === 0"
            class="fix-preview-empty"
            description="当前选择项没有可预览的变更"
        />

        <template v-else>
          <div
              v-for="group in previewGroups"
              :key="group.fieldName"
              class="preview-field"
          >
            <div class="preview-field-title">
              <span>{{ group.fieldLabel || group.fieldName }}</span>
              <span class="field-name">{{ group.fieldName }}</span>
            </div>
            <div
                v-for="change in group.changes"
                :key="change.id"
                class="preview-change"
            >
              <div class="preview-change-label">{{ change.label }}</div>
              <div class="preview-change-value before">{{ change.beforeText }}</div>
              <div class="preview-change-arrow">-></div>
              <div class="preview-change-value after">{{ change.afterText }}</div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <div>已选择 {{ selectedIssueIds.length }} / {{ selectableIssues.length }}</div>
        <div class="footer-actions">
          <a-button :disabled="selectedIssues.length === 0" @click="previewFixes">预览修复</a-button>
          <a-button type="primary" :disabled="selectedIssues.length === 0" @click="applyFixes">应用到设计器</a-button>
        </div>
      </div>
    </template>
  </a-drawer>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {generateFixPreview} from "@/views/gen/genTableExt/ai/designFixPreview";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  issues: {
    type: Array,
    default() {
      return []
    },
  },
  summary: {
    type: Object,
    default() {
      return {
        total: 0,
        error: 0,
        warning: 0,
        suggestion: 0,
        fixable: 0,
      }
    },
  },
  dsl: {
    type: Object,
    default() {
      return {}
    },
  },
})

const emit = defineEmits(['update:visible', 'apply-fixes'])

const showFixableOnly = ref(false)
const selectedIssueIds = ref([])
const activeKeys = ref(['error', 'warning', 'suggestion'])
const previewVisible = ref(false)
const previewResult = ref({
  issueCount: 0,
  changes: [],
  unsupportedIssues: [],
})

const levelConfig = [
  {key: 'error', title: '错误', color: 'red'},
  {key: 'warning', title: '警告', color: 'orange'},
  {key: 'suggestion', title: '建议', color: 'green'},
]

const visibleIssues = computed(() => {
  if (showFixableOnly.value) {
    return props.issues.filter(issue => issue.fixable)
  }
  return props.issues
})

const selectableIssues = computed(() => props.issues.filter(issue => issue.fixable))

const selectedIssues = computed(() => {
  return selectableIssues.value.filter(issue => selectedIssueIds.value.includes(issue.id))
})

const groupedIssues = computed(() => {
  return levelConfig.map(config => ({
    ...config,
    issues: visibleIssues.value.filter(issue => issue.level === config.key),
  }))
})

const previewGroups = computed(() => {
  const groupMap = {}
  previewResult.value.changes.forEach(change => {
    const key = change.fieldName || change.issueId
    if (!groupMap[key]) {
      groupMap[key] = {
        fieldName: change.fieldName,
        fieldLabel: change.fieldLabel,
        changes: [],
      }
    }
    groupMap[key].changes.push(change)
  })
  return Object.keys(groupMap).map(key => groupMap[key])
})

watch(() => props.issues, () => {
  selectedIssueIds.value = selectableIssues.value.map(issue => issue.id)
  activeKeys.value = levelConfig
      .filter(config => props.issues.some(issue => issue.level === config.key))
      .map(config => config.key)
  clearPreview()
}, {deep: true})

const close = () => {
  emit('update:visible', false)
}

const toggleIssue = (id, checked) => {
  if (checked) {
    if (!selectedIssueIds.value.includes(id)) {
      selectedIssueIds.value.push(id)
    }
  } else {
    selectedIssueIds.value = selectedIssueIds.value.filter(item => item !== id)
  }
  clearPreview()
}

const expandAll = () => {
  activeKeys.value = levelConfig.map(item => item.key)
}

const collapseAll = () => {
  activeKeys.value = []
}

const clearPreview = () => {
  previewVisible.value = false
  previewResult.value = {
    issueCount: 0,
    changes: [],
    unsupportedIssues: [],
  }
}

const previewFixes = () => {
  previewResult.value = generateFixPreview(props.dsl, selectedIssues.value)
  previewVisible.value = true
}

const applyFixes = () => {
  emit('apply-fixes', selectedIssues.value)
}
</script>

<style scoped lang="less">
.ai-check-drawer {
  min-height: 100%;
  padding-top: 8px;
  background: #f6f8fb;
}

.ai-check-toolbar {
  padding: 16px 16px 14px;
  background: #fff;
  border-bottom: 1px solid #e8edf3;
}

.ai-check-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
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
}

.summary-item.error .summary-count {
  color: #d9363e;
}

.summary-item.warning .summary-count {
  color: #d48806;
}

.summary-item.suggestion .summary-count {
  color: #389e0d;
}

.summary-item.fixable .summary-count {
  color: #1677ff;
}

.ai-check-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ai-check-empty {
  margin-top: 80px;
}

.ai-check-collapse {
  background: transparent;
}

.fix-preview {
  margin: 12px 16px 24px;
  border: 1px solid #dbe7f3;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
}

.fix-preview-header {
  padding: 12px 16px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid #e8edf3;
}

.fix-preview-title {
  font-weight: 600;
  color: #1f2937;
}

.fix-preview-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.fix-preview-empty {
  padding: 24px 0;
}

.preview-field {
  padding: 12px 16px;
  border-top: 1px solid #f0f3f7;
}

.preview-field:first-of-type {
  border-top: 0;
}

.preview-field-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #1f2937;
}

.preview-change {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr) 24px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  color: #4b5563;
}

.preview-change-label {
  color: #374151;
}

.preview-change-value {
  min-width: 0;
  word-break: break-all;
  border-radius: 4px;
  padding: 4px 8px;
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
}

.preview-change-value.before {
  background: #fff7e6;
  color: #8a4b00;
}

.preview-change-value.after {
  background: #f0f7ff;
  color: #0958d9;
}

.preview-change-arrow {
  color: #9ca3af;
  text-align: center;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.group-title {
  font-weight: 600;
}

.group-empty {
  padding: 16px;
  color: #8c8c8c;
}

.issue-row {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #edf1f5;
}

.issue-select {
  width: 20px;
  padding-top: 2px;
}

.issue-body {
  min-width: 0;
  flex: 1;
}

.issue-title-line {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 24px;
}

.issue-title {
  font-weight: 600;
  color: #1f2937;
}

.issue-field {
  margin-top: 4px;
  color: #4b5563;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.field-name {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  border-radius: 4px;
  padding: 1px 6px;
}

.issue-desc,
.issue-suggestion {
  margin-top: 6px;
  line-height: 1.5;
  color: #4b5563;
}

.issue-suggestion {
  color: #1f4e79;
}

.issue-ai-explain {
  margin-top: 10px;
  padding: 10px 12px;
  border: 1px solid #d7edf2;
  border-radius: 6px;
  background: #f7fcfd;
  color: #334155;
}

.ai-explain-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #155e75;
}

.ai-explain-summary {
  margin-top: 6px;
  line-height: 1.55;
}

.ai-explain-line {
  margin-top: 6px;
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 8px;
  line-height: 1.55;
}

.ai-explain-label {
  color: #64748b;
}

.ai-explain-context {
  margin-top: 8px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.ai-explain-context span {
  padding: 1px 6px;
  border-radius: 4px;
  background: #e6f4f7;
  color: #155e75;
  font-size: 12px;
}

.drawer-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 720px) {
  .ai-check-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .drawer-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .preview-change {
    grid-template-columns: 80px minmax(0, 1fr);
  }

  .preview-change-arrow {
    display: none;
  }
}
</style>
