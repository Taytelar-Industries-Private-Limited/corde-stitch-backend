package com.cordestitch.filter;

import com.cordestitch.exception.filter.EncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class IdEncryptor {

    @Value("${cipher.secret.key}")
    private String secretKey;

    private static final int IV_LENGTH = 12;
    private static final String ALGO = "AES/GCM/NoPadding";

    public String encrypt(String originalId) throws EncryptionException {
        try {
            log.info("Before encrypting the id: {}", originalId);
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);

            log.info("Cipher Secret Key: {}", secretKey);
            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

            byte[] encryptedBytes = cipher.doFinal(originalId.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedWithIv = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, encryptedWithIv, iv.length, encryptedBytes.length);

            String encryptId =  Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedWithIv);
            log.info("After encoding encrypted id: {}", encryptId);
            return encryptId;
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedId) throws EncryptionException {
        try {
            log.info("Before decrypting the id : {}", encryptedId);
            byte[] encryptedWithIv = Base64.getUrlDecoder().decode(encryptedId);

            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedBytes = new byte[encryptedWithIv.length - IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0,IV_LENGTH);
            System.arraycopy(encryptedWithIv, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            log.info("Cipher Secret Key: {}", secretKey);
            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedId = new String(decryptedBytes, StandardCharsets.UTF_8);
            log.info("After decrypt the id : {}", decryptedId);
            return decryptedId;
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }
}
