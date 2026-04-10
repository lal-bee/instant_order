package fun.cyhgraph.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.member.MemberCloseDTO;
import fun.cyhgraph.dto.member.MemberOpenDTO;
import fun.cyhgraph.dto.member.MemberPageQueryDTO;
import fun.cyhgraph.dto.member.MemberUpdateDTO;
import fun.cyhgraph.dto.member.UserMemberOpenDTO;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.UserMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.MemberService;
import fun.cyhgraph.utils.RoleUtil;
import fun.cyhgraph.vo.member.MemberPageVO;
import fun.cyhgraph.vo.member.UserMemberInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public PageResult page(MemberPageQueryDTO queryDTO) {
        ensureChairman();
        if (queryDTO == null) {
            queryDTO = new MemberPageQueryDTO();
        }
        queryDTO.setPage(normalizePage(queryDTO.getPage()));
        queryDTO.setPageSize(normalizePageSize(queryDTO.getPageSize()));
        queryDTO.setNow(LocalDateTime.now());

        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<User> userPage = userMapper.pageMemberQuery(queryDTO);
        List<MemberPageVO> records = new ArrayList<>();
        for (User user : userPage.getResult()) {
            MemberPageVO vo = new MemberPageVO();
            BeanUtils.copyProperties(user, vo);
            if (isValidMember(user)) {
                vo.setMemberStatus(1);
                vo.setMemberStatusText("有效会员");
            } else if (isExpiredMember(user)) {
                vo.setMemberStatus(2);
                vo.setMemberStatusText("已过期");
            } else {
                vo.setMemberStatus(0);
                vo.setMemberStatusText("非会员");
            }
            records.add(vo);
        }
        return new PageResult(userPage.getTotal(), records);
    }

    @Override
    public void open(MemberOpenDTO dto) {
        ensureChairman();
        if (dto == null || dto.getUserId() == null) {
            throw new BaseException("用户ID不能为空");
        }
        validateMemberLevel(dto.getMemberLevel());
        User user = userMapper.getById(dto.getUserId());
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        userMapper.updateMemberInfo(dto.getUserId(), 1, dto.getMemberLevel(), dto.getMemberExpireTime(), LocalDateTime.now());
    }

    @Override
    public void update(MemberUpdateDTO dto) {
        ensureChairman();
        if (dto == null || dto.getUserId() == null) {
            throw new BaseException("用户ID不能为空");
        }
        validateMemberLevel(dto.getMemberLevel());
        User user = userMapper.getById(dto.getUserId());
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        userMapper.updateMemberInfo(dto.getUserId(), 1, dto.getMemberLevel(), dto.getMemberExpireTime(), LocalDateTime.now());
    }

    @Override
    public void close(MemberCloseDTO dto) {
        ensureChairman();
        if (dto == null || dto.getUserId() == null) {
            throw new BaseException("用户ID不能为空");
        }
        User user = userMapper.getById(dto.getUserId());
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        userMapper.updateMemberInfo(dto.getUserId(), 0, 0, null, LocalDateTime.now());
    }

    @Override
    public UserMemberInfoVO getCurrentUserMemberInfo() {
        Integer userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("未获取到当前用户");
        }
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        boolean validMember = isValidMember(user);
        String statusText;
        if (validMember) {
            statusText = "有效会员";
        } else if (isExpiredMember(user)) {
            statusText = "已过期";
        } else {
            statusText = "非会员";
        }
        return UserMemberInfoVO.builder()
                .isMember(validMember ? 1 : 0)
                .memberLevel(user.getMemberLevel() == null ? 0 : user.getMemberLevel())
                .memberExpireTime(user.getMemberExpireTime())
                .memberStatusText(statusText)
                .build();
    }

    @Override
    public UserMemberInfoVO openCurrentUserMember(UserMemberOpenDTO dto) {
        Integer userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("未获取到当前用户");
        }
        if (dto == null || dto.getPlanType() == null) {
            throw new BaseException("套餐类型不能为空");
        }
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (dto.getPlanType() == 1) {
            if (user.getIsMember() != null && user.getIsMember() == 1
                    && user.getMemberLevel() != null && user.getMemberLevel() == 2
                    && user.getMemberExpireTime() == null) {
                return getCurrentUserMemberInfo();
            }
            LocalDateTime base = user.getMemberExpireTime() != null && user.getMemberExpireTime().isAfter(now)
                    ? user.getMemberExpireTime() : now;
            userMapper.updateMemberInfo(userId, 1, 1, base.plusMonths(1), now);
            return getCurrentUserMemberInfo();
        }
        if (dto.getPlanType() == 2) {
            userMapper.updateMemberInfo(userId, 1, 2, null, now);
            return getCurrentUserMemberInfo();
        }
        throw new BaseException("套餐类型非法");
    }

    @Override
    public boolean isValidMember(Integer userId) {
        if (userId == null) {
            return false;
        }
        User user = userMapper.getById(userId);
        return isValidMember(user);
    }

    @Override
    public boolean isValidMember(User user) {
        if (user == null) {
            return false;
        }
        if (user.getIsMember() == null || user.getIsMember() != 1) {
            return false;
        }
        if (user.getMemberLevel() == null || user.getMemberLevel() <= 0) {
            return false;
        }
        return user.getMemberExpireTime() == null || user.getMemberExpireTime().isAfter(LocalDateTime.now());
    }

    private boolean isExpiredMember(User user) {
        if (user == null) {
            return false;
        }
        if (user.getIsMember() == null || user.getIsMember() != 1) {
            return false;
        }
        if (user.getMemberExpireTime() == null) {
            return false;
        }
        return !user.getMemberExpireTime().isAfter(LocalDateTime.now());
    }

    private void validateMemberLevel(Integer memberLevel) {
        if (memberLevel == null) {
            throw new BaseException("会员等级不能为空");
        }
        if (memberLevel != 1 && memberLevel != 2) {
            throw new BaseException("会员等级仅支持1或2");
        }
    }

    private void ensureChairman() {
        Integer employeeId = BaseContext.getCurrentId();
        if (employeeId == null) {
            throw new BaseException("未获取到当前登录员工");
        }
        Employee employee = employeeMapper.getById(employeeId);
        if (employee == null) {
            throw new BaseException("当前登录员工不存在");
        }
        if (!RoleUtil.isChairman(employee.getRole())) {
            throw new BaseException("仅董事长可管理会员");
        }
    }

    private int normalizePage(Integer page) {
        if (page == null || page <= 0) {
            return 1;
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
