package open.judge.DockerConfig;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {
    @Bean
    public DockerClient dockerClient() {
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(DefaultDockerClientConfig.createDefaultConfigBuilder().build().getDockerHost())
                .build();

        return DockerClientBuilder.getInstance()
                .withDockerHttpClient(httpClient)
                .build();
    }
}
