package com.example.translateserver.domain.repository;

import com.example.translateserver.domain.model.Flashcard;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlashcardRepository {
    Flashcard save(Flashcard flashcard);
    Optional<Flashcard> findById(Long id);
    Optional<Flashcard> findByWord(String word);
    List<Flashcard> findDueForReview(LocalDate date);
    List<Flashcard> findAll();
    void deleteById(Long id);
}
