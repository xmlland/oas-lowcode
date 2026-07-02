const EXACT_FIELD_NAME_MAP = {
  标题: 'title',
  名称: 'name',
  主题: 'subject',
  说明: 'description',
  备注: 'remarks',
  状态: 'status',
  类型: 'type',
  类别: 'category',
  编号: 'code',
  附件: 'attachment',
  图片: 'image',
  年: 'year',
  年度: 'year',
  日期: 'date',
  时间: 'time',
  金额: 'amount',
  费用: 'cost',
  数量: 'quantity',
  单价: 'unit_price',
  合计: 'total_amount',
  申请人: 'applicant_user',
  申请部门: 'apply_office',
  申请日期: 'apply_date',
  经办人: 'handler_user',
  负责人: 'manager_user',
  部门意见: 'department_opinion',
  领导意见: 'leader_opinion',
  审批意见: 'approval_opinion',
  处理意见: 'handle_opinion',
  合同名称: 'contract_name',
  合同编号: 'contract_no',
  合同金额: 'contract_amount',
  签订日期: 'sign_date',
  采购金额: 'purchase_amount',
  采购数量: 'purchase_quantity',
  甲方: 'party_a',
  乙方: 'party_b',
  来文单位: 'sending_unit',
  来文文号: 'incoming_doc_no',
  内部编号: 'internal_no',
  文号: 'document_no',
  收文日期: 'receive_date',
  发文日期: 'send_date',
  缓急: 'urgency',
  密级: 'secret_level',
  密级期限: 'secret_period',
  局领导批示: 'leader_instruction',
  办公室批分: 'office_instruction',
  处室办理结果: 'department_result',
  主送单位: 'main_send_unit',
  抄送单位: 'copy_send_unit',
  拟稿人: 'draft_user',
  核稿人: 'review_user',
  设备名称: 'device_name',
  设备编号: 'device_code',
  采购日期: 'purchase_date',
  责任部门: 'responsible_office',
}

const FIELD_NAME_TOKENS = [
  ['是否', 'is'],
  ['启用', 'enabled'],
  ['合同', 'contract'],
  ['项目', 'project'],
  ['采购', 'purchase'],
  ['申请', 'apply'],
  ['设备', 'device'],
  ['资产', 'asset'],
  ['档案', 'archive'],
  ['客户', 'customer'],
  ['供应商', 'supplier'],
  ['产品', 'product'],
  ['物品', 'item'],
  ['物料', 'material'],
  ['订单', 'order'],
  ['单据', 'bill'],
  ['发票', 'invoice'],
  ['付款', 'payment'],
  ['收款', 'receipt'],
  ['入库', 'inbound'],
  ['出库', 'outbound'],
  ['库存', 'stock'],
  ['领用', 'receive'],
  ['借用', 'borrow'],
  ['归还', 'return'],
  ['维修', 'repair'],
  ['保养', 'maintain'],
  ['验收', 'acceptance'],
  ['报废', 'scrap'],
  ['联系人', 'contact'],
  ['联系电话', 'contact_phone'],
  ['手机号码', 'mobile'],
  ['电话号码', 'phone'],
  ['电子邮箱', 'email'],
  ['责任', 'responsible'],
  ['所属', 'belong'],
  ['归属', 'owner'],
  ['名称', 'name'],
  ['标题', 'title'],
  ['主题', 'subject'],
  ['编号', 'code'],
  ['代码', 'code'],
  ['日期', 'date'],
  ['时间', 'time'],
  ['年度', 'year'],
  ['金额', 'amount'],
  ['费用', 'cost'],
  ['价格', 'price'],
  ['单价', 'unit_price'],
  ['总价', 'total_price'],
  ['合计', 'total_amount'],
  ['数量', 'quantity'],
  ['单位', 'unit'],
  ['部门', 'office'],
  ['机构', 'office'],
  ['人员', 'user'],
  ['用户', 'user'],
  ['姓名', 'name'],
  ['电话', 'phone'],
  ['手机', 'mobile'],
  ['邮箱', 'email'],
  ['地址', 'address'],
  ['说明', 'description'],
  ['描述', 'description'],
  ['备注', 'remarks'],
  ['内容', 'content'],
  ['状态', 'status'],
  ['类型', 'type'],
  ['类别', 'category'],
  ['级别', 'level'],
  ['附件', 'attachment'],
  ['图片', 'image'],
  ['文件', 'file'],
]

const normalizeText = (value) => String(value || '').trim()

const toSnakeCase = (value, fallback = '') => {
  const text = normalizeText(value)
      .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
      .replace(/[^a-zA-Z0-9]+/g, '_')
      .replace(/_+/g, '_')
      .replace(/^_+|_+$/g, '')
      .toLowerCase()
  if (!text) {
    return fallback
  }
  return /^[a-z]/.test(text) ? text : `${fallback || 'field'}_${text}`.replace(/_+/g, '_')
}

const cleanupLabel = (label) => normalizeText(label)
    .replace(/[＊*]/g, '')
    .replace(/\([^)]*\)/g, '')
    .replace(/（[^）]*）/g, '')
    .replace(/^(请选择|请输入)/, '')
    .replace(/(字段|信息|内容)$/g, '')
    .replace(/[：:。；;，,、\s]+$/g, '')
    .trim()

const appendPart = (parts, part) => {
  String(part || '').split('_').forEach(item => {
    if (item && parts[parts.length - 1] !== item) {
      parts.push(item)
    }
  })
}

const tokenizeChineseLabel = (label) => {
  const source = cleanupLabel(label)
  const parts = []
  let index = 0
  while (index < source.length) {
    const matched = FIELD_NAME_TOKENS.find(([keyword]) => source.startsWith(keyword, index))
    if (matched) {
      appendPart(parts, matched[1])
      index += matched[0].length
    } else {
      index += 1
    }
  }
  return parts
}

export const inferFieldNameHint = (label, fallback = '') => {
  const normalized = cleanupLabel(label)
  if (!normalized) {
    return fallback
  }
  if (EXACT_FIELD_NAME_MAP[normalized]) {
    return EXACT_FIELD_NAME_MAP[normalized]
  }
  const asciiName = toSnakeCase(normalized)
  if (/^[a-z][a-z0-9_]*$/.test(asciiName) && /[a-z]/.test(normalized)) {
    return asciiName
  }
  const tokenName = tokenizeChineseLabel(normalized).join('_')
  if (tokenName) {
    return toSnakeCase(tokenName, fallback)
  }
  return fallback
}

export const ensureUniqueFieldName = (name, usedNames = {}, fallback = 'field') => {
  const normalized = toSnakeCase(name, fallback)
  if (!usedNames[normalized]) {
    usedNames[normalized] = 1
    return normalized
  }
  usedNames[normalized] += 1
  return `${normalized}_${usedNames[normalized]}`
}

export default inferFieldNameHint
