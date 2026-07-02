<template>
  <u-form v-model:value="formModel"
    v-model:disabled="disabled" :labelWidth="100" formNo="demo_260222"  >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item name="s01" label="单行文本" >
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.s01" placeholder="请输入单行文本" :maxlength="64" />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="i01" label="整数1" :rules="[{ validator : customValidator.numberValidator, message: '请输入整数'}]">
          <u-input type="number" :disabled="disabled" defaultValue="" v-model:value="formModel.i01" placeholder="请输入整数1"  />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="i02" label="整数2" :rules="[{ validator : customValidator.numberValidator, message: '请输入整数'}]">
          <u-input type="number" :disabled="disabled" defaultValue="" v-model:value="formModel.i02" placeholder="请输入整数2"  />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="b01" label="数字1" :rules="[{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.b01" placeholder="请输入数字1"  />
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="f01" label="开关" >
          <u-switch :disabled="disabled" defaultValue="false" v-model:value="formModel.f01"/>
        </a-form-item>
      </a-col>
      <a-col :span="12">
        <a-form-item name="b02" label="数字2" :rules="[{ validator : customValidator.digitsValidator, message: '请输入数字'}]">
          <u-input type="digits" :disabled="disabled" defaultValue="" v-model:value="formModel.b02" placeholder="请输入数字2"  />
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "demo_260222_form",
}
</script>
<script setup>

import {ref} from "vue";
import * as validator from "@/lib/validator"
let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)

</script>
<style scoped>
</style>
