<template>
  <div class="platform-container" id="modal-container">
    <div class="platform-left">
      <img class="logo" :src="logo" alt="logo"/>
      <user-info logoutPosition="menu"/>
      <a-menu mode="inline" @select="selectMenu" v-model:selectedKeys="selectedKeys">
        <a-menu-item :key="menu.value" v-for="menu in  menuData">
          <u-icon :type="menu.image"/>
          <span class="nav-text" style="padding: 0 5px">{{ menu.text }}</span>
        </a-menu-item>
        <a-menu-item :key="system.code" v-for="system in  subSystemArr">
          <u-icon/>
          <span class="nav-text" style="padding: 0 5px">{{ system.name }}</span>
        </a-menu-item>
      </a-menu>
    </div>
    <div class="platform-content">
      <div :style="{ background: '#fff',height:'100%'}">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component"/>
          </keep-alive>
        </router-view>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: "PlatformLayout"
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import {useStore} from "vuex";
import config, { defaultLogo } from "@/config";
import UIcon from "@/components/icon/UIcon";
import UserInfo from "@/layout/admin/UserInfo";
import {getCurrentUserAction, listDataAction} from "@/api/api";
import {useRouter} from "vue-router";

let instance = getCurrentInstance()
const store = useStore();
const router = useRouter();
let logo = ref(defaultLogo)
let menuData = computed(() => store.getters.getMenuData)
let subSystemArr = ref([])
let selectedKeys = ref([])
getCurrentUserAction().then(userRes => {
  window.$instance = instance
  store.commit('setUserView', userRes.data.userView)
  store.commit('setExtEntId', userRes.data.extEntId)
  listDataAction('sys_subsystem').then(res => {
    subSystemArr.value = res.rows
  })
  selectedKeys.value = [menuData.value[0].value]
})
const selectMenu = (item) => {
  selectedKeys.value = [item.key]
  menuData.value.forEach(menu => {
    if (menu.value === item.key) {
      router.push({
        path: menu.value
      })
    }
  })
  subSystemArr.value.forEach(system => {
    if (system.code === item.key) {
      window.open(system.baseurl)
    }
  })
}
</script>
<style lang="less" scoped>
.platform-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: row;
  align-items: flex-start;

  .platform-left {
    height: 100%;
    width: 206px;
    box-shadow: 1px 0 8px 3px #ccc;

    .logo {
      width: 100px;
      height: 100px;
      margin: 10px 53px;
    }

    :deep(.user-info) {
      justify-content: center;

      .user-info-item {
        background: #ebebeb;
        border-radius: 20px;
        padding: 5px 40px;

        .ant-dropdown-trigger {
          color: var(--ant-primary-color);
        }
      }
    }
  }

  .platform-content {
    width: calc(100% - 216px);
    height: 100%;
    margin-left: 10px;
  }
}
</style>
