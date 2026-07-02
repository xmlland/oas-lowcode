package com.jeestudio.bpm.utils;


import com.alibaba.fastjson.JSONObject;
import com.google.gson.*;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: JSON转换工具
 */
public class JsonConvertUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonConvertUtil.class);

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"
            , "MMM d, yyyy K:m:s a"};

    /**
     * JSON to object
     */
    public static <T> T jsonToObject(String pojo, Class<T> clazz) {

        return JSONObject.parseObject(pojo, clazz);
    }

    /**
     * map to object
     * {"hello_world":"1","","hello_world2":{"id":"1","name":"2"}} => {"helloWorld":"1","helloWorld2":{"id":"1","name":"2"}}
     */
    public static <T> T mapToCamelCaseToObject(Map<String,Object> map, Class<T> clazz) {
        LinkedHashMap<String, Object> tempMap = new LinkedHashMap<>();
        if ( map != null && !map.isEmpty() ){
            map.keySet().stream().forEach(key -> {
                String camelCaseKey = StringUtil.toCamelCase(key);

                if ( map.get(key) instanceof Map ){
                    Map o = (Map)map.get(key);
                    if ( o.containsKey("id")){
                        tempMap.put(camelCaseKey, o.get("id"));
                    }else {
                        tempMap.put(camelCaseKey, o);
                    }
                }else{
                    tempMap.put(camelCaseKey, map.get(key) );
                }
            });
        }
        return JSONObject.parseObject( JSONObject.toJSONString( tempMap ), clazz);
    }

    /**
     * Object to JSON
     */
    public static <T> String objectToJson(T t) {

        return JSONObject.toJSONString(t);
    }

    /**
     * String to class as: gsonBuilder().fromJson("",Zclass.class)
     */
    public static Gson gsonBuilder() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        String dateStr = json == null ? "" : json.getAsString().replaceAll("\"", "").replace("\u00A0", " ").replace("\u202F", " ");
                        boolean b = dateStr.matches("^[0-9]+$");
                        Date date = null;
                        if (b) {
                            date = new Date(Long.valueOf(dateStr));
                        } else {
                            date = parseDateString(dateStr);
                            if (date != null){
                                return date;
                            }
                            try {
                                date = DateUtils.parseDate(dateStr, parsePatterns);
                            } catch (ParseException e) {
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy K:m:s a", Locale.ENGLISH);
                                try {
                                    date = sdf.parse(dateStr);
                                } catch (ParseException ex) {
                                    logger.debug("日期解析失败, dateStr: {}", dateStr, ex);
                                }
                            }
                        }
                        return date;
                    }
                }).create();
        return gson;
    }

    static String regex = "([A-Za-z]{3}) (\\d{1,2}), (\\d{4}), (\\d{1,2}):(\\d{2}):(\\d{2}) (AM|PM)";
    static Pattern pattern = Pattern.compile(regex);

    public static Date parseDateString(String dateStr) {

        Matcher matcher = pattern.matcher(dateStr);

        if (matcher.find()) {
            String month = matcher.group(1);
            String day = matcher.group(2);
            String year = matcher.group(3);
            String hour = matcher.group(4);
            String minute = matcher.group(5);
            String second = matcher.group(6);
            String period = matcher.group(7);

            String formattedDateStr = String.format("%s %s, %s, %s:%s:%s %s", month, day, year, hour, minute, second, period);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm:ss a", Locale.ENGLISH);
            try {
                Date date = sdf.parse(formattedDateStr);
                return date;
            } catch (ParseException e) {
                logger.debug("日期字符串解析失败, dateStr: {}", dateStr, e);
            }
        }
        return null;
    }
}
