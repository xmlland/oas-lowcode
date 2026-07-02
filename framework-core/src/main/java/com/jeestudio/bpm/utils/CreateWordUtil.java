package com.jeestudio.bpm.utils;

import ch.qos.logback.classic.Logger;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.exceptions.ExceptionUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * @Description: Word文档生成工具
 */
@Component
public class CreateWordUtil {

	@Value("${docTemplateRoot}")
	private String uploadpath;

	private static String static_uploadpath;

	@PostConstruct
	private  void init(){
		static_uploadpath=uploadpath;
	}

	private final static Logger logger = (Logger) LoggerFactory.getLogger(CreateWordUtil.class);

	/**
	 * 生成word并直接输出下载流到页面
	 * @param request
	 * @param response
	 * @param templateName 模板名称
	 * @param wordName word名称
	 * @param dataMap 数据map
	 * @throws Exception
	 */
	public static void CreateFile(HttpServletRequest request, HttpServletResponse response, String templateName, String wordName, Map<String, Object> dataMap)  {

		Configuration configuration = new Configuration();
		configuration.setDefaultEncoding("utf-8");

		String folder =static_uploadpath;
		String name = folder+"/temp" + UUID.randomUUID().toString().replaceAll("-", "") + ".xml";
		File f = new File(name);
		// 设置FreeMarker的模版文件位置
		configuration.setClassForTemplateLoading(CreateWordUtil.class, "/tpl/");
		Template t = null;
		Writer w = null;
		InputStream fin = null;
		ServletOutputStream out = null;
		// 要装载的模板
		try {
			t = configuration.getTemplate(templateName);
			w = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
			t.process(dataMap, w);
			f.setReadOnly();
			f.setWritable(false);
			fin = new FileInputStream(f);
			response.setCharacterEncoding("utf-8");
			response.setContentType("application/msword");
			// 设置浏览器以下载的方式处理该文件默认名为resume.doc
			response.addHeader("Content-Disposition", "attachment;filename="
					+ new String(wordName.getBytes("gb2312"), "ISO8859-1")
					+ ".doc");
			out = response.getOutputStream();
			byte[] buffer = new byte[512]; // 缓冲区
			int bytesToRead = -1;
			// 通过循环将读入的Word文件的内容输出到浏览器中
			while ((bytesToRead = fin.read(buffer)) != -1) {
				out.write(buffer, 0, bytesToRead);
			}
			logger.info("生成word成功[{}]",name);
		} catch (Exception e) {
			logger.error(ExceptionUtil.stacktraceToString(e));
		} finally {
			try {
				if (w != null) {
					w.close();
				}
				if (fin != null) {
					fin.close();
				}
				if (out != null) {
					out.close();
				}
				if (f != null) {
					f.delete(); // 删除临时文件
				}
			} catch (Exception e) {
				logger.warn("关闭资源失败", e);
			}
		}
	}
	/**
	 * 生成word并返回生成的file 对象
	 * @param request
	 * @param templateName 模板名称
	 * @param wordname word名称
	 * @param dataMap 数据map
	 * @return
	 */
	public static File CreateFile(HttpServletRequest request, String templateName, String wordname,
                                  Map<String, Object> dataMap)  {
		Configuration configuration = new Configuration();
		configuration.setDefaultEncoding("utf-8");
		String folder =static_uploadpath;
		String wordpath=folder+"/exportWord/temp"+ UUID.randomUUID().toString().replaceAll("-", "") +"/";
		File fpath = new File(wordpath);
		if (!fpath.exists()) {
			fpath.mkdirs();
		}
		File f = new File(wordpath+wordname+".doc");
		// 设置FreeMarker的模版文件位置
		configuration.setClassForTemplateLoading(CreateWordUtil.class, "/export/template/");
		Template t = null;
		Writer w = null;
		// 要装载的模板
		try {

			w = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
			t = configuration.getTemplate(templateName);
			t.process(dataMap, w);
		} catch (Exception e) {
			logger.error(ExceptionUtil.stacktraceToString(e));
		} finally {
			try {
				if (w != null) {
					w.close();
				}
			} catch (Exception e) {
				// 关闭失败不影响主流程，异常已在外层记录
			}

		}
		logger.info("生成word成功[{}.doc]",wordpath+wordname );
		return f;
	}

	/**
	 * 在指定路径生成word并返回生成的file 对象
	 * @param request
	 * @param templateName 模板名称
	 * @param wordname word名称
	 * @param dataMap 数据map
	 * @param wordpath 生成路径
	 * @return
	 */
	public static File CreateFile(HttpServletRequest request, String templateName, String wordname, Map<String, Object> dataMap, String wordpath)  {
		Configuration configuration = new Configuration();
		configuration.setDefaultEncoding("utf-8");
		File fpath = new File(wordpath);
		if (!fpath.exists()) {
			fpath.mkdirs();
		}
		File f = new File(wordpath+wordname+".doc");
		// 设置FreeMarker的模版文件位置
		configuration.setClassForTemplateLoading(CreateWordUtil.class, "/export/template/");
		Template t = null;
		Writer w = null;
		// 要装载的模板
		try {
			t = configuration.getTemplate(templateName);
			t = configuration.getTemplate(templateName);
			t.process(dataMap, w);
		} catch (Exception e) {
			logger.error(ExceptionUtil.stacktraceToString(e));
		} finally {
			try {
				if (w != null) {
					w.close();
				}
			} catch (Exception e) {
				// 关闭失败不影响主流程，异常已在外层记录
			}

		}
		logger.info("生成word成功[{}.doc]",wordpath+wordname );
		return f;
	}

	public static String getImageString(String filename) {
		filename = filename;
		InputStream in = null;
		byte[] data = null;
		try {
			in = new FileInputStream(filename);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			logger.error(ExceptionUtil.stacktraceToString(e));
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.warn("关闭输入流失败", e);
				}
			}
		}
		return Base64Encoder.encode(data);
	}




}
