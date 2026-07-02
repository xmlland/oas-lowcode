package com.jeestudio.bpm.utils;

/**
 * @Description: 区块链工具类存根
 * 注意：实际区块链功能需要配置 conflux.web3j 依赖
 */
public class BlockChainUtil {
    
    private static Object cfx;
    
    public static Object getCfx() {
        return cfx;
    }
    
    public static void initCfx(String url, long chainId) throws Exception {
        // 存根实现
    }
    
    public static String getValue(String conAddress, String functionName, String key) {
        // 存根实现，返回空值
        return null;
    }
    
    public static String setValue(String conAddress, String functionName, String privateKey, String key, String value) {
        // 存根实现，返回空值
        return null;
    }
    
    public static String getAddress(String privateKey) {
        // 存根实现，返回空值
        return null;
    }
    
    public static String deploy(String privateKey, String bytecode) {
        // 存根实现，返回空值
        return null;
    }
}
