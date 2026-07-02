export const cloneMenuData = (data = []) => {
  try {
    return JSON.parse(JSON.stringify(data || []))
  } catch (error) {
    console.warn('[modern-menu] clone menu failed', error)
    return []
  }
}

export const getMenuChildren = (item = {}) => {
  return item.children || item.leftChildren || []
}

export const getItemKey = (item = {}) => {
  const attributes = item.attributes || {}
  const pageUrl = attributes.pageUrl || ''
  const target = attributes.target || ''
  if (!pageUrl || pageUrl.indexOf('http') >= 0 || target.indexOf('iframe') >= 0) {
    return item.key
  }
  return pageUrl + (attributes.permission || '')
}

export const isGroupMenu = (item = {}) => {
  const children = getMenuChildren(item)
  if (!children.length) return false
  const remarks = String(item.attributes?.remarks || '').toLowerCase()
  return remarks.includes('group') || remarks.includes('分组')
}

export const countLeafTodo = (menu = {}, userTodoMap = {}) => {
  const children = getMenuChildren(menu)
  const permission = menu.attributes?.permission
  if (!permission || children.length > 0) return 0
  return String(permission).split(',').reduce((sum, item) => {
    return sum + (userTodoMap[item] || 0)
  }, 0)
}

export const countChildrenTodo = (children = [], userTodoMap = {}) => {
  return children.reduce((sum, child) => {
    const childChildren = getMenuChildren(child)
    if (childChildren.length > 0) {
      return sum + countChildrenTodo(childChildren, userTodoMap)
    }
    return sum + countLeafTodo(child, userTodoMap)
  }, 0)
}

export const getMenuTodoCount = (menu = {}, menuTree = [], userTodoMap = {}) => {
  const attributes = menu.attributes || {}
  if (attributes.parentID === '1') {
    const rootMenu = menuTree.find(item => item.key === menu.key)
    return rootMenu ? countChildrenTodo(getMenuChildren(rootMenu), userTodoMap) : 0
  }
  return countLeafTodo(menu, userTodoMap)
}

export const getMenuDisplayTodoCount = (menu = {}, menuTree = [], userTodoMap = {}) => {
  const children = getMenuChildren(menu)
  if (children.length > 0) {
    return countChildrenTodo(children, userTodoMap)
  }
  return getMenuTodoCount(menu, menuTree, userTodoMap)
}

export const findMenuItem = (data = [], key) => {
  for (const item of data) {
    if (getItemKey(item) === key || item.key === key || `/${getItemKey(item)}` === key) {
      return item
    }
    const found = findMenuItem(getMenuChildren(item), key)
    if (found) return found
  }
  return null
}

export const findMenuPath = (data = [], key, parents = []) => {
  for (const item of data) {
    const currentPath = parents.concat(item)
    if (getItemKey(item) === key || item.key === key || `/${getItemKey(item)}` === key) {
      return currentPath
    }
    const childPath = findMenuPath(getMenuChildren(item), key, currentPath)
    if (childPath.length) return childPath
  }
  return []
}

export const findFirstLeaf = (data = []) => {
  for (const item of data) {
    if (isGroupMenu(item)) {
      const groupLeaf = findFirstLeaf(getMenuChildren(item))
      if (groupLeaf) return groupLeaf
      continue
    }
    const children = getMenuChildren(item)
    if (!children.length) return item
    const childLeaf = findFirstLeaf(children)
    if (childLeaf) return childLeaf
  }
  return null
}

export const findTopModuleByKey = (modules = [], key) => {
  for (const module of modules) {
    const path = findMenuPath([module], key)
    if (path.length) return module
  }
  return null
}

export const getModuleSideMenus = (module = {}) => {
  const children = getMenuChildren(module)
  return children.length ? children : [module]
}
