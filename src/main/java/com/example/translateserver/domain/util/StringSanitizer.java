package com.example.translateserver.domain.util;

public class StringSanitizer {

    /**
     * Làm sạch và chuẩn hóa chuỗi ký tự:
     * 1. Thay thế tất cả các ký tự xuống dòng (\n, \r), ký tự tab (\t) bằng khoảng trắng đơn.
     * 2. Thu gọn nhiều khoảng trắng liên tiếp ở giữa thành 1 khoảng trắng duy nhất.
     * 3. Loại bỏ khoảng trắng dư thừa ở đầu và cuối chuỗi.
     *
     * @param input Chuỗi đầu vào
     * @return Chuỗi đã được chuẩn hóa, hoặc null nếu đầu vào là null.
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        // Thay thế ký tự xuống dòng, tab bằng khoảng trắng
        String sanitized = input.replaceAll("[\\r\\n\\t]+", " ");
        // Thay thế nhiều khoảng trắng liên tiếp bằng một khoảng trắng đơn và trim
        return sanitized.replaceAll("\\s+", " ").trim();
    }

    public static String stripAccents(String input) {
        if (input == null) {
            return null;
        }
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replace("đ", "d").replace("Đ", "D");
    }
}
