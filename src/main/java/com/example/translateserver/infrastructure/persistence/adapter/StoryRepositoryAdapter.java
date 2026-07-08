package com.example.translateserver.infrastructure.persistence.adapter;

import com.example.translateserver.domain.model.Story;
import com.example.translateserver.domain.repository.StoryRepository;
import com.example.translateserver.infrastructure.persistence.entity.StoryEntity;
import com.example.translateserver.infrastructure.persistence.mapper.StoryMapper;
import com.example.translateserver.infrastructure.persistence.repository.SpringDataStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StoryRepositoryAdapter implements StoryRepository {

    private final SpringDataStoryRepository springDataStoryRepository;

    @Override
    public Story save(Story story) {
        StoryEntity entity;
        if (story.getId() != null) {
            entity = springDataStoryRepository.findById(story.getId())
                    .orElseGet(() -> StoryMapper.toEntity(story));
            entity.setTitle(story.getTitle());
            entity.setTotalChapters(story.getTotalChapters());
        } else {
            entity = StoryMapper.toEntity(story);
        }
        StoryEntity saved = springDataStoryRepository.save(entity);
        return StoryMapper.toDomain(saved);
    }

    @Override
    public Optional<Story> findById(Long id) {
        return springDataStoryRepository.findById(id)
                .map(StoryMapper::toDomain);
    }

    @Override
    public List<Story> findAll() {
        return springDataStoryRepository.findAll().stream()
                .map(StoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataStoryRepository.deleteById(id);
    }
}
