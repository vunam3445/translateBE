package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranslatePhraseUseCase {

    private final TranslationService translationService;

    /**
     * Dịch cụm từ tiếng Anh sang tiếng Việt dựa trên ngữ cảnh.
     *
     * @param phrase Cụm từ tiếng Anh (ví dụ: "take part in")
     * @param context Ngữ cảnh của câu chứa cụm từ đó
     * @return Chuỗi JSON kết quả phân tích cụm từ {"m": "nghĩa", "ex": "ví dụ"}
     */
    public String execute(String phrase, String context) {
        if (phrase == null || phrase.trim().isEmpty()) {
            return "{\"m\":\"\",\"ex\":\"\"}";
        }
        return translationService.translatePhrase(phrase, context);
    }
}
