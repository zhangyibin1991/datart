package datart.server.base.params;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zhang.yibin
 */
@Getter
@Setter
@ToString
public class JobListParam {

    private Long offset = 0L;

    private Integer limit = 20;
}
