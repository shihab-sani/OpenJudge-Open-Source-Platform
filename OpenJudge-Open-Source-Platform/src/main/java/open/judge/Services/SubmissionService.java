package open.judge.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import open.judge.DTOs.CreateSubmissionRequestDto;
import open.judge.Entity.ContributionTask;
import open.judge.Entity.Submission;
import open.judge.Entity.SubmissionStatus;
import open.judge.Entity.User;
import open.judge.GlobalExceptionHandler.BusinessException;
import open.judge.GlobalExceptionHandler.ResourceNotFoundException;
import open.judge.RedisConfig.EvaluationQueueProducer;
import open.judge.Repositories.SubmissionRepository;
import open.judge.Repositories.TaskRepository;
import open.judge.Repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EvaluationQueueProducer queueProducer;

    @Transactional
    public Submission createSubmission(CreateSubmissionRequestDto request) {
        ContributionTask task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User contributor = userRepository.findById(request.getContributorId())
                .orElseThrow(() -> new ResourceNotFoundException("Contributor not found"));

        if (request.getSourceCode().isBlank()) {
            throw new BusinessException("EMPTY_CODE", "Source code cannot be empty");
        }

        Submission submission = new Submission();
        submission.setTask(task);
        submission.setContributor(contributor);
        submission.setSourceCode(request.getSourceCode());
        submission.setFileName(request.getFileName());
        submission.setStatus(SubmissionStatus.PENDING);

        Submission saved = submissionRepository.save(submission);

        // ✅ THE FIX: Only push to Redis AFTER the database transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                queueProducer.enqueue(saved.getId());
            }
        });

        saved.setStatus(SubmissionStatus.QUEUED);
        return saved;
    }


    public Submission getSubmission(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + id));
    }
}
