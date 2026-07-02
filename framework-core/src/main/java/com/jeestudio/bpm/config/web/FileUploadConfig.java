package com.jeestudio.bpm.config.web;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.JwtUtil;
import com.jeestudio.bpm.utils.oConvertUtils;
import com.jeestudio.tools.security.utils.FileTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * @Description: 文件上传配置
 */
@Configuration//声明这是一个配置
@Slf4j
public class FileUploadConfig implements WebMvcConfigurer {

    @Autowired
    ProjectProperties projectProperties;

    @Autowired
    SecLogService secLogService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        HandlerInterceptor interceptor = new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if (isMultipartContent(request)) {
                    String userInfo = JwtUtil.getCurrentUser(request.getHeader("token"));
                    assert userInfo != null;
                    String account = userInfo.substring(0, userInfo.lastIndexOf("_"));
                    ProjectProperties.uploadConfig uploadConfig = projectProperties.getUploadConfig();
                    List<String> acceptFiles = uploadConfig.getAcceptFiles();
                    List<String> contentTypes = uploadConfig.getContentTypes();
                    MultipartHttpServletRequest request1 = (MultipartHttpServletRequest) request;
                    String requestURI = request.getRequestURI();
                    Map<String, MultipartFile> fileMap = request1.getFileMap();
                    for (Map.Entry<String, MultipartFile> stringMultipartFileEntry : fileMap.entrySet()) {
                        MultipartFile value = stringMultipartFileEntry.getValue();
                        String contentType = value.getContentType();
                        String originalFilename = value.getOriginalFilename();
                        String extend = FileUtil.getSuffix(originalFilename);


                        assert extend != null;
                        if (!acceptFiles.contains(extend.toLowerCase())) {
                            log.warn("文件类型{}不允许上传,当前用户:{},请求地址:{},文件名:{}", extend, userInfo, requestURI, originalFilename);
                            saveLog(account, request, "文件类型" + extend + "不允许上传");
                            response403(response);
                            return false;
                        }
                        if (!contentTypes.contains(contentType)) {
                            log.warn("文件类型{}不允许上传,mime异常,当前用户:{},请求地址:{},文件名:{}", contentType, userInfo, requestURI, originalFilename);
                            saveLog(account, request, "文件类型" + contentType + "不允许上传,mime异常");
                            response403(response);
                            return false;
                        }
                        String chunk = request.getParameter("chunk");

                        if (StrUtil.isEmpty(chunk) || "0".equals(chunk)) {
                            //第一个分片才能校验文件类型 或者不是分片
                            String fileTypeByStream = FileTypeUtil.getFileTypeByStream(value.getBytes());
                            if (!acceptFiles.contains(fileTypeByStream)) {
                                log.warn("文件类型{}不允许上传,文件头信息错误,当前用户:{},请求地址:{},文件名:{}", fileTypeByStream, userInfo, requestURI, originalFilename);
                                saveLog(account, request, "文件" + extend + "不允许上传,文件头信息错误");
                                response403(response);
                                return false;
                            }
                        }

                    }
                }
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
                HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
                HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
            }
        };
        registry.addInterceptor(interceptor).addPathPatterns("/**");

    }

    private void saveLog(String account, HttpServletRequest request, String content) {
        String ipAddrByRequest = oConvertUtils.getIpAddrByRequest(request);
        secLogService.saveSecLog(account, ipAddrByRequest, content, "文件上传异常", Global.NO);
    }

    private void response403(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        out.flush();
        out.close();
    }

    private boolean isMultipartContent(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return request.getContentType() != null && request.getContentType().startsWith("multipart/");
    }
}
