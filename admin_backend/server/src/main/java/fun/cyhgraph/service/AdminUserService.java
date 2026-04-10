package fun.cyhgraph.service;

import fun.cyhgraph.dto.member.MemberCloseDTO;
import fun.cyhgraph.dto.member.MemberOpenDTO;
import fun.cyhgraph.dto.member.MemberUpdateDTO;
import fun.cyhgraph.dto.user.AdminUserPageQueryDTO;
import fun.cyhgraph.dto.user.AdminUserSaveDTO;
import fun.cyhgraph.dto.user.AdminUserStatusDTO;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.result.PageResult;

public interface AdminUserService {

    PageResult pageQuery(AdminUserPageQueryDTO queryDTO);

    User getById(Integer id);

    void add(AdminUserSaveDTO dto);

    void update(AdminUserSaveDTO dto);

    void updateStatus(AdminUserStatusDTO dto);

    /**
     * 不做物理删除，统一按禁用处理
     */
    void disableByDelete(Integer id);

    void openMember(MemberOpenDTO dto);

    void updateMember(MemberUpdateDTO dto);

    void closeMember(MemberCloseDTO dto);
}

