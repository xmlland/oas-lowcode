export default {
    state: {
        subSystemCode: '',
        header: {},
        menuData: [],
        footer: {},
        headerVisible: true,
        footerVisible: true,
        activePath: '',
        activeTabs: [],
        feedbackVisible: false,
        feedbackNews: 0,
        helpVisible: false,
        helpContentId: '',
        isNotCanSaveAndComplete: false,
        nowRowIsSuspend: false, //当前编辑的数据是 挂起 的任务
        actButtonsBoundPermission: null, //根据权限判断是否 有 工作流 相关操作的 权限 例如 {'saveAndSuspend':'app:work_scene_applyinfo:suspend'}
    },
    getters: {
        getSubSystemCode: state => state.subSystemCode,
        getHeader: state => state.header,
        getMenuData: state => state.menuData,
        getFooter: state => state.footer,
        getHeaderVisible: state => state.headerVisible,
        getFooterVisible: state => state.footerVisible,
        getActivePath: state => state.activePath,
        getActiveTabs: state => state.activeTabs,
        getFeedbackVisible: state => state.feedbackVisible,
        getFeedbackNews: state => state.feedbackNews,
        getHelpVisible: state => state.helpVisible,
        getHelpContentId: state => state.helpContentId,
        getIsNotCanSaveAndComplete: state => state.isNotCanSaveAndComplete,
        getNowRowIsSuspend: state => state.nowRowIsSuspend,
        getActButtonsBoundPermission: state => state.actButtonsBoundPermission,
    },
    mutations: {
        setSubSystemCode(state, subSystemCode) {
            state.subSystemCode = subSystemCode
        },
        setHeader(state, header) {
            state.header = header
            if (header) {
                document.title = header.title
            }
        },
        setMenuData(state, menuData) {
            state.menuData = menuData
        },
        setFooter(state, footer) {
            state.footer = footer
        },
        setHeaderVisible(state, headerVisible) {
            state.headerVisible = headerVisible
        },
        setFooterVisible(state, footerVisible) {
            state.footerVisible = footerVisible
        },
        setActivePath(state, activePath) {
            state.activePath = activePath
        },
        setActiveTabs(state, activeTabs) {
            state.activeTabs = activeTabs
        },
        setFeedbackVisible(state, feedbackVisible) {
            state.feedbackVisible = feedbackVisible
        },
        setFeedbackNews(state, feedbackNews) {
            state.feedbackNews = feedbackNews
        },
        setHelpVisible(state, helpVisible) {
            state.helpVisible = helpVisible
        },
        setHelpContentId(state, helpContentId) {
            state.helpContentId = helpContentId
        },
        setIsNotCanSaveAndComplete(state, isNotCanSaveAndComplete) {
            state.isNotCanSaveAndComplete = isNotCanSaveAndComplete
        },
        setNowRowIsSuspend(state, nowRowIsSuspend) {
            state.nowRowIsSuspend = nowRowIsSuspend
        },
        setActButtonsBoundPermission(state, actButtonsBoundPermission) {
            state.actButtonsBoundPermission = actButtonsBoundPermission
        },
    },
    actions: {}
}
