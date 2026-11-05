package open.judge.GitHubAPIConfig;

public record GitHubRepoInfo(
        String name,
        String description,
        String language,
        int stargazers_count,
        int forks_count,
        String html_url
) {}
