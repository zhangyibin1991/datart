package datart.server.flink;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Flink env config.
 *
 * @author zhang.yibin
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "datart")
public class Config {

    private Flink flink = new Flink();

    private Job job = new Job();

    /**
     * timeout, seconds.
     */
    private long defaultTimeout = 60;

    @Getter
    @Setter
    public static class Flink {

        private String jobCoreJar = "/lib/flink-streaming-core.jar";

        private String jobMainClass = "com.flink.streaming.core.JobApplication";

        private String jobSavepointPath = "file:/opt/flink/savepoint";

    }

    @Getter
    @Setter
    public static class Job {
        private long submitTimeout = 300;

        private int parallelism = 1;
    }
}
