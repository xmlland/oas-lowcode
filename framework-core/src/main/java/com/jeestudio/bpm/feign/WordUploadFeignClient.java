package com.jeestudio.bpm.feign;

import com.jeestudio.bpm.feign.factory.WordUploadApiFallbackFactory;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: Word上传Feign客户端
 */
@Component
@FeignClient(url = "${export-word-url}", name = "service-WordExport", fallback = WordUploadApiFallbackFactory.class)
public interface WordUploadFeignClient extends UploadFeignClient {

    @PostMapping(value = "word/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    boolean upload(@RequestPart MultipartFile templateFile);


    class MultipartSupportConfig {

        @Autowired
        ObjectFactory<HttpMessageConverters> messageConverters;

        @Bean
        public Encoder feignFormEncoder() {
            return new SpringFormEncoder(new SpringEncoder(messageConverters));
        }

    }
}
