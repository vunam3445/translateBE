package com.example.translateserver.infrastructure.persistence.adapter;

import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.domain.repository.StoryGlossaryRepository;
import com.example.translateserver.infrastructure.persistence.entity.StoryGlossaryEntity;
import com.example.translateserver.infrastructure.persistence.mapper.StoryGlossaryMapper;
import com.example.translateserver.infrastructure.persistence.repository.SpringDataStoryGlossaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StoryGlossaryRepositoryAdapter implements StoryGlossaryRepository {

    private final SpringDataStoryGlossaryRepository springDataRepository;

    @Override
    public StoryGlossary save(StoryGlossary glossary) {
        StoryGlossaryEntity entity = StoryGlossaryMapper.toEntity(glossary);
        StoryGlossaryEntity saved = springDataRepository.save(entity);
        return StoryGlossaryMapper.toDomain(saved);
    }

    @Override
    public Optional<StoryGlossary> findById(Long id) {
        return springDataRepository.findById(id)
                .map(StoryGlossaryMapper::toDomain);
    }

    @Override
    public List<StoryGlossary> findByStoryId(Long storyId) {
        return springDataRepository.findByStoryId(storyId).stream()
                .map(StoryGlossaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StoryGlossary> findByStoryIdAndViTerm(Long storyId, String viTerm) {
        return springDataRepository.findByStoryIdAndViTerm(storyId, viTerm)
                .map(StoryGlossaryMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
