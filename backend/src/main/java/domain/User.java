package domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Stereotype MappedSuperclass (Hình 4.5). Không có bảng riêng; Customer và Staff ánh xạ
 * {@code id} sang customer_id / staff_id bằng @AttributeOverride.
 */
@MappedSuperclass
public abstract class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "full_name", nullable = false, length = 100)
	private String fullName;
	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;
	@Column(name = "failed_login_count", nullable = false, columnDefinition = "tinyint")
	private int failedLoginCount;
	@Column(name = "locked_until")
	private LocalDateTime lockedUntil;

	public void recordFailedLogin(LocalDateTime now) {
		throw new UnsupportedOperationException();
	}

	public void resetFailedCount() {
		throw new UnsupportedOperationException();
	}

	public boolean isLocked(LocalDateTime now) {
		throw new UnsupportedOperationException();
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public int getFailedLoginCount() {
		return failedLoginCount;
	}

	public LocalDateTime getLockedUntil() {
		return lockedUntil;
	}
}
