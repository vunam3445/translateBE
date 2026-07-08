-- =========================================================================
-- 1. BẢNG QUẢN LÝ TRUYỆN (STORIES)
-- =========================================================================
CREATE TABLE IF NOT EXISTS stories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,               -- Tên truyện (e.g., Phàm Nhân Tu Tiên)
    total_chapters INT DEFAULT 0,              -- Tổng số chương hiện tại
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================================================================
-- 2. BẢNG QUẢN LÝ CHƯƠNG (CHAPTERS) & HÀNG ĐỢI XỬ LÝ
-- =========================================================================
CREATE TABLE IF NOT EXISTS chapters (
    id INT PRIMARY KEY AUTO_INCREMENT,
    story_id INT NOT NULL,
    chapter_number INT NOT NULL,               -- Số chương (Chương 1, Chương 2...)
    title VARCHAR(255) NOT NULL,               -- Tiêu đề chương
    
    -- Thêm trường trạng thái phục vụ tính năng dịch ngầm (Background Queue)
    -- Giúp App Android biết chương nào dịch xong ('COMPLETED'), chương nào đang đợi ('PROCESSING')
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    
    raw_content LONGTEXT DEFAULT NULL,         -- Nội dung văn bản thô tiếng Việt của chương
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    UNIQUE KEY unique_story_chapter (story_id, chapter_number) -- Tránh trùng lặp số chương trong cùng 1 truyện
);

-- =========================================================================
-- 3. BẢNG LƯU CÂU ĐÃ PHÂN TÍCH (ANALYZED_SENTENCES) - TRỌNG TÂM CỦA CACHING & NLP
-- =========================================================================
CREATE TABLE IF NOT EXISTS analyzed_sentences (
    id INT PRIMARY KEY AUTO_INCREMENT,
    chapter_id INT NOT NULL,
    sentence_order INT NOT NULL,               -- Thứ tự của câu trong chương (để hiển thị đúng dòng)
    vi_text TEXT NOT NULL,                     -- Câu gốc Tiếng Việt
    en_text TEXT NOT NULL,                     -- Câu dịch Tiếng Anh (từ Gemini)
    tense VARCHAR(100) DEFAULT 'Unknown',      -- Thì của câu (được NLP phân tích, e.g., "Past Simple")
    
    -- Lưu mảng dữ liệu JSON bóc tách chi tiết của câu (Cụm từ NLP, Động từ V1, V2, V3, Nghĩa, Ví dụ)
    -- Giúp giảm tải việc JOIN nhiều bảng, App Android chỉ cần bốc 1 dòng là có đủ data tra từ.
    words_json_meta LONGTEXT NOT NULL,         
    
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);

-- =========================================================================
-- 4. BẢNG TỪ ĐIỂN ĐỘNG / NHÂN VẬT (STORY_GLOSSARIES)
-- =========================================================================
CREATE TABLE IF NOT EXISTS story_glossaries (
    id INT PRIMARY KEY AUTO_INCREMENT,
    story_id INT NOT NULL,
    vi_term VARCHAR(255) NOT NULL,             -- Tên tiếng Việt gốc (e.g., Cố Trường Hoài)
    en_term VARCHAR(255) NOT NULL,             -- Tên tiếng Anh phiên âm (e.g., Gu Changhuai)
    description TEXT DEFAULT NULL,             -- Ghi chú/Vai trò của nhân vật
    is_manual_addition TINYINT(1) DEFAULT 0,   -- 1: do người dùng tự thêm bằng tay, 0: do AI tự nhận diện
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    UNIQUE KEY unique_story_term (story_id, vi_term) -- Tránh tạo trùng lặp một từ khóa trong cùng một truyện
);

-- =========================================================================
-- 5. BẢNG QUẢN LÝ THẺ HỌC TỪ VỰNG (FLASHCARDS)
-- =========================================================================
CREATE TABLE IF NOT EXISTS flashcards (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL,                -- Từ tiếng Anh (e.g., surprised)
    ipa VARCHAR(100) DEFAULT NULL,             -- Phiên âm quốc tế (e.g., /səˈpraɪzd/)
    meaning TEXT NOT NULL,                     -- Nghĩa tiếng Việt trong ngữ cảnh lưu truyện
    example_en TEXT DEFAULT NULL,              -- Câu ví dụ tiếng Anh
    example_vi TEXT DEFAULT NULL,              -- Dịch nghĩa câu ví dụ
    
    -- Thêm các trường phục vụ thuật toán học lại ngắt quãng (Spaced Repetition) giống Anki
    box_level INT DEFAULT 0,                   -- Cấp độ thuộc từ (0: Mới, 1 -> 5: Càng cao càng thuộc)
    next_review_date DATE NOT NULL,            -- Hẹn ngày hiển thị lại để kiểm tra người dùng
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================================================================
-- 6. BẢNG BỔ SUNG: ĐỒNG BỘ ĐỌC NGHE (AUDIO_MARKERS) - TỐI ƯU TÍNH NĂNG NGHE TRUYỆN TTS
-- =========================================================================
-- Bảng này lưu thời gian bắt đầu và kết thúc của từng câu khi phát Audio TTS.
-- Giúp App Android biết chính xác khi giọng đọc chạy đến giây thứ bao nhiêu thì Highlight câu tương ứng.
CREATE TABLE IF NOT EXISTS audio_markers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    chapter_id INT NOT NULL,
    sentence_order INT NOT NULL,
    start_time_ms INT NOT NULL,                -- Thời gian bắt đầu (mili-giây)
    end_time_ms INT NOT NULL,                  -- Thời gian kết thúc (mili-giây)
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);
