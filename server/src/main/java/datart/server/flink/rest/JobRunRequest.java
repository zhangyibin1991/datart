package datart.server.flink.rest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
@ToString
public class JobRunRequest {

    private String jobId;

    private int parallelism = 1;
    private Boolean allowNonRestoredState;

    private String entryClass;

    private String savepointPath;

    private String programArgs;
}
