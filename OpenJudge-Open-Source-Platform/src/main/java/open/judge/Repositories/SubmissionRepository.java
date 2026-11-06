package open.judge.Repositories;

import open.judge.Entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByContributorId(Long contributorId);
    List<Submission> findByTaskId(Long taskId);

    @Query("SELECT s FROM Submission s JOIN FETCH s.task WHERE s.id = :id")
    Optional<Submission> findByIdWithTask(@Param("id") Long id);
}
