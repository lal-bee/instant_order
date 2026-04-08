package fun.cyhgraph.mapper;

import fun.cyhgraph.entity.TableInfo;
import fun.cyhgraph.vo.UserTableVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TableInfoMapper {

    @Select("select * from table_info where id = #{id}")
    TableInfo getById(Long id);

    UserTableVO getUserTableById(@Param("tableId") Long tableId);
}
