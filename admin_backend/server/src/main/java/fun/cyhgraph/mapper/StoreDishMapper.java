package fun.cyhgraph.mapper;

import fun.cyhgraph.annotation.AutoFill;
import fun.cyhgraph.entity.StoreDish;
import fun.cyhgraph.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;
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

    @Select("select status from store_dish where store_id = #{storeId} and dish_id = #{dishId} limit 1")
    Integer getStatusByStoreIdAndDishId(@Param("storeId") Long storeId, @Param("dishId") Integer dishId);

    @Select("select dish_id as dishId, status as status from store_dish where store_id = #{storeId}")
    List<Map<String, Object>> getDishStatusMapByStoreId(Long storeId);

    @Update("update store_dish set status = #{status} where store_id = #{storeId} and dish_id = #{dishId}")
    int updateStatus(@Param("storeId") Long storeId, @Param("dishId") Integer dishId, @Param("status") Integer status);

    int upsertStatus(@Param("storeId") Long storeId, @Param("dishId") Integer dishId, @Param("status") Integer status);

    List<Integer> getDishIdsByStoreIdAndCategoryId(@Param("storeId") Long storeId, @Param("categoryId") Integer categoryId);
}
