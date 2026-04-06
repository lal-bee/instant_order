package fun.cyhgraph.service.serviceImpl;

import fun.cyhgraph.entity.Store;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.StoreMapper;
import fun.cyhgraph.service.StoreService;
import fun.cyhgraph.utils.RoleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public void add(Store store) {
        ensureChairman();
        validateManagerEmployee(store.getManagerEmployeeId());
        if (store.getStatus() == null) {
            store.setStatus(1);
        }
        storeMapper.add(store);
    }

    @Override
    public void update(Store store) {
        ensureChairman();
        validateManagerEmployee(store.getManagerEmployeeId());
        storeMapper.update(store);
    }

    @Override
    public void delete(Long id) {
        ensureChairman();
        storeMapper.delete(id);
    }

    @Override
    public Store getById(Long id) {
        Employee current = getCurrentEmployee();
        if (RoleUtil.isChairman(current.getRole())) {
            return storeMapper.getById(id);
        }
        if (current.getStoreId() != null && id.equals(current.getStoreId())) {
            return storeMapper.getById(id);
        }
        throw new BaseException("无权限查看其他分店信息");
    }

    @Override
    public List<Store> getList() {
        Employee current = getCurrentEmployee();
        if (RoleUtil.isChairman(current.getRole())) {
            return storeMapper.getListWithDetail();
        }
        if (current.getStoreId() == null) {
            return List.of();
        }
        Store myStore = storeMapper.getById(current.getStoreId());
        return myStore == null ? List.of() : List.of(myStore);
    }

    @Override
    public void onOff(Long id) {
        ensureChairman();
        storeMapper.onOff(id);
    }

    private void ensureChairman() {
        Employee current = getCurrentEmployee();
        if (!RoleUtil.isChairman(current.getRole())) {
            throw new BaseException("只有董事长可以管理分店");
        }
    }

    private void validateManagerEmployee(Integer managerEmployeeId) {
        if (managerEmployeeId == null) {
            return;
        }
        Employee manager = employeeMapper.getById(managerEmployeeId);
        if (manager == null) {
            throw new BaseException("关联店长不存在");
        }
        if (!RoleUtil.isManager(manager.getRole())) {
            throw new BaseException("所选员工不是店长角色");
        }
    }

    private Employee getCurrentEmployee() {
        Integer currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BaseException("未登录");
        }
        Employee employee = employeeMapper.getById(currentId);
        if (employee == null) {
            throw new BaseException("当前员工不存在");
        }
        return employee;
    }
}
