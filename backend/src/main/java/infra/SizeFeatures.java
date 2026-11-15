package infra;

import domain.FitPref;
import domain.FitType;

/** Đầu vào mô hình gợi ý size (kiểu hỗ trợ của infra, bổ sung). */
public record SizeFeatures(Long productId, int heightCm, int weightKg, FitPref fitPref, FitType fitType) {
}
