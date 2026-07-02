package com.jeestudio.bpm.controller.cms;

import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.cms.PrtChannel;
import com.jeestudio.bpm.common.entity.cms.PrtConstants;
import com.jeestudio.bpm.common.entity.cms.PrtInformation;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.service.cms.PrtInformationService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.controller.user.UserController;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 新闻与内容门户
 */
@Tag(name = "内容管理")
@RestController
@RequestMapping("${adminPath}/app/cms")
public class CmsController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    PrtInformationService prtInformationService;

    /**
     * 获取新闻首页
     */
    @Operation(summary = "获取新闻首页")
    @RequiresPermissions("user")
    @GetMapping("/getIndex")
    public ResultJson getIndex(@RequestParam("activeChannel") String activeChannel) {
        String loginName = currentUserName.get();
        ResultJson resultJson = new ResultJson();
        PrtChannel queryChannel = new PrtChannel();
        queryChannel.setParent(new PrtChannel(PrtConstants.NUMBER_0)); //一级频道
        queryChannel.setUseable(PrtConstants.ENABLED);
        List<PrtChannel> tabs = prtInformationService.findChannelList(queryChannel);
        resultJson.put("tabs", tabs);

        if (StringUtil.isBlank(activeChannel)) {
            //2. 轮播图
            PrtInformation queryPlay = new PrtInformation();
            queryPlay.setIfPlay(PrtConstants.YES);
            queryPlay.setSelf(true);
            queryPlay.getSqlMap().put("dsf", this.getCmsDataScope()
                    + " AND a.typesimg is not null " + " AND b.useable = '" + PrtConstants.ENABLED + "' ");
            Page<PrtInformation> pagePlay = new Page<>();
            pagePlay.setPageSize(5);
            Page<PrtInformation> slider1 = prtInformationService.findPage(pagePlay, queryPlay, loginName);
            resultJson.put("slider1", slider1.getList());
            //3. 广告
            List<PrtInformation> slider2 = Lists.newArrayList();
            resultJson.put("slider2", slider1.getList());
        } else {
            resultJson.put("slider1", Lists.newArrayList());
            resultJson.put("slider2", Lists.newArrayList());
        }

        //4. 最新文章
        PrtInformation queryNew = new PrtInformation();
        Page<PrtInformation> pageNew = new Page<>();
        queryNew.setSelf(true);
        queryNew.getSqlMap().put("dsf", this.getCmsDataScope() + " AND b.useable = '" + PrtConstants.ENABLED + "' ");

        if (StringUtil.isNotBlank(activeChannel)) {
            queryNew.setChannel(new PrtChannel(activeChannel));
            pageNew.setPageSize(20);
        } else {
            pageNew.setPageSize(5);
        }
        Page<PrtInformation> rows = prtInformationService.findPage(pageNew, queryNew, loginName);
        resultJson.put("rows", rows.getList());
        if (rows.getCount() > rows.getList().size()) {
            resultJson.put("nomMore", false);
        } else {
            resultJson.put("nomMore", true);
        }
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setToken(token.get());
        return resultJson;
    }

    private String getCmsDataScope() {
        StringBuilder sqlString = new StringBuilder();

        return sqlString.toString();
    }

    /**
     * 获取新闻列表
     */
    @Operation(summary = "获取新闻列表")
    @RequiresPermissions("user")
    @GetMapping("/getInfoList")
    public ResultJson getInfoList(@RequestParam("activeChannel") String activeChannel, @RequestParam("length") String length) {
        ResultJson resultJson = new ResultJson();
        PrtInformation queryNew = new PrtInformation();
        queryNew.setSelf(true);
        queryNew.getSqlMap().put("dsf", this.getCmsDataScope() + " AND b.useable = '" + PrtConstants.ENABLED + "' ");
        if (StringUtil.isNotBlank(activeChannel)) {
            queryNew.setChannel(new PrtChannel(activeChannel));//栏目
        }
        List<PrtInformation> rows = prtInformationService.findList(queryNew);
        int len = Integer.parseInt(length);
        if ((len + 10) < rows.size()) {
            resultJson.put("rows", rows.subList(len, len + 10));
            resultJson.put("nomMore", false);
        } else if ((len + 10) == rows.size()) {
            resultJson.put("rows", rows.subList(len, len + 10));
            resultJson.put("nomMore", true);
        } else {
            resultJson.put("rows", rows.subList(len, rows.size()));
            resultJson.put("nomMore", true);
        }
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 获取新闻详情
     */
    @Operation(summary = "获取新闻详情")
    @RequiresPermissions("user")
    @GetMapping("/getInfo")
    public ResultJson getInfo(@RequestParam("infoId") String infoId) {
        return ResultJson.success().put("info", prtInformationService.get(infoId));
    }
}
