package com.example.translateserver.domain.repository;

import com.example.translateserver.domain.model.Story;

import java.util.List;
import java.util.Optional;

public interface StoryRepository {
    Story save(Story story);
    Optional<Story> findById(Long id);
    List<Story> findAll();
    void deleteById(Long id);
}
