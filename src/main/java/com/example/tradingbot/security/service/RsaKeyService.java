package com.example.tradingbot.security.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.util.StringUtils;

public final class RsaKeyService {

    private RsaKeyService() {
    }

    public static KeyPair loadOrCreate(String privateKeyPem, String publicKeyPem) {
        try {
            if (StringUtils.hasText(privateKeyPem) && StringUtils.hasText(publicKeyPem)) {
                PrivateKey privateKey = parsePrivateKey(privateKeyPem);
                PublicKey publicKey = parsePublicKey(publicKeyPem);
                return new KeyPair(publicKey, privateKey);
            }
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to load RSA keys", ex);
        }
    }

    private static PrivateKey parsePrivateKey(String pem) throws GeneralSecurityException {
        byte[] decoded = decodePem(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static PublicKey parsePublicKey(String pem) throws GeneralSecurityException {
        byte[] decoded = decodePem(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static byte[] decodePem(String pem) {
        String content = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        return Base64.getDecoder().decode(content.getBytes(StandardCharsets.US_ASCII));
    }
}
