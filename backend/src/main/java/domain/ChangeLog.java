package domain;

import java.time.LocalDateTime;

public class ChangeLog {
	private String fieldName;
	private String oldValue;
	private String newValue;
	private LocalDateTime changedAt;
	private Long id;
	public Product product;
	public Staff staff;
	public ProductVariant productVariant;

	public static ChangeLog of(Staff staff, ProductVariant variant, String field, String oldValue, String newValue) {
		throw new UnsupportedOperationException();
	}
}