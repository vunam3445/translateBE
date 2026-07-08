package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Flashcard;
import com.example.translateserver.domain.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetFlashcardsUseCase {

    private final FlashcardRepository flashcardRepository;

    public List<Flashcard> execute() {
        return flashcardRepository.findAll();
    }
}
