
/**
 * 保留小数点后n位
 * @param number 数字
 * @param n 保留位数
 * @return {number} 保留后的数字
 */
export const fixedNumber = (number, n) => {
    if (n > 20 || n < 0) {
        throw new RangeError('toFixed() digits argument must be between 0 and 20');
    }
    if (isNaN(number) || number >= Math.pow(10, 21)) {
        return number;
    }
    if (typeof (n) == 'undefined' || n === 0) {
        return (Math.round(number));
    }

    let result = number.toString();
    let arr = result.split('.');

    // 整数的情况
    if (arr.length < 2) {
        return parseFloat(result);
        /*result += '.';
        for (var i = 0; i < n; i += 1) {
            result += '0';
        }*/

    }

    let integer = arr[0];
    let decimal = arr[1];
    if (decimal.length === n) {
        return parseFloat(result);
    }
    if (decimal.length < n) {
        return parseFloat(result);
        /*for (var i = 0; i < n - decimal.length; i += 1) {
            result += '0';
        }*/

    }
    result = integer + '.' + decimal.substr(0, n);
    let last = decimal.substr(n, 1);

    // 四舍五入，转换为整数再处理，避免浮点数精度的损失
    if (parseInt(last, 10) >= 5) {
        let x = Math.pow(10, n);
        result = (Math.round((parseFloat(result) * x)) + 1) / x;
        result = result.toFixed(n);
    }

    return parseFloat(result);
}

/**
 * 小数减法
 * @param arg1
 * @param arg2
 * @returns {number} 返回计算结果
 */
export const floatSub = (arg1, arg2) => {
    const precision = Math.max(getPrecision(Number(arg1)), getPrecision(Number(arg2)));
    const multiplier = Math.pow(10, precision);
    // 使用 + 操作符可以将字符串转换为数字，并且它会自动去除多余的零。
    return +(((arg1 * multiplier - arg2 * multiplier) / multiplier).toFixed(precision));
};

/**
 * 小数乘法
 * @param arg1
 * @param arg2
 * @returns {number} 返回计算结果
 */
export const floatMul = (arg1, arg2) => {
    const precision = getPrecision(Number(arg1)) + getPrecision(Number(arg2));
    const multiplier = Math.pow(10, precision);
    return +(((arg1 * multiplier * arg2) / multiplier).toFixed(precision));
};

/**
 * 获取小数点后位数
 * @param number
 * @returns {number|number|number}
 */
export const getPrecision = (number) => {
    try {
        return number.toString().split(".")[1].length || 0;
    } catch (e) {
        return 0;
    }
};
