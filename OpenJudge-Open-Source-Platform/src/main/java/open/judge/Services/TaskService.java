package open.judge.Services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import open.judge.DTOs.CreateTaskRequestDto;
import open.judge.Entity.*;
import open.judge.GlobalExceptionHandler.BusinessException;
import open.judge.GlobalExceptionHandler.ResourceNotFoundException;
import open.judge.Repositories.ProjectRepository;
import open.judge.Repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public ContributionTask createTask(@Valid CreateTaskRequestDto request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateRules(request.rules());

        ContributionTask task = new ContributionTask();
        task.setProject(project);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDifficulty(request.difficulty() == null ? Difficulty.BEGINNER : request.difficulty());
        task.setRules(request.rules());
        return taskRepository.save(task);
    }

    private void validateRules(TaskRules rules) {
        if (rules == null || rules.getLanguage() == null) {
            throw new BusinessException("INVALID_RULES", "Rules must specify a language");
        }
        if (rules.getTestCases() == null || rules.getTestCases().isEmpty()) {
            throw new BusinessException("INVALID_RULES", "At least one test case is required");
        }
        for (TestCaseRule tc : rules.getTestCases()) {
            if (tc.getExpectedOutput() == null) {
                throw new BusinessException("INVALID_RULES", "Every test case needs an expected output");
            }
        }
    }

    public ContributionTask getById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    public List<ContributionTask> listByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public List<ContributionTask> listByDifficulty(Difficulty difficulty) {
        return taskRepository.findByDifficulty(difficulty);
    }
}
