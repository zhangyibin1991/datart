package datart.server.flink.rest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
@ToString
public class JarUploadResp extends BaseResp{

    private String filename;
}
