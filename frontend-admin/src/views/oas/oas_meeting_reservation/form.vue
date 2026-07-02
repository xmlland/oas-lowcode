<template>
  <u-form v-model:value="formModel"
          v-model:disabled="disabled" :labelWidth="100" formNo="oas_meeting_reservation"  >
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="meeting_room" label="会议室" :rules="[{ required: true, message: '请选择会议室' }]">
          <u-modal-select formNo="oas_meeting_room"
                          :searchKey="['name']"
                          :searchLabel="['名称']"
                          :columns="[
                           {title:'名称',dataIndex:'name',align:'center'},
                           {title:'位置',dataIndex:'position',align:'center'},
                           {title:'人数',dataIndex:'people_number',align:'center'},
                           {title:'设备',dataIndex:'equipment',align:'center'},
                           {title:'状态',dataIndex:'status',align:'center',dict:'oas_meeting_root_manage_state'},
                          ]"
                          :modalWidth="1200" modalTitle="请选择"
                          :filterData="[{type:'ne', value:'0', key:'a.status'}]"
                          :searchConfig="{}"
                          :formUpdateMap={}
                          v-model:formModel="formModel"
                          :disabled="disabled" defaultValue="" v-model:value="formModel.meeting_room" placeholder="请选择会议室"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="name" label="会议名称" :rules="[{ required: true, message: '请输入会议名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.name" placeholder="请输入会议名称" :maxlength="64" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="booker" label="预订人" :rules="[{ required: true, message: '请选择预订人' }]">
          <u-user-select :disabled="disabled" defaultValue="${currentUseer}" v-model:value="formModel.booker" dataScope="all" :modalWidth="1000" placeholder="请选择预订人"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="mobile_phone" label="手机" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.mobile_phone" placeholder="请输入手机" :maxlength="64" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item label="预定时段" :rules="[{ validator: validateTimeRange }]">
          <u-timeline-picker
              :disabled="disabled"
              v-model:startTime="startTimeStr"
              v-model:endTime="endTimeStr"
              :meetingRoomId="meetingRoomId"
              :currentId="formModel.id || ''"
          />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="participants" label="参会人员" >
          <u-users-select :disabled="disabled" defaultValue="" v-model:value="formModel.participants" dataScope="all" :modalWidth="1200" placeholder="请选择参会人员"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="reminder_method" label="提醒方式" >
          <u-select :disabled="disabled" :multiple="true" form-type="checkbox" defaultValue="" v-model:value="formModel.reminder_method" dict-type="oas_meeting_manage_reminders" placeholder="请选择提醒方式" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="remarks" label="备注信息" >
          <u-input :disabled="disabled" :textarea="true" defaultValue="" v-model:value="formModel.remarks" placeholder="请输入备注信息" :maxlength="255"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oas_meeting_reservation_form",
}
</script>
<script setup>

import {ref, computed, watch} from "vue";
let formModel = ref({})
let disabled = ref(false)

const meetingRoomId = computed(() => {
  return formModel.value.meeting_room?.id || ''
})

const startTimeStr = ref('')
const endTimeStr = ref('')

// Sync from formModel to local refs (for edit mode)
watch(() => formModel.value.start_time, (val) => {
  if (val) startTimeStr.value = val
}, { immediate: true })

watch(() => formModel.value.end_time, (val) => {
  if (val) endTimeStr.value = val
}, { immediate: true })

// Sync from timeline picker back to formModel
watch(startTimeStr, (val) => {
  formModel.value.start_time = val
})

watch(endTimeStr, (val) => {
  formModel.value.end_time = val
})

const validateTimeRange = (rule, value) => {
  if (!startTimeStr.value || !endTimeStr.value) {
    return Promise.reject('请在时间轴上拖拽选择预定时段')
  }
  return Promise.resolve()
}

</script>
<style scoped>
</style>
