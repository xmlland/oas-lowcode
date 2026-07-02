<template>
  <div class="left-item">
    <div class="pr">
      <div class="title" style="display: flex; align-items: center;">
        <ProfileOutlined
            style="color: var(--ant-primary-color)"/>
        <span class="xwzx">新闻中心</span>
      </div>
      <a-tabs class="rightTab" v-model:activeKey="activeKey" @change="changeTab">
        <a-tab-pane v-for="item in dynamicData" :key="item.parentId" :tab="item.tab" :parentId="item.parentId"
                    force-rende></a-tab-pane>
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
            <span>{{ item.time }}</span>
          </li>
        </ul>
        <div v-else class="empty-state">
          <a-empty
              description="暂无新闻"
              :image="aEmptyImage"
              :imageStyle="{ height: '64px' }"
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
  name: "admin_home",
}
</script>
<script setup>
/*eslint-disable*/
import {ref, onMounted, inject} from "vue";
import {useRouter} from "vue-router";
import {postAction, baseUrl} from "@/api/action";
import {fileDownloadUrl, batchGetPresignedFileUrl} from "@/api/api";
import {Empty} from "ant-design-vue";

const dynamicData = ref([])
const activeKey = ref('1');
const newsList = ref([]);
const imgsList = ref([]);
const aEmptyImage = Empty.PRESENTED_IMAGE_SIMPLE;

const router = useRouter();

let props = defineProps({
  singleTable: {
    type: Object,
    default() {
      return {}
    }
  },
  oss: {
    type: String,
    default: "minio:main:userfiles"
  }
})

/**
 * 当使用 minio 后 文件的 地址处理
 * @param fileId
 * @returns {string}
 */
function getOSSFileUrl(fileId) {
  return (baseUrl + fileDownloadUrl + fileId) + '&oss=' + encodeURIComponent(props.oss)
}

onMounted(() => {
  let param = props.singleTable.initParam || {}
  postAction('dynamic/zform/treeData?parentId=0&formNo=prt_channel&traceFlag=1').then(unsentRes => {
    const data = unsentRes.data.data;
    dynamicData.value = data.map(item => {
      if (activeKey.value === '1') {
        activeKey.value = item.id
      }
      return {
        key: item.sort,
        tab: item.name,
        parentId: item.id
      };
    });
    postAction(`dynamic/zform/datamap?path=path&traceFlag=&formNo=prt_information&parentId=${activeKey.value}`, param).then(infoRes => {
      newsList.value = [];
      for (let i = 0; i < infoRes.rows.length; i++) {
        newsList.value.push({
              sum: i,
              content: infoRes.rows[i].title,
              link: '/contentData',
              time: infoRes.rows[i].release_date
            }
        )
      }
      imgsList.value = []
      let i = 0;
      infoRes.rows.forEach(item => {
        if (item.thumb_path_ != null) {
          let news = {};
          // 先设为空，避免首次渲染时触发无谓的 fileDownload 请求
          news.imageSrc = '';
          news.fileId = item.typesimg;
          news.caption = item.title;
          news.sum = i;
          news.link = '/contentData';
          i++;
          imgsList.value.push(news)
        } else {
          i++;
        }
      });
      // 异步获取预签名URL后再设置图片地址
      const fileIds = imgsList.value.map(img => img.fileId).filter(Boolean)
      if (fileIds.length > 0) {
        batchGetPresignedFileUrl(fileIds).then(urls => {
          imgsList.value.forEach(img => {
            if (img.fileId && urls[img.fileId]) {
              img.imageSrc = urls[img.fileId]
            } else if (img.fileId) {
              // 预签名URL获取失败时，回退到 fileDownload URL
              img.imageSrc = getOSSFileUrl(img.fileId)
            }
          })
        }).catch(() => {
          // 批量获取失败时，回退到 fileDownload URL
          imgsList.value.forEach(img => {
            if (img.fileId) {
              img.imageSrc = getOSSFileUrl(img.fileId)
            }
          })
        })
      }
    })
  })
})

const changeTab = () => {

}
const done = ref(true);
let addTabToAdminContent = inject('addTabToAdminContent');
const openTab = (item) => {
  const queryString = `?parentid=${activeKey.value}&value=${item.sum}&id=0`;
  const fullPath = item.link + queryString;
  const resolved = router.resolve(fullPath);
  window.open(resolved.href, '_blank');
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
      flex: 1;

      ul {
        list-style-type: unset;

        li {
          clear: both;
          line-height: 25px;

          a {
            width: 70%;
            float: left;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            color: #000;
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
  height: 100%;
  min-height: 160px;
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
