package com.example.translateserver.domain.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SentenceAlignmentService {

    public static class AlignedPair {
        private final String viSentence;
        private final String enSentence;

        public AlignedPair(String viSentence, String enSentence) {
            this.viSentence = viSentence;
            this.enSentence = enSentence;
        }

        public String getViSentence() {
            return viSentence;
        }

        public String getEnSentence() {
            return enSentence;
        }
    }

    /**
     * Gióng hàng các câu giữa tiếng Việt gốc và tiếng Anh đã dịch.
     * Thuật toán: Tách câu theo dấu chấm câu, sau đó map 1:1.
     * Nếu độ dài danh sách câu khác nhau, ta sẽ gom các câu dư thừa lại vào câu cuối
     * để đảm bảo cấu trúc dữ liệu không bị mất hoặc lệch pha.
     *
     * @param viText Văn bản gốc tiếng Việt
     * @param enText Văn bản dịch tiếng Anh
     * @return Danh sách các cặp câu gióng hàng
     */
    public List<AlignedPair> alignSentences(String viText, String enText) {
        List<AlignedPair> alignedPairs = new ArrayList<>();
        if (viText == null || viText.trim().isEmpty() || enText == null || enText.trim().isEmpty()) {
            return alignedPairs;
        }

        List<String> viSentences = splitToSentences(viText);
        List<String> enSentences = splitToSentences(enText);

        // 1. Phân tách câu chứa chữ thực tế
        List<String> viTextSentences = new ArrayList<>();
        for (String s : viSentences) {
            if (!isPunctuationOnly(s)) {
                viTextSentences.add(s);
            }
        }

        List<String> enTextSentences = new ArrayList<>();
        for (String s : enSentences) {
            if (!isPunctuationOnly(s)) {
                enTextSentences.add(s);
            }
        }

        // 2. Gióng hàng 1-1 cho các câu chứa chữ
        List<String> alignedEnForVi = new ArrayList<>();
        int viTextSize = viTextSentences.size();
        int enTextSize = enTextSentences.size();

        for (int i = 0; i < viTextSize; i++) {
            if (i < enTextSize) {
                alignedEnForVi.add(enTextSentences.get(i));
            } else {
                alignedEnForVi.add("");
            }
        }

        // Nếu tiếng Anh nhiều hơn tiếng Việt, gộp phần thừa vào câu chứa chữ cuối cùng của tiếng Việt
        if (enTextSize > viTextSize && viTextSize > 0) {
            StringBuilder lastEn = new StringBuilder(alignedEnForVi.get(viTextSize - 1));
            for (int i = viTextSize; i < enTextSize; i++) {
                lastEn.append(" ").append(enTextSentences.get(i));
            }
            alignedEnForVi.set(viTextSize - 1, lastEn.toString());
        }

        // 3. Tái dựng kết quả cuối cùng dựa trên danh sách viSentences gốc
        int textIndex = 0;
        for (String viSent : viSentences) {
            if (isPunctuationOnly(viSent)) {
                // Nếu là câu chỉ chứa dấu câu, gán luôn bằng chính câu gốc
                alignedPairs.add(new AlignedPair(viSent, viSent));
            } else {
                // Nếu là câu chứa chữ, lấy câu tiếng Anh tương ứng từ kết quả gióng hàng
                if (textIndex < alignedEnForVi.size()) {
                    alignedPairs.add(new AlignedPair(viSent, alignedEnForVi.get(textIndex)));
                    textIndex++;
                } else {
                    alignedPairs.add(new AlignedPair(viSent, ""));
                }
            }
        }

        return alignedPairs;
    }

    private boolean isPunctuationOnly(String text) {
        if (text == null) return true;
        return !text.matches(".*[a-zA-Z\\p{L}\\d].*");
    }

    private List<String> splitToSentences(String text) {
        List<String> sentences = new ArrayList<>();
        // Tách câu bằng dấu chấm, hỏi, than và khoảng trắng theo sau
        String[] split = text.split("(?<=[.!?])\\s+");
        for (String s : split) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                sentences.add(trimmed);
            }
        }
        return sentences;
    }
}
