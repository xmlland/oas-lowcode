/**
 * 转为简单列配置
 * @param columns
 * @return {*[]}
 */
export const toSimpleColumns = (columns) => {
    let arr = []
    columns.forEach(item => {
        let col = {fieldTitle: item.title, fieldValue: item.dataIndex, alignEnum: item.align, dictType: item.dict, dictMultiple: item.dictMultiple}
        if (item.children) {
            col.children = toSimpleColumns(item.children);
        }
        arr.push(col)
    })
    return arr
}
/**
 * 构建导出配置数据
 * @param columns 表格列
 * @param subTables 子表
 * @return {{tableColumns: *[], exportWithSubTables: *[]}}
 */
export const buildExportData = (columns, subTables) => {
    let exportWithSubTables = []
    for (let t of subTables) {
        exportWithSubTables.push({
            tableName: t.tableName,
            tableAs: t.tableAs ? t.tableAs : t.tableName,
            orderBy: t.orderBy
        })
        let cols = Array.from(t.columns);
        for (let i of cols) {
            let c = {};
            c = Object.assign(c, i);
            if (Array.isArray(c.dataIndex)) {
                let dataIndex = Array.from(c.dataIndex);
                dataIndex[0] = t.tableAs + '__' + dataIndex[0];
                c.dataIndex = dataIndex;
            } else {
                if ((typeof c.dataIndex) === 'string') {
                    c.dataIndex = t.tableAs + '__' + c.dataIndex;
                }
            }
            columns.push(c)
        }
    }
    let tableColumns = toSimpleColumns(columns);
    return {
        tableColumns,
        exportWithSubTables,
        exportAsTableColumns:true
    }
}
