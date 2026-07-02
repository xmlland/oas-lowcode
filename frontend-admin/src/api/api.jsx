import {baseUrl, exportFile, getAction, postAction, postActionByParams, postActionShowLoading} from "@/api/action";
import {Base64} from 'js-base64';
import {Sm2Encrypt} from "@/lib/tool-security";
import {getStorage, setStorage} from "@/lib/storage";
import config from "@/config";
import {getWebUrl,confirm} from "@/lib/tools";
import router from "@/router";
import {Modal, message} from "ant-design-vue";
import {getZIndex} from "@/lib/zIndexUtil";

// OnlyOffice 支持的格式
const onlyOfficeExts = ['doc','docx','xls','xlsx','ppt','pptx','pdf','rtf','txt']
// 图片格式
const imageExts = ['jpg','jpeg','png','gif','bmp','webp']
// 视频格式
const videoExts = ['mp4','mov','avi','webm']
// 音频格式
const audioExts = ['mp3','wav','ogg']

function getFileExt(fileId) {
  // fileId 格式为 "id.ext"，提取扩展名
  const dotIndex = fileId.lastIndexOf('.')
  return dotIndex > -1 ? fileId.substring(dotIndex + 1).toLowerCase() : ''
}

export const createCharacterUrl = baseUrl + 'createCharacter'
const loginUrl = 'login'
const logoutUrl = 'logout'
const getCurrentUserViewUrl = 'sys/user/getCurrentUserView'
const getMenuListUrl = 'sys/menu/getMenuListHt'
const getDictUrl = 'sys/dict/getDictListView2'
const getAreaDataUrl = 'sys/tagTree/areaSubTree'
const getUserDataUrl = 'sys/tagTree/userTree'
const getTreeDataUrl = 'dynamic/zform/treeData'
export const uploadUrl = 'system/sysFile/fileUploadBatchProgress'
const getFileListUrl = 'system/sysFile/getFileList'
export const fileDownloadUrl = 'system/sysFile/fileDownload?fileId='
export const fileDownloadPathUrl = 'system/sysFile/fileDownloadByPath/'
export const deleteFileUrl = 'system/sysFile/deleteFile'
const menuTreeUrl = 'sys/tagTree/menuTree'
export const getDataUrl = 'dynamic/zform/getZformMap'
export const saveDataUrl = 'dynamic/zform/savemap'
export const batchSaveSelectUrl = 'dynamic/zform/batchSaveSelect'
const geocoderQueryUrl = '/system/geocoder/query'
const calcWeekdayUrl = '/admin/sysWeekday/calcDate'
const shorCurUrl = '/oa/shortcut/getMyShortcut'
export const deleteUrl = 'dynamic/zform/delete'
export const deleteBatchUrl = 'dynamic/zform/deleteBatch'
export const defaultTableViewUrl = {
    list: 'dynamic/zform/datamap?path=path&traceFlag=&formNo=${formNo}&parentId=${parentId}',
    delete: deleteUrl+'?formNo=${formNo}',
    deleteBatch: deleteBatchUrl+'?formNo=${formNo}',
    template: 'system/sysFile/fileDownloadTemplate?formNo=${formNo}',
    import:'dynamic/zform/impdata?uniqueId=&toCompany=&parentId=',
    export: 'dynamic/zform/expdata?path=path&traceFlag=&formNo=${formNo}&parentId=${parentId}',
}
/**
 * 获取登录用临时SM2公钥
 * @returns {Promise<unknown>}
 */
export const getLoginPublicKey = () => {
    return getAction('getLoginPublicKey')
}
/**
 * 登录请求（SM2非对称加密）
 * @param loginData 原始登录数据对象
 * @returns {Promise<unknown>}
 */
export const loginAction = async (loginData) => {
    const keyRes = await getLoginPublicKey()
    const keyId = keyRes.keyId || (keyRes.data && keyRes.data.keyId)
    const publicKey = keyRes.publicKey || (keyRes.data && keyRes.data.publicKey)

    const str = Base64.encode(JSON.stringify(loginData))
    const randomStr = Sm2Encrypt(str, publicKey)

    return postActionByParams(loginUrl, {randomStr: randomStr, loginKeyId: keyId})
}
/**
 * 退出请求
 * @returns {Promise<unknown>}
 */
export const logoutAction = (needConfirm=true) => {
    return new Promise((resolve) => {
        if (needConfirm){
            confirm({
                title: '提示',
                content: '确定退出该系统吗 ?',
                onOk: () => {
                    getAction(logoutUrl, {}).then(res=>{
                        resolve(res)
                    })
                }
            })
        }else {
            getAction(logoutUrl, {}).then(res=>{
                resolve(res)
            })
        }

    })
}
/**
 * 获取当前登录用户信息
 * @returns {Promise<unknown>}
 */
export const getCurrentUserAction = () => {
    return new Promise(resolve => {
        //调用前先获取加密配置
        getAction('sys/user/getEncryptConfig', {}).then(() => {
            getAction(getCurrentUserViewUrl, {}).then(res => {
                resolve(res)
                getDictAction('').then(dictRes => {
                    dictRes.data.data.forEach(item => {
                        if (!item.hasChildren) {
                            setStorage('Dict_' + item.parentType + item.member, item.memberName)
                        }
                    })
                })
            })
        })
    })
}
/**
 * 获取当前登录用户菜单信息
 * @returns {Promise<unknown>}
 */
export const getMenuListAction = (subSystemCode='') => {
    return getAction(getMenuListUrl, {subSystemCode})
}

// 用于存储正在进行获取字典的请求
const requestQueue = new Map();

/**
 * 获取字典请求
 * @param types
 * @returns {Promise<unknown>}
 */
export const getDictAction = (types) => {
    let storageData = getStorage(types);

    // 如果有缓存数据，直接返回 Promise 解析的数据
    if (storageData) {
        return Promise.resolve(JSON.parse(storageData));
    }

    // 如果当前类型已经有正在进行的请求，返回同一个 Promise
    if (requestQueue.has(types)) {
        return requestQueue.get(types);
    }

    // 新建一个 Promise 用于处理请求，并加入队列
    const requestPromise = new Promise((resolve, reject) => {
        async function query() {
            let isEdit = true;
            if (types === '') {
                isEdit = false;
            }

            try {
                const res = await getAction(getDictUrl, { types, isEdit });
                setStorage(types, res); // 缓存结果
                resolve(res);
            } catch (err) {
                reject(err);
            } finally {
                // 请求完成后，从队列中移除
                requestQueue.delete(types);
            }
        }

        query();
    });

    // 将 Promise 添加到队列
    requestQueue.set(types, requestPromise);

    return requestPromise;
};

/**
 * 获取行政区
 */
export const getAreaDataAction = (id) => {
    let areaData = 'areaData' + id
    let storageData = getStorage(areaData);
    if (storageData) {
        return new Promise((resolve) => {
            resolve(JSON.parse(storageData))
        })
    }

    return new Promise((resolve, reject) => {
        async function query() {
            await postActionByParams(getAreaDataUrl, {id}).then(res => {
                setStorage(areaData, res)
                resolve(res)
            }).catch(err => {
                reject(err)
            })
        }

        query()
    })
}

/**
 * 获取用户数据
 * @return {Promise<unknown>}
 */
export const getUserDataAction = (filterList=[]) => {
    return postAction(getUserDataUrl, {filterList})
}

export const getTreeDataAction = (parentId = 0, formNo = '') => {
    let traceFlag = 1;
    return postActionByParams(getTreeDataUrl, {parentId, formNo, traceFlag})
}

export const getMenuTreeAction = () => {
    return getAction(menuTreeUrl, {})
}

export const getFileListAction = (groupId) => {

    return postActionByParams(getFileListUrl, {groupId})
}
/**
 * 查询数据
 * @param formNo 表单编号
 * @param param 参数
 * @param parentId 父级id
 * @param queryParam 查询参数 拼接在url上
 * @return {Promise<unknown>}
 */
export const listDataAction = (formNo, param = {}, parentId = '',queryParam={}) => {
    if (!param.pageParam) {
        param.pageParam =  {pageNo: 1, pageSize: 10000}
    }
    let queryUrl = '';
    for (let key in queryParam) {
        queryUrl += `&${key}=${queryParam[key]}`
    }
    return postAction(`${defaultTableViewUrl.list.replace('${formNo}', formNo).replace('${parentId}', parentId)}` + queryUrl, param)
}

export const batchSaveSelectAction = (formNo, array, saveUrl = batchSaveSelectUrl) => {
    const url = `${saveUrl}${saveUrl.includes('?') ? '&' : '?'}formNo=${formNo}`;
    return postActionShowLoading(url, array);
}

export const saveDataAction = (formNo,data) => {
    data.formNo = formNo
    return postAction(saveDataUrl, data)
}

export const downLoadFileAction = (fileId) => {
    return exportFile(`${fileDownloadUrl}${fileId}`)
}
/**
 * 弹窗预览文件
 * @param file
 * @param title
 * @param groupId
 */
export const previewFileByDialog = async (file,title='预览',groupId) => {
    let style = {border: 0, width: '100%', height: (document.body.clientHeight * 0.9 - 118) + 'px'}
    let ext = (file.ext || '').toLowerCase()
    let fileId = file.id + '.' + file.ext

    // groupId 模式：走 pdfPreview 页面
    if (groupId){
        let routerUrl = router.resolve({
            path: '/pdfPreview',
            query: {gId: groupId}
        })
        let src = getWebUrl() + routerUrl.href
        Modal.info({
            appContext: window.$instance.appContext,
            wrapClassName: 'global-event-modal-wrap',
            title: title,
            width: 1200,
            zIndex: getZIndex(),
            closable: true,
            okText: '下载',
            onOk: () => {
                getFileListAction(groupId).then(res => {
                    let files = res.data.fileListMap.files;
                    if (files.length === 1){
                        downLoadFileAction(files[0].id)
                    }else{
                        exportFile(`system/sysFile/fileDownloadZip?groupId=${groupId}`)
                    }
                })
            },
            content: (
                <iframe style={style} src={src}/>
            )
        })
        return
    }

    let content = null

    if (onlyOfficeExts.includes(ext)) {
        // Office/PDF 格式 → OnlyOffice 预览
        let src = getWebUrl() + '#/onlineEditView?fileId=' + file.id + '&file_name=' + encodeURIComponent(file.name) + '&mode=view&oss='
        content = <iframe style={style} src={src}/>
    } else if (imageExts.includes(ext)) {
        // 图片格式 → 预签名URL直接访问
        const presignedUrl = await getPresignedFileUrl(fileId)
        content = <img style={{maxWidth: '100%', maxHeight: style.height}} src={presignedUrl} alt={file.name}/>
    } else if (videoExts.includes(ext)) {
        // 视频格式 → 预签名URL直接访问
        const presignedUrl = await getPresignedFileUrl(fileId)
        content = <video style={style} src={presignedUrl} controls autoplay/>
    } else if (audioExts.includes(ext)) {
        // 音频格式 → 预签名URL直接访问
        const presignedUrl = await getPresignedFileUrl(fileId)
        content = <audio style={{width: '100%'}} src={presignedUrl} controls autoplay/>
    } else {
        message.warning('该文件格式不支持在线预览')
        return
    }

    Modal.info({
        appContext: window.$instance.appContext,
        wrapClassName: 'global-event-modal-wrap',
        title: title,
        width: 1200,
        zIndex: getZIndex(),
        closable: true,
        okText: '下载',
        onOk: () => {
            downLoadFileAction(file.id)
        },
        content: content
    })
}
/**
 * 弹窗预览文件
 * @param fileArr
 * @param title
 */
export const previewFileArrByDialog = (fileArr,title='预览') => {
    let style = {border: 0, width: '100%', height: (document.body.clientHeight * 0.9 - 118) + 'px'}
    let routerUrl = router.resolve({
        path: '/pdfPreview',
        query: {
            fIds: fileArr.map(item => item.id).join(','),
            fNames: fileArr.map(item => item.name).join(','),
        }
    })
    let src = getWebUrl() + routerUrl.href
    Modal.info({
        appContext: window.$instance.appContext,
        wrapClassName: 'global-event-modal-wrap',
        title: title,
        width: 1200,
        zIndex: getZIndex(),
        closable: true,
        okText: '关闭',
        content: (
            <iframe style={style} src={src}/>
        )
    })
}
export const previewFile = async (fileId) => {
    const ext = getFileExt(fileId)
    if (onlyOfficeExts.includes(ext)) {
        // Office/PDF 格式 → OnlyOffice 预览
        const id = fileId.substring(0, fileId.lastIndexOf('.'))
        window.open(getWebUrl() + '#/onlineEditView?fileId=' + id + '&file_name=' + encodeURIComponent(fileId) + '&mode=view&oss=')
    } else if (imageExts.includes(ext) || videoExts.includes(ext) || audioExts.includes(ext)) {
        // 图片/音视频 → 预签名URL直接访问
        const presignedUrl = await getPresignedFileUrl(fileId)
        window.open(presignedUrl)
    } else {
        message.warning('该文件格式不支持在线预览')
    }
}

export const previewFileByGroupId = (groupId) => {
    let routerUrl = router.resolve({
        path: '/pdfPreview',
        query: {gId: groupId}
    })
    window.open(getWebUrl() + routerUrl.href)
}

export const getFilePreviewUrl = (fileId) =>{
    return `${window['fileHost']?window['fileHost']:getWebUrl()}${baseUrl}${fileDownloadPathUrl}${fileId}`
}

// 获取文件预签名URL（MinIO直接访问，内联预览）
export const getPresignedFileUrl = async (fileId, expirySeconds = 3600) => {
    try {
        const res = await getAction(`system/sysFile/getPresignedUrl?fileId=${fileId}&expirySeconds=${expirySeconds}`)
        if (res.code === 0 && res.data && res.data.url) {
            return res.data.url
        }
    } catch (e) {
        console.warn('获取预签名URL失败，降级为代理下载', e)
    }
    // 降级：返回原始下载 URL
    return getFilePreviewUrl(fileId)
}

// 批量获取文件预签名URL
export const batchGetPresignedFileUrl = async (fileIds, expirySeconds = 3600) => {
    try {
        const res = await postAction('system/sysFile/batchGetPresignedUrl', { fileIds, expirySeconds })
        if (res.code === 0) {
            return (res.data && res.data.urls) || {}
        }
    } catch (e) {
        console.warn('批量获取预签名URL失败', e)
    }
    return {}
}

export const getFileUrl = (fileId) =>{
    return `${getWebUrl()}${baseUrl}${fileDownloadPathUrl}${fileId}`
}

export const saveFileFieldAction = (formNo, obj) => {
    obj.formNo = formNo
    return postAction(saveDataUrl, obj)
}

export const geocoderQueryAction = (longitude, latitude) => {

    return postActionByParams(geocoderQueryUrl, {longitude, latitude, tk: config.tdtKey})
}

export const calcWeekdayAction = (date,days) => {
    return getAction(calcWeekdayUrl, {date,days})
}

export const getMyShortcut = (subSystemCode,defaultSize = 8) => {
    return getAction(shorCurUrl, {subSystemCode,defaultSize})
}

// 查询所有快捷方式
export const getAllShortcut = (subSystemCode = '') => {
    return getAction('/oa/shortcut/getAllShortcut', {subSystemCode})
}

// 保存快捷方式
export const saveMyShortcut = (obj) => {
    return postAction('/oa/shortcut/saveMyShortcut', {...obj})
}