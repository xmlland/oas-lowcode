const STYLE_OPTIONS = [
  {value: 'normal', label: '普通表单', text: '普通表单'},
  {value: 'document-form', label: '公文文单', text: '公文文单'},
  {value: 'approval', label: '审批表单', text: '审批表单'},
]

const normalizeText = (value) => String(value || '').trim()

const includesAny = (value, keywords = []) => {
  const text = normalizeText(value)
  return keywords.some(keyword => text.indexOf(keyword) >= 0)
}

const uniq = (items = []) => {
  const seen = {}
  return items.filter(item => {
    const key = normalizeText(item)
    if (!key || seen[key]) {
      return false
    }
    seen[key] = true
    return true
  })
}

const normalizeFieldLabel = (value) => {
  return normalizeText(value)
      .replace(/^(以及|还有|和|及|与)/, '')
      .replace(/(等字段|等信息|等内容|字段|信息)$/g, '')
      .replace(/[：:。；;，,、\s]+$/g, '')
      .trim()
}

const splitFieldText = (text) => {
  const source = normalizeText(text)
      .replace(/\r?\n/g, '、')
      .replace(/以及/g, '、')
      .replace(/还有/g, '、')
      .replace(/和/g, '、')
      .replace(/及/g, '、')
      .replace(/与/g, '、')
  return uniq(source.split(/[、，,；;。]/).map(normalizeFieldLabel))
}

const extractRequirementBody = (text) => {
  const source = normalizeText(text)
  const markers = ['需要', '包含', '包括', '字段有', '字段包括', '字段包含', '录入']
  const marker = markers.find(item => source.indexOf(item) >= 0)
  if (marker) {
    return source.slice(source.indexOf(marker) + marker.length)
  }
  const commaIndex = source.search(/[，,:：]/)
  return commaIndex >= 0 ? source.slice(commaIndex + 1) : ''
}

const inferTitle = (text) => {
  const beforeMarker = normalizeText(text).split(/需要|包含|包括|字段有|字段包括|字段包含|录入/)[0]
  return beforeMarker
      .replace(/^(帮我|请|生成|创建|做一个|做|新建|设计一个|设计)/, '')
      .replace(/表单|表|页面/g, '')
      .replace(/[，,：:\s]+$/g, '')
      .trim()
}

const inferStyle = (text, explicitMode = 'auto') => {
  if (explicitMode && explicitMode !== 'auto') {
    return explicitMode
  }
  if (includesAny(text, ['收文', '发文', '文号', '密级', '缓急', '批示', '处理笺'])) {
    return 'document-form'
  }
  if (includesAny(text, ['审批', '申请', '意见', '流程'])) {
    return 'approval'
  }
  return ''
}

const createQuestion = (config) => ({
  required: false,
  defaultValue: '',
  options: [],
  ...config,
})

export const createClarificationQuestions = (text = '', options = {}) => {
  const source = normalizeText(text)
  if (!source) {
    return []
  }

  const explicitMode = options.mode || 'auto'
  const inferredStyle = inferStyle(source, explicitMode)
  const fieldBody = extractRequirementBody(source)
  const labels = splitFieldText(fieldBody)
  const title = inferTitle(source)
  const questions = []

  if (explicitMode === 'auto' && !inferredStyle) {
    questions.push(createQuestion({
      id: 'form_style',
      type: 'choice',
      title: '表单类型',
      description: '需求没有明显说明是普通表单、公文文单还是审批表单。',
      required: true,
      defaultValue: 'normal',
      options: STYLE_OPTIONS,
    }))
  }

  if (!title || title.length <= 2 || title === 'AI生成') {
    questions.push(createQuestion({
      id: 'form_title',
      type: 'text',
      title: '表单标题',
      description: '请补充一个业务人员能看懂的表单名称。',
      required: true,
      defaultValue: title || '',
      placeholder: '例如：收文处理笺、合同审批表',
    }))
  }

  if (labels.length < 4 || includesAny(source, ['相关信息', '基本信息', '常用字段', '主要字段'])) {
    questions.push(createQuestion({
      id: 'core_fields',
      type: 'textarea',
      title: '核心字段',
      description: '字段太少或描述偏泛，请补充必须出现在表单里的核心字段。',
      required: labels.length < 4,
      defaultValue: labels.join('、'),
      placeholder: '例如：标题、来文单位、文号、收文日期、缓急、附件',
    }))
  }

  const styleForCommon = inferredStyle || (explicitMode === 'auto' ? '' : explicitMode)
  if (styleForCommon === 'document-form') {
    const commonFields = ['来文单位', '文号', '标题', '收文日期', '缓急', '附件']
    const missingFields = commonFields.filter(field => !labels.some(label => label.indexOf(field) >= 0 || field.indexOf(label) >= 0))
    if (missingFields.length > 0) {
      questions.push(createQuestion({
        id: 'document_common_fields',
        type: 'choice',
        title: '补齐公文字段',
        description: `当前缺少常见公文字段：${missingFields.join('、')}。`,
        defaultValue: 'yes',
        options: [
          {value: 'yes', label: '补齐', text: `补齐公文常用字段：${missingFields.join('、')}`},
          {value: 'no', label: '不补', text: '不自动补齐公文常用字段'},
        ],
        meta: {
          missingFields,
        },
      }))
    }
  }

  if (labels.length >= 8 && !includesAny(source, ['列表', '查询', '检索', '筛选'])) {
    questions.push(createQuestion({
      id: 'list_query_preference',
      type: 'choice',
      title: '列表与查询',
      description: '字段较多，建议确认列表页优先展示和查询的范围。',
      defaultValue: 'core',
      options: [
        {value: 'core', label: '核心字段', text: '列表和查询优先使用标题、编号、文号、日期、状态、人员等核心字段'},
        {value: 'more', label: '尽量丰富', text: '列表尽量展示更多短字段，查询保留核心条件'},
        {value: 'minimal', label: '保持简洁', text: '列表和查询都保持简洁，只保留最关键字段'},
      ],
    }))
  }

  return questions.slice(0, 4)
}

export const createDefaultClarificationAnswers = (questions = []) => {
  return questions.reduce((answers, question) => {
    answers[question.id] = question.defaultValue || ''
    return answers
  }, {})
}

export const getClarificationAnswerList = (questions = [], answers = {}) => {
  return questions.map(question => {
    const value = normalizeText(answers[question.id])
    const matchedOption = question.options.find(option => option.value === value)
    return {
      id: question.id,
      title: question.title,
      type: question.type,
      value,
      label: matchedOption?.label || value,
      text: matchedOption?.text || value,
    }
  }).filter(item => item.value)
}

export const getUnansweredClarificationQuestions = (questions = [], answers = {}) => {
  return questions.filter(question => question.required && !normalizeText(answers[question.id]))
}

export const buildClarifiedRequirementText = (text = '', questions = [], answers = {}) => {
  const source = normalizeText(text)
  const answerList = getClarificationAnswerList(questions, answers)
  const titleAnswer = answerList.find(answer => answer.id === 'form_title')?.value || inferTitle(source)
  const fieldSegments = []

  answerList.forEach(answer => {
    if (answer.id === 'core_fields' && answer.value) {
      fieldSegments.push(answer.value)
    } else if (answer.id === 'document_common_fields' && answer.value === 'yes') {
      const question = questions.find(item => item.id === answer.id)
      const missingFields = question?.meta?.missingFields || []
      if (missingFields.length > 0) {
        fieldSegments.push(missingFields.join('、'))
      }
    }
  })

  if (fieldSegments.length > 0) {
    const fields = uniq(fieldSegments.join('、').split(/[、，,；;。]/).map(normalizeFieldLabel)).join('、')
    const title = titleAnswer || 'AI生成表单'
    return `${title}，需要${fields}`
  }

  return source
}

export const getClarifiedGenerationOptions = (mode = 'auto', questions = [], answers = {}) => {
  const styleAnswer = normalizeText(answers.form_style)
  if (mode && mode !== 'auto') {
    return {style: mode}
  }
  if (styleAnswer) {
    return {style: styleAnswer}
  }
  return {}
}
