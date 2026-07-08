package com.example.translateserver.infrastructure.web.controller;

import com.example.translateserver.application.usecase.CreateFlashcardUseCase;
import com.example.translateserver.application.usecase.DeleteFlashcardUseCase;
import com.example.translateserver.application.usecase.GetFlashcardsUseCase;
import com.example.translateserver.domain.model.Flashcard;
import com.example.translateserver.infrastructure.web.dto.CreateFlashcardRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
@Slf4j
public class FlashcardController {

    private final GetFlashcardsUseCase getFlashcardsUseCase;
    private final CreateFlashcardUseCase createFlashcardUseCase;
    private final DeleteFlashcardUseCase deleteFlashcardUseCase;

    @GetMapping
    public ResponseEntity<List<Flashcard>> getAllFlashcards() {
        return ResponseEntity.ok(getFlashcardsUseCase.execute());
    }

    @PostMapping
    public ResponseEntity<Flashcard> createFlashcard(@Valid @RequestBody CreateFlashcardRequest request) {
        log.info("Yêu cầu tạo flashcard cho từ: {}", request.getWord());
        Flashcard created = createFlashcardUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashcardById(@PathVariable Long id) {
        log.info("Yêu cầu xóa flashcard ID: {}", id);
        deleteFlashcardUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/word/{word}")
    public ResponseEntity<Void> deleteFlashcardByWord(@PathVariable String word) {
        log.info("Yêu cầu xóa flashcard với từ: {}", word);
        deleteFlashcardUseCase.execute(word);
        return ResponseEntity.noContent().build();
    }
}
