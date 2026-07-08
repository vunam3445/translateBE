package com.example.translateserver.infrastructure.persistence.repository;

import com.example.translateserver.infrastructure.persistence.entity.StoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataStoryRepository extends JpaRepository<StoryEntity, Long> {
}
