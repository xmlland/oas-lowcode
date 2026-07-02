package com.jeestudio.tools.security.exceptions;

/**
 * @Description: 弱口令异常
 */
public class WeakPasswordException extends Exception {
    private static final long serialVersionUID = 357935757147431435L;

    public WeakPasswordException(String message) {
        super(message);
    }

    public WeakPasswordException(String weakChar, String message) {
        super(message + "：" + weakChar);
    }
}
