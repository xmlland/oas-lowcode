const toArray = (value) => Array.isArray(value) ? value : []

const normalizeText = (value) => String(value || '').trim()

const isEmpty = (value) => value === undefined || value === null || value === ''

const levelTextMap = {
  error: '错误',
  warning: '警告',
  suggestion: '建议',
}

const fixTypeTextMap = {
  updateColumn: '调整字段基础配置',
  updateListConfig: '调整列表配置',
  updateFormColProps: '调整表单布局',
}

const findField = (dsl = {}, fieldName = '') => {
  return toArray(dsl.fields).find(field => field.name === fieldName) || null
}

const findListColumn = (dsl = {}, fieldName = '') => {
  return toArray(dsl.list?.columns).find(column => column.name === fieldName) || null
}

const getFieldDisplayName = (issue = {}, field = {}) => {
  return normalizeText(issue.fieldLabel || field.label || issue.fieldName || field.name || '当前字段')
}

const getIssueKind = (issue = {}) => normalizeText(issue.id).split(':')[0]

const getPatchText = (issue = {}) => {
  const patch = issue.fix?.patch || {}
  const parts = []

  if ('isList' in patch) {
    parts.push(patch.isList === '0' || patch.isList === false ? '从列表显示中移除' : '加入列表显示')
  }
  if ('dataIndex' in patch) {
    parts.push(`列表取值改为 ${patch.dataIndex}`)
  }
  if ('dict' in patch) {
    parts.push(`补充列表字典 ${patch.dict}`)
  }
  if ('align' in patch) {
    const alignText = patch.align === 'right' ? '右对齐' : patch.align === 'center' ? '居中' : patch.align
    parts.push(`列表对齐改为${alignText}`)
  }
  if ('queryFieldType' in patch) {
    parts.push(`查询组件改为 ${patch.queryFieldType}`)
  }
  if ('queryFieldProps' in patch) {
    const props = patch.queryFieldProps || {}
    if (props.placeholder) {
      parts.push(`补充查询提示“${props.placeholder}”`)
    } else {
      parts.push('补充查询组件属性')
    }
  }
  if ('span' in patch) {
    parts.push(`表单宽度改为 ${patch.span}`)
  }

  return parts.join('，')
}

const createGenericExplanation = ({issue, field, listColumn}) => {
  const fieldName = getFieldDisplayName(issue, field)
  const levelText = levelTextMap[issue.level] || '问题'
  const fixText = getPatchText(issue)
  const fixTypeText = fixTypeTextMap[issue.fix?.type] || '需要人工调整'

  return {
    source: 'local-rule',
    summary: `${fieldName} 命中了“${issue.title}”${levelText}，建议在保存或生成页面前先确认。`,
    risk: issue.description || '当前配置可能影响页面展示、查询体验或后续代码生成结果。',
    action: fixText || issue.suggestion || '请根据业务含义手工检查字段配置。',
    fixPlan: issue.fixable ? `${fixTypeText}：${fixText || issue.suggestion}` : '该问题需要人工判断，暂不自动修复。',
    context: [
      field?.showType ? `控件类型：${field.showType}` : '',
      listColumn ? `列表列：${listColumn.title || listColumn.name}` : '',
    ].filter(Boolean),
  }
}

const issueExplainMap = {
  'duplicate-field-name': ({issue, field}) => {
    const fieldName = getFieldDisplayName(issue, field)
    const duplicateFields = toArray(issue.meta?.fields)
        .map(item => `${item.label || item.name}(${item.name})`)
        .join('、')
    return {
      source: 'local-rule',
      summary: `${fieldName} 的字段名重复。保存后同名字段会写入同一个 key，页面取值和提交数据都可能互相覆盖。`,
      risk: duplicateFields ? `重复项包括：${duplicateFields}。` : issue.description,
      action: '请保留业务含义最准确的字段名，并把其他重复字段改成唯一名称。',
      fixPlan: '该类问题无法可靠自动修复，因为需要判断字段真实业务含义。',
      context: [],
    }
  },
  'duplicate-java-field': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 复用了底层字段，可能导致数据库字段、实体字段或生成代码互相覆盖。`,
    risk: issue.description,
    action: '请检查是否由复制字段造成；如果是不同业务字段，应分配不同的底层 javaField。',
    fixPlan: '该类问题需要人工判断数据库字段含义，暂不自动修复。',
    context: [],
  }),
  'missing-required-properties': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 缺少必要属性，生成页面或保存配置时容易失败。`,
    risk: issue.description,
    action: `请补齐 ${toArray(issue.meta?.missing).join('、') || '缺失属性'} 后再继续。`,
    fixPlan: '缺失属性需要结合业务录入，暂不自动修复。',
    context: [],
  }),
  'heavy-field-shown-in-list': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 更适合放在详情或表单中查看，不适合作为列表列长期展示。`,
    risk: '大文本、附件或富文本放进列表后，会挤占横向空间，也可能拖慢列表加载和滚动体验。',
    action: '建议列表只保留标题、日期、状态、单位等便于扫描的信息。',
    fixPlan: issue.fixable ? `可自动处理：${getPatchText(issue)}。` : '请手工关闭列表显示。',
    context: [field?.showType ? `控件类型：${field.showType}` : ''].filter(Boolean),
  }),
  'title-field-span': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 是标题/名称类字段，通常承载主要内容，半行展示容易显得局促。`,
    risk: '标题内容稍长时会换行或压缩输入区域，影响表单的公文感和阅读顺序。',
    action: '建议让它占整行，把页面视觉重心留给主标题或名称。',
    fixPlan: issue.fixable ? `可自动处理：${getPatchText(issue)}。` : '请手工调整字段宽度。',
    context: [],
  }),
  'date-query-field-type': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 作为查询条件时，用日期范围通常比单日查询更贴近实际检索。`,
    risk: '单日查询会让用户反复修改日期，不利于按周、按月或按时间段查找数据。',
    action: '建议改成日期范围查询，并保留当前日期格式。',
    fixPlan: issue.fixable ? `可自动处理：${getPatchText(issue)}。` : '请手工调整查询组件。',
    context: [],
  }),
  'name-value-list-data-index': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 在列表中应优先显示名称，而不是内部 id。`,
    risk: '用户看到 id 时很难判断真实含义，列表可读性会下降。',
    action: '建议列表列取名称字段，保留 id 作为提交或关联值即可。',
    fixPlan: issue.fixable ? `可自动处理：${getPatchText(issue)}。` : '请手工调整列表取值字段。',
    context: [],
  }),
  'list-dict-missing': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 已配置字典，但列表列没有显式带上字典配置。`,
    risk: '列表中可能直接显示原始编码，而不是用户能读懂的字典文本。',
    action: '建议列表列同步字段字典，让表单和列表显示口径一致。',
    fixPlan: issue.fixable ? `可自动处理：${getPatchText(issue)}。` : '请手工补充列表字典。',
    context: [field?.dictType ? `字段字典：${field.dictType}` : ''].filter(Boolean),
  }),
  'number-list-align': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 属于数值类信息，右对齐后更容易纵向比较。`,
    risk: '左对齐的数字在列表中位数不齐，用户比较大小时会更费眼。',
    action: '建议数值列右对齐，金额和数量字段尤其适合这样处理。',
    fixPlan: issue.fixable ? `可自动处理：${getPatchText(issue)}。` : '请手工调整列表对齐方式。',
    context: [],
  }),
  'date-list-align': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 是日期类字段，居中显示能让列表版面更整齐。`,
    risk: '日期通常长度固定，左对齐会让列表视觉重心偏散。',
    action: '建议日期列居中，和公文、台账类列表的扫描习惯更一致。',
    fixPlan: issue.fixable ? `可自动处理：${getPatchText(issue)}。` : '请手工调整列表对齐方式。',
    context: [],
  }),
  'query-placeholder-missing': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 是查询条件，但缺少输入提示。`,
    risk: '查询区字段多时，用户不容易判断该输入名称、编号还是其他内容。',
    action: '建议补充简短 placeholder，保持查询区自解释。',
    fixPlan: issue.fixable ? `可自动处理：${getPatchText(issue)}。` : '请手工补充查询提示。',
    context: [],
  }),
  'select-missing-dict': ({issue, field}) => ({
    source: 'local-rule',
    summary: `${getFieldDisplayName(issue, field)} 是选择类控件，但没有可复用的字典来源。`,
    risk: '选项直接散落在页面配置中时，后续维护、列表翻译和统计口径容易不一致。',
    action: issue.suggestion || '建议创建或选择已有字典编码。',
    fixPlan: '字典编码需要结合业务口径确认，暂不自动修复。',
    context: [field?.showType ? `控件类型：${field.showType}` : ''].filter(Boolean),
  }),
}

export const createLocalIssueExplanation = (issue = {}, dsl = {}) => {
  const fieldName = issue.fix?.fieldName || issue.fieldName
  const field = findField(dsl, fieldName)
  const listColumn = findListColumn(dsl, fieldName)
  const createExplanation = issueExplainMap[getIssueKind(issue)] || createGenericExplanation
  const explanation = createExplanation({issue, dsl, field, listColumn})

  return {
    ...explanation,
    fieldName,
    generatedAt: new Date().toISOString(),
  }
}

export const createIssueExplanation = (issue = {}, dsl = {}, options = {}) => {
  if (typeof options.provider === 'function') {
    return options.provider({
      issue,
      dsl,
      localExplanation: createLocalIssueExplanation(issue, dsl),
    })
  }
  return createLocalIssueExplanation(issue, dsl)
}

export const explainFormDesignIssues = (issues = [], dsl = {}) => {
  return toArray(issues).map(issue => {
    if (issue.aiExplanation && !isEmpty(issue.aiExplanation.summary)) {
      return issue
    }
    return {
      ...issue,
      aiExplanation: createIssueExplanation(issue, dsl),
    }
  })
}

export default explainFormDesignIssues
