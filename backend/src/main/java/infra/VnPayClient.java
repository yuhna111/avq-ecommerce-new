package infra;

import domain.Order;
import java.util.Map;

public class VnPayClient implements PaymentGatewayClient {

	public String buildPaymentUrl(Order order, String txnRef) {
		throw new UnsupportedOperationException();
	}

	public boolean verifySignature(Map<String, String> params) {
		throw new UnsupportedOperationException();
	}
}