package datart.core.mappers;

import datart.core.entity.Job;
import datart.core.mappers.ext.CRUDMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;
import java.util.Set;

/**
 * @author zhang.yibin 2023/05/11
 */
public interface JobMapper extends CRUDMapper {

    String table = "`job`";

    @Insert({
            "INSERT INTO " + table +" (",
            "    `id`, `name`, ",
            "    `org_id`, ",
            "    `sql`, `parallelism`, ",
            "    `create_time`, `create_by`, ",
            "    `update_time`, `update_by`",
            ") VALUES (",
            "    #{id,jdbcType=VARCHAR}, #{name,jdbcType=VARCHAR}, ",
            "    #{orgId,jdbcType=VARCHAR}, ",
            "    #{sql,jdbcType=VARCHAR}, ",
            "    #{parallelism,jdbcType=TINYINT}, ",
            "    #{createTime,jdbcType=TIMESTAMP}, #{createBy,jdbcType=VARCHAR}, ",
            "    #{updateTime,jdbcType=TIMESTAMP}, #{updateBy,jdbcType=VARCHAR}",
            ")"
    })
    int insert(Job job);

    @Select({
            "SELECT",
            "    `id`",
            "FROM " + table
    })
    @Results({
            @Result(column="id", property="id", jdbcType= JdbcType.VARCHAR, id = true),
    })
    List<String> selectAllIds();

    @Select({
            "SELECT",
            "    `id`, `name`, `org_id`,",
            "    `sql`, `parallelism`,",
            "    `create_by`, `create_time`, `update_by`, `update_time` ",
            "FROM " + table,
            "WHERE id = #{id, jdbcType=VARCHAR}"
    })
    @Results({
            @Result(column="id", property="id", jdbcType= JdbcType.VARCHAR, id = true),
            @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
            @Result(column="org_id", property="orgId", jdbcType=JdbcType.VARCHAR),
            @Result(column="sql", property="sql", jdbcType=JdbcType.VARCHAR),
            @Result(column="parallelism", property="parallelism", jdbcType=JdbcType.TINYINT),
            @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
            @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
            @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP)
    })
    Job selectById(String id);

    @Select({
            "SELECT",
            "    `id`, `name`, `org_id`,",
            "    `sql`, `parallelism`,",
            "    `create_by`, `create_time`, `update_by`, `update_time` ",
            "FROM " + table,
            "WHERE id IN #{ids, jdbcType=VARCHAR}"
    })
    @Results({
            @Result(column="id", property="id", jdbcType= JdbcType.VARCHAR, id = true),
            @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
            @Result(column="org_id", property="orgId", jdbcType=JdbcType.VARCHAR),
            @Result(column="sql", property="sql", jdbcType=JdbcType.VARCHAR),
            @Result(column="parallelism", property="parallelism", jdbcType=JdbcType.TINYINT),
            @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
            @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
            @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP)
    })
    List<Job> selectByIds(Set<String> ids);

    @UpdateProvider(type = JobSqlProvider.class, method = "updateSelective")
    int update(Job job);

    @Update({
            "UPDATE " + table + " ",
            "SET `id` = #{newId} ",
            "WHERE `id` = #{oldId}"
    })
    int updateId(@Param("oldId") String from, @Param("newId") String to);

    @Update({
            "DELETE FROM " + table + " ",
            "WHERE `id` = #{value}"
    })
    int deleteById(String id);
}
