package domain;

public class Staff extends User {
	private String username;
	private StaffRole role;
	private boolean active;

	public boolean isActive() {
		return this.active;
	}
}