package domain;

import java.util.ArrayList;
import java.util.List;
import domain.Address;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
@AttributeOverride(name = "id", column = @Column(name = "customer_id"))
public class Customer extends User {
	@Column(name = "email", length = 120, unique = true)
	private String email;
	@Column(name = "phone", length = 15, unique = true)
	private String phone;
	// VP: public ArrayList<Address> -> private List<Address> (CLAUDE.md 3.4). Customer ◆ Address hai chiều.
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Address> addresses = new ArrayList<Address>();

	public void addAddress(Address address) {
		throw new UnsupportedOperationException();
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<Address> getAddresses() {
		return addresses;
	}
}
