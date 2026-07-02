package com.jeestudio.bpm.common.entity.cms;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 内容站点
 */
public class PrtSite extends DataEntity<PrtSite> {

    private static final long serialVersionUID = 1L;
    /** 归属机构编码 */
    private String ownerCode;
    /** 站点编号 */
    private String code;
    /** 站点名称 */
    private String name;
    /** 关键字 */
    private String keyword;
    /** 站点HTTP */
    private String httpPath;
    /** 资源位置 */
    private String resource;
    /** 排序 */
    private Integer sort;
    /** 状态 */
    private String status;

    public PrtSite() {
        super();
    }

    public PrtSite(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getHttpPath() {
        return httpPath;
    }

    public void setHttpPath(String httpPath) {
        this.httpPath = httpPath;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
