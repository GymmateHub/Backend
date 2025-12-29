package com.gymmate.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility service for common operations.
 */
@Slf4j
@Service
public class UtilityService {
  public static final String DATE_FORMAT = "yyyy-MM-dd";
  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String TIME_FORMAT = "HH:mm:ss";
  public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

  // Regex patterns for validation
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PHONE_PATTERN = Pattern.compile(
      "^\\+?[0-9]{10,15}$");
  private static final Pattern PASSWORD_PATTERN = Pattern.compile(
      "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");
  private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9]+$");
  private static final Pattern UUID_PATTERN = Pattern.compile(
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final PasswordEncoder passwordEncoder;

  public UtilityService(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = new BCryptPasswordEncoder();
  }
  // ==================== Date/Time Formatting ====================

  public static String formatDate(Date date) {
    if (date == null) return null;
    return new SimpleDateFormat(DATE_FORMAT).format(date);
  }

  public static String formatDateTime(Date date) {
    if (date == null) return null;
    return new SimpleDateFormat(DATE_TIME_FORMAT).format(date);
  }

  public static String formatTime(Date date) {
    if (date == null) return null;
    return new SimpleDateFormat(TIME_FORMAT).format(date);
  }

  public static String formatLocalDate(LocalDate date) {
    if (date == null) return null;
    return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
  }

  public static String formatLocalDateTime(LocalDateTime dateTime) {
    if (dateTime == null) return null;
    return dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public static String formatLocalTime(LocalTime time) {
    if (time == null) return null;
    return time.format(DateTimeFormatter.ofPattern(TIME_FORMAT));
  }

  // ==================== Date/Time Parsing ====================

  public static Date parseDate(String dateString) {
    if (isNullOrEmpty(dateString)) return null;
    try {
      return new SimpleDateFormat(DATE_FORMAT).parse(dateString);
    } catch (ParseException e) {
      log.error("Failed to parse date: {}", dateString, e);
      return null;
    }
  }

  public static Date parseDateTime(String dateTimeString) {
    if (isNullOrEmpty(dateTimeString)) return null;
    try {
      return new SimpleDateFormat(DATE_TIME_FORMAT).parse(dateTimeString);
    } catch (ParseException e) {
      log.error("Failed to parse datetime: {}", dateTimeString, e);
      return null;
    }
  }

  public static LocalDate parseLocalDate(String dateString) {
    if (isNullOrEmpty(dateString)) return null;
    try {
      return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
    } catch (Exception e) {
      log.error("Failed to parse local date: {}", dateString, e);
      return null;
    }
  }

  public static LocalDateTime parseLocalDateTime(String dateTimeString) {
    if (isNullOrEmpty(dateTimeString)) return null;
    try {
      return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    } catch (Exception e) {
      log.error("Failed to parse local datetime: {}", dateTimeString, e);
      return null;
    }
  }

  // ==================== Date/Time Conversion ====================

  public static LocalDate toLocalDate(Date date) {
    if (date == null) return null;
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }

  public static LocalDateTime toLocalDateTime(Date date) {
    if (date == null) return null;
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public LocalDateTime secondsToLocalDateTime(Long epochSeconds) {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
  }

  public static Date toDate(LocalDate localDate) {
    if (localDate == null) return null;
    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }

  public static Date toDate(LocalDateTime localDateTime) {
    if (localDateTime == null) return null;
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  // ==================== Date/Time Calculations ====================

  public static long daysBetween(LocalDate start, LocalDate end) {
    if (start == null || end == null) return 0;
    return ChronoUnit.DAYS.between(start, end);
  }

  public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) return 0;
    return ChronoUnit.HOURS.between(start, end);
  }

  public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) return 0;
    return ChronoUnit.MINUTES.between(start, end);
  }

  public static LocalDate addDays(LocalDate date, int days) {
    if (date == null) return null;
    return date.plusDays(days);
  }

  public static LocalDate addMonths(LocalDate date, int months) {
    if (date == null) return null;
    return date.plusMonths(months);
  }

  public static LocalDate addYears(LocalDate date, int years) {
    if (date == null) return null;
    return date.plusYears(years);
  }

  public static boolean isDateInFuture(LocalDate date) {
    if (date == null) return false;
    return date.isAfter(LocalDate.now());
  }

  public static boolean isDateInPast(LocalDate date) {
    if (date == null) return false;
    return date.isBefore(LocalDate.now());
  }

  public static boolean isDateToday(LocalDate date) {
    if (date == null) return false;
    return date.isEqual(LocalDate.now());
  }

  public static LocalDate getStartOfMonth(LocalDate date) {
    if (date == null) return null;
    return date.withDayOfMonth(1);
  }

  public static LocalDate getEndOfMonth(LocalDate date) {
    if (date == null) return null;
    return date.withDayOfMonth(date.lengthOfMonth());
  }

  public static int calculateAge(LocalDate birthDate) {
    if (birthDate == null) return 0;
    return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
  }

  // ==================== Random Generation ====================

  public static int generateRandomNumber(int length) {
    if (length <= 0) return 0;
    int min = (int) Math.pow(10, length - 1);
    int max = (int) Math.pow(10, length) - 1;
    return SECURE_RANDOM.nextInt(max - min + 1) + min;
  }

  public static String generateRandomString(int length) {
    if (length <= 0) return "";
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder result = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int index = SECURE_RANDOM.nextInt(characters.length());
      result.append(characters.charAt(index));
    }
    return result.toString();
  }

  public static String generateSecureToken(int length) {
    if (length <= 0) return "";
    byte[] bytes = new byte[length];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public static String generateUUID() {
    return UUID.randomUUID().toString();
  }

  public static String generateOTP(int length) {
    if (length <= 0) return "";
    StringBuilder otp = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      otp.append(SECURE_RANDOM.nextInt(10));
    }
    return otp.toString();
  }

  public static String generateMemberCode(String prefix) {
    String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
    String random = generateRandomString(4).toUpperCase();
    return (prefix != null ? prefix : "MEM") + "-" + timestamp + random;
  }

  public static String generateBookingReference() {
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String random = generateRandomString(6).toUpperCase();
    return "BK" + date + random;
  }

  public static String generateTransactionId() {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String random = generateRandomString(8).toUpperCase();
    return "TXN" + timestamp + random;
  }

  // ==================== Masking ====================

  public static String maskEmail(String email) {
    if (isNullOrEmpty(email) || !email.contains("@")) return email;
    int atIndex = email.indexOf("@");
    if (atIndex <= 2) return email;
    String localPart = email.substring(0, atIndex);
    String domainPart = email.substring(atIndex);
    String maskedLocal = localPart.charAt(0) +
        "*".repeat(localPart.length() - 2) +
        localPart.charAt(localPart.length() - 1);
    return maskedLocal + domainPart;
  }

  public static String maskPhoneNumber(String phoneNumber) {
    if (isNullOrEmpty(phoneNumber) || phoneNumber.length() < 7) return phoneNumber;
    int visibleStart = 3;
    int visibleEnd = 2;
    String start = phoneNumber.substring(0, visibleStart);
    String end = phoneNumber.substring(phoneNumber.length() - visibleEnd);
    String masked = "*".repeat(phoneNumber.length() - visibleStart - visibleEnd);
    return start + masked + end;
  }

  public static String maskCreditCard(String cardNumber) {
    if (isNullOrEmpty(cardNumber) || cardNumber.length() < 12) return cardNumber;
    String cleaned = cardNumber.replaceAll("[^0-9]", "");
    if (cleaned.length() < 12) return cardNumber;
    return "**** **** **** " + cleaned.substring(cleaned.length() - 4);
  }

  public static String maskName(String name) {
    if (isNullOrEmpty(name) || name.length() < 2) return name;
    return name.charAt(0) + "*".repeat(name.length() - 1);
  }

  public static String mask(String value) {
    if (isNullOrEmpty(value)) return value;
    return "*".repeat(value.length());
  }

  public static String maskPartial(String value, int visibleChars) {
    if (isNullOrEmpty(value) || value.length() <= visibleChars) return value;
    return value.substring(0, visibleChars) + "*".repeat(value.length() - visibleChars);
  }

  // ==================== Validation ====================

  public static boolean isValidEmail(String email) {
    return !isNullOrEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
  }

  public static boolean isValidPhoneNumber(String phone) {
    if (isNullOrEmpty(phone)) return false;
    String cleaned = phone.replaceAll("[\\s\\-()]", "");
    return PHONE_PATTERN.matcher(cleaned).matches();
  }

  public static boolean isValidPassword(String password) {
    return !isNullOrEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
  }

  public static boolean isValidUUID(String uuid) {
    return !isNullOrEmpty(uuid) && UUID_PATTERN.matcher(uuid).matches();
  }

  public static boolean isAlphanumeric(String value) {
    return !isNullOrEmpty(value) && ALPHANUMERIC_PATTERN.matcher(value).matches();
  }

  public static boolean isNumeric(String value) {
    if (isNullOrEmpty(value)) return false;
    try {
      Double.parseDouble(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean isNullOrEmpty(String value) {
    return value == null || value.trim().isEmpty();
  }

  public static boolean isNullOrEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  public static boolean isNullOrEmpty(Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

  public static boolean isNullOrEmpty(Object[] array) {
    return array == null || array.length == 0;
  }

  // ==================== String Manipulation ====================

  public static String capitalize(String value) {
    if (isNullOrEmpty(value)) return value;
    return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
  }

  public static String capitalizeWords(String value) {
    if (isNullOrEmpty(value)) return value;
    return Arrays.stream(value.split("\\s+"))
        .map(UtilityService::capitalize)
        .collect(Collectors.joining(" "));
  }

  public static String toSlug(String value) {
    if (isNullOrEmpty(value)) return value;
    return value.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }

  public static String toCamelCase(String value) {
    if (isNullOrEmpty(value)) return value;
    String[] parts = value.toLowerCase().split("[\\s_-]+");
    StringBuilder result = new StringBuilder(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      result.append(capitalize(parts[i]));
    }
    return result.toString();
  }

  public static String toSnakeCase(String value) {
    if (isNullOrEmpty(value)) return value;
    return value.replaceAll("([a-z])([A-Z])", "$1_$2")
        .replaceAll("[\\s-]+", "_")
        .toLowerCase();
  }

  public static String truncate(String value, int maxLength) {
    if (isNullOrEmpty(value) || value.length() <= maxLength) return value;
    return value.substring(0, maxLength);
  }

  public static String truncateWithEllipsis(String value, int maxLength) {
    if (isNullOrEmpty(value) || value.length() <= maxLength) return value;
    return value.substring(0, maxLength - 3) + "...";
  }

  public static String trimToNull(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  public static String defaultIfEmpty(String value, String defaultValue) {
    return isNullOrEmpty(value) ? defaultValue : value;
  }

  public static String removeWhitespace(String value) {
    if (isNullOrEmpty(value)) return value;
    return value.replaceAll("\\s+", "");
  }

  public static String normalizeWhitespace(String value) {
    if (isNullOrEmpty(value)) return value;
    return value.trim().replaceAll("\\s+", " ");
  }

  public static String reverse(String value) {
    if (isNullOrEmpty(value)) return value;
    return new StringBuilder(value).reverse().toString();
  }

  public static String padLeft(String value, int length, char padChar) {
    if (value == null) value = "";
    if (value.length() >= length) return value;
    return String.valueOf(padChar).repeat(length - value.length()) + value;
  }

  public static String padRight(String value, int length, char padChar) {
    if (value == null) value = "";
    if (value.length() >= length) return value;
    return value + String.valueOf(padChar).repeat(length - value.length());
  }

  public static int countOccurrences(String text, String search) {
    if (isNullOrEmpty(text) || isNullOrEmpty(search)) return 0;
    int count = 0;
    int index = 0;
    while ((index = text.indexOf(search, index)) != -1) {
      count++;
      index += search.length();
    }
    return count;
  }

  // ==================== Number/Currency Formatting ====================

  public static String formatCurrency(BigDecimal amount) {
    if (amount == null) return "0.00";
    DecimalFormat df = new DecimalFormat("#,##0.00");
    return df.format(amount);
  }

  public static String formatCurrency(BigDecimal amount, String currencySymbol) {
    return (currencySymbol != null ? currencySymbol : "$") + formatCurrency(amount);
  }

  public static String formatNumber(Number number) {
    if (number == null) return "0";
    DecimalFormat df = new DecimalFormat("#,###");
    return df.format(number);
  }

  public static String formatDecimal(Number number, int decimalPlaces) {
    if (number == null) return "0";
    String pattern = "#,##0." + "0".repeat(Math.max(0, decimalPlaces));
    DecimalFormat df = new DecimalFormat(pattern);
    return df.format(number);
  }

  public static String formatPercentage(double value) {
    DecimalFormat df = new DecimalFormat("#0.00%");
    return df.format(value);
  }

  public static BigDecimal round(BigDecimal value, int scale) {
    if (value == null) return BigDecimal.ZERO;
    return value.setScale(scale, RoundingMode.HALF_UP);
  }

  public static double parseDouble(String value, double defaultValue) {
    if (isNullOrEmpty(value)) return defaultValue;
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static int parseInt(String value, int defaultValue) {
    if (isNullOrEmpty(value)) return defaultValue;
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static long parseLong(String value, long defaultValue) {
    if (isNullOrEmpty(value)) return defaultValue;
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  // ==================== Byte Conversion ====================

  public static String byteToString(byte[] bytes) {
    if (bytes == null) return null;
    return new String(bytes);
  }

  public static byte[] stringToByte(String string) {
    if (string == null) return null;
    return string.getBytes();
  }

  public static String byteArrayToString(byte[] bytes) {
    return byteToString(bytes);
  }

  public static byte[] stringToByteArray(String string) {
    return stringToByte(string);
  }

  public static String bytesToHex(byte[] bytes) {
    if (bytes == null) return null;
    StringBuilder hex = new StringBuilder();
    for (byte b : bytes) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }

  public static byte[] hexToBytes(String hex) {
    if (isNullOrEmpty(hex)) return null;
    int len = hex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
          + Character.digit(hex.charAt(i + 1), 16));
    }
    return data;
  }

  // ==================== Encoding/Encryption ====================

  public static String encodeBase64(String value) {
    if (isNullOrEmpty(value)) return value;
    return Base64.getEncoder().encodeToString(value.getBytes());
  }

  public static String decodeBase64(String encoded) {
    if (isNullOrEmpty(encoded)) return encoded;
    try {
      return new String(Base64.getDecoder().decode(encoded));
    } catch (IllegalArgumentException e) {
      log.error("Failed to decode Base64: {}", encoded, e);
      return null;
    }
  }

  public static String encodeBase64Url(String value) {
    if (isNullOrEmpty(value)) return value;
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes());
  }

  public static String decodeBase64Url(String encoded) {
    if (isNullOrEmpty(encoded)) return encoded;
    try {
      return new String(Base64.getUrlDecoder().decode(encoded));
    } catch (IllegalArgumentException e) {
      log.error("Failed to decode Base64 URL: {}", encoded, e);
      return null;
    }
  }

  public static String encrypt(String inputValue) {
    if (isNullOrEmpty(inputValue)) return inputValue;
    String salt = BCrypt.gensalt(12);
    return BCrypt.hashpw(inputValue, salt);
  }

  public String encryptPassword(String plainPassword) {
    if (isNullOrEmpty(plainPassword)) return plainPassword;

    // logging the password encoding process
    log.debug("Encoding password");

    return passwordEncoder.encode(plainPassword);
    //  return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
  }

  public static String encryptBase64(String inputValue) {
    if (isNullOrEmpty(inputValue)) return inputValue;
    return Base64.getEncoder().encodeToString(inputValue.getBytes());
  }

  public static String decrypt(String encryptedValue) {
    if (isNullOrEmpty(encryptedValue)) return encryptedValue;
    return new String(Base64.getDecoder().decode(encryptedValue));
  }

  public static boolean verifyEncryption(String plainText, String hashedValue) {
    if (isNullOrEmpty(plainText) || isNullOrEmpty(hashedValue)) return false;
    try {
      return BCrypt.checkpw(plainText, hashedValue);
    } catch (Exception e) {
      log.error("Failed to verify encryption", e);
      return false;
    }
  }

  // ==================== Collection Utilities ====================

  public static <T> List<T> safeList(List<T> list) {
    return list != null ? list : Collections.emptyList();
  }

  public static <T> Set<T> safeSet(Set<T> set) {
    return set != null ? set : Collections.emptySet();
  }

  public static <K, V> Map<K, V> safeMap(Map<K, V> map) {
    return map != null ? map : Collections.emptyMap();
  }

  public static <T> T getFirst(List<T> list) {
    return isNullOrEmpty(list) ? null : list.get(0);
  }

  public static <T> T getLast(List<T> list) {
    return isNullOrEmpty(list) ? null : list.get(list.size() - 1);
  }

  public static <T> List<List<T>> partition(List<T> list, int size) {
    if (isNullOrEmpty(list) || size <= 0) return Collections.emptyList();
    List<List<T>> partitions = new ArrayList<>();
    for (int i = 0; i < list.size(); i += size) {
      partitions.add(list.subList(i, Math.min(i + size, list.size())));
    }
    return partitions;
  }

  // ==================== File/Size Utilities ====================

  public static String formatFileSize(long bytes) {
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String[] units = {"KB", "MB", "GB", "TB", "PB"};
    return String.format("%.2f %s", bytes / Math.pow(1024, exp), units[exp - 1]);
  }

  public static String getFileExtension(String filename) {
    if (isNullOrEmpty(filename)) return "";
    int lastDot = filename.lastIndexOf('.');
    return lastDot == -1 ? "" : filename.substring(lastDot + 1).toLowerCase();
  }

  public static String getFileNameWithoutExtension(String filename) {
    if (isNullOrEmpty(filename)) return "";
    int lastDot = filename.lastIndexOf('.');
    return lastDot == -1 ? filename : filename.substring(0, lastDot);
  }

  public static boolean isImageFile(String filename) {
    String ext = getFileExtension(filename);
    return Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg").contains(ext);
  }

  public static boolean isDocumentFile(String filename) {
    String ext = getFileExtension(filename);
    return Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv").contains(ext);
  }

  // ==================== Gym-Specific Utilities ====================

  public static String formatDuration(int minutes) {
    if (minutes < 60) return minutes + " min";
    int hours = minutes / 60;
    int remainingMinutes = minutes % 60;
    if (remainingMinutes == 0) return hours + " hr";
    return hours + " hr " + remainingMinutes + " min";
  }

  public static String formatTimeSlot(LocalTime start, LocalTime end) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
    return start.format(formatter) + " - " + end.format(formatter);
  }

  public static double calculateBMI(double weightKg, double heightCm) {
    if (weightKg <= 0 || heightCm <= 0) return 0;
    double heightM = heightCm / 100;
    return round(BigDecimal.valueOf(weightKg / (heightM * heightM)), 1).doubleValue();
  }

  public static String getBMICategory(double bmi) {
    if (bmi < 18.5) return "Underweight";
    if (bmi < 25) return "Normal";
    if (bmi < 30) return "Overweight";
    return "Obese";
  }

  public static int calculateCaloriesBurned(String activityType, int durationMinutes, double weightKg) {
    // MET values for common gym activities
    Map<String, Double> metValues = Map.of(
        "walking", 3.5,
        "running", 9.8,
        "cycling", 7.5,
        "swimming", 8.0,
        "weightlifting", 6.0,
        "yoga", 3.0,
        "hiit", 12.0,
        "elliptical", 5.0,
        "rowing", 7.0,
        "spinning", 8.5
    );
    double met = metValues.getOrDefault(activityType.toLowerCase(), 5.0);
    // Calories = MET × weight (kg) × duration (hours)
    return (int) (met * weightKg * (durationMinutes / 60.0));
  }

  // ==================== Gym-Specific Utilities ====================

  // =================== OTP Utilities ===================
  public static String generateOTP() {
    return String.format("%06d", new Random().nextInt(900000) + 100000);
  }
}
