package com.jeestudio.bpm.controller.gen;

import com.jeestudio.bpm.common.entity.gen.AiFormDesignDslGenerateRequest;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignDslGenerateResponse;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialExcelParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialFileParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialRecognizeRequest;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialRecognizeResponse;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialFileParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiModuleFormDslGenerateRequest;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialRecognizeRequest;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialRecognizeResponse;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialUrlParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialUrlParseResponse;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.service.ai.ocr.OcrProviderStatus;
import com.jeestudio.bpm.service.ai.FormDesignAIService;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: AI表单设计
 */
@Tag(name = "AI表单设计")
@RestController
@RequestMapping("${adminPath}/gen/aiFormDesign")
public class AiFormDesignAIController extends BaseController {

    @Autowired
    private FormDesignAIService formDesignAIService;

    @Operation(summary = "AI生成表单设计DSL")
    @RequiresPermissions("user")
    @PostMapping("/generateDsl")
    public ResultJson generateDsl(@RequestBody AiFormDesignDslGenerateRequest request) {
        try {
            AiFormDesignDslGenerateResponse response = formDesignAIService.generateDsl(request, currentUserName.get());
            ResultJson resultJson = ResultJson.success().put("result", response);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "AI识别表单材料")
    @RequiresPermissions("user")
    @PostMapping("/recognizeMaterial")
    public ResultJson recognizeMaterial(@RequestBody AiFormMaterialRecognizeRequest request) {
        try {
            AiFormMaterialRecognizeResponse response = formDesignAIService.recognizeMaterial(request, currentUserName.get());
            ResultJson resultJson = ResultJson.success().put("result", response);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "AI解析Excel并识别表单材料")
    @RequiresPermissions("user")
    @PostMapping("/parseExcelMaterial")
    public ResultJson parseExcelMaterial(@RequestBody AiFormMaterialExcelParseRequest request) {
        try {
            AiFormMaterialRecognizeResponse response = formDesignAIService.parseExcelMaterial(request, currentUserName.get());
            ResultJson resultJson = ResultJson.success().put("result", response);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "AI解析文件并识别表单材料")
    @RequiresPermissions("user")
    @PostMapping("/parseFileMaterial")
    public ResultJson parseFileMaterial(@RequestBody AiFormMaterialFileParseRequest request) {
        try {
            AiFormMaterialRecognizeResponse response = formDesignAIService.parseFileMaterial(request, currentUserName.get());
            ResultJson resultJson = ResultJson.success().put("result", response);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "AI按表单设计DSL生成模块表单")
    @RequiresPermissions("user")
    @PostMapping("/generateModuleFormDsl")
    public ResultJson generateModuleFormDsl(@RequestBody AiModuleFormDslGenerateRequest request) {
        try {
            AiFormDesignDslGenerateResponse response = formDesignAIService.generateModuleFormDsl(request, currentUserName.get());
            ResultJson resultJson = ResultJson.success().put("result", response);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "AI识别模块材料")
    @RequiresPermissions("user")
    @PostMapping("/recognizeModuleMaterial")
    public ResultJson recognizeModuleMaterial(@RequestBody AiModuleMaterialRecognizeRequest request) {
        try {
            AiModuleMaterialRecognizeResponse response = formDesignAIService.recognizeModuleMaterial(request, currentUserName.get());
            ResultJson resultJson = ResultJson.success().put("result", response);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "AI解析文件并识别模块材料")
    @RequiresPermissions("user")
    @PostMapping("/parseFileModuleMaterial")
    public ResultJson parseFileModuleMaterial(@RequestBody AiModuleMaterialFileParseRequest request) {
        try {
            AiModuleMaterialRecognizeResponse response = formDesignAIService.parseFileModuleMaterial(request, currentUserName.get());
            ResultJson resultJson = ResultJson.success().put("result", response);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "提取原型URL材料用于模块材料识别")
    @RequiresPermissions("user")
    @PostMapping("/extractUrlModuleMaterial")
    public ResultJson extractUrlModuleMaterial(@RequestBody AiModuleMaterialUrlParseRequest request) {
        try {
            AiModuleMaterialUrlParseResponse response = formDesignAIService.extractUrlModuleMaterial(request, currentUserName.get());
            ResultJson resultJson = ResultJson.success().put("result", response);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "获取OCR服务商状态")
    @RequiresPermissions("user")
    @PostMapping("/ocrProviderStatus")
    public ResultJson ocrProviderStatus(@RequestBody(required = false) AiFormMaterialFileParseRequest request) {
        try {
            List<OcrProviderStatus> statuses = formDesignAIService.getOcrProviderStatuses(request, currentUserName.get());
            ResultJson resultJson = ResultJson.success().put("result", statuses);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }
}
