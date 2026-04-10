package fun.cyhgraph.controller.admin;

import fun.cyhgraph.dto.member.MemberCloseDTO;
import fun.cyhgraph.dto.member.MemberOpenDTO;
import fun.cyhgraph.dto.member.MemberUpdateDTO;
import fun.cyhgraph.dto.user.AdminUserPageQueryDTO;
import fun.cyhgraph.dto.user.AdminUserSaveDTO;
import fun.cyhgraph.dto.user.AdminUserStatusDTO;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminUserController")
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/page")
    public Result<PageResult> page(AdminUserPageQueryDTO queryDTO) {
        return Result.success(adminUserService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Integer id) {
        return Result.success(adminUserService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody AdminUserSaveDTO dto) {
        adminUserService.add(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody AdminUserSaveDTO dto) {
        adminUserService.update(dto);
        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody AdminUserStatusDTO dto) {
        adminUserService.updateStatus(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        adminUserService.disableByDelete(id);
        return Result.success();
    }

    @PutMapping("/member/open")
    public Result<Void> openMember(@RequestBody MemberOpenDTO dto) {
        adminUserService.openMember(dto);
        return Result.success();
    }

    @PutMapping("/member/update")
    public Result<Void> updateMember(@RequestBody MemberUpdateDTO dto) {
        adminUserService.updateMember(dto);
        return Result.success();
    }

    @PutMapping("/member/close")
    public Result<Void> closeMember(@RequestBody MemberCloseDTO dto) {
        adminUserService.closeMember(dto);
        return Result.success();
    }
}

