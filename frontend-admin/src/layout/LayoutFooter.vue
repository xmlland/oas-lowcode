<template>
  <div class="layout-footer">

    <div class="copyright">版权所有：{{ footerData.copyright }}</div>
    <div class="org-info">
      地址：{{ footerData.orgAddress }}&nbsp;&nbsp;&nbsp;
      邮政编码：{{ footerData.orgPostal }}&nbsp;&nbsp;&nbsp;
      ICP备案编号：{{ footerData.icpCode }}
    </div>
<!--    <div class="dev-info">技术支持：{{ footerData.devName }}&nbsp;&nbsp;&nbsp;联系电话：{{ footerData.devTel }}</div>-->

  </div>
</template>

<script>
export default {
  name: "LayoutFooter",

}
</script>
<script setup>
import {computed, ref, watch} from "vue";
import {useStore} from "vuex";

const store = useStore();
let footerData = ref({
  copyright: '',
  orgAddress: '',
  orgPostal: '',
  icpCode: 'XXXX',
  devName: '北京中联普瑞科技有限公司',
  devTel: '010-82461977'
})
let footer = computed(() => store.getters.getFooter)
watch(() => footer, () => {
  Object.assign(footerData.value, footer.value)
}, {deep: true, immediate: true})

</script>

<style lang="less" scoped>
@import "@/assets/less/main.less";
.layout-footer {
  text-align: center;
  display: flex;
  flex-direction: column;
  flex-wrap: nowrap;
  height: 40px;
  justify-content: space-between;
  background: #F5F8FA;

  .copyright {
    font-family: @main-font-name;
    font-style: normal;
    font-weight: 400;
    font-size: 12px;
    line-height: 12px;
    /* identical to box height, or 100% */
    display: flex;
    align-items: center;
    justify-content: center;
    color: #BCBCBC;
  }

  .org-info {
    font-family: @main-font-name;
    font-style: normal;
    font-weight: 400;
    font-size: 12px;
    line-height: 12px;
    /* identical to box height, or 100% */

    display: flex;
    align-items: center;
    justify-content: center;

    color: #BCBCBC;
  }

  .dev-info {
    font-family: @main-font-name;
    font-style: normal;
    font-weight: 400;
    font-size: 12px;
    line-height: 12px;
    /* identical to box height, or 100% */

    display: flex;
    align-items: center;
    justify-content: center;

    color: #7AB19E;
  }
}
</style>
