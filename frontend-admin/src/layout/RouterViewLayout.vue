<template>
    <router-view v-slot="{ Component }">
      <component v-if="$route.meta&&$route.meta.noKeepAlive" :is="Component"/>
      <keep-alive v-else>
        <component :is="Component"/>
      </keep-alive>
    </router-view>
</template>

<script>
export default {
  name: "RouterViewLayout"
}
</script>

<style scoped>

</style>
