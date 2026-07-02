<template>
  <a-col :span="24">
    <div class="business-form-title" :class="$slots.icon?'':'default-icon'">
      <div v-if="$slots.icon" class="business-form-title-icon">
        <slot name="icon"></slot>
      </div>
      <slot/>
    </div>
  </a-col>
</template>

<script>
export default {
  name: "UFormTitle"
}
</script>
<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
})
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.business-form-title {
  font-size: @font-size-base;
  font-weight: normal;
  color: #102039;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 5px 30px;

  &.default-icon&::before{
    content: '';
    display: block;
    border-radius: 100%;
    width: 12px;
    height: 12px;
    background: #102039;
    margin-right: 10px;
  }

  .business-form-title-icon{
    margin-right: 10px;
    height: 21px;
    line-height: 21px;
  }

  &::after{
    content: '';
    display: block;
    height: 1px;
    background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
    margin-left: 30px;
    flex: 1;
    opacity: .3;
  }
}
</style>
