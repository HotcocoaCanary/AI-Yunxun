package yunxun.ai.canary.backend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import yunxun.ai.canary.backend.user.model.entity.UserEntity;
import yunxun.ai.canary.backend.user.repository.mysql.UserMapper;
import yunxun.ai.canary.backend.auth.service.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = resolveToken(header);
        if (StringUtils.hasText(token)) {
            Optional<Long> userIdOpt = jwtService.parseUserId(token);
            if (userIdOpt.isPresent()) {
                UserEntity user = userMapper.selectById(userIdOpt.get());
                if (user != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user.getId(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(String header) {
        if (!StringUtils.hasText(header)) {
            return null;
        }
        if (header.toLowerCase().startsWith("bearer ")) {
            return header.substring(7);
        }
        return header;
    }
}
