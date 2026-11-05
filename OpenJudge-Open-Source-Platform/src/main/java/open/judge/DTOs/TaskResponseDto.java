package open.judge.DTOs;

import open.judge.Entity.ContributionTask;
import open.judge.Entity.Difficulty;
import open.judge.Entity.TaskRules;

import java.time.Instant;

public record TaskResponseDto(
        Long id, Long projectId, String title, String description,
        Difficulty difficulty, TaskRules rules, Instant createdAt
) {
    public static TaskResponseDto from(ContributionTask t) {
        return new TaskResponseDto(t.getId(), t.getProject().getId(), t.getTitle(),
                t.getDescription(), t.getDifficulty(), t.getRules(), t.getCreatedAt());
    }
}
