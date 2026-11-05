package open.judge.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateProjectRequestDto(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @NotBlank @Pattern(regexp = "https://github\\.com/[^/]+/[^/]+",
                message = "Must be a valid GitHub repository URL") String repositoryUrl,
        @Size(max = 100) String primaryLanguage,
        Set<@Size(max = 50) String> tags,
        @NotNull Long ownerId
) {
}
