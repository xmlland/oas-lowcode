<template>
  <div style="width: 100%;height: 100%;    overflow: hidden;">

    <template v-bind:key="frame.id" v-for="(frame) in iframeList">
      <iframe v-show="pageID===frame.id" :id="frame.id" style="border: 0;width: 100%;height: 100%" :src="frame.src"></iframe>
    </template>
  </div>

</template>

<script>
export default {
  name: "IFrameViewLayout"
}
</script>
<script setup>
import {useRoute} from "vue-router";
import {computed, ref, watch} from "vue";
import {useStore} from "vuex";

let route = useRoute()
let pageID = ref('')
let iframeList = ref([])

const store = useStore();

let deleteByKey = (key) => {
  let deleteIndex = -1
  iframeList.value.forEach((item, index) => {
    if (item.id === key) {
      deleteIndex = index
    }
  })
  if (deleteIndex > -1) {
    iframeList.value.splice(deleteIndex, 1)
  }
}

let activeTabs = computed(() => store.getters.getActiveTabs)
watch(() => [activeTabs.value, route], () => {
  let newKey = route.meta.pageID;
  let isNew = true
  let currentTabs = activeTabs.value.map(item => '/' + item.key);
  let deleteKeys = []
  iframeList.value.forEach((item, index) => {
    if (currentTabs.indexOf(item.id) >= 0) {
      deleteKeys.push(item.id)
    }
    if (item.id === newKey) {
      isNew = false
    }
  })
  deleteKeys.forEach(key => {
    deleteByKey(key)
  })
  if (route.meta.target&&route.meta.target.indexOf('iframe')>=0) {
    if (isNew) {
      iframeList.value.push({
        id: newKey,
        src: route.meta.pageUrl
      })
    }
    pageID.value = newKey
  }
}, {immediate: true, deep: true})
</script>
<style scoped>

</style>
