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
        try {
            redisTemplate.opsForStream().createGroup(
                    RedisStreamConfig.STREAM_KEY,
                    ReadOffset.from("0"),
                    RedisStreamConfig.CONSUMER_GROUP
            );
            log.info("Redis Consumer Group created successfully.");
        } catch (Exception e) {
            // Check the error message or cause for BUSYGROUP
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                msg += e.getCause().getMessage();
            }

            if (msg.contains("BUSYGROUP")) {
                log.info("Redis Consumer Group already exists. Resuming worker.");
            } else {
                log.error("Failed to create consumer group", e);
            }
        }

        Thread worker = new Thread(this::consumeLoop, "evaluation-worker");
        worker.setDaemon(true);
        worker.start();
        log.info("Evaluation Worker thread started.");
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        redisTemplate.opsForStream().read(
                                Consumer.from(RedisStreamConfig.CONSUMER_GROUP, "worker-1"),
                                StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                                StreamOffset.create(RedisStreamConfig.STREAM_KEY, ReadOffset.lastConsumed())
                        );

                if (records == null || records.isEmpty()) continue;

                for (MapRecord<String, Object, Object> record : records) {
                    log.info("Picked up job from Redis: {}", record.getValue());
                    processRecord(record);
                    redisTemplate.opsForStream().acknowledge(
                            RedisStreamConfig.STREAM_KEY,
                            RedisStreamConfig.CONSUMER_GROUP,
                            record.getId()
                    );
                }
            } catch (Exception e) {
                log.error("Worker loop error: {}", e.getMessage());
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void processRecord(MapRecord<String, Object, Object> record) {
        Long submissionId = Long.valueOf(record.getValue().get("submissionId").toString());

        // ✅ FIX: Use the custom query to fetch the Task eagerly
        Submission submission = submissionRepository.findByIdWithTask(submissionId).orElse(null);

        if (submission == null) {
            log.warn("Submission {} not found in DB", submissionId);
            return;
        }

        log.info("Evaluating submission: {}", submissionId);
        submission.setStatus(SubmissionStatus.EVALUATING);
        submissionRepository.save(submission);

        Verdict verdict = sandboxService.evaluate(submission);
        log.info("Evaluation complete for submission: {}. Verdict: {}", submissionId, verdict);

        submission.setVerdict(verdict);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setEvaluatedAt(Instant.now());
        submission.setVerdictMessage(verdict == Verdict.ACCEPTED
                ? "All test cases passed"
                : "Evaluation failed: " + verdict);
        submissionRepository.save(submission);
    }
}
