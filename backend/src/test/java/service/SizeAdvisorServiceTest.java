package service;

import static org.assertj.core.api.Assertions.assertThat;

import api.dto.SizeAdviceDto;
import api.dto.SizeAdviceRequest;
import domain.Customer;
import domain.FitPref;
import domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import repository.BodyProfileRepository;

/** FB-01 qua service: luôn dùng bảng size (ai.enabled=false), mỗi lần ghi một dòng ai_inference_logs. */
class SizeAdvisorServiceTest extends ServiceTestBase {
	@Autowired SizeAdvisorService sizeAdvisorService;
	@Autowired JdbcTemplate jdbc;
	@Autowired BodyProfileRepository bodyProfileRepository;

	@Test
	void goiYTheoBangSize_ghiLogFallback_vaLuuSoDoKhiDongY() {
		Customer c = fx.customer();
		Product p = fx.product(fx.category(), "Áo gợi ý", 200_000, 5);
		fx.sizeChart(p, "S", 150, 160, 45, 55);
		fx.sizeChart(p, "M", 160, 170, 55, 65);
		em.flush();
		int before = jdbc.queryForObject("SELECT COUNT(*) FROM ai_inference_logs", Integer.class);

		SizeAdviceDto r = sizeAdvisorService.advise(c.getId(), p.getId(), new SizeAdviceRequest(165, 60, FitPref.VUA, true));

		assertThat(r.size()).isEqualTo("M");
		assertThat(r.fallbackUsed()).isTrue();
		assertThat(r.source()).isEqualTo("BANG_SIZE");
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ai_inference_logs", Integer.class)).isEqualTo(before + 1);
		assertThat(jdbc.queryForObject("SELECT fallback_used FROM ai_inference_logs ORDER BY log_id DESC LIMIT 1", Boolean.class)).isTrue();
		assertThat(jdbc.queryForObject("SELECT LENGTH(input_hash) FROM ai_inference_logs ORDER BY log_id DESC LIMIT 1", Integer.class)).isEqualTo(64);
		assertThat(bodyProfileRepository.findByCustomerId(c.getId())).isPresent();
	}

	@Test
	void khongCoBangSize_huongDanLienHeCuaHang() {
		Product p = fx.product(fx.category(), "Áo chưa có bảng size", 200_000, 5);
		em.flush();
		SizeAdviceDto r = sizeAdvisorService.advise(null, p.getId(), new SizeAdviceRequest(170, 60, FitPref.OM, false));
		assertThat(r.size()).isNull();
		assertThat(r.message()).isEqualTo("Liên hệ cửa hàng để được tư vấn size");
	}
}
