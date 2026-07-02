package com.jeestudio.bpm.modules.admin.entity;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.controller.dynamic.ZformController;
import com.jeestudio.bpm.modules.admin.service.SysFileQueueServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.ThreadLocalUtil;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

/**
 * @Description: 文件队列导出线程
 */
@Slf4j
@AllArgsConstructor
public class SysFileQueueExportThread  implements Runnable {
	ZformService zformService;
	SysFileQueueServiceI sysFileQueueService;
	String queueId;
	JSONObject zformMap;
	String formNo;
	String path;
	String traceFlag;
	String parentId;
	String currentLoginName;
	Map<String,Object> requestParams;
	RequestAttributes requestAttributes;



	@Override
	public void run(){
		try {
			UserUtil.setCurrentLoginName(currentLoginName);
			ThreadLocalUtil.setRequestParams(requestParams);
			RequestContextHolder.setRequestAttributes(requestAttributes);
			ResultJson expData = zformService.expdata(zformMap, formNo, path, traceFlag, parentId,currentLoginName);
			SysFileQueueEntity queueItem = sysFileQueueService.getById(queueId);
			queueItem.setFileId(ConvertUtil.getString(expData.getData().get("fileId")));
			queueItem.setExportStatus(SysFileQueueExportStatus.SUCCESS.name());
			sysFileQueueService.updateById(queueItem);
		} catch (Exception e) {
			log.error("异步导出文件失败, queueId={}", queueId, e);
			SysFileQueueEntity queueItem = sysFileQueueService.getById(queueId);
			queueItem.setExportStatus(SysFileQueueExportStatus.FAIL.name());
			sysFileQueueService.updateById(queueItem);
		}
		ThreadLocalUtil.removeAll();
	}
}
