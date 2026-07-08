package com.example.translateserver.infrastructure.persistence.repository;

import com.example.translateserver.infrastructure.persistence.entity.StoryGlossaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataStoryGlossaryRepository extends JpaRepository<StoryGlossaryEntity, Long> {
    List<StoryGlossaryEntity> findByStoryId(Long storyId);
    Optional<StoryGlossaryEntity> findByStoryIdAndViTerm(Long storyId, String viTerm);
}
