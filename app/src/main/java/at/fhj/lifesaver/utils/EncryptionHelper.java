package at.fhj.lifesaver.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Die Klasse {@code EncryptionHelper} bietet Hilfsmethoden zur symmetrischen AES-Verschlüsselung
 * und -Entschlüsselung von Zeichenketten unter Verwendung eines fest definierten Passworts.
 */
public class EncryptionHelper {
    private static final String PREFS_NAME = "encryption_prefs";
    private static final String SALT_KEY = "encryption_salt";

    /**
     * Verschlüsselt einen Text mit dem übergebenen Schlüssel (alte Methode - für andere Zwecke).
     */
    public static String encrypt(Context context, String sharedKey, String plainText) {
        try {
            SecretKeySpec key = getKeyFromSharedKey(context, sharedKey);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return plainText;
        }
    }

    /**
     * Entschlüsselt einen Base64-kodierten AES-Text mit dem übergebenen Schlüssel (alte Methode).
     */
    public static String decrypt(Context context, String sharedKey, String encryptedText) {
        try {
            SecretKeySpec key = getKeyFromSharedKey(context, sharedKey);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.decode(encryptedText, Base64.NO_WRAP);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return encryptedText;
        }
    }

    /**
     * Verschlüsselt mit gemeinsamen Salt für Chat zwischen zwei Benutzern
     */
    public static String encryptForChat(Context context, String user1Email, String user2Email, String sharedKey, String plainText) {
        try {
            SecretKeySpec key = getKeyFromSharedKeyWithUsers(user1Email, user2Email, sharedKey);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return plainText;
        }
    }

    /**
     * Entschlüsselt mit gemeinsamen Salt für Chat zwischen zwei Benutzern
     */
    public static String decryptForChat(Context context, String user1Email, String user2Email, String sharedKey, String encryptedText) {
        try {
            SecretKeySpec key = getKeyFromSharedKeyWithUsers(user1Email, user2Email, sharedKey);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.decode(encryptedText, Base64.NO_WRAP);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return encryptedText;
        }
    }

    /**
     * Erzeugt einen AES-Schlüssel aus dem gemeinsamen Schlüssel + Salt (alte Methode)
     */
    private static SecretKeySpec getKeyFromSharedKey(Context context, String sharedKey) throws Exception {
        byte[] salt = loadOrGenerateSalt(context);
        KeySpec spec = new PBEKeySpec(sharedKey.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Erzeugt einen AES-Schlüssel mit gemeinsamen Salt für Chat zwischen zwei Benutzern
     */
    private static SecretKeySpec getKeyFromSharedKeyWithUsers(String user1Email, String user2Email, String sharedKey) throws Exception {
        byte[] salt = getSharedSalt(user1Email, user2Email);
        KeySpec spec = new PBEKeySpec(sharedKey.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Generiert ein gemeinsames Salt aus den E-Mail-Adressen beider Benutzer
     */
    private static byte[] getSharedSalt(String user1Email, String user2Email) {
        // Sortiere die E-Mails für konsistente Reihenfolge
        String combined;
        if (user1Email.compareTo(user2Email) < 0) {
            combined = user1Email + user2Email;
        } else {
            combined = user2Email + user1Email;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            byte[] salt = new byte[16];
            System.arraycopy(hash, 0, salt, 0, 16);
            return salt;
        } catch (Exception e) {
            // Fallback: einfaches Salt aus String
            byte[] fallbackSalt = new byte[16];
            byte[] combinedBytes = combined.getBytes(StandardCharsets.UTF_8);
            int length = Math.min(16, combinedBytes.length);
            System.arraycopy(combinedBytes, 0, fallbackSalt, 0, length);
            return fallbackSalt;
        }
    }

    /**
     * Lädt oder generiert Salt für einzelne Benutzer (alte Methode)
     */
    private static byte[] loadOrGenerateSalt(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String base64Salt = prefs.getString(SALT_KEY, null);

        if (base64Salt != null) {
            return Base64.decode(base64Salt, Base64.DEFAULT);
        } else {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            prefs.edit().putString(SALT_KEY, Base64.encodeToString(salt, Base64.DEFAULT)).apply();
            return salt;
        }
    }
}
