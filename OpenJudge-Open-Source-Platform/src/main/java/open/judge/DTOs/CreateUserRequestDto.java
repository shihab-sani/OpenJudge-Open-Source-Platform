package open.judge.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import open.judge.Entity.Role;

public record CreateUserRequestDto(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 100) String displayName,
        Role role
) {}
