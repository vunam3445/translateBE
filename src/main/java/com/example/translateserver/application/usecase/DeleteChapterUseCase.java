package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.model.Story;
import com.example.translateserver.domain.repository.ChapterRepository;
import com.example.translateserver.domain.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteChapterUseCase {

    private final ChapterRepository chapterRepository;
    private final StoryRepository storyRepository;

    @Transactional
    public boolean execute(Long storyId, Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương với ID: " + chapterId));

        if (!chapter.getStoryId().equals(storyId)) {
            throw new IllegalArgumentException("Chương này không thuộc về truyện có ID: " + storyId);
        }

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy truyện với ID: " + storyId));

        chapterRepository.deleteById(chapterId);

        story.setTotalChapters(Math.max(0, story.getTotalChapters() - 1));
        storyRepository.save(story);

        return true;
    }
}
