package service;

import api.dto.RegisterRequest;
import api.dto.TokenDto;
import domain.Customer;
import domain.Staff;
import domain.User;
import infra.JwtTokenProvider;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.CustomerRepository;
import repository.StaffRepository;

@Service
public class AuthService {
	/** EX-01: không nói rõ trường nào sai */
	private static final String BAD_CREDENTIALS = "Email/SĐT hoặc mật khẩu không đúng";
	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
	public static final String ROLE_CUSTOMER = "CUSTOMER";

	private final CustomerRepository customerRepository;
	private final StaffRepository staffRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthService(CustomerRepository customerRepository, StaffRepository staffRepository,
			PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
		this.customerRepository = customerRepository;
		this.staffRepository = staffRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	/** UC01 - BR01 (EX-04), BR16 (mật khẩu >= 8 ký tự, băm BCrypt). */
	@Transactional
	public TokenDto register(RegisterRequest req) {
		String email = blankToNull(req.email());
		String phone = blankToNull(req.phone());
		if (email == null && phone == null) {
			throw BusinessException.badRequest("IDENTIFIER_REQUIRED", "Vui lòng nhập email hoặc số điện thoại");
		}
		if (req.password() == null || req.password().length() < 8) {
			throw BusinessException.badRequest("WEAK_PASSWORD", "Mật khẩu tối thiểu 8 ký tự");
		}
		if ((email != null && customerRepository.existsByEmail(email))
				|| (phone != null && customerRepository.existsByPhone(phone))) {
			throw BusinessException.conflict("ACCOUNT_EXISTS", "Email/SĐT đã được đăng ký, vui lòng đăng nhập");
		}
		Customer c = new Customer();
		c.setFullName(req.fullName().trim());
		c.setEmail(email == null ? null : email.toLowerCase());
		c.setPhone(phone);
		c.setPasswordHash(passwordEncoder.encode(req.password()));
		customerRepository.save(c);
		return token(c, ROLE_CUSTOMER);
	}

	/** UC02 - BR16/EX-01: sai 5 lần liên tiếp khóa 15 phút; đúng thì reset bộ đếm. */
	@Transactional(noRollbackFor = BusinessException.class)
	public TokenDto loginCustomer(String identifier, String password) {
		String id = identifier == null ? "" : identifier.trim();
		Customer c = customerRepository.findByEmailOrPhone(id.contains("@") ? id.toLowerCase() : id)
				.orElseThrow(() -> BusinessException.unauthorized("BAD_CREDENTIALS", BAD_CREDENTIALS));
		checkPassword(c, password);
		customerRepository.save(c);
		return token(c, ROLE_CUSTOMER);
	}

	/** UC13: nhân viên đăng nhập bằng username; cùng quy tắc khóa BR16. */
	@Transactional(noRollbackFor = BusinessException.class)
	public TokenDto loginStaff(String username, String password) {
		Staff s = staffRepository.findByUsername(username == null ? "" : username.trim())
				.orElseThrow(() -> BusinessException.unauthorized("BAD_CREDENTIALS", "Tài khoản hoặc mật khẩu không đúng"));
		if (!s.isActive()) {
			throw BusinessException.forbidden("STAFF_INACTIVE", "Tài khoản nhân viên đã bị vô hiệu hóa");
		}
		checkPassword(s, password);
		staffRepository.save(s);
		return token(s, s.getRole().name());
	}

	// ===== Hỗ trợ (private) =====

	private void checkPassword(User user, String password) {
		LocalDateTime now = LocalDateTime.now();
		if (user.isLocked(now)) {
			throw lockedError(user);
		}
		if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
			user.recordFailedLogin(now);
			if (user.isLocked(now)) {
				throw lockedError(user);
			}
			throw BusinessException.unauthorized("BAD_CREDENTIALS", BAD_CREDENTIALS);
		}
		user.resetFailedCount();
	}

	private BusinessException lockedError(User user) {
		return BusinessException.locked("ACCOUNT_LOCKED", "Bạn đã nhập sai mật khẩu 5 lần. Tài khoản tạm khóa đến "
				+ user.getLockedUntil().format(HH_MM) + ", vui lòng thử lại sau.");
	}

	private TokenDto token(User user, String role) {
		return new TokenDto(jwtTokenProvider.createToken(user.getId(), role), role, user.getId(), user.getFullName(),
				jwtTokenProvider.getExpirationMinutes());
	}

	private static String blankToNull(String s) {
		return s == null || s.isBlank() ? null : s.trim();
	}
}
