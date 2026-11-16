package infra;

import static org.assertj.core.api.Assertions.assertThat;

import domain.Order;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Cổng giả lập: giao dịch vẫn thanh toán được sau khi máy chủ khởi động lại (bộ nhớ issued bị mất). */
class MockGatewayRestoreTest {
	private static final String SECRET = "TEST-SECRET";

	private static Map<String, String> parse(String url) {
		Map<String, String> p = new HashMap<String, String>();
		for (String kv : url.substring(url.indexOf('?') + 1).split("&")) {
			String[] a = kv.split("=", 2);
			p.put(a[0], URLDecoder.decode(a[1], StandardCharsets.UTF_8));
		}
		return p;
	}

	private static Map<String, String> issueLink() {
		Order order = Mockito.mock(Order.class);
		Mockito.when(order.getTotal()).thenReturn(new BigDecimal("349000"));
		Mockito.when(order.getOrderCode()).thenReturn("AVQ0001");
		return parse(new MockPaymentGatewayClient(SECRET).buildPaymentUrl(order, "AVQ0001"));
	}

	@Test
	void sauKhiKhoiDongLai_linkDaKy_khoiPhucDuocGiaoDich() {
		Map<String, String> link = issueLink();
		MockPaymentGatewayClient restarted = new MockPaymentGatewayClient(SECRET);
		assertThat(restarted.simulateCallback("AVQ0001", true)).isNull();

		assertThat(restarted.restoreIssued(link)).isTrue();
		Map<String, String> ipn = restarted.simulateCallback("AVQ0001", true);
		assertThat(ipn).containsEntry("vnp_Amount", "34900000").containsEntry("vnp_ResponseCode", "00");
		assertThat(restarted.verifySignature(ipn)).isTrue();
	}

	@Test
	void linkBiSuaSoTien_khongDuocKhoiPhuc() {
		Map<String, String> link = issueLink();
		link.put("vnp_Amount", "100");
		MockPaymentGatewayClient restarted = new MockPaymentGatewayClient(SECRET);
		assertThat(restarted.restoreIssued(link)).isFalse();
		assertThat(restarted.simulateCallback("AVQ0001", true)).isNull();
	}
}
