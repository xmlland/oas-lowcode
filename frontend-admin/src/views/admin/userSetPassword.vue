<template>
  <u-form v-model:value="passwordModel" :labelWidth="180">
    <a-col :span="12">
      <a-form-item name="oldPassword" label="旧密码" :validateFirst="true"
                   :rules="[{ required: true, message: '请输入旧密码' }]">
        <u-input :disabled="disabled"  v-model:value="passwordModel.oldPassword" placeholder="请输入旧密码" password/>
      </a-form-item>
    </a-col>
    <a-col :span="12">
      <a-form-item name="newPassword" label="新密码" :validateFirst="true"
                   :rules="[{ required: true, message: '请输入新密码' }]">
        <u-input :disabled="disabled" v-model:value="passwordModel.newPassword" placeholder="请输入新密码"/>
      </a-form-item>
    </a-col>
    <a-col :span="12">
      <a-form-item name="password" label="确认新密码" :validateFirst="true" :rules="[{ required: true, message: '请确认新密码' }]">
        <u-input :disabled="disabled" v-model:value="passwordModel.password" placeholder="请确认新密码"/>
      </a-form-item>
    </a-col>
    <button @click="save" class="ant-btn ant-btn-primary" type="button" ant-click-animating-without-extra-node="false">
      <!----><span>确 定</span></button>
  </u-form>
</template>

<script>
import {postAction} from "@/api/action";
import {message} from "ant-design-vue";

export default {
  //存放 数据
  data: function () {
    return {
      passwordModel: {
        oldPassword: "",
        newPassword: "",
        password: ""
      }
    }
  },
  //存放 方法
  methods: {
    save() {
      if (this.passwordModel.oldPassword == "" || this.passwordModel.newPassword == "" || this.passwordModel.password == "") {
        message.error("请输入密码")
        return;
      }
      var reg1 = /[a-z]+/g;
      var reg2 = /[A-Z]+/g;
      var reg3 = /[0-9]+/g;
      var reg4 = /[`\\~!@#$%^*()_+?:{},.;[\]|]/g;
      var count = 0;
      if (reg1.test(this.passwordModel.newPassword)) {
        count++;
      }
      if (reg2.test(this.passwordModel.newPassword)) {
        count++;
      }
      if (reg3.test(this.passwordModel.newPassword)) {
        count++;
      }
      if (reg4.test(this.passwordModel.newPassword)) {
        count++;
      }
      if (count < 3) {
        message.error("密码必须包含字母大写、字母小写、数字、特殊符号任意三种及以上")
        return;
      } else {
        if (this.passwordModel.newPassword == this.passwordModel.oldPassword) {
          message.error("新密码与旧密码相同，请修改。")
          return;
        }
        if (this.passwordModel.newPassword != this.passwordModel.password) {
          message.error("两次密码不一致。")
          return;
        }
        // 满足条件
        postAction('sys/user/changePassword?oldPassword=' + this.passwordModel.oldPassword + '&newPassword=' + this.passwordModel.newPassword, {}).then(() => {
          message.success('修改密码成功');
        })
      }
    }
  }
}
</script>
<script setup>
</script>

<style scoped>
button {
  position: absolute;
  left: 46.3%;
}
</style>