package fun.cyhgraph.service.serviceImpl;

import fun.cyhgraph.constant.DishScopeConstant;
import fun.cyhgraph.constant.StatusConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.StoreDishStatusDTO;
import fun.cyhgraph.dto.StoreSpecialDishDTO;
import fun.cyhgraph.entity.Category;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.entity.Store;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class StoreMenuServiceImpl implements StoreMenuService {

    private static final String DISH_TYPE_STANDARD = "标准菜品";
    private static final String DISH_TYPE_SPECIAL = "特色菜品";

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
        ensureCanViewStoreMenu(current);
        List<Store> visibleStoreList = resolveVisibleStores(storeId, current);
        if (visibleStoreList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, String> categoryNameCache = new java.util.HashMap<>();
        List<StoreDishConfigVO> result = new ArrayList<>();
        for (Store store : visibleStoreList) {
            List<Dish> standardDishList = getStandardDishListByStore();
            List<Dish> specialDishList = dishMapper.getSpecialByStoreId(store.getId());

            appendDishVO(result, standardDishList, store, categoryNameCache, DISH_TYPE_STANDARD, current);
            appendDishVO(result, specialDishList, store, categoryNameCache, DISH_TYPE_SPECIAL, current);
        }
        return result;
    }

    @Override
    public List<Category> getStoreMenuCategoryList() {
        Employee current = getCurrentEmployee();
        ensureCanViewStoreMenu(current);
        return categoryMapper.getList(1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSpecialDish(StoreSpecialDishDTO storeSpecialDishDTO) {
        Employee current = getCurrentEmployee();
        Long targetStoreId = resolveTargetStoreIdForWrite(storeSpecialDishDTO.getStoreId(), current);
        validateSpecialDishPayload(storeSpecialDishDTO, null, DishScopeConstant.STORE_SPECIAL, targetStoreId);
        requireStore(targetStoreId);
        validateCategoryForSpecialDish(storeSpecialDishDTO.getCategoryId());

        Dish dish = new Dish();
        dish.setName(storeSpecialDishDTO.getName().trim());
        dish.setPic(storeSpecialDishDTO.getPic());
        dish.setDetail(storeSpecialDishDTO.getDetail().trim());
        dish.setPrice(storeSpecialDishDTO.getPrice());
        dish.setCategoryId(storeSpecialDishDTO.getCategoryId());
        dish.setStatus(StatusConstant.ENABLE);
        dish.setStoreId(targetStoreId);
        dish.setDishScope(DishScopeConstant.STORE_SPECIAL);
        dishMapper.addDish(dish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpecialDish(Integer dishId, StoreSpecialDishDTO storeSpecialDishDTO) {
        if (storeSpecialDishDTO == null) {
            throw new BaseException("请求体不能为空");
        }
        Employee current = getCurrentEmployee();
        if (RoleUtil.isEmployee(current.getRole())) {
            throw new BaseException("普通店员无编辑权限");
        }
        Dish origin = dishMapper.getById(dishId);
        if (origin == null) {
            throw new BaseException("菜品不存在");
        }
        if (RoleUtil.isChairman(current.getRole())) {
            Integer scope = isStandardDish(origin) ? DishScopeConstant.STANDARD : DishScopeConstant.STORE_SPECIAL;
            validateSpecialDishPayload(storeSpecialDishDTO, dishId, scope, origin.getStoreId());
        } else {
            if (origin.getStoreId() == null) {
                throw new BaseException("店长不能修改标准菜品");
            }
            ensureManagerOwnStoreOrChairman(origin.getStoreId(), current);
            if (storeSpecialDishDTO.getStoreId() != null && !storeSpecialDishDTO.getStoreId().equals(origin.getStoreId())) {
                throw new BaseException("无权操作其他门店数据");
            }
            validateSpecialDishPayload(storeSpecialDishDTO, dishId, DishScopeConstant.STORE_SPECIAL, origin.getStoreId());
        }

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
        if (RoleUtil.isEmployee(current.getRole())) {
            throw new BaseException("普通店员无编辑权限");
        }
        Dish origin = dishMapper.getById(dishId);
        if (origin == null) {
            return;
        }
        if (!RoleUtil.isChairman(current.getRole()) && origin.getStoreId() == null) {
            throw new BaseException("店长不能删除标准菜品");
        }
        ensureManagerOwnStoreOrChairman(origin.getStoreId(), current);
        dishMapper.deleteById(dishId);
        // 兼容历史数据：若此前给特色菜写过 store_dish，这里一并清理
        storeDishMapper.deleteByDishId(dishId);
    }

    @Override
    public void updateDishStatus(StoreDishStatusDTO storeDishStatusDTO) {
        if (storeDishStatusDTO == null) {
            throw new BaseException("请求体不能为空");
        }
        Long storeId = storeDishStatusDTO.getStoreId();
        Integer dishId = storeDishStatusDTO.getDishId();
        Integer status = storeDishStatusDTO.getStatus();
        if (storeId == null || dishId == null || status == null) {
            throw new BaseException("storeId、dishId、status不能为空");
        }
        if (!StatusConstant.ENABLE.equals(status) && !StatusConstant.UNABLE.equals(status)) {
            throw new BaseException("status参数非法");
        }

        Employee current = getCurrentEmployee();
        Dish dish = dishMapper.getById(dishId);
        if (dish == null) {
            throw new BaseException("菜品不存在");
        }
        requireStore(storeId);
        ensureCanUpdateDishStatus(current, dish, storeId);
        dishMapper.updateStatusById(dishId, status);
    }

    private void appendDishVO(List<StoreDishConfigVO> result,
                              List<Dish> dishList,
                              Store store,
                              Map<Integer, String> categoryNameCache,
                              String dishType,
                              Employee current) {
        for (Dish dish : dishList) {
            Integer status = resolveStatus(dish);
            StoreDishConfigVO vo = new StoreDishConfigVO();
            vo.setDishId(dish.getId());
            vo.setDishName(dish.getName());
            vo.setName(dish.getName());
            vo.setPic(dish.getPic());
            vo.setPrice(dish.getPrice());
            vo.setDetail(dish.getDetail());
            vo.setCategoryId(dish.getCategoryId());
            vo.setCategoryName(resolveCategoryName(dish.getCategoryId(), categoryNameCache));
            vo.setStatus(status);
            vo.setOnShelf(status);
            vo.setStoreId(store.getId());
            vo.setStoreName(store.getName());
            vo.setDishType(dishType);
            vo.setEditable(canEditDish(current, store.getId(), dishType));
            result.add(vo);
        }
    }

    private Integer resolveStatus(Dish dish) {
        if (dish.getStatus() == null) {
            return StatusConstant.ENABLE;
        }
        return dish.getStatus();
    }

    private List<Dish> getStandardDishListByStore() {
        return dishMapper.getAllStandardDish();
    }

    private String resolveCategoryName(Integer categoryId, Map<Integer, String> categoryNameCache) {
        if (categoryId == null) {
            return "";
        }
        if (categoryNameCache.containsKey(categoryId)) {
            return categoryNameCache.get(categoryId);
        }
        Category category = categoryMapper.getById(categoryId);
        String categoryName = category == null ? "" : category.getName();
        categoryNameCache.put(categoryId, categoryName);
        return categoryName;
    }

    private List<Store> resolveVisibleStores(Long requestStoreId, Employee current) {
        if (RoleUtil.isChairman(current.getRole())) {
            if (requestStoreId != null) {
                return Collections.singletonList(requireStore(requestStoreId));
            }
            return storeMapper.getList();
        }
        if (RoleUtil.isManager(current.getRole()) || RoleUtil.isEmployee(current.getRole())) {
            if (current.getStoreId() == null) {
                throw new BaseException("当前账号未绑定门店");
            }
            if (requestStoreId != null && !requestStoreId.equals(current.getStoreId())) {
                throw new BaseException("无权查看其他门店数据");
            }
            return Collections.singletonList(requireStore(current.getStoreId()));
        }
        throw new BaseException("角色非法，无法访问门店菜单");
    }

    private void ensureCanViewStoreMenu(Employee current) {
        if (!(RoleUtil.isChairman(current.getRole()) || RoleUtil.isManager(current.getRole()) || RoleUtil.isEmployee(current.getRole()))) {
            throw new BaseException("角色非法，无法访问门店菜单");
        }
    }

    private boolean canEditDish(Employee current, Long rowStoreId, String dishType) {
        if (RoleUtil.isChairman(current.getRole())) {
            return true;
        }
        if (RoleUtil.isManager(current.getRole())) {
            return current.getStoreId() != null
                    && current.getStoreId().equals(rowStoreId)
                    && DISH_TYPE_SPECIAL.equals(dishType);
        }
        return false;
    }

    private boolean isStandardDish(Dish dish) {
        if (dish == null) {
            return false;
        }
        if (DishScopeConstant.STANDARD.equals(dish.getDishScope())) {
            return true;
        }
        if (DishScopeConstant.STORE_SPECIAL.equals(dish.getDishScope())) {
            return false;
        }
        return dish.getStoreId() == null;
    }

    private void ensureCanUpdateDishStatus(Employee current, Dish dish, Long storeId) {
        if (RoleUtil.isEmployee(current.getRole())) {
            throw new BaseException("普通店员无编辑权限");
        }
        if (RoleUtil.isChairman(current.getRole())) {
            return;
        }
        if (RoleUtil.isManager(current.getRole())) {
            if (current.getStoreId() == null || !current.getStoreId().equals(storeId)) {
                throw new BaseException("无权操作其他门店数据");
            }
            if (isStandardDish(dish)) {
                throw new BaseException("店长不能管理标准菜品");
            }
            if (!storeId.equals(dish.getStoreId())) {
                throw new BaseException("无权操作其他门店数据");
            }
            return;
        }
        throw new BaseException("角色非法，无法执行写操作");
    }

    private void ensureManagerOwnStoreOrChairman(Long targetStoreId, Employee current) {
        if (RoleUtil.isChairman(current.getRole())) {
            return;
        }
        if (RoleUtil.isManager(current.getRole()) && current.getStoreId() != null && current.getStoreId().equals(targetStoreId)) {
            return;
        }
        if (RoleUtil.isEmployee(current.getRole())) {
            throw new BaseException("普通店员无编辑权限");
        }
        throw new BaseException("无权操作其他门店数据");
    }

    private Long resolveTargetStoreIdForWrite(Long requestStoreId, Employee current) {
        if (RoleUtil.isEmployee(current.getRole())) {
            throw new BaseException("普通店员无编辑权限");
        }
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
                throw new BaseException("无权操作其他门店数据");
            }
            return current.getStoreId();
        }
        throw new BaseException("角色非法，无法执行写操作");
    }

    private Store requireStore(Long storeId) {
        Store store = storeMapper.getById(storeId);
        if (store == null) {
            throw new BaseException("门店不存在");
        }
        return store;
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

    private void validateSpecialDishPayload(StoreSpecialDishDTO storeSpecialDishDTO,
                                            Integer excludeDishId,
                                            Integer dishScope,
                                            Long targetStoreId) {
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
        Integer sameNameCount = dishMapper.countByScopedName(
                storeSpecialDishDTO.getName().trim(),
                excludeDishId,
                dishScope,
                targetStoreId
        );
        if (sameNameCount != null && sameNameCount > 0) {
            if (DishScopeConstant.STANDARD.equals(dishScope)) {
                throw new BaseException("已存在同名标准菜，请更换名称");
            }
            throw new BaseException("当前门店已存在同名特色菜，请更换名称");
        }
    }
}
