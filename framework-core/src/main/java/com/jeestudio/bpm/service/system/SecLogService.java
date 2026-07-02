package com.jeestudio.bpm.service.system;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.mapper.base.system.SecLogDao;
import com.jeestudio.bpm.modules.admin.entity.SysSecIdataEntity;
import com.jeestudio.bpm.modules.admin.service.SysSecIConfigService;
import com.jeestudio.bpm.modules.oa.service.OaSysAnnouncementServiceI;
import com.jeestudio.bpm.security.storage.IntegrityHandler;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.secLog.SecLogAppender;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @Description: 安全日志服务
 */
@Service
//@Async
public class SecLogService {

    protected static final Logger logger = LoggerFactory.getLogger(SecLogService.class);

    public static final String ACTION_QUERY = "查询";
    public static final String ACTION_SAVE = "保存";
    public static final String ACTION_DELETE = "删除";

    @Value("${sysSecLogSpace}")
    private int sysSecLogSpace;

    @Value("${sysSecLogSpaceWarningPercent}")
    private int sysSecLogSpaceWarningPercent;

    @Value(value = "${sec.switch}")
    private boolean secSwitch;

    @Autowired
    SecLogDao secLogDao;

    @Autowired
    GenTableService genTableService;

    @Autowired
    ZformService zformService;
    @Autowired
    ProjectProperties projectProperties;

    @Autowired
    IntegrityHandler integrityHandler;

    @Autowired
    @Lazy
    OaSysAnnouncementServiceI oaSysAnnouncementService;

    //向appender中分发日志
    public void sendLogToAppender(String id, String account, String content, Date time, String ip, String type, String result) {

        List<Class<? extends SecLogAppender>> logAppender = projectProperties.getLogAppender();
        if (logAppender != null && logAppender.size() > 0) {
            HttpServletRequest request = ContextHolderUtil.getHttpServletRequest();
            Map<String, String[]> requestParam = request.getParameterMap();
            long start = 0;
            String requestURI = "";
            Object requestTimestamp = request.getSession().getAttribute("requestTimestamp");
            if (requestTimestamp != null) {
                start = ConvertUtil.getLong(requestTimestamp);
            }
            requestURI = request.getRequestURI();
            int timeConsuming = (int) (System.currentTimeMillis() - time.getTime());
            String threadName = Thread.currentThread().getName();
            //开启新线程发送日志
            long finalStart = start;
            String finalRequestURI = requestURI;
            new Thread(() -> {
                for (Class<?> appender : logAppender) {

                    try {
                        Object o = appender.newInstance();
                        if (!(o instanceof SecLogAppender)) {
                            continue;
                        }
                        SecLogAppender logAppenderInstance = (SecLogAppender) o;
                        logAppenderInstance.accept(threadName, id, account, finalStart, time.getTime(), timeConsuming, finalRequestURI, content, ip, type, (Global.YES.equals(result) || "成功".equals(result)) ? SecLogAppender.LogResult.SUCCESS : SecLogAppender.LogResult.FAIL, requestParam);
                    } catch (Exception e) {
                        logger.error("Error occurred while trying to send log to appender: " + ExceptionUtils.getStackTrace(e));
                    }
                }
            }).start();
        }
    }

    public void saveSecLogOk(String account, String ip, String content, String type, String result) {
        this.saveLog(account, ip, content, type, result);
    }

    public void saveSecLogZformOk(String account, String ip, String result, String formNo, String action) {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        String content = action + genTable.getComments() + result;
        String type = action + genTable.getComments();
        this.saveLog(account, ip, content, type, result);
    }

    @Autowired
    SysSecIConfigService sysSecIConfigService;

    public void checkIntegrityProtection(){
        int rows = 1000;
        IPage<T> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1,rows);
        IPage<Map<String, Object>> page = secLogDao.selectSecLogIntegrity(iPage);
        long pages = page.getPages();
        AtomicInteger errorCount = new AtomicInteger();
        for (int i = 1; i <= pages; i++) {
            IPage<T> iPage1 = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(i,rows);
            IPage<Map<String, Object>> page1 = secLogDao.selectSecLogIntegrity(iPage1);
            List<Map<String, Object>> records = page1.getRecords();
            records.forEach(map -> {
                String id = ConvertUtil.getString(map.get("id"));
                Object account = map.get("account_");
                Object content = map.get("content_");
                Object time = map.get("time_");
                Date dateTime = null;
                if (time instanceof Timestamp){
                    dateTime = new Date(((Timestamp) time).getTime());
                }
                Object ip = map.get("ip_");
                Object type = map.get("type_");
                Object result = map.get("result_");
                String integrityValue = ConvertUtil.getString(map.get("integrity_value"));
                String str = "" + account + content + dateTime + ip + type + result;
                String encrypt = integrityHandler.encrypt(str);
                if (!encrypt.equals(integrityValue)){
                    SysSecIdataEntity sysSecIdataEntity = new SysSecIdataEntity();
                    sysSecIdataEntity.setId(IdUtil.nanoId());
                    sysSecIdataEntity.setCreateDate(DateUtil.date());
                    sysSecIdataEntity.setParentId("sys_sec_log");
                    sysSecIdataEntity.setEnv(sysSecIConfigService.getActive());
                    sysSecIdataEntity.setDataId(ConvertUtil.getString(map.get("id")));
                    sysSecIdataEntity.setOptTime(DateUtil.date());
                    sysSecIdataEntity.setRawData(str);
                    sysSecIdataEntity.setIntegrityValue(encrypt);
                    sysSecIdataEntity.setValidPass(Global.NO);
                    sysSecIdataEntity.setValidTime(DateUtil.date());
                    sysSecIConfigService.save(sysSecIdataEntity);
                    errorCount.getAndIncrement();
                }
            });
        }

        if (errorCount.get() > 0){
            oaSysAnnouncementService.sendToRole("admin","完整性验证失败-sys_sec_log" ,
                    "完整性验证失败-sys_sec_log，" + errorCount.get() + "条数据验证失败，请及时处理"
                    , false, "system");
        }



    }

    private void saveLog(String account, String ip, String content, String type, String result) {
        String id = IdGen.uuid();
        Date time = new Date();

        Supplier<Void> supplier = () -> {
            if (projectProperties.isIntegrityProtection()) {
                String str = account + content + time + ip + type + result;
                String encrypt = integrityHandler.encrypt(str);
                secLogDao.saveSecLogIntegrity(id, account, content, time, ip, type, result, encrypt);
            } else {
                secLogDao.saveSecLog(id, account, content, time, ip, type, result);
            }
            return null;
        };

        if (projectProperties.isSecLogAsyncSave()){
            new Thread(supplier::get).start();
        }else{
            supplier.get();
        }


        this.sendLogToAppender(id, account, content, time, ip, type, result);
    }

    public Map<String, String> getSecLogSpace() {
        Map<String, String> map = new HashMap<>();
        Double logSize = Double.parseDouble(secLogDao.getSecLogSpace());
        Double logSpacePercent = logSize / sysSecLogSpace * 100;
        if (logSpacePercent > sysSecLogSpaceWarningPercent) {
            map.put("color", "red");
        } else {
            map.put("color", "green");
        }
        map.put("result",
                Double.valueOf(String.format("%.2f", logSize)) + "M, "
                        + Double.valueOf(String.format("%.2f", logSpacePercent)) + "%");
        return map;
    }

    //
    //@Async
    public void saveSecLog(String account, String ip, String content, String type, String result) {
        if (secSwitch) {
            this.saveSecLogOk(account, ip, content, type, Global.YES.equals(result) ? "成功" : "失败");
        }
    }

    //@Async
    public void saveSecLogZform(String account, String ip, String result, String formNo, String action) {
        if (secSwitch) {
            this.saveSecLogZformOk(account, ip, Global.YES.equals(result) ? "成功" : "失败", formNo, action);
        }
    }

    public Boolean getSecSwitch() {
        return this.secSwitch;
    }

    public Page<Zform> data(Zform zform, String path, String formNo, String traceFlag, String loginName, String ip) throws Exception {
        try {
            zform.setFormNo(formNo);
            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            if (zform.getPageParam() == null) {
                zform.setPageParam(new PageParam());
            }
            if (secSwitch) {
                String dsf = null;
                if (loginName.startsWith("secadmin")) {
                    dsf = " AND (a.account_ LIKE 'auditadmin%' OR (a.account_ NOT LIKE 'auditadmin%' AND a.account_ NOT LIKE 'secadmin%' AND a.account_ NOT LIKE 'sysadmin%' AND a.account_ != 'admin')) ";
                } else if (loginName.startsWith("auditadmin")) {
                    dsf = " AND ((a.account_ LIKE 'sysadmin%' OR a.account_ LIKE 'secadmin%') AND a.account_ != 'admin') ";
                } else if ("admin".equals(loginName)) {
                    dsf = " AND 1 = 1 ";
                } else {
                    dsf = " AND 1 != 1 ";
                }
                // 安全加固：校验dsf，防止SQL注入
                dsf = SqlInjectionUtil.validateDsf(dsf);
                zform.getSqlMap().put("dsf", dsf);
            }
            Page<Zform> page = zformService.findPage(new Page<Zform>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy()),
                    zform,
                    path,
                    loginName,
                    genTable,
                    traceFlag,
                    "");

            this.saveSecLogZform(loginName, ip, Global.YES, formNo, ACTION_QUERY);
            return page;
        } catch (Exception e) {
            logger.error("Error occurred while trying to get secLog list: " + ExceptionUtils.getStackTrace(e));
            this.saveSecLogZform(loginName, ip, Global.NO, formNo, ACTION_QUERY);
            throw e;
        }
    }

    public Boolean passwordExpired(String loginName) {
        Boolean passwordExpired = false;
        if (this.secSwitch) {
            User currentUser = UserUtil.getByLoginName(loginName);
            if (currentUser.getPasswordExpiredDate() != null) {
                passwordExpired = currentUser.getPasswordExpiredDate().before(new Date());
            }
        }
        return passwordExpired;
    }
}
