package com.jeestudio.bpm.security;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * @Description: 请求体解密处理器
 */
@RestControllerAdvice
@ConditionalOnProperty(name = "project.transmittalEncryption", havingValue = "true")
@Slf4j
public class DecryptRequest extends RequestBodyAdviceAdapter {
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        if (TranEncryptUtil.isSkip()) {
            return inputMessage;
        }
        InputStream inputStream = inputMessage.getBody();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            byte[] b = new byte[10240];
            int n;
            while ((n = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, n);
            }
        } catch (Exception e) {
            log.error("解密请求body数据失败,{}", ExceptionUtil.stacktraceToString(e));
            try {
                inputStream.close();
                outputStream.close();
            } catch (Exception e1) {
                log.error("解密请求body数据失败,{}", ExceptionUtil.stacktraceToString(e1));
            }
        }
        try {
            JSONObject object = JSONObject.parseObject(outputStream.toString());
            byte[] decrypt = TranEncryptUtil.decryptRequestData(object.getString("text")).getBytes();
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decrypt);
            return new HttpInputMessage() {
                public InputStream getBody() {
                    return byteArrayInputStream;
                }

                public HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };
        } catch (Exception var8) {
            log.error("解密请求数据失败", var8);
            return super.beforeBodyRead(inputMessage, parameter, targetType, converterType);
        }
    }
}
