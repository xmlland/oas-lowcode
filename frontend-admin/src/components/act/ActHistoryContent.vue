<template>
  <u-tabs>
    <a-tab-pane :key="0" tab="历史信息">
      <single-table :height="height" :showRowButtons="false" :pagination="false" :rowSelection="false"
                    :columns="[
                                  {title: '任务名称', dataIndex: ['actMap','histTaskName'], minWidth: 80, align: 'left',sorter:'false'},
                                  {title: '办理人', dataIndex: ['actMap','assignee'], minWidth: 120, align: 'left',sorter:'false'},
                                  {title: '办理意见', dataIndex: 'comment', minWidth: 150, align: 'left',sorter:'false'},
                                  {title: '开始时间', dataIndex: ['actMap','startTime'], minWidth: 120, align: 'center',sorter:'false'},
                                  {title: '结束时间', dataIndex: ['actMap','endTime'], minWidth: 120, align: 'center',sorter:'false'},
                                  /*{title: '任务历时', dataIndex:['actMap','durationTime'], minWidth: 80, align: 'center',sorter:'false'},*/
                              ]"
                    :data="procTableData"
      />
    </a-tab-pane>
    <a-tab-pane :key="1" tab="流程图">
      <div style="overflow: auto;" :style="{height:(height)+'px'}">
        <img :src="procImgData"/>
      </div>
    </a-tab-pane>
  </u-tabs>
</template>

<script>
export default {
  name: "ActHistoryContent"
}
</script>
<script setup>

import UTabs from "@/components/nav/UTabs";
import SingleTable from "@/components/table/SingleTable";
import {computed, ref, watch} from "vue";
import {getAction} from "@/api/action";
let height = computed(() => {
  return document.body.clientHeight * 0.9 - 160 - 80
})
let props = defineProps({
  procInsId: {
    type: String,
    default: ''
  }
})
let procTableData = ref([])
let procImgData = ref(null)
watch(() => props.procInsId, (newVal) => {
  if (newVal) {
    procTableData.value = []
    getAction('dynamic/zform/histoicFlow', {procInsId: props.procInsId}).then(hisRes => {
      procTableData.value = hisRes.data.data
    })
    getAction('dynamic/zform/tracePhoto', {procInsId: props.procInsId}).then(imgRes => {
      procImgData.value = 'data:image/png;base64,' + imgRes.data.data
    })
  }
}, {immediate: true})
</script>

<style scoped>

</style>
