package com.example.translateserver.infrastructure.persistence.repository;

import com.example.translateserver.infrastructure.persistence.entity.AudioMarkerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataAudioMarkerRepository extends JpaRepository<AudioMarkerEntity, Long> {
    List<AudioMarkerEntity> findByChapterId(Long chapterId);
    void deleteByChapterId(Long chapterId);
}
