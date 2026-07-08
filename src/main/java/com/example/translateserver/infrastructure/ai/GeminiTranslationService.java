package com.example.translateserver.infrastructure.ai;

import com.example.translateserver.domain.service.TranslationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiTranslationService implements TranslationService {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Value("${spring.ai.google.genai.model:gemini-2.5-flash}")
    private String modelName;

    @Value("${spring.ai.google.genai.fallback-model:gemini-3.5-flash}")
    private String fallbackModelName;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=";

    @Override
    public String translateText(String text, String glossaryPrompt) {
        Map<String, String> glossaryMap = parseGlossary(glossaryPrompt);
        String preprocessedText = applyGlossary(text, glossaryMap);

        try {
            log.info("Đang dịch text bằng Google Translate GTX API (POST)...");
            return translateWithGtx(preprocessedText);
        } catch (Exception e) {
            log.warn("Dịch bằng GTX thất bại ({}). Đang thử fallback sang Gemini...", e.getMessage());
            String prompt = "Dịch đoạn truyện sau sang tiếng Anh, giữ nguyên các khoảng xuống dòng.\n";
            if (glossaryPrompt != null && !glossaryPrompt.trim().isEmpty()) {
                prompt += "Sử dụng bảng thuật ngữ sau để dịch đúng ngữ cảnh (nếu xuất hiện):\n" + glossaryPrompt + "\n";
            }
            prompt += "Văn bản cần dịch:\n" + text;

            try {
                return callGemini(prompt, false);
            } catch (Exception ex) {
                log.error("Cả GTX và Gemini fallback đều thất bại", ex);
                throw new RuntimeException("Dịch chương truyện thất bại: " + ex.getMessage(), ex);
            }
        }
    }

    private String translateWithGtx(String text) throws Exception {
        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=vi&tl=en&dt=t";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("q", text);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        // Giãn cách 1.5 giây để tránh bị Google chặn IP
        Thread.sleep(1500);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return parseGtxResponse(response.getBody());
        } else {
            throw new RuntimeException("GTX API trả về mã lỗi: " + response.getStatusCode());
        }
    }

    private String parseGtxResponse(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        if (root.isArray() && root.size() > 0) {
            JsonNode segments = root.get(0);
            if (segments.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode segment : segments) {
                    if (segment.isArray() && segment.size() > 0) {
                        sb.append(segment.get(0).asText());
                    }
                }
                return sb.toString();
            }
        }
        throw new RuntimeException("Phản hồi từ GTX không đúng định dạng mong đợi");
    }

    private Map<String, String> parseGlossary(String glossaryPrompt) {
        Map<String, String> glossaryMap = new HashMap<>();
        if (glossaryPrompt == null || glossaryPrompt.trim().isEmpty()) {
            return glossaryMap;
        }
        String[] lines = glossaryPrompt.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("- ")) {
                String content = trimmed.substring(2);
                String[] parts = content.split("->");
                if (parts.length == 2) {
                    glossaryMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return glossaryMap;
    }

    private String applyGlossary(String text, Map<String, String> glossaryMap) {
        if (glossaryMap.isEmpty() || text == null || text.isEmpty()) {
            return text;
        }
        // Sắp xếp key theo độ dài giảm dần để thay thế cụm từ dài trước tránh tranh chấp cụm từ ngắn
        List<String> sortedKeys = new ArrayList<>(glossaryMap.keySet());
        sortedKeys.sort((a, b) -> Integer.compare(b.length(), a.length()));

        String result = text;
        for (String key : sortedKeys) {
            String value = glossaryMap.get(key);
            result = result.replace(key, value);
        }
        return result;
    }

    @Override
    public String analyzeSentence(String viText, String enText) {
        String prompt = "Phân tích câu tiếng Anh sau dựa trên câu gốc tiếng Việt.\n" +
                "Câu gốc tiếng Việt: \"" + viText + "\"\n" +
                "Câu tiếng Anh: \"" + enText + "\"\n\n" +
                "Hãy trả về định dạng JSON duy nhất, có cấu trúc như sau (chú ý viết tắt các key để tối ưu token):\n" +
                "{\n" +
                "  \"tense\": \"Tên thì tiếng Anh (ví dụ: Present Simple, Past Continuous...)\",\n" +
                "  \"grammar\": \"Giải thích cấu trúc ngữ pháp ngắn gọn bằng tiếng Việt\",\n" +
                "  \"words\": [\n" +
                "    {\n" +
                "      \"w\": \"từ tiếng Anh xuất hiện trong câu\",\n" +
                "      \"b\": \"từ gốc (lemma/baseform) của từ đó\",\n" +
                "      \"p\": \"loại từ (Noun/Verb/Adjective...)\",\n" +
                "      \"ipa\": \"phiên âm quốc tế IPA\",\n" +
                "      \"m\": \"nghĩa tiếng Việt tương ứng trong ngữ cảnh câu\",\n" +
                "      \"v2\": \"quá khứ đơn V2 (nếu là động từ, nếu không để null)\",\n" +
                "      \"v3\": \"phân từ V3 (nếu là động từ, nếu không để null)\",\n" +
                "      \"ex_en\": \"ví dụ ngắn bằng tiếng Anh chứa từ gốc này\",\n" +
                "      \"ex_vi\": \"dịch nghĩa ví dụ trên sang tiếng Việt\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Lưu ý: Chỉ cần phân tích từ 3 đến 5 từ quan trọng hoặc từ khó học trong câu.";

        try {
            String jsonResult = callGemini(prompt, true);
            // Chuẩn hóa JSON để đảm bảo không bị thừa text markdown ```json
            return cleanJsonResponse(jsonResult);
        } catch (Exception e) {
            log.error("Lỗi khi phân tích câu qua Gemini", e);
            return createFallbackGrammarJson(viText, enText);
        }
    }

    @Override
    public String translatePhrase(String phrase, String context) {
        String prompt = "Dịch tinh cụm từ '" + phrase + "' trong ngữ cảnh câu: '" + context + "'.\n" +
                "Hãy trả về định dạng JSON duy nhất với cấu trúc sau:\n" +
                "{\n" +
                "  \"m\": \"nghĩa tiếng Việt chính xác theo ngữ cảnh câu\",\n" +
                "  \"ex\": \"câu ví dụ mới bằng tiếng Anh sử dụng cụm từ này\"\n" +
                "}";

        try {
            String jsonResult = callGemini(prompt, true);
            return cleanJsonResponse(jsonResult);
        } catch (Exception e) {
            log.error("Lỗi khi dịch cụm từ qua Gemini", e);
            return "{\"m\":\"" + phrase + "\",\"ex\":\"No example available due to API timeout.\"}";
        }
    }

    /**
     * Gọi Gemini API trực tiếp bằng RestTemplate kèm cơ chế Exponential Backoff
     */
    private String callGemini(String prompt, boolean requireJson) throws Exception {
        try {
            log.info("Gọi Gemini với model chính: {}", modelName);
            return executeRequestWithModel(modelName, prompt, requireJson);
        } catch (Exception e) {
            log.warn("Model chính ({}) gặp lỗi: {}. Thử lại bằng model dự phòng: {}", modelName, e.getMessage(), fallbackModelName);
            try {
                return executeRequestWithModel(fallbackModelName, prompt, requireJson);
            } catch (Exception ex) {
                log.error("Model dự phòng ({}) cũng gặp lỗi: {}", fallbackModelName, ex.getMessage());
                throw ex;
            }
        }
    }

    /**
     * Thực hiện gửi request POST tới Gemini API cho một model cụ thể kèm cơ chế Exponential Backoff
     */
    private String executeRequestWithModel(String model, String prompt, boolean requireJson) throws Exception {
        String url = String.format(GEMINI_API_URL_TEMPLATE, model) + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Tạo body request theo chuẩn Gemini API
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode contentItem = contents.addObject();
        ArrayNode parts = contentItem.putArray("parts");
        parts.addObject().put("text", prompt);

        if (requireJson) {
            ObjectNode generationConfig = requestBody.putObject("generationConfig");
            generationConfig.put("responseMimeType", "application/json");
        }

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        int maxRetries = 3;
        int[] delays = {2000, 4000, 8000};
        Exception lastException = null;

        for (int i = 0; i <= maxRetries; i++) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode rootNode = objectMapper.readTree(response.getBody());
                    JsonNode textNode = rootNode.path("candidates")
                            .path(0)
                            .path("content")
                            .path("parts")
                            .path(0)
                            .path("text");
                    if (!textNode.isMissingNode()) {
                        return textNode.asText();
                    }
                }
            } catch (HttpClientErrorException.TooManyRequests e) {
                log.warn("Dính giới hạn rate limit (429) của Gemini API khi dùng model {}. Đang thử lại lần {}...", model, i + 1);
                lastException = e;
                if (i < maxRetries) {
                    Thread.sleep(delays[i]);
                }
            } catch (Exception e) {
                log.warn("Lỗi kết nối tới Gemini API khi dùng model {}: {}. Đang thử lại lần {}...", model, e.getMessage(), i + 1);
                lastException = e;
                if (i < maxRetries) {
                    Thread.sleep(delays[i]);
                }
            }
        }
        throw lastException != null ? lastException : new RuntimeException("Không nhận được phản hồi hợp lệ từ Gemini API cho model " + model);
    }

    private String cleanJsonResponse(String response) {
        if (response == null) {
            return "{}";
        }
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private String createFallbackGrammarJson(String viText, String enText) {
        // Fallback JSON nếu phân tích câu thất bại để không crash ứng dụng
        return "{\n" +
                "  \"tense\": \"Unknown\",\n" +
                "  \"grammar\": \"Không thể phân tích ngữ pháp tự động tại thời điểm này.\",\n" +
                "  \"words\": []\n" +
                "}";
    }
}
