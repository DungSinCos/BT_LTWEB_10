package vn.edu.demo.jwtnimbus.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.demo.jwtnimbus.auth.AuthService;
import vn.edu.demo.jwtnimbus.user.AppUser;
import vn.edu.demo.jwtnimbus.user.AppUserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AppUserRepository users;
    public UserController(AppUserRepository users) { this.users = users; }

    @GetMapping("/me")
    public AuthDtos.UserResponse currentUser(@AuthenticationPrincipal AppUser user) {
        return AuthService.toUserResponse(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuthDtos.UserResponse> allUsers() {
        return users.findAll().stream().map(AuthService::toUserResponse).toList();
    }
}
