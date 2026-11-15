package domain;

public abstract class User {
	private Long id;
	private String fullName;
	private String passwordHash;
	private int failedLoginCount;
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
}