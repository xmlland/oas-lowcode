// noinspection JSUnusedGlobalSymbols

import {computed, ref, unref} from 'vue';
import { useWebSocket } from '@vueuse/core';
// import { getToken} from "@/api/action";
// import {md5} from "@/lib/tools";
import store from "@/store";

let result;
const listeners = new Map();
let userView = computed(() => store.getters.getUserView);
let serverUrl = ref(window['serverUrl'])
/**
 * 开启 WebSocket 链接，全局只需执行一次
 */
export function connectWebSocket() {
    let userId = userView.value.id;
    // let token= getToken()
    // let wsClientId = md5(token);
    // userId = userId+ "thisIsWebSocketToken_" + token;
    // WebSocket与普通的请求所用协议有所不同，ws等同于http，wss等同于https
    if (!serverUrl.value){
        console.log("未读取到配置window['serverUrl']")
        return
    }
    // 当 serverUrl 是相对路径时，从 window.location 构造完整 URL
    let resolvedUrl = serverUrl.value;
    if (!resolvedUrl.startsWith('http')) {
        const loc = window.location;
        resolvedUrl = loc.protocol + '//' + loc.host + resolvedUrl;
    }
    let url = resolvedUrl.replace('https://', 'wss://').replace('http://', 'ws://') + 'websocket/' + userId;
    console.log("url=" + url)
    result = useWebSocket(url, {
        // 自动重连 (遇到错误最多重复连接3次)
        autoReconnect: {
            retries: 3,
            delay: 5000
        },
        // 心跳检测
        heartbeat: {
            message: "ping",
            interval: 55000
        },
        // protocols: [token],
    });
    //update-end-author:taoyan date:2022-4-24 for: v2.4.6 的 websocket 服务端，存在性能和安全问题。 #3278
    if (result) {
        result.open = onOpen;
        result.close = onClose;
        const ws = unref(result.ws);
        if (ws != null) {
            ws.onerror = onError;
            ws.onmessage = onMessage;
        }
    }
}

function onOpen() {
    console.log('[WebSocket] 连接成功');
}

function onClose(e) {
    console.log('[WebSocket] 连接断开：', e);
}

function onError(e) {
    console.log('[WebSocket] 连接发生错误: ', e);
}

function onMessage(e) {
    console.debug('[WebSocket] -----接收消息-------', e.data);
    try {
        const data = JSON.parse(e.data);
        for (const callback of listeners.keys()) {
            try {
                callback(data);
            } catch (err) {
                console.error(err);
            }
        }
    } catch (err) {
        console.error('[WebSocket] data解析失败：', err);
    }
}

/**
 * 添加 WebSocket 消息监听
 * @param {Function} callback
 */
export function onWebSocket(callback) {
    if (!listeners.has(callback)) {
        if (typeof callback === 'function') {
            listeners.set(callback, null);
        } else {
            console.debug('[WebSocket] 添加 WebSocket 消息监听失败：传入的参数不是一个方法');
        }
    }
}

/**
 * 解除 WebSocket 消息监听
 * @param {Function} callback
 */
export function offWebSocket(callback) {
    listeners.delete(callback);
}

export function useMyWebSocket() {
    return result;
}
