package com.geeksforgeeks.notespasswordmanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class VaultEncryptionService {
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private final SecretKeySpec key;
    private final SecureRandom secureRandom = new SecureRandom();

    public VaultEncryptionService(@Value("${app.vault.key:}") String encodedKey) {
        if (encodedKey.isBlank()) {
            throw new IllegalStateException("VAULT_ENCRYPTION_KEY must be set to a Base64-encoded 32-byte key.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("VAULT_ENCRYPTION_KEY must be valid Base64.", exception);
        }
        if (keyBytes.length != 32) {
            throw new IllegalStateException("VAULT_ENCRYPTION_KEY must decode to exactly 32 bytes.");
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
        Arrays.fill(keyBytes, (byte) 0);
    }

    public String encrypt(String plaintext) {
        byte[] nonce = new byte[NONCE_LENGTH];
        secureRandom.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] combined = new byte[nonce.length + ciphertext.length];
            System.arraycopy(nonce, 0, combined, 0, nonce.length);
            System.arraycopy(ciphertext, 0, combined, nonce.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not encrypt vault value.", exception);
        }
    }

    public String decrypt(String encodedValue) {
        try {
            byte[] combined = Base64.getDecoder().decode(encodedValue);
            if (combined.length <= NONCE_LENGTH) {
                throw new IllegalArgumentException("Encrypted vault value is malformed.");
            }
            byte[] nonce = Arrays.copyOfRange(combined, 0, NONCE_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(combined, NONCE_LENGTH, combined.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            return new String(cipher.doFinal(ciphertext), java.nio.charset.StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("Could not decrypt vault value. Check the configured key.", exception);
        }
    }
}
