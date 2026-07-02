package com.jeestudio.bpm.common.around;

import com.jeestudio.bpm.common.entity.gen.GenTable;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * @Description: 子表导出配置
 */
@Data
public class SubTableExportConfig {
	private String where = "";
	private String joinSql = null;
	private List<String> nameList = null;
	private List<String> nameSqlList = null;
	private GenTable parentGenTable;
	private GenTable genTable;
	private String tableName;
	private String tableAs;

	public List<String> sqlNameListChange(String tableAs, String[] tableList, String friendlySql){
		List<String> sqlNameList = new LinkedList<>();
		String[] columnsList = friendlySql.split(",");
		for(String c: columnsList){
			String name =  c.replace("a.",tableAs +".")
					.replace(" AS \""," AS \""+tableAs+"__");
			String[] charList = {" ",",","\n","("};
			for(String t:tableList){
				name = replaceName(name,t,tableAs,charList);
				if(name.length()>(t.length()+2)){
					String n = name.substring(0,t.length()+1);
					if((t+".").equals(n)){
						name = tableAs+"_"+t+"."+name.substring(t.length()+1);
					}
				}
			}
			sqlNameList.add(name);
		}
		return sqlNameList;
	}

	private String replaceName(String name,String t,String tableAs,String[] charList){
		String r = name;
		for(String c:charList){
			r = r.replace(c+t+".",c+tableAs+"_"+t+".");
		}
		return r;
	}

	public String sqlJoinChange(String tableAs, String[] tableList, String joinSql){
		String r = joinSql;
		for(String t:tableList){
			r = r.replace(" a."," "+tableAs+".");
			r = r.replace(" "+t+" ON "," "+tableAs+"_"+t+" ON ");
			r = r.replace(" "+t+" on "," "+tableAs+"_"+t+" ON ");
			r = r.replace(" "+t+"."," "+tableAs+"_"+t+".");
		}
		return r;
	}

	public void toLeftJoinByTableName(String[] tableList){
		String joinSql = sqlJoinChange(tableAs,tableList,this.genTable.getSqlJoinsWithExt());
		List<String> sqlNameList = sqlNameListChange(tableAs,tableList,this.genTable.getSqlColumnsFriendlyWithExt());
		setJoinSql("LEFT JOIN "+this.genTable.getName()+" AS "+tableAs+" ON "+ tableAs +".parent_id = a.id \n"+joinSql);
		setNameSqlList(sqlNameList);
	}
}
