package com.jeestudio.bpm.feign.factory;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.feign.RequestVo;
import com.jeestudio.bpm.feign.WordExportFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * @Description: Word导出Feign降级工厂
 */
@Component
@Slf4j
public class WordExportApiFallbackFactory implements FallbackFactory<WordExportFeignClient> {
    @Override
    public WordExportFeignClient create(Throwable cause) {
        return new WordExportFeignClient() {
            @Override
            public byte[] export(String fileName, RequestVo requestVo) {
                log.error("调用WordExportFeignClient.export失败");
                throw new RuntimeException("调用WordExportFeignClient.export失败");
            }

            @Override
            public byte[] mergeMultiWord(List<String> fileNames) {
                log.error("调用WordExportFeignClient.mergeMultiWord失败");
                throw new RuntimeException("调用WordExportFeignClient.mergeMultiWord失败");
            }

            @Override
            public JSONObject checkTemplate(List<String> fileNameList) {
                log.error("调用WordExportFeignClient.checkTemplate失败");
                throw new RuntimeException("调用WordExportFeignClient.checkTemplate失败");
            }
        };
    }
}
