package com.jeestudio.bpm.utils;

import com.jeestudio.bpm.common.view.system.OfficeView;
import com.jeestudio.bpm.common.entity.system.Office;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @Description: 机构视图工具
 */
public class OfficeUtil {
    public static void OfficeViewCopy(List<Office> sourceList, List<OfficeView> targetList) {
        for (Office object: sourceList) {
            OfficeView targetObject = new OfficeView();
            BeanUtils.copyProperties(object, targetObject);
            targetList.add(targetObject);
        }
    }
}
