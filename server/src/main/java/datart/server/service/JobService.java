package datart.server.service;

import datart.core.entity.Job;
import datart.server.base.dto.JobSavePoint;
import datart.server.base.params.JobAddParam;
import datart.server.base.params.JobListParam;
import datart.server.base.params.JobUpdateParam;

import java.util.List;
import java.util.Optional;

/**
 * @author zhang.yibin
 */
public interface JobService {

    /**
     * Fetch job by id.
     *
     * @param id job's id.
     * @return job.
     */
    Optional<Job> find(String id);

    /**
     * List query jobs.
     *
     * @param param query param.
     * @return jobs.
     */
    List<Job> find(JobListParam param);

    /**
     * Add job.
     *
     * @param param job.
     * @return job.
     */
    Job add(JobAddParam param);

    /**
     * Modify job.
     *
     * @param param job.
     */
    void modify(JobUpdateParam param);

    /**
     * Delete job.
     *
     * @param id job.
     */
    void delete(String id);

    /**
     * Create savepoint.
     *
     * @param id job's id
     * @return savepoint.
     */
    JobSavePoint savepoint(Long id);

    /**
     * Start job execution.
     *
     * @param id        job's id
     */
    String resume(String id);

    /**
     * Stop job execution.
     *
     * @param id job's id
     */
    void stop(String id);

    /**
     * Cancel job exection.
     *
     * @param id job's id.
     */
    void cancel(String id);

    List<String> exceptions(String id);
}
