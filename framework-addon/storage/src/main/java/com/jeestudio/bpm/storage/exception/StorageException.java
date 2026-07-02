package com.jeestudio.bpm.storage.exception;

/**
 * @Description: 文件存储异常
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
