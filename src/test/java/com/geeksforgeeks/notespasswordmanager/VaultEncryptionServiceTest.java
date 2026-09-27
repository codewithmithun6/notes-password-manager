package com.geeksforgeeks.notespasswordmanager;

import com.geeksforgeeks.notespasswordmanager.service.VaultEncryptionService;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultEncryptionServiceTest {
    private final String key = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void encryptsAndDecryptsSecret() {
        VaultEncryptionService encryption = new VaultEncryptionService(key);
        String encrypted = encryption.encrypt("my private secret");

        assertNotEquals("my private secret", encrypted);
        assertEquals("my private secret", encryption.decrypt(encrypted));
    }

    @Test
    void usesANewNonceForEachEncryption() {
        VaultEncryptionService encryption = new VaultEncryptionService(key);

        assertNotEquals(encryption.encrypt("same value"), encryption.encrypt("same value"));
    }

    @Test
    void rejectsKeysThatAreNot32Bytes() {
        assertThrows(IllegalStateException.class, () -> new VaultEncryptionService(
                Base64.getEncoder().encodeToString(new byte[16])));
    }
}
