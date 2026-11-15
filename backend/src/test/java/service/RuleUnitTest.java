package service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import domain.Customer;
import domain.FitPref;
import domain.FitType;
import domain.Order;
import domain.OrderStatus;
import domain.SizeChart;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import repository.OrderRepository;
import repository.OrderStatusHistoryRepository;

/** Kiểm thử đơn vị (không cần CSDL): BR09/DT-01, DT-04, BR16, FB-01. */
class RuleUnitTest {

	// ---------- BR09 / DT-01 ----------
	@ParameterizedTest(name = "tạm tính {0} -> phí {1}")
	@CsvSource({ "0,30000", "249000,30000", "350000,30000", "499999,30000", "500000,0", "520000,0", "708000,0" })
	void dt01_phiGiaoHang(long subtotal, long fee) {
		assertThat(new ShippingFeeCalculator().calculate(BigDecimal.valueOf(subtotal)))
				.isEqualByComparingTo(BigDecimal.valueOf(fee));
	}

	// ---------- DT-04 ----------
	private final OrderStateMachine sm = new OrderStateMachine(Mockito.mock(OrderRepository.class),
			Mockito.mock(OrderStatusHistoryRepository.class));

	@Test
	void dt04_bangChuyenTrangThai() {
		assertThat(sm.nextStatuses(OrderStatus.CHO_THANH_TOAN)).containsExactlyInAnyOrder(OrderStatus.CHO_XAC_NHAN, OrderStatus.DA_HUY);
		assertThat(sm.nextStatuses(OrderStatus.CHO_XAC_NHAN)).containsExactlyInAnyOrder(OrderStatus.DANG_GIAO, OrderStatus.DA_HUY);
		assertThat(sm.nextStatuses(OrderStatus.DANG_GIAO)).containsExactly(OrderStatus.DA_GIAO);
		assertThat(sm.nextStatuses(OrderStatus.DA_GIAO)).isEmpty();
		assertThat(sm.nextStatuses(OrderStatus.DA_HUY)).isEmpty();
	}

	@ParameterizedTest(name = "{0} -> {1} bị từ chối")
	@CsvSource({ "CHO_THANH_TOAN,DANG_GIAO", "CHO_THANH_TOAN,DA_GIAO", "CHO_XAC_NHAN,DA_GIAO", "CHO_XAC_NHAN,CHO_THANH_TOAN",
			"DANG_GIAO,DA_HUY", "DANG_GIAO,CHO_XAC_NHAN", "DA_GIAO,DA_HUY", "DA_HUY,CHO_XAC_NHAN" })
	void dt04_chuyenKhongHopLe_biTuChoi(OrderStatus from, OrderStatus to) {
		Order o = new Order();
		o.setStatus(from);
		assertThatThrownBy(() -> sm.transit(o, to, null, null))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "INVALID_TRANSITION");
		assertThat(o.getStatus()).isEqualTo(from);
	}

	@Test
	void dt04_sangDangGiao_sinhMaVanDon() {
		Order o = new Order();
		o.setStatus(OrderStatus.CHO_XAC_NHAN);
		sm.transit(o, OrderStatus.DANG_GIAO, null, null);
		assertThat(o.getStatus()).isEqualTo(OrderStatus.DANG_GIAO);
		assertThat(o.getTrackingCode()).startsWith("VD").hasSize(14);
	}

	// ---------- BR16 ----------
	@Test
	void br16_sai5LanKhoa15Phut_dungThiResetBoDem() {
		Customer u = new Customer();
		LocalDateTime now = LocalDateTime.of(2026, 11, 16, 10, 0);
		for (int i = 0; i < 4; i++) {
			u.recordFailedLogin(now);
		}
		assertThat(u.isLocked(now)).isFalse();
		assertThat(u.getFailedLoginCount()).isEqualTo(4);
		u.recordFailedLogin(now);
		assertThat(u.isLocked(now)).isTrue();
		assertThat(u.isLocked(now.plusMinutes(14))).isTrue();
		assertThat(u.isLocked(now.plusMinutes(15))).isFalse();
		u.resetFailedCount();
		assertThat(u.getFailedLoginCount()).isZero();
		assertThat(u.getLockedUntil()).isNull();
	}

	// ---------- FB-01 ----------
	private final FallbackPolicy policy = new FallbackPolicy();
	private final List<SizeChart> chart = List.of(size("L", 170, 180, 65, 75), size("S", 150, 160, 45, 55), size("M", 160, 170, 55, 65));

	@Test
	void fb01_khopCaChieuCaoVaCanNang() {
		assertThat(policy.ruleBasedSize(chart, 165, 60, FitPref.VUA, FitType.REGULAR)).isEqualTo("M");
		// 160 nằm ở biên S và M: lấy size đầu tiên theo chiều cao tăng dần
		assertThat(policy.ruleBasedSize(chart, 160, 55, FitPref.VUA, FitType.REGULAR)).isEqualTo("S");
	}

	@Test
	void fb01_ngoaiBang_theoChieuCaoGanNhat() {
		assertThat(policy.ruleBasedSize(chart, 190, 90, FitPref.VUA, FitType.REGULAR)).isEqualTo("L");
		assertThat(policy.ruleBasedSize(chart, 140, 40, FitPref.OM, FitType.SLIM)).isEqualTo("S");
		assertThat(policy.ruleBasedSize(chart, 165, 90, FitPref.VUA, FitType.REGULAR)).isEqualTo("M"); // cân nặng lệch
	}

	@Test
	void fb01_oversizeVaUaVua_luiMotSize() {
		assertThat(policy.ruleBasedSize(chart, 175, 70, FitPref.VUA, FitType.OVERSIZE)).isEqualTo("M");
		assertThat(policy.ruleBasedSize(chart, 155, 50, FitPref.VUA, FitType.OVERSIZE)).isEqualTo("S"); // không lùi dưới size nhỏ nhất
		assertThat(policy.ruleBasedSize(chart, 175, 70, FitPref.RONG, FitType.OVERSIZE)).isEqualTo("L");
	}

	@Test
	void fb01_khongCoBangSize_traNull() {
		assertThat(policy.ruleBasedSize(List.of(), 170, 60, FitPref.VUA, FitType.REGULAR)).isNull();
	}

	private static SizeChart size(String s, int hMin, int hMax, int wMin, int wMax) {
		SizeChart c = new SizeChart();
		c.setSize(s);
		c.setHeightMin(hMin);
		c.setHeightMax(hMax);
		c.setWeightMin(wMin);
		c.setWeightMax(wMax);
		return c;
	}
}
