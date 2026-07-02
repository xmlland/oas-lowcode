package com.jeestudio.bpm.common.entity.cms;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 内容模板
 */
public class PrtModel extends DataEntity<PrtModel> {

    private static final long serialVersionUID = 1L;
    /** 归属机构编码 */
    private String ownerCode;
    /** 模板名称 */
    private String name;
    /** 模板类型 */
    private String types;
    /** 模板路径 */
    private String modelPath;
    /** 图片路径 */
    private String picPath;
    /** 默认 */
    private String ifDefault;
    /** 状态 */
    private String useable;
    /** 标签 */
    private String label;
    /** 站点名称 */
    private PrtSite site;

    public PrtModel() {
        super();
    }

    public PrtModel(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getIfDefault() {
        return ifDefault;
    }

    public void setIfDefault(String ifDefault) {
        this.ifDefault = ifDefault;
    }

    public String getUseable() {
        return useable;
    }

    public void setUseable(String useable) {
        this.useable = useable;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PrtSite getSite() {
        return site;
    }

    public void setSite(PrtSite site) {
        this.site = site;
    }
}
