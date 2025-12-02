package yunxun.ai.canary.backend.auth.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import yunxun.ai.canary.backend.auth.model.dto.LoginRequest;
import yunxun.ai.canary.backend.auth.model.dto.RegisterRequest;
import yunxun.ai.canary.backend.auth.model.dto.TokenResponse;
import yunxun.ai.canary.backend.user.model.dto.UserProfileDto;
import yunxun.ai.canary.backend.user.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public TokenResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String refreshToken = extractToken(authorization);
        userService.logout(refreshToken);
    }

    @GetMapping("/me")
    public UserProfileDto me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String token = extractToken(authorization);
        return userService.getProfile(token).orElse(null);
    }

    private String extractToken(String authorizationHeader) {
        return Optional.ofNullable(authorizationHeader)
                .filter(h -> h.toLowerCase().startsWith("bearer "))
                .map(h -> h.substring(7))
                .orElse(authorizationHeader);
    }
}

