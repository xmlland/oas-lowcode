package com.jeestudio.bpm.aspect;

import com.jeestudio.bpm.common.param.GridselectParam;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Description: 列表选择数据切面
 */
@Aspect
@Component
public class GridSelectDataAspect {

    private static final Logger logger = LoggerFactory.getLogger(GridSelectDataAspect.class);

    @Pointcut("execution(public * com.jeestudio.bpm.controller.dynamic.ZformController.gridselectData(com.jeestudio.bpm.common.param.GridselectParam))")
    public void gridSelectData() {

    }

    @Around("gridSelectData()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Object[] args = pjp.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof GridselectParam) {
                GridselectParam gridselectParam = (GridselectParam) args[0];
                String tableName = gridselectParam.getTableName();
                if ("sys_level".equals(tableName)) {
                    gridselectParam.setDsfPlus(" AND a.useable = '1' ");
                } else if ("sys_post".equals(tableName)) {
                    gridselectParam.setDsfPlus(" AND a.useable = '1' ");
                }
            }
        } catch (Exception e) {
            logger.warn("GridSelectData aspect processing failed for request", e);
        } finally {
            Object proceed = pjp.proceed();
            return proceed;
        }
    }
}
