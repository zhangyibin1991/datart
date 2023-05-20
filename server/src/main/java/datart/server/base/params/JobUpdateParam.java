package datart.server.base.params;

import datart.core.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Range;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
@ToString
public class JobUpdateParam {
    private String jobId;

    private String jobName;

    private String sql;

    @Range(min = 1, max = 99, message = "无效的并行度：{value}。有效范围：[{min} ~ {max}]")
    private int parallelism = 1;

    /**
     * operator.
     */
    private User operator;
}
