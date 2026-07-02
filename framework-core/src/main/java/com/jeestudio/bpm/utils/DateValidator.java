package com.jeestudio.bpm.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 日期格式校验工具
 */
public class DateValidator {
    private static final Map<String, String> dateFormatMapping = new HashMap<>();

    static {
        dateFormatMapping.put("yyyy", "yyyy");
        dateFormatMapping.put("yyyy-Q", "yyyy-'Q'q");
        dateFormatMapping.put("yyyy-MM", "yyyy-MM");
        dateFormatMapping.put("yyyy-MM-dd", "yyyy-MM-dd");
        dateFormatMapping.put("yyyy-MM-dd HH", "yyyy-MM-dd HH");
        dateFormatMapping.put("yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm");
        dateFormatMapping.put("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
    }

    private static final DateTimeFormatter CST_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);

    public static DateResult validateAndSuggest(String input, String dateFormat) {
        String javaFormat = dateFormatMapping.get(dateFormat);
        if (javaFormat == null) {
            return new DateResult(false, "Unsupported format");
        }

        //预处理输入字符串，去掉时区信息，统一格式
        String normalize = normalizeInput(input);

        try {
            // 检查 CST 格式
            if (normalize.matches("^[A-Za-z]{3} [A-Za-z]{3} \\d{2} \\d{2}:\\d{2}:\\d{2} \\d{4}$")) {
                LocalDateTime dateTime = LocalDateTime.parse(normalize, CST_FORMATTER);
                String formatted = dateTime.format(DateTimeFormatter.ofPattern(javaFormat));
                return new DateResult(true, formatted);
            }

            // 检查标准格式
            String formatted = formatDate(normalize, javaFormat);
            if (!formatted.equals(normalize)) {
                return new DateResult(false, formatted);
            }
            return new DateResult(true, normalize);
        } catch (DateTimeParseException e) {
            String suggested = suggestCorrection(normalize, javaFormat);
            return new DateResult(false, suggested);
        }
    }

    private static String formatDate(String input, String dateFormat) {
        if (dateFormat.equals("yyyy")) {
            return Year.parse(input, DateTimeFormatter.ofPattern(dateFormat)).toString();
        } else if (dateFormat.equals("yyyy-MM")) {
            return YearMonth.parse(input, DateTimeFormatter.ofPattern(dateFormat)).toString();
        } else if (dateFormat.length() <= 10) {
            return LocalDate.parse(input, DateTimeFormatter.ofPattern(dateFormat)).format(DateTimeFormatter.ofPattern(dateFormat));
        } else {
            return LocalDateTime.parse(input, DateTimeFormatter.ofPattern(dateFormat)).format(DateTimeFormatter.ofPattern(dateFormat));
        }
    }

    private static String suggestCorrection(String input, String dateFormat) {
        Pattern pattern = Pattern.compile("\\d{4}[-/]?\\d{1,2}[-/]?\\d{1,2}(?:\\s\\d{1,2}(:\\d{1,2}){0,2})?");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String rawDate = matcher.group().replace("/", "-");

            String[] dateTimeParts = rawDate.split("\\s+");
            String[] dateParts = dateTimeParts[0].split("-");
            String year = dateParts[0];

            String month = dateParts.length > 1 ? String.format("%02d", Integer.parseInt(dateParts[1])) : "01";
            String day = dateParts.length > 2 ? String.format("%02d", Integer.parseInt(dateParts[2])) : "01";

            String correctedDate = year;
            if (dateFormat.contains("MM")) {
                correctedDate += "-" + month;
            }
            if (dateFormat.contains("dd")) {
                correctedDate += "-" + day;
            }

            String correctedTime = "";
            if (dateTimeParts.length > 1) {
                String[] timeParts = dateTimeParts[1].split(":");
                String hour = timeParts.length > 0 ? String.format("%02d", Integer.parseInt(timeParts[0])) : "00";
                String minute = timeParts.length > 1 ? String.format("%02d", Integer.parseInt(timeParts[1])) : "00";
                String second = timeParts.length > 2 ? String.format("%02d", Integer.parseInt(timeParts[2])) : "00";

                if (dateFormat.contains("HH:mm:ss")) {
                    correctedTime = hour + ":" + minute + ":" + second;
                } else if (dateFormat.contains("HH:mm")) {
                    correctedTime = hour + ":" + minute;
                } else if (dateFormat.contains("HH")) {
                    correctedTime = hour;
                }
            }

            String finalSuggestion = correctedDate;
            if (!correctedTime.isEmpty() && dateFormat.contains("HH")) {
                finalSuggestion += " " + correctedTime;
            }

            try {
                formatDate(finalSuggestion, dateFormat);
                return finalSuggestion;
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }

        return "";
    }

    /**
     * 预处理输入字符串，去掉时区信息，统一格式
     */
    private static String normalizeInput(String input) {
        // 去掉类似 "GMT+08:00" 或 "CST" 的时区标识
        // 这里用正则替换所有时区缩写和GMT偏移
        String result = input.replaceAll("\\sGMT[+-]\\d{2}:\\d{2}", "");
        result = result.replaceAll("\\s[A-Z]{3}", ""); // 去掉单独的时区缩写，如 CST
        return result.trim();
    }

    public static void main(String[] args) {
        DateResult result1 = validateAndSuggest("Mon Feb 03 00:00:00 2025", "yyyy-MM-dd");
        System.out.println("Valid: " + result1.getIsValid() + ", Suggested: " + result1.getSuggestedValue());

        DateResult result2 = validateAndSuggest("2025-03-04 12:00", "yyyy");
        System.out.println("Valid: " + result2.getIsValid() + ", Suggested: " + result2.getSuggestedValue());

        DateResult result3 = validateAndSuggest("2025-03-04", "yyyy-MM");
        System.out.println("Valid: " + result3.getIsValid() + ", Suggested: " + result3.getSuggestedValue());

        DateResult result4 = validateAndSuggest("2025-03-4 12", "yyyy-MM-dd HH");
        System.out.println("Valid: " + result4.getIsValid() + ", Suggested: " + result4.getSuggestedValue());
    }
}
