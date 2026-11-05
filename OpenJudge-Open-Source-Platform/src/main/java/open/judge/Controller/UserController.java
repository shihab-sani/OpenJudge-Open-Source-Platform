package open.judge.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import open.judge.DTOs.CreateUserRequestDto;
import open.judge.DTOs.UserResponseDto;
import open.judge.Entity.Role;
import open.judge.Entity.User;
import open.judge.Services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody CreateUserRequestDto request) {
        User user = userService.createUser(request.email(), request.displayName(),
                request.role() == null ? Role.CONTRIBUTOR : request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.from(user));
    }

    @GetMapping("/{id}")
    public UserResponseDto get(@PathVariable Long id) {
        return UserResponseDto.from(userService.getById(id));
    }
}
