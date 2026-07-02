import {language} from 'monaco-editor/esm/vs/basic-languages/sql/sql';
import * as monaco from 'monaco-editor';
import {oneOf} from "@/lib/tools";

const reg1 = new RegExp(`\\s+from\\s+(\\w+)\\s+(\\w+)\\s+(where|left|join|inner)`, 'ig')
const reg2 = new RegExp(`\\s+join\\s+(\\w+)\\s+(\\w+)\\s+on`, 'ig')
export const SQL_COMPLETION_CACHE_PREFIX = '__sql_Completion_MetaData__'
export const sqlCompletionItemProvider = {
    triggerCharacters: ['.', ...language.keywords],
    provideCompletionItems: (model, position) => {

        let dataBaseMetaData = window[SQL_COMPLETION_CACHE_PREFIX + model.id] || {tables: []}

        const {lineNumber, column} = position
        let sql = model.getValue()
        //获取sql中表名与别名的对应关系
        let tableAlias = {}
        let tableNames = dataBaseMetaData.tables.map(item => item.tableName.toLowerCase())

        let regArr = [reg1, reg2]
        //进行正则匹配
        regArr.forEach(reg => {
            let match = null
            // eslint-disable-next-line
            while (match = reg.exec(sql)) {
                let tableName = match[1].toLowerCase()
                let tableAliasName = match[2]
                if (oneOf(tableName, tableNames)) {
                    tableAlias[tableAliasName] = tableName
                }
            }
        })

        const textBeforePointer = model.getValueInRange({
            startLineNumber: lineNumber,
            startColumn: 0,
            endLineNumber: lineNumber,
            endColumn: column,
        })
        const tokens = textBeforePointer.trim().split(/\s+/)
        const lastToken = tokens[tokens.length - 1] // 获取最后一段非空字符串

        // 把内置的关键字数据处理下
        const sqlSuggest = language.keywords.map(item => {
            return {
                label: item,
                kind: monaco.languages.CompletionItemKind.Keyword,
                insertText: item,
                detail: '内置关键字',
            };
        });

        const tableSuggest = dataBaseMetaData.tables.map(item => {
            return {
                label: item.tableName.toLowerCase(),
                kind: monaco.languages.CompletionItemKind.Class,
                insertText: item.tableName.toLowerCase(),
                detail: (item.tableComment || item.tableName).toLowerCase(),
            };
        });

        let suggestions = []

        if (lastToken.endsWith('.')) {
            const tokenNoDot = lastToken.slice(0, lastToken.length - 1)
            if (tableAlias[tokenNoDot]) {
                let arr = []
                dataBaseMetaData.tables.forEach(item => {
                    if (item.tableName === tableAlias[tokenNoDot]) {
                        arr = item.columns.map(col => {
                            return {
                                label: col.columnName.toLowerCase(),
                                kind: monaco.languages.CompletionItemKind.Field,
                                insertText: col.columnName.toLowerCase(),
                                detail: (col.columnComment || col.columnName).toLowerCase(),
                            };
                        });
                    }
                })
                suggestions= arr
            }
        } else if (lastToken === '.') {
            suggestions = []
        } else {

            suggestions = [...tableSuggest, ...sqlSuggest]
        }

        console.log('suggestions', model.id, suggestions)
        return {
            suggestions
        };
    }
}
