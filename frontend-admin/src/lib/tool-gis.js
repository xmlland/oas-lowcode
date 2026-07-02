/**
 * 计算两个经纬度之间的距离
 * @param lng1 经度1
 * @param lat1 纬度1
 * @param lng2 经度2
 * @param lat2 纬度2
 * @return {number}
 */
export const getDistance = (lng1, lat1, lng2, lat2) => {
    lng1 = parseFloat(lng1)
    lat1 = parseFloat(lat1)
    lng2 = parseFloat(lng2)
    lat2 = parseFloat(lat2)

    function getRad(d) {
        let PI = Math.PI;
        return d * PI / 180.0;
    }

    let f = getRad((lat1 + lat2) / 2);
    let g = getRad((lat1 - lat2) / 2);
    let l = getRad((lng1 - lng2) / 2);
    let sg = Math.sin(g);
    let sl = Math.sin(l);
    let sf = Math.sin(f);
    let s, c, w, r, d, h1, h2;
    let a = 6378137.0; //The Radius of eath in meter.
    let fl = 1 / 298.257;
    sg = sg * sg;
    sl = sl * sl;
    sf = sf * sf;
    s = sg * (1 - sl) + (1 - sf) * sl;
    c = (1 - sg) * (1 - sl) + sf * sl;
    w = Math.atan(Math.sqrt(s / c));
    r = Math.sqrt(s * c) / w;
    d = 2 * w * a;
    h1 = (3 * r - 1) / 2 / c;
    h2 = (3 * r + 1) / 2 / s;
    s = d * (1 + fl * (h1 * sf * (1 - sg) - h2 * (1 - sf) * sg));
    s = s.toFixed(2); //指定小数点后的位数。
    return parseFloat(s)

}
/**
 * 从列表中找出距离某个点最近的点
 * @param lng 经度
 * @param lat 纬度
 * @param array 列表
 * @param positionFunc 获取经纬度的方法
 * @param distanceKey 距离的key
 * @return {*}
 */
export const getMinDistancePoint = (lng, lat, array, positionFunc = (item) => {
    return {
        lng: item.lng,
        lat: item.lat
    }
}, distanceKey = 'distance') => {
    let min = -1;
    let result;
    array.forEach((item) => {
        let point = positionFunc(item);
        let d = getDistance(point.lng, point.lat, lng, lat);
        if (min === -1) {
            min = d
            result = item;
            result[distanceKey] = d;
        }
        if (d < min) {
            min = d;
            result = item;
            result[distanceKey] = d;
        }
    })
    return result;
}

/**
 * 百度坐标系 (BD-09) 与 火星坐标系 (GCJ-02)的转换
 * 即 百度 转 谷歌、高德
 * @param bd_lon
 * @param bd_lat
 * @returns {*[]}
 */
export const bd09ToGcj02 = (bd_lon, bd_lat) => {
    let x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    let x = bd_lon - 0.0065;
    let y = bd_lat - 0.006;
    let z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
    let theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
    let gg_lng = z * Math.cos(theta);
    let gg_lat = z * Math.sin(theta);
    return [gg_lng, gg_lat]
}
/**
 * 判断是否在国内，不在国内则不做偏移
 * @param lng
 * @param lat
 * @returns {boolean}
 */
const out_of_china = (lng, lat)=> {
    return (lng < 72.004 || lng > 137.8347) || ((lat < 0.8293 || lat > 55.8271) || false);
}
//定义一些常量
const PI = 3.1415926535897932384626;
const a = 6378245.0;
const ee = 0.00669342162296594323;
const transformLat = (lng, lat) => {
    let ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
    ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
    ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
    return ret
}

const transformLng = (lng, lat) => {
    let ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
    ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
    ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
    return ret
}
/**
 * WGS84转GCj02
 * @param lng
 * @param lat
 * @returns {*[]}
 */
export const wgs84ToGcj02 = (lng, lat) => {
    lng = parseFloat(lng);
    lat = parseFloat(lat);
    if (out_of_china(lng, lat)) {
        return [lng, lat]
    } else {
        let dLat = transformLat(lng - 105.0, lat - 35.0);
        let dLng = transformLng(lng - 105.0, lat - 35.0);
        let radLat = lat / 180.0 * PI;
        let magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        let sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        let mgLat = lat + dLat;
        let mgLng = lng + dLng;
        return [mgLng, mgLat]
    }
}

export const gcj02toWgs84=(lng, lat) => {
    const PI = Math.PI;
    const a = 6378245.0;
    const ee = 0.00669342162296594323;

    function transformLat(x, y) {
        let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    function transformLng(x, y) {
        let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    if (out_of_china(lng, lat)) {
        return {lng, lat};
    } else {
        let dLat = transformLat(lng - 105.0, lat - 35.0);
        let dLng = transformLng(lng - 105.0, lat - 35.0);
        let radLat = lat / 180.0 * PI;
        let magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        let sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        let mgLat = lat + dLat;
        let mgLng = lng + dLng;
        return [lng * 2 - mgLng, lat * 2 - mgLat];
    }
}
