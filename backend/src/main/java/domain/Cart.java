package domain;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "carts")
public class Cart {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cart_id")
	private Long id;
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	// VP: public -> private + getter (CLAUDE.md 3.4). Customer 1 - 0..1 Cart (carts.customer_id UNIQUE).
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false, unique = true)
	private Customer customer;
	// VP: public ArrayList<CartItem> -> private List<CartItem>. Cart ◆ CartItem hai chiều.
	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartItem> items = new ArrayList<CartItem>();

	public Optional<CartItem> findItem(Long variantId) {
		throw new UnsupportedOperationException();
	}

	public CartItem addOrIncrease(ProductVariant variant, int qty) {
		throw new UnsupportedOperationException();
	}

	public void removeItems(Set<Long> variantIds) {
		throw new UnsupportedOperationException();
	}

	public BigDecimal subtotal() {
		throw new UnsupportedOperationException();
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public List<CartItem> getItems() {
		return items;
	}
}
