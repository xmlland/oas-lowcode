import L from 'leaflet';
import 'leaflet'
import 'leaflet/dist/leaflet.css'
import 'leaflet.markercluster/dist/MarkerCluster.Default.css'
import 'leaflet.markercluster'
import './BeautifyMarker-master/leaflet-beautify-marker-icon.css'
import './BeautifyMarker-master/leaflet-beautify-marker-icon'
import './leaflet/iclient-leaflet-es6.min'//引入超图leaflet的js
import 'font-awesome/css/font-awesome.min.css'

import './leaflet/proj4'
import './leaflet/proj4leaflet'

const vecUrl = '//t{s}.tianditu.gov.cn/DataServer?T=vec_w&X={x}&Y={y}&L={z}&tk='//矢量图层
const cvaUrl = '//t{s}.tianditu.gov.cn/DataServer?T=cva_w&X={x}&Y={y}&L={z}&tk='//矢量标注图层
const imgUrl = '//t{s}.tianditu.gov.cn/DataServer?T=img_w&X={x}&Y={y}&L={z}&tk='//影像图层
const ciaUrl = '//t{s}.tianditu.gov.cn/DataServer?T=cia_w&X={x}&Y={y}&L={z}&tk='//影像标注图层
import config from "@/config";
import {createApp, h} from "vue";
import Antd from "ant-design-vue";
import UComponent from "@/components";
import store from "@/store";
import router from "@/router";
import leafletImage from './leaflet-image';
import html2canvas from "html2canvas";
const subdomains = ['0', '1', '2', '3', '4', '5', '6', '7']
const CRS ={
    EPSG3857: L.CRS.EPSG3857
}

//可以在config中定义矢量及影像地图的地址 zhoury 2024-07-02
const customVecUrl = window.customVecUrl || []
const customImgUrl = window.customImgUrl || []
//超图服务的url
const superMapServiceUrl = window.superMapServiceUrl || null

class Umap {

    /**
     *
     * @param id
     * @param centerLng
     * @param centerLat
     * @param zoomControl
     * @param centerMarker
     * @param centerTitle
     * @param zoom
     * @param minZoom
     * @param maxZoom
     * @param type
     * @param mapClickEvent
     * @param mapZoomEvent
     * @param mapLoaded
     */
    constructor({
                    id = 'map-div', centerLng = config.initLng, centerLat = config.initLat,
                    zoomControl = false, centerMarker = false, centerTitle = '', zoom = config.initZoom,
                    minZoom = 5, maxZoom = 18,
                    type = 'vec',
                    mapClickEvent = () => {
                    }, mapZoomEvent = () => {
                    }, mapLoaded = () => {
                    }
                }) {

        let options = {id, centerLng, centerLat, zoomControl, centerMarker, centerTitle, zoom, minZoom, maxZoom}
        this.options = options
        if (window.customMapCrs && CRS[window.customMapCrs]) {
            //使用自定义的crs
            this.options.crs = CRS[window.customMapCrs]
        }

        let baseLayers = []

        if (type === 'vec') {
            if (customVecUrl && customVecUrl.length > 0){
                customVecUrl.forEach(url=>{
                    baseLayers.push(L.tileLayer(url, {subdomains: subdomains}))
                })
            }else{
                baseLayers.push(L.tileLayer(vecUrl + config.tdtKey, {subdomains: subdomains}))
                baseLayers.push(L.tileLayer(cvaUrl + config.tdtKey, {subdomains: subdomains}))
            }
        } else if (type === 'img') {
            if (customImgUrl && customImgUrl.length > 0){
                customImgUrl.forEach(url=>{
                    baseLayers.push(L.tileLayer(url, {subdomains: subdomains}))
                })
            }else{
                baseLayers.push(L.tileLayer(imgUrl + config.tdtKey, {subdomains: subdomains}))
                baseLayers.push(L.tileLayer(ciaUrl + config.tdtKey, {subdomains: subdomains}))
            }
        }

        let init = null
        if (superMapServiceUrl){
            //加载超图的底图
            let _options = this.options
            init = new Promise(resolve => {
                let mapOption = initMapOption(_options)
                new L.supermap.MapService(superMapServiceUrl).getMapInfo().then((res) => {
                    mapOption.crs = L.supermap.crsFromMapJSON(res.result)
                    let _map = L.map(_options.id, mapOption);
                    new L.supermap.TiledMapLayer(superMapServiceUrl).addTo(_map);
                    resolve(_map)
                });
            })
        }else{
            init = new Promise(resolve => {
                let _map = this.initMap()
                baseLayers.forEach(item => {
                    item.addTo(_map)
                })
                resolve(_map)
            })
        }
        init.then((_map)=>{
            this.map = _map
            this.map.on('click', mapClickEvent)
            this.map.on('zoom', mapZoomEvent)
            if (options.centerMarker) {
                this.centerMarker = this.addPoint(options.centerLng, options.centerLat)
            }
            mapLoaded()
        })

        /*L.control.zoom({
            zoomInTitle: '放大',
            zoomOutTitle: '缩小'
        }).addTo(this.map);*/
    }

    initMap = function () {
        let _options = this.options
        return L.map(_options.id, initMapOption(_options))
    }

    /**
     * 添加图层
     * @param url 图层地址
     * @param options 图层属性
     * @return {*}
     */
    addTileLayer = function (url, options = {}) {
        let tileLayer = new L.tileLayer(url, options)
        tileLayer.addTo(this.map);
        return tileLayer
    }

    addPoint = function (lng, lat) {

        let lat_lng = L.latLng(lat, lng)
        let circleMarker = L.circleMarker(lat_lng, {
            draggable: false,
            radius: 5,
            fill: true,
            fillOpacity: 1
        })
        circleMarker.bindTooltip(this.options.centerTitle, {
            offset: L.point(0, -10),
            direction: 'top'
        }).addTo(this.map)

        if (this.options.centerTitle !== '') {
            circleMarker.openTooltip()
        }

        return circleMarker
    }

    createIcon = function (iconOptions, beautifyIcon = false) {
        if (beautifyIcon) {
            return L.BeautifyIcon.icon(iconOptions)
        } else {
            return L.icon(iconOptions)
        }

    }

    createMarker = function (lng, lat, options, iconOptions, beautifyIcon = false, popup = false) {
        let lat_lng = L.latLng(parseFloat(lat), parseFloat(lng))
        if (iconOptions) {
            options.icon = this.createIcon(iconOptions, beautifyIcon)
        }
        let marker = L.marker(lat_lng, options)
        if (popup) {
            marker.bindPopup(options.popupContent)
        }
        return marker
    }

    addMarker = function (lng, lat, options) {
        let marker = this.createMarker(lng, lat, options)
        marker.addTo(this.map)
        return marker
    }
    addMarkers = function (markers = [], fitMarkers = false) {
        let group = L.layerGroup(markers)
        group.addTo(this.map)
        if (fitMarkers && markers.length > 0) {
            // let latlngs = []
            // markers.forEach(marker => {
            //     latlngs.push(marker.getLatLng())
            // })
            // let bounds = L.latLngBounds(latlngs)
            this.map.setView(markers[0].getLatLng(),this.options.zoom)
        }
        return group
    }
    addMarkerClusterGroup = function (markers = [], fitMarkers = false,maxClusterRadius=30) {
        let markerClusterGroup = new L.MarkerClusterGroup({
            maxClusterRadius: maxClusterRadius,
        })
        markers.forEach(item => {
            markerClusterGroup.addLayer(item)
        })
        if (fitMarkers && markers.length > 0) {
            let latlngs = []
            markers.forEach(marker => {
                latlngs.push(marker.getLatLng())
            })
            let bounds = L.latLngBounds(latlngs)
            this.map.fitBounds(bounds)
        }
        this.map.addLayer(markerClusterGroup)
        return markerClusterGroup
    }
    addPolyline = function (lnglats, options) {
        let polyline = L.polyline(lnglats, options).addTo(this.map)
        return polyline
    }
    addPolygon = function (lnglats, options) {
        let polygon = L.polygon(lnglats, options).addTo(this.map)
        return polygon
    }

    addGeoJson = function (geoJson, options) {
        return L.geoJSON(geoJson, options).addTo(this.map)
    }
    /**
     * 显示组件在弹出框中
     * @param marker marker
     * @param component 组件
     * @param props 组件属性
     * @param popupOptions 弹出框属性
     */
    openComponentInPopup = function (marker, component, props,popupOptions = {}) {
        let popupId = this.options.id + '-popup'
        let div = document.getElementById(popupId);
        if (!div) {
            div = document.createElement('div');
            div.id = popupId;
        }
        if (this.appObject) {
            this.appObject.unmount();
        }
        this.appObject = createApp({
            setup() {
            },
            render() {
                props.maker = marker;
                return h(component, props);
            },
        });
        this.appObject.use(Antd).use(UComponent).use(store).use(router)
        this.appObject.mount(div);
        let popup = marker.getPopup()
        if (popup) {
            marker.unbindPopup();
        }
        marker.bindPopup(div,popupOptions);
        marker.openPopup()
    }
    /**
     * 打印地图快照
     */
    printMapSnapshot() {
        if (!this.map) {
            console.error('Map is not initialized.');
            return;
        }

        // 创建加载动画
        const loadingDiv = document.createElement('div');
        loadingDiv.id = 'loading-indicator';
        loadingDiv.style.position = 'fixed';
        loadingDiv.style.top = '0';
        loadingDiv.style.left = '0';
        loadingDiv.style.width = '100vw';
        loadingDiv.style.height = '100vh';
        loadingDiv.style.backgroundColor = 'rgba(255, 255, 255, 0.8)';
        loadingDiv.style.zIndex = '9999';
        loadingDiv.style.display = 'flex';
        loadingDiv.style.justifyContent = 'center';
        loadingDiv.style.alignItems = 'center';
        loadingDiv.innerHTML = `
            <div style="border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 50px; height: 50px; animation: spin 1s linear infinite;"></div>
            <style>
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            </style>
        `;
        document.body.appendChild(loadingDiv);

        // 使用 leafletImage 生成快照
        leafletImage(this.map, (err, canvas) => {
            // 移除加载动画
            document.body.removeChild(loadingDiv);

            if (err) {
                console.error('Error generating image:', err);
                return;
            }

            // 将 Canvas 转换为图片数据
            const imgData = canvas.toDataURL("image/png");

            // 动态创建打印页面的样式
            const style = `
                <style>
                    @media print {
                        body {
                            margin: 0;
                            padding: 0;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                        }
                        img {
                            max-width: 100%;
                            max-height: 100%;
                        }
                    }
                </style>
            `;

            // 创建一个新窗口用于打印
            const printWindow = window.open("", "_blank");
            printWindow.document.write(`
                <html>
                <head>
                    <title>打印地图快照</title>
                    ${style}
                </head>
                <body>
                    <img src="${imgData}" alt="地图快照"/>
                </body>
                </html>
            `);

            // 打印完成后关闭窗口
            printWindow.document.close();
            printWindow.focus();
            printWindow.onload = () => {
                printWindow.print();
                printWindow.close();
            };
        });
    }
    /**
     * 下载地图快照
     */
    downloadMapSnapshot() {
        if (!this.map) {
            console.error('Map is not initialized.');
            return;
        }
        // leafletImage(this.map, (err, canvas) => {
        //     if (err) {
        //         console.error('Error generating image:', err);
        //         return;
        //     }
        //     canvas.toBlob((blob) => {
        //         const url = URL.createObjectURL(blob);
        //         const a = document.createElement("a");
        //         a.href = url;
        //         a.download = "map_snapshot.png";
        //         document.body.appendChild(a);
        //         a.click();
        //         document.body.removeChild(a);
        //     });
        // });
        html2canvas(document.querySelector("#" + this.options.id), {
            useCORS: true, //保证跨域图片的显示，如果为不添加改属性，或者值为false,地图底图不显示
        }).then((canvas) => {
            canvas.toBlob(function (blob) {
                const url = URL.createObjectURL(blob);
                const a = window.parent.document.createElement("a");
                a.href = url;
                a.download = "专题图.png";
                window.parent.document.body.appendChild(a);
                a.click();
            });
        });
    }
}
const initMapOption = function (options) {
    let _options = options
    return {
        center: [_options.centerLat, _options.centerLng], // The initial center(baidu BD-09 format) of map
        zoom: _options.zoom, // initial zoom of map
        minZoom: _options.minZoom,
        maxZoom: _options.maxZoom,
        zoomControl: _options.zoomControl,
        attributionControl: true, // 移除右下角leaflet标识
    }
}
export default Umap
