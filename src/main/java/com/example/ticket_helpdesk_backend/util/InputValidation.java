package com.example.ticket_helpdesk_backend.util;


import java.util.regex.Pattern;
import java.util.Objects;

/**
 * Bộ hàm kiểm tra đầu vào dùng Regex.
 * - Dùng Pattern pre-compile để tối ưu hiệu năng.
 * - Chỉ trả về true/false, không ném exception.
 * - Có thể dùng độc lập ở Service/Controller hoặc trong custom validator.
 */
public final class InputValidation {

    private InputValidation() {}

    // ===== Common helpers =====
    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    // ===== Regex Patterns =====

    /** Email dạng thông dụng (đủ dùng cho hầu hết app) */
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Password: tối thiểu 8 ký tự, có ít nhất 1 chữ thường, 1 chữ hoa, 1 số, 1 ký tự đặc biệt.
     */
    private static final Pattern PASSWORD_STRONG =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    /**
     * Số điện thoại Việt Nam:
     * - Bắt đầu bằng 0 hoặc +84
     * - Theo sau là các đầu số di động phổ biến 3/5/7/8/9
     * - Tổng độ dài 10 số (nếu bắt đầu 0) hoặc 11 (nếu +84)
     * Ví dụ hợp lệ: 0912345678, +84912345678
     */
    private static final Pattern VN_PHONE =
            Pattern.compile("^(?:0|\\+84)(?:3|5|7|8|9)\\d{8}$");

    /**
     * Username: 4–20 ký tự, chữ/số/._, không bắt đầu hoặc kết thúc bằng . hoặc _,
     * không có 2 dấu . hoặc _ liên tiếp.
     */
    private static final Pattern USERNAME =
            Pattern.compile("^(?=.{4,20}$)(?![._])(?!.*[._]{2})[A-Za-z0-9._]+(?<![._])$");

    /**
     * Họ tên tiếng Việt (có dấu), cho phép khoảng trắng giữa các từ, tối đa 50 ký tự.
     * Chấp nhận ký tự chữ có dấu Latin mở rộng và dấu nháy đơn.
     */
    private static final Pattern FULL_NAME_VI =
            Pattern.compile("^(?=.{1,50}$)[\\p{L}][\\p{L}'\\p{M} ]*[\\p{L}]$");

    /**
     * Địa chỉ cơ bản: chữ/số/dấu câu thông dụng, cho phép khoảng trắng, 5–120 ký tự.
     * (Bạn có thể chỉnh cho phù hợp domain của bạn)
     */
    private static final Pattern ADDRESS =
            Pattern.compile("^[\\p{L}\\p{N}\\p{M}\\s,./#'\"()-]{5,120}$");

    /** Mã bưu chính (VNPost hiện dùng 5 hoặc 6 số). */
    private static final Pattern POSTAL_CODE =
            Pattern.compile("^\\d{5,6}$");

    /** URL cơ bản (http/https) */
    private static final Pattern URL =
            Pattern.compile("^(https?://)[\\w.-]+(?:\\:[0-9]{2,5})?(?:/[^\\s]*)?$", Pattern.CASE_INSENSITIVE);

    /** IPv4 đơn giản */
    private static final Pattern IPV4 =
            Pattern.compile("^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$");

    // ===== Public API =====

    public static boolean isEmail(String email) {
        return notBlank(email) && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isStrongPassword(String password) {
        return notBlank(password) && PASSWORD_STRONG.matcher(password).matches();
    }

    public static boolean isVietnamPhone(String phone) {
        return notBlank(phone) && VN_PHONE.matcher(phone.replaceAll("\\s+", "")).matches();
    }

    public static boolean isUsername(String username) {
        return notBlank(username) && USERNAME.matcher(username).matches();
    }

    public static boolean isFullNameVi(String fullName) {
        if (!notBlank(fullName)) return false;
        final String compact = fullName.trim().replaceAll("\\s{2,}", " ");
        return FULL_NAME_VI.matcher(compact).matches();
    }

    public static boolean isAddress(String address) {
        if (!notBlank(address)) return false;
        final String compact = address.trim().replaceAll("\\s{2,}", " ");
        return ADDRESS.matcher(compact).matches();
    }

    public static boolean isPostalCode(String code) {
        return notBlank(code) && POSTAL_CODE.matcher(code.trim()).matches();
    }

    public static boolean isUrl(String url) {
        return notBlank(url) && URL.matcher(url.trim()).matches();
    }

    public static boolean isIPv4(String ip) {
        return notBlank(ip) && IPV4.matcher(ip.trim()).matches();
    }


    /** Trim + chuẩn hoá khoảng trắng còn 1 dấu cách */
    public static String normalizeSpaces(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s{2,}", " ");
    }

    /** So sánh 2 chuỗi bỏ qua khoảng trắng thừa và phân biệt hoa thường */
    public static boolean equalsNormalized(String a, String b) {
        String na = normalizeSpaces(a);
        String nb = normalizeSpaces(b);
        return Objects.equals(na, nb);
    }
}