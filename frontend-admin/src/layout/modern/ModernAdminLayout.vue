<template>
  <a-layout
    id="modal-container"
    class="modern-admin-layout"
    :class="{
      'feedback-container-show': feedbackVisible,
      'modern-list-skin': modernListSkinEnabled
    }"
    :style="layoutStyle"
  >
    <aside class="modern-sider" :class="{ collapsed }" :style="{ width: siderWidthValue }">
      <div class="modern-brand">
        <img v-if="logo" :src="logo" alt="logo">
        <div v-if="!collapsed" class="modern-brand-copy">
          <strong>{{ title }}</strong>
          <span>{{ currentModuleTitle }}</span>
        </div>
      </div>

      <nav class="modern-side-nav">
        <modern-menu-tree
          :data="sideMenus"
          :collapsed="collapsed"
          :active-key="activeKey"
          :open-keys="openKeys"
          :menu-tree="modules"
          :user-todo-map="userTodoMap"
          @toggle="toggleOpen"
          @select="selectMenu"
        />
      </nav>
    </aside>

    <a-layout class="modern-main">
      <a-layout-header class="modern-topbar">
        <button class="modern-icon-button" @click="emit('change-collapsed')">
          <menu-unfold-outlined v-if="collapsed" />
          <menu-fold-outlined v-else />
        </button>

        <div class="modern-module-tabs">
          <button
            v-for="module in modules"
            :key="module.key"
            :class="{ active: module.key === activeModuleKey }"
            @click="selectModule(module)"
          >
            <u-icon :type="module.attributes?.pageIcon" ext-class="modern-module-icon" />
            <strong>{{ module.title }}</strong>
            <span v-if="getCount(module) > 0" class="modern-module-count">{{ getCount(module) }}</span>
          </button>
        </div>

        <div class="modern-topbar-spacer"></div>
        <slot name="user"></slot>
      </a-layout-header>

      <a-layout-content class="modern-content">
        <slot name="content"></slot>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script>
export default {
  name: 'ModernAdminLayout'
}
</script>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue'
import emitter from '@/lib/event'
import config, { defaultLogo } from '@/config'
import ModernMenuTree from './ModernMenuTree.vue'
import {
  cloneMenuData,
  findFirstLeaf,
  findMenuPath,
  findTopModuleByKey,
  getItemKey,
  getMenuChildren,
  getMenuDisplayTodoCount,
  getModuleSideMenus
} from './menuAdapter'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  },
  siderWidth: {
    type: [Number, String],
    default: 220
  },
  feedbackVisible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['change-collapsed', 'click-menu', 'select-module'])

const store = useStore()
const route = useRoute()
const activeModuleKey = ref('')
const activeKey = ref('')
const openKeys = ref([])

const title = config.sysTitle || ''
const logo = defaultLogo
const modules = computed(() => cloneMenuData(store.getters.getUserMenuData))
const userTodoMap = computed(() => store.getters.getUserTodoMap || {})
const primaryColor = computed(() => store.getters.getPrimaryColor || config.theme?.primaryColor || '#4b57d5')
const modernListSkinEnabled = computed(() => config.theme?.modernListSkin !== false)
const siderWidthValue = computed(() => props.collapsed ? '76px' : `${Number(props.siderWidth) + 48}px`)
const activeModule = computed(() => {
  return modules.value.find(item => item.key === activeModuleKey.value) || modules.value[0] || {}
})
const currentModuleTitle = computed(() => activeModule.value?.title || '')
const sideMenus = computed(() => getModuleSideMenus(activeModule.value))
const layoutStyle = computed(() => ({
  '--modern-primary': primaryColor.value,
  '--modern-primary-soft': `${primaryColor.value}14`,
  '--modern-primary-softer': `${primaryColor.value}0d`,
  '--modern-page-bg': '#f3f6fb',
  '--modern-card-radius': '8px'
}))

const getCount = item => getMenuDisplayTodoCount(item, modules.value, userTodoMap.value)

const setActiveByKey = key => {
  if (!key) return
  const module = findTopModuleByKey(modules.value, key)
  if (module) {
    activeModuleKey.value = module.key
  }
  const path = findMenuPath(modules.value, key)
  if (path.length) {
    activeKey.value = getItemKey(path[path.length - 1])
    const parentKeys = path.slice(0, -1).map(item => getItemKey(item))
    openKeys.value = Array.from(new Set(openKeys.value.concat(parentKeys)))
  }
}

watch(modules, data => {
  if (!data.length) return
  if (!activeModuleKey.value || !data.some(item => item.key === activeModuleKey.value)) {
    activeModuleKey.value = data[0].key
  }
  setActiveByKey(route.path)
}, { immediate: true })

watch(() => route.path, path => {
  setActiveByKey(path)
}, { immediate: true })

emitter.on('Global_Emitter_SelectMenuByKey', key => {
  setActiveByKey(key)
})

const toggleOpen = key => {
  openKeys.value = openKeys.value.includes(key)
    ? openKeys.value.filter(item => item !== key)
    : openKeys.value.concat(key)
}

const selectModule = module => {
  activeModuleKey.value = module.key
  emit('select-module', {
    item: module,
    key: getItemKey(module),
    attributes: module.attributes || {}
  })
  const children = getMenuChildren(module)
  if (!children.length) {
    selectMenu({
      item: module,
      key: getItemKey(module),
      _selectedKeys: [getItemKey(module)],
      attributes: module.attributes || {}
    })
    return
  }
  const leaf = findFirstLeaf(children)
  if (leaf) {
    const leafPath = findMenuPath(children, getItemKey(leaf))
    openKeys.value = Array.from(new Set(openKeys.value.concat(leafPath.slice(0, -1).map(item => getItemKey(item)))))
  }
}

const selectMenu = payload => {
  activeKey.value = payload.key
  emit('click-menu', payload)
}

const selectMenuByKey = key => {
  if (!key) {
    activeKey.value = ''
    return
  }
  setActiveByKey(key)
}

defineExpose({
  selectMenuByKey
})
</script>

<style lang="less" scoped>
.modern-admin-layout {
  display: flex !important;
  flex-direction: row !important;
  width: 100vw;
  height: 100vh;
  background: var(--modern-page-bg);
  color: #1f2937;

  &.feedback-container-show {
    width: calc(100% - 425px);
  }
}

.modern-sider {
  flex: 0 0 auto;
  padding: 18px 14px 14px;
  background: #fbfcfe;
  border-right: 1px solid #e6ebf2;
  transition: width 0.2s ease;

  &.collapsed {
    padding-right: 10px;
    padding-left: 10px;
  }
}

.modern-brand {
  display: flex;
  align-items: center;
  height: 56px;
  gap: 12px;
  padding: 0 6px 12px;
  border-bottom: 1px solid #edf1f6;

  img {
    width: 34px;
    height: 34px;
    object-fit: contain;
  }
}

.modern-brand-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  line-height: 1.25;

  strong {
    overflow: hidden;
    color: #111827;
    font-size: 15px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    overflow: hidden;
    color: #687385;
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.modern-side-nav {
  height: calc(100vh - 96px);
  margin-top: 16px;
  overflow-x: hidden;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 999px;
  }
}

.modern-main {
  display: flex;
  height: 100vh;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  background: var(--modern-page-bg);
}

.modern-topbar {
  display: flex;
  align-items: center;
  height: 64px !important;
  gap: 14px;
  padding: 0 22px;
  line-height: 64px;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid #e6ebf2;
}

.modern-icon-button {
  display: inline-grid;
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  place-items: center;
  color: #4b5563;
  background: #f3f5f8;
  border: 1px solid #e3e8ef;
  border-radius: 6px;
  cursor: pointer;

  &:hover {
    color: var(--modern-primary);
    background: var(--modern-primary-soft);
  }
}

.modern-module-tabs {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 7px;
  padding: 0;
  overflow: visible;

  button {
    position: relative;
    display: inline-flex;
    /*min-width: 104px;*/
    height: 38px;
    align-items: center;
    justify-content: center;
    gap: 7px;
    padding: 0 13px;
    color: #475569;
    background: transparent;
    border: 1px solid transparent;
    border-radius: 7px;
    cursor: pointer;
    outline: none;
    transition: background 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, color 0.18s ease;

    &:hover {
      color: var(--modern-primary);
      background: var(--modern-primary-softer);
    }

    &.active {
      color: var(--modern-primary);
      background: transparent;
      border-color: transparent;
      box-shadow: none;

      &::after {
        position: absolute;
        right: 13px;
        bottom: -7px;
        left: 13px;
        height: 2px;
        background: var(--modern-primary);
        border-radius: 999px;
        content: "";
      }
    }
  }

  strong {
    overflow: hidden;
    font-size: 13px;
    font-weight: 700;
    line-height: 18px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

:deep(.modern-module-icon) {
  color: inherit;
  font-size: 16px;
}

.modern-module-count {
  color: #f5222d;
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
}

.modern-topbar-spacer {
  flex: 1;
}

.modern-content {
  flex: 1;
  height: calc(100vh - 64px);
  min-width: 0;
  padding: 8px;
  overflow-x: hidden;
  overflow-y: auto;
  background: var(--modern-page-bg);
}

.modern-content :deep(.ant-tabs) {
  overflow: hidden;
  border: 1px solid #e6ebf2;
  border-bottom: 0;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 8px 24px rgba(31, 41, 55, 0.04);
}

.modern-content :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.modern-content :deep(.ant-tabs-nav-list) {
  padding-left: 2px;
}

.modern-content :deep(.ant-tabs-tab) {
  min-width: 68px;
  justify-content: center;
}

.modern-content :deep(.ant-tabs-tab-active) {
  background: #ffffff;
}

.modern-content :deep(.admin-content) {
  overflow: auto;
  border: 1px solid #e6ebf2;
  border-top: 0;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 8px 24px rgba(31, 41, 55, 0.04);
}

.modern-content :deep(.admin-content-single) {
  height: auto;
  min-height: 100%;
  overflow: visible;
  border: 0;
  border-radius: 0;
  background: var(--modern-page-bg) !important;
  box-shadow: none;
}

.modern-content :deep(.modern-list-page .ant-table-body) {
  height: auto !important;
  max-height: none !important;
  overflow-y: visible !important;
}

.modern-content :deep(.modern-list-page .ant-table-cell-scrollbar) {
  display: none !important;
}

.modern-content :deep(.u-query-area),
.modern-content :deep(.ant-card),
.modern-content :deep(.layui-card),
.modern-content :deep(.u-tree),
.modern-content :deep(.ant-collapse),
.modern-content :deep(.ant-collapse-item),
.modern-content :deep(.ant-alert),
.modern-content :deep(.single-table-view),
.modern-content :deep(.tree-table-view),
.modern-content :deep(.left-tree-right-table-view) {
  border-radius: var(--modern-card-radius);
}

.modern-content :deep(.ant-table-striped > .ant-spin-nested-loading),
.modern-content :deep(.ant-table-wrapper),
.modern-content :deep(.ant-table),
.modern-content :deep(.ant-table-container) {
  overflow: hidden;
  border-radius: var(--modern-card-radius);
}

.modern-content :deep(.ant-table-header),
.modern-content :deep(.ant-table-content) {
  border-radius: var(--modern-card-radius) var(--modern-card-radius) 0 0;
}

.modern-content :deep(.ant-card-head) {
  border-radius: var(--modern-card-radius) var(--modern-card-radius) 0 0;
}

.modern-content :deep(.left-tree-right-table-view .left-tree.u-tree) {
  background: #ffffff !important;
  border-radius: var(--modern-card-radius) !important;
  box-shadow: none;
}

.modern-content :deep(.left-tree-right-table-view .left-tree .ant-tree),
.modern-content :deep(.left-tree-right-table-view .left-tree .ant-tree-list),
.modern-content :deep(.left-tree-right-table-view .left-tree .ant-tree-list-holder),
.modern-content :deep(.left-tree-right-table-view .left-tree .ant-tree-list-holder-inner) {
  background: #ffffff !important;
}

.modern-topbar :deep(.user-info) {
  height: 64px;
  min-width: 168px;
  color: #344054;
}

.modern-topbar :deep(.user-info-item) {
  height: 64px;
  padding: 0 10px !important;
}

.modern-topbar :deep(.ant-avatar) {
  background: #1f2937;
}

.modern-topbar :deep(.name-area) {
  align-items: flex-start !important;
  margin-left: 9px !important;
}

.modern-topbar :deep(.name-area span) {
  height: 20px !important;
  line-height: 20px !important;
}

.modern-topbar :deep(.company-name) {
  color: #667085;
  font-size: 12px;
}

.modern-topbar :deep(.user-name) {
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
}

:deep(.modern-menu-tree) {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

:deep(.modern-menu-group-title) {
  padding: 11px 10px 7px;
  color: #8a95a6;
  font-size: 12px;
  line-height: 18px;
}

:deep(.modern-menu-item) {
  position: relative;
  display: flex;
  width: 100%;
  height: 38px;
  align-items: center;
  gap: 10px;
  padding: 0 10px;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
  line-height: 38px;
  background: transparent;
  border: 0;
  border-radius: 6px;
  cursor: pointer;
  text-align: left;
  transition: color 0.18s ease, background 0.18s ease;

  &:hover {
    color: var(--modern-primary);
    background: var(--modern-primary-soft);
  }

  &.active {
    color: var(--modern-primary);
    background: var(--modern-primary-soft);
    font-weight: 600;

    &::before {
      position: absolute;
      left: 0;
      width: 3px;
      height: 18px;
      background: var(--modern-primary);
      border-radius: 3px;
      content: "";
    }
  }
}

:deep(.modern-menu-icon) {
  flex: 0 0 18px;
  color: inherit;
  font-size: 16px;
  line-height: 1;
  text-align: center;
}

:deep(.modern-menu-text) {
  overflow: hidden;
  min-width: 0;
  flex: 1;
  line-height: 20px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.modern-menu-count) {
  color: #f5222d;
  font-size: 13px;
  font-weight: 500;
  line-height: 1;
}

:deep(.modern-menu-arrow) {
  color: #94a3b8;
  font-size: 11px;
  transition: transform 0.2s ease;

  &.open {
    transform: rotate(180deg);
  }
}

:deep(.modern-submenu) {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 3px 0 7px 22px;
  padding-left: 10px;
  border-left: 1px solid #e2e8f0;
}

:deep(.modern-submenu .modern-menu-item) {
  height: 34px;
  gap: 8px;
  padding: 0 10px;
  color: #5b6677;
  font-size: 13px;
  font-weight: 500;
  line-height: 34px;
}

:deep(.modern-submenu .modern-menu-item:hover),
:deep(.modern-submenu .modern-menu-item.active) {
  color: var(--modern-primary);
  background: var(--modern-primary-softer);
}

:deep(.modern-submenu .modern-menu-item.active) {
  font-weight: 600;
}

:deep(.modern-submenu .modern-menu-item.active::before) {
  display: none;
}

:deep(.modern-submenu .modern-menu-icon) {
  flex-basis: 16px;
  font-size: 15px;
}

:deep(.modern-submenu .modern-submenu) {
  margin-left: 18px;
}

.modern-sider.collapsed :deep(.modern-menu-item) {
  justify-content: center;
  padding: 0;
}
</style>
