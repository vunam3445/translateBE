package com.example.translateserver.domain.service;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SentenceAlignmentServiceTest {

    private final SentenceAlignmentService service = new SentenceAlignmentService();

    // ────────────────────────────────────────────────────────────────
    // 1. Test tách câu theo dòng
    // ────────────────────────────────────────────────────────────────
    @Test
    public void testSplitToSentencesByLine() {
        String text = "Dòng một.\nDòng hai.\n\nDòng ba.";
        List<String> sentences = service.splitToSentences(text);
        assertEquals(3, sentences.size());
        assertEquals("Dòng một.", sentences.get(0));
        assertEquals("Dòng hai.", sentences.get(1));
        assertEquals("Dòng ba.", sentences.get(2));
    }

    // ────────────────────────────────────────────────────────────────
    // 2. Test isPunctuationOnly
    // ────────────────────────────────────────────────────────────────
    @Test
    public void testIsPunctuationOnly() {
        assertTrue(service.isPunctuationOnly("..."));
        assertTrue(service.isPunctuationOnly("…"));
        assertTrue(service.isPunctuationOnly("... ... ..."));
        assertTrue(service.isPunctuationOnly("\"...\""));
        assertTrue(service.isPunctuationOnly("'...'"));
        assertTrue(service.isPunctuationOnly(". . ."));
        assertFalse(service.isPunctuationOnly("Xin chào."));
        assertFalse(service.isPunctuationOnly("Hello world!"));
        assertFalse(service.isPunctuationOnly("\"Ta cần phải đi.\""));
    }

    // ────────────────────────────────────────────────────────────────
    // 3. Test căn chỉnh câu cơ bản (số câu bằng nhau)
    // ────────────────────────────────────────────────────────────────
    @Test
    public void testAlignmentEqual() {
        String vi = "Câu một.\nCâu hai.";
        String en = "Sentence one.\nSentence two.";
        List<SentenceAlignmentService.AlignedPair> pairs = service.alignSentences(vi, en);
        assertEquals(2, pairs.size());
        assertEquals("Sentence one.", pairs.get(0).getEnSentence());
        assertEquals("Sentence two.", pairs.get(1).getEnSentence());
    }

    // ────────────────────────────────────────────────────────────────
    // 4. Test: câu chỉ dấu câu không làm lệch pha
    // ────────────────────────────────────────────────────────────────
    @Test
    public void testPunctuationOnlyDoesNotShiftAlignment() {
        // Tiếng Việt có 3 dòng: dòng thật, dòng chỉ "...", dòng thật
        // Tiếng Anh chỉ có 2 dòng (API bỏ qua "...")
        String vi = "Anh ấy mỉm cười.\n...\nCô gái gật đầu.";
        String en = "He smiled.\nThe girl nodded.";

        List<SentenceAlignmentService.AlignedPair> pairs = service.alignSentences(vi, en);

        // Phải có 3 cặp (= số dòng vi)
        assertEquals(3, pairs.size());

        // Cặp 1: dòng thật → bản dịch đúng
        assertEquals("Anh ấy mỉm cười.", pairs.get(0).getViSentence());
        assertEquals("He smiled.", pairs.get(0).getEnSentence());

        // Cặp 2: dòng dấu câu → chính nó
        assertEquals("...", pairs.get(1).getViSentence());
        assertEquals("...", pairs.get(1).getEnSentence());

        // Cặp 3: dòng thật → bản dịch đúng (không bị lệch)
        assertEquals("Cô gái gật đầu.", pairs.get(2).getViSentence());
        assertEquals("The girl nodded.", pairs.get(2).getEnSentence());
    }

    // ────────────────────────────────────────────────────────────────
    // 5. Test với file thực tế (nếu file tồn tại trên máy dev)
    // ────────────────────────────────────────────────────────────────
    @Test
    public void testAlignmentWithRealChapterFile() throws IOException {
        java.nio.file.Path filePath = Paths.get("d:/Android/Translate/Chương 110 Trận pháp con đường.txt");
        if (!Files.exists(filePath)) {
            System.out.println("[SKIP] File không tồn tại: " + filePath);
            return;
        }

        String viText = Files.readString(filePath);
        // Giả lập bản dịch bằng cách thay mỗi từ Unicode thành "ENG"
        String enText = viText.replaceAll("[\\p{L}]+", "ENG");

        List<SentenceAlignmentService.AlignedPair> pairs = service.alignSentences(viText, enText);
        List<String> viSentences = service.splitToSentences(viText);

        // Số cặp phải bằng số dòng tiếng Việt
        assertEquals(viSentences.size(), pairs.size(),
                "Số cặp phải bằng số dòng tiếng Việt gốc");

        // Không có câu chứa chữ nào mà enSentence = viSentence (tức không bị "dịch thành tiếng Việt")
        for (SentenceAlignmentService.AlignedPair pair : pairs) {
            boolean isViOnly = service.isPunctuationOnly(pair.getViSentence());
            if (!isViOnly) {
                assertNotEquals(pair.getViSentence(), pair.getEnSentence(),
                        "Câu có chữ không được có enSentence = viSentence (chỉ dấu câu mới được): "
                                + pair.getViSentence());
            }
        }
    }
}
