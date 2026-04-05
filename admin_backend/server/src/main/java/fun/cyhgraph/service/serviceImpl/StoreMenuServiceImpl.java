package fun.cyhgraph.service.serviceImpl;

import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.constant.StatusConstant;
import fun.cyhgraph.dto.StoreMenuConfigDTO;
import fun.cyhgraph.dto.StoreSpecialDishDTO;
import fun.cyhgraph.entity.Category;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.entity.Store;
import fun.cyhgraph.entity.StoreDish;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.CategoryMapper;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.StoreDishMapper;
import fun.cyhgraph.mapper.StoreMapper;
import fun.cyhgraph.service.StoreMenuService;
import fun.cyhgraph.utils.RoleUtil;
import fun.cyhgraph.vo.StoreDishConfigVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<StoreDishConfigVO> getStoreDishConfigList(Long storeId) {
        Employee current = getCurrentEmployee();
        checkStoreViewPermission(storeId, current);
        Store store = storeMapper.getById(storeId);
        if (store == null) {
            throw new BaseException("门店不存在");
        }
        List<Dish> standardDishList = dishMapper.getStandardByHeadquartersId(store.getHeadquartersId());
        List<Dish> specialDishList = dishMapper.getSpecialByStoreId(storeId);
        List<Integer> selectedDishIds = storeDishMapper.getDishIdsByStoreId(storeId);
        Set<Integer> selectedSet = new HashSet<>(selectedDishIds);
        List<StoreDishConfigVO> result = new ArrayList<>();
        appendDishVO(result, standardDishList, "STANDARD", selectedSet);
        appendDishVO(result, specialDishList, "SPECIAL", selectedSet);
        return result;
    }

    @Override
    public List<Category> getStoreMenuCategoryList() {
        return categoryMapper.getList(1);
    }

    @Override
    public void configStoreMenu(StoreMenuConfigDTO storeMenuConfigDTO) {
        Long storeId = storeMenuConfigDTO.getStoreId();
        Employee current = getCurrentEmployee();
        checkStoreWritePermission(storeId, current);
        Store store = storeMapper.getById(storeId);
        if (store == null) {
            throw new BaseException("门店不存在");
        }
        storeDishMapper.deleteByStoreId(storeId);
        if (storeMenuConfigDTO.getDishIds() == null || storeMenuConfigDTO.getDishIds().isEmpty()) {
            return;
        }
        List<Dish> standardDishList = dishMapper.getStandardByHeadquartersId(store.getHeadquartersId());
        List<Dish> specialDishList = dishMapper.getSpecialByStoreId(storeId);
        Set<Integer> allowedDishIdSet = new HashSet<>();
        for (Dish dish : standardDishList) {
            allowedDishIdSet.add(dish.getId());
        }
        for (Dish dish : specialDishList) {
            allowedDishIdSet.add(dish.getId());
        }
        for (Integer dishId : storeMenuConfigDTO.getDishIds()) {
            if (!allowedDishIdSet.contains(dishId)) {
                continue;
            }
            StoreDish storeDish = new StoreDish();
            storeDish.setStoreId(storeId);
            storeDish.setDishId(dishId);
            storeDish.setStatus(1);
            storeDishMapper.add(storeDish);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSpecialDish(StoreSpecialDishDTO storeSpecialDishDTO) {
        validateSpecialDishPayload(storeSpecialDishDTO, null);
        Employee current = getCurrentEmployee();
        Long targetStoreId = resolveTargetStoreIdForWrite(storeSpecialDishDTO.getStoreId(), current);
        checkStoreWritePermission(targetStoreId, current);
        Store store = storeMapper.getById(targetStoreId);
        if (store == null) {
            throw new BaseException("门店不存在");
        }
        validateCategoryForSpecialDish(storeSpecialDishDTO.getCategoryId());

        Dish dish = new Dish();
        dish.setName(storeSpecialDishDTO.getName().trim());
        dish.setPic(storeSpecialDishDTO.getPic());
        dish.setDetail(storeSpecialDishDTO.getDetail().trim());
        dish.setPrice(storeSpecialDishDTO.getPrice());
        dish.setCategoryId(storeSpecialDishDTO.getCategoryId());
        dish.setStatus(1);
        dish.setStoreId(targetStoreId);
        dish.setHeadquartersId(store.getHeadquartersId());
        dishMapper.addDish(dish);

        StoreDish storeDish = new StoreDish();
        storeDish.setStoreId(targetStoreId);
        storeDish.setDishId(dish.getId());
        storeDish.setStatus(1);
        storeDishMapper.add(storeDish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpecialDish(Integer dishId, StoreSpecialDishDTO storeSpecialDishDTO) {
        if (storeSpecialDishDTO == null) {
            throw new BaseException("请求体不能为空");
        }
        Employee current = getCurrentEmployee();
        Dish origin = dishMapper.getById(dishId);
        if (origin == null) {
            throw new BaseException("菜品不存在");
        }
        if (origin.getStoreId() == null) {
            throw new BaseException("标准菜品不允许在门店菜单中修改");
        }
        checkStoreWritePermission(origin.getStoreId(), current);
        if (storeSpecialDishDTO.getStoreId() != null && !storeSpecialDishDTO.getStoreId().equals(origin.getStoreId())) {
            throw new BaseException("门店特色菜不允许跨门店修改归属");
        }
        validateSpecialDishPayload(storeSpecialDishDTO, dishId);
        validateCategoryForSpecialDish(storeSpecialDishDTO.getCategoryId());

        Dish updateDish = new Dish();
        updateDish.setId(dishId);
        updateDish.setName(storeSpecialDishDTO.getName().trim());
        updateDish.setPic(storeSpecialDishDTO.getPic());
        updateDish.setDetail(storeSpecialDishDTO.getDetail().trim());
        updateDish.setPrice(storeSpecialDishDTO.getPrice());
        updateDish.setCategoryId(storeSpecialDishDTO.getCategoryId());
        updateDish.setStoreId(origin.getStoreId());
        dishMapper.update(updateDish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpecialDish(Integer dishId) {
        Employee current = getCurrentEmployee();
        Dish origin = dishMapper.getById(dishId);
        if (origin == null) {
            return;
        }
        if (origin.getStoreId() == null) {
            throw new BaseException("标准菜品不允许在门店菜单中删除");
        }
        checkStoreWritePermission(origin.getStoreId(), current);
        dishMapper.deleteById(dishId);
        storeDishMapper.deleteByDishId(dishId);
    }

    private void appendDishVO(List<StoreDishConfigVO> result, List<Dish> dishList, String dishType, Set<Integer> selectedSet) {
        for (Dish dish : dishList) {
            StoreDishConfigVO vo = new StoreDishConfigVO();
            vo.setDishId(dish.getId());
            vo.setName(dish.getName());
            vo.setPic(dish.getPic());
            vo.setPrice(dish.getPrice());
            vo.setDetail(dish.getDetail());
            vo.setCategoryId(dish.getCategoryId());
            vo.setDishType(dishType);
            vo.setOnShelf(selectedSet.contains(dish.getId()) ? 1 : 0);
            result.add(vo);
        }
    }

    private Employee getCurrentEmployee() {
        Integer currentEmployeeId = BaseContext.getCurrentId();
        if (currentEmployeeId == null) {
            throw new BaseException("未获取到当前登录员工");
        }
        Employee current = employeeMapper.getById(currentEmployeeId);
        if (current == null) {
            throw new BaseException("当前登录员工不存在");
        }
        return current;
    }

    private void checkStoreViewPermission(Long storeId, Employee current) {
        if (RoleUtil.isChairman(current.getRole())) {
            return;
        }
        if ((RoleUtil.isManager(current.getRole()) || RoleUtil.isEmployee(current.getRole()))
                && current.getStoreId() != null
                && storeId.equals(current.getStoreId())) {
            return;
        }
        throw new BaseException("无权限查看该门店菜单");
    }

    private void checkStoreWritePermission(Long storeId, Employee current) {
        if (RoleUtil.isChairman(current.getRole())) {
            return;
        }
        if (RoleUtil.isManager(current.getRole()) && current.getStoreId() != null && storeId.equals(current.getStoreId())) {
            return;
        }
        throw new BaseException("无权限操作该门店菜单");
    }

    private Long resolveTargetStoreIdForWrite(Long requestStoreId, Employee current) {
        if (RoleUtil.isChairman(current.getRole())) {
            if (requestStoreId == null) {
                throw new BaseException("董事长新增门店特色菜时必须指定门店");
            }
            return requestStoreId;
        }
        if (RoleUtil.isManager(current.getRole())) {
            if (current.getStoreId() == null) {
                throw new BaseException("当前店长未绑定分店");
            }
            if (requestStoreId != null && !requestStoreId.equals(current.getStoreId())) {
                throw new BaseException("店长只能操作自己门店的特色菜");
            }
            return current.getStoreId();
        }
        throw new BaseException("普通员工无权限操作门店特色菜");
    }

    private void validateCategoryForSpecialDish(Integer categoryId) {
        if (categoryId == null) {
            throw new BaseException("分类不能为空");
        }
        Category category = categoryMapper.getById(categoryId);
        if (category == null) {
            throw new BaseException("分类不存在");
        }
        if (!StatusConstant.ENABLE.equals(category.getStatus())) {
            throw new BaseException("分类已停用，请选择可用分类");
        }
    }

    private void validateSpecialDishPayload(StoreSpecialDishDTO storeSpecialDishDTO, Integer excludeDishId) {
        if (storeSpecialDishDTO == null) {
            throw new BaseException("请求体不能为空");
        }
        if (!StringUtils.hasText(storeSpecialDishDTO.getName())) {
            throw new BaseException("菜品名称不能为空");
        }
        if (!StringUtils.hasText(storeSpecialDishDTO.getDetail())) {
            throw new BaseException("菜品描述不能为空");
        }
        BigDecimal price = storeSpecialDishDTO.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException("菜品价格必须大于等于0");
        }
        Integer sameNameCount = dishMapper.countByName(storeSpecialDishDTO.getName().trim(), excludeDishId);
        if (sameNameCount != null && sameNameCount > 0) {
            throw new BaseException("菜品名称已存在，请更换名称");
        }
    }
}
