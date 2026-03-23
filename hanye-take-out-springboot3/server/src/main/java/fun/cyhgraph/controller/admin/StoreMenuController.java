package fun.cyhgraph.controller.admin;

import fun.cyhgraph.dto.StoreMenuConfigDTO;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.StoreMenuService;
import fun.cyhgraph.vo.StoreDishConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/store-menu")
@Slf4j
public class StoreMenuController {

    @Autowired
    private StoreMenuService storeMenuService;

    @GetMapping("/dishes")
    public Result<List<StoreDishConfigVO>> getStoreDishConfigList(@RequestParam Long storeId) {
        return Result.success(storeMenuService.getStoreDishConfigList(storeId));
    }

    @PutMapping("/config")
    public Result configStoreMenu(@RequestBody StoreMenuConfigDTO storeMenuConfigDTO) {
        storeMenuService.configStoreMenu(storeMenuConfigDTO);
        return Result.success();
    }
}
