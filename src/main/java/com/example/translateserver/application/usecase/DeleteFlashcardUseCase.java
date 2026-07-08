package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Flashcard;
import com.example.translateserver.domain.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteFlashcardUseCase {

    private final FlashcardRepository flashcardRepository;

    public void execute(Long id) {
        flashcardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy flashcard với ID: " + id));
        flashcardRepository.deleteById(id);
    }

    public void execute(String word) {
        Flashcard flashcard = flashcardRepository.findByWord(word.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy flashcard với từ: " + word));
        flashcardRepository.deleteById(flashcard.getId());
    }
}
