package api;

import api.dto.SizeAdviceDto;
import api.dto.SizeAdviceRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import service.SizeAdvisorService;

/** UC06a gợi ý size; khách chưa đăng nhập vẫn dùng được (customerId = null, không lưu số đo). */
@RestController
public class SizeController {
	private final SizeAdvisorService sizeAdvisorService;

	public SizeController(SizeAdvisorService sizeAdvisorService) {
		this.sizeAdvisorService = sizeAdvisorService;
	}

	@PostMapping("/api/products/{productId}/size-advice")
	public SizeAdviceDto advise(@AuthenticationPrincipal(expression = "customerId") Long customerId,
			@PathVariable Long productId, @Valid @RequestBody SizeAdviceRequest req) {
		return sizeAdvisorService.advise(customerId, productId, req);
	}
}
