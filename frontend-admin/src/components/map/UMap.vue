<template>
  <div class="content">
    <div :id="id" class="u-map-container"></div>
    <div class="button_area">
      <a-radio-group v-if="props.showChangeButton" v-model:value="size">
        <a-radio-button size="small" value="vec" @click="changeMap('vec')">矢量图</a-radio-button>
        <a-radio-button size="small" value="img" @click="changeMap('img')">影像图</a-radio-button>
      </a-radio-group>
      <template v-if="props.showFullscreen">
        <a-button v-if="!fullScreen" @click="toFullscreen(true)" >
          <template #icon><fullscreen-outlined name="全屏"/></template>
          全&emsp;&emsp;屏
        </a-button>
        <a-button v-else  @click="toFullscreen(false)">
          <template #icon><fullscreen-exit-outlined name="退出全屏"/></template>
          退出全屏
        </a-button>
      </template>
      <slot name="buttonArea"></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: "UMap"
}
</script>
<script setup>
import L from 'leaflet';
import Umap from "@/lib/umap";
import {onMounted, ref, getCurrentInstance, onBeforeUnmount} from "vue";
import {UUID} from "@/lib/tools";
import config from "@/config";

let props = defineProps({
  centerLng:{
    type:Number,
    default:config.initLng
  },
  centerLat:{
    type:Number,
    default:config.initLat
  },
  zoom:{
    type:Number,
    default:config.initZoom
  },
  minZoom:{
    type:Number,
    default:5
  },
  maxZoom:{
    type:Number,
    default:18
  },
  type:{
    type:String,
    default:'vec'//vec|img 矢量地图|影像地图
  },
  mapClickEvent:{
    type:Function,
    default:function (){}
  },
  mapZoomEvent:{
    type:Function,
    default:function (){}
  },
  showChangeButton:{
    type:Boolean,
    default:false
  },
  // 是否显示全屏按钮
  showFullscreen:{
    type:Boolean,
    default:false
  }
})

let id = ref('u-map' + UUID());

let size = ref(props.type);
const vecUrl = '//t{s}.tianditu.gov.cn/DataServer?T=vec_w&X={x}&Y={y}&L={z}&tk='//矢量图层
const cvaUrl = '//t{s}.tianditu.gov.cn/DataServer?T=cva_w&X={x}&Y={y}&L={z}&tk='//矢量标注图层
const imgUrl = '//t{s}.tianditu.gov.cn/DataServer?T=img_w&X={x}&Y={y}&L={z}&tk='//影像图层
const ciaUrl = '//t{s}.tianditu.gov.cn/DataServer?T=cia_w&X={x}&Y={y}&L={z}&tk='//影像标注图层
const subdomains = ['0', '1', '2', '3', '4', '5', '6', '7']

// 动态获取天地图图层（使用时读取config.tdtKey，确保能获取到异步加载的key）
const getTileLayer = (url) => L.tileLayer(url + config.tdtKey, {subdomains: subdomains});

let vectorLayer = null;
let cvaLayer = null;
let imageLayer = null;
let ciaLayer = null;

let uMap = null
let emits = defineEmits(['loaded'])
let instance = getCurrentInstance();
let fullScreen = ref(false);
onMounted(() => {
  uMap = new Umap({
    id: id.value,
    centerLng: props.centerLng,
    centerLat: props.centerLat,
    zoom: props.zoom,
    minZoom: props.minZoom,
    maxZoom: props.maxZoom,
    mapClickEvent: props.mapClickEvent,
    mapZoomEvent: props.mapZoomEvent,
    type: props.type,
    mapLoaded:()=>{
      emits('loaded',uMap)
    }
  });
  initListener();
});

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange',loadFullScreen);
  document.removeEventListener('mozfullscreenchange',loadFullScreen);
  document.removeEventListener('webkitfullscreenchange',loadFullScreen);
  document.removeEventListener('msfullscreenchange',loadFullScreen);
});

const getMap = () => {
  return uMap
}

const changeMap = (type)=>{
  // 懒加载图层（确保能获取到异步加载的tdtKey）
  if (!vectorLayer) vectorLayer = getTileLayer(vecUrl);
  if (!cvaLayer) cvaLayer = getTileLayer(cvaUrl);
  if (!imageLayer) imageLayer = getTileLayer(imgUrl);
  if (!ciaLayer) ciaLayer = getTileLayer(ciaUrl);
  
  if(type === 'vec'){
    uMap.map.removeLayer(imageLayer);
    uMap.map.removeLayer(ciaLayer);
    vectorLayer.addTo(uMap.map);
    cvaLayer.addTo(uMap.map);
  }else{
    uMap.map.removeLayer(vectorLayer);
    uMap.map.removeLayer(cvaLayer);
    imageLayer.addTo(uMap.map);
    ciaLayer.addTo(uMap.map);
  }
}

const initListener = () => {
  // 判断当前页面是否处于全部状态，根据状态的不同对screen的值进行改变（监听状态）
  document.addEventListener('fullscreenchange',loadFullScreen);
  //火狐
  document.addEventListener('mozfullscreenchange', loadFullScreen);
  // 谷歌
  document.addEventListener('webkitfullscreenchange',loadFullScreen);
  // ie
  document.addEventListener('msfullscreenchange', loadFullScreen);
}
const loadFullScreen =  () => {
  let fullScreen = window.fullScreen || document.msFullscreenEnabled || document.webkitIsFullScreen;
  toFullscreen(fullScreen);
}

const toFullscreen = (flag) => {
  fullScreen.value = flag;
  // 不显示全屏按钮，不做全屏操作
  if ( !props.showFullscreen ){
    return;
  }
  if ( flag ){
    let element = instance.vnode.el;
    //全屏
    if (element.requestFullscreen) {
      element.requestFullscreen()
    }
    //兼容Firefox全屏
    else if (element.mozRequestFullScreen) {
      element.mozRequestFullScreen()
    }
    //兼容Chrome Safari Opera全屏
    else if (element.webkitRequestFullscreen) {
      element.webkitRequestFullscreen()
    }
    //兼容IE Edge全屏
    else if (element.msRequestFullscreen) {
      element.msRequestFullscreen()
    }
  }else{
    //退出全屏
    if (document.exitFullscreen) {
      document.exitFullscreen()
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen()
    } else if (document.webkitCancelFullScreen) {
      document.webkitCancelFullScreen()
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen()
    }
  }
}

defineExpose({
  getMap
})
</script>
<style lang="less" scoped>
.u-map-container {
  height: 100%;
  width: 100%;

  :deep(.leaflet-control-attribution) {
    display: none;
  }

}
.button_area{
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  flex-wrap: nowrap;
  align-content: center;
  justify-content: center;
  align-items: flex-end;
  :deep(.ant-btn){
    margin-top: 5px;
  }
}
.content{
  width: 100%;
  height: 100%;
  position: relative;
}
</style>
