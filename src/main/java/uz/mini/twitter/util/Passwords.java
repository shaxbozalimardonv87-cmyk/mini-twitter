package uz.mini.twitter.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class Passwords {
    public static final String PREFIX = "pbkdf2-sha256$";
    private static final int ITERATIONS = 600_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Passwords() { }

    public static String hash(String password) {
        if (password == null || password.isBlank() || password.length() > 255) {
            throw new IllegalArgumentException("Invalid password length");
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return PREFIX + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derive(password, salt));
    }

    public static boolean matches(String password, String stored) {
        if (password == null || password.isBlank() || password.length() > 255 || stored == null) return false;
        String[] parts = stored.split("\\$", -1);
        if (parts.length != 4 || !parts[0].equals("pbkdf2-sha256")
                || !parts[1].equals(Integer.toString(ITERATIONS))) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return salt.length == 16 && expected.length == 32
                    && MessageDigest.isEqual(expected, derive(password, salt));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }
}
