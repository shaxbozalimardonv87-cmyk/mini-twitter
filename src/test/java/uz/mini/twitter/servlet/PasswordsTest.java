package uz.mini.twitter.servlet;

import org.junit.jupiter.api.Test;
import uz.mini.twitter.util.Passwords;
import static org.junit.jupiter.api.Assertions.*;

class PasswordsTest {
    @Test
    void saltsAndVerifiesUnicodePasswords() {
        String first = Passwords.hash("O‘zbek🔐 password");
        assertTrue(first.startsWith(Passwords.PREFIX));
        assertNotEquals(first, Passwords.hash("O‘zbek🔐 password"));
        assertTrue(Passwords.matches("O‘zbek🔐 password", first));
        assertFalse(Passwords.matches("wrong", first));
    }

    @Test
    void rejectsPlaintextAndMalformedHashes() {
        for (String stored : new String[]{null, "password", "pbkdf2-sha256$600000$bad$bad",
                "pbkdf2-sha256$999999999$bad$bad", "other$600000$bad$bad"}) {
            assertFalse(Passwords.matches("password", stored));
        }
        assertFalse(Passwords.matches(null, "password"));
        assertThrows(IllegalArgumentException.class, () -> Passwords.hash(""));
    }
}
