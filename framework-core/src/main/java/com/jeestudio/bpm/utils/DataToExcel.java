package com.jeestudio.bpm.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: 将数据转成Excel表格
 **/
public class DataToExcel {

    private static final Logger logger = LoggerFactory.getLogger(DataToExcel.class);

    private static final ApplicationHome applicationHome = new ApplicationHome(DataToExcel.class);

    /**
     * Excel表单保护密码，通过 init 方法从配置注入
     */
    private static String excelProtectPassword = "";

    /**
     * 初始化Excel表单保护密码
     * @param password 密码值
     */
    public static void init(String password) {
        excelProtectPassword = password == null ? "" : password;
    }

    private DataToExcel(){}
    /**
     * @description
     * newList 导入到excel中的数据
     * excelTemplatePath 模板路径
     * excelTemplateName 模板名
     * fileName 文件下载名
     * excelIndex 模板工作表下表
     **/
    public static void downloadToExcel(HttpServletResponse response, List<Object> newList,
                                       String excelTemplatePath, String excelTemplateName,
                                       String fileName, Integer excelIndex){
        downloadToExcel(response, newList, excelTemplatePath, excelTemplateName, fileName, excelIndex, null);
    }

    /**
     * @description
     * newList 导入到excel中的数据
     * excelTemplatePath 模板路径
     * excelTemplateName 模板名
     * fileName 文件下载名
     * excelIndex 模板工作表下表
     * startRowNum 指定数据开始的行的下标，默认2
     **/
    public static void downloadToExcel(HttpServletResponse response, List<Object> newList,
                                       String excelTemplatePath, String excelTemplateName,
                                       String fileName, Integer excelIndex, Integer startRowNum){

        //读取项目路径：springboot，读取到target
        applicationHome.getSource().getParentFile().toString();

        //读取项目路径：ssm，读取到指定文件夹
        //String folder = "";
        //request.getSession().getServletContext().getRealPath(folder);

        //excel模板路径
        String inPath = excelTemplatePath;
        String inSplit = "/";
        String inName = excelTemplateName;

        //excel 临时文件存储路径
        String outPath = applicationHome + "/excel/etp/temporary";
        String outSplit = "/";
        String outName = fileName;

        //excel下载名称
        String downloadName = fileName;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        File file = null;
        try {
            //查询数据
            List<Object> listInDb = newList;
            //读取excel模板
            fileInputStream = new FileInputStream(applicationHome + inPath + inSplit + inName);
            //创建工作簿
            XSSFWorkbook xssfWorkbook = createExcel(listInDb, fileInputStream, excelIndex, startRowNum);
            //存储excel到临时文件
            file = new File(outPath + outSplit + outName);

            //处理文件名重复问题
            //文件的完整名称,如spring.jpeg
            String filePath = file.getName();
            String parentPath = file.getParent();
            //文件名,如spring
            String name = filePath.substring(0,filePath.indexOf("."));
            //文件后缀,如.jpeg
            String suffix = filePath.substring(filePath.lastIndexOf("."));
            int i = 1;
            //若文件存在重命名
            while(file.exists()) {
                String newFilename = name+"("+i+")"+suffix;
                file = new File(parentPath+ File.separator+newFilename);
                i++;
            }
            file.createNewFile();  //新建临时文件
            fileOutputStream = new FileOutputStream(file);
            //用文档写输出流
            xssfWorkbook.write(fileOutputStream);
            //刷新输出流
            fileOutputStream.flush();

            //读取excel文件
            InputStream fis = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            response.reset();
            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/x-msdownload");
            response.addHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(downloadName, "UTF-8") + "\"");
            response.addHeader("Content-Length", "" + file.length());
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (Exception e) {
            logger.error("Excel导出失败", e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                } catch (IOException e) {
                    logger.warn("刷新输出流失败", e);
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭输出流失败", e);
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭输入流失败", e);
                }
            }
            //删除临时文件
            if (file != null && file.exists()) {
                 file.delete();
            }
        }
    }


    /**
     * @description 将数据放入临时文件流中
     * listInDb 数据
     * fileInputStream 读入模板文件流
     * excelIndex 模板下标
     **/
    private static XSSFWorkbook createExcel(List<Object> listInDb, FileInputStream fileInputStream, Integer excelIndex, Integer startRowNum) throws IOException {
        //创建工作簿
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);

        //单元格样式
        XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
        //水平居中
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        //垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        //移除其他工作簿
        int index = 0;
        int numberOfSheets = xssfWorkbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            if(i != excelIndex){
                xssfWorkbook.removeSheetAt(index);
            }else {
                index++;
            }
        }

        //读取sheet0
        XSSFSheet sheet0 = xssfWorkbook.getSheetAt(0);
        if (excelProtectPassword != null && !excelProtectPassword.isEmpty()) {
            sheet0.protectSheet(excelProtectPassword); //设置表单保护密码
        }
        Integer cellLength= 1;
        //开始行号，无表头0，有表头1
        int startRowNumber = (startRowNum == null ? 2 : startRowNum);
        if(startRowNumber > 2){
            cellLength = Integer.valueOf(sheet0.getRow(0).getLastCellNum());
        }
        //自动换行
//        sheet0.setColumnWidth(0, 18000);
        cellStyle.setWrapText(true); //设置换行

        //遍历list
        for (int i = 0; i < listInDb.size(); i++) {
            XSSFDrawing p = sheet0.createDrawingPatriarch();
            //创建一行
            XSSFRow rowI = sheet0.createRow(startRowNumber + i);
            //为每一行创建对应的单元格数据以及格式
            if(listInDb.get(i) instanceof List){
                List object = (List)listInDb.get(i);
                if(object!=null){
                    for (int j = 0; j < object.size(); j++) {
                        Object obj = object.get(j);
                        if(obj instanceof String){
                            //创建单元格
                            XSSFCell cell0 = rowI.createCell(j);
                            //设置单元格样式
                            cell0.setCellStyle(cellStyle);
                            //设置单元格内容
                            cell0.setCellValue(obj+"");
                        }else if(obj instanceof Map){
                            Map map = (HashMap<String,String>)obj;
                            //创建单元格
                            XSSFCell cell0 = rowI.createCell(j);
                            //设置单元格样式
                            cell0.setCellStyle(cellStyle);
                            //设置单元格内容
                            cell0.setCellValue(map.get("v")+"");
                            //前四个参数是坐标点,后四个参数是编辑和显示批注时的大小.
                            XSSFComment comment=p.createCellComment(new XSSFClientAnchor(0,0,0,0,(short)cell0.getColumnIndex(),cell0.getRowIndex(),(short)cell0.getColumnIndex() + 5,cell0.getRowIndex() + 6));
                            //输入批注信息
                            comment.setString(new XSSFRichTextString("插件批注成功!"));
                            //添加作者,选中B5单元格,看状态栏
                            comment.setAuthor("Admin");
                            comment.setString(map.get("k")+"");
                            cell0.setCellComment(comment);
//                            XSSFName name = xssfWorkbook.createName();
//                            name.setNameName(map.get("k")+"");
//                            String reference = cell0.getReference();
//                            name.setRefersToFormula(reference);
                        }
                    }
                }
            }else if(listInDb.get(i) instanceof String){
                XSSFRow row0I = sheet0.createRow(i);
                String str = (String)listInDb.get(i);
                //在此处做处理，将需要的数据插入到指定行
                XSSFCell cell = row0I.createCell(0);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(str);
            }else if(listInDb.get(i) instanceof Map){
                Map map = (HashMap<String,String>)listInDb.get(i);
                if(map!=null){
                    AtomicInteger num = new AtomicInteger(0);
                    map.forEach((k,v)->{
                        int j = num.getAndIncrement();
                        //创建单元格
                        XSSFCell cell0 = rowI.createCell(j);
                        //设置单元格样式
                        cell0.setCellStyle(cellStyle);
                        //设置单元格内容
                        cell0.setCellValue(v+"");
                        //前四个参数是坐标点,后四个参数是编辑和显示批注时的大小.
                        XSSFComment comment=p.createCellComment(new XSSFClientAnchor(0,0,0,0,(short)cell0.getColumnIndex(),cell0.getRowIndex(),(short)cell0.getColumnIndex() + 5,cell0.getRowIndex() + 6));
                        //输入批注信息
                        comment.setString(new XSSFRichTextString("插件批注成功!"));
                        //添加作者,选中B5单元格,看状态栏
                        comment.setAuthor("Admin");
                        comment.setString(k+"");
                        cell0.setCellComment(comment);
                        /*XSSFName name = xssfWorkbook.createName();
                        name.setNameName(k+"");
                        String reference = cell0.getReference();
                        name.setRefersToFormula(reference);*/
                    });
                }
            }
        }
        if(startRowNumber == 0){
            //如果存在标题，在此处加上

        }else if(startRowNumber != 2){
            //插入数据到指定位置（思路是模板在指定位置留出一行，要么就是用jar包将指定行数据一个个往下移动）
            sheet0.addMergedRegion(new CellRangeAddress(1, 1, 0, (cellLength == null || cellLength<=0) ? 3 : cellLength-1));
        }
        //返回工作簿
        
        //解锁
        //遍历行row
        XSSFRow sheetRowLength = sheet0.getRow(1);//获取表头的基本数据
        if(sheetRowLength != null && sheetRowLength.getLastCellNum()>0){
            short lastCellNum = sheetRowLength.getLastCellNum();//固定区间
            //设置自动列宽
            /*for (int i = 0; i < lastCellNum; i++) {
                sheet0.autoSizeColumn(i);
                sheet0.setColumnWidth(i,sheet0.getColumnWidth(i)*17/10);
            }*/
            for (int rownum=0;rownum<sheet0.getLastRowNum();rownum++){
                XSSFRow sheetRow = sheet0.getRow(rownum);
                if(sheetRow==null){
                    continue;
                }
                //遍历列cell
                for (int cellnum=0;cellnum<lastCellNum;cellnum++){
                    XSSFCell cell = sheetRow.getCell(cellnum);
                    if(cell==null){
                        cell = sheetRow.createCell(cellnum);
                        cell.getCellStyle().setLocked(false);
                        continue;
                    }
                }
            }
        }
        sheet0.lockFormatColumns(false);
        return xssfWorkbook;
    }

    public static List<Map<String,String>> excelToListData(MultipartFile excelFile) throws IOException {
        //excel 临时文件存储路径
        InputStream inputStream = excelFile.getInputStream();
        //创建工作簿
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
        //读取sheet0
        XSSFSheet sheet0 = xssfWorkbook.getSheetAt(0);

        List<Map<String,String>> list = new ArrayList<>();

        //遍历行row
        XSSFRow sheetRow0 = sheet0.getRow(1);
        if(sheetRow0 != null && sheetRow0.getLastCellNum()>0){
            short lastCellNum = sheetRow0.getLastCellNum();//固定区间
            for (int rownum=2;rownum<sheet0.getLastRowNum();rownum++){
                Map<String,String> tempMap = new HashMap<String,String>();
                XSSFRow sheetRow = sheet0.getRow(rownum);
                if(sheetRow==null || sheetRow.getCell(0) == null || sheetRow.getCell(0).getCellComment() == null){
                    continue;
                }
                tempMap.put("id",sheetRow.getCell(0).getCellComment().getString().getString());
                //遍历列cell
                for (int cellnum=1;cellnum<lastCellNum;cellnum++){
                    XSSFCell cell = sheetRow.getCell(cellnum);
                    if(cell==null || sheetRow0.getCell(cellnum) == null || sheetRow0.getCell(cellnum).getCellComment() == null){
                        continue;
                    }
                    tempMap.put(sheetRow0.getCell(cellnum).getCellComment().getString().getString(),cell.toString());
                }
                list.add(tempMap);
            }
        }
//        if(temporaryFile.exists()){
//            temporaryFile.
//        }else {
//            temporaryFile.createNewFile();
//        }
//        excelFile.transferTo();
        return list;
    }

    public static Boolean checkExcelHead(Row row, int cellNum, String tempStr){
        String head = "";
        for (int i = 0; i < cellNum; i++) {
            Cell cell = row.getCell(i);
            if(cell != null){
                //在读取单元格内容前,设置所有单元格中内容都是字符串类型
                cell.setCellType(CellType.STRING);
                head += cell.getStringCellValue();
            };
        }
        if(StringUtil.compare(head,tempStr) == 0){
            return true;
        };
        return false;
    };

    public static void main(String[] args) throws ParseException {
    }
}
