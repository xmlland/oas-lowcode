<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="login-brand">
      <div class="brand-decoration">
        <div class="deco-circle deco-circle--1"></div>
        <div class="deco-circle deco-circle--2"></div>
        <div class="deco-circle deco-circle--3"></div>
      </div>
      <div class="brand-content">
        <img class="brand-logo" :src="defaultLogo" alt="logo"/>
        <h1 class="brand-title">OAS 低代码开发平台</h1>
        <p class="brand-subtitle">动态表单 · 工作流 · 代码生成 · AI 辅助建模</p>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="login-form-wrapper">
      <div class="login-form-card">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-subtitle">请输入您的账号密码</p>

        <u-form ref="form" v-model:value="formModel" :labelWidth="70">
          <div>
            <div class="main-item">
              <a-form-item class="login-input" name="username" label=""
                           :rules="[{ required: true, message: '请输入用户名' }]">
                <a-input v-model:value="formModel.username" placeholder="请输入用户名" size="large">
                  <template #prefix>
                    <user-outlined class="input-icon" type="user"/>
                  </template>
                </a-input>
              </a-form-item>
            </div>
            <div class="main-item">
              <a-form-item class="login-input" name="password" label=""
                           :rules="[{ required: true, message: '请输入密码' }]">
                <a-input-password v-model:value="formModel.password" placeholder="请输入密码" size="large">
                  <template #prefix>
                    <lock-outlined class="input-icon"/>
                  </template>
                </a-input-password>
              </a-form-item>
            </div>
            <div class="main-item" v-if="showValidateCodeRef">
              <a-form-item name="validateCode" label=""
                           :rules="[{ required: true, message: '请输入验证码' }]">
                <a-input @keydown="keydown" class="validate-code" :maxlength="4" v-model:value="formModel.validateCode"
                         placeholder="请输入验证码" size="large">
                  <template #prefix>
                    <check-square-outlined class="input-icon"/>
                  </template>
                  <template #addonAfter>
                    <img @click="loadValidateCode" :src="validateCodeUrl" class="validate-img">
                  </template>
                </a-input>
              </a-form-item>
            </div>
            <div class="login-btn">
              <a-button :loading="loading" @click="handleLogin" type="primary" block size="large" class="login-submit-btn">
                登录
              </a-button>
            </div>

            <!-- 快捷登录 -->
            <template v-if="userLoginConfig">
              <div class="quick-login-divider">
                <span class="divider-line"></span>
                <span class="divider-text">快捷登录</span>
                <span class="divider-line"></span>
              </div>
              <div class="quick-login-list">
                <div
                  v-for="user in userLoginConfig.userList"
                  :key="user.username"
                  class="quick-login-item"
                  @click="handleLoginUserList(user)"
                >
                  <div class="quick-avatar">{{ user.showText.charAt(0) }}</div>
                  <span class="quick-name">{{ user.showText }}</span>
                </div>
              </div>
            </template>
          </div>
        </u-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "LoginView",
}
</script>
<script setup>
import {getCurrentInstance, onMounted, ref} from "vue";
import {useStore} from "vuex";
import {useRouter, useRoute} from "vue-router";
import {homePath} from "@/router";
import {getAction} from "@/api/action";
import {defaultLogo} from "@/config";
const props = defineProps({
  showTitle: {
    type: Boolean,
    default: true
  }
})
let userLoginConfig =ref();
if (import.meta.env.DEV) {
  import('./userLoginConfig.js')
      .then(module => {
        userLoginConfig.value = module.default;
      })
}
let loading = ref(false)
let formModel = ref({
  username: '',
  password: ''
})

const store = useStore();
const router = useRouter();
const route = useRoute();
let instance = getCurrentInstance();

let validateCodeUrl = ref(null)
let imgUUID = null
let showValidateCodeRef = ref(true);
const showValidateCode = () => {
  getAction(`showValidateCode?t=${+new Date().getTime()}`).then(res => {
    showValidateCodeRef.value = res.data.showValidateCode;
  });
};
const loadValidateCode = () => {

  getAction(`createCharacter?t=${+new Date().getTime()}`).then(res => {
    validateCodeUrl.value = res.data.imgBase64
    imgUUID = res.data.imgUUID
  })
  //validateCodeUrl.value = url()
}
onMounted(() => {
  loadValidateCode()
  showValidateCode()
})

const keydown = (e) => {
  if (13 === e.keyCode) {
    handleLogin()
  }
}

const extractBaseUrl = () => {
  let url = location.href.split('#')[0]
  let baseUrl = url.split('?')[0]
  // 判断是否为HTTP或HTTPS链接
  if (url.startsWith("http://")) {
    baseUrl = url.slice(7); // 去除"http://"前缀
  } else if (url.startsWith("https://")) {
    baseUrl = url.slice(8); // 去除"https://"前缀
  }

  // 查找第一个斜杠的位置
  let slashIndex = baseUrl.indexOf("/");

  // 提取基础部分
  baseUrl = baseUrl.slice(0, slashIndex + 1);

  // 添加"http://"或"https://"前缀
  if (url.startsWith("https://")) {
    baseUrl = "https://" + baseUrl;
  } else {
    baseUrl = "http://" + baseUrl;
  }

  return baseUrl;
}

const handleLogin = (() => {
  instance.refs.form.validateFields().then(res => {
    let loginData = {
      username: res.username,
      password: res.password,
      validateCode: res.validateCode,
      referer: extractBaseUrl(),
      imgUUID: imgUUID
    }
    for (const key in route.query) {
      loginData[key] = route.query[key]
    }
    loading.value = true
    store.dispatch('userLogin', loginData).then(loginRes => {
      router.push({
        path: homePath
      })
    }).finally(() => {
      loading.value = false
      loadValidateCode()
    })
  })
})
const handleLoginUserList = (user) => {
  formModel.value.username = user.username
  formModel.value.password = user.password
  formModel.value.validateCode = '****'
  handleLogin()
}

</script>
<style lang="less" scoped>
@import '@/assets/less/design-tokens.less';

.login-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

/* 左侧品牌区 */
.login-brand {
  position: relative;
  width: 55%;
  background: @color-primary-gradient;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  .brand-decoration {
    position: absolute;
    inset: 0;
    pointer-events: none;
    overflow: hidden;

    .deco-circle {
      position: absolute;
      border-radius: @radius-circle;
      background: rgba(255, 255, 255, 0.12);
      backdrop-filter: blur(2px);

      &--1 {
        width: 500px;
        height: 500px;
        top: -150px;
        right: -100px;
      }

      &--2 {
        width: 300px;
        height: 300px;
        bottom: -80px;
        left: -60px;
        background: rgba(255, 255, 255, 0.08);
      }

      &--3 {
        width: 200px;
        height: 200px;
        top: 40%;
        left: 60%;
        background: rgba(255, 255, 255, 0.1);
      }
    }
  }

  .brand-content {
    position: relative;
    z-index: 1;
    text-align: center;
    color: @text-inverse;
    animation: fadeInUp 0.8s ease-out;

    .brand-logo {
      width: 120px;
      height: 120px;
      margin-bottom: @space-lg;
      filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
    }

    .brand-title {
      font-size: 42px;
      font-weight: 700;
      margin-bottom: @space-md;
      letter-spacing: 2px;
      color: #ffffff;
      text-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
    }

    .brand-subtitle {
      font-size: @font-size-md;
      opacity: 0.9;
      letter-spacing: 6px;
      font-weight: 300;
    }
  }
}

/* 右侧表单区 */
.login-form-wrapper {
  width: 45%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  /* 大小不一的斑点底纹 */
  background-color: @bg-page;
  background-image:
    /* 大斑点 - 左上 */
    radial-gradient(circle 120px at 15% 20%, rgba(59, 130, 246, 0.12) 0%, transparent 70%),
    /* 大斑点 - 右下 */
    radial-gradient(circle 100px at 80% 75%, rgba(239, 68, 68, 0.1) 0%, transparent 70%),
    /* 中斑点 - 右上 */
    radial-gradient(circle 80px at 75% 15%, rgba(99, 102, 241, 0.1) 0%, transparent 70%),
    /* 中斑点 - 左下 */
    radial-gradient(circle 90px at 20% 80%, rgba(59, 130, 246, 0.08) 0%, transparent 70%),
    /* 小斑点 - 散布 */
    radial-gradient(circle 50px at 35% 35%, rgba(59, 130, 246, 0.15) 0%, transparent 60%),
    radial-gradient(circle 40px at 65% 60%, rgba(99, 102, 241, 0.12) 0%, transparent 60%),
    radial-gradient(circle 35px at 45% 70%, rgba(239, 68, 68, 0.08) 0%, transparent 60%),
    radial-gradient(circle 45px at 85% 40%, rgba(59, 130, 246, 0.1) 0%, transparent 60%),
    radial-gradient(circle 30px at 10% 50%, rgba(99, 102, 241, 0.1) 0%, transparent 60%),
    radial-gradient(circle 55px at 60% 30%, rgba(59, 130, 246, 0.08) 0%, transparent 60%),
    radial-gradient(circle 25px at 90% 90%, rgba(239, 68, 68, 0.08) 0%, transparent 60%),
    radial-gradient(circle 35px at 5% 90%, rgba(59, 130, 246, 0.1) 0%, transparent 60%),
    radial-gradient(circle 40px at 50% 50%, rgba(99, 102, 241, 0.06) 0%, transparent 60%);
}

.login-form-card {
  width: 420px;
  padding: @space-2xl;
  background: @bg-card;
  border-radius: @radius-xl;
  box-shadow: @shadow-lg;
  animation: slideInRight 0.6s ease-out;

  .form-title {
    font-size: @font-size-xl;
    font-weight: 700;
    color: @text-primary;
    margin-bottom: @space-xs;
    text-align: center;
    letter-spacing: 1px;
  }

  .form-subtitle {
    font-size: @font-size-sm;
    color: @text-hint;
    margin-bottom: @space-xl;
    text-align: center;
  }

  .main-item {
    width: 100%;
  }

  .login-input {
    margin-bottom: @space-lg;

    :deep(.ant-input-affix-wrapper) {
      border-radius: @radius-md;
      height: 44px;
      transition: all @transition-base;

      &:hover, &:focus {
        border-color: @color-primary;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
      }
    }
  }

  .input-icon {
    color: @color-primary;
    font-size: @font-size-md;
  }

  .login-btn {
    margin-top: @space-md;
  }

  .login-submit-btn {
    height: 48px;
    border-radius: @radius-md;
    font-size: @font-size-md;
    font-weight: 600;
    background: @color-primary-gradient;
    border: none;
    transition: all @transition-base;
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
      transition: left 0.5s ease;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(59, 130, 246, 0.35);

      &::after {
        left: 100%;
      }
    }
  }
}

/* 验证码 */
.validate-code {
  :deep(.ant-input-group-addon:last-child) {
    padding: 0;
    overflow: hidden;
    border-radius: 0 @radius-md @radius-md 0;

    .validate-img {
      cursor: pointer;
      height: 42px;
      display: block;
    }
  }
}

/* 快捷登录 */
.quick-login-divider {
  display: flex;
  align-items: center;
  margin: @space-xl 0 @space-md;
  gap: @space-md;

  .divider-line {
    flex: 1;
    height: 1px;
    background: @border-color;
  }

  .divider-text {
    font-size: @font-size-xs;
    color: @text-hint;
  }
}

.quick-login-list {
  display: flex;
  gap: @space-md;
  justify-content: center;
  flex-wrap: wrap;
}

.quick-login-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: @space-xs;
  padding: @space-sm @space-md;
  border-radius: @radius-md;
  cursor: pointer;
  transition: all @transition-fast;
  border: 1px solid @border-color;
  min-width: 64px;

  &:hover {
    border-color: @color-primary;
    background: rgba(59, 130, 246, 0.04);
    transform: translateY(-2px);
    box-shadow: @shadow-sm;

    .quick-avatar {
      transform: scale(1.08);
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }
  }

  .quick-avatar {
    width: 40px;
    height: 40px;
    border-radius: @radius-circle;
    background: @color-primary-gradient;
    color: @text-inverse;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: @font-size-md;
    font-weight: 600;
    transition: all @transition-fast;
    box-shadow: 0 2px 6px rgba(59, 130, 246, 0.2);
  }

  .quick-name {
    font-size: @font-size-xs;
    color: @text-secondary;
    font-weight: 500;
  }
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式 */
@media (max-width: 992px) {
  .login-brand {
    display: none;
  }

  .login-form-wrapper {
    width: 100%;
  }
}
</style>
