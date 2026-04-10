package fun.cyhgraph.service;

import fun.cyhgraph.dto.StockPageQueryDTO;
import fun.cyhgraph.dto.StockUpdateDTO;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.vo.StockPageVO;

import java.util.List;
import java.util.Map;

public interface StockService {

    PageResult page(StockPageQueryDTO queryDTO);

    void update(StockUpdateDTO updateDTO);

    List<StockPageVO> warning(StockPageQueryDTO queryDTO);

    void checkAndDeductForOrder(Long storeId, Map<Integer, Integer> dishCountMap);

    void rollbackForOrder(Integer orderId);
}
