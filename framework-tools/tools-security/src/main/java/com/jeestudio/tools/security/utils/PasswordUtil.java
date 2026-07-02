package com.jeestudio.tools.security.utils;


import cn.hutool.core.util.StrUtil;
import com.jeestudio.tools.base.utils.RandomUtil;
import com.jeestudio.tools.security.constant.PasswordConstant;
import com.jeestudio.tools.security.enums.PasswordCharTypeEnum;
import com.jeestudio.tools.security.exceptions.WeakPasswordException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 密码工具
 */
public class PasswordUtil {
    private static String upperCase = "ABCDEFGHJKMNPQRSTUVWXYZ";
    private static String lowerCase = "abcdefghjkmnpqrstuvwxyz";
    private static String number = "123456789";
    private static String character = "@#*.";//移除! 容易输入错误 2022-10-21 16:23:22 zry

    public static final Pattern upperCaseAll = Pattern.compile("[A-Z]+");
    public static final Pattern lowerCaseAll = Pattern.compile("[a-z]+");
    public static final Pattern numberAll = Pattern.compile("[0-9]+");
    public static final Pattern characterAll = Pattern.compile("[~`!@#$%^&*()_\\-+={\\[}\\]|\\\\:;\"'<,>.?/]+");

    /**
     * 生成随机密码
     *
     * @return
     */
    public static String generate() {
        return generate(10);
    }

    /**
     * 生成随机密码
     *
     * @param length 位数
     * @return
     */
    public static String generate(int length) {
        String password;
        boolean isWeak;
        do {
            // 初始化一个空的字符串列表，用于存储生成的密码字符
            List<String> pass = new ArrayList<>();
            // 初始化各类字符的计数器，确保密码中至少包含每种类型的一个字符
            int _upperCaseCount = 1, _lowerCaseCount = 1, _numberCount = 1, _characterCount = 1;
            // 生成大写字母在密码中出现的位置数组
            int[] _upperCase = RandomUtil.generateNumber(upperCase.length() - 1, 0, length);
            // 生成小写字母在密码中出现的位置数组
            int[] _lowerCase = RandomUtil.generateNumber(lowerCase.length() - 1, 0, length);
            // 生成数字在密码中出现的位置数组
            int[] _number = RandomUtil.generateNumber(number.length() - 1, 0, length);
            // 生成特殊字符在密码中出现的位置数组
            int[] _character = RandomUtil.generateNumber(character.length() - 1, 0, length);
            // 从大写字母中选取一个字符添加到密码列表中
            pass.add(upperCase.substring(_upperCase[0], _upperCase[0] + 1));
            // 从小写字母中选取一个字符添加到密码列表中
            pass.add(lowerCase.substring(_lowerCase[0], _lowerCase[0] + 1));
            // 从数字中选取一个字符添加到密码列表中
            pass.add(number.substring(_number[0], _number[0] + 1));
            // 从特殊字符中选取一个字符添加到密码列表中
            pass.add(character.substring(_character[0], _character[0] + 1));
            // 生成一个随机数组，用于决定剩余密码字符的类型
            int[] ints = RandomUtil.generateNumber(4, 1, length - 1);
            // 根据生成的随机数组，选择相应的字符类型添加到密码列表中，直到达到指定的密码长度
            for (int i = 0; i < length - 4; i++) {
                int anInt = ints[i];
                // 根据随机数选择字符类型，并添加到密码列表中
                if (anInt == 1) {
                    _upperCaseCount++;
                    pass.add(upperCase.substring(_upperCase[_upperCaseCount], _upperCase[_upperCaseCount] + 1));
                } else if (anInt == 2) {
                    _lowerCaseCount++;
                    pass.add(lowerCase.substring(_lowerCase[_lowerCaseCount], _lowerCase[_lowerCaseCount] + 1));
                } else if (anInt == 3) {
                    _numberCount++;
                    pass.add(number.substring(_number[_numberCount], _number[_numberCount] + 1));
                } else if (anInt == 4) {
                    _characterCount++;
                    pass.add(character.substring(_character[_characterCount], _character[_characterCount] + 1));
                }

            }
            // 将密码列表中的字符顺序打乱，以增加密码的随机性
            Collections.shuffle(pass);
            // 将密码列表中的字符拼接成一个字符串，并返回
            password = StringUtils.join(pass, "");
            try {
                isWeak = isWeakPassword(password, length, 4, PasswordCharTypeEnum.upperCase, PasswordCharTypeEnum.lowerCase, PasswordCharTypeEnum.number, PasswordCharTypeEnum.character);
            } catch (WeakPasswordException e) {
                isWeak = true;
            }
        } while (isWeak);
        return password;
    }

    /**
     * 判断密码中是否包含简单数字组合 例如 123,456,789
     *
     * @param password 密码
     * @return boolean
     */
    private static boolean hasSimpleNumberCombination(String password) throws WeakPasswordException {
        StringBuilder numberSequence = new StringBuilder();
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                numberSequence.append(c);
                if (numberSequence.length() >= 3 && (isSequentialNumber(numberSequence.toString()) || isSameNumber(numberSequence.toString()))) {
                    throw new WeakPasswordException(numberSequence.toString(), PasswordConstant.SIMPLE_NUMBER_COMBINATION_EXCEPTION_MESSAGE);
                }
            } else {
                numberSequence.setLength(0);
            }
        }
        return false;
    }

    // 判断是否为连续数字
    private static boolean isSequentialNumber(String password) {
        return isSequentialCharacterCombination(password);
    }

    // 判断是否为相同数字
    private static boolean isSameNumber(String password) {
        char firstChar = password.charAt(0);
        for (char c : password.toCharArray()) {
            if (c != firstChar) {
                return false;
            }
        }
        return true;
    }


    /**
     * 判断密码中是否包含顺序字符组合 例如 abc,bcd,cde 024
     *
     * @param password 密码
     * @return boolean
     */
    private static boolean hasSequentialCharacterCombination(String password) throws WeakPasswordException {
        for (int i = 0; i <= password.length() - 3; i++) {
            String subStr = password.substring(i, i + 3);
            if (isSequentialCharacterCombination(subStr)) {
                throw new WeakPasswordException(subStr, PasswordConstant.SEQUENTIAL_CHARACTER_COMBINATION_EXCEPTION_MESSAGE);
            }
        }
        return false;
    }

    /**
     * 判断是否为顺序字符组合
     */
    private static boolean isSequentialCharacterCombination(String password) {
        if (password.length() < 3) {
            return false;
        }
        int increment = password.charAt(1) - password.charAt(0);
        if (increment != 1 && increment != -1) {
            return false;
        }
        for (int i = 1; i < password.length() - 1; i++) {
            if (password.charAt(i + 1) - password.charAt(i) != increment) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断密码中是否包含键位临近字符组合
     *
     * @param password 密码
     * @return boolean
     */
    private static boolean hasAdjacentKeyCombination(String password) throws WeakPasswordException {
        password = password.toLowerCase();
        for (int i = 0; i <= password.length() - 3; i++) {
            String subStr = password.substring(i, i + 3);
            if (isAdjacentKeyCombination(subStr)) {
                throw new WeakPasswordException(subStr, PasswordConstant.ADJACENT_KEY_COMBINATION_EXCEPTION_MESSAGE);
            }
        }
        return false;
    }

    /**
     * 判断是否为键位临近字符组合
     *
     * @param password 密码
     * @return boolean
     */
    private static boolean isAdjacentKeyCombination(String password) {
        for (int i = 0; i < password.length() - 1; i++) {
            char current = password.charAt(i);
            char next = password.charAt(i + 1);
            if (!isAdjacentOnKeyboard(current, next)) {
                return false;
            }
        }
        return true;
    }

    // 判断两个字符是否在键盘上相邻
    private static boolean isAdjacentOnKeyboard(char c1, char c2) {
        for (String row : PasswordConstant.KEYBOARD_ROWS) {
            int index1 = row.indexOf(c1);
            int index2 = row.indexOf(c2);
            if (index1 != -1 && index2 != -1 && Math.abs(index1 - index2) == 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWeakPasswordCnemc(String password) throws WeakPasswordException {
        return isWeakPassword(password, 12, 4, PasswordCharTypeEnum.upperCase, PasswordCharTypeEnum.lowerCase, PasswordCharTypeEnum.number, PasswordCharTypeEnum.character);
    }

    /**
     * @param password 密码
     * @return 是否弱密码
     */
    public static boolean isWeakPassword(String password, int minLength, int charTypeCount, PasswordCharTypeEnum... passwordCharTypeEnums) throws WeakPasswordException {
        if (password.length() < minLength) {
            throw new WeakPasswordException(PasswordConstant.LENGTH_EXCEPTION_MESSAGE);
        }
        //判断是否在弱密码库里面 忽略大小写
        for (String weak : PasswordConstant.WEAK_PASSWORD_LIST) {
            if (password.toLowerCase().contains(weak.toLowerCase())) {
                throw new WeakPasswordException(weak, PasswordConstant.NORMAl_WEAK_PASSWORD_EXCEPTION_MESSAGE);
            }
        }
        if (hasSimpleNumberCombination(password)) {
            return true;
        }
        if (hasSequentialCharacterCombination(password)) {
            return true;
        }
        if (hasAdjacentKeyCombination(password)) {
            return true;
        }
        //判断密码字符类型数量
        int pwdCharCount = 0;
        List<String> passwordCharTypeList = new ArrayList<>();
        //判断密码字符类型
        for (PasswordCharTypeEnum passwordCharTypeEnum : passwordCharTypeEnums) {
            if (validPasswordChar(passwordCharTypeEnum, password)) {
                pwdCharCount++;
            }
            passwordCharTypeList.add(passwordCharTypeEnum.getTypeName());
        }
        if (pwdCharCount < charTypeCount) {
            if (passwordCharTypeList.size() == 4) {
                throw new WeakPasswordException("密码需包含" + StrUtil.join("、", passwordCharTypeList));
            }
            throw new WeakPasswordException("密码需包含" + StrUtil.join("、", passwordCharTypeList) + "中的" + charTypeCount + "种");
        }
        return false;
    }

    /**
     * 验证密码字符
     *
     * @param charTypeEnum 字符类型
     * @param password     密码明文
     * @return
     */
    public static boolean validPasswordChar(PasswordCharTypeEnum charTypeEnum, String password) {
        Pattern pattern = null;
        switch (charTypeEnum) {
            case upperCase:
                pattern = upperCaseAll;
                break;
            case lowerCase:
                pattern = lowerCaseAll;
                break;
            case number:
                pattern = numberAll;
                break;
            case character:
                pattern = characterAll;
                break;
        }
        Matcher matcher = pattern.matcher(password);
        return matcher.find();
    }

    public static void main(String[] args) {
        System.out.println(generate());
    }
}
