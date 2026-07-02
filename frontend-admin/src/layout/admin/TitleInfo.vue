<template>
  <div class="title-info">
    <img v-if="showLogo" :src="logo" alt="logo" class="logo">
    <span>{{ subSystemCode ? `${subSystemMap[subSystemCode]}` : title }}</span>
  </div>
</template>

<script>
export default {
  name: "TitleInfo"
}
</script>

<script setup>
import config, { defaultLogo } from "@/config"
import {ref, onMounted, computed, watch} from "vue";
import {listDataAction} from "@/api/api";
import {useStore} from "vuex";

defineProps({
  showLogo: {
    type: Boolean,
    default: true
  }
})
const store = useStore();
let subSystemCode = computed(() => store.getters.getSubSystemCode)

let title = ref(config.sysTitle)
let logo = ref(defaultLogo)
let subSystemMap = ref({})
watch(() => subSystemCode.value, () => {
  setTitle()
})
const setTitle = () => {
  document.title = title.value + (subSystemCode.value ? ` - ${subSystemMap.value[subSystemCode.value]}` : '')
}
onMounted(() => {
  listDataAction('sys_subsystem').then(res => {
    res.rows.forEach(item => {
      subSystemMap.value[item.code] = item.name
    })
  })
  setTitle()
})
</script>
<style lang="less" scoped>
.title-info {
  height: 64px;
  line-height: 64px;
  text-align: center;
  font-size: 16px;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  color: #ffffff;
  background: transparent;
  padding-right: 24px;

  &.menu-theme-light, &.menu-theme-dark {

    &.menu-location-left {
      color: #000;
      padding-left: 22px;
      padding-right: 0;
    }
  }

  .logo {
    width: 36px;
    margin-right: 22px;
  }
}
</style>
