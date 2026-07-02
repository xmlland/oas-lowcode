import {transformDynamicFormItemConfigToGenTableColumn} from "@/views/gen/dynamicFormItem";
import {exportCurrentDesignToDsl} from "@/views/gen/genTableExt/ai/dslExport";

const EMPTY_ARRAY = [];

const unwrap = (value) => {
  if (value && typeof value === 'object' && 'value' in value) {
    return value.value;
  }
  return value;
};

const toArray = (value) => {
  const unwrapped = unwrap(value);
  return Array.isArray(unwrapped) ? unwrapped : EMPTY_ARRAY;
};

const clone = (value) => {
  if (value === undefined || value === null) {
    return value;
  }
  try {
    return JSON.parse(JSON.stringify(value));
  } catch (e) {
    return value;
  }
};

const normalizeText = (value) => String(value || '').trim();

const isYes = (value) => value === true || value === '1' || value === 1 || value === 'true';

const countBy = (items, predicate) => toArray(items).filter(predicate).length;

const getLevelCount = (issues = [], level) => countBy(issues, issue => issue.level === level);

const stableStringify = (value) => {
  if (value === null || typeof value !== 'object') {
    return JSON.stringify(value);
  }
  if (Array.isArray(value)) {
    return `[${value.map(item => stableStringify(item)).join(',')}]`;
  }
  return `{${Object.keys(value).sort().map(key => `${JSON.stringify(key)}:${stableStringify(value[key])}`).join(',')}}`;
};

const hashText = (text = '') => {
  let hash = 5381;
  for (let i = 0; i < text.length; i += 1) {
    hash = ((hash << 5) + hash) + text.charCodeAt(i);
    hash &= 0xffffffff;
  }
  return `h${(hash >>> 0).toString(16).padStart(8, '0')}`;
};

export const createFormalizeChecksum = (value) => hashText(stableStringify(value || {}));

const getListButtons = (extendJsConfig = {}) => toArray(extendJsConfig.list__buttons);

const getRowButtons = (extendJsConfig = {}) => toArray(extendJsConfig.singleTable__rowButtons);

const buildJsonColumns = (displayItems = [], hiddenItems = [], fixedItems = []) => {
  const json = [];
  const extJavaItems = [];

  displayItems.concat(hiddenItems).forEach((item, index) => {
    const clonedItem = clone(item);
    const column = transformDynamicFormItemConfigToGenTableColumn(clonedItem);
    column.formSort = index * 5;
    if (clonedItem.type === 'modalMultiSelect') {
      extJavaItems.push(column);
    } else {
      json.push(column);
    }
  });

  fixedItems.forEach(item => {
    json.push(clone(item));
  });

  return {
    json,
    extJavaItems,
  };
};

export const buildFormalizeSubmitDataPreview = (context = {}) => {
  const formModel = unwrap(context.formModel) || {};
  const formPropsModel = unwrap(context.formPropsModel) || {};
  const extendJsConfig = unwrap(context.extendJsConfig) || {};
  const displayItems = toArray(context.displayFormItemArr);
  const hiddenItems = toArray(context.hideFormItemArr);
  const fixedItems = toArray(context.fixedArr);
  const {json, extJavaItems} = buildJsonColumns(displayItems, hiddenItems, fixedItems);

  return {
    isBuildImport: getListButtons(extendJsConfig).some(item => item.value === 'import') ? '1' : '0',
    isBuildExport: getListButtons(extendJsConfig).some(item => item.value === 'export') ? '1' : '0',
    json,
    extJava: JSON.stringify(extJavaItems),
    formProps: JSON.stringify(clone(formPropsModel) || {}),
    extendJs: JSON.stringify(clone(extendJsConfig) || {}),
    pkColumnName: formModel.pkColumnName || 'id',
  };
};

const buildGenTablePreview = (context = {}, submitData = {}) => {
  const table = unwrap(context.table) || {};
  const formModel = unwrap(context.formModel) || {};

  return {
    id: formModel.id || table.id || '',
    name: formModel.name || table.name || '',
    comments: formModel.comments || table.comments || '',
    comments_EN: formModel.comments_EN || table.comments_EN || '',
    module: formModel.module || table.module || '',
    tableType: formModel.tableType || table.tableType || '0',
    parentTable: formModel.parentTable || table.parentTable || '',
    parentTableFk: formModel.parentTableFk || table.parentTableFk || '',
    pkColumnName: submitData.pkColumnName || formModel.pkColumnName || table.pkColumnName || 'id',
    isBuildImport: submitData.isBuildImport || '0',
    isBuildExport: submitData.isBuildExport || '0',
    formProps: submitData.formProps || '{}',
    extendJs: submitData.extendJs || '{}',
    extJava: submitData.extJava || '[]',
  };
};

const buildSource = (options = {}) => {
  const source = options.source || {};
  const draft = options.draft || {};
  const version = options.version || {};

  return {
    type: source.type || draft.sourceType || 'current-designer',
    draftId: source.draftId || draft.id || '',
    versionId: source.versionId || version.id || draft.currentVersionId || '',
    draftTitle: source.draftTitle || draft.title || '',
    versionName: source.versionName || version.name || '',
    versionNo: source.versionNo || version.versionNo || '',
    checksum: source.checksum || version.checksum || '',
  };
};

const buildTarget = ({dsl = {}, genTablePreview = {}, options = {}}) => {
  const target = options.target || {};
  const form = dsl.form || {};
  const genTableId = target.genTableId || genTablePreview.id || form.id || '';

  return {
    mode: target.mode || (genTableId ? 'optimize' : 'create'),
    genTableId,
    formName: target.formName || genTablePreview.name || form.name || '',
    formTitle: target.formTitle || genTablePreview.comments || form.title || '',
    module: target.module || genTablePreview.module || form.module || '',
    tableType: target.tableType || genTablePreview.tableType || form.tableType || '0',
    pkColumnName: target.pkColumnName || genTablePreview.pkColumnName || form.pkColumnName || 'id',
  };
};

const buildFieldPreview = (dsl = {}, submitData = {}) => {
  const fields = toArray(dsl.fields);
  const columns = toArray(submitData.json);
  const extJava = (() => {
    try {
      return JSON.parse(submitData.extJava || '[]');
    } catch (e) {
      return [];
    }
  })();

  return {
    fields: fields.map(field => ({
      name: field.name || '',
      label: field.label || '',
      source: field.source || '',
      type: field.type || '',
      isForm: Boolean(field.isForm),
      isList: Boolean(field.isList),
      isQuery: Boolean(field.isQuery),
      span: field.span || '',
    })),
    columns: columns.map(column => ({
      name: column.name || '',
      javaField: column.javaField || '',
      comments: column.comments || '',
      showType: column.showType || '',
      jdbcType: column.jdbcType || '',
      isForm: column.isForm || '0',
      isList: column.isList || '0',
      isQuery: column.isQuery || '0',
      formSort: column.formSort,
      listSort: column.listSort,
      searchSort: column.searchSort,
    })),
    extJava: extJava.map(column => ({
      name: column.name || '',
      javaField: column.javaField || '',
      comments: column.comments || '',
      showType: column.showType || '',
      jdbcType: column.jdbcType || '',
    })),
  };
};

export const summarizeFormalizePreview = (preview = {}) => {
  const dsl = preview.dsl || {};
  const fields = toArray(dsl.fields);
  const submitData = preview.submitDataPreview || {};
  const columns = toArray(submitData.json);
  const extJava = (() => {
    try {
      return JSON.parse(submitData.extJava || '[]');
    } catch (e) {
      return [];
    }
  })();
  const issues = toArray(preview.issues);
  const extendJsConfig = (() => {
    try {
      return JSON.parse(submitData.extendJs || '{}');
    } catch (e) {
      return {};
    }
  })();

  return {
    fieldCount: fields.length,
    displayCount: countBy(fields, field => field.source === 'display' || field.isForm),
    hiddenCount: countBy(fields, field => field.source === 'hidden'),
    fixedCount: countBy(fields, field => field.source === 'fixed'),
    extendCount: countBy(fields, field => field.source === 'extend'),
    modalMultiSelectCount: extJava.length,
    submitColumnCount: columns.length,
    listCount: countBy(columns, column => isYes(column.isList)),
    queryCount: countBy(columns, column => isYes(column.isQuery)),
    errorCount: getLevelCount(issues, 'error'),
    warningCount: getLevelCount(issues, 'warning'),
    suggestionCount: getLevelCount(issues, 'suggestion'),
    listButtonCount: getListButtons(extendJsConfig).length,
    rowButtonCount: getRowButtons(extendJsConfig).length,
  };
};

export const buildFormalizePreview = (context = {}, options = {}) => {
  const dsl = options.dsl || exportCurrentDesignToDsl(context);
  const submitDataPreview = options.submitDataPreview || buildFormalizeSubmitDataPreview(context);
  const genTablePreview = buildGenTablePreview(context, submitDataPreview);
  const source = buildSource(options);
  const target = buildTarget({
    dsl,
    genTablePreview,
    options,
  });
  const issues = toArray(options.issues).map(issue => clone(issue));
  const checksumBase = {
    target,
    submitDataPreview,
  };
  const afterChecksum = createFormalizeChecksum(checksumBase);
  const beforeChecksum = options.beforeChecksum || source.checksum || '';

  const preview = {
    version: '1.0',
    previewType: 'formalize',
    createdAt: new Date().toISOString(),
    source,
    target,
    dsl,
    genTablePreview,
    submitDataPreview,
    fieldPreview: buildFieldPreview(dsl, submitDataPreview),
    issues,
    warnings: issues.filter(issue => issue.level === 'warning'),
    errors: issues.filter(issue => issue.level === 'error'),
    checksum: {
      before: beforeChecksum,
      after: afterChecksum,
      changed: beforeChecksum ? beforeChecksum !== afterChecksum : false,
    },
    raw: {
      table: clone(unwrap(context.table) || {}),
      formModel: clone(unwrap(context.formModel) || {}),
    },
  };

  preview.summary = summarizeFormalizePreview(preview);
  preview.canFormalize = preview.summary.errorCount === 0;

  return preview;
};

export default buildFormalizePreview;
