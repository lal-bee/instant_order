package fun.cyhgraph.controller.admin;

import fun.cyhgraph.dto.member.MemberCloseDTO;
import fun.cyhgraph.dto.member.MemberOpenDTO;
import fun.cyhgraph.dto.member.MemberPageQueryDTO;
import fun.cyhgraph.dto.member.MemberUpdateDTO;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/page")
    public Result<PageResult> page(MemberPageQueryDTO queryDTO) {
        return Result.success(memberService.page(queryDTO));
    }

    @PutMapping("/open")
    public Result<Void> open(@RequestBody MemberOpenDTO dto) {
        memberService.open(dto);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody MemberUpdateDTO dto) {
        memberService.update(dto);
        return Result.success();
    }

    @PutMapping("/close")
    public Result<Void> close(@RequestBody MemberCloseDTO dto) {
        memberService.close(dto);
        return Result.success();
    }
}

