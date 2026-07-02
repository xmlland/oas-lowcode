import sm2 from './security/sm2';

/**
 * 生成SM2密钥对
 * @return {*}
 * @constructor
 */
export const Sm2GenerateKeyPairHex = () => {
    return sm2.generateKeyPairHex();
}

/**
 * 验证SM2公钥格式
 * SM2公钥应为128位十六进制字符（64字节）或130位带04前缀
 * @param {string} publicKey - 待验证的公钥
 * @return {boolean} 是否有效
 */
export const isValidSm2PublicKey = (publicKey) => {
    if (!publicKey || typeof publicKey !== 'string') {
        return false;
    }
    // 必须是十六进制字符串：128字符(无前缀)或130字符(04前缀)
    const hexPattern = /^[0-9a-fA-F]+$/;
    if (!hexPattern.test(publicKey)) {
        return false;
    }
    if (publicKey.length === 128) {
        return true;
    }
    if (publicKey.length === 130 && publicKey.startsWith('04')) {
        return true;
    }
    return false;
}

/**
 * Sm2加密数据
 * @param {any} msgString - 需加密的数据
 * @param {string} publicKey - 加密公钥
 * @return {string} 加密后的字符串
 * @throws {Error} 公钥格式无效时抛出异常
 */
export const Sm2Encrypt = (msgString, publicKey) => {
    if (msgString == null || msgString === '') {
        return msgString;
    }

    // 加密前验证公钥格式
    if (!isValidSm2PublicKey(publicKey)) {
        console.error('[SM2] Invalid public key format:', publicKey?.substring(0, 20) + '...');
        throw new Error('Invalid SM2 public key format');
    }

    let msg = msgString;
    if (typeof (msgString) === "object") {
        msg = JSON.stringify(msgString);
    }else if (typeof (msgString) === "boolean") {
        msg = msgString? 'true' : 'false';
    }else if (typeof (msgString) === "number") {
        msg = msgString + '';
    }else if (typeof (msgString) !== "string") {
        msg = msgString.toString();
    }

    // 1 - C1C3C2；	0 - C1C2C3；	默认为1
    let cipherMode = 1; // 特别注意,此处前后端需保持一致
    // 加密结果
    let encryptData = sm2.doEncrypt(msg, publicKey, cipherMode);
    // 加密后的密文前需要添加04，后端才能正常解密
    return '04' + encryptData;
}

/**
 * Sm2解密数据
 * @param {any} enStr - 待解密的数据
 * @param {string} privateKey - 解密私钥
 * @return {string} 解密后的字符串
 */
export const Sm2Decrypt = (enStr, privateKey) => {
    let msg = enStr;
    if (typeof (enStr) !== "string") {
        msg = JSON.stringify(enStr);
    }
    msg = msg.substr(2)
    // 1 - C1C3C2；	0 - C1C2C3；	默认为1
    let cipherMode = 1;
    // 解密结果
    return sm2.doDecrypt(msg, privateKey, cipherMode);
}
