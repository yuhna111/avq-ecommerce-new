package service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * BR12: mỗi 60 giây hủy các đơn Chờ thanh toán quá 15 phút (gọi OrderService.expireOverdue).
 * Đặt trong service (không phải infra) để tránh phụ thuộc vòng service <-> infra (design-notes mục 2).
 */
@Component
public class OrderExpiryScheduler {
	private static final Logger log = LoggerFactory.getLogger(OrderExpiryScheduler.class);
	private final OrderService orderService;

	public OrderExpiryScheduler(OrderService orderService) {
		this.orderService = orderService;
	}

	@Scheduled(fixedDelayString = "${order.expiry.check-interval-ms:60000}", initialDelay = 10_000)
	public void expirePendingOrders() {
		int n = orderService.expireOverdue(LocalDateTime.now());
		if (n > 0) {
			log.info("Đã tự hủy {} đơn quá hạn thanh toán", n);
		}
	}
}
