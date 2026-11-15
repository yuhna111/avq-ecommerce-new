package infra;

/** Kết quả mô hình AI (kiểu hỗ trợ của infra, bổ sung). Ngưỡng tin cậy 0,6 (Chương 3). */
public record AiSizeResult(String size, double confidence, String modelVersion) {
}
