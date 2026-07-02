<template>
  <div class="modern-user-actions">
    <a-popover
      v-model:visible="colorPopoverVisible"
      placement="bottomRight"
      trigger="click"
      overlay-class-name="modern-theme-color-popover"
    >
      <template #content>
        <div class="modern-color-panel">
          <button
            v-for="item in colorList"
            :key="item.color"
            class="modern-color-option"
            :class="{ active: item.color === primaryColor }"
            :style="{ '--option-color': item.color }"
            :title="item.key || item.color"
            @click="changePrimaryColor(item.color)"
          >
            <span></span>
          </button>
        </div>
      </template>
      <a-tooltip title="主题色">
        <button class="modern-action-button theme-button">
          <span class="theme-dot" :style="{ background: primaryColor }"></span>
        </button>
      </a-tooltip>
    </a-popover>

    <a-tooltip title="系统消息">
      <button class="modern-action-button" @click="goToMessages">
        <a-badge :count="msgUnreadCount" :overflow-count="99" :offset="[4, -4]">
          <bell-outlined />
        </a-badge>
      </button>
    </a-tooltip>

    <a-dropdown placement="bottomRight" :trigger="['click']">
      <button class="modern-user-trigger">
        <a-avatar class="modern-user-avatar">{{ avatarText }}</a-avatar>
        <span class="modern-user-name">{{ displayUserName }}</span>
      </button>
      <template #overlay>
        <a-menu class="modern-user-menu">
          <a-menu-item @click="modifyPassword">
            <lock-outlined />
            <span>修改密码</span>
          </a-menu-item>
          <a-menu-item @click="logout">
            <export-outlined />
            <span>退出系统</span>
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>

  <u-modal
    ref="modifyPasswordModal"
    :closable="modifyPasswordModalClosable"
    :width="800"
    :show-cancel="modifyPasswordModalClosable"
    :keyboard="false"
    @saveSuccess="passwordSaveSuccess"
  >
    <modify-password-form />
  </u-modal>
</template>

<script>
export default {
  name: 'ModernUserActions'
}
</script>

<script setup>
import {BellOutlined, ExportOutlined, LockOutlined} from '@ant-design/icons-vue'
import {computed, getCurrentInstance, inject, onMounted, ref, watch} from 'vue'
import {message} from 'ant-design-vue'
import {useRouter} from 'vue-router'
import {useStore} from 'vuex'
import config from '@/config'
import {logoutAction} from '@/api/api'
import {clearToken} from '@/api/action'
import {clearStorage} from '@/lib/storage'
import {reloadToIndex} from '@/lib/tools'
import ModifyPasswordForm from '@/views/user/ModifyPasswordForm'
import UModal from '@/components/modal/UModal'

const store = useStore()
const router = useRouter()
const instance = getCurrentInstance()
const addTab = inject('addTabToAdminContent')

const colorPopoverVisible = ref(false)
const modifyPasswordModalClosable = ref(true)
const passwordMessage = ref('密码修改成功，3 秒后跳转至登录页面。')

const userName = computed(() => store.getters.getUserName || '')
const avatarText = computed(() => userName.value ? userName.value.slice(0, 1) : '')
const displayUserName = computed(() => userName.value || '用户')
const msgUnreadCount = computed(() => store.getters.getMsgUnreadCount || 0)
const passwordExpired = computed(() => store.getters.getPasswordExpired)
const primaryColor = computed(() => store.getters.getPrimaryColor || config.theme?.primaryColor || '#4b57d5')
const colorList = computed(() => config.theme?.colorList || [])

const changePrimaryColor = color => {
  store.commit('setPrimaryColor', color)
  colorPopoverVisible.value = false
}

const goToMessages = () => {
  if (addTab) {
    addTab({
      key: '/oa/oa_sys_msg/list',
      attributes: {pageName: '系统消息'}
    })
  }
  router.push('/oa/oa_sys_msg/list')
}

const modifyPassword = () => {
  modifyPasswordModalClosable.value = true
  instance.refs.modifyPasswordModal.open('修改密码', {})
}

const logout = () => {
  logoutAction(false).then(() => {
    clearToken()
    clearStorage()
    store.commit('setUserView', {})
    store.commit('setExtEntId', 'null')
    reloadToIndex()
  })
}

onMounted(() => {
  watch(() => passwordExpired.value, () => {
    if (passwordExpired.value) {
      modifyPasswordModalClosable.value = false
      instance.refs.modifyPasswordModal.open('请修改密码', {})
    }
  }, {immediate: true})
})

const passwordSaveSuccess = () => {
  let sec = 3
  passwordMessage.value = `密码修改成功，${sec} 秒后跳转至登录页面。`
  const timer = setInterval(() => {
    sec -= 1
    passwordMessage.value = `密码修改成功，${sec} 秒后跳转至登录页面。`
  }, 1000)
  message.info(() => passwordMessage.value, 3, () => {
    clearInterval(timer)
    logout()
  })
}

defineExpose({
  modifyPassword
})
</script>

<style lang="less" scoped>
.modern-user-actions {
  display: flex;
  height: 64px;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.modern-action-button,
.modern-user-trigger {
  display: inline-flex;
  height: 36px;
  align-items: center;
  justify-content: center;
  color: #344054;
  background: #f7f9fc;
  border: 1px solid #e4e9f1;
  border-radius: 8px;
  cursor: pointer;
  outline: none;
  transition: color 0.18s ease, background 0.18s ease, border-color 0.18s ease;

  &:hover {
    color: var(--modern-primary);
    background: var(--modern-primary-soft);
    border-color: var(--modern-primary);
  }
}

.modern-action-button {
  width: 36px;
  padding: 0;
  font-size: 17px;
}

.theme-button {
  .theme-dot {
    display: block;
    width: 18px;
    height: 18px;
    border: 3px solid #ffffff;
    border-radius: 50%;
    box-shadow: 0 0 0 1px rgba(71, 85, 105, 0.18);
  }
}

.modern-user-trigger {
  gap: 8px;
  padding: 0 4px 0 0;
  background: transparent;
  border-color: transparent;

  &:hover {
    background: transparent;
    border-color: transparent;
  }
}

.modern-user-avatar {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
  line-height: 36px;
  background: #111827;
}

.modern-user-name {
  max-width: 96px;
  overflow: hidden;
  color: #1f2937;
  font-size: 14px;
  font-weight: 500;
  line-height: 20px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.modern-color-panel {
  display: grid;
  width: 176px;
  grid-template-columns: repeat(5, 28px);
  gap: 9px;
  padding: 4px;
}

.modern-color-option {
  position: relative;
  display: block;
  width: 28px;
  height: 28px;
  box-sizing: border-box;
  flex: 0 0 28px;
  margin: 0;
  padding: 0;
  appearance: none;
  background: transparent;
  border: 0;
  border-radius: 50%;
  cursor: pointer;
  font: inherit;
  line-height: 0;
  outline: none;
  -webkit-appearance: none;

  span {
    position: absolute;
    top: 50%;
    left: 50%;
    display: block;
    width: 22px;
    height: 22px;
    background: var(--option-color);
    border-radius: 50%;
    transform: translate(-50%, -50%);
  }

  &.active::before {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 28px;
    height: 28px;
    box-sizing: border-box;
    border: 2px solid var(--option-color);
    border-radius: 50%;
    content: '';
    pointer-events: none;
    transform: translate(-50%, -50%);
  }

  &.active::after {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 8px;
    height: 12px;
    border: solid #ffffff;
    border-width: 0 2px 2px 0;
    content: '';
    pointer-events: none;
    transform: translate(-50%, -57%) rotate(45deg);
    transform-origin: center;
  }

  &.active span {
    width: 20px;
    height: 20px;
  }
}

:global(.modern-user-menu .ant-dropdown-menu-item) {
  display: flex;
  min-width: 132px;
  align-items: center;
  gap: 8px;
}

:global(.modern-theme-color-popover .ant-popover-inner-content) {
  padding: 10px;
}
</style>
