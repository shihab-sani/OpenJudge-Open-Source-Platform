package open.judge.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import open.judge.DTOs.CreateProjectRequestDto;
import open.judge.DTOs.ProjectResponseDto;
import open.judge.Services.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Register a project (validates the GitHub URL via the free GitHub API)")
    public ResponseEntity<ProjectResponseDto> create(@Valid @RequestBody CreateProjectRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProjectResponseDto.from(projectService.createProject(request)));
    }

    @GetMapping("/{id}")
    public ProjectResponseDto get(@PathVariable Long id) {
        return ProjectResponseDto.from(projectService.getById(id));
    }

    @GetMapping
    public List<ProjectResponseDto> list() {
        return projectService.listAll().stream().map(ProjectResponseDto::from).toList();
    }
}
