package com.jeestudio.bpm.modules.prt.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.prt.service.PrtCardServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 打印卡片
 */
@RestController
@RequestMapping("${adminPath}/prt/card")
@Tag(name = "打印卡片")
@Slf4j
public class PrtCardController extends BaseController {

    @Autowired
    PrtCardServiceI prtCardService;

    @Autowired
    ZformService zformService;

    /**
     * listUserCard
     */
    @Operation(summary = "获取用户卡片列表")
    @RequiresPermissions("user")
    @GetMapping("/listUserCard")
    public ResultJson listUserCard() {
        return ResultJson.success().put("data", prtCardService.listCardByUserId(currentUserId.get()));
    }

    /**
     * saveUserCard
     */
    @Operation(summary = "保存用户卡片")
    @RequiresPermissions("user")
    @PostMapping("/saveUserCard")
    public ResultJson saveUserCard(@RequestBody JSONArray jsonArray) {
        String userId = currentUserId.get();
        String loginName = currentUserName.get();
        List<Zform> saveList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject saveObj = new JSONObject();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String card_id = jsonObject.getString("card_id");
            String is_show = jsonObject.getString("is_show");
            Integer sort = jsonObject.getInteger("sort");
            saveObj.put("user_id", userId);
            saveObj.put("card_id", card_id);
            saveObj.put("is_show", is_show);
            saveObj.put("sort", sort);
            saveObj.put("formNo", "prt_card_user");
            try {
                Zform zform = zformService.getZformFromMap(saveObj, loginName);
                saveList.add(zform);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        prtCardService.saveUserCard(userId, loginName, saveList);
        return ResultJson.success();
    }
}
