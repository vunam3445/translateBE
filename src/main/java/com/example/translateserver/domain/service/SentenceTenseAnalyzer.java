package com.example.translateserver.domain.service;

/**
 * Interface Domain Service: Phân tích thì (Tense) của một câu tiếng Anh.
 * Triển khai cụ thể sẽ nằm ở Infrastructure layer (OpenNlpTenseAnalyzer).
 * Thiết kế này tuân theo Dependency Inversion Principle (DIP) trong Clean Architecture.
 */
public interface SentenceTenseAnalyzer {

    /**
     * Phân tích thì ngữ pháp của một câu tiếng Anh.
     * Trả về chuỗi JSON chứa: tên thì, cấu trúc thì, dấu hiệu nhận biết,
     * giải thích lý do (tiếng Việt), và danh sách từ vựng kèm nhãn loại từ.
     *
     * @param englishSentence Câu tiếng Anh cần phân tích
     * @return JSON string chứa đầy đủ thông tin phân tích thì
     */
    String analyzeSentence(String englishSentence);
}
