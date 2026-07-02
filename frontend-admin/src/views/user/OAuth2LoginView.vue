<template>
  <div></div>
</template>

<script>
export default {
  name: "OAuth2LoginView"
}
</script>
<script setup>
import {baseUrl, postActionByParams} from "@/api/action";
import {useRouter, useRoute} from "vue-router";
import {getCurrentUserAction} from "@/api/api";
import store from "@/store";
import {homePath} from "@/router";

const router = useRouter();
const route = useRoute();
//oauth2server的key 用于同一个服务有多个不同访问地址的情况
let oauth2ServerKey = window['oauth2ServerKey'] || '';
//获取当前访问的地址
let oauth2RedirectReferer = location.href.split('#')[0];
if (route.query.code) {
  postActionByParams(`/oauth2/login?code=${route.query.code}`, {
    oauth2ServerKey: oauth2ServerKey,
    oauth2RedirectReferer: oauth2RedirectReferer
  }).then(res => {
    getCurrentUserAction().then(userRes => {
      if (userRes) {
        store.commit('setUserView', userRes.data.userView)
        store.commit('setExtEntId', userRes.data.extEntId)
        router.push({
          path: homePath
        })
      }
    }).catch(err => {
    })
  })
} else {
  location.href = baseUrl + '/oauth2/login?oauth2ServerKey=' + oauth2ServerKey + '&oauth2RedirectReferer=' + oauth2RedirectReferer + '&oauth2_token=' + route.query['access_token'];
}

</script>
<style scoped>

</style>
