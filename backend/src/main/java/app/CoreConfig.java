package app;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Cấu hình chung: băm mật khẩu BCrypt (BR16), bật lịch chạy OrderExpiryScheduler (BR12). */
@Configuration
public class CoreConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/** Tắt được bằng app.scheduling.enabled=false (dùng khi chạy kiểm thử). */
	@Configuration
	@EnableScheduling
	@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
	static class SchedulingConfig {
	}
}
