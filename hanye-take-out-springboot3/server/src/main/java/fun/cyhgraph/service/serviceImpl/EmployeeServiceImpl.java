package fun.cyhgraph.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.EmployeeDTO;
import fun.cyhgraph.dto.EmployeeFixPwdDTO;
import fun.cyhgraph.dto.EmployeeLoginDTO;
import fun.cyhgraph.dto.PageDTO;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.exception.EmployeeNotFoundException;
import fun.cyhgraph.exception.PasswordErrorException;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.StoreMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private StoreMapper storeMapper;

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String account = employeeLoginDTO.getAccount();
        String password = employeeLoginDTO.getPassword();
        // 先查数据库，看是否存在该账号
        Employee employee = employeeMapper.getByAccount(account);
        if (employee == null){
            throw new EmployeeNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 再将前端传过来的密码进行MD5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 和之前存进数据库的加密的密码进行比对，看看是否一样，不一样要抛异常
        if (!password.equals(employee.getPassword())){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        return employee;
    }

    /**
     * 注册/新增员工
     */
    public void register(EmployeeLoginDTO employeeLoginDTO) {
        // 先对用户的密码进行MD5加密，再存到数据库中
        String password = employeeLoginDTO.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        employeeLoginDTO.setPassword(password);

        Employee employee = new Employee();
        // 将userLoginDTO的属性拷贝到user中
        BeanUtils.copyProperties(employeeLoginDTO, employee);
        // 为user其他字段填充默认值(7-3=4个)
        employee.setName("员工");
        employee.setPhone("11111111111");
        employee.setAge(0);
        employee.setGender(1);
        if (employeeLoginDTO.getStoreId() == null) {
            throw new BaseException("注册员工必须绑定所属分店");
        }
        employee.setStoreId(employeeLoginDTO.getStoreId());
        employee.setRole("0");
        employee.setStatus(1);
        employee.setCreateUser(100); // 100表示员工自己注册，此时还不能拿到BaseContext的currentId，只能用100这个数字表示自己了
        employee.setUpdateUser(100);
        employeeMapper.regEmployee(employee);
    }

    /**
     * 根据id获取员工信息
     * @return
     */
    public Employee getEmployeeById(Integer id) {
        Employee current = getCurrentEmployee();
        if (isChairman(current.getRole())) {
            return employeeMapper.getById(id);
        }
        Employee target = employeeMapper.getById(id);
        if (target == null) {
            return null;
        }
        if (isManager(current.getRole())) {
            if (!current.getStoreId().equals(target.getStoreId())) {
                throw new BaseException("店长只能查看本店员工信息");
            }
            return target;
        }
        if (isEmployee(current.getRole()) && !current.getId().equals(target.getId())) {
            throw new BaseException("普通员工只能查看自己的信息");
        }
        return target;
    }

    /**
     * 员工分页查询
     * @return
     */
    public PageResult employeePageList(PageDTO pageDTO) {
        Employee current = getCurrentEmployee();
        if (isManager(current.getRole()) || isEmployee(current.getRole())) {
            pageDTO.setStoreId(current.getStoreId());
        }
        // 传分页参数给PageHelper自动处理，会自动加上limit和count(*)返回分页结果和总记录数
        PageHelper.startPage(pageDTO.getPage(), pageDTO.getPageSize());
        Page<Employee> pagelist = employeeMapper.pageQuery(pageDTO);
        return new PageResult(pagelist.getTotal(), pagelist.getResult());
    }

    /**
     * 修改员工
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO) {
        Employee current = getCurrentEmployee();
        Employee target = employeeMapper.getById(employeeDTO.getId());
        if (target == null) {
            throw new BaseException("员工不存在");
        }
        if (employeeDTO.getRole() != null && !employeeDTO.getRole().isEmpty()) {
            employeeDTO.setRole(normalizeRole(employeeDTO.getRole()));
        }
        checkUpdatePermission(current, target, employeeDTO);
        checkManagerUnique(employeeDTO.getStoreId(), employeeDTO.getRole(), employeeDTO.getId());

        // 缺少时间等字段，需要手动加入，否则Mapper里的autofill注解会为EmployeeDTO去setUpdateTime，然而根本没这个方法导致报错！
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employeeMapper.update(employee);
        if (isManager(employee.getRole())) {
            storeMapper.updateManager(employee.getStoreId(), employee.getId());
        }
    }

    /**
     * 删除员工
     */
    public void delete(Integer id) {
        Employee current = getCurrentEmployee();
        Employee target = employeeMapper.getById(id);
        if (target == null) {
            return;
        }
        if (isChairman(current.getRole())) {
            employeeMapper.delete(id);
            return;
        }
        if (isManager(current.getRole())) {
            if (!current.getStoreId().equals(target.getStoreId())) {
                throw new BaseException("店长只能删除本店员工");
            }
            employeeMapper.delete(id);
            return;
        }
        throw new BaseException("普通员工无权限删除员工");
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Integer id : ids) {
            delete(id);
        }
    }

    /**
     * 根据id修改员工状态
     * @param id
     */
    public void onOff(Integer id) {
        Employee current = getCurrentEmployee();
        Employee target = employeeMapper.getById(id);
        if (target == null) {
            return;
        }
        if (isChairman(current.getRole())) {
            employeeMapper.onOff(id);
            return;
        }
        if (isManager(current.getRole()) && current.getStoreId().equals(target.getStoreId())) {
            employeeMapper.onOff(id);
            return;
        }
        throw new BaseException("无权限操作员工状态");
    }

    /**
     * 管理员新增员工
     * @param employeeDTO
     */
    public void addEmployee(EmployeeDTO employeeDTO) {
        Employee current = getCurrentEmployee();
        if (employeeDTO.getStoreId() == null) {
            throw new BaseException("新增员工必须指定所属分店");
        }
        if (employeeDTO.getRole() == null || employeeDTO.getRole().isEmpty()) {
            employeeDTO.setRole("0");
        } else {
            employeeDTO.setRole(normalizeRole(employeeDTO.getRole()));
        }
        if (isManager(current.getRole()) && !current.getStoreId().equals(employeeDTO.getStoreId())) {
            throw new BaseException("店长只能新增本店员工");
        }
        if (isManager(current.getRole()) && !isEmployee(employeeDTO.getRole())) {
            throw new BaseException("店长只能新增普通员工");
        }
        if (isEmployee(current.getRole())) {
            throw new BaseException("普通员工无权限新增员工");
        }
        checkManagerUnique(employeeDTO.getStoreId(), employeeDTO.getRole(), null);

        // 先对用户的密码进行MD5加密，再存到数据库中
        String password = employeeDTO.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        employeeDTO.setPassword(password);
        // 创建employee对象，将employeeDTO的属性拷贝到employee中
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // 为user其他字段填充默认值
        employee.setStatus(1);
        employeeMapper.addEmployee(employee);
        if (isManager(employee.getRole())) {
            storeMapper.updateManager(employee.getStoreId(), employee.getId());
        }
    }

    /**
     * 修改密码
     * @param employeeFixPwdDTO
     */
    public void fixPwd(EmployeeFixPwdDTO employeeFixPwdDTO) {
        String oldPwd = employeeFixPwdDTO.getOldPwd();
        // 将前端传过来的旧密码进行MD5加密
        oldPwd = DigestUtils.md5DigestAsHex(oldPwd.getBytes());
        // 根据id查询当前账号信息
        Integer id = BaseContext.getCurrentId();
        Employee employee = employeeMapper.getById(id);
        // 和之前存进数据库的加密的密码进行比对，看看是否一样，不一样要抛异常
        if (!oldPwd.equals(employee.getPassword())){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        // 旧密码正确，将新密码加密后进行更新
        String newPwd = employeeFixPwdDTO.getNewPwd();
        String password = DigestUtils.md5DigestAsHex(newPwd.getBytes());
        employee.setPassword(password);
        employeeMapper.updatePwd(employee);
    }

    private Employee getCurrentEmployee() {
        Integer currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BaseException("未获取到当前登录员工");
        }
        Employee employee = employeeMapper.getById(currentId);
        if (employee == null) {
            throw new BaseException("当前登录员工不存在");
        }
        return employee;
    }

    private void checkManagerUnique(Long storeId, String role, Integer excludeId) {
        if (storeId == null || !isManager(role)) {
            return;
        }
        Integer managerCount = employeeMapper.countManagerByStore(storeId, excludeId == null ? 0 : excludeId);
        if (managerCount != null && managerCount > 0) {
            throw new BaseException("同一分店只能存在一个店长");
        }
    }

    private void checkUpdatePermission(Employee current, Employee target, EmployeeDTO employeeDTO) {
        if (isChairman(current.getRole())) {
            return;
        }
        if (isManager(current.getRole())) {
            if (!current.getStoreId().equals(target.getStoreId())) {
                throw new BaseException("店长只能修改本店员工");
            }
            if (employeeDTO.getRole() != null && !isEmployee(employeeDTO.getRole())) {
                throw new BaseException("店长只能维护普通员工信息");
            }
            if (employeeDTO.getStoreId() == null) {
                employeeDTO.setStoreId(target.getStoreId());
            }
            if (!target.getStoreId().equals(employeeDTO.getStoreId())) {
                throw new BaseException("店长不能把员工转移到其他门店");
            }
            return;
        }
        if (isEmployee(current.getRole())) {
            if (!current.getId().equals(target.getId())) {
                throw new BaseException("普通员工只能修改自己的数据");
            }
            employeeDTO.setStoreId(target.getStoreId());
            employeeDTO.setRole(target.getRole());
            return;
        }
        throw new BaseException("无权限修改员工");
    }

    private boolean isChairman(String role) {
        return roleLevel(role) == 2;
    }

    private boolean isManager(String role) {
        return roleLevel(role) == 1;
    }

    private boolean isEmployee(String role) {
        return roleLevel(role) == 0;
    }

    private int roleLevel(String role) {
        if (role == null) {
            return -1;
        }
        return switch (role) {
            case "2", "CHAIRMAN" -> 2;
            case "1", "MANAGER" -> 1;
            case "0", "EMPLOYEE" -> 0;
            default -> -1;
        };
    }

    private String normalizeRole(String role) {
        int level = roleLevel(role);
        if (level < 0) {
            return role;
        }
        return String.valueOf(level);
    }
}
