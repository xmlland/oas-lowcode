package com.jeestudio.bpm.modules.admin.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description: 子系统Mapper
 */
@Mapper
public interface SysSubSystemMapper {

    List<String> listSubSystemCodesByUserId(String userId);
}
