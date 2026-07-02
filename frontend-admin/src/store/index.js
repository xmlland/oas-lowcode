import {createStore} from 'vuex'
import site from "./modules/site";
import theme from "./modules/theme";
import user from "./modules/user";

export default createStore({
    state: {},
    mutations: {},
    actions: {},
    modules: {
        site,
        theme,
        user
    }
})
