package com.example.translateserver.infrastructure.persistence.adapter;

import com.example.translateserver.domain.model.Flashcard;
import com.example.translateserver.domain.repository.FlashcardRepository;
import com.example.translateserver.infrastructure.persistence.entity.FlashcardEntity;
import com.example.translateserver.infrastructure.persistence.mapper.FlashcardMapper;
import com.example.translateserver.infrastructure.persistence.repository.SpringDataFlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FlashcardRepositoryAdapter implements FlashcardRepository {

    private final SpringDataFlashcardRepository springDataRepository;

    @Override
    public Flashcard save(Flashcard flashcard) {
        FlashcardEntity entity = FlashcardMapper.toEntity(flashcard);
        FlashcardEntity saved = springDataRepository.save(entity);
        return FlashcardMapper.toDomain(saved);
    }

    @Override
    public Optional<Flashcard> findById(Long id) {
        return springDataRepository.findById(id)
                .map(FlashcardMapper::toDomain);
    }

    @Override
    public Optional<Flashcard> findByWord(String word) {
        return springDataRepository.findByWord(word)
                .map(FlashcardMapper::toDomain);
    }

    @Override
    public List<Flashcard> findDueForReview(LocalDate date) {
        return springDataRepository.findByNextReviewDateLessThanEqual(date).stream()
                .map(FlashcardMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Flashcard> findAll() {
        return springDataRepository.findAll().stream()
                .map(FlashcardMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
