<template>
  <div class="user-menu user-menu-simple " :class="[menuLocation,theme,'mix-part-'+mixPart]" v-if="menuLocation==='mix'&&mixPart===''">
    <a-menu style="width: 900px;" @select="selectMenu" v-model:openKeys="openKeys" v-model:selectedKeys="selectedKeys"
            :forceSubMenuRender="true" :theme="theme"
            :inlineIndent="10"
            :mode="menuLocation==='left'||mixPart==='left'?'inline':'horizontal'">
      <a-menu-item v-for="item in userMenuData" :key="getItemKey(item)">
        <template #icon>
          <u-icon :type="item.attributes.pageIcon" ext-class="menu-icon"/>
        </template>
        <span class="menu-item-badge">{{ item.title }}<a-badge v-if="getCount(item)>0" :count="getCount(item)"/></span>
      </a-menu-item>
      <template #overflowedIndicator>
        <unordered-list-outlined />
      </template>
    </a-menu>
  </div>
  <div v-else class="user-menu" :class="[menuLocation,theme,'mix-part-'+mixPart]">
    <a-menu @select="selectMenu" v-model:openKeys="openKeys" v-model:selectedKeys="selectedKeys"
            :forceSubMenuRender="true" :theme="theme"
            :inlineIndent="10"
            :mode="menuLocation==='left'||mixPart==='left'?'inline':'horizontal'">
      <user-menu-item :data="userMenuData" :collapsed="collapsed"/>
    </a-menu>
  </div>
</template>

<script>
import UserMenuItem from "@/layout/menu/UserMenuItem";

export default {
  name: "UserMenu",
  components: {UserMenuItem}
}
</script>
<script setup>
import {computed, ref} from "vue";
import {useStore} from "vuex";
import emitter from "@/lib/event";

let props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  },
  mixPart: {
    type: String,
    default: ''
  }

})

const store = useStore();
let menuLocation = computed(() => store.getters.getMenuLocation)
let theme = computed(() => {
  if ('left' === props.mixPart) {
    return 'light'
  } else {
    return store.getters.getTheme
  }
})
let userMenuData = computed(() => {
  let menuTree = JSON.parse(JSON.stringify(store.getters.getUserMenuData))
  if ('left' === props.mixPart) {
    menuTree = JSON.parse(JSON.stringify(store.getters.getUserMixLeftMenu))
  } else if ('mix' === menuLocation.value) {
    menuTree.forEach(item => {
      if (item.children) {
        item.leftChildren = item.children
        delete item.children
      }
    })

  }
  return menuTree
})
let selectedKeys = ref(['4'])
let openKeys = ref([])

const emits = defineEmits(['clickMenu'])

const getMenuItem = (data, key) => {
  let menuItem = null
  data.forEach(item => {
    if (getItemKey(item) === key) {
      menuItem = item
    }
    if (!menuItem && item.children && item.children.length > 0) {
      menuItem = getMenuItem(item.children, key)
    }
  })
  return menuItem
}
/**
 * 选择菜单 全局事件 提供给选项卡关闭事件使用
 */
emitter.on('Global_Emitter_SelectMenuByKey', (key) => {
  if (isInMenuData(userMenuData.value, key)) {
    //如果key在菜单数据中 则选中
    selectedKeys.value = [key]
  }else{
    //如果key不在菜单数据中 则取消选中
    selectedKeys.value = ['']
  }
})
/**
 * 判断key是否在菜单数据中
 * @param arr 菜单数据
 * @param key 菜单key
 * @return {boolean} 是否在菜单数据中
 */
const isInMenuData = (arr, key) => {
  let res = false
  arr.forEach(item => {
    if (getItemKey(item) === key) {
      res = true
    }
    if (item.children && item.children.length > 0) {
      res = res || isInMenuData(item.children, key)
    }
  })
  return res
}

const selectMenu = ({item, key, _selectedKeys}) => {
  let menuItem = getMenuItem(userMenuData.value, key)
  let attributes = menuItem.attributes
  if (menuItem.leftChildren && menuItem.leftChildren.length > 0) {
    store.commit('setUserMixLeftMenu', menuItem.leftChildren)
    store.commit('setUserMixTitle', menuItem.title)
    let funcArr = window['_sizeChange']||[]
    funcArr.forEach(func=>func())
  } else {
    emits('clickMenu', {item, key, _selectedKeys, attributes: attributes})
  }

}
const getItemKey = (item) => {
  if (!item.attributes.pageUrl || item.attributes.pageUrl === '' || item.attributes.pageUrl.indexOf('http') >= 0 || item.attributes.target.indexOf('iframe')>=0) {
    return item.key
  }
  return item.attributes.pageUrl + (item.attributes.permission || '')
}
const selectMenuByKey = (key) => {
  selectedKeys.value = [key]
}
defineExpose({
  selectMenuByKey
})

let userTodoMap = computed(() => store.getters.getUserTodoMap)

const countChildren = (children) => {
  let count = 0;
  children.forEach(child => {
    if (child.children && child.children.length > 0) {
      count += countChildren(child.children)
    } else {
      count += getCount(child)
    }
  })
  return count
}
const getChildrenCount = (item) => {
  return countChildren(item.children||[])
}
const getCount = (menu) => {
  let count = 0;

  //根节点获取待办数量
  if(menu.attributes.parentID === '1'){
    let menuTree = JSON.parse(JSON.stringify(store.getters.getUserMenuData))
    menuTree.forEach(men => {
      if(men.key===menu.key){
        count = getChildrenCount(men)
      }
    })
  }

  //叶子节点获取待办数量
  if (menu.attributes.permission && menu.children.length === 0) {
    menu.attributes.permission.split(',').forEach(permission => {
      count += userTodoMap.value[permission] || 0
    })
  }
  return count
}
</script>

<style lang="less" scoped>

.user-menu {

}

.user-menu.light {

}

.user-menu.top, .user-menu.mix {
  display: flex;

  .ant-menu {
    width: 100%;
    background: transparent;
  }
}

.user-menu.light.top, .user-menu.light.mix {
  :deep(.ant-menu-horizontal) {
    .ant-menu-overflow-item {
      // 默认状态背景色
      background-color: var(--ant-primary-color);

      // 选中状态背景色
      &.ant-menu-item-selected {
        background-color: var(--ant-primary-5);

        &::after {
          border-bottom: 2px solid #1890ff; // 默认选中下划线颜色
        }
      }

      // 悬停状态
      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }
    }
  }
}

.user-menu.dark.top, .user-menu.dark.mix {
  :deep(.ant-menu-horizontal) {
    .ant-menu-overflow-item {
      // 默认状态背景色
      //background-color: var(--ant-primary-color);

      // 选中状态背景色
      &.ant-menu-item-selected {
        background-color: rgba(255, 255, 255, 0.2);
      }

      // 悬停状态
      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }
    }
  }
}

.user-menu.mix-part-left {
  //height: 100%;

  .ant-menu {
    height: calc(100vh - 64px - 48px);
    overflow-y: auto;
    overflow-x: hidden;

    :deep(i.fa) {
      color: #000000;
    }
  }
}

.user-menu.mix-part-left {
  .ant-menu {
    /*height: 100%;*/
  }
}

.user-menu.light.top, .user-menu.light.mix {

  .ant-menu {
    background: transparent;
    border-bottom-color: transparent;
  }

  :deep(.menu-icon) {
    color: #ffffff;
  }

  :deep(.ant-menu-horizontal > .ant-menu-submenu > .ant-menu-submenu-title) {
    color: #ffffff;
  }

  :deep(.ant-menu-horizontal > .ant-menu-overflow-item > .ant-menu-title-content) {
    color: #ffffff;
  }
}

.user-menu.dark {

  :deep(.ant-menu-item-selected),:deep(.ant-menu-item-active) {
    background: var(--ant-primary-color-hover);
  }

}

.user-menu.dark.left {
  // Dark 主题左侧菜单 hover 效果
  :deep(.ant-menu-item:hover) {
    background-color: rgba(255, 255, 255, 0.08) !important;
  }
  :deep(.ant-menu-submenu-title:hover) {
    background-color: rgba(255, 255, 255, 0.08) !important;
  }
}

.user-menu.mix.dark.mix-part- {

  :deep(.ant-menu-item-selected), :deep(.ant-menu-item-active) {
    background: transparent;
  }

}

:deep(.ant-menu) {
  &.ant-menu-dark {
    color: rgba(255, 255, 255, 0.9);

    .ant-menu-item {
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

.user-menu.left.light, .user-menu.mix-part-left {
  height: calc(100vh - 64px);
  box-sizing: border-box;
  overflow-y: auto;
  border-right: 1px solid #f0f0f0;
  overflow-x: hidden;

  .ant-menu-inline {
    border-right: 0;
  }

  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  // Hover 背景框效果（比选中状态稍浅）
  :deep(.ant-menu-item:hover) {
    background-color: var(--ant-primary-1) !important;
    color: var(--ant-primary-color);
  }
  :deep(.ant-menu-submenu-title:hover) {
    background-color: var(--ant-primary-1) !important;
    color: var(--ant-primary-color);
  }

  // 去除非选中菜单项 hover 时右侧竖线，保留选中状态的蓝色竖线
  :deep(.ant-menu-item::after) {
    border-right: none !important;
  }
  :deep(.ant-menu-item-selected::after) {
    border-right: 3px solid var(--ant-primary-color) !important;
  }
}

.user-menu.mix-part-left {
  height: calc(100vh - 64px - 40px);
}

.user-menu.mix-part-left {
  box-sizing: border-box;

  .ant-menu {
    border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;
  }
}
.user-menu.left,.mix-part-left{
  :deep(.ant-menu-title-content){
    .menu-item-badge{
      white-space: pre-wrap;
      max-width: 120px;
      line-height: initial;
    }

  }
}
// 子菜单箭头方向修改：收起时向右，展开时向下
.user-menu {
  :deep(.ant-menu-submenu-arrow) {
    transform: rotate(-90deg) !important;
  }
  :deep(.ant-menu-submenu-open > .ant-menu-submenu-title > .ant-menu-submenu-arrow) {
    transform: rotate(0deg) !important;
  }
}

.user-menu-simple{
  :deep(.ant-menu-overflow-item-rest){
    display: flex;
    align-items: center;
    .ant-menu-title-content{
      display: flex;
      align-items: center;
      .anticon{
        font-size: 24px;
      }
    }
  }
}
</style>
