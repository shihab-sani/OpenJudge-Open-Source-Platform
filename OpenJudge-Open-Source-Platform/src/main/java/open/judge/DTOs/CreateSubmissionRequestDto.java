package open.judge.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSubmissionRequestDto {
    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Contributor ID is required")
    private Long contributorId;

    @NotBlank(message = "Source code cannot be empty")
    @Size(max = 100_000, message = "Source code exceeds maximum size")
    private String sourceCode;

    @NotBlank(message = "File name is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Invalid file name")
    private String fileName;
}
