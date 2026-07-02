
export const dateFormat = (fmt = 'yyyy-MM-dd',date = new Date()) => {
    let o = {
        "M+": date.getMonth() + 1, //月份
        "d+": date.getDate(), //日
        "H+": date.getHours(), //小时
        "m+": date.getMinutes(), //分
        "s+": date.getSeconds(), //秒
        "q+": Math.floor((date.getMonth() + 3) / 3), //季度
        "S": date.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (let k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

export const dateTimeFormat = (date = new Date()) => {
    return dateFormat('yyyy-MM-dd HH:mm:ss', date)
}

/**
 * 判断是否是合法的日期字符串
 * @param value
 * @return {boolean}
 */
export const validDateStr = (value) => {

    const date = /^(\d{2,4})([/\-.年]?)(\d{1,2})([/\-.月]?)(\d{1,2})日?$/;
    const matcher = value.match(date);
    if (!matcher) {
        return false;
    }

    let year = parseInt(matcher[1]);
    let month = parseInt(matcher[2]);
    let day = parseInt(matcher[3]);

    // 验证月
    if (month < 1 || month > 12) {
        return false;
    }

    // 验证日
    if (day < 1 || day > 31) {
        return false;
    }

    // 检查几个特殊月的最大天数
    if (day === 31 && (month === 4 || month === 6 || month === 9 || month === 11)) {
        return false;
    }

    if (month === 2) {
        // 在2月，非闰年最大28，闰年最大29
        return day < 29 || (day === 29 && isLeapYear(year));
    }

    return true;
}

/**
 * 判断是否是闰年
 * @param year
 * @return {boolean}
 */
export const isLeapYear = (year) => {
    year = parseInt(year);
    return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
}

/**
 * 计算两个时间的秒数
 * @param s
 * @param e
 * @return {number}
 */
export const calcSecond = (s, e) => {
    return parseInt((new Date(e).getTime() / 1000) - (new Date(s).getTime() / 1000));
}

/**
 * 计算两个时间间隔多少秒 超过60秒显示为xx分xx秒
 * @param s 开始时间
 * @param e 结束时间
 * @param offsetSecond 结束时间 往后加xx秒
 * @returns {string}
 */
export const formatSecond = (s, e, offsetSecond) => {
    let seconds = calcSecond(s, dateFormat('yyyy-MM-dd HH:mm:ss', new Date(new Date(e).getTime() + offsetSecond * 1000)))
    if (seconds > 60) {
        return Math.floor(seconds / 60) + '分' + (seconds % 60) + '秒'
    } else {
        return seconds + '秒'
    }
}

export const getCurrentYear = () => {
    return dateFormat('yyyy')
}
export const getLastYear = () => {
    return parseInt(getCurrentYear()) - 1 + ''
}
