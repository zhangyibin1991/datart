package datart.server.flink.rest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
@ToString
public class JarListResp extends BaseResp {

    private String address;
    private List<JarDesc> files;

    @Getter
    @Setter
    @ToString
    public static class JarDesc {

        private String id;

        private String name;

        private String description;

        private Long uploaded;
    }
}
