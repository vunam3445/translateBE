package com.example.translateserver.domain.repository;

import com.example.translateserver.domain.model.AudioMarker;

import java.util.List;

public interface AudioMarkerRepository {
    AudioMarker save(AudioMarker marker);
    List<AudioMarker> saveAll(List<AudioMarker> markers);
    List<AudioMarker> findByChapterId(Long chapterId);
    void deleteByChapterId(Long chapterId);
}
