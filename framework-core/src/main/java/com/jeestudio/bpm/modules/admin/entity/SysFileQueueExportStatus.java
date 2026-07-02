package com.jeestudio.bpm.modules.admin.entity;

/**
 * @Description: 文件队列导出状态
 */
public enum SysFileQueueExportStatus {
	/** 导出中 */
	EXPORTING("EXPORTING"),
	/** 成功 */
	SUCCESS("SUCCESS"),
	/** 失败 */
	FAIL("FAIL");

	SysFileQueueExportStatus(String value) {
	}
}
