<template>
  <div class="u_map_area">
    <u-map style="width: 100%;"
           :style="{height:props.height+'px'}"
           ref='showMap'
           :centerLng="defaultCenterLngLat[0]"
           :centerLat="defaultCenterLngLat[1]"
           :zoom="defaultZoom"
           :minZoom="minZoom"
           :maxZoom="maxZoom"
           :showFullscreen="true"
           :showChangeButton="true"
           :mapClickEvent="mapClickEvent">
      <template #buttonArea>
        <a-radio-group v-model:value="currentTool" style="margin-top: 5px;display: flex;flex-direction: column;flex-wrap: nowrap;justify-content: center;align-items: stretch;align-content: space-around;row-gap: 5px;">
          <a-radio-button size="small" value="circle" @click="setCurrentTool('circle')">
            <i class="fa fa-circle-o" aria-hidden="true" />
            <span style="margin-left: 8px;">圆&emsp;&emsp;形</span>
          </a-radio-button>
          <a-radio-button size="small" value="rectangle" @click="setCurrentTool('rectangle')">
            <i class="fa fa-square-o" aria-hidden="true" />
            <span style="margin-left: 8px;">矩&emsp;&emsp;形</span>
          </a-radio-button>
          <a-radio-button size="small" value="polygon" @click="setCurrentTool('polygon')">
            <gateway-outlined />
            <span style="margin-left: 9px;">多边形&emsp;</span>
          </a-radio-button>
          <a-radio-button size="small" value="reset" @click="resetLast">
            <rollback-outlined />
            <span style="margin-left: 9px;">撤销上个</span>
          </a-radio-button>
          <a-radio-button size="small" value="clean" @click="clearAllDrawings">
            <clear-outlined />
            <span style="margin-left: 9px;">清除绘制</span>
          </a-radio-button>
        </a-radio-group>
      </template>
    </u-map>
  </div>
</template>
<script>
export default {
  name: "UMapAreaSelect",
}
</script>
<script setup>
import L from 'leaflet';
import UMap from "@/components/map/UMap";
import config from "@/config";
import {getCurrentInstance, ref, onMounted, nextTick, watch} from "vue";
import {message} from "ant-design-vue";

let props = defineProps({
  value: {
    type: Array,
    default: ()=>[]
  },
  disabled: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  // 默认中心经纬度
  defaultCenterLngLat: {
    type: Array,
    default: function () {
      return [config.initLng, config.initLat]
    }
  },
  // 默认中心经纬度
  defaultZoom: {
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
  height:{
    type:Number,
    default:300
  },
});

let value = ref([]);

let instance = getCurrentInstance();
let map = null;
const showMap = ref(null);
let currentTool = ref(null);
let drawing = ref(false);
let startPoint = ref(null);
let polygonPoints = ref(null);
// 实时显示的 多边形线段
let polygonLine = ref(null);
let polygonPolygon = ref(null);

let currentRectangle = null;
let currentCircle = null;

// 绘制样式
const drawOptions = {
  color: '#3388ff',
  weight: 3,
  opacity: 0.7,
  fillOpacity: 0.2
};

onMounted(() => {
  map = instance.refs.showMap.getMap();
  nextTick(()=>{
    map = instance.refs.showMap.getMap();
    map.map?.on('mousemove', mapMouseMoveEvent)
  })
})
// 清除所有绘制
const clearAllDrawings = (saveFlag=true)=> {
  if (map){
    map.map.eachLayer(layer => {
      if (!(layer instanceof L.TileLayer)) {
        map.map.removeLayer(layer);
      }
    });
  }
  if (saveFlag){
    value.value = [];
    emits('update:value',JSON.stringify(value.value),value.value);
    emits('change',JSON.stringify(value.value),value.value);
  }
}

const uploadMap = (valueData) => {
  for (let i = 0; i < valueData.length; i++) {
    const item = valueData[i];
    switch (item.type) {
      case 'circle':
        updateCircle(item.radius,item.center);
        currentCircle = null;
        break;
      case 'rectangle':
        updateRectangle(null,item.bounds);
        currentRectangle = null;
        break;
      case 'polygon':
        polygonPoints.value = item.points;
        if (polygonPoints.value.length >= 3) {
          // 创建闭合的多边形
          polygonPolygon.value = L.polygon(polygonPoints.value, drawOptions).addTo(map.map);
        }
        break;
    }
  }
}

const setValue = (valueData)=>{
  if (!valueData){
    value.value = [];
    return;
  }
  if (typeof valueData === 'string'){
    valueData = JSON.parse(valueData);
  }

  if (!Array.isArray(valueData)){
    console.error('UMapAreaSelect组件value值类型错误');
    return;
  }
  clearAllDrawings(false);
  value.value = valueData;
  uploadMap(value.value);
}
watch(()=>props.value,()=>{
  setValue(props.value);
},{deep:true,immediate:true})

const setCurrentTool = (type) => {
  if (currentTool.value === 'polygon'){
    completeDrawing();
  }
  if (type === currentTool.value) {
    currentTool.value = null;
  }else {
    currentTool.value = type;
  }
  drawing.value = false;
}

const mapMouseMoveEvent = (e)=> {
  // console.log("鼠标移动",e.latlng);
  if ( currentTool.value && drawing.value){

    if (currentTool.value === 'circle') {
      // 动态更新圆形半径
      const radius = e.latlng.distanceTo(startPoint.value);
      updateCircle(radius);
    } else if (currentTool.value === 'rectangle') {
      // 动态更新矩形大小
      updateRectangle(e.latlng);
    } else if (currentTool.value === 'polygon') {
      // 动态更新矩形大小
      updatePolygonPreview(e.latlng); // 更新预览
    }
  }
}
const mapClickEvent = (e)=> {
  console.log("选中经纬度",e.latlng);
  if (props.disabled) {
    return;
  }

  if (!drawing.value) {
    startPoint.value = e.latlng;
    drawing.value = true;

    // 如果是多边形，添加一个点
    if (currentTool.value === 'polygon') {
      polygonPoints.value = [];
      polygonPoints.value.push(startPoint.value);
      // 初始化实时预览的折线
      polygonLine.value = L.polyline([polygonPoints.value[0], polygonPoints.value[0]], drawOptions).addTo(map.map);
    }
  }else{
    if (currentTool.value === 'circle' || currentTool.value === 'rectangle') {
      completeDrawing();
    }
    // 如果是多边形，添加更多点
    else if (currentTool.value === 'polygon') {
      polygonPoints.value.push(e.latlng);
      updatePolygonPreview(e.latlng); // 更新预览
    }
  }
  if (!currentTool.value){
    message.warn('请选择绘制工具,进行绘制');
  }
}

// 撤销上个绘制
const resetLast = ()=> {

  if (value.value && value.value.length > 0) {
    console.log(JSON.stringify(value.value))
    const lastDrawing = value.value.pop();
    console.log(JSON.stringify(value.value))
    emits('update:value',JSON.stringify(value.value),value.value);
    emits('change',JSON.stringify(value.value),value.value);
  }
}

// 完成绘制
const completeDrawing= ()=> {
  switch (currentTool.value) {
    case 'circle':
      value.value.push({
        type: 'circle',
        center: startPoint.value,
        radius: currentCircle.getRadius()
      })
      break;
    case 'rectangle':
      value.value.push({
        type: 'rectangle',
        bounds: currentRectangle.getBounds()
      });
      break;
    case 'polygon':
      // 移除预览折线
      if (polygonLine.value) {
        map.map.removeLayer(polygonLine.value);
        polygonLine.value = null;
      }

      if (polygonPoints.value.length >= 3) {
        // 创建闭合的多边形
        polygonPolygon.value = L.polygon(polygonPoints.value, drawOptions).addTo(map.map);

        value.value.push({
          type:'polygon',
          points: polygonPoints.value
        });
      }else{
        message.warn("低于3个点位，无法形成多边形");
      }

      break;
  }

  emits('update:value',JSON.stringify(value.value),value.value);
  emits('change',JSON.stringify(value.value),value.value);

  drawing.value = false;
  currentCircle = null;
  currentRectangle = null;
  polygonPoints.value = [];
}

// 更新圆形
const updateCircle = (radius,center=null)=>{
  if (!currentCircle && center) {
    currentCircle = L.circle(center, {
      radius: radius,
      ...drawOptions
    }).addTo(map.map);
  } else if (!currentCircle) {
    currentCircle = L.circle(startPoint.value, {
      radius: radius,
      ...drawOptions
    }).addTo(map.map);
  } else {
    currentCircle.setRadius(radius);
  }
}

// 更新矩形
const updateRectangle = (endPoint,bounds=null)=>{
  if (!currentRectangle && bounds) {
    currentRectangle = L.rectangle([
      [bounds._northEast.lat, bounds._northEast.lng],
      [bounds._southWest.lat, bounds._southWest.lng]
    ], drawOptions).addTo(map.map);
  } else if (!currentRectangle) {
    currentRectangle = L.rectangle([
      [startPoint.value.lat, startPoint.value.lng],
      [endPoint.lat, endPoint.lng]
    ], drawOptions).addTo(map.map);
  } else {
    currentRectangle.setBounds([
      [startPoint.value.lat, startPoint.value.lng],
      [endPoint.lat, endPoint.lng]
    ]);
  }
}

// 更新多边形预览
const updatePolygonPreview = (newPoint)=>{
  if (!polygonLine.value) return;

  // 绘制时保持临时折线预览
  const previewPoints = [...polygonPoints.value];
  previewPoints.push(newPoint);

  // 更新折线，显示临时预览
  polygonLine.value.setLatLngs(previewPoints);
}

let emits = defineEmits(['update:value', 'change'])
defineExpose(setValue)

</script>
<style scoped lang="less">
.u_map_area{

  .map-tool{
    position: absolute;
    right: 10px;
    top: 10px;
    z-index: 1000;
  }
}
</style>
