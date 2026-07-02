package com.jeestudio.bpm.service.system;

import com.jeestudio.bpm.common.entity.system.Post;
import com.jeestudio.bpm.mapper.base.system.PostDao;
import com.jeestudio.bpm.service.common.CrudService;
import org.springframework.stereotype.Service;

/**
 * @Description: 岗位服务
 */
@Service
public class PostService extends CrudService<PostDao, Post> {
}
