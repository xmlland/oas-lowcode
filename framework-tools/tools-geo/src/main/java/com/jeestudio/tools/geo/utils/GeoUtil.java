package com.jeestudio.tools.geo.utils;

import com.jeestudio.tools.geo.pojo.GeoPoint;

import java.util.List;

/**
 * @Description: 地理计算工具（以地球为标准球体，有一定误差）
 */
public class GeoUtil {


    /**
     * 点位是否在点位列表范围内  距任意一点位<distance即任务在范围内
     *
     * @param point    点位
     * @param array    点位列表
     * @param distance 距离
     * @return
     */
    public static boolean inRange(GeoPoint point, List<GeoPoint> array, double distance) {
        boolean res = false;
        for (GeoPoint geoPoint : array) {
            double dis = calcDistance(point, geoPoint);
            if (dis < distance) {
                return true;
            }
        }
        return res;
    }

    /**
     * 计算两个点位之间的距离
     *
     * @param start
     * @param end
     * @return
     */
    public static double calcDistance(GeoPoint start, GeoPoint end) {
        return calcDistance(start.getLongitude(), start.getLatitude(), end.getLongitude(), end.getLatitude());
    }

    /**
     * 计算两个点位之间的距离
     *
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static double calcDistance(double lng1, double lat1, double lng2, double lat2) {
        double f = getRad((lat1 + lat2) / 2);
        double g = getRad((lat1 - lat2) / 2);
        double l = getRad((lng1 - lng2) / 2);
        double sg = Math.sin(g);
        double sl = Math.sin(l);
        double sf = Math.sin(f);
        double s, c, w, r, d, h1, h2;
        double a = 6378137.0; //The Radius of eath in meter.
        double fl = 1 / 298.257;
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
        return s;
    }

    private static double getRad(double d) {
        double PI = Math.PI;
        return d * PI / 180.0;
    }


    //定义一些常量
    static double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
    static double PI = 3.1415926535897932384626;
    static double a = 6378245.0;
    static double ee = 0.00669342162296594323;


    /**
     * 百度坐标系 (BD-09) 与 火星坐标系 (GCJ-02)的转换
     * 即 百度 转 谷歌、高德
     *
     * @param lng 经度
     * @param lat 纬度
     */
    public static GeoPoint bd09toGcj02(double lng, double lat) {
        double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        double x = lng - 0.0065;
        double y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new GeoPoint(gg_lng, gg_lat);
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换
     * 即谷歌、高德 转 百度
     *
     * @param lng 经度
     * @param lat 纬度
     */
    public static GeoPoint gcj02toBd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * x_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * x_PI);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new GeoPoint(bd_lng, bd_lat);
    }

    /**
     * WGS84转百度坐标系 (BD-09)
     *
     * @param lng 经度
     * @param lat 纬度
     */

    public static GeoPoint wgs84toBd09(double lng, double lat) {
        GeoPoint o = GeoUtil.wgs84toGcj02(lng, lat);
        return gcj02toBd09(o.getLongitude(), o.getLatitude());
    }

    /**
     * WGS84转GCj02
     *
     * @param lng 经度
     * @param lat 纬度
     */
    public static GeoPoint wgs84toGcj02(double lng, double lat) {
        if (GeoUtil.out_of_china(lng, lat)) {
            return new GeoPoint(lng, lat);
        } else {
            double dlat = transformLat(lng - 105.0, lat - 35.0);
            double dlng = transformLng(lng - 105.0, lat - 35.0);
            double radlat = lat / 180.0 * PI;
            double magic = Math.sin(radlat);
            magic = 1 - ee * magic * magic;
            double sqrtmagic = Math.sqrt(magic);
            dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI);
            dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI);
            double mglat = lat + dlat;
            double mglng = lng + dlng;
            return new GeoPoint(mglng, mglat);
        }
    }

    /**
     * GCJ02 转换为 WGS84
     *
     * @param lng 经度
     * @param lat 纬度
     */
    public static GeoPoint gcj02ToWgs84(double lng, double lat) {
        if (out_of_china(lng, lat)) {
            return new GeoPoint(lng, lat);
        } else {
            double dlat = transformLat(lng - 105.0, lat - 35.0);
            double dlng = transformLng(lng - 105.0, lat - 35.0);
            double radlat = lat / 180.0 * PI;
            double magic = Math.sin(radlat);
            magic = 1 - ee * magic * magic;
            double sqrtmagic = Math.sqrt(magic);
            dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI);
            dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI);
            double mglat = lat + dlat;
            double mglng = lng + dlng;
            return new GeoPoint(lng * 2 - mglng, lat * 2 - mglat);
        }
    }

    private static double transformLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 判断是否在国内，不在国内则不做偏移
     *
     * @param lng 经度
     * @param lat 纬度
     */
    public static boolean out_of_china(double lng, double lat) {
        return (lng < 72.004 || lng > 137.8347) || ((lat < 0.8293 || lat > 55.8271));
    }

    /**
     * 墨卡托转经纬度
     *
     * @param lngLat 经纬度
     */

    public static GeoPoint mercatorToLngLat(GeoPoint lngLat) {
        double x = lngLat.getLongitude() / 20037508.34 * 180;
        double y = lngLat.getLatitude() / 20037508.34 * 180;
        y = 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
        return new GeoPoint(x, y);
    }

    /**
     * 计算两点的方位角
     *
     * @param lngLat1 经纬度1
     * @param lngLat2 经纬度2
     */
    public static double bearing(GeoPoint lngLat1, GeoPoint lngLat2) {

        double dx = lngLat2.getLongitude() - lngLat1.getLongitude();
        double dy = lngLat2.getLatitude() - lngLat1.getLatitude();
        return (90 - Math.toDegrees(Math.atan2(dy, dx)) + 360) % 360;

    }
}
