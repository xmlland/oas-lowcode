package com.jeestudio.eco.water;

import com.alibaba.fastjson.JSON;
import com.jeestudio.eco.water.entity.MonitorValueEntity;
import com.jeestudio.eco.water.entity.StandardEntity;
import com.jeestudio.eco.water.entity.StandardWryEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Description: 水环境评价计算工具
 */
public class Evaluate {
    /**
     * 地表水评价
     * @param dataList 待评价的数据
     * @param isYys 是否为饮用水：1是，空或0为否
     * @return 数据评价结果集
     */
    public static List<DataResult> dbs(List<DataRequest> dataList,String isYys){
        LinkedHashMap<String, DataResult> result=new LinkedHashMap<>();
        List<DataResult> resultList=new ArrayList<>();
        for(DataRequest data:dataList) {
            String sectionCode = data.getSectionCode();
            String sectionType = data.getSectionType();
            String monitorDate = data.getMonitorDate();
            String szmb = data.getSzmb();
            String key = sectionCode + "_" + sectionType + "_" + monitorDate;
            if (!result.containsKey(key)) {
                DataResult dataResult = new DataResult();
                dataResult.setSectionCode(sectionCode);
                dataResult.setSectionType(sectionType);
                dataResult.setMonitorDate(monitorDate);
                dataResult.setSectionName(data.getSectionName());
                dataResult.setAreaName(data.getAreaName());
                dataResult.setRiverName(data.getRiverName());
                dataResult.setExtendData(data.getExtendData());
                dataResult.setSzmb(szmb);
                List<FactorResult> wrwList = new ArrayList<>();
                dataResult.setWrw(wrwList);
                List<FactorResult> wryzList = new ArrayList<>();
                dataResult.setWryz(wryzList);
                List<FactorResult> cbyzList = new ArrayList<>();
                dataResult.setCbyz(cbyzList);
                List<FactorResult> bdckzbList = new ArrayList<>();
                dataResult.setBdkczb(bdckzbList);
                result.put(key, dataResult);
            }
            DataResult dataResult = result.get(key);
            List<FactorResult> wrwList = dataResult.getWrw();
            List<FactorResult> wryzList = dataResult.getWryz();
            List<FactorResult> cbyzList = dataResult.getCbyz();
            List<FactorResult> bdckzbList = dataResult.getBdkczb();

            String itemCode = data.getItemCode();

            // 污染物的评价结果实体
            FactorResult factorResult = new FactorResult();
            factorResult.setItemCode(itemCode);
            factorResult.setExtendData(data.getExtendData());

            if (Standard.itemNameMap.containsKey(itemCode)) {
                //String itemName = Standard.itemNameMap.get(itemCode);
                String itemName = data.getItemName();
                String itemCodeYs = data.getItemCodeYs();
                String itemValueStr = data.getItemValue();
                // 将监测结果字符串转换成数值Map
                MonitorValueEntity monitorValueEntity =  new MonitorValueEntity(itemValueStr);

                factorResult.setItemName(itemName);
                factorResult.setItemCodeYs(itemCodeYs);
                factorResult.setItemValue(monitorValueEntity.getValueStr());

                if (monitorValueEntity.isToDouble()) {
                    double itemValue = monitorValueEntity.getValueDouble();
                    // 污染物的水质类别
                    String itemClass = DataUtil.getDbsItemClass(itemCode, itemValue, sectionType);
                    factorResult.setItemSzlb(itemClass);

                    if("1".equals(isYys)) {
                        // 饮用水化学需氧量不参评，这里屏蔽掉
                        if ("w0101005".equals(itemCode)) {
                            // itemClass = "";
                            factorResult.setBcp("1");
                        }
                    }
                    // 如果是本地扣除指标
                    if("1".equals(data.getBdkczb())){
                        // itemClass = "";
                        factorResult.setBcp("1");
                    }
                    // 总氮 粪大肠不参评
                    if ("w0101009".equals(itemCode)||"w0101025".equals(itemCode)||"bc0103088".equals(itemCode)||"bc0103094".equals(itemCode)) {
                        // itemClass = "";
                        factorResult.setBcp("1");
                    }

                    if (!"".equals(itemClass)) {
                        int itemClassInt = Integer.parseInt(itemClass);  // 污染物的水质类别数值
                        // 处理综合水质
                        if(!"1".equals(factorResult.getBcp())) {  // 标记不参评的指标不能影响水质判定
                            if (dataResult.getSzlb() != null && !"".equals(dataResult.getSzlb())) {
                                int szlb = Integer.parseInt(dataResult.getSzlb());
                                if (itemClassInt > szlb) {
                                    dataResult.setSzlb(itemClass);
                                }
                            } else {
                                dataResult.setSzlb(itemClass);
                            }
                        }

                        // 评价污染因子（超Ⅲ类）
                        if (itemClassInt > 3) {
                            FactorResult factorResult_wryz = new FactorResult();
                            factorResult_wryz.setItemCode(itemCode);
                            factorResult_wryz.setItemName(itemName);
                            factorResult_wryz.setItemCodeYs(itemCodeYs);
                            factorResult_wryz.setItemValue(monitorValueEntity.getValueStr());
                            factorResult_wryz.setItemSzlb(itemClass);
                            factorResult_wryz.setTargetValue(String.valueOf(DataUtil.getDbsStandardValue(itemCode,sectionType,3)));
                            String cbbs = DataUtil.getDbsItemCbbs(itemCode, itemValue, 3, sectionType);
                            factorResult_wryz.setCbbs(cbbs);
                            factorResult_wryz.setBcp(factorResult.getBcp());
                            wryzList.add(factorResult_wryz);
                        }

                        // 评价超标因子（超目标）
                        if (szmb != null && !szmb.isEmpty()) {
                            int szmbInt = Integer.parseInt(szmb);
                            if (szmbInt >= 1 && szmbInt <= 5&&itemClassInt>szmbInt) {
                                String zbxz = data.getZbxz();  // 单指标的限值，-1不参评，空按szmd对应的限值处理，其余非空按给定值作为限值
                                if ("-1".equals(zbxz)) {
                                    // 不参评
                                } else {
                                    FactorResult factorResult_cbyz = new FactorResult();
                                    factorResult_cbyz.setItemCode(itemCode);
                                    factorResult_cbyz.setItemName(itemName);
                                    factorResult_cbyz.setItemCodeYs(itemCodeYs);
                                    factorResult_cbyz.setItemValue(monitorValueEntity.getValueStr());
                                    factorResult_cbyz.setItemSzlb(itemClass);

                                    if (zbxz == null || zbxz.isEmpty()) {
                                        // szmd对应的限值处理
                                        factorResult_cbyz.setTargetValue(String.valueOf(Standard.dbsMap.get(itemCode)[szmbInt - 1]));
                                        String cbbs = DataUtil.getDbsItemCbbs(itemCode, itemValue, szmbInt, sectionType);
                                        factorResult_cbyz.setCbbs(cbbs);
                                    } else {
                                        // 给定值作为限值
                                        factorResult_cbyz.setTargetValue(zbxz);
                                        String cbbs = DataUtil.getDbsItemCbbs(itemCode, itemValue, Double.parseDouble(zbxz));
                                        factorResult_cbyz.setCbbs(cbbs);
                                    }
                                    factorResult_cbyz.setBcp(factorResult.getBcp());
                                    cbyzList.add(factorResult_cbyz);
                                }
                            }

                        } // endif 有目标
                    } // endif 有水质
                    else{
                        // 如果是饮用水，需要判断补充和特定指标是否超标 sijia 2025/4/27补
                        if(!"1".equals(factorResult.getBcp())) {
                            if (szmb != null && !szmb.isEmpty() && "1".equals(isYys) && Standard.dbsBctdMap.containsKey(itemCode)) {
                                double bctd_bzz = Standard.dbsBctdMap.get(itemCode)[0];
                                if (itemValue > bctd_bzz) {
                                    String cbbs = DataUtil.getDbsItemCbbs(itemCode, itemValue, bctd_bzz);
                                    FactorResult factorResult_cbyz = new FactorResult();
                                    factorResult_cbyz.setItemCode(itemCode);
                                    factorResult_cbyz.setItemName(itemName);
                                    factorResult_cbyz.setItemCodeYs(itemCodeYs);
                                    factorResult_cbyz.setItemValue(monitorValueEntity.getValueStr());
                                    factorResult_cbyz.setItemSzlb(itemClass);
                                    factorResult_cbyz.setCbbs(cbbs);
                                    cbyzList.add(factorResult_cbyz);
                                }
                            }
                        }
                    }
                } // endif 监测值有效
            } // endif 属于评价指标
            else{
                factorResult.setItemValue(data.getItemValue());
                factorResult.setItemName(data.getItemName());
                factorResult.setItemCodeYs(data.getItemCodeYs());
            }
            wrwList.add(factorResult);
            dataResult.setWrw(wrwList);
            dataResult.setWryz(wryzList);
            dataResult.setCbyz(cbyzList);
            if("1".equals(data.getBdkczb())){
                bdckzbList.add(factorResult);
            }
            dataResult.setBdkczb(bdckzbList);
            result.put(key, dataResult);
        }
        // 所有数据评价完之后处理定类因子、达标情况
        for (String key : result.keySet()) {
            DataResult dataResult = result.get(key);
            // 处理达标情况
            if(dataResult.getSzmb()!=null&&!"".equals(dataResult.getSzmb())){
                if(dataResult.getSzlb()!=null&&!"".equals(dataResult.getSzlb())) {
                    // 判断超达标时，超标因子的集合中需要排除不参评的指标
                    int cbCount=0;
                    List<FactorResult> cbyzList = dataResult.getCbyz();
                    for(FactorResult cbyz:cbyzList){
                        if(!"1".equals(cbyz.getBcp())){
                            cbCount++;
                        }
                    }
                    if (cbCount > 0) {
                        dataResult.setDbqk("超标");
                    } else {
                        dataResult.setDbqk("达标");
                    }
                }
            }
            // 处理定类因子
            List<FactorResult> dlyzList = new ArrayList<>();
            String szlb= dataResult.getSzlb();
            if(szlb!=null&& !szlb.isEmpty()) {
                int szlbInt=Integer.parseInt(szlb);
                List<FactorResult> wrwList = dataResult.getWrw();
                for(FactorResult wrw:wrwList){
                    String itemClass=wrw.getItemSzlb();
                    if(!"1".equals(wrw.getBcp())){  //过滤标记为不参评的指标
                        if(itemClass!=null&& !itemClass.isEmpty()) {
                            int itemClassInt=Integer.parseInt(itemClass);
                            if(itemClassInt==szlbInt){
                                dlyzList.add(wrw);
                            }
                        }
                    }
                }
            }
            dataResult.setDlyz(dlyzList);
            resultList.add(dataResult);
        }
        if("1".equals(isYys)){
            yysSpe(resultList);
        }
        else{

        }
        return resultList;
    }

    public static List<DataResult> dbs(List<DataRequest> dataList){
        return dbs(dataList,"");
    }

    public static List<DataResult> dbsyys(List<DataRequest> dataList){
        return dbs(dataList,"1");
    }

    /**
     * 地下水评价
     * @param dataList 待评价的数据
     * @param isYys 是否为饮用水：1是，空或0为否
     * @return 数据评价结果集
     */
    public static List<DataResult> dxs(List<DataRequest> dataList,String isYys){
        LinkedHashMap<String, DataResult> result=new LinkedHashMap<>();
        List<DataResult> resultList=new ArrayList<>();
        for(DataRequest data:dataList) {
            String sectionCode = data.getSectionCode();
            String sectionType = "DXS";
            String monitorDate = data.getMonitorDate();
            String szmb = data.getSzmb();
            String key = sectionCode + "_" + sectionType + "_" + monitorDate;
            if (!result.containsKey(key)) {
                DataResult dataResult = new DataResult();
                dataResult.setSectionCode(sectionCode);
                dataResult.setSectionType(sectionType);
                dataResult.setMonitorDate(monitorDate);
                dataResult.setSectionName(data.getSectionName());
                dataResult.setAreaName(data.getAreaName());
                dataResult.setRiverName(data.getRiverName());
                dataResult.setExtendData(data.getExtendData());
                dataResult.setSzmb(szmb);
                List<FactorResult> wrwList = new ArrayList<>();
                dataResult.setWrw(wrwList);
                List<FactorResult> wryzList = new ArrayList<>();
                dataResult.setWryz(wryzList);
                List<FactorResult> cbyzList = new ArrayList<>();
                dataResult.setCbyz(cbyzList);
                List<FactorResult> bdckzbList = new ArrayList<>();
                dataResult.setBdkczb(bdckzbList);
                result.put(key, dataResult);
            }
            DataResult dataResult = result.get(key);
            List<FactorResult> wrwList = dataResult.getWrw();
            List<FactorResult> wryzList = dataResult.getWryz();
            List<FactorResult> cbyzList = dataResult.getCbyz();
            List<FactorResult> bdckzbList = dataResult.getBdkczb();

            String itemCode = data.getItemCode();

            // 污染物的评价结果实体
            FactorResult factorResult = new FactorResult();
            factorResult.setItemCode(itemCode);
            factorResult.setExtendData(data.getExtendData());

            if (Standard.itemNameMap.containsKey(itemCode)) {
//                String itemName = Standard.itemNameMap.get(itemCode);
                String itemName = data.getItemName();
                String itemCodeYs = data.getItemCodeYs();
                String itemValueStr = data.getItemValue();
                // 将监测结果字符串转换成数值Map
                MonitorValueEntity monitorValueEntity = new MonitorValueEntity(itemValueStr);

                factorResult.setItemName(itemName);
                factorResult.setItemCodeYs(itemCodeYs);
                factorResult.setItemValue(monitorValueEntity.getValueStr());

                if (monitorValueEntity.isToDouble()) {
                    double itemValue = monitorValueEntity.getValueDouble();
                    // 污染物的水质类别
                    String itemClass = DataUtil.getDxsItemClass(itemCode, itemValue, itemValueStr);
                    factorResult.setItemSzlb(itemClass);

                    // 如果是本地扣除指标，水质改为空（废弃）
                    if("1".equals(data.getBdkczb())){
                        //itemClass = "";
                        factorResult.setBcp("1");
                    }

                    if (!"".equals(itemClass)) {
                        int itemClassInt = Integer.parseInt(itemClass);  // 污染物的水质类别数值
                        // 处理综合水质
                        if(!"1".equals(factorResult.getBcp())) {  // 标记不参评的指标不能影响水质判定
                            if (dataResult.getSzlb() != null && !"".equals(dataResult.getSzlb())) {
                                int szlb = Integer.parseInt(dataResult.getSzlb());
                                if (itemClassInt > szlb) {
                                    dataResult.setSzlb(itemClass);
                                }
                            } else {
                                dataResult.setSzlb(itemClass);
                            }
                        }

                        // 评价污染因子（超Ⅲ类）
                        if (itemClassInt > 3) {
                            FactorResult factorResult_wryz = new FactorResult();
                            factorResult_wryz.setItemCode(itemCode);
                            factorResult_wryz.setItemName(itemName);
                            factorResult_wryz.setItemCodeYs(itemCodeYs);
                            factorResult_wryz.setItemValue(monitorValueEntity.getValueStr());
                            factorResult_wryz.setItemSzlb(itemClass);
                            factorResult_wryz.setTargetValue(String.valueOf(Standard.dxsMap.get(itemCode)[2]));
                            factorResult_wryz.setBcp(factorResult.getBcp());
                            String cbbs = DataUtil.getDxsItemCbbs(itemCode, itemValue, 3);
                            factorResult_wryz.setCbbs(cbbs);
                            wryzList.add(factorResult_wryz);
                        }

                        // 评价超标因子（超目标）
                        if (szmb != null && !szmb.isEmpty()) {
                            int szmbInt = Integer.parseInt(szmb);
                            if (szmbInt >= 1 && szmbInt <= 4&&itemClassInt>szmbInt) {
                                String zbxz = data.getZbxz();  // 单指标的限值，-1不参评，空按szmd对应的限值处理，其余非空按给定值作为限值
                                if ("-1".equals(zbxz)) {
                                    // 不参评
                                } else {
                                    FactorResult factorResult_cbyz = new FactorResult();
                                    factorResult_cbyz.setItemCode(itemCode);
                                    factorResult_cbyz.setItemName(itemName);
                                    factorResult_cbyz.setItemCodeYs(itemCodeYs);
                                    factorResult_cbyz.setItemValue(monitorValueEntity.getValueStr());
                                    factorResult_cbyz.setItemSzlb(itemClass);

                                    if (zbxz == null || zbxz.isEmpty()) {
                                        // szmd对应的限值处理
                                        factorResult_cbyz.setTargetValue(String.valueOf(Standard.dxsMap.get(itemCode)[szmbInt - 1]));
                                        String cbbs = DataUtil.getDxsItemCbbs(itemCode, itemValue, szmbInt);
                                        factorResult_cbyz.setCbbs(cbbs);
                                    } else {
                                        // 给定值作为限值
                                        factorResult_cbyz.setTargetValue(zbxz);
                                        String cbbs = DataUtil.getDxsItemCbbs(itemCode, itemValue, Double.parseDouble(zbxz));
                                        factorResult_cbyz.setCbbs(cbbs);
                                    }
                                    factorResult_cbyz.setBcp(factorResult.getBcp());
                                    cbyzList.add(factorResult_cbyz);
                                }
                            }
                        }
                    }
                }
            }
            else{
                factorResult.setItemValue(data.getItemValue());
                factorResult.setItemName(data.getItemName());
                factorResult.setItemCodeYs(data.getItemCodeYs());
            }
            wrwList.add(factorResult);
            dataResult.setWrw(wrwList);
            dataResult.setWryz(wryzList);
            dataResult.setCbyz(cbyzList);
            if("1".equals(data.getBdkczb())){
                bdckzbList.add(factorResult);
            }
            dataResult.setBdkczb(bdckzbList);
            result.put(key, dataResult);
        }
        // 所有数据评价完之后处理定类因子、达标情况
        for (String key : result.keySet()) {
            DataResult dataResult = result.get(key);
            // 处理达标情况
            if(dataResult.getSzmb()!=null&&!"".equals(dataResult.getSzmb())){
                if(dataResult.getSzlb()!=null&&!"".equals(dataResult.getSzlb())) {
                    // 判断超达标时，超标因子的集合中需要排除不参评的指标
                    int cbCount=0;
                    List<FactorResult> cbyzList = dataResult.getCbyz();
                    for(FactorResult cbyz:cbyzList){
                        if(!"1".equals(cbyz.getBcp())){
                            cbCount++;
                        }
                    }
                    if (cbCount > 0) {
                        dataResult.setDbqk("超标");
                    } else {
                        dataResult.setDbqk("达标");
                    }
                }
            }
            // 处理定类因子
            List<FactorResult> dlyzList = new ArrayList<>();
            String szlb= dataResult.getSzlb();
            if(szlb!=null&& !szlb.isEmpty()) {
                int szlbInt=Integer.parseInt(szlb);
                List<FactorResult> wrwList = dataResult.getWrw();
                for(FactorResult wrw:wrwList){
                    String itemClass=wrw.getItemSzlb();
                    if(!"1".equals(wrw.getBcp())) {  //过滤标记为不参评的指标
                        if (itemClass != null && !itemClass.isEmpty()) {
                            int itemClassInt = Integer.parseInt(itemClass);
                            if (itemClassInt == szlbInt) {
                                dlyzList.add(wrw);
                            }
                        }
                    }
                }
            }
            dataResult.setDlyz(dlyzList);
            resultList.add(dataResult);
        }
        if("1".equals(isYys)){
            yysSpe(resultList);
        }
        return resultList;
    }

    public static List<DataResult> dxs(List<DataRequest> dataList){
        return dxs(dataList,"");
    }

    public static List<DataResult> dxsyys(List<DataRequest> dataList){
        return dxs(dataList,"1");
    }

    /**
     * 海水水质评价
     * @param dataList
     * @return
     */
    public static List<DataResult> hysz(List<DataRequest> dataList){
        // 读取评价标准
        String json=readJsonFromResources("GB3097-1997.json");
        return custom(dataList,json);
    }

    /**
     * 自定义标准评价
     * @param dataList
     * @param standardJsonStr
     * @return
     */
    public static List<DataResult> custom(List<DataRequest> dataList,String standardJsonStr){
        LinkedHashMap<String, DataResult> result=new LinkedHashMap<>();
        List<DataResult> resultList=new ArrayList<>();
        try {
            // 评价标准
            StandardEntity standardEntity = JSON.parseObject(standardJsonStr, StandardEntity.class);
            for (DataRequest data : dataList) {
                String sectionCode = data.getSectionCode();
                String sectionType = "HY";
                String monitorDate = data.getMonitorDate();
                String szmb = data.getSzmb();
                String key = sectionCode + "_" + sectionType + "_" + monitorDate;
                if (!result.containsKey(key)) {
                    DataResult dataResult = new DataResult();
                    dataResult.setSectionCode(sectionCode);
                    dataResult.setSectionType(sectionType);
                    dataResult.setMonitorDate(monitorDate);
                    dataResult.setSectionName(data.getSectionName());
                    dataResult.setAreaName(data.getAreaName());
                    dataResult.setRiverName(data.getRiverName());
                    dataResult.setExtendData(data.getExtendData());
                    dataResult.setSzmb(szmb);
                    List<FactorResult> wrwList = new ArrayList<>();
                    dataResult.setWrw(wrwList);
                    List<FactorResult> wryzList = new ArrayList<>();
                    dataResult.setWryz(wryzList);
                    List<FactorResult> cbyzList = new ArrayList<>();
                    dataResult.setCbyz(cbyzList);
                    List<FactorResult> bdckzbList = new ArrayList<>();
                    dataResult.setBdkczb(bdckzbList);
                    result.put(key, dataResult);
                }
                DataResult dataResult = result.get(key);
                List<FactorResult> wrwList = dataResult.getWrw();
                List<FactorResult> wryzList = dataResult.getWryz();
                List<FactorResult> cbyzList = dataResult.getCbyz();
                List<FactorResult> bdckzbList = dataResult.getBdkczb();

                String itemCode = data.getItemCode();
                // 污染物的评价结果实体
                FactorResult factorResult = new FactorResult();
                factorResult.setItemCode(itemCode);

                // 判断该指标是否有对应的评价标准
                List<StandardWryEntity> standardWryEntities = standardEntity.getWrw();
                Optional<StandardWryEntity> adult = standardWryEntities.stream()
                        .filter(standardWry -> itemCode.equals(standardWry.getWrwbm()))
                        .findFirst();
                if (adult.isPresent()) {
                    // 该指标有对应的标准 开始评价
                    StandardWryEntity standardWry=adult.get();

                    String itemName = data.getItemName();
                    String itemCodeYs = data.getItemCodeYs();
                    String itemValueStr = data.getItemValue();
                    // 将监测结果字符串转换成数值Map
                    MonitorValueEntity monitorValueEntity = new MonitorValueEntity(itemValueStr);

                    factorResult.setItemName(itemName);
                    factorResult.setItemCodeYs(itemCodeYs);
                    factorResult.setItemValue(monitorValueEntity.getValueStr());

                    if (monitorValueEntity.isToDouble()) {
                        double itemValue = monitorValueEntity.getValueDouble();
                        // 污染物的水质类别
                        String itemClass = DataUtil.getItemClass(itemValue, standardWry);
                        // 如果是本地扣除指标，水质改为空
                        if("1".equals(data.getBdkczb())){
                            itemClass = "";
                            factorResult.setBcp("1");
                        }
                        factorResult.setItemSzlb(itemClass);

                        if (!"".equals(itemClass)) {
                            int itemClassInt = Integer.parseInt(itemClass);  // 污染物的水质类别数值
                            // 处理综合水质
                            if(dataResult.getSzlb()!=null&&!"".equals(dataResult.getSzlb())){
                                int szlb=Integer.parseInt(dataResult.getSzlb());
                                if(itemClassInt>szlb){
                                    dataResult.setSzlb(itemClass);
                                }
                            }
                            else{
                                dataResult.setSzlb(itemClass);
                            }

                            // 评价超标因子（超目标）
                            if (szmb != null && !szmb.isEmpty()) {
                                int szmbInt = Integer.parseInt(szmb);
                                if (szmbInt >= 1 && itemClassInt>szmbInt) {
                                    String zbxz = data.getZbxz();  // 单指标的限值，-1不参评，空按szmd对应的限值处理，其余非空按给定值作为限值
                                    if ("-1".equals(zbxz)) {
                                        // 不参评
                                    } else {
                                        FactorResult factorResult_cbyz = new FactorResult();
                                        factorResult_cbyz.setItemCode(itemCode);
                                        factorResult_cbyz.setItemName(itemName);
                                        factorResult_cbyz.setItemCodeYs(itemCodeYs);
                                        factorResult_cbyz.setItemValue(monitorValueEntity.getValueStr());
                                        factorResult_cbyz.setItemSzlb(itemClass);

                                        if (zbxz == null || "".equals(zbxz)) {
                                            // szmd对应的限值处理
                                            factorResult_cbyz.setTargetValue(DataUtil.getItemTargetValue(szmbInt,standardWry));
                                            factorResult_cbyz.setCbbs(DataUtil.getItemCbbs(itemValue,szmbInt,standardWry));
                                        } else {
                                            // 给定值作为限值
                                            factorResult_cbyz.setTargetValue(zbxz);
                                            String cbbs = DataUtil.getDbsItemCbbs(itemCode, itemValue, Double.parseDouble(zbxz));
                                            factorResult_cbyz.setCbbs(cbbs);
                                        }
                                        cbyzList.add(factorResult_cbyz);
                                    }
                                }

                            } // endif 有目标
                        } // endif 有水质

                    } // endif 监测值有效

                }
                else{
                    factorResult.setItemValue(data.getItemValue());
                    factorResult.setItemName(data.getItemName());
                    factorResult.setItemCodeYs(data.getItemCodeYs());
                }

                wrwList.add(factorResult);
                dataResult.setWrw(wrwList);
                dataResult.setWryz(wryzList);
                dataResult.setCbyz(cbyzList);
                if("1".equals(data.getBdkczb())){
                    bdckzbList.add(factorResult);
                }
                dataResult.setBdkczb(bdckzbList);
                result.put(key, dataResult);

            }

            // 所有数据评价完之后处理定类因子、达标情况
            for (String key : result.keySet()) {
                DataResult dataResult = result.get(key);
                // 处理达标情况
                if(dataResult.getSzmb()!=null&&!"".equals(dataResult.getSzmb())){
                    if(dataResult.getSzlb()!=null&&!"".equals(dataResult.getSzlb())) {
                        if (dataResult.getCbyz().size() > 0) {
                            dataResult.setDbqk("超标");
                        } else {
                            dataResult.setDbqk("达标");
                        }
                    }
                }
                // 处理定类因子
                List<FactorResult> dlyzList = new ArrayList<>();
                String szlb= dataResult.getSzlb();
                if(szlb!=null&& !szlb.isEmpty()) {
                    int szlbInt=Integer.parseInt(szlb);
                    List<FactorResult> wrwList = dataResult.getWrw();
                    for(FactorResult wrw:wrwList){
                        String itemClass=wrw.getItemSzlb();
                        if(!"1".equals(wrw.getBcp())) {  //过滤标记为不参评的指标
                            if (itemClass != null && !"".equals(itemClass)) {
                                int itemClassInt = Integer.parseInt(itemClass);
                                if (itemClassInt == szlbInt) {
                                    dlyzList.add(wrw);
                                }
                            }
                        }
                    }
                }
                dataResult.setDlyz(dlyzList);
                resultList.add(dataResult);
            }

        }
        catch (Exception ex){

        }
        return resultList;
    }

    /**
     * 饮用水地表水评价，在地表水的结果基础上继续追加
     */
    private static void yysSpe(List<DataResult> resultList){
        for(DataResult dataResult:resultList) {
            List<FactorResult> wrwList=dataResult.getWrw();
            if(wrwList!=null) {
                // 初始化重金属检出
                if(dataResult.getZjsjc()==null){
                    List<FactorResult> zjsjc=new ArrayList<>();
                    dataResult.setZjsjc(zjsjc);
                }
                List<FactorResult> zjsjc=dataResult.getZjsjc();
                // 初始化有机物检出
                if(dataResult.getYjwjc()==null){
                    List<FactorResult> yjwjc=new ArrayList<>();
                    dataResult.setYjwjc(yjwjc);
                }
                List<FactorResult> yjwjc=dataResult.getYjwjc();
                // 初始化检出限超标
                if(dataResult.getJcxcb()==null){
                    List<FactorResult> jcxcb=new ArrayList<>();
                    dataResult.setJcxcb(jcxcb);
                }
                List<FactorResult> jcxcb=dataResult.getJcxcb();

                // 三氮关系判定标识
                boolean hasZongdan=false,hasSandan=false;
                double zongdan=0.0,sandan=0.0;

                // 三氧关系判定标识
                boolean hasHxxyl=false,hasGmsyzs=false,hasShxyl=false;
                double hxxyl=0.0,gmsyzs=0.0,shxyl=0.0;

                for(FactorResult factorResult:wrwList) {
                    String itemCode=factorResult.getItemCode();
                    String itemValue=factorResult.getItemValue();
                    if(itemValue!=null&& !itemValue.isEmpty() &&!"-1".equals(itemValue)){
                        // 重金属检出
                        if(Arrays.asList(Standard.zjs).contains(itemCode)){
                            if(!DataUtil.jcxpd(itemValue)){
                                zjsjc.add(factorResult);
                            }
                        }

                        // 有机物检出
                        if(Arrays.asList(Standard.yjw).contains(itemCode)){
                            if(!DataUtil.jcxpd(itemValue)){
                                yjwjc.add(factorResult);
                            }
                        }

                        // 检出限超标：检出限高于三类水
                        if(DataUtil.jcxpd(itemValue)){
                            // 取出检出限，跟三类限值对比
                            String jcxStr=itemValue.replace("l","")
                                    .replace("L","")
                                    .replace("<","")
                                    .replace("＜","")
                                    .replace("-","");
                            try {
                                double jcx = Double.parseDouble(jcxStr);
                                boolean sfcb = false;
                                if (Standard.dbsMap.containsKey(itemCode)) {
                                    double bzxz = Standard.dbsMap.get(itemCode)[2];
                                    if (jcx > bzxz) {
                                        sfcb = true;
                                    }
                                } else if (Standard.dxsMap.containsKey(itemCode)) {
                                    double bzxz = Standard.dxsMap.get(itemCode)[2];
                                    if (jcx > bzxz) {
                                        sfcb = true;
                                    }
                                }
                                if (sfcb) {
                                    jcxcb.add(factorResult);
                                }
                            }
                            catch (Exception ex){
                                // 数值转换可能会出问题，出错忽略
                            }
                        }

                        MonitorValueEntity monitorValueEntity = new MonitorValueEntity(itemValue);

                        if (monitorValueEntity.isToDouble()) {
                            if("w0101009".equals(itemCode)){ // 总氮
                                hasZongdan=true;
                                zongdan=monitorValueEntity.getValueDouble();
                            }
                            else if("w0101007".equals(itemCode)||"w0102003".equals(itemCode)){ // 氨氮+硝酸盐氮
                                hasSandan=true;
                                sandan+=monitorValueEntity.getValueDouble();
                            }
                            else if("w0101005".equals(itemCode)){ // 化学需氧量
                                hasHxxyl=true;
                                hxxyl=monitorValueEntity.getValueDouble();
                            }
                            else if("w0101004".equals(itemCode)){ // 高锰酸盐指数
                                hasGmsyzs=true;
                                gmsyzs=monitorValueEntity.getValueDouble();
                            }
                            else if("w0101006".equals(itemCode)){ // 生化需氧量
                                hasShxyl=true;
                                shxyl=monitorValueEntity.getValueDouble();
                            }
                        }
                    }
                }

                dataResult.setSdgx("√");
                dataResult.setSygx("√");
                // 三氮关系：总氮＞氨氮+硝酸盐氮+亚硝酸盐氮
                if(hasZongdan&&hasSandan){
                    if(sandan>=zongdan){
                        dataResult.setSdgx("×");
                    }
                }
                // 三氧关系：化学需氧量＞高锰酸盐指数 和 化学需氧量＞五日生化需氧量
                if(hasHxxyl&&hasGmsyzs){
                    if(gmsyzs>=hxxyl){
                        dataResult.setSygx("×");
                    }
                }
                if(hasHxxyl&&hasShxyl){
                    if(shxyl>=hxxyl){
                        dataResult.setSygx("×");
                    }
                }
            }
        }
    }

    /**
     * 读取json文件
     * @param fileName
     * @return
     */
    private  static String readJsonFromResources(String fileName) {
        try (InputStream inputStream = Evaluate.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                return null;
            }
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            return null;
        }
    }
}
