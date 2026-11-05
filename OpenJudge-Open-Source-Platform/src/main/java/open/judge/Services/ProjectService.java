package open.judge.Services;

import lombok.RequiredArgsConstructor;
import open.judge.DTOs.CreateProjectRequestDto;
import open.judge.Entity.Project;
import open.judge.Entity.User;
import open.judge.GitHubAPIConfig.GitHubService;
import open.judge.GlobalExceptionHandler.ResourceNotFoundException;
import open.judge.Repositories.ProjectRepository;
import open.judge.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final GitHubService gitHubService;

    public Project createProject(CreateProjectRequestDto request) {
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        // Validate repository URL against GitHub's free REST API
        var repoInfo = gitHubService.parseAndFetch(request.repositoryUrl());

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description() != null
                ? request.description() : repoInfo.description());
        project.setRepositoryUrl(request.repositoryUrl());
        project.setPrimaryLanguage(request.primaryLanguage() != null
                ? request.primaryLanguage() : repoInfo.language());
        project.setTags(request.tags() == null ? new HashSet<>() : new HashSet<>(request.tags()));
        project.setOwner(owner);
        return projectRepository.save(project);
    }

    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    public List<Project> listAll() {
        return projectRepository.findAll();
    }
}
