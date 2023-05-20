package datart.server.flink.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
public class JobExResp extends BaseResp{

    @JsonProperty("root-exception")
    private String rootExceptions;

    private Long timestamp;

    private Boolean truncated;
}
