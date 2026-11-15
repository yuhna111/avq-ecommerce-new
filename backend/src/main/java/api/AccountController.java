package api;

import api.dto.AddressDto;
import api.dto.AddressRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.AccountService;

/** Địa chỉ giao hàng của khách (phục vụ UC09). */
@RestController
@RequestMapping("/api/account")
public class AccountController {
	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping("/addresses")
	public List<AddressDto> listAddresses(@AuthenticationPrincipal(expression = "customerId") Long customerId) {
		return accountService.listAddresses(customerId);
	}

	@PostMapping("/addresses")
	public List<AddressDto> addAddress(@AuthenticationPrincipal(expression = "customerId") Long customerId,
			@Valid @RequestBody AddressRequest req) {
		return accountService.addAddress(customerId, req);
	}
}
