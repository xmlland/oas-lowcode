package com.jeestudio.bpm.mapper.base.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 安全日志数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface SecLogDao {

    void saveSecLog(@Param("id") String id,
                    @Param("account") String account,
                    @Param("content") String content,
                    @Param("time") Date time,
                    @Param("ip") String ip,
                    @Param("type") String type,
                    @Param("result") String result);

    void saveSecLogIntegrity(@Param("id") String id,
                             @Param("account") String account,
                             @Param("content") String content,
                             @Param("time") Date time,
                             @Param("ip") String ip,
                             @Param("type") String type,
                             @Param("result") String result,
                             @Param("integrityValue") String integrityValue);


    IPage<Map<String,Object>> selectSecLogIntegrity(IPage page);

    String getSecLogSpace();
}
