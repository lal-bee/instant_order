package fun.cyhgraph.controller.admin;

import fun.cyhgraph.dto.StockPageQueryDTO;
import fun.cyhgraph.dto.StockUpdateDTO;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.StockService;
import fun.cyhgraph.vo.StockPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/page")
    public Result<PageResult> page(StockPageQueryDTO queryDTO) {
        return Result.success(stockService.page(queryDTO));
    }

    @PutMapping("/update")
    public Result update(@RequestBody StockUpdateDTO updateDTO) {
        stockService.update(updateDTO);
        return Result.success();
    }

    @GetMapping("/warning")
    public Result<List<StockPageVO>> warning(StockPageQueryDTO queryDTO) {
        return Result.success(stockService.warning(queryDTO));
    }
}
