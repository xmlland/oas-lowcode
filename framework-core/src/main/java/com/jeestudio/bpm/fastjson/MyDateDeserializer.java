package com.jeestudio.bpm.fastjson;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.jeestudio.bpm.utils.DateUtil;
import com.jeestudio.bpm.utils.StringUtil;

import java.lang.reflect.Type;

/**
 * @Description: Fastjson日期反序列化器
 */
public class MyDateDeserializer implements ObjectDeserializer {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Object val = parser.parse();
        if (val == null || StringUtil.isEmpty(val)) {
            return null;
        } else if (val instanceof String || val instanceof Long) {
            String dateStr;
            if (val instanceof Long) {
                dateStr = String.valueOf(val);
            } else {
                dateStr = (String) val;
            }
            return (T) DateUtil.strToDate(dateStr);
        }
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return 2;
    }
}
