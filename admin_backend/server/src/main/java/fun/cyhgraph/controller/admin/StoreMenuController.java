package fun.cyhgraph.controller.admin;

import fun.cyhgraph.dto.StoreSpecialDishDTO;
import fun.cyhgraph.dto.StoreDishStatusDTO;
import fun.cyhgraph.entity.Category;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.StoreMenuService;
import fun.cyhgraph.vo.StoreDishConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public Result<List<StoreDishConfigVO>> getStoreDishConfigList(@RequestParam(required = false) Long storeId) {
        return Result.success(storeMenuService.getStoreDishConfigList(storeId));
    }

    @GetMapping("/categories")
    public Result<List<Category>> getStoreMenuCategoryList() {
        return Result.success(storeMenuService.getStoreMenuCategoryList());
    }

    @PutMapping("/status")
    public Result updateDishStatus(@RequestBody StoreDishStatusDTO storeDishStatusDTO) {
        storeMenuService.updateDishStatus(storeDishStatusDTO);
        return Result.success();
    }

    @PostMapping("/special-dish")
    public Result addSpecialDish(@RequestBody StoreSpecialDishDTO storeSpecialDishDTO) {
        storeMenuService.addSpecialDish(storeSpecialDishDTO);
        return Result.success();
    }

    @PutMapping("/special-dish/{dishId}")
    public Result updateSpecialDish(@PathVariable Integer dishId, @RequestBody StoreSpecialDishDTO storeSpecialDishDTO) {
        storeMenuService.updateSpecialDish(dishId, storeSpecialDishDTO);
        return Result.success();
    }

    @DeleteMapping("/special-dish/{dishId}")
    public Result deleteSpecialDish(@PathVariable Integer dishId) {
        storeMenuService.deleteSpecialDish(dishId);
        return Result.success();
    }
}
