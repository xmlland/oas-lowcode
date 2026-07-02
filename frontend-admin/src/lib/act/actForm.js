/*[ 显示/隐藏 ] [ 编辑/只读 ] [ 必填/选填 ]*/
export const isFormItemShow = (actRuleArgs, fieldName, defaultValue = true) => {
    if (!actRuleArgs.form || !actRuleArgs.form[fieldName]) {
        return defaultValue
    }

    return actRuleArgs.form[fieldName].split('')[0] === '1'
}

export const isFormItemDisabled = (actRuleArgs, fieldName) => {
    if (!actRuleArgs.form) {
        return false
    }
    if (!actRuleArgs.form[fieldName]) {
        return true
    }
    return !(actRuleArgs.form[fieldName].split('')[1] === '1')
}

export const isFormItemRequire = (actRuleArgs, fieldName) => {
    if (!actRuleArgs.form || !actRuleArgs.form[fieldName]) {
        return false
    }
    return actRuleArgs.form[fieldName].split('')[2] === '1'
}

export const formItemStatus = (actRuleArgs, fieldName) => {
    return {
        formItemShow: isFormItemShow(actRuleArgs, fieldName),
        formItemDisabled: isFormItemDisabled(actRuleArgs, fieldName),
        formItemRequire: isFormItemRequire(actRuleArgs, fieldName),
    }
}
export const actExtendShow = (actRuleArgs) => {
    return formItemStatus(actRuleArgs, 'act.comment').formItemShow || formItemStatus(actRuleArgs, 'act.log').formItemShow
}
/**
 * 从工作流参数中获取显示的tab
 */
export const setSubTableFromActRuleArgs = (actRuleArgs) => {
    return new Promise((resolve) => {
        let tabs = [], actions = []
        let formExtend = actRuleArgs.formExtend
        if (formExtend) {
            for (let key in formExtend) {
                if (key.indexOf('act.tab') >= 0 && isFormItemShow(actRuleArgs,key)) {
                    let value = formExtend[key]
                    tabs.push({
                        key: key,
                        value: value,
                        title: value.split('|')[0],
                    })
                    actions.push(import(`@/views/${value.split('|')[1]}.vue`))
                }
            }
            if (tabs.length > 0) {
                let arr = []
                Promise.all(actions).then((res) => {
                    res.forEach((item, index) => {
                        arr.push({
                            title: tabs[index].title,
                            key: tabs[index].key,
                            component: item.default,
                            tabDisabled: isFormItemDisabled(actRuleArgs, tabs[index].key),
                        })
                    })
                    resolve(arr)
                })
            } else {
                resolve([])
            }
        } else {
            resolve([])
        }

    })

}
