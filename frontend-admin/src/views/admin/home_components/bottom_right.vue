<template>
  <div class="box">
    <div>
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" style="margin-left: 13px;" viewBox="0 0 16 16">
        <path d="M4.745 1.777a.516.516 0 1 0 1.007-.224L5.496.403A.516.516 0 0 0 4.49.627l.255 1.15ZM1.023 3.535l.994.633a.516.516 0 0 0 .554-.87l-.994-.633a.516.516 0 0 0-.554.87ZM.628 8.043l1.15-.256a.516.516 0 0 0-.223-1.008l-1.15.256a.516.516 0 1 0 .223 1.008Zm10.238-2.28a.535.535 0 0 0 .112-.012l1.15-.256a.516.516 0 1 0-.224-1.008l-1.15.256a.516.516 0 0 0 .112 1.02ZM8.522 2.728a.516.516 0 0 0 .712-.158l.633-.994a.516.516 0 0 0-.87-.554l-.633.994a.516.516 0 0 0 .158.712ZM2.819 7.032c.071.303.182.596.331.87a3.13 3.13 0 0 0 .908-.486 2.453 2.453 0 0 1-.232-.608A2.504 2.504 0 0 1 8.714 5.72l.004.038a5.42 5.42 0 0 1 1.064.25 3.51 3.51 0 0 0-.061-.512 3.535 3.535 0 0 0-6.902 1.536Zm9.175 7.364A4.758 4.758 0 0 1 8.406 16a4.76 4.76 0 0 1-3.537-1.547 2.908 2.908 0 0 1-1.056.197C2.258 14.65 1 13.441 1 11.95s1.26-2.7 2.813-2.7c.173 0 .342.015.507.044C5.124 7.924 6.652 7 8.406 7c1.769 0 3.308.94 4.107 2.328a2.93 2.93 0 0 1 .675-.078c1.553 0 2.812 1.209 2.812 2.7s-1.26 2.7-2.813 2.7a2.9 2.9 0 0 1-1.193-.254Z"/>
      </svg>
      <span class="ze">天气情况</span>
    </div>
    <div class="tianqi">
      <div style="" id="he-plugin-standard"></div>
    </div>
  </div>
</template>

<script>

export default {
  name: "admin_home",
  components: {},
  data() {
    return {}
  },
  //天气插件
  created() {
    window.WIDGET = {
      "CONFIG": {
        "layout": "2",
        "width": 288,
        "height": 400,
        "background": "5",
        "language": "zh",
        "dataColor": "000000",
        "borderRadius": "5",
        "key": "9dde943b49b140279784a0273a612035"
      }
    }
    var script = document.createElement('script')
    script.type = 'text/javascript'
    script.src = 'https://widget.qweather.net/standard/static/js/he-standard-common.js?v=2.0'
    document.getElementsByTagName('head')[0].appendChild(script)
  },
}
</script>
<script setup>
// import {ref} from "vue";
// const value = ref();
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.box {
  width: 100%;
  height: 100%;
  background: #fff;
}
.tianqi{
  //margin-top: 10px;
  overflow-x:auto;
  height: calc(100% - 28px);
  width: auto;
}
.ze{
  margin-left: 15px;
  font-size : @font-size-base;
}
</style>
