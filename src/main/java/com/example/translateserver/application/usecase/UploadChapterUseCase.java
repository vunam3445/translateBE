package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.model.ChapterStatus;
import com.example.translateserver.domain.model.Story;
import com.example.translateserver.domain.repository.ChapterRepository;
import com.example.translateserver.domain.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UploadChapterUseCase {

    private final ChapterRepository chapterRepository;
    private final StoryRepository storyRepository;

    @Transactional
    public Chapter execute(Long storyId, String title, String rawContent) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy truyện với ID: " + storyId));

        int nextChapterNumber = chapterRepository.findMaxChapterNumberByStoryId(storyId)
                .map(maxNum -> maxNum + 1)
                .orElse(1);

        Chapter chapter = Chapter.builder()
                .storyId(storyId)
                .chapterNumber(nextChapterNumber)
                .title(title)
                .rawContent(rawContent)
                .status(ChapterStatus.PENDING)
                .build();

        Chapter savedChapter = chapterRepository.save(chapter);

        story.setTotalChapters(story.getTotalChapters() + 1);
        storyRepository.save(story);

        return savedChapter;
    }
}
