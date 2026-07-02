<template>
  <u-form-container title="最新动态">
    <template #icon>
      <img src="/imgs/ext/sysMsgListIcon.png">
    </template>
    <a-input-search
        v-model:value="searchText"
        placeholder="搜索标题"
        @search="searchTitle"
        size="small"
    />
    <div class="form-container-insideBox" style="overflow: scroll">
      <template v-for="(item,key) in newsList" :key="key">
        <u-message-link :title="item.title" :color="item.types" :time="item.update_date" :href="item.menu_href" :content="item.content_"
                        :item="item"  :showDelButton="true" @clickDel="clickDel"></u-message-link>
      </template>
    </div>
  </u-form-container>
</template>

<script>
export default {
  name: "USysMsgListForm",
}
</script>
<script setup>
import {ref} from "vue";
import {postAction} from "@/api/action";
import {defaultTableViewUrl, saveDataAction} from "@/api/api";
import UFormContainer from "@/components/ext/UFormContainer";
import {Modal} from "ant-design-vue";

let searchText = ref('');
let newsList = ref([]);

const getNewsList = (data = {
  pageParam: {pageNo: 1, pageSize: 50},
})=>{
  postAction(
      defaultTableViewUrl.list
          .replace('${parentId}','')
          .replace('${formNo}','oa_sys_msg')
      ,data).then(res=>{
    newsList.value = res.rows;
  })
}

getNewsList();

const searchTitle = (value) =>{
  if(!value||value===''){
    getNewsList();
  }else {
    getNewsList({
      pageParam: {pageNo: 1, pageSize: 50},
      title:value,
      queryParamType:{
        title:''
      }});
  }
}

const clickDel = (v)=>{
  Modal.confirm({
    title:'确认删除',
    content:'删除通知“'+v.title+'”？',
    onOk: ()=>{
      saveDataAction('oa_sys_msg',{
        id:v.id,
        need_hide: "1",
        update_date: v.update_date
      }).then((res)=>{
        getNewsList();
      })
    }
  })
}

const flash=()=>{
  getNewsList();
}

defineExpose({
  flash
})

</script>
<style scoped>
.form-container-insideBox {
  height: calc(250px - 1.7rem);
}
</style>
