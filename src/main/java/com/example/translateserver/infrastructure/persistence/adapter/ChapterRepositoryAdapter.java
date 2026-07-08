package com.example.translateserver.infrastructure.persistence.adapter;

import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.repository.ChapterRepository;
import com.example.translateserver.infrastructure.persistence.entity.ChapterEntity;
import com.example.translateserver.infrastructure.persistence.mapper.ChapterMapper;
import com.example.translateserver.infrastructure.persistence.repository.SpringDataChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChapterRepositoryAdapter implements ChapterRepository {

    private final SpringDataChapterRepository springDataChapterRepository;

    @Override
    public Chapter save(Chapter chapter) {
        ChapterEntity entity;
        if (chapter.getId() != null) {
            entity = springDataChapterRepository.findById(chapter.getId())
                    .orElseGet(() -> ChapterMapper.toEntity(chapter));
            entity.setChapterNumber(chapter.getChapterNumber());
            entity.setTitle(chapter.getTitle());
            entity.setStatus(chapter.getStatus());
            entity.setRawContent(chapter.getRawContent());
            entity.setTranslatedContent(chapter.getTranslatedContent());
            entity.setTranslationProgress(chapter.getTranslationProgress());
        } else {
            entity = ChapterMapper.toEntity(chapter);
        }
        ChapterEntity saved = springDataChapterRepository.save(entity);
        return ChapterMapper.toDomain(saved);
    }

    @Override
    public Optional<Chapter> findById(Long id) {
        return springDataChapterRepository.findById(id)
                .map(ChapterMapper::toDomain);
    }

    @Override
    public Optional<Chapter> findByStoryIdAndChapterNumber(Long storyId, int chapterNumber) {
        return springDataChapterRepository.findByStoryIdAndChapterNumber(storyId, chapterNumber)
                .map(ChapterMapper::toDomain);
    }

    @Override
    public List<Chapter> findByStoryId(Long storyId) {
        return springDataChapterRepository.findByStoryId(storyId).stream()
                .map(ChapterMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataChapterRepository.deleteById(id);
    }

    @Override
    public Optional<Integer> findMaxChapterNumberByStoryId(Long storyId) {
        return springDataChapterRepository.findMaxChapterNumberByStoryId(storyId);
    }
}
