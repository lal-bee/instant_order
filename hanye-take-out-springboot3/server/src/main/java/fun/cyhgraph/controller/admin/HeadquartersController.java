package fun.cyhgraph.controller.admin;

import fun.cyhgraph.entity.Headquarters;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.HeadquartersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/headquarters")
@Slf4j
public class HeadquartersController {

    @Autowired
    private HeadquartersService headquartersService;

    @GetMapping("/list")
    public Result<List<Headquarters>> getList() {
        return Result.success(headquartersService.getList());
    }

    /**
     * 分店新增/编辑下拉专用：返回可选总店（默认启用状态）
     */
    @GetMapping("/options")
    public Result<List<Headquarters>> getOptions() {
        return Result.success(headquartersService.getEnabledOptions());
    }

    @GetMapping("/{id}")
    public Result<Headquarters> getById(@PathVariable Long id) {
        return Result.success(headquartersService.getById(id));
    }

    @PostMapping
    public Result add(@RequestBody Headquarters headquarters) {
        headquartersService.add(headquarters);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody Headquarters headquarters) {
        headquartersService.update(headquarters);
        return Result.success();
    }

    @PutMapping("/status/{id}")
    public Result onOff(@PathVariable Long id) {
        headquartersService.onOff(id);
        return Result.success();
    }
}
