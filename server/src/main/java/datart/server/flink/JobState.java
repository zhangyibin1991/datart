package datart.server.flink;

public enum JobState {
    /**
     * 作业已经创建，但还没有被提交执行。
     */
    CREATED,

    /**
     * 作业正在运行。
     */
    RUNNING,

    /**
     * 作业已经成功完成。
     */
    FINISHED,

    /**
     * 作业被取消。
     */
    CANCELED,

    /**
     * 作业执行失败。
     */
    FAILED,
    ;
}
