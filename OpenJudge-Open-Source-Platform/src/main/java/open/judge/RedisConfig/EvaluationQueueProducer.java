package open.judge.RedisConfig;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvaluationQueueProducer {
    private final StringRedisTemplate redisTemplate;

    public void enqueue(Long submissionId) {
        Map<String, String> message = Map.of(
                "submissionId", submissionId.toString(),
                "enqueuedAt", Instant.now().toString()
        );
        redisTemplate.opsForStream().add(
                StreamRecords.string(message).withStreamKey(RedisStreamConfig.STREAM_KEY)
        );
    }
}
