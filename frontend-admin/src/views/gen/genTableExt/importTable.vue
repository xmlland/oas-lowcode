<template>
  <a-spin :spinning="spinning" >
    <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="180"  :url="{
    save:'gen/genTable/importDynamic'
  }"  >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item ref="form" name="tableName" label="选择表名" :rules="[{ required: true, message: '请选择表名' }]">
            <a-select
                v-model:value="formModel.tableName"
                show-search
                allow-clear
                placeholder="请选择表名"
                style="width: 500px"
                :options="options"
                :filter-option="filterOption"
                @change="handleChange"
            ></a-select>

          </a-form-item>
        </a-col>
      </a-row>
    </u-form>
  </a-spin>

  <div style="height: 600px"></div>

</template>

<script setup>

import UForm from "@/components/form/UForm";
import {onBeforeMount, ref} from "vue";
import { sendRequest} from "@/api/action";
import emitter from "@/lib/event";

let formModel = ref({})
let disabled = ref(false)
const spinning = ref(false)
const tableList = ref(null)

const options = ref([]);
const handleChange = value => {
  emitter.emit('importTableName',formModel.value);
};

const filterOption = (input, option) => {
  return option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

onBeforeMount(() => {
  spinning.value = true

  function postAction(url, params) {
    return sendRequest({
      url: url,
      params,
      method: 'post'
    }, false)
  }

  postAction('gen/genTable/importListDynamic',).then(res => {
    tableList.value = res.data.tableList
    options.value = res.data.tableList.map(item => ({
      value: item.name,
      label: item.name
    }));

  }).finally(() => {
    spinning.value = false
  })
})

</script>

<script>
export default {
  name: "importTable"
}
</script>

<style scoped>

</style>
