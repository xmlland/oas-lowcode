<template>
  <div class="qr-code-container" :style="{width:size+'px',height:size+'px'}" :class="bordered?'bordered':''">
    <a @click="download">
      <img :src="dataUrl" alt="qr-code">
    </a>
  </div>
</template>

<script>
export default {
  name: "UQRCode"
}
</script>
<script setup>
import {ref, watch} from "vue";
import {generateQRCode} from "@/lib/tool-qrcode";

let props = defineProps({
  /**
   * 二维码内容
   */
  value: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: 'qrcode'
  },
  /**
   * 二维码尺寸
   */
  size: {
    type: Number,
    default: 160
  },
  /**
   * 二维码颜色
   */
  color: {
    type: String,
    default: '#000000'
  },
  /**
   * 二维码背景色
   */
  bgColor: {
    type: String,
    default: ''
  },
  /**
   * 二维码边框
   */
  bordered: {
    type: Boolean,
    default: true
  },
})
let dataUrl = ref('')
watch([() => props.value, () => props.size, () => props.color, () => props.bgColor], (val) => {
  if (props.value) {
    generateQRCode(props.value, props.size, props.color, props.bgColor).then(res => {
      dataUrl.value = res
    })
  }
}, {immediate: true})

const download = () => {
  const a = document.createElement('a');
  a.href = dataUrl.value;
  a.download = props.name + '.png';
  a.click();
}
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";

.qr-code-container {
  box-sizing: content-box;

  &.bordered {
    border: 1px solid @border-color-base;
  }
}
</style>
