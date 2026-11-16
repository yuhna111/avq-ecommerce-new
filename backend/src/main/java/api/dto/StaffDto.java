package api.dto;

import domain.StaffRole;
import java.time.LocalDateTime;

/** Bổ sung 17/11: tài khoản nhân viên hiển thị cho chủ cửa hàng (không có mật khẩu). */
public record StaffDto(Long id, String username, String fullName, StaffRole role, LocalDateTime lockedUntil) {
}
