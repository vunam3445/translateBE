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
     *
     * Chiến lược tách câu: theo DÒNG (newline) thay vì theo dấu chấm câu.
     * Lý do: mỗi dòng trong file truyện là một đoạn/câu logic; dấu "...", lời thoại
     * trong ngoặc kép không bị cắt sai. API dịch cũng thường dịch theo đơn vị dòng.
     *
     * Các câu chỉ chứa dấu câu (như "...", '"..."') được tách riêng và không
     * tham gia vào quá trình gióng hàng để tránh lệch pha (index shift).
     *
     * @param viText Văn bản gốc tiếng Việt
     * @param enText Văn bản dịch tiếng Anh
     * @return Danh sách các cặp câu gióng hàng, kích thước = số dòng tiếng Việt
     */
    public List<AlignedPair> alignSentences(String viText, String enText) {
        List<AlignedPair> alignedPairs = new ArrayList<>();
        if (viText == null || viText.trim().isEmpty() || enText == null || enText.trim().isEmpty()) {
            return alignedPairs;
        }

        List<String> viSentences = splitToSentences(viText);
        List<String> enSentences = splitToSentences(enText);

        // 1. Lọc ra các câu có chứa chữ thực tế (loại bỏ câu chỉ có dấu câu)
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

        // Nếu tiếng Anh nhiều hơn tiếng Việt, gộp phần thừa vào câu chứa chữ cuối cùng
        if (enTextSize > viTextSize && viTextSize > 0) {
            StringBuilder lastEn = new StringBuilder(alignedEnForVi.get(viTextSize - 1));
            for (int i = viTextSize; i < enTextSize; i++) {
                lastEn.append(" ").append(enTextSentences.get(i));
            }
            alignedEnForVi.set(viTextSize - 1, lastEn.toString());
        }

        // 3. Tái dựng kết quả cuối cùng từ danh sách viSentences gốc
        int textIndex = 0;
        for (String viSent : viSentences) {
            if (isPunctuationOnly(viSent)) {
                // Câu chỉ chứa dấu câu: giữ nguyên (không cần bản dịch tiếng Anh riêng)
                alignedPairs.add(new AlignedPair(viSent, viSent));
            } else {
                // Câu chứa chữ: lấy bản dịch tiếng Anh tương ứng
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

    /**
     * Kiểm tra xem một đoạn văn bản có chỉ chứa dấu câu hay không.
     * Nhận diện: "...", ".", "!", "?", '"..."', '...', dấu gạch ngang, v.v.
     */
    boolean isPunctuationOnly(String text) {
        if (text == null) return true;
        // Câu được coi là chỉ có dấu câu nếu không có chữ cái hoặc chữ số nào
        return !text.matches(".*[\\p{L}\\d].*");
    }

    /**
     * Tách văn bản theo DÒNG (newline).
     * Mỗi dòng không rỗng là một câu. Dấu ba chấm và lời thoại được giữ nguyên.
     */
    List<String> splitToSentences(String text) {
        List<String> sentences = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                sentences.add(trimmed);
            }
        }
        return sentences;
    }
}
