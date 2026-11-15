package domain;

import java.util.ArrayList;
import domain.Address;

public class Customer extends User {
	private String email;
	private String phone;
	public ArrayList<Address> addresses = new ArrayList<Address>();

	public void addAddress(Address address) {
		throw new UnsupportedOperationException();
	}
}