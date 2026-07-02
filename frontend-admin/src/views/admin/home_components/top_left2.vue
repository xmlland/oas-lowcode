<template>
  <div class="left-item">
    <div class="pr">
      <div class="title" style="display: flex; align-items: center;">
        <ProfileOutlined
            style="color: var(--ant-primary-color)"/>
        <span class="xwzx">消息中心</span>
      </div>
      <a-tabs class="rightTab" v-model:activeKey="activeKey" @change="changeTab">
        <a-tab-pane key="2" tab="系统消息" force-render>
          <template v-if="false">
            <USysMsgListForm></USysMsgListForm>
          </template>
        </a-tab-pane>
      </a-tabs>
    </div>
    <div class="box-left">
      <div class="left-img" v-if="imgsList.length > 0">
        <a-carousel class="img-list" :autoplay="true">
          <a-carousel-item v-for="(item, index) in imgsList" :key="index">
            <div>
              <img :src="item.imageSrc" alt="" class="slide-img">
              <div class="caption">
                <div style="width:300px;overflow:hidden;text-overflow:ellipsis;white-space: nowrap;"><a
                    class="custom-link" @click="openTab(item)">{{ item.caption }}</a></div>
              </div>
            </div>
          </a-carousel-item>
        </a-carousel>
      </div>
      <div class="right-list">
        <ul v-if="newsList.length > 0">
          <li v-for="(item,index) in newsList.slice(0,9)" :key="index">
            <a @click="openTab(item)" target="_blank" :menuName="item.menu_name">{{ item.content }}</a>
          </li>
        </ul>
        <div v-else class="empty-state">
          <a-empty
              description="暂无消息"
              :image="aEmptyImage"
              imageStyle="{ height: '64px' }"
          />
        </div>
      </div>
    </div>
    <div class="more_button">
      <a @click="openMore(item)" class="more-aaa">更多>>></a>
    </div>
  </div>
</template>

<script>
export default {
  name: "admin_home2",
}
</script>
<script setup>
/*eslint-disable*/
import {ref, onMounted, inject} from "vue";
import {postAction} from "@/api/action";
import {defaultTableViewUrl} from "@/api/api";
import {Empty} from "ant-design-vue";
const aEmptyImage = Empty.PRESENTED_IMAGE_SIMPLE;

const dynamicData = ref([])
const activeKey = ref('2');
const newsList = ref([]);
const imgsList = ref([]);

let props = defineProps({
  singleTable: {
    type: Object,
    default() {
      return {}
    }
  }
})

onMounted(() => {
  changeTab();
})

const changeTab = (val = {
  // pageParam: {pageNo: 1, pageSize: 50},
}) => {
  if (activeKey.value === '2') {
    imgsList.value = []
    newsList.value = [];
    postAction(defaultTableViewUrl.list.replace('${parentId}', '').replace('${formNo}', 'oa_sys_msg'),
        {"pageParam": {pageNo: 1, pageSize: 5, orderBy: 'create_date desc'}}).then(res => {
      newsList.value = []
      // console.log(res.rows)
      res.rows.forEach(item => {
        let news = {};
        news.content = item.content_
        news.link = item.menu_href
        news.time = item.create_date
        news.menu = item.menu_name
        newsList.value.push(news)
      })
    })
  } else {
    imgsList.value = [];
    newsList.value = [];
    if (activeKey.value == null) {
      newsList.value = [];
    } else {
      let param = props.singleTable.initParam || {}
      postAction('dynamic/zform/datamap?path=path&traceFlag=&formNo=prt_information&parentId=' + activeKey.value, param).then(infoRes => {
        for (let i = 0; i < infoRes.rows.length; i++) {
          newsList.value.push({
                sum: i,
                content: infoRes.rows[i].title,
                link: '/#/contentData',
                time: infoRes.rows[i].release_date
              }
          )
        }
        let i = 0;
        infoRes.rows.forEach(item => {
          if (item.thumb_url != null && item.thumb_url !== '') {
            let news = {};
            news.imageSrc = item.thumb_url;
            news.caption = item.title;
            news.sum = i;
            news.link = '/#/contentData';
            i++;
            imgsList.value.push(news)
          } else {
            i++;
          }
        });
      })
    }
  }
}
const done = ref(true);
let addTabToAdminContent = inject('addTabToAdminContent');
const openTab = (item) => {
  if (activeKey.value === '2') {
    if (item.menu === '已办事项') {
      done.value = true
    } else if (item.menu === '待办事项') {
      done.value = false
    }
    addTabToAdminContent({
      attributes: {
        pageName: item.menu
      },
      key: `${done.value ? item.link : item.link}`
    })
  } else {
    window.open(item.link + '?parentid=' + activeKey.value + '&value=' + item.sum + '&id=' + '0')
  }
}
const openMore = (item) => {
  if (activeKey.value !== '2') {
    addTabToAdminContent({
      attributes: {
        pageName: '新闻中心'
      },
      key: `/cms/article/list`
    })
  } else {
    addTabToAdminContent({
      attributes: {
        pageName: '消息中心'
      },
      key: `/oa/oa_sys_msg/list`
    })
  }
}
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";

.left-item {
  padding: 10px 30px 30px 30px;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  height: 100%;

  .pr {
    position: relative;
  }

  .title {
    position: absolute;
    left: 0;
    top: 12px;
    padding-bottom: 8px;
    // border-bottom: 1px solid #dfe3e6;
    .xwzx {
      font-size: @font-size-base;
      font-weight: 600;
      margin-left: 13px;
    }
  }

  :deep(.ant-tabs-nav-wrap) {
    flex-direction: row-reverse;
  }

  .box-left {
    display: flex;
    justify-content: space-between;
    // padding-top: 1vh;
    .left-img {
      width: 350px;

      .img-list {
        width: 100%;

        img {
          height: 18vh;
          max-height: 20vh;
          margin: 0 auto;
          width: 50vh;
          max-width: 100%;
          image-rendering: pixelated;
          filter: noise(10%);
        }

        .custom-link {
          color: black;
          /* 添加其他自定义样式 */
        }
      }

      //.caption{
      //  height: 18vh;
      //  max-height: 20vh;
      //  margin: 0 auto;
      //}

    }

    .right-list {
      width: calc(100%);

      ul {
        list-style-type: unset;
        padding-inline-start: 18px;

        li {
          // display: flex;
          // justify-content: space-between;
          // overflow: hidden;
          clear: both;
          line-height: 25px;

          a {
            width: 96%;
            float: left;
            text-overflow: ellipsis;
            white-space: normal;
            color: #000;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            overflow: hidden;
            -webkit-box-orient: vertical;
          }

          span {
            float: right;
            color: #d9d9d9;
            font-size: 12px;
          }

          &:hover {
            color: var(--ant-primary-color);

            a {
              color: var(--ant-primary-color);
            }

            span {
              color: var(--ant-primary-color);
            }
          }
        }
      }

    }
  }

  .more_button {
    text-align: right;
    padding-top: 12px;
    margin-top: auto;

    .more-aaa {
      color: #cccccc;
      font-size: 14px;
      font-weight: normal;
      cursor: pointer;

      &:hover {
        color: var(--ant-primary-color);
      }
    }
  }
}

.empty-state {
  height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  :deep(.ant-empty-description) {
    color: #999;
    margin-top: 8px;
  }
}
</style>
