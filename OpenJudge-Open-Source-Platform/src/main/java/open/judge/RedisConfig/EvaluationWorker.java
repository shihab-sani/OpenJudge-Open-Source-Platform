package open.judge.RedisConfig;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import open.judge.Entity.Submission;
import open.judge.Entity.SubmissionStatus;
import open.judge.Entity.Verdict;
import open.judge.Repositories.SubmissionRepository;
import open.judge.SandboxService.SandBoxService;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class EvaluationWorker {
    private final StringRedisTemplate redisTemplate;
    private final SubmissionRepository submissionRepository;
    private final SandBoxService sandboxService;

    @PostConstruct
    public void start() {
        // Create consumer group if it does not exist
        try {
            redisTemplate.opsForStream().createGroup(
                    RedisStreamConfig.STREAM_KEY, RedisStreamConfig.CONSUMER_GROUP
            );
        } catch (Exception ignored) {
            // group already exists
        }

        Thread worker = new Thread(this::consumeLoop, "evaluation-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        redisTemplate.opsForStream().read(
                                Consumer.from(RedisStreamConfig.CONSUMER_GROUP, "worker-1"),
                                StreamReadOptions.empty().count(1).block(Duration.ofSeconds(5)),
                                StreamOffset.create(RedisStreamConfig.STREAM_KEY, ReadOffset.lastConsumed())
                        );

                if (records == null) continue;

                for (MapRecord<String, Object, Object> record : records) {
                    processRecord(record);
                    redisTemplate.opsForStream().acknowledge(
                            RedisStreamConfig.STREAM_KEY,
                            RedisStreamConfig.CONSUMER_GROUP,
                            record.getId()
                    );
                }
            } catch (Exception e) {
                log.error("Worker loop error", e);
            }
        }
    }

    private void processRecord(MapRecord<String, Object, Object> record) {
        Long submissionId = Long.valueOf(record.getValue().get("submissionId").toString());
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) return;

        submission.setStatus(SubmissionStatus.EVALUATING);
        submissionRepository.save(submission);

        Verdict verdict = sandboxService.evaluate(submission);

        submission.setVerdict(verdict);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setEvaluatedAt(Instant.now());
        submission.setVerdictMessage(verdict == Verdict.ACCEPTED
                ? "All test cases passed"
                : "Evaluation failed: " + verdict);
        submissionRepository.save(submission);
    }
}
