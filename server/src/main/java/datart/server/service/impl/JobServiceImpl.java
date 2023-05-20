package datart.server.service.impl;

import com.alibaba.fastjson.JSON;
import datart.core.base.exception.Exceptions;
import datart.core.entity.Job;
import datart.core.mappers.JobMapper;
import datart.server.base.dto.JobConfig;
import datart.server.base.dto.JobSavePoint;
import datart.server.base.params.JobAddParam;
import datart.server.base.params.JobListParam;
import datart.server.base.params.JobUpdateParam;
import datart.server.flink.Client;
import datart.server.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhang.yibin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final Client client;

    private final JobMapper mapper;

    @Override
    public Optional<Job> find(String id) {
        Job job = mapper.selectById(id);
        if (job == null) {
            return Optional.empty();
        }
        JobStatus state = client.getJobStatus(JobID.fromHexString(id));
        if (state != null) {
            job.setState(state.name());
        }

        return Optional.of(job);
    }

    @Override
    public List<Job> find(JobListParam param) {

        List<String> jobIds = mapper.selectAllIds();

        Collection<JobStatusMessage> jobStatusMessages = client.jobs();

        Map<String, Job> jobsInCluster = jobStatusMessages.stream()
                .map(element -> {
                    Job job = new Job();
                    job.setId       (element.getJobId().toHexString());
                    job.setState    (element.getJobState().name());
                    job.setName     (element.getJobName());
                    job.setStartTime(element.getStartTime());
                    return job;
                }).filter(job -> jobIds.contains(job.getId()))
                .collect(Collectors.toMap(
                        Job::getId
                        , Function.identity()
                ));

        List<Job> jobs = mapper.selectAllIds()
                .stream()
                .map(mapper::selectById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (Job job : jobs) {
            String jobId = job.getId();
            Job jobInCluster = jobsInCluster.get(jobId);

            if (jobInCluster != null) {
                job.setState(jobInCluster.getState());
                job.setStartTime(jobInCluster.getStartTime());
                continue;
            }

            job.setState("UNKNOWN");
        }

        return jobs;
    }

    @Override
    @Transactional
    public Job add(JobAddParam param) {

        JobID jobId = JobID.generate();

        Job job = new Job();
        job.setId(jobId.toHexString());
        job.setName(param.getJobName());
        job.setParallelism(param.getParallelism());
        job.setSql(param.getSql());

        Assert.notNull(param.getOperator(),
                "[Assert] The current operator can't be NULL.");
        String operator = param.getOperator().getName();
        job.setCreateBy(operator);
        job.setUpdateBy(operator);

        Date ts = new Date();
        job.setCreateTime(ts);
        job.setUpdateTime(ts);

        try {
            mapper.insert(job);
        } catch (Exception ex) {
            log.info("Failed to add job. \n" +
                    "Caused by: Failed storage job: {}.", JSON.toJSON(job), ex);
            Exceptions.msg("job.add.failed");
        }

        JobConfig config = new JobConfig();
        config.setExId(jobId);
        config.setName(job.getName());
        config.setSql (job.getSql());
        try {
            client.submit(config);
        } catch (Exception ex) {
            log.info("Failed to add job.\n" +
                    "Caused by: job <{}> submission timed out or error: {}.", jobId, config, ex);
            Exceptions.msg(ex.getMessage());
        }

        return job;
    }

    @Override
    public void modify(JobUpdateParam param) {

        Assert.notNull(param.getJobId(), "[Assert] The job's id can't be NULL.");

        Job job = new Job();
        job.setId(param.getJobId());
        job.setSql(param.getSql());
        job.setParallelism(param.getParallelism());
        job.setUpdateBy(param.getOperator().getName());
        job.setUpdateTime(new Date());
        mapper.update(job);

        job = mapper.selectById(param.getJobId());

        if (job == null) {
            log.info("Failed to modify job <{}>, due to, not existed.", param.getJobId());
            Exceptions.msg("job.modify.failed");
            return;
        }

        JobID jobID = JobID.fromHexString(job.getId());

        client.cancel(jobID);

        // submit again.
        JobConfig config = new JobConfig();
        config.setExId(jobID);
        config.setName(job.getName());
        config.setSql(job.getSql());

        try {
            client.submit(config);
        } catch (Exception ex) {
            log.info("Failed to modify job: {}", JSON.toJSONString(config), ex);
            Exceptions.msg("job.modify.failed");
        }
    }

    @Override
    public void delete(String id) {

        JobID job = JobID.fromHexString(id);

        JobStatus state = client.getJobStatus(job);

        if (state == null) {
            // 集群中不存在.
            // 直接从数据库中删除.
            mapper.deleteById(id);
        }

        //  先取消任务, 然后再从数据库中删除.
        if (state == JobStatus.RUNNING) {
            client.cancel(job);

            // waiting done.
            while (true) {
                state = client.getJobStatus(job);
                if (state == JobStatus.CANCELED) {
                    break;
                }
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            }
        }

        mapper.deleteById(id);
    }

    @Override
    public JobSavePoint savepoint(Long id) {
        return null;
    }

    @Override
    public String resume(String id) {

        JobID jobId = JobID.fromHexString(id);

        Job job = mapper.selectById(id);
        if (job == null) {
            Exceptions.msg("作业不存在.");
            return id;
        }

        JobStatus state = client.getJobStatus(jobId);

        if (state == JobStatus.RUNNING) {
            return id;
        }

        if (state != null
                && state != JobStatus.FAILED
                && state != JobStatus.CANCELED) {

            try {
                client.stop(jobId);
            } catch (Exception ex) {
                log.info("Failed to resume job <{}>, due to: ", jobId, ex);
                Exceptions.msg("job.resume.failed");
                return id;
            }
        }

        // A new job id.
        JobID newJobID = JobID.generate();

        JobConfig config = new JobConfig();
        config.setExId(newJobID);
        config.setName(job.getName());
        config.setSql(job.getSql());

        try {
            client.submit(config);
        } catch (Exception ex) {
            log.info("Failed to resume job <{}>.\n" +
                    "Caused by: job <{}> submission timed out or error: {}.",
                    jobId,  jobId, config, ex);
            Exceptions.msg("job.resume.failed");
            return id;
        }

        mapper.updateId(job.getId(), newJobID.toHexString());
        return newJobID.toHexString();
    }

    @Override
    public void stop(String id) {
        JobID jobId = JobID.fromHexString(id);
        stop(jobId);
    }

    @Override
    public void cancel(String id) {
        JobID jobId = JobID.fromHexString(id);
        client.cancel(jobId);
    }

    private void stop(JobID jobID) {
        JobStatus state = client.getJobStatus(jobID);

        if (state == null
                || state == JobStatus.SUSPENDED
                || state == JobStatus.FAILED
                || state == JobStatus.CANCELED) {
            return;
        }

        client.stop(jobID);
    }

    @Override
    public List<String> exceptions(String id) {
        return client.exceptions(JobID.fromHexString(id));
    }
}
