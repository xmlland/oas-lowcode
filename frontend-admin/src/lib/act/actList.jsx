import {Modal, Textarea} from 'ant-design-vue';
import {confirmAction, getAction, postAction} from "@/api/action";
import USelect from "@/components/form/USelect";
import {confirm, hasAnyPermission, prompt} from "@/lib/tools";
import {batchSaveSelectAction} from "@/api/api";
import {useStore} from "vuex";

/**
 * 根据当前办理的行数据获取按钮
 * @param row
 * @param newRowBtrArr 新增工作流数据的按钮
 * @returns {{extend: *, text: *, disableShow: boolean, type: string, value: *, validate}[]}
 */
export const getActButtons = (row, newRowBtrArr) => {
    // region 增加挂起与激活
    const store = useStore();
    let nowRowIsSuspend = store.getters.getNowRowIsSuspend
    if (nowRowIsSuspend) {
        return [
            {"button": "激活", "button_EN": "激活", "type": "saveAndSuspend", "flag": "saveAndSuspend"},
        ].map(item => {
            return {
                value: item.type,
                text: item.button === '取消' ? '关闭' : item.button,
                disableShow: true,
                validate: item.type === "saveAndStart" || item.type === "saveAndComplete",
                type: 'primary',
                extend: item
            }
        })

    }
    // endregion 增加挂起与激活
    let result = []
    if (row && row.act) {
        let paramStr = [];
        if (row.disabled) {
            paramStr = [
                {"button": "关闭", "button_EN": "Cancel", "type": "cancel", "flag": "cancel"},
            ]
        } else if (row.act.procInsId == null || row.act.procInsId === "") {
            paramStr = newRowBtrArr || [
                {"button": "关闭", "button_EN": "Cancel", "type": "cancel", "flag": "cancel"},
                {"button": "提交", "button_EN": "Submit", "type": "saveAndStart", "flag": "saveAndStart"},
                {"button": "暂存", "button_EN": "Temporary storage", "type": "save", "flag": "save"},
            ];
        } else if (row.act.procTaskPermission == null || row.act.procTaskPermission.operation == null) {
            paramStr = [
                {"button": "关闭", "button_EN": "Cancel", "type": "cancel", "flag": "cancel"},
                {"button": "提交", "button_EN": "Submit", "type": "saveAndComplete", "flag": "saveAndComplete"},
                {"button": "暂存", "button_EN": "Temporary storage", "type": "saveAndClaim", "flag": "saveAndClaim"},
            ];
        } else {
            let btns = row.act.procTaskPermission.operation.split(',');
            for (var i = 0; i < btns.length; i++) {
                paramStr.push({
                    "button": btns[i].split('_')[0],
                    "type": btns[i].split('_')[1],
                    "flag": btns[i].split('_')[1],
                });
            }
            result = paramStr;
        }
        result = paramStr;
    } else {
        result = newRowBtrArr || [
            {"button": "关闭", "button_EN": "Cancel", "type": "cancel", "flag": "cancel"},
            {"button": "提交", "button_EN": "Submit", "type": "saveAndStart", "flag": "saveAndStart"},
            {"button": "暂存", "button_EN": "Temporary storage", "type": "save", "flag": "save"},
        ]
    }

    // region 根据 permissionBoundActButtons 中 绑定 的 权限 对应 工作流按钮 来 去除 相关按钮
    let actButtonsBoundPermission = store.getters.getActButtonsBoundPermission
    if (actButtonsBoundPermission) {
        result = result.filter(i => {
            if (actButtonsBoundPermission[i.flag]){
                return hasAnyPermission([actButtonsBoundPermission[i.flag]]);
            }else {
                return true;
            }
        });
    }
    // endregion

    return result.map(item => {
        return {
            value: item.type,
            text: item.button === '取消' ? '关闭' : item.button,
            disableShow: true,
            validate: item.type === "saveAndStart" || item.type === "saveAndComplete",
            type: (item.type === "saveAndStart" || item.type === "saveAndComplete") ? 'primary' : '',
            extend: item
        }
    })
}

/**
 * @param data 需要提交到后台的数据
 * @param hand 需要选择的分支
 * @param successResolve 成功回调
 */
const buildMultiPathBox = (data, hand, successResolve) => {
    let flag = ''
    let optionData = []
    for (let key in hand) {
        optionData.push({
            value: key,
            text: hand[key]
        })
    }

    let render = (
        <div>
            <USelect multiple={true} placeholder="请选择流程分支" style="width:100%" form-type="select" optionData={optionData} valueField="value" textField="text" onUpdate:value={(e) => {
                flag = e
            }}>
            </USelect>
        </div>
    )
    prompt({
        title: '请选择流程分支!',
        width: 800,
        emptyMessage: '请选择流程分支!',
        getValue() {
            if (flag) {
                return flag
            } else {
                return ''
            }
        },
        render: render,
        callback: (value) => {
            data.act.flag = value
            successResolve(data)
            //callback(postAction, saveDataUrl, data)
        }
    })
}

/**
 * 提交时选择用户
 * @param actSelectUserModal
 * @param data
 * @param hasSlot 是否有评论插槽
 * @param actCommentLabel 评论文本框标签
 * @param actCommentVisible 评论文本框是否显示
 * @param rowData 行数据
 * @param actSubmitText 提交前的确认文本
 * @returns {Promise<unknown>}
 */
export const handleSelectUser = (actSelectUserModal, data, {hasSlot, actCommentLabel,actCommentVisible}, rowData = {},actSubmitText = '是否提交？') => {
    return new Promise((resolve) => {
        let ra = data.actRuleArgs
        if (ra&&ra.hand&&Object.keys(ra.hand).length === 1) {
            rowData.act.flag = Object.keys(ra.hand)[0]
        }
        postAction('dynamic/zform/getUserList', rowData).then(userRes => {
            if (userRes.data != null && userRes.data.queryDataInJava) {
                Object.assign(data, userRes.data.queryDataInJava)
            }
            if (userRes.data != null && (userRes.data.isNeedUserList === false || !actCommentVisible)) {

                if (actCommentVisible){
                    //单选
                    actSelectUserModal.open(userRes.data, false, {hasSlot, actCommentLabel}, data.actRuleArgs, rowData).then(selectRes => {
                        for (let key in selectRes) {
                            if (key === 'actComment') {
                                data.act.comment = selectRes[key]
                            } else {
                                data[key] = selectRes[key]
                            }
                        }
                        resolve(data)
                    })
                    return
                }
                //不需要选择用户
                confirm({
                    title: '提示',
                    content: userRes.data.isEnd ? '是否办结？' : actSubmitText,
                    okText: '确定',
                    cancelText: '取消',
                    onOk: () => {
                        if (userRes.data.isNeedUserList !== false){
                            if (userRes.data.type === 'single' && userRes.data.userList.length === 1) {
                                data.tempLoginName = [userRes.data.userList[0].loginName]
                            }else{
                                Modal.warning({
                                    title: '提示',
                                    content: '流程配置异常，请联系管理员！'
                                })
                                return
                            }
                        }
                        resolve(data)
                    }
                })

                //resolve(data)
            } else {

                if (userRes.data != null && userRes.data.isNeedFlag != null && userRes.data.isNeedFlag) {
                    if (ra == null || ra.hand == null) {
                        //没有找到符合条件的分支
                        Modal.warning({
                            content: '无法提交，没有找到符合条件的分支'
                        })
                    } else {
                        if (Object.keys(ra.hand).length === 1) {
                            /*$.each(ra.hand, function(key, value) {
                                _this.$layero.find("#flag").val(key);
                            });
                            _this.getUserList();*/
                            //TODO 流程分支
                        } else if (ra.extend.branchType === "multi") {
                            //多人分支
                            buildMultiPathBox(data, ra.hand, resolve)
                            //_this.buildMultiPathBox(_this.ra.hand,_this.$layero);
                        } else {
                            //TODO 流程分支
                            //单人分支
                            //_this.buildSinglePathBox(_this.ra.hand,_this.$layero);
                        }
                    }
                    return false
                }
                if (userRes.data == null || userRes.data.userList == null || userRes.data.userList.length === 0) {
                    Modal.warning({
                        content: '无法提交，没有找到符合条件的用户'
                    })
                    return false;
                }
                //data.data.type==single,单选
                else if (userRes.data.type === "single") {
                    //单选
                    actSelectUserModal.open(userRes.data, false, {hasSlot, actCommentLabel}, data.actRuleArgs, rowData).then(selectRes => {
                        for (let key in selectRes) {
                            if (key === 'actComment') {
                                data.act.comment = selectRes[key]
                            } else {
                                data[key] = selectRes[key]
                            }
                        }
                        resolve(data)
                    })
                }
                //data.data.type==multi,多选
                else if (userRes.data.type === "multi") {
                    //多选
                    actSelectUserModal.open(userRes.data, true, {hasSlot, actCommentLabel}, data.actRuleArgs, rowData).then(selectRes => {
                        for (let key in selectRes) {
                            if (key === 'actComment') {
                                data.act.comment = selectRes[key]
                            } else {
                                data[key] = selectRes[key]
                            }
                        }
                        resolve(data)
                    })
                }

            }
        })

    })
}

/**
 * 退回至上一节点
 * @param data
 * @param config beforeSolve: Promise Function({...})
 * @returns {Promise<unknown>}
 */
export const handleRollBack = (data,config={parts:[],beforeSolve:undefined}) => {
    return new Promise((resolve) => {
        getAction('dynamic/zform/rollBackCheck', {procInsId: data.act.procInsId}).then(res => {
            if (res.data.success && res.data.prevNode) {
                let render = null, getValue = null
                let returnReason = null
                let returnUser = ''
                let emptyMessage = '请输入退回原因'
                let choosePart = undefined;
                let returnChoosePart = undefined;
                if (res.data.doneAssignList.length> 1) {
                    emptyMessage = '请选择退回人员，输入退回原因'
                    getValue = () => {
                        if (returnReason&&returnUser){
                            return {returnReason,returnUser}
                        }else{
                            return false
                        }
                    }
                    let user = res.data.doneAssignList.map(item => item.loginName).join(',')
                    returnUser = user
                    let partsSlot = (<div></div>)
                    if(config.parts.length>0){
                        choosePart = config.parts.map(item => item.value).join(',');
                        returnChoosePart = choosePart;
                        partsSlot = (
                          <div style="display:flex">
                              退回内容：
                              <USelect style="flex:1" multiple={true} form-type="checkbox" optionData={config.parts} valueField="value" textField="name" value={choosePart} onUpdate:value={(e) => {
                                  returnChoosePart = e;
                              }} >
                              </USelect>
                          </div>
                        )
                    }
                    render = (
                        <div>
                            <div style="display:flex">
                                退回用户：
                                <USelect style="flex:1" multiple={true} form-type="checkbox" optionData={res.data.doneAssignList} valueField="loginName" textField="name" value={user} onUpdate:value={(e) => {
                                    returnUser = e
                                }} >
                                </USelect>
                            </div>
                            <partsSlot/>
                            <Textarea placeholder="请输入退回原因" allowClear={true} autoSize={{minRows: 5, maxRows: 5}} maxlength={200} onUpdate:value={(e) => {
                                returnReason = e
                            }}>
                            </Textarea>
                        </div>
                    )
                }

                prompt({
                    title: `确定退回至【${res.data.prevNode.name}】吗？`,
                    render: render ,
                    getValue:getValue,
                    maxlength: 500,
                    placeholder: '请输入退回原因',
                    emptyMessage: emptyMessage,
                    callback: (value) => {
                        if (value.returnReason){
                            data.act.comment = value.returnReason
                            data.tempLoginName = value.returnUser.split(',')
                        }else{
                            data.act.comment = value
                        }
                        if((typeof config.beforeSolve)==='function'){
                            config.beforeSolve({
                                data,
                                returnReason: value.returnReason,
                                callBackValue: value,
                                choosePart,
                                returnChoosePart,
                            }).then(()=>{
                                resolve(data);
                            });
                        }else {
                            resolve(data)
                        }
                    }
                })
            }
        })
    })
}

/**
 * 指定节点退回
 */
export const handleSuperReject = (data) => {
    return new Promise((resolve) => {
        postAction('dynamic/zform/getNodeList', {procInsId: data.act.procInsId,act: data.act}).then(res => {
            let temp_node = ''
            let returnReason = null
            let render = (
                <div>
                    <USelect placeholder="请选择退回节点" style="width:100%" form-type="select" optionData={res.data.nodeList} valueField="taskDefinitionKey" textField="name" onUpdate:value={(e) => {
                        temp_node = e
                    }} >
                    </USelect>
                    <Textarea placeholder="请输入退回原因" allowClear={true} autoSize={{minRows: 5, maxRows: 5}} maxlength={200} onUpdate:value={(e) => {
                        returnReason = e
                    }}>
                    </Textarea>
                </div>
            )
            prompt({
                title: '请选择退回节点!',
                emptyMessage: '请选择退回节点，输入退回原因!',
                getValue() {
                    if (returnReason&&temp_node){
                        return {returnReason,temp_node}
                    }else{
                        return false
                    }
                },
                render: render,
                callback: (value) => {
                    data.tempNodeKey = value.temp_node
                    data.act.comment = value.returnReason
                    resolve(data)
                    //callback(postAction, saveDataUrl, data)
                }
            })
        })
    })
}

/**
 * 加签
 */
export const handleCreateNode = (actCreateNodeModal, data) => {

    return new Promise((resolve) => {
        actCreateNodeModal.open().then(selectRes => {
            selectRes.act = {
                flag: data.act.flag,
                procDefKey: data.act.procDefKey,
            }
            selectRes.id = data.id
            selectRes.procInsId = data.act.procInsId
            postAction('dynamic/zform/createNode', selectRes).then(res => {

                resolve(res)
            })

        })
    })
}
/**
 * 减签
 */
export const handleDeleteNode = (data) => {
    return new Promise(resolve => {
        let deleteNode = {}
        deleteNode.act = {
            flag: data.act.flag,
            procDefKey: data.act.procDefKey,
        }
        deleteNode.id = data.id
        deleteNode.procInsId = data.act.procInsId
        confirmAction('操作确认', '确定减签吗', 'dynamic/zform/deleteNode', deleteNode, (res) => {
            if (!res.data.success) {
                Modal.warning({
                    content: res.data.message
                })
            } else {
                resolve(res)
            }

        })
    })
}
/**
 * 知会
 */
export const handleNotify = (actCreateNodeModal, data) => {
    return new Promise((resolve) => {
        actCreateNodeModal.open('知会', false).then(selectRes => {
            selectRes.act = {
                flag: data.act.flag,
                procDefKey: data.act.procDefKey,
            }
            selectRes.id = data.id
            selectRes.procInsId = data.act.procInsId
            postAction('dynamic/zform/notifyNode', selectRes).then(res => {
                if (!res.data.success) {
                    Modal.warning({
                        content: res.data.message
                    })
                } else {
                    resolve(res)
                }
            })

        })
    })
}
/**
 * 分发
 */
export const handleDistribute = (actCreateNodeModal, data) => {
    return new Promise((resolve) => {
        actCreateNodeModal.open('分发', false).then(selectRes => {
            selectRes.act = {
                flag: data.act.flag,
                procDefKey: data.act.procDefKey,
            }
            selectRes.id = data.id
            selectRes.procInsId = data.act.procInsId
            postAction('dynamic/zform/distributeNode', selectRes).then(res => {
                if (!res.data.success) {
                    Modal.warning({
                        content: res.data.message
                    })
                } else {
                    resolve(res)
                }
            })

        })
    })
}

/**
 * 构造工作流表单普通保存对象
 * @param formNo
 * @param id
 * @return {{act: {param: string}, formNo, id}}
 */
export const buildTempSaveActData = (formNo, id) => {
    return {
        id: id,
        formNo: formNo,
        act: {
            param: JSON.stringify({"button": "暂存", "button_EN": "Temporary storage", "type": "save", "flag": "save"})
        }
    }
}

/**
 * 构造工作流表单提交对象
 * @param formNo
 * @param id
 * @return {{act: {param: string}, formNo, id}}
 */
export const buildSubmitActData = (formNo, id) => {
    return {
        id: id,
        formNo: formNo,
        act: {
            param: JSON.stringify({"button": "提交", "button_EN": "Submit", "type": "saveAndComplete", "flag": "saveAndComplete"})
        }
    }
}

/**
 * 构造工作流退回提交对象
 * @param formNo
 * @param id
 * @return {{act: {param: string}, formNo, id}}
 */
export const buildReturnActData = (formNo, id) => {
    return {
        id: id,
        formNo: formNo,
        act: {
            param: JSON.stringify({"button": "退回", "button_EN": "Reject", "type": "saveAndReject", "flag": "saveAndReject"})
        }
    }
}

/**
 * 批量提交工作量数据
 * @param formNo
 * @param rows
 * @param actSelectUserRef 选择用户组件
 */
export const batchSubmitActData = (formNo, rows, actSelectUserRef) => {

    let submitData = []
    rows.forEach(row => {
        let submitActData = buildSubmitActData(formNo, row.id);
        let act = {}
        Object.assign(act, row.act, submitActData.act)
        submitActData.act = act
        submitData.push(submitActData)
    })
    return new Promise((resolve,reject) => {
        if (rows.length === 0) {
            Modal.warning({
                content: '请选择要提交的数据！'
            })
            reject()
        }else{
            getAction(`dynamic/zform/getZformWithActMap?formNo=${formNo}&id=${rows[0].id}&procDefKey=${rows[0].proc_def_key}`, {}).then(actRes=>{
                let currentProcRow = actRes.data.data
                rows[0].actRuleArgs= currentProcRow.ruleArgs
                postAction('dynamic/zform/getUserList', rows[0]).then(userRes => {
                    if (!userRes.data.isNeedUserList || userRes.data.userList.length === 1) {
                        let firstRow = {}
                        Object.assign(firstRow, rows[0])
                        //选择用户并且使用 act.submit.form
                        actSelectUserRef.open(userRes.data, false, {hasSlot:false, actCommentLabel:'审批意见'}, actRes.data.data.ruleArgs, firstRow).then(selectRes => {
                            let dataArr = []
                            submitData.forEach(item => {
                                let itemNew = {}
                                Object.assign(itemNew, item)
                                for (let key in selectRes) {
                                    if (key === 'actComment') {
                                        itemNew.act.comment = selectRes[key]
                                    } else {
                                        itemNew[key] = selectRes[key]
                                    }
                                }
                                dataArr.push(itemNew)
                            })
                            batchSaveSelectAction(formNo, dataArr).then(() => {
                                resolve()
                            })
                        })
                        /*console.log(actRes)
                        let content = `是否提交选中的${rows.length}条记录？`
                        if (userRes.data.isNeedUserList && userRes.data.userList.length === 1) {
                            submitData.forEach(item => {
                                item.tempLoginName = [userRes.data.userList[0].loginName]
                            })
                            if (userRes.data.nextNode) {
                                content = `是否提交选中的${rows.length}条记录至【${userRes.data.nextNode.name}】？`
                            }
                        }
                        if (userRes.data.isEnd) {
                            content = `是否办结选中的${rows.length}条记录？`
                        }
                        prompt({
                            title: content,
                            maxlength: 100,
                            placeholder: '请输入审核意见',
                            emptyMessage: '请输入审核意见',
                            callback: (value) => {
                                submitData.forEach(item => {
                                    item.act.comment = value
                                })
                                batchSaveSelectAction(formNo, submitData).then(() => {
                                    resolve()
                                })
                            }
                        })*/
                        /*confirmAction('操作确认', content, batchSaveSelectUrl + '?formNo=' + formNo, submitData, () => {
                            resolve()
                        })*/
                    }else{
                        Modal.warning({
                            content: '选中数据不支持批量提交！'
                        })
                        reject()
                    }

                })
            })
        }
    })
}

/**
 * 批量退回工作量数据
 * @param formNo
 * @param rows
 */
export const batchReturnActData = (formNo, rows) => {
    let returnData = []
    rows.forEach(row => {
        let returnActData = buildReturnActData(formNo, row.id);
        let act = {}
        Object.assign(act, row.act, returnActData.act)
        returnActData.act = act
        returnData.push(returnActData)
    })
    return new Promise((resolve,reject) => {
        if (rows.length === 0) {
            Modal.warning({
                content: '请选择要退回的数据！'
            })
            reject()

        }else{
            prompt({
                title: `是否退回选中的${rows.length}条记录？`,
                maxlength: 100,
                placeholder: '请输入退回原因',
                emptyMessage: '请输入退回原因',
                callback: (value) => {
                    returnData.forEach(item => {
                        item.act.comment = value
                    })
                    batchSaveSelectAction(formNo, returnData).then(() => {
                        resolve()
                    })
                }
            })
        }
        /*confirmAction('操作确认', `是否退回选中的${rows.length}条记录？`, batchSaveSelectUrl + '?formNo=' + formNo, returnData, () => {
            resolve()
        })*/
    })
}
