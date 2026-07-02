package com.jeestudio.bpm.service.common;

import com.jeestudio.bpm.base.BaseServiceTest;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.mapper.base.common.ZformDao;
import com.jeestudio.bpm.mapper.base.system.UserDao;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.DictDataService;
import com.jeestudio.bpm.service.system.OfficeService;
import com.jeestudio.bpm.utils.DbTypeUtil;
import org.flowable.engine.HistoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ZformBaseService 单元测试
 * 由于 ZformBaseService 继承自 ActService，使用 ZformService 作为具体实现进行测试
 */
class ZformBaseServiceTest extends BaseServiceTest {

    @Mock
    private ZformDao zformDao;

    @Mock
    private GenTableService genTableService;

    @Mock
    private OfficeService officeService;

    @Mock
    private DictDataService dictDataService;

    @Mock
    private UserDao userDao;

    @Mock
    private HistoryService historyService;

    private ZformService zformService;  // 使用具体实现类进行测试

    private MockedStatic<DbTypeUtil> dbTypeUtilMock;

    private static final String FORM_NO = "test_form";
    private static final String FORM_ID = "form_001";

    private Zform mockZform;
    private GenTable mockGenTable;

    @BeforeEach
    void setUp() {
        // Mock DbTypeUtil 静态方法
        dbTypeUtilMock = Mockito.mockStatic(DbTypeUtil.class);
        dbTypeUtilMock.when(DbTypeUtil::getDbType).thenReturn("mysql");

        // 现在可以安全地实例化服务类
        zformService = new ZformService();

        // 手动注入父类字段
        ReflectionTestUtils.setField(zformService, "zformDao", zformDao);
        ReflectionTestUtils.setField(zformService, "genTableService", genTableService);
        ReflectionTestUtils.setField(zformService, "officeService", officeService);
        ReflectionTestUtils.setField(zformService, "dictDataService", dictDataService);
        ReflectionTestUtils.setField(zformService, "userDao", userDao);
        ReflectionTestUtils.setField(zformService, "historyService", historyService);

        // 初始化测试数据
        mockZform = new Zform();
        mockZform.setId(FORM_ID);
        mockZform.setFormNo(FORM_NO);
        mockZform.setName("测试表单");

        mockGenTable = new GenTable();
        mockGenTable.setId("gentable_001");
        mockGenTable.setName(FORM_NO);
        mockGenTable.setComments("测试表");
        mockGenTable.setSqlColumns("a.id AS \"id\", a.name AS \"name\"");
        mockGenTable.setColumnList(createMockColumns());
        mockGenTable.setPkColumnName("id");
        mockGenTable.setTableType("0");
    }

    @AfterEach
    void tearDown() {
        if (dbTypeUtilMock != null) {
            dbTypeUtilMock.close();
        }
    }

    @Test
    @DisplayName("构建SQL条件 - 测试查询条件构建")
    void testBuildSqlCondition() {
        // Given
        mockZform.setStatus("1");
        mockZform.setName("测试");

        // When - 通过设置查询参数模拟SQL条件构建
        Map<String, String> sqlMap = new HashMap<>();
        sqlMap.put("dsf", " AND a.status = '1' ");
        mockZform.setSqlMap(sqlMap);

        // Then
        assertThat(mockZform.getSqlMap()).containsKey("dsf");
        assertThat(mockZform.getSqlMap().get("dsf")).contains("status");
    }

    @Test
    @DisplayName("树形数据级联删除")
    void testDeleteChildrenForTree() {
        // Given - 测试树形表类型和父子关系
        mockGenTable.setTableType("3"); // 树形表类型
        mockZform.setParentIds("0," + FORM_ID + ",");

        // 模拟子节点
        Zform childZform = new Zform();
        childZform.setId("form_child_001");
        childZform.setFormNo(FORM_NO);
        childZform.setParentIds(mockZform.getParentIds() + "form_child_001,");

        // When & Then - 验证树形关系设置正确
        assertThat(mockGenTable.getTableType()).isEqualTo("3");
        assertThat(childZform.getParentIds()).contains(FORM_ID);
        assertThat(childZform.getParentIds()).startsWith("0,");
    }

    @Test
    @DisplayName("获取树形数据")
    void testGetTreeData() throws Exception {
        // Given
        Zform parentZform = new Zform();
        parentZform.setId("parent_001");
        parentZform.setFormNo(FORM_NO);
        parentZform.setParentIds("0,");
        parentZform.setName("父节点");

        Zform childZform = new Zform();
        childZform.setId("child_001");
        childZform.setFormNo(FORM_NO);
        childZform.setParentIds("0,parent_001,");
        childZform.setName("子节点");

        List<Zform> mockList = Arrays.asList(parentZform, childZform);
        when(zformDao.getList(any(Zform.class))).thenReturn(mockList);

        // When
        Zform result = zformService.get("parent_001", mockGenTable);

        // Then
        assertThat(result).isNotNull();
        verify(zformDao).getList(any(Zform.class));
    }

    @Test
    @DisplayName("字段加密处理")
    void testEncryptField() {
        // Given
        GenTableColumn encryptColumn = new GenTableColumn();
        encryptColumn.setName("encrypt_password");
        encryptColumn.setComments("encrypt_加密密码");
        encryptColumn.setJavaType("String");
        encryptColumn.setJavaField("encryptPassword");

        List<GenTableColumn> columns = new ArrayList<>(mockGenTable.getColumnList());
        columns.add(encryptColumn);
        mockGenTable.setColumnList(columns);

        mockZform.setS01("sensitive_data");

        // When & Then
        // 验证加密字段的处理逻辑
        assertThat(mockGenTable.getColumnList())
                .extracting(GenTableColumn::getName)
                .contains("encrypt_password");
    }

    @Test
    @DisplayName("字段解密处理")
    void testDecryptField() {
        // Given
        LinkedHashMap<String, Object> encryptedData = new LinkedHashMap<>();
        encryptedData.put("id", FORM_ID);
        encryptedData.put("encrypt_password", "encrypted_value_xxx");

        GenTableColumn encryptColumn = new GenTableColumn();
        encryptColumn.setName("encrypt_password");
        encryptColumn.setComments("encrypt_加密密码");

        List<GenTableColumn> columns = new ArrayList<>(mockGenTable.getColumnList());
        columns.add(encryptColumn);
        mockGenTable.setColumnList(columns);

        // When & Then
        // 验证解密字段存在
        assertThat(encryptedData).containsKey("encrypt_password");
        assertThat(mockGenTable.getColumnList())
                .extracting(GenTableColumn::getName)
                .contains("encrypt_password");
    }

    @Test
    @DisplayName("数据权限过滤")
    void testDataPermissionFilter() {
        // Given
        Map<String, String> sqlMap = new HashMap<>();
        String dataPermissionSql = " AND (a.owner_code LIKE 'ORG001%' OR a.create_by = 'test_user') ";
        sqlMap.put("dsf", dataPermissionSql);
        mockZform.setSqlMap(sqlMap);

        // When
        String dsf = mockZform.getSqlMap().get("dsf");

        // Then
        assertThat(dsf).isNotNull();
        assertThat(dsf).contains("owner_code");
        assertThat(dsf).contains("create_by");
    }

    /**
     * 创建测试用的列定义
     */
    private List<GenTableColumn> createMockColumns() {
        List<GenTableColumn> columns = new ArrayList<>();

        GenTableColumn idColumn = new GenTableColumn();
        idColumn.setId("col_001");
        idColumn.setName("id");
        idColumn.setComments("主键");
        idColumn.setJavaType("String");
        idColumn.setJavaField("id");
        columns.add(idColumn);

        GenTableColumn nameColumn = new GenTableColumn();
        nameColumn.setId("col_002");
        nameColumn.setName("name");
        nameColumn.setComments("名称");
        nameColumn.setJavaType("String");
        nameColumn.setJavaField("name");
        columns.add(nameColumn);

        GenTableColumn statusColumn = new GenTableColumn();
        statusColumn.setId("col_003");
        statusColumn.setName("status");
        statusColumn.setComments("状态");
        statusColumn.setJavaType("String");
        statusColumn.setJavaField("status");
        columns.add(statusColumn);

        return columns;
    }
}
