<template>
  <div :id="id" :style="{
    width:typeof width === 'number' ? width + 'px' : width,
    height:typeof height === 'number' ? height + 'px' : height
  }"></div>
</template>

<script>
export default {
  name: "LiquidFill"
}
</script>
<script setup>
import * as echarts from 'echarts'
import '@/lib/echarts/liquidfill'
import {UUID} from "@/lib/tools";
import {onMounted, ref} from "vue";

let id = ref(UUID());

defineProps({
  width: {
    type: [String, Number],
    default: '100%'
  },
  height: {
    type: [String, Number],
    default: '100%'
  },
  title:{
    type: String,
    default: '完成率'
  },
  value:{
    type: Number,
    default: 0.5
  },

})
const loadChart = () => {

  //TODO 水球图
}
onMounted(()=>{

})
</script>
<style scoped>

</style>
