<template>
  <img v-if="customImage" :src="customImage" :class="extClass">
  <i v-else-if="!image" :class="[faClass,extClass]"></i>
  <img v-else-if="image" :src="'icon/'+image+'.png'" :class="extClass">
</template>

<script>
import 'font-awesome/css/font-awesome.min.css'

export default {
  name: "UIcon"
}
</script>
<script setup>
import {ref, watch} from "vue";

let props = defineProps({
  type: {
    type: String,
    default: ''
  },
  extClass: {
    type: String,
    default: ''
  },
  image: {
    type: String,
    default: null
  }
})

let faClass = ref(null)
let customImage = ref(null)
watch(() => props.type, () => {
  let _type = props.type
  if (!_type) {
    _type = 'fa-file'
  }
  if (typeof _type === 'string' && _type.indexOf('fa-') === -1) {
    customImage.value = _type
  } else {
    faClass.value = 'fa ' + (_type === '' ? 'fa-file' : props.type)
  }
}, {immediate: true})
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
img {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

.menu-icon {
  font-size: @font-size-base;
}

img {
  width: 16px;
}
</style>
