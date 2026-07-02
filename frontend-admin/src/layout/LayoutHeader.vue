<template>
  <div class="layout-header">
    <div class="user-info" v-if="userName">

      <a-dropdown placement="bottom">
        <span class="text"> {{ userName }}</span>

        <template #overlay>
          <a-menu>
            <a-menu-item @click="logout" key="1">退出登录</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>

  </div>
</template>

<script>
export default {
  name: "LayoutHeader"
}
</script>
<script setup>
import {ref, onMounted, computed} from "vue";
import dayjs from 'dayjs';
import {useStore} from 'vuex'
import {useRouter} from 'vue-router'
import {getCurrentUserAction, getDictAction, logoutAction} from "@/api/api";
import {getToken, clearToken} from "@/api/action";
import {clearStorage, setStorage} from "@/lib/storage"

const store = useStore();
const router = useRouter();

let timeInfo = ref({
  date: '',
  week: '',
  time: ''
})

const setTimeInfo = () => {
  timeInfo.value.date = dayjs().format('YYYY-MM-DD')
  timeInfo.value.week = dayjs().format('dd')
  timeInfo.value.time = dayjs().format('HH:mm:ss')
}

setTimeInfo()
setInterval(() => {
  setTimeInfo()
}, 1000)

let userName = computed(() => store.getters.getUserName)
let emits = defineEmits(['loadSuccess'])

onMounted(() => {
  if (getToken()) {
    getCurrentUserAction().then(userRes => {
      store.commit('setUserView', userRes.data.userView)
      store.commit('setExtEntId', userRes.data.extEntId)
      emits('loadSuccess')
    })
  } else {
    emits('loadSuccess')
  }
  getDictAction('').then(dictRes => {
    dictRes.data.data.forEach(item => {
      if (!item.hasChildren) {
        setStorage('Dict_' + item.parentType + item.member, item.memberName)
      }
    })
  })
})

const logout = () => {
  logoutAction().then(res => {
    console.log('after userLogout', res)
    clearToken()
    clearStorage()
    store.commit('setUserView', {})
    store.commit('setExtEntId', 'null')
    router.push({
      path: '/',
      replace: true
    })

  })
}
</script>

<style lang="less" scoped>
@import "@/assets/less/main.less";
.layout-header {

  background: linear-gradient(0deg, rgba(20, 79, 58, 0.8), rgba(20, 79, 58, 0.8)), url(../assets/img/topbg.jpg);
  box-shadow: 0px 6px 24px rgba(0, 0, 0, 0.16);
  position: relative;

  .user-info {
    position: absolute;
    right: 24px;
    top: 24px;
    padding: 0 24px;
    height: 30px;
    line-height: 30px;
    background: rgba(255, 255, 255, 0.14);
    box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.06);
    backdrop-filter: blur(17px);
    /* Note: backdrop-filter has minimal browser support */
    border-radius: 100px;

    cursor: pointer;

    .text {
      font-family: @main-font-name;
      font-style: normal;
      font-weight: 500;
      font-size: @font-size-base;
      /* identical to box height, or 100% */

      letter-spacing: 0.3px;

      color: #FFFFFF;
    }
  }

}
</style>
