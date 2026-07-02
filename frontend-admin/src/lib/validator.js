import {postAction} from "@/api/action";
import {defaultTableViewUrl} from "@/api/api";
import {isValidCard} from "@/lib/tool-idcard";
import {removeEmptyStrings} from "@/lib/tools";

const regular = {
    digits: /^(-?\d+)(\.\d+)?$/,
    number: /^(-?\d+)(\.\d+)?$/,
    mobile: /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/,
    phone: /(^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)/,
    longitude: /^(0?\d{1,2}(\.\d{1,8})*|1[0-7]?\d{1}(\.\d{1,8})*|180(\.0{1,8})*)$/,
    latitude: /^([0-8]?\d{1}(\.\d{1,8})*|90(\.0{1,8})*)$/,
    email: /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/,
    zipCode: /^[0-9]\d{5}(?!\d)$/,
    password: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@#$%^&*]).{10,}$/,
    username: /^(?=.*[A-Za-z])(?=.*\d)|[A-Za-z\d]{6,18}$/,
}

// 获取验证器1常用2自定义
export const getValidator = (validateType) => {
    validateType = convertRegexFromDb(validateType);
    // 查找 validateType 对应的验证器
    const validatorKey = validateType + 'Validator';

    // 直接访问验证器函数
    if (typeof window[validatorKey] === 'function') {
        return window[validatorKey]; // 返回方法
    }

    // 1. 如果没有找到对应的验证器，则查找 regular 对象中的正则表达式
    // 2. 判断validateType的内容是否是正则规则
    if (regular[validateType]) {
      return async (rule, value) => {
        if (!value || regular[validateType].test(value)) {
          return Promise.resolve();
        }
        return Promise.reject(`must be ${validateType}`);
      };
    } else if (isValidRegex(validateType)) {
        return async (rule, value) => {
            if (!value || new RegExp(validateType).test(value)) {
                return Promise.resolve();
            }
            return Promise.reject(`must be ${validateType}`);
        };
    }

    // 如果没有找到正则表达式，返回默认的 reject
    return async (rule, value) => {
        return Promise.reject(`Validator for ${validateType} not found`);
    };
};
function convertRegexFromDb(dbRegex) {
    return dbRegex.replace(/\\\\/g, '\\')  // 先把所有 \\ 还原为 \
            .replace(/\\d/g, '\\d')        // 把 \\d 还原为 \d
            .replace(/\\w/g, '\\w')        // 把 \\w 还原为 \w
            .replace(/\\s/g, '\\s');       // 把 \\s 还原为 \s
}
export const isValidRegex = (str) => {
    if (str == null || str.length == 0) {
        return false;
    }
    try {
        new RegExp(str); // 尝试创建正则表达式对象
        return true; // 如果成功，则说明是一个有效的正则表达式
    } catch (e) {
        return false; // 如果抛出错误，则说明不是有效的正则表达式
    }
}
export const digitsValidator = async (rule, value) => {
    if (!value || regular.digits.test(value))
        return Promise.resolve()
    return Promise.reject('must be digits');
}
export const numberValidator = async (rule, value) => {
    if (!value || regular.number.test(value))
        return Promise.resolve()
    return Promise.reject('must be number');
}
export const mobileValidator = async (rule, value) => {
    if (!value || regular.mobile.test(value))
        return Promise.resolve()
    return Promise.reject('must be mobile');
}
export const phoneValidator = async (rule, value) => {
    if (!value || regular.phone.test(value))
        return Promise.resolve()
    return Promise.reject('must be phone');
}
export const telephoneValidator = async (rule, value) => {
    if (!value || regular.mobile.test(value) || regular.phone.test(value))
        return Promise.resolve()
    return Promise.reject('must be phone');
}
export const faxValidator = async (rule, value) => {
    if (!value || regular.phone.test(value))
        return Promise.resolve()
    return Promise.reject('must be fax');
}

export const zipCodeValidator = async (rule, value) => {
    if (!value || regular.zipCode.test(value))
        return Promise.resolve()
    return Promise.reject('must be zipCode');
}

export const emailValidator = async (rule, value) => {
    if (!value || regular.email.test(value))
        return Promise.resolve()
    return Promise.reject('must be email');
}

export const longitudeValidator = async (rule, value) => {
    if (!value || regular.longitude.test(value))
        return Promise.resolve()
    return Promise.reject('must be longitude');
}

export const latitudeValidator = async (rule, value) => {
    if (!value || regular.latitude.test(value))
        return Promise.resolve()
    return Promise.reject('must be latitude');
}
export const passwordValidator = async (rule, value) => {
    if (!value || regular.password.test(value))
        return Promise.resolve()
    return Promise.reject('must be password');
}

export const idCardValidator = async (rule, value) => {
    if (!value || isValidCard(value))
        return Promise.resolve()
    return Promise.reject('must be idCard');
}
export const usernameValidator = async (rule, value) => {
    if (!value || regular.username.test(value))
        return Promise.resolve()
    return Promise.reject('must be username');
}
export const uniqueValidator = (formNo, label, id ,filterObj={queryParamType: {}},message) => {
    return {
        validator: (rule, value) => {

            return new Promise((resolve, reject) => {
                if (!value) {
                    resolve()
                }
                value = removeEmptyStrings(value);
                let postData = {
                    pageParam: {pageNo: 1, pageSize: 1},
                }
                Object.assign(postData, filterObj)
                if (!postData.queryParamType) {
                    postData.queryParamType = {}
                }
                if (id) {
                    postData.id = id
                    postData.queryParamType = {
                        id: '!='
                    }
                }
                if (value) {
                    postData[rule.fullField] = value
                    postData.queryParamType[rule.fullField] = '='
                }
                postAction(defaultTableViewUrl.list.replace('&traceFlag=', '&traceFlag=1').replace('${parentId}', '').replace('${formNo}', formNo), postData).then(res => {
                    if (res.total === 0) {
                        resolve()
                    } else {
                        reject()
                    }
                })

            })
        },
        message: message ? message : label + '已存在'
    }
}

// 批量挂载到 window
Object.assign(window, {
    digitsValidator,
    numberValidator,
    mobileValidator,
    phoneValidator,
    telephoneValidator,
    faxValidator,
    zipCodeValidator,
    emailValidator,
    longitudeValidator,
    latitudeValidator,
    passwordValidator,
    idCardValidator,
    usernameValidator,
    uniqueValidator,
});
