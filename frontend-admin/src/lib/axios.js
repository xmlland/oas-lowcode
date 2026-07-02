import axios from 'axios'

class HttpRequest {
    constructor(baseUrl, timeout = 900000, before = () => {
    }, after = () => {
    }) {
        this.baseUrl = baseUrl
        this.timeout = timeout
        this.before = before
        this.after = after
        this.queue = {}
    }

    getInsideConfig() {
        const config = {
            baseURL: this.baseUrl,
            timeout: this.timeout,//超时时间
            headers: {
                //
            }
        }
        return config
    }

    destroy(url) {
        delete this.queue[url]
        if (!Object.keys(this.queue).length) {
            // Spin.hide()
        }
    }

    interceptors(instance, url) {
        // 请求拦截

        instance.interceptors.request.use(config => {
            this.before(config)
            // 添加全局的loading...
            if (!Object.keys(this.queue).length) {
                // Spin.show() // 不建议开启，因为界面不友好
            }
            this.queue[url] = true
            return config
        }, error => {
            return Promise.reject(error)
        })
        // 响应拦截
        instance.interceptors.response.use(res => {
            this.after(res)
            this.destroy(url)
            const {data} = res
            data.__responseHeader__ = res.headers
            return data
        }, error => {
            this.destroy(url)
            let errorInfo = error.response
            console.log(url, error, errorInfo)
            //addErrorLog(errorInfo)
            return Promise.reject(error)
        })
    }

    request(options) {
        const instance = axios.create()
        options = Object.assign(this.getInsideConfig(), options)
        this.interceptors(instance, options.url)
        return instance(options)
    }
}

export default HttpRequest
