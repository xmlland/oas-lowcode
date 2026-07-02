<template>
  <div class="theme-setting">
    <a-divider orientation="left">导航模式</a-divider>
    <div class="select-container">
      <div class="select-container-item" @click="changeLocation('left')">
        <a-tooltip>
          <template #title>侧边菜单布局</template>
          <img src="~@/assets/svg/menu-left.svg">
          <check-outlined v-if="menuLocation==='left'"/>
        </a-tooltip>
      </div>
      <div class="select-container-item" @click="changeLocation('top')">
        <a-tooltip>
          <template #title>顶部菜单布局</template>
          <img src="~@/assets/svg/menu-top.svg">
          <check-outlined v-if="menuLocation==='top'"/>
        </a-tooltip>
      </div>
      <div class="select-container-item" @click="changeLocation('mix')">
        <a-tooltip>
          <template #title>混合菜单布局</template>
          <img src="~@/assets/svg/menu-mix.svg">
          <check-outlined v-if="menuLocation==='mix'"/>
        </a-tooltip>
      </div>
    </div>
    <a-divider orientation="left">整体风格</a-divider>
    <div class="select-container">
      <div class="select-container-item" @click="changeTheme('dark')">
        <a-tooltip>
          <template #title>暗色菜单主题</template>
          <img src="~@/assets/svg/theme-dark.svg">
          <check-outlined v-if="theme==='dark'"/>
        </a-tooltip>
      </div>
      <div class="select-container-item" @click="changeTheme('light')">
        <a-tooltip>
          <template #title>亮色菜单主题</template>
          <img src="~@/assets/svg/theme-light.svg">
          <check-outlined v-if="theme==='light'"/>
        </a-tooltip>
      </div>
    </div>

    <a-divider orientation="left">主题色</a-divider>

    <div class="select-container-color">
      <div class="select-container-item-color" @click="changePrimaryColor(item.color)" v-bind:key="item.color"
           v-for="item in colorList">

        <a-tooltip placement="right">
          <template #title>{{ item.key }}</template>
          <a-tag :color="item.color">
            <check-outlined v-if="item.color===primaryColor"/>
            <check-outlined v-else :style="{color:item.color}"/>
          </a-tag>
        </a-tooltip>

      </div>

    </div>
  </div>
</template>

<script setup>
import {computed, ref} from "vue";
import {useStore} from "vuex";
import config from "@/config"
const store = useStore();

const colorList = ref(config.theme.colorList)
let menuLocation = computed(() => store.getters.getMenuLocation)
let theme = computed(() => store.getters.getTheme)
let primaryColor = computed(() => store.getters.getPrimaryColor)

const changeTheme = (value) => {
  store.commit('setTheme', value)
}
const changeLocation = (value) => {
  store.commit('setMenuLocation', value)
}
const changePrimaryColor = (value) => {
  store.commit('setPrimaryColor', value)
}
</script>
<style lang="less" scoped>
.theme-setting {
  padding: 8px;

  .select-container {
    display: flex;

    .select-container-item {
      margin-right: 10px;
      cursor: pointer;

    }

    :deep(.select-container-item) {

      > span {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
      }

    }

  }

  .select-container-color{
    display: flex;
    flex-direction: column;

    .ant-tag{
      cursor: pointer;
      margin-top: 5px;
      border-radius: 5px;
    }
  }

}
</style>
