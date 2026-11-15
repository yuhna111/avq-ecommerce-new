package api;

import api.dto.LoginRequest;
import api.dto.RegisterRequest;
import api.dto.TokenDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.AuthService;

/** UC01 đăng ký, UC02 đăng nhập khách, UC13 đăng nhập nhân viên. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public TokenDto register(@Valid @RequestBody RegisterRequest req) {
		return authService.register(req);
	}

	@PostMapping("/login")
	public TokenDto login(@Valid @RequestBody LoginRequest req) {
		return authService.loginCustomer(req.identifier(), req.password());
	}

	@PostMapping("/staff/login")
	public TokenDto staffLogin(@Valid @RequestBody LoginRequest req) {
		return authService.loginStaff(req.identifier(), req.password());
	}
}
