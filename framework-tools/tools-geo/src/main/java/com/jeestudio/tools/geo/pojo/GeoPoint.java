package com.jeestudio.tools.geo.pojo;

import java.io.Serializable;

/**
 * @Description: 地理坐标点
 */
public class GeoPoint implements Serializable {
    private static final long serialVersionUID = 6818990329439958956L;

    private double longitude;
    private double latitude;

    public GeoPoint() {
    }

    public GeoPoint(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
