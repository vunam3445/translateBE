package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Story;
import com.example.translateserver.domain.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetStoriesUseCase {
    private final StoryRepository storyRepository;

    public List<Story> execute() {
        return storyRepository.findAll();
    }
}
