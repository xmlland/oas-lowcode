<template>
  <slot v-if="hasAny()"></slot>
</template>

<script>
export default {
  name: "URole"
}
</script>
<script setup>

import {hasAnyRole} from "@/lib/tools";

let props = defineProps({
  hasRoles: {
    type: Array,
    default() {
      return null
    }
  },
  /**
   * 是否反向判断
   */
  reverse: {
    type: Boolean,
    default: false
  }
})

const hasAny = () => {
  if (props.hasRoles) {
    if (props.reverse){
      return !hasAnyRole(props.hasRoles)
    }
    return hasAnyRole(props.hasRoles)
  } else {
    return true
  }
}
</script>
<style lang="less" scoped>

</style>
