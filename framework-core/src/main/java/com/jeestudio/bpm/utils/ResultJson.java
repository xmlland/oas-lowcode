package com.jeestudio.bpm.utils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 统一响应结果
 */
public class ResultJson {

    /** 通用失败响应码。 */
    public static final int CODE_FAILED = -1;
    /** 数据导入错误响应码。 */
    public static final int DATA_IMPORT_ERROR = -9005;
    /** 需要二次鉴权响应码。 */
    public static final int REQUIRE_AUTHENTICATION = -2000;
    /** 通用成功响应码。 */
    public static final int CODE_SUCCESS = 0;
    /** 登录令牌过期响应码。 */
    public static final int CODE_TOKEN_EXPIRED = 1001;

    /** 响应码：0 成功，-1 失败，1001 token 过期。 */
    private Integer code;

    /** 中文提示信息。 */
    private String msg;

    /** 英文提示信息。 */
    private String msg_en;

    /** 扩展响应数据。 */
    private LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

    /** 当前请求刷新后的 token。 */
    private String token;

    /** 分页列表数据。 */
    private Object rows;

    /** 分页总记录数。 */
    private Object total;

    /** 分页偏移量。 */
    private Object offset;

    /** 新增成功后返回的主键 ID。 */
    private String insertedId;


    /** 前端加密使用的公钥。 */
    private String  publicKey;

    /** 加密白名单字段列表。 */
    private List<String> encryptWhiteList;

    public ResultJson() {
    }

    public ResultJson(Integer code, String msg, LinkedHashMap<String, Object> data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultJson(Integer code, String msg, String msg_en, LinkedHashMap<String, Object> data) {
        this.code = code;
        this.msg = msg;
        this.msg_en = msg_en;
        this.data = data;
    }

    public ResultJson(Integer code, String msg, LinkedHashMap<String, Object> data, String token) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.token = token;
    }

    public static ResultJson success() {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg("操作成功");
        resultJson.setMsg_en("Operation success");
        resultJson.setToken(UserUtil.getCurrentToken());
        return resultJson;
    }

    public static ResultJson successView() {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg("操作成功");
        resultJson.setMsg_en("Operation success");
        return resultJson;
    }

    public static ResultJson success(String message) {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg(message);
        resultJson.setMsg_en("Operation success");
        resultJson.setToken(UserUtil.getCurrentToken());
        return resultJson;
    }

    public static ResultJson successView(String message) {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg(message);
        resultJson.setMsg_en("Operation success");
        return resultJson;
    }

    public static ResultJson failed() {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_FAILED);
        resultJson.setMsg("操作失败");
        resultJson.setMsg_en("Operation has failed");
        return resultJson;
    }

    public static ResultJson failed(String message) {
        if (StringUtil.isNotEmpty(message)
                && (message.contains("SQLException") || message.contains("java.sql.SQL"))) {
            // 数据库异常,不往外抛出具体信息
            message = "操作失败，数据库异常";
        } else if (StringUtil.isNotEmpty(message) && (message.contains("Exception") || message.contains("exceptions"))) {
            message = "操作失败";
        } else if (StringUtil.isEmpty(message)) {
            message = "操作失败";
        }
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_FAILED);
        resultJson.setMsg(message);
        resultJson.setMsg_en("Operation has failed");
        return resultJson;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public LinkedHashMap<String, Object> getData() {
        return data;
    }

    public ResultJson setData(LinkedHashMap<String, Object> data) {
        this.data = data;
        return this;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ResultJson put(String key, Object value){
        data.put(key, value);
        return this;
    }

    public void remove(String key){
        data.remove(key);
    }

    public String getMsg_en() {
        return msg_en;
    }

    public void setMsg_en(String msg_en) {
        this.msg_en = msg_en;
    }

    public Object getRows() {
        return rows;
    }

    public ResultJson setRows(Object rows) {
        this.rows = rows;
        return this;
    }

    public Object getTotal() {
        return total;
    }

    public ResultJson setTotal(Object total) {
        this.total = total;
        return this;
    }

    public Object getOffset() {
        return offset;
    }

    public void setOffset(Object offset) {
        this.offset = offset;
    }

    public String getInsertedId() {
        return insertedId;
    }

    public ResultJson setInsertedId(String insertedId) {
        this.insertedId = insertedId;
        return this;
    }


    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public List<String> getEncryptWhiteList() {
        return encryptWhiteList;
    }

    public void setEncryptWhiteList(List<String> encryptWhiteList) {
        this.encryptWhiteList = encryptWhiteList;
    }
}
