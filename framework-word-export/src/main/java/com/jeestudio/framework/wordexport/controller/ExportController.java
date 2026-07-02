package com.jeestudio.framework.wordexport.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.data.ChartMultiSeriesRenderData;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.TableRenderData;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.deepoove.poi.util.TableTools;
import com.jeestudio.framework.wordexport.html.richTextPRenderer;
import com.jeestudio.framework.wordexport.util.TransformationConfigUtil;
import com.jeestudio.framework.wordexport.vo.RequestVo;
import com.jeestudio.tools.base.exceptions.BusinessException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.xmlbeans.XmlOptions;
import org.ddr.poi.html.ElementRenderer;
import org.ddr.poi.html.HtmlRenderConfig;
import org.ddr.poi.html.HtmlRenderPolicy;
import org.ddr.poi.html.util.CSSLength;
import org.ddr.poi.html.util.CSSLengthUnit;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Word 模板上传与导出接口
 */
@RestController
@RequestMapping("/word")
public class ExportController {

    @Value("${templatePath}")
    String templatePath;

    LoopRowTableRenderPolicy rowTableRenderPolicy = new LoopRowTableRenderPolicy();

    /**
     * 上传 Word 模板文件到模板目录。
     */
    @PostMapping("/upload")
    public boolean upload(@RequestParam MultipartFile templateFile) {
        new File(templatePath).mkdirs();
        File file = new File(templatePath + File.separator + templateFile.getOriginalFilename());
        try {
            templateFile.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * 根据模板和数据渲染 Word 文档，返回生成后的文件字节。
     */
    @PostMapping("/export")
    public byte[] export(@RequestParam String fileName, @RequestBody RequestVo requestVo) {
        try {
            Map<String, Object> configMap = requestVo.getConfigMap();
            Map<String, Object> dataMap = requestVo.getDataMap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ConfigureBuilder configureBuilder = Configure.builder();
            for (Map.Entry<String, Object> entry : configMap.entrySet()) {
                if("transformationConfig".equals(entry.getKey())){
                    Object value = entry.getValue();
                    if(value instanceof Map){
                        //  数据名       列名      需要的配置
                        Map<String,Map<String,Map<String,Object>>> transMapConfig = (Map<String,Map<String,Map<String,Object>>>)value;
                        transformationConfig(transMapConfig,dataMap);
                        continue;
                    }
                }
                if ("tableRenderData".equals(entry.getValue())) {
                    String tableValue = Objects.toString(dataMap.get(entry.getKey()));
                    dataMap.put(entry.getKey(), JSON.parseObject(tableValue, TableRenderData.class, Feature.SupportAutoType));
                }
                if ("ChartMultiSeriesRenderData".equals(entry.getValue())) {
                    String tableValue = Objects.toString(dataMap.get(entry.getKey()));
                    dataMap.put(entry.getKey(),JSON.parseObject(tableValue, ChartMultiSeriesRenderData.class, Feature.SupportAutoType));
                }
                if ("rowTableRenderPolicy".equals(entry.getValue())) {
                    configureBuilder.bind(entry.getKey(), rowTableRenderPolicy);
                }
                if ("richTextToHtmlRenderPolicy".equals(entry.getValue())) { // 处理富文本
                    HtmlRenderConfig htmlRenderConfig = new HtmlRenderConfig();
                    ArrayList<ElementRenderer> arrayList = new ArrayList<>();
                    arrayList.add(new richTextPRenderer());
                    htmlRenderConfig.setCustomRenderers(arrayList);
                    htmlRenderConfig.setGlobalFont("宋体");
                    htmlRenderConfig.setGlobalFontSize(new CSSLength(10.5, CSSLengthUnit.PT));
                    HtmlRenderPolicy htmlRenderPolicy = new HtmlRenderPolicy(htmlRenderConfig);
                    configureBuilder.bind(entry.getKey(), htmlRenderPolicy);
                    dataMap.put(entry.getKey(), dataMap.get(entry.getKey()));
                }

            }
            File file = new File(templatePath + File.separator + fileName);
            XWPFTemplate template = XWPFTemplate.compile(file, configureBuilder.useSpringEL(false).build()).render(dataMap);

            //合并单元格-行
            if ("rowspanPolicy".equals(configMap.get("rowsMerge"))) {
                // 获取表格对象
                List<XWPFTable> tables = template.getXWPFDocument().getTables();
                List<List<Map<String, Object>>> rowList = (ArrayList)dataMap.get("rowsMerge");
                for (int i = 0; i < rowList.size(); i++) {
                    //传输的合并表数量大于word里表格数量时判断
                    if(rowList.size() > tables.size()){
                        if (i >= tables.size()){
                            break;
                        }
                    }
                    List<Map<String, Object>> maps = rowList.get(i);
                    for (int j = 0; j < maps.size(); j++) {
                        Map<String, Object> map = maps.get(j);
                        int col = Integer.parseInt(map.get("col").toString());
                        int fromRow = Integer.parseInt(map.get("fromRow").toString());
                        int toRow = Integer.parseInt(map.get("toRow").toString());
                        XWPFTable table = tables.get(i);
                        TableTools.mergeCellsVertically(table, col, fromRow, toRow);
                    }
                }
            }
            template.writeAndClose(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查模板是否存在
     * @param   fileNameList List(String) 文件名列表
     * @return  existFile List(String) 存在的文件(传入，且在文件夹中找到)<br>
     *          notExistFile List(String) 不存在的文件(传入，但在文件夹中未找到)<br>
     *          unknownFile List(String) 未知的文件(未传入，但在文件夹中找到)<br>
     *          <br>
     *          code Integer 结果代码 不为0时为执行错误<br>
     *          msg @Nullable String 执行错误的原因
    * */
    @PostMapping("/checkTemplate")
    public JSONObject checkTemplate(@RequestBody List<String> fileNameList) {
        try {
            JSONObject resultJson = new JSONObject();
            List<String> unknownFile = new LinkedList<>();

            File folder = new File(templatePath);
            File[] files = folder.listFiles();
            if (files == null) {
                throw new BusinessException("未找到模板文件夹：" + templatePath);
            }

            Set<String> existFileNameSet = new HashSet<>();
            Set<String> checkedFileNameSet = new HashSet<>();

            for (File file : files) {
                existFileNameSet.add(file.getName());
                unknownFile.add(file.getName());
            }
            List<String> existFile = new LinkedList<>();
            List<String> notExistFile = new LinkedList<>();

            for (String fileName : fileNameList) {
                if (existFileNameSet.contains(fileName)) {
                    existFile.add(fileName);
                    checkedFileNameSet.add(fileName);
                } else {
                    notExistFile.add(fileName);
                }
            }

            unknownFile.removeAll(checkedFileNameSet);
            resultJson.put("existFile", existFile);
            resultJson.put("notExistFile", notExistFile);
            resultJson.put("unknownFile", unknownFile);
            resultJson.put("code", 0);
            return resultJson;
        } catch (BusinessException e) {
            e.printStackTrace();
            JSONObject resultJson = new JSONObject();
            resultJson.put("code", -1);
            resultJson.put("msg", e.getMessage());
            return resultJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 转换配置
    //                          数据名       列名      需要的配置
    void transformationConfig(Map<String,Map<String,Map<String,Object>>> transMapConfig,Map<String, Object> dataMap){

        transMapConfig.forEach((dataName,dataConfig)->{
            Object data = dataMap.get(dataName);
            if(dataConfig.size()==0 && data instanceof List){
                List<Map<String,Object>> list = (List<Map<String,Object>>) data;
                for (Map<String, Object> map : list) {
                    this.transformationConfig(transMapConfig,map);
                }
            }else if(data instanceof List){
                List<Map<String,Object>> list = (List<Map<String,Object>>) data;
                dataConfig.forEach((columnName,columnConfig)->{
                    for(Map<String,Object> i:list){
                        if(i.containsKey(columnName)){
                            Object d =  i.get(columnName);
                            columnConfig.forEach((configName,configValue)->{
                                // 转换类型
                                if("transType".equals(configName)){
                                    String v  = (String) configValue;
                                    if("picture".equals(v)){
                                        String url = (String) d;
                                        i.put(columnName,TransformationConfigUtil.transToPicture(url,columnConfig));
                                    }
                                    if("multiPicture".equals(v)){
                                        List<String> urlList = (List<String>) d;
                                        List<Map<String,PictureRenderData>> picList = new LinkedList<>();
                                        for(String url:urlList){
                                            picList.add(new HashMap<String,PictureRenderData>(){{
                                                put("picture",TransformationConfigUtil.transToPicture(url,columnConfig));
                                            }});
                                        }
                                        i.put(columnName,picList);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }


    /**
     *多个word合并，按照文件顺序合并为一个word
     * @param fileNames 指定所有需要合并的word文件名
     */
    @PostMapping("/mergeMultiWord")
    public static byte[] mergeMultiWord(@RequestBody List<String> fileNames) {
        try {
            ByteArrayOutputStream dest = new ByteArrayOutputStream();
            List<XWPFDocument> documentList = fileNames.stream()
                    .map(fileName -> {
                        try {
                            FileInputStream in = new FileInputStream(fileName);
                            OPCPackage open = OPCPackage.open(in);
                            return new XWPFDocument(open);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList()); // 将所有文档对象添加到列表中
            XWPFDocument doc;
            if (documentList.isEmpty()) {
                doc = new XWPFDocument(); // 创建一个新的空文档
            } else {
                doc = documentList.get(0); // 获取第一个文档对象
                documentList.stream()
                        .skip(1) // 跳过第一个文档对象
                        .forEach(document -> {
                            try {
                                appendBody(doc, document);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
            doc.write(dest);
            return dest.toByteArray();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
            return null;
        }

    }


    // 该函数用于将源文档对象的内容添加到目标文档对象中
    public static void appendBody( XWPFDocument src, XWPFDocument append ) throws Exception {
        CTBody src1Body = src.getDocument().getBody();
        CTBody src2Body = append.getDocument().getBody();
        List<XWPFPictureData> allPictures = append.getAllPictures();
        // 创建一个映射，用于存储图片的ID变化
        Map<String, String> map = new HashMap<>();
        for( XWPFPictureData picture : allPictures ) {
            String before = append.getRelationId( picture );
            // 将源文档对象中的图片添加到目标文档对象中，并记录图片的新旧ID
            String after = src.addPictureData( picture.getData(), Document.PICTURE_TYPE_PNG );
            map.put( before, after );
        }
        // 将源文档对象的内容添加到目标文档对象中
        appendBody( src1Body, src2Body, map );
    }

    // 该函数用于将源CTBody的内容添加到目标CTBody中
    private static void appendBody(CTBody src, CTBody append, Map<String, String> map ) throws Exception {
        XmlOptions optionsOuter = new XmlOptions();
        optionsOuter.setSaveOuter();
        String appendString = append.xmlText( optionsOuter );
        String rgex = "<[\\s]*?w:sectPr[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?w:sectPr[\\s]*?>";
        appendString = appendString.replaceAll( rgex, "" );
        // 去除分页符
        String srcString = src.xmlText().replaceAll( "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>", "" ).replaceAll( "<w:r><w:br w:type=\"page\"/></w:r>", "" );
        String prefix = srcString.substring( 0, srcString.indexOf( ">" ) + 1 );
        String mainPart = srcString.substring( srcString.indexOf( ">" ) + 1, srcString.lastIndexOf( "<" ) );
        String sufix = srcString.substring( srcString.lastIndexOf( "<" ) );
        String addPart = appendString.substring( appendString.indexOf( ">" ) + 1, appendString.lastIndexOf( "<" ) );
        if( map != null && !map.isEmpty() ) {
            // 对xml字符串中图片ID进行替换
            for( Map.Entry<String, String> set : map.entrySet() ) {
                addPart = addPart.replace( set.getKey(), set.getValue() );
            }
        }
        // 将两个文档的xml内容进行拼接
        CTBody makeBody = CTBody.Factory.parse( prefix + mainPart + addPart + sufix );
        src.set( makeBody );
    }
}
