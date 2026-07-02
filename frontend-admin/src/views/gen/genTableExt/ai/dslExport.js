const DSL_VERSION = '1.0'

const unwrap = (value) => {
  if (value && typeof value === 'object' && 'value' in value) {
    return value.value
  }
  return value
}

const toArray = (value) => {
  const unwrapped = unwrap(value)
  return Array.isArray(unwrapped) ? unwrapped : []
}

const clone = (value) => {
  if (value === undefined || value === null) {
    return value
  }
  try {
    return JSON.parse(JSON.stringify(value))
  } catch (e) {
    return value
  }
}

const parseJson = (value, fallback = {}) => {
  if (!value) {
    return fallback
  }
  if (typeof value === 'object') {
    return clone(value)
  }
  try {
    return JSON.parse(value)
  } catch (e) {
    return fallback
  }
}

const toBoolean = (value) => {
  return value === true || value === '1' || value === 1
}

const getColumnType = (column = {}, item = {}) => {
  if (item.type) {
    return item.type
  }
  const showType = column.showType
  if (showType === 'textarea') return 'textarea'
  if (showType === 'dateselect') return 'date'
  if (showType === 'select' || showType === 'radiobox' || showType === 'checkbox') return 'select'
  if (showType === 'fileupload' || showType === 'fileuploadpic' || showType === 'fileuploadonline') return 'upload'
  if (showType === 'treeselectRedio') return 'user'
  if (showType === 'treeselectCheck') return 'users'
  if (showType === 'officeselectTree') return 'office'
  if (showType === 'areaselect') return 'area'
  if (showType === 'gridselect') return 'modalSelect'
  if (showType === 'switch') return 'switch'
  if (showType === 'richText') return 'richText'
  if (showType === 'serialNo') return 'serialNo'
  return 'text'
}

const normalizeField = (item, source) => {
  const column = clone(item.column || item) || {}
  const formItemConfig = parseJson(column.formItemConfig)
  const listConfig = parseJson(column.listConfig)
  const formItemProps = clone(item.formItemProps || formItemConfig.formItemProps || {})
  const formControlProps = clone(item.formControlProps || formItemConfig.formControlProps || {})
  const colProps = clone(item.colProps || formItemConfig.colProps || {})
  const isDynamicItem = Boolean(item.column)
  const name = isDynamicItem ? (formItemProps.name || column.name || item.name || '') : (column.name || formItemProps.name || item.name || '')
  const label = isDynamicItem ? (formItemProps.label || column.comments || column.label || '') : (column.comments || column.label || formItemProps.label || '')

  return {
    id: item.id || column.id || name,
    source,
    name,
    label,
    type: getColumnType(column, item),
    key: item.key || '',
    showType: column.showType || '',
    javaField: column.javaField || '',
    jdbcType: column.jdbcType || '',
    javaType: column.javaType || '',
    required: formItemProps.required !== undefined ? toBoolean(formItemProps.required) : column.isNull === '0',
    readonly: formControlProps.disabled !== undefined ? toBoolean(formControlProps.disabled) : toBoolean(column.isReadonly),
    span: Number(colProps.span || (column.isOneLine === '1' ? 24 : 12)),
    isForm: column.isForm !== undefined ? toBoolean(column.isForm) : source === 'display',
    isList: toBoolean(column.isList),
    isQuery: toBoolean(column.isQuery),
    isExtend: toBoolean(column.isExtend),
    formSort: Number(column.formSort || 0),
    listSort: Number(column.listSort || 0),
    queryType: column.queryType || '',
    dictType: column.dictType || '',
    dateType: column.dateType || '',
    defaultValue: formControlProps.defaultValue !== undefined ? formControlProps.defaultValue : column.defaultValue,
    list: {
      dataIndex: listConfig.dataIndex || column.name || name,
      title: listConfig.title || column.comments || label,
      align: listConfig.align || column.align || '',
      minWidth: listConfig.minWidth || 150,
      sorter: listConfig.sorter || 'false',
      sortColumn: listConfig.sortColumn || '',
      ellipsis: listConfig.ellipsis === undefined ? false : listConfig.ellipsis,
      dict: listConfig.dict || column.dictType || '',
      dictMultiple: listConfig.dictMultiple || false,
      queryColumn: listConfig.queryColumn || '',
      queryDefaultValue: listConfig.queryDefaultValue || '',
      queryFieldType: listConfig.queryFieldType || '',
      queryFieldProps: listConfig.queryFieldProps || null,
      widthMultiple: listConfig.widthMultiple || 1,
      status: listConfig.status || null,
    },
    form: {
      props: clone(formItemProps),
      controlProps: clone(formControlProps),
      colProps: clone(colProps),
      formItemConfig: clone(formItemConfig),
    },
    raw: {
      column,
    },
  }
}

const normalizeMockColumn = (column = {}) => {
  const listConfig = parseJson(column.listConfig)
  return {
    name: column.name || '',
    source: toBoolean(column.isExtend) ? 'extend' : 'column',
    isExtend: toBoolean(column.isExtend),
    isList: toBoolean(column.isList),
    isQuery: toBoolean(column.isQuery),
    listSort: Number(column.listSort || 0),
    queryType: column.queryType || '',
    dataIndex: listConfig.dataIndex || column.listConfig__dataIndex || column.name || '',
    title: listConfig.title || column.listConfig__title || column.comments || '',
    align: listConfig.align || column.listConfig__align || column.align || '',
    minWidth: listConfig.minWidth || column.listConfig__minWidth || 150,
    dict: listConfig.dict || column.listConfig__dict || column.dictType || '',
    queryColumn: listConfig.queryColumn || column.listConfig__queryColumn || '',
    queryFieldType: listConfig.queryFieldType || column.listConfig__queryFieldType || '',
    queryFieldProps: listConfig.queryFieldProps || column.listConfig__queryFieldProps || null,
    raw: clone(column),
  }
}

/**
 * Export the current genTableExt designer state to an AI-friendly intermediate DSL.
 *
 * This function is intentionally side-effect free. It reads current in-memory
 * designer state and returns a normalized object that can be checked, diffed,
 * sent to AI, or transformed back into existing gen_table metadata later.
 */
export const exportCurrentDesignToDsl = (context = {}) => {
  const formModel = unwrap(context.formModel) || {}
  const table = unwrap(context.table) || {}
  const formPropsModel = unwrap(context.formPropsModel) || {}
  const extendJsConfig = unwrap(context.extendJsConfig) || {}
  const displayFormItemArr = toArray(context.displayFormItemArr)
  const hideFormItemArr = toArray(context.hideFormItemArr)
  const fixedArr = toArray(context.fixedArr)
  const mockColumns = toArray(context.mockColumns)
  const modalMultiSelectFields = displayFormItemArr
      .concat(hideFormItemArr)
      .filter(item => item.type === 'modalMultiSelect')

  const fields = []
      .concat(displayFormItemArr.filter(item => item.type !== 'modalMultiSelect').map(item => normalizeField(item, 'display')))
      .concat(hideFormItemArr.filter(item => item.type !== 'modalMultiSelect').map(item => normalizeField(item, 'hidden')))
      .concat(fixedArr.map(item => normalizeField(item, 'fixed')))
      .concat((extendJsConfig.extendColumns || []).map(item => normalizeField(item, 'extend')))
      .concat(modalMultiSelectFields.map(item => normalizeField(item, 'modalMultiSelect')))
      .sort((a, b) => Number(a.formSort) - Number(b.formSort))

  const columns = mockColumns.map(normalizeMockColumn)
  const listButtons = clone(extendJsConfig.list__buttons || [])
  const rowButtons = clone(extendJsConfig.singleTable__rowButtons || [])

  return {
    version: DSL_VERSION,
    exportedAt: new Date().toISOString(),
    form: {
      id: formModel.id || table.id || '',
      name: formModel.name || table.name || '',
      title: formModel.comments || table.comments || '',
      titleEn: formModel.comments_EN || table.comments_EN || '',
      module: formModel.module || table.module || '',
      tableType: formModel.tableType || table.tableType || '',
      parentTable: formModel.parentTable || table.parentTable || '',
      parentTableFk: formModel.parentTableFk || table.parentTableFk || '',
      pkColumnName: formModel.pkColumnName || table.pkColumnName || 'id',
      processDefinitionCategory: formModel.processDefinitionCategory || table.processDefinitionCategory || '',
      isVersion: formModel.isVersion || table.isVersion || '',
      tableSort: formModel.tableSort || table.tableSort || '',
    },
    layout: {
      labelWidth: Number(formPropsModel.labelWidth || 100),
      modalFull: formPropsModel.modal__Full === '1',
      modalWidth: Number(formPropsModel.modal__Width || 1200),
      modalTitle: formPropsModel.list__modalTitle || '',
      modalTitleFuncStr: formPropsModel.list__modalTitleFuncStr || '',
      mainTableTitle: formPropsModel.mainTableTitle || formModel.comments || table.comments || '',
      subTableType: formPropsModel.subTableType || '',
      anchorWidth: formPropsModel.anchorWidth || '',
      anchorLocation: formPropsModel.anchorLocation || '',
      saveBeforeListAddItem: formPropsModel.saveBeforeListAddItem,
    },
    fields,
    list: {
      columns,
      visibleColumns: columns.filter(item => item.isList),
      queryColumns: columns.filter(item => item.isQuery),
      buttons: listButtons,
      rowButtons,
      customSingleTableViewProps: clone(extendJsConfig.customSingleTableViewProps || {}),
      noOptButton: extendJsConfig.noOptButton || '0',
      noRowButton: extendJsConfig.noRowButton || '0',
      noCheckbox: extendJsConfig.noCheckbox || '0',
      batchSelect: extendJsConfig.batchSelect || '0',
    },
    actions: {
      clickButtons: Object.keys(extendJsConfig)
          .filter(key => key.indexOf('clickButton__') === 0)
          .reduce((obj, key) => {
            obj[key.replace('clickButton__', '')] = extendJsConfig[key]
            return obj
          }, {}),
      clickRows: Object.keys(extendJsConfig)
          .filter(key => key.indexOf('clickRow__') === 0)
          .reduce((obj, key) => {
            obj[key.replace('clickRow__', '')] = extendJsConfig[key]
            return obj
          }, {}),
    },
    raw: {
      table: clone(table),
      formModel: clone(formModel),
      formProps: clone(formPropsModel),
      extendJs: clone(extendJsConfig),
    },
  }
}

export default exportCurrentDesignToDsl
