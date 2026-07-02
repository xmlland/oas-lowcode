package com.jeestudio.bpm.feign.factory;

import com.jeestudio.bpm.feign.WordUploadFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @Description: Word上传Feign降级工厂
 */
public class WordUploadApiFallbackFactory implements FallbackFactory<WordUploadFeignClient> {
    @Override
    public WordUploadFeignClient create(Throwable throwable) {
        return (templateFile) -> {
            throw new RuntimeException("调用WordUploadFeignClient.upload失败");
        };
    }
}
