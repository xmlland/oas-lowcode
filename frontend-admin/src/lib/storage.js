import {md5} from "@/lib/tools";

export const STORAGE_PREFIX = 'STORAGE_PREFIX'
export const setStorage = (key, value) => {
    sessionStorage.setItem(key, typeof (value) == 'object' ? JSON.stringify(value) : value);
    sessionStorage.setItem(STORAGE_PREFIX + md5(key), typeof (value) == 'object' ? JSON.stringify(value) : value);
}
export const getStorage = (key) => {
    return sessionStorage.getItem(STORAGE_PREFIX + md5(key))
}
export const removeStorage = (key) => {
    sessionStorage.removeItem(key)
    return sessionStorage.removeItem(STORAGE_PREFIX + md5(key))
}
export const clearStorage = () => {
    sessionStorage.clear()
}
