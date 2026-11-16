package service;

import api.dto.CreateStaffRequest;
import api.dto.StaffDto;
import domain.Staff;
import domain.StaffRole;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.StaffRepository;

/**
 * Bổ sung 17/11 (vp-changes.md đợt 2, BR02): chủ cửa hàng quản lý tài khoản nhân viên.
 * Chỉ vai trò OWNER gọi được (SecurityConfig: /api/admin/staff/** hasRole OWNER).
 * Tài khoản tạo mới luôn có vai trò STAFF; không đặt lại mật khẩu cho tài khoản OWNER khác.
 */
@Service
public class StaffService {
	private final StaffRepository staffRepository;
	private final PasswordEncoder passwordEncoder;

	public StaffService(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
		this.staffRepository = staffRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<StaffDto> listStaff() {
		return staffRepository.findAll().stream()
				.sorted(Comparator.comparing(Staff::getRole).reversed().thenComparing(Staff::getUsername))
				.map(StaffService::toDto).toList();
	}

	@Transactional
	public StaffDto createStaff(CreateStaffRequest req) {
		String username = req.username().trim().toLowerCase();
		if (staffRepository.findByUsername(username).isPresent()) {
			throw BusinessException.conflict("USERNAME_TAKEN", "Tên đăng nhập \"" + username + "\" đã được dùng");
		}
		Staff s = new Staff();
		s.setUsername(username);
		s.setFullName(req.fullName().trim());
		s.setRole(StaffRole.STAFF);
		s.setPasswordHash(passwordEncoder.encode(req.password()));
		return toDto(staffRepository.save(s));
	}

	@Transactional
	public StaffDto resetPassword(Long staffId, String newPassword) {
		Staff s = staffRepository.findById(staffId)
				.orElseThrow(() -> BusinessException.notFound("STAFF_NOT_FOUND", "Không tìm thấy nhân viên"));
		if (s.getRole() == StaffRole.OWNER) {
			throw BusinessException.forbidden("OWNER_PROTECTED", "Không đặt lại mật khẩu cho tài khoản chủ cửa hàng");
		}
		s.setPasswordHash(passwordEncoder.encode(newPassword));
		s.resetFailedCount(); // mở khóa nếu đang bị khóa do nhập sai (BR16)
		return toDto(s);
	}

	private static StaffDto toDto(Staff s) {
		return new StaffDto(s.getId(), s.getUsername(), s.getFullName(), s.getRole(), s.getLockedUntil());
	}
}
