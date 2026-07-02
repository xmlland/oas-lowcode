<template>
  <slot v-if="hasAny()"></slot>
</template>

<script>
export default {
  name: "UPermission"
}
</script>
<script setup>

import {hasAnyPermission} from "@/lib/tools";

let props = defineProps({
  hasPermissions: {
    type: Array,
    default() {
      return null
    }
  }
})

const hasAny = () => {
  if (props.hasPermissions) {
    return hasAnyPermission(props.hasPermissions)
  } else {
    return true
  }
}
</script>
<style lang="less" scoped>

</style>
