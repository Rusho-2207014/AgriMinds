package com.agriminds.util;
import org.mindrot.jbcrypt.BCrypt;
import java.util.regex.Pattern;
public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(Constants.EMAIL_PATTERN);
    private static final Pattern PHONE_PATTERN = Pattern.compile(Constants.PHONE_PATTERN);
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        phone = phone.replaceAll("[\\s-]", "");
        return phone.matches("^(\\+?880|0)?1[3-9]\\d{8}$");
    }
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= Constants.MIN_PASSWORD_LENGTH 
            && password.length() <= Constants.MAX_PASSWORD_LENGTH;
    }
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    public static boolean isPositive(Double number) {
        return number != null && number > 0;
    }
    public static boolean isInRange(Double number, double min, double max) {
        return number != null && number >= min && number <= max;
    }
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("[<>\"']", "");
    }
}
