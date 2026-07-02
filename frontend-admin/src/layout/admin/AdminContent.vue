<template>
  <a-tabs v-if="props.showTabs" type="editable-card" v-model:activeKey="activeKey" @change="changeTab" @edit="editTab" :animated="true"
          :forceRender="true" hide-add>
    <a-tab-pane :key="tab.key" v-for="(tab) in tabData">
      <template #tab>
        <a-dropdown :trigger="['contextmenu']">
          <span>{{ tab.title }}</span>
          <template #overlay>
            <a-menu @click="({key}) => handleTabAction(key, tab)">
              <a-menu-item key="closeCurrent" :disabled="!tab.close">关闭当前</a-menu-item>
              <a-menu-item key="closeOther">关闭其他</a-menu-item>
              <a-menu-item key="closeLeft">关闭左侧</a-menu-item>
              <a-menu-item key="closeRight">关闭右侧</a-menu-item>
              <a-menu-divider />
              <a-menu-item key="closeAll">关闭全部</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </template>
      <template #closeIcon>
        <close-outlined v-if="tab.close"/>
      </template>
    </a-tab-pane>
    <template #renderTabBar="{ DefaultTabBar, ...props }">
      <component :is="DefaultTabBar" v-bind="props" :style="{ padding: '0 14px' }"/>
    </template>
  </a-tabs>
  <div class="admin-content" :class="{ 'admin-content-single': !props.showTabs }">
    <router-view v-slot="{ Component }" v-if="route.meta.target!=='iframe'">
      <keep-alive >
        <component :is="Component"/>
      </keep-alive>
    </router-view>
    <i-frame-view-layout v-show="route.meta.target==='iframe'"/>
  </div>
</template>

<script>
export default {
  name: "AdminContent"
}
</script>
<script setup>

import {ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {useStore} from "vuex";
import IFrameViewLayout from "@/layout/IFrameViewLayout";
import emitter from "@/lib/event";
let route = useRoute()
let activeKey = ref(null)
let tabData = ref([])
const props = defineProps({
  showTabs: {
    type: Boolean,
    default: true
  },
  singlePage: {
    type: Boolean,
    default: false
  }
})
const router = useRouter();
const store = useStore();
const changeTab = (activeKey) => {
  router.push({
    path: activeKey
  })
}

const editTab = (targetKey, action) => {
  console.log(targetKey, action)
  if ('remove' === action) {
    let tabIndex = -1;
    tabData.value.forEach((tab, index) => {
      if (tab.key === targetKey) {
        tabIndex = index
      }
    })
    if (activeKey.value === targetKey) {
      router.push({
        path: tabData.value[tabIndex - 1].key,
        // 回退返还时，保留原有的query参数
        query: {
          ...route.query
        }
      })
      //使用全局事件触发选中菜单
      emitter.emit('Global_Emitter_SelectMenuByKey', tabData.value[tabIndex - 1].key)
      //修改当前选中项
      activeKey.value = tabData.value[tabIndex - 1].key
    }
    tabData.value.splice(tabIndex, 1)
    store.commit('setActiveTabs',tabData.value)
  }
}
const handleTabAction = (actionKey, targetTab) => {
  switch (actionKey) {
    case 'closeCurrent':
      if (targetTab.close) {
        editTab(targetTab.key, 'remove')
      }
      break
    case 'closeOther':
      closeOtherTabs(targetTab)
      break
    case 'closeLeft':
      closeLeftTabs(targetTab)
      break
    case 'closeRight':
      closeRightTabs(targetTab)
      break
    case 'closeAll':
      closeAllTabs()
      break
  }
}

const closeOtherTabs = (targetTab) => {
  tabData.value = tabData.value.filter(tab =>
    !tab.close || tab.key === targetTab.key
  )
  if (!tabData.value.find(tab => tab.key === activeKey.value)) {
    activeKey.value = targetTab.key
    router.push({ path: targetTab.key })
    emitter.emit('Global_Emitter_SelectMenuByKey', targetTab.key)
  }
  store.commit('setActiveTabs', tabData.value)
}

const closeLeftTabs = (targetTab) => {
  const targetIndex = tabData.value.findIndex(tab => tab.key === targetTab.key)
  tabData.value = tabData.value.filter((tab, index) =>
    !tab.close || index >= targetIndex
  )
  if (!tabData.value.find(tab => tab.key === activeKey.value)) {
    activeKey.value = targetTab.key
    router.push({ path: targetTab.key })
    emitter.emit('Global_Emitter_SelectMenuByKey', targetTab.key)
  }
  store.commit('setActiveTabs', tabData.value)
}

const closeRightTabs = (targetTab) => {
  const targetIndex = tabData.value.findIndex(tab => tab.key === targetTab.key)
  tabData.value = tabData.value.filter((tab, index) =>
    !tab.close || index <= targetIndex
  )
  if (!tabData.value.find(tab => tab.key === activeKey.value)) {
    activeKey.value = targetTab.key
    router.push({ path: targetTab.key })
    emitter.emit('Global_Emitter_SelectMenuByKey', targetTab.key)
  }
  store.commit('setActiveTabs', tabData.value)
}

const closeAllTabs = () => {
  const homeTab = tabData.value.find(tab => !tab.close)
  if (homeTab) {
    tabData.value = [homeTab]
    activeKey.value = homeTab.key
    router.push({ path: homeTab.key })
    emitter.emit('Global_Emitter_SelectMenuByKey', homeTab.key)
    store.commit('setActiveTabs', tabData.value)
  }
}

const addTab = ({key,attributes,close=true}) => {
  if (props.singlePage) {
    tabData.value = [{
      key: key,
      title: attributes.pageName,
      close: close
    }]
    store.commit('setActiveTabs',tabData.value)
    activeKey.value = key
    return
  }
  let inTab = false
  tabData.value.forEach(tab => {
    if (tab.key === key) {
      inTab = true
    }
  })
  if (!inTab) {
    tabData.value.push({
      key: key,
      title: attributes.pageName,
      close: close
    })
    store.commit('setActiveTabs',tabData.value)
  }
  activeKey.value = key
}
defineExpose({
  addTab
})
</script>
<style lang="less" scoped>
.ant-tabs {
  background: #ffffff;
}

.admin-content {
  height: calc(100% - 45px);
  background: #ffffff;
}

.admin-content-single {
  height: 100%;
}
:deep(.ant-tabs-nav-list) {

  .ant-tabs-tab-with-remove:first-child {
    .ant-tabs-tab-remove {
      display: none;
    }
  }
  .ant-tabs-tab {
    border-bottom: 1px solid #888888;
  }

  /*.ant-tabs-tab-active {
    border-bottom: 1px solid var(--ant-primary-color); //如果写死的话设置皮肤就控制不了颜色了  zry 2022-12-20
  }*/
}
</style>
