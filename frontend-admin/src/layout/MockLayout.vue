<template>
  <div style="display: flex" :class="userView.roles.map(item=>'role-style-'+item)">
    <a-layout id="modal-container" class="ht-layout" :class="[menuLocation,theme]">
      <a-layout-sider :theme="theme" v-if="menuLocation==='left'" v-model:collapsed="collapsed" :trigger="null" :collapsedWidth="60" :collapsible="true" :width="siderWidth">
        <div class="menu-collapse-btn">
          <menu-unfold-outlined v-if="collapsed" @click="changeCollapsed"/>
          <menu-fold-outlined v-else @click="changeCollapsed"/>
        </div>
      </a-layout-sider>
      <a-layout>
        <a-layout-header class="ht-header">
          <title-info :class="[`menu-theme-${theme}`,`menu-location-${menuLocation}`]"/>
          <div class="user-menu"/>
          <user-info ref="userCenter"/>
        </a-layout-header>
        <a-layout>
          <a-layout-sider v-if="menuLocation==='mix'&&userMixLeftMenu.length>0" theme="light" v-model:collapsed="collapsed" :collapsedWidth="60" :collapsible="false" :width="siderWidth">
          </a-layout-sider>
          <a-layout-content :style="userMixLeftMenu.length>0?{width:collapsed?'calc(100% - 60px)':'calc(100% - '+siderWidth+'px)'}:{width:'100%'}">
            <a-tabs type="editable-card" :animated="true" :forceRender="true" hide-add>
              <a-tab-pane :key="0">
                <template #tab>
                  预览
                </template>
              </a-tab-pane>
              <template #renderTabBar="{ DefaultTabBar, ...props }">
                <component :is="DefaultTabBar" v-bind="props" :style="{ padding: '0 14px' }"/>
              </template>
            </a-tabs>
            <div class="admin-content">
              <router-view/>
            </div>
          </a-layout-content>
        </a-layout>

      </a-layout>
    </a-layout>
  </div>
</template>

<script>
export default {
  name: "MockLayout"
}
</script>
<script setup>
import {computed, ref} from "vue";
import {useStore} from "vuex";
import TitleInfo from "@/layout/admin/TitleInfo";
import UserInfo from "@/layout/admin/UserInfo";
import {getMenuListAction} from "@/api/api";

const store = useStore();
let menuLocation = computed(() => store.getters.getMenuLocation)
let userMixLeftMenu = computed(() => store.getters.getUserMixLeftMenu)
let theme = computed(() => store.getters.getTheme)
let userView = computed(() => store.getters.getUserView)

let siderWidth = ref(200);
let collapsed = ref(false);
const changeCollapsed = () => {
  collapsed.value = !collapsed.value
}

getMenuListAction().then(menuRes => {
  let userPermissionData = menuRes.data.menu.filter(item => {
    return (item.type === '3' || item.type === '4') && item.permission
  }).map(item => item.permission)
  store.commit('setUserPermissionData', userPermissionData)
})
</script>
<style lang="less" scoped>
.ht-layout {
  background: #ffffff;
  width: 100vw;
  height: 100vh;

  .ht-header {
    height: 64px !important;
    padding: 0;
    color: rgba(0, 0, 0, 0.65) !important;
    line-height: 64px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    display: flex;
    justify-content: space-between;
  }

  .menu-collapse-btn {
    height: 64px;
    text-align: left;
    font-size: 18px;
    line-height: 64px;
    padding: 0 0 0 22px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  }
}

.ht-layout.top, .ht-layout.mix {

  .ht-header {

    padding: 0 20px;

    .user-menu {
      flex: 1;
      overflow: hidden;
    }
  }

  .ant-layout-content {
    height: calc(100vh - 64px);
    width: 100%;
  }
}

.ht-layout.left {
  .user-menu {
    height: calc(100vh - 64px);
    overflow: auto;
  }
}

.ht-layout.light.top, .ht-layout.light.mix {
  .ht-header {
    background: var(--ant-primary-color);

    :deep(.user-info-item) {
      color: #fff;
    }
  }
}

.ht-layout.light.left {
  .ht-header {
    background: #ffffff;
  }
}

.ht-layout.dark {
  .ht-header {
    background: #ffffff;
  }

  .menu-collapse-btn {
    color: #ffffff;
  }
}

.ht-layout.dark.top, .ht-layout.dark.mix {
  .ht-header {
    background: #001529;

    :deep(.user-info) {
      color: #ffffff;

      .user-info-item {
      }
    }

  }
}

:deep(.ant-layout-sider-light) {
  .ant-layout-sider-trigger {
    border-right: 1px solid rgba(0, 0, 0, 0.06);
  }
}

.ant-tabs {
  background: #ffffff;
}

.admin-content {
  height: calc(100% - 45px);
  background: #ffffff;
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

}
</style>
