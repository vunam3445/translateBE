package com.example.translateserver.infrastructure.nlp;

import lombok.extern.slf4j.Slf4j;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service quản lý và khởi tạo các model của Apache OpenNLP.
 * Tự động tải model POS Tagger từ máy chủ Apache nếu chưa tồn tại cục bộ.
 * Model được cache vào thư mục "models/" tại working directory của server.
 */
@Component
@Slf4j
public class OpenNlpModelService {

    private static final String MODEL_DIR = "models";
    private static final String POS_MODEL_FILENAME = "en-pos-maxent.bin";
    private static final String POS_MODEL_URL =
            "https://downloads.sourceforge.net/project/opennlp/models-1.5/en-pos-maxent.bin";

    private POSTaggerME posTagger;
    private boolean initialized = false;

    /**
     * Khởi tạo POS Tagger từ model. Gọi một lần khi Spring context khởi động.
     * Tải model từ mạng nếu chưa có file cục bộ.
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }
        try {
            Path modelPath = getOrDownloadModel();
            try (InputStream modelIn = Files.newInputStream(modelPath)) {
                POSModel posModel = new POSModel(modelIn);
                posTagger = new POSTaggerME(posModel);
                initialized = true;
                log.info("Đã khởi tạo OpenNLP POS Tagger thành công từ: {}", modelPath.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Không thể khởi tạo OpenNLP POS Tagger: {}", e.getMessage(), e);
            initialized = false;
        }
    }

    /**
     * Lấy mảng nhãn loại từ (POS Tags) của một câu tiếng Anh.
     *
     * @param sentence Câu tiếng Anh cần phân tích
     * @return Mảng nhãn POS tương ứng với từng token, hoặc null nếu chưa sẵn sàng
     */
    public String[] tag(String sentence) {
        if (!initialized) {
            initialize();
        }
        if (!initialized || posTagger == null) {
            log.warn("POS Tagger chưa sẵn sàng, trả về null.");
            return null;
        }
        log.info("[OpenNlpModelService] Bắt đầu tách từ (tokenize) cho câu...");
        String[] tokens = SimpleTokenizer.INSTANCE.tokenize(sentence);
        if (tokens == null || tokens.length == 0) {
            return new String[0];
        }
        log.info("[OpenNlpModelService] Đang gán nhãn từ loại (tag) cho {} tokens...", tokens.length);
        String[] tags = posTagger.tag(tokens);
        log.info("[OpenNlpModelService] Gán nhãn từ loại hoàn tất.");
        return tags;
    }

    /**
     * Tách câu thành mảng tokens sử dụng SimpleTokenizer của OpenNLP.
     *
     * @param sentence Câu cần tách từ
     * @return Mảng từ (tokens)
     */
    public String[] tokenize(String sentence) {
        return SimpleTokenizer.INSTANCE.tokenize(sentence);
    }

    public boolean isInitialized() {
        return initialized;
    }

    private Path getOrDownloadModel() throws IOException {
        Path modelDir = Paths.get(MODEL_DIR);
        if (!Files.exists(modelDir)) {
            Files.createDirectories(modelDir);
        }
        Path modelPath = modelDir.resolve(POS_MODEL_FILENAME);
        if (Files.exists(modelPath) && Files.size(modelPath) > 1024) {
            log.info("Đã tìm thấy model POS hợp lệ tại: {}", modelPath.toAbsolutePath());
            return modelPath;
        }
        log.info("Model POS chưa tồn tại. Đang tải từ: {}", POS_MODEL_URL);
        
        downloadFileWithRedirects(POS_MODEL_URL, modelPath);
        
        log.info("Đã tải xong model POS ({} KB) vào: {}",
                Files.size(modelPath) / 1024,
                modelPath.toAbsolutePath());
        return modelPath;
    }

    private void downloadFileWithRedirects(String urlString, Path targetPath) throws IOException {
        String currentUrl = urlString;
        int redirectCount = 0;
        final int MAX_REDIRECTS = 5;
        
        while (redirectCount < MAX_REDIRECTS) {
            java.net.URL url = java.net.URI.create(currentUrl).toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            int status = conn.getResponseCode();
            if (status == java.net.HttpURLConnection.HTTP_MOVED_TEMP || 
                status == java.net.HttpURLConnection.HTTP_MOVED_PERM || 
                status == java.net.HttpURLConnection.HTTP_SEE_OTHER ||
                status == 307 || status == 308) {
                
                String newUrl = conn.getHeaderField("Location");
                if (newUrl != null) {
                    currentUrl = newUrl;
                    redirectCount++;
                    log.info("Redirect sang URL mới: {}", currentUrl);
                    conn.disconnect();
                    continue;
                }
            }
            
            if (status == java.net.HttpURLConnection.HTTP_OK) {
                try (InputStream in = conn.getInputStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                conn.disconnect();
                return;
            } else {
                conn.disconnect();
                throw new java.io.IOException("Server trả về mã lỗi: " + status + " khi tải " + currentUrl);
            }
        }
        throw new java.io.IOException("Quá nhiều lần chuyển hướng (redirect) khi tải từ: " + urlString);
    }
}
