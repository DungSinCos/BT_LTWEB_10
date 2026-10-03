package vn.edu.demo.jwtnimbus.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() { }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 60) String username,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(min = 8, max = 72) String password) { }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }

    public record UserResponse(Long id, String username, String email, java.util.Set<String> roles) { }

    public record AuthResponse(String tokenType, String accessToken, long expiresInSeconds, UserResponse user) { }

    public record MessageResponse(String message) { }
}
