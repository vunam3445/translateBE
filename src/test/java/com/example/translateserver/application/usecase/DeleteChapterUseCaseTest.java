package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.model.ChapterStatus;
import com.example.translateserver.domain.model.Story;
import com.example.translateserver.domain.repository.ChapterRepository;
import com.example.translateserver.domain.repository.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeleteChapterUseCaseTest {

    private ChapterRepository chapterRepository;
    private StoryRepository storyRepository;
    private DeleteChapterUseCase deleteChapterUseCase;

    @BeforeEach
    void setUp() {
        chapterRepository = mock(ChapterRepository.class);
        storyRepository = mock(StoryRepository.class);
        deleteChapterUseCase = new DeleteChapterUseCase(chapterRepository, storyRepository);
    }

    @Test
    void testDeleteChapterSuccess() {
        Long storyId = 1L;
        Long chapterId = 100L;

        // Giả lập chapter cần xóa thuộc storyId 1
        Chapter chapter = Chapter.builder()
                .id(chapterId)
                .storyId(storyId)
                .chapterNumber(1)
                .title("Chương 1")
                .status(ChapterStatus.PENDING)
                .build();

        // Giả lập truyện có 2 chương
        Story story = Story.builder()
                .id(storyId)
                .title("Truyện A")
                .totalChapters(2)
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        // Chạy UseCase
        boolean result = deleteChapterUseCase.execute(storyId, chapterId);

        // Xác minh kết quả
        assertTrue(result);
        verify(chapterRepository, times(1)).deleteById(chapterId);
        verify(storyRepository, times(1)).save(story);
        assertEquals(1, story.getTotalChapters()); // 2 - 1 = 1
    }

    @Test
    void testDeleteChapterNotFoundThrowsException() {
        Long storyId = 1L;
        Long chapterId = 999L;

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            deleteChapterUseCase.execute(storyId, chapterId);
        });

        verify(chapterRepository, never()).deleteById(anyLong());
        verify(storyRepository, never()).save(any(Story.class));
    }

    @Test
    void testDeleteChapterStoryMismatchThrowsException() {
        Long storyId = 1L;
        Long chapterId = 100L;
        Long otherStoryId = 2L;

        // Chapter thực chất thuộc về storyId 2 chứ không phải 1
        Chapter chapter = Chapter.builder()
                .id(chapterId)
                .storyId(otherStoryId)
                .chapterNumber(1)
                .title("Chương 1")
                .status(ChapterStatus.PENDING)
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));

        assertThrows(IllegalArgumentException.class, () -> {
            deleteChapterUseCase.execute(storyId, chapterId);
        });

        verify(chapterRepository, never()).deleteById(anyLong());
        verify(storyRepository, never()).save(any(Story.class));
    }

    @Test
    void testDeleteChapterStoryNotFoundThrowsException() {
        Long storyId = 1L;
        Long chapterId = 100L;

        Chapter chapter = Chapter.builder()
                .id(chapterId)
                .storyId(storyId)
                .chapterNumber(1)
                .title("Chương 1")
                .status(ChapterStatus.PENDING)
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            deleteChapterUseCase.execute(storyId, chapterId);
        });

        verify(chapterRepository, never()).deleteById(anyLong());
        verify(storyRepository, never()).save(any(Story.class));
    }
}
