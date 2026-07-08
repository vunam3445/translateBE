package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetChaptersUseCase {

    private final ChapterRepository chapterRepository;

    public List<Chapter> execute(Long storyId) {
        return chapterRepository.findByStoryId(storyId);
    }
}
