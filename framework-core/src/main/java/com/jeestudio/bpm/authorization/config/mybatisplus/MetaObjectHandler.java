package com.jeestudio.bpm.authorization.config.mybatisplus;

import cn.hutool.core.date.DateUtil;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.utils.UserUtil;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

/**
 * @Description: MyBatis-Plus元对象填充处理器
 */
public class MetaObjectHandler implements com.baomidou.mybatisplus.core.handlers.MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createDate", Date.class, getNow());
        this.strictInsertFill(metaObject, "createBy", String.class, getUserId());
        this.strictInsertFill(metaObject, "ownerCode", String.class, getOwnerCode());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "updateDate", Date.class, getNow());
        this.strictInsertFill(metaObject, "updateBy", String.class, getUserId());
    }


    private String getUserId() {
        User currentUser = UserUtil.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        return currentUser.getId();
    }

    private String getOwnerCode() {
        User currentUser = UserUtil.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        if (currentUser.getOffice() == null) {
            return null;
        }
        return currentUser.getOffice().getCode();
    }

    private Date getNow() {
        return DateUtil.date();
    }
}
