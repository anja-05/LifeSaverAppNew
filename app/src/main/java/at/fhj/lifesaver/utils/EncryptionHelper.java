package at.fhj.lifesaver.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
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
     * Verschlüsselt einen Text mit dem übergebenen Passwort.
     * @param context Anwendungskontext zum Zugriff auf SharedPreferences
     * @param password Benutzerpasswort
     * @param plainText zu verschlüsselnder Text
     * @return AES-verschlüsselter, Base64-kodierter String oder Originaltext bei Fehler
     */
    public static String encrypt(Context context, String password, String plainText) {
        try {
            SecretKeySpec key = getKeyFromPassword(context, password);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            return plainText;
        }
    }

    /**
     * Entschlüsselt einen Base64-kodierten AES-Text mit dem übergebenen Passwort.
     * @param context Anwendungskontext zum Zugriff auf SharedPreferences
     * @param password Benutzerpasswort
     * @param encryptedText Base64-kodierter verschlüsselter Text
     * @return Entschlüsselter Klartext oder Eingabetext im Fehlerfall
     */
    public static String decrypt(Context context, String password, String encryptedText) {
        try {
            SecretKeySpec key = getKeyFromPassword(context, password);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.decode(encryptedText, Base64.NO_WRAP);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encryptedText;
        }
    }

    /**
     * Erzeugt einen AES-Schlüssel aus Passwort + Salt
     * @param context Anwendungskontext
     * @param password Passwort zur Ableitung des Schlüssels
     * @return {@link SecretKeySpec} zur Verwendung mit AES
     * @throws Exception bei Fehlern bei der Schlüsselerzeugung
     */
    private static SecretKeySpec getKeyFromPassword(Context context, String password) throws Exception {
        byte[] salt = loadOrGenerateSalt(context);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Lädt den gespeicherten Salt oder generiert einen neuen Salt und speichert ihn sicher.
     * @param context Anwendungskontext
     * @return Byte-Array des Salts
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
