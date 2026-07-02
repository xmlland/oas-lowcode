const unwrapRef = (value) => {
  if (value && typeof value === 'object' && 'value' in value) {
    return value.value
  }
  return value
}

const clonePlain = (value) => {
  if (value === undefined || value === null) {
    return value
  }
  try {
    return JSON.parse(JSON.stringify(value))
  } catch (e) {
    return value
  }
}

const setRefValue = (target, value) => {
  if (target && typeof target === 'object' && 'value' in target) {
    target.value = value
    return
  }
  throw new Error('目标状态不是可写 ref')
}

const patchRefObject = (target, patch = {}) => {
  const current = unwrapRef(target)
  if (!current || typeof current !== 'object') {
    return []
  }

  const changes = []
  Object.keys(patch || {}).forEach(key => {
    const before = current[key]
    const after = patch[key]
    if (String(before ?? '') !== String(after ?? '')) {
      current[key] = after
      changes.push({
        target: 'object',
        property: key,
        before,
        after,
      })
    }
  })
  return changes
}

const toArray = (value) => {
  const unwrapped = unwrapRef(value)
  return Array.isArray(unwrapped) ? unwrapped : []
}

const countListFields = (items = []) => items.filter(item => item?.column?.isList === '1').length

const countQueryFields = (items = []) => items.filter(item => item?.column?.isQuery === '1').length

const ensurePatchCanApply = (patch = {}) => {
  if (!patch.canApply) {
    throw new Error('转换预览存在错误，不能应用到设计器')
  }
  if (!Array.isArray(patch.displayFormItemArr) || !Array.isArray(patch.hideFormItemArr)) {
    throw new Error('转换预览缺少设计器字段数组')
  }
}

export const createDraftStateSnapshot = (context = {}) => ({
  createdAt: new Date().toISOString(),
  formModel: clonePlain(unwrapRef(context.formModel) || {}),
  formPropsModel: clonePlain(unwrapRef(context.formPropsModel) || {}),
  displayFormItemArr: toArray(context.displayFormItemArr).slice(),
  hideFormItemArr: toArray(context.hideFormItemArr).slice(),
})

export const rollbackDraftStateSnapshot = (context = {}, snapshot = {}) => {
  if (!snapshot || !snapshot.createdAt) {
    throw new Error('缺少可回退的设计器快照')
  }

  setRefValue(context.formModel, clonePlain(snapshot.formModel || {}))
  setRefValue(context.formPropsModel, clonePlain(snapshot.formPropsModel || {}))
  setRefValue(context.displayFormItemArr, (snapshot.displayFormItemArr || []).slice())
  setRefValue(context.hideFormItemArr, (snapshot.hideFormItemArr || []).slice())

  const restoredDisplay = toArray(context.displayFormItemArr)
  const restoredHidden = toArray(context.hideFormItemArr)
  const restoredItems = restoredDisplay.concat(restoredHidden)

  return {
    createdAt: snapshot.createdAt,
    displayCount: restoredDisplay.length,
    hiddenCount: restoredHidden.length,
    restoredFieldCount: restoredItems.length,
    listFieldCount: countListFields(restoredItems),
    queryFieldCount: countQueryFields(restoredItems),
  }
}

export const applyDraftStatePatch = (context = {}, patch = {}, options = {}) => {
  ensurePatchCanApply(patch)

  const mode = options.mode || 'replace'
  const snapshot = options.snapshot || createDraftStateSnapshot(context)
  const beforeDisplay = toArray(context.displayFormItemArr)
  const beforeHidden = toArray(context.hideFormItemArr)
  const nextDisplay = patch.displayFormItemArr || []
  const nextHidden = patch.hideFormItemArr || []

  const formChanges = patchRefObject(context.formModel, patch.formPatch)
  const formPropsChanges = patchRefObject(context.formPropsModel, patch.formPropsPatch)

  if (mode === 'append') {
    setRefValue(context.displayFormItemArr, beforeDisplay.concat(nextDisplay))
    setRefValue(context.hideFormItemArr, beforeHidden.concat(nextHidden))
  } else {
    setRefValue(context.displayFormItemArr, nextDisplay)
    setRefValue(context.hideFormItemArr, nextHidden)
  }

  const appliedDisplay = toArray(context.displayFormItemArr)
  const appliedHidden = toArray(context.hideFormItemArr)
  const appliedItems = appliedDisplay.concat(appliedHidden)

  return {
    mode,
    displayBeforeCount: beforeDisplay.length,
    hiddenBeforeCount: beforeHidden.length,
    displayAfterCount: appliedDisplay.length,
    hiddenAfterCount: appliedHidden.length,
    appliedFieldCount: appliedItems.length,
    listFieldCount: countListFields(appliedItems),
    queryFieldCount: countQueryFields(appliedItems),
    formChanges,
    formPropsChanges,
    snapshot,
  }
}

export default applyDraftStatePatch
