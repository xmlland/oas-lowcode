<template>
  <div :style="{ background: '#fff',height:'100%'}">
    <router-view v-slot="{ Component }">
      <keep-alive>
        <component :is="Component"/>
      </keep-alive>
    </router-view>
  </div>
</template>

<script>
export default {
  name: "PlatformBlankLayout"
}
</script>
<script setup>
import {getCurrentInstance} from "vue";
import {useStore} from "vuex";
import {getCurrentUserAction} from "@/api/api";

let instance = getCurrentInstance()
const store = useStore();
getCurrentUserAction().then(userRes => {
  window.$instance = instance
  store.commit('setUserView', userRes.data.userView)
  store.commit('setExtEntId', userRes.data.extEntId)
})

</script>
<style lang="less" scoped>
.platform-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: row;
  align-items: flex-start;

  .platform-left {
    height: 100%;
    width: 206px;
    box-shadow: 1px 0 8px 3px #ccc;

    .logo {
      width: 100px;
      height: 100px;
      margin: 10px 53px;
    }

    :deep(.user-info) {
      justify-content: center;

      .user-info-item {
        background: #ebebeb;
        border-radius: 20px;
        padding: 5px 40px;

        .ant-dropdown-trigger {
          color: var(--ant-primary-color);
        }
      }
    }
  }

  .platform-content {
    width: calc(100% - 216px);
    height: 100%;
    margin-left: 10px;
  }
}
</style>
