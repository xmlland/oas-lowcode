const webConfig = {
    /**
     * 允许上传文件类型
     */
    acceptFiles: ['.jpg', '.png', '.jpeg', '.doc', '.docx', '.xls', '.xlsx', '.pdf', '.zip'],

    maxFileSize: 50,//10M

    /**
     * 行政区
     */
    areaId: '1',

    /**
     * 默认经纬度
     */
    initLng:117.191162109375,
    initLat:39.078369140625,
    /**
     * 默认地图缩放级别
     */
    initZoom:8,
    /**
     * 天地图密钥（从后端接口 /a/getTdtKey 获取）
     */
    tdtKey: '',

    /**
     * 系统名称
     */
    sysTitle: '综合办公平台',
    /**
     * 表格单元格默认值
     */
    cellEmptyDefaultValue: '',

    /**
     * 导入模板放在查询区域
     */
    importTemplateToQuery: false,

    // Workbench home route paths. Sidebar will auto-collapse on these routes.
    workbenchHomePaths: ['/ht/home'],

    theme: {
        theme: 'light', //dark light
        menuLocation: 'mix', // left top mix 系统大于7个字时不要设置left
        primaryColor: '#4b57d5',//全局主色改这里
        queryAreaStyle: 'rtl',// 查询区域风格 ltr  新增导入导出等操作在左侧 rtl 新增导入导出等操作在右侧
        adminLayout: 'modern',// classic modern
        /**
         * 主题颜色
         */
        colorList: [
            {
                key: '薄暮', color: '#F5222D'
            },
            {
                key: '火山', color: '#FA541C'
            },
            {
                key: '日暮', color: '#FAAD14'
            },
            {
                key: '明青', color: '#13C2C2'
            },
            {
                key: '极光绿', color: '#52C41A'
            },
            {
                key: '墨绿（默认）', color: '#4b57d5'
            },
            {
                key: '拂晓蓝', color: '#1890FF'
            },
            {
                key: '极客蓝', color: '#2F54EB'
            },
            {
                key: '酱紫', color: '#722ED1'
            }
        ]
    },
    table: {
        bordered: false,
        //额外减去的高度
        extraDiffHeight: 0,
        //分页配置
        pagination: {
            current: 1,//当前页数
            defaultPageSize: 10,//默认每页条数
            pageSize: 10,//每页条数 初始值需要与defaultPageSize一致
            showQuickJumper: true,//是否可以快速跳转至某页
            showSizeChanger: true,//是否展示 pageSize 切换器
            hideOnSinglePage: false,//此参数废弃 逻辑在SingleTable重新实现
            pageSizeOptions: ['10', '20', '50', '100'],//指定每页可以显示多少条 必须是字符串数组
            // showTotal: (total, range) 	用于显示数据总量和当前数据顺序
            showTotal: (total) => {
                return `共 ${total} 条`
            },
            total: 0
        }
    },
    component: {
        userSelect: {
            componentType: 'default',// default table
        },
        usersSelect: {
            componentType: 'default',// default table
        },
    },
    /**
     * 问题反馈配置
     */
    feedback: {
        button: {
            style: {
                right: '20px',
                bottom: '180px',
            }
        },
        moduleList: ['表单管理', '系统设置', '其他'],
    }
}
window['__webConfig__'] = webConfig
