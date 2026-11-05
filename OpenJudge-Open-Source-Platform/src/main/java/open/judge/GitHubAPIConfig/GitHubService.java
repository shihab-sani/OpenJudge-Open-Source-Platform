package open.judge.GitHubAPIConfig;

import lombok.RequiredArgsConstructor;
import open.judge.GlobalExceptionHandler.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class GitHubService {
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("User-Agent", "OpenJudgeOpenSourcePlatform/1.0")
            .build();

    public GitHubRepoInfo fetchRepoInfo(String owner, String repo) {
        try {
            return webClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .bodyToMono(GitHubRepoInfo.class)
                    .block(Duration.ofSeconds(5));
        } catch (Exception e) {
            throw new BusinessException("GITHUB_API_ERROR",
                    "Could not fetch repository information: " + e.getMessage());
        }
    }

    public GitHubRepoInfo parseAndFetch(String repositoryUrl) {
        var matcher = Pattern.compile("https://github\\.com/([^/]+)/([^/]+?)(?:\\.git)?/?$")
                .matcher(repositoryUrl);
        if (!matcher.matches()) {
            throw new BusinessException("INVALID_REPO_URL", "Invalid GitHub repository URL");
        }
        return fetchRepoInfo(matcher.group(1), matcher.group(2));
    }
}
