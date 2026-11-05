package open.judge.SandboxService;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import open.judge.Entity.Submission;
import open.judge.Entity.TaskRules;
import open.judge.Entity.TestCaseRule;
import open.judge.Entity.Verdict;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("SpellCheckingInspection")
public class SandBoxService {
    private final DockerClient dockerClient;

    public Verdict evaluate(Submission submission) {
        TaskRules rules = submission.getTask().getRules();

        for (TestCaseRule tc : rules.getTestCases()) {
            Verdict v = runSingleTest(submission, rules, tc);
            if (v != Verdict.ACCEPTED) {
                return v;
            }
        }
        return Verdict.ACCEPTED;
    }

    private Verdict runSingleTest(Submission submission, TaskRules rules, TestCaseRule tc) {
        String image = mapLanguageToImage(rules.getLanguage());

        String sourceB64 = Base64.getEncoder()
                .encodeToString(submission.getSourceCode().getBytes(StandardCharsets.UTF_8));
        String inputB64 = Base64.getEncoder()
                .encodeToString((tc.getInput() == null ? "" : tc.getInput())
                        .getBytes(StandardCharsets.UTF_8));

        String shellCmd = String.format(
                "set -e; " +
                        "echo %s | base64 -d > /tmp/%s; " +
                        "echo %s | base64 -d > /tmp/stdin.txt; " +
                        "%s < /tmp/stdin.txt",
                sourceB64, submission.getFileName(), inputB64,
                buildRunCommand(rules)
        );

        String containerId = null;
        try {
            // ---- HOST CONFIG: readonly root + writable /tmp + resource limits ----
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withReadonlyRootfs(true)                                    // ✔ non-deprecated on HostConfig
                    .withTmpFs(Map.of("/tmp", "rw,size=64m,exec"))               // ✔ tmpfs now on HostConfig
                    .withMemory(rules.getMemoryLimitMb() * 1024L * 1024L)
                    .withCpuCount(1L);

            containerId = dockerClient.createContainerCmd(image)
                    .withCmd("sh", "-c", shellCmd)
                    .withNetworkDisabled(true)
                    .withHostConfig(hostConfig)                                  // ✔ attach HostConfig
                    .exec()
                    .getId();

            dockerClient.startContainerCmd(containerId).exec();

            int exitCode;
            try {
                exitCode = dockerClient.waitContainerCmd(containerId)
                        .exec(new WaitContainerResultCallback())
                        .awaitStatusCode(rules.getTimeoutSeconds(), TimeUnit.SECONDS);
            } catch (Exception timeoutEx) {
                log.warn("Sandbox timeout for submission {}", submission.getId());
                return Verdict.TIME_LIMIT_EXCEEDED;
            }

            final StringBuilder logs = new StringBuilder();
            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            logs.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
                        }
                    })
                    .awaitCompletion(rules.getTimeoutSeconds(), TimeUnit.SECONDS);

            String output = logs.toString();

            if (exitCode != 0) {
                String trimmed = output.trim();
                if (trimmed.isEmpty()) {
                    return Verdict.RUNTIME_ERROR;
                }
                if (rules.getLanguage().equalsIgnoreCase("java")
                        && trimmed.toLowerCase().contains("error:")) {
                    return Verdict.COMPILATION_ERROR;
                }
                return Verdict.RUNTIME_ERROR;
            }

            if (!output.trim().equals(tc.getExpectedOutput().trim())) {
                return Verdict.WRONG_ANSWER;
            }
            return Verdict.ACCEPTED;

        } catch (Exception e) {
            log.error("Sandbox evaluation failed for submission {}", submission.getId(), e);
            return Verdict.INTERNAL_ERROR;
        } finally {
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception ignored) {
                    log.warn("Could not remove container {}", containerId);
                }
            }
        }
    }

    private String buildRunCommand(TaskRules rules) {
        return switch (rules.getLanguage().toLowerCase()) {
            case "python" -> "python3 /tmp/" + rules.getEntryPoint();
            case "java"   -> "javac -d /tmp /tmp/" + rules.getEntryPoint() + " && java -cp /tmp Main";
            default       -> throw new IllegalArgumentException(
                    "Unsupported language: " + rules.getLanguage());
        };
    }

    private String mapLanguageToImage(String language) {
        return switch (language.toLowerCase()) {
            case "python" -> "python:3.12-alpine";
            case "java"   -> "eclipse-temurin:21-alpine";
            default -> throw new IllegalArgumentException(
                    "Unsupported language: " + language);
        };
    }
}
