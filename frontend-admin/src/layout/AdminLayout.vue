<template>
  <div style="display: flex" :class="userView.roles.map(item=>'role-style-'+item)">
    <modern-admin-layout
      v-if="isModernAdminLayout"
      ref="modernLayout"
      :collapsed="collapsed"
      :sider-width="siderWidth"
      :feedback-visible="feedbackVisible"
      @change-collapsed="changeCollapsed"
      @click-menu="clickMenu"
      @select-module="handleModernModuleSelect"
    >
      <template #user>
        <modern-user-actions ref="userCenter"/>
      </template>
      <template #content>
        <admin-content ref="adminContent" :show-tabs="false" :single-page="true"/>
      </template>
    </modern-admin-layout>
    <a-layout v-else id="modal-container" class="ht-layout" :class="[menuLocation,theme,feedbackVisible?'feedback-container-show':'']">
      <a-layout-sider :theme="theme" v-if="menuLocation==='left'" v-model:collapsed="collapsed" :trigger="null" :collapsedWidth="60" :collapsible="true" :width="siderWidth">
        <!-- <title-info :class="[`menu-theme-${theme}`,`menu-location-${menuLocation}`]" :collapsed="collapsed" style="width: 200px;"/> -->
        <div class="menu-collapse-btn">
          <menu-unfold-outlined v-if="collapsed" @click="changeCollapsed"/>
          <menu-fold-outlined v-else @click="changeCollapsed"/>
        </div>
        <user-menu ref="menuLeft" :collapsed="collapsed" @clickMenu="clickMenu"/>
      </a-layout-sider>
      <a-layout>
        <a-layout-header class="ht-header">
          <title-info v-if="menuLocation==='top'||menuLocation==='mix'||menuLocation==='left'"
                      :class="[`menu-theme-${theme}`,`menu-location-${menuLocation}`]"/>
          <user-menu ref="menuTop" v-if="menuLocation==='top'||menuLocation==='mix'" @clickMenu="clickMenu"/>
          <div v-else class="user-menu"/>
          <user-info ref="userCenter" :logoutPosition="logoutPosition"/>
        </a-layout-header>
        <a-layout>
          <a-layout-sider v-if="menuLocation==='mix'&&userMixLeftMenu.length>0" theme="light" v-model:collapsed="collapsed" :collapsedWidth="60" :collapsible="false" :width="siderWidth">
            <div class="menu-title-1">
              {{ userMixTitle }}
            </div>
            <user-menu ref="menuPartLeft" mix-part="left" :collapsed="collapsed" @clickMenu="clickMenu"/>
          </a-layout-sider>
          <a-layout-content :style="userMixLeftMenu.length>0?{width:collapsed?'calc(100% - 60px)':'calc(100% - '+siderWidth+'px)'}:{width:'100%'}">
            <admin-content ref="adminContent"/>
          </a-layout-content>
        </a-layout>

      </a-layout>
    </a-layout>
    <FeedbackButton/>
    <div class="feedback-container" :class="feedbackVisible?'active':''">
      <FeedbackContent v-if="feedbackVisible"/>
    </div>
    <GlobalModal/>
  </div>

</template>

<script>
import UserMenu from "@/layout/menu/UserMenu";
import TitleInfo from './admin/TitleInfo.vue'
import {MenuFoldOutlined, MenuUnfoldOutlined} from "@ant-design/icons-vue";

export default {
  name: "AdminLayout",
  components: {
    UserMenu,
    TitleInfo,
    MenuFoldOutlined,
    MenuUnfoldOutlined
  }
}
</script>
<script setup>
import {useStore} from "vuex";
import {computed, getCurrentInstance, nextTick, onMounted, provide, ref, watch} from "vue";
import {getCurrentUserAction, getMenuListAction, logoutAction} from "@/api/api";
import {useRoute, useRouter} from "vue-router";
import {formatAccessToken, listToTree, md5, reloadToIndex, setThemeStyle} from "@/lib/tools";
import AdminContent from "@/layout/admin/AdminContent";
import UserInfo from "@/layout/admin/UserInfo";
import IFrameViewLayout from "@/layout/IFrameViewLayout";
import globalEvent from "@/lib/globalEvent";
import {clearToken} from "@/api/action";
import {clearStorage} from "@/lib/storage";

// Vite 动态路由：预收集所有 views 和 layout 组件
const viewModules = import.meta.glob('@/views/**/*.vue')
const layoutModules = import.meta.glob('@/layout/*.vue')
// import.meta.glob 会排除调用者自身(AdminLayout.vue)以避免循环依赖
// 需要手动获取自身组件引用供 loadLayout 使用
const selfComponent = getCurrentInstance().type
const loadView = (path) => {
  const key = `/src/views${path}.vue`
  if (viewModules[key]) return viewModules[key]
  console.warn(`[loadView] 未找到视图: ${key}`)
  return viewModules[key]
}
const loadLayout = (name) => {
  if (name === 'AdminLayout') return selfComponent
  const key = `/src/layout/${name}.vue`
  if (layoutModules[key]) return layoutModules[key]
  console.warn(`[loadLayout] 未找到布局: ${key}`)
  return layoutModules[key]
}
import {connectWebSocket, onWebSocket} from "@/components/webSocket/hooks/useWebSocket";
import config from "@/config";
import GlobalModal from "@/layout/GlobalModal";
import FeedbackButton from "@/components/help/FeedbackButton";
import FeedbackContent from "@/components/help/FeedbackContent";
import {notification} from "ant-design-vue";
import ModernAdminLayout from "@/layout/modern/ModernAdminLayout.vue";
import ModernUserActions from "@/layout/modern/ModernUserActions.vue";

let instance = getCurrentInstance();
let siderWidth = ref(200);
let collapsed = ref(false);
const router = useRouter();
const route = useRoute();
const changeCollapsed = () => {
  collapsed.value = !collapsed.value
}
watch(()=>collapsed.value, (val) => {
  let funcArr = window['_sizeChange']||[]
  funcArr.forEach(func=>func())
}, {immediate: true})
const normalizeWorkbenchHomePath = path => {
  if (!path || typeof path !== 'string') {
    return ''
  }
  const normalizedPath = path.trim()
  if (!normalizedPath) {
    return ''
  }
  return normalizedPath.startsWith('/') ? normalizedPath : `/${normalizedPath}`
}
const configuredWorkbenchHomePaths = Array.isArray(config.workbenchHomePaths)
  ? config.workbenchHomePaths
  : [config.workbenchHomePath]
const workbenchHomePaths = configuredWorkbenchHomePaths
  .map(normalizeWorkbenchHomePath)
  .filter(Boolean)
const effectiveWorkbenchHomePaths = workbenchHomePaths.length ? workbenchHomePaths : ['/ht/home']
const workbenchSidebarState = ref({
  active: false,
  previousCollapsed: false
})
const isWorkbenchHomePath = path => effectiveWorkbenchHomePaths.includes(normalizeWorkbenchHomePath(path))
const enterWorkbenchSidebarState = () => {
  if (!workbenchSidebarState.value.active) {
    workbenchSidebarState.value.previousCollapsed = collapsed.value
    workbenchSidebarState.value.active = true
  }
  if (!collapsed.value) {
    collapsed.value = true
  }
}
const restoreWorkbenchSidebarState = () => {
  if (!workbenchSidebarState.value.active) {
    return
  }
  const previousCollapsed = workbenchSidebarState.value.previousCollapsed
  workbenchSidebarState.value.active = false
  if (collapsed.value !== previousCollapsed) {
    collapsed.value = previousCollapsed
  }
}
const isWorkbenchMenuPayload = ({key, attributes} = {}) => {
  const pageUrl = attributes?.pageUrl || ''
  const target = attributes?.target || ''
  return isWorkbenchHomePath(key) || isWorkbenchHomePath(pageUrl) || target.indexOf('home') >= 0
}
const handleModernModuleSelect = payload => {
  if (!isWorkbenchHomePath(route.path)) {
    return
  }
  if (isWorkbenchMenuPayload(payload)) {
    enterWorkbenchSidebarState()
  } else {
    restoreWorkbenchSidebarState()
  }
}
watch(() => route.path, (toPath, fromPath) => {
  const toWorkbenchHome = isWorkbenchHomePath(toPath)
  const fromWorkbenchHome = isWorkbenchHomePath(fromPath)
  if (toWorkbenchHome && !fromWorkbenchHome) {
    enterWorkbenchSidebarState()
    return
  }
  if (!toWorkbenchHome && fromWorkbenchHome && workbenchSidebarState.value.active) {
    restoreWorkbenchSidebarState()
  }
}, {immediate: true})
const store = useStore();
let menuLocation = computed(() => store.getters.getMenuLocation)
let userMixLeftMenu = computed(() => store.getters.getUserMixLeftMenu)
let userMixTitle = computed(() => store.getters.getUserMixTitle)
let theme = computed(() => store.getters.getTheme)
let subSystemCode = computed(() => store.getters.getSubSystemCode)
let userView = computed(() => store.getters.getUserView)
let logoutPosition = config.logoutPosition || 'menu'
let feedbackVisible = computed(() => store.getters.getFeedbackVisible)
let isModernAdminLayout = computed(() => {
  return config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern'
})

let routeData = computed(() => {
  return {
    params: route.params,
    query: route.query,
  }
})
let routeArr = ref([])
let platformRouteArr = ref([])

const createRoute = (data, arr) => {
  data.forEach(item => {
    if (!item.children || item.children.length === 0) {
      let pageUrl = item.attributes.pageUrl
      if (pageUrl && pageUrl !== '') {

        if (item.attributes.target.indexOf('iframe')>=0) {
          arr.push({
            path: '/' + item.key,
            component: IFrameViewLayout,
            meta: item.attributes
          })
        } else if ('_blank' !== item.attributes.target && 'DynamicList' !== item.attributes.target && 'newTab' !== item.attributes.target && 'newWindow' !== item.attributes.target ) {
          arr.push({
            path: pageUrl + (item.attributes.permission || ''),
            component: loadView(pageUrl),
            meta: item.attributes
          })
        }

      }
    } else {
      createRoute(item.children, arr)
    }
  })
}
const updateUserTodoCount = () => {
  store.dispatch('updateUserTodoCount')
}
let defaultHome = {key: '/ht/home', attributes: {pageName: '首页',target:''}, close: false}
/**
 * 过滤菜单树 隐藏仅添加到router中的菜单
 * @param data
 * @return {*[]}
 */
const filterTree = (data) => {
  let arr = []
  data.forEach(item => {
    let obj = {}
    Object.assign(obj, item)

    if (obj.children && obj.children.length > 0) {
      let children = filterTree(obj.children)
      if (children.length > 0) {
        obj.children = children
      }
    }
    if (obj.attributes.visible || (obj.children && item.children.length > 0)) {
      arr.push(obj)
    }
  })
  return arr
}
const getMenu = () => {
  let query = {}
  let _subSystemCode = routeData.value.query.subSystemCode
  if (_subSystemCode) {
    store.commit('setSubSystemCode', _subSystemCode)
    query.subSystemCode = _subSystemCode
  } else if (subSystemCode.value !== '') {
    _subSystemCode = subSystemCode.value
    query.subSystemCode = _subSystemCode
  }
  let firstMenu = true
  getMenuListAction(_subSystemCode).then(menuRes => {
    let next = true
    if (globalEvent.afterGetMenuList) {
      next = globalEvent.afterGetMenuList({
        menuRes,userView: userView.value
      })
    }
    if (!next) {
      return
    }
    updateUserTodoCount()
    let platformMenu = []
    let menuTree = listToTree(menuRes.data.menu.filter(item => {
      if (!item.target){
        item.target = ''
      }
      item.visible = item.target.indexOf('router') === -1
      if (item.target.indexOf('home')>=0) {
        if (firstMenu) {
          firstMenu = false
          defaultHome = {
            key: (item.target && item.target.indexOf('iframe') >= 0) ? item.pageID : item.pageUrl,
            attributes: item,
          }
        }
        return item.target.indexOf('inMenu')>=0
      }
      if (item.target && item.target.indexOf('Platform') >= 0) {
        //过滤出平台菜单
        platformMenu.push(item)
        return false
      }
      return item.type === '1' && item.isShow === '1'
    }), 1, {
      title: 'pageName',
      key: 'pageID',
      parent: 'parentID'
    }, true)

    let userPermissionData = menuRes.data.menu.filter(item => {
      return (item.type === '3' || item.type === '4') && item.permission && item.permission.isShow !== '0'
    }).map(item => item.permission)
    store.commit('setUserPermissionData', userPermissionData)

    if (platformMenu.length > 0) {
      //如果有平台菜单 则添加平台菜单路由
      createRoute(listToTree(platformMenu, 1, {
        title: 'pageName',
        key: 'pageID',
        parent: 'parentID'
      }, true), platformRouteArr.value)
      router.addRoute({
        path: '/platform',
        redirect: '/platform' + platformMenu[0].pageUrl,
        component: loadLayout(platformMenu[0].target),
        children: platformRouteArr.value.map(item => {
          console.log(item.meta)
          return {
            path: item.path.replace('/', ''),
            component: item.component,
            meta: {
              value: item.path,
              title: item.meta.pageName,
              image: item.meta.pageIcon,
            }
          }
        })
      })
      nextTick(() => {
        //跳转到平台首页
        router.push({
          path: '/platform',
          replace: true,
        })
      })
      return
    }

    routeArr.value = []
    if (defaultHome.attributes.target.indexOf('iframe') === -1) {
      routeArr.value.push({
        path: defaultHome.key,
        component: loadView(defaultHome.key),
        meta: defaultHome.attributes
      })
    }else{
      routeArr.value.push({
        path: '/' + defaultHome.key,
        component: IFrameViewLayout,
        meta: defaultHome.attributes
      })

    }
    createRoute(menuTree, routeArr.value)
    store.commit('setUserMenuData', filterTree(menuTree))
    router.addRoute({
      component: loadLayout('AdminLayout'),
      meta: {hiddenFooter: true, hiddenHeader: true},
      children: routeArr.value
    })

    if (route.path === 'ht/welcome' || route.path === '/') {
      query = route.query
    }
    nextTick(() => {
      let path = defaultHome.attributes.target.indexOf('iframe') === -1 ? defaultHome.key : ('/' + defaultHome.key)
      instance.refs.adminContent.addTab({key: path, attributes: defaultHome.attributes, close: false})
      router.push({
        path: path,
        query
      })
      // 判断是否有子菜单需要跳转
      const redirectedFromPath = router.currentRoute.value.redirectedFrom?.path
      if (redirectedFromPath) {
        const params = router.currentRoute.value.redirectedFrom?.params
        const query = router.currentRoute.value.redirectedFrom?.query
        for (const key in menuRes.data.menu) {
          const item = menuRes.data.menu[key]
          const path = item.pageUrl + (item.permission || '')
          const pageID = item.pageID
          // 比较静态页面路径地址
          if (redirectedFromPath === path) {
            const pageName = item.pageName
            instance.refs.adminContent.addTab({key: redirectedFromPath, attributes:{
              pageName: pageName
            }})
            router.push({
              path: redirectedFromPath,
              params: params,
              query: query
            })
            break
          }
          // 比较动态表单的页面pageID
          if (redirectedFromPath.indexOf(pageID) >= 0) {
            const pageName = item.pageName
            instance.refs.adminContent.addTab({key: redirectedFromPath, attributes:{
              pageName: pageName
            }})
            router.push({
              path: redirectedFromPath,
              params: params,
              query: query
            })
            break
          }
        }
      }
    })
  })
}
getCurrentUserAction().then(userRes => {
  window.$instance = instance
  document.body.setAttribute('class', userRes.data.userView.roles.map(item => 'role-style-' + item).join())
  store.commit('setUserView', userRes.data.userView)
  initWebSocket()
  store.commit('setExtEntId', userRes.data.extEntId)
  if (userRes.data.userView && userRes.data.userView.passwordExpired) {
    store.commit('setPasswordExpired', true)
  }
  store.commit('setMinPasswordLength', userRes.data.minPasswordLength)
  nextTick(() => {
    getMenu()
  })
})
const openWindowMap = {}
const clickMenu = ({item, key, _selectedKeys, attributes}) => {
  if (attributes.target === 'newTab') {
    let url = formatAccessToken(attributes.actionUrl);
    let a = document.createElement('a');
    a.style.display = 'none';
    a.setAttribute('href', url);
    a.setAttribute('target', '_blank');
    a.setAttribute('rel', 'noopener');//解决新窗口打开时携带了旧窗口的sessionStorage的问题
    document.body.appendChild(a);
    a.click();
  } else if (attributes.target === 'newWindow') {
    let newWindowKey = md5(attributes.actionUrl);
    if (openWindowMap[key]) {
      openWindowMap[key].focus();
      return
    }
    let actionUrlArr = attributes.actionUrl.split('|')
    let url = formatAccessToken(actionUrlArr[0]);
    let windowWidth = 500;
    let windowHeight = 800;
    if (actionUrlArr.length === 2) {
      //从actionUrlArr[1]中解析  传入的值为width=500,height=800
      const params = actionUrlArr[1].split(',')
      for (let i = 0; i < params.length; i++) {
        const param = params[i].split('=')
        if (param[0] === 'width') {
          windowWidth = param[1]
        } else if (param[0] === 'height') {
          windowHeight = param[1]
        }
      }
    }
    const windowFeatures = `width=${windowWidth},height=${windowHeight},top=${(window.screen.height - windowHeight) / 2},left=${(window.screen.width - windowWidth) / 2},location=no,menubar=no,toolbar=no`;
    openWindowMap[newWindowKey] = window.open(url, '_blank', windowFeatures);
  } else if (attributes.target === '_blank') {
    instance.refs.menuTop&&instance.refs.menuTop.selectMenuByKey('')
    instance.refs.menuLeft&&instance.refs.menuLeft.selectMenuByKey('')
    instance.refs.modernLayout&&instance.refs.modernLayout.selectMenuByKey('')
    window.open(formatAccessToken(attributes.actionUrl));
  } else if (attributes.target === 'modifyPwd') {
    //修改密码
    instance.refs.menuTop&&instance.refs.menuTop.selectMenuByKey('')
    instance.refs.menuLeft&&instance.refs.menuLeft.selectMenuByKey('')
    instance.refs.modernLayout&&instance.refs.modernLayout.selectMenuByKey('')
    instance.refs.userCenter.modifyPassword()
  } else if (attributes.target === 'exitSystem') {
    instance.refs.menuTop&&instance.refs.menuTop.selectMenuByKey('')
    instance.refs.menuLeft&&instance.refs.menuLeft.selectMenuByKey('')
    instance.refs.modernLayout&&instance.refs.modernLayout.selectMenuByKey('')
    logoutAction().then(res => {
      clearToken()
      clearStorage()
      store.commit('setUserView', {})
      store.commit('setExtEntId', 'null')
      reloadToIndex()

    })
  } else {
    if (attributes.target.indexOf('iframe')>=0) {
      key = '/' + key
    }
    if (!attributes.pageUrl) {
      return
    }
    instance.refs.adminContent.addTab({key: key, attributes})
    let query = {}
    if (subSystemCode.value !== '') {
      query.subSystemCode = subSystemCode.value
    }
    router.push({
      path: key,
      query
    })

  }
}
//向子组件提供方法 用于触发菜单点击事件
provide('$AdminLayout_triggerClickMenu', clickMenu)
provide('addTabToAdminContent', ({key, attributes,params,query}) => {
  if(!params){
    params={};
  }
  instance.refs.adminContent.addTab({key: key, attributes})
  router.push({
    path: key,
    params: params,
    query
  })
});

provide('updateUserTodoCount', () => {
  updateUserTodoCount()
});

function initWebSocket() {
  connectWebSocket()
  onWebSocket(onWebSocketMessage);

}

// 刷新未读消息数
const refreshUnreadCount = () => {
  store.dispatch('refreshMsgUnreadCount')
}

// 定时刷新未读消息数（60秒一次）
let msgRefreshTimer = null
const startMsgRefreshTimer = () => {
  if (msgRefreshTimer) {
    clearInterval(msgRefreshTimer)
  }
  msgRefreshTimer = setInterval(() => {
    refreshUnreadCount()
  }, 60000)
}

onMounted(() => {
  setThemeStyle()
  // 初始化未读消息数
  refreshUnreadCount()
  // 启动定时刷新
  startMsgRefreshTimer()
})

function onWebSocketMessage(data) {
  console.log("接收到消息" + JSON.stringify(data))
  if (data.cmd === 'user' || data.cmd === 'topic') {
    // 1. 弹出通知提示
    notification.info({
      message: '新消息',
      description: data.msgTxt || '您有一条新消息',
      duration: 5,
      onClick: () => {
        // 如果有 menuHref，跳转到对应页面
        if (data.menuHref) {
          router.push(data.menuHref)
        }
      }
    })
    // 2. 刷新未读数
    refreshUnreadCount()
  }
}

// 向子组件提供刷新未读消息数的方法
provide('refreshMsgUnreadCount', refreshUnreadCount)

</script>
<style lang="less" scoped>
.ht-layout.feedback-container-show{
  width: calc(100% - 425px);
}
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

.menu-title-1{
  border-bottom: 1px solid #f0f0f0;
  border-right: 1px solid #f0f0f0;
  height: 40px;
  line-height: 40px;
  font-weight: bolder;
  margin-left: 8px;
}
</style>
