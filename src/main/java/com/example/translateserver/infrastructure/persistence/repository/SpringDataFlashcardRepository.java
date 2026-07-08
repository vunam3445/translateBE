package com.example.translateserver.infrastructure.persistence.repository;

import com.example.translateserver.infrastructure.persistence.entity.FlashcardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataFlashcardRepository extends JpaRepository<FlashcardEntity, Long> {
    Optional<FlashcardEntity> findByWord(String word);
    List<FlashcardEntity> findByNextReviewDateLessThanEqual(LocalDate date);
}
