<template>
  <div class="content">
    <div v-if="props.showLegend && legendArray.length > 0" class="legend">
      <div class="item" v-for="(data,index) in legendArray" :key="index">
        <div class="icon" :style="{backgroundColor:data.color}"></div>
        <div>{{data.text}}</div>
      </div>
    </div>
    <div class="image-area" :style="{height: props.showLegend && legendArray.length > 0 ? 'calc(100% - 30px)' : '100%'}">
      <div class="button" :class="{pause:state === 'play',play:state === 'pause'}" @click="playButtonClick"></div>
      <div class="main-area">
        <div class="image">
          <div :id="id" >
          </div>
          <img v-if="melImagePath" style="width: 100%; height: 100px; cursor: pointer;" :src="melImagePath" alt="" @click="showAudioImg(melImagePath)">
          <img v-if="linearImagePath" style="width: 100%; height: 100px; margin-top: 16px; cursor: pointer" :src="linearImagePath" alt="" @click="showAudioImg(linearImagePath)">
        </div>
        <e-chart v-if="duration > 0" :option="XAxisOption" :height="27"></e-chart>
      </div>
      <div style="width: 45px;position: relative;" class="right-axis" v-if="linearImagePath">
        <div style="width: 100%;height: 115px;">
          <e-chart v-if="waveMax > 0" :option="waveOption"  style="top: -7px"></e-chart>
        </div>
        <div style="width: 100%;height: 115px;">
          <e-chart v-if="timingMax > 0" :option="timingOption" ></e-chart>
        </div>
        <div style="width: 100%;height: 115px;">
          <e-chart v-if="linearMax > 0" :option="linearOption" ></e-chart>
        </div>
      </div>
      <div style="width: 25px;height: 100%;position: relative" v-if="!linearImagePath">
        <div style="width: 100%;height: 100%">
          <e-chart v-if="waveMax > 0" :option="waveOption" :width="28"></e-chart>
        </div>
        <div style="width: 100%;height: 115px">
          <e-chart v-if="timingMax > 0" :option="timingOption" :width="22"></e-chart>
        </div>
      </div>
    </div>

    <UModal ref="audioImg" :show-ok="false" :show-cancel="false">
      <UAudioImg :imageUrl="umodal_img"/>
    </UModal>
  </div>
</template>

<script>
export default {
  name: "UAudio"
}
</script>
<script setup>
import WaveSurfer from 'wavesurfer.js'
import RegionsPlugin from 'wavesurfer.js/dist/plugins/regions.js';
import SpectrogramPlugin from 'wavesurfer.js/dist/plugins/spectrogram.esm.js'
import {getCurrentInstance, onMounted, ref, watch} from "vue";
import {UUID} from "@/lib/tools";
import EChart from "@/components/echarts/EChart";
import UAudioImg from "@/components/audio/UAudioImg";

let props = defineProps({
  // 线性时频图地址
  linearImagePath: {
    type: String,
    default: null
  },
  // 梅尔时频图地址
  melImagePath: {
    type: String,
    default: null
  },
  //文件地址
  url:{
    type: String,
    default: null
  },
  //播放音频的速度。数值越低，速度越慢
  audioRate: {
    type: Number,
    default: 1
  },
  //播放期间是否将光标保持在波形的中心
  autoCenter: {
    type: Boolean,
    default: true
  },
  //是否自动播放
  autoplay:{
    type: Boolean,
    default: false
  },
  //自动滚动容器以保持视区中的当前位置
  autoScroll: {
    type: Boolean,
    default: false
  },
  lineColor: {
    type: String,
    default: '#777676'
  },
  //光标后颜色
  waveColor:{
    type: String,
    default: 'rgb(200, 0, 200)'
  },
  //光标前颜色
  progressColor:{
    type: String,
    default: 'rgb(100, 0, 100)'
  },
  //柱对其方式 top\center\bottom
  barAlign:{
    type: String,
    default: 'center'
  },
  //柱间隔
  barGap: {
    type: Number,
    default: 0
  },
  //柱高度
  barHeight: {
    type: Number,
    default: 7
  },
  //柱圆角边框
  barRadius:{
    type: Number,
    default: 0
  },
  //柱宽度
  barWidth:{
    type: Number,
    default: undefined
  },
  //当destroy方法被调用时，关闭并使所有音频上下文无效。
  closeAudioContext:{
    type: Boolean,
    default: false
  },
  //指针的填充颜色
  cursorColor:{
    type: String,
    default: '#333'
  },
  //指针的宽度
  cursorWidth:{
    type: Number,
    default: 1
  },
  //允许拖动光标以查找新位置
  dragToSeek:{
    type: [Boolean, Object],
    default(){
      return {
        //移动后触发时间的间隔
        debounceTime: 200
      }
    }
  },
  //是否拉伸波形以填充容器
  fillParent:{
    type: Boolean,
    default: true
  },
  //是否隐藏滚动条
  hideScrollbar:{
    type: Boolean,
    default: true
  },
  width:{
    type: [Number, String],
    default: '100%'
  },
  height: {
    type: [Number, String],
    default: 'auto'
  },
  //音频每秒的最小像素数
  minPxPerSec:{
    type: Number,
    default: 0
  },
  //使用PeakCache来提高大波形的渲染速度
  partialRender:{
    type: Boolean,
    default: false
  },
  showProgress:{
    type: Boolean,
    default: true
  },
  flagDataArray:{
    type: Array,
    default(){
      return [];
    }
  },
  renderFunctionEnable:{
    type: Boolean,
    default: false
  },
  showLegend: {
    type: Boolean,
    default: false
  },
  //时长
  duration: {
    type: Number,
    default: 0
  },
  //波形图最大值
  waveMax: {
    type: Number,
    default: 0
  },
  //时序图最大值
  timingMax: {
    type: Number,
    default: 0
  },
  // 线性时频图最大值
  linearMax: {
    type: Number,
    default: 0
  },
  // 需要启动的插件
  enabledPlugins: {
    type: Array,
    default: () => ['regions', 'spectrogram']
  }
})

let instance = getCurrentInstance();

const umodal_img = ref("");
const showAudioImg = (url) => {
  umodal_img.value = url;
  instance.refs.audioImg.open('查看音频');
}

const regions = RegionsPlugin.create();
let activeRegion = null;

const spectrogram = SpectrogramPlugin.create({
  labels: true,
  height: 100,
  splitChannels: true,
  scale: 'mel', // or 'linear'
  // frequencyMax: 400,
  frequencyMin: 0,
  fftSamples: 1024,
  labelsBackground: 'rgba(0, 0, 0, 0.1)',
})

let id = ref(UUID());
let surfer = null;
let legendArray = ref([]);

let state = ref('pause');

const colors = {
  '鸟叫':'#FFD60A',
  '虫鸣':'#C800C8',
  '蛙叫':'#20dab8'
}

const regionsColor = {
  '鸟叫声':'rgba(255,214,10,0.5)',
  '虫鸣声':'rgba(4,215,234,0.5)',
  '蛙叫声':'rgba(32,218,184,0.5)'
}

onMounted(()=>{
  initAudio();
})

const plugins = [
  props.enabledPlugins.includes('regions') ? regions : null,
  props.enabledPlugins.includes('spectrogram') ? spectrogram : null
].filter(plugin => plugin !== null);

const initAudio = () =>{
  surfer = WaveSurfer.create({
    container: document.getElementById(id.value),
    audioRate: props.audioRate,
    autoCenter: props.autoCenter,
    autoplay: props.autoplay,
    autoScroll: props.autoScroll,
    waveColor: props.waveColor,
    progressColor: props.progressColor,
    barAlign: props.barAlign,
    barGap: props.barGap,
    barHeight: props.barHeight,
    barRadius: props.barRadius,
    barWidth: props.barWidth,
    closeAudioContext: props.closeAudioContext,
    cursorColor: props.cursorColor,
    cursorWidth: props.cursorWidth,
    dragToSeek: props.dragToSeek,
    fillParent: props.fillParent,
    hideScrollbar: props.hideScrollbar,
    width: props.width,
    height: props.height,
    minPxPerSec: props.minPxPerSec,
    partialRender: props.partialRender,
    plugins: plugins,
    normalize: true,
    renderFunction: props.renderFunctionEnable && props.flagDataArray.length > 0 ? renderFunction : undefined,
  })

  surfer.on('ready',()=>{
    const wavesurferStyle = document.getElementById(id.value).querySelector('div').shadowRoot.querySelector('style');
    if(!props.showProgress) {
      if(wavesurferStyle){
        wavesurferStyle.textContent=':host {\n' +
            '  user-select: none;\n' +
            '  min-width: 1px;\n' +
            '}\n' +
            ':host audio {\n' +
            '  display: block;\n' +
            '  width: 100%;\n' +
            '}\n' +
            ':host .scroll {\n' +
            '  overflow-x: auto;\n' +
            '  overflow-y: hidden;\n' +
            '  width: 100%;\n' +
            '  position: relative;\n' +
            '}\n' +
            ':host .noScrollbar {\n' +
            '  scrollbar-color: transparent;\n' +
            '  scrollbar-width: none;\n' +
            '}\n' +
            ':host .noScrollbar::-webkit-scrollbar {\n' +
            '  display: none;\n' +
            '  -webkit-appearance: none;\n' +
            '}\n' +
            ':host .wrapper {\n' +
            '  position: relative;\n' +
            '  overflow: visible;\n' +
            '  z-index: 2;\n' +
            '}\n' +
            ':host .canvases {\n' +
            '  min-height: 128px;\n' +
            '  margin-bottom: 10px;\n' +
            '  clip-path: none !important;\n' +
            '}\n' +
            ':host .canvases > div {\n' +
            '  position: relative;\n' +
            '}\n' +
            ':host canvas {\n' +
            '  display: block;\n' +
            '  position: absolute;\n' +
            '  top: 0;\n' +
            '  image-rendering: pixelated;\n' +
            '}\n' +
            ':host .progress {\n' +
            '  pointer-events: none;\n' +
            '  position: absolute;\n' +
            '  z-index: 2;\n' +
            '  top: 0;\n' +
            '  left: 0;\n' +
            '  width: 0;\n' +
            '  height: 100%;\n' +
            '  overflow: hidden;\n' +
            '}\n' +
            ':host .progress > div {\n' +
            '  position: relative;\n' +
            '}\n' +
            ':host .cursor {\n' +
            '  pointer-events: none;\n' +
            '  position: absolute;\n' +
            '  z-index: 5;\n' +
            '  top: 0;\n' +
            '  left: 0;\n' +
            '  height: 100%;\n' +
            '  border-radius: 2px;\n' +
            '}\n' +
            '      '
      }
    }
    else{
      wavesurferStyle.textContent= '\n' +
          '        :host {\n' +
          '          user-select: none;\n' +
          '          min-width: 1px;\n' +
          '        }\n' +
          '        :host audio {\n' +
          '          display: block;\n' +
          '          width: 100%;\n' +
          '        }\n' +
          '        :host .scroll {\n' +
          '          overflow-x: auto;\n' +
          '          overflow-y: hidden;\n' +
          '          width: 100%;\n' +
          '          position: relative;\n' +
          '        }\n' +
          '        :host .noScrollbar {\n' +
          '          scrollbar-color: transparent;\n' +
          '          scrollbar-width: none;\n' +
          '        }\n' +
          '        :host .noScrollbar::-webkit-scrollbar {\n' +
          '          display: none;\n' +
          '          -webkit-appearance: none;\n' +
          '        }\n' +
          '        :host .wrapper {\n' +
          '          position: relative;\n' +
          '          overflow: visible;\n' +
          '          z-index: 2;\n' +
          '        }\n' +
          '        :host .canvases {\n' +
          '          min-height: 100px;\n' +
          '          margin-bottom: 10px;\n' +
          '        }\n' +
          '        :host .canvases > div {\n' +
          '          position: relative;\n' +
          '        }\n' +
          '        :host canvas {\n' +
          '          display: block;\n' +
          '          position: absolute;\n' +
          '          top: 0;\n' +
          '          image-rendering: pixelated;\n' +
          '        }\n' +
          '        :host .progress {\n' +
          '          pointer-events: none;\n' +
          '          position: absolute;\n' +
          '          z-index: 2;\n' +
          '          top: 0;\n' +
          '          left: 0;\n' +
          '          width: 0;\n' +
          '          height: 100%;\n' +
          '          overflow: hidden;\n' +
          '        }\n' +
          '        :host .progress > div {\n' +
          '          position: relative;\n' +
          '        }\n' +
          '        :host .cursor {\n' +
          '          pointer-events: none;\n' +
          '          position: absolute;\n' +
          '          z-index: 5;\n' +
          '          top: 0;\n' +
          '          left: 0;\n' +
          '          height: 100%;\n' +
          '          border-radius: 2px;\n' +
          '        }\n' +
          '      '
    }
  })

  surfer.on('play',()=>{
    state.value='play';
  })

  surfer.on('pause',()=>{
    state.value='pause';
  })

  surfer.on('decode',()=>{
    for(let i = 0 ;i < props.flagDataArray.length; i++){
      let flagData = props.flagDataArray[i];
      regions.addRegion({
        start:flagData.start,
        end:flagData.end,
        content:flagData.type,
        color: regionsColor[flagData.type]
      })
    }
  })

  surfer.on('click',(relativeX,relativeY)=>{
    activeRegion = null;
  })

  regions.on('region-clicked', (region, e) => {
    e.stopPropagation() // prevent triggering a click on the waveform
    activeRegion = region
    region.play()
  })

  regions.on('region-out', (region) => {
    if(activeRegion !== null){
      surfer.pause();
      setTimeout(() =>{
        if (activeRegion === region) {
          region.play()
        }
      },500)
    }
  })

  surfer.load(props.url);
}

const renderFunction = (peaks, ctx) =>{
  //获取画板宽高以及画板中间位置
  const width = ctx.canvas.width;
  const height = ctx.canvas.height;
  const halfHeight = height / 2;

  //获取图例数据
  if(props.showLegend){
    getLegendArray();
  }

  calcAudioDuration(props.url).then(duration =>{
    //标识数据转换
    const dataArray = calcDataArray(peaks,duration, props.flagDataArray);
    const dot = dataArray.map(item => item.value);

    ctx.clearRect(0, 0, width, height); // 清空画布
    ctx.lineWidth = props.barWidth?props.barWidth:1;
    ctx.beginPath();
    ctx.moveTo(0, halfHeight); // 从画布中间开始

    let a = width / peaks[0].length;
    let x  = 0 ;
    for(let i = 0; i < peaks[0].length; i++){

      if(dot.indexOf(i) !== -1){
        let data = dataArray.find(item => item.value === i);

        if('start' === data.type){
          ctx.strokeStyle = props.waveColor;
          ctx.stroke();
          ctx.strokeStyle = data.color;
          ctx.beginPath();

          x = drawLine(peaks,ctx,i,halfHeight,props.barHeight,x,a);
        }else if("end" === data.type){
          x = drawLine(peaks,ctx,i,halfHeight,props.barHeight,x,a);

          ctx.strokeStyle = data.color;
          ctx.stroke();
          ctx.beginPath();
        }
      }else{
        x = drawLine(peaks,ctx,i,halfHeight,props.barHeight,x,a);
      }

    }

    ctx.strokeStyle = props.waveColor;
    ctx.stroke(); // 绘制线条
  })
}

const getLegendArray = () =>{
  legendArray.value = [];
  for(let i = 0 ;i < props.flagDataArray.length; i++){
    let flagData = props.flagDataArray[i];
    legendArray.value.push(
        {color:colors[flagData.type],text:flagData.type}
    )
  }
}

const calcAudioDuration = (audioUrl) =>{
  return new Promise((resolve, reject) => {
    const audio = new Audio(audioUrl);
    audio.addEventListener('loadedmetadata', () => {
      resolve(audio.duration);
    });
    audio.addEventListener('error', (e) => {
      reject(`无法加载音频: ${e.message}`);
    });
  });
}

const calcDataArray = (peaks,totalTime,rawData) =>{
  let onSecondStep = peaks[0].length / totalTime;

  let dataArray = [];
  for(let i =0;i < rawData.length; i++){
    let color = colors[rawData[i].type]
    dataArray.push({value:Math.floor(rawData[i].start * onSecondStep) + 50 ,type:'start',color:color});
    if(rawData[i].end === totalTime){
      dataArray.push({value:peaks[0].length - 1,type:'end',color:color})
    }else{
      dataArray.push({value:Math.floor(rawData[i].end * onSecondStep) + 50,type:'end',color:color})
    }
  }

  return dataArray;
}

const drawLine = (peaks,ctx,i,halfHeight,barHeight,x,a) =>{
  const peakValue = peaks[0][i] || 0; // 获取波形数据值
  const y = halfHeight - (peakValue * halfHeight * barHeight); // 计算 y 坐标
  ctx.lineTo(x, y); // 绘制线条
  ctx.lineTo(x, halfHeight +  (peakValue * halfHeight * barHeight)); // 绘制线条
  return x + a;
}

const playButtonClick = () =>{
  surfer.playPause();
}

const getAudio = () =>{
  return surfer;
}

const getRegions = () =>{
  return regions;
}

let XAxisOption = ref({});

const updateXAxis = (duration) =>{

  if(duration > 0){
    let XAxisArray = [];

    for(let i = 0;i < duration;i++){
      XAxisArray.push(i);
    }

    XAxisOption.value = {
      grid: {
        top: '0',
        left: '7',
        right: '7',
        bottom: '0',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          boundaryGap: false,
          data: XAxisArray
        }
      ],
      yAxis: [
        {
          type: 'value',
          max: '0',
        }
      ]
    }
  }else{
    XAxisOption.value = {};
  }
}

let waveOption = ref({});

const updateWaveYAxis = (val) =>{

  if(val > 0){
    let YAxisArray = ['-'+val,val];

    waveOption.value = {
      grid: {
        top: '8',
        left: '0',
        right: '0',
        bottom: '8',
        containLabel: true
      },
      xAxis: [
        {
          show: false,
          type: 'category'
        }
      ],
      yAxis: [
        {
          type: 'value',
          position: 'right',
          boundaryGap: false,
          // data: YAxisArray
          min: YAxisArray[0],
          max: YAxisArray[1],
          axisLine: {
            show: true
          },
          axisTick: {
            show: true,
          },
          splitNumber: 3,
        }
      ]
    }
  }else{
    waveOption.value = {};
  }
}

let timingOption = ref({});

const updateTimingYAxis = (val) =>{

  if(val > 0){
    let YAxisArray = ['0',val];

    timingOption.value = {
      grid: {
        top: '10',
        left: '0',
        right: '0',
        bottom: '6',
        containLabel: true
      },
      xAxis: [
        {
          show: false,
          type: 'category'
        }
      ],
      yAxis: [
        {
          type: 'value',
          position: 'right',
          boundaryGap: false,
          // data: YAxisArray,
          min: YAxisArray[0],
          max: YAxisArray[1],
          axisLine: {
            show: true
          },
          axisTick: {
            show: true,
          },
          splitNumber: 6,
        }
      ]
    }
  }else{
    timingOption.value = {};
  }
}

let linearOption = ref({});

const updateLinearYAxis = (val) =>{
  if(val > 0){
    let YAxisArray = ['0',val];

    linearOption.value = {
      grid: {
        top: '10',
        left: '0',
        right: '0',
        bottom: '6',
        containLabel: true
      },
      xAxis: [
        {
          show: false,
          type: 'category'
        }
      ],
      yAxis: [
        {
          type: 'value',
          position: 'right',
          boundaryGap: false,
          // data: YAxisArray,
          min: YAxisArray[0],
          max: YAxisArray[1],
          axisLine: {
            show: true
          },
          axisTick: {
            show: true,
          },
          splitNumber: 6,
        },
      ]
    }
  }else{
    linearOption.value = {};
  }
}

watch(() => props.duration, () => {
  updateXAxis(props.duration);
}, {deep: true,immediate:true})

watch(() => props.waveMax, () => {
  updateWaveYAxis(props.waveMax);
}, {deep: true,immediate:true})

watch(() => props.timingMax, () => {
  updateTimingYAxis(props.timingMax);
}, {deep: true,immediate:true})

watch(() => props.linearMax, () => {
  updateLinearYAxis(props.linearMax);
}, {deep: true,immediate:true})

watch(() => props.url, () => {
  surfer.load(props.url);
}, {deep: true})

defineExpose({
  getAudio,getRegions
})
</script>
<style scoped lang="less">
.content{
  width: 100%;
  height: 100%;

  .legend{
    width: 100%;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 25px;

    .item{
      height: 20px;
      display: flex;
      gap: 10px;
      align-items: center;

      .icon{
        width: 30px;
        height: 100%;
        border-radius: 5px;
      }
    }
  }

  .image-area{
    width: 100%;
    display: flex;
    align-items: center;

    .button{
      width: 50px;
      height: 50px;
      cursor: pointer;
      background-repeat: no-repeat;
      background-size: cover;
    }

    .play{
      background-image: url("@/assets/img/audio/play.png");
    }

    .pause{
      background-image: url("@/assets/img/audio/pause.png");
    }

    .main-area{
      height: 100%;
      flex: 1;

      .image{
        width: 100%;
        //height: 100%;
        //margin-bottom: 110px;
        box-sizing: border-box;
        padding: 0 7px;
      }
    }

    .right-axis{
      height: 354px;
    }
  }
}

</style>
