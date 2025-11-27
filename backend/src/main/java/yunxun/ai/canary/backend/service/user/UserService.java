package yunxun.ai.canary.backend.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.auth.LoginRequest;
import yunxun.ai.canary.backend.model.dto.auth.RegisterRequest;
import yunxun.ai.canary.backend.model.dto.auth.TokenResponse;
import yunxun.ai.canary.backend.model.dto.auth.UserProfileDto;
import yunxun.ai.canary.backend.model.entity.user.UserEntity;
import yunxun.ai.canary.backend.repository.mysql.UserMapper;
import yunxun.ai.canary.backend.service.auth.JwtService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * minimal in-memory storage for refresh token invalidation
     */
    private final Map<String, Long> refreshTokenStore = new ConcurrentHashMap<>();

    public TokenResponse register(RegisterRequest request) {
        UserEntity existing = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, request.getEmail()));
        if (existing != null) {
            throw new IllegalArgumentException("Email already registered");
        }
        LocalDateTime now = LocalDateTime.now();
        UserEntity entity = UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(Optional.ofNullable(request.getDisplayName()).orElse("用户"))
                .status("active")
                .createdAt(now)
                .updatedAt(now)
                .build();
        userMapper.insert(entity);
        return issueTokens(entity);
    }

    public TokenResponse login(LoginRequest request) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, request.getEmail()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenStore.remove(refreshToken);
        }
    }

    public Optional<UserProfileDto> getProfile(String token) {
        return jwtService.parseUserId(token)
                .flatMap(id -> Optional.ofNullable(userMapper.selectById(id)))
                .map(user -> UserProfileDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .status(user.getStatus())
                        .build());
    }

    private TokenResponse issueTokens(UserEntity user) {
        String access = jwtService.createAccessToken(user.getId(), user.getEmail());
        String refresh = jwtService.createRefreshToken(user.getId(), user.getEmail());
        refreshTokenStore.put(refresh, user.getId());
        return TokenResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }
}
