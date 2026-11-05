package open.judge.DTOs;

import open.judge.Entity.Project;

import java.time.Instant;
import java.util.Set;

public record ProjectResponseDto(
        Long id, String name, String description, String repositoryUrl,
        String primaryLanguage, Set<String> tags, Long ownerId, Instant createdAt
) {
    public static ProjectResponseDto from(Project p) {
        return new ProjectResponseDto(p.getId(), p.getName(), p.getDescription(),
                p.getRepositoryUrl(), p.getPrimaryLanguage(), p.getTags(),
                p.getOwner().getId(), p.getCreatedAt());
    }
}
