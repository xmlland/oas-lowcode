package com.jeestudio.bpm.controller.user;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.view.system.UserView;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.admin.vo.UserExport;
import com.jeestudio.bpm.security.TranEncryptUtil;
import com.jeestudio.bpm.security.authentication.RequireAuthentication;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.service.system.UserService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import com.jeestudio.tools.excel.ExcelExportUtil;
import com.jeestudio.tools.security.utils.PasswordUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 系统设置用户
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("${adminPath}/sys/user")
public class UserController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    private SecLogService secLogService;

    /**
     * 获取加密配置
     */
    @Operation(summary = "获取加密配置")
    @RequiresPermissions("user")
    @GetMapping("/getEncryptConfig")
    public ResultJson getEncryptConfig() {
        ResultJson resultJson = success();
        TranEncryptUtil.setPublicKey(currentUserId.get());
        return resultJson;
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @RequiresPermissions("user")
    @GetMapping("/getCurrentUserView")
    public ResultJson getCurrentUserView() {
        ResultJson resultJson = userService.getCurrentUserView(currentUserId.get());
        resultJson.setToken(token.get());
        User user = UserUtil.getByLoginName(currentUserName.get());
        resultJson.put("extEntId", user.getExtEntId());
        return resultJson;
    }

    /**
     * 保存用户
     */
    @Operation(summary = "保存用户")
    @RequiresPermissions("user")
    @PostMapping("/save")
    public ResultJson save(@RequestBody Zform zform) throws Exception {
        ResultJson resultJson = userService.save(zform, currentUserName.get());
        resultJson.setToken(token.get());
        if (resultJson.getCode() == null || resultJson.getCode() != ResultJson.CODE_FAILED) {
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("保存用户成功");
            resultJson.setMsg_en("User data was saved successfully.");
        }
        if (resultJson.getCode() != null && resultJson.getCode() == ResultJson.CODE_SUCCESS) {
            cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + zform.getS01());
        }
        return resultJson;
    }

    /**
     * 获取登录异常次数
     */
    @Operation(summary = "获取登录异常次数")
    @RequiresPermissions("user")
    @GetMapping("/getLoginExceptionCount")
    public ResultJson getLoginExceptionCount(@RequestParam("loginName") String loginName) {
        return ResultJson.success().put("count", userService.getLoginExceptionCount(loginName));
    }

    /**
     * 清除登录异常次数
     */
    @Operation(summary = "清除登录异常次数")
    @RequiresPermissions("user")
    @PostMapping("/clearLoginExceptionCount")
    public ResultJson clearLoginExceptionCount(@RequestParam("loginName") String loginName) {
        userService.clearLoginExceptionCount(loginName);
        return ResultJson.success();
    }

    /**
     * 检查密码是否过期
     */
    @Operation(summary = "检查密码是否过期")
    @RequiresPermissions("user")
    @GetMapping("/isPasswordExpired")
    public ResultJson isPasswordExpired(@RequestParam("loginName") String loginName) {
        return ResultJson.success().put("expired", userService.isPasswordExpired(loginName));
    }

    /**
     * 解锁用户
     */
    @Operation(summary = "解锁用户")
    @RequiresPermissions("user")
    @PostMapping("/unlockUser")
    public ResultJson unlockUser(@RequestParam("loginName") String loginName) {
        try {
            userService.unlockUser(loginName);
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "解锁用户成功", "解锁用户", Global.YES);
            return ResultJson.success();
        } catch (Exception e) {
            logger.error("Error occurred while trying to unlock user: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "解锁用户失败", "解锁用户", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 锁定用户
     */
    @Operation(summary = "锁定用户")
    @RequiresPermissions("user")
    @PostMapping("/lockUser")
    public ResultJson lockUser(@RequestParam("loginName") String loginName) {
        try {
            userService.lockUser(loginName);
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "锁定用户成功", "锁定用户", Global.YES);
            return ResultJson.success();
        } catch (Exception e) {
            logger.error("Error occurred while trying to lock user: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "锁定用户失败", "锁定用户", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }


    @Operation(summary = "启用用户")
    @RequiresPermissions("user")
    @PostMapping("/enableUser")
    public ResultJson enableUser(@RequestParam("loginName") String loginName) {
        try {
            userService.enableUser(loginName);
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "启用用户成功", "启用用户", Global.YES);
            return ResultJson.success();
        } catch (Exception e) {
            logger.error("Error occurred while trying to enable user: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "启用用户失败", "启用用户", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }


    /**
     * 禁用用户
     */
    @Operation(summary = "禁用用户")
    @RequiresPermissions("user")
    @PostMapping("/disableUser")
    public ResultJson disableUser(@RequestParam("loginName") String loginName) {
        try {
            userService.disableUser(loginName);
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "禁用用户成功", "禁用用户", Global.YES);
            return ResultJson.success();
        } catch (Exception e) {
            logger.error("Error occurred while trying to disable user: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "禁用用户失败", "禁用用户", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 增加登录异常次数
     */
    @Operation(summary = "增加登录异常次数")
    @RequiresPermissions("user")
    @PostMapping("/addLoginExceptionCount")
    public ResultJson addLoginExceptionCount(@RequestParam("loginName") String loginName) {
        userService.addLoginExceptionCount(loginName);
        return ResultJson.success();
    }

    private final static String DES = "DES";
    private final static String ENCODE = "GBK";
    private final static String defaultKey = "zlmzlmzlm";

    public static String decrypt(String data) throws Exception {
        if (data == null)
            return null;
        // 修复密文空格问题
        byte[] buf = Base64Decoder.decode(data.replace(" ", "+"));
        byte[] bt = decrypt(buf, defaultKey.getBytes(ENCODE));
        return new String(bt, ENCODE);
    }

    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance(DES);
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
        return cipher.doFinal(data);
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @RequiresPermissions("user")
    @PostMapping("/changePassword")
    public ResultJson changePassword(@RequestBody Map<String, Object> map) {
        try {
            String oldPassword = ConvertUtil.getString(map.get("oldPassword")), newPassword = ConvertUtil.getString(map.get("newPassword"));
            oldPassword = decrypt(oldPassword);
            newPassword = decrypt(newPassword);
            ResultJson resultJson = userService.changePassword(oldPassword, newPassword, currentUserName.get());
            if (StringUtil.isNotBlank(currentUserName.get()) && resultJson.getCode() != null && resultJson.getCode() == ResultJson.CODE_SUCCESS) {
                cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + currentUserName.get());
            }
            resultJson.setToken(token.get());
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "修改密码成功", "修改密码", Global.YES);
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to update password: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "修改密码失败", "修改密码", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }

    @Operation(summary = "重置密码")
    @RequiresPermissions("user")
    @PostMapping("/resetPassword")
    public ResultJson resetPassword(@RequestParam String loginName) {
        try {
            ResultJson resultJson = userService.changePassword(loginName);
            resultJson.setToken(token.get());
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "重置密码成功", "重置密码", Global.YES);
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to update password: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "重置密码失败", "重置密码", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }

    @Operation(summary = "重置临时密码")
    @RequiresPermissions("user")
    @PostMapping("/resetTempPassword")
    @RequireAuthentication()
    public ResultJson resetTempPassword(@RequestParam String loginName, @RequestParam String publicKey) {
        try {
            ResultJson resultJson = userService.changeTempPassword(loginName, PasswordUtil.generate(), publicKey);
            resultJson.setToken(token.get());
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "重置用户" + loginName + "的临时密码成功", "重置临时密码", Global.YES);
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to reset temp password: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "重置临时密码失败", "重置临时密码", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }

    @Autowired
    ZformService zformService;

    @Autowired
    GenTableService genTableService;

    @Autowired
    SysFileService sysFileService;

    /**
     * batchResetPassword 批量重置密码
     *
     * @param newPassword
     * @param zformMap
     * @return
     */
    @Operation(summary = "批量重置密码")
    @RequiresPermissions("user")
    @PostMapping("/batchResetPassword")
    public ResultJson batchResetPassword(@RequestParam(required = false) String newPassword, @RequestBody JSONObject zformMap) {
        try {
            String formNo = "sys_user";
            zformMap.put("formNo", formNo);
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);
            Zform zform = zformService.getZformFromMap(zformMap, currentUserName.get());
            zformService.setQueryWrapper(zform, genTable, UserUtil.getCurrentUser());
            QueryWrapper<Zform> queryWrapper = zform.getQueryWrapper();
            if (zform.getParent() != null && StrUtil.isNotEmpty(zform.getParent().getId())) {
                queryWrapper.and(wrapper -> wrapper.eq("b.id", zform.getParent().getId()).or().like("b.parent_ids", StrUtil.format(",{},", zform.getParent().getId())));
            }
            List<LinkedHashMap> mapList = zformService.findMapList(formNo, queryWrapper);
            if (mapList == null || mapList.size() == 0) {
                return ResultJson.failed("没有符合条件的用户");
            }
            if (StringUtil.isNotEmpty(newPassword)) {
                newPassword = decrypt(newPassword);
            }
            try {
                List<UserExport> userExports = userService.batchResetPassword(newPassword, mapList);
                Workbook workbook = ExcelExportUtil.export(UserExport.class, userExports);
                SysFile sysFile = sysFileService.saveExportSysFile("用户列表.xlsx", workbook);
                secLogService.saveSecLog(currentUserName.get(), ip.get(), "批量重置密码成功", "批量重置密码", Global.YES);
                return ResultJson.success().put("fileId", sysFile.getId());
            } catch (BusinessException e) {
                return ResultJson.failed("批量重置密码异常，" + e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error occurred while trying to batchResetPassword: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "批量重置密码失败", "批量重置密码", Global.YES);
            return ResultJson.failed("批量重置密码异常，" + e.getMessage());
        }
    }

    /*
     * 上传签名
     * */
    @Operation(summary = "更新签名")
    @RequiresPermissions("user")
    @PostMapping("/updateSign")
    public ResultJson updateSign(@RequestParam(required = true, name = "signGroupId") String signGroupId) {
        userService.updateSign(signGroupId);
        return ResultJson.success();
    }

    /**
     * 查询当前用户主管的部门下的用户
     */
    @Operation(summary = "获取下属用户列表")
    @RequiresPermissions("user")
    @GetMapping("/getSubordinateList")
    public ResultJson getSubordinateList() {
        return ResultJson.success().setRows(userService.findSubordinateListByPrimaryPerson(currentUserId.get()));
    }
}
