import {getCookie, setCookie} from '@/lib/cookie'

export default {
    state: {
        lang: getCookie('lang') || 'zh'  // 'zh' | 'en'
    },
    getters: {
        getLang: state => state.lang,
        isEn: state => state.lang === 'en'
    },
    mutations: {
        setLang(state, lang) {
            state.lang = lang
            setCookie('lang', lang)
        }
    }
}
