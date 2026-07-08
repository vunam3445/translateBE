import os
import sqlite3

db_dir = r"D:\Android\Translate\app\src\main\assets"
db_path = os.path.join(db_dir, "dictionary.db")

# Tạo thư mục assets nếu chưa có
os.makedirs(db_dir, exist_ok=True)

# Kết nối (tự động tạo file)
conn = sqlite3.connect(db_path)
cursor = conn.cursor()

# Tạo bảng dictionary
cursor.execute("""
CREATE TABLE IF NOT EXISTS dictionary (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word TEXT NOT NULL UNIQUE,
    ipa TEXT,
    part_of_speech TEXT,
    meaning_vi TEXT NOT NULL,
    v2 TEXT,
    v3 TEXT,
    v_ing TEXT,
    plural TEXT,
    comparative TEXT,
    superlative TEXT,
    example_en TEXT,
    example_vi TEXT
);
""")

# Tạo bảng cached_translations
cursor.execute("""
CREATE TABLE IF NOT EXISTS cached_translations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phrase TEXT NOT NULL,
    context_sentence TEXT,
    meaning_vi TEXT NOT NULL,
    example_en TEXT,
    created_at INTEGER NOT NULL,
    UNIQUE(phrase, context_sentence)
);
""")

# Tạo các chỉ mục
cursor.execute("CREATE INDEX IF NOT EXISTS idx_dict_word ON dictionary(word);")
cursor.execute("CREATE INDEX IF NOT EXISTS idx_cache_phrase ON cached_translations(phrase);")

# Chèn một số từ vựng mẫu thông dụng để test offline
sample_words = [
    ("see", "/siː/", "Verb", "nhìn thấy, xem, hiểu", "saw", "seen", "seeing", None, "I can see the mountain from here.", "Tôi có thể nhìn thấy ngọn núi từ đây."),
    ("go", "/ɡəʊ/", "Verb", "đi, đi đến", "went", "gone", "going", None, "She goes to school every day.", "Cô ấy đi học mỗi ngày."),
    ("take", "/teɪk/", "Verb", "lấy, cầm, đưa đi", "took", "taken", "taking", None, "Take this umbrella with you.", "Hãy mang theo chiếc ô này bên mình."),
    ("play", "/pleɪ/", "Verb", "chơi, đóng vai", "played", "played", "playing", None, "The children are playing football.", "Lũ trẻ đang chơi đá bóng."),
    ("run", "/rʌn/", "Verb", "chạy, vận hành", "ran", "run", "running", None, "He runs every morning.", "Anh ấy chạy bộ mỗi sáng."),
    ("study", "/ˈstʌdi/", "Verb", "học tập, nghiên cứu", "studied", "studied", "studying", None, "She is studying English at university.", "Cô ấy đang học tiếng Anh ở trường đại học."),
    ("rain", "/reɪn/", "Noun/Verb", "mưa, cơn mưa", "rained", "rained", "raining", "rains", "It is starting to rain.", "Trời bắt đầu đổ mưa."),
    ("book", "/bʊk/", "Noun/Verb", "sách, đặt chỗ", "booked", "booked", "booking", "books", "I read a book last night.", "Tôi đã đọc một cuốn sách tối qua."),
    ("starry", "/ˈstɑːri/", "Adjective", "đầy sao, lấp lánh", None, None, None, None, "A starry sky is very beautiful.", "Một bầu trời đầy sao rất đẹp."),
    ("cultivation", "/ˌkʌltɪˈveɪʃn/", "Noun", "sự tu luyện, canh tác", None, None, None, "cultivations", "He spent years on his cultivation base.", "Anh ấy đã dành nhiều năm cho cơ sở tu luyện của mình."),
    ("ring", "/rɪŋ/", "Noun/Verb", "chiếc nhẫn, rung chuông", "rang", "rung", "ringing", "rings", "She wore a black ring on her finger.", "Cô ấy đeo một chiếc nhẫn màu đen trên ngón tay.")
]

for w in sample_words:
    try:
        cursor.execute("""
        INSERT OR REPLACE INTO dictionary 
        (word, ipa, part_of_speech, meaning_vi, v2, v3, v_ing, plural, example_en, example_vi)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, w)
    except Exception as e:
        print(f"Lỗi khi insert từ {w[0]}: {e}")

conn.commit()
conn.close()

print(f"Đã tạo và pre-populate thành công database sqlite tại: {db_path}")
