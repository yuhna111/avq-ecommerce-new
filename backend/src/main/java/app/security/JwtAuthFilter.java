package app.security;

import infra.JwtTokenProvider;
import infra.TokenClaims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Đọc "Authorization: Bearer <jwt>"; token hợp lệ -> đặt AuthUser + quyền ROLE_* vào SecurityContext. */
public class JwtAuthFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			try {
				TokenClaims claims = jwtTokenProvider.parse(header.substring(7));
				List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
				authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.role()));
				if ("OWNER".equals(claims.role())) {
					authorities.add(new SimpleGrantedAuthority("ROLE_STAFF")); // chủ cửa hàng có mọi quyền nhân viên
				}
				SecurityContextHolder.getContext().setAuthentication(
						new UsernamePasswordAuthenticationToken(new AuthUser(claims.userId(), claims.role()), null, authorities));
			} catch (JwtException | IllegalArgumentException e) {
				// Token sai/hết hạn: coi như chưa đăng nhập; endpoint cần đăng nhập sẽ trả 401
				SecurityContextHolder.clearContext();
			}
		}
		chain.doFilter(request, response);
	}
}
