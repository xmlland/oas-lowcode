import {dateFormat} from "@/lib/tools";
import {validDateStr} from "@/lib/tool-date";

const POWER = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
const CITY_CODES = {
    "11": "北京",
    "12": "天津",
    "13": "河北",
    "14": "山西",
    "15": "内蒙古",
    "21": "辽宁",
    "22": "吉林",
    "23": "黑龙江",
    "31": "上海",
    "32": "江苏",
    "33": "浙江",
    "34": "安徽",
    "35": "福建",
    "36": "江西",
    "37": "山东",
    "41": "河南",
    "42": "湖北",
    "43": "湖南",
    "44": "广东",
    "45": "广西",
    "46": "海南",
    "50": "重庆",
    "51": "四川",
    "52": "贵州",
    "53": "云南",
    "54": "西藏",
    "61": "陕西",
    "62": "甘肃",
    "63": "青海",
    "64": "宁夏",
    "65": "新疆",
    "71": "台湾",
    "81": "香港",
    "82": "澳门",
    "83": "台湾",
    "91": "国外"
};

export function convert15To18(idCard) {
    if (idCard.length !== 15) {
        return null;
    } else if (/^\d+$/.test(idCard)) {
        const birthYear = idCard.substring(6, 8);
        let sYear = parseInt('19' + birthYear);
        if (sYear > 2000) {
            sYear -= 100;
        }
        const idCard18 = idCard.substring(0, 6) + sYear + idCard.substring(8);
        const sVal = getCheckCode18(idCard18);
        return idCard18 + sVal;
    } else {
        return null;
    }
}

export function isValidCard(idCard) {
    if (idCard.trim() === "") {
        return false;
    } else {
        let length = idCard.length;
        switch (length) {
            case 15:
                return isValidCard15(idCard);
            case 18:
                return isValidCard18(idCard, false);
            default:
                return false;
        }
    }
}

export function isValidCard18(idCard, ignoreCase = false) {
    if (18 !== idCard.length) {
        return false;
    } else {
        let proCode = idCard.substring(0, 2);
        if (!CITY_CODES[proCode]) {
            return false;
        } else if (!validDateStr(idCard.substring(6, 14))) {
            return false;
        } else {
            let code17 = idCard.substring(0, 17);
            if (code17.match(/^\d+$/)) {
                let val = getCheckCode18(code17);
                return val === idCard.charAt(17) || (ignoreCase && val.toLowerCase() === idCard.charAt(17).toLowerCase());
            } else {
                return false;
            }
        }
    }
}

export function isValidCard15(idCard) {
    if (idCard.length !== 15) {
        return false;
    } else if (/^\d+$/.test(idCard)) {
        let proCode = idCard.substring(0, 2);
        if (CITY_CODES[proCode] === undefined) {
            return false;
        } else {
            return validDateStr("19" + idCard.substring(6, 12));
        }
    } else {
        return false;
    }
}

export function getBirthDateStr(idCard) {
    return dateFormat('yyyy-MM-dd', getBirthDate(idCard))
}

function getBirth(idCard) {
    if (!idCard) {
        throw new Error("id card must not be blank!");
    }

    const len = idCard.length;
    if (len < 15) {
        return null;
    } else {
        if (len === 15) {
            idCard = convert15To18(idCard); // Assuming convert15To18 function is defined
        }

        return idCard.substring(6, 14);
    }
}

export function getBirthDate(idCard) {
    const birthByIdCard = getBirth(idCard);
    if (birthByIdCard === null) {
        return null;
    }

    const year = parseInt(birthByIdCard.substring(0, 4), 10);
    const month = parseInt(birthByIdCard.substring(4, 6), 10) - 1; // 月份从0开始，所以需要减去1
    const day = parseInt(birthByIdCard.substring(6, 8), 10);

    return new Date(year, month, day);
}

export function getAgeByIdCard(idCard, dateToCompare = new Date()) {

    const birthDate = getBirthDate(idCard);
    //根据日期字符串 和 传入的日期对象 计算年龄
    const birthYear = birthDate.getFullYear();
    const currentYear = dateToCompare.getFullYear();

    let age = currentYear - birthYear;

    // Check if the birthday has occurred this year
    const birthMonth = birthDate.getMonth();
    const currentMonth = dateToCompare.getMonth();
    const birthDay = dateToCompare.getDate();
    const currentDay = dateToCompare.getDate();

    if (currentMonth < birthMonth || (currentMonth === birthMonth && currentDay < birthDay)) {
        age--;
    }

    return age;
}

export function getYearByIdCard(idCard) {
    const len = idCard.length;
    if (len < 15) {
        return null;
    } else {
        if (len === 15) {
            idCard = convert15To18(idCard);
        }
        return parseInt(idCard.substring(6, 10));
    }
}

export function getMonthByIdCard(idCard) {
    const len = idCard.length;
    if (len < 15) {
        return null;
    } else {
        if (len === 15) {
            idCard = convert15To18(idCard);
        }
        return parseInt(idCard.substring(10, 12));
    }
}

export function getDayByIdCard(idCard) {
    const len = idCard.length;
    if (len < 15) {
        return null;
    } else {
        if (len === 15) {
            idCard = convert15To18(idCard);
        }
        return parseInt(idCard.substring(12, 14));
    }
}

/**
 *
 * @param idCard
 * @return {null|number} 1: 男性, 0: 女性
 */
export function getGenderByIdCard(idCard) {
    if (!idCard) {
        return null;
    }

    const len = idCard.length;
    if (len < 15) {
        throw new Error("ID Card length must be 15 or 18");
    } else {
        if (len === 15) {
            idCard = convert15To18(idCard);
        }

        const sCardChar = idCard.charAt(16);
        return sCardChar % 2 !== 0 ? 1 : 0;
    }
}

export function getProvinceCodeByIdCard(idCard) {
    const len = idCard.length;
    return (len !== 15 && len !== 18) ? null : idCard.substring(0, 2);
}

export function getProvinceByIdCard(idCard) {
    const code = getProvinceCodeByIdCard(idCard);
    return code ? CITY_CODES[code] : null;
}

export function getCityCodeByIdCard(idCard) {
    const len = idCard.length;
    return (len !== 15 && len !== 18) ? null : idCard.substring(0, 4);
}

export function getDistrictCodeByIdCard(idCard) {
    const len = idCard.length;
    return (len !== 15 && len !== 18) ? null : idCard.substring(0, 6);
}

export function getIdCardInfo(idCard) {
    return new IdCard(idCard);
}

function getCheckCode18(code17) {
    const iSum = getPowerSum(code17.split(''));
    switch (iSum % 11) {
        case 0:
            return '1';
        case 1:
            return '0';
        case 2:
            return 'X';
        case 3:
            return '9';
        case 4:
            return '8';
        case 5:
            return '7';
        case 6:
            return '6';
        case 7:
            return '5';
        case 8:
            return '4';
        case 9:
            return '3';
        case 10:
            return '2';
        default:
            return ' ';
    }
}

function getPowerSum(iArr) {
    let iSum = 0;
    if (POWER.length === iArr.length) {
        for (let i = 0; i < iArr.length; ++i) {
            iSum += parseInt(iArr[i]) * POWER[i];
        }
    }
    return iSum;
}

class IdCard {
    constructor(idCard) {
        this.provinceCode = getProvinceCodeByIdCard(idCard);
        this.cityCode = getCityCodeByIdCard(idCard);
        this.birthDate = getBirthDate(idCard);
        this.gender = getGenderByIdCard(idCard);
        this.age = getAgeByIdCard(idCard);
    }

    getProvinceCode() {
        return this.provinceCode;
    }

    getProvince() {
        return CITY_CODES[this.provinceCode];
    }

    getCityCode() {
        return this.cityCode;
    }

    getBirthDate() {
        return this.birthDate;
    }

    getGender() {
        return this.gender;
    }

    getAge() {
        return this.age;
    }

    toString() {
        return `idCard{provinceCode='${this.provinceCode}', cityCode='${this.cityCode}', birthDate=${this.birthDate}, gender=${this.gender}, age=${this.age}}`;
    }
}
