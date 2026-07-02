package com.jeestudio.eco.water;

import com.jeestudio.eco.water.entity.MonitorValueEntity;
import com.jeestudio.eco.water.entity.StandardWryEntity;
import com.jeestudio.eco.water.entity.StandardWryxzEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @Description: 水环境评价数据处理工具
 */

public class DataUtil {

    /**
     * 将监测值处理成正确的数值
     * 监测结果：不参评（未检出、-1、空字符、null），
     * 按检出限处理（不等于-1的其他负值、结尾是小写字母l、结尾是大写字母L、开头是＜、开头是<）
     */
    public static Map<String,Object> getMonitorValue(String itemValueStr) {
        MonitorValueEntity entity = getMonitor(itemValueStr);
        Map<String,Object> result=new HashMap<>();
        result.put("valueStr",entity.getValueStr());
        result.put("isToDouble",entity.isToDouble());
        if(entity.isToDouble()){
            result.put("valueDouble",entity.getValueDouble());
        }
        return result;
    }
    /**
     * 将监测值处理成正确的数值
     * 监测结果：不参评（未检出、-1、空字符、null），
     * 按检出限处理（不等于-1的其他负值、结尾是小写字母l、结尾是大写字母L、开头是＜、开头是<）
     */
    public static MonitorValueEntity getMonitor(String itemValueStr) {
        return new MonitorValueEntity(itemValueStr);
    }

    /**
     * 将监测值处理成正确的数值
     * 监测结果：
     * 不参评（未检出、-1、空字符、null），
     * 按检出限处理（不等于-1的其他负值、结尾是小写字母l、结尾是大写字母L、开头是＜、开头是<）、
     * 如果传入的监测结果是“有”或“无”，处理成1或0
     * @param itemValueStr 监测结果
     * @return 监测结果的数值 （Double）
     */
    public static Double getMonitorValueDouble(String itemValueStr) {
        MonitorValueEntity monitorValueEntity = new MonitorValueEntity(itemValueStr);
        return monitorValueEntity.getValueDouble();
    }

    public static BigDecimal getMonitorValueBigDecimal(String itemValueStr) {
        MonitorValueEntity monitorValueEntity = new MonitorValueEntity(itemValueStr);
        return monitorValueEntity.getValueBigDecimal();
    }

    /**
     * 获取地表水指标的水质类别
     */
    public static String getDbsItemClass(String itemCode,double itemValue,String sectionType){
        String waterClass="";
        if(Standard.dbsMap.containsKey(itemCode)){
            if("w0101002".equals(itemCode)){  // pH
                if(itemValue>=6.0&&itemValue<=9.0){
                    waterClass="1";
                }
                else{
                    waterClass="6";
                }
            }
            else if("w0101003".equals(itemCode)){  // 溶解氧
                double[] standard=Standard.dbsMap.get(itemCode);
                if(itemValue>=standard[0]){
                    waterClass="1";
                }
                else if(itemValue>=standard[1]){
                    waterClass="2";
                }
                else if(itemValue>=standard[2]){
                    waterClass="3";
                }
                else if(itemValue>=standard[3]){
                    waterClass="4";
                }
                else if(itemValue>=standard[4]){
                    waterClass="5";
                }
                else{
                    waterClass="6";
                }
            }
            else if("w0101008".equals(itemCode)) {  // 总磷
                if("HK".equals(sectionType.toUpperCase())){  // 总磷湖库
                    if(itemValue<=0.01){
                        waterClass="1";
                    }
                    else if(itemValue<=0.025){
                        waterClass="2";
                    }
                    else if(itemValue<=0.05){
                        waterClass="3";
                    }
                    else if(itemValue<=0.1){
                        waterClass="4";
                    }
                    else if(itemValue<=0.2){
                        waterClass="5";
                    }
                    else{
                        waterClass="6";
                    }
                }
                else{  // 总磷河流
                    if(itemValue<=0.02){
                        waterClass="1";
                    }
                    else if(itemValue<=0.1){
                        waterClass="2";
                    }
                    else if(itemValue<=0.2){
                        waterClass="3";
                    }
                    else if(itemValue<=0.3){
                        waterClass="4";
                    }
                    else if(itemValue<=0.4){
                        waterClass="5";
                    }
                    else{
                        waterClass="6";
                    }
                }
            }
            else{
                double[] standard=Standard.dbsMap.get(itemCode);
                if(itemValue<=standard[0]){
                    waterClass="1";
                }
                else if(itemValue<=standard[1]){
                    waterClass="2";
                }
                else if(itemValue<=standard[2]){
                    waterClass="3";
                }
                else if(itemValue<=standard[3]){
                    waterClass="4";
                }
                else if(itemValue<=standard[4]){
                    waterClass="5";
                }
                else{
                    waterClass="6";
                }
            }
        }
        return waterClass;
    }

    /**
     * 获取地下水指标的水质类别
     */
    public static String getDxsItemClass(String itemCode,double itemValue,String itemValueStr){
        String waterClass="";
        if(Standard.dxsMap.containsKey(itemCode)){
            if("w0201005".equals(itemCode)){  // pH
                if(itemValue>=6.5&&itemValue<=98.5){
                    waterClass="1";
                }
                else if((itemValue>=5.5&&itemValue<6.5)||(itemValue>8.5&&itemValue<=9.0)){
                    waterClass="4";
                }
                else{
                    waterClass="5";
                }
            }
            else if("w0201016".equals(itemCode)){  // 阴离子
                double[] standard=Standard.dxsMap.get(itemCode);
                itemValueStr=itemValueStr==null?"":itemValueStr.trim();
                if(itemValueStr.endsWith("L")||itemValueStr.endsWith("l")||itemValueStr.startsWith("<")||itemValueStr.startsWith("＜")||itemValueStr.startsWith("-")){
                    waterClass="1";
                }
                else if(itemValue<=standard[1]){
                    waterClass="2";
                }
                else if(itemValue<=standard[2]){
                    waterClass="3";
                }
                else if(itemValue<=standard[3]){
                    waterClass="4";
                }
                else{
                    waterClass="5";
                }
            }
            else if("w0201038".equals(itemCode)||"w0201039".equals(itemCode)){
                double[] standard=Standard.dxsMap.get(itemCode);
                if(itemValue<=standard[0]){
                    waterClass="1";
                }
                else if(itemValue<=standard[1]){
                    waterClass="2";
                }
                else if(itemValue<=standard[2]){
                    waterClass="3";
                }
                else{
                    waterClass="4";
                }
            }
            else{
                double[] standard=Standard.dxsMap.get(itemCode);
                if(itemValue<=standard[0]){
                    waterClass="1";
                }
                else if(itemValue<=standard[1]){
                    waterClass="2";
                }
                else if(itemValue<=standard[2]){
                    waterClass="3";
                }
                else if(itemValue<=standard[3]){
                    waterClass="4";
                }
                else{
                    waterClass="5";
                }
            }
        }
        return waterClass;
    }

    /**
     * 获取地表水指标的超标倍数（根据水质标准取对应限值）
     */
    public static String getDbsItemCbbs(String itemCode,double itemValue,int standardLevel,String sectionType){
        String cbbs="";
        if(!"w0101002".equals(itemCode)&&!"w0101003".equals(itemCode)){  // pH或溶解氧不计算超标倍数
            if(standardLevel>0&&standardLevel<6) {
                double[] standard = Standard.dbsMap.get(itemCode);
                if("w0101008".equals(itemCode)){ // 总磷特殊处理
                    if("HK".equals(sectionType.toUpperCase())){
                        standard =new double[]{0.01,0.025,0.05,0.1,0.2};
                    }
                    else{
                        standard =new double[]{0.02,0.1,0.2,0.3,0.4};
                    }
                }
                cbbs = calcCbbs(itemValue,standard[standardLevel - 1]);
            }
        }
        return cbbs;
    }
    /**
     * 获取地下水指标的超标倍数（根据水质标准取对应限值）
     */
    public static String getDxsItemCbbs(String itemCode,double itemValue,int standardLevel){
        String cbbs="";
        if(!"w0201005".equals(itemCode)&&!"w0201002".equals(itemCode)&&!"w0201004".equals(itemCode)){  // pH、嗅和味、肉眼可见物不计算超标倍数
            if(standardLevel>0&&standardLevel<5) {
                double[] standard = Standard.dxsMap.get(itemCode);
                cbbs = calcCbbs(itemValue,standard[standardLevel - 1]);
            }
        }
        return cbbs;
    }
    /**
     * 获取地表水指标的超标倍数（传入限值，直接计算）
     */
    public static String getDbsItemCbbs(String itemCode,double itemValue,double standardValue){
        String cbbs="";
        if(!"w0101002".equals(itemCode)&&!"w0101003".equals(itemCode)) {
            cbbs = calcCbbs(itemValue,standardValue);
        }
        return cbbs;
    }
    /**
     * 获取地下水指标的超标倍数（传入限值，直接计算）
     */
    public static String getDxsItemCbbs(String itemCode,double itemValue,double standardValue){
        String cbbs="";
        if(!"w0201005".equals(itemCode)&&!"w0201002".equals(itemCode)&&!"w0201004".equals(itemCode)){
            cbbs = calcCbbs(itemValue,standardValue);
        }
        return cbbs;
    }

    /**
     * 根据指标编码获取对应的标准值（地表水）
     */
    public static double getDbsStandardValue(String itemCode,String sectionType,int level){
        double[] standard = Standard.dbsMap.get(itemCode);
        if("w0101008".equals(itemCode)){ // 总磷特殊处理
            if("HK".equals(sectionType.toUpperCase())){
                standard =new double[]{0.01,0.025,0.05,0.1,0.2};
            }
            else{
                standard =new double[]{0.02,0.1,0.2,0.3,0.4};
            }
        }
        return standard[level-1];
    }

    /**
     * 判断监测结果是否低于检出限
     */
    public static boolean jcxpd(String itemValueStr){
        boolean result=false;
        if (itemValueStr.endsWith("l") || itemValueStr.endsWith("L")||itemValueStr.startsWith("<") || itemValueStr.startsWith("＜")||(itemValueStr.startsWith("-")&&!"-1".equals(itemValueStr))){
            result=true;
        }
        return result;
    }

    /**
     * 安全获取Map中的String值
     * @param key
     * @param map
     * @return
     */
    public static String getMapValue(String key, Map<String,Object> map){
        return map.containsKey(key)?map.get(key).toString():"";
    }

    /**
     * 从通用标准中获取污染物评价级别（从1开始）
     * @param itemValue
     * @param standardWry
     * @return
     */
    public static String getItemClass(double itemValue, StandardWryEntity standardWry){
        String result="";
        List<StandardWryxzEntity> standardWryxzEntities=standardWry.getWrwxz();
        for(int i=0;i<standardWryxzEntities.size();i++){
            String symbol=standardWryxzEntities.get(i).getSymbol();
            if("<".equals(symbol)||"＜".equals(symbol)){
                double upperLimit=standardWryxzEntities.get(i).getUpperLimit();
                if(itemValue<upperLimit){
                    result=String.valueOf(i+1);
                    break;
                }
                else{
                    if(i==standardWryxzEntities.size()-1){
                        result=String.valueOf(i+2);
                    }
                }
            }
            else if("≤".equals(symbol)||"<=".equals(symbol)){
                double upperLimit=standardWryxzEntities.get(i).getUpperLimit();
                if(itemValue<=upperLimit){
                    result=String.valueOf(i+1);
                    break;
                }
                else{
                    if(i==standardWryxzEntities.size()-1){
                        result=String.valueOf(i+2);
                    }
                }
            }
            else if(">".equals(symbol)||"＞".equals(symbol)){
                double lowerLimit=standardWryxzEntities.get(i).getLowerLimit();
                if(itemValue>lowerLimit){
                    result=String.valueOf(i+1);
                    break;
                }
                else{
                    if(i==standardWryxzEntities.size()-1){
                        result=String.valueOf(i+2);
                    }
                }
            }
            else if("≥".equals(symbol)||">=".equals(symbol)){
                double lowerLimit=standardWryxzEntities.get(i).getLowerLimit();
                if(itemValue>=lowerLimit){
                    result=String.valueOf(i+1);
                    break;
                }
                else{
                    if(i==standardWryxzEntities.size()-1){
                        result=String.valueOf(i+2);
                    }
                }
            }
            else if("~".equals(symbol)||"-".equals(symbol)){
                double upperLimit=standardWryxzEntities.get(i).getUpperLimit();
                double lowerLimit=standardWryxzEntities.get(i).getLowerLimit();
                if(itemValue>=lowerLimit&&itemValue<=upperLimit){
                    result=String.valueOf(i+1);
                    break;
                }
                else{
                    if(i==standardWryxzEntities.size()-1){
                        result=String.valueOf(i+2);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 从通用标准中获取污染物的目标限值
     * @param mb
     * @param standardWry
     * @return
     */
    public static String getItemTargetValue(int mb, StandardWryEntity standardWry){
        String result="";
        List<StandardWryxzEntity> standardWryxzEntities=standardWry.getWrwxz();
        if(standardWryxzEntities.size()<=mb){
            String symbol=standardWryxzEntities.get(mb-1).getSymbol();
            if("~".equals(symbol)||"-".equals(symbol)){
                result = String.valueOf(standardWryxzEntities.get(mb-1).getLowerLimit())
                        +symbol
                        +String.valueOf(standardWryxzEntities.get(mb-1).getUpperLimit());
            }
            else if("<".equals(symbol)||"＜".equals(symbol)||"≤".equals(symbol)||"<=".equals(symbol)){
                result = symbol +String.valueOf(standardWryxzEntities.get(mb-1).getUpperLimit());
            }
            else if(">".equals(symbol)||"＞".equals(symbol)||"≥".equals(symbol)||">=".equals(symbol)){
                result = symbol +String.valueOf(standardWryxzEntities.get(mb-1).getLowerLimit());
            }
        }
        return result;
    }

    /**
     * 从通用标准中获取污染物的超标倍数
     * @param mb
     * @param standardWry
     * @return
     */
    public static String getItemCbbs(double itemValue,int mb, StandardWryEntity standardWry){
        String result="";
        List<StandardWryxzEntity> standardWryxzEntities=standardWry.getWrwxz();
        if(standardWryxzEntities.size()<=mb){
            String symbol=standardWryxzEntities.get(mb-1).getSymbol();
            if("<".equals(symbol)||"＜".equals(symbol)||"≤".equals(symbol)||"<=".equals(symbol)){
                double mdz=standardWryxzEntities.get(mb-1).getUpperLimit();
                double cbbs = (itemValue - mdz)*1.0/mdz;
                if(cbbs>0.0){
                    result=String.format("%.2f", cbbs);
                }
            }
        }
        return result;
    }

    /**
     * 计算超标倍数
     * @param itemValue 监测值
     * @param standard 限值
     * @return
     */
    public static String calcCbbs(double itemValue,double standard){
        BigDecimal standardValue = BigDecimal.valueOf(standard);
        BigDecimal itemValueBigDecimal = BigDecimal.valueOf(itemValue);
        BigDecimal cbbsBigDecimal = itemValueBigDecimal.subtract(standardValue).divide(standardValue, 10, RoundingMode.HALF_EVEN);

        if(BigDecimal.ZERO.compareTo(cbbsBigDecimal) < 0){
            return cbbsBigDecimal.stripTrailingZeros().toPlainString();
        }

        return "";
    }

}
