<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180" formNo="etl_log" >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="jobname" label="任务名称" >
          <u-input :disabled="disabled" v-model:value="formModel.jobname" placeholder="请输入任务名称"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="channel_id" label="通道ID" >
          <u-input :disabled="disabled" v-model:value="formModel.channel_id" placeholder="请输入通道ID"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="logdate" label="日志记录时间" >
          <u-date :disabled="disabled" v-model:value="formModel.logdate" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择日志记录时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="startdate" label="开始时间" >
          <u-date :disabled="disabled" v-model:value="formModel.startdate" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择开始时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="enddate" label="结束时间" >
          <u-date :disabled="disabled" v-model:value="formModel.enddate" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择结束时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="errors" label="错误数" >
          <u-input :disabled="disabled" v-model:value="formModel.errors" placeholder="请输入错误数"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="replaydate" label="重试时间" >
          <u-date :disabled="disabled" v-model:value="formModel.replaydate" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择重试时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="depdate" label="DEP时间" >
          <u-date :disabled="disabled" v-model:value="formModel.depdate" formatPatter="yyyy-MM-dd HH:mm:ss" placeholder="请选择DEP时间"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="status" label="状态" >
          <u-input :disabled="disabled" v-model:value="formModel.status" placeholder="请输入状态"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="log_field" label="日志内容" >
          <u-input :disabled="disabled" :textarea="true" v-model:value="formModel.log_field" placeholder="请输入日志内容"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="lines_written" label="写入数" >
          <u-input :disabled="disabled" v-model:value="formModel.lines_written" placeholder="请输入写入数"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="lines_input" label="输入数" >
          <u-input :disabled="disabled" v-model:value="formModel.lines_input" placeholder="请输入输入数"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="lines_rejected" label="拒绝数" >
          <u-input :disabled="disabled" v-model:value="formModel.lines_rejected" placeholder="请输入拒绝数"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="lines_read" label="读取数" >
          <u-input :disabled="disabled" v-model:value="formModel.lines_read" placeholder="请输入读取数"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="lines_updated" label="更新数" >
          <u-input :disabled="disabled" v-model:value="formModel.lines_updated" placeholder="请输入更新数"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="lines_output" label="输出数" >
          <u-input :disabled="disabled" v-model:value="formModel.lines_output" placeholder="请输入输出数"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "etl_log_form",
}
</script>
<script setup>

import {ref} from "vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>