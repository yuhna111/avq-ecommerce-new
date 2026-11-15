package api.dto;

/** Phản hồi theo chuẩn IPN của VNPay: RspCode "00" | "01" | "02" | "04" | "97" (DT-05). */
public record IpnResponse(String RspCode, String Message) {
}
