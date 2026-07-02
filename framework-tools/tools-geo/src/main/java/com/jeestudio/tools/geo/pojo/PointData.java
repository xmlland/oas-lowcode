package com.jeestudio.tools.geo.pojo;

/**
 * @Description: 点数据
 */
public class PointData extends GeoPoint {

    private static final long serialVersionUID = -8125397746896555718L;
    /**
     * 值
     */
    private double value;
    /**
     * 值等级
     **/
    private double valueLevel;

    public PointData(double longitude, double latitude, double value) {
        super(longitude, latitude);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValueLevel() {
        return valueLevel;
    }

    public void setValueLevel(double valueLevel) {
        this.valueLevel = valueLevel;
    }
}
