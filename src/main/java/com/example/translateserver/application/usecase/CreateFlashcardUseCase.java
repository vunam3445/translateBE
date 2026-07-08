package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Flashcard;
import com.example.translateserver.domain.repository.FlashcardRepository;
import com.example.translateserver.infrastructure.web.dto.CreateFlashcardRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateFlashcardUseCase {

    private final FlashcardRepository flashcardRepository;

    public Flashcard execute(CreateFlashcardRequest request) {
        flashcardRepository.findByWord(request.getWord().trim()).ifPresent(f -> {
            throw new IllegalArgumentException("Từ vựng '" + request.getWord() + "' đã tồn tại trong flashcard!");
        });

        Flashcard flashcard = Flashcard.builder()
                .word(request.getWord().trim())
                .ipa(request.getIpa() != null ? request.getIpa().trim() : null)
                .meaning(request.getMeaning().trim())
                .exampleEn(request.getExampleEn() != null ? request.getExampleEn().trim() : null)
                .exampleVi(request.getExampleVi() != null ? request.getExampleVi().trim() : null)
                .boxLevel(0)
                .nextReviewDate(LocalDate.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        return flashcardRepository.save(flashcard);
    }
}
