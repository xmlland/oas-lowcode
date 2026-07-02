<template>
  <u-form v-model:value="formModel"
    v-model:disabled="disabled" :labelWidth="150" formNo="oa_attendance_location"  >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="location_name" label="地点名称" :rules="[{ required: true, message: '请输入地点名称' }]">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.location_name" placeholder="请输入地点名称" :maxlength="100" />
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="address" label="详细地址" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.address" placeholder="请输入详细地址" :maxlength="255" />
        </a-form-item>
      </a-col>
<!--      <a-col :span="12">
        <a-form-item name="longitude" label="经度" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.longitude" placeholder="请输入经度"  />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="latitude" label="纬度" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.latitude" placeholder="请输入纬度"  />
        </a-form-item>
      </a-col>-->
      <a-col :span="24">
        <a-form-item label="位置坐标">
          <u-lng-lat-select
              format="DD"
              :disabled="disabled"
              v-model:longitude="formModel.longitude"
              v-model:latitude="formModel.latitude"
              :DDRetainDecimals="6"
              buttonText="地图选点"
              @textAddressShow="(addr) => formModel.address = addr"
          />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="radius" label="打卡范围（米）" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.radius" placeholder="请输入打卡范围（米）"  />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="wifi_name" label="WiFi名称（SSID）" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.wifi_name" placeholder="请输入WiFi名称（SSID）" :maxlength="100" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="wifi_mac" label="WiFi MAC地址（BSSID）" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.wifi_mac" placeholder="请输入WiFi MAC地址（BSSID）" :maxlength="50" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="status" label="状态" >
          <u-select :disabled="disabled" form-type="select" defaultValue="" v-model:value="formModel.status" dict-type="sys_useable" placeholder="请选择状态"/>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "oa_attendance_location_form",
}
</script>
<script setup>

import {ref} from "vue";
import ULngLatSelect from "@/components/form/ULngLatSelect.vue";
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
