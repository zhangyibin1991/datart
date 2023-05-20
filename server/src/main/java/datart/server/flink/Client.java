package datart.server.flink;

import com.alibaba.fastjson.JSON;
import datart.core.base.exception.Exceptions;
import datart.server.base.dto.JobConfig;
import datart.server.flink.rest.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.client.deployment.*;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.execution.SavepointFormatType;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.*;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zhang.yibin
 */
@Slf4j
@Service
public class Client implements InitializingBean {

    private final Environment environment;

    private final Config setting;

    private final RestTemplate restTemplate;

    /**
     * Flink cluster config.
     */
    private Configuration configuration;
    private ClusterClient<StandaloneClusterId> clusterClient;

    /**
     * Flink config property name's prefix.
     */
    private final String FLINK_CONFIG_PREFIX = "flink.";
    /**
     * Flink config properties.
     */
    private final Pattern FLINK_CONFIG_PATTERN = Pattern.compile("^flink\\..*", Pattern.DOTALL);

    public Client(Environment environment,
                  Config setting,
                  RestTemplateBuilder restTemplateBuilder) {
        this.environment = environment;
        this.setting = setting;
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Initialized properties.
     */
    @Override
    public void afterPropertiesSet() {
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Flink config.
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
        String source = "datartConfig";
        PropertySource<?> ps = propertySources.get(source);

        if (ps == null) {
            throw new IllegalArgumentException("Can't to find system configuration property sources " +
                    "in Spring environment with name: " + source);
        }

        Set<String> propertyNames = Arrays
                .stream(((PropertiesPropertySource) ps).getPropertyNames())
                .filter(name -> FLINK_CONFIG_PATTERN.matcher(name).matches())
                .collect(Collectors.toSet());

        configuration = new Configuration();
        for (String name : propertyNames) {
            Object value = ps.getProperty(name);
            if (value == null)
                continue;
            configuration.setString(
                    name.substring(FLINK_CONFIG_PREFIX.length())
                    , value.toString()
            );
        }


        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Cluster client.
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ClusterClientFactory<StandaloneClusterId> standaloneClientFactory = new StandaloneClientFactory();
        final StandaloneClusterId clusterId = standaloneClientFactory.getClusterId(configuration);
        if (clusterId == null) {
            throw new IllegalArgumentException("No cluster id was specified. " +
                    "Please specify a cluster to which you would like to connect.");
        }

        // A standalone
        try (ClusterDescriptor<StandaloneClusterId> clusterDescriptor
                     = standaloneClientFactory.createClusterDescriptor(configuration)) {
            ClusterClientProvider<StandaloneClusterId> clientProvider
                    = clusterDescriptor.retrieve(clusterId);
            this.clusterClient = clientProvider.getClusterClient();
        } catch (ClusterRetrieveException ex) {
            log.error("No standalone session, Couldn't retrieve cluster Client.", ex);
            throw new IllegalStateException("No standalone session, Couldn't retrieve cluster Client.", ex);
        }
    }

    /**
     * Fetch jobs on cluster.
     * @return jobs.
     */
    public Collection<JobStatusMessage> jobs() {
        try {
            return clusterClient.listJobs()
                    .get(
                            setting.getDefaultTimeout()
                            , TimeUnit.SECONDS
                    );
        } catch (Exception ex) {
            throw new IllegalStateException("作业获取失败.", ex);
        }
    }

    /**
     * 提交作业.
     * Note: SQL中不能使用`add jar`来添加jar包.
     * @param job job configuration.
     * @throws TimeoutException timeout.
     */
    public void submit(JobConfig job) throws TimeoutException {

        String jarId = uploadCoreJar();

        JobRunRequest body = new JobRunRequest();
        body.setJobId      (job.getExId().toHexString());
        body.setParallelism(job.getParallelism());
        body.setEntryClass (setting.getFlink().getJobMainClass());
        body.setSavepointPath(job.getSavepointPath());

        byte[] sql = job.getSql().getBytes(StandardCharsets.UTF_8);

        String args =
                  " -name " + job.getName()
                + " -sql "
                + Base64.getEncoder().encodeToString(sql);

        body.setProgramArgs(args);

        // request.
        RequestEntity<JobRunRequest> req =
                RequestEntity.post(
                        clusterClient.getWebInterfaceURL() + "/jars/{jarId}/run"
                        , jarId)
                .body(body);


        ResponseEntity<JobRunResp> resp = restTemplate.exchange(req, JobRunResp.class);
        HttpStatus statue = resp.getStatusCode();
        if (statue.is2xxSuccessful()) {
            log.info("[Job] Job <{}> has submit to run. Response: {}"
                    , job.getExId()
                    , JSON.toJSONString(resp));
            return;
        }

        String[] errors = resp.getBody() == null ? new String[]{} : resp.getBody().getErrors();
        Exceptions.msg(errors.length > 0 ? errors[0] : "作业提交失败");
    }

    public void stop(JobID job) {
        String savepoint;
        try {
            savepoint = clusterClient.stopWithSavepoint(job, true, setting.getFlink().getJobSavepointPath(), SavepointFormatType.DEFAULT)
                    .get(setting.getDefaultTimeout(), TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.info("Failed to stop job <{}>. Caused by:", job, ex);
            Exceptions.msg("job.stop.failed");
            return;
        }
        log.info("The job <{}> stopped, savepoint path: {}", job, savepoint);
    }

    public void cancel(JobID job) {

        JobStatus state = getJobStatus(job);

        if (state == JobStatus.CANCELED
                || state == JobStatus.FAILED) {
            return;
        }

        try {
            clusterClient.cancel(job)
                    .get(setting.getDefaultTimeout(), TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.info("Failed tp cancel job <{}>. Caused by: ", job, ex);
            Exceptions.msg("job.cancel.failed");
        }
    }

    /**
     * Fetch job execution status.
     *
     * @param job job id.
     * @return job's status.
     */
    public JobStatus getJobStatus(JobID job) {
        CompletableFuture<JobStatus> jobStatus = clusterClient.getJobStatus(job);
        try {
            return jobStatus.get(setting.getDefaultTimeout(), TimeUnit.SECONDS);
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message.contains("NotFoundException")) {
                return null;
            }
            throw new IllegalStateException("Failed to fetch state of job <" + job + ">.", ex);
        }
    }

//    private final static Pattern CHUNJUN_CORE_JAR_PATTERN
//            = Pattern.compile("^(?:flinkx|chunjun)-core.*\\.jar$", Pattern.DOTALL);

//    /**
//     * @return FlinkX(ChunJun) core jar.
//     */
//    private File getJarFile() {
//        File home = new File(setting.getChunjun().getHome());
//        if (!home.exists() || !home.isDirectory()) {
//            log.info("Failed to load FlinkX/Chunjun core jar from dir: {}.", setting.getChunjun().getHome());
//            throw new IllegalArgumentException("FlinkX/Chunjun 目录未配置。");
//        }
//
//        File[] jars = home.listFiles((dir, name) -> CHUNJUN_CORE_JAR_PATTERN.matcher(name).matches());
//
//        if (jars == null || jars.length < 1) {
//            throw new IllegalArgumentException("FlinkX/Chunjun 核心 jar 包不存在。");
//        }
//        return jars[0];
//    }

    /**
     * @return uploaded jar's jobId.
     */
    private String uploadCoreJar() {
        String jarPath = setting.getFlink().getJobCoreJar();
        String jarName = jarPath.substring(jarPath.lastIndexOf("/") + 1);

        List<JarListResp.JarDesc> jars = jars();

        Optional<String> jarId = jars.stream()
                .filter(jar -> Objects.equals(jar.getName(), jarName))
                .map(JarListResp.JarDesc::getId)
                .findFirst();

        if (jarId.isPresent()) {
            return jarId.get();
        }

        synchronized (Client.class) {
            FileSystemResourceLoader loader  = new FileSystemResourceLoader();
            Resource resource = loader.getResource(jarPath);

            if (!resource.exists()) {
                log.warn("The core jar file can't be found with path: {}", jarPath);
                throw new IllegalArgumentException("Jar包不存在");
            }

            HttpHeaders header = new HttpHeaders();
            // header.add(HttpHeaders.CONTENT_TYPE, "application/x-java-archive");

            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("jarFile", resource);

            ResponseEntity<JarUploadResp> resp = restTemplate.exchange(
                    clusterClient.getWebInterfaceURL() + "/jars/upload"
                    , HttpMethod.POST
                    , new HttpEntity<>(form, header)
                    , JarUploadResp.class
            );

            assert resp.getBody() != null;

            if (resp.getStatusCode().is2xxSuccessful()) {
                String filename = resp.getBody().getFilename();
                // Make id.
                return filename.substring(filename.lastIndexOf("/") + 1);
            }

            String[] errors = resp.getBody().getErrors();

            log.warn("[Job] Failed to update file, due to, {}", (Object[]) errors);

            throw new IllegalStateException("Jar包上传失败.");
        }
    }

    private List<JarListResp.JarDesc> jars() {
        ResponseEntity<JarListResp> resp = restTemplate.exchange(
                RequestEntity
                        .get(clusterClient.getWebInterfaceURL() + "/jars")
                        .build()
                , JarListResp.class
        );

        if (resp.getStatusCode().is2xxSuccessful()) {
            return resp.getBody() == null ? Collections.emptyList() : resp.getBody().getFiles();
        }

        throw new IllegalStateException("Jar包获取失败");
    }

    public List<String> exceptions(JobID jobId) {

        ResponseEntity<JobExResp> resp = restTemplate.exchange(
                clusterClient.getWebInterfaceURL() + "/jobs/" + jobId.toHexString() + "/exceptions"
                , HttpMethod.GET
                , HttpEntity.EMPTY
                , JobExResp.class
        );
        if (resp.getStatusCode().is2xxSuccessful()) {
            JobExResp ex = resp.getBody();
            if (ex == null) {
                return Collections.emptyList();
            }

            String root = ex.getRootExceptions();
            if (root == null) {
                return Arrays.asList("No Root Exception.");
            }

            return Arrays.asList(ex.getRootExceptions().split("\n"));
        }

        String[] errors = resp.getBody() == null ? resp.getBody().getErrors() : new String[]{};

        Exceptions.msg(errors.length == 0 ? "日志获取失败" : errors[0]);
        return Collections.emptyList();
    }

//    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//    // Job graph.
//    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//    /**
//     * Job graph creation.
//     *
//     * @param job job definition.
//     * @return job graph.
//     */
//    private JobGraph createJobGraph(JobConfig job) {
//
//        Assert.notNull(job.getExId(), "[Assert] The job's id can't be NULL.");

//            File jarFile           = getJarFile();
//            String entryPointClass = setting.getChunjun().getMainClass();
//            List<URL> classPaths   = Collections.emptyList();
//            String[] args          = assemblyArgs(job);
//
////            SavepointRestoreSettings srs = SavepointRestoreSettings.forPath(
////                      job.getSavepointPath()
////                    , job.isAllowNonRestoredState()
////            );
//            SavepointRestoreSettings srs = SavepointRestoreSettings.none();
//
//            PackagedProgram program;
//            try {
//                program = PackagedProgram.newBuilder()
//                        .setJarFile(jarFile)
//                        .setUserClassPaths(classPaths)
//                        .setEntryPointClassName(entryPointClass)
//                        .setConfiguration(configuration)
//                        .setArguments(args)
//                        .setSavepointRestoreSettings(srs)
//                        .build();
//            } catch (ProgramInvocationException ex) {
//                // This invocation is thrown if the Program can't be properly loaded.
//                // Causes may be a missing / wrong class or manifest files.
//                throw new IllegalArgumentException("Failed to package program.", ex);
//            }
//            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//            // JobGraph.
//            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//            try {
//                return PackagedProgramUtils.createJobGraph(
//                        program
//                        , configuration
//                        , setting.getJob().getParallelism()
//                        , job.getExId()
//                        , false
//                );
//            } catch (ProgramInvocationException ex) {
//                throw new IllegalStateException("Failed to generate Job-Graph.", ex);
//            }
//
//        StreamExecutionEnvironment env
//                = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
//        env.setParallelism(job.getParallelism());
//
//        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
//
//        tableEnv.executeSql(job.getSql());
//
//        return env.getStreamGraph().getJobGraph();
//    }

//    /**
//     * 组装FlinkX/Chunjun参数
//     *
//     * @see com.dtstack.chunjun.options.Options in Flinkx/Chunjun
//     */
//    private String[] assemblyArgs(JobConfig job) {
//        List<String> args = new ArrayList<>();
//        args.add("-jobName");
//        args.add(job.getName());
//        args.add("-mode");
//        args.add("standalone");
//        args.add("-job");
//        args.add(job.getSql());
//        args.add("-jobType");
//        args.add("sql");
//        args.add("-chunjunDistDir");
//        args.add(setting.getChunjun().getHome());
//        args.add("-remoteChunJunDistDir");
//        args.add(setting.getChunjun().getHome());
//        return args.toArray(new String[0]);
//    }
}
