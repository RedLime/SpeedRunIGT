package com.redlimerl.speedrunigt.crypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Crypto {
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();

    private static Cipher cipher(int opMode, String key) {
        if (key.length() != 32 && key.length() != 16) throw new RuntimeException("SecretKey length is not 16 or 32 chars");

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec sk = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec iv = new IvParameterSpec(key.substring(0, 16).getBytes(StandardCharsets.UTF_8));
            c.init(opMode, sk, iv);
            return c;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeException("Cipher Algorithm Exception");
        }
    }

    public static String encrypt(String str, String key) {
        try {
            return new String(encoder.encode(cipher(Cipher.ENCRYPT_MODE, key).doFinal(str.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Bad crypt request");
        }
    }

    public static String decrypt(String str, String key) {
        try {
            return new String(cipher(Cipher.DECRYPT_MODE, key).doFinal(decoder.decode(str.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Bad crypt request");
        }
    }
}
