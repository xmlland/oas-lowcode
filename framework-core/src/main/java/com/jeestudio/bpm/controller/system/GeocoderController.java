package com.jeestudio.bpm.controller.system;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 地理编码
 */
@Tag(name = "地理编码")
@RestController
@RequestMapping("${adminPath}/system/geocoder")
public class GeocoderController extends BaseController {
    protected static final Logger logger = LoggerFactory.getLogger(GeocoderController.class);

    @Operation(summary = "根据经纬度查询地址")
    @RequiresPermissions("user")
    @PostMapping("/query")
    public ResultJson query(@RequestParam("longitude") String longitude, @RequestParam("latitude") String latitude, @RequestParam("tk") String tk) {
        JSONObject jsonObject = post(longitude, latitude, tk);
        return ResultJson.success().put("data", jsonObject);
    }

    private static JSONObject post(String longitude, String latitude, String tk) {

        String queryUrl = "http://api.tianditu.gov.cn/geocoder";

        JSONObject postData = new JSONObject();
        postData.put("lon", longitude);
        postData.put("lat", latitude);
        postData.put("ver", 1);
        HttpResponse execute = HttpUtil.createGet(queryUrl)
                .form("postStr", postData.toJSONString())
                .form("tk", tk)
                .form("type", "geocode")
                .execute();
        if (execute.isOk()) {
            JSONObject jsonObject = JSONObject.parseObject(execute.body());
            if (jsonObject.containsKey("status") && jsonObject.getString("status").equals("0")) {
                return jsonObject;
            }
        }
        throw new RuntimeException("请求失败");
    }
}
