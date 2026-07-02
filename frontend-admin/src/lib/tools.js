import store from "@/store";
import {getStorage} from "@/lib/storage";
import {getToken} from "@/api/action";
import html2canvas from "html2canvas";
import randomColor from "./ext/randomColor"

import blueMd5 from 'blueimp-md5'

export const md5 = (str) => {
    return blueMd5(str)
}

export {confirm, confirmModal, prompt, promptModal} from './tool-modal.jsx';
/**
 * 平滑的将div滚动到指定位置
 * @param containerId 容器id
 * @param targetId 目标id
 * @param duration 持续时间
 */
export const scrollToDiv = (containerId, targetId, duration = 1000,onSuccess=()=>{}) => {
    let container = document.getElementById(containerId);
    let target = document.getElementById(targetId)
    const startY = container.scrollTop; // 当前滚动位置
    let endY = target.offsetTop - container.offsetTop; // 需要滚动的距离
    const distance = endY - startY; // 需要滚动的距离

    let start;

    function step(timestamp) {
        if (!start) start = timestamp;
        const progress = timestamp - start;
        const scrollY = easeInOutQuad(progress, startY, distance, duration);
        container.scrollTo({
            top: scrollY,
        });
        if (progress < duration) {
            window.requestAnimationFrame(step);
        }else{
            onSuccess()
        }
    }

    function easeInOutQuad(t, b, c, d) {
        t /= d / 2;
        if (t < 1) return c / 2 * t * t + b;
        t--;
        return -c / 2 * (t * (t - 2) - 1) + b;
    }

    window.requestAnimationFrame(step);
}
/**
 * 清除所有的定时器
 */
export const clearAllInterval = () => {
    let end = setInterval(function () {
    }, 10000);
    for (let i = 1; i <= end; i++) {
        clearInterval(i);
    }
}
/**
 * @param {Array} array1
 * @param {Array} array2
 * @description 得到两个数组的交集, 两个数组的元素为数值或字符串
 */
export const getIntersection = (array1, array2) => {
    let arr1 = [], arr2 = []
    if (array1.length > array2.length) {
        arr1 = array1
        arr2 = array2
    } else {
        arr1 = array2
        arr2 = array1
    }
    let len = Math.min(arr1.length, arr2.length)
    let i = -1
    let res = []
    while (++i < len) {
        const item = arr2[i]
        if (arr1.indexOf(item) > -1) res.push(item)
    }
    return res
}

/**
 * @param {Array} arr1
 * @param {Array} arr2
 * @description 得到两个数组的并集, 两个数组的元素为数值或字符串
 */
export const getUnion = (arr1, arr2) => {
    return Array.from(new Set([...arr1, ...arr2]))
}

/**
 * @param {String|Number} value 要验证的字符串或数值
 * @param {*} validList 用来验证的列表
 * @param ignoreCase 忽略大小写
 * @param regular 是否是正则表达式
 */
export function oneOf(value, validList,ignoreCase = false, regular = false) {
    for (let i = 0; i < validList.length; i++) {
        if (regular) {
            if (new RegExp(validList[i]).test(value)) {
                return true
            }
        }
        if (value === validList[i]) {
            return true
        }else if(ignoreCase){
            if(((typeof value)==='string')&&((typeof validList[i])==='string')){
                if(value.toLowerCase() === validList[i].toLowerCase()){
                    return true;
                }
            }
        }
    }
    return false
}

/**
 * 计算两个字符串的相似度
 * @param str1
 * @param str2
 * @return {number} 返回相似度 0-1
 */
export const calculateSimilarity = (str1, str2) => {
    const m = str1.length;
    const n = str2.length;

    // 创建一个二维数组来保存编辑距离
    const dp = [];
    for (let i = 0; i <= m; i++) {
        dp[i] = [];
        dp[i][0] = i;
    }
    for (let j = 0; j <= n; j++) {
        dp[0][j] = j;
    }

    // 计算编辑距离
    for (let i = 1; i <= m; i++) {
        for (let j = 1; j <= n; j++) {
            if (str1[i - 1] === str2[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1];
            } else {
                dp[i][j] = Math.min(
                    dp[i - 1][j] + 1, // 删除操作
                    dp[i][j - 1] + 1, // 插入操作
                    dp[i - 1][j - 1] + 1 // 替换操作
                );
            }
        }
    }

    // 返回相似度
    return 1 - dp[m][n] / Math.max(m, n);
}
/**
 * 替换字符串所有指定字符串
 * @param str 字符串
 * @param find 被替换的内容
 * @param replace 替换的内容
 * @return {*} 替换后的字符串
 */
export const replaceAll=(str, find, replace='')=> {
    return str.replace(new RegExp(find, 'g'), replace);
}
/**
 * 去除对象中 所有空字符
 * @param obj 对象
 */
export const removeEmptyStrings = (obj)=>{
    // 递归函数，用于处理对象和数组
    function process(value) {
        if (Array.isArray(value)) {
            // 如果是数组，遍历数组中的每个元素
            return value.map(item => process(item));
        } else if (typeof value === 'object' && value !== null) {
            // 如果是对象，遍历对象的每个属性
            const result = {};
            for (const key in value) {
                if (Object.prototype.hasOwnProperty.call(value, key)) {
                    result[key] = process(value[key]);
                }
            }
            return result;
        } else if (typeof value === 'string') {
            // 如果是字符串，去除所有空字符
            return value.replace(/\s+/g, '');
        } else {
            // 其他类型的数据直接返回
            return value;
        }
    }

    return process(obj);
}
/**
 * 获取文件后缀名
 * @param filename 文件名
 * @return {string} 小写的后缀名
 */
export const getFileExtend = (filename) => {
    let index = filename.lastIndexOf(".");
    let suffix = filename.substr(index + 1);
    return suffix.toLowerCase()
}

export const UUID = () => {
    let s = [];
    let hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    let uuid = s.join("");
    return uuid;
}

export const getData = (item, keys) => {
    if (Array.isArray(keys)) {
        let parentId = item
        keys.forEach(key => {
            if (!parentId){
                parentId = {}
            }
            parentId = parentId[key]
        })
        return parentId
    } else {
        return item[keys]
    }
}

export const setData = (item, keys, value) => {
    if (Array.isArray(keys) && keys.length > 1) {
        let child = {}
        setData(child, keys.slice(1), value)
        item[keys[0]] = child
    } else if (Array.isArray(keys) && keys.length === 1) {
        item[keys[0]] = value
    } else {
        item[keys] = value
    }
}
/**
 *
 * @param data 需要递归的数据
 * @param parent 根的上级
 * @param transform
 * @param tree
 * @param parentTitles
 * @returns {*[]}
 */
export const listToTree = (data, parent, transform, tree = false, parentTitles = []) => {
    let result = []
    data.forEach((item) => {
        //这里用 == 因为有可能是数字 类型不一致
        if (getData(item, transform.parent) == parent) {
            let res = {
                title: getData(item, transform.title),
                key: getData(item, transform.key),
                value: getData(item, transform.key),
                disabled: item.disabled === undefined?false:item.disabled,
                class: item.className || (!transform.formDisabled ? ((transform.status && !item.hasChildren && '填报注意事项' !== getData(item, transform.title)) ? ' data-status ' : '') + (getData(item, transform.done) ? ' data-done ' : '') : ''),
                attributes: item,
                isLeaf: true,
            }
            let titles = JSON.parse(JSON.stringify(parentTitles))
            titles.push(res.title)
            if (item.hasChildren || tree) {
                res.children = listToTree(data, getData(item, transform.key), transform, tree, JSON.parse(JSON.stringify(titles)))
                if (res.children.length > 0) {
                    res.isLeaf = false
                }
            }
            res.parentTitles = JSON.parse(JSON.stringify(titles))
            res.selectable = transform.selectable ? transform.selectable(res) : true
            if (transform.showCount) {
                if (typeof (transform.showCount) === "function") {
                    let showCount = transform.showCount(item, res);
                    if (showCount) {
                        res.title = `${res.title}（${showCount}）`
                    } else {
                        res.title = `${res.title}`
                    }

                } else {
                    res.title = `${res.title}（${res.children ? res.children.length : 0}）`
                }

            }
            result.push(res)
        }

    })
    return result
}

export const getWebUrl = () => {
    let routerIndex = window.location.href.indexOf('#');
    let url = window.location.href.slice(0, routerIndex > 0 ? routerIndex : window.location.href.length);
    return url;
}

/**
 * 保留小数点后n位
 * @param number 数字
 * @param n 保留位数
 * @return {number} 保留后的数字
 */
export { fixedNumber } from './tool-math.js';

export const groupBy = (array, f, groupName = 'group', childrenName = 'children') => {
    let groups = [];
    let indexList = [];
    //给每一个项增加一个组名
    let dataList = array.map(item => {
        if (typeof f === 'function') {
            item._groupName = f(item);
        }else {
            item._groupName = item[f];
        }
        return item;
    });
    //排除重名
    indexList = Array.from(new Set(dataList.map(item => item._groupName)));
    //根据组名分组
    indexList.forEach(item => {
        let obj = {}
        obj[groupName] = item
        obj[childrenName] = dataList.filter(sitem => {
            return sitem._groupName === item;
        })
        groups.push(obj)
    });
    return groups;
}

/**
 * 对数组中指定属性的求和
 * @param data 数组
 * @param key 属性 或者函数
 * @return {number}
 */
export const arraySum = (data, key) => {
    let sumVal = 0.0;
    data.forEach(item => {
        // 使用 key 函数提取分组键，如果是字符串属性则直接访问
        const val = typeof key === 'function' ? key(item) : item[key];
        sumVal += parseFloat(val) || 0.0
    })
    return Math.floor(sumVal);
}

export const hasAnyRole = (roles) => {
    return getIntersection(store.getters.getUserView.roles, roles).length > 0
}
export const hasAnyPermission = (permission) => {
    return getIntersection(store.getters.getUserPermissionData, permission).length > 0
}
export const reloadToIndex = () => {
    location.href = ''
}

let areaId = ref(config.areaId)
export const isProvinceUser = () => {
    let office = store.getters.getUserView.office;
    if(office.area.id && office.area.id === areaId.value){
        return true;
    }else{
        return false
    }
}

import {dateFormat, calcSecond, formatSecond, getCurrentYear, getLastYear} from './tool-date';
import {Modal} from "ant-design-vue";
import {ref} from "vue";
import config from "@/config";
export {dateFormat, calcSecond, formatSecond, getCurrentYear, getLastYear};

const defaultValue = {
    '${currentUser}': () => {
        let userView = store.getters.getUserView;
        if (userView) {
            return {
                id: userView.id,
                name: userView.name,
            }
        }
        return null
    },
    '${currentUserName}': () => {
        let userView = store.getters.getUserView;
        if (userView) {
            return userView.name
        }
        return null
    },
    '${currentTime}': () => {
        return dateFormat('yyyy-MM-dd HH:mm:ss')
    },
    '${currentDate}': () => {
        return dateFormat()
    },
    '${currentYear}': () => {
        return getCurrentYear()
    },
    '${currentYear}-01-01': () => {
        return getCurrentYear()+'-01-01'
    },
    '${currentCompany}': () => {
        let userView = store.getters.getUserView;
        if (userView && userView.company && userView.company.id) {
            return {
                id: userView.company.id,
                name: userView.company.name
            }
        }
        return null
    },
    '${currentCompanyArea}': () => {
        let userView = store.getters.getUserView;
        if (userView && userView.company && userView.company.area) {
            return userView.company.area.id
        }
        return null
    },
    '${currentOffice}': () => {
        let userView = store.getters.getUserView;
        if (userView && userView.office && userView.office.id) {
            return {
                id: userView.office.id,
                name: userView.office.name
            }
        }
        return null
    },
}
export const replaceDefaultValue = (value) => {
    if (value) {
        if (defaultValue[value]) {
            value = defaultValue[value]()
        }
    }
    return value
}

export const isPromise = (val) => {
    return val != null && typeof (val) === 'object' && typeof (val.then) === 'function'
};

export const translateDict = (text, dict, dictMultiple) => {
    if (!text) {
        return ''
    }
    if (parseInt(dictMultiple) !== 0) {
        let split = text.split(',');
        let arr = []
        split.forEach(s => {
            arr.push(getStorage('Dict_' + dict + s) || '')
        })
        return arr.join()
    }
    let dictText = getStorage('Dict_' + dict + text) || ''
    return dictText || text
}

/**
 * 从固定数组中获取某个值的text
 * @param value 值
 * @param array 数组
 * @param valueField 值字段
 * @param textField 文本字段
 */
export const getTextFormArray = (value, array, valueField = 'value', textField = 'text') => {
    let text = ''
    if (value && array && array.length > 0) {
        array.forEach(item => {
            if (item[valueField] === value) {
                text = item[textField]
            }
        })
    }
    return text
}

export const formatAccessToken = (url) => {
    return url.replace('{access_token}', getToken())
}

/**
* 深拷贝对象
* */
export const deepClone = (obj, hash = new WeakMap())  =>{
    if (typeof obj !== 'object' || obj === null) {
        return obj;
    }
    if (hash.has(obj)) {
        return hash.get(obj);
    }
    let result = Array.isArray(obj) ? [] : obj.constructor ? new obj.constructor() : Object.create(null);
    hash.set(obj, result);
    for (let key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
            result[key] = deepClone(obj[key], hash);
        }
    }
    return result;
}

/**
 * 判断是否为空字符串
 * @param text
 * @return {boolean}
 */
export const isEmpty = (text) => {
    return text == null || text === '' || text === undefined;
}

/**
 * 判断是否不为空字符串
 * @param text
 * @return {boolean}
 */
export const isNotEmpty = (text) => {
    return !isEmpty(text);
}

/**
 * 判断是否以指定字符串结束
 * @param str
 * @param endStr
 * @return {boolean}
 */
export const isEndWith = (str, endStr) => {
    return str.substring(str.length - endStr.length) === endStr
}

/**
 * 判断对象是否为null和空
 * @param obj
 * @return {boolean}
 */
export const isNullOrEmpty = (obj) => {
    return obj === null || (typeof obj === 'object' && Object.keys(obj).length === 0);
}

/**
 * 克隆一个javaBean对象 移除id createBy create_date updateBy update_date del_flag owner_code等字段 用于重新保存数据
 * @param source
 * @return {*}
 */
export const cloneJavaBean = (source) => {
    //先拷贝一份 避免影响之前的数据
    let target = deepClone(source)
    delete target.id
    delete target.createBy
    delete target.create_date
    delete target.updateBy
    delete target.update_date
    delete target.del_flag
    delete target.owner_code
    return target
}
/**
 * 保存页面为图片
 * @param options html2canvas的参数
 * @param fileName 保存的文件名
 */
export const saveDocumentAsImg = (options, fileName = "capture") => {
    htmlToCanvas(document.body, options, fileName)
}
/**
 * 保存指定id的html元素为图片
 * @param id html元素的id
 * @param options html2canvas的参数
 * @param fileName 保存的文件名
 */
export const saveHtmlAsImg = (id, options, fileName = "capture") => {
    htmlToCanvas(document.querySelector("#" + id), options, fileName)
}

const htmlToCanvas = (element, options, fileName) => {
    const modal = Modal.info({
        title: '提示',
        okButtonProps: {style: {display: 'none'}},
        content: '处理中...',
    });
    html2canvas(element, options).then(canvas => {
        saveCanvasAsImg(canvas, fileName)
        modal.destroy();
    });
}

const saveCanvasAsImg = (canvas, fileName) => {
    const base64Img = canvas.toDataURL('image/png')
    let a = document.createElement('a') // 生成一个a元素
    let event = new MouseEvent('click') // 创建一个单击事件
    a.download = fileName // 设置图片名称
    a.href = base64Img // 将生成的URL设置为a.href属性
    a.dispatchEvent(event)
}

export const randomColorByStr = (val = '') => {

    const stringToUniqueInt = (str) => {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            const char = str.charCodeAt(i);
            hash = (hash + char) * 233 + 131313; // 使用特定的乘数和增量来散列
        }
        return hash >>> 0; // 转换为32位无符号整数
    }
    return randomColor({
        seed: stringToUniqueInt(val)
    })
}

export const setElementStyleByClass = (root, className, style = {}) => {
    //将style对象解析为字符串
    let styleStr = '';
    for (let key in style) {
        styleStr += key + ':' + style[key] + ' !important;'
    }
    let styleNode = document.createElement("style");
    //设置type属性
    styleNode.type = "text/css";
    //判断该style标签是否存在
    let styleNodes = document.head.getElementsByTagName('style');
    for (let i = 0; i < styleNodes.length; i++) {
        if (styleNodes[i].innerHTML.indexOf(className) > -1) {
            //存在则删除
            document.head.removeChild(styleNodes[i]);
        }
    }
    styleNode.innerHTML = className + '{' + styleStr + '}';
    document.head.appendChild(styleNode);
}

export const setThemeStyle = (root = document, themeStyle = null, parentClass = '', isRoot = true) => {
    const findParentClass = (node) => {
        let arr = ['.' + replaceAll(node.className, ' ', '.')]
        let parent = node.parentElement
        //parent存在并且不是body
        while (parent && parent.tagName !== 'BODY') {
            if (parent.className) {
                arr.push('.' + replaceAll(parent.className, ' ', '.'))
            }
            parent = parent.parentElement
        }

        return arr.reverse().join(' ')
    }

    themeStyle = themeStyle || JSON.parse(JSON.stringify(config.themeStyle || {}))
    for (let item in themeStyle) {
        let keyArr = item.split(',');
        keyArr.forEach((key) => {

            let classStyle = JSON.parse(JSON.stringify(themeStyle[item]))
            let elements = root.getElementsByClassName(key)
            if (elements.length === 0 && isRoot) {
                console.warn('未找到class为' + key + '的元素,根节点必须在页面渲染时存在')
                setTimeout(() => {
                    setThemeStyle()
                }, 100)
                return
            }
            let tempParentClass = ''
            if (isRoot) {
                tempParentClass = findParentClass(elements[0])
            } else if (key.indexOf(':') >= 0) {
                tempParentClass = parentClass + key
            } else if (key.indexOf('>') === 0) {
                tempParentClass = parentClass + key
            } else {
                tempParentClass = parentClass + ' .' + key
            }
            if (classStyle && classStyle.style) {
                setElementStyleByClass(root, tempParentClass, classStyle.style)
                delete classStyle.style
            }
            if (classStyle && !isNullOrEmpty(classStyle)) {
                setThemeStyle(elements[0], classStyle, tempParentClass, false)
            }
        })
    }
}
