package service;

import domain.ChangeLog;
import domain.OrderItem;
import domain.ProductVariant;
import domain.Staff;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repository.ProductChangeLogRepository;
import repository.ProductVariantRepository;
import repository.StaffRepository;

@Service
public class StockService {
	private final ProductVariantRepository productVariantRepository;
	private final StaffRepository staffRepository;
	private final ProductChangeLogRepository productChangeLogRepository;

	public StockService(ProductVariantRepository productVariantRepository, StaffRepository staffRepository,
			ProductChangeLogRepository productChangeLogRepository) {
		this.productVariantRepository = productVariantRepository;
		this.staffRepository = staffRepository;
		this.productChangeLogRepository = productChangeLogRepository;
	}

	/**
	 * BR11: giữ hàng khi tạo đơn - gọi TRƯỚC khi lưu đơn, trong cùng giao dịch của OrderService.place.
	 * Trừ theo thứ tự variant_id tăng dần để các giao dịch đồng thời khóa dòng cùng thứ tự (tránh deadlock).
	 * Thiếu hàng -> EX-06, ném lỗi để rollback toàn bộ (không tạo đơn, giỏ giữ nguyên).
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void reserve(List<OrderItem> items) {
		List<OrderItem> sorted = new ArrayList<OrderItem>(items);
		sorted.sort(Comparator.comparing(i -> i.getVariant().getId()));
		List<String> shortages = new ArrayList<String>();
		for (OrderItem item : sorted) {
			int updated = productVariantRepository.decreaseIfEnough(item.getVariant().getId(), item.getQuantity());
			if (updated == 0) {
				shortages.add(item.getProductName() + " (size " + item.getSize() + ", màu " + item.getColor() + ")");
			}
		}
		if (!shortages.isEmpty()) {
			throw BusinessException.conflict("OUT_OF_STOCK",
					"Không đủ hàng, vui lòng chỉnh lại giỏ: " + String.join("; ", shortages));
		}
	}

	/** BR11: hoàn kho khi đơn bị hủy (cùng giao dịch với việc hủy). */
	@Transactional(propagation = Propagation.MANDATORY)
	public void restore(List<OrderItem> items) {
		List<OrderItem> sorted = new ArrayList<OrderItem>(items);
		sorted.sort(Comparator.comparing(i -> i.getVariant().getId()));
		for (OrderItem item : sorted) {
			productVariantRepository.increase(item.getVariant().getId(), item.getQuantity());
		}
	}

	/** UC19 - BR06: tồn kho không âm; FR31: ghi product_change_logs; DB-R1: khóa lạc quan theo version. */
	@Transactional
	public void updateStock(Long staffId, Long variantId, int stock) {
		if (stock < 0) {
			throw BusinessException.badRequest("INVALID_STOCK", "Tồn kho phải là số nguyên không âm");
		}
		Staff staff = staffRepository.findById(staffId)
				.orElseThrow(() -> BusinessException.forbidden("STAFF_NOT_FOUND", "Không tìm thấy nhân viên"));
		ProductVariant variant = productVariantRepository.findById(variantId)
				.orElseThrow(() -> BusinessException.notFound("VARIANT_NOT_FOUND", "Không tìm thấy biến thể"));
		int old = variant.getStockQty();
		if (old == stock) {
			return;
		}
		variant.setStock(stock);
		productVariantRepository.saveAndFlush(variant);
		productChangeLogRepository.save(ChangeLog.of(staff, variant.getProduct(), variant, "stock_qty", String.valueOf(old), String.valueOf(stock)));
	}
}
