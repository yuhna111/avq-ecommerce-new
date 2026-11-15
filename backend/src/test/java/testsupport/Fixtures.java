package testsupport;

import domain.Category;
import domain.Customer;
import domain.FitType;
import domain.Product;
import domain.ProductVariant;
import domain.SizeChart;
import domain.Staff;
import domain.StaffRole;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tạo dữ liệu thử bằng EntityManager. Mọi mã định danh có hậu tố ngẫu nhiên để không đụng dữ liệu demo.
 */
public class Fixtures {
	private final EntityManager em;
	private final String tag = UUID.randomUUID().toString().substring(0, 8);

	public Fixtures(EntityManager em) {
		this.em = em;
	}

	public String tag() {
		return tag;
	}

	public Category category() {
		Category c = new Category();
		c.setName("Danh mục test " + tag);
		c.setSlug("test-" + tag + "-" + UUID.randomUUID().toString().substring(0, 4));
		em.persist(c);
		return c;
	}

	/** Sản phẩm hiển thị có một biến thể M/Đen với tồn kho cho trước. */
	public Product product(Category c, String name, long price, int stock) {
		Product p = new Product();
		p.setName(name);
		p.updateInfo(BigDecimal.valueOf(price), "Mô tả " + name, "Cotton");
		p.setVisible(true);
		p.setFitType(FitType.REGULAR);
		p.setCategory(c);
		ProductVariant v = p.addVariant("M", "Đen", "SKU-" + tag + "-" + UUID.randomUUID().toString().substring(0, 6));
		v.setStock(stock);
		em.persist(p);
		return p;
	}

	public ProductVariant variant(Product p) {
		return p.getVariants().get(0);
	}

	public SizeChart sizeChart(Product p, String size, int hMin, int hMax, int wMin, int wMax) {
		SizeChart s = new SizeChart();
		s.setProduct(p);
		s.setSize(size);
		s.setHeightMin(hMin);
		s.setHeightMax(hMax);
		s.setWeightMin(wMin);
		s.setWeightMax(wMax);
		em.persist(s);
		return s;
	}

	public Customer customer() {
		Customer c = new Customer();
		c.setFullName("Khách test " + tag);
		c.setPasswordHash("$2a$10$test");
		c.setEmail("test-" + tag + "-" + UUID.randomUUID().toString().substring(0, 4) + "@avq.test");
		c.setPhone("09" + String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 8));
		em.persist(c);
		return c;
	}

	public Staff staff() {
		Staff s = new Staff();
		s.setFullName("Nhân viên test " + tag);
		s.setPasswordHash("$2a$10$test");
		s.setUsername("nv-" + tag + "-" + UUID.randomUUID().toString().substring(0, 4));
		s.setRole(StaffRole.STAFF);
		em.persist(s);
		return s;
	}
}
