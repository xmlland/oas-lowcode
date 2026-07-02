import _globalEvent from "@/_custom/_globalEvent";
import {Modal} from "ant-design-vue";
import {getZIndex} from "@/lib/zIndexUtil";
import ActInfoView from "@/components/act/ActInfoView";
import InfoView from "@/components/ext/InfoView";

const maxModalCount = 2
let globalModalArray = []
export const addGlobalModal = (modal) => {
    globalModalArray.push(modal)
}
export const removeGlobalModal = () => {
    globalModalArray.splice(globalModalArray.length - 1, 1)
}
export const getGlobalModal = () => {
    if (globalModalArray.length === maxModalCount) {
        return globalModalArray[globalModalArray.length - 1]
    }
    return null
}
let globalEvent = {
    showActInfo(id, procDefKey, formNo, module, title = '申请信息', width = 1200) {
        let oldModal = getGlobalModal()
        if(oldModal){
            oldModal.update({
                title: title,
                width: width,
                content: (
                    <ActInfoView id={id} procDefKey={procDefKey} formNo={formNo} module={module}/>
                )
            })
            return
        }
        let modal = Modal.info({
            appContext: window.$instance.appContext,
            title: title,
            wrapClassName: 'global-event-modal-wrap',
            width: width,
            zIndex: getZIndex(),
            closable: true,
            okText: '关闭',
            afterClose: () => {
                removeGlobalModal()
            },
            content: (
                <ActInfoView id={id} procDefKey={procDefKey} formNo={formNo} module={module}/>
            )
        });
        addGlobalModal(modal)
    },
    showInfo(id, formNo, module, title = '详细内容', width = 1200) {
        let oldModal = getGlobalModal()
        if(oldModal){
            oldModal.update({
                title: title,
                width: width,
                content: (
                  <InfoView id={id} formNo={formNo} module={module}/>
                )
            })
            return
        }
        let modal = Modal.info({
            appContext: window.$instance.appContext,
            title: title,
            wrapClassName: 'global-event-modal-wrap',
            width: width,
            zIndex: getZIndex(),
            closable: true,
            okText: '关闭',
            afterClose: () => {
                removeGlobalModal()
            },
            content: (
              <InfoView id={id} formNo={formNo} module={module}/>
            )
        });
        addGlobalModal(modal)
    },
}
Object.assign(globalEvent, _globalEvent)
export default globalEvent
