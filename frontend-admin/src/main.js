// 运行时配置注入：将 .env.* 中的配置挂载到 window（替代 public/env/*/env.js）
// 必须在所有 import 之前执行，因为 router/store 模块会立即读取 window 全局变量
const cfg = __RUNTIME_CONFIG__;
window['baseUrl'] = cfg.baseUrl;
window['serverUrl'] = cfg.serverUrl;
window['fileHost'] = cfg.fileHost;
window['documentServerUrl'] = cfg.documentServerUrl;
window['onlyofficeInnerUrl'] = cfg.onlyofficeInnerUrl;
window['cookiePrefix'] = cfg.cookiePrefix;
window['tokenKey'] = cfg.tokenKey;
window['tokenName'] = cfg.tokenName;

import {createApp} from 'vue'
import App from './App.vue'
import './assets/less/main.less';
import './assets/fonts/font.css'
import router from "@/router";
import store from "@/store";

// 获取天地图Key（如果config.js中未配置则从后端接口获取）
;(function() {
    var config = window['__webConfig__'];
    if (config && !config.tdtKey) {
        fetch(window['baseUrl'] + 'getTdtKey')
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data && data.code === 0 && data.msg) {
                    config.tdtKey = data.msg;
                }
            })
            .catch(function(err) {
                console.warn('获取天地图Key失败', err);
            });
    }
})();
//引入 ant desgin vue
import Antd from 'ant-design-vue';
//引入 ant desgin vue icon
import * as Icons from '@ant-design/icons-vue';
import UComponent from './components/index'
import {getIntersection} from "@/lib/tools";

const app = createApp(App);
// 全局使用图标
const icons = Icons;
for (const i in icons) {
    app.component(i, icons[i]);
}
app.directive('hasRole', {
    mounted(el, binging) {
        let show = false;
        if (store.getters.getUserView.roles && getIntersection(store.getters.getUserView.roles, binging.value).length > 0) {
            show = true
        }
        if (!show) {
            el.parentNode && el.parentNode.removeChild(el);
        }
    }
})
app.directive('drag', {
    mounted(el) {
        let startEvt, moveEvt, endEvt
        // 判断是否支持触摸事件
        if ('ontouchstart' in window) {
            startEvt = 'touchstart'
            moveEvt = 'touchmove'
            endEvt = 'touchend'
        } else {
            startEvt = 'mousedown'
            moveEvt = 'mousemove'
            endEvt = 'mouseup'
        }
        el.style.cursor = 'grab'
        let disX, disY, left, top, starX, starY;
        const moveFun = (event) => {
            // 兼容IE浏览器
            let e = event || window.event
            left = (e.touches ? e.touches[0].clientX : e.clientX) - disX
            top = (e.touches ? e.touches[0].clientY : e.clientY) - disY
            // 限制拖拽的X范围，不能拖出屏幕
            if (left < 0) {
                left = 0
            } else if (left > document.documentElement.clientWidth - el.offsetWidth) {
                left = document.documentElement.clientWidth - el.offsetWidth
            }
            // 限制拖拽的Y范围，不能拖出屏幕
            if (top < 0) {
                top = 0
            } else if (top > window.innerHeight - el.offsetHeight) {
                top = window.innerHeight - el.offsetHeight
            }
            el.style.left = left + 'px'
            el.style.top = top + 'px'
        }

        const endFun = () => {
            el.style.cursor = ''
            document.removeEventListener(moveEvt, moveFun)
            document.removeEventListener(endEvt, endFun)

        }

        el.addEventListener(startEvt, (event) => {
            // 阻止页面的滚动，缩放
            event.preventDefault()
            // 兼容IE浏览器
            el.style.cursor = 'grabbing'
            let e = event || window.event
            // 手指按下时的坐标
            starX = e.touches ? e.touches[0].clientX : e.clientX
            starY = e.touches ? e.touches[0].clientY : e.clientY
            // 手指相对于拖动元素左上角的位置
            disX = starX - el.offsetLeft
            disY = starY - el.offsetTop
            // 按下之后才监听后续事件
            document.addEventListener(moveEvt, moveFun)
            document.addEventListener(endEvt, endFun)
        })

    }
})
app.use(Antd).use(UComponent).use(store).use(router).mount('#app')
