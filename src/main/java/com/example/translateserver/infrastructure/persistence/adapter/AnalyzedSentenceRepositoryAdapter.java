package com.example.translateserver.infrastructure.persistence.adapter;

import com.example.translateserver.domain.model.AnalyzedSentence;
import com.example.translateserver.domain.repository.AnalyzedSentenceRepository;
import com.example.translateserver.infrastructure.persistence.entity.AnalyzedSentenceEntity;
import com.example.translateserver.infrastructure.persistence.mapper.AnalyzedSentenceMapper;
import com.example.translateserver.infrastructure.persistence.repository.SpringDataAnalyzedSentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AnalyzedSentenceRepositoryAdapter implements AnalyzedSentenceRepository {

    private final SpringDataAnalyzedSentenceRepository springDataRepository;

    @Override
    public AnalyzedSentence save(AnalyzedSentence sentence) {
        AnalyzedSentenceEntity entity = AnalyzedSentenceMapper.toEntity(sentence);
        AnalyzedSentenceEntity saved = springDataRepository.save(entity);
        return AnalyzedSentenceMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public List<AnalyzedSentence> saveAll(List<AnalyzedSentence> sentences) {
        List<AnalyzedSentenceEntity> entities = sentences.stream()
                .map(AnalyzedSentenceMapper::toEntity)
                .collect(Collectors.toList());
        List<AnalyzedSentenceEntity> saved = springDataRepository.saveAll(entities);
        return saved.stream()
                .map(AnalyzedSentenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyzedSentence> findByChapterId(Long chapterId) {
        return springDataRepository.findByChapterId(chapterId).stream()
                .map(AnalyzedSentenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AnalyzedSentence> findByChapterIdAndSentenceOrder(Long chapterId, int sentenceOrder) {
        return springDataRepository.findByChapterIdAndSentenceOrder(chapterId, sentenceOrder)
                .map(AnalyzedSentenceMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByChapterId(Long chapterId) {
        springDataRepository.deleteByChapterId(chapterId);
    }
}
