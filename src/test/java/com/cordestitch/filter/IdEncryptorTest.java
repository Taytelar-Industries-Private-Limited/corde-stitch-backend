package com.cordestitch.filter;

import com.cordestitch.exception.filter.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class IdEncryptorTest {
    @InjectMocks
    private IdEncryptor idEncryptor;

    private static final String SECRET_KEY = "1234567890123456"; // 16 characters for AES-128 key

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(idEncryptor, "secretKey", SECRET_KEY);
    }

    @Test
    void testEncryptAndDecrypt() {
        String originalId = "testId123";
            String encryptedId = idEncryptor.encrypt(originalId);
            assertNotNull(encryptedId, "Encrypted ID should not be null");
            String decryptedId = idEncryptor.decrypt(encryptedId);
            assertNotNull(decryptedId, "Decrypted ID should not be null");
            assertEquals(originalId, decryptedId, "Decrypted ID should match the original ID");
    }

    @Test
    void testEncryptionExceptionHandling() {
            ReflectionTestUtils.setField(idEncryptor, "secretKey", "shortkey");
            assertThrows(EncryptionException.class, () -> idEncryptor.encrypt("testId123"),
                    "EncryptionException should be thrown due to invalid key length");

    }

    @Test
    void testDecryptionExceptionHandling() {
            String invalidEncryptedId = "invalidBase64==";
            assertThrows(EncryptionException.class, () -> idEncryptor.decrypt(invalidEncryptedId),
                    "EncryptionException should be thrown due to invalid encrypted data");

    }

}