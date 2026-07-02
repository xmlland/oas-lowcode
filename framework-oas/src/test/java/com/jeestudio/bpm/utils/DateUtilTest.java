package com.jeestudio.bpm.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * DateUtil 单元测试
 * 测试日期工具类的各种格式化、解析和计算功能
 */
class DateUtilTest {

    // ==================== getDate 测试 ====================

    @Test
    @DisplayName("获取当前日期字符串 - 默认格式 yyyy-MM-dd")
    void testGetDate_DefaultFormat() {
        String date = DateUtil.getDate();
        
        assertThat(date).isNotNull();
        // 格式校验：yyyy-MM-dd
        assertThat(date).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    @DisplayName("获取当前日期字符串 - 自定义格式")
    void testGetDate_CustomPattern() {
        String dateYYYYMMDD = DateUtil.getDate("yyyyMMdd");
        assertThat(dateYYYYMMDD).matches("\\d{8}");

        String dateWithTime = DateUtil.getDateTime();
        // 格式：yyyy-MM-dd HH:mm:ss
        assertThat(dateWithTime).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    // ==================== dateToString / stringToDate 测试 ====================

    @Test
    @DisplayName("日期转字符串 - 正常转换")
    void testDateToString_Normal() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 15, 10, 30, 45);
        Date date = cal.getTime();

        String result = DateUtil.dateToString(date, "yyyy-MM-dd");
        assertThat(result).isEqualTo("2024-03-15");

        String resultWithTime = DateUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss");
        assertThat(resultWithTime).startsWith("2024-03-15 10:30");
    }

    @Test
    @DisplayName("字符串转日期 - 正常转换")
    void testStringToDate_Normal() {
        Date date = DateUtil.stringToDate("2024-03-15", "yyyy-MM-dd");
        
        assertThat(date).isNotNull();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2024);
        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.MARCH);
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);
    }

    @Test
    @DisplayName("字符串转日期 - 无效格式返回 null")
    void testStringToDate_InvalidFormat() {
        Date date = DateUtil.stringToDate("invalid-date", "yyyy-MM-dd");
        assertThat(date).isNull();

        date = DateUtil.stringToDate("2024/03/15", "yyyy-MM-dd");
        assertThat(date).isNull();
    }

    // ==================== parseDate 测试 ====================

    @Test
    @DisplayName("自动解析日期 - 多种格式支持")
    void testParseDate_MultipleFormats() {
        // yyyy-MM-dd 格式
        Date date1 = DateUtil.parseDate("2024-03-15");
        assertThat(date1).isNotNull();

        // yyyy/MM/dd 格式
        Date date2 = DateUtil.parseDate("2024/03/15");
        assertThat(date2).isNotNull();

        // yyyy.MM.dd 格式
        Date date3 = DateUtil.parseDate("2024.03.15");
        assertThat(date3).isNotNull();

        // yyyy-MM-dd HH:mm:ss 格式
        Date date4 = DateUtil.parseDate("2024-03-15 10:30:00");
        assertThat(date4).isNotNull();
    }

    @Test
    @DisplayName("解析日期 - null 输入返回 null")
    void testParseDate_NullInput() {
        Date date = DateUtil.parseDate(null);
        assertThat(date).isNull();
    }

    @Test
    @DisplayName("解析日期 - 无效格式返回 null")
    void testParseDate_InvalidFormat() {
        Date date = DateUtil.parseDate("not-a-date");
        assertThat(date).isNull();
    }

    // ==================== isDate 测试 ====================

    @Test
    @DisplayName("判断是否为日期格式 - 正确格式")
    void testIsDate_ValidFormat() {
        assertThat(DateUtil.isDate("2024-03-15", "yyyy-MM-dd")).isTrue();
        assertThat(DateUtil.isDate("2024/03/15", "yyyy/MM/dd")).isTrue();
        assertThat(DateUtil.isDate("20240315", "yyyyMMdd")).isTrue();
    }

    @Test
    @DisplayName("判断是否为日期格式 - 错误格式")
    void testIsDate_InvalidFormat() {
        assertThat(DateUtil.isDate("2024-13-15", "yyyy-MM-dd")).isFalse();  // 月份超出范围
        assertThat(DateUtil.isDate("invalid", "yyyy-MM-dd")).isFalse();
        assertThat(DateUtil.isDate("2024/03/15", "yyyy-MM-dd")).isFalse();  // 格式不匹配
    }

    // ==================== compareDate 测试 ====================

    @Test
    @DisplayName("比较日期字符串 - 大小关系")
    void testCompareDate_StringDates() {
        // smallDate < largeDate 返回 "1"
        assertThat(DateUtil.compareDate("2024-01-01", "2024-12-31", "yyyy-MM-dd"))
                .isEqualTo("1");
        
        // smallDate > largeDate 返回 "2"
        assertThat(DateUtil.compareDate("2024-12-31", "2024-01-01", "yyyy-MM-dd"))
                .isEqualTo("2");
        
        // smallDate == largeDate 返回 "0"
        assertThat(DateUtil.compareDate("2024-03-15", "2024-03-15", "yyyy-MM-dd"))
                .isEqualTo("0");
    }

    @Test
    @DisplayName("比较 Date 对象 - 大小关系")
    void testCompareDate_DateObjects() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2024, Calendar.JANUARY, 1);
        Date date1 = cal1.getTime();
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.DECEMBER, 31);
        Date date2 = cal2.getTime();

        assertThat(DateUtil.compareDate(date1, date2)).isEqualTo("1");  // date1 < date2
        assertThat(DateUtil.compareDate(date2, date1)).isEqualTo("2");  // date2 > date1
        assertThat(DateUtil.compareDate(date1, date1)).isEqualTo("0");  // 相等
    }

    // ==================== daysBetween 测试 ====================

    @Test
    @DisplayName("计算两日期之间的天数 - 字符串日期")
    void testDaysBetween_StringDates() {
        int days = DateUtil.daysBetween("2024-01-01", "2024-01-11", "yyyy-MM-dd");
        assertThat(days).isEqualTo(10);

        // 相同日期返回 0
        int sameDays = DateUtil.daysBetween("2024-03-15", "2024-03-15", "yyyy-MM-dd");
        assertThat(sameDays).isEqualTo(0);
    }

    @Test
    @DisplayName("计算两日期之间的天数 - Date 对象")
    void testDaysBetween_DateObjects() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        Date date1 = cal1.getTime();
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.JANUARY, 11, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        Date date2 = cal2.getTime();

        int days = DateUtil.daysBetween(date1, date2);
        assertThat(days).isEqualTo(10);

        // 交换顺序，结果应该相同（取绝对值）
        int daysReversed = DateUtil.daysBetween(date2, date1);
        assertThat(daysReversed).isEqualTo(10);
    }
}
