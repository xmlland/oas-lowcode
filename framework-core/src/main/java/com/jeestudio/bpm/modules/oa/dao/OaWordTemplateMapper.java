package com.jeestudio.bpm.modules.oa.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.modules.oa.entity.OaWordTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * @Description: Word模板Mapper
 */
@Mapper
public interface OaWordTemplateMapper extends BaseMapper<OaWordTemplateEntity> {

    /**
     * master 模式：LEFT JOIN sys_file_ 获取模板文件（仅 master 模式有效，跨数据源不可用）
     */
    @Select("select b.* from oa_word_template a left join sys_file_ b on a.template_file = b.group_id_ where a.id = #{templateId}")
    SysFile getFileByTemplateId(String templateId);

    /**
     * master 模式：LEFT JOIN sys_file_ 获取所有模板文件列表（仅 master 模式有效，跨数据源不可用）
     */
    @Select("select b.id,b.ext_,a.word_code,a.word_name,b.name_ from oa_word_template a left join sys_file_ b on a.template_file = b.group_id_")
    List<Map<String,String>> findAllFile();

    /**
     * 查询 template_file（无 JOIN），用于 datahouse 模式
     */
    @Select("select template_file from oa_word_template where id = #{templateId}")
    String getTemplateFileByTemplateId(String templateId);

    /**
     * 查询所有模板记录（不含 JOIN），用于 datahouse 模式
     */
    @Select("select id, word_code, word_name, template_file from oa_word_template")
    List<Map<String,String>> findAllTemplateRecords();
}
