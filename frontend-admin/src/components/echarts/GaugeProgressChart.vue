<template>
  <e-chart :width="width" :height="height" :option="option"/>
</template>

<script>
export default {
  name: "GaugeProgressChart"
}
</script>
<script setup>
import '@/lib/echarts/liquidfill'
import {computed} from "vue";
import config from "@/config";
import EChart from "@/components/echarts/EChart";

let props = defineProps({
  width: {
    type: [String, Number],
    default: '100%'
  },
  height: {
    type: [String, Number],
    default: '100%'
  },
  title: {
    type: String,
    default: '完成率'
  },
  value: {
    type: Number,
    default: 50
  },
  progressWidth: {
    type: Number,
    default: 10
  },
  nameFontSize: {
    type: Number,
    default: 16
  },
  valueFontSize: {
    type: Number,
    default: 16
  },
  progressColor: {
    type: String,
    default: config.theme.primaryColor
  },
})
let option = computed(() => {
  return {
    series: [
      {
        type: 'gauge',
        startAngle: -90,
        endAngle: -450,
        progress: {
          show: true,
          width: props.progressWidth,
          itemStyle: {
            color: props.progressColor
          }
        },
        axisLine: {
          lineStyle: {
            width: props.progressWidth
          }
        },
        axisTick: {
          show: false
        },
        splitLine: {
          show: false
        },
        axisLabel: {
          show: false
        },
        pointer: {
          show: false
        },
        anchor: {
          show: false
        },

        data: [
          {
            value: props.value,
            name: props.title,
            title: {
              show: true,
              offsetCenter: [0, '30%'],
              fontSize: props.nameFontSize
            },
            detail: {
              show: true,
              offsetCenter: [0, '-15%'],
              formatter: function (value) {
                return value.toFixed(0) + '%';
              },
              fontSize: props.valueFontSize
            }
          }
        ]
      }
    ]
  }
})

</script>
<style scoped>

</style>
