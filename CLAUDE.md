# Quy Chuẩn Dự Án Translateserver

## 1. Lệnh Phát Triển & Kiểm Thử
*   **Biên dịch:** `mvn compile`
*   **Build toàn bộ dự án:** `mvn clean package`
*   **Chạy Unit Tests:** `mvn test`
*   **Chạy một class Test cụ thể:** `mvn test -Dtest=ClassName` (Ví dụ: `mvn test -Dtest=UploadChapterUseCaseTest`)
*   **Chạy Server Spring Boot (Dev):** `mvn spring-boot:run`

## 2. Quy Chuẩn Code (Code Style)
*   **Kiến trúc:** Tuân thủ mô hình **Clean Architecture** nghiêm ngặt:
    *   `domain`: Chứa các thực thể cốt lõi (`domain/model`), interface Repository (`domain/repository`), dịch vụ lõi (`domain/service`). Không phụ thuộc vào thư viện bên ngoài hay Spring.
    *   `application`: Chứa các ca sử dụng (`application/usecase`), nhận đầu vào, điều phối nghiệp vụ và gọi Domain/Infrastructure.
    *   `infrastructure`: Chứa các chi tiết triển khai cụ thể:
        *   `infrastructure/persistence`: JPA Entities, Mappers, Adapters thực hiện truy xuất DB.
        *   `infrastructure/ai`: Tích hợp dịch vụ bên ngoài như Gemini AI.
        *   `infrastructure/web`: Rest Controllers, DTOs xử lý API HTTP.
*   **SOLID & Thiết Kế:**
    *   **Single Responsibility Principle (SRP):** Mỗi class thực hiện đúng một nhiệm vụ (Ví dụ: tách biệt `Use Case` xử lý nghiệp vụ với `Controller` nhận REST Request, và `Service` gọi API bên ngoài).
    *   **Open/Closed Principle (OCP):** Cấu hình các tham số môi trường và dịch vụ bên ngoài (như Model Gemini) thông qua `application.properties` thay vì hardcode trực tiếp trong Java.
    *   **Fallback Model Mechanism (Cơ chế dự phòng):** Để đảm bảo tính bền bỉ trước các lỗi quá tải (503 Service Unavailable) hoặc giới hạn băng thông (429 Too Many Requests), hệ thống tích hợp cơ chế dự phòng tự động chuyển sang model phụ (ví dụ: mặc định dùng `gemini-2.5-flash` và dự phòng `gemini-3.5-flash`) nếu model chính bị lỗi sau khi đã cạn số lần thử lại (retries).
    *   **In-Memory Active Tracking (Quản lý trạng thái dịch thuật dưới bộ nhớ):** Tránh việc sử dụng trực tiếp trạng thái `PROCESSING` từ Database để chặn chạy trùng lặp luồng, sử dụng một `Set` lưu trữ tạm thời trong JVM để theo dõi tiến trình thực tế. Điều này giúp ngăn ngừa lỗi kẹt tiến trình khi Server bị restart/crash đột ngột trong khi DB vẫn giữ trạng thái cũ.
*   **Quy chuẩn đặt tên:**
    *   Class Java: PascalCase (ví dụ: `GeminiTranslationService`, `TranslateChapterUseCase`).
    *   Method/Variable: camelCase (ví dụ: `translateText`, `apiKey`).
    *   Database Tables & Columns: snake_case (ví dụ: `stories`, `chapter_number`).
*   **Xử lý bất đồng bộ:** Sử dụng `CompletableFuture.runAsync` để thực hiện các luồng công việc nặng (như dịch thuật AI) dưới nền mà không chặn API Response.
*   **Logs:** Sử dụng annotation `@Slf4j` từ Lombok để ghi nhận logs thay vì sử dụng `System.out.println`.
