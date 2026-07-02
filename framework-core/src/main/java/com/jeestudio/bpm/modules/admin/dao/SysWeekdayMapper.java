package com.jeestudio.bpm.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeestudio.bpm.modules.admin.entity.SysWeekdayEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;


/**
 * @Description: 工作日历Mapper
 */
@Mapper
public interface SysWeekdayMapper extends BaseMapper<SysWeekdayEntity> {
    @Select("select max(date_) from (select top #{days} * from sys_weekday where is_weekday = 1 and date_ >= #{date} order by date_ ) a")
    Date calcDateMssql(String date, int days);

    @Select("select max(date_) from (select * from sys_weekday where is_weekday = 1 and  date_ >= #{date} order by date_ limit #{days} ) a")
    Date calcDateMysql(String date, int days);

    @Select("select max(date_) from (select * from sys_weekday where is_weekday = 1 and  date_ >= #{date} and rownum <= #{days} order by date_   ) a")
    Date calcDateOracle(String date, int days);
}
