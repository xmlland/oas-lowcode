const isEmptyQueryValue = (value) => {
    if (value === undefined || value === null || value === '') {
        return true
    }
    if (Array.isArray(value)) {
        return value.length === 0
    }
    if (Object.prototype.toString.call(value) === '[object Object]') {
        const keys = Object.keys(value)
        if (keys.length === 0) {
            return true
        }
        if (Object.prototype.hasOwnProperty.call(value, 'id') && (value.id === undefined || value.id === null || value.id === '')) {
            return true
        }
        if (Object.prototype.hasOwnProperty.call(value, 'value') && (value.value === undefined || value.value === null || value.value === '')) {
            return true
        }
        return keys.every(key => value[key] === undefined || value[key] === null || value[key] === '')
    }
    return false
}

export const mergeQueryData = (_queryData={}, _initParam={}) => {
    let queryData = {}, initParam = {};
    Object.assign(queryData, _queryData)
    Object.assign(initParam, _initParam)
    let tempData = {};
    let data = {};
    for (let key in queryData) {
        if (!isEmptyQueryValue(queryData[key])) {
            tempData[key] = queryData[key]
        }
    }
    if (initParam.queryParamType && tempData.queryParamType) {
        Object.assign(tempData.queryParamType, initParam.queryParamType)
        delete initParam.queryParamType
    }
    Object.assign(data, initParam, tempData)
    return data
}
