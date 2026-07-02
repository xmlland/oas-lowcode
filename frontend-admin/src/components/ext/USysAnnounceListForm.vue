<template>
  <u-form-container title="通知公告">
    <template #icon>
      <img src="/imgs/ext/announcementIcon.png">
    </template>
    <div class="form-container-insideBox" style="overflow: scroll;">
      <template v-for="(item,key) in announceList" :key="key">
        <u-message-link :item="item" :ref="(el)=>setLinkRef(el, 'link_'+item.id)" @onOpenModal="onOpenModal" @modalClickOk="modalClickOk"
                        :title="item.title" :color="item.message_color" :time="item.update_date" :href="item.menu_href" :content="item.content_" :toHtml="true"></u-message-link>
      </template>
    </div>
  </u-form-container>
</template>

<script>
export default {
  name: "USysAnnounceListForm",
}
</script>
<script setup>
import {ref, nextTick, computed} from "vue";
import {listDataAction, saveDataAction} from "@/api/api";
import UFormContainer from "@/components/ext/UFormContainer";
import store from "@/store";

let userView = computed(() => store.getters.getUserView);

let props =  defineProps({
  getAnnounceList:{
    type:Function,
    default: (r) =>{
      return r;
    }
  },
})

let announceList = ref([]);

const getAnnouncements = () =>{
  listDataAction('oa_sys_announcement',{
    pageParam: {pageNo: 1, pageSize: 50},
  },'',{queryUnread:'queryUnread'}).then(res=>{
    announceList.value = props.getAnnounceList(res.rows);
    getAnnouncementsRead();
  })
}

getAnnouncements();

const onOpenModal = ()=>{
  nextTick(()=>{
    let noDownloadVideos =  document.getElementsByClassName("noDownloadVideo");
    for(let v of noDownloadVideos){
      v.setAttribute("controlsList", "nodownload");
    }
  })
}

const linkRefs = {};
const setLinkRef = (el, key) => {
  if (el) {
    linkRefs[key] = el
  }
}

const openModal = (id) =>{
  let linkId = 'link_'+id;
  linkRefs[linkId].openModal();
}

let readIdMap = {};

const getAnnouncementsRead = () =>{
  listDataAction('oa_sys_announcement_read',{
    user_id:userView.value.id,
    queryParamType:{
      user_id:''
    }
  }).then(res=>{
    let rIdMap = {}
    for(let k in res.rows){
      let i = res.rows[k];
      rIdMap[i.announcement_id] = i;
    }
    readIdMap = rIdMap;

    for(let o of announceList.value){
      if(o.automatically_pop==='1'){
        if(!rIdMap[o.id]){
          openModal(o.id);
        }
      }
    }

    setTimeout(()=>{
      getAnnouncements();
    },1000*60*5)
  })
}

const modalClickOk = (item) =>{
  if(!readIdMap[item.id]){
    saveDataAction("oa_sys_announcement_read",{
      user_id:userView.value.id,
      announcement_id: item.id
    }).then((res)=>{})
        .catch((err)=>{})
  }
}

</script>

<style scoped>
.form-container-insideBox {
  height: 250px;
}
</style>
