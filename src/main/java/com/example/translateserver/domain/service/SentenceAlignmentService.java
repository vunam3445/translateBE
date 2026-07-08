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

        int viSize = viSentences.size();
        int enSize = enSentences.size();

        if (viSize == enSize) {
            for (int i = 0; i < viSize; i++) {
                alignedPairs.add(new AlignedPair(viSentences.get(i), enSentences.get(i)));
            }
        } else if (viSize > enSize) {
            // Nhiều câu tiếng Việt hơn tiếng Anh. Gộp các câu tiếng Việt thừa vào câu cuối.
            for (int i = 0; i < enSize - 1; i++) {
                alignedPairs.add(new AlignedPair(viSentences.get(i), enSentences.get(i)));
            }
            // Gộp phần còn lại của tiếng Việt
            StringBuilder lastVi = new StringBuilder();
            for (int i = enSize - 1; i < viSize; i++) {
                if (lastVi.length() > 0) {
                    lastVi.append(" ");
                }
                lastVi.append(viSentences.get(i));
            }
            alignedPairs.add(new AlignedPair(lastVi.toString(), enSentences.get(enSize - 1)));
        } else {
            // Nhiều câu tiếng Anh hơn tiếng Việt. Gộp các câu tiếng Anh thừa vào câu cuối.
            for (int i = 0; i < viSize - 1; i++) {
                alignedPairs.add(new AlignedPair(viSentences.get(i), enSentences.get(i)));
            }
            // Gộp phần còn lại của tiếng Anh
            StringBuilder lastEn = new StringBuilder();
            for (int i = viSize - 1; i < enSize; i++) {
                if (lastEn.length() > 0) {
                    lastEn.append(" ");
                }
                lastEn.append(enSentences.get(i));
            }
            alignedPairs.add(new AlignedPair(viSentences.get(viSize - 1), lastEn.toString()));
        }

        return alignedPairs;
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
