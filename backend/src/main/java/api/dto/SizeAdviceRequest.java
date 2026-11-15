package api.dto;

import domain.FitPref;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** FB-01: số đo + kiểu mặc ưa thích; saveProfile = khách đồng ý lưu số đo (body_profiles). */
public record SizeAdviceRequest(
		@NotNull(message = "Vui lòng nhập chiều cao") @Min(value = 100, message = "Chiều cao từ 100 đến 220 cm") @Max(value = 220, message = "Chiều cao từ 100 đến 220 cm") Integer heightCm,
		@NotNull(message = "Vui lòng nhập cân nặng") @Min(value = 25, message = "Cân nặng từ 25 đến 150 kg") @Max(value = 150, message = "Cân nặng từ 25 đến 150 kg") Integer weightKg,
		@NotNull(message = "Vui lòng chọn kiểu mặc") FitPref fitPref,
		boolean saveProfile) {
}
