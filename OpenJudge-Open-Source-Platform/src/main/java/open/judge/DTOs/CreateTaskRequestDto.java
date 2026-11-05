package open.judge.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import open.judge.Entity.Difficulty;
import open.judge.Entity.TaskRules;

public record CreateTaskRequestDto(
        @NotNull Long projectId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String description,
        Difficulty difficulty,
        @NotNull @Valid TaskRules rules
) {}
