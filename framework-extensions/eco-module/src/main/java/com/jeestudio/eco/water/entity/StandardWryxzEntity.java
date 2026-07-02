package com.jeestudio.eco.water.entity;
/**
 * @Description: 污染物限值标准数据实体
 */

public class StandardWryxzEntity {
    private String symbol;

    private double upperLimit;

    private double lowerLimit;

    public void setSymbol(String symbol){
        this.symbol = symbol;
    }
    public String getSymbol(){
        return this.symbol;
    }
    public void setUpperLimit(double upperLimit){
        this.upperLimit = upperLimit;
    }
    public double getUpperLimit(){
        return this.upperLimit;
    }
    public void setLowerLimit(double lowerLimit){
        this.lowerLimit = lowerLimit;
    }
    public double getLowerLimit(){
        return this.lowerLimit;
    }
}
