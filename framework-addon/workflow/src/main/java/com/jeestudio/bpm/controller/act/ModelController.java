package com.jeestudio.bpm.controller.act;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.bpm.service.act.ActModelService;
import com.jeestudio.bpm.service.act.TaskSettingService;
import com.jeestudio.bpm.service.system.OfficeService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 工作流模型
 */
@Tag(name = "工作流模型")
@RestController
@RequestMapping("${adminPath}/service")
public class ModelController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(ModelController.class);

    final String MODEL_ID = "modelId";
    final String MODEL_NAME = "name";
    final String MODEL_REVISION = "revision";
    final String MODEL_DESCRIPTION = "description";

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActModelService actModelService;

    @Autowired
    private OfficeService officeService;

    @Autowired
    private TaskSettingService taskSettingService;

    /**
     * 获取模板集
     */
    @Operation(summary = "获取模板集")
    @RequiresPermissions("user")
    @RequestMapping(value = "/editor/stencilset", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public String getStencilset() throws IOException {
        InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset.json");
        assert stencilsetStream != null;
        String stencilset = IOUtils.toString(stencilsetStream, StandardCharsets.UTF_8);
        stencilset = stencilset.replaceAll("\"", "\\\"");
        return stencilset;
    }

    /**
     * 获取编辑器JSON
     */
    @Operation(summary = "获取编辑器JSON")
    @RequiresPermissions("user")
    @RequestMapping(value = "/model/{modelId}/json", method = RequestMethod.GET, produces = "application/json")
    public ResultJson getEditorJson(@PathVariable String modelId) {
        logger.info("Model ID: " + modelId);
        ResultJson resultJson = new ResultJson();
        ObjectNode modelNode = null;
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
                } else {
                    modelNode = objectMapper.createObjectNode();
                    modelNode.put(MODEL_NAME, model.getName());
                }
                modelNode.put(MODEL_ID, model.getId());
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(
                        new String(repositoryService.getModelEditorSource(model.getId()), StandardCharsets.UTF_8));
                modelNode.put("model", editorJsonNode);

            } catch (Exception e) {
                logger.error("Error while getting editor Json:" + ExceptionUtils.getStackTrace(e));
            }
        } else {
            logger.info("Editor Json model is null, model id:" + modelId);
        }
        resultJson.put("model", modelNode);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 保存模型
     */
    @Operation(summary = "保存模型")
    @RequiresPermissions("user")
    @RequestMapping(value = "/model/{modelId}/save", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public ResultJson saveModel(@PathVariable String modelId, @RequestParam("json_xml") String json_xml, @RequestParam("svg_xml") String svg_xml, @RequestParam("name") String name, @RequestParam("description") String description) throws TranscoderException, IOException {
        Model model = repositoryService.getModel(modelId);
        ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
        modelJson.put(MODEL_NAME, name);
        modelJson.put(MODEL_DESCRIPTION, description);
        model.setMetaInfo(modelJson.toString());
        model.setName(name);
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(), json_xml.getBytes(StandardCharsets.UTF_8));
        ObjectNode jsonNodes = (ObjectNode) objectMapper.readTree(json_xml);
        taskSettingService.updateTaskName(jsonNodes);
        InputStream svgStream = new ByteArrayInputStream(svg_xml.getBytes(StandardCharsets.UTF_8));
        TranscoderInput input = new TranscoderInput(svgStream);
        PNGTranscoder transcoder = new PNGTranscoder();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outStream);
        transcoder.transcode(input, output);
        final byte[] result = outStream.toByteArray();
        repositoryService.addModelEditorSourceExtra(model.getId(), result);
        outStream.close();
        return ResultJson.success("保存模型成功");
    }

    @Operation(summary = "获取模型Map列表")
    @RequiresPermissions("user")
    @PostMapping("/model/listmap")
    public ResultJson listmap(@RequestBody JSONObject zformMap) {
        ResultJson resultJson = ResultJson.success();
        String treeId = "0";
        JSONObject parent = zformMap.getJSONObject("parent");
        if (parent != null) {
            treeId = parent.getString("id");
        }
        String category = zformMap.getString("category");
        String pageNo = zformMap.getJSONObject("pageParam").getString("pageNo");
        String pageSize = zformMap.getJSONObject("pageParam").getString("pageSize");
        LinkedHashMap<String, Object> map = this.getModelListData(category, treeId, pageNo, pageSize);
        if (!map.isEmpty()) {
            if (map.get("rows") != null) {
                resultJson.setRows(map.get("rows"));
            }
            if (map.get("total") != null) {
                resultJson.setTotal(map.get("total"));
            }
        }
        return resultJson;
    }

    /**
     * 获取模型列表
     */
    @Operation(summary = "获取模型列表")
    @RequiresPermissions("user")
    @RequestMapping(value = "/model/list", produces = "application/json")
    public ResultJson getModelList(String category, String treeId, String pageNo, String pageSize) {
        ResultJson resultJson = ResultJson.success();
        LinkedHashMap<String, Object> map = this.getModelListData(category, treeId, pageNo, pageSize);
        if (!map.isEmpty()) {
            if (map.get("rows") != null) {
                resultJson.setRows(map.get("rows"));
            }
            if (map.get("total") != null) {
                resultJson.setTotal(map.get("total"));
            }
        }
        resultJson.setData(map);
        return resultJson;
    }

    private LinkedHashMap<String, Object> getModelListData(String category,
                                                           String treeId,
                                                           String pageNo,
                                                           String pageSize) {
        LinkedHashMap<String, Object> map = Maps.newLinkedHashMap();
        try {
            Page<Model> page = new Page<>();
            page.setPageNo(Integer.parseInt(pageNo));
            page.setPageSize(Integer.parseInt(pageSize));
            page = actModelService.modelList(page, category, treeId);
            List<Model> modelList = page.getList();
            long modelListCount = page.getCount();

            List<Map<String, Object>> dataList = Lists.newArrayList();
            for (org.flowable.engine.repository.Model m : modelList) {
                Map<String, Object> modelMap = Maps.newHashMap();
                modelMap.put("id", m.getId());
                modelMap.put("category", m.getCategory());
                modelMap.put("category_name", m.getCategory());
                modelMap.put("key", m.getKey());
                modelMap.put("name", m.getName());
                modelMap.put("version", m.getVersion());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                modelMap.put("createTime", simpleDateFormat.format(m.getCreateTime()));
                modelMap.put("lastUpdateTime", simpleDateFormat.format(m.getLastUpdateTime()));
                modelMap.put("metaInfo", new Gson().fromJson(m.getMetaInfo(), Map.class));
                if (StringUtils.isNotBlank(m.getTenantId())) {
                    modelMap.put("officeName", officeService.get(m.getTenantId()).getName());
                }
                dataList.add(modelMap);
            }

            map.put("rows", dataList);
            map.put("total", modelListCount);
        } catch (Exception e) {
            logger.error("Error while getting model list:" + ExceptionUtils.getStackTrace(e));
        }
        return map;
    }

    /**
     * 创建模型
     */
    @Operation(summary = "创建模型")
    @RequiresPermissions("user")
    @RequestMapping(value = "/model/create", method = RequestMethod.GET, produces = "application/json")
    public ResultJson createModel(String name, String key, String description, String category, String scope, String officeId) throws UnsupportedEncodingException {
        actModelService.create(name, key, description, category, scope, officeId);
        return ResultJson.success();
    }

    /**
     * 获取模型数据
     */
    @Operation(summary = "获取模型数据")
    @RequiresPermissions("user")
    @GetMapping("/model/getModelData")
    public ResultJson getModelData(@RequestParam("formNo") String formNo, @RequestParam("id") String id) {
        return ResultJson.success().setData(this.getModelDataMap(id));
    }

    /**
     * 获取模型数据列表
     */
    @Operation(summary = "获取模型数据列表")
    @RequiresPermissions("user")
    @RequestMapping(value = "/model/data", method = RequestMethod.GET, produces = "application/json")
    public ResultJson getModelData(String modelId) {
        return ResultJson.success().setData(this.getModelDataMap(modelId));
    }

    private LinkedHashMap<String, Object> getModelDataMap(String modelId) {
        org.flowable.engine.repository.Model m = actModelService.get(modelId);
        LinkedHashMap<String, Object> modelMap = Maps.newLinkedHashMap();
        modelMap.put("id", m.getId());
        modelMap.put("category", m.getCategory());
        modelMap.put("key", m.getKey());
        modelMap.put("name", m.getName());
        modelMap.put("version", m.getVersion());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        modelMap.put("createTime", simpleDateFormat.format(m.getCreateTime()));
        modelMap.put("lastUpdateTime", simpleDateFormat.format(m.getLastUpdateTime()));
        modelMap.put("metaInfo", new Gson().fromJson(m.getMetaInfo(), Map.class));
        if (StringUtils.isNotBlank(m.getTenantId())) {
            Office office = new Office();
            office.setId(m.getTenantId());
            office.setName(officeService.get(m.getTenantId()).getName());
            modelMap.put("office", office);
            modelMap.put("officeId", m.getTenantId());
            modelMap.put("officeName", officeService.get(m.getTenantId()).getName());
        } else {
            modelMap.put("office", null);
            modelMap.put("officeId", "");
            modelMap.put("officeName", "");
        }
        return modelMap;
    }

    /**
     * 更新模型
     */
    @Operation(summary = "更新模型")
    @RequiresPermissions("user")
    @PostMapping("/model/updateModel")
    public ResultJson updateModel2(@RequestBody JSONObject map) throws UnsupportedEncodingException {
        String modelId = map.getString("id");
        String scope = map.getJSONObject("metaInfo").getString("scope");
        String officeId = map.getJSONObject("office").getString("id");
        if (StringUtil.isEmpty(modelId)) {
            String name = map.getString("name");
            String description = map.getString("description");
            String category = map.getString("category");
            if(StringUtil.isEmpty(category)) {
                category = map.getString("new_category");
            }
            actModelService.create(name, null, description, category, scope, officeId);
        } else {
            actModelService.update(modelId, scope, officeId);
        }
        return ResultJson.success();
    }

    /**
     * 更新模型版本
     */
    @Operation(summary = "更新模型版本")
    @RequiresPermissions("user")
    @RequestMapping(value = "/model/update", method = RequestMethod.GET, produces = "application/json")
    public ResultJson updateModel(String modelId, String scope, String officeId) {
        actModelService.update(modelId, scope, officeId);
        return ResultJson.success();
    }

    /**
     * 部署模型
     */
    @Operation(summary = "部署模型")
    @RequiresPermissions("user")
    @RequestMapping(value = "/model/deploy", method = RequestMethod.GET, produces = "application/json")
    public ResultJson deployModel(String id) {
        actModelService.deploy(id);
        return ResultJson.success();
    }

    /**
     * 删除模型
     */
    @Operation(summary = "删除模型")
    @RequiresPermissions("user")
    @RequestMapping(value = "/model/delete", method = RequestMethod.GET, produces = "application/json")
    public ResultJson deleteModel(String id) {
        actModelService.delete(id);
        return ResultJson.success();
    }
}
