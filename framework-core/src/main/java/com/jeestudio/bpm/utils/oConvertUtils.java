package com.jeestudio.bpm.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Description: 通用转换工具
 */
@Slf4j
public class oConvertUtils {

	/**
	 * Checks if an object is empty (null or empty string).
	 *
	 * @param object the object to check
	 * @return true if the object is empty, false otherwise
	 */
	public static boolean isEmpty(Object object) {
		return object == null || "".equals(object) || Global.STRING_NULL.equals(object);
	}

	/**
	 * Checks if an object is not empty.
	 *
	 * @param object the object to check
	 * @return true if the object is not empty, false otherwise
	 */
	public static boolean isNotEmpty(Object object) {
		return object != null && !"".equals(object) && !object.equals(Global.STRING_NULL);
	}

	/**
	 * Decodes a string from one character encoding to another.
	 *
	 * @param strIn      the input string
	 * @param sourceCode the source encoding
	 * @param targetCode the target encoding
	 * @return the decoded string
	 */
	public static String decode(String strIn, String sourceCode, String targetCode) {
		return code2code(strIn, sourceCode, targetCode);
	}

	/**
	 * Converts a string from one encoding to another.
	 *
	 * @param strIn      the input string
	 * @param sourceCode the source encoding
	 * @param targetCode the target encoding
	 * @return the converted string
	 */
	private static String code2code(String strIn, String sourceCode, String targetCode) {
		if (strIn == null || strIn.trim().isEmpty()) {
			return strIn;
		}
		try {
			byte[] bytes = strIn.getBytes(sourceCode);
			return new String(bytes, targetCode);
		} catch (Exception e) {
			log.error("Encoding error", e);
			return null;
		}
	}

	// Various methods to convert strings to integers with default values
	public static int getInt(String s, int defval) {
		if (isEmpty(s)) {
			return defval;
		}
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return defval;
		}
	}

	public static int getInt(String s) {
		return getInt(s, 0);
	}

	public static Integer[] getInts(String[] s) {
		if (s == null) {
			return null;
		}
		return Arrays.stream(s)
				.map(Integer::parseInt)
				.toArray(Integer[]::new);
	}

	public static double getDouble(String s, double defval) {
		if (isEmpty(s)) {
			return defval;
		}
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return defval;
		}
	}

	public static String getString(Object object) {
		return object == null ? "" : object.toString().trim();
	}

	public static String getString(String s, String defval) {
		return isEmpty(s) ? defval : s.trim();
	}

	/**
	 * Gets the local IP address.
	 *
	 * @return the local IP address
	 */
	public static String getIp() {
		try {
			InetAddress address = InetAddress.getLocalHost();
			return address.getHostAddress();
		} catch (UnknownHostException e) {
			log.error("Unable to get local IP address", e);
			return null;
		}
	}

	/**
	 * Gets the client's IP address from the request.
	 *
	 * @param request the HTTP request
	 * @return the IP address
	 */
	public static String getIpAddrByRequest(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (isEmpty(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (isEmpty(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		return isEmpty(ip) ? request.getRemoteAddr() : ip;
	}

	public static String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip.split(",")[0]; // 处理多级代理的情况
	}

	/**
	 * Checks if the given IP address is a private IP.
	 *
	 * @param ipAddress the IP address to check
	 * @return true if it's a private IP, false otherwise
	 */
	public static boolean isInnerIp(String ipAddress) {
		long ipNum = getIpNum(ipAddress);
		return (isInner(ipNum, getIpNum("10.0.0.0"), getIpNum("10.255.255.255")) ||
				isInner(ipNum, getIpNum("172.16.0.0"), getIpNum("172.31.255.255")) ||
				isInner(ipNum, getIpNum("192.168.0.0"), getIpNum("192.168.255.255")) ||
				"127.0.0.1".equals(ipAddress));
	}

	private static long getIpNum(String ipAddress) {
		String[] ipParts = ipAddress.split("\\.");
		return (Long.parseLong(ipParts[0]) << 24) |
				(Long.parseLong(ipParts[1]) << 16) |
				(Long.parseLong(ipParts[2]) << 8) |
				Long.parseLong(ipParts[3]);
	}

	private static boolean isInner(long userIp, long begin, long end) {
		return userIp >= begin && userIp <= end;
	}

	/**
	 * Converts a string from snake_case to camelCase.
	 *
	 * @param name the input string in snake_case
	 * @return the converted string in camelCase
	 */
	public static String camelName(String name) {
		if (isEmpty(name)) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		String[] parts = name.split("_");
		for (String part : parts) {
			if (!part.isEmpty()) {
				if (result.length() == 0) {
					result.append(part.toLowerCase());
				} else {
					result.append(part.substring(0, 1).toUpperCase())
							.append(part.substring(1).toLowerCase());
				}
			}
		}
		return result.toString();
	}

	/**
	 * Converts a list of maps to lowercase keys.
	 *
	 * @param list the list of maps
	 * @return a new list with all keys in lowercase
	 */
	public static List<Map<String, Object>> toLowerCasePageList(List<Map<String, Object>> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		for (Map<String, Object> row : list) {
			Map<String, Object> resultMap = new HashMap<>();
			row.forEach((key, value) -> resultMap.put(key.toLowerCase(), value));
			resultList.add(resultMap);
		}
		return resultList;
	}

	/**
	 * Converts a list of entities to a list of models.
	 *
	 * @param fromList the list of entities
	 * @param tClass   the target class
	 * @param <F>      the entity type
	 * @param <T>      the model type
	 * @return the list of models
	 */
	public static <F, T> List<T> entityListToModelList(List<F> fromList, Class<T> tClass) {
		if (fromList == null || fromList.isEmpty()) {
			return Collections.emptyList();
		}
		List<T> tList = new ArrayList<>();
		for (F f : fromList) {
			tList.add(entityToModel(f, tClass));
		}
		return tList;
	}

	/**
	 * Converts an entity to a model.
	 *
	 * @param entity    the entity to convert
	 * @param modelClass the target model class
	 * @param <F>       the entity type
	 * @param <T>       the model type
	 * @return the converted model
	 */
	public static <F, T> T entityToModel(F entity, Class<T> modelClass) {
		if (entity == null || modelClass == null) {
			return null;
		}
		try {
			T model = modelClass.getDeclaredConstructor().newInstance();
			BeanUtils.copyProperties(entity, model);
			return model;
		} catch (Exception e) {
			log.error("Error converting entity to model", e);
			return null;
		}
	}

	/**
	 * Reads static text content from a specified URL.
	 *
	 * @param url the URL of the static text
	 * @return the content as a string
	 */
	public static String readStatic(String url) {
		try (InputStream stream = oConvertUtils.class.getClassLoader().getResourceAsStream(url.replace("classpath:", ""))) {
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error("Error reading static file", e);
			return "";
		}
	}

	// Additional utility methods can be added here...
}
