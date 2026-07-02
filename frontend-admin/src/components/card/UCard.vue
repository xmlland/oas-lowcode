<template>
  <div class="card-container" :class="flex?'card-container-flex':''" :style="{padding:padding+'px'}">
    <div v-if="title" class="card-title"> {{ title }}
      <slot v-if="$slots.extra" name="extra"></slot>
    </div>
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: "UCard"
}
</script>
<script setup>

defineProps({
  flex: {
    type: Boolean,
    default: true
  },
  title: {
    type: String,
    default: ''
  },
  padding: {
    type: Number,
    default: 24
  }
})
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.card-container-flex {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.card-container {
  background: #FFFFFF;
  box-shadow: 6px 6px 54px rgba(0, 0 ,0,   0.05);
  border-radius: 14px;

  .card-title {
    height: 38px;
    padding-left: 16px;
    font-family: @main-font-name;
    font-style: normal;
    font-weight: 700;
    font-size: 18px;
    line-height: 38px;
    letter-spacing: 0.3px;
    color: var(--ant-primary-color);
    position: relative;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }

  .card-title::before {
    position: absolute;
    content: '';
    left: 0;
    top: 7px;
    width: 4px;
    height: 24px;
    background: var(--ant-primary-color);
    border-radius: 12px;
  }
}
</style>
