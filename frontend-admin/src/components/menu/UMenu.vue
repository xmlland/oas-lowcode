<template>
  <div class="u-menu">
    <div @click="changeActive(item)" class="u-menu-item" :class="activeKey===item.value?'active':''"
         v-bind:key="item.value" v-for="item in data">
      <u-icon :image="item.image"/>
      <span> {{ item.text }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: "UMenu"
}
</script>
<script setup>

defineProps({
  data: {
    type: Array,
    default() {
      return []
    }
  },
  activeKey: {
    type: String,
    default: ''
  }
})

let emits = defineEmits(['update:activeKey', 'change'])
const changeActive = (item) => {
  if ('_blank' !== item.target) {
    emits('update:activeKey', item.value)
  }
  emits('change', {key: item.value, target: item.target})
}
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.u-menu {
  display: flex;
  height: 56px;
  align-items: center;

  .u-menu-item {
    cursor: pointer;
    height: 35px;
    margin: 0 16px;
    padding: 0 24px;
    display: flex;
    align-items: center;
    position: relative;
    opacity: .6;

    span {
      padding-left: 6px;
      font-family: @main-font-name;
      font-style: normal;
      font-weight: 300;
      font-size: 16px;
      line-height: 23px;
      letter-spacing: 0.3px;
      color: #FFFFFF;
    }

  }

  .u-menu-item.active {
    background: rgba(255, 255, 255, 0.14);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.06);
    backdrop-filter: blur(17px);
    /* Note: backdrop-filter has minimal browser support */
    border-radius: 100px;
    opacity: 1;
  }

  .u-menu-item::after {
    content: '';
    position: absolute;
    height: 20px;
    top: 7.5px;
    right: -16px;
    width: 1px;
    background: #fff;
  }

  .u-menu-item:last-child::after {
    content: unset;
  }
}
</style>
