package api.dto;

public record AddressDto(Long id, String receiverName, String receiverPhone, String addressLine, boolean isDefault) {
}
