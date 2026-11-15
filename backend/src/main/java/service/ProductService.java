package service;

import api.dto.CategoryDto;
import api.dto.CreateProductRequest;
import api.dto.ProductCardDto;
import api.dto.ProductDetailDto;
import api.dto.UpdateProductRequest;
import domain.Category;
import domain.ChangeLog;
import domain.Product;
import domain.ProductImage;
import domain.Staff;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.CategoryRepository;
import repository.ProductChangeLogRepository;
import repository.ProductFilter;
import repository.ProductRepository;
import repository.SizeChartRepository;
import repository.StaffRepository;

@Service
public class ProductService {
	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final SizeChartRepository sizeChartRepository;
	private final StaffRepository staffRepository;
	private final ProductChangeLogRepository productChangeLogRepository;

	public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
			SizeChartRepository sizeChartRepository, StaffRepository staffRepository,
			ProductChangeLogRepository productChangeLogRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.sizeChartRepository = sizeChartRepository;
		this.staffRepository = staffRepository;
		this.productChangeLogRepository = productChangeLogRepository;
	}

	@Transactional(readOnly = true)
	public List<CategoryDto> listCategories() {
		return categoryRepository.findAll(Sort.by("name")).stream().map(DtoMapper::category).toList();
	}

	/** UC04/UC05 - BR03: khách xem, tìm, lọc không cần đăng nhập; chỉ sản phẩm đang hiển thị. */
	@Transactional(readOnly = true)
	public List<ProductCardDto> search(ProductFilter filter) {
		ProductFilter f = filter == null ? new ProductFilter() : filter;
		f.setIncludeHidden(false);
		List<Product> products = productRepository.search(f);
		Comparator<Product> order = switch (f.getSort() == null ? "" : f.getSort()) {
			case "price_asc" -> Comparator.comparing(Product::getPrice);
			case "price_desc" -> Comparator.comparing(Product::getPrice).reversed();
			default -> Comparator.comparing(Product::getId).reversed();
		};
		return products.stream().sorted(order).map(DtoMapper::productCard).toList();
	}

	/** UC06 */
	@Transactional(readOnly = true)
	public ProductDetailDto getDetail(Long id) {
		Product p = productRepository.findByIdAndVisibleTrue(id)
				.orElseThrow(() -> BusinessException.notFound("PRODUCT_NOT_FOUND", "Sản phẩm không tồn tại hoặc đã ngừng bán"));
		return DtoMapper.productDetail(p, hasSizeChart(p));
	}

	/** UC17 - BR06, BR17: ít nhất một biến thể, SKU duy nhất. */
	@Transactional
	public ProductDetailDto create(Long staffId, CreateProductRequest req) {
		staff(staffId);
		if (req.variants() == null || req.variants().isEmpty()) {
			throw BusinessException.badRequest("VARIANT_REQUIRED", "Sản phẩm phải có ít nhất một biến thể");
		}
		if (req.price() == null || req.price().signum() <= 0) {
			throw BusinessException.badRequest("INVALID_PRICE", "Giá bán phải lớn hơn 0");
		}
		Set<String> skus = new HashSet<String>();
		Set<String> sizeColors = new HashSet<String>();
		for (CreateProductRequest.VariantRequest v : req.variants()) {
			if (!skus.add(v.sku().trim().toUpperCase())) {
				throw BusinessException.badRequest("DUPLICATE_SKU", "SKU \"" + v.sku() + "\" bị lặp");
			}
			if (!sizeColors.add(v.size().trim().toUpperCase() + "|" + v.color().trim().toLowerCase())) {
				throw BusinessException.badRequest("DUPLICATE_VARIANT", "Biến thể size " + v.size() + " màu " + v.color() + " bị lặp");
			}
		}
		Category category = categoryRepository.findById(req.categoryId())
				.orElseThrow(() -> BusinessException.badRequest("CATEGORY_NOT_FOUND", "Danh mục không tồn tại"));

		Product p = new Product();
		p.setName(req.name().trim());
		p.updateInfo(req.price(), req.description(), req.material());
		p.setFitType(req.fitType());
		p.setCategory(category);
		p.setVisible(true);
		for (CreateProductRequest.VariantRequest v : req.variants()) {
			p.addVariant(v.size().trim().toUpperCase(), v.color().trim(), v.sku().trim().toUpperCase()).setStock(v.stock());
		}
		if (req.imageUrls() != null) {
			boolean first = true;
			for (String url : req.imageUrls()) {
				if (url == null || url.isBlank()) {
					continue;
				}
				ProductImage img = new ProductImage();
				img.setProduct(p);
				img.setUrl(url.trim());
				img.setMain(first);
				p.getImage().add(img);
				first = false;
			}
		}
		try {
			productRepository.saveAndFlush(p);
		} catch (DataIntegrityViolationException e) {
			throw BusinessException.conflict("SKU_EXISTS", "Mã SKU đã tồn tại, vui lòng dùng mã khác");
		}
		return DtoMapper.productDetail(p, false);
	}

	/** UC18: sửa giá/mô tả/chất liệu, ẩn/hiện; FR31 ghi product_change_logs cho từng trường thay đổi. */
	@Transactional
	public ProductDetailDto update(Long staffId, Long id, UpdateProductRequest req) {
		Staff staff = staff(staffId);
		Product p = productRepository.findById(id)
				.orElseThrow(() -> BusinessException.notFound("PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm"));
		BigDecimal oldPrice = p.getPrice();
		String oldDesc = p.getDescription();
		String oldMaterial = p.getMaterial();
		boolean oldVisible = p.isVisible();
		if (req.price() != null && req.price().signum() <= 0) {
			throw BusinessException.badRequest("INVALID_PRICE", "Giá bán phải lớn hơn 0");
		}
		p.updateInfo(req.price(), req.description(), req.material());
		if (req.visible() != null) {
			p.setVisible(req.visible());
		}
		log(staff, p, "price", oldPrice.toPlainString(), p.getPrice().toPlainString());
		log(staff, p, "description", oldDesc, p.getDescription());
		log(staff, p, "material", oldMaterial, p.getMaterial());
		log(staff, p, "is_visible", String.valueOf(oldVisible), String.valueOf(p.isVisible()));
		productRepository.save(p);
		return DtoMapper.productDetail(p, hasSizeChart(p));
	}

	/**
	 * BỔ SUNG - thiết kế thiếu phương thức service cho AdminProductController.listAll (Hình 4.8):
	 * nhân viên xem mọi sản phẩm kể cả đang ẩn, kèm biến thể và tồn kho. Ghi vào báo cáo lệch thiết kế.
	 */
	@Transactional(readOnly = true)
	public List<ProductDetailDto> listAllForStaff() {
		ProductFilter all = new ProductFilter();
		all.setIncludeHidden(true);
		return productRepository.search(all).stream().map(p -> DtoMapper.productDetail(p, hasSizeChart(p))).toList();
	}

	// ===== Hỗ trợ (private) =====

	private Staff staff(Long staffId) {
		return staffRepository.findById(staffId)
				.orElseThrow(() -> BusinessException.forbidden("STAFF_NOT_FOUND", "Không tìm thấy nhân viên"));
	}

	private boolean hasSizeChart(Product p) {
		return !sizeChartRepository.findByProductIdOrderByHeightMinAsc(p.getId()).isEmpty();
	}

	private void log(Staff staff, Product p, String field, String oldValue, String newValue) {
		if (Objects.equals(oldValue, newValue)) {
			return;
		}
		ChangeLog log = ChangeLog.of(staff, null, field, truncate(oldValue), truncate(newValue));
		log.setProduct(p);
		productChangeLogRepository.save(log);
	}

	private static String truncate(String s) {
		return s == null || s.length() <= 255 ? s : s.substring(0, 252) + "...";
	}
}
