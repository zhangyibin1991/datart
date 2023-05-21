package datart.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
@ToString
public class Job extends BaseEntity {

    private String orgId;

    private String name;

    private String state;

    private String sql;

    private Integer parallelism;

    private long startTime;
}
