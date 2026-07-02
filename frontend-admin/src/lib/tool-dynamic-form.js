import {groupBy} from "@/lib/tools";

/**
 * 计算表达式  type支持 eq ne
 * @param arr 表达式数组 [{fieldName: 'name', type: 'eq', value: '1', group: 1}, {fieldName: 'name', type: 'eq', value: '1', group: 1}]
 * @param formData 表单数据
 * @return {boolean} 返回表达式计算结果
 */
export const calcPredicate = (arr, formData) => {
    let groupArr = groupBy(arr, (item) => item.group);
    let result = true
    const calcResult = (item) => {
        if (item.type === 'eq') {
            //等于
            return formData[item.fieldName] === item.value
        } else if (item.type === 'ne') {
            //不等于
            return formData[item.fieldName] !== item.value
        }
    }

    groupArr.forEach(group => {
        let groupResult = true
        group.children.forEach((item, index) => {
            //如果是第一个条件或者是and条件
            if (!item.condition || item.condition === 'and' || index === 0) {
                groupResult = groupResult && calcResult(item)
            } else if (item.condition === 'or') {
                groupResult = calcResult(item) || groupResult
            }
        })
        result = result && groupResult
    })
    return result
}
