import Cookies from 'js-cookie'

const getCookiePrefix = () => {
    const prefix = window['cookiePrefix'];
    if (!prefix) {
        console.warn(`window['cookiePrefix'] 未配置，使用默认值`)
    }
    return prefix || 'jeeStudio_ant_vue3';
}
let cookieExpires = window['cookieExpires'] || 1 / 48;
/**
 * 设置cookie
 * @param key
 * @param value
 * @param expires 过期天数
 */
export const setCookie = (key, value, expires = cookieExpires) => {
    Cookies.set(getCookiePrefix() + '_' + key, value, {expires: expires})
}
/**
 * 删除cookie
 * @param key
 */
export const removeCookie = (key) => {
    Cookies.set(getCookiePrefix() + '_' + key, '', {expires: -1})
}
/**
 * 获取cookie
 * @param key
 * @returns {boolean|*}
 */
export const getCookie = (key) => {
    const data = Cookies.get(getCookiePrefix() + '_' + key)
    if (data) return data
    else return false
}
