package fun.cyhgraph.controller.admin;

import fun.cyhgraph.entity.Store;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/store")
@Slf4j
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping("/list")
    public Result<List<Store>> getList() {
        return Result.success(storeService.getList());
    }

    @GetMapping("/{id}")
    public Result<Store> getById(@PathVariable Long id) {
        return Result.success(storeService.getById(id));
    }

    @PostMapping
    public Result add(@RequestBody Store store) {
        storeService.add(store);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody Store store) {
        storeService.update(store);
        return Result.success();
    }

    @PutMapping("/status/{id}")
    public Result onOff(@PathVariable Long id) {
        storeService.onOff(id);
        return Result.success();
    }
}
