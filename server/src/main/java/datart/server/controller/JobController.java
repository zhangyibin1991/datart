package datart.server.controller;

import datart.core.base.exception.Exceptions;
import datart.core.entity.Job;
import datart.server.base.dto.JobSavePoint;
import datart.server.base.dto.ResponseData;
import datart.server.base.params.JobAddParam;
import datart.server.base.params.JobListParam;
import datart.server.base.params.JobUpdateParam;
import datart.server.service.JobService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.JobStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Flink jobs.
 * @author zhang.yibin
 */
@Api
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/jobs")
public class JobController extends BaseController {
    private final JobService service;

    /**
     * Page list query jobs.
     *
     * @param param condition.
     * @return resp.
     */
    @GetMapping
    public ResponseData<List<Job>> list(JobListParam param) {
        return ResponseData.success(service.find(param));
    }

    /**
     * Fetch job's detail.
     *
     * @param id job's id.
     * @return resp.
     */
    @GetMapping("/{id}")
    public ResponseData<Job> job(@PathVariable String id) {
        return service.find(id)
                .map(ResponseData::success)
                .orElse(ResponseData.failure("实时任务不存在"));
    }

    /**
     * Add job.
     *
     * @param param job definition.
     * @return resp.
     */
    @PostMapping
    public ResponseData<String> add(@RequestBody JobAddParam param) {

        param.setOperator(getCurrentUser());

        Job job = service.add(param);
        return ResponseData.success(job.getId());
    }

    /**
     * Modify job.
     *
     * @param id    job's id.
     * @param param job's definition.
     */
    @PutMapping("/{id}")
    public ResponseData<String> modify(@PathVariable String id, @RequestBody JobUpdateParam param) {
        param.setJobId(id);
        service.modify(param);
        return ResponseData.success(id);
    }

    /**
     * Delete job.
     * @param id job's id.
     * @return resp.
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseData.success(id);
    }

    /**
     * Make savepoint.
     *
     * @param id job's id.
     * @return reps.
     */
    @PostMapping("/{id:\\d+}/savepoints")
    public ResponseData<JobSavePoint> savepoint(@PathVariable Long id) {
        return ResponseData.success(service.savepoint(id));
    }

    /**
     * Trigger job to run.
     *
     * @param id job's id
     * @return resp.
     */
    @PutMapping("/{id}/state")
    public ResponseData<String> suspendOrResumeJob(@PathVariable String id,
                                                   @RequestBody String body) {
        JobStatus state = JobStatus.valueOf(body);
        if (state == JobStatus.SUSPENDED) {
            service.stop(id);
            return ResponseData.success(id);
        }
        else if (state == JobStatus.RUNNING) {
            return ResponseData.success(service.resume(id));
        }
        else if (state == JobStatus.CANCELED) {
            service.cancel(id);
            return ResponseData.success(id);
        }
        else {
            Exceptions.msg("无效的状态: " + state.name());
            return ResponseData.success(id);
        }
    }

    @GetMapping("/{id}/exceptions")
    public ResponseData<List<String>> exceptions(@PathVariable String id) {
        return ResponseData.success(service.exceptions(id));
    }
}
