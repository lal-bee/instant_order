package fun.cyhgraph.controller.user;

import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.TableInfoService;
import fun.cyhgraph.vo.UserTableVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userTableController")
@RequestMapping("/user/table")
@Slf4j
public class TableController {

    @Autowired
    private TableInfoService tableInfoService;

    @GetMapping("/{tableId}")
    public Result<UserTableVO> getById(@PathVariable Long tableId) {
        log.info("根据tableId查询餐桌信息:{}", tableId);
        return Result.success(tableInfoService.getUserTableById(tableId));
    }
}
