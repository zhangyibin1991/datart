package datart.core.mappers;

import datart.core.entity.Job;
import org.apache.ibatis.jdbc.SQL;

/**
 * @author zhang.yibin
 */
public class JobSqlProvider {

    public String updateSelective(Job job) {
        SQL sql = new SQL();
        sql.UPDATE(JobMapper.table)
                .SET(
                        "`update_by` = #{updateBy,jdbcType=VARCHAR}",
                        "`update_time` = #{updateTime,jdbcType=TIMESTAMP}"
                );
        if (job.getName() != null) {
            sql.SET("`name` = #{name,jdbcType=VARCHAR}");
        }
        if (job.getSql() != null) {
            sql.SET("`sql` = #{sql,jdbcType=VARCHAR}");
        }
        if (job.getParallelism() != null) {
            sql.SET("`parallelism` = #{parallelism,jdbcType=TINYINT}");
        }
        sql.WHERE("`id` = #{id}");
        return sql.toString();
    }
}
