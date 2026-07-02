package com.jeestudio.tools.geo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.geo.FeatureCollection;
import com.jeestudio.tools.geo.entity.Polygon2D;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
/**
 * @Description: 从GeoJSON文件加载多边形Polygon2D
 */
public class GeoJsonPolygonLoader {
    /**
     * @Description: 从GeoJSON文件中读取，转换为FeatureCollection
     * @param filePath GeoJSON文件路径
     * @return com.alibaba.fastjson.support.geo.FeatureCollection
     **/
    public static FeatureCollection loadFromGeoJsonFeatureCollection(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return JSON.parseObject(content, FeatureCollection.class);
    }
    /**
     * @Description: 从GeoJSON文件中读取，转换为FeatureCollection
     * @param file GeoJSON文件
     * @return com.alibaba.fastjson.support.geo.FeatureCollection
     **/
    public static FeatureCollection loadFromGeoJsonFeatureCollection(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        return JSON.parseObject(content, FeatureCollection.class);
    }

    /**
     * @Description: 从GeoJSON文件中读取，转换为Polygon2D多边形
     * @param filePath GeoJSON文件路径
     * @return java.util.List<com.jeestudio.tools.geo.entity.Polygon2D>
     **/
    public static List<Polygon2D> loadFromGeoJsonFeaturePolygons(String filePath) throws IOException {
        String geojson = new String(Files.readAllBytes(Paths.get(filePath)));
        return convertFeatureCollectionToPolygons(geojson);
    }
    /**
     * @Description: 从GeoJSON文件中读取，转换为Polygon2D多边形
     * @param file GeoJSON文件
     * @return java.util.List<com.jeestudio.tools.geo.entity.Polygon2D>
     **/
    public static List<Polygon2D> loadFromGeoJsonFeaturePolygons(File file) throws IOException {
        String geojson = new String(Files.readAllBytes(file.toPath()));
        return convertFeatureCollectionToPolygons(geojson);
    }
    /**
     * @Description: 从GeoJSON文件中读取，转换为Polygon2D多边形
     * @param filePath GeoJSON文件路径
     * @return com.jeestudio.tools.geo.entity.Polygon2D
     **/
    public static Polygon2D loadFromGeoJsonFeaturePolygon(String filePath) throws IOException {
        String geojson = new String(Files.readAllBytes(Paths.get(filePath)));
        List<Polygon2D> polygons = convertFeatureCollectionToPolygons(geojson);
        if (polygons.isEmpty()) {
            System.err.println(" GeoJSON FeatureCollection is empty");
            throw new IllegalArgumentException(" GeoJSON FeatureCollection is empty");
        }else if (polygons.size() > 1) {
            System.err.println(" GeoJSON FeatureCollection has more than one Polygon");
            throw new IllegalArgumentException(" GeoJSON FeatureCollection has more than one Polygon");
        }
        return polygons.get(0);
    }
    /**
     * @Description: 从GeoJSON文件中读取，转换为Polygon2D多边形
     * @param file GeoJSON文件
     * @return com.jeestudio.tools.geo.entity.Polygon2D
     **/
    public static Polygon2D loadFromGeoJsonFeaturePolygon(File file) throws IOException {
        String geojson = new String(Files.readAllBytes(file.toPath()));
        List<Polygon2D> polygons = convertFeatureCollectionToPolygons(geojson);
        if (polygons.isEmpty()) {
            System.err.println(" GeoJSON FeatureCollection is empty");
            throw new IllegalArgumentException(" GeoJSON FeatureCollection is empty");
        }else if (polygons.size() > 1) {
            System.err.println(" GeoJSON FeatureCollection has more than one Polygon");
            throw new IllegalArgumentException(" GeoJSON FeatureCollection has more than one Polygon");
        }
        return polygons.get(0);
    }
    /**
     * @Description: 从GeoJSON文件中读取，转换为Polygon2D多边形
     * @param geoJson GeoJSON字符串
     * @return com.jeestudio.tools.geo.entity.Polygon2D
     **/
    public static List<Polygon2D> convertFeatureCollectionToPolygons(String geoJson) {
        List<Polygon2D> polygons = new ArrayList<>();
        JSONObject featureCollection = JSON.parseObject(geoJson);

        if (!featureCollection.containsKey("features") || !(featureCollection.get("features") instanceof JSONArray)) {
            System.err.println("Invalid GeoJSON FeatureCollection");
            throw new IllegalArgumentException("Invalid GeoJSON FeatureCollection");
        }

        JSONArray features = featureCollection.getJSONArray("features");
        for (Object featureElement : features) {
            JSONObject feature = (JSONObject) featureElement;
            if (!feature.containsKey("geometry") || !(feature.get("geometry") instanceof JSONObject)) {
                continue;
            }

            JSONObject geometry = feature.getJSONObject("geometry");
            String type = geometry.getString("type");

            if ("Polygon".equals(type)) {
                polygons.add(parsePolygon(geometry));
            } else if ("MultiPolygon".equals(type)) {
                polygons.addAll(parseMultiPolygon(geometry));
            }
        }
        return polygons;
    }
    /**
     * @Description: 将某json转换为Polygon2D多边形
     * @param geometry 边界数据
     * @return com.jeestudio.tools.geo.entity.Polygon2D
     **/
    private static Polygon2D parsePolygon(JSONObject geometry) {
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        JSONArray exteriorRing = coordinates.getJSONArray(0);
        return createPolygonFromRing(exteriorRing);
    }

    /**
     * @Description: 将某json转换为Polygon2D多边形
     * @param geometry 边界数据
     * @return java.util.List<com.jeestudio.tools.geo.entity.Polygon2D>
     **/
    private static List<Polygon2D> parseMultiPolygon(JSONObject geometry) {
        List<Polygon2D> polygons = new ArrayList<>();
        JSONArray coordinates = geometry.getJSONArray("coordinates");

        for (Object polygonElement : coordinates) {
            JSONArray polygonCoords = (JSONArray) polygonElement;
            JSONArray exteriorRing = polygonCoords.getJSONArray(0);
            polygons.add(createPolygonFromRing(exteriorRing));
        }
        return polygons;
    }

    /**
     * @Description: 将某json转换为Polygon2D多边形
     * @param ring 边界数据
     * @return com.jeestudio.tools.geo.entity.Polygon2D
     **/
    private static Polygon2D createPolygonFromRing(JSONArray ring) {
        Polygon2D polygon = new Polygon2D();
        for (Object pointElement : ring) {
            JSONArray point = (JSONArray) pointElement;
            double x = point.getDoubleValue(0);
            double y = point.getDoubleValue(1);
            polygon.addPoint2D(x, y);
        }
        return polygon;
    }

    public static void main(String[] args) throws IOException {
        FeatureCollection featureCollection = loadFromGeoJsonFeatureCollection("D:\\temp\\beijing.json");
        System.out.println(JSON.toJSONString(featureCollection));
        Polygon2D polygon = loadFromGeoJsonFeaturePolygon("D:\\temp\\beijing.json");
        Rectangle bounds = polygon.getBounds();
        System.out.println(JSON.toJSONString(bounds));
    }
}
