import {Modal, Textarea} from "ant-design-vue";
import {createVNode, nextTick} from "vue";

export const confirm = ({title, content, onOk, onCancel}) => {
    Modal.confirm({
        title: title || '确认？',
        content: content || '确定进行该操作吗？',
        cancelText: '取消',
        okText: '确定',
        onOk: onOk,
        onCancel: onCancel,
        zIndex: 10000
    });
}

export const confirmModal = confirm

export const prompt = (props = {},) => {

    let required = true;
    if (typeof props.required !== 'undefined') {
        required = props.required
    }
    let _val = props.defaultValue || '';
    let value = _val
    let vNode = createVNode(Textarea, {
        allowClear: true,
        autoSize: {minRows: 5, maxRows: 5},
        maxlength: props.maxlength || 200,
        showCount: false,
        placeholder: props.placeholder || '',
        defaultValue: _val || '',
        'onUpdate:value': (e) => {
            value = e
        }
    });
    Modal.info({
        title: props.title || '请输入',
        okText: props.okText ||'确定',
        closable: true,
        zIndex: 10040,
        width: props.width || 416,
        wrapClassName: 'global-event-modal-wrap',
        onOk: () => {
            return new Promise((resolve, reject) => {
                let callbackValue = value
                if (props.getValue) {
                    callbackValue = props.getValue()
                }
                if (callbackValue||!required) {
                    props.callback && props.callback(callbackValue)
                    resolve()
                } else {
                    // 未填写内容弹个框提示一下 #1427  zry 2022-11-26 16:25
                    Modal.warning({
                        class: 'top-message',
                        zIndex: 10050,
                        content: typeof props.emptyMessage === 'function' ? props.emptyMessage(callbackValue) : (props.emptyMessage || '请填写')
                    })
                    reject()
                }
            })

        },
        onCancel: props.onCancel,
        content: props.render || vNode
    })
}

export const promptModal = prompt

/**
 * 使用UModal打开iframe窗口
 * @param title 标题
 * @param iframeUrl
 * @param width 宽度
 * @param height 高度
 */
export const openIframeInUModal = (title = '查看', iframeUrl, {width = 800, height = 600}) => {
    let modalId = top.globalRef.modal.init({
        width: width,
        customBodyStyle: {padding: 0},
        noFooter: true,
        component: (
            <iframe src={iframeUrl} style={{border: 0, width: width + 'px', height: height + 'px'}}></iframe>
        )
    })
    nextTick(() => {
        top.globalRef.modal.getRef(modalId).open(title, {}, true)
    })
}
