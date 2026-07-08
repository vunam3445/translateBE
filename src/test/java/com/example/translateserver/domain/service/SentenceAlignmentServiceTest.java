package com.example.translateserver.domain.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SentenceAlignmentServiceTest {

    @Test
    public void testAlignmentWithPunctuationOnlySentences() throws Exception {
        // Load sample Vietnamese text (the first 20 lines of the chapter file)
        String viText = Files.readString(Paths.get("d:/Android/Translate/Chương 110 Trận pháp con đường.txt"));
        // Simulate a fake English translation where punctuation‑only sentences are kept as‑is
        // For simplicity we just replace Vietnamese words with "ENG" but keep punctuation unchanged.
        String enText = viText.replaceAll("[\\p{L}]+", "ENG");

        SentenceAlignmentService service = new SentenceAlignmentService();
        List<SentenceAlignmentService.AlignedPair> pairs = service.alignSentences(viText, enText);

        // 1. The number of aligned pairs must equal the number of original Vietnamese sentences.
        List<String> viSentences = service.splitToSentences(viText);
        assertEquals(viSentences.size(), pairs.size(), "Aligned pairs count should match Vietnamese sentences count");

        // 2. For any sentence that contains only punctuation, the English side should be identical.
        for (int i = 0; i < viSentences.size(); i++) {
            String vi = viSentences.get(i);
            String en = pairs.get(i).getEnSentence();
            if (!vi.matches(".*[a-zA-Z\\p{L}\\d].*")) { // punctuation‑only
                assertEquals(vi, en, "Punctuation‑only sentence should map to itself");
            }
        }
    }
}
