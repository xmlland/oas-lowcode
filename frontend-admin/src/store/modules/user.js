import {getCurrentUserAction, loginAction} from "@/api/api";
import {clearToken, getAction, setToken} from "@/api/action";
import store from "@/store"
import {clearStorage} from "@/lib/storage";
import {getUnreadCount} from "@/api/msg";

export default {
    state: {
        userView: {
            roles: [],
        },
        passwordExpired: false,
        minPasswordLength: 8,
        userMenuData: [],
        userMixLeftMenu: [],
        userMixTitle: '',
        userPermissionData: [],
        extEntId: 'null',
        userTodoMap: {},
        msgUnreadCount: 0
    },
    getters: {
        getUserView: state => state.userView,
        getPasswordExpired: state => state.passwordExpired,
        getMinPasswordLength: state => state.minPasswordLength,
        getUserName: state => state.userView.name,
        getUserMenuData: state => state.userMenuData,
        getUserMixLeftMenu: state => state.userMixLeftMenu,
        getUserMixTitle: state => state.userMixTitle,
        getUserPermissionData: state => state.userPermissionData,
        getExtEntId: state => state.extEntId,
        getUserTodoMap: state => state.userTodoMap,
        getMsgUnreadCount: state => state.msgUnreadCount
    },
    mutations: {
        setUserView(state, userView) {
            state.userView = userView
        },
        setPasswordExpired(state, passwordExpired) {
            state.passwordExpired = passwordExpired
        },
        setMinPasswordLength(state, minPasswordLength) {
            state.minPasswordLength = minPasswordLength
        },
        setUserMenuData(state, userMenuData) {
            state.userMenuData = userMenuData
        },
        setUserMixLeftMenu(state, userMixLeftMenu) {
            state.userMixLeftMenu = userMixLeftMenu
        },
        setUserMixTitle(state, userMixTitle) {
            state.userMixTitle = userMixTitle
        },
        setUserPermissionData(state, userPermissionData) {
            state.userPermissionData = userPermissionData
        },
        setExtEntId(state, extEntId) {
            state.extEntId = extEntId
        },
        setUserTodoMap(state, userTodoMap) {
            state.userTodoMap = userTodoMap
        },
        setMsgUnreadCount(state, count) {
            state.msgUnreadCount = count
        }
    },
    actions: {
        /**
         * 用户登录
         * @param context
         * @param loginData 原始登录数据对象（加密在loginAction内部完成）
         * @returns {Promise<unknown>}
         */
        userLogin(context, loginData) {
            return new Promise((resolve, reject) => {
                loginAction(loginData).then(res => {
                    console.log('login success', res)
                    if (res.data && res.data.redirect) {
                        clearToken()
                        clearStorage()
                        store.commit('setUserView', {})
                        store.commit('setExtEntId', 'null')
                        location.href = res.data.redirect
                        return
                    }
                    if (res.token) {
                        setToken(res.token)
                    }
                    getCurrentUserAction().then(userRes => {
                        store.commit('setUserView', userRes.data.userView)
                        store.commit('setExtEntId', userRes.data.extEntId)
                        if (userRes.data.userView && userRes.data.userView.passwordExpired) {
                            store.commit('setPasswordExpired', true)
                        }
                        resolve(userRes)
                    }).catch(userErr => {
                        console.log(userErr)
                    })

                }).catch(err => {
                    reject(err)
                })
            })

        },
        /**
         * 查询用户待办
         */
        updateUserTodoCount() {
            getAction('/act/process/todoMap', {}).then(res => {
                let data = res.data.data;
                let dataDefine = res.data.dataDefine;
                let map = {};
                for (let key in dataDefine) {
                    if (data[key]) {
                        let count = map[dataDefine[key]] || 0;
                        count += data[key];
                        map[dataDefine[key]] = count;
                    }
                }
                Object.assign(data, map)
                console.log('data',data)
                store.commit('setUserTodoMap', data)
            })
        },
        /**
         * 刷新消息未读数
         */
        refreshMsgUnreadCount() {
            getUnreadCount().then(res => {
                if (res.code === 0) {
                    store.commit('setMsgUnreadCount', res.count || 0)
                }
            }).catch(err => {
                console.error('获取未读消息数失败:', err)
            })
        }
    }
}
