package com.jeestudio.bpm.controller.base;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.excel.ExcelField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: 控制器基类，封装登录上下文与统一响应处理
 */
public abstract class BaseController {

    protected ThreadLocal<String> token = new ThreadLocal<>();
    protected ThreadLocal<String> currentUserName = new ThreadLocal<>();
    protected ThreadLocal<String> currentUserId = new ThreadLocal<>();
    protected ThreadLocal<String> ip = new ThreadLocal<>();
    protected String currentUserNameGuest = "guest";

    @Autowired
    ZformService zformService;

    @ModelAttribute
    public void init(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if(httpServletRequest.getHeader("token") != null){
            ThreadLocal<String> msg = new ThreadLocal<>();
            msg.set(JwtUtil.getCurrentUser(httpServletRequest.getHeader("token")));
            token.set(httpServletResponse.getHeader("token"));
            String userInfo = msg.get();
            currentUserName.set(userInfo.substring(0, userInfo.lastIndexOf("_")));
            currentUserId.set(userInfo.substring(userInfo.lastIndexOf("_") + 1));
            ip.set(oConvertUtils.getIpAddrByRequest(httpServletRequest));
            UserUtil.setCurrentLoginName(currentUserName.get());
        } else {
            currentUserName.set(currentUserNameGuest);
            ip.set(oConvertUtils.getIpAddrByRequest(httpServletRequest));
        }
    }

    @Deprecated
    public ResultJson failed() {
        return failed("操作失败");
    }

    @Deprecated
    public ResultJson failed(String msg) {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_FAILED);
        if (StringUtil.isNotEmpty(msg)) {
            // 如果消息包含异常类名，说明是系统异常，不暴露给前端
            if (msg.contains("Exception") || msg.contains("Error") || msg.contains("java.sql")) {
                msg = "操作失败，请联系管理员";
            }
        }
        resultJson.setMsg(msg);
        resultJson.setMsg_en("Operation has failed");
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 类型安全的异常处理方法
     * @param throwable 异常对象
     * @return 统一返回结果
     */
    public ResultJson failed(Throwable throwable) {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_FAILED);
        if (throwable instanceof BusinessException) {
            resultJson.setMsg(throwable.getMessage());
        } else if (throwable instanceof SQLException) {
            resultJson.setMsg("操作失败，数据库异常");
        } else {
            resultJson.setMsg("操作失败，请联系管理员");
        }
        resultJson.setMsg_en("Operation has failed");
        resultJson.setToken(token.get());
        return resultJson;
    }

    @Deprecated
    public ResultJson failedView() {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_FAILED);
        resultJson.setMsg("操作失败");
        resultJson.setMsg_en("Operation has failed");
        return resultJson;
    }

    @Deprecated
    public ResultJson success() {
        return success("操作成功");
    }

    @Deprecated
    public ResultJson success(IPage iPage) {
        ResultJson resultJson = success("查询分页数据成功");
        resultJson.setRows(iPage.getRecords());
        resultJson.setTotal(iPage.getTotal());
        return resultJson;
    }

    @Deprecated
    public ResultJson success(String msg) {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg(msg);
        resultJson.setMsg_en("Operation success");
        resultJson.setToken(token.get());
        return resultJson;
    }

    public ResultJson exportExcel(String sheetName, JSONObject requestMap, List<Map<String, Object>> listResult) throws Exception {
        return zformService.exportExcel(sheetName, requestMap, listResult);
    }

    public ResultJson exportExcel(String excelName, List<ExcelField> excelExportFieldList, List<Map<String, Object>> listResult) throws Exception {
        return zformService.exportExcel(excelName,excelExportFieldList,listResult);
    }

    public ResultJson exportExcel(String excelName, List<ExcelField> excelExportFieldList, List<Map<String, Object>> listResult,boolean haveSuffix) throws Exception {
        return zformService.exportExcel(excelName,excelExportFieldList,listResult,haveSuffix);
    }


    static final Hashtable<String, Lock> lockMap = new Hashtable<>();

    /**
     * 获取锁
     * @param lockKey
     * @return
     */
    public Lock getLock(String lockKey){
        synchronized (lockMap) {
            if (!lockMap.containsKey(lockKey)) {
                lockMap.put(lockKey, new ReentrantLock());
            }
        }
        return lockMap.get(lockKey);
    }
}
