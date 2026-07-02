package com.jeestudio.tools.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @Description: Excel动态导出工具
 */

public class ExcelExportUtilDynamic {
	private Sheet sheetAt;
	private CellStyle style;

	private int row = 0;
	private int col = 0;

	public ExcelExportUtilDynamic(Sheet sheetAt, CellStyle style){
		this.sheetAt = sheetAt;
		this.style = style;
	}

	public ExcelExportUtilDynamic setHeadStyle(int height, int width){
		return setHeadStyle(height,width,this.style);
	}

	public ExcelExportUtilDynamic setHeadStyle(int height, int width, CellStyle titleStyle){
		for (int i = 0; i < height; i++) {
			Row row = this.sheetAt.createRow(i);
			for (int j = 0; j < width; j++) {
				Cell cell = row.createCell(j);
				cell.setCellStyle(titleStyle);
			}
		}
		return this;
	}

	public ExcelExportUtilDynamic setContentStyle(int height, int width, CellStyle contentStyle){
		return setContentStyle(this.row,0,height,width,contentStyle);
	}

	public ExcelExportUtilDynamic setContentStyle(int startRow, int height, int width, CellStyle contentStyle){
		return setContentStyle(startRow,0,height,width,contentStyle);
	}

	public ExcelExportUtilDynamic setContentStyle(int startRow, int startRol, int height, int width, CellStyle contentStyle){
		for (int i = startRow; i < startRow + height; i++) {
			Row row = this.sheetAt.createRow(i);
			for (int j = startRol; j < startRol + width; j++) {
				Cell cell = row.createCell(j);
				cell.setCellStyle(contentStyle);
			}
		}
		return this;
	}

	public ExcelExportUtilDynamic startRow(){
		return startRow(1);
	}

	public ExcelExportUtilDynamic startRow(int n){
		this.row+=n;
		this.col=0;
		return this;
	}

	public ExcelExportUtilDynamic addCell(Object value){
		return addCell(value,this.style);
	}

	public ExcelExportUtilDynamic addCell(Object value, CellStyle style){
		ExcelExportUtil.setCellValue(this.sheetAt, row, col, value, style);
		col++;
		return this;
	}

	public ExcelExportUtilDynamic addCell(Object value, int width){
		return addCell(value,1,width,this.style);
	}

	public ExcelExportUtilDynamic addCell(Object value, int height, int width){
		return addCell(value,height,width,this.style);
	}

	public ExcelExportUtilDynamic addCell(Object value, int height, int width, CellStyle style){
		ExcelExportUtil.setCellValue(this.sheetAt, row, col, value, style);
		if(height>1||width>1){
			ExcelExportUtil.addMergedRegion(this.sheetAt,row,row+(height-1),col,col+(width-1));
		}
		col+=width;
		return this;
	}

	public void freezeRow(){
		freezeRow(this.row);
	}

	public void freezeRowPlus(int n){
		freezeRow(this.row+n);
	}

	public void freezeRow(int r){
		this.sheetAt.createFreezePane(0, r, 0, r);
	}

	public Sheet getSheetAt() {
		return sheetAt;
	}

	public void setSheetAt(Sheet sheetAt) {
		this.sheetAt = sheetAt;
	}

	public CellStyle getStyle() {
		return style;
	}

	public void setStyle(CellStyle style) {
		this.style = style;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

}
