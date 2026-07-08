package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.model.ChapterStatus;
import com.example.translateserver.domain.model.Story;
import com.example.translateserver.domain.repository.ChapterRepository;
import com.example.translateserver.domain.repository.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UploadChapterUseCaseTest {

    private ChapterRepository chapterRepository;
    private StoryRepository storyRepository;
    private UploadChapterUseCase uploadChapterUseCase;

    @BeforeEach
    void setUp() {
        chapterRepository = mock(ChapterRepository.class);
        storyRepository = mock(StoryRepository.class);
        uploadChapterUseCase = new UploadChapterUseCase(chapterRepository, storyRepository);
    }

    @Test
    void testUploadFirstChapterSuccess() {
        Long storyId = 1L;
        String title = "Chương mở đầu";
        String content = "Nội dung chương 1...";

        // Giả lập truyện có sẵn với 0 chương
        Story story = Story.builder()
                .id(storyId)
                .title("Truyện A")
                .totalChapters(0)
                .build();
        
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(chapterRepository.findMaxChapterNumberByStoryId(storyId)).thenReturn(Optional.empty());

        // Giả lập lưu chương thành công
        Chapter mockChapter = Chapter.builder()
                .id(100L)
                .storyId(storyId)
                .chapterNumber(1)
                .title(title)
                .rawContent(content)
                .status(ChapterStatus.PENDING)
                .build();
        when(chapterRepository.save(any(Chapter.class))).thenReturn(mockChapter);

        // Chạy UseCase
        Chapter result = uploadChapterUseCase.execute(storyId, title, content);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(1, result.getChapterNumber());
        assertEquals(ChapterStatus.PENDING, result.getStatus());
        assertEquals("Chương mở đầu", result.getTitle());
        assertEquals(content, result.getRawContent());

        // Đảm bảo truyện được cập nhật số chương và lưu lại
        verify(storyRepository, times(1)).save(story);
        assertEquals(1, story.getTotalChapters());
    }

    @Test
    void testUploadNextChapterSuccess() {
        Long storyId = 1L;
        String title = "Chương thứ hai";
        String content = "Nội dung chương 2...";

        // Giả lập truyện có sẵn với 1 chương
        Story story = Story.builder()
                .id(storyId)
                .title("Truyện A")
                .totalChapters(1)
                .build();
        
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        // Đã có chương 1 lớn nhất
        when(chapterRepository.findMaxChapterNumberByStoryId(storyId)).thenReturn(Optional.of(1));

        // Giả lập lưu chương thành công với số chương 2
        Chapter mockChapter = Chapter.builder()
                .id(101L)
                .storyId(storyId)
                .chapterNumber(2)
                .title(title)
                .rawContent(content)
                .status(ChapterStatus.PENDING)
                .build();
        when(chapterRepository.save(any(Chapter.class))).thenReturn(mockChapter);

        // Chạy UseCase
        Chapter result = uploadChapterUseCase.execute(storyId, title, content);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(2, result.getChapterNumber()); // Phải là chương 2
        assertEquals(101L, result.getId());

        // Đảm bảo truyện được cập nhật số chương từ 1 lên 2 và lưu lại
        verify(storyRepository, times(1)).save(story);
        assertEquals(2, story.getTotalChapters());
    }

    @Test
    void testUploadChapterStoryNotFoundThrowsException() {
        Long storyId = 999L;
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            uploadChapterUseCase.execute(storyId, "Chương 1", "Nội dung");
        });

        verify(chapterRepository, never()).save(any(Chapter.class));
        verify(storyRepository, never()).save(any(Story.class));
    }
}
