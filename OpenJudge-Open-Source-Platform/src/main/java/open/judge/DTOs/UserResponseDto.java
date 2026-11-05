package open.judge.DTOs;

import open.judge.Entity.Role;
import open.judge.Entity.User;

import java.time.Instant;

public record UserResponseDto(Long id, String email, String displayName, Role role, Instant createdAt) {
    public static UserResponseDto from(User u) {
        return new UserResponseDto(u.getId(), u.getEmail(), u.getDisplayName(), u.getRole(), u.getCreatedAt());
    }
}
