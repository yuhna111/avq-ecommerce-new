package infra;

import domain.Order;

public interface PaymentGatewayClient {

	public String buildPaymentUrl(Order order, String txnRef);

	public boolean verifySignature(Map<String, String> params);
}