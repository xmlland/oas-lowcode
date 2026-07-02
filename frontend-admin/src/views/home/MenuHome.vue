<template>
  <div class="menu-home">
    <template v-bind:key="menuIndex" v-for="(menu,menuIndex) in menuData">
      <div class="menu-card" v-if="menu.deep>=3">
        <div class="card-title">
          <div class="menu-name" :style="{width:maxTitleLength * 20 +'px'}">
            {{ menu.title }}
          </div>
        </div>
        <div class="menu-content">
          <template v-bind:key="menu2Index" v-for="(menu2,menu2Index) in menu.children">
            <template v-if=" menu2.children &&  menu2.children.length>0">
              <div class="menu-title-level-2" v-if="menu.childrenCount>15">{{ menu2.title }}</div>
              <template v-bind:key="menu3Index" v-for="(menu3,menu3Index) in menu2.children">
                <div class="menu-item" @click="toMenu(menu3)">
                  {{ menu3.title }}
                </div>
              </template>
            </template>
            <div v-else class="menu-item" @click="toMenu(menu2)">
              <!-- 没有三级菜单-->
              {{ menu2.title }}
            </div>
          </template>
        </div>
      </div>
      <div class="menu-card" v-else>
        <div class="card-title">
          <div class="menu-name" :style="{width:maxTitleLength * 20 +'px'}">
            {{ menu.title }}
          </div>
        </div>
        <div class="menu-content">
          <template v-if="menu.deep===1">
            <!-- 一级菜单-->
            <div class="menu-item" @click="toMenu(menu)">
              {{ menu.title }}
            </div>
          </template>
          <template v-if="menu.deep===2">
            <!-- 二级菜单-->
            <template v-bind:key="menu2Index" v-for="(menu2,menu2Index) in menu.children">
              <div class="menu-item" @click="toMenu(menu2)">
                {{ menu2.title }}
              </div>
            </template>
          </template>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
export default {
  name: "MenuHome"
}
</script>
<script setup>
import {ref, computed, onMounted, inject} from "vue";
import {useStore} from "vuex";
import {replaceAll} from "@/lib/tools";

const store = useStore();

let userMenuData = computed(() => store.getters.getUserMenuData)

let menuData = ref([])

const calcDeep = (arr) => {
  let deep = 1;
  arr.forEach(item => {
    if (item.children) {
      deep = Math.max(deep, calcDeep(item.children) + 1)
    }
  })
  return deep
}
const countChildren = (arr) => {
  let count = 0;
  arr.forEach(item => {
    count += (item.children && item.children.length > 0 ? countChildren(item.children) : 1)
  })
  return count
}

let activeKey = ref({})
let maxTitleLength = ref(0)
onMounted(() => {
  let res = []
  userMenuData.value.forEach(item => {
    item.deep = calcDeep(item.children || [])
    item.childrenCount = countChildren(item.children || [])
    res.push(item)
    activeKey.value[item.key] = 0
    maxTitleLength.value = Math.max(maxTitleLength.value, replaceAll(replaceAll(item.title, '\\(', ''), '\\)', '').length)
  })
  menuData.value = res
})

let triggerClickMenu = inject('$AdminLayout_triggerClickMenu')
const toMenu = (menu) => {

  if (menu.children && menu.children.length > 0) {
    toMenu(menu.children[0])
    return
  }
  triggerClickMenu && triggerClickMenu({
    key: getItemKey(menu),
    attributes: menu.attributes
  })
}

const getItemKey = (item) => {
  if (!item.attributes.pageUrl || item.attributes.pageUrl === '' || item.attributes.pageUrl.indexOf('http') >= 0 || item.attributes.target.indexOf('iframe') >= 0) {
    return item.key
  }
  return item.attributes.pageUrl + (item.attributes.permission || '')
}
</script>
<style lang="less" scoped>
.menu-home {
  padding: 20px;
  height: 100%;
  background: url("~@/assets/img/home/homeBg.png");
  background-size: 100% 100%;
  overflow: auto;

  .menu-card {
    width: 100%;
    margin-bottom: 20px;
    padding: 15px 30px;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(255, 255, 255, 0.5) 100%);
    border-radius: 8px;

    .card-title {

      width: 100%;
      display: flex;
      justify-content: space-between;
      flex-direction: row;

      .menu-name {
        height: 40px;
        line-height: 40px;
        font-size: 16px;
        border-bottom: 3px solid var(--ant-primary-color);
      }
    }

    .menu-content {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      padding: 20px 0 0 0;

      .menu-title-level-2{
        width: 100%;
        height: 26px;
        line-height: 26px;
        font-weight: 700;
      }

      .menu-item {
        height: 36px;
        line-height: 36px;
        padding: 0 20px;
        font-size: 14px;
        color: #333;
        background: #fff;
        border-radius: 8px;
        cursor: pointer;
        margin: 0 20px 20px 0;

        &:hover {
          background-color: var(--ant-primary-color);
          color: #fff;
          box-shadow: 10px 10px 99px 6px #dedede;;
        }

      }
    }
  }
}

</style>
