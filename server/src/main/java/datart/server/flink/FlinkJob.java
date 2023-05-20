package datart.server.flink;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
@ToString
public class FlinkJob {

    /**
     * job's id.
     */
    private String id;

    private JobState state;
}
