package com.jeestudio.tools.geo.utils;

import cn.hutool.core.date.DatePattern;
import com.jeestudio.tools.geo.entity.Polygon2D;
import com.jeestudio.tools.geo.kriging.Kriging;
import com.jeestudio.tools.geo.pojo.ColorConfig;
import com.jeestudio.tools.geo.pojo.PointData;
import java.awt.geom.Rectangle2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.jeestudio.tools.geo.pojo.ColorConfig.DEFAULT_OPACITY;

/**
 * @Description: GIS PNG生成工具
 */
public class GisPngUtil {

    // 默认生成图片宽度
    private static final int DEFAULT_PIC_WIDTH = 1000;
    // 默认生成图片高度
    private static final int DEFAULT_PIC_HEIGHT = 1000;

    /**
     * @Description: 根据数据生成 AQI的 png差值图片
     * @param dataList 数据列表
     * @param geoJsonFile 区域GeoJson文件
     * @return java.awt.image.BufferedImage
     **/
    public static BufferedImage generateAirAQIPng(List<PointData> dataList, File geoJsonFile) {
        ColorConfig colorConfig = ColorConfig.getAirAqiColorConfig();
        Polygon2D polygon;
        try {
            polygon = GeoJsonPolygonLoader.loadFromGeoJsonFeaturePolygon(geoJsonFile);
        } catch (IOException e) {
            throw new RuntimeException("读取geoJson文件失败,"+geoJsonFile.toPath());
        }
        return generatePng(dataList, polygon, colorConfig, DEFAULT_PIC_WIDTH, DEFAULT_PIC_HEIGHT);
    }
    /**
     * @Description: 根据数据生成 AQI的 png差值图片
     * @param dataList 数据列表
     * @param geoJsonPath 区域GeoJson路径
     * @return java.awt.image.BufferedImage
     **/
    public static BufferedImage generateAirAQIPng(List<PointData> dataList, String geoJsonPath) {
        ColorConfig colorConfig = ColorConfig.getAirAqiColorConfig();
        return generatePng(dataList, geoJsonPath, colorConfig, DEFAULT_PIC_WIDTH, DEFAULT_PIC_HEIGHT);
    }
    /**
     * @Description: 根据数据生成 AQI的 png差值图片
     * @param dataList 数据列表
     * @param geoJsonPath 区域GeoJson路径
     * @param colorConfig 等级颜色配置
     * @return java.awt.image.BufferedImage
     **/
    public static BufferedImage generatePng(List<PointData> dataList, String geoJsonPath, ColorConfig colorConfig) {
        return generatePng(dataList, geoJsonPath, colorConfig, DEFAULT_PIC_WIDTH, DEFAULT_PIC_HEIGHT);
    }
    /**
     * @Description: 根据数据生成 AQI的 png差值图片
     * @param dataList 数据列表
     * @param geoJsonFile 区域GeoJson文件
     * @param colorConfig 等级颜色配置
     * @return java.awt.image.BufferedImage
     **/
    public static BufferedImage generatePng(List<PointData> dataList, File geoJsonFile, ColorConfig colorConfig) {
        Polygon2D polygon;
        try {
            polygon = GeoJsonPolygonLoader.loadFromGeoJsonFeaturePolygon(geoJsonFile);
        } catch (IOException e) {
            throw new RuntimeException("读取geoJson文件失败,"+geoJsonFile.toPath());
        }
        return generatePng(dataList, polygon, colorConfig, DEFAULT_PIC_WIDTH, DEFAULT_PIC_HEIGHT);
    }
    /**
     * @Description: 根据数据生成png差值图片
     * @param dataList 数据列表
     * @param geoJsonPath 区域GeoJson路径
     * @param colorConfig 等级颜色配置
     * @param picWidth 照片宽度
     * @param picHeight 照片高度
     * @return java.awt.image.BufferedImage
     **/
    public static BufferedImage generatePng(List<PointData> dataList, String geoJsonPath, ColorConfig colorConfig,int picWidth,int picHeight) {
        Polygon2D polygon;
        try {
            polygon = GeoJsonPolygonLoader.loadFromGeoJsonFeaturePolygon(geoJsonPath);
        } catch (IOException e) {
            throw new RuntimeException("读取geoJson文件失败,"+geoJsonPath);
        }
        return generatePng(dataList, polygon, colorConfig, picWidth, picHeight);
    }
    /**
     * @Description: 根据数据生成png差值图片
     * @param dataList 数据列表
     * @param polygon 区域GeoJson
     * @param colorConfig 等级颜色配置
     * @return java.awt.image.BufferedImage
     **/
    public static BufferedImage generatePng(List<PointData> dataList, Polygon2D polygon, ColorConfig colorConfig) {
        return generatePng(dataList, polygon, colorConfig, DEFAULT_PIC_WIDTH, DEFAULT_PIC_HEIGHT);
    }
    /**
     * @Description: 根据数据生成png差值图片,等级差使用AQI空气等级颜色
     * @param dataList 数据列表
     * @param polygon 区域GeoJson
     * @return java.awt.image.BufferedImage
     **/
    public static BufferedImage generateAutoLevelPng(List<PointData> dataList, Polygon2D polygon) {
        return generateAutoLevelPng(dataList, polygon, DEFAULT_PIC_WIDTH, DEFAULT_PIC_HEIGHT);
    }
    /**
     * @Description: 根据数据生成png差值图片,等级差使用AQI空气等级颜色
     * @param dataList 数据列表
     * @param polygon 区域GeoJson
     * @param picWidth 照片宽度
     * @param picHeight 照片高度
     * @return java.awt.image.BufferedImage
     **/
    public static BufferedImage generateAutoLevelPng(List<PointData> dataList, Polygon2D polygon, int picWidth,int picHeight) {
        ColorConfig colorConfig = autoLevel(dataList);
        return generatePng(dataList, polygon, colorConfig, picWidth, picHeight);
    }
    /**
     * @Description: 根据数据生成png差值图片,等级差使用AQI空气等级颜色
     * @param dataList 数据列表
     * @param polygon 区域GeoJson
     * @param colorConfig 等级颜色配置
     * @param picWidth 照片宽度
     * @param picHeight 照片高度
     * @return java.awt.image.BufferedImage
     **/
    public static BufferedImage generatePng(List<PointData> dataList, Polygon2D polygon, ColorConfig colorConfig,int picWidth,int picHeight) {

        if (colorConfig == null || colorConfig.getColorArray() == null || colorConfig.getColorArray().isEmpty()) {
            throw new RuntimeException("颜色配置不能为空");
        }
        long start1 = System.currentTimeMillis();
        // 线程数量，可以根据CPU核心数调整
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // 将数据值，根据颜色等级，转换为对应等级值，方便后续转换为颜色
//        for (PointData pointData : dataList) {
//            pointData.setValueLevel(colorConfig.getLevel(pointData.getValue()));
//        }

        // 栅格点数据
        double[][] krigingGridData;
        long start = System.currentTimeMillis();
        Rectangle2D.Double boundingBox = polygon.getBoundsBox();
        try {
            System.out.println("GisPngUtil.generatePng：开始生成栅格数据集");
            krigingGridData = dataToKrigingGrid(boundingBox, dataList, picWidth, picHeight);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("GisPngUtil.generatePng：生成栅格数据集失败，原因：" + e.getMessage());
            throw new RuntimeException("生成栅格数据集失败，原因："+e.getMessage());
        } finally {
            System.out.println("GisPngUtil.generatePng：生成栅格数据集耗时：" + (System.currentTimeMillis() - start) + " 毫秒");
        }
        // 获取图像范围
        BufferedImage bufferedImage = new BufferedImage( picWidth, picHeight, BufferedImage.TYPE_INT_ARGB);

        //对栅格数据开始填色  //计时开始
        System.out.println("GisPngUtil.generatePng：开始填充颜色");
        List<Future<?>> futures = new ArrayList<>();
        //遍历所有像素,宽度遍历，即按照列遍历
        for (int i = 0; i < picWidth; i++) {
//            System.out.println("GisPngUtil.generatePng：开始填充颜色-第"+i+"行");
            double[] samples = krigingGridData[i];
            int finalI = i;
            futures.add( executorService.submit(() -> {
                int k = 0;
                for (double sample : samples) {
                    int finalK = k;
                        try {
                            // 将图片坐标转换为地理坐标
                            double lon = boundingBox.getMinX() + (boundingBox.getMaxX() - boundingBox.getMinX()) * finalK / picWidth;
                            double lat = boundingBox.getMinY() + (boundingBox.getMaxY() - boundingBox.getMinY()) * (picHeight - finalI) / picHeight;

                            // 在区域范围内则绘制数据
                            if (polygon.contains(lon, lat)) {

    //                        DirectPosition worldPos = geometry.gridToWorld(new GridCoordinates2D(finalI, finalK));
    //                        boolean isInArea = isGeoArea(polygon, new GeoPoint(worldPos.getCoordinate()[0], worldPos.getCoordinate()[1]));
    //                        if (isInArea) {
//                                bufferedImage.setRGB(finalK, finalI, colorConfig.getColor(sample));
                                bufferedImage.setRGB(finalK, finalI, colorConfig.getColor(colorConfig.getLevel(sample)));
    //                        }
                            }
                        } catch (Exception e) {
                            System.out.println("GisPngUtil.generatePng：填充颜色失败"+e.getMessage());
                        }
                    k++;
                }
            }));
        }
        try {
            for (Future<?> future : futures) {
                future.get();
            }
            executorService.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        //计时结束
        System.out.println("GisPngUtil.generatePng：生成热力图结束，耗时：" + (System.currentTimeMillis() - start1)+" 毫秒");
        return bufferedImage;
    }

    /**
     * @Description: 将图片数据 保存为 png图片
     * @param bufferedImage 图片数据
     * @param filePath 保存路径
     * @param fileName 保存文件名
     **/
    public static void savePng(BufferedImage bufferedImage, String filePath, String fileName) {
        try {
            ImageIO.write(bufferedImage, "png", new File(filePath+fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @Description: 将图片数据 转换为base64图片数据
     * @param bufferedImage 图片数据
     * @return java.lang.String
     **/
    public static String getBase64String(BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Description: 根据数据生成等级分类，默认使用 AQI颜色等级
     * @param dataList 数据列表
     * @return java.awt.Color
     **/
    public static ColorConfig autoLevel(List<PointData> dataList) {
        return ColorConfig.autoLevel(dataList);
    }
    /**
     * @Description: 根据数据生成等级分类，使用颜色列表区分等级
     * @param dataList 数据列表
     * @param colorList 颜色列表
     * @return java.awt.Color
     **/
    public static ColorConfig autoColorLevel(List<PointData> dataList,List<String> colorList) {
        return ColorConfig.autoColorLevel(dataList,colorList,DEFAULT_OPACITY);
    }
    /**
     * 根据数据生成等级分类，使用颜色列表区分等级
     * @param dataList 数据列表
     * @param colorList 颜色列表
     * @param opacity 透明度
     * @return java.awt.Color
     */
    public static ColorConfig autoColorLevel(List<PointData> dataList,List<String> colorList,int opacity) {
        return ColorConfig.autoColorLevel(dataList,colorList,opacity);
    }


    /**
     * @Description: 根据地图四至范围，点位数据，使用克里金插值方法进行插值，对地图生成插值数据
     * @param boundingBox 绘制地图，四至范围 {最小经度,最小纬度,最大经度,最大纬度}
     * @param pointDataList 绘制插值点数据
     * @param width 图片宽度
     * @param height 图片高度
     * @return org.geotools.coverage.grid.GridCoverage2D
     **/
    public static double[][] dataToKrigingGrid(Rectangle2D boundingBox, List<PointData> pointDataList,int width,int height) throws Exception {
        //构造克里金插值需要的数据
        double[] rainValues = new double[pointDataList.size()];
        double[] rainXs = new double[pointDataList.size()];
        double[] rainYs = new double[pointDataList.size()];
        for (int i = 0; i < pointDataList.size(); i++) {
            PointData pointData = pointDataList.get(i);
            double longitude = pointData.getLongitude();
            double latitude = pointData.getLatitude();
            // 使用 等级值进行插值
//            double value = pointData.getValueLevel();
            double value = pointData.getValue();
            Point point = latLonToPixel(latitude, longitude, boundingBox, width, height);
            //构造克里金插值需要的数据
            rainValues[i] = value;
            rainXs[i] = point.getX();
            rainYs[i] = point.getY();
        }
        //克里金插值-
        Kriging kriging = new Kriging(Kriging.SPHERICAL_MODEL, 0.5, 1000);
        if (rainValues.length > 0) {
            kriging.train(rainValues, rainXs, rainYs);
        }
        //使用克里金插值 填充画布中的像素值
        double[][] dataRaster = new double[height][width];
        // 线程数量，可以根据CPU核心数调整
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<?>> futureList = new ArrayList<>();
        for (int j = 0; j < height; j++) {
            int finalJ = j;
            futureList.add( executorService.submit(() -> {
                for (int i = 0; i < width; i++) {
                    double value = kriging.predict(i, finalJ);
                    dataRaster[finalJ][i] = value;
                }
            }) );
        }
        for (Future<?> future : futureList) {
            future.get();
        }
        executorService.shutdown();

        return dataRaster;
    }

    /**
     * @Description: 将经纬度点转换为图像像素坐标
     * @param lat 纬度
     * @param lon 经度
     * @param bbox 四至范围
     * @param imageWidth 图片宽度
     * @param imageHeight 图片高度
     * @return java.awt.Point
     **/
    public static Point latLonToPixel(double lat, double lon, Rectangle2D bbox, int imageWidth, int imageHeight) {
        double xRatio = (lon - bbox.getMinX()) / bbox.getWidth();
        double yRatio = (bbox.getMaxY() - lat) / bbox.getHeight(); // 注意Y轴是倒转的

        int pixelX = (int) (xRatio * imageWidth);
        int pixelY = (int) (yRatio * imageHeight);
        return new Point(pixelX, pixelY);
    }
    public static void main(String[] args) throws Exception {
//        test1();
        test2();
//        test3();
    }
    private static void test1() throws Exception {

        String json150000 = "D:\\temp\\beijing.json";
        Polygon2D polygon = GeoJsonPolygonLoader.loadFromGeoJsonFeaturePolygon(json150000);
        List<PointData> pointDataList = new ArrayList<>();
        pointDataList.add(new PointData(116.554971,40.036805,17.8));
        pointDataList.add(new PointData(116.601149,39.867385,8.58));
        pointDataList.add(new PointData(116.139455,39.860407,9.96));
        pointDataList.add(new PointData(116.171410,39.786625,16.9));
        pointDataList.add(new PointData(116.172214,40.139873,18.6));
        pointDataList.add(new PointData(116.177304,40.034556,17));
        pointDataList.add(new PointData(115.495303,40.026858,43.2));
        pointDataList.add(new PointData(115.832533,40.021018,27.2));
        pointDataList.add(new PointData(115.841148,39.776110,45.2));
        pointDataList.add(new PointData(115.921296,39.664695,15.8));
        pointDataList.add(new PointData(116.143212,39.629487,13.4));
        pointDataList.add(new PointData(116.820533,39.751642,19.9));
        pointDataList.add(new PointData(116.680198,39.705249,19.2));
        pointDataList.add(new PointData(116.755004,39.639947,13.3));
        pointDataList.add(new PointData(116.845371,40.245881,19.2));
        pointDataList.add(new PointData(116.605531,40.242008,14.5));
        pointDataList.add(new PointData(116.493205,40.191992,19));
        pointDataList.add(new PointData(116.773927,40.061091,18.4));
        pointDataList.add(new PointData(116.693012,40.146721,16.4));
        pointDataList.add(new PointData(116.291336,40.244135,29.3));
        pointDataList.add(new PointData(116.569729,39.703912,19.3));
        pointDataList.add(new PointData(116.398062,39.687164,4.62));
        pointDataList.add(new PointData(116.427869,39.639233,15.9));
        pointDataList.add(new PointData(116.302284,39.594798,10.3));
        pointDataList.add(new PointData(116.299612,39.525456,5.6));
        pointDataList.add(new PointData(116.728528,40.315385,8.58));
        pointDataList.add(new PointData(116.975054,40.125038,16.5));
        pointDataList.add(new PointData(117.045403,40.559216,25.6));
        pointDataList.add(new PointData(116.756783,40.423706,6.51));
        pointDataList.add(new PointData(117.099811,40.402054,35.9));
        pointDataList.add(new PointData(116.468129,40.676140,63.3));
        pointDataList.add(new PointData(116.273071,40.566217,13.9));
        pointDataList.add(new PointData(116.184863,40.551562,9.17));
        pointDataList.add(new PointData(116.124484,40.505459,15.2));
        pointDataList.add(new PointData(116.018152,40.464982,16.4));
        pointDataList.add(new PointData(116.033518,40.346471,30.3));
        pointDataList.add(new PointData(117.011812,40.204255,15.3));
        pointDataList.add(new PointData(117.013851,40.200751,13.7));
        pointDataList.add(new PointData(117.015127,40.196181,12.9));
        pointDataList.add(new PointData(117.01062,40.19508,14.8));
        pointDataList.add(new PointData(117.010794,40.19277,16.5));
        long start = System.currentTimeMillis();


        List<String> colorList = new ArrayList<String>(){{
            add("#9ED27C");
            add("#AEDE7C");
            add("#C3E978");
            add("#E1F385");
            add("#FDFE7C");
            add("#FFDE80");
            add("#FDBF80");
            add("#FE9F7C");
            add("#FF7B7B");
        }};
        ColorConfig colorConfig = autoColorLevel(pointDataList,colorList);

        BufferedImage bufferedImage = generatePng(pointDataList, polygon, colorConfig);
//        BufferedImage bufferedImage = generateAutoLevelPng(pointDataList, polygon, 1000,1000);
        savePng(bufferedImage,"D:/temp/",  DatePattern.PURE_DATETIME_FORMAT.format(new Date()) + "_test1__"+(System.currentTimeMillis() - start)+".png");

    }
    public static void test2() throws Exception {
        String json150000 = "D:\\temp\\beijing.json";
        Polygon2D polygon = GeoJsonPolygonLoader.loadFromGeoJsonFeaturePolygon(json150000);
        List<PointData> pointDataList = new ArrayList<>();
        pointDataList.add(new PointData(115.938988,39.766454,0.08));
        pointDataList.add(new PointData(115.921538,39.719876,0.20));
        pointDataList.add(new PointData(115.974978,39.744140,0.18));
        pointDataList.add(new PointData(115.965177,39.717395,0.05));
        pointDataList.add(new PointData(115.992088,39.721882,0.09));
        pointDataList.add(new PointData(115.993767,39.718249,0.09));
        pointDataList.add(new PointData(115.945018,39.742584,0.10));
        pointDataList.add(new PointData(115.943862,39.737923,0.21));
        pointDataList.add(new PointData(115.951143,39.737425,0.13));
        pointDataList.add(new PointData(115.946972,39.733621,0.18));
        pointDataList.add(new PointData(115.947773,39.735420,0.10));
        pointDataList.add(new PointData(115.948658,39.743629,0.05));
        pointDataList.add(new PointData(115.950103,39.731881,0.12));
        pointDataList.add(new PointData(115.962608,39.654219,0.11));
        pointDataList.add(new PointData(115.959313,39.650821,0.18));
        pointDataList.add(new PointData(115.962238,39.64705,0.20));
        pointDataList.add(new PointData(115.966082,39.6516,0.06));
        pointDataList.add(new PointData(115.96359,39.650778,0.05));
        pointDataList.add(new PointData(115.9654,39.649447,0.12));
        pointDataList.add(new PointData(115.9654,39.649447,0.11));
        pointDataList.add(new PointData(115.915480,39.729509,0.20));
        pointDataList.add(new PointData(115.925623,39.723490,0.11));
        pointDataList.add(new PointData(115.951143,39.737425,0.20));
        pointDataList.add(new PointData(115.964812,39.738853,0.12));
        pointDataList.add(new PointData(116.082353,39.647797,0.10));
        pointDataList.add(new PointData(116.090625,39.645043,0.10));
        pointDataList.add(new PointData(116.094147,39.645698,0.08));
        pointDataList.add(new PointData(116.096263,39.644545,0.11));
        pointDataList.add(new PointData(116.091737,39.652281,0.09));
        pointDataList.add(new PointData(116.101985,39.641647,0.13));
        pointDataList.add(new PointData(115.979077,39.738999,0.08));
        pointDataList.add(new PointData(115.985510,39.733709,0.09));
        pointDataList.add(new PointData(115.987462,39.731495,0.06));
        pointDataList.add(new PointData(115.994603,39.728937,0.10));
        pointDataList.add(new PointData(115.997157,39.688854,0.20));
        pointDataList.add(new PointData(116.000682,39.686437,0.16));
        pointDataList.add(new PointData(116.000657,39.683897,0.12));
        pointDataList.add(new PointData(115.997960,39.677279,0.15));
        pointDataList.add(new PointData(115.972282,39.743600,4.90));
        pointDataList.add(new PointData(115.981403,39.746008,0.15));
        pointDataList.add(new PointData(115.983013,39.740099,0.10));
        pointDataList.add(new PointData(115.986072,39.741458,0.10));
        pointDataList.add(new PointData(115.989307,39.740400,0.08));
        pointDataList.add(new PointData(116.121098,39.677116,0.15));
        pointDataList.add(new PointData(116.121098,39.677116,0.15));
        pointDataList.add(new PointData(116.128687,39.672105,0.20));
        pointDataList.add(new PointData(116.128687,39.672105,0.20));
        pointDataList.add(new PointData(116.126623,39.670918,0.15));
        pointDataList.add(new PointData(116.126623,39.670918,0.15));
        pointDataList.add(new PointData(116.105963,39.673631,0.12));
        pointDataList.add(new PointData(116.105963,39.673631,0.12));
        pointDataList.add(new PointData(115.979077,39.738999,0.06));
        pointDataList.add(new PointData(115.985510,39.733709,0.11));
        pointDataList.add(new PointData(115.977617,39.741437,0.10));
        pointDataList.add(new PointData(116.552473,40.090198,0.10));
        pointDataList.add(new PointData(116.551120,40.085940,0.12));
        pointDataList.add(new PointData(116.551120,40.085940,0.10));
        pointDataList.add(new PointData(116.552473,40.090198,0.12));
        pointDataList.add(new PointData(116.552528,40.092694,0.08));
        pointDataList.add(new PointData(116.264408,39.754477,0.11));
        pointDataList.add(new PointData(116.268340,39.748994,0.10));
        pointDataList.add(new PointData(116.270653,39.755508,0.04));
        pointDataList.add(new PointData(116.272145,39.754757,0.13));
        pointDataList.add(new PointData(116.273465,39.749215,0.08));
        pointDataList.add(new PointData(116.265978,39.761186,0.06));
        pointDataList.add(new PointData(116.329975,40.057928,0.12));
        pointDataList.add(new PointData(116.329975,40.057928,0.10));
        long start = System.currentTimeMillis();


        List<String> colorList = new ArrayList<String>(){{
            add("#9ED27C");
            add("#AEDE7C");
            add("#C3E978");
            add("#E1F385");
            add("#FDFE7C");
            add("#FFDE80");
            add("#FDBF80");
            add("#FE9F7C");
            add("#FF7B7B");
        }};
        ColorConfig colorConfig = autoColorLevel(pointDataList,colorList);

        BufferedImage bufferedImage = generatePng(pointDataList, polygon, colorConfig);
//        BufferedImage bufferedImage = generateAutoLevelPng(pointDataList, polygon, 1000,1000);
        savePng(bufferedImage,"D:/temp/",  DatePattern.PURE_DATETIME_FORMAT.format(new Date()) + "_test2__"+(System.currentTimeMillis() - start)+".png");

    }
    public static void test3() throws Exception {
        String json150000 = "D:\\temp\\beijing.json";
        Polygon2D polygon = GeoJsonPolygonLoader.loadFromGeoJsonFeaturePolygon(json150000);
        List<PointData> pointDataList = new ArrayList<>();
        pointDataList.add(new PointData(115.938988,39.766454,17.2));
        pointDataList.add(new PointData(115.921538,39.719876,75.9));
        pointDataList.add(new PointData(115.974978,39.744140,53.8));
        pointDataList.add(new PointData(115.965177,39.717395,20.1));
        pointDataList.add(new PointData(115.992088,39.721882,15.5));
        pointDataList.add(new PointData(115.993767,39.718249,4.54));
        pointDataList.add(new PointData(115.945018,39.742584,17.4));
        pointDataList.add(new PointData(115.943862,39.737923,65.4));
        pointDataList.add(new PointData(115.951143,39.737425,35.5));
        pointDataList.add(new PointData(115.946972,39.733621,41.3));
        pointDataList.add(new PointData(115.947773,39.735420,21.4));
        pointDataList.add(new PointData(115.948658,39.743629,8.71));
        pointDataList.add(new PointData(115.950103,39.731881,17.2));
        pointDataList.add(new PointData(115.962608,39.654219,12.8));
        pointDataList.add(new PointData(115.959313,39.650821,36.7));
        pointDataList.add(new PointData(115.962238,39.64705,34.6));
        pointDataList.add(new PointData(115.966082,39.6516,17.6));
        pointDataList.add(new PointData(115.96359,39.650778,7.60));
        pointDataList.add(new PointData(115.9654,39.649447,43.0));
        pointDataList.add(new PointData(115.9654,39.649447,35.6));
        pointDataList.add(new PointData(115.915480,39.729509,35.6));
        pointDataList.add(new PointData(115.925623,39.723490,29.0));
        pointDataList.add(new PointData(115.951143,39.737425,37.1));
        pointDataList.add(new PointData(115.964812,39.738853,16.4));
        pointDataList.add(new PointData(116.082353,39.647797,9.26));
        pointDataList.add(new PointData(116.090625,39.645043,9.54));
        pointDataList.add(new PointData(116.094147,39.645698,5.19));
        pointDataList.add(new PointData(116.096263,39.644545,12.6));
        pointDataList.add(new PointData(116.091737,39.652281,3.23));
        pointDataList.add(new PointData(116.101985,39.641647,26.8));
        pointDataList.add(new PointData(115.979077,39.738999,8.48));
        pointDataList.add(new PointData(115.985510,39.733709,10.2));
        pointDataList.add(new PointData(115.987462,39.731495,5.07));
        pointDataList.add(new PointData(115.994603,39.728937,18.2));
        pointDataList.add(new PointData(115.997157,39.688854,22.7));
        pointDataList.add(new PointData(116.000682,39.686437,29.4));
        pointDataList.add(new PointData(116.000657,39.683897,19.5));
        pointDataList.add(new PointData(115.997960,39.677279,31.4));
        pointDataList.add(new PointData(115.972282,39.743600,37.8));
        pointDataList.add(new PointData(115.981403,39.746008,39.0));
        pointDataList.add(new PointData(115.983013,39.740099,13.9));
        pointDataList.add(new PointData(115.986072,39.741458,16.1));
        pointDataList.add(new PointData(115.989307,39.740400,8.74));
        pointDataList.add(new PointData(116.121098,39.677116,23.0));
        pointDataList.add(new PointData(116.121098,39.677116,23.0));
        pointDataList.add(new PointData(116.128687,39.672105,23.1));
        pointDataList.add(new PointData(116.128687,39.672105,23.1));
        pointDataList.add(new PointData(116.126623,39.670918,28.6));
        pointDataList.add(new PointData(116.126623,39.670918,28.6));
        pointDataList.add(new PointData(116.105963,39.673631,21.4));
        pointDataList.add(new PointData(116.105963,39.673631,21.4));
        pointDataList.add(new PointData(115.979077,39.738999,8.70));
        pointDataList.add(new PointData(115.985510,39.733709,12.0));
        pointDataList.add(new PointData(115.977617,39.741437,19.9));
        pointDataList.add(new PointData(116.552473,40.090198,11.2));
        pointDataList.add(new PointData(116.551120,40.085940,25.7));
        pointDataList.add(new PointData(116.551120,40.085940,14.8));
        pointDataList.add(new PointData(116.552473,40.090198,21.9));
        pointDataList.add(new PointData(116.552528,40.092694,11.0));
        pointDataList.add(new PointData(116.264408,39.754477,11.2));
        pointDataList.add(new PointData(116.268340,39.748994,7.10));
        pointDataList.add(new PointData(116.270653,39.755508,5.08));
        pointDataList.add(new PointData(116.272145,39.754757,6.91));
        pointDataList.add(new PointData(116.273465,39.749215,16.4));
        pointDataList.add(new PointData(116.265978,39.761186,9.19));
        pointDataList.add(new PointData(116.329975,40.057928,13.4));
        pointDataList.add(new PointData(116.329975,40.057928,13.4));
        long start = System.currentTimeMillis();


        List<String> colorList = new ArrayList<String>(){{
            add("#9ED27C");
            add("#AEDE7C");
            add("#C3E978");
            add("#E1F385");
            add("#FDFE7C");
            add("#FFDE80");
            add("#FDBF80");
            add("#FE9F7C");
            add("#FF7B7B");
        }};
        ColorConfig colorConfig = autoColorLevel(pointDataList,colorList);

        BufferedImage bufferedImage = generatePng(pointDataList, polygon, colorConfig);
//        BufferedImage bufferedImage = generateAutoLevelPng(pointDataList, polygon, 1000,1000);
        savePng(bufferedImage,"D:/temp/",  DatePattern.PURE_DATETIME_FORMAT.format(new Date()) + "_test3__"+(System.currentTimeMillis() - start)+".png");

    }
}
