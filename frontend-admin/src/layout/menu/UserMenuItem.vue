<template>

  <template v-for="item in data">

    <a-menu-item-group v-if="isGroupMenu(item)" :key="'group-' + item.key">
      <template #title>
        <span class="menu-group-title">{{ item.title }}</span>
      </template>
      <user-menu-item :data="item.children"/>
    </a-menu-item-group>

    <a-sub-menu :key="item.key" v-else-if="item.children&&item.children.length>0">
      <template #icon>
        <u-icon :type="item.attributes.pageIcon " ext-class="menu-icon"/>
      </template>
      <template #title>
        <span class="menu-item-badge">{{ item.title }}<a-badge v-if="getChildrenCount(item)>0" :count="getChildrenCount(item)"/></span>
      </template>
      <user-menu-item :data="item.children"/>
    </a-sub-menu>
<!--    <a-menu-item v-else-if="item.children&&item.children.length===1" :key="getItemKey(item.children[0])">
      <template #icon>
        <u-icon :type="item.children[0].pageIcon+ ' menu-icon'"/>
      </template>
      <span class="menu-item-badge">{{ item.title + ' - ' + item.children[0].title }}<a-badge :count="userTodoMap[item.children[0].attributes.permission]"/></span>
    </a-menu-item>-->
    <a-menu-item v-else :key="getItemKey(item)">
      <template #icon>
        <u-icon :type="item.attributes.pageIcon" ext-class="menu-icon"/>
      </template>
      <span class="menu-item-badge">{{ item.title }}<a-badge v-if="getCount(item)>0" :count="getCount(item)"/></span>
    </a-menu-item>
  </template>
</template>

<script>

export default {
  name: "UserMenuItem"
}
</script>
<script setup>
import {computed} from "vue";
import {useStore} from "vuex";

const store = useStore();
let userTodoMap = computed(() => store.getters.getUserTodoMap)
defineProps({
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
})

const getItemKey = (item) => {
  if (!item.attributes.pageUrl || item.attributes.pageUrl === '' || item.attributes.pageUrl.indexOf('http') >= 0 || item.attributes.target.indexOf('iframe')>=0) {
    return item.key
  }
  return item.attributes.pageUrl + (item.attributes.permission || '')
}
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
  return countChildren(item.children)
}
const isGroupMenu = (item) => {
  if (!item.children || item.children.length === 0) return false
  const remarks = (item.attributes?.remarks || '').toLowerCase()
  return remarks.includes('分组') || remarks.includes('group')
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
  if (menu.attributes.permission && menu?.children?.length === 0) {
    menu.attributes.permission.split(',').forEach(permission => {
      count += userTodoMap.value[permission] || 0
    })
  }
  return count
}
</script>

<style lang="less">

.menu-group-title {
  font-size: inherit;
  letter-spacing: 0.5px;
}

.ant-menu-item-group {
  border-top: 1px solid #d9d9d9;
  margin-top: 8px;
  padding-top: 4px;

  &:first-child {
    border-top: none;
    margin-top: 0;
    padding-top: 0;
  }
}

.ant-menu-item-group-title {
  font-size: 14px !important;
  color: rgba(0, 0, 0, 0.65) !important;
  padding-left: 10px !important;
  padding-top: 8px !important;
  padding-bottom: 4px !important;
  cursor: default;
}

.ant-menu-dark .ant-menu-item-group {
  border-top-color: rgba(255, 255, 255, 0.12) !important;
}

.ant-menu-dark .ant-menu-item-group-title {
  color: rgba(255, 255, 255, 0.5) !important;
}

.user-menu {
  .ant-menu-item .ant-menu-item-icon + span,
  .ant-menu-submenu-title .ant-menu-item-icon + span,
  .ant-menu-item .fa + span,
  .ant-menu-submenu-title .fa + span,
  .ant-menu-item .ant-menu-title-content,
  .ant-menu-title-content {

  }

  .menu-icon {
    font-size: 26px;
  }

  .ant-menu.ant-menu-inline-collapsed > .ant-menu-item .menu-icon + span,
  .ant-menu.ant-menu-inline-collapsed > .ant-menu-item-group > .ant-menu-item-group-list > .ant-menu-item .menu-icon + span,
  .ant-menu.ant-menu-inline-collapsed > .ant-menu-item-group > .ant-menu-item-group-list > .ant-menu-submenu > .ant-menu-submenu-title .menu-icon + span,
  .ant-menu.ant-menu-inline-collapsed > .ant-menu-submenu > .ant-menu-submenu-title .menu-icon + span {
    opacity: 0;
    display: inline-block;
  }
}

.user-menu.left, .mix-part-left {
  .menu-item-badge {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .ant-badge {
      margin-left: 0;
    }
  }
}

.menu-item-badge {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;

  .ant-badge {
    margin-left: 5px;
  }
}
</style>
<style>
.menu-icon {
  /*margin-right: 10px;*/
}
</style>
