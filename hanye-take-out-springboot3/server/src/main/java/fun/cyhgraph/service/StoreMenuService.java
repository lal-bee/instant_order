package fun.cyhgraph.service;

import fun.cyhgraph.dto.StoreMenuConfigDTO;
import fun.cyhgraph.vo.StoreDishConfigVO;

import java.util.List;

public interface StoreMenuService {
    List<StoreDishConfigVO> getStoreDishConfigList(Long storeId);

    void configStoreMenu(StoreMenuConfigDTO storeMenuConfigDTO);
}
