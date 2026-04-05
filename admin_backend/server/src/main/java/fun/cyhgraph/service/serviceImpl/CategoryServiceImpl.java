package fun.cyhgraph.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.CategoryDTO;
import fun.cyhgraph.dto.CategoryTypePageDTO;
import fun.cyhgraph.entity.Category;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.entity.Store;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.CategoryMapper;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.StoreMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.CategoryService;
import fun.cyhgraph.utils.RoleUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private StoreMapper storeMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    public void addCategory(CategoryDTO categoryDTO) {
        categoryDTO.setHeadquartersId(resolveHeadquartersId(categoryDTO.getHeadquartersId()));
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(1);  // 默认启用
        categoryMapper.add(category);
    }

    /**
     * 根据type条件分页查询
     * @param categoryTypePageDTO
     * @return
     */
    public PageResult getPageList(CategoryTypePageDTO categoryTypePageDTO) {
        PageHelper.startPage(categoryTypePageDTO.getPage(), categoryTypePageDTO.getPageSize());
        Page<Category> pagelist = categoryMapper.getPageList(categoryTypePageDTO);
        return new PageResult(pagelist.getTotal(), pagelist.getResult());
    }

    /**
     * 获取所有分类列表
     * @return
     */
    public List<Category> getList(Integer type) {
        List<Category> categoryList = categoryMapper.getList(type);
        return categoryList;
    }

    @Override
    public List<Category> getListByStoreId(Long storeId, Integer type) {
        List<Category> categoryList = categoryMapper.getListByStoreId(storeId);
        if (type == null) {
            return categoryList;
        }
        return categoryList.stream().filter(c -> type.equals(c.getType())).toList();
    }

    /**
     * 根据id查询分类
     * @param id
     * @return
     */
    public Category getById(Integer id) {
        return categoryMapper.getById(id);
    }

    /**
     * 更新分类信息
     * @param categoryDTO
     */
    public void udpate(CategoryDTO categoryDTO) {
        categoryDTO.setHeadquartersId(resolveHeadquartersId(categoryDTO.getHeadquartersId()));
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.update(category);
    }

    /**
     * 根据id删除分类
     * @param id
     */
    public void delete(Integer id) {
        categoryMapper.delete(id);
    }

    /**
     * 分类起售/停售
     */
    public void onOff(Integer id) {
        categoryMapper.onOff(id);
    }

    private Long resolveHeadquartersId(Long requestHeadquartersId) {
        Integer currentEmployeeId = BaseContext.getCurrentId();
        Employee current = employeeMapper.getById(currentEmployeeId);
        if (current == null) {
            throw new BaseException("当前登录员工不存在");
        }
        if (RoleUtil.isChairman(current.getRole())) {
            // 董事长支持全局操作：未指定总部时不按总部过滤
            return requestHeadquartersId;
        }
        if (current.getStoreId() == null) {
            return requestHeadquartersId;
        }
        Store store = storeMapper.getById(current.getStoreId());
        if (store == null) {
            return requestHeadquartersId;
        }
        return store.getHeadquartersId();
    }
}
