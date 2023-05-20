package datart.server.base.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.common.JobID;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
public class JobConfig {

    /**
     * 在 Flink 中, appId 表示 Flink 应用程序在集群中的唯一标识符,
     * 可以用于查询和监控 Flink 应用程序的状态和进度。
     */
    private String appId;

    /**
     * Flink 任务执行计划的唯一标识符, 可以用于查询和监控 Flink 作业的状态和进度.
     * 在 Flink Web UI 或者通过 Flink REST API 查询作业状态时, 可以使用 exId 进行查询.
     * exId 通常是一个字符串，形如 f8c8e8f3b8f0c6f7c3e6f8b8e8d3
     * exId 只在作业提交时生成, 如果作业被取消或者失败后重新提交, 会生成新的 exId.
     * 因此, 如果需要持久化记录作业的状态和进度，建议记录作业名称或者自定义的作业ID.
     */
    private JobID exId;

    private String name;

    private String sql;

    private String[] args;

    private String savepointPath;

    private int parallelism = 1;

    private boolean allowNonRestoredState = false;

}
