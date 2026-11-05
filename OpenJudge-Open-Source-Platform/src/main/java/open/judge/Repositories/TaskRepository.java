package open.judge.Repositories;

import open.judge.Entity.ContributionTask;
import open.judge.Entity.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<ContributionTask, Long> {
    List<ContributionTask> findByProjectId(Long projectId);
    List<ContributionTask> findByDifficulty(Difficulty difficulty);
}
