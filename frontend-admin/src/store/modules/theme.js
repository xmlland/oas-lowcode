import {ConfigProvider} from "ant-design-vue";
import {setCookie} from "@/lib/cookie";

export default {
    state: {
        theme: '',
        menuLocation: '',
        primaryColor: ''
    },
    getters: {
        getTheme: state => state.theme,
        getMenuLocation: state => state.menuLocation,
        getPrimaryColor: state => state.primaryColor,
    },
    mutations: {
        setTheme(state, theme) {
            state.theme = theme
            setCookie('theme', theme, 36500)
        },
        setMenuLocation(state, menuLocation) {
            state.menuLocation = menuLocation
            setCookie('menuLocation', menuLocation, 36500)
        },
        setPrimaryColor(state, primaryColor) {
            state.primaryColor = primaryColor
            setCookie('primaryColor', primaryColor, 36500)
            ConfigProvider.config({
                theme: {
                    primaryColor: primaryColor
                }
            })
        },
    },
    actions: {}
}
