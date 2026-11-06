package open.judge.GitHubAPIConfig;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import open.judge.GlobalExceptionHandler.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.regex.Pattern;


@Service
@Slf4j
public class GitHubService {
    private final WebClient webClient;

    public GitHubService(@Value("${github.token:}") String githubToken) {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl("https://api.github.com")
                // GitHub recommends using your GitHub username as the User-Agent
                .defaultHeader("User-Agent", "shihab-sani")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");

        // If a token is provided in application.yml, attach it as a Bearer token
        if (githubToken != null && !githubToken.trim().isEmpty()) {
            builder.defaultHeader("Authorization", "Bearer " + githubToken);
        }

        this.webClient = builder.build();
    }

    public GitHubRepoInfo fetchRepoInfo(String owner, String repo) {
        try {
            return webClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .bodyToMono(GitHubRepoInfo.class)
                    .block(Duration.ofSeconds(5));
        } catch (WebClientResponseException e) {
            // Log the exact rate-limit headers to help debug if it fails again
            String remaining = e.getHeaders().getFirst("x-ratelimit-remaining");
            String resetTime = e.getHeaders().getFirst("x-ratelimit-reset");
            log.error("GitHub API Error. Status: {}, Remaining Requests: {}, Reset Time: {}",
                    e.getStatusCode(), remaining, resetTime);

            throw new BusinessException("GITHUB_API_ERROR",
                    "GitHub API rejected the request: " + e.getStatusText());
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
