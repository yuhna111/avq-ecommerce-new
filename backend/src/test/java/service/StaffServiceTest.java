package service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import api.dto.CreateStaffRequest;
import api.dto.StaffDto;
import domain.Staff;
import domain.StaffRole;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Bổ sung 17/11 – BR02: chủ cửa hàng cấp tài khoản nhân viên, đặt lại mật khẩu. */
class StaffServiceTest extends ServiceTestBase {
	@Autowired StaffService staffService;
	@Autowired AuthService authService;

	@Test
	void taoNhanVien_dangNhapDuoc_vaiTroLuonLaStaff() {
		String u = "nvmoi" + fx.tag();
		StaffDto d = staffService.createStaff(new CreateStaffRequest(u, "Lê Văn Mới", "matkhau123"));
		assertThat(d.role()).isEqualTo(StaffRole.STAFF);
		assertThat(authService.loginStaff(u, "matkhau123").role()).isEqualTo("STAFF");
		assertThat(staffService.listStaff()).extracting(StaffDto::username).contains(u);
	}

	@Test
	void trungTenDangNhap_biTuChoi() {
		Staff s = fx.staff();
		assertThatThrownBy(() -> staffService.createStaff(new CreateStaffRequest(s.getUsername(), "Trùng", "matkhau123")))
				.isInstanceOf(BusinessException.class).extracting("code").isEqualTo("USERNAME_TAKEN");
	}

	@Test
	void datLaiMatKhau_moKhoaTaiKhoan() {
		Staff s = fx.staff();
		for (int i = 0; i < 5; i++) {
			s.recordFailedLogin(LocalDateTime.now());
		}
		assertThat(s.isLocked(LocalDateTime.now())).isTrue();
		flushClear();
		staffService.resetPassword(s.getId(), "matkhaumoi1");
		flushClear();
		assertThat(authService.loginStaff(s.getUsername(), "matkhaumoi1").role()).isEqualTo("STAFF");
	}

	@Test
	void khongDatLaiMatKhauChoChuCuaHang() {
		Staff owner = fx.staff();
		owner.setRole(StaffRole.OWNER);
		flushClear();
		assertThatThrownBy(() -> staffService.resetPassword(owner.getId(), "matkhaumoi1"))
				.isInstanceOf(BusinessException.class).extracting("code").isEqualTo("OWNER_PROTECTED");
	}
}
