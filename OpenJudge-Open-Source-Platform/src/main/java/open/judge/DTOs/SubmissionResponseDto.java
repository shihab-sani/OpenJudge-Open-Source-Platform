package open.judge.DTOs;

import open.judge.Entity.Submission;

import java.time.Instant;

public record SubmissionResponseDto(
        Long id, Long taskId, Long contributorId, String status,
        String verdict, String verdictMessage, Instant createdAt, Instant evaluatedAt
) {
    public static SubmissionResponseDto from(Submission s) {
        return new SubmissionResponseDto(s.getId(), s.getTask().getId(),
                s.getContributor().getId(), s.getStatus().name(),
                s.getVerdict() == null ? null : s.getVerdict().name(),
                s.getVerdictMessage(), s.getCreatedAt(), s.getEvaluatedAt());
    }
}
