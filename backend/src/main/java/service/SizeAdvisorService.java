package service;

import api.dto.SizeAdviceDto;
import api.dto.SizeAdviceRequest;
import domain.BodyProfile;
import domain.Customer;
import domain.Product;
import domain.SizeChart;
import infra.AiServiceClient;
import infra.AiSizeResult;
import infra.SizeFeatures;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.BodyProfileRepository;
import repository.CustomerRepository;
import repository.ProductRepository;
import repository.SizeChartRepository;

/**
 * UC06a - FB-01: thử AI (ngưỡng tin cậy 0,6; chờ tối đa 1,5 giây), không có/không tin được thì dùng bảng size.
 * Bản demo không có AI thật nên luôn dùng FallbackPolicy.
 *
 * LỆCH THIẾT KẾ: bảng ai_inference_logs (DB-R7) chưa có lớp tương ứng trong VP -> ghi bằng JdbcTemplate
 * (CLAUDE.md mục 4). Đề xuất bổ sung lớp AiInferenceLog vào VP.
 */
@Service
public class SizeAdvisorService {
	private static final double MIN_CONFIDENCE = 0.6;
	private static final int AI_TIMEOUT_MS = 1500;
	private static final String RULE_MODEL_VERSION = "rule-based-v1";
	private static final String NO_CHART_MESSAGE = "Liên hệ cửa hàng để được tư vấn size";

	private final ProductRepository productRepository;
	private final SizeChartRepository sizeChartRepository;
	private final BodyProfileRepository bodyProfileRepository;
	private final CustomerRepository customerRepository;
	private final AiServiceClient aiServiceClient;
	private final FallbackPolicy fallbackPolicy;
	private final JdbcTemplate jdbcTemplate;

	public SizeAdvisorService(ProductRepository productRepository, SizeChartRepository sizeChartRepository,
			BodyProfileRepository bodyProfileRepository, CustomerRepository customerRepository,
			AiServiceClient aiServiceClient, FallbackPolicy fallbackPolicy, JdbcTemplate jdbcTemplate) {
		this.productRepository = productRepository;
		this.sizeChartRepository = sizeChartRepository;
		this.bodyProfileRepository = bodyProfileRepository;
		this.customerRepository = customerRepository;
		this.aiServiceClient = aiServiceClient;
		this.fallbackPolicy = fallbackPolicy;
		this.jdbcTemplate = jdbcTemplate;
	}

	/** customerId = null khi khách chưa đăng nhập (vẫn được gợi ý, không lưu số đo). */
	@Transactional
	public SizeAdviceDto advise(Long customerId, Long productId, SizeAdviceRequest req) {
		long start = System.nanoTime();
		Product product = productRepository.findByIdAndVisibleTrue(productId)
				.orElseThrow(() -> BusinessException.notFound("PRODUCT_NOT_FOUND", "Sản phẩm không tồn tại hoặc đã ngừng bán"));
		int h = req.heightCm();
		int w = req.weightKg();

		AiSizeResult ai = aiServiceClient.predictSize(
				new SizeFeatures(productId, h, w, req.fitPref(), product.getFitType()), AI_TIMEOUT_MS);
		String size;
		String source;
		String modelVersion;
		boolean fallbackUsed;
		if (ai != null && ai.size() != null && ai.confidence() >= MIN_CONFIDENCE) {
			size = ai.size();
			source = "AI";
			modelVersion = ai.modelVersion();
			fallbackUsed = false;
		} else {
			List<SizeChart> chart = sizeChartRepository.findByProductIdOrderByHeightMinAsc(productId);
			size = fallbackPolicy.ruleBasedSize(chart, h, w, req.fitPref(), product.getFitType());
			source = "BANG_SIZE";
			modelVersion = RULE_MODEL_VERSION;
			fallbackUsed = true;
		}
		String message = size == null ? NO_CHART_MESSAGE
				: "Size phù hợp với bạn: " + size + " (theo bảng size của sản phẩm, phom " + product.getFitType() + ")";

		if (customerId != null && req.saveProfile()) {
			saveProfile(customerId, h, w, req);
		}
		int latencyMs = (int) ((System.nanoTime() - start) / 1_000_000);
		logInference(productId, h, w, req, size, modelVersion, latencyMs, fallbackUsed);
		return new SizeAdviceDto(size, message, fallbackUsed, source);
	}

	// ===== Hỗ trợ (private) =====

	private void saveProfile(Long customerId, int h, int w, SizeAdviceRequest req) {
		BodyProfile profile = bodyProfileRepository.findByCustomerId(customerId).orElseGet(() -> {
			Customer c = customerRepository.findById(customerId)
					.orElseThrow(() -> BusinessException.unauthorized("CUSTOMER_NOT_FOUND", "Vui lòng đăng nhập lại"));
			BodyProfile p = new BodyProfile();
			p.setCustomer(c);
			return p;
		});
		profile.update(h, w, req.fitPref());
		bodyProfileRepository.save(profile);
	}

	/** Một dòng ai_inference_logs mỗi lần gợi ý; input_hash = SHA-256 số đo (không lưu số đo thô). */
	private void logInference(Long productId, int h, int w, SizeAdviceRequest req, String size, String modelVersion,
			int latencyMs, boolean fallbackUsed) {
		String inputHash = sha256(h + "|" + w + "|" + req.fitPref());
		String output = "{\"productId\":" + productId + ",\"size\":" + (size == null ? "null" : "\"" + size + "\"") + "}";
		jdbcTemplate.update("INSERT INTO ai_inference_logs (feature, model_version, input_hash, output_json, latency_ms, "
				+ "fallback_used, created_at) VALUES ('SIZE', ?, ?, ?, ?, ?, ?)",
				modelVersion, inputHash, output, latencyMs, fallbackUsed, LocalDateTime.now());
	}

	private static String sha256(String s) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
