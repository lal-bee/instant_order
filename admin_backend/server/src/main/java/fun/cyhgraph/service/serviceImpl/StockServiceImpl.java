package fun.cyhgraph.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.StockPageQueryDTO;
import fun.cyhgraph.dto.StockUpdateDTO;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.entity.Order;
import fun.cyhgraph.entity.OrderDetail;
import fun.cyhgraph.entity.Store;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.OrderDetailMapper;
import fun.cyhgraph.mapper.OrderMapper;
import fun.cyhgraph.mapper.StoreDishMapper;
import fun.cyhgraph.mapper.StoreMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.StockService;
import fun.cyhgraph.utils.RoleUtil;
import fun.cyhgraph.vo.StockPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockServiceImpl implements StockService {

    private static final Integer DEFAULT_STOCK = 100;
    private static final Integer DEFAULT_WARNING_STOCK = 10;

    @Autowired
    private StoreDishMapper storeDishMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public PageResult page(StockPageQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new StockPageQueryDTO();
        }
        int page = queryDTO.getPage() == null || queryDTO.getPage() < 1 ? 1 : queryDTO.getPage();
        int pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 ? 10 : queryDTO.getPageSize();
        Employee current = getCurrentEmployee();
        Long targetStoreId = resolveViewStoreId(current, queryDTO.getStoreId());
        initStockRowsForVisibleStore(current, targetStoreId);

        queryDTO.setStoreId(targetStoreId);
        PageHelper.startPage(page, pageSize);
        Page<StockPageVO> pageResult = (Page<StockPageVO>) storeDishMapper.pageQuery(queryDTO);
        return new PageResult(pageResult.getTotal(), pageResult.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StockUpdateDTO updateDTO) {
        if (updateDTO == null) {
            throw new BaseException("请求体不能为空");
        }
        if (updateDTO.getStoreId() == null || updateDTO.getDishId() == null) {
            throw new BaseException("storeId、dishId不能为空");
        }
        if (updateDTO.getStock() == null && updateDTO.getWarningStock() == null) {
            throw new BaseException("stock和warningStock不能同时为空");
        }
        if (updateDTO.getStock() != null && updateDTO.getStock() < 0) {
            throw new BaseException("库存不能小于0");
        }
        if (updateDTO.getWarningStock() != null && updateDTO.getWarningStock() < 0) {
            throw new BaseException("预警库存不能小于0");
        }
        Employee current = getCurrentEmployee();
        Long targetStoreId = resolveWriteStoreId(current, updateDTO.getStoreId());
        updateDTO.setStoreId(targetStoreId);
        ensureStoreExists(targetStoreId);
        int initCount = storeDishMapper.batchInitStockForStoreAndDishIds(
                targetStoreId,
                Collections.singletonList(updateDTO.getDishId()),
                DEFAULT_STOCK,
                DEFAULT_WARNING_STOCK
        );
        if (initCount < 1) {
            // 兼容行已存在且未插入成功的场景，继续走更新即可
        }
        if (updateDTO.getStock() != null) {
            int affected = storeDishMapper.updateStock(targetStoreId, updateDTO.getDishId(), updateDTO.getStock());
            if (affected < 1) {
                throw new BaseException("菜品不存在或不属于当前门店");
            }
        }
        if (updateDTO.getWarningStock() != null) {
            int affected = storeDishMapper.updateWarningStock(targetStoreId, updateDTO.getDishId(), updateDTO.getWarningStock());
            if (affected < 1) {
                throw new BaseException("菜品不存在或不属于当前门店");
            }
        }
    }

    @Override
    public List<StockPageVO> warning(StockPageQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new StockPageQueryDTO();
        }
        Employee current = getCurrentEmployee();
        Long targetStoreId = resolveViewStoreId(current, queryDTO.getStoreId());
        initStockRowsForVisibleStore(current, targetStoreId);
        queryDTO.setStoreId(targetStoreId);
        return storeDishMapper.warningList(queryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndDeductForOrder(Long storeId, Map<Integer, Integer> dishCountMap) {
        if (storeId == null || dishCountMap == null || dishCountMap.isEmpty()) {
            return;
        }
        ensureStoreExists(storeId);
        List<Integer> dishIds = new ArrayList<>(dishCountMap.keySet());
        storeDishMapper.batchInitStockForStoreAndDishIds(storeId, dishIds, DEFAULT_STOCK, DEFAULT_WARNING_STOCK);
        for (Map.Entry<Integer, Integer> entry : dishCountMap.entrySet()) {
            Integer dishId = entry.getKey();
            Integer deductNum = entry.getValue();
            if (dishId == null || deductNum == null || deductNum <= 0) {
                continue;
            }
            int affected = storeDishMapper.deductStock(storeId, dishId, deductNum);
            if (affected < 1) {
                throw new BaseException(MessageConstant.DISH_STOCK_NOT_ENOUGH);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackForOrder(Integer orderId) {
        if (orderId == null) {
            return;
        }
        Order order = orderMapper.getById(orderId);
        if (order == null || order.getStoreId() == null) {
            return;
        }
        Integer marked = orderMapper.markStockDeducted(orderId, 0, 1);
        if (marked == null || marked < 1) {
            return;
        }
        List<OrderDetail> detailList = orderDetailMapper.getById(orderId);
        if (detailList == null || detailList.isEmpty()) {
            return;
        }
        Map<Integer, Integer> dishCountMap = new LinkedHashMap<>();
        for (OrderDetail detail : detailList) {
            if (detail.getDishId() == null || detail.getNumber() == null || detail.getNumber() <= 0) {
                continue;
            }
            dishCountMap.merge(detail.getDishId(), detail.getNumber(), Integer::sum);
        }
        for (Map.Entry<Integer, Integer> entry : dishCountMap.entrySet()) {
            storeDishMapper.rollbackStock(order.getStoreId(), entry.getKey(), entry.getValue());
        }
    }

    private void initStockRowsForVisibleStore(Employee current, Long targetStoreId) {
        if (RoleUtil.isChairman(current.getRole())) {
            if (targetStoreId != null) {
                storeDishMapper.batchInitStockForStore(targetStoreId, DEFAULT_STOCK, DEFAULT_WARNING_STOCK);
                return;
            }
            List<Store> storeList = storeMapper.getList();
            for (Store store : storeList) {
                storeDishMapper.batchInitStockForStore(store.getId(), DEFAULT_STOCK, DEFAULT_WARNING_STOCK);
            }
            return;
        }
        if (current.getStoreId() != null) {
            storeDishMapper.batchInitStockForStore(current.getStoreId(), DEFAULT_STOCK, DEFAULT_WARNING_STOCK);
        }
    }

    private Long resolveViewStoreId(Employee current, Long requestStoreId) {
        if (RoleUtil.isChairman(current.getRole())) {
            return requestStoreId;
        }
        if (RoleUtil.isManager(current.getRole()) || RoleUtil.isEmployee(current.getRole())) {
            if (current.getStoreId() == null) {
                throw new BaseException("当前账号未绑定门店");
            }
            if (requestStoreId != null && !requestStoreId.equals(current.getStoreId())) {
                throw new BaseException("无权查看其他门店库存");
            }
            return current.getStoreId();
        }
        throw new BaseException("角色非法，无法访问库存模块");
    }

    private Long resolveWriteStoreId(Employee current, Long requestStoreId) {
        if (RoleUtil.isChairman(current.getRole())) {
            if (requestStoreId == null) {
                throw new BaseException("董事长修改库存时必须指定门店");
            }
            return requestStoreId;
        }
        if (RoleUtil.isManager(current.getRole())) {
            if (current.getStoreId() == null) {
                throw new BaseException("当前账号未绑定门店");
            }
            if (requestStoreId != null && !requestStoreId.equals(current.getStoreId())) {
                throw new BaseException("无权操作其他门店库存");
            }
            return current.getStoreId();
        }
        if (RoleUtil.isEmployee(current.getRole())) {
            throw new BaseException("普通店员无编辑权限");
        }
        throw new BaseException("角色非法，无法执行写操作");
    }

    private void ensureStoreExists(Long storeId) {
        Store store = storeMapper.getById(storeId);
        if (store == null) {
            throw new BaseException("门店不存在");
        }
    }

    private Employee getCurrentEmployee() {
        Integer employeeId = BaseContext.getCurrentId();
        if (employeeId == null) {
            throw new BaseException("未获取到当前登录员工");
        }
        Employee employee = employeeMapper.getById(employeeId);
        if (employee == null) {
            throw new BaseException("当前登录员工不存在");
        }
        return employee;
    }
}
