<template>
  <div class="u-title-container">
    <div class="u-title">
      <slot/>
    </div>
    <slot v-if="$slots.extra" name="extra"></slot>
  </div>
</template>

<script>
export default {
  name: "UTitle"
}
</script>

<style lang="less" scoped>
@import "@/assets/less/main.less";
.u-title-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;

  .u-title {
    position: relative;
    display: flex;
    align-items: center;
    padding: 12px 0;
    font-size: @font-size-base;
    background: transparent;
    border: 0;
    outline: none;
    cursor: pointer;
    color: var(--ant-primary-color);
    text-shadow: 0 0 0.25px currentcolor;
    margin: 0 0 16px 0;
  }

  .u-title::after {
    content: '';
    position: absolute;
    background: var(--ant-primary-color);
    pointer-events: none;
    bottom: 0;
    height: 2px;
    width: 100%;
  }
}
</style>
