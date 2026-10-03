package vn.edu.demo.jwtnimbus.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.demo.jwtnimbus.api.AuthDtos;
import vn.edu.demo.jwtnimbus.jwt.NimbusJwtService;
import vn.edu.demo.jwtnimbus.user.AppUser;
import vn.edu.demo.jwtnimbus.user.AppUserRepository;

import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final NimbusJwtService jwtService;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, NimbusJwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.UserResponse register(AuthDtos.RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByUsername(username)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        AppUser user = users.save(new AppUser(username, email, passwordEncoder.encode(request.password()), Set.of("USER")));
        return toUserResponse(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        AppUser user = users.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        return new AuthDtos.AuthResponse("Bearer", jwtService.createToken(user), jwtService.getExpirationSeconds(), toUserResponse(user));
    }

    public static AuthDtos.UserResponse toUserResponse(AppUser user) {
        return new AuthDtos.UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles());
    }
}
