package fun.cyhgraph.service;

import fun.cyhgraph.dto.member.MemberCloseDTO;
import fun.cyhgraph.dto.member.MemberOpenDTO;
import fun.cyhgraph.dto.member.MemberPageQueryDTO;
import fun.cyhgraph.dto.member.MemberUpdateDTO;
import fun.cyhgraph.dto.member.UserMemberOpenDTO;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.vo.member.UserMemberInfoVO;

public interface MemberService {

    PageResult page(MemberPageQueryDTO queryDTO);

    void open(MemberOpenDTO dto);

    void update(MemberUpdateDTO dto);

    void close(MemberCloseDTO dto);

    UserMemberInfoVO getCurrentUserMemberInfo();

    UserMemberInfoVO openCurrentUserMember(UserMemberOpenDTO dto);

    boolean isValidMember(Integer userId);

    boolean isValidMember(User user);
}
