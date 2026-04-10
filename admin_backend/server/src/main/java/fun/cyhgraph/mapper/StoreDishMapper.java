package fun.cyhgraph.mapper;

import fun.cyhgraph.annotation.AutoFill;
import fun.cyhgraph.entity.StoreDish;
import fun.cyhgraph.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import fun.cyhgraph.vo.StockPageVO;
import fun.cyhgraph.dto.StockPageQueryDTO;

@Mapper
public interface StoreDishMapper {

    @AutoFill(OperationType.INSERT)
    void add(StoreDish storeDish);

    @Delete("delete from store_dish where dish_id = #{dishId}")
    void deleteByDishId(Integer dishId);

    int batchInitStockForStore(@Param("storeId") Long storeId,
                               @Param("defaultStock") Integer defaultStock,
                               @Param("defaultWarningStock") Integer defaultWarningStock);

    int batchInitStockForStoreAndDishIds(@Param("storeId") Long storeId,
                                         @Param("dishIds") List<Integer> dishIds,
                                         @Param("defaultStock") Integer defaultStock,
                                         @Param("defaultWarningStock") Integer defaultWarningStock);

    int deductStock(@Param("storeId") Long storeId, @Param("dishId") Integer dishId, @Param("deductNum") Integer deductNum);

    int rollbackStock(@Param("storeId") Long storeId, @Param("dishId") Integer dishId, @Param("rollbackNum") Integer rollbackNum);

    List<StockPageVO> pageQuery(StockPageQueryDTO queryDTO);

    List<StockPageVO> warningList(StockPageQueryDTO queryDTO);

    int updateStock(@Param("storeId") Long storeId, @Param("dishId") Integer dishId, @Param("stock") Integer stock);

    int updateWarningStock(@Param("storeId") Long storeId, @Param("dishId") Integer dishId, @Param("warningStock") Integer warningStock);
}
