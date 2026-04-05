package fun.cyhgraph.mapper;

import fun.cyhgraph.annotation.AutoFill;
import fun.cyhgraph.entity.StoreDish;
import fun.cyhgraph.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreDishMapper {

    @AutoFill(OperationType.INSERT)
    void add(StoreDish storeDish);

    @Delete("delete from store_dish where store_id = #{storeId}")
    void deleteByStoreId(Long storeId);

    @Delete("delete from store_dish where store_id = #{storeId} and dish_id = #{dishId}")
    void deleteByStoreIdAndDishId(@Param("storeId") Long storeId, @Param("dishId") Integer dishId);

    @Delete("delete from store_dish where dish_id = #{dishId}")
    void deleteByDishId(Integer dishId);

    @Select("select dish_id from store_dish where store_id = #{storeId} and status = 1")
    List<Integer> getDishIdsByStoreId(Long storeId);

    List<Integer> getDishIdsByStoreIdAndCategoryId(@Param("storeId") Long storeId, @Param("categoryId") Integer categoryId);
}
