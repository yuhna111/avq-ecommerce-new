package domain;

public class Address {
	private Long id;
	private boolean isDefault;
	private String receiverName;
	private String receiverPhone;
	private String addressLine;
	public Customer customer;

	public void setDefault(boolean isDefault) {
		throw new UnsupportedOperationException();
	}

	public String toSnapshot() {
		throw new UnsupportedOperationException();
	}
}