<template>
  <div id="app">
    <div id="content" class="custom-content">
      <div class="main-body pad1">
        <div class="article-wrapper">
          <div class="info">
            <h2 style="text-align: center">{{ title }}</h2>
            <h5 class="author">发布人：{{author}}</h5>
            <h5 class="publishTime">发布时间：{{publishTime}}</h5>
          </div>
          <div class="article-content" v-html="contentHtml" @change="changeTab"></div>
        </div>
        <div class="footer">Copyright © 版权所有</div>
      </div>
    </div>
  </div>
</template>

<script>
</script>
<script setup>
import {ref, onMounted} from "vue";
import {postAction} from "@/api/action";

const contentHtml = ref('');
const title = ref('');
const author = ref('');
const publishTime = ref('');
let props = defineProps({
  singleTable: {
    type: Object,
    default() {
      return {}
    }
  }
})
let fullURL = window.location.href;

onMounted(()=>{

  let param = props.singleTable.initParam || {}
  let queryString = fullURL.split('?')[1];

  // 将查询参数部分拆分成键值对数组
  let params = queryString.split('&');

  // 遍历参数数组，找到指定的参数并获取其值
  let parentid, value,id;
  params.forEach(function(param) {
    let parts = param.split('=');
    let key = parts[0];
    let val = parts[1];
    if (key === 'parentid') {
      parentid = val;
    } else if (key === 'value') {
      value = val;
    } else if (key === 'id') {
      id =val
    }
  });
  postAction('dynamic/zform/datamap?path=path&traceFlag=&formNo=prt_information&parentId='+parentid, param).then(infoRes => {
    if (id === '0') {
      contentHtml.value = infoRes.rows[value].content;
      title.value = infoRes.rows[value].title;
      author.value = infoRes.rows[value].author;
      publishTime.value = infoRes.rows[value].release_date;
    } else {
      for (let i = 0; i < infoRes.rows.length; i++) {
        if (id === infoRes.rows[i].id) {
          contentHtml.value = infoRes.rows[i].content;
          title.value = infoRes.rows[i].title;
          author.value = infoRes.rows[i].author;
          publishTime.value = infoRes.rows[i].release_date;
        }
      }
    }
  })
})
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
//.custom-content {
//  font-size: 14px;
//  display: inline-block;
//  margin-right: 20px;
//  text-align: center;
//  color: #333;
//}
.author {
  font-size: 14px;
  display: inline-block;
  margin-right: 20px;
  text-align: center;
  color: #333;
}
.publishTime {
  font-size: 14px;
  display: inline-block;
  margin-right: 20px;
  text-align: center;
  color: #333;
  padding: 0px 20px;
}
.article-wrapper {
  max-width: 60vw;
  margin: 10px auto 0;
  padding: 20px;
  text-align: center;
  border: 1px solid #e7eaec; /* 添加边框样式 */
  padding: 10px 10px; /* 添加文字和边框的间距 */
}
.article-content {
  text-indent: 2em;
  max-width: 60vw;
  margin: 0 auto;
  padding: 0px 10px 30px;
  line-height: 1.5;
  text-align: left;
  box-sizing: border-box;
}
.info{
  border-bottom: 1px solid #e7eaec;
  padding: 10px 0px;
}
.main-body.pad1 {
  padding: 10px 0 78px;
}
.main-body {
  border-top: 1px solid #e7eaec;
  padding-top: 30px;
  padding-bottom: 38px;
  box-sizing: border-box;
}
.footer {
  background: #fff;
  font-size: 12px;
  color: #4a4a4a;
  text-align: center;
  height: 50px;
  line-height: 37px;
  border-top: 1px solid #e8ebed;
  position: fixed;
  bottom: 0px;
  left: 0px;
  width: 100%;
}
</style>
