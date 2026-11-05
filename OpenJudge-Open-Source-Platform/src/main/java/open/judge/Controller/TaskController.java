package open.judge.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import open.judge.DTOs.CreateTaskRequestDto;
import open.judge.DTOs.TaskResponseDto;
import open.judge.Entity.ContributionTask;
import open.judge.Entity.Difficulty;
import open.judge.Services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks")
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a contribution task with evaluator rules")
    public ResponseEntity<TaskResponseDto> create(@Valid @RequestBody CreateTaskRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponseDto.from(taskService.createTask(request)));
    }

    @GetMapping("/{id}")
    public TaskResponseDto get(@PathVariable Long id) {
        return TaskResponseDto.from(taskService.getById(id));
    }

    @GetMapping
    public List<TaskResponseDto> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Difficulty difficulty) {
        List<ContributionTask> tasks = projectId != null
                ? taskService.listByProject(projectId)
                : difficulty != null
                ? taskService.listByDifficulty(difficulty)
                : List.of();
        return tasks.stream().map(TaskResponseDto::from).toList();
    }
}
