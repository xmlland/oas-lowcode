import HttpRequest from "@/lib/axios";
import {Input, message, Modal} from 'ant-design-vue';
import {clearAllInterval, confirm, deepClone, oneOf, prompt} from "@/lib/tools";
import router, {loginPath} from "@/router";
import {getStorage, removeStorage, setStorage} from "@/lib/storage";
import {Sm2Decrypt, Sm2Encrypt, Sm2GenerateKeyPairHex} from "@/lib/tool-security";

//可以在env.js通过window['tokenKey']配置token的key
export const TOKEN_KEY = window['tokenKey'] || '$tokenBPM'
const SERVER_PUBLIC_KEY = '$serverPublicKey'
const ENCRYPT_WHITE_LIST = '$encryptWhiteList'
export const baseUrl = window['baseUrl'] || '/jeeStudio/gtoa/a/';

export const documentServerUrl = window['documentServerUrl'];
export const serverUrl = window['serverUrl'];
export const tokenName = window['tokenName'] || 'token';
if (!window['baseUrl']) {
    console.warn(`window['baseUrl'] 未配置，使用默认值`)
}
export const setToken = (token) => {
    return setStorage(TOKEN_KEY, token)
}
export const getToken = () => {
    return getStorage(TOKEN_KEY)
}
export const clearToken = () => {
    removeStorage(SERVER_PUBLIC_KEY)
    return removeStorage(TOKEN_KEY)
}
export const setServerPublicKey = (token) => {
    return setStorage(SERVER_PUBLIC_KEY, token)
}
export const getServerPublicKey = () => {
    return getStorage(SERVER_PUBLIC_KEY)
}
export const setEncryptWhiteList = (encryptWhiteList) => {
    return setStorage(ENCRYPT_WHITE_LIST, encryptWhiteList)
}
export const getEncryptWhiteList = () => {
    return getStorage(ENCRYPT_WHITE_LIST)
}
export const getHeaderToken = () => {
    const token = getToken()
    let header = {}
    header[tokenName] = token
    return header
}
let keyPairHex = Sm2GenerateKeyPairHex();
let tokenKeyPairHex = Sm2GenerateKeyPairHex();
const request = new HttpRequest(baseUrl, 900000, (config) => {
    const token = getToken()
    if (token) {
        config.headers[tokenName] = token // 让每个请求携带自定义 token 请根据实际情况自行修改
    }
    if (window.showResponseLog){
        console.log(JSON.parse(JSON.stringify(config)));
    }
    const serverPublicKey = getServerPublicKey();
    let encryptWhiteList = getEncryptWhiteList();
    if(!encryptWhiteList || (encryptWhiteList==='undefined')){
        encryptWhiteList=[];
    } else {
        encryptWhiteList = JSON.parse(encryptWhiteList)
        encryptWhiteList.forEach((item,index)=>{
            encryptWhiteList[index] = item.replace('**','.*').replace('/','') + '$'
        })
    }
    let url = config.url
    if (serverPublicKey && (!oneOf(url.split('?')[0],encryptWhiteList,false,true))) {
        let urlParameters = getURLParameters(url);
        let _url = url.split('?')[0]
        let isFirst = true
        for (let key in urlParameters) {
            _url += (isFirst ? '?' : '&') + key + '=' + Sm2Encrypt(urlParameters[key], serverPublicKey)
            isFirst = false
        }
        config.url = _url
        config.headers['serverPublicKey'] = serverPublicKey
        if (config.params) {
            for (let pKey in config.params) {
                config.params[pKey] = Sm2Encrypt(config.params[pKey], serverPublicKey)
            }
        }
        if (config.method === 'post' && config.headers['content-type'] !== 'multipart/form-data' && typeof (config.data) === 'object') {
            config.data = {
                text: Sm2Encrypt(config.data, serverPublicKey)
            }
        } else if (config.method === 'post' && config.headers['content-type'] === 'multipart/form-data') {
            for (let pKey in config.data) {
                let isFile = config.data[pKey] instanceof File
                if (!isFile) {
                    config.data[pKey] = Sm2Encrypt(config.data[pKey], serverPublicKey)
                }
            }
        }
    }
    config.headers['clientPublicKey'] = keyPairHex.publicKey;
    config.headers['tokenPublicKey'] = tokenKeyPairHex.publicKey;
}, (res) => {
    const serverPublicKey = getServerPublicKey();
    if (serverPublicKey && res.data && res.data.data && typeof (res.data.data) === 'string') {
        res.data = JSON.parse(Sm2Decrypt(res.data.data, keyPairHex.privateKey))
        if (window.showResponseLog){
            console.log(this,res.data)
        }
    }
    if (res && res.data && res.data.data && res.data.data.tokenEncrypt) {
        res.data.token = Sm2Decrypt(res.data.token, tokenKeyPairHex.privateKey)
    }
})

const getURLParameters = url => {
    return url.match(/([^?=&]+)(=([^&]*))/g)
        .reduce((a, v) => (a[v.slice(0, v.indexOf('='))] = v.slice(v.indexOf('=') + 1), a), {}
        );
}

const errorMsgSecond = 0;
let errCount = 0;
const handleSuccess = (res, resolve, reject, showMsg) => {
    if (res.code === 0) {
        showMsg && message.success(res.msg);
        // Priority 1: Check response header for refreshed token (from backend auto-refresh)
        const headerToken = res.__responseHeader__?.token
        if (headerToken && typeof headerToken === 'string' && headerToken.length > 0) {
            setToken(headerToken)
        }
        // Priority 2: Token from response body (e.g., login response)
        else if (res.token) {
            setToken(res.token)
        }
        if (res.publicKey) {
            setServerPublicKey(res.publicKey)
            setEncryptWhiteList(res.encryptWhiteList)
        }
        resolve(res);
    } else {

        if (res.code === 1001) {
            clearToken()
            clearAllInterval()
            router.push({
                path: loginPath
            })
        }
        if (res.code !== -9005 && res.code !== -2000) {//二次鉴权与文件导入从外面处理
            if (errCount > 0) {
                Modal.destroyAll();
            }
            errCount++;
            if (res.msg) {
                Modal.error({
                    title: '错误',
                    content: res.msg
                });
            } else {
                Modal.error({
                    title: '错误',
                    content: '未知错误'
                });
            }
            errCount--;
        }
        reject(res);
    }
}
const handleError = (error, resolve, reject) => {
    if (error.response && error.response.data) {
        let res = error.response.data
        if (res.code === 1001) {
            clearToken()
            clearAllInterval()
            router.push({
                path: loginPath
            })
        }
        if (res.code !== -9005) {//文件导入从外面处理
            errCount++;
            if (errCount > 0) {
                Modal.destroyAll();
            }
            if (res.msg) {
                Modal.error({
                    title: '错误',
                    content: res.msg
                });
            } else {
                Modal.error({
                    title: '错误',
                    duration: errorMsgSecond,
                    content: '未知错误'
                });
            }
            errCount--;
        }
    }

    reject(error)
}
export const sendRequest = (options, showMsg) => {
    if (options.url) {
        if (options.url.indexOf("/datamapView") > -1) {
            options.url = options.url.replace("/datamapView?", "/datamapView/" + new Date().getTime()+'/?');
        } else if (options.url.indexOf("/datamap") > -1) {
            options.url = options.url.replace("/datamap?", "/datamap/" + new Date().getTime()+'/?');
        } else {
            if (options.url.indexOf("?") > -1) {
                options.url += "&_v=" + new Date().getTime();
            } else {
                options.url += "?_v=" + new Date().getTime();
            }
        }
    }
    return new Promise(function (resolve, reject) {
        request.request(options).then(res => {
            // 二次鉴权
            let authPerson;
            let authKey;

            if(res.code === -2000) {
                prompt({
                    title: '请进行二次鉴权!',
                    width: 800,
                    emptyMessage: '请填写!',
                    getValue() {
                        if (authKey&&authPerson){
                            return {authKey,authPerson}
                        }else{
                            return false
                        }
                    },
                    render : (
                        <div>
                            请输入鉴权人：
                            <Input placeholder="请输入鉴权人" allowClear={true} maxlength={200} onUpdate:value={(e) => {
                                authPerson = e
                            }}>
                            </Input>
                            <br/>
                            请输入鉴权密钥：
                            <Input placeholder="请输入鉴权密钥" allowClear={true} maxlength={200} onUpdate:value={(e) => {
                                authKey = e
                            }}>
                            </Input>
                        </div>
                    ),
                    callback: () => {
                        let publicKey = res?.data?.authPublicKey;
                        let authKeySm2 = Sm2Encrypt(authKey,publicKey);
                        let authPersonSm2 = Sm2Encrypt(authPerson,publicKey);
                        let options2 = deepClone(options);
                        let headers = options2.headers;
                        if(!headers){
                            headers = {};
                        }
                        headers['authorizationPublicKey'] = publicKey;
                        headers['authorizationKey'] = authKeySm2;
                        headers['authPerson'] = authPersonSm2;
                        options2.headers = headers;
                        sendRequest(options2, res).then((res2) =>{
                            handleSuccess(res2, resolve, reject, showMsg)
                        }).catch(error => {
                            handleError(error, resolve, reject)
                        });
                    },
                    onCancel: () =>{
                        reject({msg:'已取消'});
                    }
                })
                return;
            }
            handleSuccess(res, resolve, reject, showMsg)
        }).catch(error => {
            handleError(error, resolve, reject)
        })
    });
}

export const getAction = (url, params) => {
    return sendRequest({
        url: url,
        params,
        method: 'get'
    }, false)
}
/**
 * post请求 显示loading
 * @param url
 * @param data
 * @return {Promise<unknown>}
 */
export const postActionShowLoading = (url, data) => {
    return postAction(url, data, null, null, true)
}
export const postAction = (url, data = {}, headers, transformRequest, showLoading = false) => {
    let options = {
        url: url,
        data,
        method: 'post'
    }
    if (headers) {
        options.headers = headers
    }
    if (transformRequest) {
        options.transformRequest = transformRequest
    }
    if (showLoading) {
        const modal = Modal.info({
            title: '提示',
            okButtonProps: {style: {display: 'none'}},
            content: '数据处理中...',
        });
        return new Promise((resolve,reject) => {
            sendRequest(options, false).then(res => {
                resolve(res)
            }).catch(res=>{
                reject(res)
            }).finally(() => {
                modal.destroy();
            })
        })
    }
    return sendRequest(options, false)
}
export const postActionByParams = (url, params, upload, onUploadProgress) => {
    let req = {
        url: url,
        params,
        method: 'post'
    }

    if (upload) {
        req.data = params;
        delete req.params;
        req.headers = {
            'content-type': 'multipart/form-data'
        }
        if (onUploadProgress) {
            req.onUploadProgress = onUploadProgress
        }
    }
    return sendRequest(req, false)
}
export const uploadAction = (uploadUrl, formData, onUploadProgress) => {
    return postActionByParams(uploadUrl, formData, true, onUploadProgress)
}

export const deleteAction = (url, params) => {
    return sendRequest({
        url: url,
        params,
        method: 'delete'
    })
}
export const confirmAction = (title, content, url, data, onOk, onCancel, method) => {
    let _method = method || 'post'
    confirm({
        title: title,
        content: content,
        onOk: () => {
            const msg = message.loading({
                content: '处理中...',
                duration: 0
            });
            let action = postAction;
            if ('delete' === _method) {
                action = deleteAction
            }
            if ('get' === _method) {
                action = getAction
            }
            action(url, data).then(res => {
                if (res.code === 0) {
                    onOk(res);
                }
                setTimeout(msg, 0);
            }).catch(err => {
                setTimeout(msg, 0);
                console.error(err)
            })
        },
        onCancel: () => {
            onCancel && onCancel()
        }
    });
}

export const exportFile = (url) => {
    //下载文件 在本页面打开 防止浏览器拦截 通过修改location.href来实现
    window.location.href = baseUrl + url;
    //window.open(baseUrl + url, "_blank");
}
