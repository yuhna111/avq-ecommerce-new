package app;

import app.security.AuthUser;
import app.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import infra.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * BR03, BR15: xem/tìm sản phẩm không cần đăng nhập; giỏ, đặt hàng, đơn của tôi cần vai trò khách;
 * /api/admin/** chỉ nhân viên (STAFF, OWNER). Không lưu phiên (JWT).
 */
@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider, ObjectMapper mapper)
			throws Exception {
		http
				.csrf(c -> c.disable())
				.cors(c -> {
				})
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.anonymous(a -> a.principal(AuthUser.ANONYMOUS).authorities(AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/products/*/size-advice").permitAll()
						.requestMatchers("/api/payments/vnpay-ipn").permitAll()
						.requestMatchers("/api/payments/mock/**").hasRole("CUSTOMER")
						.requestMatchers("/api/cart/**", "/api/orders/**", "/api/account/**").hasRole("CUSTOMER")
						.requestMatchers("/api/admin/**").hasRole("STAFF")
						.requestMatchers("/api/**").denyAll()
						.anyRequest().permitAll())
				.exceptionHandling(e -> e
						.authenticationEntryPoint((req, res, ex) ->
								write(res, mapper, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
										"Vui lòng đăng nhập để tiếp tục"))
						.accessDeniedHandler((req, res, ex) ->
								write(res, mapper, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
										"Bạn không có quyền thực hiện thao tác này")))
				.addFilterBefore(new JwtAuthFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	/** Frontend chạy qua proxy Vite (cùng nguồn); vẫn cho phép gọi thẳng từ cổng 5173 khi cần. */
	@Bean
	public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:http://localhost:5173}") List<String> origins) {
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(origins);
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", cfg);
		return source;
	}

	private static void write(HttpServletResponse res, ObjectMapper mapper, int status, String code, String message)
			throws java.io.IOException {
		res.setStatus(status);
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setCharacterEncoding("UTF-8");
		mapper.writeValue(res.getOutputStream(), Map.of("code", code, "message", message));
	}
}
