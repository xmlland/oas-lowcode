import {createRouter, createWebHashHistory} from 'vue-router'
import store from "@/store";
import NProgress from 'nprogress' // progress bar
import staticRouterData from "@/router/staticRouterData";
import config from "@/config";
// Progress 进度条样式
import 'nprogress/nprogress.css'
import LoginView from "@/views/user/LoginView";
import OAuth2LoginView from "@/views/user/OAuth2LoginView";
import {baseUrl, getToken} from "@/api/action";
import AdminLayout from "@/layout/AdminLayout";

import DscLayout from "@/layout/DscLayout";
import PdfPreviewLayout from "@/layout/PdfPreviewLayout";
import {whiteUrl} from "@/router/whiteUrl";
import contentData from "@/views/admin/home_components/content_data.vue";
import OADonelist from "@/views/oa/oa_done/list.vue";
import OAToDoAnddoing from "@/views/oa/oa_todoanddoing/list.vue";
import SpecialSymbols from "@/layout/admin/SpecialSymbols";
import MockLayout from "@/layout/MockLayout";
import NotFound from "@/views/error/NotFound";

export const loginPath = '/user/login'
export const oauth2LoginPath = '/user/oauth2'
export const homePath = '/ht'
const routes = [{
    path: '/user',
    redirect: loginPath,
    component: DscLayout,
    meta: {
        header: {
            title: config.sysTitle,
            subTitle: ''
        },
        hiddenFooter: true, hiddenHeader: true
    },
    children: [{
        path: 'login',
        component: LoginView,
    },{
        path: 'oauth2',
        component: OAuth2LoginView,
    }]
}, {
    path: homePath,
    redirect: '/ht/welcome',
    component: AdminLayout,
    meta: {hiddenFooter: true, hiddenHeader: true},
    children: [
        {
            path: 'home',
            component: () => import('@/views/admin/home.vue')
        },
        {
            path: 'welcome',
            component: () => import('@/views/admin/welcome.vue')
        },
        {
            path: 'dynamic/:code',
            component: () => import('@/components/dynamic/DynamicList.vue'),
            meta: {
                target: 'DynamicList'
            }
        },
    ]
},{
    path: '/pdfPreview',
    component: PdfPreviewLayout,
},{
    path: '/contentData',
    component:contentData,
    meta: {hiddenFooter: true, hiddenHeader: true},
    children: []
},{
    path: '/OADonelist',
    component:OADonelist,
    meta: {hiddenFooter: true, hiddenHeader: true},
    children: []
},{
    path: '/OAToDoAnddoing',
    component:OAToDoAnddoing,
    meta: {hiddenFooter: true, hiddenHeader: true},
    children: []
},{
    path: '/specialSymbols',
    component:SpecialSymbols,
}, {
    path: '/dynamic',
    redirect: '/dynamic/mockHome',
    component: MockLayout,
    children: [
        {
            path: 'mockHome',
            component: () => import('@/components/dynamic/DynamicMockHome.vue')
        },

    ]
},{
    path: '/404',
    component: NotFound,
}].concat(staticRouterData)

const router = createRouter({
    //内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
    history: createWebHashHistory(),
    routes, // `routes: routes` 的缩写
})
const whiteUrlList = [].concat(whiteUrl)
router.beforeEach((to, from, next) => {
    // ...
    // 返回 false 以取消导航
    //
    NProgress.start();
    document.title = to.meta.title || config.sysTitle
    if (to.path.indexOf(homePath + '/dynamic/') > -1 && !router.$$isNotFirst) {
        //动态路由第一次进入,跳转到首页,防止刷新动态路由页面时出现空白,并添加query参数
        next({
            path: homePath,
            query: to.query,
            replace: true
        })
    }

    let token = getToken();

    console.log('router to from',to,from)
    if (to.matched.length === 0) {
        next({
            path: homePath,
            query: to.query
        })
        return;
    }
    if (to) {
        if (to.meta && to.meta.hiddenHeader) {
            store.commit('setHeaderVisible', false)
        } else {
            store.commit('setHeaderVisible', true)
        }
        if (to.meta && to.meta.hiddenFooter) {
            store.commit('setFooterVisible', false)
        } else {
            store.commit('setFooterVisible', true)
        }
        if (to.matched.length === 3) {
            let match = to.matched[1]
            store.commit('setActivePath', match.path)
        } else {
            store.commit('setActivePath', to.path)
        }

    }
    if (to.matched && to.matched.length > 1) {
        let match = to.matched[0]

        if (match) {
            if (match.meta) {
                store.commit('setHeader', match.meta.header)
            }
            if (match.children) {
                let menuData = [];
                match.children.forEach(child => {
                    if (child.meta) {
                        menuData.push({
                            value: match.path + '/' + child.path,
                            text: child.meta.title,
                            image: child.meta.image,
                            target: child.meta.target,
                        })
                    }

                })
                store.commit('setMenuData', menuData)
            }
        }

    }

    if (token) {
        if (to.path === loginPath) {
            next({
                path: homePath
            })
            return
        }
        if (to.path === oauth2LoginPath) {
            next({
                path: homePath
            })
            return
        }
        if (to.path === null) {
            next({
                path: homePath
            })
            return
        }
    } else {
        if (whiteUrlList.indexOf(to.path) !== -1) {
            //白名单直接跳转
            next()
            return
        }

        if (to.path !== loginPath) {
            next({
                path: loginPath,
                replace: true
            })
            return
        }
    }

    router.$$isNotFirst = true
    next() // 跳转

})
router.afterEach(() => {
    NProgress.done()
})
export default router
