package datart.server.base.params;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.common.JobStatus;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
public class JobStateUpdateParam {

    private JobStatus state;
}
