package fun.cyhgraph.mapper;

import com.github.pagehelper.Page;
import fun.cyhgraph.annotation.AutoFill;
import fun.cyhgraph.dto.DishDTO;
import fun.cyhgraph.dto.DishPageDTO;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper {
    @AutoFill(OperationType.INSERT)
    void addDish(Dish dish);

    Page<Dish> getPageList(DishPageDTO dishPageDTO);

    @Select("select * from dish where id = #{id}")
    Dish getById(Integer id);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    void deleteBatch(List<Integer> ids);

    @Update("update dish set status = IF(status = 0, 1, 0) where id = #{id}")
    void onOff(Integer id);

    List<Dish> getList(Dish dish);

    List<Dish> getListByStoreAndCategory(@Param("storeId") Long storeId, @Param("categoryId") Integer categoryId);

    List<Dish> getByHeadquartersId(Long headquartersId);

    List<Dish> getStandardByHeadquartersId(Long headquartersId);

    List<Dish> getSpecialByStoreId(Long storeId);

    Integer countByName(@Param("name") String name, @Param("excludeId") Integer excludeId);

    @Delete("delete from dish where id = #{id}")
    void deleteById(Integer id);

    @Select("select count(id) from dish where status = #{i}")
    Integer getByStatus(int i);
}
