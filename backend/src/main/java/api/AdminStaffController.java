package api;

import api.dto.CreateStaffRequest;
import api.dto.ResetPasswordRequest;
import api.dto.StaffDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.StaffService;

/** Bổ sung 17/11 (BR02): chủ cửa hàng quản lý tài khoản nhân viên. Chỉ vai trò OWNER. */
@RestController
@RequestMapping("/api/admin/staff")
public class AdminStaffController {
	private final StaffService staffService;

	public AdminStaffController(StaffService staffService) {
		this.staffService = staffService;
	}

	@GetMapping
	public List<StaffDto> list() {
		return staffService.listStaff();
	}

	@PostMapping
	public StaffDto create(@Valid @RequestBody CreateStaffRequest req) {
		return staffService.createStaff(req);
	}

	@PutMapping("/{id}/password")
	public StaffDto resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest req) {
		return staffService.resetPassword(id, req.password());
	}
}
