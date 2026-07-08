package com.example.translateserver.domain.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

    /**
     * Chia nhỏ văn bản tiếng Việt thành các đoạn nhỏ khoảng 800 - 1000 chữ.
     * Cắt dựa trên dấu xuống dòng (\n) hoặc dấu chấm câu để tránh cắt đôi câu văn.
     *
     * @param text Văn bản chương truyện cần cắt
     * @param targetSize Số lượng từ (words) lý tưởng cho mỗi chunk (mặc định ~900)
     * @return Danh sách các chunk văn bản
     */
    public List<String> chunkText(String text, int targetSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        // Tách văn bản thành các đoạn (paragraphs) dựa trên ký tự xuống dòng
        String[] paragraphs = text.split("\n");
        StringBuilder currentChunk = new StringBuilder();
        int currentWordCount = 0;

        for (String paragraph : paragraphs) {
            String trimmedParagraph = paragraph.trim();
            if (trimmedParagraph.isEmpty()) {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n");
                }
                continue;
            }

            int paragraphWordCount = countWords(trimmedParagraph);

            // Nếu một đoạn văn quá dài hơn cả targetSize, ta phải cắt nó theo câu
            if (paragraphWordCount > targetSize) {
                // Đẩy chunk hiện tại đi nếu có
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentWordCount = 0;
                }

                // Chia đoạn văn dài thành các câu
                List<String> sentences = splitIntoSentences(trimmedParagraph);
                for (String sentence : sentences) {
                    int sentenceWordCount = countWords(sentence);
                    if (currentWordCount + sentenceWordCount > targetSize && currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString().trim());
                        currentChunk = new StringBuilder();
                        currentWordCount = 0;
                    }
                    if (currentChunk.length() > 0) {
                        currentChunk.append(" ");
                    }
                    currentChunk.append(sentence);
                    currentWordCount += sentenceWordCount;
                }
                
                // Hết đoạn văn dài, nếu chunk có dữ liệu lớn hơn 70% targetSize thì đẩy đi luôn để giữ ranh giới paragraph
                if (currentWordCount > targetSize * 0.7) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentWordCount = 0;
                }
            } else {
                // Nếu thêm paragraph này vào vượt quá targetSize, đẩy chunk cũ đi
                if (currentWordCount + paragraphWordCount > targetSize && currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentWordCount = 0;
                }

                if (currentChunk.length() > 0) {
                    currentChunk.append("\n");
                }
                currentChunk.append(trimmedParagraph);
                currentWordCount += paragraphWordCount;
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    public List<String> chunkText(String text) {
        return chunkText(text, 900); // Mặc định target size là 900 từ
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private List<String> splitIntoSentences(String paragraph) {
        List<String> sentences = new ArrayList<>();
        // Tách câu bằng regex giữ lại dấu chấm câu [.!?]
        // Match bất kỳ ký tự nào cho tới khi gặp [.!?] kèm theo khoảng trắng hoặc kết thúc chuỗi
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[^.!?]+[.!?]?");
        java.util.regex.Matcher matcher = pattern.matcher(paragraph);
        while (matcher.find()) {
            String sentence = matcher.group().trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }
        if (sentences.isEmpty() && !paragraph.isEmpty()) {
            sentences.add(paragraph);
        }
        return sentences;
    }
}
