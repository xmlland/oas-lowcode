<template>
  <div class="modern-menu-tree" :class="{ collapsed }">
    <template v-for="item in data" :key="item.key">
      <div v-if="isGroup(item)" class="modern-menu-group">
        <div v-if="!collapsed" class="modern-menu-group-title">{{ item.title }}</div>
        <modern-menu-tree
          :data="getChildren(item)"
          :collapsed="collapsed"
          :active-key="activeKey"
          :open-keys="openKeys"
          :menu-tree="menuTree"
          :user-todo-map="userTodoMap"
          @toggle="key => emit('toggle', key)"
          @select="payload => emit('select', payload)"
        />
      </div>

      <div v-else>
        <button
          class="modern-menu-item"
          :class="{ active: isActive(item), parent: hasChildren(item) }"
          :title="collapsed ? item.title : ''"
          @click="handleClick(item)"
        >
          <u-icon :type="item.attributes?.pageIcon" ext-class="modern-menu-icon" />
          <span v-if="!collapsed" class="modern-menu-text">{{ item.title }}</span>
          <span v-if="!collapsed && getCount(item) > 0" class="modern-menu-count">{{ getCount(item) }}</span>
          <down-outlined
            v-if="!collapsed && hasChildren(item)"
            class="modern-menu-arrow"
            :class="{ open: isOpen(item) }"
          />
        </button>

        <div v-if="!collapsed && hasChildren(item) && isOpen(item)" class="modern-submenu">
          <modern-menu-tree
            :data="getChildren(item)"
            :collapsed="collapsed"
            :active-key="activeKey"
            :open-keys="openKeys"
            :menu-tree="menuTree"
            :user-todo-map="userTodoMap"
            @toggle="key => emit('toggle', key)"
            @select="payload => emit('select', payload)"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script>
export default {
  name: 'ModernMenuTree'
}
</script>

<script setup>
import { DownOutlined } from '@ant-design/icons-vue'
import {
  getItemKey,
  getMenuChildren,
  getMenuDisplayTodoCount,
  isGroupMenu
} from './menuAdapter'

const props = defineProps({
  data: {
    type: Array,
    default() {
      return []
    }
  },
  collapsed: {
    type: Boolean,
    default: false
  },
  activeKey: {
    type: String,
    default: ''
  },
  openKeys: {
    type: Array,
    default() {
      return []
    }
  },
  menuTree: {
    type: Array,
    default() {
      return []
    }
  },
  userTodoMap: {
    type: Object,
    default() {
      return {}
    }
  }
})

const emit = defineEmits(['toggle', 'select'])

const getChildren = item => getMenuChildren(item)
const hasChildren = item => getChildren(item).length > 0
const getKey = item => getItemKey(item)
const isGroup = item => isGroupMenu(item)
const isOpen = item => props.openKeys.includes(getKey(item))
const isActive = item => props.activeKey === getKey(item) || getChildren(item).some(child => isActive(child))
const getCount = item => getMenuDisplayTodoCount(item, props.menuTree, props.userTodoMap)

const handleClick = item => {
  if (hasChildren(item)) {
    emit('toggle', getKey(item))
    return
  }
  emit('select', {
    item,
    key: getKey(item),
    _selectedKeys: [getKey(item)],
    attributes: item.attributes || {}
  })
}
</script>
