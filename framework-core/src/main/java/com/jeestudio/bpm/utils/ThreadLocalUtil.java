package com.jeestudio.bpm.utils;

import lombok.Data;

import java.util.Map;

/**
 * @Description: ThreadLocal请求参数工具
 */

public class ThreadLocalUtil {
	private static ThreadLocal<Map<String,Object>> requestParams = new ThreadLocal<>();

	public static Map<String,Object> getRequestParams(){
		return requestParams.get();
	}

	public static void setRequestParams(Map<String,Object> params){
		requestParams.set(params);
	}

	public static void removeAll(){
		requestParams.remove();
	}
}
