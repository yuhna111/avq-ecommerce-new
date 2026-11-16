package domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Thêm theo VP (Hình 4.5, bổ sung 16/11): nhật ký mỗi lần gọi mô hình AI hoặc dùng phương án dự phòng (DB-R7).
 * Bảng ai_inference_logs không có khóa ngoại nên lớp không có quan hệ.
 */
@Entity
@Table(name = "ai_inference_logs")
public class AiInferenceLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_id")
	private Long id;
	@Enumerated(EnumType.STRING)
	@Column(name = "feature", nullable = false, columnDefinition = "enum('SIZE','IMAGE')")
	private AiFeature feature;
	@Column(name = "model_version", nullable = false, length = 40)
	private String modelVersion;
	@Column(name = "input_hash", nullable = false, columnDefinition = "char(64)")
	private String inputHash;
	@Column(name = "output_json", columnDefinition = "text")
	private String outputJson;
	@Column(name = "latency_ms", nullable = false)
	private int latencyMs;
	@Column(name = "fallback_used", nullable = false, columnDefinition = "tinyint(1)")
	private boolean fallbackUsed;
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	// ===== Constructor, getter (bổ sung - CLAUDE.md 3.3) =====

	protected AiInferenceLog() {
	}

	public AiInferenceLog(AiFeature feature, String modelVersion, String inputHash, String outputJson, int latencyMs,
			boolean fallbackUsed) {
		this.feature = feature;
		this.modelVersion = modelVersion;
		this.inputHash = inputHash;
		this.outputJson = outputJson;
		this.latencyMs = latencyMs;
		this.fallbackUsed = fallbackUsed;
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public AiFeature getFeature() {
		return feature;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public String getInputHash() {
		return inputHash;
	}

	public String getOutputJson() {
		return outputJson;
	}

	public int getLatencyMs() {
		return latencyMs;
	}

	public boolean isFallbackUsed() {
		return fallbackUsed;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
