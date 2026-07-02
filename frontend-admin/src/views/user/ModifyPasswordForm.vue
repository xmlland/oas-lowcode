<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :url="{save:'sys/user/changePassword'}" formNo="user" :labelWidth="100" :getExtendSaveData="getExtendSaveData">
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item :extra="'密码至少'+minPasswordLength+'位，由数字、大小写字母和特殊符号（@#$%^&*）组成；修改密码后需重新登录,请谨慎操作'">
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="oldPassword" label=""
                     :rules="[{ required: true, message: '请输入当前密码' }]">
          <a-input-password v-model:value="formModel.oldPassword" placeholder="请输入当前密码">
            <template #addonBefore>
              <safety-outlined/>
            </template>
          </a-input-password>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="newPassword" label=""
                     :rules="[{ required: true, message: '请输入新密码' },{ validator : customValidator.passwordValidator, message: '密码不符合要求'},{ validator : checkNewPassword, message: '不得与当前密码相同'}]">
          <a-input-password v-model:value="formModel.newPassword" placeholder="请输入新密码">
            <template #addonBefore>
              <safety-outlined/>
            </template>
          </a-input-password>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="checkPassword" label=""
                     :rules="[{ required: true, message: '请再次输入新密码' },{ validator : checkPassword, message: '两次输入的密码不一致'}]">
          <a-input-password v-model:value="formModel.checkPassword" placeholder="请再次输入新密码">
            <template #addonBefore>
              <safety-outlined/>
            </template>
          </a-input-password>
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "ModifyPasswordForm"
}
</script>
<script setup>

import {computed, ref} from "vue";
import * as validator from "@/lib/validator"
import UForm from "@/components/form/UForm";
import {encryptByDESModeEBC} from "@/lib/cryptoJS-aes";
import {useStore} from "vuex";
const store = useStore();
let minPasswordLength = computed(() => store.getters.getMinPasswordLength)
let customValidator = ref(validator)
let formModel = ref({})
let disabled = ref(false)

const checkPassword = async (rule, value) => {
  if (!value || value === formModel.value.newPassword)
    return Promise.resolve()
  return Promise.reject('password must equal');
}
const checkNewPassword = async (rule, value) => {
  if (!value || value !== formModel.value.oldPassword)
    return Promise.resolve()
  return Promise.reject('password must equal');
}

const getExtendSaveData = () => {
  return new Promise((resolve, reject) => {
    resolve({
      oldPassword: encryptByDESModeEBC(formModel.value.oldPassword),
      newPassword: encryptByDESModeEBC(formModel.value.newPassword)
    })
  })
}
</script>
<style scoped>

</style>
