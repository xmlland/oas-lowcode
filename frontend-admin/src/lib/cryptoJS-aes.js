import CryptoJS from "crypto-js"

// AES key must match backend Aes.java KEY
// Backend default: "abcdefgabcdefg12", configurable via LOGIN_ENCRYPT_KEY env variable
const AES_KEY = "qcyc9fhIoRayQbBO";

/**
 * AES/CBC/PKCS7 encrypt
 * Generate random 16-byte IV, prepend to ciphertext, Base64 encode the whole thing.
 * Format: Base64(IV + ciphertext)
 * Compatible with backend Aes.java aesDecryptByBytes()
 */
export function encryptByAESModeCBC(message) {
    const key = CryptoJS.enc.Utf8.parse(AES_KEY);
    const iv = CryptoJS.lib.WordArray.random(16);

    const encrypted = CryptoJS.AES.encrypt(message, key, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });

    // IV + ciphertext, matching backend format: Base64(IV + ciphertext)
    const combined = iv.concat(encrypted.ciphertext);
    return CryptoJS.enc.Base64.stringify(combined);
}

/**
 * AES/CBC/PKCS7 decrypt
 * Base64 decode, first 16 bytes = IV, rest = ciphertext
 */
export function decryptByAESModeCBC(ciphertext) {
    const key = CryptoJS.enc.Utf8.parse(AES_KEY);
    const cipherBytes = CryptoJS.enc.Base64.parse(ciphertext);

    // Extract IV (first 16 bytes = 4 words)
    const iv = CryptoJS.lib.WordArray.create(cipherBytes.words.slice(0, 4), 16);
    // Extract ciphertext (after 16 bytes)
    const encryptedData = CryptoJS.lib.WordArray.create(
        cipherBytes.words.slice(4),
        cipherBytes.sigBytes - 16
    );

    const decrypted = CryptoJS.AES.decrypt(
        {ciphertext: encryptedData},
        key,
        {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        }
    );

    return decrypted.toString(CryptoJS.enc.Utf8);
}

// Backward-compatible aliases so callers only need to change import path
export const encryptByDESModeEBC = encryptByAESModeCBC;
export const decryptByDESModeEBC = decryptByAESModeCBC;
