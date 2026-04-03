package fun.cyhgraph.service.serviceImpl;

import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.StoreMenuConfigDTO;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.entity.Store;
import fun.cyhgraph.entity.StoreDish;
import fun.cyhgraph.enumeration.RoleEnum;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.StoreDishMapper;
import fun.cyhgraph.mapper.StoreMapper;
import fun.cyhgraph.service.StoreMenuService;
import fun.cyhgraph.vo.StoreDishConfigVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StoreMenuServiceImpl implements StoreMenuService {

    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private StoreDishMapper storeDishMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public List<StoreDishConfigVO> getStoreDishConfigList(Long storeId) {
        checkStorePermission(storeId);
        Store store = storeMapper.getById(storeId);
        if (store == null) {
            throw new BaseException("门店不存在");
        }
        List<Dish> dishList = dishMapper.getByHeadquartersId(store.getHeadquartersId());
        List<Integer> selectedDishIds = storeDishMapper.getDishIdsByStoreId(storeId);
        Set<Integer> selectedSet = new HashSet<>(selectedDishIds);
        List<StoreDishConfigVO> result = new ArrayList<>();
        for (Dish dish : dishList) {
            StoreDishConfigVO vo = new StoreDishConfigVO();
            vo.setDishId(dish.getId());
            vo.setName(dish.getName());
            vo.setPic(dish.getPic());
            vo.setPrice(dish.getPrice());
            vo.setDetail(dish.getDetail());
            vo.setCategoryId(dish.getCategoryId());
            vo.setOnShelf(selectedSet.contains(dish.getId()) ? 1 : 0);
            result.add(vo);
        }
        return result;
    }

    @Override
    public void configStoreMenu(StoreMenuConfigDTO storeMenuConfigDTO) {
        Long storeId = storeMenuConfigDTO.getStoreId();
        checkStorePermission(storeId);
        Store store = storeMapper.getById(storeId);
        if (store == null) {
            throw new BaseException("门店不存在");
        }
        storeDishMapper.deleteByStoreId(storeId);
        if (storeMenuConfigDTO.getDishIds() == null || storeMenuConfigDTO.getDishIds().isEmpty()) {
            return;
        }
        List<Dish> standardDishList = dishMapper.getByHeadquartersId(store.getHeadquartersId());
        Set<Integer> standardDishIdSet = new HashSet<>();
        for (Dish dish : standardDishList) {
            standardDishIdSet.add(dish.getId());
        }
        for (Integer dishId : storeMenuConfigDTO.getDishIds()) {
            if (!standardDishIdSet.contains(dishId)) {
                continue;
            }
            StoreDish storeDish = new StoreDish();
            storeDish.setStoreId(storeId);
            storeDish.setDishId(dishId);
            storeDish.setStatus(1);
            storeDishMapper.add(storeDish);
        }
    }

    private void checkStorePermission(Long storeId) {
        Integer currentEmployeeId = BaseContext.getCurrentId();
        Employee current = employeeMapper.getById(currentEmployeeId);
        if (current == null) {
            throw new BaseException("当前登录员工不存在");
        }
        if (RoleEnum.CHAIRMAN.name().equals(current.getRole())) {
            return;
        }
        if (RoleEnum.MANAGER.name().equals(current.getRole()) && storeId.equals(current.getStoreId())) {
            return;
        }
        throw new BaseException("无权限操作该门店菜单");
    }
}
