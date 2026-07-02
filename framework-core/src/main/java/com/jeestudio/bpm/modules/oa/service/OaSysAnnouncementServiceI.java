package com.jeestudio.bpm.modules.oa.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.modules.oa.entity.OaSysAnnouncementEntity;

import java.util.Date;
import java.util.List;


/**
 * @Description: OA系统公告服务接口
 */
public interface OaSysAnnouncementServiceI extends IService<OaSysAnnouncementEntity> {


    /**
     * 发送系统消息通知指定角色
     *
     * @param title    标题
     * @param content  内容
     * @param roleCode 角色编码
     * @param autoPop  是否自动弹出
     */
    void sendToRole(String title, String content, boolean autoPop, String roleCode);

    /**
     * 发送系统消息通知指定角色
     *
     * @param title   标题
     * @param content 内容
     * @param autoPop 是否自动弹出
     * @param roleId  角色ID
     */
    void sendToRoleId(String title, String content, boolean autoPop, String roleId);

    /**
     * 发送系统消息通知指定角色
     *
     * @param senderLoginName 发送者登录名
     * @param title           标题
     * @param content         内容
     * @param autoPop         是否自动弹出
     * @param roleCode        角色编码
     */
    void sendToRole(String senderLoginName, String title, String content, boolean autoPop, String roleCode);

    /**
     * 发送系统消息通知指定角色的用户
     *
     * @param senderLoginName 发送者登录名
     * @param title           标题
     * @param content         内容
     * @param autoPop         是否自动弹出
     * @param roleCode        角色编码
     */
    void sendToUserByRole(String senderLoginName, String title, String content, boolean autoPop, String roleCode);

    /**
     * 发送系统消息通知指定角色的用户 通过用户查询条件
     *
     * @param senderLoginName  发送者登录名
     * @param title            标题
     * @param content          内容
     * @param autoPop          是否自动弹出
     * @param roleCode         角色编码
     * @param userQueryWrapper 用户查询条件 使用sys_user的表单配置
     */
    void sendToUserByRole(String senderLoginName, String title, String content, boolean autoPop, String roleCode, QueryWrapper<User> userQueryWrapper);

    /**
     * 发送系统消息通知指定角色
     *
     * @param senderLoginName 发送者登录名
     * @param title           标题
     * @param content         内容
     * @param autoPop         是否自动弹出
     * @param roleId          角色ID
     */
    void sendToRoleId(String senderLoginName, String title, String content, boolean autoPop, String roleId);

    /**
     * 发送系统消息通知指定用户
     *
     * @param title   标题
     * @param content 内容
     * @param autoPop 是否自动弹出
     * @param userId  用户ID
     */
    void sendToUser(String title, String content, boolean autoPop, String userId);

    /**
     * 发送系统消息通知指定用户
     *
     * @param title   标题
     * @param content 内容
     * @param autoPop 是否自动弹出
     * @param userIds 用户ID
     */
    void sendToUsers(String title, String content, boolean autoPop, String... userIds);

    /**
     * 发送系统消息通知指定用户
     *
     * @param title      标题
     * @param content    内容
     * @param autoPop    是否自动弹出
     * @param userIdList 用户ID列表
     */
    void sendToUserList(String title, String content, boolean autoPop, List<String> userIdList);

    /**
     * 发送系统消息通知指定用户
     *
     * @param senderLoginName 发送者登录名
     * @param title           标题
     * @param content         内容
     * @param autoPop         是否自动弹出
     * @param userId          用户ID
     */
    void sendToUser(String senderLoginName, String title, String content, boolean autoPop, String userId);

    /**
     * 发送系统消息通知指定用户
     *
     * @param senderLoginName 发送者登录名
     * @param title           标题
     * @param content         内容
     * @param autoPop         是否自动弹出
     * @param userIds         用户ID
     */
    void sendToUsers(String senderLoginName, String title, String content, boolean autoPop, String... userIds);

    /**
     * 发送系统消息通知指定用户
     *
     * @param senderLoginName 发送者登录名
     * @param title           标题
     * @param content         内容
     * @param autoPop         是否自动弹出
     * @param userIdList      用户ID列表
     */
    void sendToUserList(String senderLoginName, String title, String content, boolean autoPop, List<String> userIdList);

    /**
     * 保存系统消息通知
     * @param senderLoginName 发送者登录名
     * @param title 标题
     * @param content 内容
     * @param autoPop 是否自动弹出
     * @param roleId 角色ID
     * @param sendTime 发送时间
     */

    void saveAnnouncement(String senderLoginName, String title, String content, boolean autoPop, String roleId, Date sendTime);

    /**
     * 保存系统消息通知
     * @param senderLoginName 发送者登录名
     * @param title 标题
     * @param content 内容
     * @param autoPop 是否自动弹出
     * @param userIdList 用户ID列表
     * @param sendTime 发送时间
     */
    void saveAnnouncement(String senderLoginName, String title, String content, boolean autoPop, List<String> userIdList, Date sendTime);
}
