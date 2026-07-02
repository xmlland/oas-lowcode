package com.jeestudio.bpm.mapper.base.gen;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignDraftVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * @Description: AI表单草稿版本数据访问接口
 */

@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface AiFormDesignDraftVersionDao extends CrudDao<AiFormDesignDraftVersion> {

    List<AiFormDesignDraftVersion> findByDraftId(@Param("draftId") String draftId);

    int countByDraftId(@Param("draftId") String draftId);

    int deleteByDraftId(@Param("draftId") String draftId);
}
