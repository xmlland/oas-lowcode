const LEVEL_ERROR = 'error';
const LEVEL_WARNING = 'warning';
const LEVEL_SUGGESTION = 'suggestion';

const FORM_NAME_PATTERN = /^[a-z][a-z0-9_]*$/;

const DEFAULT_PROTECTED_FIELD_NAMES = [
  'id',
  'del_flag',
  'delFlag',
  'create_by',
  'createBy.id',
  'create_date',
  'createDate',
  'update_by',
  'updateBy.id',
  'update_date',
  'updateDate',
  'remarks',
  'owner_code',
  'ownerCode',
];

const toArray = (value) => Array.isArray(value) ? value : [];

const normalizeText = (value) => String(value || '').trim();

const isEmpty = (value) => value === undefined || value === null || normalizeText(value) === '';

const parseJsonWithError = (value, label) => {
  if (value === undefined || value === null || value === '') {
    return {
      ok: true,
      value: null,
    };
  }
  if (typeof value === 'object') {
    return {
      ok: true,
      value,
    };
  }
  try {
    return {
      ok: true,
      value: JSON.parse(value),
    };
  } catch (error) {
    return {
      ok: false,
      label,
      error,
    };
  }
};

const createIssue = ({
  id,
  level = LEVEL_ERROR,
  title,
  fieldName = '',
  fieldLabel = '',
  description = '',
  suggestion = '',
  fixable = false,
  fix = null,
  meta = {},
}) => ({
  id,
  level,
  title,
  fieldName,
  fieldLabel,
  description,
  suggestion,
  fixable,
  fix,
  meta: {
    source: 'formalize-check',
    ...meta,
  },
});

const groupBy = (items, getter) => {
  const map = {};
  toArray(items).forEach(item => {
    const key = normalizeText(getter(item));
    if (!key) {
      return;
    }
    if (!map[key]) {
      map[key] = [];
    }
    map[key].push(item);
  });
  return Object.keys(map)
      .filter(key => map[key].length > 1)
      .map(key => ({
        key,
        items: map[key],
      }));
};

const getSubmitColumns = (preview = {}) => toArray(preview.submitDataPreview?.json);

const getDslFields = (preview = {}) => toArray(preview.dsl?.fields);

const getBusinessFields = (preview = {}) => {
  return getDslFields(preview).filter(field => !['fixed', 'extend'].includes(field.source));
};

const getFixedFieldNames = (preview = {}, options = {}) => {
  const fromDsl = getDslFields(preview)
      .filter(field => field.source === 'fixed')
      .map(field => field.name || field.javaField)
      .filter(Boolean);
  return Array.from(new Set(DEFAULT_PROTECTED_FIELD_NAMES.concat(options.fixedFieldNames || []).concat(fromDsl)));
};

const checkTargetInfo = (preview = {}) => {
  const target = preview.target || {};
  const issues = [];

  if (isEmpty(target.formName)) {
    issues.push(createIssue({
      id: 'formalize-form-name-missing',
      title: '表名不能为空',
      description: '正式保存需要明确的表单名称，它会作为 gen_table.name 和后续动态表单编号使用。',
      suggestion: '请先补充小写字母、数字、下划线格式的表名，例如 contract_apply。',
    }));
  } else if (!FORM_NAME_PATTERN.test(normalizeText(target.formName))) {
    issues.push(createIssue({
      id: `formalize-form-name-invalid:${target.formName}`,
      title: '表名格式不合法',
      description: '表名只能使用小写字母、数字和下划线，并且必须以字母开头。',
      suggestion: '请将表名改为类似 contract_apply 的格式。',
      meta: {
        formName: target.formName,
      },
    }));
  }

  if (isEmpty(target.formTitle)) {
    issues.push(createIssue({
      id: 'formalize-form-title-missing',
      title: '表单标题不能为空',
      description: '表单标题会用于设计器、弹窗标题和用户可读名称。',
      suggestion: '请补充一个面向用户的中文标题。',
    }));
  }

  if (isEmpty(target.module)) {
    issues.push(createIssue({
      id: 'formalize-module-missing',
      title: '模块不能为空',
      description: '模块会影响表单归属、生成路径和权限编码。',
      suggestion: '请补充模块名，例如 oas、demo、admin。',
    }));
  } else if (!FORM_NAME_PATTERN.test(normalizeText(target.module))) {
    issues.push(createIssue({
      id: `formalize-module-invalid:${target.module}`,
      title: '模块名格式不合法',
      description: '模块名只能使用小写字母、数字和下划线，并且必须以字母开头。',
      suggestion: '请将模块名改为小写下划线格式。',
      meta: {
        module: target.module,
      },
    }));
  }

  if (isEmpty(target.pkColumnName)) {
    issues.push(createIssue({
      id: 'formalize-pk-column-missing',
      title: '主键字段不能为空',
      description: '正式保存需要明确主键字段，默认应为 id。',
      suggestion: '请将主键字段设置为 id，或确认当前表单确实使用其他主键。',
    }));
  }

  return issues;
};

const checkNameConflicts = (preview = {}, options = {}) => {
  const target = preview.target || {};
  const formName = normalizeText(target.formName);
  const existingFormNames = toArray(options.existingFormNames).map(normalizeText);
  const existingDbTableNames = toArray(options.existingDbTableNames).map(normalizeText);
  const issues = [];

  if (target.mode === 'create' && formName && existingFormNames.includes(formName)) {
    issues.push(createIssue({
      id: `formalize-form-name-duplicated:${formName}`,
      title: '表定义已存在',
      description: `系统中已经存在名为 ${formName} 的表单定义，直接正式保存会与已有 gen_table 冲突。`,
      suggestion: '请更换表名，或改为基于已有表单的优化流程。',
      meta: {
        formName,
      },
    }));
  }

  if (target.mode === 'create' && formName && existingDbTableNames.includes(formName)) {
    issues.push(createIssue({
      id: `formalize-db-table-exists:${formName}`,
      title: '数据库物理表已存在',
      description: `数据库中已经存在 ${formName}，新建动态表单时应先从数据库导入表结构。`,
      suggestion: '请使用现有导入表流程，避免 AI 草稿直接覆盖已有业务表语义。',
      meta: {
        formName,
      },
    }));
  }

  return issues;
};

const checkFieldPresence = (preview = {}) => {
  const summary = preview.summary || {};
  const issues = [];

  if (summary.fieldCount === 0) {
    issues.push(createIssue({
      id: 'formalize-no-fields',
      title: '没有字段可保存',
      description: '当前正式化预览没有任何字段，保存后不是一个可用的表单设计。',
      suggestion: '请先生成或添加业务字段。',
    }));
  }

  if (summary.displayCount === 0) {
    issues.push(createIssue({
      id: 'formalize-no-display-fields',
      title: '显示区域没有字段',
      description: '正式表单至少应有一个显示区域字段，否则用户打开表单时可能看不到可填写内容。',
      suggestion: '请至少保留一个显示区域业务字段。',
    }));
  }

  if (summary.submitColumnCount === 0) {
    issues.push(createIssue({
      id: 'formalize-no-submit-columns',
      title: '没有提交字段',
      description: '正式保存请求中没有 gen_table_column 字段数据。',
      suggestion: '请检查正式化预览生成逻辑或当前设计器字段状态。',
    }));
  }

  return issues;
};

const checkDuplicateColumns = (preview = {}) => {
  const columns = getSubmitColumns(preview);
  const issues = [];

  groupBy(columns, column => column.name).forEach(({key, items}) => {
    issues.push(createIssue({
      id: `formalize-duplicate-column-name:${key}`,
      title: '提交字段名重复',
      fieldName: key,
      description: `即将保存的字段名 ${key} 出现 ${items.length} 次，保存后可能互相覆盖。`,
      suggestion: '请重命名其中一个字段，确保字段名唯一。',
      meta: {
        fields: items.map(item => ({
          name: item.name,
          javaField: item.javaField,
          comments: item.comments,
        })),
      },
    }));
  });

  groupBy(columns, column => column.javaField).forEach(({key, items}) => {
    issues.push(createIssue({
      id: `formalize-duplicate-java-field:${key}`,
      title: '底层字段重复',
      fieldName: key,
      description: `即将保存的 javaField ${key} 出现 ${items.length} 次，可能造成数据字段冲突。`,
      suggestion: '请检查字段复制或底层字段分配。',
      meta: {
        fields: items.map(item => ({
          name: item.name,
          javaField: item.javaField,
          comments: item.comments,
        })),
      },
    }));
  });

  return issues;
};

const checkProtectedFieldConflict = (preview = {}, options = {}) => {
  const protectedNames = getFixedFieldNames(preview, options);
  const protectedSet = new Set(protectedNames.map(normalizeText).filter(Boolean));

  return getBusinessFields(preview)
      .filter(field => !(field.source === 'hidden' && (
        protectedSet.has(normalizeText(field.name)) || protectedSet.has(normalizeText(field.javaField))
      )))
      .filter(field => protectedSet.has(normalizeText(field.name)) || protectedSet.has(normalizeText(field.javaField)))
      .map(field => createIssue({
        id: `formalize-protected-field-conflict:${field.name || field.javaField}`,
        title: '字段与系统固定字段冲突',
        fieldName: field.name || field.javaField || '',
        fieldLabel: field.label || '',
        description: '业务字段不能与 id、创建人、创建时间、备注、删除标记、归属机构等系统固定字段冲突。',
        suggestion: '请重命名该业务字段，或确认它应作为系统固定字段处理。',
        meta: {
          protectedNames,
          field,
        },
      }));
};

const checkRequiredColumnProperties = (preview = {}) => {
  const issues = [];
  getSubmitColumns(preview).forEach((column, index) => {
    const missing = [];
    if (isEmpty(column.name)) missing.push('name');
    if (isEmpty(column.javaField)) missing.push('javaField');
    if (isEmpty(column.comments)) missing.push('comments');
    if (isEmpty(column.showType)) missing.push('showType');
    if (isEmpty(column.jdbcType)) missing.push('jdbcType');

    if (missing.length > 0) {
      issues.push(createIssue({
        id: `formalize-column-required-missing:${column.name || column.javaField || index}`,
        title: '提交字段缺少必要属性',
        fieldName: column.name || column.javaField || '',
        fieldLabel: column.comments || '',
        description: `字段缺少必要属性：${missing.join('、')}。`,
        suggestion: '请补齐字段名、底层字段、字段说明、控件类型和数据库类型后再正式保存。',
        meta: {
          index,
          missing,
          column,
        },
      }));
    }
  });
  return issues;
};

const checkJsonFields = (preview = {}) => {
  const submitData = preview.submitDataPreview || {};
  const issues = [];
  const rootJsonChecks = [
    ['formProps', submitData.formProps],
    ['extendJs', submitData.extendJs],
    ['extJava', submitData.extJava],
  ];

  rootJsonChecks.forEach(([label, value]) => {
    const result = parseJsonWithError(value, label);
    if (!result.ok) {
      issues.push(createIssue({
        id: `formalize-json-invalid:${label}`,
        title: 'JSON 配置不合法',
        description: `${label} 不是合法 JSON，正式保存后后端或运行期可能解析失败。`,
        suggestion: '请检查该配置项的 JSON 字符串。',
        meta: {
          label,
          message: result.error?.message || '',
        },
      }));
    }
  });

  getSubmitColumns(preview).forEach((column, index) => {
    [
      ['formItemConfig', column.formItemConfig],
      ['listConfig', column.listConfig],
      ['settings', column.settings],
    ].forEach(([label, value]) => {
      const result = parseJsonWithError(value, label);
      if (!result.ok) {
        issues.push(createIssue({
          id: `formalize-column-json-invalid:${column.name || index}:${label}`,
          title: '字段 JSON 配置不合法',
          fieldName: column.name || '',
          fieldLabel: column.comments || '',
          description: `字段 ${column.comments || column.name || index} 的 ${label} 不是合法 JSON。`,
          suggestion: '请返回设计器检查该字段配置。',
          meta: {
            label,
            index,
            message: result.error?.message || '',
          },
        }));
      }
    });
  });

  return issues;
};

const checkSourceContext = (preview = {}, options = {}) => {
  const source = preview.source || {};
  const checksum = preview.checksum || {};
  const issues = [];
  const hasAiSource = Boolean(source.draftId || source.versionId || source.draftTitle)
      || source.type === 'server-draft'
      || source.type === 'local-draft'
      || source.type === 'ai-draft'
      || source.type === 'form-material';

  if (options.requireAiSource && !hasAiSource) {
    issues.push(createIssue({
      id: 'formalize-ai-source-missing',
      level: LEVEL_WARNING,
      title: '缺少 AI 草稿来源',
      description: '当前正式化预览没有明确的 AI 草稿来源，保存后将无法建立完整追溯关系。',
      suggestion: '建议从服务器草稿打开后再进入正式保存流程。',
    }));
  }

  if (options.requireRemoteSource && isEmpty(source.draftId)) {
    issues.push(createIssue({
      id: 'formalize-remote-draft-id-missing',
      level: LEVEL_WARNING,
      title: '缺少服务器草稿 ID',
      description: '当前预览无法关联服务器草稿，正式保存成功后无法自动回写草稿状态。',
      suggestion: '建议先保存到服务器草稿，或打开服务器草稿后再正式化。',
    }));
  }

  if (hasAiSource && isEmpty(source.versionId)) {
    issues.push(createIssue({
      id: 'formalize-draft-version-missing',
      level: LEVEL_WARNING,
      title: '缺少草稿版本 ID',
      description: '缺少版本 ID 会降低正式保存追溯精度。',
      suggestion: '建议从服务器草稿的具体版本进入正式化流程。',
    }));
  }

  if (options.latestVersionId && source.versionId && options.latestVersionId !== source.versionId) {
    issues.push(createIssue({
      id: 'formalize-draft-version-not-latest',
      level: LEVEL_WARNING,
      title: '当前不是服务器最新草稿版本',
      description: '当前页面使用的草稿版本不是服务器最新版本，继续保存可能覆盖较新的设计意图。',
      suggestion: '建议先打开服务器最新版本确认后再正式保存。',
      meta: {
        currentVersionId: source.versionId,
        latestVersionId: options.latestVersionId,
      },
    }));
  }

  if (checksum.changed) {
    issues.push(createIssue({
      id: 'formalize-source-checksum-changed',
      level: LEVEL_WARNING,
      title: '草稿应用后已被人工修改',
      description: '当前正式化预览与来源草稿 checksum 不一致，说明应用草稿后可能又做过人工调整。',
      suggestion: '这是允许的，但建议在正式保存前重新检查字段、列表和查询配置。',
      meta: {
        before: checksum.before,
        after: checksum.after,
      },
    }));
  }

  return issues;
};

export const checkFormalizePreview = (preview = {}, options = {}) => {
  const issues = []
      .concat(checkTargetInfo(preview, options))
      .concat(checkNameConflicts(preview, options))
      .concat(checkFieldPresence(preview, options))
      .concat(checkDuplicateColumns(preview, options))
      .concat(checkProtectedFieldConflict(preview, options))
      .concat(checkRequiredColumnProperties(preview, options))
      .concat(checkJsonFields(preview, options))
      .concat(checkSourceContext(preview, options));

  return issues;
};

export const summarizeFormalizeIssues = (issues = []) => {
  const summary = {
    total: toArray(issues).length,
    error: 0,
    warning: 0,
    suggestion: 0,
    fixable: 0,
  };

  toArray(issues).forEach(issue => {
    if (summary[issue.level] !== undefined) {
      summary[issue.level] += 1;
    }
    if (issue.fixable) {
      summary.fixable += 1;
    }
  });

  return summary;
};

export const buildFormalizeCheckResult = (preview = {}, options = {}) => {
  const issues = checkFormalizePreview(preview, options);
  const summary = summarizeFormalizeIssues(issues);

  return {
    preview,
    issues,
    summary,
    errors: issues.filter(issue => issue.level === LEVEL_ERROR),
    warnings: issues.filter(issue => issue.level === LEVEL_WARNING),
    suggestions: issues.filter(issue => issue.level === LEVEL_SUGGESTION),
    canFormalize: summary.error === 0,
    needConfirm: summary.error === 0 && summary.total > 0,
    topIssues: issues.slice(0, 8),
  };
};

export const mergeFormalizeIssuesToPreview = (preview = {}, issues = []) => {
  const nextPreview = {
    ...preview,
    issues: toArray(issues),
    warnings: toArray(issues).filter(issue => issue.level === LEVEL_WARNING),
    errors: toArray(issues).filter(issue => issue.level === LEVEL_ERROR),
  };

  nextPreview.summary = {
    ...(preview.summary || {}),
    errorCount: nextPreview.errors.length,
    warningCount: nextPreview.warnings.length,
    suggestionCount: toArray(issues).filter(issue => issue.level === LEVEL_SUGGESTION).length,
  };
  nextPreview.canFormalize = nextPreview.errors.length === 0;

  return nextPreview;
};

export default checkFormalizePreview;
