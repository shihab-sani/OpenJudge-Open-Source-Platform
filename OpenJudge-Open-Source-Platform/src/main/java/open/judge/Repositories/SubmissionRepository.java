package open.judge.Repositories;

import open.judge.Entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByContributorId(Long contributorId);
    List<Submission> findByTaskId(Long taskId);
}
