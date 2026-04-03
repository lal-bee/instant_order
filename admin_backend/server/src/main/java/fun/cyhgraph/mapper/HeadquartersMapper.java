package fun.cyhgraph.mapper;

import fun.cyhgraph.annotation.AutoFill;
import fun.cyhgraph.entity.Headquarters;
import fun.cyhgraph.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HeadquartersMapper {

    @AutoFill(OperationType.INSERT)
    @Insert("insert into headquarters (name, status, create_user, update_user, create_time, update_time) values " +
            "(#{name}, #{status}, #{createUser}, #{updateUser}, #{createTime}, #{updateTime})")
    void add(Headquarters headquarters);

    @Select("select * from headquarters where id = #{id}")
    Headquarters getById(Long id);

    @Select("select * from headquarters order by id asc")
    List<Headquarters> getList();

    @Select("select * from headquarters where status = 1 order by id asc")
    List<Headquarters> getEnabledList();

    @AutoFill(OperationType.UPDATE)
    void update(Headquarters headquarters);

    @Delete("delete from headquarters where id = #{id}")
    void delete(Long id);

    @Update("update headquarters set status = IF(status = 0, 1, 0) where id = #{id}")
    void onOff(Long id);
}
