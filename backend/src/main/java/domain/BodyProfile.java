package domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * body_profiles.customer_id vừa là khóa chính vừa là khóa ngoại (R8) -> @MapsId (CLAUDE.md mục 4).
 */
@Entity
@Table(name = "body_profiles")
public class BodyProfile {
	// BỔ SUNG kỹ thuật: trường khóa customerId (không có trong thiết kế) để dùng @MapsId;
	// giá trị luôn bằng customer.id. Ghi vào báo cáo.
	@Id
	@Column(name = "customer_id")
	private Long customerId;
	@Column(name = "height_cm", nullable = false, columnDefinition = "smallint")
	private int heightCm;
	@Column(name = "weight_kg", nullable = false, columnDefinition = "smallint")
	private int weightKg;
	@Enumerated(EnumType.STRING)
	@Column(name = "fit_pref", nullable = false, columnDefinition = "enum('OM','VUA','RONG')")
	private FitPref fitPref;
	@Column(name = "consent_at", nullable = false)
	private LocalDateTime consentAt;
	// VP: public -> private + getter (CLAUDE.md 3.4). BodyProfile -> Customer một chiều.
	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	public void update(int heightCm, int weightKg, FitPref fitPref) {
		this.heightCm = heightCm;
		this.weightKg = weightKg;
		this.fitPref = fitPref;
		this.consentAt = LocalDateTime.now();
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getCustomerId() {
		return customerId;
	}

	public int getHeightCm() {
		return heightCm;
	}

	public int getWeightKg() {
		return weightKg;
	}

	public FitPref getFitPref() {
		return fitPref;
	}

	public LocalDateTime getConsentAt() {
		return consentAt;
	}

	public void setConsentAt(LocalDateTime consentAt) {
		this.consentAt = consentAt;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
