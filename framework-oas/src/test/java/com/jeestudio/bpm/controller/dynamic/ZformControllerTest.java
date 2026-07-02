package com.jeestudio.bpm.controller.dynamic;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.utils.ResultJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ZformController 单元测试
 * 使用纯 Mockito 测试，不加载 Spring Context
 * 
 * 注意：由于 ZformController 依赖复杂的 Spring Context（Shiro、HttpServletRequest 等），
 * 这里采用反射方式设置 ThreadLocal 变量，模拟用户认证信息
 */
@ExtendWith(MockitoExtension.class)
class ZformControllerTest {

    private static final String TEST_USER_NAME = "test_user";
    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_IP = "127.0.0.1";
    private static final String TEST_TOKEN = "test_token";
    private static final String TEST_FORM_NO = "test_form";

    @Mock
    private ZformService zformService;

    @Mock
    private GenTableService genTableService;

    @Mock
    private SecLogService secLogService;

    @Mock
    private ProjectProperties projectProperties;

    @InjectMocks
    private ZformController zformController;

    @BeforeEach
    void setUp() throws Exception {
        // 通过反射设置 BaseController 中的 ThreadLocal 变量
        setThreadLocalField("currentUserName", TEST_USER_NAME);
        setThreadLocalField("currentUserId", TEST_USER_ID);
        setThreadLocalField("ip", TEST_IP);
        setThreadLocalField("token", TEST_TOKEN);
    }

    /**
     * 通过反射设置 ThreadLocal 字段的值
     */
    private void setThreadLocalField(String fieldName, String value) throws Exception {
        Field field = zformController.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ThreadLocal<String> threadLocal = (ThreadLocal<String>) field.get(zformController);
        if (threadLocal == null) {
            threadLocal = new ThreadLocal<>();
            field.set(zformController, threadLocal);
        }
        threadLocal.set(value);
    }

    // ==================== getZform 测试 ====================

    @Test
    @DisplayName("根据 ID 获取表单数据 - 成功")
    void testGetZform_Success() throws Exception {
        // Arrange
        String formNo = TEST_FORM_NO;
        String id = "zform_001";
        
        GenTable genTable = new GenTable();
        genTable.setName(formNo);
        
        Zform zform = new Zform();
        zform.setId(id);
        zform.setFormNo(formNo);

        when(genTableService.getGenTableWithDefination(formNo)).thenReturn(genTable);
        when(zformService.get(eq(id), eq(TEST_USER_NAME), eq(genTable))).thenReturn(zform);

        // Act
        ResultJson result = zformController.getZform(formNo, id);

        // Assert
        assertThat(result.getCode()).isEqualTo(ResultJson.CODE_SUCCESS);
        assertThat(result.getData()).containsKey("data");
        
        verify(genTableService).getGenTableWithDefination(formNo);
        verify(zformService).get(id, TEST_USER_NAME, genTable);
    }

    // ==================== getZformMap 测试 ====================

    @Test
    @DisplayName("获取表单 Map 数据 - 成功")
    void testGetZformMap_Success() throws Exception {
        // Arrange
        String formNo = TEST_FORM_NO;
        String id = "zform_001";
        
        GenTable genTable = new GenTable();
        genTable.setName(formNo);
        
        LinkedHashMap<String, Object> zformMap = new LinkedHashMap<>();
        zformMap.put("id", id);
        zformMap.put("name", "Test Form");

        when(genTableService.getGenTableWithDefination(formNo)).thenReturn(genTable);
        when(zformService.getMap(eq(id), eq(genTable), eq(""), eq(TEST_USER_NAME))).thenReturn(zformMap);

        // Act
        ResultJson result = zformController.getZformMap(formNo, id);

        // Assert
        assertThat(result.getCode()).isEqualTo(ResultJson.CODE_SUCCESS);
        assertThat(result.getData()).containsKey("data");
        
        verify(genTableService).getGenTableWithDefination(formNo);
        verify(zformService).getMap(id, genTable, "", TEST_USER_NAME);
    }

    // ==================== save 测试 ====================

    @Test
    @DisplayName("保存表单数据 - 成功")
    void testSave_Success() throws Exception {
        // Arrange
        Zform zform = new Zform();
        zform.setFormNo(TEST_FORM_NO);
        zform.setId("new_zform_001");

        ResultJson expectedResult = ResultJson.success("保存成功");
        expectedResult.setInsertedId(zform.getId());

        when(zformService.saveZform(eq(zform), eq(TEST_USER_NAME), eq("/dynamic/zform")))
                .thenReturn(expectedResult);
        doNothing().when(secLogService).saveSecLogZform(anyString(), anyString(), anyString(), anyString(), anyString());

        // Act
        ResultJson result = zformController.save(zform);

        // Assert
        assertThat(result.getCode()).isEqualTo(ResultJson.CODE_SUCCESS);
        
        verify(zformService).saveZform(zform, TEST_USER_NAME, "/dynamic/zform");
        verify(secLogService).saveSecLogZform(eq(TEST_USER_NAME), eq(TEST_IP), anyString(), eq(TEST_FORM_NO), anyString());
    }

    @Test
    @DisplayName("保存表单数据 - 服务层抛异常")
    void testSave_ServiceException() throws Exception {
        // Arrange
        Zform zform = new Zform();
        zform.setFormNo(TEST_FORM_NO);

        when(zformService.saveZform(any(Zform.class), eq(TEST_USER_NAME), eq("/dynamic/zform")))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResultJson result = zformController.save(zform);

        // Assert
        assertThat(result.getCode()).isEqualTo(ResultJson.CODE_FAILED);
    }

    // ==================== delete 测试 ====================

    @Test
    @DisplayName("删除表单数据 - 成功")
    void testDelete_Success() throws Exception {
        // Arrange
        String formNo = TEST_FORM_NO;
        String ids = "id1,id2,id3";
        
        GenTable genTable = new GenTable();
        genTable.setName(formNo);

        when(genTableService.getGenTableWithDefination(formNo)).thenReturn(genTable);
        doNothing().when(zformService).deleteAll(eq(ids), eq(formNo), eq(genTable), eq(TEST_USER_NAME));
        doNothing().when(secLogService).saveSecLogZform(anyString(), anyString(), anyString(), anyString(), anyString());

        // Act
        ResultJson result = zformController.delete(formNo, ids);

        // Assert
        assertThat(result.getCode()).isEqualTo(ResultJson.CODE_SUCCESS);
        assertThat(result.getMsg()).contains("删除成功");
        
        verify(genTableService).getGenTableWithDefination(formNo);
        verify(zformService).deleteAll(ids, formNo, genTable, TEST_USER_NAME);
    }

    @Test
    @DisplayName("删除表单数据 - 服务层抛异常")
    void testDelete_ServiceException() throws Exception {
        // Arrange
        String formNo = TEST_FORM_NO;
        String ids = "id1";
        
        GenTable genTable = new GenTable();
        when(genTableService.getGenTableWithDefination(formNo)).thenReturn(genTable);
        doThrow(new RuntimeException("Delete failed")).when(zformService)
                .deleteAll(eq(ids), eq(formNo), eq(genTable), eq(TEST_USER_NAME));

        // Act
        ResultJson result = zformController.delete(formNo, ids);

        // Assert
        assertThat(result.getCode()).isEqualTo(ResultJson.CODE_FAILED);
    }

    // ==================== dataChildren 测试 ====================

    @Test
    @DisplayName("获取子表数据 - 有父ID时返回数据")
    void testDataChildren_WithParentId() throws Exception {
        // Arrange
        String formNo = TEST_FORM_NO;
        String path = "test_path";
        String traceFlag = "0";
        String parentId = "parent_001";
        
        Zform zform = new Zform();
        zform.setFormNo(formNo);
        zform.setPageParam(new PageParam());
        
        GenTable genTable = new GenTable();
        genTable.setName(formNo);
        
        Page<Zform> page = new Page<>();
        List<Zform> zformList = new ArrayList<>();
        Zform child = new Zform();
        child.setId("child_001");
        zformList.add(child);
        page.setList(zformList);

        when(genTableService.getGenTableWithDefination(formNo)).thenReturn(genTable);
        when(zformService.findPage(any(Page.class), eq(zform), eq(path), eq(TEST_USER_NAME), 
                eq(genTable), eq(traceFlag), eq(parentId))).thenReturn(page);

        // Act
        ResultJson result = zformController.dataChildren(zform, path, formNo, traceFlag, parentId);

        // Assert
        assertThat(result.getCode()).isEqualTo(ResultJson.CODE_SUCCESS);
        
        verify(genTableService).getGenTableWithDefination(formNo);
    }

    @Test
    @DisplayName("获取子表数据 - 无父ID时返回空列表")
    void testDataChildren_WithoutParentId() throws Exception {
        // Arrange
        String formNo = TEST_FORM_NO;
        String path = "test_path";
        String traceFlag = "0";
        String parentId = "";  // 空父ID
        
        Zform zform = new Zform();
        zform.setFormNo(formNo);

        // Act
        ResultJson result = zformController.dataChildren(zform, path, formNo, traceFlag, parentId);

        // Assert
        assertThat(result.getCode()).isEqualTo(ResultJson.CODE_SUCCESS);
        assertThat(result.getTotal()).isEqualTo(0L);
        
        // 不应该调用 service
        verify(genTableService, never()).getGenTableWithDefination(anyString());
    }

    // ==================== getZformMapView 测试 ====================

    @Test
    @DisplayName("获取表单 Map 查看数据 - 成功")
    void testGetZformMapView_Success() throws Exception {
        // Arrange
        String formNo = TEST_FORM_NO;
        String id = "zform_001";
        
        GenTable genTable = new GenTable();
        genTable.setName(formNo);
        
        LinkedHashMap<String, Object> zformMap = new LinkedHashMap<>();
        zformMap.put("id", id);
        zformMap.put("status", "published");

        // 使用 lenient 模式因为 currentUserName 可能被用作 guest 模式
        lenient().when(genTableService.getGenTableWithDefination(formNo)).thenReturn(genTable);
        lenient().when(zformService.getMap(eq(id), eq(genTable), eq(GenTable.EXT_FLAG_VIEW), anyString()))
                .thenReturn(zformMap);

        // Act
        ResultJson result = zformController.getZformMapView(formNo, id);

        // Assert
        assertThat(result.getCode()).isEqualTo(ResultJson.CODE_SUCCESS);
    }
}
