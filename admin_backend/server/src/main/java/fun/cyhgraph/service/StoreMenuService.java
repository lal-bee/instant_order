package fun.cyhgraph.service;

import fun.cyhgraph.dto.StoreMenuConfigDTO;
import fun.cyhgraph.dto.StoreSpecialDishDTO;
import fun.cyhgraph.dto.StoreDishStatusDTO;
import fun.cyhgraph.entity.Category;
import fun.cyhgraph.vo.StoreDishConfigVO;

import java.util.List;

public interface StoreMenuService {
    List<StoreDishConfigVO> getStoreDishConfigList(Long storeId);

    List<Category> getStoreMenuCategoryList();

    void configStoreMenu(StoreMenuConfigDTO storeMenuConfigDTO);

    void addSpecialDish(StoreSpecialDishDTO storeSpecialDishDTO);

    void updateSpecialDish(Integer dishId, StoreSpecialDishDTO storeSpecialDishDTO);

    void deleteSpecialDish(Integer dishId);

    void updateDishStatus(StoreDishStatusDTO storeDishStatusDTO);
}
