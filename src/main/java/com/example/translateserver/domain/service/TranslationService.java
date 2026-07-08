package com.example.translateserver.domain.service;

public interface TranslationService {

    /**
     * Dịch thô một đoạn truyện từ Tiếng Việt sang Tiếng Anh, kèm theo chỉ dẫn glossary.
     *
     * @param text Đoạn truyện cần dịch
     * @param glossaryPrompt Chuỗi prompt mô tả bảng thuật ngữ glossary
     * @return Đoạn truyện đã dịch sang tiếng Anh
     */
    String translateText(String text, String glossaryPrompt);

    /**
     * Phân tích cú pháp, thì (tense), nghĩa của từ trong câu tiếng Anh dựa trên ngữ cảnh tiếng Việt.
     * Trả về chuỗi định dạng JSON kết quả.
     *
     * @param viText Câu gốc tiếng Việt
     * @param enText Câu dịch tiếng Anh
     * @return JSON phân tích của câu và các từ quan trọng
     */
    String analyzeSentence(String viText, String enText);

    /**
     * Dịch tinh cụm từ tiếng Anh sang tiếng Việt dựa trên ngữ cảnh câu.
     * Trả về chuỗi định dạng JSON chứa nghĩa và ví dụ.
     *
     * @param phrase Cụm từ cần dịch
     * @param context Ngữ cảnh của cụm từ (câu chứa nó)
     * @return JSON {"m": "nghĩa", "ex": "ví dụ"}
     */
    String translatePhrase(String phrase, String context);
}
