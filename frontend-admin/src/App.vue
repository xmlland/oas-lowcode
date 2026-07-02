<template>
  <a-config-provider :locale="locale" :input="{autocomplete:'off'}">
    <router-view/>
  </a-config-provider>
</template>

<script>

export default {
  name: 'App'
}
</script>
<script setup>
import {useStore} from 'vuex'
// 默认语言为 en-US，如果你需要设置其他语言，推荐在入口文件全局设置 locale
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {getCookie} from "@/lib/cookie";
import config from "@/config"

dayjs.locale('zh-cn');
const store = useStore();
let locale = zhCN;

let theme = getCookie('theme') || config.theme.theme
let menuLocation = getCookie('menuLocation') || config.theme.menuLocation
let primaryColor = getCookie('primaryColor') || config.theme.primaryColor
store.commit('setTheme', theme)
store.commit('setMenuLocation', menuLocation)
store.commit('setPrimaryColor', primaryColor)

</script>

<style lang="less">
/*@import "assets/less/main";*/

#app-container {
  height: 100vh;
  padding: 0;
  margin: 0;
  min-width: 1080px;
  min-height: 760px;
  overflow: hidden;
}

</style>
