<template>
  <div class="e-chart-container" :style="style">
    <div v-if="!loading" :id="id" :style="style2"></div>
<!--    <div class="full-screen-btn" @click="toggleFullScreen">-->
<!--      <fullscreen-outlined/>-->
<!--    </div>-->
    <div v-if="fullScreen" class="full-screen-container" :class="fullScreen?'open':''">

      <div class="full-screen-content" :class="fullScreen?'open':''" :style="{background:fullScreenBackground}">
        <div class="close" @click="toggleFullScreen">
          <close-circle-outlined/>
        </div>
        <div v-if="!loading" :id="id+'screen'" class="full-screen-e-chart"></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "EChart"
}
</script>
<script setup>
import * as echarts from 'echarts'
import '@/lib/echarts/liquidfill'
import {UUID} from "@/lib/tools";
import {ref, watch, onMounted, nextTick} from "vue";

let id = ref(UUID());

let props = defineProps({
  width: {
    type: [String, Number],
    default: '100%'
  },
  height: {
    type: [String, Number],
    default: '100%'
  },
  option: {
    type: Object,
    default: () => {
      return {}
    }
  },
  fullScreenBackground: {
    type: String,
    default: '#103B5D'
  }
})
let style = ref({
  width: typeof props.width === 'number' ? props.width + 'px' : props.width,
  height: typeof props.height === 'number' ? props.height + 'px' : props.height
})
let style2 = ref({
  width: typeof props.width === 'number' ? props.width + 'px' : '100%',
  height: typeof props.height === 'number' ? props.height + 'px' : '100%'
})
let loading = ref(true)
let myChart = null
let oldWidth = null

let emits = defineEmits(['clickEChart'])
const loadChart = (forceUpdate = false) => {
  console.log('loadChart')
  if (oldWidth) {
    if (!forceUpdate && document.getElementById(id.value) && oldWidth === document.getElementById(id.value).clientWidth) {
      return
    }
  }
  loading.value = true
  nextTick(() => {
    loading.value = false
    nextTick(() => {
      if (oldWidth === null) {
        oldWidth = document.getElementById(id.value).clientWidth
      }
      myChart = echarts.init(document.getElementById(id.value));
      // 使用刚指定的配置项和数据显示图表。
      myChart.clear();
      myChart.setOption(props.option)

      myChart.on('click', function (params) {
        emits('clickEChart', params, myChart)
      });
    })
  })
}
watch(() => props.option, () => {
  loadChart(true)
}, {deep: true})

onMounted(() => {
  console.log('onMounted')
  loadChart()
  let newVar = window['_sizeChange'] || [];
  newVar.push(loadChart)
  window['_sizeChange'] = newVar;
})
//  当窗口或者大小发生改变时执行resize，重新绘制图表
window.addEventListener("resize", function () {
  loadChart();
});
let fullScreen = ref(false)
const toggleFullScreen = () => {
  fullScreen.value = !fullScreen.value
  nextTick(() => {
    if (fullScreen.value) {
      let fullChart = echarts.init(document.getElementById(id.value + 'screen'));
      // 使用刚指定的配置项和数据显示图表。
      fullChart.clear();
      fullChart.setOption(props.option)
    }
  })
}

const setOption = (option) => {
  myChart && myChart.setOption(option)
}
const getInstance = () => {
  return myChart
}
defineExpose({
  setOption, getInstance
})
</script>
<style lang="less" scoped>
.e-chart-container {
  position: relative;

  .full-screen-btn {
    position: absolute;
    right: 10px;
    top: 10px;
    z-index: 999;
    cursor: pointer;
    color: #fff;
  }

  .full-screen-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    opacity: 0;
    visibility: hidden;
    transition: opacity 0.3s, visibility 0s 0.3s;
    z-index: 99999;

    &.open {
      opacity: 1;
      visibility: visible;
      transition-delay: 0s;
    }

    .full-screen-content {
      position: relative;
      margin: 20px;
      padding: 20px;
      width: calc(100% - 40px);
      height: calc(100% - 40px);
      opacity: 0;
      visibility: hidden;
      transition: opacity 0.3s, visibility 0s 0.3s;

      &.open {
        opacity: 1;
        visibility: visible;
        transition-delay: 0s;
      }

      .close {
        cursor: pointer;
        position: absolute;
        right: -12px;
        top: -21px;
        font-size: 26px;
        color: #fff;
      }

      .full-screen-e-chart {
        width: 100%;
        height: 100%;
      }
    }
  }
}
</style>
