<template>
  <div class="user-info">
    <div class="user-info-item">
      <a-dropdown>
        <div>
          <a-avatar>{{ avatarText }}</a-avatar>
          <span class="name-area">
            <span class="company-name"> {{ companyName }}</span>
            <span class="user-no" v-if="userView.no"> {{ userView.no }}</span>
            <span class="user-name"> {{ formatUserName }}  </span>
          </span>
        </div>

        <template #overlay>
          <a-menu>
            <a-menu-item @click="goToMessages">
              <div class="menu-item-with-badge">
                <BellOutlined style="margin-right: 8px;" />
                <span>系统消息</span>
                <a-badge v-if="msgUnreadCount > 0" :count="msgUnreadCount" :overflow-count="99" class="msg-badge" />
              </div>
            </a-menu-item>
            <a-menu-item @click="modifyPassword">
              <LockOutlined style="margin-right: 8px;" />
              <span>修改密码</span>
            </a-menu-item>
            <u-permission :has-permissions="['page:specialSymbols']">
              <a-menu-item @click="specialSymbolsClick">
                <FormOutlined style="margin-right: 8px;" />
                <span>Ω特殊符号</span>
              </a-menu-item>
            </u-permission>
            <u-permission :has-permissions="['theme:setting']">
              <a-menu-item @click="goSetting">
                <SkinOutlined style="margin-right: 8px;" />
                <span>设置皮肤</span>
              </a-menu-item>
            </u-permission>
            <u-permission :has-permissions="['sign:upload']">
              <a-menu-item @click="uploadSign">
                <EditOutlined style="margin-right: 8px;" />
                <span>上传签名</span>
              </a-menu-item>
            </u-permission>
            <a-menu-item v-if="logoutPosition==='menu'" @click="logout">
              <ExportOutlined style="margin-right: 8px;" />
              <span>退出系统</span>
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
    <div v-if="logoutPosition==='right'" class="user-info-item user-logout" @click="logout">
      <export-outlined/>
      退出系统
    </div>
  </div>
  <u-modal :closable="modifyPasswordModalClosable" :width="800" ref="modifyPasswordModal" :show-cancel="modifyPasswordModalClosable"
           :keyboard="false"
           @saveSuccess="passwordSaveSuccess">
    <modify-password-form/>
  </u-modal>
  <u-modal ref="uploadSignModalRef" :width="800" :customOK="true" @clickOk="uploadSignClickOk">
    <u-form ref="uploadSignUForm" v-model:value="signFormModel">
      <a-row>
        <a-col>
          <a-form-item name="sign" label="签名" :rules="[{ required: true, message: '请上传签名' }]">
            <u-upload :picture="true" :disabled="false" :fileCount="1" v-model:value="signFormModel.sign"/>
          </a-form-item>
        </a-col>
      </a-row>
    </u-form>
  </u-modal>
  <a-drawer
      v-if="settingVisible"
      :style="{width:'236px'}"
      :mask="false"
      v-model:visible="settingVisible"
      title="修改皮肤设置"
      placement="right"
      @after-visible-change="afterVisibleChange"
  >
    <theme-setting/>
  </a-drawer>
</template>

<script>
export default {
  name: "UserInfo"
}
</script>
<script setup>
import {computed, ref, onMounted, watch, getCurrentInstance, inject} from "vue";
import {useStore} from "vuex";
import {logoutAction} from "@/api/api";
import {clearToken, postAction} from "@/api/action";
import {clearStorage} from "@/lib/storage";
import {getWebUrl, reloadToIndex} from "@/lib/tools";
import ThemeSetting from "@/layout/admin/ThemeSetting";
import UPermission from "@/components/role/UPermission";
import UModal from "@/components/modal/UModal";
import {message, Modal} from "ant-design-vue";
import ModifyPasswordForm from "@/views/user/ModifyPasswordForm";
import {useRouter} from "vue-router";
import config from "@/config";
import {BellOutlined, ExportOutlined, LockOutlined, SkinOutlined, EditOutlined, FormOutlined} from "@ant-design/icons-vue";
const store = useStore();
const router = useRouter();
const addTab = inject('addTabToAdminContent');
defineProps({
  logoutPosition: {
    type: String,
    default: 'menu'//menu right
  }
})

let userName = computed(() => store.getters.getUserName)
let avatarText = computed(() => {
  if (userName.value) {
    return userName.value.slice(0, 1)
  }
  return ''
})
let formatUserName = computed(() => {
  if (userName.value) {
    return userName.value.length > 3 ? userName.value.slice(0, 3) + '···' : userName.value
  }
  return ''
})
let userView = computed(() => store.getters.getUserView)
let companyName = computed(() => {
  if (userView.value.company) {
    return userView.value.company.shortName || (userView.value.company.name.length > 6 ? userView.value.company.name.slice(0, 6) + '···' : userView.value.company.name)
  }
  return ''
})
// 消息未读数
let msgUnreadCount = computed(() => store.getters.getMsgUnreadCount)
// 跳转到消息列表页
const goToMessages = () => {
  if (addTab) {
    addTab({
      key: '/oa/oa_sys_msg/list',
      attributes: { pageName: '系统消息' }
    })
    router.push('/oa/oa_sys_msg/list')
  }
}
let passwordExpired = computed(() => store.getters.getPasswordExpired)
let modifyPasswordModalClosable = ref(true)
let settingVisible = ref(false)
let instance = getCurrentInstance();
onMounted(() => {
  watch(() => passwordExpired.value, () => {
    if (passwordExpired.value) {
      modifyPasswordModalClosable.value = false
      instance.refs.modifyPasswordModal.open('请修改密码', {})
    }
  }, {immediate: true})
})
const modifyPassword = () => {
  console.log('modifyPassword')
  modifyPasswordModalClosable.value = true
  instance.refs.modifyPasswordModal.open('修改密码', {})
}
const goSetting = () => {
  settingVisible.value = true
}
const afterVisibleChange = (visible) => {
  settingVisible.value = visible
};

let signFormModel = ref({});

const uploadSign = () =>{
  signFormModel.value.sign = userView.value.sign;
  instance.refs.uploadSignModalRef.open('上传签名',{});
}

const uploadSignClickOk = (cb) =>{
  instance.refs.uploadSignUForm.validateFields().then((values) => {
    cb(()=>{
      return new Promise((resolve,reject)=>{
        postAction("sys/user/updateSign?signGroupId="+signFormModel.value.sign,{}).then((res)=>{
          let nUserView = {};
          Object.assign(nUserView,userView.value);
          nUserView.sign = signFormModel.value.sign;
          store.commit('setUserView', nUserView);
          resolve();
          Modal.success({
            title:'成功',
            content:'上传签名成功'
          })
        })
      })
    })
  })
}

const logout = () => {
  logoutAction(false).then(res => {
    clearToken()
    clearStorage()
    store.commit('setUserView', {})
    store.commit('setExtEntId', 'null')

    reloadToIndex()

  })
}

const specialSymbolsClick = () => {
  let routerUrl = router.resolve({
    path: '/specialSymbols'
  })
  window.open(getWebUrl() + routerUrl.href)
}
let content = ref('密码修改成功，3秒后跳转至登录页面！')
const passwordSaveSuccess = () => {
  let sec = 3;
  let inter = setInterval(() => {
    sec--
    content.value = `密码修改成功，${sec}秒后跳转至登录页面！`
  }, 1000)
  message.info(() => content.value, 3, () => {
    clearInterval(inter)
    logout()
  })

}
defineExpose({
  modifyPassword
})
</script>
<style lang="less" scoped>
.user-info {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .user-info-item {

    cursor: pointer;
    padding: 0 20px;
    display: flex;
    flex-direction: row;
    align-items: center;

    .ant-dropdown-trigger {
      display: flex;
      align-items: center;
      justify-content: center;

      .name-area {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        flex-wrap: nowrap;
        margin-left: 10px;

        span {
          height: 26px;
          line-height: 26px;
        }
      }
    }
  }
}

.menu-item-with-badge {
  display: flex;
  align-items: center;
  width: 100%;
  
  .msg-badge {
    margin-left: auto;
  }
}
</style>
