package com.jeestudio.eco.water.entity;

import java.util.List;
/**
 * @Description: 污染物标准数据实体
 */

public class StandardWryEntity {
    private String wrwmc;

    private String wrwbm;

    private List<StandardWryxzEntity> wrwxz;

    public void setWrwmc(String wrwmc){
        this.wrwmc = wrwmc;
    }
    public String getWrwmc(){
        return this.wrwmc;
    }
    public void setWrwbm(String wrwbm){
        this.wrwbm = wrwbm;
    }
    public String getWrwbm(){
        return this.wrwbm;
    }
    public void setWrwxz(List<StandardWryxzEntity> wrwxz){
        this.wrwxz = wrwxz;
    }
    public List<StandardWryxzEntity> getWrwxz(){
        return this.wrwxz;
    }
}
