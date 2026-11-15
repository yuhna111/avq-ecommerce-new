package domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "staff")
@AttributeOverride(name = "id", column = @Column(name = "staff_id"))
public class Staff extends User {
	@Column(name = "username", nullable = false, length = 50, unique = true)
	private String username;
	// Cột staff.role là varchar(20) (không phải ENUM) -> columnDefinition khớp DDL.
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, columnDefinition = "varchar(20)")
	private StaffRole role;
	// LỆCH THIẾT KẾ: bảng staff không có cột active (DDL V2). Không được sửa CSDL nên
	// đánh dấu @Transient: không lưu, luôn true (đặt trong constructor). Ghi vào báo cáo.
	@Transient
	private boolean active;

	public Staff() {
		this.active = true;
	}

	public boolean isActive() {
		return this.active;
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public StaffRole getRole() {
		return role;
	}

	public void setRole(StaffRole role) {
		this.role = role;
	}
}
