package datart.server.base.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.flink.api.common.JobStatus;

/**
 * Flink job.
 * @author zhang.yibin
 */
@Getter
@Setter
@ToString
public class Job {

    /**
     * The job's id in Flink.
     */
    private String id;

    private String name;

    private JobStatus state;

    private String sql;

    private Integer parallelism;

    private String savepointSetting;

    private long startTime;
}

