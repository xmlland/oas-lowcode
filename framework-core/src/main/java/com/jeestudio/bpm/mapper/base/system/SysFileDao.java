package com.jeestudio.bpm.mapper.base.system;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.system.SysFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 系统文件数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface SysFileDao extends CrudDao<SysFile> {

    List<SysFile> findListAndContent(SysFile sysFile);
    List<SysFile> findByStoragePath(@Param("paths") List<String> paths);
    int saveSecretLevel(SysFile sysFile);
}
