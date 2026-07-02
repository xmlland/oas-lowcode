<template>
  <div :class="(onlyShowButton?containerClass:'') +' '+lnglatClass " :style="containerStyle" v-if="props.format==='DMS'">
    <a-input-group v-if="!onlyShowButton" size="default">
      <a-row :gutter="8">
        <a-col :span="7">
          <a-form-item name="longitude_degree"
                       :rules="[{ required: required, message: '' },{ validator : longitudeValidator,message: ''}]"
                       label="经度">
            <a-input-number :controls="controls" :precision="0" :min="0" :max="180" :readonly="readonly"
                            placeholder="度" v-model:value="longitude_degree" @change="changeLngD"></a-input-number>
          </a-form-item>
        </a-col>
        °
        <a-col :span="2">
          <a-form-item name="longitude_branch"
                       :rules="[{ required: required, message: '' },{ validator : longitudeValidator,message: ''}]">
            <a-input-number :controls="controls" :precision="0" :min="0" :max="60" :readonly="readonly" placeholder="分"
                            v-model:value="longitude_branch" @change="changeLngF"></a-input-number>
          </a-form-item>
        </a-col>
        '
        <a-col :span="2">
          <a-form-item name="longitude_second"
                       :rules="[{ required: required, message: '' },{ validator : longitudeValidator,message: ''}]">
            <a-input-number :controls="controls" :precision="DMSRetainDecimals" :min="0" :max="60" :readonly="readonly"
                            placeholder="秒" v-model:value="longitude_second" @change="changeLngM"></a-input-number>
          </a-form-item>
        </a-col>
        "
        <a-col :span="6">
          <a-form-item name="latitude_degree"
                       :rules="[{ required: required, message: '' },{ validator : latitudeValidator,message: ''}]"
                       label="纬度" :labelCol="{span:12}" style="margin-left: 80px;">
            <a-input-number :controls="controls" :precision="0" :min="0" :max="90" :readonly="readonly" placeholder="度"
                            v-model:value="latitude_degree" @change="changeLatD"
                            style="min-width: 70px;"></a-input-number>
          </a-form-item>
        </a-col>
        °
        <a-col :span="2">
          <a-form-item name="latitude_branch"
                       :rules="[{ required: required, message: '' },{ validator : latitudeValidator,message: ''}]">
            <a-input-number :controls="controls" :precision="0" :min="0" :max="60" :readonly="readonly" placeholder="分"
                            v-model:value="latitude_branch" @change="changeLatF"></a-input-number>
          </a-form-item>
        </a-col>
        '
        <a-col :span="2">
          <a-form-item name="latitude_second"
                       :rules="[{ required: required, message: '' },{ validator : latitudeValidator,message: ''}]">
            <a-input-number :controls="controls" :precision="DMSRetainDecimals" :min="0" :max="60" :readonly="readonly"
                            placeholder="秒" v-model:value="latitude_second" @change="changeLatM"></a-input-number>
          </a-form-item>
        </a-col>
        "
        <a-col :span="2">
          <a-button @click="onSelect" style="">{{ buttonText }}</a-button>
        </a-col>
      </a-row>
      <div v-if="checkLongitudeVal || checkLatitudeVal"
           style="color: red;margin-top: -10px;padding-left: calc(12%);margin-bottom: 10px;">
        <span v-html="props.scopeErrorMessage"></span>
      </div>
    </a-input-group>
    <template v-else>
      <slot/>
      <slot v-if="$slots.select" name="select" @click="onSelect"/>
      <template v-else>
        <a-button @click="onSelect">{{ buttonText }}</a-button>
      </template>
    </template>
  </div>
  <div :class="(onlyShowButton?containerClass:'')+' '+lnglatClass" :style="containerStyle" v-if="props.format==='DD'">
    <a-input-group v-if="!onlyShowButton" size="default">
      <a-row :gutter="8">
        <a-col :span="4">
          <a-form-item name="longitude"
                       :rules="[{ required: required, message: '' },{ validator : latitudeValidator,message: ''}]">
            <a-input-number :readonly="readonly" placeholder="经度" v-model:value="longitude" @change="changeLng"
                            :min="0" :max="180"></a-input-number>
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item name="latitude" label="纬度"
                       :rules="[{ required: required, message: '' },{ validator : latitudeValidator,message: ''}]"
                       style="margin-left: 14%">
            <a-input-number :readonly="readonly" placeholder="纬度" v-model:value="latitude" @change="changeLat"
                            :min="0" :max="90"></a-input-number>
          </a-form-item>
        </a-col>
        <a-col :span="5">
          <a-button @click="onSelect">{{ buttonText }}</a-button>
        </a-col>
      </a-row>
      <div v-if="checkLongitudeVal || checkLatitudeVal"
           style="color: red;margin-top: -10px;padding-left: calc(12%);margin-bottom: 10px;">
        <span v-html="props.scopeErrorMessage"></span>
      </div>
    </a-input-group>
    <template v-else>
      <slot/>
      <slot v-if="$slots.select" name="select" :event="onSelect" :buttonText="buttonText"/>
      <template v-else>
        <a-button @click="onSelect">{{ buttonText }}</a-button>
      </template>
    </template>
  </div>

  <u-modal ref='modal' :customBodyStyle="{padding:0}" :customOK="true" @clickOk="clickOk" @afterClose="onCancel"
           :showOk="!disabled">
    <u-map style="width: 100%;height: 630px" ref='showMap' :mapClickEvent="mapClickEvent">

    </u-map>
    <div style="position: absolute;top: 0;left: 0;z-index: 400;width: 100%">
      <div style="margin-left: 30px;margin-top: 15px">
        <a-row v-show="!disabled">
          <a-radio-group v-model:value="searchWay">
            <a-radio-button value="address">地址</a-radio-button>
            <a-radio-button value="lngLat">经纬度(小数)</a-radio-button>
            <a-radio-button value="lngLat2">经纬度(度分秒)</a-radio-button>
          </a-radio-group>
          <a-col :span="8" v-if="searchWay==='address'">
            <a-auto-complete
                v-model:value="addressSearchInput"
                :options="addressOptions"
                placeholder="地址"
                style="width: 100%"
                @select="addressSelect"
                @search="addressTextSearchOption"
            />
          </a-col>
          <template v-if="searchWay==='lngLat'">
            <a-col :span="4">
              <u-input @change="latLngInputChange" type="digits" :disabled="disabled" defaultValue=""
                       v-model:value="longitude" :min="0" :max="180" placeholder="请输入经度"/>
            </a-col>
            <a-col :span="4">
              <u-input @change="latLngInputChange" type="digits" :disabled="disabled" defaultValue=""
                       v-model:value="latitude" :min="0" :max="90" placeholder="请输入纬度"/>
            </a-col>
          </template>
          <template v-if="searchWay==='lngLat2'">

            <a-col :span="3">
              <a-form-item name="longitude_degree" :rules="[{ required: required, message: '' }]">
                <a-input-number :controls="controls" :precision="0" :min="0" :max="180" :readonly="readonly"
                                placeholder="度" v-model:value="longitude_degree" @change="latLngInputChange">
                  <template #addonBefore>
                    经度
                  </template>
                  <template #addonAfter>
                    °
                  </template>
                </a-input-number>
              </a-form-item>
            </a-col>
            <a-col :span="2">
              <a-form-item name="longitude_branch" :rules="[{ required: required, message: '' }]">
                <a-input-number :controls="controls" :precision="0" :min="0" :max="60" :readonly="readonly"
                                placeholder="分" v-model:value="longitude_branch" @change="latLngInputChange">
                  <template #addonAfter>
                    '
                  </template>
                </a-input-number>
              </a-form-item>
            </a-col>
            <a-col :span="2">
              <a-form-item name="longitude_second" :rules="[{ required: required, message: '' }]">
                <a-input-number :controls="controls" :precision="DMSRetainDecimals" :min="0" :max="60"
                                :readonly="readonly" placeholder="秒" v-model:value="longitude_second"
                                @change="latLngInputChange">
                  <template #addonAfter>
                    "
                  </template>
                </a-input-number>
              </a-form-item>
            </a-col>
            <a-col :span="1">
            </a-col>
            <a-col :span="3">
              <a-form-item name="latitude_degree" :rules="[{ required: required, message: '' }]">
                <a-input-number :controls="controls" :precision="0" :min="0" :max="90" :readonly="readonly"
                                placeholder="度" v-model:value="latitude_degree" @change="latLngInputChange"
                                style="min-width: 70px;">
                  <template #addonBefore>
                    纬度
                  </template>
                  <template #addonAfter>
                    °
                  </template>
                </a-input-number>
              </a-form-item>
            </a-col>
            <a-col :span="2">
              <a-form-item name="latitude_branch" :rules="[{ required: required, message: '' }]">
                <a-input-number :controls="controls" :precision="0" :min="0" :max="60" :readonly="readonly"
                                placeholder="分" v-model:value="latitude_branch" @change="latLngInputChange">
                  <template #addonAfter>
                    '
                  </template>
                </a-input-number>
              </a-form-item>
            </a-col>
            <a-col :span="2">
              <a-form-item name="latitude_second" :rules="[{ required: required, message: '' }]">
                <a-input-number :controls="controls" :precision="DMSRetainDecimals" :min="0" :max="60"
                                :readonly="readonly" placeholder="秒" v-model:value="latitude_second"
                                @change="latLngInputChange">
                  <template #addonAfter>
                    "
                  </template>
                </a-input-number>
              </a-form-item>
            </a-col>
          </template>
        </a-row>
      </div>
    </div>
  </u-modal>
</template>
<script>
export default {
  name: "ULngLatSelect",
}
</script>
<script setup>
import UMap from "@/components/map/UMap";
import icon from "@/assets/img/maker-icon.png";
import config from "@/config";
import axios from "axios";
import {getCurrentInstance, ref, watch, onErrorCaptured} from "vue";
import {isEmpty, isNotEmpty} from "@/lib/tools";
import {message} from "ant-design-vue";

let props = defineProps({
  format: {
    type: String,
    default: 'DMS'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: true
  },
  required: {
    type: Boolean,
    default: false
  },
  longitudeDegree: {
    type: [String, Number],
    default: ''
  },
  longitudeBranch: {
    type: [String, Number],
    default: ''
  },
  longitudeSecond: {
    type: [String, Number],
    default: ''
  },
  latitudeDegree: {
    type: [String, Number],
    default: ''
  },
  latitudeBranch: {
    type: [String, Number],
    default: ''
  },
  latitudeSecond: {
    type: [String, Number],
    default: ''
  },
  longitude: {
    type: [String, Number],
    default: ''
  },
  latitude: {
    type: [String, Number],
    default: ''
  },
  /**
   * DMS模式保留几位小数
   */
  DMSRetainDecimals: {
    type: Number,
    default: 2
  },
  /**
   * DD模式保留几位小数
   */
  DDRetainDecimals: {
    type: Number,
    default: 6
  },
  buttonText: {
    type: String,
    default: '地图选择'
  },
  /**
   * 仅显示选择按钮
   */
  onlyShowButton: {
    type: Boolean,
    default: false
  },
  // 是否显示增减按钮
  controls: {
    type: Boolean,
    default: true
  },
  containerClass: {
    type: String,
    default: 'ant-col ant-col-24'
  },
  lnglatClass: {
    type: String,
    default: 'lnglat-select'
  },
  containerStyle: {
    type: Object,
    default() {
      return {}
    }
  },
  // 经度范围 区域 ，默认为 中国范围
  longitudeScope: {
    type: Array,
    default() {
      return [73.5, 135.2]
    }
  },
  // 纬度范围 区域 ，默认为 中国范围
  latitudeScope: {
    type: Array,
    default() {
      return [3.8, 48.5]
    }
  },
  // 纬度范围 区域 ，默认为 中国范围
  scopeErrorMessage: {
    type: String,
    default: "请选择合理位置！",
  }
})

onErrorCaptured((err) => {
  console.log(err);
  return false;
})

let instance = getCurrentInstance();
let maker = null;
let isInputOrClick = ref(false) // true 是输入框输入值，false是点击地图
const showMap = ref(null)//这个时候获取了子组件
const mapClickEvent = ref(function (e) {
  isInputOrClick.value = false;
  if (!props.disabled) {
    maker.setLatLng(e.latlng);
  }
  maker.openPopup();
})

//定义经纬度变量
let longitude_degree = ref(''),
    longitude_branch = ref(''),
    longitude_second = ref(''),
    latitude_degree = ref(''),
    latitude_branch = ref(''),
    latitude_second = ref(''),
    longitude = ref(''),
    latitude = ref(''),
    makerRes = null;

let checkLongitudeVal = ref(false);
let checkLatitudeVal = ref(false);
let saveCloseFlag = ref(false);

let addressSearchInput = ref('');
let addressOptions = ref([]);
let searchWay = ref('address');
const latLngInputChange = () => {
  if (searchWay.value === 'lngLat') {
    LngLatToAddress(latitude.value, longitude.value).then(address => {
      emits('textAddressShow', address + '')
      maker.setLatLng({
        lat: latitude.value,
        lng: longitude.value
      });
      maker.openPopup();
    })
  } else if (searchWay.value === 'lngLat2') {
    let lng = (parseFloat(isEmpty(longitude_second.value) ? '0' : longitude_second.value) / 60 + parseInt(isEmpty(longitude_branch.value) ? '0' : longitude_branch.value)) / 60 + parseInt(isEmpty(longitude_degree.value) ? '0' : longitude_degree.value);
    let lat = (parseFloat(isEmpty(latitude_second.value) ? '0' : latitude_second.value) / 60 + parseInt(isEmpty(latitude_branch.value) ? '0' : latitude_branch.value)) / 60 + parseInt(isEmpty(latitude_degree.value) ? '0' : latitude_degree.value);
    LngLatToAddress(lat, lng).then(address => {
      emits('textAddressShow', address + '')
      maker.setLatLng({
        lat: lat,
        lng: lng
      });
      maker.openPopup();
    })
  }
}

//按钮点击事件
const onSelect = () => {
  let lng = config.initLng;
  let lat = config.initLat;
  if (props.format === 'DMS' && isNotEmpty(props.longitudeDegree) && isNotEmpty(props.latitudeDegree)) {
    lng = (parseFloat(isEmpty(props.longitudeSecond) ? '0' : props.longitudeSecond) / 60 + parseInt(isEmpty(longitude_branch.value) ? '0' : longitude_branch.value)) / 60 + parseInt(isEmpty(props.longitudeDegree) ? '0' : props.longitudeDegree);
    lat = (parseFloat(isEmpty(props.latitudeSecond) ? '0' : props.latitudeSecond) / 60 + parseInt(isEmpty(props.latitudeBranch) ? '0' : props.latitudeBranch)) / 60 + parseInt(isEmpty(props.latitudeDegree) ? '0' : props.latitudeDegree);
  }
  if (props.format === 'DD' && isNotEmpty(props.longitude) && isNotEmpty(props.latitude)) {
    lng = props.longitude;
    lat = props.latitude;
  }
  if (props.disabled) {
    instance.refs.modal.open('查看位置')
  } else {
    instance.refs.modal.open('选择位置')
  }
  saveCloseFlag.value = false;
  instance.refs.modal.$nextTick(() => {
    let map = showMap.value.getMap(); //获取map
    maker = map.createMarker(lng, lat, { //创建maker
      popupContent: function (e) {

        makerRes = e._latlng;

        let lng_d = Math.floor(e._latlng.lng);
        let lng_f = Math.floor((e._latlng.lng - lng_d) * 60);
        let lng_m = (((e._latlng.lng - lng_d) * 60 - lng_f) * 60).toFixed(props.DMSRetainDecimals);
        let lat_d = Math.floor(e._latlng.lat);
        let lat_f = Math.floor((e._latlng.lat - lat_d) * 60);
        let lat_m = (((e._latlng.lat - lat_d) * 60 - lat_f) * 60).toFixed(props.DMSRetainDecimals);
        longitude_degree.value = lng_d;
        longitude_branch.value = lng_f;
        longitude_second.value = lng_m;
        latitude_degree.value = lat_d;
        latitude_branch.value = lat_f;
        latitude_second.value = lat_m;

        longitude.value = e._latlng.lng.toFixed(props.DDRetainDecimals);
        latitude.value = e._latlng.lat.toFixed(props.DDRetainDecimals);

        if (props.format === "DMS") {
          return '经度:' + lng_d + '°' + lng_f + '\'' + lng_m + '"' + ',' + '纬度:' + lat_d + '°' + lat_f + '\'' + lat_m + '"'
        } else {
          return '经度:' + e._latlng.lng.toFixed(props.DDRetainDecimals) + ',' + '纬度:' + e._latlng.lat.toFixed(props.DDRetainDecimals)
        }
      }, draggable: false, autoPan: true,
    }, {
      iconUrl: icon,
      popupAnchor: [0, -32],
      iconAnchor: [16, 32]
    }, false, true);
    //maker的移动事件
    if (!props.disabled) {
      maker.options.draggable = true
      maker.on("dragend", (res) => {
        makerRes = res.target._latlng;
        //显示maker弹窗
        maker.openPopup();
      })
    }
    map.addMarkers([maker], true); //添加maker
    //显示maker弹窗
    maker.openPopup();
  })
}

const clickOk = () => {
  //将maker的值传给input
  Promise.all([
    longitudeValidator(),
    latitudeValidator()
  ]).then(() => {
    if (props.format === 'DMS') {
      longitude_degree.value = Math.floor(makerRes.lng) + '';
      longitude_branch.value = Math.floor((makerRes.lng - parseInt(longitude_degree.value)) * 60) + '';
      longitude_second.value = (((makerRes.lng - parseInt(longitude_degree.value)) * 60 - parseInt(longitude_branch.value)) * 60).toFixed(props.DMSRetainDecimals) + '';
      latitude_degree.value = Math.floor(makerRes.lat) + '';
      latitude_branch.value = Math.floor((makerRes.lat - parseInt(latitude_degree.value)) * 60) + '';
      latitude_second.value = (((makerRes.lat - parseInt(latitude_degree.value)) * 60 - parseInt(latitude_branch.value)) * 60).toFixed(props.DMSRetainDecimals) + '';

      emits('update:longitudeDegree', longitude_degree.value + '')
      emits('update:longitudeBranch', longitude_branch.value + '')
      emits('update:longitudeSecond', longitude_second.value + '')
      emits('update:latitudeDegree', latitude_degree.value + '')
      emits('update:latitudeBranch', latitude_branch.value + '')
      emits('update:latitudeSecond', latitude_second.value + '')
    } else if (props.format === 'DD') {
      longitude.value = makerRes.lng.toFixed(props.DDRetainDecimals)
      latitude.value = makerRes.lat.toFixed(props.DDRetainDecimals);
      if (isInputOrClick.value) {
        emits('update:longitude', longitude.value + '')
        emits('update:latitude', latitude.value + '')
      } else {
        LngLatToAddress(latitude.value, longitude.value).then(address => {
          console.log(address)
          addressSearchInput.value = ''
          addressOptions.value = []
          emits('update:longitude', longitude.value + '')
          emits('update:latitude', latitude.value + '')
          emits('textAddressShow', address + '')
        })
      }
    }
    // 确定保存关闭
    saveCloseFlag.value = true;

    instance.refs.modal.close();
  }).catch(() => {
    message.warn(props.scopeErrorMessage || "请选择中国境内范围位置！")
  })
}

const onCancel = () => {
  if ( !saveCloseFlag.value ){
    // 取消时，数据回显
    longitude_degree.value = props.longitudeDegree;
    longitude_branch.value = props.longitudeBranch;
    longitude_second.value = props.longitudeSecond;
    latitude_degree.value = props.latitudeDegree;
    latitude_branch.value = props.latitudeBranch;
    latitude_second.value = props.latitudeSecond;
    longitude.value = props.longitude;
    latitude.value = props.latitude;

  }
}

watch(() => props.longitudeDegree, () => {
  longitude_degree.value = props.longitudeDegree;
}, {immediate: true})

watch(() => props.longitudeBranch, () => {
  longitude_branch.value = props.longitudeBranch;
}, {immediate: true})

watch(() => props.longitudeSecond, () => {
  longitude_second.value = props.longitudeSecond;
}, {immediate: true})

watch(() => props.latitudeDegree, () => {
  latitude_degree.value = props.latitudeDegree;
}, {immediate: true})

watch(() => props.latitudeBranch, () => {
  latitude_branch.value = props.latitudeBranch;
}, {immediate: true})

watch(() => props.latitudeSecond, () => {
  latitude_second.value = props.latitudeSecond;
}, {immediate: true})

watch(() => props.longitude, () => {
  longitude.value = props.longitude;
}, {immediate: true})

watch(() => props.latitude, () => {
  latitude.value = props.latitude;
}, {immediate: true})

let emits = defineEmits(['textAddressShow', 'update:longitudeDegree', 'update:longitudeBranch', 'update:longitudeSecond', 'update:latitudeDegree', 'update:latitudeBranch', 'update:latitudeSecond', 'update:longitude', 'update:latitude']);

const changeLngD = () => {
  emits('update:longitudeDegree', longitude_degree.value + '')
}

const changeLngF = () => {
  emits('update:longitudeBranch', longitude_branch.value + '')
}

const changeLngM = () => {
  emits('update:longitudeSecond', longitude_second.value + '')
}

const changeLatD = () => {
  emits('update:latitudeDegree', latitude_degree.value + '')
}

const changeLatF = () => {
  emits('update:latitudeBranch', latitude_branch.value + '')
}

const changeLatM = () => {
  emits('update:latitudeSecond', latitude_second.value + '')
}

const changeLng = () => {
  emits('update:longitude', longitude.value + '')
}

const changeLat = () => {
  emits('update:latitude', latitude.value + '')
}

const addressTextSearchOption = (searchText) => {
  let map = showMap.value.getMap()
  var bounds = map.map.getBounds();
  var southWest = bounds.getSouthWest();
  var northEast = bounds.getNorthEast();
  let bound = southWest.lng + ',' + southWest.lat + ',' + northEast.lng + ',' + northEast.lat
  axios({
    method: 'GET',
    url: 'http://api.tianditu.gov.cn/v2/search',
    params: {
      postStr: {
        "keyWord": searchText,
        "level": 12,
        "mapBound": bound,
        "queryType": 2,
        "start": 0,
        "count": 10
      },
      type: 'query',
      tk: config.tdtKey
    }
  }).then((res) => {
    if (res.data.status.infocode === 1000) {
      if (!addressSearchInput.value) {
        addressOptions.value = [];
      } else {
        let pList = [];
        for (let p of res.data.pois) {
          pList.push({value: p.name + '(' + p.address + ')', p: p});
        }
        addressOptions.value = pList;
      }
    } else {
      // alert(result.getMsg());
    }
  }).catch((err) => {

  })

}

const addressSelect = (value, all) => {
  isInputOrClick.value = true;
  let lonlatArray = all.p.lonlat.split(',');
  if (isInputOrClick.value) {
    LngLatToAddress(lonlatArray[1], lonlatArray[0]).then(address => {
      // console.log(address)
      emits('textAddressShow', address + '')
      maker.setLatLng({
        lat: lonlatArray[1],
        lng: lonlatArray[0]
      });
      maker.openPopup();
    })
  }

}
const LngLatToAddress = (lat, lng) => {
  return new Promise((resolve => {
    axios({
      method: 'GET',
      url: 'http://api.tianditu.gov.cn/geocoder',
      params: {
        postStr: {
          "lon": lng,
          "lat": lat,
          "ver": 1
        },
        type: 'geocode',
        tk: config.tdtKey
      }
    }).then((res) => {
      console.log(res.data)
      if (res.data.status === "0") {
        resolve(res.data.result.formatted_address)
      } else {
        resolve('')
      }
    }).catch((err) => {
      resolve('')
    })
  }))
}
const longitudeValidator = () => {
  let lng = config.initLng;
  if (instance.refs.modal.getModalVisible()) {
    lng = makerRes.lng.toFixed(props.DDRetainDecimals);
  } else if (props.format === 'DMS' && isNotEmpty(props.longitudeDegree) && isNotEmpty(props.latitudeDegree)) {
    lng = (parseFloat(isEmpty(props.longitudeSecond) ? '0' : props.longitudeSecond) / 60 + parseInt(isEmpty(longitude_branch.value) ? '0' : longitude_branch.value)) / 60 + parseInt(isEmpty(props.longitudeDegree) ? '0' : props.longitudeDegree);
  } else if (props.format === 'DD' && isNotEmpty(props.longitude) && isNotEmpty(props.latitude)) {
    lng = props.longitude;
  }

  if (props.longitudeScope[0] > lng || lng > props.longitudeScope[1]) {
    checkLongitudeVal.value = true;
    return Promise.reject("");
  }
  checkLongitudeVal.value = false;
  return Promise.resolve();
}
const latitudeValidator = () => {
  let lat = config.initLat;

  if (instance.refs.modal.getModalVisible()) {
    lat = makerRes.lat.toFixed(props.DDRetainDecimals);
  } else if (props.format === 'DMS' && isNotEmpty(props.longitudeDegree) && isNotEmpty(props.latitudeDegree)) {
    lat = (parseFloat(isEmpty(props.latitudeSecond) ? '0' : props.latitudeSecond) / 60 + parseInt(isEmpty(props.latitudeBranch) ? '0' : props.latitudeBranch)) / 60 + parseInt(isEmpty(props.latitudeDegree) ? '0' : props.latitudeDegree);
  } else if (props.format === 'DD' && isNotEmpty(props.longitude) && isNotEmpty(props.latitude)) {
    lat = props.latitude;
  }

  if (props.latitudeScope[0] > lat || lat > props.latitudeScope[1]) {
    checkLatitudeVal.value = true;
    return Promise.reject("");
  }
  checkLatitudeVal.value = false;
  return Promise.resolve();
}
</script>
<style scoped>
.ant-input-number {
  width: 100%;
}
</style>
