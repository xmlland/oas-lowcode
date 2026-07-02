<template>
  <a-spin class="auth-permission-form" :spinning="spinning">
    <!-- 顶部操作栏 -->
    <div class="auth-form-toolbar">
      <div class="auth-form-main-actions">
        <a-button class="auth-permission-top-btn" type="primary" @click="selectAll">全选</a-button>
        <a-button class="auth-permission-top-btn" type="primary" @click="unSelectAll">全不选</a-button>
      </div>
      <a-input-search
        class="auth-form-search"
        v-model:value="searchText"
        placeholder="搜索菜单"
        style="width: 200px"
        @search="onSearch"
        allowClear
      />
    </div>

    <!-- 双栏布局 -->
    <a-row :gutter="16" style="height: calc(70vh - 60px);">
      <!-- 左侧：菜单分类 -->
      <a-col :span="8" style="height: 100%;">
        <a-card class="auth-permission-card" title="菜单分类" size="small" style="height: 100%; overflow: auto;">
          <div class="category-list">
            <div
              v-for="category in categoryTreeData"
              :key="category.key"
              :class="['category-item', { 'selected': selectedCategoryKeys.includes(category.key) }]"
              @click="onCategorySelect([category.key])"
            >
              <!-- 叶子节点显示复选框 -->
              <a-checkbox
                v-if="category.isLeaf"
                :checked="leftCheckedKeys.includes(category.key)"
                @click.stop
                @change="(e) => onCategoryItemCheck(e, category)"
                style="margin-right: 8px;"
              />
              <!-- 非叶子节点显示占位 -->
              <span v-else style="display: inline-block; width: 16px; margin-right: 8px;"></span>
              <span :style="{ color: getCategoryColor(category.key) }">{{ category.title }}</span>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：权限详情 -->
      <a-col :span="16" style="height: 100%;">
        <a-card class="auth-permission-card" :title="currentCategoryTitle" size="small" style="height: 100%; overflow: auto;">
          <!-- 权限操作按钮 -->
          <div class="auth-category-actions">
            <a-space>
              <a-button size="small" @click="selectCurrentCategory">全选当前分类</a-button>
              <a-button size="small" @click="unselectCurrentCategory">取消当前分类</a-button>
              <a-button size="small" @click="expandCurrentCategory">展开全部</a-button>
              <a-button size="small" @click="collapseCurrentCategory">折叠全部</a-button>
            </a-space>
          </div>

          <!-- 权限树 -->
          <a-tree
            v-if="currentMenus.length > 0"
            :tree-data="currentMenus"
            :checked-keys="currentCheckedKeys"
            @check="onRightTreeCheck"
            :expanded-keys="rightExpandedKeys"
            @expand="onRightExpand"
            :checkable="true"
            :check-strictly="false"
            :selectable="false"
            :default-expand-all="true"
          >
            <template #title="{ title, remarks }">
              <span>{{ title }}</span>
              <a-tag v-if="remarks" color="blue" size="small" style="margin-left: 8px;">{{ remarks }}</a-tag>
            </template>
          </a-tree>

          <!-- 空状态 -->
          <a-empty v-else description="请选择左侧菜单分类" />
        </a-card>
      </a-col>
    </a-row>
  </a-spin>
</template>

<script>
export default {
  name: "authForm"
}
</script>

<script setup>
import { ref, computed, watch, nextTick } from "vue";
import { getMenuTreeAction } from "@/api/api";
import { listToTree, oneOf } from "@/lib/tools";
import { getAction } from "@/api/action";

let props = defineProps({
  roleId: {
    type: String,
    default: ''
  },
  getAuthUrl: {
    type: String,
    default: 'system/role/getAuth'
  }
})

let spinning = ref(true)
let treeData = ref([])
let flatMenuList = ref([])
let checkedKeys = ref([])
let searchText = ref('')
let selectedCategoryKeys = ref([])
let expandedKeys = ref([])
let rightExpandedKeys = ref([])
let leftCheckedKeys = ref([])

// 获取二级菜单作为分类（一级菜单是虚拟根，取其子项）
const categoryTreeData = computed(() => {
  const categories = []
  treeData.value.forEach(firstLevel => {
    if (firstLevel.children && firstLevel.children.length > 0) {
      firstLevel.children.forEach(secondLevel => {
        // 判断是否为叶子节点（没有子节点或子节点为空）
        const isLeaf = !secondLevel.children || secondLevel.children.length === 0
        categories.push({
          title: secondLevel.title,
          key: secondLevel.key,
          icon: secondLevel.icon,
          parentTitle: firstLevel.title,
          isLeaf: isLeaf
        })
      })
    }
  })
  return categories
})

// 当前选中的分类标题
const currentCategoryTitle = computed(() => {
  if (selectedCategoryKeys.value.length === 0) {
    return '请选择菜单分类'
  }
  const category = categoryTreeData.value.find(item => item.key === selectedCategoryKeys.value[0])
  return category ? `权限详情 - ${category.title}` : '权限详情'
})

// 当前分类下已勾选的key（用于右侧树显示）
const currentCheckedKeys = computed(() => {
  // 获取当前分类下所有节点的key
  const currentKeys = new Set()
  const collectKeys = (nodes) => {
    nodes.forEach(node => {
      currentKeys.add(node.key)
      if (node.children) {
        collectKeys(node.children)
      }
    })
  }
  collectKeys(currentMenus.value)

  // 从全局checkedKeys中筛选出属于当前分类的key
  return checkedKeys.value.filter(key => currentKeys.has(key))
})

// 当前分类下的菜单列表（三级及以下菜单）
const currentMenus = computed(() => {
  if (selectedCategoryKeys.value.length === 0) {
    return []
  }

  // 在树中查找选中的二级菜单
  let selectedSecondLevel = null
  for (const firstLevel of treeData.value) {
    if (firstLevel.children) {
      const found = firstLevel.children.find(item => item.key === selectedCategoryKeys.value[0])
      if (found) {
        selectedSecondLevel = found
        break
      }
    }
  }

  if (!selectedSecondLevel || !selectedSecondLevel.children) {
    return []
  }

  // 过滤搜索
  let menus = selectedSecondLevel.children
  if (searchText.value) {
    const searchLower = searchText.value.toLowerCase()
    menus = menus.filter(menu => {
      const matchTitle = menu.title && menu.title.toLowerCase().includes(searchLower)
      const matchChild = menu.children && menu.children.some(child =>
        child.title && child.title.toLowerCase().includes(searchLower)
      )
      return matchTitle || matchChild
    })
  }

  return menus
})

// 获取分类颜色（根据是否有选中项）
const getCategoryColor = (key) => {
  // 在树中查找该二级菜单
  let secondLevelMenu = null
  for (const firstLevel of treeData.value) {
    if (firstLevel.children) {
      const found = firstLevel.children.find(item => item.key === key)
      if (found) {
        secondLevelMenu = found
        break
      }
    }
  }

  if (!secondLevelMenu || !secondLevelMenu.children) return '#000'

  const hasChecked = secondLevelMenu.children.some(menu => {
    if (checkedKeys.value.includes(menu.key)) return true
    if (menu.children) {
      return menu.children.some(child => checkedKeys.value.includes(child.key))
    }
    return false
  })

  return hasChecked ? '#1890ff' : '#000'
}

// 分类选择
const onCategorySelect = (selectedKeys) => {
  selectedCategoryKeys.value = selectedKeys
}

// 左侧分类项勾选事件（用于叶子节点）
const onCategoryItemCheck = (e, category) => {
  const checked = e.target.checked
  const categoryKey = category.key

  // 查找该分类（二级菜单）
  let secondLevelMenu = null
  for (const firstLevel of treeData.value) {
    if (firstLevel.children) {
      const found = firstLevel.children.find(item => item.key === categoryKey)
      if (found) {
        secondLevelMenu = found
        break
      }
    }
  }

  if (!secondLevelMenu) return

  // 获取该分类下所有权限key
  const allKeys = []

  if (secondLevelMenu.children && secondLevelMenu.children.length > 0) {
    // 有子节点，收集所有子节点key
    const collectKeys = (nodes) => {
      nodes.forEach(node => {
        allKeys.push(node.key)
        if (node.children) {
          collectKeys(node.children)
        }
      })
    }
    collectKeys(secondLevelMenu.children)
  } else {
    // 叶子节点，直接添加自身key
    allKeys.push(categoryKey)
  }

  if (checked) {
    // 全选：添加所有key
    allKeys.forEach(key => {
      if (!checkedKeys.value.includes(key)) {
        checkedKeys.value.push(key)
      }
    })
    // 添加到左侧勾选状态
    if (!leftCheckedKeys.value.includes(categoryKey)) {
      leftCheckedKeys.value.push(categoryKey)
    }
  } else {
    // 取消：移除所有key
    checkedKeys.value = checkedKeys.value.filter(k => !allKeys.includes(k))
    // 从左侧勾选状态移除
    leftCheckedKeys.value = leftCheckedKeys.value.filter(k => k !== categoryKey)
  }
}

// 检查菜单是否全选
const isMenuChecked = (menuKey) => {
  const menu = findMenuByKey(menuKey)
  if (!menu) return false

  if (!menu.children || menu.children.length === 0) {
    return checkedKeys.value.includes(menuKey)
  }

  return menu.children.every(child => checkedKeys.value.includes(child.key))
}

// 检查菜单是否部分选中
const isMenuIndeterminate = (menuKey) => {
  const menu = findMenuByKey(menuKey)
  if (!menu || !menu.children || menu.children.length === 0) {
    return false
  }

  const checkedCount = menu.children.filter(child =>
    checkedKeys.value.includes(child.key)
  ).length

  return checkedCount > 0 && checkedCount < menu.children.length
}

// 获取菜单下已选中的子项
const getCheckedChildren = (menu) => {
  if (!menu.children) return []
  return menu.children
    .filter(child => checkedKeys.value.includes(child.key))
    .map(child => child.key)
}

// 菜单勾选变化
const onMenuCheckChange = (e, menu) => {
  const checked = e.target.checked

  if (!menu.children || menu.children.length === 0) {
    // 叶子节点
    if (checked) {
      if (!checkedKeys.value.includes(menu.key)) {
        checkedKeys.value.push(menu.key)
      }
    } else {
      checkedKeys.value = checkedKeys.value.filter(k => k !== menu.key)
    }
  } else {
    // 有子节点，全选或全不选所有子项
    if (checked) {
      menu.children.forEach(child => {
        if (!checkedKeys.value.includes(child.key)) {
          checkedKeys.value.push(child.key)
        }
      })
    } else {
      const childKeys = menu.children.map(c => c.key)
      checkedKeys.value = checkedKeys.value.filter(k => !childKeys.includes(k))
    }
  }

  // 自动勾选父级
  if (checked) {
    autoCheckParents(menu.key)
  }
}

// 子权限变化
const onChildrenChange = (vals, menu) => {
  const childKeys = menu.children.map(c => c.key)

  // 移除该菜单下所有子项
  checkedKeys.value = checkedKeys.value.filter(k => !childKeys.includes(k))

  // 添加选中的子项
  vals.forEach(key => {
    if (!checkedKeys.value.includes(key)) {
      checkedKeys.value.push(key)
    }
  })

  // 如果有选中子项，自动勾选父级
  if (vals.length > 0) {
    autoCheckParents(menu.key)
  }
}

// 自动勾选父级
const autoCheckParents = (key) => {
  const parent = findParentByKey(key)
  if (parent && !checkedKeys.value.includes(parent.key)) {
    checkedKeys.value.push(parent.key)
    autoCheckParents(parent.key)
  }
}

// 根据key查找菜单
const findMenuByKey = (key) => {
  return flatMenuList.value.find(item => item.key === key)
}

// 根据key查找父级
const findParentByKey = (key) => {
  for (const category of treeData.value) {
    if (category.children) {
      for (const menu of category.children) {
        if (menu.key === key) {
          return category
        }
        if (menu.children) {
          for (const child of menu.children) {
            if (child.key === key) {
              return menu
            }
          }
        }
      }
    }
  }
  return null
}

// 搜索
const onSearch = (value) => {
  searchText.value = value
}

// 右侧树展开事件
const onRightExpand = (expandedKeys) => {
  rightExpandedKeys.value = expandedKeys
}

// 右侧树勾选事件
const onRightTreeCheck = (newCheckedKeys, e) => {
  // 获取当前分类下所有节点的key
  const currentKeys = new Set()
  const collectKeys = (nodes) => {
    nodes.forEach(node => {
      currentKeys.add(node.key)
      if (node.children) {
        collectKeys(node.children)
      }
    })
  }
  collectKeys(currentMenus.value)

  // 从全局checkedKeys中移除当前分类的所有key
  const otherKeys = checkedKeys.value.filter(k => !currentKeys.has(k))

  // 添加新勾选的key（只添加属于当前分类的）
  const validNewKeys = newCheckedKeys.filter(k => currentKeys.has(k))

  // 合并
  checkedKeys.value = [...otherKeys, ...validNewKeys]

  // 同步更新左侧勾选状态
  updateLeftCheckedKeys()
}

// 展开当前分类下的所有节点
const expandCurrentCategory = () => {
  if (currentMenus.value.length === 0) return

  const allKeys = []
  const collectKeys = (nodes) => {
    nodes.forEach(node => {
      allKeys.push(node.key)
      if (node.children) {
        collectKeys(node.children)
      }
    })
  }
  collectKeys(currentMenus.value)
  rightExpandedKeys.value = allKeys
}

// 折叠当前分类下的所有节点
const collapseCurrentCategory = () => {
  rightExpandedKeys.value = []
}

// 全选当前分类
const selectCurrentCategory = () => {
  if (selectedCategoryKeys.value.length === 0) return

  // 查找当前选中的二级菜单
  let selectedSecondLevel = null
  for (const firstLevel of treeData.value) {
    if (firstLevel.children) {
      const found = firstLevel.children.find(item => item.key === selectedCategoryKeys.value[0])
      if (found) {
        selectedSecondLevel = found
        break
      }
    }
  }

  if (!selectedSecondLevel || !selectedSecondLevel.children) return

  selectedSecondLevel.children.forEach(menu => {
    if (!checkedKeys.value.includes(menu.key)) {
      checkedKeys.value.push(menu.key)
    }
    if (menu.children) {
      menu.children.forEach(child => {
        if (!checkedKeys.value.includes(child.key)) {
          checkedKeys.value.push(child.key)
        }
      })
    }
  })
}

// 取消当前分类
const unselectCurrentCategory = () => {
  if (selectedCategoryKeys.value.length === 0) return

  // 查找当前选中的二级菜单
  let selectedSecondLevel = null
  for (const firstLevel of treeData.value) {
    if (firstLevel.children) {
      const found = firstLevel.children.find(item => item.key === selectedCategoryKeys.value[0])
      if (found) {
        selectedSecondLevel = found
        break
      }
    }
  }

  if (!selectedSecondLevel || !selectedSecondLevel.children) return

  const keysToRemove = []
  selectedSecondLevel.children.forEach(menu => {
    keysToRemove.push(menu.key)
    if (menu.children) {
      menu.children.forEach(child => {
        keysToRemove.push(child.key)
      })
    }
  })

  checkedKeys.value = checkedKeys.value.filter(k => !keysToRemove.includes(k))
}

// 更新左侧分类勾选状态
const updateLeftCheckedKeys = () => {
  const newLeftChecked = []
  categoryTreeData.value.forEach(category => {
    // 查找该分类对应的二级菜单
    let secondLevelMenu = null
    for (const firstLevel of treeData.value) {
      if (firstLevel.children) {
        const found = firstLevel.children.find(item => item.key === category.key)
        if (found) {
          secondLevelMenu = found
          break
        }
      }
    }

    if (!secondLevelMenu) return

    // 获取该分类下所有权限key
    const allKeys = []
    if (secondLevelMenu.children && secondLevelMenu.children.length > 0) {
      // 有子节点，收集所有子节点key
      const collectKeys = (nodes) => {
        nodes.forEach(node => {
          allKeys.push(node.key)
          if (node.children) {
            collectKeys(node.children)
          }
        })
      }
      collectKeys(secondLevelMenu.children)
    } else {
      // 叶子节点，直接添加自身key
      allKeys.push(category.key)
    }

    // 检查该分类下所有权限是否都被选中
    const allChecked = allKeys.length > 0 && allKeys.every(key => checkedKeys.value.includes(key))
    if (allChecked) {
      newLeftChecked.push(category.key)
    }
  })
  leftCheckedKeys.value = newLeftChecked
}

// 全选
const selectAll = () => {
  const allKeys = []
  const collectKeys = (nodes) => {
    nodes.forEach(node => {
      allKeys.push(node.key)
      if (node.children) {
        collectKeys(node.children)
      }
    })
  }
  collectKeys(treeData.value)
  checkedKeys.value = allKeys
}

// 全不选
const unSelectAll = () => {
  checkedKeys.value = []
}

// 获取提交数据
const getCheckData = () => {
  return {
    id: props.roleId,
    ids: Array.from(new Set(checkedKeys.value)).join(),
  }
}

// 初始化数据
if (props.roleId) {
  let actions = [getMenuTreeAction(), getAction(props.getAuthUrl, { id: props.roleId })]
  Promise.all(actions).then(resArr => {
    // 构建树形数据
    const rawData = resArr[0].data.data.map(item => {
      item.name = item.remarks ? `${item.name}(${item.remarks})` : item.name
      return item
    })

    treeData.value = listToTree(rawData, '0', {
      title: 'name',
      key: 'id',
      parent: 'parentId'
    }, true)

    // 构建扁平列表
    flatMenuList.value = rawData.map(item => ({
      key: item.id,
      title: item.name,
      parentId: item.parentId,
      remarks: item.remarks
    }))

    // 设置已选权限
    checkedKeys.value = resArr[1].data.data || []

    // 默认选中第一个二级分类
    const categories = categoryTreeData.value
    if (categories.length > 0) {
      selectedCategoryKeys.value = [categories[0].key]
    }

    // 初始化左侧勾选状态
    updateLeftCheckedKeys()
  }).finally(() => {
    spinning.value = false
  })
}

defineExpose({
  getCheckData
})
</script>

<style scoped>
.auth-permission-form :deep(.ant-btn),
.auth-permission-form :deep(.ant-input-search-button) {
  height: 32px;
  border-radius: 6px !important;
  box-shadow: none;
}

:deep(button.auth-permission-top-btn.ant-btn),
:deep(button.auth-permission-top-btn.ant-btn-primary) {
  min-width: 96px;
  height: 36px;
  border-radius: 6px !important;
  overflow: hidden;
  box-shadow: none;
}

.auth-form-toolbar {
  margin: 0 0 10px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.auth-form-main-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.auth-category-actions {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.auth-form-search {
  flex: 0 0 auto;
}

.auth-permission-form :deep(.auth-permission-card.ant-card) {
  border-color: #e6ebf2;
  border-radius: 8px;
  box-shadow: none;
}

.auth-permission-form :deep(.auth-permission-card .ant-card-head) {
  min-height: 56px;
  padding: 0 18px;
  border-bottom-color: #eef2f7;
}

.auth-permission-form :deep(.auth-permission-card .ant-card-head-title) {
  color: #1f2937;
  font-size: 15px;
  font-weight: 500;
}

.auth-permission-form :deep(.auth-permission-card .ant-card-body) {
  padding: 12px;
}

.auth-permission-form :deep(.auth-category-actions .ant-space) {
  gap: 8px !important;
}

:deep(.ant-tree) {
  background: transparent;
}

.menu-item {
  transition: all 0.3s;
}

.menu-item:hover {
  background: #e6f7ff;
}

.category-list {
  padding: 8px 6px;
}

.category-item {
  min-height: 40px;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
}

.category-item:hover {
  background: #f8fafc;
}

.category-item.selected {
  background: #e6f4ff;
}
</style>
