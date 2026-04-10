package fun.cyhgraph.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.member.MemberCloseDTO;
import fun.cyhgraph.dto.member.MemberOpenDTO;
import fun.cyhgraph.dto.member.MemberUpdateDTO;
import fun.cyhgraph.dto.user.AdminUserPageQueryDTO;
import fun.cyhgraph.dto.user.AdminUserSaveDTO;
import fun.cyhgraph.dto.user.AdminUserStatusDTO;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.UserMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.AdminUserService;
import fun.cyhgraph.service.MemberService;
import fun.cyhgraph.utils.RoleUtil;
import fun.cyhgraph.vo.user.AdminUserPageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private MemberService memberService;

    @Override
    public PageResult pageQuery(AdminUserPageQueryDTO queryDTO) {
        ensureChairman();
        if (queryDTO == null) {
            queryDTO = new AdminUserPageQueryDTO();
        }
        if (queryDTO.getPage() == null || queryDTO.getPage() <= 0) {
            queryDTO.setPage(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() <= 0) {
            queryDTO.setPageSize(10);
        }
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<User> page = userMapper.pageAdminUser(queryDTO, LocalDateTime.now());
        List<AdminUserPageVO> records = new ArrayList<>();
        for (User user : page.getResult()) {
            AdminUserPageVO vo = new AdminUserPageVO();
            BeanUtils.copyProperties(user, vo);
            vo.setMemberStatusText(memberService.isValidMember(user) ? "是" : "否");
            records.add(vo);
        }
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public User getById(Integer id) {
        ensureChairman();
        if (id == null) {
            throw new BaseException("用户ID不能为空");
        }
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        return user;
    }

    @Override
    public void add(AdminUserSaveDTO dto) {
        ensureChairman();
        if (dto == null) {
            throw new BaseException("请求参数不能为空");
        }
        if (!StringUtils.hasText(dto.getUsername())) {
            throw new BaseException("用户名不能为空");
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BaseException("密码不能为空");
        }
        if (userMapper.getByUsername(dto.getUsername()) != null) {
            throw new BaseException("用户名已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setPic(dto.getPic());
        user.setStatus(1);
        user.setIsMember(0);
        user.setMemberLevel(0);
        user.setOpenid("ADMIN_CREATE_" + DigestUtils.md5DigestAsHex((dto.getUsername() + now).getBytes()));
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
    }

    @Override
    public void update(AdminUserSaveDTO dto) {
        ensureChairman();
        if (dto == null || dto.getId() == null) {
            throw new BaseException("用户ID不能为空");
        }
        User dbUser = userMapper.getById(dto.getId());
        if (dbUser == null) {
            throw new BaseException("用户不存在");
        }
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setPic(dto.getPic());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateStatus(AdminUserStatusDTO dto) {
        ensureChairman();
        if (dto == null || dto.getId() == null) {
            throw new BaseException("用户ID不能为空");
        }
        if (dto.getStatus() == null || (dto.getStatus() != 0 && dto.getStatus() != 1)) {
            throw new BaseException("状态仅支持0或1");
        }
        User dbUser = userMapper.getById(dto.getId());
        if (dbUser == null) {
            throw new BaseException("用户不存在");
        }
        userMapper.updateUserStatus(dto.getId(), dto.getStatus(), LocalDateTime.now());
    }

    @Override
    public void disableByDelete(Integer id) {
        ensureChairman();
        if (id == null) {
            throw new BaseException("用户ID不能为空");
        }
        User dbUser = userMapper.getById(id);
        if (dbUser == null) {
            throw new BaseException("用户不存在");
        }
        userMapper.updateUserStatus(id, 0, LocalDateTime.now());
    }

    @Override
    public void openMember(MemberOpenDTO dto) {
        ensureChairman();
        memberService.open(dto);
    }

    @Override
    public void updateMember(MemberUpdateDTO dto) {
        ensureChairman();
        memberService.update(dto);
    }

    @Override
    public void closeMember(MemberCloseDTO dto) {
        ensureChairman();
        memberService.close(dto);
    }

    private void ensureChairman() {
        Integer currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BaseException("未获取到当前登录员工");
        }
        Employee employee = employeeMapper.getById(currentId);
        if (employee == null) {
            throw new BaseException("当前登录员工不存在");
        }
        if (!RoleUtil.isChairman(employee.getRole())) {
            throw new BaseException("仅董事长可管理用户会员");
        }
    }
}

