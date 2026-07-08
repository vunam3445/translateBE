package com.example.translateserver.domain.util;

import java.util.HashMap;
import java.util.Map;

public class PinyinConverter {
    private static final Map<String, String> SYLLABLE_MAP = new HashMap<>();

    static {
        SYLLABLE_MAP.put("lâm", "lin");
        SYLLABLE_MAP.put("phong", "feng");
        SYLLABLE_MAP.put("hắc", "hei");
        SYLLABLE_MAP.put("tiêu", "xiao");
        SYLLABLE_MAP.put("phàm", "fan");
        SYLLABLE_MAP.put("diệp", "ye");
        SYLLABLE_MAP.put("trần", "chen");
        SYLLABLE_MAP.put("nguyễn", "ruan");
        SYLLABLE_MAP.put("lê", "li");
        SYLLABLE_MAP.put("vương", "wang");
        SYLLABLE_MAP.put("đường", "tang");
        SYLLABLE_MAP.put("bách", "bai");
        SYLLABLE_MAP.put("thảo", "cao");
        SYLLABLE_MAP.put("dương", "yang");
        SYLLABLE_MAP.put("long", "long");
        SYLLABLE_MAP.put("vân", "yun");
        SYLLABLE_MAP.put("phượng", "feng");
        SYLLABLE_MAP.put("tuyết", "xue");
        SYLLABLE_MAP.put("sơn", "shan");
        SYLLABLE_MAP.put("thủy", "shui");
        SYLLABLE_MAP.put("hỏa", "huo");
        SYLLABLE_MAP.put("mộc", "mu");
        SYLLABLE_MAP.put("kim", "jin");
        SYLLABLE_MAP.put("thổ", "tu");
        SYLLABLE_MAP.put("vô", "wu");
        SYLLABLE_MAP.put("cực", "ji");
        SYLLABLE_MAP.put("kiếm", "jian");
        SYLLABLE_MAP.put("tông", "zong");
        SYLLABLE_MAP.put("thiên", "tian");
        SYLLABLE_MAP.put("địa", "di");
        SYLLABLE_MAP.put("huyền", "xuan");
        SYLLABLE_MAP.put("hoàng", "huang");
        SYLLABLE_MAP.put("tần", "qin");
        SYLLABLE_MAP.put("bạch", "bai");
        SYLLABLE_MAP.put("nam", "nan");
        SYLLABLE_MAP.put("bắc", "bei");
        SYLLABLE_MAP.put("đông", "dong");
        SYLLABLE_MAP.put("tây", "xi");
    }

    public static String convertToPinyin(String hanVietText) {
        if (hanVietText == null || hanVietText.trim().isEmpty()) {
            return hanVietText;
        }
        String[] words = hanVietText.split("\\s+");
        StringBuilder pinyin = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            String pinyinWord = SYLLABLE_MAP.getOrDefault(word, StringSanitizer.stripAccents(word));
            
            if (Character.isUpperCase(words[i].charAt(0))) {
                pinyinWord = Character.toUpperCase(pinyinWord.charAt(0)) + pinyinWord.substring(1);
            }
            pinyin.append(pinyinWord);
            if (i < words.length - 1) {
                pinyin.append(" ");
            }
        }
        return pinyin.toString();
    }
}
