package fun.cyhgraph.mapper;

import fun.cyhgraph.annotation.AutoFill;
import fun.cyhgraph.entity.Store;
import fun.cyhgraph.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface StoreMapper {

    @AutoFill(OperationType.INSERT)
    @Insert("insert into store (name, manager_employee_id, status, create_user, update_user, create_time, update_time) values " +
            "(#{name}, #{managerEmployeeId}, #{status}, #{createUser}, #{updateUser}, #{createTime}, #{updateTime})")
    void add(Store store);

    @Select("select * from store where id = #{id}")
    Store getById(Long id);

    @Select("select * from store order by id asc")
    List<Store> getList();

    List<Store> getListWithDetail();

    @AutoFill(OperationType.UPDATE)
    void update(Store store);

    @Delete("delete from store where id = #{id}")
    void delete(Long id);

    @Update("update store set manager_employee_id = #{managerEmployeeId} where id = #{storeId}")
    void updateManager(@Param("storeId") Long storeId, @Param("managerEmployeeId") Integer managerEmployeeId);

    @Update("update store set status = IF(status = 0, 1, 0) where id = #{id}")
    void onOff(Long id);
}
