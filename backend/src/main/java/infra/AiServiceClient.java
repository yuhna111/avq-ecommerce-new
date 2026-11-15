package infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Client dịch vụ AI gợi ý size. Bản demo KHÔNG có dịch vụ AI thật (CLAUDE.md mục 2, 7): ai.enabled=false
 * nên predictSize luôn trả về null và SizeAdvisorService dùng FallbackPolicy (FB-01).
 */
@Component
public class AiServiceClient {
	private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);
	private final boolean enabled;

	public AiServiceClient(@Value("${ai.enabled:false}") boolean enabled) {
		this.enabled = enabled;
	}

	/** null = không có kết quả (AI tắt, lỗi hoặc quá thời gian chờ) -> bên gọi dùng bảng size. */
	public AiSizeResult predictSize(SizeFeatures features, int timeoutMs) {
		if (!enabled) {
			return null;
		}
		// Ngoài phạm vi demo: chưa tích hợp mô hình thật, coi như không có kết quả để kích hoạt FB-01.
		log.warn("ai.enabled=true nhưng chưa cấu hình dịch vụ AI; dùng bảng size.");
		return null;
	}

	// ===== Getter (bổ sung) =====

	public boolean isEnabled() {
		return enabled;
	}
}
