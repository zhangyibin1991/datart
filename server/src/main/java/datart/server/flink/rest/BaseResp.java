package datart.server.flink.rest;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
public class BaseResp {

    // set while error.
    private String[] errors = {};
}
