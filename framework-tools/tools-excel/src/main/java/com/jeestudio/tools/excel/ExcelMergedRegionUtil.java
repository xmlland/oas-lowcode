package com.jeestudio.tools.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Description: Excel合并单元格工具
 */
public class ExcelMergedRegionUtil {
	private final Sheet sheetAt;
	private final List<Integer[]> mergeColumns=new LinkedList<>();
	private final Map<Object,Integer[]> mergeRows=new HashMap<>();
	private final int startIndex;

	public ExcelMergedRegionUtil(Sheet sheetAt, int startIndex) {
		this.sheetAt = sheetAt;
		this.startIndex = startIndex;
	}

	public ExcelMergedRegionUtil setMergeColumns(int n){
		return setMergeColumns(n,n);
	}

	public ExcelMergedRegionUtil setMergeRangeColumns(int start,int end){
		for(int i=start;i<=end;i++){
			setMergeColumns(i);
		}
		return this;
	}

	public ExcelMergedRegionUtil setMergeColumns(int from,int to){
		mergeColumns.add(new Integer[]{from,to});
		return this;
	}

	public ExcelMergedRegionUtil addDataId(Object id,int row){
		if(mergeRows.containsKey(id)){
			Integer[] zoom = mergeRows.get(id);
			zoom[1]+=1;
		}else {
			mergeRows.put(id,new Integer[]{startIndex+row,startIndex+row});
		}
		return this;
	}

	public ExcelMergedRegionUtil merge(){
		mergeRows.forEach((id,row)->{
			mergeColumns.forEach(c ->{
				if(!(row[0].equals(row[1]) && c[0].equals(c[1]))){
					CellStyle cellStyle = sheetAt.getRow(row[0]).getCell(c[0]).getCellStyle();
					cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
					ExcelExportUtil.addMergedRegion(this.sheetAt,row[0],row[1],c[0],c[1]);
				}
			});
		});
		return this;
	}
}

