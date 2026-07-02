import { getAction, postAction } from './action'

// 获取未读消息数
export const getUnreadCount = () => {
  return getAction('system/sysMsg/getUnreadCount')
}

// 标记单条消息已读
export const setMsgRead = (id) => {
  return postAction('system/sysMsg/setRead', { id })
}

// 全部标记已读
export const setMsgReadAll = () => {
  return postAction('system/sysMsg/setReadAll')
}

// 批量标记已读
export const setMsgReadBatch = (ids) => {
  return postAction('system/sysMsg/setReadBatch', { ids })
}
