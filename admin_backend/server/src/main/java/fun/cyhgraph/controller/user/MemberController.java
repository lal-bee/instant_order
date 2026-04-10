package fun.cyhgraph.controller.user;

import fun.cyhgraph.dto.member.UserMemberOpenDTO;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.MemberService;
import fun.cyhgraph.vo.member.UserMemberInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userMemberController")
@RequestMapping("/user/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/info")
    public Result<UserMemberInfoVO> info() {
        return Result.success(memberService.getCurrentUserMemberInfo());
    }

    @PutMapping("/open")
    public Result<UserMemberInfoVO> open(@RequestBody UserMemberOpenDTO dto) {
        return Result.success(memberService.openCurrentUserMember(dto));
    }
}
